package xmlMVC;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SAXValidater {
	
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema"; 
	
	
	public static void main (String[] args)
		throws SAXException,
		ParserConfigurationException,
		IOException
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			
			SAXParser saxParser = factory.newSAXParser();
			saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			saxParser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", "file:///Users/willmoore/ome-MSc/ontologies%20etc/MIACA-Schema%20CAOM102006.xsd");

			
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new MyContentHandler());
			
			try {
				xmlReader.parse(new File(args[0]).toURL().toString());
			} catch (SAXException ex) {
				System.out.println("SAX-exception!");
				ex.printStackTrace();
			}
		}
}
