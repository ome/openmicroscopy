/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.MovieAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
 * Brings up the widget to play a movie.
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
public class MovieAction
    extends ViewerAction
{

    /** The name of the action. */
    private static final String NAME = "Movie...";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Bring up the movie player.";
    
    /** 
     * Sets the enabled flag depending on the tab selected.
     * @see ViewerAction#onTabSelection()
     */
    protected void onTabSelection()
    {
    	if (model.isBigImage()) {
    		setEnabled(false);
    		return;
    	}
    	if (model.getSelectedIndex() == ImViewer.PROJECTION_INDEX) {
			setEnabled(false);
		} else {
			if (model.isPlayingMovie()) setEnabled(false);
			else {
				int max = Math.max(model.getMaxZ(), model.getRealT());
				setEnabled(max > 1);
			}
		}
    }
    
    /**
     * Sets the enabled flag depending on the tab selected.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e)
    {
    	switch (model.getState()) {
			case ImViewer.CHANNEL_MOVIE:
				setEnabled(false);
				break;
			case ImViewer.READY:
				onTabSelection();
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public MovieAction(ImViewer model)
    {
        super(model, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager icons = IconManager.getInstance();
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.MOVIE));
    }
    
    /** 
     * Brings the movie player on screen.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	if (!model.isPlayingMovie()) model.playMovie(true, true, -1);
    }

}
