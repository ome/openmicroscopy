/*
 * org.openmicroscopy.shoola.env.rnd.RenderingEngine
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

package org.openmicroscopy.shoola.env.rnd;

//Java imports
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.events.ImageLoaded;
import org.openmicroscopy.shoola.env.rnd.events.ImageRendered;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage;
import org.openmicroscopy.shoola.env.rnd.events.RenderingPropChange;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSource;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSourceException;

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
public class RenderingEngine
	implements AgentEventListener
{

	private static RenderingEngine		singleton;
	
	//NB: this can't be called outside of container b/c agents have no refs
	//to the singleton container. So we can be sure this method is going to
	//create services just once.
	public static RenderingEngine getInstance(Container c)
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)	singleton = new RenderingEngine(c);
		return singleton;
	}
	
	
	
	private Registry		registry;
	private Map				renderers;
	
	
	private RenderingEngine(Container c)
	{
		registry = c.getRegistry();
		renderers = new HashMap();
	}
	
	private void hanldeException(String message, Exception cause)
	{
		throw new RuntimeException(message, cause);
		//TODO: post an event on Swing thread that launches UserNotifier.
	}
	
	private void handleLoadImage(LoadImage request)
	{
		Renderer rnd = new Renderer(request.getImageID(), request.getPixelsID(),
																		this);
		try {
			rnd.initialize();
			renderers.put(new Integer(request.getPixelsID()), rnd);
			//TODO: how do we figure when to remove? 
			RenderingDef original = rnd.getRenderingDef(), copy;
			copy = original.copy();
			EventBus eventBus = registry.getEventBus();
			RenderingControlImpl facade = new RenderingControlImpl(rnd);
			RenderingControlProxy proxy =
							new RenderingControlProxy(facade, copy, eventBus);
			ImageLoaded response = new ImageLoaded(request, proxy);
			eventBus.post(response);  //TODO: this has to be run w/in Swing thread.
		} catch (MetadataSourceException mse) {
			hanldeException("Can't load image metadata. Image id: "+
													request.getImageID(), mse);
		}																
	}
	
	private void handleRenderImage(RenderImage request)
	{
		Renderer rnd = (Renderer) renderers.get(
											new Integer(request.getPixelsID()));
		//TODO: if null, log?
		if (rnd != null) {
			try {
				PlaneDef pd = request.getPlaneDef();
				BufferedImage img;
				if (pd == null)	img = rnd.render();
				else	img = rnd.render(pd);
				ImageRendered response = new ImageRendered(request, img);
				EventBus eventBus = registry.getEventBus();
				eventBus.post(response);  //TODO: this has to be run w/in Swing thread.
			} catch (Exception dse) {  //TODO: DataSourceException, omeis.
				hanldeException("Can't load pixels data. Pixels id: "+
													request.getPixelsID(), dse);
			}
		}
	}
	
	private void handleRenderingPropChange(RenderingPropChange event)
	{
		event.doUpdate();
	}
	
	MetadataSource getMetadataSource(int imageID, int pixelsID)
	{
		return new MetadataSource(imageID, pixelsID, registry);
	}
	
	DataSink getDataSink(int imageID, int pixelsID)
	{
		//TODO: implement!
		return null;
	}
	
	public void activate()
	{
		EventBus eventBus = registry.getEventBus();
		eventBus.register(this, LoadImage.class);
		eventBus.register(this, RenderImage.class);
		eventBus.register(this, RenderingPropChange.class);
		//TODO: start event loop in its own thread.
	}
	
	public void terminate()
	{
		//TODO: implement.
	}

	public void eventFired(AgentEvent e) 
	{
		//TODO: put event on the queue and remove the following.
		if (e instanceof LoadImage)	handleLoadImage((LoadImage) e);
		else if	(e instanceof RenderImage)	handleRenderImage((RenderImage) e);
		else if (e instanceof RenderingPropChange)
			handleRenderingPropChange((RenderingPropChange) e);
	}

}
