/*
 * org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * A non-modal dialog displaying the various controls to play a movie across
 * z-sections and timepoints.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class MoviePlayerDialog
    extends JDialog
{

	/** Bound property indicating that the dialog is closed. */
	public static final String CLOSE_PROPERTY = "close";

	/** 
	 * Bounds property indicating that the state of the player has changed.
	 */
	public static final String MOVIE_STATE_CHANGED_PROPERTY =
	        "movieStateChanged";
	
	/** Indicates to play movie across z-sections only. */
	public static final int ACROSS_Z = 300;
    
    /** Indicates to play movie across timepoints only. */
	public static final int ACROSS_T = 301;

	/** Indicates to play movie across small t only (lifetime). */
    public static final int ACROSS_BIN = 303;

    /** Indicates to play movie across z-sections and timepoint.s */
	public static final int ACROSS_ZT = 302;

	/** Indicates to perform a click to play the movie. */
	public static final int DO_CLICK_PLAY = 0;

	/** Indicates to perform a click to stop playing the movie. */
	public static final int DO_CLICK_PAUSE = 1;

    /** Reference to the component controlling the timer. */
    private MoviePlayer player;

    /** The UI delegate. */
    private MoviePlayerUI uiDelegate;

    /** Reference to the parent model. */
    private ImViewer model;

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        getContentPane().add(uiDelegate);
        pack();
    }

    /** Adds a window listener to stop timer if the window is closed. */
    private void initListeners()
    {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            { 
               player.setPlayerState(Player.STOP);
               firePropertyChange(CLOSE_PROPERTY, Boolean.FALSE, Boolean.TRUE);
            }});
    }

    /**
     * Creates a new instance.
     *
     * @param owner The owner of the this dialog.
     * @param model Reference to the {@link ImViewer}.
     *              Mustn't be <code>null</code>.
     */
    public MoviePlayerDialog(JFrame owner, ImViewer model)
    {
        super(owner);
        setResizable(false);
        if (model == null) throw new NullPointerException("No model.");
        setTitle("Movie Player: "+model.getImageName());
        this.model = model;
        player = new MoviePlayer(model, this);
        uiDelegate = new MoviePlayerUI(player);
        player.setStartT(model.getRealSelectedT());
        new MoviePlayerControl(player, uiDelegate);
        initListeners();
        buildGUI();
    }

    /**
     * Swaps the <code>Play</code> and <code>Pause</code> icons depending on the
     * specified flag.
     *
     * @param b Pass <code>true</code> to set the <code>Pause</code> icon
     *          <code>false</code> to set the <code>Play</code> icon.
     */
    void setMoviePlay(boolean b)
    { 
    	if (b)
    		firePropertyChange(MOVIE_STATE_CHANGED_PROPERTY,
    						Boolean.FALSE, Boolean.TRUE);
    	else firePropertyChange(MOVIE_STATE_CHANGED_PROPERTY,
				Boolean.TRUE, Boolean.FALSE);
        if (uiDelegate != null) uiDelegate.setMoviePlay(b);
    }

    /**
     * Fires an event to render the plane specified by the z-section,
     * timepoint and bin.
     */
    void renderImage()
    {
        int z = model.getDefaultZ();
        int t = model.getRealSelectedT();
        int bin = model.getSelectedBin();
        switch (player.getMovieIndex()) {
            case ACROSS_T:
                t = player.getFrameNumberT();
                break;
            case ACROSS_Z:
                z = player.getFrameNumberZ();
                break;
            case ACROSS_BIN:
               bin = player.getFrameNumberBin();
                break;
            case ACROSS_ZT:
                z = player.getFrameNumberZ();
                t = player.getFrameNumberT();
        }
        model.setSelectedXYPlane(z, t, bin);
    }

    /** Notifies that the state has changed. */
    void notifyPlayerStateChange() {}

    /**
     * Sets the index of the movie player.
     * 
     * @param index The index to set.
     */
    public void setMovieIndex(int index)
    {
    	if (uiDelegate != null) {
    		uiDelegate.setMovieIndex(index);
    		uiDelegate.setDefaultMovieType();
    	}
    	player.setMovieIndex(index);
    }

    /**
     * Returns the movie index.
     * 
     * @return See above.
     */
    public int getMovieIndex() { return player.getMovieIndex(); }
    
    /** 
     * Sets the start and end timepoint.
     * 
     * @param start The starting point.
     * @param end	The end point.
     */
    public void setTimeRange(int start, int end)
    {
    	if (start < player.getMinT() || start >= end || end > player.getMaxT())
    		return;
    	player.setEndT(end);
    	player.setStartT(start);
    	if (uiDelegate != null) {
    		uiDelegate.setEndT(end);
    		uiDelegate.setStartT(start);
    	}
    }
    
    /** 
     * Sets the start and end Z-section.
     * 
     * @param start The starting point.
     * @param end	The end point.
     */
    public void setZRange(int start, int end)
    {
    	if (start < player.getMinZ() || start >= end || end > player.getMaxZ())
    		return;
    	player.setEndZ(end);
    	player.setStartZ(start);
    	if (uiDelegate != null) {
    		uiDelegate.setEndZ(end);
    		uiDelegate.setStartZ(start);
    	}
	}

    /** 
     * Sets the start and end bin value.
     * 
     * @param start The starting point.
     * @param end   The end point.
     */
    public void setBinRange(int start, int end)
    {
        if (start < player.getMinBin() || start >= end ||
            end > player.getMaxBin())
            return;
        player.setEndBin(end);
        player.setStartBin(start);
    }

    /**
     * Performs a click on the button corresponding to the passed index.
     * 
     * @param index One out of the following constants: {@link #DO_CLICK_PLAY}
     * 				or {@link #DO_CLICK_PAUSE}.
     */
    public void doClick(int index)
    {
    	switch (index) {
			case DO_CLICK_PAUSE:
			case DO_CLICK_PLAY:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    	if (uiDelegate != null) uiDelegate.doClick(index);
    }

}
