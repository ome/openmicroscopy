/*
 * org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayer
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
    
    /** Indicates to play the movie in loop starting from the end point. */
    protected static final int      LOOP_BACKWARD = 204;
    
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
    
    /** The selected bin.*/
    private int frameNumberBin;

    /** The start bin. */
    private int startBin;

    /** The end bin. */
    private int endBin;
    
    /** Initializes the default values. */
    private void initialize()
    {
        up = true;
        movieType = FORWARD;
        timerDelay = FPS_INIT;
        startT = model.getRealSelectedT();
        endT = getMaxT();
        startZ = model.getDefaultZ();
        endZ = getMaxZ();
        startBin = model.getSelectedBin();
        endBin = model.getMaxLifetimeBin();
        if (timerDelay > getMaximumTimer()) timerDelay = getMaximumTimer();
        setTimerDelay(timerDelay);
        if (getMaxT() > 1) index = MoviePlayerDialog.ACROSS_T;
        else if (getMaxZ() != 0) index = MoviePlayerDialog.ACROSS_Z;
        else if (getMaxBin() > 1) index = MoviePlayerDialog.ACROSS_BIN;
    }
    
    /** Resets the frame number depending on the movie type. */
    private void setFrameNumbers()
    {
    	switch (movieType) {
			case BACKWARD:
			case LOOP_BACKWARD:
				frameNumberZ = endZ;
	            frameNumberT = endT;
	            frameNumberBin = endBin;
				break;
			default:
			    frameNumberBin = startBin;
				frameNumberZ = startZ;
            	frameNumberT = startT;
		}
    }

    /** Plays movie across bins. */
    private void playMovieAcrossBin()
    {
        switch (movieType) {
            case LOOP:
                if (frameNumberBin == endBin) frameNumberBin = startBin;
                else frameNumberBin++;
                break;
            case LOOP_BACKWARD:
                if (frameNumberBin == startZ) frameNumberBin = endBin;
                else frameNumberBin--;
                break;
            case BACKWARD:
                if (frameNumberBin == startBin) {
                    frameNumberBin = endBin;
                    setPlayerState(Player.STOP);
                } else frameNumberBin--;
                break;
            case FORWARD:
                if (frameNumberBin == endBin) {
                    frameNumberBin = startBin; 
                    setPlayerState(Player.STOP);
                } else frameNumberBin++;
                break;
            case PINGPONG:
                if (frameNumberBin < endBin && up) frameNumberBin++;
                else if (frameNumberBin > startBin && !up) frameNumberBin--;
                else if (frameNumberBin == endBin && up) {
                    frameNumberBin--;
                    up = false;
                } else if (frameNumberBin == startBin && !up) {
                    frameNumberBin++;
                    up = true;
                }
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
            case LOOP_BACKWARD:
            	if (frameNumberZ == startZ) frameNumberZ = endZ;
                else frameNumberZ--;
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
            case LOOP_BACKWARD:
                if (frameNumberT == startT) frameNumberT = endT;
                else frameNumberT--;
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
            case LOOP_BACKWARD: 
                if (frameNumberZ == startZ) {
                    frameNumberZ = endZ;
                    if (frameNumberT == startT) {
                        frameNumberT = endT; 
                    } else frameNumberT--;
                } else frameNumberZ--;
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
     * Creates a new instance.
     * 
     * @param model Reference to the {@link ImViewer}.
     *              Mustn't be <code>null</code>.
     * @param parent Reference to frame hosting this movie player.
     *              Mustn't be <code>null</code>.
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
     * The maximum value of the timer.
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
     * Returns the maximum number of timepoints.
     * 
     * @return See above.
     */
    int getMaxT() { return model.getRealT()-1; }

    /**
     * Returns the maximum number of z-sections minus 1.
     * 
     * @return See above.
     */
    int getMaxZ() { return model.getMaxZ(); }

    /**
     * Returns the maximum number of bins.
     * 
     * @return See above.
     */
    int getMaxBin() { return model.getMaxLifetimeBin(); }

    /**
     * Returns the minimum value of bin.
     * 
     * @return See above.
     */
    int getMinBin() { return 0; }

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
    	startT = v;
        setPlayerState(Player.STOP);
        setFrameNumbers();
    }

    /**
     * Sets the start z-section.
     * 
     * @param v The value to set.
     */
    void setStartZ(int v)
    {
    	startZ = v;
    	setPlayerState(Player.STOP);
    	setFrameNumbers();
    }

    /**
     * Sets the start bin.
     * 
     * @param v The value to set.
     */
    void setStartBin(int v)
    {
        startBin = v;
        setPlayerState(Player.STOP);
        setFrameNumbers();
    }

    /**
     * Sets the end timepoint.
     * 
     * @param v The value to set.
     */
    void setEndT(int v)
    {
    	endT = v;
        setPlayerState(Player.STOP);
        setFrameNumbers();
    }

    /**
     * Sets the end z-section.
     * 
     * @param v The value to set.
     */
    void setEndZ(int v)
    {
        endZ = v;
        setPlayerState(Player.STOP);
        setFrameNumbers();
    }

    /**
     * Sets the end bin.
     * 
     * @param v The value to set.
     */
    void setEndBin(int v)
    {
        endBin = v;
        setPlayerState(Player.STOP);
        setFrameNumbers();
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
     * Returns the current bin.
     * 
     * @return See above.
     */
    int getFrameNumberBin() { return frameNumberBin; }

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
     * Overridden to play the movie.
     * @see Player#onPlayerStateChange()
     */
    protected void onPlayerStateChange()
    {
        switch (state) {
            case START:
                parent.setMoviePlay(true);
                timer.start();
                break;
            case STOP:
                parent.setMoviePlay(false);
                timer.stop();
                setFrameNumbers();
                up = true;
                break;
            case PAUSE:
                parent.setMoviePlay(false);
                timer.stop();
        }
    }

    /**
     * Plays movie depending on the movie index.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        switch (index) {
            case MoviePlayerDialog.ACROSS_Z:
                if (frameNumberZ <= getMaxZ() && frameNumberZ >= startZ
                        && frameNumberZ <= endZ && state == Player.START) {
                    parent.renderImage();
                    playMovieAcrossZ();
                } 
                break;
            case MoviePlayerDialog.ACROSS_T:
                if (frameNumberT <= getMaxT() && frameNumberT >= startT
                        && frameNumberT <= endT && state == Player.START) {
                    parent.renderImage();
                    playMovieAcrossT();
                }
                break;
            case MoviePlayerDialog.ACROSS_BIN:
                if (frameNumberBin <= getMaxBin() && frameNumberBin >= startBin
                        && frameNumberBin <= endBin && state == Player.START) {
                    parent.renderImage();
                    playMovieAcrossBin();
                }
                break;
            case MoviePlayerDialog.ACROSS_ZT:
                parent.renderImage();
                playMovieAcrossZT();
                break;
        }
    }

}
