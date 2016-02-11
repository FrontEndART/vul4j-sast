/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.sql.Timestamp;
import java.util.Date;

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
import de.intevation.lada.model.land.LProbe;
import de.intevation.lada.model.land.LStatusProtokoll;
import de.intevation.lada.model.land.MessungTranslation;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;

/**
 * REST service for Messung objects.
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
 *      "fertig": [boolean],
 *      "letzteAenderung": [timestamp],
 *      "messdauer": [number],
 *      "messzeitpunkt": [timestamp],
 *      "mmtId": [string],
 *      "probeId": [number],
 *      "owner": [boolean],
 *      "readonly": [boolean],
 *      "nebenprobenNr": [string],
 *      "geplant": [boolean],
 *      "treeModified": [timestamp],
 *      "parentModified": [timestamp],
 *      "messungsIdAlt": [number]
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
@Path("rest/messung")
@RequestScoped
public class MessungService {

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

    @Inject
    @ValidationConfig(type="Messung")
    private Validator validator;

    /**
     * Get all Messung objects.
     * <p>
     * The requested objects can be filtered using a URL parameter named
     * probeId.
     * <p>
     * Example: http://example.com/messung?probeId=[ID]
     *
     * @return Response object containing all Messung objects.
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
        if (params.isEmpty() || !params.containsKey("probeId")) {
            return defaultRepo.getAll(LMessung.class, "land");
        }
        String probeId = params.getFirst("probeId");
        QueryBuilder<LMessung> builder =
            new QueryBuilder<LMessung>(
                defaultRepo.entityManager("land"),
                LMessung.class);
        builder.and("probeId", probeId);
        return authorization.filter(
            request,
            defaultRepo.filter(builder.getQuery(), "land"),
            LMessung.class);
    }

    /**
     * Get a Messung object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messung/{id}
     *
     * @return Response object containing a single Messung.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        Response response =
            defaultRepo.getById(LMessung.class, Integer.valueOf(id), "land");
        LMessung messung = (LMessung)response.getData();
        Violation violation = validator.validate(messung);
        if (violation.hasErrors() || violation.hasWarnings()) {
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            request,
            response,
            LMessung.class);
    }

    /**
     * Create a Messung object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "probeId": [number],
     *  "mmtId": [string],
     *  "nebenprobenNr": [string],
     *  "messdauer": [number],
     *  "fertig": [boolean],
     *  "geplant": [boolean],
     *  "messungsIdAlt": [string],
     *  "treeModified": null,
     *  "parentModified": null,
     *  "messzeitpunkt": [date],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Messung.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        LMessung messung
    ) {
        if (!authorization.isAuthorized(
                request,
                messung,
                RequestMethod.POST,
                LMessung.class)
        ) {
            return new Response(false, 699, null);
        }

        Violation violation = validator.validate(messung);
        if (violation.hasErrors()) {
            Response response = new Response(false, 604, messung);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        /* Persist the new messung object*/
        Response response = defaultRepo.create(messung, "land");
        LMessung ret = (LMessung)response.getData();
        /* Create and persist a new probe translation object*/
        MessungTranslation trans = new MessungTranslation();
        trans.setMessungsId(ret);
        defaultRepo.create(trans, "land");
        /* Get and return the new probe object*/
        Response created =
            defaultRepo.getById(LMessung.class, ret.getId(), "land");
        if(violation.hasWarnings()) {
            created.setWarnings(violation.getWarnings());
        }

        LStatusProtokoll status = new LStatusProtokoll();
        status.setDatum(new Timestamp(new Date().getTime()));
        status.setMessungsId(((LMessung)created.getData()).getId());
        LProbe probe =
            defaultRepo.getByIdPlain(LProbe.class, ret.getProbeId(), "land");
        status.setErzeuger(probe.getMstId());
        status.setStatusStufe(1);
        status.setStatusWert(0);
        defaultRepo.create(status, "land");
        ret.setStatus(status.getId());
        defaultRepo.update(ret, "land");
        Response updated=
            defaultRepo.getById(LMessung.class, ret.getId(), "land");

        return authorization.filter(
            request,
            updated,
            LMessung.class);
    }

    /**
     * Update an existing Messung object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "probeId": [number],
     *  "mmtId": [string],
     *  "nebenprobenNr": [string],
     *  "messdauer": [number],
     *  "fertig": [boolean],
     *  "geplant": [boolean],
     *  "messungsIdAlt": [number],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "messzeitpunkt": [date],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Messung object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        LMessung messung
    ) {
        if (!authorization.isAuthorized(
                request,
                messung,
                RequestMethod.PUT,
                LMessung.class)
        ) {
            return new Response(false, 699, null);
        }
        if (lock.isLocked(messung)) {
            return new Response(false, 697, null);
        }
        Violation violation = validator.validate(messung);
        if (violation.hasErrors()) {
            Response response = new Response(false, 604, messung);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }
        messung.setLetzteAenderung(new Timestamp(new Date().getTime()));
        Response response = defaultRepo.update(messung, "land");
        Response updated = defaultRepo.getById(
            LMessung.class,
            ((LMessung)response.getData()).getId(), "land");
        if(violation.hasWarnings()) {
            updated.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            request,
            updated,
            LMessung.class);
    }

    /**
     * Delete an existing Messung object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messung/{id}
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
        /* Get the messung object by id*/
        Response messung =
            defaultRepo.getById(LMessung.class, Integer.valueOf(id), "land");
        LMessung messungObj = (LMessung)messung.getData();
        if (!authorization.isAuthorized(
                request,
                messungObj,
                RequestMethod.DELETE,
                LMessung.class)
        ) {
            return new Response(false, 699, null);
        }
        if (lock.isLocked(messung)) {
            return new Response(false, 697, null);
        }

        /* Delete the messung object*/
        return defaultRepo.delete(messungObj, "land");
    }
}
