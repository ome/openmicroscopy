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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.gateway.model.RectangleData;
import omero.log.SimpleLogger;

/**
 * Sample code showing how to organize ROIs in Folders
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROIFolders {

    // The value used if the configuration file is not used. To edit*/
    /** The server address. */
    private static String hostName = "serverName";

    /** The username. */
    private static String userName = "userName";

    /** The password. */
    private static String password = "password";

    /** Information to edit. */
    private static long imageId = 1;
    // end edit

    private ImageData image;

    private Gateway gateway;

    private SecurityContext ctx;

    /**
     * start-code
     */

    /**
     * Loads the image.
     * 
     * @param imageID
     *            The id of the image to load.
     * @return See above.
     */
    private ImageData loadImage(long imageID) throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        return browse.getImage(ctx, imageID);
    }

    /** Creates rois and organize in folders */
    private void createROIs() throws Exception {
        ROIFacility roifac = gateway.getFacility(ROIFacility.class);

        // Create 3 Rectangle ROIs

        Collection<ROIData> rois = new ArrayList<ROIData>();

        ROIData roi = new ROIData();
        roi.setImage(image.asImage());
        RectangleData rectangleData = new RectangleData(10, 10, 10, 10);
        rectangleData.setZ(0);
        rectangleData.setT(0);
        roi.addShapeData(rectangleData);
        rois.add(roi);

        roi = new ROIData();
        roi.setImage(image.asImage());
        rectangleData = new RectangleData(20, 20, 10, 10);
        rectangleData.setZ(0);
        rectangleData.setT(0);
        roi.addShapeData(rectangleData);
        rois.add(roi);

        roi = new ROIData();
        roi.setImage(image.asImage());
        rectangleData = new RectangleData(30, 30, 10, 10);
        rectangleData.setZ(0);
        rectangleData.setT(0);
        roi.addShapeData(rectangleData);
        rois.add(roi);

        rois = roifac.saveROIs(ctx, image.getId(), rois);

        // Add each ROI to a different folder
        for (ROIData r : rois) {
            FolderData folder = new FolderData();
            folder.setName("Folder for ROI " + r.getId());
            roifac.addRoisToFolders(ctx, image.getId(), Arrays.asList(r),
                    Arrays.asList(folder));
        }

        // Get the ROI folders associated with the image
        Collection<FolderData> folders = roifac.getROIFolders(ctx, image.getId());
        for (FolderData folder : folders) {
            Collection<ROIResult> result = roifac.loadROIsForFolder(ctx,
                    image.getId(), folder.getId());
            Collection<ROIData> folderRois = result.iterator().next().getROIs();
            // Do something with the ROIs
        }
    }

    /**
     * end-code
     */
    /**
     * Connects and invokes the various methods.
     * 
     * @param args
     *            The login credentials.
     * @param imageId
     *            The image id
     */
    ROIFolders(String[] args, long imageId) {
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());

            image = loadImage(imageId);
            createROIs();
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
     * Runs the script without configuration options.
     *
     * @param args
     *            The login credentials.
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0)
            args = new String[] { "--omero.host=" + hostName,
                    "--omero.user=" + userName, "--omero.pass=" + password };

        new ROIFolders(args, imageId);
        System.exit(0);
    }

}
