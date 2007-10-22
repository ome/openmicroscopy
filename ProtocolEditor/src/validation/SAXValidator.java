package validation;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SAXValidator {
	
	private static ArrayList<String> errorMessages = new ArrayList<String>();
	
	static public void validate(Document document) throws SAXException {
		Transformer transformer;
		File tempFile = new File("temp");
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource(document);
			Result output = new StreamResult(tempFile);
			transformer.transform(source, output);
			validate(tempFile.getAbsolutePath());
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	static public void validate(String filePath) throws SAXException {
		
		errorMessages.clear();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			
			XMLReader xmlReader = saxParser.getXMLReader();
			
			xmlReader.setContentHandler(new SAXValidator.MyXmlHandler());
			xmlReader.setErrorHandler(new SAXValidator.MyXmlHandler());
			
			xmlReader.parse(new File(filePath).toURL().toString());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (String errorMessage: errorMessages) {
			System.out.println(errorMessage);
		}
	}
	

	public static class MyXmlHandler extends DefaultHandler{

			public void startDocument() {
				System.out.println("STARTING");
				System.out.println();
			}
			
			public void startElement(String uri, String localName, String qualName, Attributes attribs) {
				System.out.println("Start tag: " + qualName);
			}
			
			public void endDocument() {
				System.out.println();
				System.out.println("ENDING NORMALLY!");
			}
			
			
			public void warning(SAXParseException e) {
				System.out.println("Warning:");
				showSpecifics(e);
				System.out.println();
			}
			
			public void error(SAXParseException e) {
				System.out.println("Error:");
				showSpecifics(e);
				System.out.println();
			}
			
			public void fatalError(SAXParseException e) {
				System.out.println("Fatal Error:");
				showSpecifics(e);
				System.out.println();
			}
			
			public void showSpecifics(SAXParseException e) {
				System.out.println(e.getMessage());
				System.out.println(" Line " + e.getLineNumber());
				System.out.println(" Column " + e.getColumnNumber());
				System.out.println(" Document " + e.getSystemId());
				
				errorMessages.add(e.getMessage());
			}


	}

}