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

import java.util.Collection;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.log.SimpleLogger;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;

/** 
 * Follow samples code indicating how to use OMERO.tables
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class HowToUseTables
{

    //Edit the information below
    /** The server address.*/
    private static String hostName = "serverName";

    /** The username.*/
    private static String userName = "userName";

    /** The password.*/
    private static String password = "password";
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
            throws Exception {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        return browse.getImage(ctx, imageID);
    }

// Create table
// ============

    /** 
     * Creates a table and links to an Image.
     * @throws Exception
     */
    private void createTableandLinkToImage()
            throws Exception
    {
        TableDataColumn[] columns = new TableDataColumn[3];
        columns[0] =  new TableDataColumn("ID", 0, Long.class);
        columns[1] =  new TableDataColumn("Name", 1, String.class);
        columns[2] =  new TableDataColumn("Value", 2, Double.class);
        
        Object[][] data = new Object[3][5];
        data[0] = new Long[] {1l, 2l, 3l, 4l, 5l};
        data[1] = new String[] {"one", "two", "three", "four", "five"};
        data[2] = new Double[] {1d, 2d, 3d, 4d, 5d};
        
        TableData tableData = new TableData(columns, data);
        
        TablesFacility fac = gateway.getFacility(TablesFacility.class);
        
        // Attach the table to the image
        tableData = fac.addTable(ctx, image, "My Data", tableData);
        
        // Find the table again
        Collection<FileAnnotationData> tables = fac.getAvailableTables(ctx, image);
        long fileId  = tables.iterator().next().getFileID();
        
        // Request second and third column of the first three rows
        TableData tableData2 = fac.getTable(ctx, fileId, 0, 2, 1, 2);
        
        // do something, e.g. print to System.out
        int nRows = tableData2.getData()[0].length;
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < tableData2.getColumns().length; col++) {
                Object o = tableData2.getData()[col][row];
                System.out.print(o + " ["
                        + tableData2.getColumns()[col].getType() + "]\t");
            }
            System.out.println();
        }
    }

    /**
     * end-code
     */

    /**
     * Connects and invokes the various methods.
     *
     * @param args The login credentials.
     * @param imageId omero Image ID.
     */
    HowToUseTables(String[] args, long imageId)
    {
        LoginCredentials cred = new LoginCredentials(args);
        gateway = new Gateway(new SimpleLogger());
        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            image = loadImage(imageId);
            createTableandLinkToImage();
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
        
        // Edit image Id if you want to use a custom imageId instead of the configuration option
        new HowToUseTables(args, 1);
        System.exit(0);
    }

}
