package net.floodlightcontroller.core;

import net.floodlightcontroller.core.INetMapTopologyObjects.IPortObject;
import net.floodlightcontroller.flowcache.web.DatapathSummarySerializer;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Incidence;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.VertexFrame;

public interface INetMapTopologyObjects {
	
public interface IBaseObject extends VertexFrame {
	
	@JsonProperty("state")
	@Property("state")
	public String getState();
	
	@Property("state")
	public void setState(final String state);
	
	@JsonIgnore
	@Property("type")
	public String getType();
	@Property("type")
	public void setType(final String type);
	
}
	
public interface ISwitchObject extends IBaseObject{
		
		@JsonProperty("dpid")
		@Property("dpid")
		public String getDPID();
		
		@Property("dpid")
		public void setDPID(String dpid);
				
		@JsonProperty("ports")
		@Adjacency(label="on")
		public Iterable<IPortObject> getPorts();

// Requires Frames 2.3.0		
		@JsonIgnore
		@GremlinGroovy("_().out('on').has('number',port_num)")
		public IPortObject getPort(@GremlinParam("port_num") final short port_num);
		
		@Adjacency(label="on")
		public void addPort(final IPortObject port);
		
		@Adjacency(label="on")
		public void removePort(final IPortObject port);
		
		@JsonIgnore
		@GremlinGroovy("_().out('on').out('host')")
		public Iterable<IDeviceObject> getDevices();
		
		@JsonIgnore
		@Incidence(label="switch",direction = Direction.IN)
		public Iterable<IFlowEntry> getFlowEntries();

	}
	
	public interface IPortObject extends IBaseObject{
				
		@JsonProperty("number")
		@Property("number")
		public Short getNumber();
		
		@Property("number")
		public void setNumber(Short n);
		
		@JsonProperty("desc")
		@Property("desc")
		public String getDesc();
		
		@Property("desc")
		public void setDesc(String s);
		
		@JsonIgnore
		@Property("port_state")
		public Integer getPortState();
		
		@Property("port_state")
		public void setPortState(Integer s);
		
		@JsonIgnore
		@Incidence(label="on",direction = Direction.IN)
		public ISwitchObject getSwitch();
				
		@JsonProperty("devices")
		@Adjacency(label="host")
		public Iterable<IDeviceObject> getDevices();
		
		@Adjacency(label="host")
		public void setDevice(final IDeviceObject device);
		
		@Adjacency(label="host")
		public void removeDevice(final IDeviceObject device);
		
		@JsonIgnore
		@Incidence(label="inport",direction = Direction.IN)
		public Iterable<IFlowEntry> getInFlowEntries();
		
		@JsonIgnore
		@Incidence(label="outport",direction = Direction.IN)
		public Iterable<IFlowEntry> getOutFlowEntries();
		
		@JsonIgnore
		@Adjacency(label="link")
		public Iterable<IPortObject> getLinkedPorts();
		
		@Adjacency(label="link")
		public void removeLink(final IPortObject dest_port);
		
		@Adjacency(label="link")
		public void setLinkPort(final IPortObject dest_port);			
		
//		@JsonIgnore
//		@Adjacency(label="link")
//		public Iterable<ILinkObject> getLinks();
	}
	
	public interface IDeviceObject extends IBaseObject {
		
		@JsonProperty("mac")
		@Property("dl_addr")
		public String getMACAddress();
		@Property("dl_addr")
		public void setMACAddress(String macaddr);
		
		@JsonProperty("ipv4")
		@Property("nw_addr")
		public String getIPAddress();
		@Property("dl_addr")
		public void setIPAddress(String ipaddr);
		
		@JsonIgnore
		@Incidence(label="host",direction = Direction.IN)
		public Iterable<IPortObject> getAttachedPorts();
			
		@JsonIgnore
		@Incidence(label="host",direction=Direction.IN)
		public void setHostPort(final IPortObject port);
		
		@JsonIgnore
		@Incidence(label="host",direction=Direction.IN)
		public void removeHostPort(final IPortObject port);
		
		@JsonIgnore
		@GremlinGroovy("_().in('host').in('on')")
		public Iterable<ISwitchObject> getSwitch();
		
/*		@JsonProperty("dpid")
		@GremlinGroovy("_().in('host').in('on').next().getProperty('dpid')")
		public Iterable<String> getSwitchDPID();
		
		@JsonProperty("number")
		@GremlinGroovy("_().in('host').transform{it.number}")
		public Iterable<Short> getPortNumber();
		
		@JsonProperty("AttachmentPoint")
		@GremlinGroovy("_().in('host').in('on').path(){it.number}{it.dpid}")
		public Iterable<SwitchPort> getAttachmentPoints();*/
	}

public interface IFlowPath extends IBaseObject {
		@JsonProperty("flowId")
		@Property("flow_id")
		public String getFlowId();

		@Property("flow_id")
		public void setFlowId(String flowId);

		@JsonProperty("installerId")
		@Property("installer_id")
		public String getInstallerId();

		@Property("installer_id")
		public void setInstallerId(String installerId);

		@JsonProperty("srcDpid")
		@Property("src_switch")
		public String getSrcSwitch();

		@Property("src_switch")
		public void setSrcSwitch(String srcSwitch);

		@JsonProperty("srcPort")
		@Property("src_port")
		public Short getSrcPort();

		@Property("src_port")
		public void setSrcPort(Short srcPort);

		@JsonProperty("dstDpid")
		@Property("dst_switch")
		public String getDstSwitch();

		@Property("dst_switch")
		public void setDstSwitch(String dstSwitch);

		@JsonProperty("dstPort")
		@Property("dst_port")
		public Short getDstPort();

		@Property("dst_port")
		public void setDstPort(Short dstPort);

		@JsonProperty("dataPath")
		@JsonSerialize(using=DatapathSummarySerializer.class)
		@Property("data_path_summary")
		public String getDataPathSummary();

		@Property("data_path_summary")
		public void setDataPathSummary(String dataPathSummary);

		@JsonIgnore
		@Adjacency(label="flow", direction=Direction.IN)
		public Iterable<IFlowEntry> getFlowEntries();

		@Adjacency(label="flow", direction=Direction.IN)
		public void addFlowEntry(final IFlowEntry flowEntry);

		@Adjacency(label="flow", direction=Direction.IN)
		public void removeFlowEntry(final IFlowEntry flowEntry);

		@JsonIgnore
		@Property("matchEthernetFrameType")
		public Short getMatchEthernetFrameType();

		@Property("matchEthernetFrameType")
		public void setMatchEthernetFrameType(Short matchEthernetFrameType);

		@JsonIgnore
		@Property("matchSrcMac")
		public String getMatchSrcMac();

		@Property("matchSrcMac")
		public void setMatchSrcMac(String matchSrcMac);

		@JsonIgnore
		@Property("matchDstMac")
		public String getMatchDstMac();

		@Property("matchDstMac")
		public void setMatchDstMac(String matchDstMac);

		@JsonIgnore
		@Property("matchSrcIPv4Net")
		public String getMatchSrcIPv4Net();

		@Property("matchSrcIPv4Net")
		public void setMatchSrcIPv4Net(String matchSrcIPv4Net);

		@JsonIgnore
		@Property("matchDstIPv4Net")
		public String getMatchDstIPv4Net();

		@Property("matchDstIPv4Net")
		public void setMatchDstIPv4Net(String matchDstIPv4Net);
		
		@JsonIgnore
		@GremlinGroovy("_().in('flow').out('switch')")
		public Iterable<ISwitchObject> getSwitches();
		
		@JsonIgnore
		@Property("state")
		public String getState();

		@JsonIgnore
		@Property("user_state")
		public String getUserState();

		@Property("user_state")
		public void setUserState(String userState);
	}

public interface IFlowEntry extends IBaseObject {
		@Property("flow_entry_id")
		public String getFlowEntryId();

		@Property("flow_entry_id")
		public void setFlowEntryId(String flowEntryId);

		@Property("switch_dpid")
		public String getSwitchDpid();

		@Property("switch_dpid")
		public void setSwitchDpid(String switchDpid);

		@Property("user_state")
		public String getUserState();

		@Property("user_state")
		public void setUserState(String userState);

		@Property("switch_state")
		public String getSwitchState();

		@Property("switch_state")
		public void setSwitchState(String switchState);

		@Property("error_state_type")
		public String getErrorStateType();

		@Property("error_state_type")
		public void setErrorStateType(String errorStateType);

		@Property("error_state_code")
		public String getErrorStateCode();

		@Property("error_state_code")
		public void setErrorStateCode(String errorStateCode);

		@Property("matchInPort")
		public Short getMatchInPort();

		@Property("matchInPort")
		public void setMatchInPort(Short matchInPort);

		@Property("matchEthernetFrameType")
		public Short getMatchEthernetFrameType();

		@Property("matchEthernetFrameType")
		public void setMatchEthernetFrameType(Short matchEthernetFrameType);

		@Property("matchSrcMac")
		public String getMatchSrcMac();

		@Property("matchSrcMac")
		public void setMatchSrcMac(String matchSrcMac);

		@Property("matchDstMac")
		public String getMatchDstMac();

		@Property("matchDstMac")
		public void setMatchDstMac(String matchDstMac);

		@Property("matchSrcIPv4Net")
		public String getMatchSrcIPv4Net();

		@Property("matchSrcIPv4Net")
		public void setMatchSrcIPv4Net(String matchSrcIPv4Net);

		@Property("matchDstIPv4Net")
		public String getMatchDstIPv4Net();

		@Property("matchDstIPv4Net")
		public void setMatchDstIPv4Net(String matchDstIPv4Net);

		@Property("actionOutput")
		public Short getActionOutput();

		@Property("actionOutput")
		public void setActionOutput(Short actionOutput);

		@Adjacency(label="flow")
		public IFlowPath getFlow();

		@Adjacency(label="flow")
		public void setFlow(IFlowPath flow);

		@Adjacency(label="switch")
		public ISwitchObject getSwitch();

		@Adjacency(label="switch")
		public void setSwitch(ISwitchObject sw);

		@Adjacency(label="inport")
		public IPortObject getInPort();

		@Adjacency(label="inport")
		public void setInPort(IPortObject port);

		@Adjacency(label="outport")
		public IPortObject getOutPort();

		@Adjacency(label="outport")
		public void setOutPort(IPortObject port);
	}
}
