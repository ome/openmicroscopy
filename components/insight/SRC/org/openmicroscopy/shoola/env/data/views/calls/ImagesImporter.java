/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImagesImporter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.ImportRequestData;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.svc.SvcRegistry;
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;

import com.google.gson.Gson;

/** 
 * Command to import images in a container if specified.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImagesImporter
    extends BatchCallTree
{
    /** 
     * Map of result, key is the file to import, value is an object or a
     * string.
     */
    private Map<ImportableFile, Object> partialResult;

    /** The object hosting the information for the import. */
    private ImportableObject object;

    /** recycle the session key.*/
    private String sessionKey;

    /**
     * Imports the file.
     *
     * @param ImportableFile The file to import.
     * @param Pass <code>true</code> to close the import,
     *        <code>false</code> otherwise.
     */
    private void importFile(ImportableFile importable, boolean close)
    {
        partialResult = new HashMap<ImportableFile, Object>();
        //To be read from config.
        Boolean offline = (Boolean)
                context.lookup(LookupNames.OFFLINE_IMPORT_ENABLED);
        if (offline != null && offline) {
            String tokenURL = (String)
                    context.lookup(LookupNames.OFFLINE_IMPORT_URL);
            Communicator c;
            CommunicatorDescriptor desc = new CommunicatorDescriptor
                (HttpChannel.CONNECTION_PER_REQUEST, tokenURL, -1);
            try {
                //code not ready for sudo operation
                //check creation of tags and containers
                OmeroImageService os = context.getImageService();
                Object o = os.importFile(object, importable, close);
                if (o instanceof ImportException) {
                    partialResult.put(importable, o);
                    return;
                }
                if (o instanceof Boolean) {
                    Boolean b = (Boolean) o;
                    if (!b.booleanValue() ||
                          importable.getStatus().isMarkedAsDuplicate()) {
                        partialResult.put(importable, o);
                        return;
                    }
                }
                AdminService svc = context.getAdminService();
                c = SvcRegistry.getCommunicator(desc);
                ImportRequestData data = new ImportRequestData();
                ExperimenterData exp = importable.getUser();
                if (exp == null) {
                    exp = svc.getUserDetails();
                }
                data.experimenterEmail = exp.getEmail();
                data.omeroHost = svc.getServerName();
                if (svc.getPort() > 0) {
                    data.omeroPort = ""+svc.getPort();
                }
                data.targetUri = importable.getOriginalFile().getAbsolutePath();
                DataObject target = importable.getDataset();
                if (target != null && target.getId() > 0) {
                    data.datasetId = ""+target.getId();
                }
                target = importable.getParent();
                if (target != null) {
                    if (target instanceof ScreenData) {
                        data.screenId = ""+target.getId();
                    }
                }
                Collection<TagAnnotationData> tags = object.getTags();
                if (CollectionUtils.isNotEmpty(tags)) {
                    List<String> ids = new ArrayList<String>();
                    Iterator<TagAnnotationData> i = tags.iterator();
                    while (i.hasNext()) {
                        target = i.next();
                        if (target.getId() > 0) {
                            ids.add(""+target.getId());
                        }
                    }
                    data.annotationIds = ids.toArray(new String[ids.size()]);
                }
                //create a new client //no sudo for that demo
                if (sessionKey == null) {
                    omero.client cc = new omero.client(svc.getServerName(),
                            svc.getPort());
                    //use the login credentials.
                    UserCredentials uc = (UserCredentials)
                            context.lookup(LookupNames.USER_CREDENTIALS);
                    cc.createSession(uc.getUserName(), uc.getPassword());
                    sessionKey = cc.getSessionId();
                }
                data.sessionKey = sessionKey;
                //Prepare json string
                Gson writer = new Gson();
                c.enqueueImport(writer.toJson(data), new StringBuilder());
                importable.getStatus().markedAsOffLineImport();
                partialResult.put(importable, true);
            } catch (Exception e) {
                partialResult.put(importable, e);
            }
        } else {
            OmeroImageService os = context.getImageService();
            try {
                partialResult.put(importable, 
                        os.importFile(object, importable, close));
            } catch (Exception e) {
                partialResult.put(importable, e);
            }
        }
    }

    /**
     * Adds the {@link #importFile} to the computation tree.
     *
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    { 
        ImportableFile io;
        List<ImportableFile> files = object.getFiles();
        Iterator<ImportableFile> i = files.iterator();
        int index = 0;
        int n = files.size()-1;
        while (i.hasNext()) {
            io = (ImportableFile) i.next();
            final ImportableFile f = io;
            final boolean b = index == n;
            index++;
            add(new BatchCall("Importing file") {
                public void doCall() { importFile(f, b); }
            }); 
        }
    }

    /**
     * Returns the lastly retrieved thumbnail.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     *
     * @return A Map whose key is the file to import and the value the
     *         imported object.
     */
    protected Object getPartialResult() { return partialResult; }

    /**
     * Returns the root node of the requested tree.
     *
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }

    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
     * exception so to fail early and in the call.
     *
     * @param object The object hosting all import information.
     */
    public ImagesImporter(ImportableObject object)
    {
        if (object == null || CollectionUtils.isEmpty(object.getFiles()))
            throw new IllegalArgumentException("No Files to import.");
        this.object = object;
    }

}
