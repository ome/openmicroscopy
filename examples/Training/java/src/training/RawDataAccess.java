/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee & Open Microscopy Environment.
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
import java.util.List;
import java.util.Map;

import omero.api.RawPixelsStorePrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.rnd.Plane2D;
import omero.log.SimpleLogger;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;

/** 
 * Sample code showing how to access raw data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class RawDataAccess
{

    //The value used if the configuration file is not used. To edit*/
    /** The server address.*/
    private static String hostName = "serverName";

    /** The username.*/
    private static String userName = "userName";

    /** The password.*/
    private static String password = "password";

    private static long imageId = 1;
    //end edit

    /** The image.*/
    private ImageData image;

    private Gateway gateway;

    private SecurityContext ctx;

    /**
     * start-code
     */

    /**
     * Loads the image.
     * @param imageID The id of the image to load.
     * @return See above.
     */
    private ImageData loadImage(long imageID)
            throws Exception
    {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        return browse.getImage(ctx, imageID);
    }

// Retrieve plane
// ==============
    /**
     * Retrieve a given plane. 
     * This is useful when you need the pixels intensity.
     */
    private void retrievePlane()
            throws Exception
    {
        try (RawDataFacility rdf = gateway.getFacility(RawDataFacility.class)) {
            // To retrieve the image, see above.
            PixelsData pixels = image.getDefaultPixels();
            int sizeZ = pixels.getSizeZ();
            int sizeT = pixels.getSizeT();
            int sizeC = pixels.getSizeC();
            Plane2D p;
            for (int z = 0; z < sizeZ; z++)
                for (int t = 0; t < sizeT; t++)
                    for (int c = 0; c < sizeC; c++)
                        p = rdf.getPlane(ctx, pixels, z, t, c);
        }
    }

// Retrieve tile
// =============

    /**
     * Retrieve a given tile.
     * This is useful when you need the pixels intensity.
     */
    private void retrieveTile()
            throws Exception
    {
        try (RawDataFacility rdf = gateway.getFacility(RawDataFacility.class)) {
            //To retrieve the image, see above.
            PixelsData pixels = image.getDefaultPixels();
            int sizeZ = pixels.getSizeZ();
            int sizeT = pixels.getSizeT();
            int sizeC = pixels.getSizeC();
            //tile = (50, 50, 10, 10)  x, y, width, height of tile
            int x = 0;
            int y = 0;
            int width = pixels.getSizeX()/2;
            int height = pixels.getSizeY()/2;
            Plane2D p;
            for (int z = 0; z < sizeZ; z++) {
                for (int t = 0; t < sizeT; t++) {
                    for (int c = 0; c < sizeC; c++) {
                        p = rdf.getTile(ctx, pixels, z, t, c, x, y, width,
                                height);
                    }
                }
            }
        }
    }

// Retrieve stack
// ==============

    /**
     * Retrieve a given stack.
     * This is useful when you need the pixels intensity.
     */
    private void retrieveStack()
            throws Exception
    {
        // TODO: Add method to RawDataFacility !
        //To retrieve the image, see above.
        PixelsData pixels = image.getDefaultPixels();
        int sizeT = pixels.getSizeT();
        int sizeC = pixels.getSizeC();
        long pixelsId = pixels.getId();
        RawPixelsStorePrx store = null;
        try {
            store = gateway.getPixelsStore(ctx);
            store.setPixelsId(pixelsId, false);
            for (int t = 0; t < sizeT; t++) {
                for (int c = 0; c < sizeC; c++) {
                    byte[] plane = store.getStack(c, t);
                }
            }
        } catch (Exception e) {
            throw new Exception("Cannot read the stack", e);
        } finally {
            if (store != null) store.close();
        }
    }

// Retrieve hypercube
// ==================

    /**
     * Retrieve a given hypercube.
     * This is useful when you need the pixels intensity.
     */
    private void retrieveHypercube()
            throws Exception
    {
        // TODO: Add method to RawDataFacility !
        //To retrieve the image, see above.
        PixelsData pixels = image.getDefaultPixels();
        long pixelsId = pixels.getId();
        // offset values in each dimension XYZCT
        List<Integer> offset = new ArrayList<Integer>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            offset.add(i, 0);
        }
        List<Integer> size = new ArrayList<Integer>();
        size.add(pixels.getSizeX());
        size.add(pixels.getSizeY());
        size.add(pixels.getSizeZ());
        size.add(pixels.getSizeC());
        size.add(pixels.getSizeT());
        // indicate the step in each direction, step = 1, 
        //will return values at index 0, 1, 2.
        //step = 2, values at index 0, 2, 4 etc.
        List<Integer> step = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            step.add(i, 1);
        }
        RawPixelsStorePrx store = null;
        try {
            store = gateway.getPixelsStore(ctx);
            store.setPixelsId(pixelsId, false);
            byte[] values = store.getHypercube(offset, size, step);
        } catch (Exception e) {
            throw new Exception("Cannot read the hypercube", e);
        } finally {
            if (store != null) store.close();
        }
    }

 // Retrieve histogram
 // ==================

    /**
     * Retrieve the histogram
     */
    private void retrieveHistogram() throws Exception {
        PixelsData pixels = image.getDefaultPixels();
        long pixelsId = pixels.getId();
        RawPixelsStorePrx store = null;
        try {
            store = gateway.getPixelsStore(ctx);
            store.setPixelsId(pixelsId, false);
            int[] channels = new int[] { 0 };
            int binCount = 256;
            Map<Integer, int[]> histdata = store.getHistogram(channels,
                    binCount, false, null);
            int[] histogram = histdata.get(0);
            printHistogram(histogram);
        } catch (Exception e) {
            throw new Exception("Cannot get the histogram data", e);
        } finally {
            if (store != null)
                store.close();
        }
    }
     
    /**
     * Print a histogram to stdout
     * 
     * @param data
     *            The histogram data
     */
    private void printHistogram(int[] data) {
        int max = 0;
        for (int d : data)
            max = Math.max(max, d);

        int step = max / 100;

        for (int i = 0; i < data.length; i++) {
            String s = String.format("%1$ 4d |", i);
            int x = data[i];
            StringBuilder bar = new StringBuilder();
            do {
                bar.append("]");
                x -= step;
            } while (x > 0);
            while (bar.length() <= 100)
                bar.append(" ");
            System.out.println(s + bar + " " + data[i]);
        }
    }
     
    /**
     * end-code
     */
    /**
     * Connects and invokes the various methods.
     * @param args The login credentials.
     * @param imageId The image id.
     */
    RawDataAccess(String[] args, long imageId)
    {
        LoginCredentials cred = new LoginCredentials(args);
        gateway = new Gateway(new SimpleLogger());
        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            image = loadImage(imageId);
            retrievePlane();
            retrieveTile();
            retrieveStack();
            retrieveHypercube();
            retrieveHistogram();
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
     * @param args The login credentials.
     */
    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
            args = new String[] { "--omero.host=" + hostName,
                "--omero.user=" + userName, "--omero.pass=" + password };

        new RawDataAccess(args, imageId);
        System.exit(0);
    }

}
