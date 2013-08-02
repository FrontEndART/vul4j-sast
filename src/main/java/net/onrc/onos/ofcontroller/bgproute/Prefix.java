package net.onrc.onos.ofcontroller.bgproute;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Prefix {
	private int prefixLength;
	private InetAddress address;

	public Prefix(byte[] addr, int prefixLength) throws UnknownHostException {
		//try {
		address = InetAddress.getByAddress(addr);
		//} catch (UnknownHostException e) {
		//	System.out.println("InetAddress exception");
		//	return;
		//}
		this.prefixLength = prefixLength;
		//System.out.println(address.toString() + "/" + prefixLength);
	}

	public Prefix(String str, int prefixLength) throws UnknownHostException {
		//try {
		address = InetAddress.getByName(str);
		//} catch (UnknownHostException e) {
		//	System.out.println("InetAddress exception");
		//	return;
		//}
		this.prefixLength = prefixLength;
	}

	public int getPrefixLength() {
		return prefixLength;
	}
	
	public byte[] getAddress() {
		return address.getAddress();
	}
	
	@Override
	public String toString() {
		return address.getHostAddress() + "/" + prefixLength;
	}
}
