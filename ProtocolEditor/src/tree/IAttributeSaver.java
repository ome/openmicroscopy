package tree;

import java.util.Map;

public interface IAttributeSaver {

	public void setAttribute(String attributeName, String attributeValue, boolean addToUndoRedoQueue);
	
	/** 
	 * This method allows users to update several attributes at once. 
	 * The title is used for display purposes in the undo/redo queue.
	 * This is only added to undo/redo queue if rememberUndo is true;
	 */
	public void setAttributes(String title, Map keyValuePairs, boolean rememberUndo);
	
	public String getAttribute(String attributeName);
	
	public boolean isAttributeTrue(String attributeName);
	
	public Map getAllAttributes();
}
