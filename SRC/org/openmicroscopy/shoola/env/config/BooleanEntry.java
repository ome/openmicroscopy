package org.openmicroscopy.shoola.env.config;

// Java imports 
import org.w3c.dom.Node;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class BooleanEntry extends Entry {
    
    private Boolean value;
    BooleanEntry() {
    }
    protected void setContent(Node node) { 
        try {
            Node child = node.getFirstChild(); // has only one child
            value = new Boolean(child.getNodeValue());
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    Object getValue() {
        return value; 
    }
}
