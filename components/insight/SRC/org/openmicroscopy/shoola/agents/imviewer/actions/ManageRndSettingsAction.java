/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ManageRndSettingsAction 
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
package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modifies the rendering settings.
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
public class ManageRndSettingsAction 
	extends ViewerAction
{

	/** Identified the copy action. */
	public static final int 	COPY = 0;
	
	/** Identified the paste action. */
	public static final int 	PASTE = 1;
	
	/** Identified the <code>Reset</code> action. */
	public static final int 	RESET = 2;
	
	/** Identified the <code>Set Min max</code> action. */
	public static final int 	SET_MIN_MAX = 3;
	
	/** Identified the <code>Set Owner</code> action. */
	public static final int 	SET_OWNER = 4;
	
	/** Identified the <code>Undo</code> action. */
	public static final int 	UNDO = 5;
	
	/** Identified the <code>Set Min max</code> action. */
	public static final int 	SET_ABSOLUTE_MIN_MAX = 6;
	
	/** The name of the action if the index is {@link #COPY}. */
    private static final String NAME_COPY = "Copy Settings";
    
    /** The description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY = 
    										"Copy the rendering settings.";
    
    /** The name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE = "Paste Settings";
    
    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE = 
    									"Paste the rendering settings.";
    
	/** The default name of the action if the index is {@link #RESET}. */
    private static final String NAME_RESET = "Reset Default Settings";
    
    /** The description of the action if the index is {@link #RESET}. */
    private static final String DESCRIPTION_RESET = 
    	"Reset the rendering settings created while importing.";
    
    /** The default name of the action if the index is {@link #SET_MIN_MAX}. */
    private static final String NAME_SET_MIN_MAX = "Min/Max";
    
    /** 
     * The default name of the action if the index is 
     * {@link #ABSOLUTE_SET_MIN_MAX}. 
     */
    private static final String NAME_SET_ABSOLUTE_MIN_MAX ="Full Range";
    
    /** The description of the action if the index is {@link #SET_MIN_MAX}. */
    private static final String DESCRIPTION_SET_MIN_MAX = 
    				"Set the Pixels Intensity interval to min/max" +
    				"for all channels.";
    
    /** The name of the action if the index is {@link #SET_OWNER}. */
    private static final String NAME_SET_OWNER= "Set Owner's Settings";
    
    /** 
     * The description of the action if the index is {@link #SET_OWNER}. 
     */
    private static final String DESCRIPTION_SET_OWNER  = 
    									"Set the Owner's rendering settings.";
    
    /** The name of the action if the index is {@link #UNDO}. */
    private static final String NAME_UNDO = "Undo changes";
    
    /** The description of the action if the index is {@link #UNDO}. */
    private static final String DESCRIPTION_UNDO  = "Undo the changes.";
    
    /** The description of the action if {@link #SET_ABSOLUTE_MIN_MAX}. */
	private static final String DESCRIPTION_SET_ABSOLUTE_MIN_MAX = 
		"Set the Pixels Intensity interval to the full range for all channels.";
	
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
				name = NAME_COPY;
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_COPY));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.COPY));
				break;
			case PASTE:
				name = NAME_PASTE;
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
				putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASTE));
				break;
			case RESET:
				name = NAME_RESET;
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_RESET));
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.RND_REDO));
				break;
			case SET_MIN_MAX:
				name = NAME_SET_MIN_MAX;
				putValue(Action.SHORT_DESCRIPTION, 
					UIUtilities.formatToolTipText(DESCRIPTION_SET_MIN_MAX));
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.RND_MIN_MAX));
				break;
			case SET_ABSOLUTE_MIN_MAX:
				name = NAME_SET_ABSOLUTE_MIN_MAX;
				putValue(Action.SHORT_DESCRIPTION, 
					UIUtilities.formatToolTipText(
							DESCRIPTION_SET_ABSOLUTE_MIN_MAX));
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.RND_MIN_MAX));
				break;
			case SET_OWNER:
				name = NAME_SET_OWNER;
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(
							DESCRIPTION_SET_OWNER));
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.RND_OWNER));
				break;
			case UNDO:
				name = NAME_UNDO;
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(
							DESCRIPTION_UNDO));
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.RND_UNDO));
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/**
     * Disposes and closes the movie player when the {@link ImViewer} is
     * discarded.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e)
    {
    	if (model.getState() == ImViewer.READY) onTabSelection();
    	else setEnabled(false);
    }
    
	 /** 
     * Sets the enabled flag depending on the tab selected.
     * @see ViewerAction#onTabSelection()
     */
    protected void onTabSelection()
    {
    	if (model.getSelectedIndex() == ImViewer.PROJECTION_INDEX)
    		setEnabled(false);
    	else {
    		switch (index) {
				case PASTE:
					if (!model.hasSettingsToPaste()) {
						setEnabled(false);
					} else {
						setEnabled(model.canAnnotate());
					}
					break;
				case RESET:
				case SET_MIN_MAX:
					setEnabled(model.canAnnotate());
					break;
				case SET_OWNER:
					if (model.isUserOwner()) setEnabled(false);
					else setEnabled(model.canAnnotate());
					break;
				case COPY:
					setEnabled(model.canAnnotate());
			}
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
	public ManageRndSettingsAction(ImViewer model, int index)
	{
		super(model);
		icons = IconManager.getInstance();
		checkIndex(index);
		this.index = index;
	}

	/** 
     * Resets the default rendering settings.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	//model.setOriginalRndSettings();
    	switch (index) {
    		case COPY:
    			model.copyRenderingSettings();
    			break;
    		case PASTE:
    			model.pasteRenderingSettings();
    			break;
			case RESET:
				model.resetDefaultRndSettings();
				break;
			case SET_MIN_MAX:
				model.setRangeAllChannels(false);
				break;
			case SET_ABSOLUTE_MIN_MAX:
				model.setRangeAllChannels(true);
				break;
			case SET_OWNER:
				model.setOwnerSettings();
		}
    }

}
