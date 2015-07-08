/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import de.intevation.lada.model.stamm.Messgroesse;
import de.intevation.lada.model.stamm.MmtMessgroesse;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for Messgroesse objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id":[number],
 *      "beschreibung": [string],
 *      "defaultFarbe": [string],
 *      "eudfNuklidId": [number],
 *      "idfNuklidKey": [string],
 *      "istLeitnuklid": [boolean],
 *      "kennungBvl": [string],
 *      "messgroesse": [string]
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
@Path("messgroesse")
@RequestScoped
public class MessgroesseService {

    @Inject
    private Logger logger = Logger.getLogger(MessgroesseService.class);

    /**
     * The data repository granting read access.
     */
    @Inject
    @RepositoryConfig(type=RepositoryType.RO)
    private Repository defaultRepo;

    /**
     * Get all Messgroesse objects.
     * <p>
     * Example: http://example.com/messgroesse
     *
     * @return Response object containing all Messgroesse objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("mmtId")) {
            logger.debug("no filter");
            return defaultRepo.getAll(Messgroesse.class, "stamm");
        }
        String mmtId = params.getFirst("mmtId");
        if (mmtId.length() > 3) {
            return new Response(false, 400, "bad request");
        }

        Query query =
            defaultRepo.queryFromString(
                "select messgroesse_id from mmt_messgroesse where mmt_id = '"
                + mmtId + "'", "stamm");
        List<Integer> ids = query.getResultList();
        QueryBuilder<Messgroesse> builder2 =
            new QueryBuilder<Messgroesse>(
                defaultRepo.entityManager("stamm"),
                Messgroesse.class);
        builder2.orIntList("id", ids);
        return defaultRepo.filter(builder2.getQuery(), "stamm");
    }

    /**
     * Get a single Messgroesse object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/messgroesse/{id}
     *
     * @return Response object containing a single Messgroesse.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpHeaders headers,
        @PathParam("id") String id
    ) {
        return defaultRepo.getById(
            Messgroesse.class,
            Integer.valueOf(id),
            "stamm");
    }
}
