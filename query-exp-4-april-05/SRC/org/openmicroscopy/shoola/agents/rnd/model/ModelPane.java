/*
 * org.openmicroscopy.shoola.agents.rnd.model.ModelPane
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

package org.openmicroscopy.shoola.agents.rnd.model;


//Java imports
import java.awt.Dimension;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;

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
public abstract class ModelPane
	extends JPanel
{
	
	/** Height of a cell in the table. */
	protected static final int			ROW_HEIGHT = 25;
	
	/** Default width of a cell. */
	protected static final int			DEFAULT_WIDTH = 30;
	
	/** Width of the label cell. */
	protected static final int			WIDTH_LABEL = 100;
	
	/** Default Height of the JButton. */
	protected static final int			BUTTON_HEIGHT = 15;
	
	/** Default width of the JButton. */
	protected static final int			BUTTON_WIDTH = 15;
	
	protected static final Dimension	DIM_BUTTON = 
											new Dimension(BUTTON_WIDTH, 
														BUTTON_HEIGHT);
		
	protected RenderingAgtCtrl 	eventManager;
		
	public void setEventManager(RenderingAgtCtrl eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public abstract void buildComponent();
	
}
