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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import omero.api.IMetadataPrx;
import omero.api.IQueryPrx;
import omero.api.RawFileStorePrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.log.SimpleLogger;
import omero.model.Annotation;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.sys.ParametersI;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Sample code showing how to write data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class WriteData
{

    //The value used if the configuration file is not used. To edit*/
    /** The server address.*/
    private static String hostName = "serverName";

    /** The username.*/
    private static String userName = "userName";

    /** The password.*/
    private static String password = "password";

    /** The id of an image.*/
    private static long imageId = 1;

    /** The id of a project.*/
    private static long projectId = 1;

    //end edit

    /** Maximum size of data read at once. */
    private static final int INC = 262144;

    /** The image.*/
    private ImageData image;

    private String fileMimeType = "application/octet-stream";

    private String description = "description";

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

// Create dataset
// ==============

    /** 
     * Create a new dataset and link it to existing project.
     * @param projectId The id of the project.
     */
    private void createNewDataset(long projectId)
            throws Exception
    {
        DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
        //Using IObject directly
        Dataset dataset = new DatasetI();
        dataset.setName(omero.rtypes.rstring("new Name 1"));
        dataset.setDescription(omero.rtypes.rstring("new description 1"));
        //Using the pojo
        DatasetData datasetData = new DatasetData();
        datasetData.setName("new Name 2");
        datasetData.setDescription("new description 2");
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setChild(dataset);
        link.setParent(new ProjectI(projectId, false));
        IObject r = dm.saveAndReturnObject(ctx, link);
        //With pojo
        link = new ProjectDatasetLinkI();
        link.setChild(datasetData.asDataset());
        link.setParent(new ProjectI(projectId, false));
        r = dm.saveAndReturnObject(ctx, link);
    }

// Create tag
// ==========

    /** 
     * Create a new tag and link it to existing project.
     * @param projectId The id of the project.
     */
    private void createNewTag(long projectId)
            throws Exception
    {
        DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("new tag 1"));
        tag.setDescription(omero.rtypes.rstring("new tag 1"));
        //Using the pojo
        TagAnnotationData tagData = new TagAnnotationData("new tag 2");
        tagData.setTagDescription("new tag 2");
        ProjectAnnotationLink link = new ProjectAnnotationLinkI();
        link.setChild(tag);
        link.setParent(new ProjectI(projectId, false));
        IObject r = dm.saveAndReturnObject(ctx, link);
        //With pojo
        link = new ProjectAnnotationLinkI();
        link.setChild(tagData.asAnnotation());
        link.setParent(new ProjectI(projectId, false));
        r = dm.saveAndReturnObject(ctx, link);
    }

// Create file annotation
// ======================

    /**
     * How to create a file annotation and link to an image.
     * To attach a file to an object e.g. an image, few objects need to be created:
     * - an OriginalFile
     * - a FileAnnotation
     * - a link between the Image and the FileAnnotation.
     */
    private void createFileAnnotationAndLinkToImage()
            throws Exception
    {
        DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
        // To retrieve the image see above.
        File file = File.createTempFile("temp-file-name_", ".tmp"); 
        String name = file.getName();
        String absolutePath = file.getAbsolutePath();
        String path = absolutePath.substring(0, 
                absolutePath.length()-name.length());
        // create the original file object.
        OriginalFile originalFile = new OriginalFileI();
        originalFile.setName(omero.rtypes.rstring(name));
        originalFile.setPath(omero.rtypes.rstring(path));
        originalFile.setSize(omero.rtypes.rlong(file.length()));
        final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
        checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
        originalFile.setHasher(checksumAlgorithm);
        originalFile.setMimetype(omero.rtypes.rstring(fileMimeType)); // or "application/octet-stream"
        // now we save the originalFile object
        originalFile = (OriginalFile) dm.saveAndReturnObject(ctx, originalFile);
        // Initialize the service to load the raw data
        RawFileStorePrx rawFileStore = null;
        FileInputStream stream = null;
        try {
            rawFileStore = gateway.getRawFileService(ctx);
            rawFileStore.setFileId(originalFile.getId().getValue());
            // open file and read stream.
            stream = new FileInputStream(file);
            long pos = 0;
            int rlen;
            byte[] buf = new byte[INC];
            ByteBuffer bbuf;
            while ((rlen = stream.read(buf)) > 0) {
                rawFileStore.write(buf, pos, rlen);
                pos += rlen;
                bbuf = ByteBuffer.wrap(buf);
                bbuf.limit(rlen);
            }
            originalFile = rawFileStore.save();
        } catch (Exception e) {
            throw new Exception("Cannot read data", e);
        } finally {
            if (rawFileStore != null) rawFileStore.close();
            if (stream != null) stream.close();
            if (file != null) file.delete();
        }
        //now we have an original File in DB and raw data uploaded.
        // We now need to link the Original file to the image using 
        // the File annotation object. That's the way to do it.
        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(originalFile);
        fa.setDescription(omero.rtypes.rstring(description)); // The description set above e.g. PointsModel
        fa.setNs(omero.rtypes.rstring(Setup.TRAINING_NS)); // The name space you have set to identify the file annotation.
        // save the file annotation.
        fa = (FileAnnotation) dm.saveAndReturnObject(ctx, fa);
        // now link the image and the annotation
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(fa);
        link.setParent(image.asImage());
        // save the link back to the server.
        link = (ImageAnnotationLink) dm.saveAndReturnObject(ctx, link);
        // To attach to a Dataset use DatasetAnnotationLink;
    }

// Load annotation
// ===============

    /**
     * Load all the annotations with a given namespace linked to images.
     */
    private void loadAnnotationsLinkedToImage()
            throws Exception
    {
        long userId = gateway.getLoggedInUser().getId();
        List<String> nsToInclude = new ArrayList<String>();
        nsToInclude.add(Setup.TRAINING_NS);
        List<String> nsToExclude = new ArrayList<String>();
        ParametersI param = new ParametersI();
        param.exp(omero.rtypes.rlong(userId)); //load the annotation for a given user.
        IMetadataPrx proxy = gateway.getMetadataService(ctx);
        // retrieve the annotations linked to images, for datasets use: omero.model.Dataset.class
        List<Annotation> annotations = proxy.loadSpecifiedAnnotations(
                FileAnnotation.class.getName(), nsToInclude, nsToExclude, param);
        //Do something with annotations.
        Iterator<Annotation> j = annotations.iterator();
        Annotation annotation;
        FileAnnotationData fa;
        RawFileStorePrx store = null;
        File file = File.createTempFile("temp-file-name_", ".tmp"); 
        try {
            store = gateway.getRawFileService(ctx);
            int index = 0;
            FileOutputStream stream = new FileOutputStream(file);
            OriginalFile of;
            while (j.hasNext()) {
                annotation = j.next();
                if (annotation instanceof FileAnnotation && index == 0) {
                    fa = new FileAnnotationData((FileAnnotation) annotation);
                    //The id of te original file
                    of = getOriginalFile(fa.getFileID());
                    store.setFileId(fa.getFileID());
                    int offset = 0;
                    long size = of.getSize().getValue();
                    //name of the file
                    of.getName().getValue();
                    try {
                        for (offset = 0; (offset+INC) < size;) {
                            stream.write(store.read(offset, INC));
                            offset += INC;
                        }
                    } finally {
                        stream.write(store.read(offset, (int) (size-offset)));
                        stream.close();
                    }
                    index++;
                }
            }
        } catch (Exception e) {
            throw new Exception("cannot read the data", e);
        } finally {
            if (store != null) store.close();
            if (file != null) file.delete();
        }
    }

    /**
     * Returns the original file corresponding to the passed id.
     * @param id The id identifying the file.
     * @return See above.
     * @throws Exception If an error occurred.
     */
    private OriginalFile getOriginalFile(long id)
            throws Exception
    {
        ParametersI param = new ParametersI();
        param.map.put("id", omero.rtypes.rlong(id));
        IQueryPrx svc = gateway.getQueryService(ctx);
        return (OriginalFile) svc.findByQuery(
                "select p from OriginalFile as p " +
                        "where p.id = :id", param);
    }

    /**
     * end-code
     */

    /**
     * Connects and invokes the various methods.
     *
     * @param args The login credentials.
     * @param imageId  The image id.
     * @param projectId The project id.
     */
    WriteData(String[] args, long imageId, long projectId)
    {
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            image = loadImage(imageId);
            createFileAnnotationAndLinkToImage();
            loadAnnotationsLinkedToImage();
            createNewDataset(projectId);
            createNewTag(projectId);
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

        new WriteData(args, imageId, projectId);
        System.exit(0);
    }

}
