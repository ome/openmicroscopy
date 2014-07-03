/*
 * org.openmicroscopy.shoola.util.ui.search.GroupContext 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.util.ui.search;

import pojos.ExperimenterData;

/** 
 * Host information about the experimenter to search for.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ExperimenterContext
{
        /** ID indicating all experimenters should be included in the search */
        public static final int ALL_EXPERIMENTERS_ID = Integer.MAX_VALUE;
    
	/** The experimenter to handle.*/
	private String experimenter;
	
	/** The identifier of the experimenter.*/
	private long id;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param experimenter The name of the experimenter to handle.
	 * @param id The identifier of the experimenter.
	 */
	public ExperimenterContext(String experimenter, long id)
	{
		this.experimenter = experimenter;
		this.id = id;
	}
	
	/**
         * Creates a new instance.
         */
	public ExperimenterContext(ExperimenterData exp)
        {
                this.experimenter = exp.getFirstName()+" "+exp.getLastName();
                this.id = exp.getId();
        }
        
	/**
	 * Returns the id of the experimenter hosted by the component.
	 * 
	 * @return See above.
	 */
	public long getId() { return id; }
	
	/**
	 * Overridden to return the name of the experimenter.
	 * @see Object#toString()
	 */
	public String toString() { return experimenter; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (id ^ (id >>> 32));
            return result;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExperimenterContext other = (ExperimenterContext) obj;
            if (id != other.id)
                return false;
            return true;
        }

	
}
