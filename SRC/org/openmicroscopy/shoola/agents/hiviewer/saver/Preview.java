/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.Preview
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

package org.openmicroscopy.shoola.agents.hiviewer.saver;



//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
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
class Preview
    extends JDialog
{
    
    private static final Dimension  HBOX = new Dimension(5, 0);
    
    private static final String[]   gaps, bgColors;
    
    private static final HashMap    colorsMap;      
    
    
    private static final int        WHITE = 0;
    private static final int        BLACK = 1;
    private static final int        DARK_GRAY = 2;
    private static final int        GRAY = 3;
    private static final int        LIGHT_GRAY = 4;
    private static final int        MAX = 4;

    private static final int        GAP_TWO = 0;
    private static final int        GAP_FOUR = 1;
    private static final int        GAP_SIX = 2;
    private static final int        GAP_HEIGHT = 3;
    private static final int        GAP_TEN = 4;
    private static final int        GAP_MAX = 4;
    
    static {
        //Background colors
        bgColors = new String[MAX+1];
        bgColors[WHITE] = "White";
        bgColors[BLACK] = "Black";
        bgColors[DARK_GRAY] = "Dark gray";
        bgColors[GRAY] = "Gray";
        bgColors[LIGHT_GRAY] = "Light gray";
        colorsMap = new HashMap();
        colorsMap.put(new Integer(BLACK), Color.BLACK);
        colorsMap.put(new Integer(WHITE), Color.WHITE);
        colorsMap.put(new Integer(DARK_GRAY), Color.DARK_GRAY);
        colorsMap.put(new Integer(GRAY), Color.GRAY);
        colorsMap.put(new Integer(LIGHT_GRAY), Color.LIGHT_GRAY);
        //gaps between images
        gaps = new String[GAP_MAX+1];
        gaps[GAP_TWO] = "2";
        gaps[GAP_FOUR] = "4";
        gaps[GAP_SIX] = "6";
        gaps[GAP_HEIGHT] = "8";
        gaps[GAP_TEN] = "10"; 
    }
    
    private PreviewMng  manager;
    
    PreviewCanvas       previewCanvas;
    
    JButton             preview, save, cancel;
    
    JComboBox           colors, spacing;
    
    
    Preview(ContainerSaver model)
    {
        super(model, "Preview", true);
        initComponents();
        manager = new PreviewMng(this, model);
        previewCanvas.setImage(manager.getImage());
        buildGUI();
    }
  
    /** Initializes the UI components. */
    private void initComponents()
    {
        colors = new JComboBox(bgColors);
        spacing = new JComboBox(gaps);
        previewCanvas = new PreviewCanvas();
        save = new JButton("Save");
        save.setToolTipText(
            UIUtilities.formatToolTipText("Save the preview image."));
        cancel = new JButton("Cancel");
        cancel.setToolTipText(
            UIUtilities.formatToolTipText("Don't save the preview image."));
        preview = new JButton("Preview");
        preview.setToolTipText(
                UIUtilities.formatToolTipText("Preview the modified image."));
    }

    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        IconManager im = IconManager.getInstance();
        TitlePanel tp = new TitlePanel("Preview", "Preview...", 
                im.getIcon(IconManager.SAVE_AS_BIG));
        JScrollPane pane = new JScrollPane(previewCanvas);
        Container c = getContentPane();
        c.add(tp, BorderLayout.NORTH);
        c.add(pane, BorderLayout.CENTER);
        c.add(buildControlsPanel(), BorderLayout.EAST);
        c.add(buildToolBar(), BorderLayout.SOUTH);
    }
    
    /** Builds the panel containing the preview controls. */
    private JPanel buildControlsPanel()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.anchor = GridBagConstraints.NORTHWEST;
        Component cp = Box.createRigidArea(new Dimension(10, 0));
        gridbag.setConstraints(cp, c);
        p.add(cp);
        JLabel label = new JLabel("Background");
        c.gridx = 1;
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridy = 1;
        gridbag.setConstraints(colors, c);
        p.add(colors);
        c.gridy = 2;
        label = new JLabel("Spacing");
        gridbag.setConstraints(label, c);
        p.add(label);
        c.gridy = 3;
        gridbag.setConstraints(spacing, c);
        p.add(spacing);
        //finalP.
        return p;
    }
    
    /** Builds the toolBar. */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.add(UIUtilities.buildComponentPanelRight(buildButtonPanel()));
        return bar;
    }
    
    /** Build panel with buttons. */
    private JPanel buildButtonPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(preview);
        p.add(Box.createRigidArea(HBOX));
        p.add(save);
        p.add(Box.createRigidArea(HBOX));
        p.add(cancel);
        p.setOpaque(false); //make panel transparent
        return p;
    }
    
    Color getSelectedColor(int index)
    {
        if (index < 0 || index > colorsMap.size()) return Color.WHITE;
        return (Color) colorsMap.get(new Integer(index));
    }
    
    /** Close the preview widget. */
    void closeWindow()
    {
        setVisible(false);
        dispose();
    }
    
}
