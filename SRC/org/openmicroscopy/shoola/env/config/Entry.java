package org.openmicroscopy.shoola.env.config;

//Java imports
import java.util.HashMap;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Sits at the base of a hierarchy of classes that represent entries in configuration file
 * It represents a name-value pair, where the name is the content of the <code>name</code>
 * attribute.
 * Subclasses of <code>Entry</code> implement the <code>setContent()</code> method to grab the tag's
 * content which is then used for building the object returned by the implementation of 
 * <code>getValue()<code>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

abstract class Entry {
    
    static  HashMap     contentHandlers;
    static  String      NAME = "name", TYPE = "type", DEFAULT = "string";
    static {
        contentHandlers = new HashMap();
        contentHandlers.put("string", StringEntry.class);
        contentHandlers.put("integer", IntegerEntry.class);
        contentHandlers.put("float", FloatEntry.class);
        contentHandlers.put("double", DoubleEntry.class);
        contentHandlers.put("boolean", BooleanEntry.class);
        contentHandlers.put("OMEIS", OMEISEntry.class);
        contentHandlers.put("OMEDS", OMEDSEntry.class);
        contentHandlers.put("font", FontEntry.class);
        contentHandlers.put("icon", IconEntry.class);
    }
    private String name;
    private static class NameTypePair {
        String  name, type;
    }
    
/* For a given entry or structuredEntry tag, creates a concrete <code>Entry</code> object to
 * handle the conversion of the tag's content into an object
 *
 *@param   n    DOM node representing the tag
 */  
    
    static Entry createEntryFor(Node node) {
        Entry entry = null;
        if (node.hasAttributes()) { // to be removed when we have xmlSchema (config)
            NameTypePair ntp = retrieveEntryAttributes(node);
            String key = ntp.type==null? DEFAULT : ntp.type;
            Class handler = (Class)contentHandlers.get(key);
            try {
                if (handler == null) handler = Class.forName(key); 
                entry = (Entry)handler.newInstance();
            } catch(Exception e) { throw new RuntimeException(e); } 
            entry.name = ntp.name;
            entry.setContent(node);
        } //throw...
        return entry;
    }
    
/* retrieves the value of the attributes name and type and initializes
 *
 *@param   n    DOM node
 */    
    private static NameTypePair retrieveEntryAttributes(Node n) {
        NameTypePair    ntp = new NameTypePair();
        NamedNodeMap    list = n.getAttributes();
        try {
            for (int i = 0; i<list.getLength(); ++i) {
                Node na = list.item(i);
                if (na.getNodeName() == NAME ) ntp.name = na.getNodeValue();
                else if (na.getNodeName() == TYPE )  ntp.type = na.getNodeValue();
            }
        } catch (DOMException dex) { throw new RuntimeException(dex); }
        if( ntp.name == null || ntp.name.length()==0)
            throw new RuntimeException(" Blah..");
        return ntp;
    }
/* returns the content of the <code>name</code> attribute of a configuration entry
 */  
    public String   getName() {
        return name;
    }

    abstract Object getValue();
    protected abstract void setContent(Node node);
    
    
}
