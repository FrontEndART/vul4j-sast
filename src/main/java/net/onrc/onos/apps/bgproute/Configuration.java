package net.onrc.onos.apps.bgproute;

import java.util.Collections;
import java.util.List;

import net.floodlightcontroller.util.MACAddress;

import org.codehaus.jackson.annotate.JsonProperty;
import org.openflow.util.HexString;

public class Configuration {
	private long bgpdAttachmentDpid;
	private short bgpdAttachmentPort;
	private MACAddress bgpdMacAddress;
	private short vlan;
	private List<String> switches;
	private List<Interface> interfaces;
	private List<BgpPeer> peers;
	
	public Configuration() {
		// TODO Auto-generated constructor stub
	}

	public long getBgpdAttachmentDpid() {
		return bgpdAttachmentDpid;
	}

	@JsonProperty("bgpdAttachmentDpid")
	public void setBgpdAttachmentDpid(String bgpdAttachmentDpid) {
		this.bgpdAttachmentDpid = HexString.toLong(bgpdAttachmentDpid);
	}

	public short getBgpdAttachmentPort() {
		return bgpdAttachmentPort;
	}

	@JsonProperty("bgpdAttachmentPort")
	public void setBgpdAttachmentPort(short bgpdAttachmentPort) {
		this.bgpdAttachmentPort = bgpdAttachmentPort;
	}
	
	public MACAddress getBgpdMacAddress() {
		return bgpdMacAddress;
	}

	@JsonProperty("bgpdMacAddress")
	public void setBgpdMacAddress(String strMacAddress) {
		this.bgpdMacAddress = MACAddress.valueOf(strMacAddress);
	}
	
	public List<String> getSwitches() {
		return Collections.unmodifiableList(switches);
	}
	
	@JsonProperty("vlan")
	public void setVlan(short vlan) {
		this.vlan = vlan;
	}
	
	public short getVlan() {
		return vlan;
	}

	@JsonProperty("switches")
	public void setSwitches(List<String> switches) {
		this.switches = switches;
	}
	
	public List<Interface> getInterfaces() {
		return Collections.unmodifiableList(interfaces);
	}

	@JsonProperty("interfaces")
	public void setInterfaces(List<Interface> interfaces) {
		this.interfaces = interfaces;
	}
	
	public List<BgpPeer> getPeers() {
		return Collections.unmodifiableList(peers);
	}

	@JsonProperty("bgpPeers")
	public void setPeers(List<BgpPeer> peers) {
		this.peers = peers;
	}

}
