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

import org.apache.log4j.Logger;

import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.LMessung;
import de.intevation.lada.model.land.LMesswert;
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
 * REST service for Messwert objects.
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
 *      "grenzwertueberschreitung": [boolean],
 *      "letzteAenderung": [timestamp],
 *      "mehId": [number],
 *      "messfehler": [number],
 *      "messgroesseId": [number],
 *      "messungsId": [number],
 *      "messwert": [number],
 *      "messwertNwg": [string],
 *      "nwgZuMesswert": [number],
 *      "owner": [boolean],
 *      "readonly":[boolean],
 *      "treeModified": [timestamp],
 *      "parentModified": [timestamp]
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
@Path("messwert")
@RequestScoped
public class MesswertService {

    /**
     * The logger used in this class.
     */
    @Inject
    private Logger logger;

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
    @ValidationConfig(type="Messwert")
    private Validator validator;

    /**
     * Get all Messwert objects.
     * <p>
     * The requested objects can be filtered using a URL parameter named
     * messungsId.
     * <p>
     * Example: http://example.com/messwert?messungsId=[ID]
     *
     * @return Response object containing all Messwert objects.
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
            return new Response(false, 699, null);
        }
        String messungId = params.getFirst("messungsId");
        int id;
        try {
            id = Integer.valueOf(messungId);
        }
        catch(NumberFormatException nfe) {
            return new Response(false, 698, null);
        }
        LMessung messung = defaultRepo.getByIdPlain(
            LMessung.class,
            id,
            "land");
        if (!authorization.isAuthorized(authorization.getInfo(request), messung)) {
            if (!authorization.isAuthorized(id, LMessung.class)) {
                return new Response(false, 699, null);
            }
        }
        QueryBuilder<LMesswert> builder =
            new QueryBuilder<LMesswert>(
                defaultRepo.entityManager("land"),
                LMesswert.class);
        builder.and("messungsId", messungId);
        return authorization.filter(
            request,
            defaultRepo.filter(builder.getQuery(), "land"),
            LMesswert.class);
    }

    /**
     * Get a Messwert object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messwert/{id}
     *
     * @return Response object containing a single Messwert.
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
            defaultRepo.getById(LMesswert.class, Integer.valueOf(id), "land");
        LMesswert messwert = (LMesswert)response.getData();
        LMessung messung = defaultRepo.getByIdPlain(
            LMessung.class,
            messwert.getMessungsId(),
            "land");
        if (!authorization.isAuthorized(authorization.getInfo(request), messung)) {
            if (!authorization.isAuthorized(messung.getId(), LMessung.class)) {
                return new Response(false, 699, null);
            }
        }
        Violation violation = validator.validate(messwert);
        if (violation.hasErrors() || violation.hasWarnings()) {
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            request,
            response,
            LMesswert.class);
    }

    /**
     * Create a Messwert object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "messgroesseId": [number],
     *  "messwert": [number],
     *  "messwertNwg": [string],
     *  "messfehler": [number],
     *  "nwgZuMesswert": [number],
     *  "mehId": [number],
     *  "grenzwertueberschreitung": [boolean],
     *  "treeModified": null,
     *  "parentModified": null,
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return A response object containing the created Messwert.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        LMesswert messwert
    ) {
        if (!authorization.isAuthorized(
                request,
                messwert,
                RequestMethod.POST,
                LMesswert.class)
        ) {
            return new Response(false, 699, null);
        }
        Violation violation = validator.validate(messwert);
        if (violation.hasErrors()) {
            Response response = new Response(false, 604, messwert);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        /* Persist the new messung object*/
        Response response = defaultRepo.create(messwert, "land");
        if(violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            request,
            response,
            LMesswert.class);
    }

    /**
     * Update an existing Messwert object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "owner": [boolean],
     *  "messungsId": [number],
     *  "messgroesseId": [number],
     *  "messwert": [number],
     *  "messwertNwg": [string],
     *  "messfehler": [number],
     *  "nwgZuMesswert": [number],
     *  "mehId": [number],
     *  "grenzwertueberschreitung": [boolean],
     *  "treeModified": [timestamp],
     *  "parentModified": [timestamp],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Messwert object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        LMesswert messwert
    ) {
        if (!authorization.isAuthorized(
                request,
                messwert,
                RequestMethod.PUT,
                LMesswert.class)
        ) {
            return new Response(false, 699, null);
        }
        if (lock.isLocked(messwert)) {
            return new Response(false, 697, null);
        }
        Violation violation = validator.validate(messwert);
        if (violation.hasErrors()) {
            Response response = new Response(false, 604, messwert);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        messwert.setLetzteAenderung(new Timestamp(new Date().getTime()));
        Response response = defaultRepo.update(messwert, "land");
        Response updated = defaultRepo.getById(
            LMesswert.class,
            ((LMesswert)response.getData()).getId(), "land");
        if(violation.hasWarnings()) {
            updated.setWarnings(violation.getWarnings());
        }

        return authorization.filter(
            request,
            updated,
            LMesswert.class);
    }

    /**
     * Delete an existing Messwert object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messwert/{id}
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
        /* Get the messwert object by id*/
        Response messwert =
            defaultRepo.getById(LMesswert.class, Integer.valueOf(id), "land");
        LMesswert messwertObj = (LMesswert)messwert.getData();
        if (!authorization.isAuthorized(
                request,
                messwertObj,
                RequestMethod.DELETE,
                LMesswert.class)
        ) {
            return new Response(false, 699, null);
        }
        if (lock.isLocked(messwert)) {
            return new Response(false, 697, null);
        }
        /* Delete the messwert object*/
        return defaultRepo.delete(messwertObj, "land");
    }
}
