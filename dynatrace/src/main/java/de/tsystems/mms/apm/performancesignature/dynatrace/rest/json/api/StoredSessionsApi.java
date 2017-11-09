/*
 * Copyright (c) 2014 T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tsystems.mms.apm.performancesignature.dynatrace.rest.json.api;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.json.*;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.json.model.DeletedStoredSessions;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.json.model.SessionMetadata;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.json.model.Sessions;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoredSessionsApi {
    private ApiClient apiClient;

    public StoredSessionsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public StoredSessionsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for deleteStoredSession
     *
     * @param sessionid Unique session id (required)
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call deleteStoredSessionCall(String sessionid) throws ApiException {
        // create path and map variables
        String localVarPath = ApiClient.API_SUFFIX + "/sessions/{sessionid}"
                .replaceAll("\\{sessionid\\}", apiClient.escapeString(sessionid));

        List<Pair> localVarQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"basicAuth"};
        return apiClient.buildCall(localVarPath, "DELETE", localVarQueryParams, null, localVarHeaderParams, localVarFormParams, localVarAuthNames);
    }

    @SuppressWarnings("rawtypes")
    private Call deleteStoredSessionValidateBeforeCall(String sessionid) throws ApiException {
        // verify the required parameter 'sessionid' is set
        if (sessionid == null) {
            throw new ApiException("Missing the required parameter 'sessionid' when calling deleteStoredSession");
        }

        return deleteStoredSessionCall(sessionid);
    }

    /**
     * Delete individual stored session
     * Delete a stored session.
     *
     * @param sessionid Unique session id (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public void deleteStoredSession(String sessionid) throws ApiException {
        deleteStoredSessionWithHttpInfo(sessionid);
    }

    /**
     * Delete individual stored session
     * Delete a stored session.
     *
     * @param sessionid Unique session id (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Void> deleteStoredSessionWithHttpInfo(String sessionid) throws ApiException {
        Call call = deleteStoredSessionValidateBeforeCall(sessionid);
        return apiClient.execute(call);
    }

    /**
     * Build call for deleteStoredSessions
     *
     * @param type             Type of sessions which shall be deleted (required)
     * @param olderthanminutes The minimum age in minutes a stored session must have to be deleted. The value 0 will delete stored sessions regardless of their age (required)
     * @param profile          If specified, the deletion will only affect stored sessions of this System Profile. Otherwise the System Profile will be irrelevant for deletion (optional)
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call deleteStoredSessionsCall(String type, String olderthanminutes, String profile) throws ApiException {
        // create path and map variables
        String localVarPath = ApiClient.API_SUFFIX + "/sessions";

        List<Pair> localVarQueryParams = new ArrayList<>();
        if (type != null)
            localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
        if (olderthanminutes != null)
            localVarQueryParams.addAll(apiClient.parameterToPairs("", "olderthanminutes", olderthanminutes));
        if (profile != null)
            localVarQueryParams.addAll(apiClient.parameterToPairs("", "profile", profile));

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"basicAuth"};
        return apiClient.buildCall(localVarPath, "DELETE", localVarQueryParams, null, localVarHeaderParams, localVarFormParams, localVarAuthNames);
    }

    @SuppressWarnings("rawtypes")
    private Call deleteStoredSessionsValidateBeforeCall(String type, String olderthanminutes, String profile) throws ApiException {
        // verify the required parameter 'type' is set
        if (type == null) {
            throw new ApiException("Missing the required parameter 'type' when calling deleteStoredSessions");
        }

        // verify the required parameter 'olderthanminutes' is set
        if (olderthanminutes == null) {
            throw new ApiException("Missing the required parameter 'olderthanminutes' when calling deleteStoredSessions");
        }

        return deleteStoredSessionsCall(type, olderthanminutes, profile);
    }

    /**
     * Delete multiple stored sessions
     * Delete every stored session that matches a specified session type and is older than a specified number of minutes. Additionally the deletion request can be restricted to stored sessions of a specific System Profile.
     *
     * @param type             Type of sessions which shall be deleted (required)
     * @param olderthanminutes The minimum age in minutes a stored session must have to be deleted. The value 0 will delete stored sessions regardless of their age (required)
     * @param profile          If specified, the deletion will only affect stored sessions of this System Profile. Otherwise the System Profile will be irrelevant for deletion (optional)
     * @return DeletedStoredSessions
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public DeletedStoredSessions deleteStoredSessions(String type, String olderthanminutes, String profile) throws ApiException {
        ApiResponse<DeletedStoredSessions> resp = deleteStoredSessionsWithHttpInfo(type, olderthanminutes, profile);
        return resp.getData();
    }

    /**
     * Delete multiple stored sessions
     * Delete every stored session that matches a specified session type and is older than a specified number of minutes. Additionally the deletion request can be restricted to stored sessions of a specific System Profile.
     *
     * @param type             Type of sessions which shall be deleted (required)
     * @param olderthanminutes The minimum age in minutes a stored session must have to be deleted. The value 0 will delete stored sessions regardless of their age (required)
     * @param profile          If specified, the deletion will only affect stored sessions of this System Profile. Otherwise the System Profile will be irrelevant for deletion (optional)
     * @return ApiResponse&lt;DeletedStoredSessions&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<DeletedStoredSessions> deleteStoredSessionsWithHttpInfo(String type, String olderthanminutes, String profile) throws ApiException {
        Call call = deleteStoredSessionsValidateBeforeCall(type, olderthanminutes, profile);
        Type localVarReturnType = new TypeToken<DeletedStoredSessions>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Build call for getStoredSession
     *
     * @param sessionid                 Unique session id (required)
     * @param removeconfidentialstrings true to remove confidential strings from the exported session, false to keep them included (optional, default to true)
     * @param timeframestart            Timeframe filter start time timestamp (ISO8601) (optional)
     * @param timeframeend              Timeframe filter end time timestamp (ISO8601) (optional)
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call getStoredSessionCall(String sessionid, Boolean removeconfidentialstrings, String timeframestart, String timeframeend) throws ApiException {
        // create path and map variables
        String localVarPath = ApiClient.API_SUFFIX + "/sessions/{sessionid}"
                .replaceAll("\\{sessionid\\}", apiClient.escapeString(sessionid));

        List<Pair> localVarQueryParams = new ArrayList<>();
        if (removeconfidentialstrings != null) {
            localVarQueryParams.addAll(apiClient.parameterToPairs("", "removeconfidentialstrings", removeconfidentialstrings));
        }
        if (timeframestart != null) {
            localVarQueryParams.addAll(apiClient.parameterToPairs("", "timeframestart", timeframestart));
        }
        if (timeframeend != null) {
            localVarQueryParams.addAll(apiClient.parameterToPairs("", "timeframeend", timeframeend));
        }

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/octet-stream"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"basicAuth"};
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, null, localVarHeaderParams, localVarFormParams, localVarAuthNames);
    }

    @SuppressWarnings("rawtypes")
    private Call getStoredSessionValidateBeforeCall(String sessionid, Boolean removeconfidentialstrings, String timeframestart, String timeframeend) throws ApiException {
        // verify the required parameter 'sessionid' is set
        if (sessionid == null) {
            throw new ApiException("Missing the required parameter 'sessionid' when calling getStoredSession");
        }

        return getStoredSessionCall(sessionid, removeconfidentialstrings, timeframestart, timeframeend);
    }

    /**
     * Export stored session
     * Export a stored session. For large stored sessions the size of the downloaded file can be huge. Make sure the download machine has enough free space to ensure that the download is successful.
     *
     * @param sessionid                 Unique session id (required)
     * @param removeconfidentialstrings true to remove confidential strings from the exported session, false to keep them included (optional, default to true)
     * @param timeframestart            Timeframe filter start time timestamp (ISO8601) (optional)
     * @param timeframeend              Timeframe filter end time timestamp (ISO8601) (optional)
     * @return File
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public File getStoredSession(String sessionid, Boolean removeconfidentialstrings, String timeframestart, String timeframeend) throws ApiException {
        ApiResponse<File> resp = getStoredSessionWithHttpInfo(sessionid, removeconfidentialstrings, timeframestart, timeframeend);
        return resp.getData();
    }

    /**
     * Export stored session
     * Export a stored session. For large stored sessions the size of the downloaded file can be huge. Make sure the download machine has enough free space to ensure that the download is successful.
     *
     * @param sessionid                 Unique session id (required)
     * @param removeconfidentialstrings true to remove confidential strings from the exported session, false to keep them included (optional, default to true)
     * @param timeframestart            Timeframe filter start time timestamp (ISO8601) (optional)
     * @param timeframeend              Timeframe filter end time timestamp (ISO8601) (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<File> getStoredSessionWithHttpInfo(String sessionid, Boolean removeconfidentialstrings, String timeframestart, String timeframeend) throws ApiException {
        Call call = getStoredSessionValidateBeforeCall(sessionid, removeconfidentialstrings, timeframestart, timeframeend);
        Type localVarReturnType = new TypeToken<File>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Build call for getStoredSessionMetaData
     *
     * @param sessionid Unique session id (required)
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call getStoredSessionMetaDataCall(String sessionid) throws ApiException {
        // create path and map variables
        String localVarPath = ApiClient.API_SUFFIX + "/sessions/{sessionid}/metadata"
                .replaceAll("\\{sessionid\\}", apiClient.escapeString(sessionid));

        List<Pair> localVarQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"basicAuth"};
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, null, localVarHeaderParams, localVarFormParams, localVarAuthNames);
    }

    @SuppressWarnings("rawtypes")
    private Call getStoredSessionMetaDataValidateBeforeCall(String sessionid) throws ApiException {
        // verify the required parameter 'sessionid' is set
        if (sessionid == null) {
            throw new ApiException("Missing the required parameter 'sessionid' when calling getStoredSessionMetaData");
        }

        return getStoredSessionMetaDataCall(sessionid);
    }

    /**
     * Get session metadata
     * Get comprehensive details of a stored session. For more information look at the response entity.
     *
     * @param sessionid Unique session id (required)
     * @return SessionMetadata
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public SessionMetadata getStoredSessionMetaData(String sessionid) throws ApiException {
        ApiResponse<SessionMetadata> resp = getStoredSessionMetaDataWithHttpInfo(sessionid);
        return resp.getData();
    }

    /**
     * Get session metadata
     * Get comprehensive details of a stored session. For more information look at the response entity.
     *
     * @param sessionid Unique session id (required)
     * @return ApiResponse&lt;SessionMetadata&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<SessionMetadata> getStoredSessionMetaDataWithHttpInfo(String sessionid) throws ApiException {
        Call call = getStoredSessionMetaDataValidateBeforeCall(sessionid);
        Type localVarReturnType = new TypeToken<SessionMetadata>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Build call for listStoredSessions
     *
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call listStoredSessionsCall() throws ApiException {
        // create path and map variables
        String localVarPath = ApiClient.API_SUFFIX + "/sessions";

        List<Pair> localVarQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"basicAuth"};
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, null, localVarHeaderParams, localVarFormParams, localVarAuthNames);
    }

    /**
     * List stored sessions
     * Get a list of all stored sessions which are available to the current user.
     *
     * @return Sessions
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public Sessions listStoredSessions() throws ApiException {
        ApiResponse<Sessions> resp = listStoredSessionsWithHttpInfo();
        return resp.getData();
    }

    /**
     * List stored sessions
     * Get a list of all stored sessions which are available to the current user.
     *
     * @return ApiResponse&lt;Sessions&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Sessions> listStoredSessionsWithHttpInfo() throws ApiException {
        Call call = listStoredSessionsCall();
        Type localVarReturnType = new TypeToken<Sessions>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }
}