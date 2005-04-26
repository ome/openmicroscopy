/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.RegExFinder
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

package org.openmicroscopy.shoola.agents.hiviewer.view;




//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExCmd;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modal dialog. View and control are combined.
 * This class will be modified when we review the workflow.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class RegExFinder
    extends JDialog
{

    private static final String     TITLE = "Find in ";
    
    private static final String     CONTAIN = "contains: ";
    
    private static final int        TEXT_WIDTH = 15;
    
    /*
    private static final int        MAX_ID = 2;
    
    static final int                IMG_ONLY = 0;
    
    static final int                CONTAINER_ONLY = 1;
    
    static final int                BOTH = 2;
    */
    
    private static final Dimension  HBOX = new Dimension(10, 0);

    /** Horizontal space between the cells in the grid. */
    private static final int        H_SPACE = 10;
    
    //private static final String[]   selection;
    
    /** Width of a character. */
    private int         txtWidth;
    
    /** find and cancel button. */
    JButton             find, cancel;
    
    /** Textfield containing the regular expression. */
    JTextField          regExField;
    
    /** 
     * Levels of selection: i.e. at image's level, container's 
     * level or both.
     */
    //JComboBox           levels;
    
    /*
    static {
        selection = new String[MAX_ID+1];
        selection[IMG_ONLY] = "Images only";
        selection[CONTAINER_ONLY] = "Containers only";
        selection[BOTH] = "Both";
    }
    */
    
    /** Creates a new instance. */
    public RegExFinder(HiViewer model, int index)
    {
        super(model.getUI());
        initComponents();
        new RegExFinderMng(this, model, index);
        txtWidth = getFontMetrics(getFont()).charWidth('m');
        String title = getType(index);
        setTitle(TITLE+""+title);
        setModal(true);
        buildUI(title+" "+CONTAIN);
    }
    
    /** Returns the title according to the index. */
    private String getType(int index)
    {
        String s = "";
        switch (index) {
            case FindRegExCmd.IN_TITLE:
                s = "Title"; break;
            case FindRegExCmd.IN_ANNOTATION:
                s = "Annotation";
        }
        return s;
    }
    
    /** Initializes the component. */
    private void initComponents()
    {
        //levels = new JComboBox(selection);
        regExField = new JTextField();
        find = new JButton("Find");
        find.setToolTipText(
                UIUtilities.formatToolTipText("Find the specified regular" +
                        "expression."));
        cancel = new JButton("Cancel");
        cancel.setToolTipText(
                UIUtilities.formatToolTipText("Close the window"));
    }
    
    /** Display the different filters in a JPanel. */
    private JPanel filtersPanel(String name)
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel(name);
        c.ipadx = H_SPACE;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridx = 1;
        JPanel containPanel = regExPanel();
        gridbag.setConstraints(containPanel, c);
        p.add(containPanel);
        /**
        c.gridx = 0;
        c.gridy = 1;
        label = new JLabel("Check for:");
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridx = 1;
        containPanel = UIUtilities.buildComponentPanel(levels);
        gridbag.setConstraints(containPanel, c);
        p.add(containPanel);
        */
        return p;
    }
    
    /** Display the regExfield in a JPanel. */
    private JPanel regExPanel()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        Insets insets = regExField.getInsets();
        c.ipadx = insets.left+TEXT_WIDTH*txtWidth+insets.right;
        gridbag.setConstraints(regExField, c);
        p.add(regExField);
        return UIUtilities.buildComponentPanel(p);
    }
    
    /** Build the ToolBar Panel. */
    private JPanel barPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(find);
        p.add(Box.createRigidArea(HBOX));
        p.add(cancel);
        p.add(Box.createRigidArea(HBOX));
        p.setOpaque(false); //make panel transparent
        return UIUtilities.buildComponentPanelRight(p);
    }
    
    /** Build and lay out the main Panel. */
    private JPanel buildMain(String name)
    {
        JPanel p = new JPanel(), all = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        p.add(filtersPanel(name));
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.setBorder(BorderFactory.createEmptyBorder());
        all.add(p);
        all.add(barPanel());
        return all;
    }
    
    /** Build and lay out the GUI. */
    private void buildUI(String name)
    {
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(buildMain(name), BorderLayout.CENTER);
        pack();
    }
  
}
