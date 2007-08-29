package xmlMVC;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class MyContentHandler extends DefaultHandler {
	public void startDocument() {
		System.out.println("STARTING");
		System.out.println();
	}
	
	public void startElement(String uri, String localName, String qualName, Attributes attributes) {
		System.out.println("Start Tag: " + qualName + ", Uri: " + uri);
	}
	
	public void endDocument() {
		System.out.println();
		System.out.println("ENDING NORMALLY!");
	}
}

