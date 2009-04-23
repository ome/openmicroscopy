/*
 * ome.ij.dm.browser.BrowserModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.dm.browser;


//Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


//Third-party libraries

//Application-internal dependencies

/** 
 * The Model component in the <code>Browser</code> MVC triad.
 * This class tracks the <code>Browser</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. However, this class doesn't know the actual hierarchy
 * the <code>Browser</code> is for.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class BrowserModel 
{

	/** The collection of selected nodes in the visualization tree. */
	private Set 		selectedNodes;

	/** Holds one of the state flags defined by {@link Browser}. */
	private int			state;
    
	/** The sorting index. One of the constants defined by {@link Browser}.*/
	private int			sortingIndex;
	
    /** Reference to the component that embeds this model. */
    protected Browser	component; 
    
    /** 
	 * Sorts the passed nodes by row.
	 * 
	 * @param nodes The nodes to sort.
	 * @return See above.
	 */
	private List sortByDate(Collection nodes)
	{
		List<Object> l = new ArrayList<Object>();
		if (nodes == null) return l;
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			l.add(i.next());
		}
		Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Timestamp t1 = ((TreeImageDisplay) o1).getTime();
                Timestamp t2 = ((TreeImageDisplay) o2).getTime();
                int v = 0;
                int r = t1.compareTo(t2);
                if (r < 0) v = -1;
                else if (r > 0) v = 1;
                return -v;
            }
        };
        Collections.sort(l, c);
		return l;
	}
	
	/** 
	 * Sorts the passed nodes by row.
	 * 
	 * @param nodes The nodes to sort.
	 * @return See above.
	 */
	private List sortByName(Collection nodes)
	{
		List<Object> l = new ArrayList<Object>();
		if (nodes == null) return l;
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			l.add(i.next());
		}
		Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
            	String s1 = ((TreeImageDisplay) o1).getNodeName();
            	String s2 = ((TreeImageDisplay) o2).getNodeName();
 
            	if (s1 == null && s2 == null) return 0; 
                else if (s1 == null) return -1; 
                else if (s2 == null) return 1; 
                int v = 0;
                int result = (s1.toLowerCase()).compareTo(s2.toLowerCase());
                if (result < 0) v = -1;
                else if (result > 0) v = 1;
                return v;
            }
        };
        Collections.sort(l, c);
		return l;
	}
	
    /**
     * Creates a new object and sets its state to {@link Browser#NEW}.       
     */
    protected BrowserModel()
    { 
        state = Browser.NEW;
        selectedNodes = new HashSet();
        sortingIndex = Browser.SORT_NODES_BY_NAME;
    }
    
    /**
     * Called by the <code>Browser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Browser component) { this.component = component; }
    
    /**
     * Returns the currently selected node.
     * 
     * @return See above.
     */
    TreeImageDisplay getLastSelectedDisplay()
    { 
        int n = selectedNodes.size();
        if (n == 0) return null;
        Iterator i = selectedNodes.iterator();
        int index = 0;
        while (i.hasNext()) {
            if (index == (n-1)) return (TreeImageDisplay) i.next();
            index++;
        }
        return null;
    }

    /**
     * Sets the selected node.
     * 
     * @param display The selected value.
	 * @param single  Pass <code>true</code> if the method is invoked for
	 *                single selection, <code>false</code> for multi-selection.
     */
    void setSelectedDisplay(TreeImageDisplay display, boolean single)
    {
    	if (single) {
    		selectedNodes.clear();
            if (display != null) selectedNodes.add(display);
    	} else {
    		if (!selectedNodes.contains(display) && display != null)
    			selectedNodes.add(display);
    	}
    }
    
	/**
	 * Returns the current state.
	 * 
	 * @return One of the state constants defined by the {@link Browser}.  
	 */
	int getState() { return state; }

	 /**
     * Starts the asynchronous retrieval of the leaves contained in the 
     * currently selected <code>TreeImageDisplay</code> objects needed
     * by this model and sets the state to {@link Browser#LOADING_LEAVES}.
     * 
	 * @param expNode 	The node hosting the experimenter.
	 * @param node		The parent of the data. Pass <code>null</code>
	 * 					to retrieve all data.	
     */
    void fireLeavesLoading(TreeImageDisplay expNode, TreeImageDisplay node)
    {
    	state = Browser.LOADING_LEAVES;
    	/*
    	Object ho = node.getUserObject();
    	if (ho instanceof DatasetData)  {
    		DatasetData d = (DatasetData) ho;
    		try {
    			long id = d.getId();
    			DataService ds = ServicesFactory.getInstance().getDataService();
    			Collection r = ds.loadImages(id);
    			Iterator i = r.iterator();
    			DataObject object;
    			Class klass = d.getClass();
    			while (i.hasNext()) {
        			object = (DataObject) i.next();
    				if (object.getClass().equals(klass)
    						&& object.getId() == id) {
    					if (object instanceof DatasetData) {
    						component.setLeaves(((DatasetData) object).getImages(), 
    								node, expNode);
    					} 
    				}
    			}
			} catch (Exception e) {
				// TODO: handle exception
			}
    		/*
    		currentLoader = new ExperimenterDataLoader(component, 
    				ExperimenterDataLoader.DATASET, 
    				(TreeImageSet) expNode, (TreeImageSet) node);
    		 currentLoader.load();
    		 */
    	//} 
    }

    /**
     * Starts the asynchronous retrieval of the number of items contained 
     * in the <code>TreeImageSet</code> containing images e.g. a 
     * <code>Dataset</code> and sets the state to 
     * {@link Browser#COUNTING_ITEMS}.
     * 
     * @param containers The collection of <code>DataObject</code>s.
     * @param nodes      The corresponding nodes.
     */
    void fireContainerCountLoading(Set containers, Set<TreeImageSet> nodes)
    {
        if (containers == null || containers.size() == 0) {
            state = Browser.READY;
            return;
        }
    }
	
    /**
     * Sets the object in the {@link Browser#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        cancel();
       
        state = Browser.DISCARDED;
    }
    
    /** 
     * Cancels any ongoing data loading and sets the state to 
     * {@link Browser#READY}.
     */
    void cancel()
    {
        state = Browser.READY;
    }

    /**
	 * Starts the asynchronous retrieval of the hierarchy objects needed
	 * by this model and sets the state to {@link Browser#LOADING_DATA}
	 * depending on the value of the {@link #filterType}. 
	 * 
	 * @param expNode 	The node hosting the experimenter.
	 */
	void fireExperimenterDataLoading(TreeImageSet expNode)
	{
		state = Browser.LOADING_DATA;
		/*
		DataService ds = ServicesFactory.getInstance().getDataService();
		try {
			component.setExperimenterData(expNode, ds.loadProjects());
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	/**
	 * Sets the sorting index.
	 * 
	 * @param index The value to set.
	 */
	void setSortingIndex(int index) { sortingIndex = index; }
	
	/**
	 * Returns the sorting index.
	 * 
	 * @return The value to set.
	 */
	int getSortingIndex() { return sortingIndex; }
	
	/**
	 * Sorts the passed collection of nodes.
	 * 
	 * @param nodes The collection to sort.
	 * @return See above.
	 */
	Collection sort(Collection nodes)
	{
		switch (sortingIndex) {
			case Browser.SORT_NODES_BY_DATE:
				return sortByDate(nodes);
			case Browser.SORT_NODES_BY_NAME:
				return sortByName(nodes);
		}
		return nodes;
	}

	/**
	 * Sets the state.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }

}
