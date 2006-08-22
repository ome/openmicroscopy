/*
 * org.openmicroscopy.shoola.agents.roi.pane.PaintingControls
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.ROIFactory;
import org.openmicroscopy.shoola.util.ui.ButtonMenu;
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
public class PaintingControls
    extends JPanel
{
    
    /** Buttons to select the shape of the ROI. */
    JButton                             rectangle, ellipse;
    
    ButtonMenu                          eraseMenu;
    
    JCheckBox                           textOnOff;
    
    /** Border of the pressed button. */
    private Border                      pressedBorder;
    
    private PaintingControlsMng         manager;
     
    /** 
     * 
     * @param control   reference to the {@link ROIAgtCtrl control}.
     * @param registry  reference to the {@link Registry registry}.
     */
    public PaintingControls(ROIAgtCtrl control)
    {
        pressedBorder = BorderFactory.createLoweredBevelBorder();
        manager = new PaintingControlsMng(this, control);
        initButtons(IconManager.getInstance(control.getRegistry()));
        initBoxes();
        manager.attachListeners();
        buildGUI();
    }

    public void setBorderButtons(boolean b)
    {
        setButtonBorder(ellipse, b);
        setButtonBorder(rectangle, b);
    }
    
    public void paintButton(int type)
    {
        switch (type) {
            case ROIFactory.RECTANGLE:
                rectangle.setBorderPainted(true);
                ellipse.setBorderPainted(false);  
                break;
            case ROIFactory.ELLIPSE:
                rectangle.setBorderPainted(false);
                ellipse.setBorderPainted(true);
        }
    }
    
    public void paintButton(boolean b)
    {
        rectangle.setBorderPainted(b);
        ellipse.setBorderPainted(b);
    }
    
    /** Initialize channel ComboBox. */
    private void initBoxes()
    {
        textOnOff = new JCheckBox();
        textOnOff.setToolTipText(
                UIUtilities.formatToolTipText("Display the ROI #."));
        textOnOff.setSelected(false);
    }

    /** Initialize the buttons. */
    private void initButtons(IconManager im)
    {
        eraseMenu = new ButtonMenu(im.getIcon(IconManager.ERASE));
        eraseMenu.setToolTipText(
                UIUtilities.formatToolTipText("Erase shapes"));
        //Add items to the menu.
        JMenuItem item = new JMenuItem("Erase current shape");
        eraseMenu.addToMenu(item);
        manager.attachItemListener(item, PaintingControlsMng.ERASE);
        item = new JMenuItem("Erase all shapes");
        eraseMenu.addToMenu(item);
        manager.attachItemListener(item, PaintingControlsMng.ERASE_ALL);
        //item = new JMenuItem("Undo");
        //eraseMenu.addToMenu(item);
        //manager.attachItemListener(item, PaintingControlsMng.UNDO_ERASE);
        
        rectangle = new JButton(im.getIcon(IconManager.RECTANGLE));
        rectangle.setToolTipText(
                UIUtilities.formatToolTipText("Draw a rectangle."));
        setButtonBorder(rectangle, false);
        ellipse = new JButton(im.getIcon(IconManager.ELLIPSE));
        ellipse.setToolTipText(
                UIUtilities.formatToolTipText("Draw an ellipse."));
        setButtonBorder(ellipse, false);
        
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        add(UIUtilities.buildComponentPanel(
                UIUtilities.setTextFont(" Drawing context")));
        add(UIUtilities.buildComponentPanel(buildBar()));
        add(UIUtilities.buildComponentPanel(buildControlsPanel()));
    }

    /** Build panel with several controls. */
    public JPanel buildControlsPanel()
    {
        JPanel p = new JPanel(), bp = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(" Show label");
        bp = UIUtilities.buildComponentPanel(textOnOff);
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        return p;
    }
    
    /** Build a toolBar with buttons. */
    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(rectangle);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(ellipse);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(eraseMenu);
        return bar;
    }
    
    /** Set a LoweredBevelBorder but don't paint it. */
    private void setButtonBorder(JButton button, boolean painted) 
    {
        button.setBorder(pressedBorder);
        button.setBorderPainted(painted);
    }
    
}
