/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ModulePaletteWindow
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainPaletteCanvas;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.util.ui.Constants;



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
public class ChainPaletteWindow extends JFrame implements ActionListener {

	private static final String TITLE="Chain Palette";
	
	/** Data manager*/
	private ChainDataManager dataManager;
	
	/** Chain canvas */
	private ChainPaletteCanvas chainCanvas;
	
	private JButton zoom;
	private JButton pan;

	public ChainPaletteWindow(ChainDataManager dataManager) {
		super(TITLE);
		this.dataManager = dataManager;
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		
	}
	
	public void buildGUI() {
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		ButtonGroup group = new ButtonGroup();
		IconFactory icons = dataManager.getIconFactory();
		Icon zoomIcon = icons.getIcon("zoomMode.png");
		Icon panIcon = icons.getIcon("pan.png");
		

		if (panIcon != null)
			pan= new JButton(panIcon);
		else 
			pan = new JButton("pan");
		pan.addActionListener(this);
		group.add(pan);
		tb.add(pan);
		
		if (zoomIcon != null)
			zoom= new JButton(zoomIcon);
		else 
			zoom = new JButton("zoom");
		zoom.addActionListener(this);
		group.add(zoom);
		tb.add(zoom);
		
		
		pan.setEnabled(true);
		zoom.setEnabled(false);
		tb.setFloatable(false);
		
		content.add(tb,BorderLayout.NORTH);
		
		
		chainCanvas = new ChainPaletteCanvas(dataManager);
		chainCanvas.setContents(dataManager.getChains());
		chainCanvas.layoutContents();
		chainCanvas.completeInitialization();
		
		content.add(chainCanvas,BorderLayout.CENTER);
		
		chainCanvas.setPreferredSize(
				new Dimension(ModulePaletteWindow.SIDE,ModulePaletteWindow.SIDE));
		//setSize(new Dimension(ModulePaletteWindow.SIDE,ModulePaletteWindow.SIDE));
		pack();
	
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (chainCanvas != null)
					chainCanvas.scaleToResize();
			}
		});
		//tb.setMaximumSize(tb.getSize());
	}
	
	public void focusOnPalette() {
		chainCanvas.scaleToSize();
	}
	
	public void displayNewChain(LayoutChainData chain) {
		chainCanvas.displayNewChain(chain);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == zoom) {
			System.err.println("switching to zoom");
			pan.setEnabled(true);
			zoom.setEnabled(false);
			chainCanvas.setToZoom();
		}
		else {
			System.err.println("switching to pan..");
			pan.setEnabled(false);
			zoom.setEnabled(true);
			chainCanvas.setToPan();
		}
	}
}
