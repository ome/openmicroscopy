/*
 * org.openmicroscopy.shoola.agents.rnd.RenderingAgt
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

package org.openmicroscopy.shoola.agents.rnd;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.metadata.ChannelData;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopFrame;

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
public class RenderingAgt
	implements Agent
{
	/** Reference to the registry. */
	private Registry			registry;
	
	/** Reference to the GUI. */
	private RenderingAgtUIF		presentation;
	
	/** Reference to the control component. */
	private RenderingAgtCtrl	control;
	
	private ChannelData[]		channelData;
	
	//TEMPO
	/** Reference to the topFrame. */
	private TopFrame				topFrame;
	/** Creates a new instance. */
	public RenderingAgt() {}
	
	/** Implemented as specified by {@link Agent}. */
	public void activate()
	{
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		presentation.setVisible(true);
	}

	/** Implemented as specified by {@link Agent}. */
	public void terminate()
	{
	
	}

	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx)
	{
		registry = ctx;
		control  = new RenderingAgtCtrl(this);
		presentation = new RenderingAgtUIF(control, registry);
		//TODO: to be removed
		topFrame = registry.getTopFrame();
		topFrame.addToMenu(TopFrame.VIEW, presentation.getViewMenuItem());
	}

	/** Implemented as specified by {@link Agent}. */
	public boolean canTerminate()
	{
		return true;
	}

	public RenderingAgtUIF getPresentation()
	{
		return presentation;
	}

	Registry getRegistry()
	{
		return registry;
	}
	
	ChannelData[] getChannelData()
	{
		channelData = new ChannelData[2];
		ChannelData cd = new ChannelData(0, 200, "test"),
					cd1 = new ChannelData(1, 250, "test");
		channelData[0] = cd;
		channelData[1] = cd1;
		return channelData;
	}

}
