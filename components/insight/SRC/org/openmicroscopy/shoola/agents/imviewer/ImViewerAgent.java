/*
 * org.openmicroscopy.shoola.agents.iviewer.ImViewerAgent
 *
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

package org.openmicroscopy.shoola.agents.imviewer;

//Java imports
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;


import ome.model.units.BigResult;

//Third-party libraries
import org.apache.commons.collections.CollectionUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.FocusGainedEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.ImageViewport;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.events.iviewer.RendererUnloadedEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsSaved;
import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerState;
import org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded;
import org.openmicroscopy.shoola.agents.events.measurement.SelectChannel;
import org.openmicroscopy.shoola.agents.events.measurement.SelectPlane;
import org.openmicroscopy.shoola.agents.events.metadata.ChannelSavedEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.DeleteObjectEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.DisplayModeEvent;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.ReloadRenderingEngine;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.events.ViewInPluginEvent;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.env.ui.ViewObjectEvent;

import pojos.ChannelData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.WellSampleData;

/** 
 * The ImViewer agent. This agent displays an <code>Image</code> and the 
 * controls to modify the rendering settings.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">
 *              donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class ImViewerAgent
    implements Agent, AgentEventListener
{

    /** The default error message. */
    public static final String ERROR = " An error occurred while modifying " +
            "the rendering settings.";

    /** Reference to the registry. */
    private static Registry registry; 

    /** The display mode.*/
    private int displayMode = -1;

    /**
     * Helper method.
     *
     * @return A reference to the <code>Registry</code>.
     */
    public static Registry getRegistry() { return registry; }

    /**
     * Helper method returning the current user's details.
     *
     * @return See above.
     */
    public static ExperimenterData getUserDetails()
    { 
        return (ExperimenterData) registry.lookup(
                LookupNames.CURRENT_USER_DETAILS);
    }

    /**
     * Helper method returning <code>true</code> if the connection is fast,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public static boolean isFastConnection()
    {
        int value = (Integer) registry.lookup(LookupNames.CONNECTION_SPEED);
        return value == ImViewer.UNCOMPRESSED;
    }

    /**
     * Handles the {@link ViewImage} event.
     *
     * @param evt The event to handle.
     */
    private void handleViewImage(ViewImage evt)
    {
        if (evt == null) return;
        Boolean available = (Boolean)
                registry.lookup(LookupNames.BINARY_AVAILABLE);
        if (available != null && !available.booleanValue()) return;
        List<ViewImageObject> images = evt.getImages();
        ViewImageObject object = images.get(0);
        DataObject image = object.getImage();
        Rectangle r = evt.getRequesterBounds();
        ImViewer view = null;
        boolean b = evt.isSeparateWindow();
        if (image != null) {
            if (evt.getPlugin() > 0) {
                ViewInPluginEvent event = new ViewInPluginEvent(
                        evt.getSecurityContext(), image, evt.getPlugin());
                EventBus bus = registry.getEventBus();
                bus.post(event);
                return;
            }
            PixelsData pixels = null;
            if (image instanceof ImageData) {
                pixels = ((ImageData) image).getDefaultPixels();
            } else if (image instanceof WellSampleData) {
                pixels = ((WellSampleData) image).getImage().getDefaultPixels();
            }
            if (pixels != null)
                view = ImViewerFactory.getImageViewer(evt.getSecurityContext(),
                        image, r, b);
        } else {
            if (evt.getPlugin() > 0) {
                ViewInPluginEvent event = new ViewInPluginEvent(
                        evt.getSecurityContext(),object.getImageID(),
                        evt.getPlugin());
                EventBus bus = registry.getEventBus();
                bus.post(event);
            } else {
                view = ImViewerFactory.getImageViewer(evt.getSecurityContext(),
                        object.getImageID(), r, b);
            }
        }

        if (view != null) {
            view.activate(object.getSettings(), object.getSelectedUserID(),
                    displayMode);
            view.setContext(object.getParent(), object.getGrandParent());
        }
    }
    
    /**
     * Convenience method for logging BigResult exceptions
     * 
     * @param src
     *            The origin of the exception
     * @param exception
     *            The exception
     * @param property
     *            The property which conversion triggered the exception
     */
    public static void logBigResultExeption(Object src, Object exception,
            String property) {
        if (exception instanceof BigResult) {
            ImViewerAgent
                    .getRegistry()
                    .getLogger()
                    .warn(src,
                            "Arithmetic overflow; "
                                    + property
                                    + " is "
                                    + ((BigResult) exception).result
                                            .doubleValue());
        }
    }

    /**
     * Handles the {@link SaveRelatedData} event.
     *
     * @param evt The event to handle.
     */
    private void handleSaveRelatedData(SaveRelatedData evt)
    {
        if (evt == null) return;
        ImViewerFactory.storeEvent(evt);
    }

    /**
     * Handles the {@link MeasurementToolLoaded} event.
     *
     * @param evt The event to handle.
     */
    private void handleMeasurementToolLoaded(MeasurementToolLoaded evt)
    {
        if (evt == null) return;
        MeasurementTool request = (MeasurementTool) evt.getACT();
        PixelsData pixels = request.getPixels();
        if (pixels == null) return;
        long pixelsID = pixels.getId();
        //TODO: review
        ImViewer view = ImViewerFactory.getImageViewer(evt.getSecurityContext(),
                pixelsID);
        if (view != null) {
            switch (evt.getIndex()) {
            case MeasurementToolLoaded.ADD:
                view.addToView(evt.getView());
                break;
            case MeasurementToolLoaded.REMOVE:
                view.removeFromView(evt.getView());
            }
        }
    }

    /**
     * Handles the {@link SelectPlane} event.
     *
     * @param evt The event to handle.
     */
    private void handleSelectPlane(SelectPlane evt)
    {
        if (evt == null) return;
        long pixelsID = evt.getPixelsID();
        ImViewer view = ImViewerFactory.getImageViewer(null, pixelsID);
        if (view != null && !view.isPlayingMovie()) {
            Rectangle r = evt.getBounds();
            if (r != null) {
                view.setSelectedRegion(evt.getDefaultZ(), evt.getDefaultT(),
                        evt.getBounds());
            } else 
                view.setSelectedXYPlane(evt.getDefaultZ(), evt.getDefaultT());
        }
    }

    /**
     * Handles the {@link SelectChannel} event.
     *
     * @param evt The event to handle.
     */
    private void handleSelectChannel(SelectChannel evt)
    {
        if (evt == null) return;
        long pixelsID = evt.getPixelsID();
        ImViewer view = ImViewerFactory.getImageViewer(null, pixelsID);
        if (view != null && !view.isPlayingMovie()) {
            List<Integer> l = view.getActiveChannels();
            List<Integer> channels = evt.getChannels();
            Iterator<Integer> i = channels.iterator();
            Integer c;
            while (i.hasNext()) {
                c = i.next();
                if (!l.contains(c)) {
                    view.setChannelSelection(c, true);
                }
            }
        }
    }

    /**
     * Handles the {@link CopyRndSettings} event.
     *
     * @param evt The event to handle.
     */
    public void handleCopyRndSettingsEvent(CopyRndSettings evt)
    {
        if (evt == null) return;
        ImViewerFactory.copyRndSettings(evt.getImage(), evt.getRndDef());
    }

    /**
     * Handles the {@link RndSettingsCopied} event.
     *
     * @param evt The event to handle.
     */
    private void handleRndSettingsCopied(RndSettingsCopied evt)
    {
        Collection<Long> ids = evt.getImagesIDs();
        if (CollectionUtils.isEmpty(ids)) return;
        Iterator<Long> i = ids.iterator();
        ImViewer view;
        long id = evt.getRefPixelsID();
        while (i.hasNext()) {
            view = ImViewerFactory.getImageViewerFromImage(null, i.next());
            if (view != null ) {
                if (view.getPixelsID() != id) {
                    view.pasteRenderingSettings();
                }
                view.reloadRenderingThumbs();
            }     
        }
    }
    /**
     * Handles the {@link RndSettingsSaved} event.
     *
     * @param evt The event to handle.
     */
    public void handleRndSettingsSavedEvent(RndSettingsSaved evt)
    {
        if (evt == null) return;
        ImViewerFactory.rndSettingsSaved(evt.getRefPixelsID(),
                evt.getSettings());
    }

    /**
     * Indicates to bring up the window if a related window gained focus
     *
     * @param evt The event to handle.
     */
    private void handleFocusGainedEvent(FocusGainedEvent evt)
    {
        /*
    	ImViewer viewer = ImViewerFactory.getImageViewer(
				evt.getPixelsID());
    	if (viewer == null) return;
    	if (viewer.getState() != ImViewer.DISCARDED ||
    		evt.getIndex() != FocusGainedEvent.VIEWER_FOCUS) {
			//viewer.toFront();
		}
         */
    }

    /**
     * Displays the passed rectangle if possible.
     *
     * @param evt The event to handle.
     */
    private void handleImageViewportEvent(ImageViewport evt)
    {
        if (evt == null) return;
        ImViewer viewer = ImViewerFactory.getImageViewer(null,
                evt.getPixelsID());
        if (viewer == null) return;
        viewer.scrollToViewport(evt.getBounds());
    }

    /**
     * Removes all the references to the existing viewers.
     *
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
        if (evt == null) return;
        ImViewerFactory.onGroupSwitched(evt.isSuccessful());
    }

    /**
     * Indicates that it was possible to reconnect.
     *
     * @param evt The event to handle.
     */
    private void handleReconnectedEvent(ReconnectedEvent evt)
    {
        ImViewerFactory.onGroupSwitched(true);
    }

    /**
     * Views the passed object if the object is an image.
     *
     * @param evt The event to handle.
     */
    private void handleViewObjectEvent(ViewObjectEvent evt)
    {
        if (evt == null) return;
        Object o = evt.getObject();
        if (evt.browseObject()) return;
        if (o instanceof ImageData) {
            if (evt.getPlugin() > 0) {
                JComponent src = evt.getSource();
                if (src != null) src.setEnabled(true);
                ViewInPluginEvent event = new ViewInPluginEvent(
                        evt.getSecurityContext(), ((ImageData) o).getId(),
                        evt.getPlugin());
                EventBus bus = registry.getEventBus();
                bus.post(event);
            } else {
                ImViewer view = ImViewerFactory.getImageViewer(
                        evt.getSecurityContext(), ((ImageData) o).getId(),
                        null, true);
                if (view != null) {
                    view.activate(null, getUserDetails().getId(),
                            displayMode);
                    JComponent src = evt.getSource();
                    if (src != null) src.setEnabled(true);
                }
            }
        }
    }

    /**
     * Indicates that the rendered could not be loaded.
     *
     * @param evt The event to handle.
     */
    private void handleRendererUnloadedEvent(RendererUnloadedEvent evt)
    {
        if (evt == null) return;
        ImViewer viewer = ImViewerFactory.getImageViewer(null,
                evt.getPixelsID());
        if (viewer == null) return;
        viewer.discard();
    }

    /**
     * Checks the files to delete and see if a viewer is opened.
     *
     * @param evt The event to handle.
     */
    private void handleDeleteObjectEvent(DeleteObjectEvent evt)
    {
        if (evt == null) return;
        List<DataObject> objects = evt.getObjects();
        if (objects == null) return;
        Iterator<DataObject> i = objects.iterator();
        DataObject object;
        ImViewer viewer;
        EventBus bus = registry.getEventBus();
        while (i.hasNext()) {
            object = i.next();
            if (object instanceof ImageData) {
                checkImageForDelete((ImageData) object);
            } else {
                viewer = ImViewerFactory.getImageViewerFromParent(object);
                if (viewer != null) {
                    //Post an event to discard Measurement tool
                    ViewerState event = new ViewerState(viewer.getPixelsID(),
                            ViewerState.CLOSE);
                    bus.post(event);
                    viewer.discard();
                }
            }
        }
    }

    /**
     * Closes the viewer b/c the rendering engine could not be loaded.
     *
     * @param evt The event to handle.
     */
    private void handleReloadRenderingEngineEvent(ReloadRenderingEngine evt)
    {
        if (evt == null) return;
        List<Long> pixels = evt.getPixelsID();
        if (CollectionUtils.isEmpty(pixels)) return;
        Iterator<Long> i = pixels.iterator();
        Long id;
        ImViewer viewer;
        UserNotifier un = registry.getUserNotifier();
        while (i.hasNext()) {
            id = i.next();
            viewer = ImViewerFactory.getImageViewer(null, id);
            if (viewer != null) {
                un.notifyInfo("Reload", "The rendering engine could not be " +
                        "reloaded for " + viewer.getUI().getTitle()+
                        ".\nThe viewer will now close.");
                viewer.discard();
            }
        }
    }

    /**
     * Updates the view when the channels have been updated.
     *
     * @param evt The event to handle.
     */
    private void handleChannelSavedEvent(ChannelSavedEvent evt)
    {
        List<ChannelData> channels = evt.getChannels();
        Iterator<Long> i = evt.getImageIds().iterator();
        SecurityContext ctx = evt.getSecurityContext();
        ImViewer viewer;
        while (i.hasNext()) {
            viewer = ImViewerFactory.getImageViewerFromImage(ctx, i.next());
            if (viewer != null) {
                viewer.onUpdatedChannels(channels);
            }
        }
    }

    /**
     * Updates the view when the mode is changed.
     *
     * @param evt The event to handle.
     */
    private void handleDisplayModeEvent(DisplayModeEvent evt)
    {
        displayMode = evt.getDisplayMode();
        ImViewerFactory.setDisplayMode(displayMode);
    }

    /**
     * Checks if the passed image is actually opened in the viewer.
     *
     * @param image The image to handle.
     */
    private void checkImageForDelete(ImageData image)
    {
        if (image.getId() < 0) return;
        PixelsData pixels = image.getDefaultPixels();
        if (pixels == null) return;
        EventBus bus = registry.getEventBus();
        ImViewer viewer = ImViewerFactory.getImageViewer(null, pixels.getId());
        if (viewer != null) {
            //Post an event to discard Measurement tool
            ViewerState event = new ViewerState(viewer.getPixelsID(),
                    ViewerState.CLOSE);
            bus.post(event);
            viewer.discard();
        }
    }

    /** Creates a new instance. */
    public ImViewerAgent() {}

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate(boolean)
     */
    public void activate(boolean master) {}

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#terminate()
     */
    public void terminate()
    {
        Environment env = (Environment) registry.lookup(LookupNames.ENV);
        if (env.isRunAsPlugin())
            ImViewerFactory.onGroupSwitched(true);
    }

    /** 
     * Implemented as specified by {@link Agent}.
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, ViewImage.class);
        bus.register(this, MeasurementToolLoaded.class);
        bus.register(this, SelectPlane.class);
        bus.register(this, CopyRndSettings.class);
        bus.register(this, SaveRelatedData.class);
        bus.register(this, FocusGainedEvent.class);
        bus.register(this, ImageViewport.class);
        bus.register(this, UserGroupSwitched.class);
        bus.register(this, ViewObjectEvent.class);
        bus.register(this, RendererUnloadedEvent.class);
        bus.register(this, DeleteObjectEvent.class);
        bus.register(this, RndSettingsSaved.class);
        bus.register(this, ReloadRenderingEngine.class);
        bus.register(this, ReconnectedEvent.class);
        bus.register(this, SelectChannel.class);
        bus.register(this, ChannelSavedEvent.class);
        bus.register(this, DisplayModeEvent.class);
        bus.register(this, RndSettingsCopied.class);
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#canTerminate()
     */
    public boolean canTerminate() { return true; }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#getDataToSave()
     */
    public AgentSaveInfo getDataToSave()
    {
        List<Object> list = ImViewerFactory.getInstancesToSave();
        if (CollectionUtils.isEmpty(list)) return null;
        return new AgentSaveInfo("Image Viewer", list);
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#save(List)
     */
    public void save(List<Object> instances)
    {
        ImViewerFactory.saveInstances(instances);
    }

    /**
     * Responds to events fired trigger on the bus.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof ViewImage) 
            handleViewImage((ViewImage) e);
        else if (e instanceof MeasurementToolLoaded)
            handleMeasurementToolLoaded((MeasurementToolLoaded) e);
        else if (e instanceof SelectPlane)
            handleSelectPlane((SelectPlane) e);
        else if (e instanceof CopyRndSettings)
            handleCopyRndSettingsEvent((CopyRndSettings) e);
        else if (e instanceof SaveRelatedData)
            handleSaveRelatedData((SaveRelatedData) e);
        else if (e instanceof FocusGainedEvent)
            handleFocusGainedEvent((FocusGainedEvent) e);
        else if (e instanceof ImageViewport)
            handleImageViewportEvent((ImageViewport) e);
        else if (e instanceof UserGroupSwitched)
            handleUserGroupSwitched((UserGroupSwitched) e);
        else if (e instanceof ViewObjectEvent)
            handleViewObjectEvent((ViewObjectEvent) e);
        else if (e instanceof RendererUnloadedEvent)
            handleRendererUnloadedEvent((RendererUnloadedEvent) e);
        else if (e instanceof DeleteObjectEvent)
            handleDeleteObjectEvent((DeleteObjectEvent) e);
        else if (e instanceof RndSettingsSaved) 
            handleRndSettingsSavedEvent((RndSettingsSaved) e);
        else if (e instanceof ReloadRenderingEngine) 
            handleReloadRenderingEngineEvent((ReloadRenderingEngine) e);
        else if (e instanceof ReconnectedEvent)
            handleReconnectedEvent((ReconnectedEvent) e);
        else if (e instanceof SelectChannel)
            handleSelectChannel((SelectChannel) e);
        else if (e instanceof ChannelSavedEvent)
            handleChannelSavedEvent((ChannelSavedEvent) e);
        else if (e instanceof DisplayModeEvent)
            handleDisplayModeEvent((DisplayModeEvent) e);
        else if (e instanceof RndSettingsCopied)
            handleRndSettingsCopied((RndSettingsCopied) e);
    }

}
