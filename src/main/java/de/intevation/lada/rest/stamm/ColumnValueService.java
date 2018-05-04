/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;


import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.rest.Response;


/**
 * REST-Service for preconfigured queries.
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
 *      "id": [string],
 *      "name": [string],
 *      "description": [string],
 *      "sql": [string],
 *      "filters": [array],
 *      "results": [array]
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
@Path("rest/columnvalue")
@RequestScoped
public class ColumnValueService {

    @Inject
    @RepositoryConfig(type=RepositoryType.RW)
    private Repository repository;

    @Inject
    @AuthorizationConfig(type=AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Request all user defined grid_column_value objects
     * @return All GridColumnValue objects referencing the given query.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueries(
        @Context HttpServletRequest request,
        @Context UriInfo info
    ) {
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
        UserInfo userInfo = authorization.getInfo(request);
        EntityManager em = repository.entityManager(Strings.STAMM);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GridColumnValue> criteriaQuery = builder.createQuery(GridColumnValue.class);
        Root<GridColumnValue> root = criteriaQuery.from(GridColumnValue.class);
        Join<GridColumnValue, GridColumn> value = root.join("gridColumn", javax.persistence.criteria.JoinType.LEFT);
        Predicate filter = builder.equal(value.get("query"), id);
        Predicate uId = builder.equal(root.get("userId"), userInfo.getUserId());
        Predicate nullId = builder.isNull(root.get("userId"));
        Predicate userIdFilter = builder.or(uId, nullId);
        filter = builder.and(filter, userIdFilter);
        criteriaQuery.where(filter);
        List<GridColumnValue> queries = repository.filterPlain(criteriaQuery, Strings.STAMM);

        return new Response(true, 200, queries);
    }

    /**
     * Creates a new grid_column_value in the database
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpServletRequest request,
        GridColumnValue gridColumnValue
    ) {
        UserInfo userInfo = authorization.getInfo(request);
        if (gridColumnValue.getUserId() != null &&
            !gridColumnValue.getUserId().equals(userInfo.getUserId())) {
                return new Response(false, 699, null);
        } else {
            gridColumnValue.setUserId(userInfo.getUserId());
            GridColumn gridColumn = new GridColumn();
            gridColumn.setId(gridColumnValue.getGridColumnId());
            gridColumnValue.setGridColumn(gridColumn);

            return repository.create(gridColumnValue, Strings.STAMM);
        }

    }

    /**
     * Update an existing grid_column_value in the database
     */
    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpServletRequest request,
        GridColumnValue gridColumnValue
    ) {
        UserInfo userInfo = authorization.getInfo(request);
        if (gridColumnValue.getUserId() != null &&
            !gridColumnValue.getUserId().equals(userInfo.getUserId())) {
                return new Response(false, 699, null);
        } else {
            gridColumnValue.setUserId(userInfo.getUserId());
            GridColumn gridColumn = new GridColumn();
            gridColumn.setId(gridColumnValue.getGridColumnId());
            gridColumnValue.setGridColumn(gridColumn);

            return repository.update(gridColumnValue, Strings.STAMM);
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ){
        UserInfo userInfo = authorization.getInfo(request);
        GridColumnValue gridColumnValue = repository.getByIdPlain(
            GridColumnValue.class,
            Integer.valueOf(id),
            Strings.STAMM);
        if (gridColumnValue.getUserId().equals(userInfo.getUserId())) {
            return repository.delete(gridColumnValue, Strings.STAMM);
        }
        return new Response(false, 699, null);
    }

}
