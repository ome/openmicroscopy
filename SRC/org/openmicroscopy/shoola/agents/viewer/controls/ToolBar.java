/*
 * org.openmicroscopy.shoola.agents.viewer.controls.ToolBar
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

package org.openmicroscopy.shoola.agents.viewer.controls;


//Java imports
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
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
	extends JPanel
{

	/** Bring up the 3Dimage viewer. */
	private JButton					viewer3D;
	
	/** Bring up the save image widget. */
	private JButton					saveAs;
	
	/** Bring up the  image inspector widget. */
	private JButton					inspector;
																																							
	/** Control to post an event to bring up the rendering widget. */
	private JButton					render;
	
	/** Buttons to control the playback of time movie. */
	private JButton         		play, stop, rewind, pause, forward; 
	
	/** Allows user t specify the movie playback rate in frames per second. */
	private JSpinner        		fps;
	
	/** To define new editor for JSpinner (due to JSpinner bug). */
	private JTextField      		editor; 
	
	/** Fields displaying the current z-section and the current timepoint. */
	private JTextField				zField, tField;
	
	/** TextField with the start (resp. end) timepoint. */
	private JTextField				movieStart, movieEnd;
	
	private JPanel					controlsMoviePanel;
	
	/** Labels displaying the number of timepoints and of z-sections. */
	private JLabel					zLabel, tLabel;
	
	private JPanel					ztPanel;
	
	private ToolBarManager			manager;
	
	private int 					txtWidth;
	
	public ToolBar(ViewerCtrl control, Registry registry, int sizeT,
							int sizeZ, int t, int z)
	{
		initTxtWidth();
		initComponents(registry, sizeT-1, sizeZ-1);
		initTextFields(t, z, sizeT, sizeZ);
		manager = new ToolBarManager(control, this, sizeT, t, sizeZ, z);
		manager.attachListeners();
		buildToolBar();
	}

	public JButton getViewer3D() { return viewer3D; }
	
	public JButton getSaveAs() { return saveAs; }

	public JLabel getZLabel() { return zLabel; }
	
	public JLabel getTLabel() { return tLabel; }
	
	public JSpinner getFPS() { return fps; }
	
	public JTextField getEditor() { return editor; }

	public JButton getInspector() { return inspector; } 
	
	public JButton getPlay() { return play; }

	public JButton getRender() { return render; }

	public JButton getRewind() { return rewind; }
	
	public JButton getPause() { return pause; }
	
	public JButton getForward() { return forward; }

	public JButton getStop() { return stop; }

	public JTextField getTField() { return tField; }

	public JTextField getZField() { return zField; }

	public JTextField getMovieStart() { return movieStart; }
	
	public JTextField getMovieEnd() { return movieEnd; }
	
	public ToolBarManager getManager() { return manager; }
	
	/** Initialize the buttons components. */
	private void initComponents(Registry registry, int maxT, int maxZ)
	{
		//buttons
		IconManager im = IconManager.getInstance(registry);
		viewer3D = new JButton(im.getIcon(IconManager.VIEWER3D));
		viewer3D.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the image3D viewer."));
		viewer3D.setEnabled(maxZ ==0);	
		saveAs = new JButton(im.getIcon(IconManager.SAVEAS));
		saveAs.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the save image window."));
			
		inspector  =  new JButton(im.getIcon(IconManager.INSPECTOR));
		inspector.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the inspector panel."));
		render =  new JButton(im.getIcon(IconManager.RENDER));
		render.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the rendering panel."));	
		play = new JButton(im.getIcon(IconManager.MOVIE));
		play.setToolTipText(
		UIUtilities.formatToolTipText("Play movie from current timepoint."));
		stop = new JButton(im.getIcon(IconManager.STOP));
		stop.setToolTipText(UIUtilities.formatToolTipText("Stop movie."));
		rewind = new JButton(im.getIcon(IconManager.REWIND));
	  	rewind.setToolTipText(
	  		UIUtilities.formatToolTipText("Go to the first timepoint."));
		forward = new JButton(im.getIcon(IconManager.FORWARD));
		forward.setToolTipText(
			UIUtilities.formatToolTipText("Go to the last timepoint."));	
		pause = new JButton(im.getIcon(IconManager.PLAYER_PAUSE));
		pause.setToolTipText(
			UIUtilities.formatToolTipText("Stop the movie."));
					
	  	//Spinner timepoint granularity is 1, so must be stepSize
	  	//fps = new JSpinner(new SpinnerNumberModel(12, 0, sizeT, 1));  
	  	fps = new JSpinner(new SpinnerNumberModel(12, 12, 12, 1));
	  	editor = new JTextField("12", (""+maxT).length());
	  	String s = "Select or enter the movie playback rate " +
						"(frames per second).";
	  	editor.setToolTipText(UIUtilities.formatToolTipText(s));
	  	fps.setEditor(editor);
	  	//only one timepoint.
	  	boolean b;
	  	if (maxT == 0) b = false;
	  	else b = true;
		play.setEnabled(b);
		rewind.setEnabled(b);
		stop.setEnabled(b);
		pause.setEnabled(b);
		forward.setEnabled(b);
		fps.setEnabled(b);
		editor.setEnabled(b);	
	}
	
	/** 
	 * Initializes the text Fields displaying the current z-section and the
	 * the current timepoint.
	 */
	private void initTextFields(int t, int z, int maxT, int maxZ)
	{
		zLabel = new JLabel("/"+maxZ);
		tLabel = new JLabel("/"+maxT);
		tField = new JTextField(""+t, (""+maxT).length());
		if (maxT-1 == 0) tField.setEditable(false);
		tField.setForeground(Viewer.STEELBLUE);
		tField.setToolTipText(
			UIUtilities.formatToolTipText("Enter a timepoint."));
		zField = new JTextField(""+z, (""+maxZ).length());
		zField.setForeground(Viewer.STEELBLUE);
		zField.setToolTipText(
			UIUtilities.formatToolTipText("Enter a Z point"));
		if (maxZ-1 == 0) zField.setEditable(false);
		ztPanel = textFieldsPanel((""+maxZ).length(), (""+maxT).length());
		
		movieStart = new JTextField(""+0, (""+maxT).length());
		movieStart.setForeground(Viewer.STEELBLUE);
		movieStart.setToolTipText(
		UIUtilities.formatToolTipText("Enter the starting point of the movie"));
		movieEnd = new JTextField(""+maxT, (""+maxT).length());
		movieEnd.setForeground(Viewer.STEELBLUE);
		movieEnd.setToolTipText(
		UIUtilities.formatToolTipText("Enter the end point of the movie"));
		controlsMoviePanel = buildControlsMoviePanel((""+maxT).length());
	}
	
	/** Build the main tool bar. */
	private void buildToolBar() 
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(buildBar());
		add(moviePanel());
		add(controlsMoviePanel);
		add(new JSeparator(SwingConstants.VERTICAL));
		add(ztPanel);
	}
	
	/** Build a toolBar with buttons. */
	private JToolBar buildBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.add(render);
		bar.add(inspector);
		bar.add(viewer3D);
		bar.add(saveAs);
		bar.add(new JSeparator(SwingConstants.VERTICAL));
		//movie controls.
		bar.add(play);
		//bar.add(pause);
		//bar.add(stop);
		bar.add(rewind);
		bar.add(forward);
		return bar;
	}
	
	/** Build panel with text editor. */
	private JPanel moviePanel()
	{
		JPanel p = new JPanel();
		//p.setLayout(new FlowLayout(FlowLayout.LEFT));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		c.fill = GridBagConstraints.NONE;
		JLabel l = new JLabel(" Rate ");
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(l, c);
		p.add(l);
		c.gridx = 1;
		gridbag.setConstraints(fps, c);
		p.add(fps);
		//p.add(Box.createRigidArea(H_SPACER_SIZE));
		return p;
	}
	
	private JPanel buildControlsMoviePanel(int length)
	{
		JPanel p = new JPanel();
		JLabel l = new JLabel(" start ");
		int x = length/2*txtWidth;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(l, c);
		p.add(l);
		c.gridx = 1;
		c.ipadx = x;
		gridbag.setConstraints(movieStart, c);
		p.add(movieStart);
		c.gridx = 2;
		c.ipadx = 0;
		l = new JLabel(" end ");
		gridbag.setConstraints(l, c);
		p.add(l);
		c.gridx = 3;
		c.ipadx = x;
		gridbag.setConstraints(movieEnd, c);
		p.add(movieEnd);
		return p; 
	}
	
	/** Build panel with labels and text fields. */
	private JPanel textFieldsPanel(int zLength, int tLength)
	{
		JPanel p = new JPanel();
		JLabel l = new JLabel(" Z ");
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(l, c);
		p.add(l);
		c.gridx = 1;
		Insets insets = zField.getInsets();
		c.ipadx = insets.left+zLength/2*txtWidth+insets.right;
		gridbag.setConstraints(zField, c);
		p.add(zField);
		c.gridx = 2;
		gridbag.setConstraints(zLabel, c);
		p.add(zLabel);
		c.gridx = 3;
		c.ipadx = 0;
		l = new JLabel(" T ");
		gridbag.setConstraints(l, c);
		p.add(l);
		c.gridx = 4;
		insets = tField.getInsets();
		c.ipadx = insets.left+tLength/2*txtWidth+insets.right;
		gridbag.setConstraints(tField, c);
		p.add(tField);
		c.gridx = 5;
		gridbag.setConstraints(tLabel, c);
		p.add(tLabel);
		return p;
	}
	
	/** Initializes the width of the text. */
	private void initTxtWidth()
	{
		FontMetrics metrics = getFontMetrics(getFont());
		txtWidth = metrics.charWidth('m');
	}
	
}
