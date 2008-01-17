/*
 * org.openmicroscopy.shoola.agents.util.archived.view.DownloaderFactory 
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
package org.openmicroscopy.shoola.agents.util.archived.view;




//Java imports
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;

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
 * @since OME3.0
 */
public class DownloaderFactory
{

	/** The sole instance. */
    private static final DownloaderFactory  singleton = new DownloaderFactory();
    
    /**
     * Returns the {@link Downloader}.
     * 
     * @param ctx 		A reference to the {@link Registry}.
     * @param pixelsID	The pixels set ID.
     * @return See above.
     */
    public static Downloader getDownloader(Registry ctx, long pixelsID)
    {
    	if (singleton.registry == null) singleton.registry = ctx;
    	return singleton.createDownloader(pixelsID);
    }
    
    /**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return singleton.registry; }
    
    /**
     * Helper method. 
     * 
     * @return A reference to the {@link JFrame owner}.
     */
    public static JFrame getOwner()
    { 
    	return getRegistry().getTaskBar().getFrame(); 
    }
    
    /** Reference to the registry. */
    private Registry         registry;
    
    /** Creates a new instance.*/
    private DownloaderFactory() {}
    
    /**
     * Creates a <code>Downloader</code>.
     * 
     * @param pixelsID The set of pixels the downloader is for.
     * @return See above.
     */
    private Downloader createDownloader(long pixelsID)
    {
    	DownloaderModel model = new DownloaderModel(pixelsID);
    	DownloaderComponent component = new DownloaderComponent(model);
    	model.initialize(component);
    	component.initialize();
        return component;
    }
    
}
