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

import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.is.CompositingSettings;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.datamodel.CategoryTree;
import org.openmicroscopy.shoola.agents.browser.images.OverlayMethod;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethodZOrder;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.layout.GroupModel;
import org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod;
import org.openmicroscopy.shoola.agents.browser.layout.ImageIDComparator;
import org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod;
import org.openmicroscopy.shoola.agents.browser.layout.SingleGroupingMethod;
import org.openmicroscopy.shoola.env.data.model.DatasetData;

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
    private DatasetData backingModel;

    private Set thumbnailSet;
    private Map imageIDMap;
    
    private Set progressListeners;
    private Set modelListeners;

    private LayoutMethod layoutMethod;
    private List groupModels;
    private GroupingMethod groupingMethod;
    
    private CompositingSettings renderSettings;
    private PaintMethodZOrder annotationModel;

    private Set selectedThumbnails;
    
    private AttributeMap attributeMap;
    private CategoryTree categoryTree;
    
    private List availableTypesList;

    private Map modeClassMap;

    private BrowserModeClass panActionClass;
    private BrowserModeClass majorUIModeClass;
    private BrowserModeClass selectModeClass;
    private BrowserModeClass zoomModeClass;
    private BrowserModeClass semanticModeClass;
    
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
    
    /**
     * Specifies the name of the browser mode class that governs the current
     * zoom state of the browser.
     */
    public static final String ZOOM_MODE_NAME = "zoomMode";
    
    /**
     * Specifies the name of the browser mode class that governs what
     * happens on mouseover (name vs. semantic zoom)
     */
    public static final String SEMANTIC_MODE_NAME = "semanticMode";
    

    // common initialization routine
    private void init()
    {
        env = BrowserEnvironment.getInstance();
        progressListeners = new HashSet();
        modelListeners = new HashSet();
        selectedThumbnails = new HashSet();
        attributeMap = new AttributeMap();
        groupingMethod = new SingleGroupingMethod();
        groupModels = Arrays.asList(groupingMethod.getGroups());
        thumbnailSet = new HashSet();
        imageIDMap = new HashMap();
        annotationModel = new PaintMethodZOrder();
        modeClassMap = new HashMap();
        availableTypesList = new ArrayList();

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
                              
        zoomModeClass =
            new BrowserModeClass(ZOOM_MODE_NAME,
                                 new BrowserMode[] { BrowserMode.ZOOM_TO_FIT_MODE,
                                                     BrowserMode.ZOOM_50_MODE,
                                                     BrowserMode.ZOOM_75_MODE,
                                                     BrowserMode.ZOOM_ACTUAL_MODE,
                                                     BrowserMode.ZOOM_200_MODE},
                                 BrowserMode.ZOOM_TO_FIT_MODE);
                                 
        semanticModeClass =
            new BrowserModeClass(SEMANTIC_MODE_NAME,
                                 new BrowserMode[] { BrowserMode.NOOP_MODE,
                                                     BrowserMode.IMAGE_NAME_MODE,
                                                     BrowserMode.SEMANTIC_ZOOMING_MODE},
                                 BrowserMode.SEMANTIC_ZOOMING_MODE);
        
        modeClassMap.put(PAN_MODE_NAME,panActionClass);
        modeClassMap.put(MAJOR_UI_MODE_NAME,majorUIModeClass);
        modeClassMap.put(SELECT_MODE_NAME,selectModeClass);
        modeClassMap.put(ZOOM_MODE_NAME,zoomModeClass);
        modeClassMap.put(SEMANTIC_MODE_NAME,semanticModeClass);
        
    }

    /**
     * Creates a BrowserModel with empty backing dataset information.  The
     * model does not yet contain any images-- the images are loaded lazily.
     */
    public BrowserModel()
    {
        init();
    }
    
    /**
     * Creates a BrowserModel with the specified backing dataset.  The model
     * does not yet contain any images-- the images are loaded lazily.
     * @param dataset The backing dataset.
     */
    public BrowserModel(DatasetData dataset)
    {
        setDataset(dataset);
        init();
    }
    
    /**
     * Returns the backing dataset.
     * @return See above.
     */
    public DatasetData getDataset()
    {
        return backingModel;
    }
    
    /**
     * Sets the OME dataset for the browser model.  Can be null if you
     * want to nullify the dataset model.
     * 
     * @param dataset The backing dataset.
     */
    public void setDataset(DatasetData dataset)
    {
        this.backingModel = dataset;
    }
    
    /**
     * Returns the mapping of STs (or ST names) to the respective attributes
     * that the backing dataset has.
     * @return See above.
     */
    public AttributeMap getAttributes()
    {
        return attributeMap;
    }
    
    /**
     * Sets the mapping of STs (or ST names) to the respective attributes
     * that the backing dataset has.
     * @param map The above mapping.
     */
    public void setAttributes(AttributeMap map)
    {
        this.attributeMap = map;
    }
    
    /**
     * Returns the current dataset-dependent hierarchy of category groups
     * and phenotypes.
     * @return See above.
     */
    public CategoryTree getCategoryTree()
    {
        return categoryTree;
    }
    
    /**
     * Sets the current dataset-dependent hierarchy of category groups and
     * phenotypes.
     * @param tree
     */
    public void setCategoryTree(CategoryTree tree)
    {
        this.categoryTree = tree;
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
        if(thumb == null || thumbnailSet.contains(thumb))
        {
            return;
        }
        
        thumbnailSet.add(thumb);
        if(thumb.isMultipleThumbnail())
        {
            ThumbnailDataModel[] tdms = thumb.getMultipleModels();
            for(int i=0;i<tdms.length;i++)
            {
                imageIDMap.put(new Integer(tdms[i].getID()),tdms[i]);
            }
        }
        else
        {
            imageIDMap.put(new Integer(thumb.getModel().getID()),
                           thumb.getModel());
        }
        
        GroupModel group = groupingMethod.getGroup(thumb);
        group.addThumbnail(thumb);
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener listener =
                (BrowserModelListener)iter.next();
            
            listener.thumbnailAdded(thumb);
        }
        updateModelListeners();
        
    }
    
   	/**
   	 * Batch add thumbnails to the browser.
   	 * @param thumbs An array of thumbnails to add.
   	 */
    public void addThumbnails(Thumbnail[] thumbs)
    {
    	if(thumbs == null || thumbs.length == 0)
    	{
    		return;
    	}
    	
        int added = 0;
    	for(int i=0;i<thumbs.length;i++)
    	{
            if(!thumbnailSet.contains(thumbs[i]))
            {
    		    thumbnailSet.add(thumbs[i]);
                if(thumbs[i].isMultipleThumbnail())
                {
                    ThumbnailDataModel[] tdms = thumbs[i].getMultipleModels();
                    for(int j=0;j<tdms.length;j++)
                    {
                        imageIDMap.put(new Integer(tdms[j].getID()),tdms[j]);
                    }
                }
                else
                {
                    imageIDMap.put(new Integer(thumbs[i].getModel().getID()),
                                   thumbs[i].getModel());
                }
    		    GroupModel group = groupingMethod.getGroup(thumbs[i]);
    		    group.addThumbnail(thumbs[i]);
                added++;
            }
    	}
        
        if(added == 0)
        {
            return;
        }
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener listener =
                (BrowserModelListener)iter.next();
            
            listener.thumbnailsAdded(thumbs);
        }
    	updateModelListeners();
    }
    
    /**
     * Removes a visible thumbnail from the browser model.
     * @param thumb The thumbnail to remove.
     */
    public void removeThumbnail(Thumbnail thumb)
    {
        if(thumb == null || !thumbnailSet.contains(thumb))
        {
            return;
        }
        thumbnailSet.remove(thumb);
        GroupModel group = groupingMethod.getGroup(thumb);
        group.removeThumbnail(thumb);
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener listener =
                (BrowserModelListener)iter.next();
            
            listener.thumbnailRemoved(thumb);
        }
        
        updateModelListeners();
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
     * Returns the thumbnail data model for the image with the given ID.
     * @param imageID The ID of the image to retrieve the model.
     * @return The model for the particular ID.
     */
    public ThumbnailDataModel getModel(int imageID)
    {
        return (ThumbnailDataModel)imageIDMap.get(new Integer(imageID));
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
     * Adds a specific relevant semantic type to this browser model, to
     * watch for in measurements (like heat maps)
     * @param st The type to add.
     */
    public void addRelevantType(SemanticType st)
    {
        if(st != null)
        {
            availableTypesList.add(st);
        }
    }
    
    /**
     * Removes a specific relevant semantic type from this browser model.
     * @param st The type to remove.
     */
    public void removeRelevantType(SemanticType st)
    {
        if(st != null)
        {
            availableTypesList.remove(st);
        }
    }
    
    /**
     * Gets a list of the relevant semantic types for attributes in this
     * browser model (i.e., which semantic types apply to images within the
     * dataset)
     * @return See above.
     */
    public List getRelevantTypes()
    {
        return Collections.unmodifiableList(availableTypesList);
    }
    
    /**
     * Sets the relevant semantic types to the specified list.
     * @param types Which semantic types apply to images within this dataset.
     */
    public void setRelevantTypes(SemanticType[] types)
    {
        if(types == null || types.length == 0)
        {
            return;
        }
        availableTypesList = Arrays.asList(types);
        
        Comparator typeComp = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                SemanticType st1 = (SemanticType)o1;
                SemanticType st2 = (SemanticType)o2;
                
                return st1.getName().compareTo(st2.getName());
            }
        };
        Collections.sort(availableTypesList,typeComp);
        for(int i=0;i<availableTypesList.size();i++)
        {
            SemanticType type = (SemanticType)availableTypesList.get(i);
        }
    }
    
    /**
     * Adds a paint method that will be applied to all thumbnails in the
     * model.  For example, adding the paint method which paints a thumbnail's
     * name will cause the name to be rendered on all thumbnails.
     * 
     * The variable position specifies where this paint method should be
     * included.  It must be one of Thumbnail.FOREGROUND_PAINT_METHOD,
     * Thumbnail.MIDDLE_PAINT_METHOD, or Thumbnail.BACKGROUND_PAINT_METHOD.
     */
    public void addPaintMethod(PaintMethod m, int position)
    {
        if(m == null)
        {
            return;
        }
        
        if(position == Thumbnail.FOREGROUND_PAINT_METHOD)
        {
            for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
            {
                Thumbnail t = (Thumbnail)iter.next();
                t.addForegroundPaintMethod(m);
            }
        }
        else if(position == Thumbnail.MIDDLE_PAINT_METHOD)
        {
            for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
            {
                Thumbnail t = (Thumbnail)iter.next();
                t.addMiddlePaintMethod(m);
            }
        }
        else if(position == Thumbnail.BACKGROUND_PAINT_METHOD)
        {
            for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
            {
                Thumbnail t = (Thumbnail)iter.next();
                t.addBackgroundPaintMethod(m);
            }
        }
        else
        {
            throw new IllegalArgumentException("Invalid paint method location");
        }
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener bml =
                (BrowserModelListener)iter.next();
            bml.paintMethodsChanged();
        }
    }
    
    /**
     * Removes a paint method from the ordering in the model.  For example,
     * removing the paint method which paints a thumbnail's name will prevent
     * the name from being rendered on all thumbnails in the model.
     * 
     * The position parameter specifies from which layer in the thumbnail the
     * paint method should be removed.
     * 
     * @param m The paint method to deactivate.
     */
    public void removePaintMethod(PaintMethod m, int position)
    {
        if(m == null)
        {
            return;
        }
       
        if(position == Thumbnail.FOREGROUND_PAINT_METHOD)
        {
            for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
            {
                Thumbnail t = (Thumbnail)iter.next();
                t.removeForegroundPaintMethod(m);
            }
        }
        else if(position == Thumbnail.MIDDLE_PAINT_METHOD)
        {
            for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
            {
                Thumbnail t = (Thumbnail)iter.next();
                t.removeMiddlePaintMethod(m);
            }
        }
        else if(position == Thumbnail.BACKGROUND_PAINT_METHOD)
        {
            for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
            {
                Thumbnail t = (Thumbnail)iter.next();
                t.addBackgroundPaintMethod(m);
            }
        }
        else
        {
            throw new IllegalArgumentException("Invalid paint method location");
        }
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener bml =
                (BrowserModelListener)iter.next();
            bml.paintMethodsChanged();
        }
    }
    
    /**
     * Adds an overlay method (to dynamically add contextual nodes that respond
     * to some sort of UI)
     * @param method The overlay method to add to all thumbnails.
     */
    public void addOverlayMethod(OverlayMethod method)
    {
        if(method == null) return;
        for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.addOverlayMethod(method);
        }
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener bml =
                (BrowserModelListener)iter.next();
            bml.paintMethodsChanged();
        }
    }
    
    /**
     * Removes an overlay method.
     * @param method
     */
    public void removeOverlayMethod(OverlayMethod method)
    {
        if(method == null) return;
        for(Iterator iter = thumbnailSet.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.removeOverlayMethod(method);
        }
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener bml =
                (BrowserModelListener)iter.next();
            bml.paintMethodsChanged();
        }
    }

    /**
     * Select the specified thumbnail.
     * 
     * @param t The thumbnail to select.
     */
    public void selectThumbnail(Thumbnail t)
    {
        if(t == null || selectedThumbnails.contains(t))
        {
            return;
        }
        else
        {
            selectedThumbnails.add(t);
            Thumbnail[] array = new Thumbnail[] {t};
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserModelListener bml =
                    (BrowserModelListener)iter.next();
                bml.thumbnailsSelected(array);
            }
        }
    }
    
    /**
     * Select the specified thumbnails.
     * @param ts The thumbnails to select.
     */
    public void selectThumbnails(Thumbnail[] ts)
    {
        if(ts == null || ts.length == 0)
        {
            return;
        }
        else
        {
            int added = 0;
            for(int i=0;i<ts.length;i++)
            {
                if(!selectedThumbnails.contains(ts[i]))
                {
                    selectedThumbnails.add(ts[i]);
                    added++;
                }
            }
            
            if(added == 0)
            {
                return;
            }
            
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserModelListener bml =
                    (BrowserModelListener)iter.next();
                
                bml.thumbnailsSelected(ts);
            }
        }
    }

    /**
     * Deselect the specified thumbnail.
     * 
     * @param t The thumbnail to deselect.
     */
    public void deselectThumbnail(Thumbnail t)
    {
        if(t == null || !selectedThumbnails.contains(t))
        {
            return;
        }
        else
        {
            selectedThumbnails.remove(t);
            Thumbnail[] ts = new Thumbnail[] {t};
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserModelListener bml =
                    (BrowserModelListener)iter.next();
                bml.thumbnailsDeselected(ts);
            }
        }
    }
    
    /**
     * Deselect all images in the model.
     */
    public void deselectAllThumbnails()
    {
        Thumbnail[] selected = new Thumbnail[selectedThumbnails.size()];
        if(selected.length == 0)
        {
            return;
        }
        
        selectedThumbnails.toArray(selected);
        selectedThumbnails.clear();
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            BrowserModelListener bml =
                (BrowserModelListener)iter.next();
            bml.thumbnailsDeselected(selected);
        }
    }

    /**
     * Returns whether or not this thumbnail is currently selected.
     * @param t The thumbnail to check
     * @return Whether or not it is selected.
     */
    public boolean isThumbnailSelected(Thumbnail t)
    {
        return selectedThumbnails.contains(t);
    }

    /**
     * Return an unmodifiable set of selected images.
     * @return The set of selected images.
     */
    public Set getSelectedImages()
    {
        return Collections.unmodifiableSet(selectedThumbnails);
    }
    
    /**
     * Returns the mapping between image IDs and data models.
     * @return
     */
    public Map getImageDataMap()
    {
        return Collections.unmodifiableMap(imageIDMap);
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
