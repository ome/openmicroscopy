/*
 * org.openmicroscopy.shoola.agents.roi.pane.AssistantDialog
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

package org.openmicroscopy.shoola.agents.roi.pane;


//Java imports
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.defs.ScreenROI;
import org.openmicroscopy.shoola.util.ui.ReferenceFramePanel;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.RowHeaderRenderer;


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
public class AssistantDialog
    extends JDialog
{
    
    private static final String     MSG = "Click on cell, drag, release and " +
            "copy the selection if any drawn on the corresponding plane";
    
    private static final String     TITLE = "ROI Assistant, ROI #";
    
    public static final Color       DEFAULT_COLOR = Color.WHITE, 
                                    SELECTED_COLOR =  Color.BLUE,
                                    ALPHA_SELECTED = new Color(0, 0, 255, 100);
    public static final Color       POSITION_COLOR = Color.GRAY;

    public static final int         WIDTH_MIN = 20, WIDTH_MAX = 25, MAX = 100;

    private static final Dimension  VBOX = new Dimension(0, 10);

    private JScrollPane             scrollPane;

    JButton                         copyStack;

    JTextField                      startT, endT;

    JRadioButton                    allTimepoints, finalTimepoints;
    
    JCheckBox                       moveResizeBox;
    
    /** Width of a caracter. */
    private int                     txtWidth;

    ColoredCellTable                table;
    
    AssistantDialogMng              manager;
    
    public AssistantDialog(ROIAgtCtrl control, int numRows, int numColumns,
                           ScreenROI roi)
    {
        super(control.getReferenceFrame());
        manager = new AssistantDialogMng(this, control, numColumns-1);
        manager.setSelectedColor(roi.getAreaColor());
        initComponents(IconManager.getInstance(control.getRegistry()), numRows, 
                    numColumns, roi);
        buildComponent(roi);
        manager.attachListeners();
        manager.initDimensions();
        buildGUI((""+numRows).length());
        pack();
    }
    
    public void buildComponent(ScreenROI roi)
    {
        manager.setSelectedColor(roi.getAreaColor());
        table.buildTableData(roi.getLogicalROI());
        table.repaint();
        setTitle(TITLE+""+roi.getIndex());
    }
    
    /** Initializes the GUI components. */
    private void initComponents(IconManager im, int numRows, int numColumns, 
                                ScreenROI roi)
    {
        moveResizeBox = new JCheckBox("Apply op to the stack");
        moveResizeBox.setToolTipText(
                UIUtilities.formatToolTipText("Apply the Move/Resize action " +
                        "to all 2D-selections drawn on planes " +
                        "within the stack"));
        allTimepoints = new JRadioButton("Fill from start to end");
        finalTimepoints = new JRadioButton("Copy start to end timepoints");
        allTimepoints.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(allTimepoints);
        group.add(finalTimepoints);
        copyStack = new JButton(im.getIcon(IconManager.COPY_STACK));
        copyStack.setToolTipText(
                UIUtilities.formatToolTipText("Copy the selected stack " +
                        "across time."));
        //TextField
        startT = new JTextField(""+0, (""+numColumns).length());
        endT = new JTextField(""+(numColumns-1), (""+numColumns).length());
        initTxtWidth(startT);
        
        //Initialize the table
        table = new ColoredCellTable(numRows, numColumns, roi.getLogicalROI());
        JList rowHeader = new JList(new HeaderListModel(numRows));
        if (numRows > MAX) rowHeader.setFixedCellWidth(WIDTH_MAX);
        else rowHeader.setFixedCellWidth(WIDTH_MIN);
        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));
        scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(rowHeader);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(int l)
    {
        JPanel panel = new JPanel(); 
        panel.add(scrollPane);
        Container container = getContentPane();
        ReferenceFramePanel rfp = new ReferenceFramePanel("t", "z", "0");
        rfp.setBackground(Color.white);
        TitlePanel tp = new TitlePanel("ROI selection Assistant", MSG, rfp);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(tp);
        container.add(Box.createRigidArea(VBOX));
        container.add(scrollPane);
        container.add(Box.createRigidArea(VBOX));
        container.add(buildMain(l));
    }
    
    /** Build the main panel. */
    private JPanel buildMain(int l)
    {
        JPanel  p = new JPanel(), allControls = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(buildStack(l));
        p.add(Box.createRigidArea(ROIAgtUIF.VBOX));
        p.add(buildMoveResizePanel());
        allControls.setLayout(new FlowLayout(FlowLayout.LEFT));
        allControls.add(p);
        return allControls;
    }
    
    private JPanel buildMoveResizePanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(UIUtilities.setTextFont(" Move/Resize"));
        p.add(moveResizeBox);
        return UIUtilities.buildComponentPanel(p);
    }
    
    private JPanel buildStack(int l)
    {
        JPanel  p = new JPanel(), pStack = new JPanel(),
                radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(allTimepoints);
        radioPanel.add(finalTimepoints);
        pStack.setLayout(new BoxLayout(pStack, BoxLayout.X_AXIS));
        pStack.add(buildMinorBar());
        pStack.add(UIUtilities.buildComponentPanel(buildCopyStack(l)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(UIUtilities.buildComponentPanel(
                UIUtilities.setTextFont(" Copy stack")));
        p.add(pStack);
        p.add(Box.createRigidArea(ROIAgtUIF.VBOX));
        p.add(radioPanel);
        return p;
    }

    private JPanel buildCopyStack(int length)
    {
        JPanel p = new JPanel(), msp;
        JLabel l = new JLabel(" From t: ");
        Insets insets = endT.getInsets();
        int x = insets.left+length*txtWidth+insets.left;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.weightx = 0;   
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        c.ipadx = x;
        c.weightx = 0.5;
        msp = UIUtilities.buildComponentPanel(startT);
        gridbag.setConstraints(msp, c);
        p.add(msp);
        c.gridx = 2;
        c.ipadx = 0;
        c.weightx = 0;
        l = new JLabel(" To t:");
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 3;
        c.ipadx = x;
        c.weightx = 0.5;
        msp = UIUtilities.buildComponentPanel(endT);
        gridbag.setConstraints(msp, c);
        p.add(msp);
        return p; 
    }
    
    private JToolBar buildMinorBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(copyStack);
        return bar;
    }
    
    /** Initializes the width of the text. */
    private void initTxtWidth(JComponent c)
    {
        FontMetrics metrics = getFontMetrics(c.getFont());
        txtWidth = metrics.charWidth('m');
    }
    
}
