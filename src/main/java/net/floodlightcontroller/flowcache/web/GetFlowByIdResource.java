package net.floodlightcontroller.flowcache.web;

import net.floodlightcontroller.util.FlowId;
import net.floodlightcontroller.util.FlowPath;
import net.onrc.onos.ofcontroller.flowcache.IFlowService;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetFlowByIdResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(GetFlowByIdResource.class);

    @Get("json")
    public FlowPath retrieve() {
	FlowPath result = null;

        IFlowService flowService =
                (IFlowService)getContext().getAttributes().
                get(IFlowService.class.getCanonicalName());

        if (flowService == null) {
	    log.debug("ONOS Flow Service not found");
            return result;
	}

	// Extract the arguments
	String flowIdStr = (String) getRequestAttributes().get("flow-id");
	FlowId flowId = new FlowId(flowIdStr);

	log.debug("Get Flow Id: " + flowIdStr);

	result = flowService.getFlow(flowId);

        return result;
    }
}
