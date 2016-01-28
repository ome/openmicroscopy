/*
 * org.openmicroscopy.shoola.agents.imviewer.TileLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.imviewer;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.data.Tile;

/** 
 * Loads the tiles.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TileLoader
	extends DataLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The collection of tiles to load.*/
    private Collection<Tile> tiles;
    
    /** The ID of the pixels set. */
    private long pixelsID;
    
    /** The plane to render.*/
    private PlaneDef pDef;
    
    /** Count the number of tiles loaded.*/
    private int count;
    
    /** The proxy to use.*/
    private RenderingControl proxy;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     * @param pDef The plane to render.
     * @param proxy The rendering control to use.
     * @param tiles The tiles to handle.
     */
	public TileLoader(ImViewer viewer, SecurityContext ctx, long pixelsID,
			PlaneDef pDef, RenderingControl proxy, Collection<Tile> tiles)
	{
		super(viewer, ctx);
		if (tiles == null || tiles.size() == 0)
			throw new IllegalArgumentException("No tiles to load.");
		if (pDef == null)
			throw new IllegalArgumentException("No plane to render.");
		if (proxy == null)
			throw new IllegalArgumentException("No rendering control.");
		if (pixelsID != proxy.getPixelsID())
			throw new IllegalArgumentException("Pixels ID not valid.");
		this.tiles = tiles;
		this.pixelsID = pixelsID;
		this.pDef = pDef;
		this.proxy = proxy;
	}
	
	/**
     * Loads the tiles.
     * @see DataLoader#load()
     */
    public void load()
    {
    	handle = ivView.loadTiles(ctx, pixelsID, pDef, proxy, tiles, this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual pay-load (tile) is delivered progressively
     * during the updates.
     * @see DataLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Tile Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError(s, s, exc);
    }
    
    /** 
     * Feeds the tiles back to the viewer, as they arrive. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
        String status = fe.getStatus();
        int percDone = fe.getPercentDone();
        if (status == null) 
            status = (percDone == 100) ? "" :  //Else
                                     ""; //Description wasn't available.
        viewer.setStatus(status, percDone);
        Tile tile = (Tile) fe.getPartialResult();
        if (tile != null) {
        	count++;
        	if (count == tiles.size()) viewer.setTileCount(count);
        	else viewer.setTileCount(0);
        } 
    }

}
