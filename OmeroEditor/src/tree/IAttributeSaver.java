package tree;

import java.util.Map;

public interface IAttributeSaver {

	public void setAttribute(String attributeName, String attributeValue, boolean addToUndoRedoQueue);
	
	/** 
	 * This method allows users to update several attributes at once. 
	 * The title is used for display purposes in the undo/redo queue.
	 * This is only added to undo/redo queue if rememberUndo is true;
	 */
	public Map<String, String> setAttributes(String title, Map<String, String> keyValuePairs, boolean rememberUndo);
	
	public String getAttribute(String attributeName);
	
	public boolean isAttributeTrue(String attributeName);
	
	public Map getAllAttributes();
	
	/**
	 * This is used for checking whether a field has been filled, 
	 * and also to get the attribute for loading a default value
	 * (default should be loaded into first attribute in list). 
	 * 
	 * @return	A list of attribute names that are used to hold the 'value'
	 * 				of this field. 
	 */
	public String[] getValueAttributes();
}
