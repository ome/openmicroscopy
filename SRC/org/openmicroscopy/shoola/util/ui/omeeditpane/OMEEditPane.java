/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEEditPane 
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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class OMEEditPane
	extends JTextPane
{
	
	/** Regex expression for text. */
	public static String TEXTREGEX = "[-a-zA-Z0-9+&@#/%?~_|!:,.;]*";
	
	/** Regex for a sentence. */
	public static String SENTENCEREGEX = "[-a-zA-Z0-9+&@#/%?~_|!:,. ;]*";
	
	/** Regex for a sequence of characters. */
	public static String CHARACTERREGEX = "[a-zA-Z]+[a-zA-Z0-9]+";
	
	/** Regex for a wiki link. */
	public static String WIKILINKREGEX = "\\[\\["+SENTENCEREGEX+"\\]\\]";
	
	/** Regex expression defining Thumbnail [Thumbnail: id 30]. */
	public static String THUMBNAILREGEX = "\\[(Thumbnail|thumbnail):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";

	/** Regex expression defining Dataset [Dataset: id 30]. */
	public static String DATASETREGEX = "\\[(Dataset|dataset):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";

	/** Regex expression defining Project [Project: id 30]. */
	public static String PROJECTREGEX = "\\[(Project|project):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";
	
	/** Regex expression defining Image [Image: id 30]. */
	public static String IMAGEREGEX = "\\[(Image|image):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";
	
	/** Regex expression defining Wiki Heading. */
	public static String HEADINGREGEX = "(^[=]{3}[ ]+"+SENTENCEREGEX+"[ ]+[=]{3}[ ]*$|^[=]{2}[ ]+"+SENTENCEREGEX+"[ ]+[=]{2}[ ]*$|^[=]{1}[ ]+"+SENTENCEREGEX+"[ ]+[=]{1}[ ]*$)";
			
	/** Regex for a bullet list. */
	public static String BULLETREGEX = "^\\*[ ]+"+SENTENCEREGEX;

	/** Regex for bold. */
	public static String BOLDREGEX = "'''"+SENTENCEREGEX+"'''";

	/** Italic regex. */
	public static String ITALICREGEX = "''"+SENTENCEREGEX+"''";

	/** Italic and bold regex. */
	public static String ITALICBOLDREGEX = "'''''"+SENTENCEREGEX+"'''''";

	/** Indent regex. */
	public static String INDENTREGEX = "^[:]+"+SENTENCEREGEX+"$";
		
	/** Regex expression defining url. */
	public static String URLREGEX = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	
	/** Regex for names linked regex. */
	public static String NAMEDLINKREGEX = "\\["+URLREGEX+"[ ]+"+SENTENCEREGEX+"\\]";
	
	/** The editor kit	 */
	private OMEEditorKit	editorKit;
	
	/**
	 * Define the OMEEditPane, create the default maps, and styles. 
	 * @param formatters the formatters for the text.
	 */
	public OMEEditPane(Map<String, FormatSelectionAction> formatters)
	{
		super();
		editorKit = new OMEEditorKit(formatters);
		this.setEditorKitForContentType("text/wiki", editorKit);
	    this.setContentType("text/wiki");
	}
	
	/**
	 * Called when the mouse is pressed and calls the actionPerformed event for
	 * the appropriate SelectionAction of the formatter.value.
	 * @param e mouse event.
	 * @throws BadLocationException if the mouse is outside text.
	 */
	public void onSelection(MouseEvent e) throws BadLocationException
	{
		int index = viewToModel(new Point(e.getX(), e.getY()));
		WikiView view = editorKit.getView();
		SelectionAction selectionAction = view.getSelectionAction(index);
		if(selectionAction != null)
		{
			selectionAction.onSelection(view.getSelectedText(index));
		}
	}
	
	
}



