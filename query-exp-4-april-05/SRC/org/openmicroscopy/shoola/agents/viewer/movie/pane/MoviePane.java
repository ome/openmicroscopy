/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.TMoviePane
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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.movie.Player;
import org.openmicroscopy.shoola.agents.viewer.movie.defs.MovieSettings;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.GraphicSlider;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class MoviePane
    extends JPanel
{

    GraphicSlider               sliderT, sliderZ;
    
    /** TextField with the start (resp. end) value. */
    JTextField                  movieStartT, movieEndT;
    
    /** TextField with the start (resp. end) value. */
    JTextField                  movieStartZ, movieEndZ;
    
   JRadioButton                 movieZ, movieT;
    
    /** Width of a caracter. */
    private int                 txtWidth;

    private MoviePaneMng        manager;
    
    MoviePane(PlayerUIMng playerMng, Registry registry, int maxT, 
              int maxZ, MovieSettings settings)
    {
        manager = new MoviePaneMng(this, playerMng, registry, maxT, maxZ, 
                                    settings);
        initTxtWidth();
        initFields(maxT, maxZ, settings);
        initComponents(maxT, maxZ, settings);
        manager.attachListeners();
        buildGUI(maxT, maxZ);
    }

    /** Initializes the slider. */
    private void initComponents(int maxT, int maxZ, MovieSettings settings)
    {
        sliderT = new GraphicSlider(maxT, settings.getStartT(), 
                                    settings.getEndT());
        sliderT.setBackground(getBackground());
        sliderZ = new GraphicSlider(maxZ, settings.getStartZ(),
                                    settings.getEndZ());
        sliderZ.setBackground(getBackground());
        sliderZ.setKnobColor(new Color(0, 255, 0, 90));
        
        movieZ = new JRadioButton(" Z ");
        movieT = new JRadioButton(" T ");
        ButtonGroup group = new ButtonGroup();
        group.add(movieZ);
        group.add(movieT); 
        if (maxT == 0) {
            sliderZ.attachMouseListeners();
            movieZ.setSelected(true);
            movieT.setSelected(false);
        } else {
            sliderT.attachMouseListeners();
            movieT.setSelected(true);
            movieZ.setSelected(false);
        } 
        if (settings.getMovieIndex() == Player.MOVIE_Z) 
            movieZ.setSelected(true);
        else movieT.setSelected(true);
    }

    /** Initializes the text Fields displaying the current values. */
    private void initFields(int maxT, int maxZ, MovieSettings settings)
    {
        movieStartT = new JTextField(""+settings.getStartT(), 
                                    (""+maxT).length());
        movieStartT.setForeground(ViewerUIF.STEELBLUE);
        movieStartT.setToolTipText(
                UIUtilities.formatToolTipText("Enter the starting value."));
        movieEndT = new JTextField(""+settings.getEndT(), (""+maxT).length());
        movieEndT.setForeground(ViewerUIF.STEELBLUE);
        movieEndT.setToolTipText(
                UIUtilities.formatToolTipText("Enter the end value."));
        
        movieStartZ = new JTextField(""+settings.getStartZ(), 
                                    (""+maxZ).length());
        movieStartZ.setForeground(ViewerUIF.STEELBLUE);
        movieStartZ.setToolTipText(
                UIUtilities.formatToolTipText("Enter the starting value."));
        movieEndZ = new JTextField(""+settings.getEndZ(), (""+maxZ).length());
        movieEndZ.setForeground(ViewerUIF.STEELBLUE);
        movieEndZ.setToolTipText(
                UIUtilities.formatToolTipText("Enter the end value."));
        if (maxZ == 0) setFieldsEnabled(true, false);
        else {
            if (settings.getMovieIndex() == Player.MOVIE_Z)
                    setFieldsEnabled(false, true);
            else setFieldsEnabled(true, false);
        }
    }

    private void setFieldsEnabled(boolean forT, boolean forZ)
    {
        movieStartZ.setEnabled(forZ);
        movieEndZ.setEnabled(forZ); 
        movieStartT.setEnabled(forT);
        movieEndT.setEnabled(forT); 
    }

    private JPanel buildControlsMoviePanel(int length, JTextField start, 
                                            JTextField end)
    {
        JPanel p = new JPanel();
        JLabel l = new JLabel(" Start ");
        Insets insets = end.getInsets();
        int x = insets.left+length*txtWidth+insets.left;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.weightx = 0;        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        c.ipadx = x;
        c.weightx = 0.5;
        JPanel msp = UIUtilities.buildComponentPanel(start);
        gridbag.setConstraints(msp, c);
        p.add(msp);
        c.gridx = 2;
        c.ipadx = 0;
        c.weightx = 0;
        l = new JLabel(" End ");
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 3;
        c.ipadx = x;
        c.weightx = 0.5;
        msp = UIUtilities.buildComponentPanel(end);
        gridbag.setConstraints(msp, c);
        p.add(msp);
        return p; 
    }

    /** Initializes the width of the text. */
    private void initTxtWidth()
    {
        FontMetrics metrics = getFontMetrics(getFont());
        txtWidth = metrics.charWidth('m');
    }

    private JPanel buildGroupPanel(GraphicSlider slider, JRadioButton button,
                                    JPanel controls)
    {
        JPanel p = new JPanel(), group = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(slider.getUI());
        p.add(controls);
        group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
        group.add(button);
        group.add(p);
        return group;
    }

    /** Build and lay out the GUI. */
    private void buildGUI(int maxT, int maxZ)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = buildGroupPanel(sliderZ, movieZ, 
                    buildControlsMoviePanel((""+maxZ).length(), movieStartZ, 
                                            movieEndZ));
        add(p);
        p = buildGroupPanel(sliderT, movieT, 
                buildControlsMoviePanel((""+maxT).length(), movieStartT, 
                                        movieEndT));
        add(p);
        add(new JSeparator());
    }

}

