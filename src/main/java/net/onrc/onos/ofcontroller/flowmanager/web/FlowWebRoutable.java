package net.onrc.onos.ofcontroller.flowmanager.web;

import net.floodlightcontroller.restserver.RestletRoutable;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * REST API implementation for the Flow Manager.
 */
public class FlowWebRoutable implements RestletRoutable {
    /**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/add/json", AddFlowResource.class);
        router.attach("/add-shortest-path/json", AddShortestPathFlowResource.class);
        router.attach("/delete/{flow-id}/json", DeleteFlowResource.class);
        router.attach("/get/{flow-id}/json", GetFlowByIdResource.class);
        router.attach("/getall-by-installer-id/{installer-id}/{src-dpid}/{src-port}/{dst-dpid}/{dst-port}/json", GetAllFlowsByInstallerIdResource.class);
        router.attach("/getall-by-endpoints/{src-dpid}/{src-port}/{dst-dpid}/{dst-port}/json", GetAllFlowsByEndpointsResource.class);
        router.attach("/getall/json", GetAllFlowsResource.class);
        router.attach("/getsummary/{flow-id}/{max-flows}/json", GetSummaryFlowsResource.class);
        return router;
    }

    /**
     * Set the base path for the Topology
     */
    @Override
    public String basePath() {
        return "/wm/flow";
    }
}
