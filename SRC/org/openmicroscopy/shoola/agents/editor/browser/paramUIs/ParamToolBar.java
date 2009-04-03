 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamToolBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.ParamEditor;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A small tool-bar for each parameter, for editing "Required", deleting etc. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ParamToolBar 
	extends AbstractParamEditor 
	implements ActionListener{
	
	/** Action command for the 'Required' button, to set required to 'True' */
	private static final String 	REQUIRED = "required";
	
	/** Action command for the 'Required' button, to set required to 'True' */
	private static final String 	NOT_REQUIRED = "notRequired";
	
	/** Action command for the 'Delete Param' button */
	private static final String 	DELETE_PARAM = "deleteParam";

	/**
	 * Builds the tool bars, and adds them to container. 
	 */
	private void buildUI()
	{
		IAttributes parameter = getParameter();
		
		IconManager iM = IconManager.getInstance();
		
		// LEFT tool bar. 
		JToolBar leftToolBar = new JToolBar();
		leftToolBar.setBackground(null);
		leftToolBar.setFloatable(false);
		Border bottomRight = BorderFactory.createMatteBorder(0, 0, 1, 1,
                UIUtilities.LIGHT_GREY);
		leftToolBar.setBorder(bottomRight);
		
		// indicate & toggle whether parameter is necessary (required)
		Icon red = iM.getIcon(IconManager.RED_ASTERISK_ICON_11);
		Icon grey = iM.getIcon(IconManager.GREY_ASTERISK_ICON_11);
		JButton requiredButton = new CustomButton(grey);
		requiredButton.addActionListener(this);
		if (parameter.isAttributeTrue(AbstractParam.PARAM_REQUIRED)) {
			requiredButton.setToolTipText("This parameter is 'required'");
			requiredButton.setIcon(red);
			requiredButton.setActionCommand(NOT_REQUIRED);
		} else {
			requiredButton.setToolTipText("Mark this as a 'required' parameter");
			requiredButton.setActionCommand(REQUIRED);
		}
		leftToolBar.add(requiredButton);
		
		// indicate whether parameter has description (show with toolTip)
		Icon info = iM.getIcon(IconManager.INFO_ICON_12);
		JButton infoButton = new CustomButton(info);
		infoButton.setEnabled(false);
		leftToolBar.add(infoButton);
		String paramDesc = parameter.getAttribute(AbstractParam.PARAM_DESC);
		if (paramDesc != null) {
			infoButton.setToolTipText("<html><div style='width:250px; " +
					"padding:2px'>" + "Parameter Description:<br>" + 
					paramDesc + "</div></html>");
			infoButton.setEnabled(true);
		} else {
			infoButton.setToolTipText("No parameter description");
		}
		
	
		// RIGHT tool bar. 
		JToolBar rightToolBar = new JToolBar();
		rightToolBar.setBackground(null);
		rightToolBar.setFloatable(false);
		Border bottomLeft = BorderFactory.createMatteBorder(0, 1, 1, 0,
                UIUtilities.LIGHT_GREY);
		rightToolBar.setBorder(bottomLeft);
		
		// Delete parameter button
		Icon delete = iM.getIcon(IconManager.DELETE_ICON_12);
		JButton deleteButton = new CustomButton(delete);
		deleteButton.setActionCommand(DELETE_PARAM);
		deleteButton.addActionListener(this);
		deleteButton.setToolTipText("Delete this parameter");
		rightToolBar.add(deleteButton);
		
		setLayout(new BorderLayout());
		setBackground(null);
		setBorder(null);
		add(leftToolBar, BorderLayout.WEST);
		add(rightToolBar, BorderLayout.EAST);
	}
	
	public ParamToolBar(IParam parameter) 
	{
		super(parameter);
		
		buildUI();
	}

	/**
	 * Implemented as specified by the {@link ActionListener} interface
	 * Handles click of the "Required" button, to toggle required 
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if (REQUIRED.equals(cmd)) {
			attributeEdited(AbstractParam.PARAM_REQUIRED, "true");
		} else if (NOT_REQUIRED.equals(cmd)) {
			attributeEdited(AbstractParam.PARAM_REQUIRED, "false");
		} else if (DELETE_PARAM.equals(cmd)) {
			firePropertyChange(ParamEditor.PARAM_TYPE, null, "No Param");
		}
	}
	
	public String getEditDisplayName() {
		return "Edit 'Required' status";
	}
}
