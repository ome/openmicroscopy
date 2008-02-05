package actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.DataFieldNode;
import ui.IModel;
import cmd.ActionCmd;
import cmd.PrintExportCmd;

public class PrintExportHighltd extends ProtocolEditorAction {
	
	public PrintExportHighltd(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Export highlighted Fields");
		putValue(Action.SHORT_DESCRIPTION, "Exports highlighted fields to html for printing");
		//putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.PRINT_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		List<DataFieldNode> rootNodes = model.getHighlightedNodes();
		
		ActionCmd printAll = new PrintExportCmd(rootNodes);
		printAll.execute();
	}
	
	
	// disable if no files are open
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
}
