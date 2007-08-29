package xmlMVC;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;

import xmlMVC.ImportElementChooser.DoneButtonListener;


public class SearchPanel extends JPanel {

	boolean raw = false;
	XMLView xmlView;
	
	String resultsText;
	
	public SearchPanel(String searchString, XMLView xmlView) {
		
		this.xmlView = xmlView;
		
		//System.out.println("SearchPanel searchterm: " + searchString);
		
		ArrayList<SearchResultHtml> results = new ArrayList<SearchResultHtml>();
		
		try {
			SearchFiles.search(searchString, results);
		 
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		resultsText = "<html><div style='padding: 5px 5px 5px 5px;'>";
		
		for (SearchResultHtml result: results) {
			resultsText = resultsText + result.getPaneText();
		}
		
		if (results.isEmpty()) {
			resultsText = resultsText + "Your search returned no results";
		}
		
		resultsText = resultsText + "</div></html>";
		
		JEditorPane resultsPane;
		resultsPane = new JEditorPane("text/html", resultsText);

		resultsPane.setEditable(false);
		resultsPane.addHyperlinkListener(new ResultHyperLinkListener());
		//resultsPane.setMinimumSize(new Dimension(300, 1000));

		JScrollPane resultsScrollPane = new JScrollPane(resultsPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//resultsScrollPane.setMaximumSize(new Dimension(300, 500));
		
		Icon noIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		JButton closeButton = new JButton("Close this window", noIcon);
		closeButton.addActionListener(new ClosePanelListener());
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(300, 500));
		this.add(resultsScrollPane, BorderLayout.CENTER);
		this.add(closeButton, BorderLayout.NORTH);
		
		this.setMinimumSize(new Dimension(300, 1000));
		
	}
	
	public void openSearchResultFile(File file) {
		xmlView.openFile(file);
	}
	
	public class ResultHyperLinkListener implements HyperlinkListener {
		public void hyperlinkUpdate (HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				if (event != null) {
					String filePath = event.getURL().toString();
					filePath = filePath.replace("http:/", "");
					System.out.println("SearchPanel.hyperlinkUpdate: filePath: " + filePath);
					
					File file = new File(filePath);
					System.out.println(file.getAbsolutePath());
					
					openSearchResultFile(file);
				}
			}
		}
	}
	
	public class ClosePanelListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			xmlView.updateSearchPanel(null);
		}
	}
	
}
