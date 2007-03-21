/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;

//Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.CBDataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ChannelMetadataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ClassificationsLoader;
import org.openmicroscopy.shoola.agents.hiviewer.Declassifier;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.clsf.ClassificationPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor.EditorPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.info.InfoPane;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorFactory;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * The Model component in the <code>ClipBoard</code> MVC triad.
 * This class tracks the <code>ClipBoard</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. The {@link ClipBoardComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk 
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClipBoardModel
{
    
    /** Holds one of the state flags defined by {@link ClipBoard}. */
    private int                     		state;
    
    /** Reference to the {@link HiViewer}. */
    private HiViewer                		parentModel;
    
    /** The index of the selected pane. */
    private int                     		paneIndex;
    
    /** The classifications retrieved for an image. */
    private Set                     		classifications;
    
    /** The {@link ViewerSorter} used to sort the annotations. */
    private ViewerSorter            		sorter;
    
    /** 
     * Will either be a hierarchy loader, a thumbnail loader, or 
     * <code>null</code> depending on the current state. 
     */
    private CBDataLoader            		currentLoader;
    
    /** The map holding the {@link ClipBoardPane}s. */
    private HashMap<Integer, ClipBoardPane>	cbPanes;
    
    /** Flag indicating if the clipBoard is shown or hidden. */
    private boolean                 		display;
    
    /** Reference to the component that embeds this model. */
    protected ClipBoardComponent    		component;
    

    /** Initializes the default values. */
    private void init()
    {
        setPaneIndex(ClipBoard.FIND_PANE);
        cbPanes = new HashMap<Integer, ClipBoardPane>();
        sorter = new ViewerSorter();
        sorter.setAscending(false);
    }
    
    /** Initializes the components composing the clipBoard. */
    private void createClipBoardPanes()
    {
        cbPanes.put(new Integer(ClipBoard.FIND_PANE), new FindPane(component));
        int layout = AnnotatorEditor.HORIZONTAL_LAYOUT;
        if (!ClipBoard.HORIZONTAL_SPLIT)
        	layout = AnnotatorEditor.VERTICAL_LAYOUT;
        AnnotatorEditor annotator = AnnotatorFactory.getEditor(
        					HiViewerAgent.getRegistry(), null, layout);
        cbPanes.put(new Integer(ClipBoard.ANNOTATION_PANE), 
                    new AnnotationPane(component, annotator));
        cbPanes.put(new Integer(ClipBoard.INFO_PANE), 
                    new InfoPane(component));
        cbPanes.put(new Integer(ClipBoard.EDITOR_PANE), 
                new EditorPane(component));
        cbPanes.put(new Integer(ClipBoard.CLASSIFICATION_PANE), 
                new ClassificationPane(component));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parentModel A reference to the {@link HiViewer}, viewed as 
     * the parentModel. Mustn't be null.
     */
    ClipBoardModel(HiViewer parentModel)
    {
        if (parentModel == null)
            throw new NullPointerException("No parent model.");
        this.parentModel = parentModel;
        init();
    }
    
    /**
     * Called by the <code>ClipBoard</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(ClipBoardComponent component)
    {
        if (component == null) throw new NullPointerException("No component");
        this.component = component;
        createClipBoardPanes();
    }
    
    /**
     * Returns the {@link ClipBoardPane} corresponding to the specified index,
     * <code>null</code> if there is no component corresponding to the index.
     * 
     * @param index The <code>ClipBoardPane</code> index.
     * @return See above.
     */
    ClipBoardPane getClipboardPane(int index) 
    {
        return cbPanes.get(new Integer(index));
    }
    
    /**
     * Returns the {@link ClipBoardPane}s composing the clip board.
     *  
     * @return See above.
     */
    Map getClipBoardPanes() { return cbPanes; }
    
    /**
     * Returns the {@link HiViewer} model.
     * 
     * @return See below.
     */
    HiViewer getParentModel() { return parentModel; }
    
    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link ClipBoard} interface.  
     */
    int getState() { return state; }
    
    /** 
     * Sets the state.
     *
     * @param state The state to set.
     */
    void setState(int state) { this.state = state; }

    /**
     * Returns the index of the selected pane.
     * 
     * @return One of the flags defined by the {@link ClipBoard} interface.
     */
    int getPaneIndex() { return paneIndex; }
    
    /**
     * Sets the index of the selected tabbedPane.
     * 
     * @param i The index of the tabbedPane.
     */
    void setPaneIndex(int i)  { paneIndex = i; }
    
    /**
     * Returns the current user's details. Helper method
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails()
    {
        return parentModel.getUserDetails();
    }
    
    /**
     * Starts the asynchronous retrieval of the classifications 
     * and sets the state to {@link ClipBoard#LOADING_ANNOTATIONS}.
     * 
     * @param ho The <code>DataObject</code> to retrieve the classification for.
     */
    void fireClassificationsLoading(ImageData ho)
    {
        currentLoader = new ClassificationsLoader(component, ho.getId(), 
                							parentModel.getRootID());  
        currentLoader.load();
        state = ClipBoard.LOADING_CLASSIFICATIONS;
    }
    
    /**
     * Starts the asynchronous retrieval of the annotations 
     * and sets the state to {@link ClipBoard#LOADING_ANNOTATIONS}.
     * 
     * @param ho The <code>DataObject</code> to retrieve the annotation for.
     */
    void fireAnnotationsLoading(DataObject ho)
    {
    }
    
    /** 
     * Returns the id of the current user. 
     * 
     * @return See above.
     */
    long getUserID() { return parentModel.getUserDetails().getId(); }
    
    /** 
     * Returns the id of the user's group used for data retrieval. 
     * 
     * @return See above.
     */
    long getGroupID() { return parentModel.getRootID(); }

    /**
     * Starts the asynchronous retrieval of the channel metadata.
     * 
     * @param img The image to handle.
     */
    void fireChannelsMetadataLoading(ImageData img)
    {
        currentLoader = new ChannelMetadataLoader(component, img);
        currentLoader.load();
        state = ClipBoard.LOADING_CHANNELS_METADATA;
    }

    /**
     * Sets to <code>true</code> to show the component,
     * <code>false</code> to hide.
     * 
     * @param b The value to set.
     */
    void setDisplay(boolean b) { display = b; }
    
    /**
     * Returns <code>true</code> if the component is visible, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    boolean isDisplay() { return display; }

    /**
     * Sets the retrieved classifications.
     * 
     * @param classifications The value to set.
     */
    void setClassifications(Set classifications)
    {
        this.classifications = classifications;
        state = ClipBoard.CLASSIFICATIONS_READY;
    }

    /**
     * Starts the asynchronous declassification of the image.
     * 
     * @param image The image to declassify.
     * @param paths The categories containing the image.
     */
    void declassifyImage(ImageData image, Set paths)
    {
        currentLoader = new Declassifier(component, image, paths);
        currentLoader.load();
        state = ClipBoard.DECLASSIFICATION;
    }
    
}
