/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer
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

package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenAcquisitionData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Determines and sets the icon corresponding to a data object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeCellRenderer
    extends DefaultTreeCellRenderer
{
 
	/** The dimension of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
    /** Reference to the {@link IconManager}. */
    private IconManager         icons;
    
    /** Flag to indicate if the number of children is visible. */
    private boolean             numberChildrenVisible;
    
    /** The ID of the current user. */
    //private long				userID;

    /** Filter to identify protocol file. */
    private EditorFileFilter 	filter;
    
    /**
     * Sets the icon and the text corresponding to the user's object.
     * 
     * @param node The node to handle.
     */
    private void setIcon(TreeImageDisplay node)
    {
    	Object usrObject = node.getUserObject();
        Icon icon = icons.getIcon(IconManager.FILE_TEXT);
        if (usrObject instanceof ProjectData) {
        	if (EditorUtil.isAnnotated(usrObject))
        		icon = icons.getIcon(IconManager.PROJECT_ANNOTATED);
        	else icon = icons.getIcon(IconManager.PROJECT);
        } else if (usrObject instanceof DatasetData) {
            if (EditorUtil.isAnnotated(usrObject))
        		icon = icons.getIcon(IconManager.DATASET_ANNOTATED);
            else icon = icons.getIcon(IconManager.DATASET);
        } else if (usrObject instanceof ImageData) {
        	/*
            if (EditorUtil.isAnnotated(usrObject))
        		icon = icons.getIcon(IconManager.IMAGE_ANNOTATED);
            else icon = icons.getIcon(IconManager.IMAGE);
            */
        	icon = icons.getIcon(IconManager.IMAGE);
        } else if (usrObject instanceof TagAnnotationData) {
        	TagAnnotationData tag = (TagAnnotationData) usrObject;
        	String ns = tag.getNameSpace();
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
        		icon = icons.getIcon(IconManager.TAG_SET);
        	else icon = icons.getIcon(IconManager.TAG);
        } else if (usrObject instanceof ScreenData) {
        	if (EditorUtil.isAnnotated(usrObject))
        		icon = icons.getIcon(IconManager.SCREEN_ANNOTATED);
        	else icon = icons.getIcon(IconManager.SCREEN);
        } else if (usrObject instanceof PlateData) {
        	if (EditorUtil.isAnnotated(usrObject))
        		icon = icons.getIcon(IconManager.PLATE_ANNOTATED);
        	else icon = icons.getIcon(IconManager.PLATE);
        } else if (usrObject instanceof ScreenAcquisitionData) {
        	icon = icons.getIcon(IconManager.PLATE_ACQUISITION);
        } else if (usrObject instanceof FileAnnotationData) {
        	FileAnnotationData data = (FileAnnotationData) usrObject;
        	String format = data.getFileFormat();
        	if (FileAnnotationData.PDF.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_PDF);
        	else if (FileAnnotationData.TEXT.equals(format) ||
        			FileAnnotationData.CSV.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_TEXT);
        	else if (FileAnnotationData.HTML.equals(format) ||
        			FileAnnotationData.HTM.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_HTML);
        	else if (FileAnnotationData.MS_POWER_POINT.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_SHOW.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_X.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_PPT);
        	else if (FileAnnotationData.MS_WORD.equals(format) ||
        			FileAnnotationData.MS_WORD_X.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_WORD);
        	else if (FileAnnotationData.MS_EXCEL.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_EXCEL);
        	else if (FileAnnotationData.XML.equals(format) ||
        			FileAnnotationData.RTF.equals(format)) {
        		if (filter.accept(data.getFileName())) {
        			if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(
        					data.getNameSpace())) {
        				icon = icons.getIcon(
        						IconManager.FILE_PROTOCOL_EXPERIMENT);
        			} else icon = icons.getIcon(IconManager.FILE_EDITOR);
        		} else icon = icons.getIcon(IconManager.FILE_XML);
        	} else if (data.isMovieFile()) {
        		icon = icons.getIcon(IconManager.MOVIE);
        	} else icon = icons.getIcon(IconManager.FILE_TEXT); 
        } else if (usrObject instanceof File) {
        	File f = (File) usrObject;
        	if (f.isDirectory()) icon = icons.getIcon(IconManager.DIRECTORY);
        	else icon = icons.getIcon(IconManager.FILE_TEXT);
        } else if (node instanceof TreeImageTimeSet)
        	icon = icons.getIcon(IconManager.DATE);
        else if (node instanceof TreeFileSet) {
        	TreeFileSet n = (TreeFileSet) node;
        	switch (n.getType()) {
				case TreeFileSet.EXPERIMENT:
					icon = icons.getIcon(IconManager.EDITOR_PROTOCOL);
					break;
				case TreeFileSet.PROTOCOL:
					icon = icons.getIcon(IconManager.EDITOR_EXPERIMENT);
					break;
				case TreeFileSet.MOVIE:
					icon = icons.getIcon(IconManager.MOVIE_FOLDER);
					break;
				default:
					icon = icons.getIcon(IconManager.ROOT);
			}
        	
        } else if (usrObject instanceof String)
        	icon = icons.getIcon(IconManager.ROOT);
        else if (usrObject instanceof ExperimenterData)
        	icon = icons.getIcon(IconManager.OWNER);
        setIcon(icon);
    }

    /**
     * Sets the color of the selected cell depending on the darkness 
     * of the specified color.
     * 
     * @param c The color of reference.
     */
    private void setTextColor(Color c)
    {
    	if (c == null) return;
    	// check if the passed color is dark if yes, modify the text color.
    	if (UIUtilities.isDarkColor(c))
    		setForeground(UIUtilities.DEFAULT_TEXT);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param b Passed <code>true</code> to show the number of children,
     *          <code>false</code> otherwise.
     */ 
    public TreeCellRenderer(boolean b)
    {
        numberChildrenVisible = b;
        icons = IconManager.getInstance();
        filter = new EditorFileFilter();
    }
    
    /** Creates a new instance. */
    public TreeCellRenderer() { this(true); }
    
    /**
     * Overridden to set the icon and the text.
     * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
     * 								boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        setIcon(icons.getIcon(IconManager.FILE_TEXT));
        if (!(value instanceof TreeImageDisplay)) return this;
        TreeImageDisplay  node = (TreeImageDisplay) value;
        
        int w = 0;
        FontMetrics fm = getFontMetrics(getFont());
        Object ho = node.getUserObject();
        if (node.getLevel() == 0 && !(ho instanceof File)) {
        	if (ho instanceof ExperimenterData)
        		setIcon(icons.getIcon(IconManager.OWNER));
        	else setIcon(icons.getIcon(IconManager.ROOT));
            if (getIcon() != null) w += getIcon().getIconWidth();
            w += getIconTextGap();
            w += fm.stringWidth(getText());
            setPreferredSize(new Dimension(w, fm.getHeight()));
            if (sel) setTextColor(getBackgroundSelectionColor());
            return this;
        }
        setIcon(node);
    	
        if (numberChildrenVisible) setText(node.getNodeText());
        else setText(node.getNodeName());
        setToolTipText(node.getToolTip());
        Color c = node.getHighLight();
        if (c == null) c = tree.getForeground();
        setForeground(c);
        if (!sel) setBorderSelectionColor(getBackground());
        else setTextColor(getBackgroundSelectionColor());
        
        if (getIcon() != null) w += getIcon().getIconWidth();
        else w += SIZE.width;
        w += getIconTextGap();
        if (ho instanceof ImageData)
        	w += fm.stringWidth(node.getNodeName());
        else if (node instanceof TreeFileSet)
        	w +=  fm.stringWidth(getText())+40;
        else w += fm.stringWidth(getText());
        setPreferredSize(new Dimension(w, fm.getHeight()+4));//4 b/c GTK L&F
        setEnabled(node.isSelectable());
        return this;
    }
  
}
