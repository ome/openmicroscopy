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
import javax.swing.JDialog;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.metadata.ChannelData;
import org.openmicroscopy.shoola.agents.rnd.model.GreyScalePane;
import org.openmicroscopy.shoola.agents.rnd.model.HSBPane;
import org.openmicroscopy.shoola.agents.rnd.model.ModelPane;
import org.openmicroscopy.shoola.agents.rnd.model.RGBPane;
import org.openmicroscopy.shoola.env.config.Registry;
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
	/** Action command ID to display the Rendering internalFrame. */
	static final int		R_VISIBLE = 5;
	
	/** Action command ID to display the {@link GreyScalePane}. */
	static final int		GREY = RenderingDef.GS;
	
	/** Action command ID to display the {@link RGBPane}. */
	static final int		RGB = RenderingDef.RGB;
	
	/** Action command ID to display the {@link HSBPane}. */
	static final int		HSB = RenderingDef.HSB;
	
	/** Action command ID. */
	static final int		SAVE = 4;
	
	private boolean 		active;
	private HashMap 		renderersPool;
	
	private RenderingAgt	abstraction;
	
	RenderingAgtCtrl(RenderingAgt abstraction)
	{
		this.abstraction = abstraction;
		active = false;
		renderersPool = new HashMap();
	}
	
	/** 
	 * Returns the abstraction component of this agent.
	 *
	 * @return  See above.
	 */
	RenderingAgt getAbstraction()
	{
		return abstraction;
	}
	
	/** Forward event to {@link RenderingAgtUIF}. */
	public void showDialog(JDialog dialog)
	{
		abstraction.getPresentation().showDialog(dialog);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public int getCodomainStart()
	{
		return abstraction.getCodomainStart();
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public int getCodomainEnd()
	{
		return abstraction.getCodomainEnd();
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public PixelsStatsEntry[] getChannelStats(int w)
	{
		return abstraction.getChannelStats(w);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public double getGlobalChannelWindowStart(int w)
	{
		return abstraction.getGlobalChannelWindowStart(w);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public double getGlobalChannelWindowEnd(int w)
	{
		return abstraction.getGlobalChannelWindowEnd(w);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public Comparable getChannelWindowStart(int w)
	{
		return abstraction.getChannelWindowStart(w);
	}

	/** Forward event to {@link RenderingAgt}. */
	public Comparable getChannelWindowEnd(int w)
	{
		return abstraction.getChannelWindowEnd(w);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public QuantumDef getQuantumDef()
	{
		return abstraction.getQuantumDef();
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void setActive(int w, boolean active)
	{
		abstraction.setActive(w, active);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public boolean isActive(int w)
	{
		return abstraction.isActive(w);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public int[] getRGBA(int w)
	{
		return abstraction.getRGBA(w);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void setRGBA(int w, int red, int green, int blue, int alpha)
	{
		abstraction.setRGBA(w, red, green, blue, alpha);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void setChannelWindowStart(int w, int x)
	{
		//TODO: support others format
		abstraction.setChannelWindowStart(w, new Integer(x));
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void setChannelWindowEnd(int w, int x)
	{
		//TODO: support others format
		abstraction.setChannelWindowEnd(w, new Integer(x));
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void setCodomainLowerBound(int x)
	{
		abstraction.setCodomainLowerBound(x);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void setCodomainUpperBound(int x)
	{
		abstraction.setCodomainUpperBound(x);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void setQuantumStrategy(int k, int family, int resolution)
	{
		abstraction.setQuantumStrategy(k, family, resolution);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void addCodomainMap(CodomainMapContext ctx)
	{
		abstraction.addCodomainMap(ctx);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void removeCodomainMap(CodomainMapContext ctx)
	{
		abstraction.removeCodomainMap(ctx);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public void updateCodomainMap(CodomainMapContext ctx)
	{
		abstraction.updateCodomainMap(ctx);
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public Registry getRegistry()
	{
		return abstraction.getRegistry();
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public ChannelData[] getChannelData()
	{
		return abstraction.getChannelData();
	}
	
	/** Handle events. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
		   int index = Integer.parseInt(s);
		   switch (index) { 
				case R_VISIBLE:
					abstraction.activate();
					break;
				case SAVE:
					saveDisplayOptions();
				case GREY:
				case RGB:
				case HSB:
					activateRenderingModel(index);
					break;	   	
		   }
		} catch(NumberFormatException nfe) {   
			   throw nfe;  //just to be on the safe side...
		} 
	}
	
	/** Attach listener to a menu Item. */
	void setMenuItemListener(JMenuItem item, int id)
	{
		item.setActionCommand(""+id);
		item.addActionListener(this);
	}

	/** Return the current RenderingModel. */
	ModelPane getModelPane()
	{
		Class c = getRendererClass(abstraction.getModel());
		return activate(c);
	}

	private void activateRenderingModel(int i)
	{
		Class c = getRendererClass(i);
		abstraction.getPresentation().setModelPane(activate(c));
		abstraction.setModel(i);
	}
	
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
		} catch(Exception e) { throw new RuntimeException(e); }
		return model;
	}
	
	/** Return class associated to the constant. */
	private Class getRendererClass(int i)
	{
		Class result = null;
		switch(i) {
			case GREY:
				result = GreyScalePane.class;
				break;
			case HSB:
				result = HSBPane.class;
				break;
			case RGB:
				result = RGBPane.class;
		}
		
		return result;
	}
	
	private void saveDisplayOptions()
	{
	
	}
	
}
