/*
 * org.openmicroscopy.shoola.agents.viewer.controls.TNavigator
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
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;

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
class TNavigator
	extends JPanel
{
	/** Dimension of the JPanel which contains the slider. */
	private static final int		PANEL_HEIGHT = 40;
	private static final int		PANEL_WIDTH = 100;
	
	private static final Dimension	DIM = new Dimension(PANEL_WIDTH, 
														PANEL_HEIGHT);
	

	/** The slider used to move across time.*/
	private JSlider         		tSlider;
		
	/** Text field to allow user to specify a timepoint. */
	private JTextField      		tField;
	
	/** Buttons to control the playback of time movie. */
	private JButton         		play, stop, rewind; 
	
	/** Allows user t specify the movie playback rate in frames per second. */
	private JSpinner        		fps;
	
	/** To define new editor for JSpinner (due to JSpinner bug). */
	private JTextField      		editor; 

	private TNavigatorManager		manager;
	
	private IconManager				im;
	
	TNavigator(NavigationPaletteManager topManager, int sizeT, int t)
	{
		manager = new TNavigatorManager(this, topManager, sizeT, t);
		im = IconManager.getInstance(topManager.getRegistry());
		initComponents(sizeT, t);
		manager.attachListeners();
		buildGUI();
	}
	
	public JTextField getEditor()
	{
		return editor;
	}

	public JSpinner getFps()
	{
		return fps;
	}

	public JButton getPlay()
	{
		return play;
	}

	public JButton getRewind()
	{
		return rewind;
	}

	public JButton getStop()
	{
		return stop;
	}

	public JTextField getTField()
	{
		return tField;
	}

	public JSlider getTSlider()
	{
		return tSlider;
	}

	/** Initializes the component. */
	private void initComponents(int sizeT, int t) 
	{
		//Slider
		tSlider = new JSlider(JSlider.HORIZONTAL, 0, sizeT, t);
		tSlider.setToolTipText("Move the slider to navigate across time");
		tSlider.setMinorTickSpacing(1);
		tSlider.setMajorTickSpacing(10);
		tSlider.setPaintTicks(true);
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel(""+0) );
		labelTable.put(new Integer(sizeT), new JLabel(""+sizeT));
		tSlider.setLabelTable(labelTable);
		tSlider.setPaintLabels(true);
		
		//textField
		tField = new JTextField("0", (""+sizeT).length());
		tField.setForeground(NavigationPalette.STEELBLUE);
		tField.setToolTipText("Enter a timepoint");
		//buttons
		play = new JButton(im.getIcon(IconManager.MOVIE));
		play.setToolTipText("Play movie from current timepoint");
		stop = new JButton(im.getIcon(IconManager.STOP));
		stop.setToolTipText("Stop movie");
		rewind = new JButton(im.getIcon(IconManager.REWIND));
		rewind.setToolTipText("Go to first timepoint");
		//Spinner timepoint granularity is 1, so must be stepSize
		//fps = new JSpinner(new SpinnerNumberModel(12, 0, sizeT, 1));  
		fps = new JSpinner(new SpinnerNumberModel(12, 12, 12, 1));
		editor = new JTextField("12", (""+sizeT).length());
		String s = "Select or enter the movie playback rate " +
					"(frames per second)";
		editor.setToolTipText(s);
		fps.setEditor(editor);
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{ 
		JPanel topControls = new JPanel();
		topControls.setLayout(new BoxLayout(topControls, BoxLayout.X_AXIS)); 
		topControls.add(buildMoviePanel());
		add(topControls);
		add(buildSliderPanel());
	}
	
	/**
	 * Build a panel containing a slider along with current selection
	 * 
	 * @return See above.
	 */
	private JPanel buildSliderPanel()
	{
		JPanel p = new JPanel(), field = new JPanel(), slider = new JPanel();
		slider.setLayout(null);
		slider.setOpaque(false);
  		slider.setPreferredSize(DIM);
  		slider.setSize(DIM);
  		tSlider.setPreferredSize(DIM);
  		tSlider.setBounds(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
  		slider.add(tSlider);
  		JLabel current = new JLabel("Current T: ");
  		current.setForeground(NavigationPalette.STEELBLUE);
  		field.add(current);
  		field.add(tField);
  		field.setAlignmentX(LEFT_ALIGNMENT);
  		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
  		p.add(field);
 		p.add(slider);
  		return p;
	}
    
	/** 
	 * Builds a panel containing the movie control buttons 
	 * and the frames per second spinner.
	 *
	 * @return      The above mentioned panel.
	 */
	private JPanel buildMoviePanel()
	{
		JPanel p = new JPanel(), buttons = new JPanel(), 
				spinner = new JPanel();
		JLabel rate = new JLabel("Rate: ");
		spinner.add(rate);
		spinner.add(fps);
		buttons.add(play);
		buttons.add(stop);
		buttons.add(rewind);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 
		p.add(buttons);
		p.add(spinner);
		return p;
	}

}
