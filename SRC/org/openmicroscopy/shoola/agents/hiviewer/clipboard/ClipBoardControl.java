/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardControl
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;


//Java imports
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.util.LoadingWin;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.AnnotationData;

/** 
 * The {@link ClipBoard}'s controller.
 * 
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClipBoardControl
    implements ChangeListener, PropertyChangeListener
{

    /** The {@link ClipBoardUI} View. */
    private ClipBoardUI         view;
    
    /** The {@link ClipBoardModel} Model. */
    private ClipBoardModel      model;
    
    /** The {@link ClipBoard} Component. */
    private ClipBoard           component;
    
    /**
     * Handles the selection of a new node in the {@link Browser} component.
     * 
     * @param pce The event.
     */
    private void handleBrowserSelectedDisplay(PropertyChangeEvent pce)
    {
        if (!(pce.getNewValue().equals(pce.getOldValue())) 
                && pce.getNewValue() != null) {
            ImageDisplay oldNode, newNode;
            Colors colors = Colors.getInstance();
            newNode = (ImageDisplay) pce.getNewValue();
            newNode.setHighlight(colors.getSelectedHighLight(newNode));
            if (pce.getOldValue() != null) {
                oldNode = (ImageDisplay) pce.getOldValue();
                oldNode.setHighlight(
                        colors.getDeselectedHighLight(oldNode));
            }
        }
        ImageDisplay node = 
            model.getParentModel().getBrowser().getSelectedDisplay();
        view.onDisplayChange(node);
    }
    
    /**
     * Brings on screen the selected node. The nodes containing the child
     * are visited i.e. parent then grandparent all the way up to the root node.
     * 
     * @param childBounds 	The bounds of the selected node.
     * @param parent 		The node containing the child.
     * @param isRoot		<code>true</code> if its the root node, 
     * 						<code>false</code> otherwise.	
     */
    private void scrollToNode(Rectangle childBounds, ImageDisplay parent,
                                boolean isRoot)
    {
        JScrollPane dskDecorator = parent.getDeskDecorator();
        Rectangle viewRect = dskDecorator.getViewport().getViewRect();
        if (!viewRect.contains(childBounds)) {
            JScrollBar vBar = dskDecorator.getVerticalScrollBar();
            JScrollBar hBar = dskDecorator.getHorizontalScrollBar();
            vBar.setValue(childBounds.y);
            hBar.setValue(childBounds.x);
        }
        if (!isRoot) {
            ImageDisplay node = parent.getParentDisplay();
            scrollToNode(childBounds, node, (node.getParentDisplay() == null));       
        }      
    }
    
    
    /**
     * Creates a new instance.
     * 
     * @param component A reference to the model. Mustn't be <code>null</code>.
     */
    ClipBoardControl(ClipBoard component)
    {
        if (component == null) throw new NullPointerException("No component.");
        this.component = component;
    }

    /**
     * Links the MVC triad.
     * 
     * @param view The {@link ClipBoardUI} view. Mustn't be <code>null</code>.
     * @param model The {@link ClipBoardModel} model.
     *              Mustn't be <code>null</code>.
     */
    void initialize(ClipBoardUI view, ClipBoardModel model)
    {
        if (view == null) throw new NullPointerException("No view.");
        if (model == null) throw new NullPointerException("No model.");
        this.view = view;
        this.model = model;
        component.addChangeListener(this);
        model.getParentModel().addChangeListener(this);
    }

    /**
     * Retrieves the annotations for the specified data object.
     * 
     * @param objectID          The ID of the data object.
     * @param annotationIndex   The annotation index.
     */
    void retrieveAnnotations(long objectID, int annotationIndex)
    {
        component.retrieveAnnotations(objectID, annotationIndex);
    }
    
    /**
     * Sets the selected tabbed pane. If the Annotation tabbed pane is
     * deselected any ongoing data loading is cancelled.
     * 
     * @param index The index of the selected pane.
     */
    void setPaneIndex(int index){ component.setPaneIndex(index, null); }
    
    /**
     * Updates the specified annotation.
     * 
     * @param data The annotation data object.
     */
    void updateAnnotation(AnnotationData data)
    {
        component.updateAnnotation(data);
    }
    
    /**
     * Creates a new annotation for the currently selected data object.
     * 
     * @param txt The text of the annotation.
     */
    void createAnnotation(String txt) { component.createAnnotation(txt); }
    
    /**
     * Deletes the specified annotation.
     * 
     * @param data The annotation data objetc.
     */
    void deleteAnnotation(AnnotationData data)
    {
        component.deleteAnnotation(data);
    }
    
    /**
     * Discards any on-going annotation retrieval.
     */
    void discardAnnotation() { component.discardAnnotation(); }
    
    /**
     * Reacts to a specific property change fired by the browser.
     * and the component hosting the result of a <code>Search</code> action.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        if (name.equals(Browser.SELECTED_DISPLAY_PROPERTY))
            handleBrowserSelectedDisplay(pce); 
        else if (name.equals(ClipBoard.LOCALIZE_IMAGE_DISPLAY)) {
            ImageDisplay node = (ImageDisplay) pce.getNewValue();
            ImageDisplay parent = node.getParentDisplay();
            scrollToNode(node.getBounds(), parent,
                        (parent.getParentDisplay() == null));
        }
    }

    /**
     * Listens to change events. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent ce)
    {
        Object source = ce.getSource();
        if (source instanceof HiViewer) {
            HiViewer parentModel = model.getParentModel();
            if (parentModel.getState() == HiViewer.READY) {
                parentModel.getBrowser().addPropertyChangeListener(
                        Browser.SELECTED_DISPLAY_PROPERTY, this);
                view.initListener();
            }  
        } else if (source instanceof ClipBoard) {
            switch (model.getState()) {
                case ClipBoard.EDIT_ANNOTATIONS:
                case ClipBoard.LOADING_ANNOTATIONS:
                    model.getLoadingWin().setOnScreen();
                    break;
                case ClipBoard.ANNOTATIONS_READY:
                case ClipBoard.DISCARDED_ANNOTATIONS:
                    model.getLoadingWin().setClosed(true);
                    break;
            }
        }
    }

}
