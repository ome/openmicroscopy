/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.PLayerUI
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.movie.PlayerManager;
import org.openmicroscopy.shoola.agents.viewer.movie.defs.MovieSettings;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Top panel, initializes and lay out the two panels: {@link ControlsPane}
 * and {@link MoviePane}.
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
public class PlayerUI
    extends JPanel
{

    private static final Dimension  V_SPACE = new Dimension(0, 20);
    
    ControlsPane                    cPane;
    
    MoviePane                       mPane;
   
    private PlayerUIMng             manager;
    
    public PlayerUI(PlayerManager playerManager, Registry reg, int maxT, 
                    int maxZ, MovieSettings settings)
    {
        manager = new PlayerUIMng(this, playerManager);
        playerManager.setPlayerUIMng(manager);
        init(reg, maxT, maxZ, settings);
        buildGUI();
    }
    
    /** Initialize the two GUI components. */
    private void init(Registry reg, int maxT, int maxZ, MovieSettings settings)
    {
        int max = Math.max(maxT, maxZ);
        cPane = new ControlsPane(manager, reg, max, settings.getRate(), 
                                    settings.getMovieType());
        mPane = new MoviePane(manager, reg, maxT, maxZ, settings);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(mPane);
        add(Box.createRigidArea(V_SPACE));
        add(cPane);
    }
    
}
