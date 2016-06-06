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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.log.SimpleLogger;
import omero.romio.PlaneDef;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;


/** 
 * Sample code showing how to start a rendering engine and how to interact
 * with it.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class RenderImages
{

    //The value used if the configuration file is not used. To edit*/
    /** The server address.*/
    private static String hostName = "serverName";

    /** The username.*/
    private static String userName = "userName";

    /** The password.*/
    private static String password = "password";

    /** Information to edit.*/
    private static long imageId = 1;
    //end edit

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

// Initialize rendering engine
// ===========================

    /**
     * Creates a rendering engine.
     */
    private void createRenderingEngine()
            throws Exception
    {
        PixelsData pixels = image.getDefaultPixels();
        long pixelsId = pixels.getId();
        RenderingEnginePrx proxy = null;
        try {
            proxy = gateway.getRenderingService(ctx, pixelsId);
            proxy.lookupPixels(pixelsId);
            if (!(proxy.lookupRenderingDef(pixelsId))) {
                proxy.resetDefaultSettings(true);
                proxy.lookupRenderingDef(pixelsId);
            }
            proxy.load();
            // Now can interact with the rendering engine.
            proxy.setActive(0, Boolean.valueOf(false));
            // to render the image uncompressed
            PlaneDef pDef = new PlaneDef();
            pDef.z = 0;
            pDef.t = 0;
            pDef.slice = omero.romio.XY.value;
            //render the data uncompressed.
            int[] uncompressed = proxy.renderAsPackedInt(pDef);
            byte[] compressed = proxy.renderCompressed(pDef);
            //Create a buffered image
            ByteArrayInputStream stream = new ByteArrayInputStream(compressed);
            BufferedImage image = ImageIO.read(stream);
            System.err.println(image.getWidth()+" "+image.getHeight());
        } catch (Exception e) {
            throw new Exception("Cannot render image", e);
        } finally {
            if (proxy != null) proxy.close();
        }
    }

// Retrieve thumbnails
// ===================

    /**
     * Retrieves the thumbnails
     */
    private void retrieveThumbnails()
            throws Exception
    {
        ThumbnailStorePrx store = null;
        try {
            store = gateway.getThumbnailService(ctx);
            PixelsData pixels = image.getDefaultPixels();
            Map<Long, byte[]> map = store.getThumbnailByLongestSideSet(
                    omero.rtypes.rint(96), Arrays.asList(pixels.getId()));
            Entry<Long, byte[]> entry;
            Iterator<Entry<Long, byte[]>> i = map.entrySet().iterator();
            ByteArrayInputStream stream;
            //Create a buffered image to display
            Map<Long, BufferedImage> results = new HashMap<Long, BufferedImage>();
            while (i.hasNext()) {
                entry = i.next();
                stream = new ByteArrayInputStream(entry.getValue());
                results.put(entry.getKey(), ImageIO.read(stream));
            }
        } catch (Exception e) {
            throw new Exception("Cannot retrieve thumnails", e);
        } finally {
            if (store != null) store.close();
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
    RenderImages(String[] args, long imageId)
    {	
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            image = loadImage(imageId);
            createRenderingEngine();
            retrieveThumbnails();
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

        new RenderImages(args, imageId);
        System.exit(0);
    }

}
