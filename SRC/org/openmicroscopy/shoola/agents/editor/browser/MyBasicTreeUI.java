 /*
 * treeModel.MyBasicTreeUI 
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

//Java imports

import java.awt.event.MouseEvent;

import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

/** 
 * This class extends BasicTreeUI in order to override the startEditing()
 * method. 
 * The desired behaviour is to enter editing mode for a given node of the
 * JTree with a single click, instead of requiring one click to select the
 * node, and another to start editing (this is default JTree behaviour).
 * 
 * The startEditing() method is usually called for each click.
 * On the first click, the mouseEvent points to the node that was clicked,
 * a call to DefaultTreeCellEditor.isCellEditable() returns false, 
 * and the selection path is then changed to this node, by calling the
 * selectPathForEvent() method. 
 * On the second click, isCellEditable() is called again. Although it 
 * still returns false, a timer is started. When this timer finishes 
 * (in 1.2 seconds), the startEditing() method is called a third time.
 * This time, it is passed a Null MouseEvent, which causes the
 * editing mode to be entered immediately. 
 * 
 * The overriding startEditing() method (below) causes the above steps 
 * (Selection, then Editing) to occur on a Single mouse click. 
 * 
 * This is achieved by first calling selectPathForEvent() 
 * (if MouseEvent != null), then setting the MouseEvent to null, and 
 * passing this to super.startEditing(), which causes editing to start
 * immediately.
 * 
 * In addition, a check is made, following path selection, that 
 * the Shift key is not held down. If the shift key is used, the 
 * selectPathForEvent() method will have selected multiple paths. 
 * It is important not to enter editing mode following this,
 * because this will cause the selection to revert
 * to a single path. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MyBasicTreeUI 
	extends BasicTreeUI 
{
	
	/**
	 * This method causes path selection, then editing, to start on a single click,
	 * instead of requiring two mouse clicks. 
	 * See the class comments above for more information.
	 * 
	 * @see BasicTreeUI#startEditing(TreePath, MouseEvent) 
	 */
   protected boolean startEditing(TreePath path, MouseEvent event) 
   {  
	   // If the event isn't null, 
	   if (event != null) {
		   
		   // Use the event to change path selection...
		   selectPathForEvent(path, event);
	   
		   //...and stop here if the SHIFT key was used (for multiple selection)
		   if (((event.getModifiers()) &  (MouseEvent.SHIFT_MASK))==1) {
			   return false;
		   }
	   }
	   
	   // Now you want to start editing, by passing the super class method
	   // a null mouse event.
	   return super.startEditing(path, null);
   }
}
