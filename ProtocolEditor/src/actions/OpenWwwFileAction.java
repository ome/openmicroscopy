package actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ui.IModel;
import util.FileDownload;
import util.ImageFactory;

public class OpenWwwFileAction 
	extends ProtocolEditorAction {
	
	public OpenWwwFileAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Open file from URL");
		putValue(Action.SHORT_DESCRIPTION, "Open an on-line file");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.WWW_FILE_ICON)); 
	}


	public void actionPerformed(ActionEvent e) {
	
		openWwwFile();
	}
	
	public void openWwwFile() {
		Object[] possibilities = {"http://cvs.openmicroscopy.org.uk/svn/specification/Xml/Working/completesample.xml",
				"http://trac.openmicroscopy.org.uk/~will/protocolFiles/experiments/AuroraB%20fix-stain.exp", 
				"http://trac.openmicroscopy.org.uk/~will/protocolFiles/experiments/arwen_slice_1.exp"};
		String url = (String)JOptionPane.showInputDialog(
                frame,
                "Enter a url for an XML file to open:",
                "Open a www file",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                "");
		
		try {
			File downloadedFile = FileDownload.downloadFile(url);
			model.openThisFile(downloadedFile);
		} catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(frame, "invalid URL, please try again");
		}

	}
}
