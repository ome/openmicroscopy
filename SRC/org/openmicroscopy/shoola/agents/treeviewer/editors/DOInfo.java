/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOInfo
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.GroupData;

/** 
 * A UI component displaying owner's information, image's information etc.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DOInfo
    extends JPanel
{

    /** The text displayed before the group's details. */
    private static final String		GROUP_TEXT = "Group's information: ";
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    private static final Dimension  SMALL_V_SPACER_SIZE = new Dimension(1, 6);
    
    /** The panel hosting the group's details. */
    private JPanel groupPanel;
    
    /**
     * Builds the panel hosting the information
     * 
     * @param details The information to display.
     * @param groups Collection of groups. if <code>null</code>, ignore.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details, Set groups)
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
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        if (groups != null) {
           ++c.gridy;
           c.gridx = 0;
           JPanel bar = new JPanel();
           bar.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
           i = groups.iterator();
           JComponent component;
           while (i.hasNext()) {
               component = new GroupComponent((GroupData) i.next(), this);
               bar.add(component);
           }	
           label = UIUtilities.setTextFont(EditorUtil.GROUPS);
           c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
           c.fill = GridBagConstraints.NONE;      //reset to default
           c.weightx = 0.0;  
           content.add(label, c);
           label.setLabelFor(bar);
           c.gridx = 1;
           c.gridwidth = GridBagConstraints.REMAINDER;     //end row
           c.fill = GridBagConstraints.HORIZONTAL;
           c.weightx = 1.0;
           content.add(bar, c); 
       }
       return content;
    }
    
    /** 
     * Builds and lays out the GUI.
     *
     * @param details The visualization map.
     * @param groups	Collection of groups.
     */
    private void buildGUI(Map details, Set groups)
    {
        groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JPanel contentPanel = buildContentPanel(details, groups);
        setLayout(new BorderLayout());
        setMaximumSize(contentPanel.getPreferredSize());
        setBorder(new EtchedBorder());
        add(contentPanel, BorderLayout.NORTH);
        add(groupPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param details The visualization map. Mustn't be <code>null</code>.
     */
    DOInfo(Map details)
    {
        if (details == null) 
            throw new IllegalArgumentException("Visualization map cannot be" +
                    " null");
        buildGUI(details, null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param details 	The visualization map. Mustn't be <code>null</code>.
     * @param groups	Collection of groups.
     */
    /*
    DOInfo(Map details, Set groups)
    {
        if (details == null) 
            throw new IllegalArgumentException("Visualization map cannot be" +
                    " null");
        buildGUI(details, null);
    }
    */
    
    /**
     * Shows the specified group's details.
     * 
     * @param details The details to display.
     */
    void showGroupDetails(Map details)
    {
        groupPanel.removeAll();
        groupPanel.add(UIUtilities.buildComponentPanel(new JLabel(GROUP_TEXT)));
        groupPanel.add(new JSeparator());
        groupPanel.add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
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
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(content, BorderLayout.NORTH);
        groupPanel.add(p);
        validate();
        repaint();
    }

    /**
     * 
     */
    void hideGroupDetails()
    {
        groupPanel.removeAll();
        validate();
        repaint();
    }
    
}
