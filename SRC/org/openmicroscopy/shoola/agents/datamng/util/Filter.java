/*
 * org.openmicroscopy.shoola.agents.datamng.util.Filter
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

package org.openmicroscopy.shoola.agents.datamng.util;




//Java imports
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
public class Filter
    extends JDialog
{

    static final int                LIMIT = 100, LIMIT_MAX = 1000;
    
    JButton                         applyButton, cancelButton;  
    
    JCheckBox                       annotation, limit;
    
    JTextField                      name, visibleItem, month, day, year;
    
    JComboBox                       dates_types, name_types;
    
    private DataManagerCtrl         agentCtrl;
    
    private int                     txtWidth;
    
    private static final String[]   dateSelection, nameSelection;
    
    private static final int        MAX_WITH = 15, EXTRA = 2;
    
    static final int                LESS = 0, GREATER = 1, MAX = 1;
    
    static final int                CONTAIN = 0, NOT_CONTAIN = 1;
    
    static {
        dateSelection = new String[MAX+1];
        dateSelection[LESS] = "<=";
        dateSelection[GREATER] = ">=";
        nameSelection = new String[2];
        nameSelection[CONTAIN] = "contains";
        nameSelection[NOT_CONTAIN] = "does not contain";
    }
    
    public Filter(DataManagerCtrl agentCtrl, ISelector selector)
    {
        super(agentCtrl.getReferenceFrame(), "Filters", true);
        txtWidth = getFontMetrics(getFont()).charWidth('m');
        this.agentCtrl = agentCtrl;
        initComponents();
        new FilterMng(this, selector, agentCtrl);
        buildGUI();
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
        applyButton = new JButton("Apply");
        applyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyButton.setToolTipText(
            UIUtilities.formatToolTipText("Apply filter."));
        cancelButton = new JButton("Cancel");
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.setToolTipText(
            UIUtilities.formatToolTipText("Cancel and close."));
        annotation = new JCheckBox();
        annotation.setSelected(false);
        limit = new JCheckBox("Limit visible items to");
        limit.setSelected(true);
        name = new JTextField();
        //Initializes the date textFields
        day = new JTextField();
        month = new JTextField();
        year = new JTextField(); 
        visibleItem = new JTextField(""+LIMIT);
        dates_types = new JComboBox(dateSelection);
        name_types = new JComboBox(nameSelection);
    }
    
    /** Display the different filters in a JPanel. */
    private JPanel filtersPanel()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel(" Name");
        c.ipadx = DataManagerUIF.H_SPACE;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridy = 1;
        label = new JLabel(" Date");
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridy = 2;
        label = new JLabel(" Annotated");
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridy = 3;
        c.gridy = 0;
        c.gridx = 1;
        JPanel containPanel = namePanel();
        gridbag.setConstraints(containPanel, c);
        p.add(containPanel);
        c.gridy = 1;
        containPanel = UIUtilities.buildComponentPanel(datePanel());
        gridbag.setConstraints(containPanel, c);
        p.add(containPanel);
        c.gridy = 2;
        containPanel = UIUtilities.buildComponentPanel(annotation);
        gridbag.setConstraints(containPanel, c);
        p.add(containPanel);
        return p;
    }
    
    /** Build and lay out the limit Panel. */
    private JPanel limitPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(UIUtilities.buildComponentPanel(limit));
        p.add(UIUtilities.buildComponentPanel(visibleItem));
        return UIUtilities.buildComponentPanel(p);
    }
    
    /** Build and lay out the namePanel. */
    private JPanel namePanel()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        JPanel contain = UIUtilities.buildComponentPanel(name_types);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(contain, c);
        p.add(contain);
        c.gridx = 1;
        Insets insets = name.getInsets();
        c.ipadx = insets.left+MAX_WITH*txtWidth+insets.right;
        gridbag.setConstraints(name, c);
        p.add(name);
        return UIUtilities.buildComponentPanel(p);
    }
    
    /** Build and lay out the Date panel. */
    private JPanel datePanel()
    {
        JPanel p = new JPanel();
        JLabel label = new JLabel("-");
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        JPanel contain = UIUtilities.buildComponentPanel(dates_types);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(contain, c);
        p.add(contain);
        c.gridx = 1;
        Insets insets = day.getInsets();
        c.ipadx = insets.left+txtWidth+insets.right;
        gridbag.setConstraints(day, c);
        p.add(day);
        c.gridx = 2;
        c.ipadx = 0;
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridx = 3;
        c.ipadx = insets.left+txtWidth+insets.right;
        gridbag.setConstraints(month, c);
        p.add(month);
        c.gridx = 4;
        c.ipadx = 0;
        label = new JLabel("-");
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridx = 5;
        c.ipadx = insets.left+EXTRA*txtWidth+insets.right;
        gridbag.setConstraints(year, c);
        p.add(year);
        c.gridx = 6;
        label = new JLabel("(dd-mm-yyyy)");
        gridbag.setConstraints(label, c);
        p.add(label);
        return UIUtilities.buildComponentPanel(p);
    }
    
    /** Build the ToolBar Panel. */
    private JPanel barPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(applyButton);
        p.add(Box.createRigidArea(DataManagerUIF.HBOX));
        p.add(cancelButton);
        p.add(Box.createRigidArea(DataManagerUIF.HBOX));
        p.setOpaque(false); //make panel transparent
        return UIUtilities.buildComponentPanelRight(p);
    }
    
    /** Build and lay out the main Panel. */
    private JPanel buildMain()
    {
        JPanel p = new JPanel(), all = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        p.add(limitPanel());
        p.add(filtersPanel());
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.setBorder(BorderFactory.createEmptyBorder());
        all.add(p);
        all.add(barPanel());
        return all;
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        IconManager im = IconManager.getInstance(agentCtrl.getRegistry());
        TitlePanel tp = new TitlePanel(" Filters", " Apply filters.", 
                            im.getIcon(IconManager.FILTER_BIG));
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(tp, BorderLayout.NORTH);
        getContentPane().add(buildMain(), BorderLayout.CENTER);
        setSize(DataManagerUIF.ADD_WIN_WIDTH, DataManagerUIF.ADD_WIN_HEIGHT);
    }
    
}
