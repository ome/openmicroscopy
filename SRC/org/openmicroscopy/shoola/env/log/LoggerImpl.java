/*
 * org.openmicroscopy.shoola.env.log.LoggerImpl
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

package org.openmicroscopy.shoola.env.log;


//Java imports

//Third-party libraries
import org.apache.log4j.Category;
//Application-internal dependencies

/** 
 * Implements the {@link logger} interface. 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class LoggerImpl
    implements Logger
{
    
    public LoggerImpl() {}
    
	/** Implemented as specified by {@link Logger}. */     
    public void debug(Object c, String logMsg)
    {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.debug(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}. */ 
    public void error(Object c, String logMsg)
    {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.error(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}.*/ 
    public void fatal(Object c, String logMsg)
    {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.fatal(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}. */ 
    public void info(Object c, String logMsg)
    {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.info(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}. */ 
    public void warn(Object c, String logMsg)
    {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.warn(logMsg);
    }
  
}
