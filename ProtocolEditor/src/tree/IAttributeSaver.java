package tree;

import java.util.Map;

public interface IAttributeSaver {

	public void setAttribute(String attributeName, String attributeValue, boolean addToUndoRedoQueue);
	
	public String getAttribute(String attributeName);
	
	public boolean isAttributeTrue(String attributeName);
	
	public Map getAllAttributes();
}
