/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3) 
 * and comes with ABSOLUTELY NO WARRANTY! Check out 
 * the documentation coming with IMIS-Labordaten-Application for details. 
 */
package de.intevation.lada.rest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import de.intevation.lada.auth.Authentication;
import de.intevation.lada.auth.AuthenticationException;
import de.intevation.lada.data.QueryBuilder;
import de.intevation.lada.data.Repository;
import de.intevation.lada.model.LKommentarM;
import de.intevation.lada.model.LKommentarMId;
import de.intevation.lada.model.LMessung;
import de.intevation.lada.model.LProbe;

/**
 * This class produces a RESTful service to read, write and update
 * LKommentarM objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("/messkommentare")
@RequestScoped
public class LKommentarMService
{
    /**
     * The Repository for SMessStelle.
     */
    @Inject
    @Named("lkommentarmrepository")
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @Named("ldapauth")
    private Authentication authentication;

    /**
     * Request LKommentarM via a filter.
     *
     * Query parameters are used for the filter in form of key-value pairs.
     *
     * @param info      The URL query parameters.
     * @param headers   The HTTP header containing authorization information.
     * @return Response object.
     */
    @GET
    @Produces("text/json")
    public Response filter(
        @Context UriInfo info,
        @Context HttpHeaders headers
    ) {
        try {
            if (!authentication.isAuthorizedUser(headers)) {
                return new Response(false, 699, new ArrayList<LKommentarM>());
            }
            MultivaluedMap<String, String> params = info.getQueryParameters();
            if (params.isEmpty() ||
                !params.containsKey("probeId") ||
                !params.containsKey("messungsId")
            ) {
                return new Response(false, 609, new ArrayList<LKommentarM>());
            }
            String probeId = params.getFirst("probeId");
            QueryBuilder<LKommentarM> builder =
                new QueryBuilder<LKommentarM>(
                    repository.getEntityManager(), LKommentarM.class);
            builder.and("probeId", probeId)
                .and("messungsId", params.getFirst("messungsId"));
            return repository.filter(builder.getQuery());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LKommentarM>());
        }
    }

    /**
     * Update LKommentarM objects.
     *
     * @param kommentar The LKommentarM object to update.
     * @param headers   The HTTP header containing authorization information.
     * @return Response object.
     */
    @PUT
    @Path("/{kId}/{messungsId}/{probeId}")
    @Produces("text/json")
    @Consumes("application/json")
    public Response update(
        LKommentarM kommentar,
        @Context HttpHeaders headers
    ) {
        try {
            String probeId = kommentar.getProbeId();
            if (authentication.hasAccess(headers, probeId)) {
                return repository.update(kommentar);
            }
            return new Response(false, 698, new ArrayList<LKommentarM>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LKommentarM>());
        }
    }

    /**
     * Create a new LKommentarM object.
     *
     * @param kommentar The new LKommentarM object.
     * @param headers   The HTTP header containing authorization information.
     * @return Response object.
     */
    @POST
    @Produces("text/json")
    @Consumes("application/json")
    public Response create(
        LKommentarM kommentar,
        @Context HttpHeaders headers
    ) {
        try {
            String probeId = kommentar.getProbeId();
            if (authentication.hasAccess(headers, probeId)) {
                LKommentarMId id = new LKommentarMId();
                Query q =
                    repository.getEntityManager().createNativeQuery(
                        "select nextval('kommentar_m_id_seq')");
                BigInteger seqId = (BigInteger)q.getSingleResult();
                id.setKId(seqId.intValue());
                id.setMessungsId(kommentar.getMessungsId());
                id.setProbeId(probeId);
                kommentar.setId(id);
                return repository.create(kommentar);
            }
            return new Response(false, 698, new ArrayList<LKommentarM>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LKommentarM>());
        }
    }

    /**
     * Delete a LKommentarM object.
     *
     * @param kId          The object id.
     * @param messungsId   The {@link LMessung} id.
     * @param probeId      The {@link LProbe} id
     * @param headers      The HTTP header containing authorization information.
     * @return Response object.
     */
    @DELETE
    @Path("/{kId}/{messungsId}/{probeId}")
    public Response delete(
        @PathParam("kId") String kId,
        @PathParam("messungsId") String messungsId,
        @PathParam("probeId") String probeId,
        @Context HttpHeaders headers
    ) {
        try {
            if (authentication.hasAccess(headers, probeId)) {
                QueryBuilder<LKommentarM> builder =
                    new QueryBuilder<LKommentarM>(
                        repository.getEntityManager(),
                        LKommentarM.class);
                builder.and("KId", kId)
                    .and("messungsId", messungsId)
                    .and("probeId", probeId);
                Response response = repository.filter(builder.getQuery());
                List<LKommentarM> list = (List<LKommentarM>)response.getData();
                if (!list.isEmpty()) {
                    repository.delete(list.get(0));
                    return new Response(true, 200, null);
                }
                return new Response(false, 600, null);
            }
            return new Response(false, 698, new ArrayList<LKommentarM>());
        }
        catch(AuthenticationException ae) {
            return new Response(false, 699, new ArrayList<LKommentarM>());
        }
    }
}
