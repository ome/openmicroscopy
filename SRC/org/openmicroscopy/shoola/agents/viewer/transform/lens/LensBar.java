/*
 * org.openmicroscopy.shoola.agents.viewer.transform.lens.LensBar
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

package org.openmicroscopy.shoola.agents.viewer.transform.lens;


//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspector;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;
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
public class LensBar
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
    
    private static final String[]       selectionOfColors;

    static {        
        selectionOfColors = new String[MAX_COLOR+1];
        selectionOfColors[RED] = "Red";
        selectionOfColors[GREEN] = "Green";
        selectionOfColors[BLUE] = "Blue";
        selectionOfColors[CYAN] = "Cyan";
        selectionOfColors[MAGENTA] = "Magenta";
        selectionOfColors[ORANGE] = "Orange";
        selectionOfColors[PINK] = "Pink";
        selectionOfColors[YELLOW] = "Yellow";
    }

    private JTextField              magFactorField;
    
    /** zoom buttons. */                                                                                                                                                
    private JButton                 sizePlus, sizeMinus;
    
    private JButton                 magPlus, magMinus;;
    
    private JCheckBox               onOff, pin, painting;
    
    private JComboBox               colors;
    
    private LensBarMng              manager;
    
    private Registry                registry;
    
    public LensBar(Registry registry, ImageInspectorManager mng)
    {
        this.registry = registry;
        manager = new LensBarMng(this, mng);
        initComponents();
        manager.attachListeners();
        buildGUI();
    }
    
    Registry getRegistry() { return registry; }
    
    JTextField getMagFactorField() { return magFactorField; }
    
    JCheckBox getPainting() { return painting; }
    
    JCheckBox getOnOff() { return onOff; }
    
    JCheckBox getPin() { return pin; }
    
    JButton getMagPlus() { return magPlus; }
    
    JButton getMagMinus() { return magMinus; }
    
    JButton getSizePlus() { return sizePlus; }
    
    JButton getSizeMinus() { return sizeMinus; }
    
    JComboBox getColors() { return colors; }
    
    public LensBarMng getManager() { return manager; }
    
    /** Initialize the zoom components. */
    private void initComponents()
    {
        IconManager im = IconManager.getInstance(registry);
        magPlus = new JButton(im.getIcon(IconManager.ZOOMIN));
        magPlus.setToolTipText(
            UIUtilities.formatToolTipText("Zoom in.")); 
        magMinus = new JButton(im.getIcon(IconManager.ZOOMOUT));
        magMinus.setToolTipText(
            UIUtilities.formatToolTipText("Zoom out."));
         
        sizePlus = new JButton(im.getIcon(IconManager.PLUS));
        sizePlus.setToolTipText(
            UIUtilities.formatToolTipText("Increase the size of the lens.")); 
        sizeMinus = new JButton(im.getIcon(IconManager.MINUS));
        sizeMinus.setToolTipText(
            UIUtilities.formatToolTipText("Decrease the size of the lens."));
        
        onOff = new JCheckBox();
        onOff.setToolTipText(
                UIUtilities.formatToolTipText("Lens on or off."));
        onOff.setSelected(true);
        
        pin = new JCheckBox();
        pin.setToolTipText(
                UIUtilities.formatToolTipText("Pin the lens."));
        magFactorField = new JTextField(""+manager.getMagFactor(), 
                            (""+ImageInspector.MAX_ZOOM_LEVEL).length());
        magFactorField.setEnabled(false);
        
        painting = new JCheckBox();
        painting.setToolTipText(
                UIUtilities.formatToolTipText("Paint the border of the Lens."));
        painting.setSelected(false);
        
        colors = new JComboBox(selectionOfColors);
        colors.setToolTipText(
                UIUtilities.formatToolTipText("Pick a color to draw the lens'" +
                        " border."));
    }   
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(UIUtilities.buildComponentPanel(UIUtilities.setTextFont("Lens")));
        add(buildControls());
        add(buildBoxesPanel());
        add(buildPaintingPanel());
    }
    
    private JPanel buildBoxesPanel()
    {
        JPanel p = new JPanel(), bp = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        JLabel l = new JLabel(" Lens On/Off");
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.weightx = 0.5;
        c.gridx = 1;
        bp = UIUtilities.buildComponentPanel(onOff);
        gridbag.setConstraints(bp, c);
        p.add(bp);
        l = new JLabel(" Pin lens");
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        c.weightx = 0.5;
        bp = UIUtilities.buildComponentPanel(pin);
        gridbag.setConstraints(bp, c);
        p.add(bp);
        return p;
    }
    
    private JPanel buildPaintingPanel()
    {
        JPanel p = new JPanel(), bp = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        JLabel l = new JLabel(" Painting");
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 0;
        c.weightx = 0.5;
        c.gridx = 1;
        bp = UIUtilities.buildComponentPanel(painting);
        gridbag.setConstraints(bp, c);
        p.add(bp);
        l = new JLabel(" Color");
        c.weightx = 0;
        c.gridx = 2;
        //c.gridy = 1;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 3;
        c.weightx = 0.5;
        bp = UIUtilities.buildComponentPanel(colors);
        gridbag.setConstraints(bp, c);
        p.add(bp);
        return p;
    }
    
    private JPanel buildControls()
    {
        JPanel p = new JPanel(), bp = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        bp = buildButtonsPanel(sizePlus, sizeMinus, "Size: ");
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(bp, c);
        p.add(bp);
        c.gridx = 1;
        bp = buildButtonsPanel(magPlus, magMinus, "Mag: ");
        gridbag.setConstraints(bp, c);
        p.add(bp);
        c.gridy = 1;
        bp = buildMagPanel();
        gridbag.setConstraints(bp, c);
        p.add(bp);
        return p;
    }
    
    private JPanel buildMagPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(UIUtilities.buildComponentPanel(new JLabel("Factor: x")));
        p.add(UIUtilities.buildComponentPanel(magFactorField));
        return p;
    }
    
    private JPanel buildButtonsPanel(JButton plus, JButton minus, String txt)
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(UIUtilities.buildComponentPanel(new JLabel(txt)));
        p.add(buildToolBar(plus, minus));
        return p;
        
    }
    
    /** Build the toolBar. */
    private JToolBar buildToolBar(JButton plus, JButton minus) 
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(minus);
        bar.add(plus);
        return bar;
    }

}
