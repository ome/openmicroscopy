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

// TODO: put hotspot areas in LayoutMethod or BrowserModel? (Layout class?)
import java.util.*;

import org.openmicroscopy.shoola.agents.browser.datamodel.ThumbnailSourceMap;
import org.openmicroscopy.shoola.agents.browser.layout.GroupModel;
import org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod;
import org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod;
import org.openmicroscopy.shoola.agents.browser.layout.SingleGroupingMethod;

/**
 * The backing data model for the browser (not including overlays) and
 * currently browsed set in the top browser component's MVC architecture.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserModel
{
    private BrowserEnvironment env;

    private ThumbnailSourceMap sourceModel;

    private LayoutMethod layoutMethod;
    
    private List groupModels;
    private GroupingMethod groupingMethod;

    private Set selectedImages;
    private Set hiddenImages;

    private BrowserMode currentMode;

    // common initialization routine
    private void init()
    {
        env = BrowserEnvironment.getInstance();
        selectedImages = new HashSet();
        hiddenImages = new HashSet();
        groupModels = new ArrayList();

        // default behavior (may replace later)
        currentMode = BrowserMode.DEFAULT_MODE;
        // TODO: add layout method initialization
    }

    /**
     * Creates a BrowserModel with an empty backing ThumbnailSourceMap.  That is,
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
     * Creates a BrowserModel with the backing ThumbnailSourceMap.  An error
     * will be thrown to the user if no such map (which contains the Thumbnail
     * and data source information) is supplied.
     *
     * @param dataModel The data model to back the BrowserModel.
     */
    public BrowserModel(ThumbnailSourceMap dataModel)
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
    public ThumbnailSourceMap getDataModel()
    {
        return sourceModel;
    }

    /**
     * Sets the backing model to the specified source mapping.  Will throw an
     * error if the dataModel is null (but not if it's empty)
     * 
     * @param dataModel The desired backing model.
     */
    public void setDataModel(ThumbnailSourceMap dataModel)
    {
        if (dataModel == null)
        {
            sendInternalError("null dataModel passed to BrowserModel.setDataModel");
            return;
        }
        else
        {
            this.sourceModel = dataModel;
        }
    }

    /**
     * Select the image with the specified ID.
     * 
     * @param imageID The ID of the image to select.
     */
    public void selectImage(int imageID)
    {
        // TODO: fill in method
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
