/*
 * org.openmicroscopy.shoola.agents.rnd.pane.QuantumPaneManager
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

package org.openmicroscopy.shoola.agents.rnd.pane;


//Java imports
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;
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
class QuantumPaneManager
{
	/** minimum value (real value) of the input window. */
	private int 				minimum;
	
	/** maximum value (real value) of the input window. */
	private int 				maximum;
	
	/** The current window input start value. */
	private int					curStart;
	
	/** The current window input end value. */
	private int					curEnd;
	
	/** The current window output start value. */
	private int					curOutputStart;
	
	/** The current window output end value. */
	private int					curOutputEnd;
	
	/** Reference to the {@link QuantumPane view}. */
	private QuantumPane			view;
	
	/** Reference to the {@link RenderingAgtCtrl eventManager}. */
	private RenderingAgtCtrl	eventManager;
	
	QuantumPaneManager(RenderingAgtCtrl eventManager, QuantumPane view)
	{
		this.eventManager = eventManager;
		this.view = view;
	}
	
	void setCurStart(int v)
	{
		curStart = v;
	}
	
	void setCurEnd(int v)
	{
		curEnd = v;
	}
	
	int getCurStart()
	{
		return curStart;
	}
	
	int getCurEnd()
	{
		return curEnd;
	}
	
	void setMinimum(int minimum)
	{
		this.minimum = minimum;
	}
	
	void setMaximum(int maximum)
	{
		this.maximum = maximum;
	}

	int getMaximum()
	{
		return maximum;
	}

	int getMinimum()
	{
		return minimum;
	}
	
	int getCurOutputEnd()
	{
		return curOutputEnd;
	}

	int getCurOutputStart()
	{
		return curOutputStart;
	}
	
	void setCurOutputEnd(int i)
	{
		curOutputEnd = i;
	}

	void setCurOutputStart(int i)
	{
		curOutputStart = i;
	}

	void setStrategy()
	{
	}

	/**
	 * 
	 * @param index		index of the wavelength.
	 */
	void setWavelength(int index)
	{
	}
	
	/** Forward event to the {@link GraphicsRepresentation}. */
	void updateGraphic(int coefficient, int family)
	{
		if (family == QuantumFactory.POLYNOMIAL) 
			view.getGRepresentation().setControlLocation(coefficient);
		else if (family == QuantumFactory.EXPONENTIAL) 
			view.getGRepresentation().setControlAndEndLocation(coefficient);
	}
	
	/** Forward event to the {@link GraphicsRepresentation}. */
	void updateGraphic(int family) 
	{
		view.getGRepresentation().setControlsPoints(family);
		if (family == QuantumFactory.LOGARITHMIC)
			view.getGRepresentation().setControlLocation(
										GraphicsRepresentation.MIN);
   		else if (family == QuantumFactory.LINEAR || 
				family == QuantumFactory.POLYNOMIAL)
			view.getGRepresentation().setControlLocation(
										GraphicsRepresentation.INIT);
		else if (family == QuantumFactory.EXPONENTIAL)
			view.getGRepresentation().setControlAndEndLocation(
										GraphicsRepresentation.INIT);
	}
	
	/** Forward event to the {@link GraphicsRepresentation}. */
	void updateGraphic(boolean b)
	{
		view.getGRepresentation().reverse(b);
	}
	
	/** 
	 * Resize the input window and forward event to the different views.
	 *
	 * @param value	real input value.
	 */
	void setInputWindowStart(int value)
	{
		//TODO: update window
		curStart = value;
		DomainPaneManager dpManager = view.getDomainPane().getManager();
		GraphicsRepresentationManager 
			grManager = view.getGRepresentation().getManager();
		dpManager.setInputWindowStart(value);
		grManager.setInputWindowStart(value);			
	}
	
	/** 
	 * Set the window input and synchronize the different view.
	 *
	 * @param value	real input value.
	 */
	void setInputWindowEnd(int value)
	{
		curEnd = value;
		//TODO: update window
		DomainPaneManager dpManager = view.getDomainPane().getManager();
		GraphicsRepresentationManager 
			grManager = view.getGRepresentation().getManager();
		dpManager.setInputWindowEnd(value);
		grManager.setInputWindowEnd(value);	
	}
	
	/** Forward event to the {@link RenderingAgtCtrl eventManager}. */
	void showDialog(JDialog dialog)
	{
		eventManager.showDialog(dialog);
	}
	
	/** Retrieve the main Frame. */
	JFrame getReferenceFrame()
	{
		return (JFrame) eventManager.getRegistry().getTopFrame().getFrame();
	}

	RenderingAgtCtrl getEventManager()
	{
		return eventManager;
	}

}
