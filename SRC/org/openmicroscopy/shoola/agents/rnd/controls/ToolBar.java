/*
 * org.openmicroscopy.shoola.agents.rnd.controls.ToolBar
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

package org.openmicroscopy.shoola.agents.rnd.controls;


//Java imports
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;
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
public class ToolBar
	extends JToolBar
{
	
	/** Dimension of the separator between the toolBars. */
	private static final Dimension	SEPARATOR = new Dimension(15, 0);
	
	private JButton					saveButton, greyButton, rgbButton, 
									hsbButton;
	
	public ToolBar(RenderingAgtCtrl control, Registry registry)
	{
		initButtons(registry);
		new ToolBarManager(control, this);
		buildToolBar();
	}
	
	/** Return the grey color model button. */
	JButton getGreyButton() { return greyButton; }

	/** Return the grey color model button. */
	JButton getHsbButton() { return hsbButton; }

	/** Return the grey color model button. */
	JButton getSave() { return saveButton; }

	/** Return the grey color model button. */
	JButton getRgbButton() { return rgbButton; }
	
	/** Initialize the control buttons. */
	private void initButtons(Registry registry)
	{
		IconManager im = IconManager.getInstance(registry);
		saveButton =  new JButton(im.getIcon(IconManager.SAVE_SETTINGS));
		saveButton.setToolTipText(
			UIUtilities.formatToolTipText("Save the settings in the DB."));
		greyButton =  new JButton(im.getIcon(IconManager.GREYSCALE));
		greyButton.setToolTipText(
			UIUtilities.formatToolTipText("Select the GreyScale color model."));
		rgbButton =  new JButton(im.getIcon(IconManager.RGB));
		rgbButton.setToolTipText(
			UIUtilities.formatToolTipText("Select the RGB color model."));
		hsbButton =  new JButton(im.getIcon(IconManager.HSB));
		hsbButton.setToolTipText(
			UIUtilities.formatToolTipText("Select the HSB color model."));
	}
	
	/** Build and lay out the tool bar. */
	private void buildToolBar()
	{
		setFloatable(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(buildModelToolBar());
		addSeparator(SEPARATOR);
		add(buildSaveBar());
	}
	
	/** Toolbar containing the model buttons. */
	private JToolBar buildModelToolBar()
	{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.add(greyButton);
		tb.addSeparator();
		tb.add(rgbButton);
		tb.addSeparator();
		tb.add(hsbButton);
		return tb;
	}
	
	/** Tool bar with the save setting button. */
	private JToolBar buildSaveBar()
	{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.add(saveButton);
		return tb;
	}

}
