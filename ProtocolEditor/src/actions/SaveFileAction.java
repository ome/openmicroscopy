package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import util.ImageFactory;

public class SaveFileAction
	extends ProtocolEditorAction {
	
	public SaveFileAction(IModel model) {
		super(model);
	
		putValue(Action.NAME, "Save File");
		putValue(Action.SHORT_DESCRIPTION, "Save the current file");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.SAVE_ICON)); 
	}

	
	
	public void actionPerformed(ActionEvent event) {
		// if the current file is not saved (still called "untitled")
		if (model.getCurrentFile() == null) return;
		
		if (model.getCurrentFile().getName().equals("untitled")) {
			Action action = new SaveFileAsAction(model);
			action.actionPerformed(event);
		}
		else {
			int option = JOptionPane.showConfirmDialog(null, "Save changes? " + 
					"\n This will over-write the original file",
					"Save Changes?", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				model.saveTreeToXmlFile(model.getCurrentFile());
				JOptionPane.showMessageDialog(frame, "Experiment saved.");
			}
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
}
