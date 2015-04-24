/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.emory.mathcs.backport.java.util.Arrays;
import ome.formats.importer.IObserver;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.ImportException;
import omero.gateway.model.ImportCallback;
import omero.gateway.model.ImportableFile;
import omero.gateway.model.ImportableObject;
import pojos.ExperimenterData;

/**
 * {@link Facility} which provides data transfer functionality, i. e. download
 * files and upload/import files.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TransferFacility extends Facility {

    private TransferFacilityHelper helper;
    private DataManagerFacility datamanager;
    
    TransferFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.datamanager = gateway.getFacility(DataManagerFacility.class);
        this.helper = new TransferFacilityHelper(gateway, datamanager, this);
    }

    public void uploadImage(SecurityContext context, File image, 
            ImportCallback observer) throws DSAccessException,
            DSOutOfServiceException, ImportException {
        uploadImage(context, image, true, false, gateway.getLoggedInUser(),
            observer);
    }
    public void uploadImage(SecurityContext context, File image, boolean folderAsContainer, boolean overrideName, ExperimenterData user,
            ImportCallback observer) throws DSAccessException,
            DSOutOfServiceException, ImportException {
        ImportableFile imf = new ImportableFile(image, folderAsContainer);
        imf.setGroup(user.getDefaultGroup());
        imf.setStatus(observer);
        
        @SuppressWarnings("unchecked")
        ImportableObject imo = new ImportableObject(Arrays.asList(new ImportableFile[]{imf}),
                overrideName);
        
        helper.importFile(imo, imf, user, true);
    }

    public void uploadImage(SecurityContext context, File image,
            IObserver observer, String username, String groupname)
            throws DSAccessException, DSOutOfServiceException {
    }

    public List<File> downloadImage(SecurityContext context, String targetPath,
            long imageId) throws DSAccessException {
        return null;
    }

}
