/*
 * org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;

/** 
 * Utility class used to refresh the tree.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class RefreshExperimenterDef
{

	/** The node hosting the experimenter the data are for. */
    private TreeImageSet		expNode;
    
    /** 
     * Collection of expanded nodes. Only the nodes containing images
     * are in this list. 
     */
    private List        		expandedNodes;
    
    /** Contains the expanded top container nodes ID. */
    private Map         		expandedTopNodes;
    
    /** The results of the call. */
    private Object				results;
    
    /**
     * Creates a new instance.
     * 
     * @param expNode			The experimenter node. 
     * 							Mustn't be <code>null</code>.
     * @param expandedNodes		The List of expanded nodes.
     * @param expandedTopNodes	Contains the expanded nodes with images.
     */
    public RefreshExperimenterDef(TreeImageSet expNode, List expandedNodes, 
    							Map expandedTopNodes)
    {
    	if (expNode == null)
        	throw new IllegalArgumentException("Node not valid.");
    	this.expandedNodes = expandedNodes;
        this.expandedTopNodes = expandedTopNodes;
        this.expNode = expNode;
    }
    
    /**
     * Returns the list of expanded nodes.
     * 
     * @return See above.
     */
    public List getExpandedNodes() { return expandedNodes; }
    
    /**
     * Returns the map containing the expanded nodes with images.
     * 
     * @return See above.
     */
    public Map getExpandedTopNodes() { return expandedTopNodes; }
    
    /**
     * Returns the node hosting the experimenter.
     * 
     * @return See above.
     */
    public TreeImageSet	getExperimenterNode() { return expNode; }
    
    /**
     * Returns the results of the call.
     * 
     * @return See above.
     */
    public Object getResults() { return results; }
    
    /**
     * Sets the results of the call.
     * 
     * @param results The value to set.
     */
    public void setResults(Object results) { this.results = results; }
    
}
