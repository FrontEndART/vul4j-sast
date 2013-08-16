package de.intevation.lada.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.intevation.lada.model.query.QueryConfig;
import de.intevation.lada.model.query.QueryFilter;
import de.intevation.lada.model.query.ResultConfig;


public class QueryTools
{
    public static String readConfigFile() {
        String file = System.getProperty("de.intevation.lada.sqlconfig");
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(file));
            Charset encoding = Charset.defaultCharset();
            return encoding.decode(ByteBuffer.wrap(encoded)).toString();
        }
        catch (IOException ioe) {
            return null;
        }
    }

    public static List<QueryConfig> getConfig() {
        String content = readConfigFile();
        if (content == null) {
            return null;
        }
        List<QueryConfig> configs = new ArrayList<QueryConfig>();
        JSONArray queries;
        try {
            queries = new JSONArray(content);
            for (int i = 0; i < queries.length(); i++) {
                JSONObject query = queries.getJSONObject(i);
                QueryConfig qConf = new QueryConfig();
                qConf.setId(query.getInt("id"));
                qConf.setName(query.getString("name"));
                qConf.setDescription(query.getString("description"));
                qConf.setSql(query.getString("sql"));
                JSONArray filters = query.getJSONArray("filters");
                List<QueryFilter> qFilters = new ArrayList<QueryFilter>();
                for (int j = 0; j < filters.length(); j++) {
                    JSONObject filter = filters.getJSONObject(j);
                    QueryFilter qFilter = new QueryFilter();
                    qFilter.setDataIndex(filter.getString("dataIndex"));
                    qFilter.setType(filter.getString("type"));
                    qFilter.setLabel(filter.getString("label"));
                    qFilters.add(qFilter);
                }
                qConf.setFilters(qFilters);
                JSONArray results = query.getJSONArray("result");
                List<ResultConfig> sResults = new ArrayList<ResultConfig>();
                for (int k = 0; k < results.length(); k++) {
                    JSONObject result = results.getJSONObject(k);
                    ResultConfig config = new ResultConfig();
                    config.setDataIndex(result.getString("dataIndex"));
                    config.setHeader(result.getString("header"));
                    config.setWidth(result.optInt("width", 100));
                    config.setFlex(result.optInt("flex", 0));
                    sResults.add(config);
                }
                qConf.setResults(sResults);
                configs.add(qConf);
            }
        }
        catch (JSONException e) {
            return null;
        }
        return configs;
    }

    public static JSONObject getQueryById(String id) {
        try {
            String content = readConfigFile();
            if (content != null) {
                JSONArray queries = new JSONArray(content);
                for (int i = 0; i < queries.length(); i++) {
                    JSONObject query = queries.getJSONObject(i);
                    if (query.getString("id").equals(id)) {
                        return query;
                    }
                }
            }
            return null;
        }
        catch (JSONException e) {
            return null;
        }
    }
}
