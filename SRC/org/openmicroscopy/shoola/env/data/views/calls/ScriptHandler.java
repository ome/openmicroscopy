/*
 * org.openmicroscopy.shoola.env.data.views.calls.ScriptRunner 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Creates a batch call to run or upload a script.
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
public class ScriptHandler 
	extends BatchCallTree
{

	/** Indicates to run the script. */
	public static final int	RUN = 0;
	
	/** Indicates to upload the script. */
	public static final int	UPLOAD = 1;
	
	/** The result of the call. */
    private Object				result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   		loadCall;
    
    /**
     * Creates a {@link BatchCall} to upload or run the script.
     * 
     * @param script The script to run or upload. 
     * @param index  One of the constants defined by the script.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCall(final ScriptObject script, final int index)
    {
    	return new BatchCall("Run the script") {
    		public void doCall() throws Exception
    		{
    			OmeroImageService os = context.getImageService();
    			switch (index) {
	    			case RUN:
	    				result = os.runScript(script);
	    				break;
	    			case UPLOAD:
	    				result = os.uploadScript(script);
    			}
    		}
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Map</code>.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param script The script to run or upload.
     * @param index	 One of the constants defined by this class.
     */
    public ScriptHandler(ScriptObject script, int index)
    {
    	if (script == null) 
    		throw new IllegalArgumentException("No script specified."); 
		loadCall = makeCall(script, index);
    }
    
}
