/*
 * org.openmicroscopy.shoola.agents.viewer.movie.PlayerManager
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Timer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.movie.pane.PlayerUIMng;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage;

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
public class PlayerManager
	implements ActionListener
{
	
    private static final int    DELAY = 1000;
    
	private Player				view;
    
	private ViewerCtrl			control;
	
	private int					delay;
    
	private boolean				playing, pause, up;
    
	private Timer 				timer;
    
    /** current image dispayed. */
	private int 				frameNumber;

	private int					max;
	
	private int					curValue;
	
	private int					startMovie, endMovie;
    
    /** One the constants defined above. */
    private int                 movieType;
    
    private int                 index;
    
    //private int                 pixelsID;
    
    private PlayerUIMng         playerUIMng;
    
	/**
	 * 
	 * @param view		reference to the view.
	 * @param control	reference to the {@link ViewerCtrl control}.
     * @param index     one the contants {@link #MOVIE_T} or {@link #MOVIE_Z}.
	 */
	public PlayerManager(Player view, ViewerCtrl control, int max, int index, 
                        int s, int e)
	{
		this.view = view;
		this.control = control;
        setIndex(index, max, s, e);
        movieType = Player.LOOP;
		playing = false;
        pause = false;
        up = true;
		delay = DELAY/Player.FPS_INIT;
		//Set up a timer that calls this object's action handler.
		timer = new Timer(delay, this);
		timer.setInitialDelay(delay);//*10??
		timer.setCoalesce(true);
        attachListener();
	}
	
    public void setPlayerUIMng(PlayerUIMng mng)
    {
        playerUIMng = mng;
    }
    
    public void setIndex(int index, int max, int s, int e)
    { 
        this.index = index;
        this.max = max; 
        setStartMovie(s);
        setEndMovie(e);
        if (index == Player.MOVIE_Z) curValue = control.getDefaultT();
        else  curValue = control.getDefaultZ();   
    }
    
    /** Attach a window listener. */
    private void attachListener()
    {
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { onClosing(); }
        });
    }

	public boolean isPlaying() { return playing; }
	
    /** One of the constants defined above. */
    public void setMovieType(int type)
    { 
        movieType = type;
        up = true;
     }
    
	public void setStartMovie(int startMovie) { this.startMovie = startMovie; }
	
	public void setEndMovie(int endMovie) { this.endMovie = endMovie; }
	
	/** Reset the timer delay. */
	public void setTimerDelay(int v)
	{
		delay = DELAY/v;
		timer.setDelay(delay);
        timer.setInitialDelay(delay*10); //have to check if we keep it
	}
	
	/** Play the movie. */
	public void play()
    { 
       if (!pause) setStartFrameNumber();
       pause = false;
       playing = true;
       timer.start();
    }
	
    /** 
     * Stop playing the movie. In the case we don't reset the
     *  <code>frameNumber<code> to <code>startMovie</code> or 
     * <code>endMovie</code>.
     */
    public void pause()
    {
        playing = false;
        pause = true;
        syncSlider();
    }
    
	/** Stop the movie. */
	public void stop()
    {
        timer.stop();
        playing = false;
        pause = false;
        up = true;
        syncSlider();     
    }
    	
	/** Handle event fired by timer. Advance the animation frame. */
	public void actionPerformed(ActionEvent e)
	{
        
        if (frameNumber <= max && frameNumber >= startMovie && 
            frameNumber <= endMovie && playing)
        {
            
            renderImage(frameNumber);
            switch (movieType) {
                case Player.LOOP:
                    handleLoop();
                    break;
                case Player.BACKWARD:
                    handleBackward();
                    break;
                case Player.FORWARD:
                    handleForward();
                    break;
                case Player.PINGPONG:
                    handlePingPong();
                    break;
            }
        }
	}
    
    /** Handle <code>ping-pong selection</code> selection. */
    private void handlePingPong()
    {
       if (frameNumber < endMovie && up) frameNumber++;
       else if (frameNumber > startMovie && !up) frameNumber--;
       else if (frameNumber == endMovie && up) {
           frameNumber--;
           up = false;
       } else if (frameNumber == startMovie && !up) {
           frameNumber++;
           up = true;
       }   
    }
    
    /** Handle <code>loop</code> selection. */
    private void handleLoop()
    {
        if (frameNumber == endMovie) frameNumber = startMovie;
        else frameNumber++;
    }
    
    /** Handle <code>forward</code> selection. */
    private void handleForward()
    {
        if (frameNumber == endMovie) {
            playing = false;
            timer.stop();
            frameNumber = startMovie;      
            setBorderPlay(false);
            
        } else frameNumber++;
    }
    
    /** Handle <code>backward</code> selection. */
    private void handleBackward()
    {
        if (frameNumber == startMovie) {
            frameNumber = endMovie;
            playing = false;
            timer.stop();
            setBorderPlay(false);
        } else frameNumber--;
    }
    
    /** Synchronizes the tSlider or zSlider according to the index selected. */
    private void syncSlider()
    {
        if (index == Player.MOVIE_T) control.resetTSlider(frameNumber);
        else control.resetZSlider(frameNumber); 
    }
    
    private void setBorderPlay(boolean b)
    {
        playerUIMng.setBorderPlay(b);
    }
    
    /** Set the starting point of the movie. */
    private void setStartFrameNumber()
    {
        frameNumber = startMovie;
        if (movieType == Player.BACKWARD) frameNumber = endMovie;
    }
    
	/** 
     * Post a {@link RenderImage} event and update the textField according to 
     * the index.
     */
	private void renderImage(int v)
	{
        int t = v, z = curValue;
        if (index == Player.MOVIE_Z) {
            t = curValue;
            z = v;
        }
        control.renderImage(z, t);
	}

    /** Window closed. */
    private void onClosing()
    {
        timer.stop();
        //Save the settings if they want to play the movie again.
        control.setMovieSettings(playerUIMng.getStartZ(), playerUIMng.getEndZ(),
                                playerUIMng.getStartT(), playerUIMng.getEndT(), 
                                movieType, index, (DELAY/delay));
        view.dispose();
    }
    
}
