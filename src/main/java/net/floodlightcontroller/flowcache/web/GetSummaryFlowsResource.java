package net.floodlightcontroller.flowcache.web;

import java.util.ArrayList;

import net.floodlightcontroller.flowcache.IFlowService;
import net.floodlightcontroller.util.FlowPath;
import net.floodlightcontroller.util.FlowId;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetSummaryFlowsResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(GetAllFlowsResource.class);

    @Get("json")
    public ArrayList<FlowPath> retrieve() {
    	ArrayList<FlowPath> result = null;
    	
    	IFlowService flowService = (IFlowService)getContext().getAttributes().get(IFlowService.class.getCanonicalName());

        if (flowService == null) {
        	log.debug("ONOS Flow Service not found");
        	return result;
        }

        // Extract the arguments
        log.debug("Get All Flows Endpoints");

        FlowId flowId = new FlowId(0);
        
        result = flowService.getAllFlowsSummary(flowId, 0);

        return result;
    }
}
