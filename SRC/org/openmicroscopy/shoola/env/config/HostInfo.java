package org.openmicroscopy.shoola.env.config;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class HostInfo {
    
    String  host;
    Integer port;
    static final String PORT = "port", HOST = "host";
    
    void setValue(String value, String tag) {
        try {
            if (tag.equals(PORT)) port = new Integer(value); // catch 
            else if (tag.equals(HOST)) host = value;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    String getHost() {
        return host;
    }
    Integer getPort() {
        return port;
    }
    
}
