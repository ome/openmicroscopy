/*
 * org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomBar
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

package org.openmicroscopy.shoola.agents.viewer.transform.zooming;

//Java imports
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;
import org.openmicroscopy.shoola.agents.viewer.transform.ToolBar;
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
public class ZoomBar
	extends JToolBar
{
	
	private static final String		MAX_LETTER = "300%";
	
	/** width of a letter according to the Font. */
	private int 					txtWidth;
	
	/** zoom buttons. */																																				
	private JButton					zoomIn, zoomOut, zoomFit, saveAs;
	
	/** Field displaying the zooming factor. */
	private JTextField				zoomField;
	
	private ZoomBarManager			manager;
	
	private Registry 				registry;
	
	private ToolBar					tbContainer;
	
	public ZoomBar(Registry registry, ImageInspectorManager mng, 
					ToolBar tbContainer)
	{
		this.registry = registry;
		this.tbContainer = tbContainer;
		initTxtWidth();
		initZoomComponents();
		manager = new ZoomBarManager(this, mng);
		manager.attachListeners();
		buildToolBar();
	}
	
	Registry getRegistry() { return registry; }
	
	JButton getSaveAs() { return saveAs; }
	
	JButton getZoomIn() { return zoomIn; }
	
	JButton getZoomOut() { return zoomOut; }
	
	JButton getZoomFit() { return zoomFit; }
	
	JTextField getZoomField() { return zoomField; }
	
	public ZoomBarManager getManager() { return manager; }
	
	/** Initialize the zoom components. */
	private void initZoomComponents()
	{
		//buttons
		IconManager im = IconManager.getInstance(registry);
		Icon zoomInIcon = im.getIcon(IconManager.ZOOMIN);
		zoomIn = new JButton(zoomInIcon);
		zoomIn.setToolTipText(
			UIUtilities.formatToolTipText("Zoom in."));	
		zoomOut = new JButton(im.getIcon(IconManager.ZOOMOUT));
		zoomOut.setToolTipText(
			UIUtilities.formatToolTipText("Zoom out."));
		zoomFit = new JButton(im.getIcon(IconManager.ZOOMFIT));
		zoomFit.setToolTipText(
			UIUtilities.formatToolTipText("Reset."));
		zoomField = new JTextField("100%", MAX_LETTER.length());
		zoomField.setForeground(Viewer.STEELBLUE);
		zoomField.setToolTipText(
			UIUtilities.formatToolTipText("zooming percentage."));
		
		saveAs = new JButton(im.getIcon(IconManager.SAVEAS));
		saveAs.setToolTipText(
					UIUtilities.formatToolTipText("Save zoomed image."));	
		//Set the separator of tbContainer.
		tbContainer.setSeparator(
				UIUtilities.toolBarSeparator(zoomIn, zoomInIcon));
	}	
	
	/** Build the toolBar. */
	private void buildToolBar() 
	{
		setFloatable(false);
		putClientProperty("JToolBar.isRollover", new Boolean(true));
		add(saveAs);
		add(zoomOut);
		add(zoomIn);
		add(zoomFit);
		add(buildTextPanel());
	}

	/** Panel containing textField. */
	private JPanel buildTextPanel()
	{
		JPanel p = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.ipadx = txtWidth/2;
		gridbag.setConstraints(zoomField, c);
		p.add(zoomField);
		p.setAlignmentX(LEFT_ALIGNMENT);
		return p;
	}
	
	/** Initializes the width of the text. */
	private void initTxtWidth()
	{
		FontMetrics metrics = getFontMetrics(getFont());
		txtWidth = MAX_LETTER.length()*metrics.charWidth('m');
	}
	
}
