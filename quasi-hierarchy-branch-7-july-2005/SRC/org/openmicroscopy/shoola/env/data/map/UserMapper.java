/*
 * org.openmicroscopy.shoola.env.data.map.UserMapper
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.map;


//Java imports
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.shoola.env.data.DataManagementService;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class UserMapper
{
	
    public static void setFilters(Criteria c, Map filters, 
                                    Map complexFilters)
    {
        if (filters != null) {
            //get Limit
            Integer i = (Integer) 
                    filters.get(DataManagementService.FILTER_LIMIT);
            if (i != null) c.setLimit(i.intValue());
        }
        if (complexFilters != null) {
            Iterator j = complexFilters.keySet().iterator(), k;
            Map map;
            String column, key;
            Object value;
            while (j.hasNext()) {
                column = (String) j.next();
                map = (Map) complexFilters.get(column);
                k = map.keySet().iterator();
                while (k.hasNext()) {
                    key = (String) k.next();
                    value = map.get(key);
                    if (key.equals(DataManagementService.FILTER_CONTAIN) ||
                        key.equals(DataManagementService.FILTER_NOT_CONTAIN)) 
                       value = "%"+value+"%";
                    c.addFilter( column, key, value);
                } 
            }
        }
    }
    
	public static Criteria getUserStateCriteria()
	{
		Criteria criteria = new Criteria();
		criteria.addWantedField("experimenter");
        criteria.addWantedField("experimenter", "Group");
        criteria.addWantedField("experimenter", "FirstName");
        criteria.addWantedField("experimenter", "LastName");
		return criteria;
	}
    
    /** Field required to ownwer's details. */
    static void objectOwnerCriteria(Criteria c)
    {
        //Specify which fields we want for the owner.
        c.addWantedField("owner", "FirstName");
        c.addWantedField("owner", "LastName");
        c.addWantedField("owner", "Email");
        c.addWantedField("owner", "Institution");
        c.addWantedField("owner", "Group");

        //Specify which fields we want for the owner's group.
        c.addWantedField("owner.Group", "Name");   
    }
    
}
