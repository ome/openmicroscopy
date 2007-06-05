/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.AnnotationPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Component hosting the annotator.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AnnotationPane 
	extends ClipBoardPane
{

	/** Reference to the annotator. */
	private AnnotatorEditor annotator;
	
	/**
     * Creates a new instance.
     * 
     * @param model 	Reference to the Model. Mustn't be <code>null</code>.
     * @param annotator Reference to the editor. Mustn't be <code>null</code>.
     */
    AnnotationPane(ClipBoard model, AnnotatorEditor annotator)
    {
        super(model);
        if (annotator == null)
        	throw new IllegalArgumentException("No annotator");
        this.annotator = annotator;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(annotator.getUI());
    }
    
    /**
     * Registers the passed listener.
     * 
     * @param listener The listener to register.
     */
    void registerListener(PropertyChangeListener listener)
    {
    	annotator.addPropertyChangeListener(listener);
    }
    
    /**
     * Retrives the annotation for the passed DataObject.
     * 
     * @param ho The object to handle.
     */
    void retrieveAnnotation(DataObject ho)
    {
    	annotator.retrieveAnnotations(ho);
    }
    
    /**
     * Overridden to return the name of this UI component.
     * @see ClipBoardPane#getPaneName()
     */
    public String getPaneName() { return "Annotation"; }

    /**
     * Overridden to return the icon related to this UI component.
     * @see ClipBoardPane#getPaneIcon()
     */
    public Icon getPaneIcon()
    {
        return IconManager.getInstance().getIcon(IconManager.ANNOTATE);
    }

    /**
     * Overridden to return the index of this UI component.
     * @see ClipBoardPane#getPaneIndex()
     */
    public int getPaneIndex() { return ClipBoard.ANNOTATION_PANE; }
    
    /**
     * Overridden to return the description of this UI component.
     * @see ClipBoardPane#getPaneDescription()
     */
    public String getPaneDescription() { return "Annotate the selected item."; }
    
    /**
     * Overridden to return the icon related to this UI component.
     * @see ClipBoardPane#onDisplayChange(ImageDisplay)
     */
    public void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (model.getSelectedPaneIndex() != ClipBoard.ANNOTATION_PANE) return;
        if (selectedDisplay == null) {
            annotator.retrieveAnnotations(null);
            return;
        }
        Object ho = selectedDisplay.getHierarchyObject();
        if (ho == null) {
        	annotator.retrieveAnnotations(null);
            return; //root
        }
        if (ho instanceof ImageData || ho instanceof DatasetData) {
            annotator.retrieveAnnotations((DataObject) ho);
        } else annotator.retrieveAnnotations(null);  
    }
    
}
