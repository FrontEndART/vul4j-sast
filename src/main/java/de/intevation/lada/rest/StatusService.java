/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.LMessung;
import de.intevation.lada.model.land.LStatusProtokoll;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for Status objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "erzeuger": [string],
 *      "messungsId": [number],
 *      "status": [number],
 *      "owner": [boolean],
 *      "readonly": [boolean],
 *      "treeModified": [timestamp],
 *      "parentModified": [timestamp],
 *      "sdatum": [timestamp],
 *      "skommentar": [string]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("status")
@RequestScoped
public class StatusService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    @RepositoryConfig(type=RepositoryType.RW)
    private Repository defaultRepo;

    /**
     * The object lock mechanism.
     */
    @Inject
    @LockConfig(type=LockType.TIMESTAMP)
    private ObjectLocker lock;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type=AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Get all Status objects.
     * <p>
     * The requested objects can be filtered using a URL parameter named
     * messungsId.
     * <p>
     * Example: http://example.com/status?messungsId=[ID]
     *
     * @return Response object containing all Status objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info,
        @Context HttpServletRequest request
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("messungsId")) {
            return defaultRepo.getAll(LStatusProtokoll.class, "land");
        }
        String messungId = params.getFirst("messungsId");
        QueryBuilder<LStatusProtokoll> builder =
            new QueryBuilder<LStatusProtokoll>(
                defaultRepo.entityManager("land"),
                LStatusProtokoll.class);
        builder.and("messungsId", messungId);
        return authorization.filter(
            request,
            defaultRepo.filter(builder.getQuery(), "land"),
            LStatusProtokoll.class);
    }

    /**
     * Get a single Status object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/status/{id}
     *
     * @return Response object containing a single Status.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        return authorization.filter(
            request,
            defaultRepo.getById(LStatusProtokoll.class, Integer.valueOf(id), "land"),
            LStatusProtokoll.class);
    }

    /**
     * Create a Status object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "erzeuger": [string],
     *  "status": [number],
     *  "skommentar": [string],
     *  "treeModified":null,
     *  "parentModified":null,
     *  "sdatum": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Status.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        LStatusProtokoll status
    ) {
        if (!authorization.isAuthorized(
                request,
                status,
                RequestMethod.POST,
                LStatusProtokoll.class)
        ) {
            return new Response(false, 699, null);
        }
        UserInfo userInfo = authorization.getInfo(request);
        LMessung messung = defaultRepo.getByIdPlain(
            LMessung.class, status.getMessungsId(), "land");
        LStatusProtokoll currentStatus = defaultRepo.getByIdPlain(
            LStatusProtokoll.class, messung.getStatus(), "land");
        boolean next = false;
        boolean change = false;
        for (int i = 0; i < userInfo.getFunktionen().size(); i++) {
            if (userInfo.getFunktionen().get(i) > currentStatus.getStatusStufe()) {
                next = true;
                change = false;
                break;
            }
            else if (userInfo.getFunktionen().get(i) == currentStatus.getStatusStufe()) {
                change = true;
            }
        }
        if ((change || next) && status.getStatusWert() == 4) {
            status.setStatusStufe(1);
        }
        else if (change) {
            status.setStatusStufe(currentStatus.getStatusStufe());
        }
        else if (next) {
            status.setStatusStufe(currentStatus.getStatusStufe() + 1);
        }
        else {
            return new Response(false, 699, null);
        }
        Response response = defaultRepo.create(status, "land");
        LStatusProtokoll created = (LStatusProtokoll)response.getData();
        messung.setStatus(created.getId());
        defaultRepo.update(messung, "land");
        /* Persist the new object*/
        return authorization.filter(
            request,
            response,
            LStatusProtokoll.class);
    }

    /**
     * Update an existing Status object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "erzeuger": [string],
     *  "status": [number],
     *  "skommentar": [string],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "sdatum": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Status object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        LStatusProtokoll status
    ) {
        if (!authorization.isAuthorized(
                request,
                status,
                RequestMethod.PUT,
                LStatusProtokoll.class)
        ) {
            return new Response(false, 699, null);
        }
        if (lock.isLocked(status)) {
            return new Response(false, 697, null);
        }
        if (status.getStatusWert() == 0) {
            return new Response(false, 699, null);
        }

        UserInfo userInfo = authorization.getInfo(request);
        if (!userInfo.getMessstellen().contains(status.getErzeuger())) {
            return new Response(false, 699, null);
        }
        LMessung messung = defaultRepo.getByIdPlain(
            LMessung.class, status.getMessungsId(), "land");
        LStatusProtokoll statusNew = new LStatusProtokoll();
        statusNew.setDatum(status.getDatum());
        statusNew.setErzeuger(status.getErzeuger());
        statusNew.setMessungsId(status.getMessungsId());
        statusNew.setStatusStufe(status.getStatusStufe());
        statusNew.setStatusWert(status.getStatusWert());
        statusNew.setText(status.getText());
        Response response = defaultRepo.create(statusNew, "land");
        LStatusProtokoll created = (LStatusProtokoll)response.getData();
        messung.setStatus(created.getId());
        defaultRepo.update(messung, "land");

        return authorization.filter(
            request,
            response,
            LStatusProtokoll.class);
    }

    /**
     * Delete an existing Status object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/status/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        /* Get the object by id*/
        Response object =
            defaultRepo.getById(LStatusProtokoll.class, Integer.valueOf(id), "land");
        LStatusProtokoll obj = (LStatusProtokoll)object.getData();
        if (!authorization.isAuthorized(
                request,
                obj,
                RequestMethod.DELETE,
                LStatusProtokoll.class)
        ) {
            return new Response(false, 699, null);
        }
        if (lock.isLocked(obj)) {
            return new Response(false, 697, null);
        }
        /* Delete the object*/
        return defaultRepo.delete(obj, "land");
    }
}
