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
import org.openmicroscopy.shoola.agents.browser.layout.ImageIDComparator;
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

    private Map modeClassMap;

    private BrowserModeClass panActionClass;
    private BrowserModeClass majorUIModeClass;
    private BrowserModeClass selectModeClass;
    
    /**
     * Specifies the name of the browser mode class that governs the current
     * panning action of the browser view.
     */
    public static final String PAN_MODE_NAME = "panAction";
    
    /**
     * Specifies the name of the browser mode class that governs the current
     * major UI mode of the browser.
     */
    public static final String MAJOR_UI_MODE_NAME = "uiMode";
    
    /**
     * Specifies the name of the browser mode class that governs the current
     * selection state of the browser.
     */
    public static final String SELECT_MODE_NAME = "selectMode";
    

    // common initialization routine
    private void init()
    {
        env = BrowserEnvironment.getInstance();
        progressListeners = new HashSet();
        modelListeners = new HashSet();
        selectedImages = new HashSet();
        hiddenImages = new HashSet();
        groupingMethod = new SingleGroupingMethod();
        groupModels = Arrays.asList(groupingMethod.getGroups());
        thumbnailSet = new HashSet();
        annotationModel = new PaintMethodZOrder();
        modeClassMap = new HashMap();

        panActionClass =
            new BrowserModeClass(PAN_MODE_NAME,
                                 new BrowserMode[] { BrowserMode.DEFAULT_MODE,
                                                     BrowserMode.HAND_MODE},
                                 BrowserMode.DEFAULT_MODE);
                                 
        majorUIModeClass =
            new BrowserModeClass(MAJOR_UI_MODE_NAME,
                                 new BrowserMode[] { BrowserMode.DEFAULT_MODE,
                                                     BrowserMode.ANNOTATE_MODE,
                                                     BrowserMode.CLASSIFY_MODE,
                                                     BrowserMode.GRAPH_MODE},
                                 BrowserMode.DEFAULT_MODE);
        
        selectModeClass =
            new BrowserModeClass(SELECT_MODE_NAME,
                                 new BrowserMode[] { BrowserMode.UNSELECTED_MODE,
                                                     BrowserMode.SELECTING_MODE,
                                                     BrowserMode.SELECTED_MODE},
                                 BrowserMode.UNSELECTED_MODE);
        
        modeClassMap.put(PAN_MODE_NAME,panActionClass);
        modeClassMap.put(MAJOR_UI_MODE_NAME,majorUIModeClass);
        modeClassMap.put(SELECT_MODE_NAME,selectModeClass);
        
    }

    /**
     * Creates a BrowserModel with an empty backing ThumbnailSourceModel.  That is,
     * there are not yet any loaded thumbnails, although the model is accessible.
     *
     */
    public BrowserModel()
    {
        init();
    }
    
    // TODO: include constructor which loads settings (so that the grouping
    // model doesn't always revert to the default)
    
    /**
     * Adds a thumbnail to the browser.  Will do nothing if thumb is null.
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
   	 * Batch add thumbnails to the browser.
   	 * @param thumbs An array of thumbnails to add
   	 */
    public void addThumbnails(Thumbnail[] thumbs)
    {
    	System.err.println("Add thumbnails");
    	if(thumbs == null || thumbs.length == 0)
    	{
    		return;
    	}
    	
    	for(int i=0;i<thumbs.length;i++)
    	{
    		thumbnailSet.add(thumbs[i]);
    		GroupModel group = groupingMethod.getGroup(thumbs[i]);
    		group.addThumbnail(thumbs[i]);
    	}
    	updateModelListeners();
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
     * Returns a list of thumbnails in the model, ordered by ID.
     * @return See above.
     */
    public List getThumbnails()
    {
        List thumbnailList = new ArrayList(thumbnailSet);
        Collections.sort(thumbnailList,new ImageIDComparator());
        return Collections.unmodifiableList(thumbnailList);
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
     * Returns the layout method with which the view should place the
     * thumbnail objects onscreen.
     * @return The current layout method.
     */
    public LayoutMethod getLayoutMethod()
    {
        return layoutMethod;
    }
    
    /**
     * Sets the thumbnail layout method to the specified method.  Will
     * do nothing if the method is null.
     * @param lm The layout method for an attached view to use.
     */
    public void setLayoutMethod(LayoutMethod lm)
    {
        if(lm != null)
        {
            this.layoutMethod = lm;
            updateModelListeners();
        }
    }
    
    /**
     * Returns the method by which individual thumbnails are divided into
     * certain criteria or phenotypes (and look so onscreen)
     * @return The grouping/dividing method used to distinguish thumbnails.
     */
    public GroupingMethod getGroupingMethod()
    {
        return groupingMethod;
    }
    
    /**
     * Sets the grouping method to the specified method.
     * @param gm The grouping method to use.
     */
    public void setGroupingMethod(GroupingMethod gm)
    {
        // TODO: reestablish groups, likely
        if(gm != null)
        {
            this.groupingMethod = gm;
            updateModelListeners();
        }
    }
    
    /**
     * Returns the current mode for the class specified by the given name.
     * If no such class exists, this method will return null.
     * @param modeClassName See above.
     */
    public BrowserMode getCurrentMode(String modeClassName)
    {
        BrowserModeClass modeClass =
            (BrowserModeClass)modeClassMap.get(modeClassName);
        
        if(modeClass == null)
        {
            return null;
        }
        else return modeClass.getSelected();
    }
    
    /**
     * Sets the current mode for this class to the specified value.  If
     * the class doesn't exist, this will do nothing.
     */
    public void setCurrentMode(String modeClassName, BrowserMode mode)
    {
        BrowserModeClass modeClass =
            (BrowserModeClass)modeClassMap.get(modeClassName);
        
        if(modeClass == null)
        {
            return;
        }
        
        // this evaluates to true if an actual change occurred.
        if(modeClass.setSelected(mode))
        {
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserModelListener bml = (BrowserModelListener)iter.next();
                bml.modeChanged(modeClassName,mode);
            }
        }
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
    
    /**
     * Adds a model listener to this model.  Wooo.
     * @param listener
     */
    public void addModelListener(BrowserModelListener listener)
    {
    	if(listener != null)
    	{
    		modelListeners.add(listener);
    	}
    }
    
    public void removeModelListener(BrowserModelListener listener)
    {
    	if(listener != null)
    	{
    		modelListeners.remove(listener);
    	}
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
