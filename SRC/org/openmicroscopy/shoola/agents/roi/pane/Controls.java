/*
 * org.openmicroscopy.shoola.agents.roi.pane.Controls
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.env.config.Registry;
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
public class Controls
    extends JPanel
{
    
    /** Selection of line color for the ROI. */
    static final int                    RED = 0;
    static final int                    GREEN = 1;
    static final int                    BLUE = 2;
    static final int                    CYAN = 3;
    static final int                    MAGENTA = 4;
    static final int                    ORANGE = 5;
    static final int                    PINK = 6;
    static final int                    YELLOW = 7;
    
    static final int                    MAX_COLOR = 7;
    
    private static final Dimension      BOX = new Dimension(10, 16), 
                                        HBOX = new Dimension(5, 0);
    
    /** Selection of colors. */
    private static final String[]       selection;
    
    private static final Color[]        colorSelection;
    static {
        selection = new String[MAX_COLOR+1];
        selection[RED] = "Red";
        selection[GREEN] = "Green";
        selection[BLUE] = "Blue";
        selection[CYAN] = "Cyan";
        selection[MAGENTA] = "Magenta";
        selection[ORANGE] = "Orange";
        selection[PINK] = "Pink";
        selection[YELLOW] = "Yellow";
        
        colorSelection = new Color[MAX_COLOR+1];
        colorSelection[RED] = Color.RED;
        colorSelection[GREEN] = Color.GREEN;
        colorSelection[BLUE] = Color.BLUE;
        colorSelection[CYAN] = Color.CYAN;
        colorSelection[MAGENTA] = Color.MAGENTA;
        colorSelection[ORANGE] = Color.ORANGE;
        colorSelection[PINK] = Color.PINK;
        colorSelection[YELLOW] = Color.YELLOW;
    }

    /** Buttons to select the shape of the ROI. */
    private JButton                     rectangle, ellipse;
    
    /** Handle the ROI shapes. */
    private JButton                     erase, eraseAll, moveROI, sizeROI, 
                                        undoErase;
    
    /** Analyse data. */
    private JButton                     analyse;
    
    /** Channels. */
    private JComboBox                   channels;
    
    /** Color selection. */
    private JComboBox                   colors;
    
    private JCheckBox                   drawOnOff;
    
    private JCheckBox                   textOnOff;
    
    /** Border of the pressed button. */
    private Border                      pressedBorder;
    
    private ControlsManager              manager;

    private ROIAgtCtrl                  control;
     
    /** 
     * 
     * @param control   reference to the {@link ROIAgtCtrl control}.
     * @param registry  reference to the {@link Registry registry}.
     */
    public Controls(ROIAgtCtrl control, Registry registry)
    {
        this.control = control;
        pressedBorder = BorderFactory.createLoweredBevelBorder();
        initButtons(IconManager.getInstance(registry));
        initBoxes();
        manager = new ControlsManager(this, control);
        buildGUI();
    }
      
    public JCheckBox getDrawOnOff() { return drawOnOff; }
    
    public JCheckBox getTextOnOff() { return textOnOff; }
    
    Color getColorSelected(int i) { return colorSelection[i]; }    
    
    JComboBox getColors() { return colors; }
    
    JComboBox getChannels() { return channels; }
    
    JButton getUndoErase() { return undoErase; }
    
    JButton getMoveROI() { return moveROI; }
    
    JButton getSizeROI() { return sizeROI; }
    
    JButton getEraseAll() { return eraseAll; }
    
    JButton getAnalyse() { return analyse; }
    
    JButton getEllipse() { return ellipse; }

    JButton getErase() { return erase; }
    
    JButton getRectangle() { return rectangle; }

    public ControlsManager getManager() { return manager; }

    /** Initialize channel ComboBox. */
    private void initBoxes()
    {
        channels = new JComboBox(control.getChannels());
        channels.setToolTipText(
                UIUtilities.formatToolTipText("Select a wavelength."));
        colors = new JComboBox(selection);
        colors.setToolTipText(
                UIUtilities.formatToolTipText("Pick a color for this ROI."));
        drawOnOff = new JCheckBox();
        drawOnOff.setToolTipText(
                UIUtilities.formatToolTipText("Display the selections."));
        drawOnOff.setSelected(true);
        
        textOnOff = new JCheckBox();
        textOnOff.setToolTipText(
                UIUtilities.formatToolTipText("Display the ROI #."));
        textOnOff.setSelected(false);
    }

    /** Initialize the buttons. */
    private void initButtons(IconManager im)
    {
        rectangle = new JButton(im.getIcon(IconManager.RECTANGLE));
        rectangle.setToolTipText(
                UIUtilities.formatToolTipText("Draw a rectangle."));
        setButtonBorder(rectangle, true);
        ellipse = new JButton(im.getIcon(IconManager.ELLIPSE));
        ellipse.setToolTipText(
                UIUtilities.formatToolTipText("Draw an ellipse."));
        setButtonBorder(ellipse, false);
        erase = new JButton(im.getIcon(IconManager.ERASE));
        erase.setToolTipText(
                UIUtilities.formatToolTipText("Erase the current shape."));
        setButtonBorder(erase, false);
        eraseAll = new JButton(im.getIcon(IconManager.ERASE_ALL));
        eraseAll.setToolTipText(
                UIUtilities.formatToolTipText("Erase all shapes."));
        setButtonBorder(eraseAll, false);
        analyse = new JButton(im.getIcon(IconManager.ANALYSE));
        analyse.setToolTipText(
                UIUtilities.formatToolTipText("Analyse data."));
        setButtonBorder(analyse, false);
        moveROI = new JButton(im.getIcon(IconManager.MOVE_ROI));
        moveROI.setToolTipText(
                UIUtilities.formatToolTipText("Move the selected selection."));
        setButtonBorder(moveROI, false);
        sizeROI = new JButton(im.getIcon(IconManager.SIZE_ROI));
        sizeROI.setToolTipText(
                UIUtilities.formatToolTipText("Resize the selected " +
                                                "selection."));
        setButtonBorder(sizeROI, false);
        undoErase = new JButton(im.getIcon(IconManager.UNDO_ERASE));
        undoErase.setToolTipText(
                UIUtilities.formatToolTipText("Restore the last erased " +
                                                "selection."));
        setButtonBorder(undoErase, false);
        
    }
    
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildComponentPanel(UIUtilities.setTextFont(" Drawing context")), 
                BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
    }
    
    /** Build and lay out the GUI. */
    private JPanel buildMain()
    {
        JPanel main = new JPanel(), all = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel p = buildControlsPanel();
        JToolBar bar = buildButtonsBar();
        main.setLayout(gridbag);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(bar, c);
        main.add(bar);
        c.gridy = 1;
        Component box = Box.createRigidArea(BOX);
        gridbag.setConstraints(box, c);
        add(box);
        c.gridy = 2;
        gridbag.setConstraints(p, c);
        main.add(p);
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.add(buildComponentPanel(main));
        all.add(new JSeparator(JSeparator.HORIZONTAL));
        return all;
    }

    /** Build panel with several controls. */
    public JPanel buildControlsPanel()
    {
        JPanel p = new JPanel(), bp = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        JLabel l = new JLabel(" Selection Color ");
        bp = buildComponentPanel(colors);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        c.gridx = 0;
        c.gridy = 1;
        l = new JLabel(" Channel ");
        bp = buildComponentPanel(channels);
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        c.gridx = 0;
        c.gridy = 2;
        l = new JLabel(" Draw ");
        bp = buildComponentPanel(drawOnOff);
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        c.gridx = 0;
        c.gridy = 3;
        l = new JLabel(" Label ");
        bp = buildComponentPanel(textOnOff);
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        return p;
    }
    
    /** Build a toolBar with buttons. */
    private JToolBar buildButtonsBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(rectangle);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(ellipse);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(moveROI);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(sizeROI);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(erase);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(eraseAll);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(undoErase);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(analyse);
        return bar;
    }
    
    /** Wrap a JComponent in a JPanel. */
    private JPanel buildComponentPanel(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(component);
        return p;
    }  
    
    /** Set a LoweredBevelBorder but don't paint it. */
    private void setButtonBorder(JButton button, boolean painted) 
    {
        button.setBorder(pressedBorder);
        button.setBorderPainted(painted);
    }
}
