/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd.ModuleSelection
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
 * Written by:    Harry Hochheiser <hsh@nih.gov>,based on Sun's StringSelection
 * implementation
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd;


//Java imports
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


//Third-party libraries

//Application-internal dependencies


/**
 * To do chain selection and module drag/drop,we need to create special-purpose
 * selection and flavor classes. They look almost identical, but we need 
 * separate classes to distinguish between the two cases.
 *
 * @author Harry Hochheiser
 * @version 2.1
 * @since OME2.1
 */  
 public class ModuleSelection implements Transferable {
 	
	
	private String data;
		
					   
	 public ModuleSelection(int d) {
		 this.data = Integer.toString(d);
	 }
	 
	
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor [] res =  new DataFlavor[1];
		res[0] = ModuleFlavor.moduleFlavor;
		return res;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(ModuleFlavor.moduleFlavor);
	}

	public Object getTransferData(DataFlavor flavor)
				 throws UnsupportedFlavorException, IOException
	{
		 if (flavor.equals(ModuleFlavor.moduleFlavor))
			 return (Object)data;
		 else 
			throw new UnsupportedFlavorException(flavor);
	}
 }
