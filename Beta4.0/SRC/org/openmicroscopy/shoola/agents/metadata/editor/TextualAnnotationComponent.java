/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TextualAnnotationComponent 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.TextualAnnotationData;

/** 
 * Component displaying a textual annotation.
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
class TextualAnnotationComponent 
	extends JPanel
{

	/** Area displaying the textual annotation. */
	private MultilineLabel 			area;
	
	/** The annotation to handle. */
	private TextualAnnotationData	data;
	
	/** Reference to the model.	*/
	private EditorModel				model;

	/** Initializes the UI components. */
	private void initialize()
	{
		area = new MultilineLabel();
        area.setEditable(false);
        area.setOpaque(true);
        area.setText(data.getText());
    	String owner = model.formatOwner(data);
		String date = model.formatDate(data);
		TitledLineBorder border = new TitledLineBorder(owner+" "+date);
		border.setTitleFont(area.getFont().deriveFont(Font.BOLD));
		area.setBorder(border);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(area);
	}
	
	/**
	 * Creates a new component.
	 * 
	 * @param uiDelegate	Reference to the ui component hosting this 
	 * 						component.
	 * @param model			Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 * @param data			The annotation to handle.
	 */
	TextualAnnotationComponent(EditorModel model, TextualAnnotationData data)
	{
		this.data = data;
		this.model = model;
		initialize();
		buildGUI();
	}
	
	/**
	 * Sets the background of the area, the background color
	 * will change when the user clicks on the node.
	 * 
	 * @param color The color to set.
	 */
	void setAreaColor(Color color)
	{
		area.setOriginalBackground(color);
		setBackground(color);
		((TitledLineBorder) area.getBorder()).setLineColor(color);
	}
	
}
