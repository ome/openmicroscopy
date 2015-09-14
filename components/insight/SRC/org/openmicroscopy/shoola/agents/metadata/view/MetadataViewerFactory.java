/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import omero.model.NamedValue;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.gateway.model.WellSampleData;

/** 
 * Factory to create {@link MetadataViewer} component.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class MetadataViewerFactory
    implements ChangeListener
{

    /** The sole instance. */
    private static final MetadataViewerFactory  
    singleton = new MetadataViewerFactory();

    /** Reference to an image from which the rnd settings can be copied */
    private static ImageData copyRenderingSettingsFrom;

    /** 'Pending' rendering settings not yet stored with an image */
    private static RndProxyDef copiedRndSettings;

    /** Copy/Paste storage for MapAnnotations */
    private static List<NamedValue> copiedMapAnnotationsEntries = new ArrayList<NamedValue>();

    /**
     * Returns the {@link MetadataViewer}.
     * 
     * @param data	The object viewed as the root of the browser.
     * @param type  The type of data object to handle, if type is either
     *              <code>DatasetData</code> or <code>TagAnnotationData</code>,
     *              this implies that the viewer is for a batch annotation.
     * @return See above.
     */
    public static MetadataViewer getViewer(List<Object> data, Class type)
    {
        if (data == null || data.size() == 0)
            throw new IllegalArgumentException("No data to edit");
        MetadataViewerModel model = new MetadataViewerModel(data, 
                MetadataViewer.RND_GENERAL);
        model.setDataType(type);
        return singleton.createViewer(model);
    }

    /**
     * Returns the {@link MetadataViewer}.
     * 
     * @param refObject	The object viewed as the root of the browser.
     * @return See above.
     */
    public static MetadataViewer getViewer(Object refObject)
    {
        return getViewer(refObject, MetadataViewer.RND_GENERAL);
    }

    /**
     * Returns the {@link MetadataViewer}.
     * 
     * @param refObject	The object viewed as the root of the browser.
     * @param index 	One of the following constants: 
     * 					{@link MetadataViewer#RND_GENERAL} or
     * 					{@link MetadataViewer#RND_SPECIFIC}.
     * @return See above.
     */
    public static MetadataViewer getViewer(Object refObject, int index)
    {
        MetadataViewerModel model = new MetadataViewerModel(refObject, index);
        return singleton.createViewer(model);
    }

    /**
     * Returns the {@link MetadataViewer} associated to the object specified.
     * 
     * @param objectType The type of object to handle.
     * @param id The id of the object the component is for.
     * @return See above.
     */
    public static MetadataViewer getViewerFromId(String objectType, long id)
    {
        Iterator<MetadataViewer> i = singleton.viewers.iterator();
        MetadataViewerComponent viewer;
        Object ref;
        String name;
        long refId;
        while (i.hasNext()) {
            viewer = (MetadataViewerComponent) i.next();
            ref = viewer.getRefObject();
            name = ref.getClass().getName();
            if (ref instanceof WellSampleData)
                name = ImageData.class.getName();
            if (name.equals(objectType) && ref instanceof DataObject) {
                refId =  ((DataObject) ref).getId();
                if (ref instanceof WellSampleData)
                    refId = ((WellSampleData) ref).getImage().getId();
                if (id == refId) return viewer;
            }

        }
        return null;
    }

    /**
     * Returns the instances to save.
     * 
     * @return See above.
     */
    public static List<Object> getInstancesToSave()
    {
        if (singleton.viewers.size() == 0) return null;
        List<Object> instances = new ArrayList<Object>();
        Iterator<MetadataViewer> i = singleton.viewers.iterator();
        MetadataViewerComponent comp;
        while (i.hasNext()) {
            comp = (MetadataViewerComponent) i.next();
            if (comp.hasDataToSave()) instances.add(comp);
        }
        return instances;
    }

    /**
     * Sets the display mode.
     * 
     * @param displayMode The value to set.
     */
    public static void setDiplayMode(int displayMode)
    {
        Iterator<MetadataViewer> i = singleton.viewers.iterator();
        MetadataViewerComponent comp;
        while (i.hasNext()) {
            comp = (MetadataViewerComponent) i.next();
            comp.setDisplayMode(displayMode);
        }
    }

    /** 
     * Saves the passed instances and discards them. 
     * 
     * @param instances The instances to save.
     */
    public static void saveInstances(List<Object> instances)
    {
        //if (singleton.viewers.size() == 0) return;
        if (instances != null) {
            Iterator i = instances.iterator();
            MetadataViewerComponent comp;
            Object o;
            while (i.hasNext()) {
                o = i.next();
                if (o instanceof MetadataViewerComponent) {
                    ((MetadataViewerComponent) o).saveBeforeClose();
                }
            }
        }
    }

    /**
     * Notifies the model that the user's group has successfully be modified
     * if the passed value is <code>true</code>, unsuccessfully 
     * if <code>false</code>.
     * 
     * @param success 	Pass <code>true</code> if successful, <code>false</code>
     * 					otherwise.
     */
    public static void onGroupSwitched(boolean success)
    {
        if (!success) return;
        singleton.clear();
    }

    /** All the tracked components. */
    private List<MetadataViewer>	viewers;

    /** Creates a new instance. */
    private MetadataViewerFactory()
    {
        viewers = new ArrayList<MetadataViewer>();
    }

    /** Discards and clears.*/
    private void clear()
    {
        Iterator<MetadataViewer> i = singleton.viewers.iterator();
        MetadataViewer v;
        while (i.hasNext()) {
            v = i.next();
            v.removeChangeListener(this);
            v.discard();
        }
        singleton.viewers.clear();
    }

    /**
     * Creates and returns a {@link MetadataViewer}.
     * 
     * @param model	The Model.
     * @return See above.
     */
    private MetadataViewer createViewer(MetadataViewerModel model)
    {
        MetadataViewerComponent comp = new MetadataViewerComponent(model);
        model.initialize(comp);
        comp.initialize();
        comp.addChangeListener(this);
        viewers.add(comp);
        return comp;
    }

    /**
     * Removes a viewer from the {@link #viewers} set when it is
     * {@link MetadataViewer#DISCARDED discarded}. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        MetadataViewerComponent comp = (MetadataViewerComponent) e.getSource(); 
        if (comp.getState() == MetadataViewer.DISCARDED && viewers.size() > 0) 
            viewers.remove(comp);
    }

    /**
     * Applies the settings of a previous set image to
     * the renderer (does not save them).
     */
    public static void applyCopiedRndSettings(long imageId) {
        for(MetadataViewer viewer : singleton.viewers) {
            Object obj = viewer.getRefObject();
            if(obj instanceof ImageData) {
                ImageData img = (ImageData) obj;
                if(img.getId()==imageId) {
                    viewer.applyCopiedRndSettings();
                }
            }
            if (obj instanceof WellSampleData) {
                ImageData img = ((WellSampleData) obj).getImage();
                if(img.getId()==imageId) {
                    viewer.applyCopiedRndSettings();
                }
            }
        }
    }

    /**
     * Checks if there have been rendering settings copied
     * which could be pasted to the image with the given imageId.
     *
     * @param imageId  The image to check for copied rendering
     *                 settings
     *
     * @return See above
     */
    public static boolean hasRndSettingsCopied(long imageId) {
        for(MetadataViewer viewer : singleton.viewers) {
            Object obj = viewer.getRefObject();
            if(obj instanceof ImageData) {
                ImageData img = (ImageData) obj;
                if(img.getId()==imageId) {
                    return viewer.hasRndSettingsCopied();
                }
            }
            else if (obj instanceof WellSampleData) {
                ImageData img = ((WellSampleData)obj).getImage();
                if(img.getId()==imageId) {
                    return viewer.hasRndSettingsCopied();
                }
            }
        }
        return false;
    }

    /**
     * Get the image from which to copy the rendering settings 
     * from
     * @return See above
     */
    public static ImageData getCopyRenderingSettingsFrom() {
        return MetadataViewerFactory.copyRenderingSettingsFrom;
    }

    /**
     * Sets a reference to an image which settings can be applied (copied) to 
     * the renderer.
     * See also {@link #applyCopiedRndSettings(long)}
     * @param copyRenderingSettingsFrom The image to copy the rendering settings from
     * @param copiedRndSettings 'Pending' rendering settings
     */
    public static void setCopyRenderingSettingsFrom(ImageData copyRenderingSettingsFrom, RndProxyDef copiedRndSettings) {
        MetadataViewerFactory.copyRenderingSettingsFrom = copyRenderingSettingsFrom;
        MetadataViewerFactory.copiedRndSettings = copiedRndSettings;
    }

    /**
     * Get the rendering settings which can be applied to the renderer
     * @return See above.
     */
    public static RndProxyDef getCopiedRndSettings() {
        return MetadataViewerFactory.copiedRndSettings;
    }

    /**
     * Access the copy/paste storage for MapAnnotations
     * 
     * @return A list of MapAnnotation entries or an empty list if there aren't
     *         any
     */
    public static List<NamedValue> getCopiedMapAnnotationsEntries() {
        return copiedMapAnnotationsEntries;
    }

}
