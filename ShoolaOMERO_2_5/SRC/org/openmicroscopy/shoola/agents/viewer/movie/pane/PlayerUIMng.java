/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.PlayerUIMng
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.movie.PlayerManager;

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
public class PlayerUIMng
{

    private PlayerManager   playerManager;
    
    private PlayerUI        view;
    
    public PlayerUIMng(PlayerUI view, PlayerManager playerManager)
    {
        this.view = view;
        this.playerManager = playerManager;
    }
    
    public void setBorderPlay(boolean b)
    {
        view.cPane.play.setBorderPainted(b);
    }
    
    public int getStartZ()
    {
        return Integer.parseInt(view.mPane.movieStartZ.getText());
    }
    
    public int getStartT()
    {
        return Integer.parseInt(view.mPane.movieStartT.getText());
    }
    
    public int getEndZ()
    {
        return Integer.parseInt(view.mPane.movieEndZ.getText());
    }
    
    public int getEndT()
    {
        return Integer.parseInt(view.mPane.movieEndT.getText());
    }
    
    /** Set the movie index.*/
    void setIndex(int index, int max, int startMovie, int endMovie)
    { 
        playerManager.setIndex(index, max, startMovie, endMovie);
    }
    
    /** Set the start value. */
    void setStartMovie(int v)
    { 
        stop();
        setBorderPlay(false);
        playerManager.setStartMovie(v);
    }
    
    /** Set the end value. */
    void setEndMovie(int v)
    { 
        stop();
        setBorderPlay(false);
        playerManager.setEndMovie(v);
    }
    
    /** Start playing the movie. */
    void play()
    { 
        if (!playerManager.isPlaying()) playerManager.play();
    }
    
    /** Stop playing the movie. */
    void pause()
    { 
        if (playerManager.isPlaying()) playerManager.pause(); 
    }
    
    /** Stop playing the movie. */
    void stop()
    { 
        if (playerManager.isPlaying()) playerManager.stop();
    }
    
    /** Set the movie type. */
    void setMovieType(int type)
    { 
        stop();
        setBorderPlay(false);
        playerManager.setMovieType(type);
    }
    
    /** Set the timer. */
    void setTimerDelay(int rate)
    {
        stop();
        setBorderPlay(false);
        playerManager.setTimerDelay(rate);
    } 
    
}
