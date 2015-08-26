/*
* org.openmicroscopy.shoola.agents.measurement.actions.KeywordSelectionAction
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.actions;

import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxList;

/**
 * Selects the keywords associated a namespace.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class KeywordSelectionAction
	extends MeasurementViewerAction
	implements TableModelListener
{

	/**
	 * Creates a new instance.
	 * 
	 * @param model Model for action.
	 */
	public KeywordSelectionAction(MeasurementViewer model)
	{
		super(model);
	}

	/**
	 * Implemented as specified by the {@link TableModelListener}.
	 * @see TableModelListener#tableChanged(TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e)
	{
		CheckBoxList checkBoxList = (CheckBoxList) e.getSource();
	    List<String> keywords = checkBoxList.getTrueValues();
	    model.setKeyword(keywords);
	}
    
}
