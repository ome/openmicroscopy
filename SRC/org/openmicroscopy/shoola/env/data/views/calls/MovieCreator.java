/*
 * org.openmicroscopy.shoola.env.data.views.calls.MovieCreator 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to create a movie.
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
public class MovieCreator 
	extends BatchCallTree
{

	/** The results of the call. */
    private Object        results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;

    /**
     * Creates a {@link BatchCall} to create a movie.
     * 
     * @param imageID 	The id of the image.	
     * @param pixelsID 	The id of the pixels set.
     * @param channels 	The channels to map.
     * @param param 	The parameters to create the movie.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long imageID, final long pixelsID, 
    		final List<Integer> channels, final MovieExportParam param)
    {
        return new BatchCall("Creating movie: ") {
            public void doCall() throws Exception
            {
                OmeroImageService os = context.getImageService();
                results = os.createMovie(imageID, pixelsID, channels, param);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the root node of the requested tree.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return results; }
    
    /**
     * Creates a new instance.
     * 
     * @param imageID 	The id of the image.	
     * @param pixelsID 	The id of the pixels set.
     * @param channels 	The channels to map.
     * @param param 	The parameters to create the movie.
     */
	public MovieCreator(long imageID, long pixelsID, List<Integer> channels, 
			MovieExportParam param)
	{
		loadCall = makeBatchCall(imageID, pixelsID, channels, param);
	}
	
}
