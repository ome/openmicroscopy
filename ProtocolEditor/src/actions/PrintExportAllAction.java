package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import cmd.ActionCmd;
import cmd.PrintExportCmd;

import tree.DataFieldNode;
import ui.IModel;

public class PrintExportAllAction extends ProtocolEditorAction {
	
	public PrintExportAllAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Export the whole document");
		putValue(Action.SHORT_DESCRIPTION, "Exports the entire document to html for printing");
		//putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.PRINT_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		DataFieldNode rootNode = model.getRootNode();
		
		ActionCmd printAll = new PrintExportCmd(rootNode);
		printAll.execute();
	}
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
	
}
