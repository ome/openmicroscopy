/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.ManageObjectAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ActionCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CopyCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CutCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Cuts, copies and pastes objects. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ManageObjectAction 
	extends TreeViewerAction
{

	/** Identified the copy action. */
	public static final int 	COPY = 0;
	
	/** Identified the paste action. */
	public static final int 	PASTE = 1;
	
	/** Identified the remove action. */
	public static final int 	REMOVE = 2;
	
	/** Identified the cut action. */
	public static final int 	CUT = 3;
	
	/** The default name of the action if the index is {@link #COPY}. */
    private static final String NAME_COPY = "Copy";
    
    /** The description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY = 
    											"Copy the selected elements.";
    
    /** The default name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE = "Paste";
    
    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE = 
    									"Paste the selected elements.";
    
    /** The default name of the action if the index is {@link #REMOVE}. */
    private static final String NAME_REMOVE = "Delete";
    
    /** The description of the action if the index is {@link #REMOVE}. */
    private static final String DESCRIPTION_REMOVE = 
    								"Delete the selected elements.";
	
    /** The default name of the action if the index is {@link #CUT}. */
    private static final String NAME_CUT = "Cut";
    
    /** The description of the action if the index is {@link #CUT}. */
    private static final String DESCRIPTION_CUT = 
    								"Cut the selected elements.";
    
	/** One of the constants defined by this class. */
	private int 		index;
	
	/** Helper reference to the icons manager. */
	private IconManager icons;
	
	/**
	 * Checks if the passed index is supported.
	 * 
	 * @param value The value to control.
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case COPY:
				name = NAME_COPY;
				putValue(Action.NAME, NAME_COPY);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_COPY));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.COPY));
				break;
			case PASTE:
				name = NAME_PASTE;
				putValue(Action.NAME, NAME_PASTE);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASTE));
				break;
			case REMOVE:
				name = NAME_REMOVE;
				putValue(Action.NAME, NAME_REMOVE);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_REMOVE));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.DELETE));
				break;
			case CUT:
				name = NAME_CUT;
				putValue(Action.NAME, NAME_CUT);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_CUT));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.CUT));
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/** 
	 * Sets the action enabled depending on the state of the {@link Browser}.
	 * @see TreeViewerAction#onBrowserStateChange(Browser)
	 */
	protected void onBrowserStateChange(Browser browser)
	{
		if (browser == null) return;
		switch (browser.getState()) {
			case Browser.LOADING_DATA:
			case Browser.LOADING_LEAVES:
			//case Browser.COUNTING_ITEMS:  
				setEnabled(false);
				break;
			default:
				onDisplayChange(browser.getLastSelectedDisplay());
			break;
		}
	}
	
	/**
	 * Returns <code>true</code> if the pasting action is valid,
	 * <code>false</code> otherwise.
	 * 
	 * @param ho 	The selected data object.
	 * @param klass	The type identifying the objects to copy.
	 * @return See above.
	 */
	private boolean isPasteValid(Object ho, Class klass)
	{
		if (ho instanceof ProjectData && DatasetData.class.equals(klass))
			return true;
		else if (ho instanceof ScreenData && PlateData.class.equals(klass))
			return true;
		else if (ho instanceof DatasetData && ImageData.class.equals(klass))
			return true;
		else if (ho instanceof GroupData && 
				ExperimenterData.class.equals(klass))
			return true;
		else if (ho instanceof TagAnnotationData && 
				TagAnnotationData.class.equals(klass)) {
			TagAnnotationData tag = (TagAnnotationData) ho;
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
				return true;
		}
		return false;
	}
	
	/**
	 * Sets the action enabled depending on the selected type.
	 * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
	 */
	protected void onDisplayChange(TreeImageDisplay selectedDisplay)
	{
		if (selectedDisplay == null) {
			setEnabled(false);
			return;
		}
		Browser browser = model.getSelectedBrowser();
        if (browser == null) {
        	setEnabled(false);
            return;
        }
        Object ho = selectedDisplay.getUserObject(); 
        TreeImageDisplay[] selected;
        int count = 0;
        TreeImageDisplay parentDisplay = selectedDisplay.getParentDisplay();
        Object parent = null;
        if (parentDisplay != null) parent = parentDisplay.getUserObject();
        switch (index) {
			case PASTE:
				Class klass = model.hasDataToCopy();
				if (klass == null) {
					setEnabled(false);
		            return;
				}
				if (ho instanceof ProjectData || ho instanceof ScreenData ||
					ho instanceof DatasetData || ho instanceof GroupData ||
					ho instanceof TagAnnotationData) {
					selected = browser.getSelectedDisplays();
		    		for (int i = 0; i < selected.length; i++) {
		    			ho = selected[i].getUserObject();
		    			if (isPasteValid(ho, klass)) {
		    				if (ho instanceof GroupData) {
		    					count++;
			    			} else {
			    				if (model.isUserOwner(ho)) count++;
			    			}
		    			}
					}
		    		setEnabled(count == selected.length);
				} else setEnabled(false);
				break;
			case REMOVE:
				if (ho instanceof ProjectData || ho instanceof DatasetData ||
					ho instanceof ScreenData || ho instanceof PlateData ||
					ho instanceof PlateAcquisitionData ||
					ho instanceof FileAnnotationData || 
					ho instanceof TagAnnotationData ||
					ho instanceof ImageData) {
					selected = browser.getSelectedDisplays();
		    		for (int i = 0; i < selected.length; i++) {
						if (model.canDeleteObject(selected[i].getUserObject())) 
							count++;
					}
		    		setEnabled(count == selected.length);
				} else if (ho instanceof ExperimenterData) {
					if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
						/*
		        		setEnabled(true);
		        		selected = browser.getSelectedDisplays();
		        		if (selected != null) {
		        			TreeImageDisplay d;
		        			ExperimenterData exp;
		        			boolean b = true;
		        			long id = TreeViewerAgent.getUserDetails().getId();
		        			for (int i = 0; i < selected.length; i++) {
		        				d = selected[i];
		        				exp = (ExperimenterData) d.getUserObject();
		        				if (exp.getId() == id) {
		        					b = false;
		        					break;
		        				}
		        			}
		        			setEnabled(b);
		        		}
		        		*/
						setEnabled(false);
		        	} else setEnabled(false);
				} else if (ho instanceof GroupData) {
					setEnabled(false); //TODO
				} else setEnabled(false);
				
				break;
			case COPY:
			case CUT:
				if (ho instanceof DatasetData || ho instanceof ImageData || 
			         ho instanceof PlateData) {
					selected = browser.getSelectedDisplays();
		    		for (int i = 0; i < selected.length; i++) {
						if (model.isUserOwner(selected[i].getUserObject())) 
							count++;
					}
		    		if (index == CUT) {
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
					setEnabled(browser.getBrowserType() == 
						Browser.ADMIN_EXPLORER);
				} else if (ho instanceof TagAnnotationData) {
					TagAnnotationData tag = (TagAnnotationData) ho;
					if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
							tag.getNameSpace()))
						setEnabled(false);
					else {
						selected = browser.getSelectedDisplays();
			    		for (int i = 0; i < selected.length; i++) {
			    			if (model.isUserOwner(selected[i].getUserObject())) 
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
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 * @param index One of the constants defined by this class.
	 */
	public ManageObjectAction(TreeViewer model, int index)
	{
		super(model);
		icons = IconManager.getInstance();
		checkIndex(index);
		this.index = index;
	}
	
	/**
     * Copies, pastes, cuts or removes the selected objects.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	ActionCmd cmd = null;
    	switch (index) {
			case COPY:
				cmd = new CopyCmd(model);
				break;
			case PASTE:
				cmd = new PasteCmd(model);
				break;
			case REMOVE:
				cmd = new DeleteCmd(model.getSelectedBrowser());
				break;
			case CUT:
				cmd = new CutCmd(model);
		}
    	if (cmd != null) cmd.execute();
    }
    
}
