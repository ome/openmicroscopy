/*
 * org.openmicroscopy.shoola.agents.viewer.Viewer
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

package org.openmicroscopy.shoola.agents.viewer;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.events.DisplayRendering;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.events.ImageLoaded;
import org.openmicroscopy.shoola.env.rnd.events.ImageRendered;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class Viewer
	implements Agent, AgentEventListener
{
		
	/** Background color. */
	public static final Color		BACKGROUND_COLOR = new Color(204, 204, 255);
	
	public static final Color		STEELBLUE = new Color(0x4682B4);
	
	public static final Dimension	TOOLBAR_DIMENSION = new Dimension(20, 300);
	
		/** Dimension of the separator between the toolBars. */
	public static final Dimension	SEPARATOR_END = new Dimension(100, 0);
	public static final Dimension	SEPARATOR = new Dimension(15, 0);
	
	/** Reference to the {@link Registry}. */
	private Registry				registry;
	
	private ViewerUIF				presentation;
	private ViewerCtrl				control;
	private TopFrame				topFrame;
	private RenderingControl		renderingControl;
	
	private int						curImageID, curPixelsID;
	private BufferedImage			curImage;
	
	private JCheckBoxMenuItem		viewItem;
	private JButton 				viewButton;
	
	/** Implemented as specified by {@link Agent}. */
	public void activate() {}
	
	/** Implemented as specified by {@link Agent}. */
	public void terminate() {}

	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx) 
	{
		registry = ctx;
		EventBus bus = registry.getEventBus();
		bus.register(this, ImageLoaded.class);
		bus.register(this, ImageRendered.class);
		topFrame = registry.getTopFrame();
		IconManager im = IconManager.getInstance(registry);
		Icon icon = im.getIcon(IconManager.VIEWER);
		viewItem = getViewMenuItem(icon);
		viewButton = getViewButton(icon);
		topFrame.addToMenu(TopFrame.VIEW, viewItem);
		topFrame.addToToolBar(TopFrame.VIEW_TB, viewButton);
	}

	/** Implemented as specified by {@link Agent}. */
	public boolean canTerminate() { return true; }

	ViewerUIF getPresentation() { return presentation; }
	
	Registry getRegistry() { return registry; }
	
	int getModel() { return renderingControl.getModel(); }
	
	void setModel(int model) { renderingControl.setModel(model); }
	
	PixelsDimensions getPixelsDims()
	{ 
		return renderingControl.getPixelsDims();
	}
	
	int getCurPixelsID() { return curPixelsID; } 
	
	/** Default timepoint. */
	int getDefaultT() { return renderingControl.getDefaultT(); }
	
	/** Default z-section in the stack. */
	int getDefaultZ() { return renderingControl.getDefaultZ(); }
	
	/** Return the current buffered image. */
	BufferedImage getCurImage() { return curImage; }
	
	/** 2D-plane selected. */
	void onPlaneSelected(int z, int t)
	{
		PlaneDef def = new PlaneDef(PlaneDef.XY, t);
		def.setZ(z);
		registry.getEventBus().post(new RenderImage(curPixelsID, def));	
	}
	
	/** Post an event to bring up the rendering agt. */
	void showRendering()
	{
		registry.getEventBus().post(new DisplayRendering());
	}
	
	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e) 
	{
		if (e instanceof ImageLoaded)
			handleImageLoaded((ImageLoaded) e);
		else if (e instanceof ImageRendered)
			handleImageRendered((ImageRendered) e);
	}
	
	/** Handle event @see ImageLoaded. */
	private void handleImageLoaded(ImageLoaded response)
	{
		LoadImage request = (LoadImage) response.getACT();
		renderingControl = response.getProxy();
		PixelsDimensions pxsDims = renderingControl.getPixelsDims();
		if (curImageID != request.getImageID()) {
			if (presentation == null) buildPresentation(pxsDims);
			initPresentation(request.getImageName(), pxsDims, false);
			curImageID = request.getImageID();
			curPixelsID = request.getPixelsID();
			registry.getEventBus().post(new RenderImage(curPixelsID));
		} else
			initPresentation(request.getImageName(), pxsDims, true);
	}
	
	/** Set the default. */
	private void initPresentation(String imageName, PixelsDimensions pxsDims, 
									boolean active)
	{
		presentation.setDefaultZT(getDefaultT(), getDefaultZ(), 
									pxsDims.sizeT, pxsDims.sizeZ);
		presentation.setImageName(imageName);
		presentation.setActive(active);
		setPresentation();
	}
	
	/** Handle event @see ImageRendered. */
	private void handleImageRendered(ImageRendered response)
	{
		curImage = null;
		curImage = response.getRenderedImage();
		presentation.setImage(curImage);
		setMenuSelection(true);
	}
	
	/** Select the menuItem. */
	void setMenuSelection(boolean b) { viewItem.setSelected(b); }
	
	/** Bring up or not the window. */
	void setPresentation()
	{
		if (presentation != null) {
			if (presentation.isClosed()) showPresentation();  
			if (presentation.isIcon()) deiconifyPresentation();
			setMenuSelection(true);
			//Activate the Frame.
			try {
				presentation.setSelected(true);
			} catch (Exception e) {}	
		}			
	}
	
	/** Display the presentation. */
	void showPresentation()
	{
		topFrame.removeFromDesktop(presentation);
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		try {
			presentation.setClosed(false);
		} catch (Exception e) {}
		presentation.setVisible(true);	
	}

	/** Pop up the presentation. */
	void deiconifyPresentation()
	{
		topFrame.deiconifyFrame(presentation);
		try {
			presentation.setIcon(false);
		} catch (Exception e) {}
	}
	
	/** Build the GUI. */
	private void buildPresentation(PixelsDimensions pxsDims)
	{
		control = new ViewerCtrl(this);
		presentation = new ViewerUIF(control, registry, pxsDims, getDefaultT(), 
									getDefaultZ());
		control.setPresentation(presentation);
		control.attachListener();
		control.attachItemListener(viewItem, ViewerCtrl.V_VISIBLE);
		control.attachItemListener(viewButton, ViewerCtrl.V_VISIBLE);
		viewItem.setEnabled(true);
		viewButton.setEnabled(true);
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		presentation.setVisible(true);	
	}
	
	/** 
	 * Menu item to add to the 
	 * {@link org.openmicroscopy.shoola.env.ui.TopFrame} menu bar.
	 */
	private JCheckBoxMenuItem getViewMenuItem(Icon icon)
	{
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Viewer", icon);
		menuItem.setEnabled(false);
		return menuItem;
	}
	
	private JButton getViewButton(Icon icon)
	{
		JButton b = new JButton(icon);
		b.setEnabled(false);
		b.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the viewer."));
		return b;
	}
	
}
