/*
 * org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.util.player;




//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;

/** 
* The UI delegate.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
class MoviePlayerUI
	extends JPanel
{
  
	/** The maximum value for which the ticks are painted. */
	private static final int		MAX_RANGE = 100;

	/** UI identifier corresponding to {@link MoviePlayer#LOOP}. */
	private static final int        LOOP_CMD = 0;

	/** UI identifier corresponding to {@link MoviePlayer#LOOP_BACKWARD}. */
	private static final int        LOOP_BACKWARD_CMD = 1;

	/** UI identifier corresponding to {@link MoviePlayer#BACKWARD}. */
	private static final int        BACKWARD_CMD = 2;

	/** UI identifier corresponding to {@link MoviePlayer#FORWARD}. */
	private static final int        FORWARD_CMD = 3;

	/** UI identifier corresponding to {@link MoviePlayer#PINGPONG}. */
	private static final int        PINGPONG_CMD = 4;

	/** Movie type selections. */
	private static final String[]   selections;

	/** Width of a character w.r.t the font metrics. */
	private int         charWidth;

	/** Reference to the model. */
	private MoviePlayer model;

	/** Tool bar hosting the buttons. */
	private JToolBar    toolBar;

	/** The button to play the movie. */
	JButton             play;

	/** The button to pause the movie. */
	JButton             pause;

	/** The button to pause the movie. */
	JButton             stop;

	/** To specify the movie playback rate in frames per second. */
	JSpinner            fps;

	/** To define new editor for JSpinner (due to JSpinner bug). */
	NumericalTextField	editor;

	/** To select the movie type. */
	JComboBox           movieTypes;

	/** Field hosting the start z-section. */
	JTextField          startZ;

	/** Field hosting the end z-section. */
	JTextField          endZ;

	/** Field hosting the start timepoint. */
	JTextField          startT;

	/** Field hosting the end timepoint. */
	JTextField          endT;

	/** Box to select to play the movie across z-section. */
	JCheckBox           acrossZ;

	/** Box to select to play the movie across timepoint. */
	JCheckBox           acrossT;

	/** Box to select to play the movie across z-section and timepoint. */
	//JRadioButton        acrossZT;

	/** Two knobs slider to select the z-section interval. */
	TwoKnobsSlider      zSlider;

	/** Two knobs slider to select the timepoint interval. */
	TwoKnobsSlider      tSlider;

	/** Initializes the static components. */
	static {
		selections = new String[5];
		selections[LOOP_CMD] = "Loop";
		selections[LOOP_BACKWARD_CMD] = "Loop Backward";
		selections[BACKWARD_CMD] = "Backward";
		selections[FORWARD_CMD] = "Forward";
		selections[PINGPONG_CMD] = "Back and Forth";  
	}

	/**
	 * Returns the UI index corresponding to the specified movie type.
	 * 
	 * @param v The movie type
	 * @return See above.
	 */
	private int getMovieTypeIndex(int v)
	{
		switch (v) {
		case MoviePlayer.LOOP: return LOOP_CMD;
		case MoviePlayer.BACKWARD: return BACKWARD_CMD;
		case MoviePlayer.FORWARD: return FORWARD_CMD;
		case MoviePlayer.PINGPONG: return PINGPONG_CMD;
		case MoviePlayer.LOOP_BACKWARD: return LOOP_BACKWARD_CMD;
		}
		throw new IllegalArgumentException("Movie type not supported.");
	}

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		//movie controls
		play = new JButton(icons.getIcon(IconManager.PLAY));
		play.setToolTipText(UIUtilities.formatToolTipText("Play movie."));
		pause = new JButton(icons.getIcon(IconManager.PAUSE));
		pause.setToolTipText(UIUtilities.formatToolTipText("Pause."));
		stop = new JButton(icons.getIcon(IconManager.STOP));
		stop.setToolTipText(UIUtilities.formatToolTipText("Stop movie."));

		movieTypes = new JComboBox(selections);
		movieTypes.setSelectedIndex(getMovieTypeIndex(model.getMovieType()));

		//Spinner timepoint granularity is 1, so must be stepSize  
		int max = model.getMaximumTimer();
		
		fps = new JSpinner(new SpinnerNumberModel(model.getTimerDelay(), 
				MoviePlayer.FPS_MIN, max, 1));
		String s = "Select or enter the movie playback rate " +
				"(frames per second).";
		editor = new NumericalTextField(MoviePlayer.FPS_MIN, max, Integer.class);
		editor.setText(""+model.getTimerDelay());
		editor.setToolTipText(s);
		editor.setColumns((""+max).length());
		fps.setEditor(editor);

		//movie selection
		int maxZ = model.getMaxZ();
		int minZ = model.getMinZ();
		int rangeZ = maxZ-minZ;
		zSlider = new TwoKnobsSlider(minZ, maxZ, model.getStartZ(), 
									model.getEndZ());
		zSlider.setPaintEndLabels(false);
		zSlider.setPaintLabels(false);
		if (rangeZ <= 0 || rangeZ > MAX_RANGE) zSlider.setPaintTicks(false);
		int length = (""+(maxZ+1)).length();
		startZ = new JTextField(""+(model.getStartZ()+1), length);
		startZ.setToolTipText(
				UIUtilities.formatToolTipText("Enter the start z-section."));
		endZ = new JTextField(""+(model.getEndZ()+1), length);
		endZ.setToolTipText(
				UIUtilities.formatToolTipText("Enter the end z-section."));

		int maxT = model.getMaxT();
		int minT = model.getMinT();
		int rangeT = maxT-minT;
		tSlider = new TwoKnobsSlider(minT, maxT, model.getStartT(), 
				model.getEndT());
		tSlider.setPaintEndLabels(false);
		tSlider.setPaintLabels(false);

		if (rangeT <= 0 || rangeT > MAX_RANGE) tSlider.setPaintTicks(false);
		length = (""+(maxT+1)).length();
		startT = new JTextField(""+(model.getStartT()+1), length);
		startT.setToolTipText(
				UIUtilities.formatToolTipText("Enter the start timepoint."));
		endT = new JTextField(""+(model.getEndT()+1), length);
		endT.setToolTipText(
				UIUtilities.formatToolTipText("Enter the end timepoint."));
		acrossZ = new JCheckBox("Across Z");
		acrossT = new JCheckBox("Across T");
		//acrossZT = new JRadioButton("Across Z and T");
		
		//ButtonGroup group = new ButtonGroup();
		//group.add(acrossZ);
		//group.add(acrossT);
		//group.add(acrossZT);
	}

	/** 
	 * Builds the tool bar hosting the buttons.
	 * 
	 * @return See above.
	 */
	private JToolBar buildToolBar()
	{
		toolBar = new JToolBar();
		toolBar.setBorder(BorderFactory.createEtchedBorder());
		toolBar.setFloatable(false);
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		toolBar.add(play);
		toolBar.add(stop);
		return toolBar;
	}

	/** 
	 * Builds panel with frames per second controls.
	 * 
	 * @return See above.
	 */
	private JPanel createFPSControls()
	{
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(new GridBagLayout());
		JLabel l = new JLabel("Frame Rate");
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		p.add(l, c);
		c.gridx = 1;
		c.insets = new Insets(0, 10, 0, 0);
		p.add(fps, c);
		return p;
	}

	/** 
	 * 	Builds a panel containing the plays label and the plays
	 *  control combo box.
	 *  
	 *  @return Panel containing label and combobox for direction controls.
	 */
	private JPanel createDirectionControls()
	{
		JPanel contain = new JPanel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		JLabel l = new JLabel("Play Mode:");
		contain.add(l, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(0, 10, 0, 0);
		contain.add(movieTypes, gbc);
		return contain;
	}

	/**
	 * Builds a panel wrapping the start and end text.
	 * 
	 * @param length The Length of the text fields.
	 * @param start     The start textFields.
	 * @param end The end textField.
	 * @return See below.
	 */
	private JPanel buildControlsMoviePanel(int length, JTextField start, 
			JTextField end)
	{
		JPanel p = new JPanel();
		Insets insets = end.getInsets();
		int x = insets.left+length*charWidth+insets.left;
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(new GridBagLayout());
		c.weightx = 0;
		c.anchor = GridBagConstraints.WEST;
		p.add(new JLabel(" Start "), c);
		c.gridx = 1;
		c.ipadx = x;
		c.weightx = 0.5;
		p.add(UIUtilities.buildComponentPanel(start), c);
		c.gridx = 2;
		c.ipadx = 0;
		c.weightx = 0;
		p.add(new JLabel(" End "), c);
		c.gridx = 3;
		c.ipadx = x;
		c.weightx = 0.5;
		p.add(UIUtilities.buildComponentPanel(end), c);
		return p; 
	}

	/** 
	 * Builds a panel with a slider, a radio button and JPanel with 
	 * controls.
	 * 
	 * @param slider The slider to wrap.
	 * @param button The radioButton to wrap.
	 * @param controls The panel to wrap.
	 * @return See below.
	 */
	private JPanel buildGroupPanel(TwoKnobsSlider slider, JCheckBox button,
			JPanel controls)
	{
		JPanel p = new JPanel(), group = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(slider);
		p.add(controls);
		group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
		group.add(button);
		group.add(p);
		return group;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		// lays out the movie selection
		JPanel movie = new JPanel();
		movie.setBorder(new TitledBorder("Frame Selection"));
		movie.setLayout(new GridBagLayout());
		GridBagConstraints mc = new GridBagConstraints();
		mc.insets = new Insets(5, 5, 5, 5);
		JPanel p = buildGroupPanel(zSlider, acrossZ, 
				buildControlsMoviePanel((""+model.getMaxZ()).length(), startZ, 
						endZ));
		mc.gridy = 1;
		mc.anchor = GridBagConstraints.WEST;
		movie.add(p, mc);
		p = buildGroupPanel(tSlider, acrossT, 
				buildControlsMoviePanel((""+model.getMaxT()).length(), startT, 
						endT));
		mc.gridy = 2;
		movie.add(p, mc);
		//mc.gridy = 3;
		//movie.add(acrossZT, mc);


		//lays out the controls
		JPanel controls = new JPanel();
		controls.setBorder(new TitledBorder("Animation Control"));
		GridBagConstraints c = new GridBagConstraints();
		controls.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		controls.add(buildToolBar(), c);
		c.gridx = 1;
		controls.add(createDirectionControls(), c);
		c.insets = new Insets(10, 0, 10, 0);
		c.gridx = 0;
		c.gridy = 1;
		controls.add(createFPSControls(), c);


		//lays out components
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(controls, gbc);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		/*
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(this.getWidth(), 2));
		sep.setMinimumSize(new Dimension(this.getWidth(), 2));
		add(sep, gbc);
		*/
		
		
		gbc.gridy = 2;
		add(movie, gbc);
	}

	/**
	 * Creates a new instance. 
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	MoviePlayerUI(MoviePlayer model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		charWidth = getFontMetrics(getFont()).charWidth('m');
		initComponents();
		setDefaults();
		buildGUI();
	}

	/** Sets the default. */
	void setDefaults()
	{
		int maxZ = model.getMaxZ();
		int maxT = model.getMaxT();
		zSlider.setEnabled(maxZ != 0);
		startZ.setEnabled(maxZ != 0);
		endZ.setEnabled(maxZ != 0);
		tSlider.setEnabled(maxT != 0);
		startT.setEnabled(maxT != 0);
		endT.setEnabled(maxT != 0);
		acrossZ.setEnabled(maxZ != 0);
		acrossT.setEnabled(maxT != 0);
		//acrossZT.setEnabled(!((maxZ == 0) || (maxT == 0)));

		switch (model.getMovieIndex()) {
			case MoviePlayerDialog.ACROSS_Z:
				acrossZ.setSelected(true);
				break;
			case MoviePlayerDialog.ACROSS_T:
				acrossT.setSelected(true);
				break;
			case MoviePlayerDialog.ACROSS_ZT:
				acrossZ.setSelected(true);
				acrossT.setSelected(true);
		}
	}

	/**
	 * Returns the movie type corresponding to the specified UI type.
	 * 
	 * @param uiType The UI type.
	 * @return See above.
	 */
	int getMovieType(int uiType)
	{
		switch (uiType) {
			case LOOP_CMD: return MoviePlayer.LOOP;
			case BACKWARD_CMD: return MoviePlayer.BACKWARD;
			case FORWARD_CMD: return MoviePlayer.FORWARD;
			case PINGPONG_CMD: return MoviePlayer.PINGPONG;
			case LOOP_BACKWARD_CMD: return MoviePlayer.LOOP_BACKWARD;
		}
		throw new IllegalArgumentException("UI index not supported.");
	}

	/** Sets the movie type to {@link #FORWARD_CMD}. */
	void setDefaultMovieType()
	{
		movieTypes.setSelectedIndex(FORWARD_CMD);
	}

	/**
	 * Sets the start z-section.
	 * 
	 * @param v The value to set.
	 */
	void setStartZ(int v)
	{
		startZ.setText(""+(v+1));
		zSlider.setStartValue(v);
	}

	/**
	 * Sets the start timepoint.
	 * 
	 * @param v The value to set.
	 */
	void setStartT(int v)
	{
		startT.setText(""+(v+1));
		tSlider.setStartValue(v);
	}

	/**
	 * Sets the end z-section.
	 * 
	 * @param v The value to set.
	 */
	void setEndZ(int v)
	{
		endZ.setText(""+(v+1));
		zSlider.setEndValue(v);
	}

	/**
	 * Sets the end timepoint.
	 * 
	 * @param v The value to set.
	 */
	void setEndT(int v)
	{
		endT.setText(""+(v+1));
		tSlider.setEndValue(v);
	}

	/**
	 * Swaps the {@link #play} and {@link #pause} depending on the 
	 * specified flag.
	 * 
	 * @param b Pass <code>true</code> to set the {@link #pause} button
	 *          <code>false</code> otherwise.
	 */
	void setMoviePlay(boolean b)
	{
		toolBar.removeAll();
		if (b) toolBar.add(pause);
		else toolBar.add(play);
		toolBar.add(stop);
		toolBar.repaint();
	}

	/**
	 * Updates the UI components displaying the timer's delay.
	 * 
	 * @param v The value to set.
	 */
	void setTimerDelay(int v)
	{
		fps.setValue(Integer.valueOf(v)); 
		editor.setText(""+v);
	}

	/** 
	 * Sets the movie index.
	 * 
	 * @param index The index of the movie.
	 */
	void setMovieIndex(int index)
	{
		switch (index) {
			case MoviePlayerDialog.ACROSS_Z:
				acrossZ.setSelected(true);
				break;
			case MoviePlayerDialog.ACROSS_T:
				acrossT.setSelected(true);
		}
	}

	/** 
	 * Initiates a click on the {@link #pause} button 
	 * if the passed index is {@link MoviePlayerDialog#DO_CLICK_PAUSE}
	 * on the {@link #play} button 
	 * if the passed index is {@link MoviePlayerDialog#DO_CLICK_PLAY}. 
	 * 
	 * @param index The index identifying the button.
	 */
	void doClick(int index)
	{
		switch (index) {
			case MoviePlayerDialog.DO_CLICK_PAUSE:
				pause.doClick();
				break;
			case MoviePlayerDialog.DO_CLICK_PLAY:
				play.doClick();
				break;
		}
	}
  
}
