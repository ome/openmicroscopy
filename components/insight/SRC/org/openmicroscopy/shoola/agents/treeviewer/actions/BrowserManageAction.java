/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/**
 * Brings up the <code>Manager</code> menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class BrowserManageAction 
	extends BrowserAction
	 implements MouseListener
{

	/** Indicates to show the menu for tags. */
	public static final int		NEW_TAGS = 0;
	
	/** Indicates to show the menu for container. */
	public static final int		NEW_CONTAINERS = 1;
	
	/** Indicates to show the menu for administrative tasks. */
	public static final int		NEW_ADMIN = 2;
	
	/** Identified the copy action. */
	public static final int 	COPY = 3;
	
	/** Identified the paste action. */
	public static final int 	PASTE = 4;
	
	/** Identified the cut action. */
	public static final int 	CUT = 5;
	
    /** The description of the action. */
    private static final String DESCRIPTION_CONTAINERS = 
    	"Create new Project, Dataset or Screen.";

    /** The description of the action. */
    private static final String DESCRIPTION_TAGS = "Create new Tag Set or Tag";

    /** The description of the action. */
    private static final String DESCRIPTION_ADMIN = "Create new Group or User.";

    /** The description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY_LINK = "Copy link(s) to the selected element(s) into the clipboard.";
    
    /** Alternative description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY = "Copy the selected element(s) into the clipboard.";
    
    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE_LINK = "Paste link(s) from the clipboard.";
    
    /** Alternative description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE = "Paste element(s) from the clipboard.";

    /** The description of the action if the index is {@link #CUT}. */
    private static final String DESCRIPTION_CUT_LINK = "Cut the selected link(s).";
    
    /** Alternative description of the action if the index is {@link #CUT}. */
    private static final String DESCRIPTION_CUT = "Cut the selected element(s).";

    /** The location of the mouse pressed. */
    private Point 	point;
    
    /** One of the constants defined by this class. */
    private int		index;
    
    /** The id of the user currently logged in.*/
    private long userID;
    
    /**
     * Controls if the passed index is valid or not.
     * 
     * @param index
     */
    private void checkIndex(int index)
    {
    	IconManager im = IconManager.getInstance();
    	switch (index) {
			case NEW_CONTAINERS:
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.CREATE));
				putValue(Action.SHORT_DESCRIPTION, 
	                UIUtilities.formatToolTipText(DESCRIPTION_CONTAINERS));
				break;
			case NEW_TAGS:
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.TAG));
				putValue(Action.SHORT_DESCRIPTION, 
		                UIUtilities.formatToolTipText(DESCRIPTION_TAGS));
				break;
			case NEW_ADMIN:
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.CREATE));
				putValue(Action.SHORT_DESCRIPTION, 
		                UIUtilities.formatToolTipText(DESCRIPTION_ADMIN));
				break;
			case COPY:
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_COPY));
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.COPY));
				break;
			case PASTE:
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.PASTE));
				break;
			case CUT:
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_CUT));
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.CUT));
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
    /**
     * Handles the experimenter.
     * 
     * @param display The node to handle.
     */
    private void handleExperimenter(TreeImageDisplay display)
    {
    	if (display == null) {
    		setEnabled(false);
    	} else {
    		Object ho = display.getUserObject();
        	long id = TreeViewerAgent.getUserDetails().getId();
        	if (ho instanceof ExperimenterData) {
        		ExperimenterData exp = (ExperimenterData) ho;
        		setEnabled(exp.getId() == id);
        	} 
    	}
    }
    
    /**
	 * Returns <code>true</code> if the pasting action is valid,
	 * <code>false</code> otherwise.
	 * 
	 * @param ho The selected data object.
	 * @param list The objects to copy.
	 * @return See above.
	 */
	private boolean isPasteValid(Object ho, List<DataObject>list)
	{
		Iterator<DataObject> i = list.iterator();
		DataObject os;
		int count = 0;
		while (i.hasNext()) {
			os = i.next();
			if (!EditorUtil.isTransferable(ho, os, userID)) return false;
			count++;
		}
		return count == list.size();
	}
	
	/**
	 * Handles the selection when the index is <code>CUT</code>,
	 * <code>COPY</code> or <code>PASTE</code>.
	 * 
	 * @param selectedDisplay The node to handle.
	 */
    private void handleSelection(TreeImageDisplay selectedDisplay)
    {
    	Object ho = selectedDisplay.getUserObject(); 
        TreeImageDisplay[] selected;
        int count = 0;
        TreeImageDisplay parentDisplay = selectedDisplay.getParentDisplay();
        Object parent = null;
        if (parentDisplay != null) parent = parentDisplay.getUserObject();
    	 if (parentDisplay != null) parent = parentDisplay.getUserObject();
         switch (index) {
 			case PASTE:
 				List<DataObject> list = model.getDataToCopy();
 				if (list == null || list.size() == 0) {
 					setEnabled(false);
 		            return;
 				}
 				if (ho instanceof ProjectData || ho instanceof ScreenData ||
 					ho instanceof DatasetData || ho instanceof GroupData ||
 					ho instanceof TagAnnotationData) {
 					selected = model.getSelectedDisplays();
 		    		for (int i = 0; i < selected.length; i++) {
 		    			ho = selected[i].getUserObject();
 		    			if (isPasteValid(ho, list)) {
 		    				if (ho instanceof GroupData) {
 		    					count++;
 			    			} else {
 			    				if (model.canLink(ho)) count++;
 			    			}
 		    			}
 					}
 		    		setEnabled(count == selected.length);
 				} else if (ho instanceof ExperimenterData ||
 						ho instanceof GroupData) {
 					if (model.getBrowserType() != Browser.ADMIN_EXPLORER) {
 						selected = model.getSelectedDisplays();
 			    		for (int i = 0; i < selected.length; i++) {
 			    			ho = selected[i].getUserObject();
 			    			if (isPasteValid(ho, list)) count++;
 						}
 			    		setEnabled(count == selected.length);
 					}
 				} else setEnabled(false);
 				break;
 			case COPY:
 			case CUT:
 				if (ho instanceof DatasetData || ho instanceof ImageData || 
 			         ho instanceof PlateData) {
 					selected = model.getSelectedDisplays();
 		    		for (int i = 0; i < selected.length; i++) {
 						if (model.canLink(selected[i].getUserObject())) 
 							count++;
 					}
 		    		if (index == COPY) {
 	                    if (ho instanceof DatasetData) {
 	                        if (!(parent instanceof ProjectData)) {
 	                            setEnabled(false);
 	                            return;
 	                        }
 	                    } else if (ho instanceof ImageData) {
 	                        if (!(parent instanceof DatasetData ||
 	                                parent instanceof TagAnnotationData)) {
 	                            setEnabled(false);
 	                            return;
 	                        }
 	                    } else if (ho instanceof PlateData) {
 	                        if (!(parent instanceof ScreenData)) {
 	                            setEnabled(false);
 	                            return;
 	                        }
 	                    }
 	                }
 	                setEnabled(count == selected.length);
 				} else if (ho instanceof ExperimenterData) {
 					setEnabled(model.getBrowserType() == 
 						Browser.ADMIN_EXPLORER);
 				} else if (ho instanceof TagAnnotationData) {
 					TagAnnotationData tag = (TagAnnotationData) ho;
 					if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
 							tag.getNameSpace()))
 						setEnabled(false);
 					else {
 						selected = model.getSelectedDisplays();
 			    		for (int i = 0; i < selected.length; i++) {
 			    			if (model.canAnnotate(selected[i].getUserObject())) 
 			    				count++;
 						}
 			    		if (index == CUT) {
 			    			if (!(parent instanceof TagAnnotationData)) {
 			    				setEnabled(false);
 			    				return;
 				    		}
 			    		}
 			    		setEnabled(count == selected.length);
 					}
 				} else setEnabled(false);
 		}
    }
   
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	 switch (model.getState()) {
	    	 case Browser.LOADING_DATA:
	         case Browser.LOADING_LEAVES:
	         case Browser.COUNTING_ITEMS:
	             setEnabled(false);
	             break;
	         default:
	        	onDisplayChange(model.getLastSelectedDisplay());
         }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
    	switch (index) {
			case NEW_ADMIN:
			case NEW_CONTAINERS:
			case NEW_TAGS:
				setEnabled(true);
				return;
		}
    	if (model.getBrowserType() == Browser.ADMIN_EXPLORER) {
    		setEnabled(TreeViewerAgent.isAdministrator());
    		return;
    	}
    	
    	adaptActionNameDescription(model.getBrowserType());
    	
    	if (selectedDisplay == null) {
    		handleExperimenter(model.getLastSelectedDisplay());
    		return;
    	}
    	Object ho = selectedDisplay.getUserObject();
    	if (ho instanceof ExperimenterData) {
    		handleExperimenter(selectedDisplay);
    		return;
    	} 
    	switch (index) {
	    	case COPY:
	    	case PASTE:
	    	case CUT:
	    		handleSelection(selectedDisplay);
	    		break;
	    	default:
	    		if (ho instanceof ExperimenterData) {
	    			long id = TreeViewerAgent.getUserDetails().getId();
	    			ExperimenterData exp = (ExperimenterData) ho;
	    			setEnabled(exp.getId() == id);
	    		} else setEnabled(model.canLink(ho));
		}
    }
    
    /**
     * Adapt the name and description of this action with respect to the
     * selected browser
     * 
     * @param browserType
     *            The type of the {@link Browser}
     */
    private void adaptActionNameDescription(int browserType) {
        if (browserType == Browser.ADMIN_EXPLORER) {
            switch (index) {
                case CUT:
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_CUT));
                    break;
                case COPY:
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_COPY));
                    break;
                case PASTE:
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
            }
        } else {
            switch (index) {
                case CUT:
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_CUT_LINK));
                    break;
                case COPY:
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities
                                    .formatToolTipText(DESCRIPTION_COPY_LINK));
                    break;
                case PASTE:
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities
                                    .formatToolTipText(DESCRIPTION_PASTE_LINK));
            }
        }
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
	public BrowserManageAction(Browser model, int index)
	{
		super(model);
		checkIndex(index);
		this.index = index;
		setEnabled(false);
		userID = TreeViewerAgent.getUserDetails().getId();
	}
	
	/**
     * Copies/Cuts/Pastes depending on the index.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	switch (index) {
			case COPY:
				model.setNodesToCopy(model.getSelectedDisplays(), 
						TreeViewer.COPY_AND_PASTE);
				break;
			case PASTE:
				model.paste(model.getSelectedDisplays());
				break;
			case CUT:
				 model.setNodesToCopy(model.getSelectedDisplays(), 
                         TreeViewer.CUT_AND_PASTE);
		}
    }
    
	/** 
     * Sets the location of the point where the <code>mousePressed</code>
     * event occurred. 
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) { point = me.getPoint(); }
    
    /** 
     * Brings up the menu. 
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    {
        Object source = me.getSource();
        if (point == null) point = me.getPoint();
        if (source instanceof Component && isEnabled()) {
        	switch (index) {
				case NEW_TAGS:
					model.showMenu(TreeViewer.CREATE_MENU_TAGS, 
		            		(Component) source, point);
					break;
				case NEW_CONTAINERS:
					if (model.getBrowserType() == Browser.PROJECTS_EXPLORER)
						model.showMenu(TreeViewer.CREATE_MENU_CONTAINERS, 
		            		(Component) source, point);
					else 
						model.showMenu(TreeViewer.CREATE_MENU_SCREENS, 
			            		(Component) source, point);
					break;
				case NEW_ADMIN:
					model.showMenu(TreeViewer.CREATE_MENU_ADMIN, 
		            		(Component) source, point);
        	}
        }
    }
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseEntered(MouseEvent)
     */   
    public void mouseEntered(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseExited(MouseEvent)
     */   
    public void mouseExited(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseClicked(MouseEvent)
     */   
    public void mouseClicked(MouseEvent e) {}
    
}
