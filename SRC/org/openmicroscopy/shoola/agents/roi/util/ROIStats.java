/*
 * org.openmicroscopy.shoola.agents.roi.pane.ROIStats
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

package org.openmicroscopy.shoola.agents.roi.util;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.env.config.Registry;
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
public class ROIStats
    extends JDialog
{
    
    private static final Dimension  HBOX = new Dimension(10, 0),
                                    VIEWPORT = new Dimension(450, 200);
  
    private JButton                 close, save;
    
    private StatsTableModel         model;
    
    private ROIStatsMng             manager;
    
    public ROIStats(ROIAgtCtrl control, Registry reg)
    {
        super(control.getReferenceFrame(), "ROI Results");
        manager = new ROIStatsMng(this, control);
        IconManager im = IconManager.getInstance(reg);
        init(im);
        manager.attachListeners();
        buildGUI(im);
        pack();  
    }
    
    JButton getClose() { return close; }
    
    JButton getSave() { return save; }
    
    AbstractTableModel getModel() { return model; }
    
    private void init(IconManager im)
    {
        close = new JButton(im.getIcon(IconManager.CLOSE));
        save = new JButton(im.getIcon(IconManager.SAVE));
        close.setToolTipText(
                UIUtilities.formatToolTipText("Close the window."));
        save.setToolTipText(
                UIUtilities.formatToolTipText("Save the result as a " +
                                            "text file."));
    }
    
    /** Build toolBar with JButtons. */
    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(save);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(close);
        return bar;
    }

    private JPanel buildResultsTable()
    {
        JPanel result = new JPanel();
        model = new StatsTableModel(manager.getROIStats());
        JTable  t = new JTable(model);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //Wrap table in a scroll pane and add it to the panel
        JScrollPane sp = new JScrollPane(t);
        t.setPreferredScrollableViewportSize(VIEWPORT);
        result.add(sp);
        return result;
    }
    
    private void buildGUI(IconManager im)
    {
        TitlePanel tp = new TitlePanel("ROI stats", 
                                "Result of the ROI analysis.", 
                                im.getIcon(IconManager.ROISHEET_BIG));
        //set layout and add components
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(tp, BorderLayout.NORTH);
        getContentPane().add(buildResultsTable(), BorderLayout.CENTER);
        getContentPane().add(buildBar(), BorderLayout.SOUTH);
    }

}
