package net.floodlightcontroller.core.web;

import java.util.Iterator;

import net.floodlightcontroller.core.ISwitchStorage.ISwitchObject;
import net.floodlightcontroller.core.internal.TopoSwitchServiceImpl;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TopoSwitchesResource extends ServerResource {
	
	@Get("json")
	public Iterator<ISwitchObject> retrieve() {
		TopoSwitchServiceImpl impl = new TopoSwitchServiceImpl();
		
		String filter = (String) getRequestAttributes().get("filter");
		
		if (filter.equals("active")) {
			return (Iterator<ISwitchObject>) impl.GetActiveSwitches().iterator();
		}
		if (filter.equals("inactive")) {
			return (Iterator<ISwitchObject>) impl.GetInactiveSwitches().iterator();
		} else {
		    return (Iterator<ISwitchObject>) impl.GetAllSwitches().iterator();
		}
	}

}
