/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;
import java.util.Map;

import javax.ejb.EJBTransactionRolledbackException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.TransactionRequiredException;
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

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.lock.LockConfig;
import de.intevation.lada.lock.LockType;
import de.intevation.lada.lock.ObjectLocker;
import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.query.QueryTools;
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
 * REST service for Probe objects.
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
 *      "id":[number],
 *      "baId": [string],
 *      "datenbasisId": [number],
 *      "letzteAenderung": [timestamp],
 *      "media": [string],
 *      "mediaDesk": [string],
 *      "mittelungsdauer": [number],
 *      "mstId": [string],
 *      "netzbetreiberId":[string],
 *      "probeentnahmeBeginn": [timestamp],
 *      "probeentnahmeEnde": [timestamp],
 *      "probenartId": [number],
 *      "test": [boolean],
 *      "umwId": [string],
 *      "hauptprobenNr": [string],
 *      "erzeugerId": [string],
 *      "mpKat": [string],
 *      "mplId": [number],
 *      "mprId": [number],
 *      "probeNehmerId": [number],
 *      "solldatumBeginn": [timestamp],
 *      "solldatumEnde": [timestamp],
 *      "treeModified": [timestamp],
 *      "readonly": [boolean],
 *      "owner": [boolean],
 *      "probeIdAlt": [string]
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
@Path("rest/probe")
@RequestScoped
public class ProbeService {

    /**
     * The data repository granting read/write access.
     */
    @Inject
    @RepositoryConfig(type=RepositoryType.RW)
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type=AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * The object lock mechanism.
     */
    @Inject
    @LockConfig(type=LockType.TIMESTAMP)
    private ObjectLocker lock;

    /**
     * The validator used for Probe objects.
     */
    @Inject
    @ValidationConfig(type="Probe")
    private Validator validator;

    /**
     * The factory to create Probe objects.
     * Used for messprogramm.
     */
    @Inject
    private ProbeFactory factory;

    @Inject
    private QueryTools queryTools;

    /**
     * Get all Probe objects.
     * <p>
     * The requested objects can be filtered using the following URL
     * parameters:<br>
     *  * qid: The id of the query.<br>
     *  * page: The page to display in a paginated result grid.<br>
     *  * start: The first Probe item.<br>
     *  * limit: The count of Probe items.<br>
     *  * sort: Sort the result ascending(ASC) or descenting (DESC).<br>
     *  <br>
     *  The response data contains a stripped set of Probe objects. The returned fields
     *  are defined in the query used in the request.
     * <p>
     * Example:
     * http://example.com/probe?qid=[ID]&page=[PAGE]&start=[START]&limit=[LIMIT]&sort=[{"property":"probeId","direction":"ASC"}]
     *
     * @return Response object containing all Probe objects.
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/")
    @Produces("application/json")
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info,
        @Context HttpServletRequest request
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("qid")) {
            return repository.getAll(Probe.class, "land");
        }
        Integer id = null;
        try {
            id = Integer.valueOf(params.getFirst("qid"));
        }
        catch (NumberFormatException e) {
            return new Response(false, 603, "Not a valid filter id");
        }
        List<Map<String, Object>> result =
            queryTools.getResultForQuery(params, id, "probe");

        int size = result.size();
        if (params.containsKey("start") && params.containsKey("limit")) {
            int start = Integer.valueOf(params.getFirst("start"));
            int limit = Integer.valueOf(params.getFirst("limit"));
            int end = limit + start;
            if (start + limit > result.size()) {
                end = result.size();
            }
            result = result.subList(start, end);
        }

        QueryBuilder<Probe> pBuilder = new QueryBuilder<Probe>(
            repository.entityManager("land"), Probe.class);
        for (Map<String, Object> entry: result) {
            pBuilder.or("id", (Integer)entry.get("id"));
        }
        Response r = repository.filter(pBuilder.getQuery(), "land");
        r = authorization.filter(request, r, Probe.class);
        List<Probe> proben = (List<Probe>)r.getData();
        for (Map<String, Object> entry: result) {
            Integer pId = Integer.valueOf(entry.get("id").toString());
            setAuthData(proben, entry, pId);
        }
        return new Response(true, 200, result, size);
    }

    private void setAuthData(
        List<Probe> proben,
        Map<String, Object> entry,
        Integer id
    ) {
        for (int i = 0; i < proben.size(); i++) {
            if (id.equals(proben.get(i).getId())) {
                entry.put("readonly", proben.get(i).isReadonly());
                entry.put("owner", proben.get(i).isOwner());
                return;
            }
        }
    }

    /**
     * Get a single Probe object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/probe/{id}
     *
     * @return Response object containing a single Probe.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id,
        @Context HttpServletRequest request
    ) {
        Response response =
            repository.getById(Probe.class, Integer.valueOf(id), "land");
        Violation violation = validator.validate(response.getData());
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }
        return this.authorization.filter(request, response, Probe.class);
    }

    /**
     * Create a new Probe object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "probeIdAlt": [string],
     *  "hauptprobenNr": [string],
     *  "test": [boolean],
     *  "netzbetreiberId": [string],
     *  "mstId": [string],
     *  "datenbasisId": [number],
     *  "baId": [string],
     *  "probenartId": [number],
     *  "mediaDesk": [string],
     *  "media": [string],
     *  "umwId": [string],
     *  "mittelungsdauer": [number],
     *  "erzeugerId":[string],
     *  "probeNehmerId": [number],
     *  "mpKat": [string],
     *  "mplId": [number],
     *  "mprId": [number],
     *  "treeModified":null,
     *  "probeentnahmeBeginn": [date],
     *  "probeentnahmeEnde": [date],
     *  "letzteAenderung": [date],
     *  "solldatumBeginn": [date],
     *  "solldatumEnde": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the new probe object.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        Probe probe
    ) {
        if (!authorization.isAuthorized(
                request,
                probe,
                RequestMethod.POST,
                Probe.class)
        ) {
            return new Response(false, 699, null);
        }
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Response response = new Response(false, 604, probe);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }
        if (probe.getUmwId() == null || probe.getUmwId().equals("")) {
            probe = factory.findUmweltId(probe);
        }
        probe = factory.findMediaDesk(probe);

        /* Persist the new probe object*/
        Response newProbe = repository.create(probe, "land");

        if(violation.hasWarnings()) {
            newProbe.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            request,
            newProbe,
            Probe.class);
    }

    /**
     * Create new Probe objects from a messprogramm.
     * <p>
     * <p>
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "start": [timestamp],
     *  "end": [timestamp]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the new probe object.
     */
    @POST
    @Path("/messprogramm")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFromMessprogramm(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        JsonObject object
    ) {
        int id = object.getInt("id");
        Messprogramm messprogramm = repository.getByIdPlain(
            Messprogramm.class, id, "land");
        if (messprogramm == null) {
            return new Response(false, 600, null);
        }
        // Use a dummy probe with same mstId as the messprogramm to authorize
        // the user to create probe objects.
        Probe testProbe = new Probe();
        testProbe.setMstId(messprogramm.getMstId());
        if (!authorization.isAuthorized(
                request,
                testProbe,
                RequestMethod.POST,
                Probe.class)
        ) {
            return new Response(false, 699, null);
        }

        long start = 0;
        long end = 0;
        try {
            start = object.getJsonNumber("start").longValue();
            end = object.getJsonNumber("end").longValue();
        } catch (ClassCastException e) {
            // Catch invalid (i.e. too high) time values
            return new Response(false, 612, null);
        }
        if (start > end) {
            return new Response(false, 662, null);
        }
        List<Probe> proben = factory.create(
            messprogramm,
            start,
            end);
        return new Response(true, 200, proben);
    }

    /**
     * Update an existing Probe object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "probeIdAlt": [string],
     *  "hauptprobenNr": [string],
     *  "test": [boolean],
     *  "netzbetreiberId": [string],
     *  "mstId": [string],
     *  "datenbasisId": [number],
     *  "baId": [string],
     *  "probenartId": [number],
     *  "mediaDesk": [string],
     *  "media": [string],
     *  "umwId": [string],
     *  "mittelungsdauer": [number],
     *  "erzeugerId": [number],
     *  "probeNehmerId": [number],
     *  "mpKat": [string],
     *  "mplId": [number],
     *  "mprId": [number],
     *  "treeModified": [timestamp],
     *  "probeentnahmeBeginn": [date],
     *  "probeentnahmeEnde": [date],
     *  "letzteAenderung": [date],
     *  "solldatumBeginn": [date],
     *  "solldatumEnde":[date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Probe object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpHeaders headers,
        @Context HttpServletRequest request,
        @PathParam("id") String id,
        Probe probe
    ) {
        if (!authorization.isAuthorized(
                request,
                probe,
                RequestMethod.PUT,
                Probe.class)
        ) {
            return new Response(false, 699, null);
        }
        if (lock.isLocked(probe)) {
            return new Response(false, 697, null);
        }
        if (probe.getMediaDesk() == null || probe.getMediaDesk() == "") {
            probe = factory.findMediaDesk(probe);
        }
        Violation violation = validator.validate(probe);
        if (violation.hasErrors()) {
            Response response = new Response(false, 604, null);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }
        if (probe.getUmwId() == null || probe.getUmwId() == "") {
            factory.findUmweltId(probe);
        }
        Response response = repository.update(probe, "land");
        if (!response.getSuccess()) {
            return response;
        }
        Response updated = repository.getById(
            Probe.class,
            ((Probe)response.getData()).getId(), "land");
        if (violation.hasWarnings()) {
            updated.setWarnings(violation.getWarnings());
        }
        return authorization.filter(
            request,
            updated,
            Probe.class);
    }

    /**
     * Delete an existing Probe object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/probe/{id}
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
        /* Get the probe object by id*/
        Response probe =
            repository.getById(Probe.class, Integer.valueOf(id), "land");
        if (!probe.getSuccess()) {
            return probe;
        }
        Probe probeObj = (Probe)probe.getData();
        if (!authorization.isAuthorized(
                request,
                probeObj,
                RequestMethod.DELETE,
                Probe.class)
        ) {
            return new Response(false, 699, null);
        }
        /* Delete the probe object*/
        try {
            Response response = repository.delete(probeObj, "land");
            return response;
        }
        catch(IllegalArgumentException | EJBTransactionRolledbackException |
            TransactionRequiredException e) {
            return new Response(false, 600, "");
        }
    }
}
