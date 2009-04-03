/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.PlayMovieAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Plays a movei without the movie displaying the player movie.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PlayMovieAction
	extends ViewerAction
{

	/** The description of the action. */
    private static final String DESCRIPTION = "Play movie.";
    
	/** Helper reference to the icon manager. */
    private IconManager icons;
    
    /** 
     * Overriden to make sure that the movie player is not enabled when 
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
			case ImViewer.RENDERING_CONTROL_LOADED:
			case ImViewer.READY:
				setEnabled(model.getMaxT() != 0);
				break;
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
	public PlayMovieAction(ImViewer model)
    {
        super(model);
        icons = IconManager.getInstance();
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
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
     * Plays movie.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       if (model.isMoviePlaying()) {
    	   setActionIcon(true);
    	   model.playMovie(false, false);
       } else {
    	   setActionIcon(false);
    	   model.playMovie(true, false);
       } 
    }
	
}
