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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExAnnotationVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExTitleVisitor;
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
    implements ActionListener
{

    public static final int         FOR_TITLE = 0;
    
    public static final int         FOR_ANNOTATION = 1;
    
    public static final Dimension   HBOX = new Dimension(10, 0);
    
    /** Action command ID for the Find button. */
    private static final int        FIND = 0;
    
    /** Action command ID for the Cancel button. */
    private static final int        CANCEL = 1;
    
    private static final String     TITLE = "Find in ";
    
    private static final String     CONTAIN = "contains: ";
    
    private static final int        TEXT_WIDTH = 15;
    
    private Browser     browser;
    
    private int         index;
    
    private JButton     find, cancel;
    
    private JTextField  regExField;
    
    /** Width of a character. */
    private int         txtWidth;
    
    public RegExFinder(int index, Browser browser, Frame owner)
    {
        super(owner);
        this.browser = browser;
        this.index = index;
        txtWidth = getFontMetrics(getFont()).charWidth('m');
        String title = getType();
        setTitle(TITLE+""+title);
        setModal(true);
        initComponents();
        buildUI(title+" "+CONTAIN);
    }
    
    String getType()
    {
        String s = "";
        switch (index) {
            case FOR_TITLE:
                s = "Title"; break;
            case FOR_ANNOTATION:
                s = "Annotation"; break;
        }
        return s;
    }
    
    /** Initializes the component. */
    private void initComponents()
    {
        regExField = new JTextField();
        find = new JButton("Find");
        find.setToolTipText(
                UIUtilities.formatToolTipText("Find the specified regular" +
                        "expression."));
        cancel = new JButton("Cancel");
        cancel.setToolTipText(
                UIUtilities.formatToolTipText("Close the window"));
        attachButtonListeners(find, FIND);
        attachButtonListeners(cancel, CANCEL);
    }
    
    /** Display the different filters in a JPanel. */
    private JPanel filtersPanel(String name)
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel(name);
        c.ipadx = DataManagerUIF.H_SPACE;
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
        return p;
    }
    
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

    /** Attach an {@link ActionListener} to an {@link JButton}. */
    private void attachButtonListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Find the specified regular expression. */
    private void findRegEx()
    {
        String regEx = regExField.getText();
        cancel();
        //Need to check the expression.
        ImageDisplayVisitor visitor = null;
        switch (index) {
            case FOR_TITLE:
                visitor = new FindRegExTitleVisitor(regEx);
                break;
            case FOR_ANNOTATION:
                visitor = new FindRegExAnnotationVisitor(regEx);
        }
        if (visitor != null) browser.accept(visitor);
    }
    
    /** Close and dispose. */
    private void cancel()
    {
        setVisible(false);
        dispose();
    }
    
    /** Handle event fired by JButton. */
    public void actionPerformed(ActionEvent e)
    {
        try {
            int index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case FIND:
                    findRegEx(); break;
                case CANCEL:
                    cancel();  
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
        } 
    }
    
}
