package org.openmicroscopy.shoola.env.config;

// Java imports 
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class IconEntry extends Entry {
    
    private String value;
    IconEntry() {
    }
    
/** Implemented as specified by {@linkEntry}.
 */  
    protected void setContent(Node node) { 
        try {
            //the node is supposed to have tags as children, add control b/c we don't use yet a 
            // XMLSchema config
            if (node.hasChildNodes()) {
                NodeList childList = node.getChildNodes();
                String host = null, port = null;
                for (int i = 0; i<childList.getLength(); i++){
                    Node child = childList.item(i);
                    if (child.getNodeType() == child.ELEMENT_NODE) 
                        value = child.getFirstChild().getNodeValue();
                }
            }  
        } catch (DOMException dex) { throw new RuntimeException(dex); }
    }
/** Implemented as specified by {@linkEntry}.
 */  
    Object getValue() {
        return value; 
    }
    
    
}
