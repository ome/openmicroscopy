/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ChainPaletteWindow
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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainBuilderAgent;
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainPaletteCanvas;




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
public class ChainPaletteWindow extends JFrame  {

	private static final String TITLE="ChainBuilder Preview - Chain Palette";
	
	/** Data manager*/
	private ChainDataManager dataManager;
	
	/** Chain canvas */
	private ChainPaletteCanvas chainCanvas;
	
	public ChainPaletteWindow(ChainDataManager dataManager) {
		super(TITLE);
		this.dataManager = dataManager;
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	
	/*public void setOverview(ChainPaletteOverviewWindow overview) {
		this.overview = overview;
	}*/
	
	public void buildGUI() {
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		

		chainCanvas = new ChainPaletteCanvas(dataManager);
		chainCanvas.setContents(dataManager.getChains());
		long start;
		if (ChainBuilderAgent.DEBUG_TIMING)
			start = System.currentTimeMillis();
		chainCanvas.layoutContents();
		
		if (ChainBuilderAgent.DEBUG_TIMING) {
			long end;
			end = System.currentTimeMillis()-start;
			System.err.println("time to layout chains is "+end);
		}
		chainCanvas.completeInitialization();		
	
		setSize(new Dimension(ModulePaletteWindow.SIDE,ModulePaletteWindow.SIDE));
		content.add(chainCanvas);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (chainCanvas != null)
					chainCanvas.scaleToResize();
			}
		});
	}
	
	public void focusOnPalette() {
		chainCanvas.scaleToSize();
	}
	
	public ChainPaletteCanvas getCanvas() {
		return chainCanvas;
	}
	
	public void displayNewChain(LayoutChainData chain) {
		chainCanvas.displayNewChain(chain);
	}
}
