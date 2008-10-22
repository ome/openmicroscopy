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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.ViewFactory;

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
public class OMEEditorKit
	extends EditorKit
{

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#createCaret()
	 */
	@Override
	public Caret createCaret()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#createDefaultDocument()
	 */
	@Override
	public Document createDefaultDocument()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#getActions()
	 */
	@Override
	public Action[] getActions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#getContentType()
	 */
	@Override
	public String getContentType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#getViewFactory()
	 */
	@Override
	public ViewFactory getViewFactory()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#read(java.io.InputStream, javax.swing.text.Document, int)
	 */
	@Override
	public void read(InputStream in, Document doc, int pos) throws IOException,
			BadLocationException
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#read(java.io.Reader, javax.swing.text.Document, int)
	 */
	@Override
	public void read(Reader in, Document doc, int pos) throws IOException,
			BadLocationException
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#write(java.io.OutputStream, javax.swing.text.Document, int, int)
	 */
	@Override
	public void write(OutputStream out, Document doc, int pos, int len)
			throws IOException, BadLocationException
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#write(java.io.Writer, javax.swing.text.Document, int, int)
	 */
	@Override
	public void write(Writer out, Document doc, int pos, int len)
			throws IOException, BadLocationException
	{
		// TODO Auto-generated method stub
		
	}	

}


