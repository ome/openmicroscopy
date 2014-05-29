/*
 * org.openmicroscopy.shoola.env.data.model.ScriptActivityParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class storing information about the script to handle.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ScriptActivityParam
{

    /** Indicates to run the script. */
    public static final int RUN = 0;

    /** Indicates to upload the script. */
    public static final int UPLOAD = 1;

    /** Indicates to download the script. */
    public static final int DOWNLOAD = 2;

    /** Indicates to view the script. */
    public static final int VIEW = 3;

    /** The script to handle. */
    private ScriptObject script;

    /** One of the constants defined by this class. */
    private int index;

    /**
     * Creates a new instance.
     * 
     * @param script The script to handle.
     * @param index  One of the constants defined by this class.
     */
    public ScriptActivityParam(ScriptObject script, int index)
    {
        this.script = script;
        this.index = index;
    }

    /**
     * Returns the index, one of the constants defined by this class.
     * 
     * @return See above.
     */
    public int getIndex() { return index; }

    /**
     * Returns the script to handle.
     * 
     * @return See above.
     */
    public ScriptObject getScript() { return script; }

}
