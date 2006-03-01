/*
 * org.openmicroscopy.shoola.agents.viewer.movie.Dummy
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

package org.openmicroscopy.shoola.agents.viewer.movie;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BoxLayout;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.movie.defs.MovieSettings;
import org.openmicroscopy.shoola.agents.viewer.movie.pane.PlayerUI;

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
public class Player
	extends JDialog
{
    

    /** Movie type constants. */
    public static final int         LOOP = 0, BACKWARD = 1, FORWARD = 2, 
                                    PINGPONG = 3;
    
    /** Action command ID to bring up the movie widget. */
    public static final int         MOVIE_T = 100;
    
    /** Action command ID to bring up the movie widget. */
    public static final int         MOVIE_Z = 101;
    
    /** Rate constants. */
    public static final int         FPS_INIT = 12, FPS_MIN = 1;
    
    private PlayerUI                player;
    
    private PlayerManager           manager;
    
    /** Initializes the components. */
    private void init(ViewerCtrl control, int maxT, int maxZ, 
                    MovieSettings settings)
    {
        int max = maxT;
        int s = settings.getStartT(), e = settings.getEndT();
        if (maxT == 0) {
            s = settings.getStartZ();
            e = settings.getEndZ();
            max = maxZ;
        }
        manager = new PlayerManager(this, control, max, 
                                settings.getMovieIndex(), s, e);
        player = new PlayerUI(manager, control.getRegistry(), maxT, maxZ, 
                            settings); 
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        getContentPane().add(player, BorderLayout.CENTER);
        pack();
    }
    
	/**
	 * 
	 * @param control	reference to the {@link ViewerCtrl control}.
	 * @param maxValue	timepoint-1 b/c OME values start at 0 or section s-1.
	 */
	public Player(ViewerCtrl control, int maxT, int maxZ, 
                MovieSettings settings)
	{
		super(control.getReferenceFrame(), "Movie player");
		init(control, maxT, maxZ, settings);
		buildGUI();
	}
	
    public void close()
    {
        manager.stop();
        setVisible(false);
        dispose();
    }
    
}
