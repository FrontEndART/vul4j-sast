package net.onrc.onos.ofcontroller.floodlightlistener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openflow.protocol.OFPhysicalPort;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceListener;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.onrc.onos.graph.DBOperation;
import net.onrc.onos.graph.DBConnection;
import net.onrc.onos.graph.GraphDBManager;
import net.onrc.onos.datagrid.IDatagridService;
import net.onrc.onos.graph.IDBConnection;
import net.onrc.onos.graph.LocalTopologyEventListener;
import net.onrc.onos.ofcontroller.core.IDeviceStorage;
import net.onrc.onos.ofcontroller.core.ILinkStorage;
import net.onrc.onos.ofcontroller.core.IOFSwitchPortListener;
import net.onrc.onos.ofcontroller.core.ISwitchStorage;
import net.onrc.onos.ofcontroller.core.INetMapStorage.DM_OPERATION;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.ISwitchObject;
import net.onrc.onos.ofcontroller.core.ISwitchStorage.SwitchState;
import net.onrc.onos.ofcontroller.core.internal.DeviceStorageImpl;
import net.onrc.onos.ofcontroller.core.internal.LinkStorageImpl;
import net.onrc.onos.ofcontroller.core.internal.SwitchStorageImpl;
import net.onrc.onos.ofcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.onrc.onos.ofcontroller.linkdiscovery.ILinkDiscoveryService;
import net.onrc.onos.ofcontroller.linkdiscovery.LinkInfo;
import net.onrc.onos.ofcontroller.proxyarp.ArpMessage;
import net.onrc.onos.ofcontroller.topology.TopologyElement;
import net.onrc.onos.registry.controller.IControllerRegistryService;
import net.onrc.onos.registry.controller.IControllerRegistryService.ControlChangeCallback;
import net.onrc.onos.registry.controller.RegistryException;

public class NetworkGraphPublisher implements IDeviceListener,
					      IOFSwitchListener,
					      IOFSwitchPortListener,
					      ILinkDiscoveryListener,
					      IFloodlightModule,
					      INetworkGraphService {
	
	protected IDeviceStorage devStore;
	protected ISwitchStorage swStore;
	protected ILinkStorage linkStore;
	protected final static Logger log = LoggerFactory.getLogger(NetworkGraphPublisher.class);
	protected IDeviceService deviceService;
	protected IControllerRegistryService registryService;
	protected DBOperation op;
	
	protected static final String DBConfigFile = "dbconf";
        protected static final String GraphDBStore = "graph_db_store";
	protected static final String CleanupEnabled = "EnableCleanup";
	protected IThreadPoolService threadPool;
	protected IFloodlightProviderService floodlightProvider;
	
	protected final int CLEANUP_TASK_INTERVAL = 60; // 1 min
	protected SingletonTask cleanupTask;
	protected ILinkDiscoveryService linkDiscovery;

	protected IDatagridService datagridService;
	
	/**
     *  Cleanup and synch switch state from registry
     */
    protected class SwitchCleanup implements ControlChangeCallback, Runnable {
        @Override
        public void run() {
            try {
            	log.debug("Running cleanup thread");
		op = GraphDBManager.getDBOperation("ramcloud", "/tmp/ramcloudconf");
                switchCleanup();
            }
            catch (Exception e) {
                log.error("Error in cleanup thread", e);
            } finally {
            	op.close();
                    cleanupTask.reschedule(CLEANUP_TASK_INTERVAL,
                                              TimeUnit.SECONDS);
            }
        }

		@Override
		public void controlChanged(long dpid, boolean hasControl) {
			if (hasControl) {
				log.debug("got control to set inactive sw {}", HexString.toHexString(dpid));
				try {
					// Get the affected ports
					List<Short> ports = swStore.getPorts(HexString.toHexString(dpid));
					// Get the affected links
					List<Link> links = linkStore.getLinks(HexString.toHexString(dpid));
					// Get the affected reverse links
					List<Link> reverseLinks = linkStore.getReverseLinks(HexString.toHexString(dpid));
					links.addAll(reverseLinks);

					//if (swStore.updateSwitch(HexString.toHexString(dpid), SwitchState.INACTIVE, DM_OPERATION.UPDATE)) {
					if (swStore.deactivateSwitch(HexString.toHexString(dpid))) {
					    registryService.releaseControl(dpid);
					    
					    // TODO publish UPDATE_SWITCH event here
					    //
					    // NOTE: Here we explicitly send
					    // notification to remove the
					    // switch, because it is inactive
					    //
					    TopologyElement topologyElement =
						new TopologyElement(dpid);
					    datagridService.notificationSendTopologyElementRemoved(topologyElement);

					    // Publish: remove the affected ports
					    for (Short port : ports) {
						TopologyElement topologyElementPort =
						    new TopologyElement(dpid, port);
						datagridService.notificationSendTopologyElementRemoved(topologyElementPort);
					    }
					    // Publish: remove the affected links
					    for (Link link : links) {
						TopologyElement topologyElementLink =
						    new TopologyElement(link.getSrc(),
									link.getSrcPort(),
									link.getDst(),
									link.getDstPort());
						datagridService.notificationSendTopologyElementRemoved(topologyElementLink);
					    }
					}
				} catch (Exception e) {
	                log.error("Error in SwitchCleanup:controlChanged ", e);
				}
			}						
		}
    }

    protected void switchCleanup() {
    	//op.close();
	op = GraphDBManager.getDBOperation("ramcloud", "/tmp/ramcloudconf");
    	Iterable<ISwitchObject> switches = op.getActiveSwitches();
    	
    	log.debug("Checking for inactive switches");
    	// For each switch check if a controller exists in controller registry
    	for (ISwitchObject sw: switches) {
			//log.debug("checking if switch is inactive: {}", sw.getDPID());
			try {
				long dpid = HexString.toLong(sw.getDPID());
				String controller = registryService.getControllerForSwitch(dpid);
				if (controller == null) {
					log.debug("request Control to set inactive sw {}", HexString.toHexString(dpid));
					registryService.requestControl(dpid, new SwitchCleanup());
				//} else {
				//	log.debug("sw {} is controlled by controller: {}",HexString.toHexString(dpid),controller);
				}
			} catch (NumberFormatException e) {
				log.debug("Caught NumberFormatException trying to requestControl in cleanup thread");
				e.printStackTrace();
			} catch (RegistryException e) {
				log.debug("Caught RegistryException trying to requestControl in cleanup thread");
				e.printStackTrace();
			}
		}
    	op.close();
    }

	@Override
	public void linkDiscoveryUpdate(LDUpdate update) {
		Link lt = new Link(update.getSrc(),update.getSrcPort(),update.getDst(),update.getDstPort());
		//log.debug("{}:LinkDicoveryUpdate(): Updating Link {}",this.getClass(), lt);
		
		switch (update.getOperation()) {
			case LINK_REMOVED:
				log.debug("LinkDiscoveryUpdate(): Removing link {}", lt);
				
				if (linkStore.deleteLink(lt)) {
				    // TODO publish DELETE_LINK event here
				    TopologyElement topologyElement =
					new TopologyElement(update.getSrc(),
							    update.getSrcPort(),
							    update.getDst(),
							    update.getDstPort());
				    datagridService.notificationSendTopologyElementRemoved(topologyElement);
				}
				break;
			case LINK_UPDATED:
				log.debug("LinkDiscoveryUpdate(): Updating link {}", lt);
				
				LinkInfo linfo = linkStore.getLinkInfo(lt);
				// TODO update "linfo" using portState derived using "update"
				if (linkStore.update(lt, linfo, DM_OPERATION.UPDATE)) {
				    // TODO publish UPDATE_LINK event here
				    //
				    // TODO NOTE: Here we assume that updated
				    // link is UP.
				    //
				    TopologyElement topologyElement =
					new TopologyElement(update.getSrc(),
							    update.getSrcPort(),
							    update.getDst(),
							    update.getDstPort());
				    datagridService.notificationSendTopologyElementUpdated(topologyElement);
				}
				break;
			case LINK_ADDED:
				log.debug("LinkDiscoveryUpdate(): Adding link {}", lt);
				
				if (linkStore.addLink(lt)) {
				    // TODO publish ADD_LINK event here
				    TopologyElement topologyElement =
					new TopologyElement(update.getSrc(),
							    update.getSrcPort(),
							    update.getDst(),
							    update.getDstPort());
				    datagridService.notificationSendTopologyElementAdded(topologyElement);
				}
				break;
			default:
				break;
		}

	}

	@Override
	public void addedSwitch(IOFSwitch sw) {
		if (registryService.hasControl(sw.getId())) {
			if (swStore.addSwitch(sw)) {
			    // TODO publish ADD_SWITCH event here
			    TopologyElement topologyElement =
				new TopologyElement(sw.getId());
			    datagridService.notificationSendTopologyElementAdded(topologyElement);

			    // Publish: add the ports
			    // TODO: Add only ports that are UP?
			    for (OFPhysicalPort port : sw.getPorts()) {
				TopologyElement topologyElementPort =
				    new TopologyElement(sw.getId(),
							port.getPortNumber());
				datagridService.notificationSendTopologyElementAdded(topologyElementPort);
			    }

			    // Add all links that might be connected already
			    List<Link> links = linkStore.getLinks(HexString.toHexString(sw.getId()));
			    // Add all reverse links as well
			    List<Link> reverseLinks = linkStore.getReverseLinks(HexString.toHexString(sw.getId()));
			    links.addAll(reverseLinks);

			    // Publish: add the links
			    for (Link link : links) {
				TopologyElement topologyElementLink =
				    new TopologyElement(link.getSrc(),
							link.getSrcPort(),
							link.getDst(),
							link.getDstPort());
				datagridService.notificationSendTopologyElementAdded(topologyElementLink);
			    }
			}
		}
	}

	@Override
	public void removedSwitch(IOFSwitch sw) {
		/*
		if (registryService.hasControl(sw.getId())) {
			// Get the affected ports
			List<Short> ports = swStore.getPorts(HexString.toHexString(sw.getId()));
			// Get the affected links
			List<Link> links = linkStore.getLinks(HexString.toHexString(sw.getId()));
			// Get the affected reverse links
			List<Link> reverseLinks = linkStore.getReverseLinks(HexString.toHexString(sw.getId()));
			links.addAll(reverseLinks);

			if (swStore.deleteSwitch(sw.getStringId())) {
			    // TODO publish DELETE_SWITCH event here
			    TopologyElement topologyElement =
				new TopologyElement(sw.getId());
			    datagridService.notificationSendTopologyElementRemoved(topologyElement);

			    // Publish: remove the affected ports
			    for (Short port : ports) {
				TopologyElement topologyElementPort =
				    new TopologyElement(sw.getId(), port);
				datagridService.notificationSendTopologyElementRemoved(topologyElementPort);
			    }
			    // Publish: remove the affected links
			    for (Link link : links) {
				TopologyElement topologyElementLink =
				    new TopologyElement(link.getSrc(),
							link.getSrcPort(),
							link.getDst(),
							link.getDstPort());
				datagridService.notificationSendTopologyElementRemoved(topologyElementLink);
			    }
			}
		}
		*/
	}

	@Override
	public void switchPortChanged(Long switchId) {
		// NOTE: Event not needed here. This callback always coincide with add/remove callback.
	}


	@Override
	public void switchPortAdded(Long switchId, OFPhysicalPort port) {
		if (swStore.addPort(HexString.toHexString(switchId), port)) {
		    // TODO publish ADD_PORT event here
		    TopologyElement topologyElement =
			new TopologyElement(switchId, port.getPortNumber());
		    datagridService.notificationSendTopologyElementAdded(topologyElement);

		    // Add all links that might be connected already
		    List<Link> links = linkStore.getLinks(switchId, port.getPortNumber());
		    // Add all reverse links as well
		    List<Link> reverseLinks = linkStore.getReverseLinks(switchId, port.getPortNumber());
		    links.addAll(reverseLinks);

		    // Publish: add the links
		    for (Link link : links) {
			TopologyElement topologyElementLink =
			    new TopologyElement(link.getSrc(),
						link.getSrcPort(),
						link.getDst(),
						link.getDstPort());
			datagridService.notificationSendTopologyElementAdded(topologyElementLink);
		    }
		}
	}

	@Override
	public void switchPortRemoved(Long switchId, OFPhysicalPort port) {
		// Remove all links that might be connected already
		List<Link> links = linkStore.getLinks(switchId, port.getPortNumber());
		// Remove all reverse links as well
		List<Link> reverseLinks = linkStore.getReverseLinks(switchId, port.getPortNumber());
		links.addAll(reverseLinks);

		if (swStore.deletePort(HexString.toHexString(switchId), port.getPortNumber())) {
		    // TODO publish DELETE_PORT event here
		    TopologyElement topologyElement =
			new TopologyElement(switchId, port.getPortNumber());
		    datagridService.notificationSendTopologyElementRemoved(topologyElement);

		    // Publish: remove the links
		    for (Link link : links) {
			TopologyElement topologyElementLink =
			    new TopologyElement(link.getSrc(),
						link.getSrcPort(),
						link.getDst(),
						link.getDstPort());
			datagridService.notificationSendTopologyElementRemoved(topologyElementLink);
		    }
		}
	}

	@Override
	public String getName() {
		return "NetworkGraphPublisher";
	}

	@Override
	public void deviceAdded(IDevice device) {
		log.debug("{}:deviceAdded(): Adding device {}",this.getClass(),device.getMACAddressString());
		devStore.addDevice(device);
		for (int intIpv4Address : device.getIPv4Addresses()) {
			datagridService.sendArpRequest(
					ArpMessage.newReply(InetAddresses.fromInteger(intIpv4Address)));
		}
	}

	@Override
	public void deviceRemoved(IDevice device) {
		// TODO Auto-generated method stub
		devStore.removeDevice(device);
	}

	@Override
	public void deviceMoved(IDevice device) {
		devStore.changeDeviceAttachments(device);
	}

	@Override
	public void deviceIPV4AddrChanged(IDevice device) {
		devStore.changeDeviceIPv4Address(device);
	}

	@Override
	public void deviceVlanChanged(IDevice device) {
		// TODO Auto-generated method stub
	}
	

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l =
		    new ArrayList<Class<? extends IFloodlightService>>();
		l.add(INetworkGraphService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>,
		    IFloodlightService> m =
		    new HashMap<Class<? extends IFloodlightService>,
		    IFloodlightService>();
		m.put(INetworkGraphService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
	    Collection<Class<? extends IFloodlightService>> l =
	            new ArrayList<Class<? extends IFloodlightService>>();
	        l.add(IFloodlightProviderService.class);
	        l.add(IDeviceService.class);
	        l.add(IDatagridService.class);
	        l.add(IThreadPoolService.class);
	        return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		Map<String, String> configMap = context.getConfigParams(this);
		String conf = configMap.get(DBConfigFile);
                String dbStore = configMap.get(GraphDBStore);
		System.out.println("conf" + conf + "dbStore" + dbStore);
		op = GraphDBManager.getDBOperation("ramcloud", "/tmp/ramcloudconf");
		//op = GraphDBManager.getDBOperation(dbStore, conf);
		if (op == null) {
		    System.out.println("publisher op is null");
		}
		
		floodlightProvider =
	            context.getServiceImpl(IFloodlightProviderService.class);
		deviceService = context.getServiceImpl(IDeviceService.class);
		linkDiscovery = context.getServiceImpl(ILinkDiscoveryService.class);
		threadPool = context.getServiceImpl(IThreadPoolService.class);
		registryService = context.getServiceImpl(IControllerRegistryService.class);
		datagridService = context.getServiceImpl(IDatagridService.class);
		
		devStore = new DeviceStorageImpl();
		devStore.init(dbStore, conf);
		
		swStore = new SwitchStorageImpl();
		swStore.init(dbStore, conf);
		
		linkStore = new LinkStorageImpl();
		linkStore.init(dbStore, conf);
				
		log.debug("Initializing NetworkGraphPublisher module with {}", conf);
		
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		Map<String, String> configMap = context.getConfigParams(this);
		String cleanupNeeded = configMap.get(CleanupEnabled);

		deviceService.addListener(this);
		floodlightProvider.addOFSwitchListener(this);
		linkDiscovery.addListener(this);
		
		log.debug("Adding EventListener");
		System.out.println("start Up op " + op);
		IDBConnection conn = op.getDBConnection();
		conn.addEventListener(new LocalTopologyEventListener((DBConnection) conn));
	       // Setup the Cleanup task. 
		if (cleanupNeeded == null || !cleanupNeeded.equals("False")) {
				ScheduledExecutorService ses = threadPool.getScheduledExecutor();
				cleanupTask = new SingletonTask(ses, new SwitchCleanup());
				cleanupTask.reschedule(CLEANUP_TASK_INTERVAL, TimeUnit.SECONDS);
		}

		//
		// NOTE: No need to register with the Datagrid Service,
		// because we don't need to receive any notifications from it.
		//
	}

}
