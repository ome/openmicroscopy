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

	static final int			SAVE_AS = 0, MOVIE_PLAY = 1, MOVIE_STOP = 4,
								MOVIE_REWIND = 5, MOVIE_FORWARD = 6,
								MOVIE_PAUSE = 7;
	
	static final int			FPS_INIT = 12, FPS_MIN = 1;
			
	private Player				view;
	private ViewerCtrl			control;
	
	private Registry			registry;
				
	private BufferedImage[]		frames;		
	
	private int					delay;
	private boolean				frozen, rewind;
	private Timer 				timer;
	private int 				frameNumber;

	private int					sizeT;
	private ToolBarManager		tbm;
	
	private int					curZ;
	
	private int					startMovie, endMovie;
	
	/**
	 * 
	 * @param view		reference to the view.
	 * @param control	reference to the {@link ViewerCtrl control}.
	 * @param maxT		timepoint-1 b/c OME values start at 0.
	 */
	public PlayerManager(Player view, ViewerCtrl control, int maxT)
	{
		this.view = view;
		this.control = control;
		registry = control.getRegistry();
		sizeT = maxT;
		curZ = control.getDefaultZ();
		frames = new BufferedImage[maxT+1];
		frozen = false;
		rewind = false;
		delay = 1000/FPS_INIT;
		//Set up a timer that calls this object's action handler.
		timer = new Timer(delay, this);
		timer.setInitialDelay(delay*10);
		timer.setCoalesce(true);
		EventBus bus = registry.getEventBus();
		bus.register(this, ImageRendered.class);
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
	
	void setStartMovie(int startMovie) { this.startMovie = startMovie; }
	
	void setEndMovie(int endMovie) { this.endMovie = endMovie; }
	
	/** Reset the timer delay. */
	void setTimerDelay(int v)
	{
		delay = 1000/v;
		timer.setDelay(delay);
		timer.setInitialDelay(delay*10);
	}
	
	private void play(boolean b) 
	{
		timer.start();
		frozen = false;
		rewind = b;
	}
	
	/** Play the movie. */
	void play() { play(false); }
	
	/** Pause. */
	void pause()
	{
		timer.stop();
		frozen = true;
	}
	
	/** Stop the movie. */
	void stop()
	{
		resetTimer(true);
		updateImage(frameNumber);
	}
	
	/** Go to the first selected timepoint, and start the movie from there. */
	void rewind()
	{
		//first stop the timer;
		if (!frozen) timer.stop();
		frameNumber = startMovie;
		//restart the timer.
		play(false);
	}
	
	/** Go to the last selected timepoint, and start the movie from there. */
	void forward()
	{
		//first stop the timer;
		if (!frozen) timer.stop();
		frameNumber = endMovie;
		play(true);	
	}

	/** Save the movie. */
	void saveAs()
	{
		UserNotifier un = registry.getUserNotifier();
		un.notifyInfo("Save movie", "Sorry not yet implemented.");
	}
	
	/** Stop the animation and reset the starting timepoint if requested. */
	void resetTimer(boolean b)
	{
		timer.stop();
		if (b) frameNumber = startMovie;
		frozen = true;
		rewind = false;
	}
	
	/** Handle event fired by timer. Advance the animation frame. */
	public void actionPerformed(ActionEvent e)
	{
		if (frameNumber <= sizeT && frameNumber >= startMovie && 
			frameNumber <= endMovie && !frozen) {
			updateImage(frameNumber);	
			if (rewind)	frameNumber--;
			else  frameNumber++;
		} else resetTimer(true); 
	}

	/** Update the image, if not already created an event is posted. */
	private void updateImage(int t)
	{
		BufferedImage img = frames[t];
		if (img == null)	renderImage(t);
		else paintImage(img, t);
	}
	
	/** Post a renderImageEvent. */
	void renderImage(int t)
	{
		PlaneDef def = new PlaneDef(PlaneDef.XY, t);
		def.setZ(curZ); 
		RenderImage render = new RenderImage(control.getCurPixelsID(), def);
		render.setMovie(true);
		registry.getEventBus().post(render);	
	}

	/** Synchronize the different components. */
	private void paintImage(BufferedImage img, int t)
	{
		tbm.setTLabel(t);
		view.getCanvas().paintImage(img);
	}
	
	/** Handle events fired. */
	private void handleImageRendered(ImageRendered e)
	{
		RenderImage request = (RenderImage) e.getACT();
		if (request.isMovie()) {
			BufferedImage img = e.getRenderedImage();
			int t = request.getPlaneDef().getT();
			frames[t] = img;
			paintImage(img, t);
		}
	}
	
	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e) 
	{
		if (e instanceof ImageRendered)
			handleImageRendered((ImageRendered) e);
	}
	
}
