/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.LinkLayer;
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
package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;

//Java Imports
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.openmicroscopy.shoola.util.ui.piccolo.Link;
import org.openmicroscopy.shoola.util.ui.piccolo.ModuleView;

//Third-party libraries
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies

/** 
 * A {@link PLayer} specifically designed to hold PLink objects - both ParamLinks and 
 * PModuleLinks. This layer also handles the transition between showing only
 * PModuleLinks and showing the underlying ParamLinks
 * 
  * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
*/ 


public class LinkLayer extends PLayer {
	
	/**
	 * A node to hold all ParamLinks.
	 */ 
	private PNode params;
	
	/**
	 * A node to hold all modules;
	 */ 
	private PNode modules;
	
	public LinkLayer() {
		super();
		params = new PNode();
		addChild(params);
		//modules = new PNode();
		//addChild(modules);
	}
	
	
	/**
	 * When a {@link ParamLink} is added to this layer, give the 
	 * link a pointer back to this layer.
	 * 
	 * @param link
	 */
	// addChild() is called when the link starts.
	public void addChild(ParamLink link) {
		params.addChild(link);
		link.setLinkLayer(this);
	}
	
	/**
	 * 
	 * @return An iterator for the {@link ParamLink} objects held in this 
	 * layer.
	 * 
	 */
	public Iterator linkIterator() {
		return params.getChildrenIterator();
	}
	
	public Collection links() {
		return params.getChildrenReference();
	}
	
	protected Collection parameters() {
		return new Vector(links());
	}
	
	protected Collection  modules() {
		if (modules == null)
			return null;
		return new Vector(modules.getChildrenReference());
	}
	/** 
	 * Move all of the links from the source to be added to this.
	 * @param source
	 */
	public void reparentLinks(LinkLayer source) {
		Link node;
		Iterator iter;
		// first do param links of original
		Collection sourceParams = source.parameters();
		if (sourceParams != null) {
			// params is always added, so we don't need to check to see if it is 
			//null
			iter = sourceParams.iterator();
			while (iter.hasNext()) {
				node = (Link) iter.next();
				node.reparent(params);
				node.setLinkLayer(this);
			}
		}
		
		// then modules
		Collection sourceMods = source.modules();
		if (sourceMods != null) {
			if (modules == null) {
				modules = new PNode();
				addChild(modules);
			}
			iter = sourceMods.iterator();
			while (iter.hasNext()) {
				node = (Link) iter.next();
				node.reparent(modules);
				node.setLinkLayer(this);
			}
			
		}
	}
	
	/**
	 * When a link between two parameters is completed, we need to make sure 
	 * that thare is also a direct link between the two modules involved. 
	 * If there is no such link, create a new {@link ModuleLink} for the 
	 * two modules and add it to the modules layer.
	 * 
	 * @param link a newly-completed {@link ParamLink}
	 * @return The link between the two modules involved in link
	 */
	public ModuleLink completeLink(ParamLink link) {
		FormalOutput output = link.getOutput();
		FormalInput input = link.getInput();
		ModuleView start = output.getModuleView();
		ModuleView end = input.getModuleView();
	
		// only add a link if we don't have one already
	
		ModuleLink lnk = findModuleLink(start,end);
		if (lnk == null) {// if there is no link
			lnk = new ModuleLink(this,link,start,end);
			addModuleLink(lnk);
		}
		return lnk;
	}
	
	public void addModuleLink(ModuleLink lnk) {
		if (modules == null) {
			modules = new PNode();
			addChild(modules);
		}
		modules.addChild(lnk);
		
	}
	
	
	public void showModuleLinks() {
		if (params != null) {
			params.setVisible(false);
		}
		if (modules != null) {
			modules.setVisible(true);
			modules.setPickable(true);
		}
	}

	public void showParamLinks() {
		if (modules != null) {
			modules.setVisible(false);
		}
		if (params != null) {
			params.setVisible(true);
			params.setPickable(true);
		}
		
	}
	
	
	/**
	 * When a link between parameters is removed, we might need to
	 * remove the link between the associated modules. However, we do this
	 * only if we don't already have another existing link between the two
	 * modules - in that case, we need to keep the link between the modules.
	 * @param link
	 */
	public void removeModuleLinks(ParamLink link) {
		if (link.getOutput() == null || link.getInput() == null)
			return;
		ModuleView start = link.getOutput().getModuleView();
		ModuleView end = link.getInput().getModuleView();
		
		ParamLink lnk;
		ModuleView s;
		ModuleView e;
		
		Iterator iter = params.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ParamLink) {
				lnk = (ParamLink) obj;
				if (lnk.getOutput() != null)
					s = lnk.getOutput().getModuleView();
				else
					s = null;
				if (lnk.getInput() != null)
					e = lnk.getInput().getModuleView();
				else 
					e=null;
				// if same thing, we're done. don't clobber.
				if (s == start && e == end)
					return;
			}
			else
				System.err.println("*** removeModuleLinks(). Shouldn't get here");
		}
		// nothing equal, we need to remove it.
		removeModuleLink(start,end);
	}
	
	/**
	 * Do the job of removing the link between modules.
	 * @param start
	 * @param end
	 */
	private void removeModuleLink(ModuleView start,ModuleView end) {
		
		ModuleLink lnk = findModuleLink(start,end);
		removeModuleLink(lnk);
		
	}
	
	public void removeModuleLink(ModuleLink lnk) {
		if (lnk != null && modules != null)
			modules.removeChild(lnk);
	}
	
	/**
	 * 
	 * @param start The module at the start of a link
	 * @param end   The module at the end of a link
	 * @return the {@link ModuleLink linking thhe two modules
	 */
	public ModuleLink findModuleLink(ModuleView start,ModuleView end) {
		ModuleLink lnk = null;
		
		if (modules == null)
			return null;
		
		Iterator iter = modules.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ModuleLink) {
				lnk = (ModuleLink) obj;
				ModuleView s = lnk.getStart();
				ModuleView e = lnk.getEnd();
				if (start == s && end == e || start == e && end == s ) 
					return lnk;
			}
		}
		return null;
	}
	
	/**
	 * When a direct link between modules is removed, remove 
	 * all {@link ParamLink} instances between those two modules.
	 * @param link
	 */
	public void removeParamLinks(ModuleLink link) {
		ModuleView start = link.getStart();
		ModuleView end= link.getEnd();
		
		Vector toRemove = new Vector();
		Iterator iter=params.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ParamLink) {
				ParamLink lnk = (ParamLink) obj;
				ModuleView s = lnk.getOutput().getModuleView();
				ModuleView e = lnk.getInput().getModuleView();
				// if it's a link betweenn our two places, kill it.
				// but we don't call lnk.remove(), as this would do bad 
				// recursive things, as it would try to remove the
				// associated modules, which would call this proocedure.
				// It's not clear whether or not these calls are sufficiently
				// re-entrant to handle the recursion, so this approac
				// is a bit safer.
				if (s == start && e == end) {
					toRemove.add(lnk);
					lnk.clearLinks();
				}
			}
		}
		params.removeChildren(toRemove);
	}
}