/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ChannelMovieAction
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Plays a movie across channels.
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
public class ChannelMovieAction
    extends ViewerAction
    implements PropertyChangeListener
{

    /** The description of the action. */
    private static final String DESCRIPTION = "Play movie across channels.";
    
    /** Helper reference to the icon manager. */
    private IconManager icons;
    
    /** Flag to indicate that we play a movie across channels. */
    private boolean play;
    
    /** 
     * Overridden to make sure that the movie player is not enabled when 
     * there is only one channel.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e)
    {
    	int state = model.getState();
    	switch (state) {
    		case ImViewer.DISCARDED:
    		case ImViewer.NEW:
    			setEnabled(false);
    			return;
    		case ImViewer.CHANNEL_MOVIE:
    			setEnabled(!model.isBigImage());
    			return;
		}
    	if (model.isBigImage()) {
    		setEnabled(false);
    		return;
    	}
        if (play) {
        	if (model.getActiveChannels().size() > 1)
        		setEnabled(true);
        } else {
            if (state == ImViewer.READY)
                setEnabled((model.getActiveChannels().size() > 1));
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public ChannelMovieAction(ImViewer model)
    {
        super(model);
        icons = IconManager.getInstance();
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PLAY));
        model.addPropertyChangeListener(ImViewer.CHANNEL_ACTIVE_PROPERTY, this);
    }

    /** 
     * Plays movie across channels.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Icon icon = (Icon) getValue(Action.SMALL_ICON);
        play = false;
        if (icon.toString().equals(
            icons.getIcon(IconManager.PLAY).toString())) {
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PAUSE));
            play = true;
        } else {
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PLAY));
        }  
        model.playChannelMovie(play);
    }

    /**
     * Reacts to propertyChange fired by the {@link ImViewer}.
     * 
     * @param evt The event to handle.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(ImViewer.CHANNEL_ACTIVE_PROPERTY)) {
            if (!play) {
                if (model.getState() == ImViewer.READY)
                    setEnabled(model.getActiveChannels().size() > 1);
            }    
        }
    }
    
}
