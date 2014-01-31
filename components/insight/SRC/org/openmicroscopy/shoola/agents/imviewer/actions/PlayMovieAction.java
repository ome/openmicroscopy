/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.PlayMovieAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.actions;



//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Plays a movie without the movie displaying the player movie.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class PlayMovieAction
	extends ViewerAction
{

    /** Indicates to play the movie across Z. */
    public static final int ACROSS_Z = MoviePlayerDialog.ACROSS_Z;

    /** Indicates to play the movie across T. */
    public static final int ACROSS_T = MoviePlayerDialog.ACROSS_T;

    /** Indicates to play the movie across t. */
    public static final int ACROSS_LIFETIME = MoviePlayerDialog.ACROSS_BIN;

    /** The description of the action. */
    private static final String DESCRIPTION_ACROSS_Z = "Play movie across Z.";

    /** The description of the action. */
    private static final String DESCRIPTION_ACROSS_T = "Play movie across T.";

    /** The description of the action. */
    private static final String DESCRIPTION_ACROSS_BIN = "Play movie across t.";

	/** Helper reference to the icon manager. */
    private IconManager icons;

    /** One of the constants defined by this class. */
    private int index;

    /**
     * Checks if the passed index is valid.
     *
     * @param index The value to handle.
     */
    private void checkIndex(int index)
    {
    	switch (index) {
			case ACROSS_Z:
			case ACROSS_T:
			case ACROSS_LIFETIME:
				break;
			default:
				throw new IllegalArgumentException("Index not valid.");
		}
    }

    /**
     * Overridden to make sure that the movie player is not enabled when 
     * there is only one channel.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e)
    {
        switch (model.getState()) {
        case ImViewer.DISCARDED:
            break;
        case ImViewer.CHANNEL_MOVIE:
            setEnabled(false);
            break;
        case ImViewer.READY:
            if (model.isBigImage()) {
                setEnabled(false);
            } else {
                if (model.isPlayingMovie()) {
                    setEnabled(model.getMovieIndex() == index);
                } else {
                    switch (index) {
                    case ACROSS_LIFETIME:
                        setEnabled(model.getMaxLifetimeBin() > 1);
                        break;
                    case ACROSS_T:
                        setEnabled(model.getRealT() > 1);
                        break;
                    case ACROSS_Z:
                        setEnabled(model.getMaxZ() != 0);
                    }
                }
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param index	One of the constants defined by this class.
     */
	public PlayMovieAction(ImViewer model, int index)
	{
	    super(model);
	    checkIndex(index);
	    this.index = index;
	    icons = IconManager.getInstance();
	    switch (index) {
	    case ACROSS_T:
	        putValue(Action.SHORT_DESCRIPTION,
	                UIUtilities.formatToolTipText(DESCRIPTION_ACROSS_T));
	        break;
	    case ACROSS_Z:
	        putValue(Action.SHORT_DESCRIPTION,
	                UIUtilities.formatToolTipText(DESCRIPTION_ACROSS_Z));
	        break;
	    case ACROSS_LIFETIME:
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_ACROSS_BIN));
	    }

        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PLAY));
    }

	/**
	 * Sets the icon of the action.
	 * 
	 * @param b Pass <code>true</code> to set the <code>Play</code> icon,
	 * 			<code>false</code> to set the <code>Pause</code> icon
	 */
	public void setActionIcon(boolean b)
	{
		if (b)
			putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PLAY));
		else 
			putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PAUSE));
	}

	/**
     * Plays movie across z-sections or time-points.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	boolean b = true;
    	if (model.isPlayingMovie()) b = false;
    	setActionIcon(!b);
    	model.playMovie(b, false, index);
    }

}
