 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamValuesTable 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomComboBox;
import org.openmicroscopy.shoola.agents.editor.uiComponents.DDTableCellRenderer;
import org.openmicroscopy.shoola.agents.editor.uiComponents.TableEditUI;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a table for displaying multiple values for each parameter in a 
 * field/step. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ParamValuesTable 
	extends JTable
{

	public ParamValuesTable(IField field) 
	{
		TableModel tableModel = field.getTableData();
		if (tableModel != null)
			setModel(tableModel);
		//super(tableModel);
		
		List<IParam> params = field.getAtomicParams();
		IParam param;
		JComboBox comboBox;
		String options;
		for (int c=0; c < params.size(); c++) {
			param = params.get(c);
			if (param instanceof EnumParam) {
				options = param.getAttribute(EnumParam.ENUM_OPTIONS);
				if (options != null) {
					String[] ddOptions = options.split(",");
					String[] listOptions = new String[ddOptions.length + 1];
					listOptions[0] = EnumEditor.NO_OPTION_CHOSEN;
					for (int i=0; i<ddOptions.length; i++) {
						listOptions[i+1] = ddOptions[i].trim();
					}
					comboBox = new CustomComboBox(listOptions);
					getColumnModel().getColumn(c).setCellEditor(
							new DefaultCellEditor(comboBox));
					getColumnModel().getColumn(c).setCellRenderer(
							new DDTableCellRenderer());
				}
			}
		}
	}
}
