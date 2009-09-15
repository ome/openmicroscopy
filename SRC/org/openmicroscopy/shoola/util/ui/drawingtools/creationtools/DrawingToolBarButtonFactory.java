/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingToolBarButtonFactory 
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
package org.openmicroscopy.shoola.util.ui.drawingtools.creationtools;

//Java imports
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;

//Third-party libraries
import org.jhotdraw.app.action.DeleteAction;
import org.jhotdraw.app.action.DuplicateAction;
import org.jhotdraw.app.action.SelectAllAction;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.draw.action.ButtonFactory;
import org.jhotdraw.draw.action.MoveToBackAction;
import org.jhotdraw.draw.action.MoveToFrontAction;
import org.jhotdraw.draw.action.SelectSameAction;
import org.jhotdraw.util.ResourceBundleUtil;

//Application-internal dependencies

/** 
 * Helper methods to add controls to toll bar.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DrawingToolBarButtonFactory
{
    
	/** Labels base name. */
	private static final String BASE_NAME = "org.jhotdraw.draw.Labels";
	
    /**
     * Creates the collection of drawing actions.
     * 
     * @param edit The drawing editor.
     * @return See above.
     */
    private static Collection<Action> createDrawingActions(DrawingEditor edit)
    {
        LinkedList<Action> a = new LinkedList<Action>();
        a.add(new SelectAllAction());
        a.add(new SelectSameAction(edit));
        a.add(new DeleteAction());
        return a;
    }
    
    /**
     * Creates the collection of selections actions.
     * 
     * @param edit The drawing editor.
     * @return See above.
     */
    private static Collection<Action> createSelectionActions(DrawingEditor edit)
    {
        LinkedList<Action> a = new LinkedList<Action>();
        a.add(new DuplicateAction());
        a.add(null); // separator
        a.add(new MoveToFrontAction(edit));
        a.add(new MoveToBackAction(edit));
        return a;
    }
    
    /**
     * Adds a selection tool to the toolbar.
     * 
     * @param tb		The tool bar displaying the controls.
     * @param editor	The drawing editor.
     * @param readOnly  If the objects are read only then no menus should be 
     * 					displayed.
     */
    public static void addSelectionToolTo(JToolBar tb, 
    					final DrawingEditor editor, boolean readOnly)
    {
    	if(!readOnly)
    		ButtonFactory.addSelectionToolTo(tb, editor, 
        		createDrawingActions(editor), createSelectionActions(editor));
    	else
    		ButtonFactory.addSelectionToolTo(tb, editor, 
    				new LinkedList<Action>(), new LinkedList<Action>()); 
    }
    
    /**
     * Adds a tool to the tool bar.
     * 
     * @param tb		The tool bar displaying the controls.
     * @param editor	The drawing editor.
     * @param tool		The tool to add.
     * @param labelKey	The label linked to the key.
     */
    public static void addToolTo(JToolBar tb, DrawingEditor editor,
            Tool tool, String labelKey)
    {
    	ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle(BASE_NAME);
    	ButtonFactory.addToolTo(tb, editor, tool, labelKey, labels);
    }
    
    /**
     * Creates a basic tool bar.
     * 
     * @return See above.
     */
    public static JToolBar createDefaultBar()
    {
    	JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.putClientProperty("toolButtonGroup", new ButtonGroup());
		return toolBar;
    }
    
}


