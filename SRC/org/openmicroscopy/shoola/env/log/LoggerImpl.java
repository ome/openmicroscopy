package org.openmicroscopy.shoola.env.log;

// Java import
import org.apache.log4j.Category;

/**
 * It is an adapter that makes use of the Log4j library to implement the operations
 * defined by the interface <code>Logger</code>. Its methods transform the orginal call
 * to a suitable call for Log4j.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public class LoggerImpl
    implements Logger {
    
    // TODO: configure log4j
    public LoggerImpl() {
        
    }
    
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
