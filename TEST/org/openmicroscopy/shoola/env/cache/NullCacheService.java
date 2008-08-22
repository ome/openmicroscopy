/*
 * org.openmicroscopy.shoola.env.cache.NullCacheService 
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
package org.openmicroscopy.shoola.env.cache;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
 * @since 3.0-Beta3
 */
public class NullCacheService 
	implements CacheService
{

	public void addElement(int cacheID, Object key, Object element) {
		// TODO Auto-generated method stub
		
	}

	public void clearCache(int cacheID) {
		// TODO Auto-generated method stub
		
	}

	public int createCache() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int createCache(int type) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getElement(int cacheID, Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeCache(int cacheID) {
		// TODO Auto-generated method stub
		
	}

}
