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
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.util.browser.SmartFolder;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.dnd.DnDTree;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.MultiImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.PlateAcquisitionData;
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
 
	/** Reference to the <code>Image</code> icon. */
	private static final Icon IMAGE_ICON;
	
	/** Reference to the <code>Image</code> icon. */
	private static final Icon IMAGE_ANNOTATED_ICON;
	
	/** Reference to the <code>Image</code> icon. */
	private static final Icon IMAGE_UNREGISTERED_ICON;
	
	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_ICON;
	
	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_ANNOTATED_ICON;
	
	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_TO_REFRESH_ICON;
	
	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_ANNOTATED_TO_REFRESH_ICON;
	
	/** Reference to the <code>Project</code> icon. */
	private static final Icon PROJECT_ICON;
	
	/** Reference to the <code>Project</code> icon. */
	private static final Icon PROJECT_ANNOTATED_ICON;
	
	/** Reference to the <code>Project</code> icon. */
	private static final Icon PROJECT_TO_REFRESH_ICON;
	
	/** Reference to the <code>Project</code> icon. */
	private static final Icon PROJECT_ANNOTATED_TO_REFRESH_ICON;
	
	/** Reference to the <code>Screen</code> icon. */
	private static final Icon SCREEN_ICON;
	
	/** Reference to the <code>Screen</code> icon. */
	private static final Icon SCREEN_ANNOTATED_ICON;
	
	/** Reference to the <code>Screen</code> icon. */
	private static final Icon SCREEN_TO_REFRESH_ICON;
	
	/** Reference to the <code>Screen</code> icon. */
	private static final Icon SCREEN_ANNOTATED_TO_REFRESH_ICON;
	
	/** Reference to the <code>Plate</code> icon. */
	private static final Icon PLATE_ICON;
	
	/** Reference to the <code>Plate</code> icon. */
	private static final Icon PLATE_ANNOTATED_ICON;
	
	/** Reference to the <code>Plate Acquisition</code> icon. */
	private static final Icon PLATE_ACQUISITION_ICON;
	
	/** Reference to the Annotated<code>Plate Acquisition</code> icon. */
	private static final Icon PLATE_ACQUISITION_ANNOTATED_ICON;
	
	/** Reference to the <code>File</code> icon. */
	private static final Icon FILE_ICON;
	
	/** Reference to the <code>Tag</code> icon. */
	private static final Icon TAG_ICON;
	
	/** Reference to the <code>Tag Set</code> icon. */
	private static final Icon TAG_SET_ICON;
	
	/** Reference to the <code>Tag not owned but used</code> icon. */
	private static final Icon TAG_OTHER_OWNER_ICON;
	
	/** Reference to the <code>Tag</code> icon. */
	private static final Icon PERSONAL_ICON;
	
	/** Reference to the <code>Image</code> icon. */
	private static final Icon IMAGE_DIRECTORY_ICON;
	
	/** Reference to the <code>Image</code> icon. */
	private static final Icon IMAGE_DIRECTORY_UNREGISTERED_ICON;
	
	/** Reference to the <code>Directory</code> icon. */
	private static final Icon DIRECTORY_ICON;
	
	/** Reference to the <code>Directory</code> icon. */
	private static final Icon DIRECTORY_REGISTERED_ICON;
	
	/** Reference to the <code>Owner</code> icon. */
	private static final Icon OWNER_ICON;
	
	/** Reference to the <code>Owner</code> icon. */
	private static final Icon OWNER_NOT_ACTIVE_ICON;
	
	/** Reference to the <code>Root</code> icon. */
	private static final Icon ROOT_ICON;
	
	/** Reference to the <code>Owner</code> to refresh icon. */
	private static final Icon OWNER_TO_REFRESH_ICON;
	
	/** Reference to the <code>Text File</code> icon. */
	private static final Icon FILE_TEXT_ICON;
	
	/** Reference to the <code>PDF File</code> icon. */
	private static final Icon FILE_PDF_ICON;
	
	/** Reference to the <code>HTML File</code> icon. */
	private static final Icon FILE_HTML_ICON;
	
	/** Reference to the <code>Power Point File</code> icon. */
	private static final Icon FILE_PPT_ICON;
	
	/** Reference to the <code>Word File</code> icon. */
	private static final Icon FILE_WORD_ICON;
	
	/** Reference to the <code>Excel File</code> icon. */
	private static final Icon FILE_EXCEL_ICON;
	
	/** Reference to the <code>XML File</code> icon. */
	private static final Icon FILE_XML_ICON;
	
	/** Reference to the <code>Editor File</code> icon. */
	private static final Icon FILE_EDITOR_ICON;
	
	/** Reference to the <code>Protocol File</code> icon. */
	private static final Icon FILE_PROTOCOL_ICON;
	
	/** Reference to the <code>Registered File</code> icon. */
	private static final Icon FILE_REGISTERED_ICON;
	
	/** Reference to the <code>Protocol</code> icon. */
	private static final Icon EDITOR_PROTOCOL_ICON;
	
	/** Reference to the <code>Experiment</code> icon. */
	private static final Icon EDITOR_EXPERIMENT_ICON;
	
	/** Reference to the <code>Movie</code> icon. */
	private static final Icon MOVIE_ICON;
	
	/** Reference to the <code>Movie folder</code> icon. */
	private static final Icon MOVIE_FOLDER_ICON;
	
	/** Reference to the <code>Date</code> icon. */
	private static final Icon DATE_ICON;
	
	/** Reference to the <code>Date</code> icon. */
	private static final Icon OWNER_GROUP_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		OWNER_GROUP_ICON = icons.getIcon(IconManager.OWNER_GROUP);
		IMAGE_ICON = icons.getIcon(IconManager.IMAGE);
		IMAGE_ANNOTATED_ICON = icons.getIcon(IconManager.IMAGE_ANNOTATED);
		IMAGE_UNREGISTERED_ICON = icons.getIcon(IconManager.IMAGE_UNREGISTERED);
		DATASET_ICON = icons.getIcon(IconManager.DATASET);
		PROJECT_ICON = icons.getIcon(IconManager.PROJECT);
		SCREEN_ICON = icons.getIcon(IconManager.SCREEN);
		PLATE_ICON = icons.getIcon(IconManager.PLATE);
		FILE_ICON = icons.getIcon(IconManager.FILE);
		PROJECT_ANNOTATED_ICON = icons.getIcon(IconManager.PROJECT_ANNOTATED);
		PROJECT_TO_REFRESH_ICON = icons.getIcon(IconManager.PROJECT_TO_REFRESH);
		DATASET_ANNOTATED_ICON = icons.getIcon(IconManager.DATASET_ANNOTATED);
		DATASET_TO_REFRESH_ICON = icons.getIcon(IconManager.DATASET_TO_REFRESH);
		DATASET_ANNOTATED_TO_REFRESH_ICON = 
			icons.getIcon(IconManager.DATASET_ANNOTATED_TO_REFRESH);
		PROJECT_ANNOTATED_TO_REFRESH_ICON = 
			icons.getIcon(IconManager.PROJECT_ANNOTATED_TO_REFRESH);
		TAG_ICON = icons.getIcon(IconManager.TAG);
		TAG_SET_ICON = icons.getIcon(IconManager.TAG_SET);
		TAG_OTHER_OWNER_ICON = icons.getIcon(IconManager.TAG_OTHER_OWNER);
		SCREEN_ANNOTATED_ICON = icons.getIcon(IconManager.SCREEN_ANNOTATED);
		SCREEN_TO_REFRESH_ICON = icons.getIcon(IconManager.SCREEN_TO_REFRESH);
		SCREEN_ANNOTATED_TO_REFRESH_ICON = 
			icons.getIcon(IconManager.SCREEN_ANNOTATED_TO_REFRESH);
		PLATE_ANNOTATED_ICON = icons.getIcon(IconManager.PLATE_ANNOTATED);
		PLATE_ACQUISITION_ICON = icons.getIcon(IconManager.PLATE_ACQUISITION);
		PERSONAL_ICON = icons.getIcon(IconManager.PERSONAL);
		IMAGE_DIRECTORY_ICON = icons.getIcon(IconManager.IMAGE_DIRECTORY);
		IMAGE_DIRECTORY_UNREGISTERED_ICON = 
			icons.getIcon(IconManager.IMAGE_DIRECTORY_UNREGISTERED);
		DIRECTORY_ICON = icons.getIcon(IconManager.DIRECTORY);
		DIRECTORY_REGISTERED_ICON =
			icons.getIcon(IconManager.DIRECTORY_REGISTERED);
		OWNER_ICON = icons.getIcon(IconManager.OWNER);
		OWNER_NOT_ACTIVE_ICON = icons.getIcon(IconManager.OWNER_NOT_ACTIVE);
		ROOT_ICON = icons.getIcon(IconManager.ROOT);
		OWNER_TO_REFRESH_ICON = icons.getIcon(IconManager.REFRESH);
		//icons.getIcon(IconManager.OWNER_TO_REFRESH);
		FILE_TEXT_ICON = icons.getIcon(IconManager.FILE_TEXT);
		FILE_PDF_ICON = icons.getIcon(IconManager.FILE_PDF);
		FILE_HTML_ICON = icons.getIcon(IconManager.FILE_HTML);
		FILE_PPT_ICON = icons.getIcon(IconManager.FILE_PPT);
		FILE_WORD_ICON = icons.getIcon(IconManager.FILE_WORD);
		FILE_EXCEL_ICON = icons.getIcon(IconManager.FILE_EXCEL);
		FILE_XML_ICON = icons.getIcon(IconManager.FILE_XML);
		FILE_PROTOCOL_ICON = icons.getIcon(IconManager.FILE_PROTOCOL_EXPERIMENT);
		FILE_EDITOR_ICON = icons.getIcon(IconManager.FILE_EDITOR);
		FILE_REGISTERED_ICON = icons.getIcon(IconManager.FILE_REGISTERED);
		MOVIE_ICON = icons.getIcon(IconManager.MOVIE);
		EDITOR_PROTOCOL_ICON = icons.getIcon(IconManager.EDITOR_PROTOCOL);
		EDITOR_EXPERIMENT_ICON = icons.getIcon(IconManager.EDITOR_EXPERIMENT);
		MOVIE_FOLDER_ICON = icons.getIcon(IconManager.MOVIE_FOLDER);
		DATE_ICON = icons.getIcon(IconManager.DATE);
		PLATE_ACQUISITION_ANNOTATED_ICON = icons.getIcon(
				IconManager.PLATE_ACQUISITION_ANNOTATED);
	}
	
	/** The dimension of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
    /** Flag to indicate if the number of children is visible. */
    private boolean             numberChildrenVisible;
    
    /** The ID of the current user. */
    private long				userID;

    /** Filter to identify protocol file. */
    private EditorFileFilter 	filter;
    
    /** Flag indicating if the node to render is the target node.*/
    private boolean isTargetNode;
    
    /** Flag indicating if the node to render is the target node.*/
    private boolean droppedAllowed;
    
    /** The color used when dragging.*/
    private Color draggedColor;

    /** Indicates if the node is selected or not.*/
    private boolean selected;
    
    /** The location of the text.*/
    private int xText;
    
    /**
     * Sets the icon and the text corresponding to the user's object.
     * 
     * @param node The node to handle.
     */
    private void setIcon(TreeImageDisplay node)
    {
    	Object usrObject = node.getUserObject();
        Icon icon = FILE_TEXT_ICON;
        if (usrObject instanceof ProjectData) {
        	if (node.isToRefresh()) {
        		if (EditorUtil.isAnnotated(usrObject))
            		icon = PROJECT_ANNOTATED_TO_REFRESH_ICON;
            	else icon = PROJECT_TO_REFRESH_ICON;
        	} else {
        		if (EditorUtil.isAnnotated(usrObject))
            		icon = PROJECT_ANNOTATED_ICON;
            	else icon = PROJECT_ICON;
        	}
        } else if (usrObject instanceof DatasetData) {
        	if (node.isToRefresh()) {
        		if (EditorUtil.isAnnotated(usrObject))
            		icon = DATASET_ANNOTATED_TO_REFRESH_ICON;
                else icon = DATASET_TO_REFRESH_ICON;
        	} else {
        		if (EditorUtil.isAnnotated(usrObject))
            		icon = DATASET_ANNOTATED_ICON;
                else icon = DATASET_ICON;
        	}
        } else if (usrObject instanceof ImageData) {
            if (EditorUtil.isAnnotated(usrObject))
        		icon = IMAGE_ANNOTATED_ICON;
            else {
            	ImageData o = (ImageData) usrObject;
            	if (o.getId() < 0) icon = IMAGE_UNREGISTERED_ICON;
            	else icon = IMAGE_ICON;
            }
        } else if (usrObject instanceof TagAnnotationData) {
        	TagAnnotationData tag = (TagAnnotationData) usrObject;
        	String ns = tag.getNameSpace();
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
        		icon = TAG_SET_ICON;
        	else {
        		icon = TAG_ICON;
        		TreeImageDisplay n = BrowserFactory.getDataOwner(node);
        		if (n != null) {
        			ExperimenterData exp = (ExperimenterData) n.getUserObject();
        			if (!EditorUtil.isUserOwner(tag, exp.getId()))
        				icon = TAG_OTHER_OWNER_ICON;
        		}
        	}
        } else if (usrObject instanceof ScreenData) {
        	if (node.isToRefresh()) {
        		if (EditorUtil.isAnnotated(usrObject))
            		icon = SCREEN_ANNOTATED_TO_REFRESH_ICON;
            	else icon = SCREEN_TO_REFRESH_ICON;;
        	} else {
        		if (EditorUtil.isAnnotated(usrObject))
            		icon = SCREEN_ANNOTATED_ICON;
            	else icon = SCREEN_ICON;
        	}
        } else if (usrObject instanceof PlateData) {
        	if (EditorUtil.isAnnotated(usrObject))
        		icon = PLATE_ANNOTATED_ICON;
        	else icon = PLATE_ICON; 
        } else if (usrObject instanceof PlateAcquisitionData) {
        	if (EditorUtil.isAnnotated(usrObject))
        		icon = PLATE_ACQUISITION_ANNOTATED_ICON;
        	else icon = PLATE_ACQUISITION_ICON; 
        } else if (usrObject instanceof GroupData) {
        	icon = OWNER_GROUP_ICON;
        } else if (usrObject instanceof FileAnnotationData) {
        	FileAnnotationData data = (FileAnnotationData) usrObject;
        	String format = data.getFileFormat();
        	if (FileAnnotationData.PDF.equals(format)) 
        		icon = FILE_PDF_ICON;
        	else if (FileAnnotationData.TEXT.equals(format) ||
        			FileAnnotationData.CSV.equals(format)) 
        		icon = FILE_TEXT_ICON;
        	else if (FileAnnotationData.HTML.equals(format) ||
        			FileAnnotationData.HTM.equals(format)) 
        		icon = FILE_HTML_ICON;
        	else if (FileAnnotationData.MS_POWER_POINT.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_SHOW.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_X.equals(format)) 
        		icon = FILE_PPT_ICON;
        	else if (FileAnnotationData.MS_WORD.equals(format) ||
        			FileAnnotationData.MS_WORD_X.equals(format)) 
        		icon = FILE_WORD_ICON;
        	else if (FileAnnotationData.MS_EXCEL.equals(format)) 
        		icon =FILE_EXCEL_ICON;
        	else if (FileAnnotationData.XML.equals(format) ||
        			FileAnnotationData.RTF.equals(format)) {
        		if (filter.accept(data.getFileName())) {
        			if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(
        					data.getNameSpace())) {
        				icon = FILE_PROTOCOL_ICON;
        			} else icon = FILE_EDITOR_ICON;
        		} else icon = FILE_XML_ICON;
        	} else if (data.isMovieFile()) {
        		icon = MOVIE_ICON;
        	} else icon = FILE_TEXT_ICON; 
        } else if (usrObject instanceof MultiImageData) {
        	MultiImageData mi = (MultiImageData) usrObject;
        	if (mi.getId() > 0) 
        		icon = IMAGE_DIRECTORY_ICON;
        	else icon = IMAGE_DIRECTORY_UNREGISTERED_ICON;
        } else if (usrObject instanceof FileData) {
        	FileData f = (FileData) usrObject;
        	if (f.isDirectory()) {
        		if (f.getId() > 0) icon = DIRECTORY_REGISTERED_ICON;
        		else icon = DIRECTORY_ICON;
        	} else {
        		if (f.getId() > 0)
        			icon = FILE_REGISTERED_ICON;
        		else icon = FILE_TEXT_ICON;
        	}
        } else if (node instanceof SmartFolder) {
        	if (GroupData.class.equals(((SmartFolder) node).getType())) {
        		icon = PERSONAL_ICON;
        	}
        } else if (node instanceof TreeImageTimeSet)
        	icon = DATE_ICON;
        else if (node instanceof TreeFileSet) {
        	TreeFileSet n = (TreeFileSet) node;
        	switch (n.getType()) {
				case TreeFileSet.EXPERIMENT:
					icon = EDITOR_PROTOCOL_ICON;
					break;
				case TreeFileSet.PROTOCOL:
					icon = EDITOR_EXPERIMENT_ICON;
					break;
				case TreeFileSet.MOVIE:
					icon = MOVIE_FOLDER_ICON;
					break;
				default:
					icon = ROOT_ICON;
			}
        	
        } else if (usrObject instanceof String)
        	icon = ROOT_ICON;
        else if (usrObject instanceof ExperimenterData) {
        	ExperimenterData exp = (ExperimenterData) usrObject;
        	if (node.isToRefresh()) icon = OWNER_TO_REFRESH_ICON;
        	else {
        		if (exp.isActive()) icon = OWNER_ICON;
            	else icon = OWNER_NOT_ACTIVE_ICON;
        	}
        } 
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
        selected = false;
        filter = new EditorFileFilter();
        draggedColor = new Color(backgroundSelectionColor.getRed(),
				backgroundSelectionColor.getGreen(),
				backgroundSelectionColor.getBlue(), 100);
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
        isTargetNode = false;
        droppedAllowed = true;
        selected = sel;
        if (tree instanceof DnDTree) {
        	DnDTree dndTree = (DnDTree) tree;
        	isTargetNode = (value == dndTree.getDropTargetNode());
        	if (dndTree.getRowDropLocation() == row) {
        		droppedAllowed = false;
        	}
        }
        setIcon(FILE_TEXT_ICON);
        if (!(value instanceof TreeImageDisplay)) return this;
        TreeImageDisplay  node = (TreeImageDisplay) value;
        
        int w = 0;
        FontMetrics fm = getFontMetrics(getFont());
        Object ho = node.getUserObject();
        if (node.getLevel() == 0) {// && !(ho instanceof FileData)) {
        	if (ho instanceof ExperimenterData) setIcon(OWNER_ICON);
        	else setIcon(ROOT_ICON);
            if (getIcon() != null) w += getIcon().getIconWidth();
            w += getIconTextGap();
            w += fm.stringWidth(getText());
            setPreferredSize(new Dimension(w, fm.getHeight()));
            Color c = node.getHighLight();
            if (c == null) c = tree.getForeground();
            setForeground(c);
            if (!sel) setBorderSelectionColor(getBackground());
            else setTextColor(getBackgroundSelectionColor());
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
        xText = w;
        if (ho instanceof ImageData)
        	w += fm.stringWidth(node.getNodeName());
        else if (node instanceof TreeFileSet)
        	w +=  fm.stringWidth(getText())+40;
        else w += fm.stringWidth(getText());
        
        setPreferredSize(new Dimension(w, fm.getHeight()+4));//4 b/c GTK L&F
        setEnabled(node.isSelectable());
        return this;
    }
    
    /**
     * Overridden to highlight the destination of the target.
     * @see paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
    	//super.paintComponent(g);
    	if (isTargetNode) {
			if (!droppedAllowed) {
				if (selected) g.setColor(backgroundSelectionColor);
				else g.setColor(backgroundNonSelectionColor);
				
			} else g.setColor(draggedColor);
			g.fillRect(xText, 0, getSize().width, getSize().height);
		}
    	selected = false;
    	isTargetNode = false;
    	droppedAllowed = false;
    	super.paintComponent(g);
	}
  
}
