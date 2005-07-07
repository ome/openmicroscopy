/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ChainPaletteOverviewWindow
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

package org.openmicroscopy.shoola.agents.chainbuilder.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import 
	 org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainPaletteOverviewCanvas;




/** 
 * An example of a top-level window that inherits from {@link TopWindow}.
 * 
 * @author  <br>Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">
 * 					hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class ChainPaletteOverviewWindow extends JFrame  {

	private static final String TITLE="Overview";
	
	public static final int SIDE=150;
	
	/** Chain canvas */
	private ChainPaletteOverviewCanvas overviewCanvas;
	
	/** the chain window we're tied to */
	private ChainPaletteWindow palette;
	
	public ChainPaletteOverviewWindow(ChainPaletteWindow palette) {
		super(TITLE);
		this.palette = palette;
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(false);
	}
	
	public void buildGUI() {
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		overviewCanvas = new ChainPaletteOverviewCanvas();
		overviewCanvas.setPreferredSize(new Dimension(SIDE,SIDE));
		content.add(overviewCanvas);
		setLocation(ModulePaletteWindow.SIDE,0);
		overviewCanvas.setDetailCanvas(palette.getCanvas());
		pack();
	}
}
