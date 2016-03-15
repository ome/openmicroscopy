/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee & Open Microscopy Environment.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.log.SimpleLogger;
import omero.model.Length;
import omero.model.enums.UnitsLength;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.WellData;

/**
 * Sample code showing how to load data from an OMERO server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ReadData {

    // The value used if the configuration file is not used. To edit*/
    /** The server address. */
    private static String hostName = "serverName";

    /** The username. */
    private static String userName = "userName";

    /** The password. */
    private static String password = "password";

    /** The id of a dataset. */
    private static long datasetId = 1;

    /** The id of an image. */
    private static long imageId = 1;

    /** The id of a plate. */
    private static long plateId = 1;

    // end edit

    private Gateway gateway;

    private ExperimenterData user;

    private SecurityContext ctx;

    /**
     * start-code
     */

// Projects
// ========

    /**
     * Retrieve the projects owned by the user currently logged in.
     * If a project contains datasets, the datasets will automatically be
     * loaded.
     */
    private void loadProjects() throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<ProjectData> projects = browse.getProjects(ctx);
        Iterator<ProjectData> i = projects.iterator();
        ProjectData project;
        Set<DatasetData> datasets;
        Iterator<DatasetData> j;
        DatasetData dataset;
        while (i.hasNext()) {
            project = i.next();
            System.err.println("Project:" + project.getId() + " "
                    + project.getName());
            datasets = project.getDatasets();
            j = datasets.iterator();
            while (j.hasNext()) {
                dataset = j.next();
                System.err.println("dataset:" + dataset.getId() + " "
                        + dataset.getName());
                // Do something here
                // If images loaded.
                // dataset.getImages();
            }
        }
    }

// Datasets
// ========

    /**
     * Retrieve the datasets owned by the user currently logged in.
     */
    @SuppressWarnings("unchecked")
    private void loadDatasets() throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<DatasetData> datasets = browse.getDatasets(ctx);
        Iterator<DatasetData> i = datasets.iterator();
        DatasetData dataset;
        Set<ImageData> images;
        Iterator<ImageData> j;
        ImageData image;
        while (i.hasNext()) {
            dataset = i.next();
            images = dataset.getImages();
            j = images.iterator();
            while (j.hasNext()) {
                image = j.next();
                System.err.println("image:" + image.getId() + " "
                        + image.getName());
            }
        }
    }

// Images
// ======

    /**
     * Retrieve the images contained in a dataset.
     * In that case, we specify the dataset's id.
     * @param datasetId The dataset's id.
     */
    @SuppressWarnings("unchecked")
    private void loadImagesInDataset(long datasetId) throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<ImageData> images = browse.getImagesForDatasets(ctx, Arrays.asList(datasetId));
        Iterator<ImageData> j = images.iterator();
        ImageData image;
        while (j.hasNext()) {
            image = j.next();
            System.err
            .println("image:" + image.getId() + " " + image.getName());
        }
    }

    /**
     * Retrieve an image if the identifier is known.
     * @param imageId The image's id.
     */
    private void loadImage(long imageId) throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        ImageData image = browse.getImage(ctx, imageId);
        PixelsData pixels = image.getDefaultPixels();
        System.err.println(pixels.getSizeZ()); // The number of z-sections.
        System.err.println(pixels.getSizeT()); // The number of timepoints.
        System.err.println(pixels.getSizeC()); // The number of channels.
        System.err.println(pixels.getSizeX()); // The number of pixels along the
        // X-axis.
        System.err.println(pixels.getSizeY()); // The number of pixels along the
        // Y-axis.
        // Get Pixel Size for the above Image
        Length sizeX = pixels.getPixelSizeX(null);
        if (sizeX != null) {
            System.err.println("Pixel Size X:" + sizeX.getValue()
                    + sizeX.getSymbol());
        }
        // To get the size the size with different units, E.g. Angstroms
        Length sizeXang = pixels.getPixelSizeX(UnitsLength.ANGSTROM);
        if (sizeXang != null) {
            System.err.println("Pixel Size X:" + sizeXang.getValue()
                    + sizeXang.getSymbol());
        }
    }

// Screens
// =======

    /**
     * Retrieve Screening data owned by the user currently logged in.
     * To learn about the model go to ScreenPlateWell. Note that the wells are
     * not loaded.
     */
    private void loadScreens() throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<ScreenData> screens = browse.getScreens(ctx);
        Iterator<ScreenData> i = screens.iterator();
        ScreenData screen;
        Set<PlateData> plates;
        Iterator<PlateData> j;
        PlateData plate;
        while (i.hasNext()) {
            screen = i.next();
            System.err.println("screen:" + screen.getId() + " "
                    + screen.getName());
            plates = screen.getPlates();
            j = plates.iterator();
            while (j.hasNext()) {
                plate = j.next();
                System.err.println("plate:" + plate.getId() + " "
                        + plate.getName());
            }
        }
    }

// Wells
// =====

    /**
     * Retrieve Screening data owned by the user currently logged in.
     * To learn about the model go to ScreenPlateWell.
     * @param plateId The plate's id.
     */
    private void loadWells(long plateId) throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<WellData> wells = browse.getWells(ctx, plateId);
        Iterator<WellData> i = wells.iterator();
        WellData well;
        while (i.hasNext()) {
            well = i.next();
            System.err.println("well:" + well.getId());
        }
    }

// Plates
// ======

    /**
     * Retrieve Screening data owned by the user currently logged in.
     * To learn about the model go to ScreenPlateWell. Note that the wells are
     * not loaded.
     */
    private void loadPlate() throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<PlateData> plates = browse.getPlates(ctx);
        Iterator<PlateData> i = plates.iterator();
        PlateData plate;
        while (i.hasNext()) {
            plate = i.next();
            System.err.println("plate:" + plate.getId());
        }
    }

    /**
     * end-code
     */

    /**
     * Connects and invokes the various methods.
     * @param args The login credentials.
     * @param datasetId The dataset's id.
     * @param plateId The plate's id.
     * @param imageId The image's id.
     */
    ReadData(String[] args, long datasetId, long plateId, long imageId) {

        LoginCredentials cred = new LoginCredentials(args);
        gateway = new Gateway(new SimpleLogger());

        try {
            user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());

            loadProjects();
            loadDatasets();
            loadImagesInDataset(datasetId);
            loadImage(imageId);
            loadScreens();
            loadWells(plateId);
            loadPlate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            gateway.disconnect();
        }

    }

    /**
     * Runs the script without configuration options.
     *
     * @param args The login credentials.
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0)
            args = new String[] { "--omero.host=" + hostName,
                "--omero.user=" + userName, "--omero.pass=" + password };

        new ReadData(args, datasetId, plateId, imageId);
        System.exit(0);
    }

}
