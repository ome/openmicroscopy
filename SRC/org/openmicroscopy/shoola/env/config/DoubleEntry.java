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

class DoubleEntry extends Entry {
    
    private Double value;
    DoubleEntry() {
    }
/** Implemented as specified by {@linkEntry}.
 */  
    protected void setContent(Node node) { 
        try {
            Node child = node.getFirstChild(); // has only one child
            value = new Double(child.getNodeValue());
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
/** Implemented as specified by {@linkEntry}.
 */  
    Object getValue() {
        return value; 
    }
    
    
}
