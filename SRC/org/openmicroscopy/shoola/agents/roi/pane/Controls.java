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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.env.config.Registry;
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
    
    private ButtonMenu                  eraseMenu;
    
    /** Analyse data. */
    private JButton                     analyse;
    
    /** Channels. */
    private JComboBox                   channels;
    
    /** Color selection. */
    private JComboBox                   colors;
    
    private JCheckBox                   textOnOff, annotationOnOff;
    
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
        manager = new ControlsManager(this, control);
        initButtons(IconManager.getInstance(registry));
        initBoxes();
        manager.attachListeners();
        buildGUI();
    }
     
    public JCheckBox getTextOnOff() { return textOnOff; }
    
    public JCheckBox getAnnotationOnOff() { return annotationOnOff; }
    
    Color getColorSelected(int i) { return colorSelection[i]; }    
    
    JComboBox getColors() { return colors; }
    
    JComboBox getChannels() { return channels; }
    
    JButton getAnalyse() { return analyse; }
    
    JButton getEllipse() { return ellipse; }
    
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
                UIUtilities.formatToolTipText("Pick a color for the shape."));
        textOnOff = new JCheckBox();
        textOnOff.setToolTipText(
                UIUtilities.formatToolTipText("Display the ROI #."));
        textOnOff.setSelected(false);
        annotationOnOff = new JCheckBox();
        annotationOnOff.setToolTipText(
                UIUtilities.formatToolTipText("Display the annotated ROI."));
        annotationOnOff.setSelected(false);
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
        manager.attachItemListener(item, ControlsManager.ERASE);
        item = new JMenuItem("Erase all shapes");
        eraseMenu.addToMenu(item);
        manager.attachItemListener(item, ControlsManager.ERASE_ALL);
        item = new JMenuItem("Undo.");
        eraseMenu.addToMenu(item);
        manager.attachItemListener(item, ControlsManager.UNDO_ERASE);
        
        rectangle = new JButton(im.getIcon(IconManager.RECTANGLE));
        rectangle.setToolTipText(
                UIUtilities.formatToolTipText("Draw a rectangle."));
        setButtonBorder(rectangle, true);
        ellipse = new JButton(im.getIcon(IconManager.ELLIPSE));
        ellipse.setToolTipText(
                UIUtilities.formatToolTipText("Draw an ellipse."));
        setButtonBorder(ellipse, false);
        analyse = new JButton(im.getIcon(IconManager.ANALYSE));
        analyse.setToolTipText(
                UIUtilities.formatToolTipText("Analyse data."));
        setButtonBorder(analyse, false);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(UIUtilities.buildComponentPanel(
                UIUtilities.setTextFont(" Drawing context")), 
                BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
    }
    
    /** Build the main panel. */
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
        all.add(UIUtilities.buildComponentPanel(main));
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
        JLabel l = new JLabel(" Selection Color");
        bp = UIUtilities.buildComponentPanel(colors);
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
        l = new JLabel(" Channel");
        bp = UIUtilities.buildComponentPanel(channels);
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        c.gridx = 0;
        c.gridy = 2;
        l = new JLabel(" Label");
        bp = UIUtilities.buildComponentPanel(textOnOff);
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        c.gridx = 0;
        c.gridy = 3;
        l = new JLabel(" Annotate");
        bp = UIUtilities.buildComponentPanel(annotationOnOff);
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
        bar.add(eraseMenu);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(analyse);
        return bar;
    }
    
    /** Set a LoweredBevelBorder but don't paint it. */
    private void setButtonBorder(JButton button, boolean painted) 
    {
        button.setBorder(pressedBorder);
        button.setBorderPainted(painted);
    }
    
}
