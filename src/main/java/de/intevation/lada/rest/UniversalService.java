/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.deser.impl.NoClassDefFoundDeserializer;

import de.intevation.lada.model.QueryColumns;
import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.util.data.Strings;

/**
 * REST service for universal objects.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * </p>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/universal")
@RequestScoped
public class UniversalService {

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

    @Inject
    private QueryTools queryTools;


    /**
     * Execute query, using the given result columns
     */
    @POST
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public Response execute(
        @Context HttpHeaders headers,
        QueryColumns columns
    ) {
        Integer qid;

        List<GridColumnValue> gridColumnValues= columns.getColumns();


        if (gridColumnValues == null ||
                gridColumnValues.isEmpty()) {
            //TODO: Error code if no columns are given
            return new Response(false, 999, null);
        }
        //TODO gridcolumns are not fetched
        for (GridColumnValue columnValue : gridColumnValues) {
            columnValue.setGridColumn(repository.getByIdPlain(
                GridColumn.class,
                Integer.valueOf(columnValue.getGridColumnId()),
            Strings.STAMM));
    
        }
        GridColumn gridColumn = repository.getByIdPlain(
            GridColumn.class,
            Integer.valueOf(gridColumnValues.get(0).getGridColumnId()),
        Strings.STAMM);

        qid = gridColumn.getQuery();

        List<Map<String, Object>> result =
            queryTools.getResultForQuery(columns.getColumns(), qid);
        return new Response(true, 200, result);
    }

    /**
     * Get all objects.
     * <p>
     * The requested objects can be filtered using the following URL
     * parameters:<br>
     *  * qid: The id of the query.<br>
     *  * page: The page to display in a paginated result grid.<br>
     *  * start: The first Probe item.<br>
     *  * limit: The count of Probe items.<br>
     *  * sort: Sort the result ascending(ASC) or descenting (DESC).<br>
     *  <br>
     *  The response data contains a set of objects. The returned fields
     *  are defined in the query used in the request.
     * <p>
     * Example:
     *
     * @return Response object containing all Probe objects.
     */
    @GET
    @Path("/")
    @Produces("application/json")
    public Response get(
        @Context HttpHeaders headers,
        @Context UriInfo info,
        @Context HttpServletRequest request
    ) {
        /*

        MultivaluedMap<String, String> params = info.getQueryParameters();
        if (params.isEmpty() || !params.containsKey("qid")) {
            return new Response(false, 603, "Not a valid filter id");
        }
        
        Integer id = null;
        try {
            id = Integer.valueOf(params.getFirst("qid"));
        }
        catch (NumberFormatException e) {
            return new Response(false, 603, "Not a valid filter id");
        }
        List<Map<String, Object>> result =
            queryTools.getResultForQuery(params, id);

        List<Map<String, Object>> filtered;
        if (params.containsKey("filter")) {
            filtered = queryTools.filterResult(params.getFirst("filter"), result);
        }
        else {
            filtered = result;
        }

        if (filtered.isEmpty()) {
            return new Response(true, 200, filtered, 0);
        }

        int size = filtered.size();
        if (params.containsKey("start") && params.containsKey("limit")) {
            int start = Integer.valueOf(params.getFirst("start"));
            int limit = Integer.valueOf(params.getFirst("limit"));
            int end = limit + start;
            if (start + limit > filtered.size()) {
                end = filtered.size();
            }
            filtered = filtered.subList(start, end);
        }

        return new Response(true, 200, filtered, size);
        */
        return new Response(true, 200, null);
    }
}
