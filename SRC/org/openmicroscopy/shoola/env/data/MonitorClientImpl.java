/*
 * org.openmicroscopy.shoola.env.data.MonitorClientImpl 
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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import pojos.DataObject;
import Ice.Current;
import monitors.EventInfo;
import monitors._MonitorClientDisp;

/** 
 * 
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
class MonitorClientImpl 
	extends _MonitorClientDisp
{

	private DataObject container;
	
	private OMEROMetadataStore metadataStore;
	
	private OMEROWrapper reader;
	
	private void importImage(String path)
	{
		ImportLibrary lib = new ImportLibrary(metadataStore, reader);
		lib.setDataset(container.asDataset());
		try {
			lib.importImage(new File(path), 0, 0, 1, path, false);
			metadataStore.createRoot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param gateway
	 * @param container
	 */
	MonitorClientImpl(OMEROMetadataStore metadataStore,
			DataObject container)
	{
		this.container = container;
		this.metadataStore = metadataStore;
		reader = new OMEROWrapper();
	}
	
	/**
	 * Import the image.
	 * @see _MonitorClientDisp#fsEventHappened(String, EventInfo[])
	 */
	public void fsEventHappened(String id, EventInfo[] info, Current __current)
	{
		EventInfo event;
		String path = FSFileSystemView.FS_NAME;
		for (int i = 0; i < info.length; i++) {
			event = info[i];
			path += event.fileId;
			try {
				importImage(path);
				metadataStore.createRoot();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
