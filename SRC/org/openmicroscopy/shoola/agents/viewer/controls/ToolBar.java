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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;

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
	extends JToolBar
{
	
	/** Height of the JPanel which contains the textfield. */
	private static final int		HEIGHT = 20;
	
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
	
	/** Labels displaying the number of timepoints and of z-sections. */
	private JLabel					zLabel, tLabel;
	
	private JPanel					zPanel, tPanel;
	
	private ToolBarManager			manager;
	
	private int 					txtWidth;
	
	public ToolBar(ViewerCtrl control, Registry registry, int sizeT,
							int sizeZ, int t, int z)
	{
		initTxtWidth();
		initMovieComponents(registry, sizeT-1);
		initTextFields(t, z, sizeT, sizeZ);
		manager = new ToolBarManager(control, this, sizeT, t, sizeZ, z);
		manager.attachListeners();
		buildToolBar();
	}
	
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

	public ToolBarManager getManager() { return manager; }
	
	/** Initialize the movie components. */
	private void initMovieComponents(Registry registry, int maxT)
	{
		//buttons
		IconManager im = IconManager.getInstance(registry);
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
		tField = new JTextField();
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
		
		JLabel label = new JLabel(" Z ");
		zPanel = TextFieldPanel(label, zField, zLabel, ("/"+maxZ).length());
		label = new JLabel(" T ");
		tPanel = TextFieldPanel(label, tField, tLabel, ("/"+maxT).length());
	}
	
	/** Build the main tool bar. */
	private void buildToolBar() 
	{
		setFloatable(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(buildRenderingBar());
		addSeparator(Viewer.SEPARATOR);
		add(buildMovieBar());
		addSeparator(Viewer.SEPARATOR);
		add(buildTextFieldBar());
		addSeparator(Viewer.SEPARATOR_END);
	}
	
	/** Tool bar containing the textFields. */
	private JToolBar buildTextFieldBar()
	{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.add(zPanel);
		tb.add(tPanel);
		tb.addSeparator();
		return tb;
	}
	
	/** 
	 * Build Panel with TextField to diplay in the toolBar. 
	 * Use to control the size of the textField in the ToolBar.
	 */
	private JPanel TextFieldPanel(JLabel l, JTextField field, JLabel le, 
					int length)
	{
		Insets insets = field.getInsets();
		int y = insets.left+length*txtWidth+insets.right;
		JPanel p = new JPanel(), pField = new JPanel(), 
				pAll = new JPanel();
		pField.setLayout(null);
		Dimension d = new Dimension(y, HEIGHT);
		pField.setPreferredSize(d);
		pField.setSize(d);
		field.setPreferredSize(d);
		field.setSize(d);
		pField.add(field);
		
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(l);
		p.add(pField);
		p.add(le);
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		pAll.setLayout(gridbag);
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(p, c);
		pAll.add(p);

		return pAll;
	}
	
	/** Tool bar containing the rendering buttons. */
	private JToolBar buildRenderingBar()
	{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.add(render);
		tb.add(inspector);
		tb.add(saveAs);
		tb.addSeparator();
		return tb;
	}
	
	/** Tool bar containing the movie buttons. */
	private JToolBar buildMovieBar()
	{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.add(play);
		tb.add(pause);
		tb.add(stop);
		tb.add(rewind);
		tb.add(forward);
		JLabel label = new JLabel(" Rate ");
		tb.add(label);
		tb.add(fps);
		tb.addSeparator();
		return tb;
	}
	
	
	private void initTxtWidth()
	{
		FontMetrics metrics = getFontMetrics(getFont());
		txtWidth = metrics.charWidth('m');
	}
	
}
