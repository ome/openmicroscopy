package org.openmicroscopy.shoola.env.log;


/**
 * Defines the operations available to any external class
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public interface Logger {
    public void debug(Object c, String logMsg);
    public void error(Object c, String logMsg);
    public void fatal(Object c, String logMsg);
    public void info(Object c, String logMsg);
    public void warn(Object c, String logMsg);
    
    
}
