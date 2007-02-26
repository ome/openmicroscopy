/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.UserProfile 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
class UserProfile
	extends JPanel
{

    /** Reference to the View. */
    private EditorUI            	view;
    
    /** Reference to the Model. */
    private EditorModel         	model;
    
    /** Reference to the Control. */
    private EditorControl       	controller;
    
    /** The editable items. */
    private Map<String, JComponent>	items;
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	items = new HashMap<String, JComponent>();
    }
    
    /**
     * Builds the panel hosting the information
     * 
     * @param details 	The information to display.
     * @param editable	Pass <code>true</code> if the edited user is the
     * 					current user, <code>false</code> otherwise.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details, boolean editable)
    {
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        Iterator i = details.keySet().iterator();
        JLabel label;
        JTextField area;
        String key, value;
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = (String) details.get(key);
            label = UIUtilities.setTextFont(key);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            //c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(editable);
            area.setEnabled(false);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
            items.put(key, area);
        }
        return content;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     * 
     * @param details 	The information to display.
     * @param editable	Pass <code>true</code> if the edited user is the
     * 					current user, <code>false</code> otherwise.
     */
    private void buildGUI(Map details, boolean editable)
    {
    	setBorder(new EtchedBorder());
    	JPanel contentPanel = buildContentPanel(details, editable);
    	double[][] tl = {{TableLayout.FILL}, {TableLayout.PREFERRED}}; 
    	setLayout(new TableLayout(tl));
    	add(contentPanel, "0, 0, f, t");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view          Reference to the {@link EditorUI}.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the {@link EditorModel}.
     *                      Mustn't be <code>null</code>.  
     * @param controller    Reference to the {@link EditorControl}.
     *                      Mustn't be <code>null</code>.                           
     */
	UserProfile(EditorUI view, EditorModel model, EditorControl controller)
	{
		if (view == null) throw new IllegalArgumentException("No View.");
        if (model == null)  throw new IllegalArgumentException("No Model.");
        if (controller == null) 
            throw new IllegalArgumentException("No Control.");
        this.view = view;
        this.model = model;
        this.controller = controller;
        initComponents();
	}
	
	/**
	 * Builds the UI.
	 * 
     * @param details 	The information to display.
     * @param editable	Pass <code>true</code> if the edited user is the
     * 					current user, <code>false</code> otherwise.
	 */
	void initialize(Map details, boolean editable)
	{
		buildGUI(details, editable);
	}
	
}
