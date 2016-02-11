/*
 * Copyright (C) 2015-2016 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway.facility;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import ome.formats.importer.IObserver;
import omero.RLong;
import omero.api.IPixelsPrx;
import omero.api.RawPixelsStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.ImportException;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ImportCallback;
import omero.gateway.model.ImportableFile;
import omero.gateway.model.ImportableObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ProjectData;
import omero.model.IObject;
import omero.model.PixelsType;

/**
 * {@link Facility} which provides data transfer functionality, i.e. download
 * files and upload/import files.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TransferFacility extends Facility {

    /** Reference to the helper class */
    private TransferFacilityHelper helper;

    /** Reference to the {@link DataManagerFacility} */
    private DataManagerFacility datamanager;
    
    /** Reference to the {@link BrowseFacility} */
    private BrowseFacility browser;

    /** References to currently open pixel stores; key is the pixels id*/
    private static Map<Long, RawPixelsStorePrx> pixelStores = new ConcurrentHashMap<Long, RawPixelsStorePrx>();

    /**
     * Creates a new instance
     * @param gateway Reference to the {@link Gateway}
     */
    TransferFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.datamanager = gateway.getFacility(DataManagerFacility.class);
        this.browser = gateway.getFacility(BrowseFacility.class);
        this.helper = new TransferFacilityHelper(gateway, datamanager, this);
    }

    /**
     * Uploads an image to the server
     *
     * @param context The security context.
     * @param image The image to upload.
     * @param observer The observer to notify components of upload status.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @throws ImportException 
     *             If an error occurred while importing data.
     */
    public void uploadImage(SecurityContext context, File image,
            ImportCallback observer) throws DSAccessException,
            DSOutOfServiceException, ImportException {
        uploadImage(context, image, true, false, gateway.getLoggedInUser(),
                observer);
    }

    /**
     * Uploads an image to the server.
     *
     * @param context
     *            The security context.
     * @param image
     *            The image to upload.
     * @param folderAsContainer
     *            Indicates to use the folder's name to create a dataset or
     *            screen.
     * @param overrideName
     *            Indicates to override the file's name. (if set to
     *            <code>true</code> the full file path will be used as image
     *            name)
     * @param user
     *            The user to import the image for (can be <code>null</code>)
     * @param observer
     *            The observer to notify components of upload status.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @throws ImportException
     *             If an error occurred while importing data.
     */
    public void uploadImage(SecurityContext context, File image,
            boolean folderAsContainer, boolean overrideName, ExperimenterData user,
            ImportCallback observer) throws DSAccessException,
            DSOutOfServiceException, ImportException {
        if(user == null)
            user = gateway.getLoggedInUser();
        
        ImportableFile imf = new ImportableFile(image, folderAsContainer);
        imf.setGroup(user.getDefaultGroup());
        imf.setStatus(observer);

        ImportableObject imo = new ImportableObject(Arrays.asList(new ImportableFile[]{imf}),
                overrideName);
        
        helper.importFile(imo, imf, user, true);
    }
    
    /**
     * Uploads an image to the server.
     *
     * @param context
     *            The security context.
     * @param image
     *            The image to upload.
     * @param overrideName
     *            Indicates to override the file's name. (if set to
     *            <code>true</code> the full file path will be used as image
     *            name)
     * @param targetProject
     *            The {@link ProjectData} to import the image to (can be
     *            <code>null</code>)
     * @param targetDataset
     *            The {@link DatasetData} to import the image to (can be
     *            <code>null</code>)
     * @param user
     *            The user to import the image for (can be <code>null</code>)
     * @param observer
     *            The observer to notify components of upload status.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     * @throws ImportException
     *             If an error occurred while importing data.
     */
    public void uploadImage(SecurityContext context, File image,
            boolean overrideName, ProjectData targetProject,
            DatasetData targetDataset, ExperimenterData user,
            ImportCallback observer) throws DSAccessException,
            DSOutOfServiceException, ImportException {
        if (user == null)
            user = gateway.getLoggedInUser();

        ImportableFile imf = new ImportableFile(image, false);
        imf.setGroup(user.getDefaultGroup());
        imf.setStatus(observer);
        if (targetDataset != null)
            imf.setLocation(targetProject, targetDataset);

        ImportableObject imo = new ImportableObject(
                Arrays.asList(new ImportableFile[] { imf }), overrideName);

        helper.importFile(imo, imf, user, true);
    }
    
    /**
     * Uploads an image to the server.
     *
     * @param context The security context.
     * @param image The image to upload.
     * @param observer The observer to notify components of upload status.
     * @param username The OMERO user name.
     * @param groupname The group to import the data to.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void uploadImage(SecurityContext context, File image,
            IObserver observer, String username, String groupname)
                    throws DSAccessException, DSOutOfServiceException {
    }

    /**
     * Downloads the original file of an image from the server.
     *
     * @param context The security context.
     * @param targetPath Path to the file.
     * @param imageId The identifier of the image.
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public List<File> downloadImage(SecurityContext context, String targetPath,
            long imageId) throws DSAccessException, DSOutOfServiceException {
        return helper.downloadImage(context, targetPath, imageId);
    }

    /**
     * Creates a new image on the server. Use
     * {@link #uploadPlane(SecurityContext, long, int, int, int, byte[])} to add
     * the pixels data to the image and make sure to call
     * {@link #closeImage(SecurityContext, long)} at the end.
     * 
     * @param ctx
     *            The security context
     * @param sizeX
     *            The size in x dimension
     * @param sizeY
     *            The size in y dimension
     * @param c
     *            The number of channels
     * @param sizeZ
     *            The size in z dimension
     * @param sizeT
     *            The size in t dimension
     * @param type
     *            The pixels type (see
     *            {@link BrowseFacility#getPixelsTypes(SecurityContext)}
     * @param name
     *            The name of the image
     * @param description
     *            A description (can be <code>null</code>)
     * @return The newly created {@link ImageData}
     * @throws DSAccessException
     *             If the connection is broken, or not logged in
     * @throws DSOutOfServiceException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public ImageData createImage(SecurityContext ctx, int sizeX, int sizeY,
            int c, int sizeZ, int sizeT, String type, String name,
            String description) throws DSAccessException,
            DSOutOfServiceException {
        try {
            IPixelsPrx proxy = gateway.getPixelsService(ctx);

            List<IObject> l = proxy.getAllEnumerations(PixelsType.class
                    .getName());
            Iterator<IObject> i = l.iterator();
            PixelsType pt = null;
            while (i.hasNext()) {
                PixelsType o = (PixelsType) i.next();
                String value = o.getValue().getValue();
                if (value.equals(type)) {
                    pt = o;
                    break;
                }
            }
            if (type == null)
                throw new Exception("Pixels Type not valid.");

            List<Integer> channels = new ArrayList<Integer>();
            for (int channel = 0; channel < c; channel++)
                channels.add(channel);

            RLong idNew = proxy.createImage(sizeX, sizeY, sizeZ, sizeT,
                    channels, pt, name, description);

            return browser.getImage(ctx, idNew.getValue());
        } catch (Throwable e) {
            handleException(this, e, "Couldn't create new image");
        }
        return null;
    }

    /**
     * Uploads the pixels data for a certain plane (see
     * {@link #createImage(SecurityContext, int, int, int, int, int, String, String, String)}
     * ).
     * 
     * @param ctx
     *            The security context
     * @param pixelsId
     *            The pixels id
     * @param c
     *            The channel
     * @param z
     *            The z dimension
     * @param t
     *            The t dimension
     * @param data
     *            The pixel data
     * @throws DSAccessException
     *             If the connection is broken, or not logged in
     * @throws DSOutOfServiceException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void uploadPlane(SecurityContext ctx, long pixelsId, int c, int z,
            int t, byte[] data) throws DSOutOfServiceException,
            DSAccessException {
        try {
            RawPixelsStorePrx store = TransferFacility.pixelStores.get(pixelsId);
            if (store == null) {
                store = gateway.getPixelsStore(ctx);
                store.setPixelsId(pixelsId, false);
                TransferFacility.pixelStores.put(pixelsId, store);
            }
            store.setPlane(data, z, c, t);
        } catch (Throwable e) {
            handleException(this, e, "Couldn't upload plane data");
        }
    }

    /**
     * Saves the data and closes the images (see
     * {@link #createImage(SecurityContext, int, int, int, int, int, String, String, String)}
     * ).
     * 
     * @param ctx
     *            The security context
     * @param pixelsId
     *            The pixels id
     * @throws DSAccessException
     *             If the connection is broken, or not logged in
     * @throws DSOutOfServiceException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void closeImage(SecurityContext ctx, long pixelsId)
            throws DSOutOfServiceException, DSAccessException {
        try {
            RawPixelsStorePrx store = pixelStores.get(pixelsId);
            if (store != null) {
                store.save();
                TransferFacility.pixelStores.remove(store);
            } else
                throw new Exception("No RawPixelsStore for image with pixels id " + pixelsId
                        + " found!");
        } catch (Throwable e) {
            handleException(this, e, "Couldn't close image with pixels id " + pixelsId);
        }
    }
    
}
