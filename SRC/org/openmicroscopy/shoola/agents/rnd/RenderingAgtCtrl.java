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
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
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
	static final int		R_VISIBLE = 0;
	
	/** Action command ID to display the {@link GreyScalePane}. */
	static final int		GREY = 1;
	
	/** Action command ID to display the {@link RGBPane}. */
	static final int		RGB = 2;
	
	/** Action command ID to display the {@link HSBPane}. */
	static final int		HSB = 3;
	
	/** Action command ID. */
	static final int		SAVE = 4;
	
	private boolean 		active;
	private HashMap 		renderersPool;
	
	private ModelPane		curRenderer;
	
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
	public Registry getRegistry()
	{
		return abstraction.getRegistry();
	}
	
	/** Forward event to {@link RenderingAgt}. */
	public ChannelData[] getChannelData()
	{
		return abstraction.getChannelData();
	}
	
	/** Foward event to {@link RenderingAgt}. */
	public RenderingDef getRenderingDef()
	{ 
		return null;
	}
	
	/** Attach listener to a menu Item. */
	void setMenuItemListener(JMenuItem item, int id)
	{
		item.setActionCommand(""+id);
		item.addActionListener(this);
	}

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
		//impossible if IDs are set correctly
		} catch(NumberFormatException nfe) {   
			   throw nfe;  //just to be on the safe side...
		} 
	}
	
	private void saveDisplayOptions()
	{
		
	}
	
	private void activateRenderingModel(int i)
	{
		Class c = getRendererClass(i);
		activate(c);
	}
	
	private void activate(Class c)
	{
	   ModelPane rnd = (ModelPane) renderersPool.get(c);
	   if (rnd == null) {
		   rnd = createRenderer(c);
		   //rnd.setEventManager(this);
		   renderersPool.put(c, rnd);
	   }
	   rnd.setVisible(true);
	}
	
	private ModelPane createRenderer(Class c)
	{ 
		ModelPane model = null;
		try {
			model = (ModelPane) c.newInstance();
		} catch(Exception e) { throw new RuntimeException(e); }
		return model;
	}
	
	/**
	 * Return class associated to the constant.
	 */
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
	
}
