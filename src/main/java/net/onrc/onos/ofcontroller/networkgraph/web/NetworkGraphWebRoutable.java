package net.onrc.onos.ofcontroller.networkgraph.web;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class NetworkGraphWebRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		router.attach("/rc/switches/json", RamcloudSwitchesResource.class);
		router.attach("/rc/links/json", RamcloudLinksResource.class);
		router.attach("/rc/ports/json", RamcloudPortsResource.class);
		router.attach("/ng/switches/json", NetworkGraphSwitchesResource.class);
		router.attach("/ng/links/json", NetworkGraphLinksResource.class);
		router.attach("/ng/shortest-path/{src-dpid}/{dst-dpid}/json", NetworkGraphShortestPathResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/wm/onos";
	}

}
