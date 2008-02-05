package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.Tree.Actions;
import ui.IModel;

public class ClearFieldsHighltdAction extends ProtocolEditorAction {
	
	public ClearFieldsHighltdAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Clear Values for Highligted fields (and all child fields)");
		putValue(Action.SHORT_DESCRIPTION, null);
		//putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.LOAD_DEFAULTS_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		model.editCurrentTree(Actions.CLEAR_FIELDS_HIGHLIGHTED_FIELDS);
	}
	
	
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
}

