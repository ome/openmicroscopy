package xmlMVC;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XMLDocument {
	
	static char dirSep = System.getProperty("file.separator").charAt(0);

	
	
	public static Document Document(File f)
    throws IOException, InterruptedException  {
		// make a new, empty document
		Document doc = new Document();

		// Add the url as a field named "path".  Use a field that is 
		// indexed (i.e. searchable), but don't tokenize the field into words.
		doc.add(new Field("path", f.getPath().replace(dirSep, '/'), Field.Store.YES,
				Field.Index.UN_TOKENIZED));
		
		doc.add(new Field("name", f.getName(), Field.Store.YES,
				Field.Index.UN_TOKENIZED));

		// Add the last modified date of the file a field named "modified".  
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new Field("modified",
				DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE),
				Field.Store.YES, Field.Index.UN_TOKENIZED));
		
//		HashMap allAttributes = (new XMLMethods().getAllXmlFileAttributes(f));
//		Iterator keyIterator = allAttributes.keySet().iterator();
//		
//		while (keyIterator.hasNext()) {
//			String key = (String)keyIterator.next();
//			String value = (String)allAttributes.get(key);
//			
//			if ((value != null) && (value.length() > 0)) {
//				doc.add(new Field(key, value, Field.Store.YES, Field.Index.TOKENIZED));
//				System.out.println("XMLDocument: Adding Field: name: " + key + ", value: " + value);
//			}
//		}
		

		// return the document
		return doc;
	}
	

}
