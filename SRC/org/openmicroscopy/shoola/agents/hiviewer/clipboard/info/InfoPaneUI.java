/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.info.InfoPaneUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.info;



//Java imports
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The UI delegate for the {@link InfoPane}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class InfoPaneUI
    extends JPanel
{

    /** Message displayed when information available for the object. */
    private static String   EDIT_MSG = "Information for ";
    
    /** The default message. */
    private static String   DEFAULT_MSG = "No information available";
    
    /** Reference to the Model. */
    private InfoPane    			model;
    
    /** 
     * The panel hosting the image information if the edited object is an
     * image.
     */
    private JPanel      			contentPanel;
    
    /** The label presenting the edition context. */
    private JLabel      			titleLabel;
    
    /** The panel hosting the {@link #titleLabel} and a separator. */
    private JPanel      			titlePanel;
    
    /** The fields displaying the values. */
    private Map<String, JTextField>	fields;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        titleLabel = new JLabel(DEFAULT_MSG);
        titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(new JSeparator());
        contentPanel = buildContentPanel(
        					InfoPaneUtil.transformPixelsData(null));
    }
    
    /**
     * Builds the panel hosting the information.
     * 
     * @param details The information to display.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details)
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
        fields = new HashMap<String, JTextField>(details.size());
        FontMetrics fm = getFontMetrics(getFont());
        int h = fm.getHeight();
        Dimension d = new Dimension(60, h+4);
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = (String) details.get(key);
            label = UIUtilities.setTextFont(key);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            area.setEnabled(false);
            label.setLabelFor(area);
            fields.put(key, area);
            c.gridx = 1;
            area.setPreferredSize(d);
            //c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            //c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;//1.0;
            content.add(area, c);  
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(new JLabel(), c); 
        }
        return content;
    }
    
    /**
     * Resets the values.
     * 
     * @param details The new value to display.
     */
    private void resetValues(Map details)
    {
    	Iterator i = fields.keySet().iterator();
    	String key;
    	String text = "";
    	JTextField field;
    	while (i.hasNext()) {
			key = (String) i.next();
			if (details != null) text = (String) details.get(key);
			field = fields.get(key);
			field.setText(text);
		}
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	double[][] tl = {{TableLayout.FILL}, 
    					{TableLayout.PREFERRED, TableLayout.PREFERRED}}; 
    	setLayout(new TableLayout(tl));
    	add(titlePanel, "0, 0, f, t");
    	add(contentPanel, "0, 1, f, t");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * 
     */
    InfoPaneUI(InfoPane model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
    }
    
    /**
     * Displays the image information.
     * 
     * @param details   The visualization map. 
     * @param name      The name of the <code>DataObjet</code>.
     */
    void displayDetails(Map details, String name)
    {
    	resetValues(details);
        if (details == null || details.size() == 0) {
            titleLabel.setText(DEFAULT_MSG);
            contentPanel.setVisible(false);
        } else {
        	contentPanel.setVisible(true);
            titleLabel.setText(EDIT_MSG+name);
        }
        validate();
        repaint();
    }
    
}

