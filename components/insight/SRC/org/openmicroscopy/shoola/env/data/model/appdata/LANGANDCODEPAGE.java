/*
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.env.data.model.appdata;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Provides a <a href="https://github.com/twall/jna">JNA</a> Structure used to store
 * enumerated languages and code pages on windows
 * 
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/windows/desktop/ms647464
 *      (v=vs.85).aspx">VerQueryValue
 *      function (MSDN)</a>
 * 
 * @author Scott Littlewood, <a
 *         href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class LANGANDCODEPAGE extends Structure {

	/**
	 * Windows language value
	 */
	public short wLanguage;

	/**
	 * Windows CodePage value
	 */
	public short wCodePage;

	/**
	 * Create a @link {@link LANGANDCODEPAGE} populated with the value provided
	 * by the pointer
	 * 
	 * @param pointer
	 *            to the value used to populate the structure
	 */
	public LANGANDCODEPAGE(Pointer pointer) {
		super(pointer);
	}
	
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "wLanguage", "wCodePage" });
    }
}