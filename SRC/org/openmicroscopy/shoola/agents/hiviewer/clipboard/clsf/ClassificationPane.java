/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.clsf.ClassificationPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.clsf;




//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardPane;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import pojos.CategoryData;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * The UI component displaying the categories containing the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ClassificationPane
    extends ClipBoardPane
{

    /** The component hosting the display. */
    private ClassificationPaneUI    uiDelegate;
    
    /** The cuurently selected image. */
    private ImageData              image;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ClassificationPane(ClipBoard model)
    {
        super(model);
        uiDelegate = new ClassificationPaneUI(this);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        add(uiDelegate, c);
    }
    
    /**
     * Browses the specified <code>DataObject</code>.
     * 
     * @param object The object to browse.
     */
    void browse(DataObject object)
    {
        if (object == null) return;
        model.browse(object);
    }
    
    /** 
     * Declassifies the image from the selected categories. 
     * 
     * @param nodes The collection of selected nodes.
     */
    void declassify(Set nodes)
    {
        if (nodes == null || nodes.size() == 0) {
            UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Tagging", "You first need to " +
                    "remove the tags.");
            return;
        }
        if (image == null) return;
        Set paths = new HashSet(nodes.size());
        Iterator i = nodes.iterator();
        Object object;
        while (i.hasNext()) {
            object = ((TreeCheckNode) i.next()).getUserObject();
            if (object instanceof CategoryData) paths.add(object);
        } 
        model.declassifyImage(image, paths);
    }
    
    /**
     * Displays the specified CG/C nodes.
     * 
     * @param nodes The nodes to display.
     */
    public void showClassifications(Set nodes)
    {
        if (nodes == null) return;
        uiDelegate.showClassifications(nodes);
    }
    
    /**
     * Overridden to return the name of this UI component.
     * @see ClipBoardPane#getPaneName()
     */
    public String getPaneName() { return "Tagging"; }

    /**
     * Overridden to return the icon related to this UI component.
     * @see ClipBoardPane#getPaneIcon()
     */
    public Icon getPaneIcon()
    {
        return IconManager.getInstance().getIcon(IconManager.CLASSIFY);
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
    public String getPaneDescription() { return "Display the tags " +
            "linked to the image."; }
    
    /**
     * Overridden to return the icon related to this UI component.
     * @see ClipBoardPane#onDisplayChange(ImageDisplay)
     */
    public void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (model.getSelectedPaneIndex() != ClipBoard.CLASSIFICATION_PANE) 
            return;
        image = null;
        if (selectedDisplay == null) {
            uiDelegate.onSelectedDisplay(null);
            return;
        }
        Object ho = selectedDisplay.getHierarchyObject();
        if (ho == null) {
            uiDelegate.onSelectedDisplay(null);
            return; //root
        }
        String title = null;
        if (ho instanceof ImageData) {
            ImageData img = (ImageData) ho;
            Long n = null;//img.getClassificationCount();
            if (n == null || n.longValue() == 0) title = null;
            else {
                image = img;
                title = img.getName();
                model.retrieveClassifications(img);
            }
        } 
        uiDelegate.onSelectedDisplay(title);   
    }

    /**
     * Overridden to return the name of this UI component.
     * @see ClipBoardPane#hasDataToSave()
     */
    public boolean hasDataToSave() {  return false; }
}
