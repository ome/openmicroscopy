package org.openmicroscopy.shoola.env.config;

// Java imports 
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
/** 
 * Implemented as specified by {@linkEntry}.
 * Builds and return an Icon Object
 * @return  An object implementing {@link javax.swing.Icon Icon} or <code>null</code> if the path 
 *                  was invalid.
 */  
    Object getValue() {
        URL     location = IconEntry.class.getResource(value);
        if ( location != null ) {
            return new ImageIcon(location);
        } else {
            //TODO  errorMsg via logService
            System.err.println("Couldn't find file: "+value);
            return null;
        } 
    }
    
    
}
