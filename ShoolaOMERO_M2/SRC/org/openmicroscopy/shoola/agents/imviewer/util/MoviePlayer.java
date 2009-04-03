/*
 * org.openmicroscopy.shoola.agents.imviewer.util.MoviePlayer
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
import java.awt.event.ActionEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * Player to play movies across z-sections/timepoints.
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
class MoviePlayer
    extends Player
{

    /** Minimal value of the spinner. */
    public static final int         FPS_MIN = 1;
    
    /** Initial value of the spinner. */
    public static final int         FPS_INIT = 12;
    
    /** Indicates to play the movie in loops. */
    protected static final int      LOOP = 200;
    
    /** Indicates to play the movie from end to start. */
    protected static final int      BACKWARD = 201;
    
    /** Indicates to play the movie from start to end. */
    protected static final int      FORWARD = 202;
    
    /** Indicates to play the movie round trip. */
    protected static final int      PINGPONG = 203;
    
    /** Indicates to play movie across z-sections only. */
    static final int                ACROSS_Z = 300;
    
    /** Indicates to play movie across timepoints only. */
    static final int                ACROSS_T = 301;
    
    /** Indicates to play movie across z-sections and timepoint.s */
    static final int                ACROSS_ZT = 302;
    
    /** The start z-section. */
    private int                 startZ;
    
    /** The end z-section. */
    private int                 endZ;
    
    /** The start timepoint. */
    private int                 startT;
    
    /** The end timepoint. */
    private int                 endT;
    
    /** The timer delay used for display. */
    private int                 timerDelay;
    
    /** The current z-section. */
    private int                 frameNumberZ;
    
    /** The current timepoint. */
    private int                 frameNumberT;
    
    /** The movie index. One of the following constants {@link #ACROSS_T},
     * {@link #ACROSS_Z} or {@link #ACROSS_ZT}.
     */
    private int                 index; 
    
    /** 
     * One of the following constants: 
     * {@link #LOOP}, {@link #BACKWARD}, {@link #FORWARD} or {@link #PINGPONG}.
     */
    private int                 movieType;
    
    /** 
     * Flag used to control the direction of play when the movie is played 
     * backward and forward.
     */
    private boolean             up;
    
    /** Reference to the frame hosting this movie player. */
    private MoviePlayerDialog   parent;
    
    /** Initializes the default values. */
    private void initialize()
    {
        up = true;
        movieType = FORWARD;
        timerDelay = FPS_INIT;
        startT = getMinT();
        endT = getMaxT();
        startZ = getMinZ();
        endZ = getMaxZ();
        if (timerDelay > getMaximumTimer()) timerDelay = getMaximumTimer();
        setTimerDelay(timerDelay);
        if (getMaxZ() != 0) index = ACROSS_Z;
        else if (getMaxT() != 0) index = ACROSS_T;
    }
    
    /** Resets the frame number depending on the movie type. */
    private void setFrameNumbers()
    {
        if (movieType == BACKWARD) {
            frameNumberZ = endZ;
            frameNumberT = endT;
        } else {
            frameNumberZ = startZ;
            frameNumberT = startT;
        }  
    }
    
    /** Plays movie across z-sections. */
    private void playMovieAcrossZ()
    {
        switch (movieType) {
            case LOOP:
                if (frameNumberZ == endZ) frameNumberZ = startZ;
                else frameNumberZ++;
                break;
            case BACKWARD:
                if (frameNumberZ == startZ) {
                    frameNumberZ = endZ;
                    setPlayerState(Player.STOP);
                } else frameNumberZ--;
                break;
            case FORWARD:
                if (frameNumberZ == endZ) {
                    frameNumberZ = startZ; 
                    setPlayerState(Player.STOP);
                } else frameNumberZ++;
                break;
            case PINGPONG:
                if (frameNumberZ < endZ && up) frameNumberZ++;
                else if (frameNumberZ > startZ && !up) frameNumberZ--;
                else if (frameNumberZ == endZ && up) {
                    frameNumberZ--;
                    up = false;
                } else if (frameNumberZ == startZ && !up) {
                    frameNumberZ++;
                    up = true;
                }   
        }
    }
    
    /** Plays movie across timepoints. */
    private void playMovieAcrossT()
    {
        switch (movieType) {
            case LOOP:
                if (frameNumberT == endT) frameNumberT = startT;
                else frameNumberT++;
                break;
            case BACKWARD:
                if (frameNumberT == startT) {
                    frameNumberT = endT;
                    setPlayerState(Player.STOP);
                } else frameNumberT--;
                break;
            case FORWARD:
                if (frameNumberT == endT) {
                    frameNumberT = startT; 
                    setPlayerState(Player.STOP);
                } else frameNumberT++;
                break;
            case PINGPONG:
                if (frameNumberT < endT && up) frameNumberT++;
                else if (frameNumberT > startT && !up) frameNumberT--;
                else if (frameNumberT == endT && up) {
                    frameNumberT--;
                    up = false;
                } else if (frameNumberT == startT && !up) {
                    frameNumberT++;
                    up = true;
                }   
        }
    }
    
    /** Plays movie across z-sections and timepoints. */
    private void playMovieAcrossZT()
    {
        switch (movieType) {
            case LOOP: 
                if (frameNumberZ == endZ) {
                    frameNumberZ = startZ;
                    if (frameNumberT == endT) {
                        frameNumberT = startT; 
                    } else frameNumberT++;
                } else frameNumberZ++;
                break;
            case BACKWARD: 
                if (frameNumberZ == startZ) {
                    frameNumberZ = endZ;
                    if (frameNumberT == startT) {
                        frameNumberT = endT;
                        setPlayerState(Player.STOP);
                    } else frameNumberT--;
                } else frameNumberZ--;
                break;
            case FORWARD: 
                if (frameNumberZ == endZ) {
                    frameNumberZ = startZ; 
                    if (frameNumberT == endT) {
                        frameNumberT = startT; 
                        setPlayerState(Player.STOP);
                    } else frameNumberT++;
                } else frameNumberZ++;                        
                break;
            case PINGPONG: 
                if (up) {
                    if (frameNumberZ == endZ) {
                        if (frameNumberT == endT) {
                            frameNumberZ--;
                            up = false;
                        } else {
                            frameNumberZ = startZ;
                            frameNumberT++;
                        }
                    } else frameNumberZ++;
                } else {
                    if (frameNumberZ == startZ) {
                        if (frameNumberT == startT) {
                            frameNumberZ++;
                            up = true;
                        } else {
                            frameNumberZ = endZ;
                            frameNumberT--;
                        }
                    } else frameNumberZ--;
                }
        }
    }
    
    /**
     * Plays movie.
     * @see Player#onPlayerStateChange()
     */
    protected void onPlayerStateChange()
    {
        switch (state) {
            case START:
                if (historyState == Player.PAUSE) {
                    switch (index) {
                        case ACROSS_Z:
                            //if (movieType == BACKWARD) frameNumberZ = endZ;
                            //else frameNumberZ = startZ;
                            break;
                        case ACROSS_T:
                            //if (movieType == BACKWARD) frameNumberT = endT;
                            //else frameNumberT = startT;
                            break;
                    }
                }
                parent.setMoviePlay(true);
                timer.start();
                break;
            case STOP:
                parent.setMoviePlay(false);
                timer.stop();
                setFrameNumbers();
                up = true;
        }  
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the {@link ImViewer}.
     *                  Mustn't be <code>null</code>.
     * @param parent    Reference to frame hosting this movie player.
     *                  Mustn't be <code>null</code>.
     */
    MoviePlayer(ImViewer model, MoviePlayerDialog parent)
    {
        super(model);
        this.parent = parent;
        initialize();
        setFrameNumbers();
    }
    
    /**
     * Returns the timer's delay.
     * 
     * @return See above.
     */
    int getTimerDelay() { return timerDelay; }
    
    /**
     * The maixmum value of the timer.
     * 
     * @return See above.
     */
    int getMaximumTimer() { return Math.max(getMaxZ(), getMaxT()); }
    
    /**
     * Returns the type of movie currently played. One of the following 
     * constants: {@link #LOOP}, {@link #BACKWARD}, {@link #FORWARD} or 
     * {@link #PINGPONG}.
     * 
     * @return See above.
     */
    int getMovieType() { return movieType; }
    
    /**
     * Returns the maximum number of timepoints minus 1.
     * 
     * @return See above.
     */
    int getMaxT() { return model.getMaxT(); }
    
    /**
     * Returns the maximum number of z-sections minus 1.
     * 
     * @return See above.
     */
    int getMaxZ() { return model.getMaxZ(); }
    
    /**
     * Returns the minimum value of z-sections.
     * 
     * @return See above.
     */
    int getMinZ() { return 0; }
    
    /**
     * Returns the minimum value of timepoints.
     * 
     * @return See above.
     */
    int getMinT() { return 0; }
    
    /**
     * Sets the start timepoint.
     * 
     * @param v The value to set.
     */
    void setStartT(int v)
    {
        setPlayerState(Player.STOP);
        startT = v;
    }
    
    /**
     * Sets the start z-section.
     * 
     * @param v The value to set.
     */
    void setStartZ(int v)
    {
        setPlayerState(Player.STOP);
        startZ = v;
    }
    
    /**
     * Sets the end timepoint.
     * 
     * @param v The value to set.
     */
    void setEndT(int v)
    {
        setPlayerState(Player.STOP);
        endT = v;
    }
    
    /**
     * Sets the end z-section.
     * 
     * @param v The value to set.
     */
    void setEndZ(int v)
    {
        setPlayerState(Player.STOP);
        endZ = v;
    }
    
    /**
     * Sets the delay of the timer.
     * 
     * @param delay
     */
    void setTimerDelay(int delay)
    {
        setPlayerState(Player.STOP);
        timerDelay = delay;
        setDelay(delay);
    }
    
    /**
     * Sets the type of movie to play.
     * 
     * @param type The value to set.
     */
    void setMovieType(int type)
    {
       setPlayerState(Player.STOP);
       movieType = type;
    }
    
    /**
     * Returns the starting timepoint.
     * 
     * @return See above.
     */
    int getStartT() { return startT; }
    
    /**
     * Returns the ending timepoint.
     * 
     * @return See above.
     */
    int getEndT() { return endT; }
    
    /**
     * Returns the starting z-section.
     * 
     * @return See above.
     */
    int getStartZ() { return startZ; }
    
    /**
     * Returns the ending z-section.
     * 
     * @return See above.
     */
    int getEndZ() { return endZ; }
    
    /**
     * Returns the movie index. One of the following constants:
     * {@link #ACROSS_T}, {@link #ACROSS_Z} or {@link #ACROSS_ZT}.
     * 
     * @return See above.
     */
    int getMovieIndex() { return index; }
    
    /**
     * Sets the movie index. One of the following constants:
     * {@link #ACROSS_T}, {@link #ACROSS_Z} or {@link #ACROSS_ZT}.
     * 
     * @param index The value to set.
     */
    void setMovieIndex(int index)
    {
        if (this.index == index) return;
        setPlayerState(Player.STOP);
        this.index = index;
    }
    
    /**
     * Returns the current z-section.
     * 
     * @return See above.
     */
    int getFrameNumberZ() { return frameNumberZ; }
    
    /**
     * Returns the current timepoint.
     * 
     * @return See above.
     */
    int getFrameNumberT() { return frameNumberT; }
    

    
    
    /**
     * Plays movie depending on the movie index.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        switch (index) {
            case ACROSS_Z:
                if (frameNumberZ <= getMaxZ() && frameNumberZ >= startZ
                        && frameNumberZ <= endZ && state == Player.START) {
                    parent.renderImage();
                    playMovieAcrossZ();
                } 
                break;
            case ACROSS_T:
                if (frameNumberT <= getMaxT() && frameNumberT >= startT
                        && frameNumberT <= endT && state == Player.START) {
                    parent.renderImage();
                    playMovieAcrossT();
                }
                break;
            case ACROSS_ZT:
                parent.renderImage();
                playMovieAcrossZT();
                break;
        }
    }
    
}
