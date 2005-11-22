/*
 * ome.testing.SqlPropertiesParser
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.testing;

// Java imports
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies

/**
 * utility to parse a properties file. See "resources/test_queries.properties"
 * for an explanation of the format.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public abstract class SqlPropertiesParser
{

    private static Log log = LogFactory.getLog(SqlPropertiesParser.class);

    protected static void load(Properties props, String filename)
    {
        InputStream is = SqlPropertiesParser.class.getClassLoader()
                .getResourceAsStream(filename);

        try
        {
            props.load(is);

        } catch (Exception e)
        {
            throw new RuntimeException("Failed to parse properties file "
                    + filename, e);
        } finally
        {
            if (null != is)
                try
                {
                    is.close();
                } catch (Exception e)
                {
                    throw new RuntimeException(
                            "Failed to close properties file " + filename, e);
                }
        }

    }

    /* last one wins */
    public static Map parse(String[] filenames)
    {
        Map result = new HashMap();
        Properties props = new Properties();
        for (int i = 0; i < filenames.length; i++)
        {
            load(props, filenames[i]);
        }

        for (Enumeration en = props.propertyNames(); en.hasMoreElements();)
        {
            String name = (String) en.nextElement();
            result.put(name, props.getProperty(name));
        }

        for (Iterator it = result.keySet().iterator(); it.hasNext();)
        {
            String name = (String) it.next();
            String value = (String) result.get(name);
            if (!value.startsWith("select"))
            {
                String[] strValues = value.split("\\s*,\\s*");
                Long[] longValues = new Long[strValues.length];
                for (int i = 0; i < strValues.length; i++)
                {
                    longValues[i] = Long.valueOf(strValues[i]);
                }
                result.put(name, Arrays.asList(longValues));
            }
        }

        return result;

    }
}
