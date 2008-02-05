package ui;

import java.io.File;
import java.util.List;

import tree.DataFieldNode;
import tree.Tree.Actions;

public interface IModel {
	
	public void openBlankProtocolFile();
	
	public void openThisFile(File file);
	
	public File getCurrentFile();
	
	public void saveTreeToXmlFile(File file);

	public String[] getOpenFileList();
	
	public DataFieldNode getRootNode();
	
	public List<DataFieldNode> getHighlightedNodes();
	
	public void editCurrentTree(Actions newAction);
	
	public void multiplyValueOfSelectedFields(float factor);
	
	// undo/redo
	public boolean canUndo();
	public boolean canRedo();
	public String getUndoCommand();
	public String getRedoCommand();
}
