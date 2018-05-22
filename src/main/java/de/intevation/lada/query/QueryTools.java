/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.query;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.hibernate.jpa.internal.QueryImpl;

import de.intevation.lada.model.stammdaten.Filter;
import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.BaseQuery;
import de.intevation.lada.model.stammdaten.Result;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;


/**
 * Utility class to handle the SQL query configuration.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class QueryTools
{

    @Inject
    @RepositoryConfig(type=RepositoryType.RO)
    private Repository repository;

    @Inject
    private Logger logger;
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getResultForQuery(
        MultivaluedMap<String, String> params,

        Integer qId
    ) {
        return null;
    }
    /**
     * Execute query and return the filtered and sorted results.
     * @param customColumns Customized column configs, containing filter, sorting and references to the respective column.
     * @param qId Query id.
     * @return List of result maps.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getResultForQuery(
        List<GridColumnValue> customColumns,
        Integer qId
    ) {
        //A pattern for finding multiselect date filter values
        Pattern multiselectDatePattern = Pattern.compile("[0-9]*,[0-9]*");

        QueryBuilder<BaseQuery> builder = new QueryBuilder<BaseQuery>(
            repository.entityManager(Strings.STAMM),
            BaseQuery.class
        );
        builder.and("id", qId);
        BaseQuery query = repository.filterPlain(builder.getQuery(), Strings.STAMM).get(0);

        String sql = query.getSql();

        List<GridColumn> columns = new ArrayList<GridColumn>();
        //Map containing all sort statements, sorted by sortIndex
        TreeMap<Integer, String> sortIndMap = new TreeMap<Integer, String>();
        //Map containing all filters and filter values
        MultivaluedMap<String, String> filterValues = new MultivaluedHashMap<String, String>();
        String filterSql = "";
        String sortSql = "";

        for (GridColumnValue customColumn : customColumns) {

            //Build ORDER BY clause
            columns.add(customColumn.getGridColumn());
            if (customColumn.getSort() != null
                && !customColumn.getSort().isEmpty()) {

                String sortValue = customColumn.getGridColumn().getDataIndex() + " "
                        + customColumn.getSort() + " ";
                Integer key = customColumn.getSortIndex() != null ? customColumn.getSortIndex() : -1;
                String value = sortIndMap.get(key);
                value = value != null ? value + ", "  + sortValue : sortValue;
                sortIndMap.put(key, value);
            }

            if (customColumn.getFilterActive() != null
                    && customColumn.getFilterActive() == true) {
                //Build WHERE clause
                if (filterSql.isEmpty()) {
                    filterSql += " WHERE ";
                } else {
                    filterSql += " AND ";
                }
                Filter filter = customColumn.getGridColumn().getFilter();
                String filterValue = customColumn.getFilterValue();

                if (filter.getFilterType().getMultiselect() == false) {
                    filterValues.add(filter.getParameter(), filterValue);
                } else {
                    //If filter is a multiselect date filter
                    if (filter.getFilterType().getType().equals("listdatetime")) {
                        //Get parameters as comma separated values, expected to be in milliseconds
                        String[] params = filter.getParameter().split(",");
                        Matcher matcher = multiselectDatePattern.matcher(filterValue);
                        if (matcher.find()) {
                            String[] values = matcher.group(0).split(",", -1);
                            //Get filter values and convert to seconds
                            long from = values[0].equals("") ? 0: Long.valueOf(values[0])/1000;
                            long to = values[1].equals("") ? Integer.MAX_VALUE: Long.valueOf(values[1])/1000;
                            //Add parameters and values to filter map
                            filterValues.add(params[0], String.valueOf(from));
                            filterValues.add(params[1], String.valueOf(to));
                        }
                    }else {
                        //else add all filtervalues to the same parameter name
                        String[] multiselect = filterValue.split(",");
                        for (String value : multiselect) {
                            filterValues.add(filter.getParameter(), value);
                        }
                    }
                }
                filterSql += filter.getSql();
            }
        }

        if (sortIndMap.size() > 0) {
            NavigableMap <Integer, String> orderedSorts = sortIndMap.tailMap(0, true);
            String unorderedSorts = sortIndMap.get(-1);
            sortSql += "";
            for (String sortString : orderedSorts.values()) {
                if (sortSql.isEmpty()){
                    sortSql += " ORDER BY " + sortString;
                } else {
                    sortSql += ", " + sortString;
                }
            }
            if (unorderedSorts!= null && !unorderedSorts.isEmpty()) {
                if (sortSql.isEmpty()){
                    sortSql += " ORDER BY " + unorderedSorts;
                } else {
                    sortSql += ", " + unorderedSorts;
                }
            }

        }

        if (!filterSql.isEmpty()){
            sql += filterSql + " ";
        }
        sql += sortSql + ";";
        javax.persistence.Query q = prepareQuery(
            sql,
            filterValues,
            repository.entityManager(Strings.LAND));
        if (q == null) {
            return new ArrayList<>();
        }
        return prepareResult(q.getResultList(), columns);
    }

    public List<Map<String, Object>> filterResult(
        String filter,
        List<Map<String, Object>> items
    ) {
        return null;
    }

    /**
     * Creates a query from a given sql and inserts the given parameters.
     * @param sql The query sql string
     * @param params A map containing parameter names and values
     * @param manager Entity manager
     */
    public javax.persistence.Query prepareQuery(
        String sql,
        MultivaluedMap<String, String> params,
        EntityManager manager
    ) {
        javax.persistence.Query query = manager.createNativeQuery(sql);
        Set<String> keys = params.keySet();
        for(String key : keys) {
            List<String> values = new ArrayList<String>();
            for (String value: params.get(key)) {
                values.add(value);
            }
            query.setParameter(key, values);
        }
        return query;

    }

    /**
     * Prepares the query result for the client,
     * @param result A list of query results
     * @param names The columns queried by the client
     * @return List of result maps, containing only the configured columns
     */
    public List<Map<String, Object>> prepareResult(
        List<Object[]> result,
        List<GridColumn> names
    ) {
        if (result.size() == 0) {
            return null;
        }
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Object[] row: result) {
            Map<String, Object> set = new HashMap<String, Object>();
            for (int i = 0; i < names.size(); i++) {
                set.put(names.get(i).getDataIndex(), row[names.get(i).getPosition() - 1]);
            }
            ret.add(set);
        }
        return ret;
    }
}