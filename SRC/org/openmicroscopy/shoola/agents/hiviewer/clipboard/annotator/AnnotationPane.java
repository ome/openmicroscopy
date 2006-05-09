/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.annotator.AnnotationPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.annotator;


//Java imports
import java.util.Map;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardPane;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * The UI component displaying the textual annotation linked to a
 * <code>DataObject</code>. The user can create/update/delete the annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class AnnotationPane
    extends ClipBoardPane
{
    
    /** Flag to indicate if the object is annotated. */
    private boolean             annotated;
    
    /** The component hosting the display. */
    private AnnotationPaneUI    uiDelegate;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public AnnotationPane(ClipBoard model)
    {
        super(model);
        uiDelegate = new AnnotationPaneUI(this);
        add(uiDelegate);
    }

    /**
     * Sets to <code>true</code> if the object is annotated, <code>false</code>
     * otherwise;
     * 
     * @param annotated Passed <code>true</code> if the object is annotated,
     *                  <code>false</code> otherwise.
     */
    void setAnnotated(boolean  annotated) { this.annotated = annotated; }
    
    /**
     * Returns the sorted annotations.
     * 
     * @return See above.
     */
    Map getAnnotations() { return model.getAnnotations(); }
    
    /**
     * Returns the user's details.
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails() { return model.getUserDetails(); }
    
    /** Saves the annotation. */
    void save()
    {
        //TODO Add controls if annotated
        AnnotationData data = model.getUserAnnotationData();
        String text = uiDelegate.getAnnotationText().trim();
        if (data == null) { //creation
            Object ho = model.getHierarchyObject();
            if (ho instanceof ImageData)
                data = new AnnotationData(AnnotationData.IMAGE_ANNOTATION);
            else if (ho instanceof DatasetData)
                data = new AnnotationData(AnnotationData.DATASET_ANNOTATION);
            data.setText(text);
            model.editAnnotation(data, ClipBoard.CREATE_ANNOTATION);
        } else { //update or delete
            if (uiDelegate.isAnnotationDeleted()) {
                model.editAnnotation(data, ClipBoard.DELETE_ANNOTATION);
            } else {
                data.setText(text);
                model.editAnnotation(data, ClipBoard.UPDATE_ANNOTATION);
            }
        }
    }
    
    /**
     * Displays the annotation linked to the currently selected item.
     *
     */
    public void showAnnotations() { uiDelegate.showAnnotations(); }
    
    /**
     * Overriden to return the name of this UI component.
     * @see ClipBoardPane#getPaneName()
     */
    public String getPaneName() { return "Annotation"; }

    /**
     * Overriden to return the icon related to this UI component.
     * @see ClipBoardPane#getPaneIcon()
     */
    public Icon getPaneIcon()
    {
        return IconManager.getInstance().getIcon(IconManager.ANNOTATE);
    }

    /**
     * Overriden to return the index of this UI component.
     * @see ClipBoardPane#getPaneIndex()
     */
    public int getPaneIndex() { return ClipBoard.ANNOTATION_PANE; }
    
    /**
     * Overriden to return the index of this UI component.
     * @see ClipBoardPane#getPaneDescription()
     */
    public String getPaneDescription() { return "Annotate the selected item."; }
    
    /**
     * Overriden to return the icon related to this UI component.
     * @see ClipBoardPane#onDisplayChange(ImageDisplay)
     */
    public void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (model.getSelectedPaneIndex() != ClipBoard.ANNOTATION_PANE) return;
        if (selectedDisplay == null) {
            uiDelegate.onSelectedDisplay(false, null);
            return;
        }
        Object ho = selectedDisplay.getHierarchyObject();
        if (ho == null) {
            uiDelegate.onSelectedDisplay(false, null);
            return; //root
        }
        String title = null;
        if (ho instanceof ImageData) {
            title = ((ImageData) ho).getName();
            model.retrieveAnnotations((DataObject) ho);
        } else if (ho instanceof DatasetData) {
            title = ((DatasetData) ho).getName();
            model.retrieveAnnotations((DataObject) ho);
        }
        uiDelegate.onSelectedDisplay(false, title);   
    }
    
}
