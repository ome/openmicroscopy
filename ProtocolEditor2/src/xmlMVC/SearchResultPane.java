package xmlMVC;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class SearchResultPane{ 
	
	String path;
	SearchPanel searchPanel;
	
	String paneText;
	
	public SearchResultPane(Document doc, String searchString) {
		
		
		String name = null;
		path = doc.get("path");
		String date = doc.get("modified");
        if (path != null) {
          name = doc.get("name");
        } else {
          System.out.println("No path for this document");
        }
		
        paneText = "<a href='http:/" + path + "'>"+ name + "</a> <br>";
        
        paneText = paneText + date + "<br>";
        
        List fields = doc.getFields();
        String[] searchTerms = searchString.split(" ");
        
        for (Object field: fields) {
        	String fieldName = ((Field)field).name();
        	String value = ((Field)field).stringValue();
        	
        	for (int i=0; i<searchTerms.length; i++) {
        		
        		if (value.contains(searchTerms[i])) {
        			String labelText = fieldName + ": " + value;
        			labelText = labelText.replace(searchTerms[i], "<b>" + searchTerms[i] + "</b>");
        			paneText = paneText + labelText + "<br>";
        		}
        		else {
        			System.out.println("SearchResult: value: " + value + " does not contain " + searchTerms[i]);
                	
        		}
        	}
        	//System.out.println(paneText);
        }
        
        
	}
	
	public String getPaneText() {
		return paneText;
	}

}
