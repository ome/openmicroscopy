/*
 * org.openmicroscopy.shoola.env.data.views.calls.TileLoader 
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.Collection;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;
import omero.romio.RegionDef;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.data.Region;
import org.openmicroscopy.shoola.env.rnd.data.Tile;
import org.openmicroscopy.shoola.util.image.geom.Factory;

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
	extends BatchCallTree
{

    /** Helper reference to the image service. */
    private OmeroImageService		service;
    
	/** The lastly retrieve tile. */
    private Object	currentTile;
    
    /** The id of the pixels set. */
    private long pixelsID;
    
    /** The tiles.*/
    private Collection<Tile> tiles;
    
    /** Flag indicating to return the tile as texture or not.*/
    private boolean asTexture;
    
    /** The plane to render.*/
    private PlaneDef pDef;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Loads the tile.
     * 
     * @param tile The tile to load.
     */
    private void loadTile(Tile tile) 
    {
    	Region rt = tile.getRegion();
    	try {
    		pDef.region = new RegionDef(rt.getX(), rt.getY(), 
    				rt.getWidth(), rt.getHeight());
        	tile.setImage(service.renderImage(ctx, pixelsID, pDef, asTexture,
        			false));
		} catch (Exception e) {
			tile.setImage(Factory.createDefaultImageThumbnail(rt.getWidth(), 
					rt.getHeight()));
		}
    	currentTile = tile;
    }
    
    /**
     * Returns the lastly retrieved tile.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return A Map containing the index of the tile and the associated image.
     */
    protected Object getPartialResult() { return currentTile; }
    
    /**
     * Returns <code>null</code> as there's no final result.
     * In fact, tiles are progressively delivered with feedback events. 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }
    
    /**
     * Adds a {@link BatchCall} to the tree for each tile to retrieve.
     * The batch call simply invokes {@link #loadTile(Tile)}.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
    	Iterator<Tile> i = tiles.iterator();
    	String description = "Loading tile";
    	Tile tile;
    	while (i.hasNext()) {
			tile = i.next();
			final Tile t = tile;
			add(new BatchCall(description) {
        		public void doCall() { 
        			loadTile(t);
        		}
        	});  
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param pixelsID 	The id of the pixels set.
     * @param pDef The plane to render.
	 * @param tiles	The tiles.
	 * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
     */
    public TileLoader(SecurityContext ctx, long pixelsID, PlaneDef pDef,
    		Collection<Tile> tiles, boolean asTexture)
    {
        if (pixelsID < 0)
            throw new IllegalArgumentException("ID not valid.");
        if (tiles == null || tiles.size() == 0)
            throw new IllegalArgumentException("No tiles to load.");
        if (pDef == null)
        	 throw new IllegalArgumentException("No plane to render.");
        this.ctx = ctx;
        this.pixelsID = pixelsID;
        this.tiles = tiles;
        this.asTexture = asTexture;
        this.pDef = pDef;
        service = context.getImageService();
    }
    
}
