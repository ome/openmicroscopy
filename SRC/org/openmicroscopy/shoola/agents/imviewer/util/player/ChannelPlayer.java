/*
 * org.openmicroscopy.shoola.agents.imviewer.util.player.ChannelPlayer
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

package org.openmicroscopy.shoola.agents.imviewer.util.player;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * Basic timer to play a movie across channels.
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
public class ChannelPlayer
    extends Player
{

    /** The index of the channel. */
    private int         index;
    
    /** The list of active channels. */
    private List        activeChannels;
    
    /** The index of the played channel. */
    private int         n;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link ImViewer}.
     *              Mustn't be <code>null</code>.
     */
    public ChannelPlayer(ImViewer model)
    {
        super(model);
        activeChannels = model.getActiveChannels();
        n = 0;
        if (activeChannels != null && activeChannels.size() > 0)
            index = ((Integer) activeChannels.get(n)).intValue();
    }
     
    /**
     * Returns the list of channels used to play this movie.
     * 
     * @return See above.
     */
    public List getChannels() { return activeChannels; }
    
    /** Starts/Stops the timer. */
    protected void onPlayerStateChange()
    {
        switch (state) {
            case START:
                timer.start();
                break;
            case STOP:
                timer.stop();  
                break;
        }  
    }
    
    /**
     * Reacts to action event fired by the timer.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (activeChannels == null || activeChannels.size() == 0) return;
        for (int j = 0; j < model.getMaxC(); j++)
            model.setChannelActive(j, j == index);
        model.displayChannelMovie();
        n++;
        if (n == activeChannels.size()) n = 0;
        index = ((Integer) activeChannels.get(n)).intValue();
    }
  
}
