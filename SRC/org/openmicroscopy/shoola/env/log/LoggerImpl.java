/*
 * org.openmicroscopy.shoola.env.log.LoggerImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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


/*------------------------------------------------------------------------------
 *
* Written by:     Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *                      <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 *                      Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *                      <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.log;

// Java import
import org.apache.log4j.Category;

/** It is an adapter that makes use of the Log4j library to implement the operations
 * defined by the interface <code>Logger</code>. Its methods transform the orginal call
 * to a suitable call for Log4j.
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public class LoggerImpl
    implements Logger {
    
    // TODO: configure log4j
    public LoggerImpl() {}
    
/** Implemented as specified by {@linkLogger}.
 */     
    public void debug(Object c, String logMsg) {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.debug(logMsg);
    }
    
/** Implemented as specified by {@linkLogger}.
 */ 
    public void error(Object c, String logMsg) {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.error(logMsg);
    }
    
/** Implemented as specified by {@linkLogger}.
 */ 
    public void fatal(Object c, String logMsg) {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.fatal(logMsg);
    }
    
/** Implemented as specified by {@linkLogger}.
 */ 
    public void info(Object c, String logMsg) {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.info(logMsg);
    }
    
/** Implemented as specified by {@linkLogger}.
 */ 
    public void warn(Object c, String logMsg) {
        Category category = null;
        if (c != null) category = Category.getInstance(c.getClass().getName());
        else category = Category.getRoot();
        category.warn(logMsg);
    }

    
}
