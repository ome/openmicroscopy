/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package training.gateway;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.ImportException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.Facility;
import omero.gateway.facility.SearchFacility;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ImportCallback;
import omero.gateway.model.SearchParameters;
import omero.gateway.model.SearchResult;
import omero.gateway.model.SearchResultCollection;
import omero.log.SimpleLogger;
import omero.util.CommonsLangUtils;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;

/**
 * A simple example to show the basic usage of the {@link Gateway} and it's
 * {@link Facility}s
 * 
 * Dependencies:
 * backport-util-concurrent
 * blitz
 * common
 * formats-api
 * formats-bsd
 * formats-common
 * formats-gpl
 * ice-db
 * ice-freeze
 * ice-glacier2
 * ice-grid
 * ice-storm
 * ice
 * ini4j
 * model-psql
 * ome-java
 * ome-poi
 * ome-xml
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class GatewayExample {

    private static final String DEFAULT_HOST = "localhost"; 
    private static final String DEFAULT_PORT = "4064";
    private static final String DEFAULT_USER = "root"; 

    public static void main(String[] args) throws IOException {
        GatewayExample exp = new GatewayExample();
        exp.run();
    }

    public void run() throws IOException {

        String[] input = requestCredentials();

        LoginCredentials c = new LoginCredentials();
        c.getServer().setHostname(input[0]);
        c.getServer().setPort(Integer.parseInt(input[1]));
        c.getUser().setUsername(input[2]);
        c.getUser().setPassword(input[3]);

        Gateway gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData exp = gateway.connect(c);

            System.out.println("Logged in as: "+exp.getUserName()+" (id="+exp.getId()+")");
            
            BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
            SearchFacility search = gateway.getFacility(SearchFacility.class);
            TransferFacility transfer = gateway
                    .getFacility(TransferFacility.class);

            SecurityContext ctx = new SecurityContext(exp.getDefaultGroup()
                    .getId());

            /** Example for browsing through the data hierarchhy */
            System.out.println("\nListing Datasets:");
            Collection<GroupData> groups = browse.getAvailableGroups(ctx, exp);
            for (GroupData group : groups) {
                if (group.getId() == exp.getDefaultGroup().getId())
                    System.out.println("Group: " + group.getName() + " (id="
                            + group.getId() + ") [default]");
                else
                    System.out.println("Group: " + group.getName() + " (id="
                            + group.getId() + ")");

                SecurityContext groupContext = new SecurityContext(
                        group.getId());
                Collection<DatasetData> datasets = browse
                        .getDatasets(groupContext);
                for (DatasetData dataset : datasets) {
                    System.out.println("\t" + dataset.getName() + " (id="
                            + dataset.getId() + ") - Owner: "+dataset.getOwner().getId());
                    for (Object obj : dataset.getImages()) {
                        if (obj instanceof ImageData) {
                            ImageData img = (ImageData) obj;
                            System.out.println("\t\t" + img.getName() + " (id="
                                    + img.getId() + ", fileid="
                                    + img.getFilesetId() + ") - Owner: "+img.getOwner().getId());
                        }
                    }
                }
            }

            /** Example for searching */
            System.out.println("\n\nSearch term: ");
            String searchTerm = readLine();
            if (!CommonsLangUtils.isEmpty(searchTerm)) {
                SearchParameters param = new SearchParameters(
                        SearchParameters.ALL_SCOPE, SearchParameters.ALL_TYPES,
                        searchTerm);
                for (GroupData grp : browse.getAvailableGroups(ctx, exp)) {
                    SecurityContext groupContext = new SecurityContext(
                            grp.getId());
                    SearchResultCollection results = search.search(
                            groupContext, param);
                    System.out.println("Search result for group '"
                            + grp.getName() + "':");
                    for (SearchResult result : results) {
                        DataObject obj = result.getObject();
                        if (obj instanceof ProjectData) {
                            ProjectData proj = (ProjectData) obj;
                            System.out.println("Project: " + proj.getName());
                        }
                        if (obj instanceof DatasetData) {
                            DatasetData ds = (DatasetData) obj;
                            System.out.println("Dataset: " + ds.getName());
                        }
                        if (obj instanceof ImageData) {
                            ImageData img = (ImageData) obj;
                            System.out.println("Image: " + img.getName());
                        }
                    }
                }
            }

            /** Example for downloading an image (original file format) */
            System.out
                    .println("\n\nDownload image (must be in your default group), image id: ");
            String id = readLine();
            if (!CommonsLangUtils.isEmpty(id)) {
                System.out.println("\n\nDownload, target path: ");
                String path = readLine();
                List<File> files = transfer.downloadImage(ctx, path,
                        Long.parseLong(id));
                for (File file : files) {
                    System.out.println("Downloaded to: "
                            + file.getAbsolutePath());
                }
            }

            /** Example for uploading (i. e. importing) an image */
            System.out.println("\n\nUpload, file path: ");
            String ufile = readLine();
            if (!CommonsLangUtils.isEmpty(ufile)) {
                File f = new File(ufile);
                final ImportCallback cb = new ImportCallback();
                transfer.uploadImage(ctx, f, cb);

                System.out.print("Uploading ...");

                while (!cb.isFinished()) {
                    try {
                        System.out.print(".");
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("done");
            }

        } catch (DSOutOfServiceException e) {
            e.printStackTrace();
        } catch (DSAccessException e) {
            e.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (ImportException e1) {
            e1.printStackTrace();
        } finally {
            gateway.disconnect();
        }
    }

    /**
     * Helper method to read from console
     * 
     * @return
     * @throws IOException
     */
    private static String readLine() throws IOException {
        if (System.console() != null) {
            return System.console().readLine();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        return reader.readLine();
    }

    /**
     * Helper method to read from console
     * 
     * @return
     * @throws IOException
     */
    private static char[] readPassword() throws IOException {
        if (System.console() != null)
            return System.console().readPassword();
        return readLine().toCharArray();
    }

    /**
     * Request the login credentials from the console
     * 
     * @return
     * @throws IOException
     */
    private static String[] requestCredentials() throws IOException {
        String[] result = new String[4];

        System.out.print("Host [" + DEFAULT_HOST + "]: ");
        String host = readLine();
        if (host.isEmpty())
            host = DEFAULT_HOST;
        result[0] = host;

        System.out.print("Port [" + DEFAULT_PORT + "]: ");
        String port = readLine();
        if (port.isEmpty())
            port = DEFAULT_PORT;
        result[1] = port;

        System.out.print("Username [" + DEFAULT_USER + "]: ");
        String username = readLine();
        if (username.isEmpty())
            username = DEFAULT_USER;
        result[2] = username;

        System.out.print("Password: ");
        String password = new String(readPassword());
        result[3] = password;

        return result;
    }
}
