/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.NexView;
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;

//Java imports

//Third-party libraries
import edu.umd.cs.piccolo.nodes.PPath;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainNodeExecutionData;
import org.openmicroscopy.shoola.util.ui.Constants;

/** 
 * A Piccolo widget for Node Executions
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class NexView extends PPath {
	
	private ChainNodeExecutionData nex;

	public NexView(ChainNodeExecutionData nex) {
		super();
		this.nex =nex;
		nex.setNexView(this);
		// set path
		setPathToRectangle(0,0,Constants.NEX_SIDE,Constants.NEX_SIDE);
		setPaint(Constants.NEX_COLOR);
		setStroke(null);
	}
}
