/*
 * org.openmicroscopy.shoola.agents.dataBrowser.actions.ManageRndSettingsAction 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
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
import pojos.WellSampleData;

/** 
 * Copies, pastes or resets the rendering settings depending on the
 * specified index.
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
public class ManageRndSettingsAction 
	extends DataBrowserAction
	implements PropertyChangeListener
{

	/** Identified the copy action. */
	public static final int 	COPY = 0;
	
	/** Identified the paste action. */
	public static final int 	PASTE = 1;
	
	/** Identified the reset action. */
	public static final int 	RESET = 2;
	
	/** Identified the reset action. */
	public static final int 	SET_ORIGINAL = 3;
	
	/** The default name of the action if the index is {@link #COPY}. */
    private static final String NAME_COPY = "Copy Settings";
    
    /** The description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY = 
    										"Copy the rendering settings.";
    
    /** The default name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE = "Paste Settings";
    
    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE = 
    									"Paste the rendering settings.";
    
    /** The default name of the action if the index is {@link #RESET}. */
    private static final String NAME_RESET = "Reset Settings";
    
    /** The description of the action if the index is {@link #RESET}. */
    private static final String DESCRIPTION_RESET = 
    									"Reset the rendering settings.";
    
    /** The default name of the action if the index is {@link #SET_ORIGINAL}. */
    private static final String NAME_SET_ORIGINAL = "Set Original Settings";
    
    /** The description of the action if the index is {@link #SET_ORIGINAL}. */
    private static final String DESCRIPTION_SET_ORIGINAL = 
    									"Set the original rendering settings.";
    
    /** Helper reference to the icons manager. */
	private IconManager icons;
	
	/** One of the constants defined by this class. */
	private int 		index;
	
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
			case RESET:
				putValue(Action.NAME, NAME_RESET);
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_RESET));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.UNDO));
				break;
			case SET_ORIGINAL:
				putValue(Action.NAME, NAME_SET_ORIGINAL);
				putValue(Action.SHORT_DESCRIPTION, 
					UIUtilities.formatToolTipText(DESCRIPTION_SET_ORIGINAL));
				//putValue(Action.SMALL_ICON, 
					//	icons.getIcon(IconManager.SET_ORIGINAL_RND_SETTINGS));
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.UNDO));
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
        Collection nodes = browser.getSelectedDisplays();
        Object ho = node.getHierarchyObject();
        
        switch (index) {
			case COPY:
				if (nodes.size() > 1) setEnabled(false);
	    		else {
	    			setEnabled(ho instanceof WellSampleData ||
	    					ho instanceof ImageData);
	    		}
				break;
			case PASTE:
				if (!model.hasRndSettings()) {
					setEnabled(false);
					return;
				}
				if (!(ho instanceof ImageData || ho instanceof DatasetData ||
						ho instanceof WellSampleData))
					setEnabled(false);
				else {
					if (nodes.size() > 1) 
						setEnabled(ho instanceof ImageData ||
							ho instanceof WellSampleData);
		        	else setEnabled(true);
				}
				break;
			case RESET:
				if (!(ho instanceof ImageData || ho instanceof DatasetData ||
						ho instanceof WellSampleData))
					setEnabled(false);
				else setEnabled(true);
				break;
			case SET_ORIGINAL:
				if (!(ho instanceof ImageData || ho instanceof DatasetData ||
						ho instanceof WellSampleData))
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
	public ManageRndSettingsAction(DataBrowser model, int index)
	{
		super(model);
		setEnabled(false);
		icons = IconManager.getInstance();
		checkIndex(index);
		this.index = index;
		model.addPropertyChangeListener(DataBrowser.COPY_ITEMS_PROPERTY, this);
		model.addPropertyChangeListener(
				DataBrowser.RND_SETTINGS_TO_COPY_PROPERTY, this);
	}
	
	/**
     * Copies, pastes or resets the rendering settings for the selected objects.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	switch (index) {
			case COPY:
				model.copyRndSettings();
				break;
			case PASTE:
				model.pasteRndSettings();
				break;
			case RESET:
				model.resetRndSettings();
				break;
			case SET_ORIGINAL:
				model.setOriginalSettings();
		}
    }
    
    /**
     * Reacts to property changes in the {@link DataBrowser}.
     * Sets the enabled flag.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
    	String name = evt.getPropertyName();
    	if (DataBrowser.COPY_ITEMS_PROPERTY.equals(name) ||
    		DataBrowser.RND_SETTINGS_TO_COPY_PROPERTY.equals(name) ||
    		Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY.equals(
        			name)) {
    		Browser browser = model.getBrowser();
        	if (browser != null)
        		onDisplayChange(browser.getLastSelectedDisplay());
    	}
    }
	
}
