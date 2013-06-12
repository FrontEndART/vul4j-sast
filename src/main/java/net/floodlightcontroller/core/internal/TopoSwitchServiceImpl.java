package net.floodlightcontroller.core.internal;

import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IPortObject;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.ISwitchObject;
import net.onrc.onos.ofcontroller.core.INetMapTopologyService.ITopoSwitchService;
import net.onrc.onos.util.GraphDBConnection;
import net.onrc.onos.util.GraphDBConnection.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopoSwitchServiceImpl implements ITopoSwitchService {
	
	private GraphDBConnection conn;
	protected static Logger log = LoggerFactory.getLogger(TopoSwitchServiceImpl.class);


	public void finalize() {
		close();
	}
	
	@Override
	public void close() {

		conn.close();
	}
	
	@Override
	public Iterable<ISwitchObject> getActiveSwitches() {
		// TODO Auto-generated method stub
		conn = GraphDBConnection.getInstance("/tmp/cassandra.titan");
		conn.close(); //Commit to ensure we see latest data
		return conn.utils().getActiveSwitches(conn);
	}

	@Override
	public Iterable<ISwitchObject> getAllSwitches() {
		// TODO Auto-generated method stub
		conn = GraphDBConnection.getInstance("/tmp/cassandra.titan");
		conn.close(); //Commit to ensure we see latest data
		return conn.utils().getAllSwitches(conn);
	}

	@Override
	public Iterable<ISwitchObject> getInactiveSwitches() {
		// TODO Auto-generated method stub
		conn = GraphDBConnection.getInstance("/tmp/cassandra.titan");
		conn.close(); //Commit to ensure we see latest data
		return conn.utils().getInactiveSwitches(conn);
	}

	@Override
	public Iterable<IPortObject> getPortsOnSwitch(String dpid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPortObject getPortOnSwitch(String dpid, short port_num) {
		// TODO Auto-generated method stub
		return null;
	}	
}
