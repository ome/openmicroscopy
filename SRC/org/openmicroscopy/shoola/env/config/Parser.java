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
 * In charge of parsing a configuration file, extracting entries 
* (<code>entry</code> and <code>structuredEntry</code> tags) obtaining a <code>Entry</code> object
 * to represent each of those entries, adding the object to a given <code>RegistryImpl</code> object.
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
    //validate against the XMLschema: not yet implemented (no XMLSchema for configFile)
    private     boolean     validating = false; 
    // we only retrieve the content of the following tags
    static String[] tagsEntry = {
        "entry",
        "structuredEntry",
    };
/* creates an instance of Parser with one parameter
 *
 * @param   configFile  configuration file (XML file)
 */
    Parser(String configFile) {
        this.configFile = configFile;
        parse(configFile);
    }
    
/* creates an instance of Parser with two parameters
 *  not useful now b/c no XMLSchema for configFile available
 *
 * @param  configFile                   configuration file (XML file)
 * @param  configFileXMLSchema  XML schema linked to XML configuration file
 */    
    Parser(String configFile, String configFileXMLSchema) {
        this.configFile = configFile;
        this.configFileXMLSchema = configFileXMLSchema;
        validating = true;
        parse(configFile);
    }
    
/* Parse the XML configuration file and build a DOM tree 
 * 
 *@param name   configuraition file (XML file)
 */
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
    
/* retrieves the content of the  entry and structuredEntry tags.
 * Stores the DOM representation i.e. DOM node into an arrayList
 */
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
/* validate against the config schema not yet implemented
 */
    private void validate() {
    }
    
    
}
