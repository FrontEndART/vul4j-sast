package de.intevation.lada.rest;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.auth.Authentication;
import de.intevation.lada.auth.AuthenticationException;
import de.intevation.lada.data.QueryBuilder;
import de.intevation.lada.data.Repository;
import de.intevation.lada.model.LMessung;
import de.intevation.lada.model.LMessungId;

/**
* This class produces a RESTful service to read, write and update
* LMessung objects.
*
* @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
*/
@Path("/messung")
@RequestScoped
public class LMessungService
{
    /**
     * The Repository for LMessung.
     */
    @Inject
    @Named("lmessungrepository")
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @Named("ldapauth")
    private Authentication authentication;

    /**
     * Request LMessung via a filter.
     *
     * Query parameters are used for the filter in form of key-value pairs.
     * This filter can take the three parameters
     *   probe=$PROBEID (String)
     *
     * @param info The URL query parameters.
     * @return JSON Object via Rest service.
     */
    @GET
    @Produces("text/json")
    public Response filter(
        @Context UriInfo info,
        @Context HttpHeaders headers
    ) {
        try {
            if (!authentication.isAuthorizedUser(headers)) {
                return new Response(false, 699, new ArrayList<LMessung>());
            }
            MultivaluedMap<String, String> params = info.getQueryParameters();
            if (params.isEmpty() || !(params.containsKey("probeId"))) {
                return new Response(false, 609, new ArrayList<LMessung>());
            }
            String probeId = params.getFirst("probeId");
            if (authentication.hasAccess(headers, probeId)) {
                QueryBuilder<LMessung> builder =
                    new QueryBuilder<LMessung>(
                        repository.getEntityManager(),
                        LMessung.class);
                builder.and("probeId", probeId);
                return repository.filter(builder.getQuery());
            }
            return new Response(false, 698, new ArrayList<LMessung>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LMessung>());
        }
    }

    /**
     * Update a LMessung object.
     *
     * @param messung   The LMessung object to update.
     * @param headers   The HTTP header containing authorization information.
     * @return Response object.
     */
    @PUT
    @Path("/{messungId}/{probeId}")
    @Produces("text/json")
    @Consumes("application/json")
    public Response update(
        LMessung messung,
        @Context HttpHeaders headers
    ) {
        try {
            String probeId = messung.getProbeId();
            int messungsId = messung.getId().getMessungsId();
            if (authentication.hasAccess(headers, probeId) &&
                !isReadOnly(probeId, messungsId)) {
                return repository.update(messung);
            }
            return new Response(false, 698, new ArrayList<LMessung>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LMessung>());
        }
    }

    /**
     * Create a new LMessung object.
     *
     * @param messung   The new LMessung object.
     * @param headers   The HTTP header containing authorization information.
     * @return Response object.
     */
    @POST
    @Produces("text/json")
    @Consumes("application/json")
    public Response create(
        LMessung messung,
        @Context HttpHeaders headers
    ) {
        try {
            String probeId = messung.getProbeId();
            if (authentication.hasAccess(headers, probeId)) {
                LMessungId id = new LMessungId();
                id.setProbeId(probeId);
                messung.setId(id);
                return repository.create(messung);
            }
            return new Response(false, 698, new ArrayList<LMessung>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LMessung>());
        }
    }

    private boolean isReadOnly(String probeId, Integer messungsId) {
        QueryBuilder<LMessung> builder =
            new QueryBuilder<LMessung>(
                repository.getEntityManager(),
                LMessung.class);
        builder.and("probeId", probeId)
            .and("messungsId", String.valueOf(messungsId));
        Response response = repository.filter(builder.getQuery());
        List<LMessung> messungen = (List<LMessung>) response.getData();
        if (messungen.isEmpty()) {
            return true;
        }
        if (messungen.size() > 1) {
            return true;
        }
        return messungen.get(0).isFertig();
    }
}
