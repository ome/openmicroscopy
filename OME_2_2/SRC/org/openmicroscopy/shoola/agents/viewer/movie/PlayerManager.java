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
import java.awt.image.BufferedImage;
import javax.swing.AbstractButton;
import javax.swing.Timer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.events.ImageRendered;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
	implements ActionListener, AgentEventListener
{

	static final int			FPS_INIT = 12, FPS_MIN = 1;
			
	private Player				view;
    
	private ViewerCtrl			control;
	
	private Registry			registry;
				
	private BufferedImage[]		frames;		
	
	private int					delay;
	private boolean				frozen, rewind;
	private Timer 				timer;
	private int 				frameNumber;

	private int					sizeValue;
    
	private ToolBarManager		tbm;
	
	private int					curValue;
	
	private int					startMovie, endMovie;
	
    private int                 index;
    
	/**
	 * 
	 * @param view		reference to the view.
	 * @param control	reference to the {@link ViewerCtrl control}.
	 * @param maxValue	timepoint-1 b/c OME values start at 0 or z-1.
     * @param index     one of the movie defined in {@link ViewerCtrl control}.
	 */
	public PlayerManager(Player view, ViewerCtrl control, int maxValue, 
                        int index)
	{
		this.view = view;
        this.index = index;
		this.control = control;
		registry = control.getRegistry();
		sizeValue = maxValue;
		if (index == ViewerCtrl.MOVIE_T)
            curValue = control.getDefaultZ();
        else if (index == ViewerCtrl.MOVIE_Z)
            curValue = control.getDefaultT();
		frames = new BufferedImage[maxValue+1];
		frozen = true;
		rewind = false;
		delay = 1000/FPS_INIT;
		//Set up a timer that calls this object's action handler.
		timer = new Timer(delay, this);
		timer.setInitialDelay(delay*10);
		timer.setCoalesce(true);
        attachListener();
		EventBus bus = registry.getEventBus();
		bus.register(this, ImageRendered.class);
	}
	
    /** Attach a window listener. */
    private void attachListener()
    {
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { timer.stop(); }
        });
    }
    
	/** Attach listener to a menu Item. */
	void attachItemListener(AbstractButton item, int id)
	{
		item.setActionCommand(""+id);
		item.addActionListener(this);
	}
	
	boolean isFrozen() { return frozen; }
	
	void setToolBarManager(ToolBarManager tbm)
	{
		this.tbm = tbm;
		startMovie = tbm.getCurMovieStart();
		endMovie = tbm.getCurMovieEnd();
		frameNumber = startMovie;
	}
	
	void setStartMovie(int startMovie)
    { 
        this.startMovie = startMovie;
        setFrameNumber(startMovie);
    }
	
	void setEndMovie(int endMovie)
    { 
        this.endMovie = endMovie;
        setFrameNumber(endMovie);
    }
	
	/** Reset the timer delay. */
	void setTimerDelay(int v)
	{
		delay = 1000/v;
		timer.setDelay(delay);
		timer.setInitialDelay(delay*10);
	}
	
	/** Play the movie. */
	void play() { play(false); }
	
	/** Pause. */
	void pause()
	{
		if (!frozen) timer.stop();
		frozen = true;
	}
	
	/** Stop the movie. */
	void stop()
	{
		stopTimer();
		frameNumber = startMovie;
		updateImage(frameNumber);
	}
	
	/** Go to the first selected timepoint, and start the movie from there. */
	void rewind()
	{
		//first stop the timer;
		if (!frozen) timer.stop();
        if (frameNumber != startMovie || frameNumber != endMovie) frameNumber--;
		play(true);
	}
	
    /** Go to the first time point. */
    void playStart()
    {
        setFrameNumber(startMovie);
    }
    
    /** Go to the last time point. */
    void playEnd()
    {
        setFrameNumber(endMovie);
    }
    
    private void setFrameNumber(int nb)
    {
        //first stop the timer;
        if (!frozen) timer.stop();
        frozen = true;
        frameNumber = nb;
        updateImage(frameNumber);    
    }
    
    private void play(boolean b) 
    {
        rewind = b;
        frozen = false;
        timer.start(); 
    }
    
	/** Save the movie. */
	void saveAs()
	{
		UserNotifier un = registry.getUserNotifier();
		un.notifyInfo("Save movie", "Sorry not yet implemented.");
	}
	
    /** Stop the animation. */
    void stopTimer()
    {
        timer.stop();       
        frozen = true;
        rewind = false;
    }
	
	/** Handle event fired by timer. Advance the animation frame. */
    /** For now, we play the movie from startMovie. */
	public void actionPerformed(ActionEvent e)
	{
		if (frameNumber <= sizeValue && !frozen &&
            frameNumber >= startMovie && frameNumber <= endMovie) {
            updateImage(frameNumber);
            if (rewind) {
                if (frameNumber == startMovie) stopTimer();
                else frameNumber--;
            } else {
                if (frameNumber == endMovie) stopTimer();
                else frameNumber++;  
            }
		} //else resetTimer(true);
	}

	/** Update the image, if not already created an event is posted. */
	private void updateImage(int v)
	{
		BufferedImage img = frames[v];
		if (img == null)	renderImage(v);
		else paintImage(img, v);
	}
	
	/** Post a renderImageEvent. */
	void renderImage(int v)
	{
		PlaneDef def = null;
        if (index == ViewerCtrl.MOVIE_T) {
            def = new PlaneDef(PlaneDef.XY, v);
            def.setZ(curValue); 
        } else {
            def = new PlaneDef(PlaneDef.XY, curValue);
            def.setZ(v);
        }
		RenderImage render = new RenderImage(control.getCurPixelsID(), def);
		render.setMovie(true);
		registry.getEventBus().post(render);	
	}

	/** Synchronize the different components. */
	private void paintImage(BufferedImage img, int v)
	{
		tbm.setLabel(v);
		view.getCanvas().paintImage(img);
	}
	
	/** Handle events fired. */
	private void handleImageRendered(ImageRendered e)
	{
		RenderImage request = (RenderImage) e.getACT();
		if (request.isMovie()) {
			BufferedImage img = e.getRenderedImage();
            int v = 0; 
            if (index == ViewerCtrl.MOVIE_T)
                v = request.getPlaneDef().getT();
            else if (index == ViewerCtrl.MOVIE_Z)
                v = request.getPlaneDef().getZ();
			frames[v] = img;
			paintImage(img, v);
		}
	}
	
	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e) 
	{
		if (e instanceof ImageRendered)
			handleImageRendered((ImageRendered) e);
	}
	
}
