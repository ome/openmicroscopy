
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

package ui.fieldEditors;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.AlarmSetter;


public class FieldEditorDateTime extends FieldEditor {

	AlarmSetter alarmSetter;
	
	
	public FieldEditorDateTime (IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		alarmSetter = new AlarmSetter(dataField, DataFieldConstants.ALARM_SECONDS);
		
		attributeFieldsPanel.add(alarmSetter);
		
		// this is called by the super() constructor, but at that time
		// not all components will have been instantiated. 
		refreshLockedStatus();
	}
	
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);
		
		// need to check != null because this is called by the super() constructor
		// before all subclass components have been instantiated. 
		if (alarmSetter != null) {
			alarmSetter.setEnabled(enabled);
		}
	}
}
