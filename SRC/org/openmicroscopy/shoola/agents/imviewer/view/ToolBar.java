/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ToolBar
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

package org.openmicroscopy.shoola.agents.imviewer.view;




//Java imports
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * Presents the variable controls of the viewer.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ToolBar
    extends JPanel
    implements ActionListener, FocusListener
{

    /** Lenght of the z-section selection text field. */
    private static final String     MAX_LENGTH_Z = "99";
    
    /** Lenght of the timepoint selection text field. */
    private static final String     MAX_LENGTH_T = "100";
    
    /** Action command ID to indicate that a new z-section is selected.*/
    private static final int        Z_SELECTED = 0;
    
    /** Action command ID to indicate that a new timepoint is selected.*/
    private static final int        T_SELECTED = 1;
    
    /** Reference to the control. */
    private ImViewerControl controller;
    
    /** Reference to the model. */
    private ImViewerModel   model;
    
    /** The tool bar hosting the controls. */
    private JToolBar        bar;
    
    /** The z-section selection field. */
    private JTextField      zSelectionField;
    
    /** The timepoint selection field. */
    private JTextField      tSelectionField;
    
    /** Label displaying the number of z-sections. */
    private JLabel          zLabel;
    
    /** Label displaying the number of timepoints. */
    private JLabel          tLabel;
    
    /** The currently selected z-section. */
    private int             currentZ;
    
    /** The currently selected timepoint. */
    private int             currentT;
    
    /** Width of a character w.r.t. the font metric. */
    private int             charWidth;
    
    /**
     * Helper method to create a {@link JButton} with an icon and an action.
     * 
     * @param icon      The icon associated.
     * @param action    The action associated.
     * @return See above.
     */
    private JButton createButton(Icon icon, Action action)
    {
        JButton button = new JButton(icon);
        button.setAction(action);
        return button;
    }
    
    /** Helper method to create the tool bar hosting the buttons. */
    private void createControlsBar()
    {
        bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        IconManager im = IconManager.getInstance();
        JButton button =  createButton(im.getIcon(IconManager.RENDERER), 
                            controller.getAction(ImViewerControl.RENDERER));
        bar.add(button);
        button =  createButton(im.getIcon(IconManager.MOVIE), 
                controller.getAction(ImViewerControl.MOVIE));
        bar.add(button);    
        button =  createButton(im.getIcon(IconManager.LENS), 
                controller.getAction(ImViewerControl.LENS));
        bar.add(button);  
        bar.add(new JSeparator(SwingConstants.VERTICAL));
        button =  createButton(im.getIcon(IconManager.SAVE), 
                controller.getAction(ImViewerControl.SAVE));
        bar.add(button);  
    }
    
    /** Initializes the components composing this tool bar. */
    private void initComponents()
    {
        createControlsBar();
        zSelectionField = new JTextField();
        zSelectionField.setEditable(false);
        tSelectionField = new JTextField();
        tSelectionField.setEditable(false);
        zLabel = new JLabel();
        tLabel = new JLabel();
    }
    
    /**
     * Initializes the value of the fields displaying the currently selected
     * z-section and timepoint.
     */
    private void initializeValues()
    {
        int maxZ = model.getMaxZ();
        int maxT = model.getMaxT();
        currentZ = model.getDefaultZ();
        currentT = model.getDefaultT();
        zSelectionField.setColumns((""+maxZ).length());
        zSelectionField.setText(""+currentZ);
        tSelectionField.setColumns((""+maxT).length());
        tSelectionField.setText(""+currentT);
        zLabel.setText("/"+maxZ);
        tLabel.setText("/"+maxT);
        zSelectionField.setActionCommand(""+Z_SELECTED);  
        zSelectionField.addActionListener(this);
        zSelectionField.addFocusListener(this);
        tSelectionField.setActionCommand(""+T_SELECTED);  
        tSelectionField.addActionListener(this);
        tSelectionField.addFocusListener(this);
    }
    
    
    /** 
     * Handles the action event fired by the timepoint text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid timepoint, then we simply 
     * suggest the user to enter a valid one.
     */
    private void tSelectionHandler()
    {
        boolean valid = false;
        int val = 0;
        try {
            val = Integer.parseInt(tSelectionField.getText());
            if (0 <= val && val <= model.getMaxT()) valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            currentT = val;
            controller.setSelectedXYPlane(currentZ, currentT);
        } else {
            tSelectionField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid timepoint", 
                "Please enter a timepoint between 0 and "+model.getMaxT());
        }
    }
    
    /** 
     * Handles the action event fired by the z-section text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid z-section, then we simply 
     * suggest the user to enter a valid one.
     */
    private void zSelectionHandler()
    {
        boolean valid = false;
        int val = 0;
        try {
            val = Integer.parseInt(zSelectionField.getText());
            if (0 <= val && val <= model.getMaxZ()) valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            currentZ = val;
            controller.setSelectedXYPlane(currentZ, currentT);
        } else {
            zSelectionField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid z-section", 
                "Please enter a z-section between 0 and "+model.getMaxZ());
        }
    }
    
    /**
     * Builds and lays out a panel with the fields used to select a z-section.
     * 
     * @return See above.
     */
    private JPanel buildFieldsComponent()
    {
        JPanel fields = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        fields.setLayout(gridbag);
        
        JLabel label = new JLabel(" Z ");
        gridbag.setConstraints(label, c);
        fields.add(label);
        c.gridx = 1;
        //ZSelection field.
        Insets insets = zSelectionField.getInsets();
        c.ipadx = insets.left+MAX_LENGTH_Z.length()*charWidth+insets.right;
        gridbag.setConstraints(zSelectionField, c);
        fields.add(zSelectionField);
        c.gridx = 2;
        gridbag.setConstraints(zLabel, c);
        fields.add(zLabel);
        c.gridx = 3;
        c.ipadx = 0;
        label = new JLabel(" T ");
        gridbag.setConstraints(label, c);
        fields.add(label);
        c.gridx = 4;
        insets = tSelectionField.getInsets();
        c.ipadx = insets.left+MAX_LENGTH_T.length()*charWidth+insets.right;
        gridbag.setConstraints(tSelectionField, c);
        fields.add(tSelectionField);
        c.gridx = 5;
        gridbag.setConstraints(tLabel, c);
        fields.add(tLabel);
        return fields;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(bar);
        add(buildFieldsComponent());
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param controller    Reference to the control. 
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the model.
     *                      Mustn't be <code>null</code>.
     */
    ToolBar(ImViewerControl controller, ImViewerModel model)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.controller = controller;    
        this.model = model;
        charWidth = getFontMetrics(getFont()).charWidth('m');
        initComponents();
    }
    
    /** 
     * This method should be called straight after the metadata and the
     * rendering settings are loaded.
     */
    void buildComponent()
    {
        initializeValues();
        buildGUI();
    }
    
    /**
     * Updates UI components when a new z-section is selected.
     * 
     * @param z The selected z-section.
     */
    void setZSection(int z) { zSelectionField.setText(""+z); }
    
    /**
     * Updates UI components when a new timepoint is selected.
     * 
     * @param t The selected timepoint.
     */
    void setTimepoint(int t) { tSelectionField.setText(""+t); }
    
    /** 
     * Reacts to {@link ImViewer} change events.
     * 
     * @param b Pass <code>true</code> to enable the UI components, 
     *          <code>false</code> otherwise.
     */
    void onStateChange(boolean b)
    {
        if (b) {
            tSelectionField.setEditable(model.getMaxT() != 0);
            tSelectionField.setEditable(model.getMaxZ() != 0);
        } else {
            tSelectionField.setEditable(b);
            zSelectionField.setEditable(b);
        }
    }
    
    /**
     * Displays a new XY-plane when a new timepoint or z-section is selected.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case T_SELECTED:
                    tSelectionHandler(); break;
                case Z_SELECTED:
                    zSelectionHandler(); break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }

    /** 
     * Handles the lost of focus on the timepoint text field and z-section
     * text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * timepoint.
     * @see FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent e)
    {
        String tVal = tSelectionField.getText(), t = ""+currentT;
        String zVal = zSelectionField.getText(), z = ""+currentZ;
        if (tVal == null || !tVal.equals(t)) tSelectionField.setText(t);
        if (zVal == null || !zVal.equals(z)) zSelectionField.setText(z);
    }
    
    /** 
     * Required by the {@link FocusListener} I/F but not actually needed 
     * in our case, no op implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
    
}
