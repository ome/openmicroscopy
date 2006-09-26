/*
 * org.openmicroscopy.shoola.agents.imviewer.util.MoviePlayerUI
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

package org.openmicroscopy.shoola.agents.imviewer.util;




//Java imports
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
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
    
    /** Vertical space between the panes. */
    private static final Dimension  VSPACE = new Dimension(0, 20);
    
    /** UI identifier corresponding to {@link MoviePlayer#LOOP}. */
    private static final int        LOOP_CMD = 0;
    
    /** UI identifier corresponding to {@link MoviePlayer#BACKWARD}. */
    private static final int        BACKWARD_CMD = 1;
    
    /** UI identifier corresponding to {@link MoviePlayer#FORWARD}. */
    private static final int        FORWARD_CMD = 2;
    
    /** UI identifier corresponding to {@link MoviePlayer#PINGPONG}. */
    private static final int        PINGPONG_CMD = 3;
    
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
    JTextField          editor;
    
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
    JRadioButton        acrossZ;
    
    /** Box to select to play the movie across timepoint. */
    JRadioButton        acrossT;
    
    /** Box to select to play the movie across z-section and timepoint. */
    JRadioButton        acrossZT;
    
    /** Two knobs slider to select the z-section interval. */
    TwoKnobsSlider      zSlider;

    /** Two knobs slider to select the timepoint interval. */
    TwoKnobsSlider      tSlider;
    
    /** Initializes the static components. */
    static {
        selections = new String[4];
        selections[LOOP_CMD] = "Loop";
        selections[BACKWARD_CMD] = "Backward";
        selections[FORWARD_CMD] = "Forward";
        selections[PINGPONG_CMD] = "Round trip";  
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
        }
        throw new IllegalArgumentException("Movie type not supported.");
    }
    
    /** Initializes the components constituing the display. */
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
        
        //      Spinner timepoint granularity is 1, so must be stepSize  
        int max = model.getMaximumTimer();
        fps = new JSpinner(new SpinnerNumberModel(model.getTimerDelay(), 
                    MoviePlayer.FPS_MIN, max, 1));
        editor = new JTextField(""+model.getTimerDelay(), (""+max).length());
        String s = "Select or enter the movie playback rate " +
                        "(frames per second).";
        editor.setToolTipText(UIUtilities.formatToolTipText(s));
        fps.setEditor(editor);
        
         //movie selection
        int maxZ = model.getMaxZ();
        zSlider = new TwoKnobsSlider(model.getMinZ(), maxZ, 
                                    model.getStartZ(), model.getEndZ());
        zSlider.setPaintEndLabels(false);
        zSlider.setPaintLabels(false);
        startZ = new JTextField(""+model.getStartZ(), (""+maxZ).length());
        startZ.setToolTipText(
                UIUtilities.formatToolTipText("Enter the start z-section."));
        endZ = new JTextField(""+model.getEndZ(), (""+maxZ).length());
        endZ.setToolTipText(
                UIUtilities.formatToolTipText("Enter the end z-section."));
        
        int maxT = model.getMaxT();
        tSlider = new TwoKnobsSlider(model.getMinT(), maxT, 
                                model.getStartT(), model.getEndT());
        tSlider.setPaintEndLabels(false);
        tSlider.setPaintLabels(false);
        startT = new JTextField(""+model.getStartT(), (""+maxT).length());
        startT.setToolTipText(
                UIUtilities.formatToolTipText("Enter the start timepoint."));
        endT = new JTextField(""+model.getEndT(), (""+maxT).length());
        endT.setToolTipText(
                UIUtilities.formatToolTipText("Enter the end timepoint."));
        acrossZ = new JRadioButton("Across Z");
        acrossT = new JRadioButton("Across T");
        acrossZT = new JRadioButton("Across Z and T");
        ButtonGroup group = new ButtonGroup();
        group.add(acrossZ);
        group.add(acrossT);
        group.add(acrossZT);
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
        toolBar.setFloatable(true);
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.add(play);
        //bar.add(Box.createRigidArea(HBOX));
        //bar.add(pause);
        //bar.add(Box.createRigidArea(HBOX));
        toolBar.add(stop);
        return toolBar;
    }
    
    /** 
     * Builds panel with text editor.
     * 
     * @return See below.
     */
    private JPanel buildControlsPanel()
    {
        JPanel p = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(new GridBagLayout());
        JLabel l = new JLabel("Rate");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        p.add(l, c);
        c.gridx = 1;
        JPanel contain = UIUtilities.buildComponentPanel(fps);
        p.add(contain, c);
        c.gridx = 0;
        c.gridy = 1;
        l = new JLabel("Play");
        p.add(l, c);
        c.gridx = 1;
        contain = UIUtilities.buildComponentPanel(movieTypes);
        p.add(contain, c);
        return p;
    }
    
    /** 
     * Builds panel with frames pers second controls.
     * 
     * @return See above.
     */
    private JPanel createFPSControls()
    {
    	 JPanel p = new JPanel();
         GridBagConstraints c = new GridBagConstraints();
         p.setLayout(new GridBagLayout());
         JLabel l = new JLabel("Rate");
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.WEST;
         p.add(l, c);
         c.gridx = 1;
         c.insets = new Insets(0,10,0,0);
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
        JLabel l = new JLabel("Play");
        contain.add(l, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0,10,0,0);
        contain.add(movieTypes, gbc);
        return contain;
    }
    
    /**
     * Builds a panel wrapping the start and end text.
     * 
     * @param length The Length of the textfields.
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
    private JPanel buildGroupPanel(TwoKnobsSlider slider, JRadioButton button,
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
        movie.setLayout(new GridBagLayout());
        GridBagConstraints mc = new GridBagConstraints();
        mc.gridx = 0;
        mc.gridy = 0;
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
        mc.gridy = 3;
        movie.add(acrossZT, mc);
       
        
        //lays out the controls
        JPanel controls = new JPanel();
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
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(controls, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setPreferredSize(new Dimension(this.getWidth(), 2));
        sep.setMinimumSize(new Dimension(this.getWidth(), 2));
      
        add(sep, gbc);
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
        acrossZT.setEnabled(!((maxZ == 0) || (maxT == 0)));
        int index = model.getMovieIndex();

        if (index == MoviePlayer.ACROSS_Z) {
            acrossZ.setSelected(true);
        } else if (index == MoviePlayer.ACROSS_T) {
            acrossT.setSelected(true);
        } else if (index == MoviePlayer.ACROSS_ZT) {
            acrossZT.setSelected(true);
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
        }
        throw new IllegalArgumentException("UI index not supported.");
    }
    
    /**
     * Sets the start z-section.
     * 
     * @param v The value to set.
     */
    void setStartZ(int v)
    {
        startZ.setText(""+v);
        zSlider.setStartValue(v);
    }
    
    /**
     * Sets the start timepoint.
     * 
     * @param v The value to set.
     */
    void setStartT(int v)
    {
        startT.setText(""+v);
        tSlider.setStartValue(v);
    }
    
    /**
     * Sets the end z-section.
     * 
     * @param v The value to set.
     */
    void setEndZ(int v)
    {
        endZ.setText(""+v);
        zSlider.setEndValue(v);
    }
    
    /**
     * Sets the end timepoint.
     * 
     * @param v The value to set.
     */
    void setEndT(int v)
    {
        endT.setText(""+v);
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
        fps.setValue(new Integer(v)); 
        editor.setText(""+v);
    }
    
}
