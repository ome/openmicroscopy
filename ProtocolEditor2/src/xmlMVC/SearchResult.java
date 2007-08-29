package xmlMVC;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class SearchResult extends JPanel {
	
	String path;
	SearchPanel searchPanel;
	
	public SearchResult(Document doc, String searchString) {
		
		this.setLayout(new BorderLayout());
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		Box horizontalBox = Box.createHorizontalBox();
		
		String name = null;
		path = doc.get("path");
		String date = doc.get("modified");
        if (path != null) {
          name = doc.get("name");
        } else {
          System.out.println("No path for this document");
        }
		
        if (name != null) {
        	JButton titleButton = new JButton(name);
        	titleButton.addActionListener(new SearchResultButtonListener());
        	horizontalBox.add(titleButton);
        }

        if (date != null) {
        	JLabel dateLabel = new JLabel("Last Modified: " + date);
        	horizontalBox.add(dateLabel);
        }
        
        this.add(horizontalBox, BorderLayout.NORTH);
        
        Box matchingFieldsPanel = Box.createVerticalBox();
        matchingFieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        if (path != null) {
        	matchingFieldsPanel.add(new JLabel(path));
        }
        
        List fields = doc.getFields();
        String[] searchTerms = searchString.split(" ");
        
        for (Object field: fields) {
        	String fieldName = ((Field)field).name();
        	String value = ((Field)field).stringValue();
        	System.out.println("SearchResult: fieldName: " + fieldName + ", value: " + value);
        	
        	for (int i=0; i<searchTerms.length; i++) {
        		if (value.contains(searchTerms[i])) {
        			String labelText = "<html>" + fieldName + ": " + value + "</html>";
        			labelText = labelText.replace(searchTerms[i], "<b>" + searchTerms[i] + "</b>");
        			matchingFieldsPanel.add(new JLabel(labelText));
        		}
        	}
        }
        //matchingFieldsBox.add(matchingFieldsPanel);
        //matchingFieldsBox.add(Box.createHorizontalGlue());
        this.add(matchingFieldsPanel, BorderLayout.WEST);
        
	}
	
	public class SearchResultButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			openSearchResultFile(path);
		}
	}

	public void openSearchResultFile(String absolutePath) {
		
		File file = new File(absolutePath);
		
		searchPanel.openSearchResultFile(file);
	}
	
	public void setParent(SearchPanel searchPanel) {
		this.searchPanel = searchPanel;
	}
}
