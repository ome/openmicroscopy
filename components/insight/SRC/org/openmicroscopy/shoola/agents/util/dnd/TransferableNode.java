/*
 * org.openmicroscopy.shoola.agents.util.dnd.TransferableNode 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.dnd;


//Java imports
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


//Third-party libraries

//Application-internal dependencies

/** 
 * This is just a dummy Transferable, have to handle the transferable
 * data our own, because of Windows DnD issues.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class TransferableNode 
	implements Transferable
{

	/**
	 * Creates a new instance.
	 * 
	 * @param object The object to transfer.
	 */
	TransferableNode (String s)
	{
	}
	
	/**
	 * Returns the supported flavor.
	 * @see Transferable#getTransferData(DataFlavor)
	 */
	public Object getTransferData(DataFlavor df)
		throws UnsupportedFlavorException, IOException
	{
	    return "";
	}
	
	/**
	 * Returns the <code>true</code> if the specified flavor is the local one.
	 * @see Transferable#isDataFlavorSupported(DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor df)
	{
	    return true;
	}
	
	/**
	 * Returns the supported flavor.
	 * @see Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors()
	{
		return DnDTree.supportedFlavors;
	}
	
}
