/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.ChainNodeExecutionData
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
package org.openmicroscopy.shoola.agents.chainbuilder.data;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.NexView;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;


public class ChainNodeExecutionData extends NodeExecutionData {

	/* widget used to view this nex */
	private NexView nexView;
	
	public ChainNodeExecutionData() {}
	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new ChainNodeExecutionData(); }

	public void setNexView(NexView nexView) {
		this.nexView = nexView;
	}
	
	public NexView getNexView() {
		return nexView;
	}
}
