package training;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * An example for using the OMERO JSON API with Java
 * (Implementation see JSONClient.java)
 *  
 * Run it with command line parameters:
 * --omero.webhost=http://web-dev-merge.openmicroscopy.org
 * --omero.servername=eel-merge
 * --omero.user=xxx 
 * --omero.pass=xxx
 *   
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class JSONClientExample {

    String baseURL = "";
    String username = "";
    String password = "";
    String servername = "";

    public JSONClientExample(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].trim().startsWith("--omero.webhost")) {
                    baseURL = args[i].substring(args[i].indexOf('=') + 1)
                            .trim();
                    baseURL += "/api";
                }
                if (args[i].trim().startsWith("--omero.servername"))
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
        if (baseURL.isEmpty() || username.isEmpty() || password.isEmpty()
                || servername.isEmpty()) {
            System.err
                    .println("No omero.webhost, omero.user, omero.pass or omero.servername specified.");
            System.exit(1);
        }
    }

    public void run() throws Exception {
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
        System.out.println("Update name of dataset: " + dataset);
        dataset = client.update(modifiedDataset);
        System.out.println(dataset);
    }

    public static void main(String[] args) {
        JSONClientExample example = new JSONClientExample(args);
        try {
            example.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
