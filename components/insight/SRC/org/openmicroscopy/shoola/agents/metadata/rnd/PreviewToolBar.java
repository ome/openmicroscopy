/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.PreviewToolBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.rnd;


import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the various controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class PreviewToolBar
	extends JPanel
{

    /** Space between buttons. */
    static final int SPACE = 3;

    /** Text of the preview check box. */
    private static final String     PREVIEW = "Live Update";

    /** The description of the preview check box. */
    private static final String     PREVIEW_DESCRIPTION = "Update the " +
            "rendering settings immediately. Not available for large " +
            "images";


    /** Reference to the control. */
    private RendererControl control;

    /** Reference to the model. */
    private RendererModel model;

    /** Label indicating the selected plane. */
    private JLabel selectedPlane;

    /** Preview option for render settings */
    private JToggleButton       preview;

    /** Initializes the component. */
    private void initComponents()
    {
        selectedPlane = new JLabel();
        Font font = selectedPlane.getFont();
        Font newFont = font.deriveFont(font.getStyle(),
                font.getSize()-2);
        selectedPlane.setFont(newFont);
        setSelectedPlane();

        preview = new JCheckBox(PREVIEW);
        preview.setEnabled(!model.isBigImage());
        preview.setToolTipText(PREVIEW_DESCRIPTION);
        preview.setFont(newFont);
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setBackground(UIUtilities.BACKGROUND_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(selectedPlane);
        add(Box.createHorizontalGlue());
        add(preview);
    }

    /**
     * Creates a new instance.
     *
     * @param control Reference to the control.
     * @param model Reference to the model.
     */
    PreviewToolBar(RendererControl control, RendererModel model)
    {
        this.control = control;
        this.model = model;
        initComponents();
        buildGUI();
    }

    /** Indicates the selected plane. */
    void setSelectedPlane()
    {
        String s = "Z:"+(model.getDefaultZ()+1)+"/"+model.getMaxZ();
        s += " T:"+(model.getRealSelectedT()+1)+"/"+model.getRealT();
        if (model.isLifetimeImage()) {
            s += " "+EditorUtil.SMALL_T_VARIABLE+":"+(model.getSelectedBin()+1);
            s += "/"+(model.getMaxLifetimeBin());
        }
        selectedPlane.setText(s);
    }

    /**
     * Returns <code>true</code> if the live update is selected, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isLiveUpdate() { return preview.isSelected(); }

}
