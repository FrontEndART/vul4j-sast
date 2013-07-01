package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.authentication.Authentication;
import de.intevation.lada.authentication.AuthenticationException;
import de.intevation.lada.authentication.AuthenticationResponse;
import de.intevation.lada.data.QueryBuilder;
import de.intevation.lada.data.Repository;
import de.intevation.lada.model.LProbe;
import de.intevation.lada.model.LProbeInfo;

/**
* This class produces a RESTful service to read the contents of LProbe table.
* 
* @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
*/
@Path("/proben")
@RequestScoped
public class LProbeService {

    /**
     * The Repository for LProbe.
     */
    @Inject
    @Named("lproberepository")
    private Repository repository;

    /**
     * The logger for this class.
     */
    @Inject
    private Logger log;

    @Inject
    @Named("ldapauth")
    private Authentication authentication;

    /**
     * Request a LProbe via its id.
     *
     * @param id The LProbe id
     * @return JSON Object via REST service.
     */
    @GET
    @Path("/{id}")
    @Produces("text/json")
    public Response findById(
        @PathParam("id") String id,
        @Context HttpHeaders header
    ) {
        try {
            AuthenticationResponse auth =
                authentication.authorizedGroups(header);
            Response response =
                repository.findById(LProbeInfo.class, id);
            List<LProbeInfo> probe = (List<LProbeInfo>)response.getData();
            if (probe.isEmpty()) {
                return new Response(false, 601, new ArrayList<LProbeInfo>());
            }
            String nbId = probe.get(0).getNetzbetreiberId();
            String mstId = probe.get(0).getMstId();
            if (auth.getNetzbetreiber().contains(nbId)) {
                if (auth.getMst().contains(mstId)) {
                    //TODO: Test if probe has a messung that has status 'ready'.
                    return response;
                }
                response.setReadonly(true);
                return response;
            }
            return new Response(false, 698, new ArrayList<LProbe>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LProbe>());
        }
    }

    /**
     * Request LProbe via a filter.
     *
     * Query parameters are used for the filter in form of key-value pairs.
     * This filter can take the three parameters
     *   mst=$MSTID (String)
     *   uwb=$UWBID (String)
     *   begin=$PROBEENTNAHMEBEGIN (Timestamp)
     *
     * @param info The URL query parameters.
     * @return JSON Object via Rest service.
     */
    @GET
    @Produces("text/json")
    public Response filter(
        @Context UriInfo info,
        @Context HttpHeaders header
    ) {
        try {
            AuthenticationResponse auth =
                authentication.authorizedGroups(header);
            QueryBuilder<LProbeInfo> builder =
                new QueryBuilder<LProbeInfo>(
                    repository.getEntityManager(),
                    LProbeInfo.class);
            builder.or("netzbetreiberId", auth.getNetzbetreiber());
            MultivaluedMap<String, String> params = info.getQueryParameters();
            if (params.isEmpty()) {
                return repository.filter(builder.getQuery());
            }
            QueryBuilder<LProbeInfo> mstBuilder = builder.getEmptyBuilder();
            if (params.keySet().contains("mst")) {
                String[] paramValues = params.getFirst("mst").split(",");
                for (String pv: paramValues) {
                    mstBuilder.or("mstId", pv);
                }
                builder.and(mstBuilder);
            }
            QueryBuilder<LProbeInfo> umwBuilder = builder.getEmptyBuilder();
            if (params.keySet().contains("uwb")) {
                String[] paramValues = params.getFirst("uwb").split(",");
                for (String pv: paramValues) {
                    umwBuilder.or("umwId", pv);
                }
                builder.and(umwBuilder);
            }
            QueryBuilder<LProbeInfo> beginBuilder = builder.getEmptyBuilder();
            if (params.keySet().contains("bedin")) {
                String[] paramValues = params.getFirst("begin").split(",");
                for (String pv: paramValues) {
                    beginBuilder.or("probeentnahmeBegin", pv);
                }
                builder.and(beginBuilder);
            }
            builder.distinct();
            return repository.filter(builder.getQuery());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LProbe>());
        }
    }

    @PUT
    @Path("/{id}")
    @Produces("text/json")
    @Consumes("application/json")
    public Response update(LProbeInfo probe) {
        return repository.update(probe);
    }

    @POST
    @Produces("text/json")
    @Consumes("application/json")
    public Response create(LProbeInfo probe) {
        LProbe p = probe.toLProbe();
        return repository.create(p);
    }
}
