package ui.components;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ProtocolFileFilter extends FileFilter {
	
	public boolean accept(File file) {
		boolean recognisedFileType = 
			//	allows "MS Windows" to see directories
			((file.getName().endsWith("pro")) || (file.getName().endsWith("exp")) || 
					(file.getName().endsWith("xml")) || (file.isDirectory()));
		return recognisedFileType;
	}
		
	public String getDescription() {
		return " .pro .exp .xml files";
	}
	
}
