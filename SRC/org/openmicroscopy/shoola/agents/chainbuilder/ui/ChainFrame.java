/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ChainFrame
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

//Java import
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainCreationCanvas;
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
public class ChainFrame extends JFrame implements ActionListener {
	

	/** the lifetime of a status label */
	private static final int MSG_LIFETIME =5000;
	
	/*** status label should take width of whole screen.*/
	private static final int MAX_WIDTH=1000;
	/** the canvas that holds the chain */
	private ChainCreationCanvas canvas;
	
	/** the save button */
	private JButton save;
	
	/** the ui manager  */
	private UIManager uiManager;
	/** the chain data manager */
	private ChainDataManager manager;
	
	/** 
	 * A status label for feedback regarding problems during the chain creation
	 * process
	 */
	private JLabel statusLabel;
	
	/**
	 * A timer for killing the status label
	 */
	private final Timer timer = new Timer(MSG_LIFETIME,this);
	
	public ChainFrame(int index,final ChainDataManager manager,CmdTable cmdTable,
			final UIManager uiManager) {
		super("New OME Chain: "+index);
		
		this.manager = manager;
		this.uiManager = uiManager;
		
		setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		Container container = getContentPane();
		canvas  = new ChainCreationCanvas(this,manager);
		
		container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
		// create tool bar.
		JToolBar  tb = new JToolBar();
		tb.setLayout(new BoxLayout(tb,BoxLayout.X_AXIS));
		IconFactory icons = manager.getIconFactory();
		Icon saveIcon = icons.getIcon("save.png");
		if (saveIcon != null)
			save = new JButton(saveIcon);
		else 
			save = new JButton("save chain");
		save.addActionListener
			(cmdTable.lookupActionListener("save chain"));
		save.setAlignmentX(Component.LEFT_ALIGNMENT);
		tb.setFloatable(false);
		tb.add(save);
		tb.setAlignmentX(Component.LEFT_ALIGNMENT);
		setSaveEnabled(false);
		container.add(tb);
		canvas.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(canvas);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		statusLabel = new JLabel();
		statusLabel.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		statusLabel.setFont(Constants.SMALL_TOOLTIP_FONT);
		statusLabel.setText("      ");
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(statusLabel);
		
		
		container.add(panel);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				ChainFrame c = (ChainFrame) e.getWindow();
				uiManager.setCurrentChainFrame(c);
			}
		});
		pack();
		show();
		
		tb.setMaximumSize(tb.getSize());
	//	tb.setPreferredSize(tb.getSize());
		//tb.setMinimumSize(tb.getSize());
		Dimension d  = new Dimension(MAX_WIDTH,panel.getHeight());
		
		statusLabel.setMaximumSize(d);
	}
	
	public void save() {
		ChainSaveFrame saveFrame = new ChainSaveFrame(this,manager);
		saveFrame.show();
	}
	
	public void completeSave(String name,String desc) {
		canvas.save(name,desc);
	}
	
	public void setSaveEnabled(boolean v) {
		save.setEnabled(v);
	}
	
	public void updateChainPalette(LayoutChainData chain) {
		uiManager.updateChainPalette(chain);
	}
	
	public void setStatusLabel(String label) {
		statusLabel.setText(label);
		if (timer.isRunning())
			timer.restart();
		else
			timer.start();
	}
	
	public void actionPerformed(ActionEvent e ) {
		statusLabel.setText("");
	}
}
