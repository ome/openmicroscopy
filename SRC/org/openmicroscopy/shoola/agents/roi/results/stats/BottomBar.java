/*
 * org.openmicroscopy.shoola.agents.roi.results.pane.BottomBar
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

package org.openmicroscopy.shoola.agents.roi.results.stats;

//Java imports
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.env.config.Registry;
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
class BottomBar
    extends JPanel
{
    
    JButton         save, graphic, back, forward, background, ratio, 
                    initial;
    
    BottomBar(StatsResultsPaneMng mng, Registry reg)
    {
        initComponents(IconManager.getInstance(reg));
        new BottomBarMng(this, mng);
        buildGUI();
    }
    
    void setEnabledButtons(boolean b)
    {
        graphic.setEnabled(b);
        forward.setEnabled(b);
        back.setEnabled(!b);
    }
    
    void setEnabledMoveButtons(boolean b)
    {
        forward.setEnabled(b);
        back.setEnabled(b);
    }
    
    /** Initializes the components. */
    void initComponents(IconManager im)
    {
        save = new JButton(im.getIcon(IconManager.SAVE));
        save.setToolTipText(
                UIUtilities.formatToolTipText("Save the result as a " +
                                            "Text file."));
        graphic = new JButton(im.getIcon(IconManager.GRAPHIC));
        graphic.setToolTipText(
                UIUtilities.formatToolTipText("Present results in a " +
                        "grapichal form."));
        back = new JButton(im.getIcon(IconManager.BACK));
        back.setToolTipText(
        UIUtilities.formatToolTipText("Back to the table."));
        back.setEnabled(false);
        forward = new JButton(im.getIcon(IconManager.FORWARD));
        forward.setToolTipText(
                UIUtilities.formatToolTipText("Back to the graphic."));
        forward.setEnabled(false);
        ratio = new JButton(im.getIcon(IconManager.RATIO));
        ratio.setToolTipText(
                UIUtilities.formatToolTipText("Calculate the ratio " +
                        "of the results of 2 different ROIs."));
        background = new JButton(im.getIcon(IconManager.BACKGROUND));
        background.setToolTipText(
                UIUtilities.formatToolTipText("Substract background."));
        initial = new JButton(im.getIcon(IconManager.INITIAL));
        initial.setToolTipText(
                UIUtilities.formatToolTipText("Display the original results."));
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        JPanel p = new JPanel();
        p.add(buildBar());
        add(p); 
    }
    
    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(initial);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(background);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(ratio);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(graphic);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(back);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(forward);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(save);
        return bar;
    }
    
}
