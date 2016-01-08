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
    private String hostName = "serverName";

    /** The username. */
    private String userName = "userName";

    /** The password. */
    private String password = "password";

    /** Reference to the gateway */
    private Gateway gateway;

    /** The current SecurityContext */
    private SecurityContext ctx;

    /**
     * Connects and invokes the various methods.
     * 
     * @param info
     *            The configuration information
     */
    ImportImage(ConfigurationInfo info) {
        if (info == null) {
            info = new ConfigurationInfo();
            info.setHostName(hostName);
            info.setPassword(password);
            info.setUserName(userName);
        }

        LoginCredentials cred = new LoginCredentials();
        cred.getServer().setHostname(info.getHostName());
        cred.getServer().setPort(info.getPort());
        cred.getUser().setUsername(info.getUserName());
        cred.getUser().setPassword(info.getPassword());

        gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());

            gatewayImport(info);
            apiImport(info);
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
    private void gatewayImport(ConfigurationInfo info) {
        File fakeImage = null;

        try {
            fakeImage = File.createTempFile("gateway_image", ".fake");
            ImportCallback cb = new ImportCallback();
            TransferFacility tf = gateway.getFacility(TransferFacility.class);
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
    private void apiImport(ConfigurationInfo info) {
        File fakeImage = null;

        try {
            fakeImage = File.createTempFile("api_image", ".fake");

            String[] paths = new String[] { fakeImage.getPath() };

            ImportConfig config = new ome.formats.importer.ImportConfig();

            config.email.set("");
            config.sendFiles.set(true);
            config.sendReport.set(false);
            config.contOnError.set(false);
            config.debug.set(false);

            config.hostname.set(info.getHostName());
            config.port.set(info.getPort());
            config.username.set(info.getUserName());
            config.password.set(info.getPassword());

            // import images into dataset with id=1
            config.targetClass.set("omero.model.Dataset");
            config.targetId.set(1L);
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
        new ImportImage(null);
        System.exit(0);
    }

}
