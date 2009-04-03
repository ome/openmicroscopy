/*
 * org.openmicroscopy.vis.chains.ome.GraphLayoutNode
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.chainbuilder.data.layout;

//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
 
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleView;
/** 
 * Behavior for nodes that can be involved in chainlayout. Generally will be 
 * two types of classes that implement this: graph nodes and "dummy nodes" that
 * are used to make the layout proper (ie., layered nodes with no edges spanning
 * more than one layer.
 * 
  @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public interface GraphLayoutNode {
	
	public String getName();
	
	public Iterator	succLinkIterator();
	
	public Iterator predLinkIterator();
	
	public void setSuccLinks(Set newLinks);
	
	public void addSuccLink(LayoutLink link);
	
	public void addPredLink(LayoutLink link);
	
	public void removePredLink(LayoutLink link); 
	
	public Collection getPredecessors();
	
	public Collection getSuccessors();
	
	public double getPosInLayer();
	
	public void setPosInLayer(double pos);
	
	
	public int getLayer();
	
	public void setLayer(int layer);
	
	public boolean hasLayer();
	
	public void setModuleView(ModuleView mod);
	
	public ModuleView getModuleView();
}