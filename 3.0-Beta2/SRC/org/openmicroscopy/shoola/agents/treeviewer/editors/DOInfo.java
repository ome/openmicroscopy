/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOInfo
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.BorderLayout;
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

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.PermissionData;

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

    /** Indicate that this component displays <code>Owner</code> details. */
    static final int               OWNER_TYPE = 0;
        
    /** 
     * Indicate that this component displays <code>Image Info</code> details. 
     */
    static final int               INFO_TYPE = 1;
    
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

    /** The panel hosting the content's details. */
    private JPanel          contentPanel;
    
    /** Reference to the Model. */
    private EditorModel     model;
    
    /** Reference to the Model. */
    private EditorUI        view;
    
    /** One of the constants defined by this class. */
    private int             infoType;
    
    /**
     * Controls if the specifed type is supported.
     * 
     * @param t The value to check.
     */
    private void checkType(int t)
    {
        switch (t) {
            case OWNER_TYPE:
            case INFO_TYPE:   
                infoType = t;
                return;
            default:
                throw new IllegalArgumentException("Type not supported");
        }
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
            //c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            area.setEnabled(false);
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
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @param permissions   The permissions of the edited object.
     * @return See above.
     */
    private JPanel buildPermissions(final PermissionData permissions)
    {
        JPanel content = new JPanel();
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED} }; //rows
        content.setLayout(new TableLayout(tl));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        //The owner is the only person allowed to modify the permissions.
        //boolean isOwner = model.isObjectOwner();
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
        //box.setEnabled(isOwner);
        box.setEnabled(false);
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
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 0, l, c");
        content.add(p, "1, 0, l, c");  
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
        //box.setEnabled(isOwner);
        box.setEnabled(false);
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
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 1, l, c");
        content.add(p, "1,1, l, c"); 
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
        //box.setEnabled(isOwner);
        box.setEnabled(false);
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
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 2, l, c");
        content.add(p, "1, 2, l, c"); 
        return content;
    }
    
    /** 
     * Builds and lays out the GUI.
     *
     * @param details       The visualization map.
     * @param permission    Pass <code>true</code> to display the permission,
     *                      <code>false</code> otherwise.
     */
    private void buildGUI(Map details, boolean permission)
    {
        contentPanel = buildContentPanel(details);
        //setBorder(new EtchedBorder());
        if (model.getObjectPermissions() != null && permission) {
        	double[][] tl = {{TableLayout.FILL}, //columns
        					{TableLayout.PREFERRED, TableLayout.PREFERRED} }; //rows
        	setLayout(new TableLayout(tl));
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            p.add(new JSeparator());
            p.add(Box.createRigidArea(EditorUI.SMALL_V_SPACER_SIZE));
            p.add(buildPermissions(model.getObjectPermissions()));
            p.add(Box.createVerticalGlue());
            
            add(contentPanel, "0, 0, f, t");
            add(p, "0, 1, f, t");
        } else {
        	double[][] tl = {{TableLayout.FILL}, {TableLayout.PREFERRED}}; 
        	setLayout(new TableLayout(tl));
        	add(contentPanel, "0, 0, f, t");
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
     * @param type          The component's type.
     */
    DOInfo(EditorUI view, EditorModel model, Map details, boolean permission, 
           int type)
    {
        if (view == null)
            throw new IllegalArgumentException("No model.");
        if (model == null)
            throw new IllegalArgumentException("No model.");
        if (details == null) 
            throw new IllegalArgumentException("Visualization map cannot be" +
                    " null");
        this.view = view;
        this.model = model;
        checkType(type);
        buildGUI(details, permission);
    }

    /**
     * Returns the type of component.
     * 
     * @return See above.
     */
    int getInfoType() { return infoType; }
    
    /**
     * Sets the image metadata.
     * 
     * @param details See above.
     */
    void setChannelsData(Map details)
    {
        if (details == null) 
            throw new IllegalArgumentException("Visualization map cannot be" +
                    " null");
        if (infoType != INFO_TYPE) return;
        remove(contentPanel);
        contentPanel = buildContentPanel(details);
        
        add(contentPanel, BorderLayout.NORTH);
        validate();
        repaint();
    }
    
}
