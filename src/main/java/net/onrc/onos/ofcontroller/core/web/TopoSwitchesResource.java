package net.onrc.onos.ofcontroller.core.web;

import java.util.Iterator;

import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.ISwitchObject;
import net.onrc.onos.ofcontroller.core.internal.TopoSwitchServiceImpl;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TopoSwitchesResource extends ServerResource {
	
	@Get("json")
	public Iterator<ISwitchObject> retrieve() {
		TopoSwitchServiceImpl impl = new TopoSwitchServiceImpl();
		
		String filter = (String) getRequestAttributes().get("filter");
		
		if (filter.equals("active")) {
			return impl.getActiveSwitches().iterator();
		}
		if (filter.equals("inactive")) {
			return impl.getInactiveSwitches().iterator();
		} else {
		    return impl.getAllSwitches().iterator();
		}
	}

}
