
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package actions;

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import tree.DataFieldConstants;
import tree.Tree.Actions;
import ui.IModel;
import util.ImageFactory;


public class LockFieldsAction extends ProtocolEditorAction {
	
	Icon lockedIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOCKED_ICON);
	Icon unlockedIcon = ImageFactory.getInstance().getIcon(ImageFactory.UNLOCKED_ICON);
	Icon lockedIcon48 = ImageFactory.getInstance().getIcon(ImageFactory.LOCKED_ICON_48);
	
	/**
	 * These descriptions (toolTipTexts) describe the 4 different states that this action
	 * can have, based on the locked status of the currently highlighted field(s) and 
	 * their ancestors. 
	 */
	
	/**
	 * This state indicates that the highlighted field(s) is unlocked and it's 
	 * ancestors are unlocked. 
	 * Therefore the available action is "Lock" and the action is enabled.
	 * Clicking this action will lock the highlighted fields. 
	 */
	public static final String UNLOCKED_ANCESTOR_UNLOCKED = 
		"Lock fields to prevent editing. Also prevents editing of child fields";
	
	/**
	 * This state indicates that the highlighted field(s) is locked but it's 
	 * ancestors are unlocked. 
	 * Therefore the available action is "Unlock" and the action is enabled.
	 * Clicking this action will unlock the highlighted fields. 
	 */
	public static final String LOCKED_ANCESTOR_UNLOCKED = "Unlock fields to allow editing.";
	
	/**
	 * This state indicates that the highlighted field(s) is unlocked but it's 
	 * ancestors are locked. Therefore this field is effectively locked, and 
	 * the Icon will indicate a locked lock. 
	 * However, because an ancestor is locked, the action is disabled. 
	 */
	public static final String UNLOCKED_ANCESTOR_LOCKED = "Field is locked because an ancestor is locked." +
			" To edit this field, unlock the locked ancestor";
	
	/**
	 * This state indicates that the highlighted field(s) is locked and it's 
	 * ancestors are locked. Therefore this field is effectively "double-locked", and 
	 * the Icon will indicate a locked lock. 
	 * However, because an ancestor is locked, the action is disabled. 
	 */
	public static final String LOCKED_ANCESTOR_LOCKED = "Cannot unlock this field because an ancestor is locked. " +
			"To unlock this field, you must unlock the ancestor first";
	
	/**
	 * This is the current state of this action. One of the four described above. 
	 */
	private String currentState;
	
	
	public LockFieldsAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Lock Fields");
		
		/*
		 * The default state. These are updated according to the locked state of the highlighted
		 *  fields and their ancestors. 
		 */
		currentState = UNLOCKED_ANCESTOR_UNLOCKED;
		putValue(Action.SHORT_DESCRIPTION, UNLOCKED_ANCESTOR_UNLOCKED);
		putValue(Action.SMALL_ICON, unlockedIcon); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (currentState.equals(UNLOCKED_ANCESTOR_UNLOCKED))
			lockFields();
		
		else if (currentState.equals(LOCKED_ANCESTOR_UNLOCKED)) {
			unlockFields();
		}
	}
	
	
	public void lockFields() {
		
		Calendar now = new GregorianCalendar();
		SimpleDateFormat time = new SimpleDateFormat("HH:mm 'on' EEE, MMM d, yyyy");
		String nowString = time.format(now.getTime());
		
		String user = System.getProperty("user.name");
		
		String message = "This field will be marked as:\n" +
				"Locked at: " + nowString + "\n by user:";
				
		/*
		 * Need to get user to confirm locking, and give them a chance to change their Name. 
		 */
		Object chosenName = JOptionPane.showInputDialog(null, message, "Confirm Lock",
				JOptionPane.WARNING_MESSAGE, lockedIcon48, null, user);
		
		// Check that the user did not cancel
		if (chosenName != null) {
			
			/*
			 * Make a HashMap of all the attributes that describe the lock
			 */
			HashMap<String, String> lockingAttributes = new HashMap<String, String>();
			
			lockingAttributes.put(DataFieldConstants.FIELD_LOCKED_USER_NAME, chosenName.toString());
			lockingAttributes.put(DataFieldConstants.FIELD_LOCKED_UTC, now.getTimeInMillis() + "");
			
			model.lockHighlightedFields(lockingAttributes);
		}
	}
	
	public void unlockFields() {
		
		SimpleDateFormat time = new SimpleDateFormat("HH:mm 'on' EEE, MMM d, yyyy");
		
		/*
		 * Need to get user to confirm unlocking! 
		 */
		
		List<HashMap<String, String>> lockedFields = model.getLockedFieldsAttributes();
		
		String message = "<html>The field:</html>\n";
		
		for (HashMap<String, String> map : lockedFields) {
			long timeStamp = new Long(map.get(DataFieldConstants.FIELD_LOCKED_UTC));
			String dateTime = time.format(new Date(timeStamp));
			
			String line = "<html><b>- " + map.get(DataFieldConstants.ELEMENT_NAME) + "</b> was locked by <i>" +
				map.get(DataFieldConstants.FIELD_LOCKED_USER_NAME) + "</i> at " + dateTime + "</html>\n";
			message = message + line;
		}
		message = message + "\n<html>Are you sure you want to unlock " + 
			(lockedFields.size()>1 ? "these fields?":"this field?") + "</html>";
		
		Object[] options = {"Cancel", "Unlock"};
		
		int yesNo = JOptionPane.showOptionDialog(null, message,
				"Confirm Unlock", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				lockedIcon48, options, options[0]);
		
		if (yesNo == 1) {
			model.editCurrentTree(Actions.UNLOCK_HIGHLIGHTED_FIELDS);
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		boolean filesOpen = !(fileList.length == 0);
		
		// if no files open, action is disabled.
		if (!filesOpen) {
			this.setEnabled(false);
			putValue(Action.SMALL_ICON, unlockedIcon); 
			return;
		}
		
		/*
		 * If files are open, need to set enabled an icon, depending on whether the 
		 * highlighted fields are locked, their ancestors are locked, etc...
		 */
		else {
			
			/*
			 * If the ancestors of highlighted field are locked, this
			 * action should be disabled, since turning lock on/off will not affect
			 * whether this field is locked. 
			 */
			boolean ancestorsLocked = model.areAncestorFieldsLocked();
			setEnabled(!ancestorsLocked);
			
			/*
			 * The icon is in the "locked" state if the highlighted field OR ancestors are locked
			 * 
			 * The toolTipText is set depending on the locked state of the highlighted field and
			 * it's ancestors. 
			 */
			boolean fieldsLocked = model.areHighlightedFieldsLocked();
			
			//System.out.println("LockFieldsAction  fieldsLocked: " + 
			//		fieldsLocked + " ancestorsLocked: " + ancestorsLocked);
			
			if (!fieldsLocked && !ancestorsLocked) {
				currentState = UNLOCKED_ANCESTOR_UNLOCKED;
				putValue(Action.SMALL_ICON, (unlockedIcon)); 
				putValue(Action.SHORT_DESCRIPTION, UNLOCKED_ANCESTOR_UNLOCKED);
			}
			
			else if (fieldsLocked && !ancestorsLocked) {
				currentState = LOCKED_ANCESTOR_UNLOCKED;
				putValue(Action.SMALL_ICON, (lockedIcon)); 
				putValue(Action.SHORT_DESCRIPTION, LOCKED_ANCESTOR_UNLOCKED);
			}
			
			else if (!fieldsLocked && ancestorsLocked) {
				currentState = UNLOCKED_ANCESTOR_LOCKED;
				putValue(Action.SMALL_ICON, (lockedIcon)); 
				putValue(Action.SHORT_DESCRIPTION, UNLOCKED_ANCESTOR_LOCKED);
			}
			
			else if (fieldsLocked && ancestorsLocked) {
				currentState = LOCKED_ANCESTOR_LOCKED;
				putValue(Action.SMALL_ICON, (lockedIcon)); 
				putValue(Action.SHORT_DESCRIPTION, LOCKED_ANCESTOR_LOCKED);
			}
		}
	
	}
	
	
}
