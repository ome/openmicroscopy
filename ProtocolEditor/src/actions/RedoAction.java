package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.Tree.Actions;
import ui.IModel;
import util.ImageFactory;

public class RedoAction extends ProtocolEditorAction {
	
	public RedoAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Redo");
		putValue(Action.SHORT_DESCRIPTION, "Redo");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.REDO_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		model.editCurrentTree(Actions.REDO_ACTION);
	}
	
	public void stateChanged(ChangeEvent e) {
		setEnabled(model.canRedo());
		putValue(Action.SHORT_DESCRIPTION, model.getRedoCommand());
		refreshName();
	}
	
	// subclass can override this to avoid setting name for button
	protected void refreshName() {
		putValue(Action.NAME, model.getRedoCommand());
	}
}
