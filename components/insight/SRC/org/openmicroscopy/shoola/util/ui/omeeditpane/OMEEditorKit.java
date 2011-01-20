/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEEditorKit 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.util.HashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

//Third-party libraries

//Application-internal dependencies

/** 
 * Sets of styles to format the wiki text. 
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
class OMEEditorKit
	extends StyledEditorKit
	implements ViewFactory
{
	
	/** The editor pane for the kit. */
	private JEditorPane 						editorPane;

	/** The format map of the editor pane, mapping regex, and format.*/
	private Map<String, FormatSelectionAction>	formatMap;
	
	/** The view of the editorpane. */
	private WikiView 							view;
	
	/**
	 * Creates the editor kit.
	 * 
	 * @param formatters The available formatters.
	 */
	OMEEditorKit(Map<String, FormatSelectionAction> formatters)
	{
		this.formatMap = formatters;
	}
	
	/**
	 * Adds the specified formatter.
	 * 
	 * @param value The value to identify the formatter.
	 * @param action The action associated.
	 */
	void addFormatter(String value, FormatSelectionAction action)
	{
		if (formatMap == null) 
			formatMap = new HashMap<String, FormatSelectionAction>();
		formatMap.put(value, action);
	}
	
	/**
	 * Returns the view of the edit component.
	 * 
	 * @return See above.
	 */
	public ViewFactory getViewFactory() 
	{
		return this;
	}

	/**
	 * Creates a new instance for element.
	 * 
	 * @param element see above.
	 * @return see above.
	 */
	public View create(Element element) 
	{
		view = new WikiView(element, formatMap, editorPane);
		return view;
	}
	
	/**
	 * Returns the view.
	 * 
	 * @return See above.
	 */
	public WikiView getView() { return view; }
	
	/**
	 * Installs the View on the given EditorPane. This is called by Swing and
	 * can be used to do anything you need on the JEditorPane control.  No 
	 * default actions set. 
	 * @see StyledEditorKit#install(JEditorPane)
	 */
	public void install(JEditorPane editorPane) 
	{
		super.install(editorPane);
		this.editorPane = editorPane;
		Keymap km_parent = JTextComponent.getKeymap(
				JTextComponent.DEFAULT_KEYMAP);
		Keymap km_new = JTextComponent.addKeymap(null, km_parent);
		editorPane.setKeymap(km_new);
	}
	
}


