package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.Tree.Actions;
import ui.IModel;
import util.ImageFactory;

public class UndoAction extends ProtocolEditorAction {
	
	public UndoAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Undo");
		putValue(Action.SHORT_DESCRIPTION, "Undo");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.UNDO_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		model.editCurrentTree(Actions.UNDO_LAST_ACTION);
	}
	
	public void stateChanged(ChangeEvent e) {
		setEnabled(model.canUndo());
		putValue(Action.SHORT_DESCRIPTION, model.getUndoCommand());
		refreshName();
	}
	
	// subclass can override this to avoid setting name for button
	protected void refreshName() {
		putValue(Action.NAME, model.getUndoCommand());
	}
}
