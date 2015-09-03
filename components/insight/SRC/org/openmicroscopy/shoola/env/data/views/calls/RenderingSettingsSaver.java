/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DataSourceException;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;

/** 
* Command to paste the rendering settings.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* @since OME3.0
*/
public class RenderingSettingsSaver
extends BatchCallTree
{

    /** Indicates to paste the rendering settings. */
    public static final int PASTE = 0;

    /** Indicates to reset the rendering settings. */
    public static final int RESET = 1;

    /** Indicates to reset the rendering settings. */
    public static final int SET_MIN_MAX = 2;

    /** Indicates to create rendering settings. */
    public static final int CREATE = 3;

    /** Indicates to reset the rendering settings. */
    public static final int SET_OWNER = 4;

    /** Result of the call. */
    private Object result;

    /** Loads the specified tree. */
    private BatchCall loadCall;

    /** The security context.*/
    private SecurityContext ctx;

    /** 
     * Controls if the passed type is supported.
     *
     * @param type The type to check;
     */
    private void checkRootType(Class type)
    {
        if (ImageData.class.equals(type) || DatasetData.class.equals(type) ||
                PlateData.class.equals(type) || ProjectData.class.equals(type)
                || ScreenData.class.equals(type) ||
                PlateAcquisitionData.class.equals(type))
            return;
        throw new IllegalArgumentException("Type not supported.");
    }

    /**
     * Creates a {@link BatchCall} to paste the rendering settings.
     * 
     * @param pixelsID The id of the pixels set of reference.
     * @param rootType The type of nodes. Can either be 
     *                 <code>ImageData</code>, <code>DatasetData</code>, 
     *                 <code>ProjectData</code>, <code>ScreenData</code>,
     *                 <code>PlateData</code>.
     * @param ids The id of the nodes to apply settings to. 
     * @param index One of the constants defined by this class.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long pixelsID, final Class rootType,
            final List<Long> ids, final int index)
    {
        return new BatchCall("Modify the rendering settings: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                switch (index) {
                    case PASTE:
                        result = rds.pasteRenderingSettings(ctx, pixelsID,
                                rootType, ids);
                        break;
                    case RESET:
                        result = rds.resetRenderingSettings(ctx, rootType, ids);
                        break;
                    case SET_MIN_MAX:
                        result = rds.setMinMaxSettings(ctx, rootType, ids);
                        break;
                    case SET_OWNER:
                        result = rds.setOwnerRenderingSettings(ctx, rootType, 
                                ids);
                }
            }
        };
    } 

    /**
     * Creates a {@link BatchCall} to paste the rendering settings.
     * 
     * @param rootType The type of nodes. Can either be
     *                 <code>ImageData</code>, <code>DatasetData</code>, 
     *                  <code>ProjectData</code>, <code>ScreenData</code>,
     *                  <code>PlateData</code>.
     * @param ids The id of the nodes to apply settings to.
     * @param def The rendering settings to paste
     * @param refImage The image the rendering settings belong to
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Class rootType,
            final List<Long> ids, final RndProxyDef def, final ImageData refImage) {
        return new BatchCall("Modify the rendering settings: ") {
            public void doCall() throws Exception {

                long userId = MetadataViewerAgent.getUserDetails().getId();

                OmeroImageService rds = context.getImageService();

                // load (the user's) original settings for the reference image             
                RndProxyDef original = null;
                Map<DataObject, Collection<RndProxyDef>> tmp = rds.getRenderingSettings(ctx, refImage.getDefaultPixels().getId(), userId);
                for(Entry<DataObject, Collection<RndProxyDef>> e : tmp.entrySet()) {
                    if(e.getKey() instanceof ExperimenterData) {
                        ExperimenterData exp = (ExperimenterData)e.getKey();
                        if(exp.getId()==userId) {
                            original = e.getValue().iterator().next();
                            break;
                        }
                    }
                }

                // reload the Renderer for the reference image
                MetadataViewer viewer = MetadataViewerFactory
                        .getViewer(refImage);
                viewer.reloadRenderingControl();

                Renderer rnd = null;
                if (viewer != null) {
                    rnd = viewer.getRenderer();

                    if (rnd != null) {
                        try {
                            // apply the pending rendering settings
                            rnd.resetSettings(def, true);
                            rnd.saveCurrentSettings();
                        } catch (Throwable e) {
                            throw new DataSourceException("Could not save pending rendering settings for image id "+refImage.getId());
                        } 

                    }
                }

                // do the actual paste
                Map map = rds.pasteRenderingSettings(ctx, refImage.getDefaultPixels().getId(), rootType,
                        ids);
                result = map;

                boolean refImagePartOfSaved = ((List<Long>)map.get(Boolean.TRUE)).contains(refImage.getId());
                
                if (rnd != null && original != null && !refImagePartOfSaved) {
                    // reset the reference image to it's previous settings
                    rnd.resetSettings(original, true);
                    rnd.saveCurrentSettings();
                }
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to paste the rendering settings.
     * 
     * @param pixelsID The id of the pixels set of reference.
     * @param ref The time reference object.
     * @param index One of the constants defined by this class.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long pixelsID,
            final TimeRefObject ref, final int index)
    {
        return new BatchCall("Modify the rendering settings: ") {
            public void doCall() throws Exception
            {
                long userID = ((ExperimenterData) context.lookup(
                        LookupNames.CURRENT_USER_DETAILS)).getId();
                OmeroDataService os = context.getDataService();
                Collection l = os.getImagesPeriod(ctx, ref.getStartTime(),
                        ref.getEndTime(), userID, true); 
                if (l != null) {
                    Iterator i = l.iterator();
                    DataObject element;
                    List<Long> ids = new ArrayList<Long>(l.size());
                    while (i.hasNext()) {
                        element = (DataObject) i.next();
                        ids.add(element.getId());
                    }

                    OmeroImageService rds = context.getImageService();
                    switch (index) {
                        case PASTE:
                            result = rds.pasteRenderingSettings(ctx, pixelsID,
                                    ImageData.class, ids);
                            break;
                        case RESET:
                            result = rds.resetRenderingSettings(ctx,
                                    ImageData.class, ids);
                            break;
                        case SET_MIN_MAX:
                            result = rds.setMinMaxSettings(ctx, ImageData.class,
                                    ids);
                            break;
                        case SET_OWNER:
                            result = rds.setOwnerRenderingSettings(ctx,
                                    ImageData.class, ids);
                            break;
                    }
                }
            }
        };
    } 

    /**
     *  Creates a {@link BatchCall} to create the rendering settings.
     * 
     * @param pixelsID The id of the pixels set.
     * @param rndToCopy The rendering setting to copy.
     * @param indexes Collection of channel's indexes.
     *                Mustn't be <code>null</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCreateBatchCall(final long pixelsID, 
            final RndProxyDef rndToCopy, final List<Integer> indexes)
    {
        return new BatchCall("Paste the rendering settings: ") {
            public void doCall() throws Exception
            {
                OmeroImageService os = context.getImageService();
                result = os.createRenderingSettings(ctx, pixelsID, rndToCopy,
                        indexes);
            }
        };
    }

    /**
     * Adds the {@link #loadCall} to the computation tree.
     *
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the {@link RenderingControl}.
     *
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param rootNodeType The type of nodes. Can either be 
     *                      code>ImageData</code>, <code>DatasetData</code>, 
     *                      <code>ProjectData</code>, <code>ScreenData</code>,
     *                      <code>PlateData</code>.
     * @param ids The nodes to apply settings to. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    public RenderingSettingsSaver(SecurityContext ctx, Class rootNodeType,
            List<Long> ids, int index)
    {
        checkRootType(rootNodeType);
        if (CollectionUtils.isEmpty(ids))
            throw new IllegalArgumentException("No nodes specified.");
        this.ctx = ctx;
        loadCall = makeBatchCall(-1, rootNodeType, ids, index);
    }

    /**
     * Creates a new instance.
     *
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set of reference.
     * @param rootNodeType The type of nodes. Can either be 
     *                     code>ImageData</code>, <code>DatasetData</code>.
     * @param ids The nodes to apply settings to. Mustn't be <code>null</code>.
     */
    public RenderingSettingsSaver(SecurityContext ctx, long pixelsID,
            Class rootNodeType, List<Long> ids)
    {
        checkRootType(rootNodeType);
        if (CollectionUtils.isEmpty(ids))
            throw new IllegalArgumentException("No nodes specified.");
        if (pixelsID < 0)
            throw new IllegalArgumentException("Pixels ID not valid.");
        this.ctx = ctx;
        loadCall = makeBatchCall(pixelsID, rootNodeType, ids, PASTE);
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param rootNodeType The type of nodes. Can either be 
     *                     <code>ImageData</code>, <code>DatasetData</code>.
     * @param ids The nodes to apply settings to. Mustn't be <code>null</code>.
     * @param def The 'pending' rendering settings
     * @param refImage The image the rendering settings belong to
     */
    public RenderingSettingsSaver(SecurityContext ctx, Class rootNodeType, 
            List<Long> ids, RndProxyDef def, final ImageData refImage) {
        checkRootType(rootNodeType);
        if (CollectionUtils.isEmpty(ids))
            throw new IllegalArgumentException("No nodes specified.");
        if (refImage == null)
            throw new IllegalArgumentException("No reference image provided.");
        this.ctx = ctx;
        loadCall = makeBatchCall(rootNodeType, ids, def, refImage);
    }

    /**
     * Creates a new instance.
     *
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set of reference.
     * @param ref The time reference object.
     */
    public RenderingSettingsSaver(SecurityContext ctx, long pixelsID,
            TimeRefObject ref)
    {
        if (pixelsID < 0)
            throw new IllegalArgumentException("Pixels ID not valid.");
        if (ref == null)
            throw new IllegalArgumentException("Period not valid.");
        this.ctx = ctx;
        loadCall = makeBatchCall(pixelsID, ref, PASTE);
    }

    /**
     * Creates a new instance.
     *
     * @param ctx The security context.
     * @param ref The time reference object.
     * @param index  One of the constants defined by this class.
     */
    public RenderingSettingsSaver(SecurityContext ctx, TimeRefObject ref,
            int index)
    {
        if (ref == null)
            throw new IllegalArgumentException("Period not valid.");
        this.ctx = ctx;
        loadCall = makeBatchCall(-1, ref, index);
    }

    /**
     * Creates a new instance.
     *
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     * @param rndToCopy The rendering setting to copy.
     * @param indexes Collection of channel's indexes.
     *                Mustn't be <code>null</code>.
     */
    public RenderingSettingsSaver(SecurityContext ctx, long pixelsID,
            RndProxyDef rndToCopy,List<Integer> indexes)
    {
        if (pixelsID < 0)
            throw new IllegalArgumentException("Pixels ID not valid.");
        this.ctx = ctx;
        loadCall = makeCreateBatchCall(pixelsID, rndToCopy, indexes);
    }

}
