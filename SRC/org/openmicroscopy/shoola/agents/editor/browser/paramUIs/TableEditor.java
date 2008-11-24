 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TableEditor 
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

//Java imports

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TableParam;
import org.openmicroscopy.shoola.agents.editor.model.tables.MutableTableModel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.TableEditUI;

/** 
 * A UI component for viewing and editing the data in a TableModel.
 * This class gets the table model to edit from a Parameter object. 
 * 
 * If the tableModel is an instance of MutableTableModel, can also add 
 * and remove rows. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TableEditor 
	extends AbstractParamEditor
	implements PropertyChangeListener
{
	
	private JPanel tableEditUI;
	
	/**
	 * Initialises the UI components. 
	 */
	private void initialise() 
	{	
		IParam param = (IParam)getParameter();
		
		TableModel tableModel;
		// Check this is a TableParam. 
		// If so, get the table model, and use it to make a new JTable.
		if (param instanceof TableParam) {
			/* use the table model to instanciate a JTable */
			tableModel = ((TableParam)param).getTableModel();
			tableEditUI = new TableEditUI(tableModel);
			tableEditUI.addPropertyChangeListener(TableEditUI.SIZE_CHANGED, this);
		}
	}
	
	/**
	 * Builds the UI.
	 * Puts the JTable in a ScrollPane, so that the column names are displayed.
	 * Adds buttons for adding/removing rows. 
	 */
	private void buildUI() 
	{ 
		
        add(tableEditUI, BorderLayout.CENTER);
	}

	/**
	 * Fires a PropertyChange for FieldPanel.UPDATE_EDITING_PROPERTY
	 * so that the size of this panel is refreshed, and editing continues...
	 */
	private void refreshEditingSize() 
	{
		// Need to resize...
		firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
	}

	/**
	 * Creates an instance of this class.
	 * Gets the tableModel from the parameter object. 
	 * Builds the UI. 
	 * 
	 * @param param		The table parameter this UI component edits
	 */
	public TableEditor(IParam param) 
	{	
		super(param);
		setLayout(new BorderLayout());
		
		initialise();
		
		buildUI();
	}
	
	/**
	 * Need to override this method for the JPanel to fix a bug with 
	 * resizing. 
	 * Otherwise when the JTable gains focus, it makes this panel bigger.
	 * This is AFTER the JTree that displays this field has called 
	 * getPreferredSize(). 
	 * Therefore, if the field is selected, this panel is too big for 
	 * the field, and overlaps surrounding components. 
	 * 
	 * @see JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() 
	{	
		return tableEditUI.getPreferredSize();
	}
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Table"; }

	
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (TableEditUI.SIZE_CHANGED.equals(evt.getPropertyName())) {
			refreshEditingSize();
		}
	}

}
