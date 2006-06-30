/*
 * org.openmicroscopy.shoola.agents.imviewer.util.Player
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
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * 
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
public abstract class Player
    implements ActionListener, ChangeListener
{

    /** Indicates the <i>Start</i> state of the timer. */
    public static final int     START = 0;
    
    /** Indicates the <i>Stop</i> state of the timer. */
    public static final int     STOP = 1;
    

    /** The delay between to image. */
    protected static final int  DELAY = 1000;
    
    /** The initial delay. */
    protected static final int  INITIAL_DELAY = 0;
    
    /** Reference to the {@link ImViewer}. */
    protected ImViewer  model;
    
    /** The timer controlling the display. */
    protected Timer     timer;
    
    /** The state of the timer. */
    protected int       state;
    
    
    /**
     * Subclasses should override the method to synchronize the timer.
     */
    protected abstract void onPlayerStateChange();
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link ImViewer}.
     *              Mustn't be <code>null</code>.
     */
    public Player(ImViewer model)
    {
        if (model == null) 
            throw new IllegalArgumentException("Model cannot be null.");
        this.model = model;
        state = -1;
        model.addChangeListener(this);
        timer = new Timer(DELAY, this);
        timer.setInitialDelay(INITIAL_DELAY);
        timer.setCoalesce(true);
    }

    /**
     * Sets the state of the player.
     * 
     * @param state One of the constants defined by this class.
     */
    public void setPlayerState(int state)
    {
        switch (state) {
            case START:
            case STOP: 
                break;
            default:
                throw new IllegalArgumentException("State not supported.");
        }
        if (this.state == state) return;
        this.state = state;
        onPlayerStateChange();
    }

    /** 
     * Reacts to change event from the {@link ImViewer}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        if (model.getState() == ImViewer.DISCARDED)
            setPlayerState(STOP);
    }
    
}
