/*
 * org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.model.GreyScalePane;
import org.openmicroscopy.shoola.agents.rnd.model.HSBPane;
import org.openmicroscopy.shoola.agents.rnd.model.ModelPane;
import org.openmicroscopy.shoola.agents.rnd.model.RGBPane;
import org.openmicroscopy.shoola.agents.rnd.pane.QuantumPane;
import org.openmicroscopy.shoola.env.InternalError;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapContext;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;

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
public class RenderingAgtCtrl
	implements ActionListener
{

	/** Action command ID to display the {@link GreyScalePane}. */
	static final int				GREY = RenderingDef.GS;
	
	/** Action command ID to display the {@link RGBPane}. */
	static final int				RGB = RenderingDef.RGB;
	
	/** Action command ID to display the {@link HSBPane}. */
	static final int				HSB = RenderingDef.HSB;

	/** Action command ID. */
	static final int				SAVE = 4;
	
	private boolean 				displayed;
	
	/** String corresponding to the specified model. */
	private String					modelType;
	
	private HashMap 				renderersPool;
	
	private RenderingAgt			abstraction;
	
	private RenderingAgtUIF			presentation;
	
	private Icon					modelIcon;
	
	private IconManager				im;
	
	RenderingAgtCtrl(RenderingAgt abstraction)
	{
		this.abstraction = abstraction;
		displayed = false;
		renderersPool = new HashMap();
		im = IconManager.getInstance(abstraction.getRegistry());
	}

	void setDisplayed(boolean b) { displayed = b; }
	
	void setPresentation(RenderingAgtUIF presentation)
	{
		this.presentation = presentation;
	}
	
	/** Returns the {@link RenderingAgt abstraction}. */
	RenderingAgt getAbstraction() { return abstraction; }
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public JFrame getReferenceFrame()
	{
		return abstraction.getRegistry().getTopFrame().getFrame();
	}
	
	/** Forward event to {@link RenderingAgtUIF presentation}. */
	public void setMappingPane()
	{
		presentation.setMappingPane();
	}
	
	/** Forward event to {@link RenderingAgt abstraction}.  */
	public void updateChannelData(ChannelData cd)
	{
		abstraction.updateChannelData(cd);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public int getCodomainStart() { return abstraction.getCodomainStart(); }
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public int getCodomainEnd() { return abstraction.getCodomainEnd(); }
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public PixelsStatsEntry[] getChannelStats(int w)
	{
		return abstraction.getChannelStats(w);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public double getGlobalChannelWindowStart(int w)
	{
		return abstraction.getGlobalChannelWindowStart(w);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public double getGlobalChannelWindowEnd(int w)
	{
		return abstraction.getGlobalChannelWindowEnd(w);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public Comparable getChannelWindowStart(int w)
	{
		return abstraction.getChannelWindowStart(w);
	}

	/** Forward event to {@link RenderingAgt abstraction}. */
	public Comparable getChannelWindowEnd(int w)
	{
		return abstraction.getChannelWindowEnd(w);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public QuantumDef getQuantumDef() { return abstraction.getQuantumDef(); }
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setActive(int w, boolean active)
	{
		if (active) {
			presentation.getQuantumPane().setSelectedWavelength(w);
			presentation.getTabs().setSelectedIndex(RenderingAgtUIF.POS_MODEL);
		}
		abstraction.setActive(w, active);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setActive(int w) { 
		presentation.getQuantumPane().setSelectedWavelength(w);
		presentation.getTabs().setSelectedIndex(RenderingAgtUIF.POS_MODEL);
		abstraction.setActive(w); 
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public boolean isActive(int w) { return abstraction.isActive(w); }
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public int[] getRGBA(int w) { return abstraction.getRGBA(w); }
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setRGBA(int w, int red, int green, int blue, int alpha)
	{
		abstraction.setRGBA(w, red, green, blue, alpha);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setChannelWindowStart(int w, int x)
	{
		//TODO: support other formats
		abstraction.setChannelWindowStart(w, new Integer(x));
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setChannelWindowEnd(int w, int x)
	{
		//TODO: support other formats
		abstraction.setChannelWindowEnd(w, new Integer(x));
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setCodomainLowerBound(int x)
	{
		abstraction.setCodomainLowerBound(x);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setCodomainUpperBound(int x)
	{
		abstraction.setCodomainUpperBound(x);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void setQuantumStrategy(double k, int family, int resolution)
	{
		abstraction.setQuantumStrategy(k, family, resolution);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void addCodomainMap(CodomainMapContext ctx)
	{
		abstraction.addCodomainMap(ctx);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void removeCodomainMap(CodomainMapContext ctx)
	{
		abstraction.removeCodomainMap(ctx);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public void updateCodomainMap(CodomainMapContext ctx)
	{
		abstraction.updateCodomainMap(ctx);
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public Registry getRegistry() { return abstraction.getRegistry(); }
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public ChannelData[] getChannelData()
	{ 
		return abstraction.getChannelData();
	}
	
	/** Forward event to {@link RenderingAgt abstraction}. */
	public ChannelData getChannelData(int w)
	{
		return abstraction.getChannelData(w);
	}
	
	/** Handle events. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
		   switch (index) { 
				case SAVE:
					saveDisplayOptions();
					break;
				case GREY:
				case RGB:
				case HSB:
					activateRenderingModel(index);
					break;	   	
		   }
		} catch(NumberFormatException nfe) {   
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** Save the image settings. */
	public void saveDisplayOptions()
	{
		//TODO: implement method.
	}
	
	/** Create the specified panel. */
	public void activateRenderingModel(int i)
	{
		Class c = getRendererClass(i);
		presentation.setModelPane(activate(c));
		QuantumPane qp = presentation.getQuantumPane();
		if (i == GREY) {
			qp.setSelectionWavelengthsEnable(false);
			ChannelData[] channelData = getChannelData();
			for (int j = 0; j < channelData.length; j++) {
				if (isActive(j)) {
					qp.setSelectedWavelength(j);
					presentation.getTabs().setSelectedIndex(
											RenderingAgtUIF.POS_MODEL);
					break;
				}
			}
		} else qp.setSelectionWavelengthsEnable(true);
		abstraction.setModel(i);
	}
	
	String getModelType() { return modelType; }
	
	Icon getModelIcon() { return modelIcon; }
	
	/** Attach listener to a menu Item. */
	void setMenuItemListener(JMenuItem item, int id)
	{
		item.setActionCommand(""+id);
		item.addActionListener(this);
	}

	/** Return the current RenderingModel. */
	ModelPane getModelPane()
	{
		return activate(getRendererClass(abstraction.getModel()));
	}
	
	/** Retrieve or instanciate the ModelPane. */ 
	private ModelPane activate(Class c)
	{
	   ModelPane rnd = (ModelPane) renderersPool.get(c);
	   if (rnd == null) {
		   rnd = createRenderer(c);
		   rnd.setEventManager(this);
		   renderersPool.put(c, rnd);
	   }
	   return rnd;
	}
	
	/** Create the model associated to the Class. */
	private ModelPane createRenderer(Class c)
	{ 
		ModelPane model = null;
		try {
			model = (ModelPane) c.newInstance();
		} catch(Exception e) { 
			String msg = "Can't create an instance of "+c.getName();
			throw new InternalError(msg, e);
		}
		return model;
	}
	
	/** Return class associated to the constant. */
	private Class getRendererClass(int index)
	{
		Class result = null;
		try {
			switch (index) {
				case GREY:
					result = GreyScalePane.class;
					modelType = "Grey";
					modelIcon = im.getIcon(IconManager.GREYSCALE);
					break;
				case HSB:
					result = HSBPane.class;
					modelType = "HSB";
					modelIcon = im.getIcon(IconManager.HSB);
					break;
				case RGB:
					result = RGBPane.class;
					modelType = "RGB";
					modelIcon = im.getIcon(IconManager.RGB);
			}
		}catch(NumberFormatException nfe) {   
			throw new Error("Invalid Action ID "+index, nfe);
		}
		
		return result;
	}
	
}
