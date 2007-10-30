package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import tree.DataField;

// class to hold some useful XML methods

public class XMLMethods {

	ArrayList<HashMap> elementList = new ArrayList<HashMap>();
	

// given an xml file, this method returns an arrayList of elements, 
// each one as a hashmap of attributes (elementPath+attributeName, value)
public ArrayList<HashMap> getAllXmlFileAttributes(File file) throws FileNotFoundException, SAXParseException {
	
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new XMLContentHandler());
			xmlReader.parse(file.toURL().toString());
		
		} catch (SAXParseException spEx) {
			throw spEx;
		} catch (SAXException sEx) {
			sEx.printStackTrace();
		} catch (ParserConfigurationException pcEx) {
			pcEx.printStackTrace();
		} catch (FileNotFoundException fnfEx) {
			throw fnfEx;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return elementList;
	}
	
	
	public class XMLContentHandler extends DefaultHandler {
		
		String elementPath = "";
		
		public void startElement(String uri, String localName, String qualName, Attributes attribs) {
			
			String elementName = attribs.getValue(DataField.ELEMENT_NAME);
			if (elementName != null) elementName = elementName.replace("/", "-");	// don't add any confusing / into path
			
			elementPath = elementPath + "/" + elementName;
			
			HashMap attributeMap = new HashMap();
			
			for (int i=0; i<attribs.getLength(); i++) {
				
				attributeMap.put(elementPath + "/" + attribs.getQName(i), attribs.getValue(i));
				
			}
			elementList.add(attributeMap);
			
		}
		
		public void endElement(String uri, String localName, String qualName) {
			
			// remove the last /elementName from the path
			
			int lastSlashIndex = elementPath.lastIndexOf("/");
			if (lastSlashIndex > 1)
				elementPath = elementPath.substring(0, lastSlashIndex);
		}
		
	}

}
