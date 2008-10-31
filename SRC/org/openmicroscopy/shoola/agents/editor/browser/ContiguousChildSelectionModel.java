 /*
 * org.openmicroscopy.shoola.agents.editor.browser.ContiguousChildSelectionModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser;

// Java Imports

import java.util.Vector;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

/** 
 * This Selection Model ensures that all selection paths are Contiguous, and
 * that they also have the same parent (ie - they are siblings). 
 * 
 * This class extends DefaultTreeSelectionModel, and overrides 
 * setSelectionPaths(TreePath[] paths), to check that the parent of 
 * all the new paths are the same. If they are, it calls the 
 * super.setSelectionPaths(paths). 
 * If not, it picks the first or last item
 * (whichever is judged to be the most recently clicked) and passes this
 * in a single-item array to 
 * super.setSelectionPaths(newPaths).
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ContiguousChildSelectionModel 
	extends DefaultTreeSelectionModel 
{
	
	/**
	 * Creates an instance. 
	 * Sets selectionMode to {@link DISCONTIGUOUS_TREE_SELECTION}
	 */
	public ContiguousChildSelectionModel() 
	{
		super();
		setSelectionMode(DISCONTIGUOUS_TREE_SELECTION);
	}
	
	
	 /**
     * Sets the selection to the paths in paths.  This checks that the parent of 
	 * all the new paths are the same. If they are, it calls the 
	 * super.setSelectionPaths(paths). 
	 * If not, it picks the first or last item
	 * (whichever is judged to be the most recently clicked) and passes this
	 * in a single-item array to 
	 * super.setSelectionPaths(newPaths).
	 * 
	 * @see DefaultTreeSelectionModel.setSelectionPaths(TreePath[])
     *
     * @param pPaths new selection
     */
   public void setSelectionPaths(TreePath[] pPaths) 
   { 
	   if ((pPaths != null) && (pPaths.length > 0)) {
		   TreePath firstPathParent = pPaths[0].getParentPath();
		   
		   // If parentPath is null, then the first path is the root.
		   // Simply select this
		   if (firstPathParent == null) {
			   TreePath[] paths = new TreePath[1];
			   paths[0] = pPaths[0];
			   super.setSelectionPaths(paths);
			   return;
		   }
		   
		   // Check first and last paths in the list, to check they both have the
		   // same parent. 
		   // THIS ASSUMES that the paths are "in order" and that the last or
		   // first element is the one that has just been clicked to select
		   // the range. 
		   // If these don't have same parent , then simply setSelectionPaths();
		   if (pPaths.length > 1) {
			   TreePath lastParent = pPaths[pPaths.length-1].getParentPath();
			   if (! firstPathParent.equals(lastParent)) {
				   
				   // In setting a new selection of a single path,
				   // Need to make sure it is not the same path as 
				   // is currently selected...
				   
				   // a new single-item array, to hold the new path
				   TreePath[] paths = new TreePath[1];
				   
				   TreePath[] oldPaths = getSelectionPaths();
				   if ((oldPaths.length > 0) &&
					   (! pPaths[0].equals(oldPaths[0]))) {
					   // if the first items in the old and new paths are 
					   // NOT the same, this will move the selection
					   paths[0] = pPaths[0];
				   }
				   else {
					   // otherwise, this will move the selection
					   paths[0] = pPaths[pPaths.length - 1];
				   }
				   
				   super.setSelectionPaths(paths);
				   return;
				   
			   } 
			   // So, First and last paths DO have the same parent,
			   // Need to get an array that has all the intervening
			   // paths that have the same parent! 
			   Vector<TreePath> paths = new Vector<TreePath>();
			   // add the first path
			   paths.add(pPaths[0]);
			   
			   for (int i=1; i<pPaths.length; i++) {
				   if (firstPathParent.equals(pPaths[i].getParentPath())) {
					   // add others that have the same parent
					   paths.add(pPaths[i]);
				   }
			   }
			   
			   // convert Vector to array
			   TreePath[] childPaths = new TreePath[paths.size()];
			   int pathCount = 0;
			   for (TreePath path: paths) {
				   childPaths[pathCount] = path;
				   pathCount++;
			   }
			   super.setSelectionPaths(childPaths);
			   return;
		   }
	   }
	   
	   // pPaths.length = 0 or 1?
	   // Therefore, we can simply setSelectionPaths()
	   super.setSelectionPaths(pPaths);
	
   }
   
   
	
	/**
     * This overrides DefaultTreeSelectionModel.addSelectionPaths(TreePath[])
     * The intention is to ensure that all new paths have the same parent
     * (are siblings) with the existing selected paths. 
     * However, this method never seems to be called by clicking on new
     * nodes to select additional paths. 
     * So, hasn't been tested! 
     *
     * @param paths the new path to add to the current selection
     */
   public void addSelectionPaths(TreePath[] paths) 
   {
	
	   // Check if any new paths have different parent 
	   
	   TreePath[] currentPaths = this.getSelectionPaths();
	   // if no selection, simple set the selection paths to the new paths
	   if ((currentPaths == null) || (currentPaths.length == 0)) {
		   setSelectionPaths(paths);
		   return;
	   }
	   TreePath currentParent = currentPaths[0].getParentPath();
	   
	   // If the parent of the current path is null, then 
	   // the current path is the root, so it is OK to 
	   // simply set the selection paths to the new paths.
	   if (currentParent == null) {
		   setSelectionPaths(paths);
		   return;
	   }
	   
	   // Check each new path, to make sure that it's parent is the 
	   // same as current parent.
	   // If not, then simply setSelectionPaths();
	   for (int i=0; i<paths.length; i++) {
		   TreePath newParent = paths[i].getParentPath();
		   if (! currentParent.equals(newParent)) {
			   setSelectionPaths(paths);
			   return;
		   }
	   }
	   
	   // By this point, we can be sure that the current parent is not 
	   // null and that all the new paths
	   // have the same parent as the current path.
	   // Therefore, simply add them. 
	   super.addSelectionPaths(paths);
   }
}
