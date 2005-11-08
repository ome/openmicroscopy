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
import java.awt.image.BufferedImage;

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
    
    /** Dimension of the box put between buttons. */
    private static final Dimension  HBOX = new Dimension(5, 0);
    
    /** Dimension of the box put between buttons. */
    private static final Dimension  RIGID_HBOX = new Dimension(10, 0);
    
    private static final String[]   gaps, bgColors;
    
    /** Identifies the <i>White</i> color for the background. */
    private static final int        WHITE = 0;
    
    /** Identifies the <i>Black</i> color for the background. */
    private static final int        BLACK = 1;
    
    /** Identifies the <i>Dark gray</i> color for the background. */
    private static final int        DARK_GRAY = 2;
    
    /** Identifies the <i>Gray</i> color for the background. */
    private static final int        GRAY = 3;
    
    /** Identifies the <i>Ligh gray</i> color for the background. */
    private static final int        LIGHT_GRAY = 4;
    
    /** The number of supported colors for the background. */
    private static final int        MAX = 4;

    /** Identifies the gap of <i>2 pixels</i> put between thumbnails. */
    private static final int        GAP_TWO = 0;
    
    /** Identifies the gap of <i>4 pixels</i> put between thumbnails. */
    private static final int        GAP_FOUR = 1;
    
    /** Identifies the gap of <i>6 pixels</i> put between thumbnails. */
    private static final int        GAP_SIX = 2;
    
    /** Identifies the gap of <i>8 pixels</i> put between thumbnails. */
    private static final int        GAP_HEIGHT = 3;
    
    /** Identifies the gap of <i>10 pixels</i> put between thumbnails. */
    private static final int        GAP_TEN = 4;
    
    /** The number of supported gaps. */
    private static final int        GAP_MAX = 4;
    
    static {
        //Background colors
        bgColors = new String[MAX+1];
        bgColors[WHITE] = "White";
        bgColors[BLACK] = "Black";
        bgColors[DARK_GRAY] = "Dark gray";
        bgColors[GRAY] = "Gray";
        bgColors[LIGHT_GRAY] = "Light gray";
        //gaps between images
        gaps = new String[GAP_MAX+1];
        gaps[GAP_TWO] = "2";
        gaps[GAP_FOUR] = "4";
        gaps[GAP_SIX] = "6";
        gaps[GAP_HEIGHT] = "8";
        gaps[GAP_TEN] = "10"; 
    }
    
    /** Reference to this class' manager. */
    private PreviewMng  manager;
    
    /** The canvas hosting the previewed image. */
    PreviewCanvas       previewCanvas;
    
    /** The <i>Preview</i> button. */
    JButton             preview;
    
    /** The <i>Save</i> button. */
    JButton             save;
    
    /** The <i>Cancel</i> button. */
    JButton             cancel;
    
    /** The <i>colors</i> selection box. */
    JComboBox           colors;
    
    /** The <i>gaps</i> selection box. */
    JComboBox           spacing;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        colors = new JComboBox(bgColors);
        spacing = new JComboBox(gaps);
        previewCanvas = new PreviewCanvas(this);
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

    /** Builds and lays out the GUI. */
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
        Component cp = Box.createRigidArea(RIGID_HBOX);
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
    
    /** Builds  a panel with buttons. */
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
    
    /** 
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    Preview(ContainerSaver model)
    {
        super(model, "Preview", true);
        initComponents();
        manager = new PreviewMng(this, model);
        previewCanvas.paintImage();
        buildGUI();
    }

    /**
     * Returns the color associated to the passed index.
     * 
     * @param index The passed index.
     * @return See above.
     */
    Color getSelectedColor(int index)
    {
        switch (index) {
            case BLACK: return Color.BLACK;
            case DARK_GRAY: return Color.DARK_GRAY;
            case GRAY: return Color.GRAY;
            case LIGHT_GRAY: return Color.LIGHT_GRAY;
            case WHITE:
            default:
                return Color.WHITE;
        }
    }
    
    /** 
     * Returns the previewed image.
     * 
     * @return See above.
     */
    BufferedImage getImage() { return manager.getImage(); }
    
    /** Closes and disposes. */
    void closeWindow()
    {
        setVisible(false);
        dispose();
    }
    
}
