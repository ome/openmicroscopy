/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor.PermissionPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.PermissionData;

/** 
 * 
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
class PermissionPane
    extends JPanel
{

    /** Text displaying before the owner's permissions. */
    private static final String    OWNER = "Owner: ";
    
    /** Text displaying before the group's permissions. */
    private static final String     GROUP = "Group: ";
    
    /** Text displaying before the world's permissions. */
    private static final String     WORLD = "Others: ";
    
    /** Text describing the <code>Read</code> permission. */
    private static final String     READ = "Read";
    
    /** Text describing the <code>Write</code> permission. */
    private static final String     WRITE = "Write";
    
    /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    private static final Dimension  SMALL_V_SPACER_SIZE = new Dimension(1, 6);
    
    /** Reference to the Model. */
    private EditorPane      model;
    
    /** Reference to the View. */
    private EditorPaneUI    view;
    
    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @param permissions   The permissions of the edited object.
     * @return See above.
     */
    private JPanel buildPermissions(final PermissionData permissions)
    {
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        //The owner is the only person allowed to modify the permissions.
        boolean isOwner = model.isObjectOwner();
        //Owner
        JLabel label = UIUtilities.setTextFont(OWNER);
        JPanel p = new JPanel();
        JCheckBox box =  new JCheckBox(READ);
        box.setSelected(permissions.isUserRead());
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserRead(source.isSelected());
               view.setEdit(true);
            }
        });
        box.setEnabled(isOwner);
        p.add(box);
        box =  new JCheckBox(WRITE);
        box.setSelected(permissions.isUserWrite());
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserWrite(source.isSelected());
               view.setEdit(true);
            }
        
        });
        box.setEnabled(isOwner);
        p.add(box);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        label.setLabelFor(p);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(UIUtilities.buildComponentPanel(p), c);  
        //Group
        label = UIUtilities.setTextFont(GROUP);
        p = new JPanel();
        box =  new JCheckBox(READ);
        box.setSelected(permissions.isGroupRead());
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupRead(source.isSelected());
               view.setEdit(true);
            }
        });
        box.setEnabled(isOwner);
        p.add(box);
        box =  new JCheckBox(WRITE);
        box.setSelected(permissions.isGroupWrite());
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupWrite(source.isSelected());
               view.setEdit(true);
            }
        
        });
        box.setEnabled(isOwner);
        p.add(box);
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        label.setLabelFor(p);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(UIUtilities.buildComponentPanel(p), c);  
        //OTHER
        label = UIUtilities.setTextFont(WORLD);
        p = new JPanel();
        box =  new JCheckBox(READ);
        box.setSelected(permissions.isWorldRead());
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldRead(source.isSelected());
               view.setEdit(true);
            }
        });
        box.setEnabled(isOwner);
        p.add(box);
        box =  new JCheckBox(WRITE);
        box.setSelected(permissions.isWorldWrite());
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldWrite(source.isSelected());
               view.setEdit(true);
            }
        
        });
        box.setEnabled(isOwner);
        p.add(box);
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        label.setLabelFor(p);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(UIUtilities.buildComponentPanel(p), c);  
        return content;
    }
    
    /**
     * Builds the panel hosting the information
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
        return content;
    }
    
    /** 
     * Builds and lays out the GUI.
     *
     * @param details       The visualization map.
     * @param permission    The object's permission.
     */
    private void buildGUI(Map details, PermissionData permission)
    {
        JPanel contentPanel = buildContentPanel(details);
        setLayout(new BorderLayout());
        setMaximumSize(contentPanel.getPreferredSize());
        setBorder(new EtchedBorder());
        add(contentPanel, BorderLayout.NORTH);
        if (permission != null) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            p.add(new JSeparator());
            p.add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
            p.add(buildPermissions(permission));
            p.add(Box.createVerticalGlue());
            add(p);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view          Reference to the View. Mustn't be <code>null</code>.
     * @param model         Reference to the Model. 
     *                      Mustn't be <code>null</code>.
     * @param details       The visualization map. Mustn't be <code>null</code>.

     * @param permission    Pass <code>true</code> to display the permission,
     *                      <code>false</code> otherwise.
     */
    PermissionPane(EditorPaneUI view, EditorPane model, Map details,
            PermissionData permission)
    {
        if (details == null) 
            throw new IllegalArgumentException("Visualization map cannot be" +
                    " null");
        if (view == null)
            throw new IllegalArgumentException("No view.");
        if (model == null)
            throw new IllegalArgumentException("No model.");
        this.model = model;
        buildGUI(details, permission);
    }
    
}
