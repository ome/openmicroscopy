/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.graphic.ContextDialog
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

package org.openmicroscopy.shoola.agents.roi.results.stats.graphic;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPane;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPaneMng;
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
public class ContextDialog
    extends JDialog
{
    
    static final int                NAME = 0, BOOLEAN = 1, BUTTON = 2;
    
    private static final String[]   list;
    
    JComboBox                       statObject;
    
    JRadioButton                    zAsAxis, tAsAxis;
    
    JButton                         show;
    
    SelectionPane                   zPane, tPane;
    
    static {
        list = new String[StatsResultsPane.MAX_ID-StatsResultsPane.MINUS+1];
        list[StatsResultsPane.MIN-StatsResultsPane.MINUS] = "Min";
        list[StatsResultsPane.MAX-StatsResultsPane.MINUS] = "Max";
        list[StatsResultsPane.MEAN-StatsResultsPane.MINUS] = "Mean";
        list[StatsResultsPane.STD-StatsResultsPane.MINUS] = "StD";
        list[StatsResultsPane.AREA-StatsResultsPane.MINUS] = "Area";
        list[StatsResultsPane.SUM-StatsResultsPane.MINUS] = "Sum";
    }
    
    public ContextDialog(StatsResultsPaneMng mng, List zSelected, List tSelected)
    {
        super(mng.getParent(), "Plot Results");
        initComponents(zSelected, tSelected, mng);
        new ContextDialogMng(this, mng);
        buildGUI(IconManager.getInstance(mng.getRegistry()));
        pack();
    }
    
    /** Initialize the components. */
    private void initComponents(List zSelected, List tSelected, 
                                StatsResultsPaneMng mng)
    {
        statObject = new JComboBox(list);
        zAsAxis = new JRadioButton("z-section as x-axis");
        tAsAxis = new JRadioButton("timepoint as x-axis");
        tAsAxis.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(zAsAxis);
        group.add(tAsAxis);
        IconManager im = IconManager.getInstance(mng.getRegistry());
        show = new JButton(im.getIcon(IconManager.ROISHEET));
        show.setToolTipText(UIUtilities.formatToolTipText("Display graphics"));
        zPane = new SelectionPane(zSelected, "Z", mng);
        tPane = new SelectionPane(tSelected, "T", mng);
    }
    
    private JPanel buildSelection()
    {
        JPanel all = new JPanel(), p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        all.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        c.gridy = 0;
        p = UIUtilities.buildComponentPanel(zAsAxis);
        gridbag.setConstraints(p, c);
        all.add(p);
        c.gridx = 1;
        p = UIUtilities.buildComponentPanel(tAsAxis);
        gridbag.setConstraints(p, c);
        all.add(p);
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(zPane, c);
        all.add(zPane);
        c.gridx = 1;
        gridbag.setConstraints(tPane, c);
        all.add(tPane);
        return all;
    }
    
    private JPanel buildTop()
    {
        JPanel p = new JPanel();
        JLabel label = new JLabel("Display: ");
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(UIUtilities.buildComponentPanel(label));
        p.add(UIUtilities.buildComponentPanel(statObject));
        return UIUtilities.buildComponentPanel(p);
    }
    
    private JPanel buildBottom() 
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(show);
        return p;
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(IconManager im)
    {
        TitlePanel tp = new TitlePanel("Plot Results", 
                "f:A->B where A subset of T or Z ", 
                im.getIcon(IconManager.GRAPHIC_BIG));
        Container contentPane = getContentPane();
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(buildTop());
        p.add(buildSelection());
        contentPane.add(tp, BorderLayout.NORTH);
        contentPane.add(p, BorderLayout.CENTER);
        contentPane.add(buildBottom(), BorderLayout.SOUTH);
    }
    
}
