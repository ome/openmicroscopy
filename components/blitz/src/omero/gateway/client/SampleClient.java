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

package omero.gateway.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;
import omero.gateway.Gateway;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.ImportException;
import omero.gateway.model.ExportFormat;
import omero.gateway.model.SecurityContext;
import omero.gateway.model.UserCredentials;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.util.PojoMapper;

/**
 * A SampleClient demonstrating the use of the OMERO Java gateway
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class SampleClient {

    final static String DEFAULT_HOST = "localhost";

    final static int DEFAULT_PORT = 4064;
    
    final static float COMPRESSION = 0.8f;

    public static void main(String[] args) {

        UserCredentials uc = requestCredentials();

        Gateway gw = new Gateway();

        try {
            ExperimenterData user = gw.connect(uc, "SampleClient", COMPRESSION);

            String serverVersion = gw.getServerVersion();
            System.out.println("Connection established, Server version: "
                    + serverVersion);

            System.out.println("Logged in as user " + user.getFirstName() + " "
                    + user.getLastName() + " (Username: " + user.getUserName()
                    + ").");

            // create a SecurityContext which is further used to access the
            // server
            SecurityContext ctx = new SecurityContext(user.getGroupId());

            Set<GroupData> groups = gw.getAvailableGroups(ctx, user, true);
            System.out.println("Member of " + groups.size() + " groups.");

            boolean isAdmin = gw.isAdministrator(ctx, user);
            System.out.println("Is admin: " + isAdmin);

            ParametersI param = new ParametersI();
            param.leaves();

            Set data = gw.loadContainerHierarchy(ctx, DatasetData.class, null,
                    param);
            System.out.println("Has access to " + data.size() + " datasets:");
            listContainerHierarchy(data);

            String tmp = readLine("Import image file, enter full path: ");
            File file = new File(tmp);

            tmp = readLine("Import into Dataset, enter ID: ");
            long dsId = Long.parseLong(tmp);
            
            IObject obj = gw.findIObject(ctx, Dataset.class.getName(), dsId);
            DatasetData ds = (DatasetData) PojoMapper.asDataObject(obj);
            
            ImportObserver obs = new ImportObserver();
            gw.importFile(ctx, file, ds, obs, user);
            
            System.out.println("Waiting for import to complete...");
            while(!obs.isDone()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            System.out.println("...ok.");
            
            data = gw.loadContainerHierarchy(ctx, DatasetData.class, null,
                    param);
            System.out.println("Datasets reloaded:");
            listContainerHierarchy(data);
            
            tmp = readLine("Export image, enter ID: ");
            long imgID;
            try {
                imgID = Long.parseLong(tmp);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }

            obj = gw.findIObject(ctx, Image.class.getName(), imgID);
            ImageData img = (ImageData) PojoMapper.asDataObject(obj);

            File f = new File(System.getProperty("user.home") + "/"
                    + img.getName() + ".ome.tiff");
            gw.exportImageAsOMEObject(ctx, ExportFormat.OME_TIFF, f,
                    img.getId());
            System.out.println("Exported as " + f.getAbsolutePath());

        } catch (DSOutOfServiceException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        } catch (DSAccessException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        } catch (ImportException e1) {
            System.err.println("Import failed!");
            e1.printStackTrace();
        } finally {
            gw.disconnect();
            System.out.println("Connection closed.");
        }
    }

    /**
     * Just iterates through the datasets and prints the names/ids 
     * on System.out
     * @param data
     */
    private static void listContainerHierarchy(Set data) {
        Iterator dsIt = data.iterator();
        while (dsIt.hasNext()) {
            DatasetData ds = (DatasetData) dsIt.next();
            System.out.println(" * " + ds.getName()+" ("+ds.getId()+")");
            Set images = ds.getImages();
            Iterator imgIt = images.iterator();
            while (imgIt.hasNext()) {
                ImageData img = (ImageData) imgIt.next();
                System.out.println("\t - " + img.getName() + " (id="
                        + img.getId() + ")");
            }
        }
    }
    
    /**
     * Asks the user to type in his credentials
     * 
     * @return
     */
    private static UserCredentials requestCredentials() {

        String host = readLine("Host [" + DEFAULT_HOST + "]: ");
        if (host.trim().length() == 0) {
            host = DEFAULT_HOST;
        }

        String tmp = readLine("Port [" + DEFAULT_PORT + "]: ");
        int port = DEFAULT_PORT;
        try {
            port = Integer.parseInt(tmp.trim());
        } catch (NumberFormatException e) {
        }

        String username = readLine("Username: ");

        String password = new String(readPassword("Password: "));

        UserCredentials uc = new UserCredentials();
        uc.setEncrypted(true);
        uc.setHostName(host);
        uc.setPort(port);
        uc.setUserName(username);
        uc.setPassword(password);
        return uc;
    }

    /**
     * Helper method for reading from the commandline
     * 
     * @param format
     * @param args
     * @return
     */
    private static String readLine(String format, Object... args) {
        if (System.console() != null) {
            return System.console().readLine(format, args);
        }
        System.out.print(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Helper method for reading from the commandline
     * 
     * @param format
     * @param args
     * @return
     */
    private static char[] readPassword(String format, Object... args) {
        if (System.console() != null)
            return System.console().readPassword(format, args);
        return readLine(format, args).toCharArray();
    }

    /**
     * A simple IObserver implementation checking for IMPORT_DONE event 
     */
    static class ImportObserver implements IObserver {

        boolean done = false;
        
        @Override
        public void update(IObservable observable, ImportEvent event) {
            if(event instanceof ImportEvent.IMPORT_DONE) {
                done = true;
            }
        }
        
        boolean isDone() {
            return done;
        }
    }
}
