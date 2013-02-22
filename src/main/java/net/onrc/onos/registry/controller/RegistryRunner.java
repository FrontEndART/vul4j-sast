package net.onrc.onos.registry.controller;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.onrc.onos.registry.controller.IControllerRegistryService.ControlChangeCallback;

import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for lightweight testing of the mastership module without having
 * to load up the entire ONOS.
 * @author jono
 *
 */
public class RegistryRunner {
	protected static Logger log = LoggerFactory.getLogger(RegistryRunner.class);

	public static void main(String args[]){
		FloodlightModuleContext fmc = new FloodlightModuleContext();
		ZookeeperRegistry rm = new ZookeeperRegistry();
		
		fmc.addConfigParam(rm, "enableZookeeper", "true");
		
		String id = null;
		if (args.length > 0){
			id = args[0];
			log.info("Using unique id: {}", id);
		}
		
		try {
			rm.init(fmc);
			rm.startUp(fmc);
			
			if (id == null){
				log.error("No unique ID supplied");
				System.exit(1);
			}
			
			rm.registerController(id);
			//rm.setMastershipId(id);
				
			rm.requestControl(1L, 
				new ControlChangeCallback(){
					@Override
					public void controlChanged(long dpid, boolean isMaster) {
						if (isMaster){
							log.debug("Callback for becoming master for {}", HexString.toHexString(dpid));
						}
						else {
							log.debug("Callback for losing mastership for {}", HexString.toHexString(dpid));
						}
					}
				});
			
			Thread.sleep(1000);
			
			/*
			Map<String, List<ControllerRegistryEntry>> switches = rm.getAllSwitches();
			for (List<ControllerRegistryEntry> ls : switches.values()){
				for (ControllerRegistryEntry cre : ls){
					log.debug("ctrlr: {}", cre.getControllerId());
				}
			}
			*/
			//"Server" loop
			while (true) {
				Thread.sleep(60000);
			}
			
		} catch (FloodlightModuleException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.debug("is master: {}", rm.hasControl(1L));
	}
}
