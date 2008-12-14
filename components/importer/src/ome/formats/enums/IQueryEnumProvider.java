/*
 * ome.formats.enums.IQueryEnumProvider
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.formats.enums;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.api.IQuery;
import ome.formats.enums.handler.EnumHandlerFactory;
import ome.formats.enums.handler.EnumerationHandler;
import ome.model.IEnum;
import ome.model.IObject;

/**
 * An enumeration provider which uses IQuery and a cache to fulfill the
 * contract of an EnumerationProvider.
 *  
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class IQueryEnumProvider implements EnumerationProvider
{
    /** Logger for this class. */
    private static Log log = LogFactory.getLog(IQueryEnumProvider.class);
    
    /** Enumeration cache. */
    private Map<Class<? extends IEnum>, HashMap<String, IEnum>> enumCache = 
        new HashMap<Class<? extends IEnum>, HashMap<String, IEnum>>();
    
    /** Query service. */
    private IQuery iQuery;
    
    /** Enumeration handler factory. */
    private EnumHandlerFactory enumHandlerFactory = new EnumHandlerFactory();
    
    /**
     * Default IQuery based enumeration provider constructor.
     * @param iQuery OMERO query service to use for enumeration lookups.
     */
    public IQueryEnumProvider(IQuery iQuery)
    {
    	this.iQuery = iQuery;
    }
    
    /**
     * Creates an unloaded copy of an enumeration object.
     * @param enumeration Enumeration to copy.
     * @return See above.
     */
    private IEnum copyEnumeration(IEnum enumeration)
    {
        Class<? extends IEnum> klass = enumeration.getClass();
        try
        {
            Constructor<? extends IObject> constructor = 
                klass.getDeclaredConstructor(
                    new Class[] { Long.class, boolean.class });
            return (IEnum) constructor.newInstance(
                new Object[] { enumeration.getId(), false });
        }
        catch (Exception e)
        {
            String m = "Unable to copy enumeration: " + enumeration;
            log.error(m, e);
            throw new EnumerationException(m, klass, enumeration.getValue());
        }
    }
    
    /* (non-Javadoc)
     * @see ome.formats.enums.EnumerationProvider#getEnumeration(java.lang.Class, java.lang.String, boolean)
     */
    public IEnum getEnumeration(Class<? extends IEnum> klass, String value,
    		                    boolean loaded)
    {
        if (klass == null)
            throw new NullPointerException("Expecting not-null klass.");
        if (value == null)
        {
            log.warn("Not performing query for enumeration " + klass +
                     " with value of null.");
            return null;
        }
        if (value.length() == 0)
        {
            log.warn("Not performing query for enumeration " + klass +
                     " with value of zero length.");
            return null;
        }

        HashMap<String, IEnum> enumerations = getEnumerations(klass);
        EnumerationHandler handler = enumHandlerFactory.getHandler(klass);
        IEnum otherEnumeration = enumerations.get("Other");
        // Step 1, check if we've got an exact match for our enumeration value.
        if (enumerations.containsKey(value))
        {
        	if (!loaded)
        	{
        		return copyEnumeration(enumerations.get(value));
        	}
        	return enumerations.get(value);
        }
        // Step 2, check if our enumeration handler can find a match.
        IEnum enumeration = handler.findEnumeration(enumerations, value);
        if (enumeration != null)
        {
        	if (!loaded)
        	{
        		return copyEnumeration(enumeration);
        	}
        	return enumeration;
        }
        // Step 3, fall through to an "Other" enumeration if we have one.
        if (otherEnumeration != null)
        {
            log.warn("Enumeration '" + value + "' does not exist in '" 
            		 + klass + "' setting to 'Other'");
            return otherEnumeration;
        }
        // Step 4, fail hard we have no enumeration to return.
        throw new EnumerationException("Problem finding enumeration:" + value,
                                       klass, value);
    }
    
	/* (non-Javadoc)
	 * @see ome.formats.enums.EnumerationProvider#getEnumerations(java.lang.Class)
	 */
	public HashMap<String, IEnum> getEnumerations(Class<? extends IEnum> klass)
	{
        if (!enumCache.containsKey(klass))
        {
            List<IEnum> enumerationList = 
                (List<IEnum>) iQuery.findAll(klass, null);
            if (enumerationList == null)
                throw new EnumerationException("Problem finding enumeration: ",
                                               klass, null);
            
            HashMap<String, IEnum> enumerations = 
            	new HashMap<String, IEnum>();
            for (IEnum enumeration : enumerationList)
            {
            	enumerations.put(enumeration.getValue(), enumeration);
            }
            enumCache.put(klass, enumerations);
        }
        return enumCache.get(klass);
	}
}
