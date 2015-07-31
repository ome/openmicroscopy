/*
 * training.ReadData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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

//Java imports
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.log.SimpleLogger;
import omero.model.Length;
import omero.model.enums.UnitsLength;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.WellData;

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
    private String hostName = "serverName";

    /** The username. */
    private String userName = "userName";

    /** The password. */
    private String password = "password";

    /** The id of a dataset. */
    private long datasetId = 1;

    /** The id of an image. */
    private long imageId = 1;

    /** The id of a plate. */
    private long plateId = 1;

    // end edit

    private Gateway gateway;

    private ExperimenterData user;
    
    private SecurityContext ctx;
    
    /**
     * Retrieve the projects owned by the user currently logged in.
     * 
     * If a project contains datasets, the datasets will automatically be
     * loaded.
     */
    private void loadProjects(ConfigurationInfo info) throws Exception {
        
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

    /**
     * Retrieve the datasets owned by the user currently logged in.
     */
    @SuppressWarnings("unchecked")
    private void loadDatasets(ConfigurationInfo info) throws Exception {
        
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

    /**
     * Retrieve the images contained in a dataset.
     * 
     * In that case, we specify the dataset's id.
     * 
     * @param info
     *            The configuration information.
     */
    @SuppressWarnings("unchecked")
    private void loadImagesInDataset(ConfigurationInfo info) throws Exception {
        
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<ImageData> images = browse.getImagesForDatasets(ctx, Arrays.asList(info.getDatasetId()));

        Iterator<ImageData> j = images.iterator();
        ImageData image;
        while (j.hasNext()) {
            image = j.next();
            System.err
                    .println("image:" + image.getId() + " " + image.getName());
            // Do something
        }
    }

    /**
     * Retrieve an image if the identifier is known.
     */
    private void loadImage(ConfigurationInfo info) throws Exception {
        
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        ImageData image = browse.getImage(ctx, info.getImageId());
       
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

    /**
     * Retrieve Screening data owned by the user currently logged in.
     * 
     * To learn about the model go to ScreenPlateWell. Note that the wells are
     * not loaded.
     */
    private void loadScreens(ConfigurationInfo info) throws Exception {
        
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

    /**
     * Retrieve Screening data owned by the user currently logged in.
     * 
     * To learn about the model go to ScreenPlateWell. Note that the wells are
     * not loaded.
     */
    private void loadWells(ConfigurationInfo info) throws Exception {
        
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        Collection<WellData> wells = browse.getWells(ctx, info.getPlateId());

        Iterator<WellData> i = wells.iterator();
        WellData well;
        while (i.hasNext()) {
            well = i.next();
            System.err.println("well:" + well.getId());
        }
    }

    /**
     * Retrieve Screening data owned by the user currently logged in.
     * 
     * To learn about the model go to ScreenPlateWell. Note that the wells are
     * not loaded.
     */
    private void loadPlate(ConfigurationInfo info) throws Exception {
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
     * Connects and invokes the various methods.
     * 
     * @param info
     *            The configuration information.
     */
    ReadData(ConfigurationInfo info) {
        if (info == null) {
            info = new ConfigurationInfo();
            info.setHostName(hostName);
            info.setPassword(password);
            info.setUserName(userName);
            info.setImageId(imageId);
            info.setDatasetId(datasetId);
            info.setPlateId(plateId);
        }

        LoginCredentials cred = new LoginCredentials();
        cred.getServer().setHostname(info.getHostName());
        cred.getServer().setPort(info.getPort());
        cred.getUser().setUsername(info.getUserName());
        cred.getUser().setPassword(info.getPassword());

        gateway = new Gateway(new SimpleLogger());

        try {
            user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            
            loadProjects(info);
            loadDatasets(info);
            loadImagesInDataset(info);
            loadImage(info);
            loadScreens(info);
            loadWells(info);
            loadPlate(info);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            gateway.disconnect();
        }

    }

    /**
     * Runs the script without configuration options.
     * 
     * @param args
     */
    public static void main(String[] args) {
        new ReadData(null);
        System.exit(0);
    }

}
