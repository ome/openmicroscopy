package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import ui.IModel;
import util.ImageFactory;

public class NewFileAction 
	extends ProtocolEditorAction {
	
	public NewFileAction(IModel model) {
		
		super(model);
		
		putValue(Action.NAME, "New File");
		putValue(Action.SHORT_DESCRIPTION, "Open a New Blank File");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.NEW_FILE_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		model.openBlankProtocolFile();
	}

}
