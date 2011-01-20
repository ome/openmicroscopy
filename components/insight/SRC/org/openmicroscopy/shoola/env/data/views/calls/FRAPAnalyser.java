/*
 * org.openmicroscopy.shoola.env.data.views.calls.FRAPAnalyser 
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

//Third-party libraries

//Application-internal dependencies
import java.util.List;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * FRAP analysis.
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
public class FRAPAnalyser
	extends BatchCallTree
{

	/** The results of the call. */
    private Object        results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;

    /**
     * Creates a {@link BatchCall} to analyze the images.
     * 
     * @param ids   The objects to analyse.
	 * @param type 	The type of node to analyse.
	 * @param param The parameters.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final List<Long> ids, 
    		final Class type, final Object param)
    {
        return new BatchCall("Loading user's images: ") {
            public void doCall() throws Exception
            {
                OmeroImageService os = context.getImageService();
                results = os.analyseFrap(ids, type, param);
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
     * @param ids   The objects to analyze.
	 * @param type 	The type of node to analyze.
	 * @param param The parameters.
     */
	public FRAPAnalyser(List<Long> ids, Class type, Object param)
	{
		loadCall = makeBatchCall(ids, type, param);
	}
}
