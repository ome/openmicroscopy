/*
 * org.openmicroscopy.shoola.agents.browser.BrowserModel
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

import java.util.*;

import org.openmicroscopy.is.CompositingSettings;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethodZOrder;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.layout.GroupModel;
import org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod;
import org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod;
import org.openmicroscopy.shoola.agents.browser.layout.SingleGroupingMethod;

/**
 * The backing data model for the browser (not including overlays) and
 * currently browsed set in the top browser component's MVC architecture.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserModel
{
    private BrowserEnvironment env;

    private ThumbnailSourceModel sourceModel;
    private Set thumbnailSet;
    
    private Set progressListeners;
    private Set modelListeners;

    private LayoutMethod layoutMethod;
    private List groupModels;
    private GroupingMethod groupingMethod;
    
    private CompositingSettings renderSettings;
    private PaintMethodZOrder annotationModel;

    private Set selectedImages;
    private Set hiddenImages;

    private BrowserMode currentMode;

    // common initialization routine
    private void init()
    {
        env = BrowserEnvironment.getInstance();
        progressListeners = new HashSet();
        modelListeners = new HashSet();
        selectedImages = new HashSet();
        hiddenImages = new HashSet();
        groupModels = new ArrayList();
        thumbnailSet = new HashSet();
        annotationModel = new PaintMethodZOrder();

        // default behavior (may replace later)
        currentMode = BrowserMode.DEFAULT_MODE;
    }

    /**
     * Creates a BrowserModel with an empty backing ThumbnailSourceModel.  That is,
     * there are not yet any loaded thumbnails, although the model is accessible.
     *
     */
    public BrowserModel()
    {
        init();
        GroupModel model = new GroupModel("all");
        groupModels.add(model);
        groupingMethod = new SingleGroupingMethod(model);
    }

    /**
     * Creates a BrowserModel with the backing ThumbnailSourceModel.  An error
     * will be thrown to the user if no such map (which contains the Thumbnail
     * and data source information) is supplied.
     *
     * @param dataModel The data model to back the BrowserModel.
     */
    public BrowserModel(ThumbnailSourceModel dataModel)
    {
        init();

        if (dataModel == null)
        {
            sendInternalError("null dataModel passed to BrowserModel constructor");
            return;
        }
        else
        {
            this.sourceModel = dataModel;
        }
    }
    
    // TODO: include constructor which loads settings (so that the grouping
    // model doesn't always revert to the default)

    /**
     * Gets the backing source/image data model.
     * @return The thumbnail/data source backing model.
     */
    public ThumbnailSourceModel getDataModel()
    {
        return sourceModel;
    }

    /**
     * Sets the backing model to the specified source mapping.  Will throw an
     * error if the dataModel is null (but not if it's empty)
     * 
     * @param dataModel The desired backing model.
     */
    public void setDataModel(ThumbnailSourceModel dataModel)
    {
        if (dataModel == null)
        {
            sendInternalError("null dataModel passed to BrowserModel.setDataModel");
            return;
        }
        else
        {
            this.sourceModel = dataModel;
            updateModelListeners();
        }
    }
    
    /**
     * Adds a thumbnail to the browser.  This thumbnail *should* also be in
     * the source browser.  Will do nothing if thumb is null.
     * 
     * @param thumb The thumbnail to add.
     */
    public void addThumbnail(Thumbnail thumb)
    {
        if(thumb != null)
        {
            thumbnailSet.add(thumb);
            GroupModel group = groupingMethod.getGroup(thumb);
            group.addThumbnail(thumb);
            updateModelListeners();
        }
    }
    
    /**
     * Removes a visible thumbnail from the browser model.
     * @param thumb The thumbnail to remove.
     */
    public void removeThumbnail(Thumbnail thumb)
    {
        if(thumb != null)
        {
            thumbnailSet.remove(thumb);
            GroupModel group = groupingMethod.getGroup(thumb);
            group.removeThumbnail(thumb);
            updateModelListeners();
        }
    }
    
    /**
     * Clears all thumbnails from the browser model.
     */
    public void clearThumbnails()
    {
        thumbnailSet.clear();
        for(Iterator iter = groupModels.iterator(); iter.hasNext();)
        {
            GroupModel group = (GroupModel)iter.next();
            group.clearThumbnails();
        }
        updateModelListeners();
    }

    /**
     * Select the image with the specified ID.
     * 
     * @param imageID The ID of the image to select.
     */
    public void selectImage(int imageID)
    {
        
    }

    /**
     * Deselect the image with the specified ID.
     * @param imageID The ID of the image to deselect.
     */
    public void deselectImage(int imageID)
    {
        // TODO: fill in method
    }

    /**
     * Hide the image with the specified ID.
     */
    public void hideImage(int imageID)
    {
        // TODO: fill in method
    }

    /**
     * Unhide the image with the specified ID.
     */
    public void showImage(int imageID)
    {
        // TODO: fill in method
    }

    /**
     * Return an unmodifiable set of selected images.
     * @return The set of selected images.
     */
    public Set getSelectedImages()
    {
        return Collections.unmodifiableSet(selectedImages);
    }

    /**
     * Return an unmodifiable set of hidden images in the model.
     * @return The set of hidden images.
     */
    public Set getHiddenImages()
    {
        return Collections.unmodifiableSet(hiddenImages);
    }
    
    /**
     * Explicitly notify all listeners that the model has been updated.
     */
    public void fireModelUpdated()
    {
        updateModelListeners();
    }
    
    // notifies all classes listening to the status of this model that
    // there has been a change.
    private void updateModelListeners()
    {
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener listener =
                (BrowserModelListener)iter.next();
            listener.modelUpdated();
        }
    }

    // send a message through the BrowserEnvironment/MessageHandler framework.
    private void sendError(String message)
    {
        MessageHandler handler = env.getMessageHandler();
        handler.reportError(message);
    }

    // send an internal error message through the BrowserEnvironment framework
    private void sendInternalError(String message)
    {
        MessageHandler handler = env.getMessageHandler();
        handler.reportInternalError(message);
    }
}
