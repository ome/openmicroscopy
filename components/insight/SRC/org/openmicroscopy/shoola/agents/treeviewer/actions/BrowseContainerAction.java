/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowseContainerAction
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

package org.openmicroscopy.shoola.agents.treeviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FileData;
import pojos.ImageData;
import pojos.MultiImageData;
import pojos.PlateData;
import pojos.PlateAcquisitionData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Browses the selected node depending on the hierarchy object type.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class BrowseContainerAction
    extends TreeViewerAction
{

	/** Name of the action when the <code>DataObject</code> isn't an Image. */
    private static final String NAME_NO_THUMBNAILS = "Browse w/o thumbnails";
    
    /** Name of the action when the <code>DataObject</code> isn't an Image. */
    private static final String NAME = "Browse";
    
    /** Default description of the action. */
    private static final String DESCRIPTION_DEFAULT = "Browse.";
    
    /** 
     * Description of the action if the <code>DataObject</code> is 
     * a Project. 
     */
    private static final String DESCRIPTION_PROJECT = "Browse the selected " +
    		"Project.";
    
    /** 
     * Description of the action if the <code>DataObject</code> is 
     * a Tag. 
     */
    private static final String DESCRIPTION_TAG = "Browse the selected " +
    		"Tag.";
    
    /** 
     * Description of the action if the node is <code>TreeImageTimeSet</code>. 
     */
    private static final String DESCRIPTION_TIME = "Browse the selected " +
    		"period.";
    
    /** 
     * Description of the action if the <code>DataObject</code> is a Plate. 
     */
    private static final String DESCRIPTION_PLATE = "Browse the selected " +
    		"Plate.";
    
    /** 
     * Description of the action if the <code>DataObject</code> is a Dataset. 
     */
    private static final String DESCRIPTION_DATASET = "Browse the selected " +
    		"Dataset.";
    /** 
     * Description of the action if the <code>Object</code> is a folder. 
     */
    private static final String DESCRIPTION_FOLDER = "Browse the selected " +
    		"Folder.";
    /** 
     * Description of the action if the <code>Object</code> is a folder. 
     */
    private static final String DESCRIPTION_MULTIIMAGE = 
    	"Browse the selected Image.";
    
    /** Convenience reference to the icon manager. */
    private static IconManager	icons = IconManager.getInstance();
    
    /** Flag indicating to browse the object and retrieve the thumbnails. */
    private final boolean withThumbnails;
    
    /**
     * Sets the action enabled depending on the browser's type and 
     * the currently selected node. Sets the name of the action depending on 
     * the <code>DataObject</code> hosted by the currently selected node.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null || model.getSelectedBrowser() == null) {
            setEnabled(false);
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER)); 
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DEFAULT));
            return;
        }
        if (selectedDisplay.getParentDisplay() == null) { //root
            name = NAME;
            setEnabled(false);
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER)); 
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DEFAULT));
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        Browser browser = model.getSelectedBrowser();
        if (selectedDisplay instanceof TreeImageTimeSet) {
        	
        	putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER));
        	putValue(Action.SHORT_DESCRIPTION, 
        			UIUtilities.formatToolTipText(DESCRIPTION_TIME));
        	if (withThumbnails) name = NAME;
        	else {
        		name = NAME_NO_THUMBNAILS;
        		setEnabled(false);
        		return;
        	}
        	TreeImageDisplay[] array = browser.getSelectedDisplays();
        	if (array != null && array.length > 1) {
        		setEnabled(false);
        	} else {
        		TreeImageTimeSet timeNode = (TreeImageTimeSet) selectedDisplay;
        		long number = timeNode.getNumberItems();
        		setEnabled(number > 0);
        	}
            
            return;
        }
        if (ho == null ||
        	ho instanceof ExperimenterData || ho instanceof ImageData ||
        	ho instanceof FileAnnotationData){// || ho instanceof DatasetData) {
        	putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DEFAULT));
        	setEnabled(false);
        } else if (ho instanceof File) {
        	File f = (File) ho;
        	if (f.isDirectory() && !f.isHidden()) {
        		putValue(Action.SHORT_DESCRIPTION, 
                        UIUtilities.formatToolTipText(DESCRIPTION_FOLDER));
            	setEnabled(true);
        	}
        } else if (ho instanceof PlateAcquisitionData) {
        	setEnabled(true);
        	putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_PLATE));
        } else if (!(ho instanceof DataObject)) {
        	putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DEFAULT));
        	setEnabled(false);
        } else {
            if (browser != null) {
                if (browser.getSelectedDisplays().length > 1) {
                    setEnabled(true);
                    //for this version
                    //setEnabled(false);
                    if (withThumbnails) name = NAME;
                	else name = NAME_NO_THUMBNAILS;
                    putValue(Action.SMALL_ICON, 
                    			icons.getIcon(IconManager.BROWSER)); 
                    putValue(Action.SHORT_DESCRIPTION, 
                            UIUtilities.formatToolTipText(DESCRIPTION_DEFAULT));
                    return;
                }
            }
            if (withThumbnails) name = NAME;
        	else name = NAME_NO_THUMBNAILS;
        	putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER));

        	String description = DESCRIPTION_DEFAULT;
        	
            if (selectedDisplay instanceof TreeImageSet) {
                TreeImageSet tim = (TreeImageSet) selectedDisplay;
            	long n = tim.getNumberItems();
            	if (ho instanceof ScreenData) setEnabled(false);
                else if (ho instanceof PlateData) {
                	List l = selectedDisplay.getChildrenDisplay();
                	description = DESCRIPTION_PLATE;
                	setEnabled((l == null || l.size() <= 1));
                } else if (ho instanceof PlateAcquisitionData) {
                	description = DESCRIPTION_PLATE;
                	setEnabled(true);
                } else if (ho instanceof ProjectData) {
                	description = DESCRIPTION_PROJECT;
                	if (n > 0) {
                	    List children = tim.getChildrenDisplay();
                	    Iterator i = children.iterator();
                	    boolean enabled = false;
                	    while (i.hasNext()) {
                            TreeImageDisplay node = (TreeImageDisplay) i.next();
                            if (node.getNumberOfItems() > 0) {
                                enabled = true;
                                break;
                            }
                        }
                	    setEnabled(enabled);
                	} else {
                	    setEnabled(false);
                	}
                	if (!withThumbnails) setEnabled(false);
                } else if (ho instanceof DatasetData) {
                	description = DESCRIPTION_DATASET;
                	setEnabled(n > 0);
                	if (!withThumbnails) setEnabled(false);
                } else if (ho instanceof TagAnnotationData) {
            		String ns = ((TagAnnotationData) ho).getNameSpace();
            		if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
            			setEnabled(false);
            		else {
            			description = DESCRIPTION_TAG;
            			setEnabled(n > 0);
            		}
            		if (!withThumbnails) setEnabled(false);
                } else if (ho instanceof MultiImageData) {
                	MultiImageData mi = (MultiImageData) ho;
            		if (!mi.isHidden()) {
            			setEnabled(mi.getComponents().size() > 0);
            			description = DESCRIPTION_MULTIIMAGE;
            		} else setEnabled(false);
            	} else if (ho instanceof FileData) {
            		FileData f = (FileData) ho;
            		if (f.isDirectory() && !f.isHidden()) {
            			setEnabled(true);
            			description = DESCRIPTION_FOLDER;
            		} else setEnabled(false);
            	}
            }
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(description));
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public BrowseContainerAction(TreeViewer model)
    {
        this(model, true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
     */
    public BrowseContainerAction(TreeViewer model, boolean withThumbnails)
    {
        super(model);
        this.withThumbnails = withThumbnails;
        if (withThumbnails) name = NAME;
        else name = NAME_NO_THUMBNAILS;
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION_DEFAULT));
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER)); 
    }
    
    /**
     * Creates a  {@link ViewCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       ViewCmd cmd = new ViewCmd(model, withThumbnails);
       cmd.execute();
    }
    
}
