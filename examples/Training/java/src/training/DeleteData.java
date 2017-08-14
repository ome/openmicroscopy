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

import omero.cmd.Response;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.DataManagerFacility;
import omero.log.SimpleLogger;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.gateway.model.ExperimenterData;

/**
 * Sample code showing how to delete data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class DeleteData
{

    //The value used if the configuration file is not used.*/
    /** The server address.*/
    private static String hostName = "serverName";

    /** The username.*/
    private static String userName = "userName";

    /** The password.*/
    private static String password = "password";
    //end edit

    private Gateway gateway;

    private SecurityContext ctx;

    /**
     * start-code
     */

    /**
     * Creates an original file.
     * @return See above.
     * @throws Exception
     */
    private OriginalFile createOriginalFile() throws Exception {
        OriginalFileI oFile = new OriginalFileI();
        oFile.setName(omero.rtypes.rstring("of1"));
        oFile.setPath(omero.rtypes.rstring("/omero"));
        oFile.setSize(omero.rtypes.rlong(0));
        final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
        checksumAlgorithm.setValue(
                omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
        oFile.setHasher(checksumAlgorithm);
        oFile.setMimetype(omero.rtypes.rstring("application/octet-stream"));
        return oFile;
    }

// Delete Image
// ============

    /** 
     * Delete Image.
     * In the following example, we create an image and delete it.
     */
    private void deleteImage()
            throws Exception
    {
        DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
        //First create an image.
        Image img = new ImageI();
        img.setName(omero.rtypes.rstring("image1"));
        img.setDescription(omero.rtypes.rstring("descriptionImage1"));
        img = (Image) dm.saveAndReturnObject(ctx, img);
        Response rsp = dm.delete(ctx, img).loop(10, 500);
        System.err.println(rsp);
    }

// Delete Annotation
// =================

    /** 
     * Delete File annotation.
     * In the following example, we create a file annotation, link it to a
     * dataset and delete the annotation.
     */
    private void deleteFileAnnotation()
            throws Exception
    {
        DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
        Dataset d = new DatasetI();
        d.setName(omero.rtypes.rstring("FileAnnotationDelete"));
        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(createOriginalFile());
        d.linkAnnotation(fa);
        d = (Dataset) dm.saveAndReturnObject(ctx, d);
        fa = (FileAnnotation) d.linkedAnnotationList().get(0);
        Response rsp = dm.delete(ctx, fa).loop(10, 500);
        System.err.println(rsp);
    }

    /**
     * end-code
     */

    /**
     * Connects and invokes the various methods.
     *
     * @param args The login credentials.
     */
    DeleteData(String[] args)
    {
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());

            deleteImage();
            deleteFileAnnotation();
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

        new DeleteData(args);
        System.exit(0);
    }

}
