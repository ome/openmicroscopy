/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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

import java.io.File;

import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.cli.ErrorHandler;
import ome.formats.importer.cli.LoggingImportMonitor;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImportCallback;
import omero.log.SimpleLogger;

/**
 * Sample code showing how to import an image.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ImportImage {

    // The values used if the configuration file is not used. To edit*/
    /** The server address. */
    private static String hostName = "serverName";

    /** The username. */
    private static String userName = "userName";

    /** The password. */
    private static String password = "password";

    //end edit
    
    /** Reference to the gateway */
    private Gateway gateway;

    /** The current SecurityContext */
    private SecurityContext ctx;

    /**
     * Connects and invokes the various methods.
     * 
     * @param args The login credentials
     */
    ImportImage(String[] args) {
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());

            gatewayImport();
            apiImport(args);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                gateway.disconnect(); // Be sure to disconnect
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Import an image file via the Gateway (recommended)
     */
    private void gatewayImport() {
        File fakeImage = null;

        try {
            fakeImage = File.createTempFile("gateway_image", ".fake");
            ImportCallback cb = new ImportCallback();
            
            TransferFacility tf = gateway.getFacility(TransferFacility.class);
            // the uploadImage method will automatically create a dataset with
            // the same name as the directory, the image file is located in
            tf.uploadImage(ctx, fakeImage, cb);

            // wait for the upload to finish
            while (!cb.isFinished()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fakeImage != null)
                fakeImage.delete();
        }

    }

    /**
     * Import an image directly using the Java API
     */
    private void apiImport(String[] args) {
        File fakeImage = null;

        try {

            String host = hostName, port = "" + Setup.DEFAULT_PORT, user = userName, pass = password;
            for (String arg : args) {
                if (arg.startsWith("--omero.host"))
                    host = arg.substring(arg.indexOf('=') + 1);
                if (arg.startsWith("--omero.port"))
                    port = arg.substring(arg.indexOf('=') + 1);
                if (arg.startsWith("--omero.user"))
                    user = arg.substring(arg.indexOf('=') + 1);
                if (arg.startsWith("--omero.pass"))
                    pass = arg.substring(arg.indexOf('=') + 1);
            }
            
            fakeImage = File.createTempFile("api_image", ".fake");

            String[] paths = new String[] { fakeImage.getPath() };

            ImportConfig config = new ome.formats.importer.ImportConfig();

            config.email.set("");
            config.sendFiles.set(true);
            config.sendReport.set(false);
            config.contOnError.set(false);
            config.debug.set(false);

            config.hostname.set(host);
            config.port.set(Integer.parseInt(port));
            config.username.set(user);
            config.password.set(pass);

            // the imported image will go into 'orphaned images' unless
            // you specify a particular existing dataset like this:
            // config.targetClass.set("omero.model.Dataset");
            // config.targetId.set(1L);
            OMEROMetadataStoreClient store = config.createStore();
            store.logVersionInfo(config.getIniVersionNumber());
            OMEROWrapper reader = new OMEROWrapper(config);
            ImportLibrary library = new ImportLibrary(store, reader);

            ErrorHandler handler = new ErrorHandler(config);
            library.addObserver(new LoggingImportMonitor());

            ImportCandidates candidates = new ImportCandidates(reader, paths,
                    handler);
            reader.setMetadataOptions(new DefaultMetadataOptions(
                    MetadataLevel.ALL));
            library.importCandidates(config, candidates);

            store.logout();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fakeImage != null)
                fakeImage.delete();
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0)
            args = new String[] { "--omero.host=" + hostName,
                    "--omero.user=" + userName, "--omero.pass=" + password };
        
        new ImportImage(args);
        System.exit(0);
    }

}
