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
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.actions.ManageRndSettingsAction;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.ROICountLoader;
import pojos.ExperimenterData;

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
public class PreviewToolBar
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
    
    /** Text of the ROI count label */
    private static final String ROI_LABEL_TEXT = "ROI Count: ";
    
    /** Reference to the control. */
    private RendererControl control;

    /** Reference to the model. */
    private RendererModel model;

    /** Label indicating the selected plane. */
    private JLabel selectedPlane;

    /** Preview option for render settings */
    private JToggleButton       preview;
    
    /** Label showing the number of ROIs */
    private JLabel roiLabel;
    
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
         
         roiLabel = new JLabel(ROI_LABEL_TEXT+"...");
         roiLabel.setFont(newFont);
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	GridLayout layout = new GridLayout(0,3);
    	layout.setHgap(10);
        setLayout(layout);
        JPanel p = UIUtilities.buildComponentPanelRight(selectedPlane);
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        add(p);
        add(roiLabel); 
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
    	loadROICount();
    }

    /** 
     * Starts an asyc. call to load the number of ROIs
     */
    void loadROICount() {
        ExperimenterData exp = MetadataViewerAgent.getUserDetails();
        ROICountLoader l = new ROICountLoader(new SecurityContext(exp.getGroupId()), this, model.getRefImage().getId(), exp.getId());
        l.load();
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

    /**
     * Returns <code>true</code> if the live update is selected, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isLiveUpdate() { return preview.isSelected(); }
    
    /** 
     * Updates the label showing the number of ROIs
     */
    public void updateROICount(int n) {
        roiLabel.setText(ROI_LABEL_TEXT+""+n);
        revalidate();
    }
    
}
