package net.onrc.onos.ofcontroller.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import net.onrc.onos.graph.GraphDBConnection;
import net.onrc.onos.graph.GraphDBOperation;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IFlowEntry;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IFlowPath;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.ISwitchObject;
import net.onrc.onos.ofcontroller.core.internal.SwitchStorageImpl;
import net.onrc.onos.ofcontroller.core.internal.TestDatabaseManager;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.LoggerFactory;
import org.powermock.modules.junit4.PowerMockRunner;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;

//Add Powermock preparation
@RunWith(PowerMockRunner.class)
@PrepareForTest({TitanFactory.class, GraphDBConnection.class, GraphDBOperation.class, SwitchStorageImpl.class})
public class INetMapTopologyObjectsIFlowPathTest {
	
	//The test network is ./titan/schema/test-network.xml

	protected static org.slf4j.Logger log = LoggerFactory.getLogger(SwitchStorageImpl.class);

	String conf;
    private GraphDBConnection conn = null;
    private GraphDBOperation ope = null;
    private TitanGraph titanGraph = null;
	private IFlowPath flowPath = null;
	private IFlowEntry flowEntry = null;
	
	@Before
	public void setUp() throws Exception {
		conf = "/dummy/path/to/db";
		
		// Make mock cassandra DB
		// Replace TitanFactory.open() to return mock DB
		TestDatabaseManager.deleteTestDatabase();
		titanGraph = TestDatabaseManager.getTestDatabase();
		//TestDatabaseManager.populateTestData(titanGraph);
		PowerMock.mockStatic(TitanFactory.class);
		EasyMock.expect(TitanFactory.open((String)EasyMock.anyObject())).andReturn(titanGraph);
		PowerMock.replay(TitanFactory.class);
		
		conn = GraphDBConnection.getInstance(conf);
		ope = new GraphDBOperation(conn);
		
		flowPath = ope.newFlowPath();
		flowEntry = ope.newFlowEntry();
		flowEntry.setState("zz");
	}

	@After
	public void tearDown() throws Exception {
		titanGraph.shutdown();
		TestDatabaseManager.deleteTestDatabase();
	}
	
	/**
	 * Desc:
	 *  Test method for get and set FlowId method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the flow id.
	 * 2. Should get the flow id.
	 */
	@Test
	public void testSetGetFlowId() {
		String flowId = "xx";
		flowPath.setFlowId(flowId);
		assertEquals(flowPath.getFlowId(), flowId);
	}
	
	/**
	 * Desc:
	 *  Test method for get and set InstallerId method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the installer id.
	 * 2. Should get the installer id.
	 */
	@Test
	public void testSetGetInstallerId() {
		String flowId = "xx";
		String installerId = "yy";
		flowPath.setFlowId(flowId);
		flowPath.setInstallerId(installerId);
		assertEquals(flowPath.getInstallerId(), installerId);
	}

	/**
	 * Desc:
	 *  Test method for get and set FlowPathType method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the Flow Path Type.
	 * 2. Should get the Flow Path Type.
	 */
	@Test
	public void testSetGetFlowPathType() {
		String flowId = "xx";
		String flowPathType = "FP_TYPE_SHORTEST_PATH";
		flowPath.setFlowId(flowId);
		flowPath.setFlowPathType(flowPathType);
		assertEquals(flowPath.getFlowPathType(), flowPathType);
	}

	/**
	 * Desc:
	 *  Test method for get and set FlowPathFlags method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the Flow Path Flags.
	 * 2. Should get the Flow Path Flags.
	 */
	@Test
	public void testSetGetFlowPathFlags() {
		String flowId = "xx";
		Long flowPathFlags = new Long(0x3);
		flowPath.setFlowId(flowId);
		flowPath.setFlowPathFlags(flowPathFlags);
		assertEquals(flowPath.getFlowPathFlags(), flowPathFlags);
	}

	/**
	 * Desc:
	 *  Test method for get and set SourceSwitch method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the source switch.
	 * 2. Should get the source switch.
	 */
	@Test
	public void testSetGetSourceSwitch() {
		String flowId = "xx";
		String sourceSwitch = "aa";
		flowPath.setFlowId(flowId);
		flowPath.setSrcSwitch(sourceSwitch);
		assertEquals(flowPath.getSrcSwitch(), sourceSwitch);
	}
	
	/**
	 * Desc:
	 *  Test method for get and set SourcePort method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the source port.
	 * 2. Should get the source port.
	 */
	@Test
	public void testSetGetSourcePort() {
		String flowId = "xx";
		Short sourcePort = 1;
		flowPath.setFlowId(flowId);
		flowPath.setSrcPort(sourcePort);
		assertEquals(flowPath.getSrcPort(), sourcePort);
	}
	
	/**
	 * Desc:
	 *  Test method for get and set DestSwitch method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the dest switch.
	 * 2. Should get the dest switch.
	 */
	@Test
	public void testSetGetDestSwitch() {
		String flowId = "xx";
		String destSwitch = "bb";
		flowPath.setFlowId(flowId);
		flowPath.setDstSwitch(destSwitch);
		assertEquals(flowPath.getDstSwitch(), destSwitch);
	}
	
	/**
	 * Desc:
	 *  Test method for get and set DestPort method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the source dest port.
	 * 2. Should get the source dest port.
	 */
	@Test
	public void testSetGetDstPort() {
		String flowId = "xx";
		Short dstPort = 2;
		flowPath.setFlowId(flowId);
		flowPath.setDstPort(dstPort);
		assertEquals(flowPath.getDstPort(), dstPort);
	}
	
	/**
	 * Desc:
	 *  Test method for get and set DataPathSummary method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set the data path summary.
	 * 2. Should get the data path summary.
	 */
	@Test
	public void testSetGetDataPathSummary() {
		String flowId = "xx";
		String dataPathSummary = "yy";
		flowPath.setFlowId(flowId);
		flowPath.setInstallerId(dataPathSummary);
		assertEquals(flowPath.getInstallerId(), dataPathSummary);
	}
	
	public boolean testIFlowEntry(IFlowPath fp, IFlowEntry fe)
	{
		ArrayList<IFlowEntry> flowEntryList = new ArrayList<IFlowEntry>();
		for(IFlowEntry inFlowEntry : fp.getFlowEntries())
		{
			flowEntryList.add(inFlowEntry);
		}
		return flowEntryList.contains(fe);
	}
	
	/**
	 * Desc:
	 *  Test method for addFlowEntry and getFlorEntries method.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should add the FlowEntry.
	 * 2. Should get the FlowEntries. It is tested in the testIFlowEntry method.
	 */
	@Test
	public void testAddFlowEntryAndGetFlowEntries() {
		String flowId = "xx";
		flowPath.setFlowId(flowId);
		flowPath.addFlowEntry(flowEntry);
		IFlowEntry flowEntry2 = ope.newFlowEntry();
		flowPath.addFlowEntry(flowEntry2);
		assertTrue(testIFlowEntry(flowPath, flowEntry));
		assertTrue(testIFlowEntry(flowPath, flowEntry2));
	}
	
	/**
	 * Desc:
	 *  Test method for remove FlowEntry.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should remove FlowEntry.
	 */
	@Test
	public void testRemoveFlowEntry() {
		String flowId = "xx";
		flowPath.setFlowId(flowId);
		flowPath.addFlowEntry(flowEntry);
		flowPath.removeFlowEntry(flowEntry);
		assertTrue(!testIFlowEntry(flowPath, flowEntry));
	}
	
	/**
	 * Desc:
	 *  Test method for set and get MatchSrcMac
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchSrcMac.
	 * 2. Should get MatchSrcMac.
	 */
	@Test
	public void testSetGetMatchSrcMac() {
		String flowId = "xx";
		String matchSrcMac = "00:00:00:00:00:11";
		flowPath.setFlowId(flowId);
		flowPath.setMatchSrcMac(matchSrcMac);
		assertEquals(flowPath.getMatchSrcMac(), matchSrcMac);
	}
	
	/**
	 * Desc:
	 *  Test method for set and get MatchDstMac.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchDstMac.
	 * 2. Should get MatchDstMac.
	 */
	@Test
	public void testSetGetMatchDstMac() {
		String flowId = "xx";
		String matchDstMac = "00:00:00:00:00:11";
		flowPath.setFlowId(flowId);
		flowPath.setMatchDstMac(matchDstMac);
		assertEquals(flowPath.getMatchDstMac(), matchDstMac);
	}

	/**
	 * Desc:
	 *  Test method for set and get MatchEthernetFrameType
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchEthernetFrameType.
	 * 2. Should get MatchEthernetFrameType.
	 */
	@Test
	public void testSetGetMatchEthernetFrameType() {
		String flowId = "xx";
		Short matchEthernetFrameType = 1;
		flowPath.setFlowId(flowId);
		flowPath.setMatchEthernetFrameType(matchEthernetFrameType);
		assertEquals(flowPath.getMatchEthernetFrameType(), matchEthernetFrameType);
	}

	/**
	 * Desc:
	 *  Test method for set and get MatchVlanId
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchVlanId.
	 * 2. Should get MatchVlanId.
	 */
	@Test
	public void testSetGetMatchVlanId() {
		String flowId = "xx";
		Short matchVlanId = 10;
		flowPath.setFlowId(flowId);
		flowPath.setMatchVlanId(matchVlanId);
		assertEquals(flowPath.getMatchVlanId(), matchVlanId);
	}

	/**
	 * Desc:
	 *  Test method for set and get MatchVlanPriority
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchVlanPriority.
	 * 2. Should get MatchVlanPriority.
	 */
	@Test
	public void testSetGetMatchVlanPriority() {
		String flowId = "xx";
		Byte matchVlanPriority = 20;
		flowPath.setFlowId(flowId);
		flowPath.setMatchVlanPriority(matchVlanPriority);
		assertEquals(flowPath.getMatchVlanPriority(), matchVlanPriority);
	}
	
	/**
	 * Desc:
	 *  Test method for set and get MatchSrcIPv4Net.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchSrcIPv4Net.
	 * 2. Should get MatchSrcIPv4Net.
	 */
	@Test
	public void testSetGetMatchSrcIPv4Net() {
		String flowId = "xx";
		String ip = "192.168.0.1";
		flowPath.setFlowId(flowId);
		flowPath.setMatchSrcIPv4Net(ip);
		assertEquals(flowPath.getMatchSrcIPv4Net(), ip);
	}
	
	/**
	 * Desc:
	 *  Test method for set and get MatchDstIPv4Net.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchDstIPv4Net.
	 * 2. Should get MatchDstIPv4Net.
	 */
	@Test
	public void testSetGetMatchDstIPv4Net() {
		String flowId = "xx";
		String ip = "192.168.0.1";
		flowPath.setFlowId(flowId);
		flowPath.setMatchDstIPv4Net(ip);
		assertEquals(flowPath.getMatchDstIPv4Net(), ip);
	}

	/**
	 * Desc:
	 *  Test method for set and get MatchIpProto
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchIpProto.
	 * 2. Should get MatchIpProto.
	 */
	@Test
	public void testSetGetMatchIpProto() {
		String flowId = "xx";
		Byte matchIpProto = 20;
		flowPath.setFlowId(flowId);
		flowPath.setMatchIpProto(matchIpProto);
		assertEquals(flowPath.getMatchIpProto(), matchIpProto);
	}

	/**
	 * Desc:
	 *  Test method for set and get MatchIpToS
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchIpToS.
	 * 2. Should get MatchIpToS.
	 */
	@Test
	public void testSetGetMatchIpToS() {
		String flowId = "xx";
		Byte matchIpToS = 20;
		flowPath.setFlowId(flowId);
		flowPath.setMatchIpToS(matchIpToS);
		assertEquals(flowPath.getMatchIpToS(), matchIpToS);
	}

	/**
	 * Desc:
	 *  Test method for set and get MatchSrcTcpUdpPort.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchSrcTcpUdpPort.
	 * 2. Should get MatchSrcTcpUdpPort.
	 */
	@Test
	public void testSetGetMatchSrcTcpUdpPort() {
		String flowId = "xx";
		Short srcTcpUdpPort = (short)65535;
		flowPath.setFlowId(flowId);
		flowPath.setMatchSrcTcpUdpPort(srcTcpUdpPort);
		assertEquals(flowPath.getMatchSrcTcpUdpPort(), srcTcpUdpPort);
	}

	/**
	 * Desc:
	 *  Test method for set and get MatchDstTcpUdpPort.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set MatchDstTcpUdpPort.
	 * 2. Should get MatchDstTcpUdpPort.
	 */
	@Test
	public void testSetGetMatchDstTcpUdpPort() {
		String flowId = "xx";
		Short dstTcpUdpPort = (short)65535;
		flowPath.setFlowId(flowId);
		flowPath.setMatchDstTcpUdpPort(dstTcpUdpPort);
		assertEquals(flowPath.getMatchDstTcpUdpPort(), dstTcpUdpPort);
	}

	/**
	 * Desc:
	 *  Test method for set and get UserState.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should set UserState.
	 * 2. Should get UserState.
	 */
	@Test
	public void testSetGetUserState() {
		String flowId = "xx";
		String userStatus = "Good";
		flowPath.setFlowId(flowId);
		flowPath.setUserState(userStatus);
		assertEquals(flowPath.getUserState(), userStatus);
	}
	
	/**
	 * Desc:
	 *  Test method for get Switches.
	 * Condition:
	 *  N/A
	 * Expect:
	 * 1. Should get switches.
	 */
	@Test
	public void testGetSwitches() {
		String flowId = "xx";
		String dpid = "1";
		flowPath.setFlowId(flowId);
		ISwitchObject sw = ope.newSwitch(dpid);
		flowEntry.setSwitch(sw);
		flowPath.addFlowEntry(flowEntry);
		
		HashMap<String, ISwitchObject> swList = new HashMap<String, ISwitchObject>();
		for(ISwitchObject insw : flowPath.getSwitches()){
			swList.put(sw.getDPID(), insw);
		}
		
		assertTrue(swList.containsKey(dpid));
	}
	
	//TODO Dont know how to set the state property.
	@Test
	public void testGetState() {
		String status = null;
		assertEquals(flowPath.getState(), status);
	}
	
	
}
