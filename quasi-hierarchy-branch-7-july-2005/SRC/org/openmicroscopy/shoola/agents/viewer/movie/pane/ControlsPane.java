/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.PlayPane
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

package org.openmicroscopy.shoola.agents.viewer.movie.pane;


//Java imports
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.movie.Player;
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
public class ControlsPane
    extends JPanel
{
    
    private static final Dimension  HBOX = new Dimension(10, 0);
    
    private static final String[]   selections;
    
    /** Movie controls. */
    JButton                         play, stop, pause;
    
    /** Allows user t specify the movie playback rate in frames per second. */
    JSpinner                        fps;

    /** To define new editor for JSpinner (due to JSpinner bug). */
    JTextField                      editor;
    
    JComboBox                       movieType;
    /** Border of the pressed button. */
    private Border                  pressedBorder;

    private ControlsPaneMng         manager;
    
    static {
        selections = new String[4];
        selections[Player.LOOP] = "Loop";
        selections[Player.BACKWARD] = "Backward";
        selections[Player.FORWARD] = "Forward";
        selections[Player.PINGPONG] = "Round trip";  
    }
    
    /**
     * 
     * @param mng           Reference to the {@link playerUIMng}.
     * @param registry      Reference to the {@link Registrt}.
     * @param max           minimum of maxZ and maxT.
     */
    public ControlsPane(PlayerUIMng mng, Registry registry, int max, 
                        int fpsInit, int type)
    {
        pressedBorder = BorderFactory.createLoweredBevelBorder();
        initComponents(registry, max, fpsInit, type);
        manager = new ControlsPaneMng(this, mng, registry, max);
        buildGUI();
    }
    
    ControlsPaneMng getManager() { return manager; }
    
    /** Initialize the GUI components. */
    private void initComponents(Registry registry, int max, int init, 
                                    int type)
    {
        IconManager im = IconManager.getInstance(registry);
        play = new JButton(im.getIcon(IconManager.PLAY));
        play.setToolTipText(UIUtilities.formatToolTipText("Play movie."));
        stop = new JButton(im.getIcon(IconManager.STOP));
        stop.setToolTipText(UIUtilities.formatToolTipText("Stop movie."));
        pause = new JButton(im.getIcon(IconManager.PAUSE));
        pause.setToolTipText(UIUtilities.formatToolTipText("Pause."));
        
        setButtonBorder(play);
        setButtonBorder(pause);
        setButtonBorder(stop);
        //Spinner timepoint granularity is 1, so must be stepSize  
        fps = new JSpinner(new SpinnerNumberModel(init, Player.FPS_MIN, max, 
                                    1));
        editor = new JTextField(""+init, (""+max).length());
        String s = "Select or enter the movie playback rate " +
                        "(frames per second).";
        editor.setToolTipText(UIUtilities.formatToolTipText(s));
        fps.setEditor(editor);
        
        movieType = new JComboBox(selections);
        movieType.setSelectedIndex(type);
    }

    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel p = buildControlsPanel();
        JToolBar bar = buildToolBar();
        setLayout(gridbag);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(p, c);
        add(p);
        c.gridx = 1;
        gridbag.setConstraints(bar, c);
        add(bar);
    }
    
    /** Build toolBar with JButtons. */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(play);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(pause);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(stop);
        return bar;
    }
    
    /** Set a LoweredBevelBorder but don't paint it. */
    private void setButtonBorder(JButton button) 
    {
        button.setBorder(pressedBorder);
        button.setBorderPainted(false);
    }
    
    /** Build panel with text editor. */
    private JPanel buildControlsPanel()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        JLabel l = new JLabel(" Rate");
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        JPanel fpsp = buildComponentPanel(fps);
        gridbag.setConstraints(fpsp, c);
        p.add(fpsp);
        c.gridx = 0;
        c.gridy = 1;
        l = new JLabel(" Play");
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        JPanel cp = buildComponentPanel(movieType);
        gridbag.setConstraints(cp, c);
        p.add(cp);
        return p;
    }
    
    /** Wrap a JComponent in a JPanel. */
    private JPanel buildComponentPanel(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(component);
        return p;
    }  
    
}
