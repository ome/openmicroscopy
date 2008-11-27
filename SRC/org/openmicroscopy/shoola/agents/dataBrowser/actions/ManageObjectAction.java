/*
 * org.openmicroscopy.shoola.agents.dataBrowser.actions.ManageObjectAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Manages the object i.e. either copy, paste, cut or remove. 
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
public class ManageObjectAction 
	extends DataBrowserAction
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
				putValue(Action.NAME, NAME_COPY);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_COPY));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.COPY));
				break;
			case PASTE:
				putValue(Action.NAME, NAME_PASTE);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASTE));
				break;
			case REMOVE:
				putValue(Action.NAME, NAME_REMOVE);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_REMOVE));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.REMOVE));
				break;
			case CUT:
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
     * Callback to notify a change of state.
     * @see DataBrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	Browser browser = model.getBrowser();
    	if (browser != null)
    		onDisplayChange(browser.getLastSelectedDisplay());
    }
    
    /**
     * Sets the action enabled depending on the currently selected display.
     * @see DataBrowserAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay node)
    {
    	Browser browser = model.getBrowser();
        if (node == null || browser == null) {
            setEnabled(false);
            return;
        }
        Object ho = node.getHierarchyObject();
        ImageDisplay parent = node.getParentDisplay();
        Object parentObject;
        switch (index) {
			case COPY:
				if (ho instanceof DatasetData) {
					if (parent == null) setEnabled(false);
		    		else {
		    			parentObject = parent.getHierarchyObject();
		    			if (parentObject instanceof ProjectData)
							setEnabled(model.isObjectWritable(ho));
						else setEnabled(false);
		    		}
				} else if (ho instanceof ImageData)
					setEnabled(model.isObjectWritable(ho));
				else setEnabled(false);
				
				break;
			case PASTE:
				 if ((ho instanceof DatasetData) ||(ho instanceof ImageData))
					 setEnabled(model.isObjectWritable(ho));
			     else setEnabled(false);
				break;
			case REMOVE:
				if ((ho instanceof ProjectData) || (ho instanceof DatasetData)
						|| (ho instanceof ImageData)) {
					setEnabled(model.isObjectWritable(ho));
				/*
				else if (ho instanceof DatasetData) {
					if (parent == null) setEnabled(false);
					else {
		    			parentObject = parent.getHierarchyObject();
		    			if (parentObject instanceof ProjectData)
							setEnabled(model.isObjectWritable(ho));
						else setEnabled(false);
		    		}
				} else if (ho instanceof ImageData) {
					if (parent == null) setEnabled(false);
					else {
		    			parentObject = parent.getHierarchyObject();
		    			if (parentObject instanceof DatasetData)
							setEnabled(model.isObjectWritable(ho));
						else setEnabled(false);
		    		}
		    		*/
				} else setEnabled(false);
				break;
			case CUT:
				if (!(ho instanceof ImageData || ho instanceof DatasetData))
					setEnabled(false);
				else setEnabled(true);
		}
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 * @param index	One of the management constants defined by this class.
	 */
	public ManageObjectAction(DataBrowser model, int index)
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
    	switch (index) {
			case COPY:
				model.copy();
				break;
			case PASTE:
				model.paste();
				break;
			case REMOVE:
				model.remove();
				break;
			case CUT:
				model.cut();
		}
    }
    
}
