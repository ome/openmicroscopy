package org.openmicroscopy.shoola.env.config;

//Java imports 
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
class Parser{

    private     Document    document;
    private     String      configFile;
    private     String      configFileXMLSchema;
    private     ArrayList   entriesTags;
    //to validate against the XMLschema: not yet implemented (no XMLSchema for configFile)
    private     boolean     validating = false; 
    // we only retrieve the content of the following tags
    static String[] tagsEntry = {
        "entry",
        "structuredEntry",
    };
    Parser(String configFile) {
        this.configFile = configFile;
        parse(configFile);
    }
    // not useful now b/c no XMLSchema for configFile available
    Parser(String configFile, String configFileXMLSchema) {
        this.configFile = configFile;
        this.configFileXMLSchema = configFileXMLSchema;
        validating = true;
        parse(configFile);
    }
    private void parse(String name) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(name));
            if (validating) {
                factory.setValidating(true);   
                factory.setNamespaceAware(true);
                validate();
            }
            readConfigEntries();
            Iterator i = entriesTags.iterator();
            while (i.hasNext()){
               Node node = (Node)i.next();
               Entry entry = Entry.createEntryFor(node);
            }
        } catch (Exception e) { throw new RuntimeException(e); }   
    }
    
/* read all "entry" and "structuredEntry" tags */
    private void readConfigEntries() {
        entriesTags = new ArrayList();
        for (int k = 0; k<tagsEntry.length; ++k) {
            NodeList list = document.getElementsByTagName(tagsEntry[k]);
            int l = entriesTags.size();
            for (int i = 0; i<list.getLength();++i) {
                Node n = list.item(i);
                if (n.hasChildNodes()) entriesTags.add(k*l+i, n);
            }
        }
    }
// validate against the config schema not yet implemented
    private void validate() {
    }
}
