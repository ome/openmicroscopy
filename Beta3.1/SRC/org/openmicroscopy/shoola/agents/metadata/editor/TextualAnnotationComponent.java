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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
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

	/** The tooltip of the delete button. */
	private static final String	DELETE_TIP = "Delete the annotation.";
	
	/** 
	 * The delete button or <code>null</code> if the component is
	 * not editable.
	 */
	private JButton 				delete;
	
	/** Area displaying the textual annotation. */
	private MultilineLabel 			area;
	
	/** The annotation to handle. */
	private TextualAnnotationData	data;
	
	/** Reference to the UI component hosting this component. */
	private TextualAnnotationsUI 	uiDelegate;
	
	/** Reference to the model.	*/
	private EditorModel				model;
	
	/** Initializes the UI components. */
	private void initialize()
	{
		area = new MultilineLabel();
        area.setEditable(false);
        area.setOpaque(true);
        area.setText(data.getText());
        IconManager icons = IconManager.getInstance();
        if (model.isCurrentUserOwner(data)) {
    		delete = new JButton(icons.getIcon(IconManager.REMOVE));
    		delete.setToolTipText(DELETE_TIP);
    		UIUtilities.unifiedButtonLookAndFeel(delete);
    		delete.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent e) {
					uiDelegate.deleteAnnotation(data);
				}
			
			});
    	}
        String owner = model.formatOwner(data);
		String date = model.formatDate(data);
		UIUtilities.setBoldTitledBorder(owner+" "+date, area);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JToolBar bar = null;
		if (delete != null) {
			bar = new JToolBar();
			bar.setFloatable(false);
			bar.setRollover(true);
			bar.setBorder(null);
			bar.setOpaque(true);
			bar.add(delete);
	        
		}
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.FILL} }; //rows
        setOpaque(true);
        setLayout(new TableLayout(tl));
        if (bar != null) add(bar, "0, 0, l, c");
        add(area, "1, 0, 1, 1");
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
	TextualAnnotationComponent(TextualAnnotationsUI uiDelegate,
								EditorModel model, TextualAnnotationData data)
	{
		this.uiDelegate = uiDelegate;
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
	}
	
}
