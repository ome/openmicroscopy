/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.PreviewToolBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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


//Java imports
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.actions.ManageRndSettingsAction;
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

    /** Reference to the control. */
    private RendererControl control;

    /** Reference to the model. */
    private RendererModel model;

    /** Label indicating the selected plane. */
    private JLabel selectedPlane;

    /** Initializes the component. */
    private void initComponents()
    {
    	 selectedPlane = new JLabel();
         Font font = selectedPlane.getFont();
         selectedPlane.setFont(font.deriveFont(font.getStyle(),
         		font.getSize()-2));
         setSelectedPlane();
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	JToolBar bar = new JToolBar();
    	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
        bar.setBorder(null);
        bar.setRollover(true);
        bar.setFloatable(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(bar);
        add(Box.createHorizontalStrut(5));
        JPanel p = UIUtilities.buildComponentPanelRight(selectedPlane);
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        add(p);
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
    	String s = "Z="+(model.getDefaultZ()+1)+"/"+model.getMaxZ();
    	s += " T="+(model.getRealSelectedT()+1)+"/"+model.getRealT();
    	if (model.isLifetimeImage()) {
			s += " "+EditorUtil.SMALL_T_VARIABLE+"="+(model.getSelectedBin()+1);
			s += "/"+(model.getMaxLifetimeBin());
		}
    	selectedPlane.setText(s);
    }

}
