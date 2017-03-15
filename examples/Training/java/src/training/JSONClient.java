/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;

/**
 * An example for using the OMERO JSON API with Java
 * 
 * This example client needs additional dependencies: 
 *   Java API for JSON Processing: https://jsonp.java.net
 *   Apache HTTPComponents: https://hc.apache.org/index.html
 * 
 * Run it with command line parameters:
 * --omero.apiurl=http://web-dev-merge.openmicroscopy.org/api
 * --omero.host=eel-merge
 * --omero.user=xxx 
 * --omero.pass=xxx
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class JSONClient {

    /** The base API URL */
    private String baseURL;

    /** The base URL used for requests, including API version */
    private String requestURL;

    /** The URLs the API provides */
    private Map<String, String> serviceURLs;

    /** The http client */
    private HttpClient httpClient;

    /** The http context */
    private BasicHttpContext httpContext;

    /** The CSRF token **/
    private String token;

    /**
     * Creates a new JSON client
     * 
     * @param baseURL
     *            The base API URL
     */
    public JSONClient(String baseURL) {
        this.baseURL = baseURL;
        this.httpClient = HttpClients.createDefault();
        this.httpContext = new BasicHttpContext();
        BasicCookieStore cookieStore = new BasicCookieStore();
        cookieStore.clear();
        this.httpContext.setAttribute(HttpClientContext.COOKIE_STORE,
                cookieStore);
    }

    /**
     * Get the available API versions
     * 
     * @return See above
     * @throws Exception
     *             If something went wrong
     */
    public List<JsonObject> getVersion() throws Exception {
        JsonObject json = (JsonObject) get(baseURL);
        JsonArray jarray = json.getJsonArray("data");
        List<JsonObject> result = new ArrayList<JsonObject>();
        for (JsonValue value : jarray) {
            result.add((JsonObject) value);
        }

        JsonObject server = result.get(result.size() - 1);
        this.requestURL = server.getJsonString("url:base").getString();

        return result;
    }

    /**
     * Get the available URLs provided by the API
     * 
     * @return See above
     * @throws Exception
     *             If something went wrong
     */
    public Map<String, String> getURLs() throws Exception {
        JsonObject json = (JsonObject) get(requestURL);

        this.serviceURLs = new HashMap<String, String>();

        for (Entry<String, JsonValue> entry : json.entrySet()) {
            this.serviceURLs.put(entry.getKey(),
                    ((JsonString) entry.getValue()).getString());
        }

        return this.serviceURLs;
    }

    /**
     * Get the accessible servers
     * 
     * @return See above
     * @throws Exception
     *             If something went wrong
     */
    public Map<String, Integer> getServers() throws Exception {
        Map<String, Integer> result = new HashMap<String, Integer>();
        String url = serviceURLs.get("url:servers");
        JsonObject json = (JsonObject) get(url);
        JsonArray data = json.getJsonArray("data");
        for (int i = 0; i < data.size(); i++) {
            JsonObject server = data.getJsonObject(i);
            result.put(server.getString("server"), server.getInt("id"));
        }
        return result;
    }

    /**
     * Request a CSRF token
     * 
     * @return The CSRF token
     * @throws Exception
     *             If something went wrong
     */
    private String getCSRFToken() throws Exception {
        String url = serviceURLs.get("url:token");
        JsonObject json = (JsonObject) get(url);
        return json.getJsonString("data").getString();
    }

    /**
     * Log in a server
     * 
     * @param username
     *            The username
     * @param password
     *            The password
     * @param serverId
     *            The server id
     * @return See above
     * @throws Exception
     *             If something went wrong
     */
    public JsonObject login(String username, String password, int serverId)
            throws Exception {
        // make sure we have all the necessary URLs
        getVersion();
        getURLs();

        // make sure we have a CSRF token
        if (this.token == null)
            this.token = getCSRFToken();

        String url = serviceURLs.get("url:login");
        Map<String, String> params = new HashMap<String, String>();
        params.put("server", "" + serverId);
        params.put("username", username);
        params.put("password", password);
        try {
            JsonObject response = (JsonObject) post(url, params);
            JsonObject ctx = response.getJsonObject("eventContext");
            return ctx;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * List the available datasets
     * 
     * @return See above
     * @throws Exception
     *             If something went wrong
     */
    public Collection<JsonObject> listDatasets() throws Exception {
        List<JsonObject> result = new ArrayList<JsonObject>();
        String url = serviceURLs.get("url:datasets");
        JsonObject json = (JsonObject) get(url);
        JsonArray data = json.getJsonArray("data");
        for (int i = 0; i < data.size(); i++) {
            result.add(data.getJsonObject(i));
        }
        return result;
    }

    /**
     * List the images of a certain dataset
     * 
     * @param datasetId
     *            The dataset id
     * @return See above
     * @throws Exception
     *             If something went wrong
     */
    public Collection<JsonObject> listImages(int datasetId) throws Exception {
        List<JsonObject> result = new ArrayList<JsonObject>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("dataset", "" + datasetId);
        String url = serviceURLs.get("url:images");
        JsonObject json = (JsonObject) get(url, params);
        JsonArray data = json.getJsonArray("data");
        for (int i = 0; i < data.size(); i++) {
            result.add(data.getJsonObject(i));
        }
        return result;
    }

    /**
     * Update an object
     * 
     * @param object
     *            The JSON object
     * @return The updated object
     * @throws Exception
     *             If something went wrong
     */
    public JsonObject update(JsonObject object) throws Exception {
        String url = serviceURLs.get("url:save");
        JsonObject updatedObject = (JsonObject) put(url, object);
        return updatedObject;
    }

    /**
     * Perform a get request
     * 
     * @param urlString
     *            The request URL
     * @return The response
     * @throws Exception
     *             Exception If something went wrong
     */
    private JsonStructure get(String urlString) throws Exception {
        return get(urlString, null);
    }

    /**
     * Perform a GET request
     * 
     * @param urlString
     *            The request URL
     * @param params
     *            The parameters
     * @return The response
     * @throws Exception
     *             If something went wrong
     */
    private JsonStructure get(String urlString, Map<String, String> params)
            throws Exception {
        JsonReader reader = null;
        try {
            HttpGet httpGet = null;
            if (params == null || params.isEmpty())
                httpGet = new HttpGet(urlString);
            else {
                URIBuilder builder = new URIBuilder(urlString);
                for (Entry<String, String> e : params.entrySet()) {
                    builder.addParameter(e.getKey(), e.getValue());
                }
                httpGet = new HttpGet(builder.build());
            }
            HttpResponse res = httpClient.execute(httpGet);
            reader = Json.createReader(new BufferedReader(
                    new InputStreamReader(res.getEntity().getContent())));
            return reader.read();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    /**
     * Perform a PUT request
     * 
     * @param url
     *            The request URL
     * @param data
     *            The JSON data
     * @return The response
     * @throws HttpException
     *             If something went wrong
     * @throws ClientProtocolException
     *             If something went wrong
     * @throws IOException
     *             If something went wrong
     */
    private JsonStructure put(String url, JsonObject data)
            throws HttpException, ClientProtocolException, IOException {
        HttpPut httpPut = new HttpPut(url);
        if (data != null) {
            StringEntity requestEntity = new StringEntity(data.toString(),
                    ContentType.APPLICATION_JSON);
            httpPut.setEntity(requestEntity);
        }
        httpPut.addHeader("X-CSRFToken", this.token);
        HttpResponse res = httpClient.execute(httpPut);
        if (res.getStatusLine().getStatusCode() != 200)
            throw new HttpException("PUT failed. URL: " + url + " Status:"
                    + res.getStatusLine());

        try (JsonReader reader = Json.createReader(new BufferedReader(
                new InputStreamReader(res.getEntity().getContent())))) {
            return reader.read();
        }
    }

    /**
     * Perform a POST request
     * 
     * @param url
     *            The request URL
     * @param params
     *            The paramters
     * @return The response
     * @throws HttpException
     *             If something went wrong
     * @throws ClientProtocolException
     *             If something went wrong
     * @throws IOException
     *             If something went wrong
     */
    private JsonStructure post(String url, Map<String, String> params)
            throws HttpException, ClientProtocolException, IOException {

        HttpPost httpPost = new HttpPost(url);
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry
                        .getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        }
        httpPost.addHeader("X-CSRFToken", this.token);
        HttpResponse res = httpClient.execute(httpPost);
        if (res.getStatusLine().getStatusCode() != 200)
            throw new HttpException("POST failed. URL: " + url + " Status:"
                    + res.getStatusLine());

        try (JsonReader reader = Json.createReader(new BufferedReader(
                new InputStreamReader(res.getEntity().getContent())))) {
            return reader.read();
        }
    }

    /**
     * The main method
     * 
     * @param args
     *            Command line arguments
     * @throws Exception
     *             If something went wrong
     */
    public static void main(String[] args) throws Exception {
        String baseURL = "";
        String username = "";
        String password = "";
        String servername = "";
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].trim().startsWith("--omero.apiurl"))
                    baseURL = args[i].substring(args[i].indexOf('=') + 1)
                            .trim();
                if (args[i].trim().startsWith("--omero.host"))
                    servername = args[i].substring(args[i].indexOf('=') + 1)
                            .trim();
                if (args[i].trim().startsWith("--omero.user"))
                    username = args[i].substring(args[i].indexOf('=') + 1)
                            .trim();
                if (args[i].trim().startsWith("--omero.pass"))
                    password = args[i].substring(args[i].indexOf('=') + 1)
                            .trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (username.isEmpty() || password.isEmpty() || servername.isEmpty()) {
            System.err
                    .println("No omero.apiurl, omero.user, omero.pass or omero.host specified.");
            System.exit(1);
        }

        JSONClient client = new JSONClient(baseURL);

        List<JsonObject> versions = client.getVersion();
        System.out.println("API versions:");
        for (JsonObject version : versions)
            System.out.println(version);

        System.out.println("URLs:");
        Map<String, String> urls = client.getURLs();
        for (Entry<String, String> url : urls.entrySet())
            System.out.println(url.getKey() + " : " + url.getValue());

        System.out.println("Servers:");
        Map<String, Integer> servers = client.getServers();
        for (Entry<String, Integer> server : servers.entrySet())
            System.out.println(server.getKey() + " : " + server.getValue());

        System.out.println("Log in to server " + servername + ":");
        JsonObject ctx = client.login(username, password,
                servers.get(servername));
        System.out.println("Logged in: " + ctx);

        System.out.println("Datasets:");
        Collection<JsonObject> datasets = client.listDatasets();
        for (JsonObject dataset : datasets) {
            System.out.println(dataset);
        }

        JsonObject dataset = datasets.iterator().next();
        int datasetId = dataset.getJsonNumber("@id").intValue();
        System.out.println("Images in dataset " + datasetId + ":");
        Collection<JsonObject> images = client.listImages(datasetId);
        for (JsonObject image : images) {
            System.out.println(image);
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (String key : dataset.keySet()) {
            if (key.equals("Name"))
                builder.add(key, "New dataset name");
            else
                builder.add(key, dataset.get(key));
        }
        JsonObject modifiedDataset = builder.build();
        System.out.println("Update name of dataset: "+dataset);
        dataset = client.update(modifiedDataset);
        System.out.println(dataset);
    }
}
