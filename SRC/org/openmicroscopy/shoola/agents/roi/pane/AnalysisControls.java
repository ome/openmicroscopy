/*
 * org.openmicroscopy.shoola.agents.roi.pane.AnalysisControls
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.util.ui.ExtendedDefaultListModel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AnalysisControls
    extends JPanel
{
    
    static final int                WIDTH_MIN = 20, WIDTH_MAX = 25, MAX = 100;
    
    static final Color              DEFAULT_COLOR = Color.WHITE, 
                                    SELECTED_COLOR =  Color.BLUE;
    static final Color              POSITION_COLOR = Color.GRAY;
    
    private static final Dimension  MAX_SCROLL = new Dimension(80, 40), 
                                    MIN_SCROLL = new Dimension(60, 20),
                                    VBOX = new Dimension(0, 5);
    
    private Dimension               dimScroll;

    JButton                         analyseStats;
    
    JList                           listChannels, listROI;
    
    private AnalysisControlsMng     manager;
    
    public AnalysisControls(ROIAgtCtrl control, String[] data)
    {
        manager = new AnalysisControlsMng(this, control);
        dimScroll = MAX_SCROLL;
        initComponents(control, data);
        manager.attachListeners();
        buildGUI();
    }

    public void addROI5D(int index)
    {
        DefaultListModel model = (DefaultListModel) listROI.getModel();
        model.addElement(new String("#"+index));
    }
    
    public void removeROI5D(int index)
    {
        DefaultListModel model = (DefaultListModel) listROI.getModel();
        model.remove(index);
    }
    
    public AnalysisControlsMng getManager() { return manager; }
    
    /** Initializes the slider. */
    private void initComponents(ROIAgtCtrl control, String[] data)
    {
        if (control.getChannels().length < 2) dimScroll = MIN_SCROLL;
        listChannels = new JList(control.getChannels());
        listChannels.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listChannels.setLayoutOrientation(JList.VERTICAL);
        IconManager im = IconManager.getInstance(control.getRegistry());
        analyseStats = new JButton(im.getIcon(IconManager.ANALYSE));
        listROI = new JList(new ExtendedDefaultListModel(data));
        listROI.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listROI.setLayoutOrientation(JList.VERTICAL);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        add(UIUtilities.buildComponentPanel(
                UIUtilities.setTextFont(" Analysis context")));
        add(UIUtilities.buildComponentPanel(buildBar()));
        add(buildListPanel());
    }
    
    /** Build the selection panel. */
    private JPanel buildListPanel()
    {
        JPanel p = new JPanel();
        JLabel label = new JLabel("Select channels ");
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.weightx = 0.5;        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        gridbag.setConstraints(label, c);
        p.add(label);
        JScrollPane scrollPane = new JScrollPane(listChannels);
        scrollPane.setPreferredSize(dimScroll);
        c.gridx = 1;
        gridbag.setConstraints(scrollPane, c);
        p.add(scrollPane);
        c.gridy = 1;
        Component box = Box.createRigidArea(VBOX);
        gridbag.setConstraints(box, c);
        p.add(box);
        label = new JLabel("Select ROI ");
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(label, c);
        p.add(label);
        scrollPane = new JScrollPane(listROI);
        scrollPane.setPreferredSize(dimScroll);
        c.gridx = 1;
        gridbag.setConstraints(scrollPane, c);
        p.add(scrollPane);
        return UIUtilities.buildComponentPanel(p);
    }   
    
    /** Build a toolBar with buttons. */
    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(analyseStats);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        return bar;
    } 
    
}



