/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorUtil 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;

/** 
 * Collection of helper methods.
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
class AnnotatorUtil
{

	/** Background color of the hightlighted node. */
	static final Color		HIGHLIGHT = new Color(204, 255, 204);
	
	/** Background color of the even rows. */
	static final Color		BACKGROUND = Color.WHITE;
	
	/** Background color of the add rows. */
	static final Color		BACKGROUND_ONE = new Color(236, 243, 254);

    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    static final Dimension	SMALL_V_SPACER_SIZE = new Dimension(1, 6);
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * horizontally.
     */
    static final Dimension	SMALL_H_SPACER_SIZE = new Dimension(6, 1);
    
    /** The preferred size of the annotation area. */
    static final Dimension	AREA_SIZE = new Dimension(200, 150);
    
    /** Default text. */
    static final String		COMMENT = " annotation";
    
    /** Text describing the new annotation. */
    static final String		NEW_ANNOTATION = "New annotation";
    
    /** 
     * Text message when the annotation mode is 
     * {@link Annotator#BULK_ANNOTATE_MODE}.
     */
    static final String		BULK_TEXT = "Enter the textual annotation.";
    
	/** 
	 * The size of the invisible components used to separate buttons
	 * horizontally.
	 */
	static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
  
    /** 
     * Sorts the passed collection of annotations by date starting with the
     * most recent.
     * 
     * @param annotations   Collection of {@link AnnotationData} linked to 
     *                      the currently edited <code>Dataset</code> or
     *                      <code>Image</code>.
     */
    static void sortAnnotationByDate(List annotations)
    {
        if (annotations == null || annotations.size() == 0) return;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Timestamp t1 = ((AnnotationData) o1).getLastModified(),
                          t2 = ((AnnotationData) o2).getLastModified();
                long n1 = t1.getTime();
                long n2 = t2.getTime();
                int v = 0;
                if (n1 < n2) v = -1;
                else if (n1 > n2) v = 1;
                return -v;
            }
        };
        Collections.sort(annotations, c);
    }
    
    /**
     * Sets the default values of the specified component.
     * 
     * @param area 	The area to format.
     * @param title The title to set.
     */
    static void setAnnotationAreaDefault(MultilineLabel area, String title)
    {
    	CompoundBorder border = BorderFactory.createCompoundBorder(
    			new TitledLineBorder(title), 
    			BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    	area.setBorder(border);
    	area.setOriginalBackground(BACKGROUND);
    	area.setOpaque(true);
    	area.setEditable(true);
    	area.setPreferredSize(AREA_SIZE);
    }
    
    /**
     * Initializes and returns a tree used to display the data objects to 
     * annotate or the users who annotates the objects.
     * 
     * @return See above.
     */
    static JTree initTree()
    {
    	JTree treeDisplay = new JTree();
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        treeDisplay.setModel(new DefaultTreeModel(root));
    	treeDisplay.setRootVisible(false);
        treeDisplay.setVisible(true);
        treeDisplay.setShowsRootHandles(true);
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeDisplay.setCellRenderer(new EditorTreeCellRenderer());
        return treeDisplay;
        
    }
    
    /**
     * Returns the partial name of the image's name
     * 
     * @param originalName The original name.
     * @return See above.
     */
    static String getPartialName(String originalName)
    {
        if (Pattern.compile("/").matcher(originalName).find()) {
            String[] l = originalName.split("/", 0);
            int n = l.length;
            if (n == 1) return l[0];
            return UIUtilities.DOTS+l[n-2]+"/"+l[n-1]; 
        } else if (Pattern.compile("\\\\").matcher(originalName).find()) {
            String[] l = originalName.split("\\\\", 0);
            int n = l.length;
            if (n == 1) return l[0];
            return UIUtilities.DOTS+l[n-2]+"\\"+l[n-1];
        } 
        return originalName;
    }
    
}
