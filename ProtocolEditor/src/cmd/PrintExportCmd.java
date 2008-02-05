package cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import tree.DataFieldNode;
import ui.components.ExportDialog;
import util.HtmlOutputter;
import util.PreferencesManager;

public class PrintExportCmd implements ActionCmd {
	
	JFrame frame = null;
	
	List<DataFieldNode> rootNodes;
	
	public PrintExportCmd(List<DataFieldNode> rootNodes) {
		this.rootNodes = rootNodes;
	}
	
	public PrintExportCmd(DataFieldNode rootNode) {
		rootNodes = new ArrayList<DataFieldNode>();
		rootNodes.add(rootNode);
	}
	
	public void execute() {
		printToHtml();
	}
	
public void printToHtml() {
		
		// Can't work out how to export xsl file into .jar
		// xmlModel.transformXmlToHtml();
		
		// use this method for now
		
		final JFileChooser fc = new JFileChooser();
		
		File currentLocation = null;
		if (PreferencesManager.getPreference(PreferencesManager.CURRENT_EXPORT_FOLDER) != null) {
			currentLocation = new File(PreferencesManager.getPreference(PreferencesManager.CURRENT_EXPORT_FOLDER));
		} 
		fc.setCurrentDirectory(currentLocation);

		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.getName().endsWith(".html") || (f.isDirectory()))
					return true;
				return false;
			}
			public String getDescription() {
				return ".html files";
			}
		});
		int returnVal = fc.showSaveDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fc.getSelectedFile();
		if (file.isDirectory()) {
            JOptionPane.showMessageDialog(frame, "Please choose a file name (not a directory)");
            // try again! 
            printToHtml();
            return;
        }
		
		// remember where the user last exported a file
		PreferencesManager.setPreference(PreferencesManager.CURRENT_EXPORT_FOLDER, file.getParent());
		
		// now, make sure the filename ends .html
        String filePathAndName = file.getAbsolutePath();
        if (!(filePathAndName.endsWith(".html"))) {	// if not..
        	filePathAndName = filePathAndName + ".html";
        	file = new File(filePathAndName);
        }
		
		LinkedHashMap<String,Boolean> booleanMap = new LinkedHashMap<String,Boolean>();
		booleanMap.put("Show every field (include collapsed fields)", true);
		booleanMap.put("Show descriptions", true);
		booleanMap.put("Show default values", true);
		booleanMap.put("Show Url", true);
		booleanMap.put("Show Table Data", true);
		booleanMap.put("Show all other attributes", true);
	
		
		ExportDialog printDialog = new ExportDialog(frame, null, "Print Options", booleanMap);
		printDialog.pack();
		printDialog.setVisible(true);
		
		if (printDialog.getValue().equals(JOptionPane.OK_OPTION)) {
		
			booleanMap = printDialog.getBooleanMap();
			boolean showEveryField = booleanMap.get("Show every field (include collapsed fields)");
			boolean	showDescriptions = booleanMap.get("Show descriptions");
			boolean	showDefaultValues = booleanMap.get("Show default values");
			boolean	showUrl = booleanMap.get("Show Url");
			boolean	showAllOtherAttributes = booleanMap.get("Show all other attributes");
			boolean printTableData = booleanMap.get("Show Table Data");
		
			HtmlOutputter.outputHTML(file, rootNodes, showEveryField, 
					showDescriptions, showDefaultValues, 
					showUrl, showAllOtherAttributes, printTableData);
		}
	}

}
