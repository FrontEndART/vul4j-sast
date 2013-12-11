package net.onrc.onos.ofcontroller.core;

import net.floodlightcontroller.devicemanager.IDevice;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IDeviceObject;

public interface IDeviceStorage extends INetMapStorage {
	
	public IDeviceObject addDevice(IDevice device);
	public IDeviceObject updateDevice(IDevice device);
	public void removeDevice(IDevice device);
	public void removeDevice(IDeviceObject deviceObject);
	public IDeviceObject getDeviceByMac(String mac);
	public IDeviceObject getDeviceByIP(int ipv4Address);
	public void changeDeviceAttachments(IDevice device);
	public void changeDeviceIPv4Address(IDevice device);	
}
