package org.openmicroscopy.shoola.env.config;

// Java imports 
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class MapEntry 
    extends Entry {
    
    private Map     tagsValues;
    
    MapEntry() {
    }
    
/** Implemented as specified by {@linkEntry}.
 */    
    protected void setContent(Node node) { 
        tagsValues = new HashMap();
        try {
            if (node.hasChildNodes()) {
                NodeList childList = node.getChildNodes();
                for (int i = 0; i<childList.getLength(); i++){
                    Node child = childList.item(i);
                    if (child.getNodeType() == child.ELEMENT_NODE)
                        tagsValues.put(child.getNodeName(), child.getFirstChild().getNodeValue());
                }
            }  
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    
/** Implemented as specified by {@linkEntry}.
 */  
    Object getValue() {
        return tagsValues; 
    }
    
    
}
