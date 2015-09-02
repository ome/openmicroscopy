/*
* pojos.WorkFlow
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
*------------------------------------------------------------------------------
*/
package omero.gateway.model;

//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.Namespace;
import omero.model.NamespaceI;
import omero.rtypes;
import pojos.DataObject;

/**
 * The data that makes up an <i>OME</i> worflow object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class WorkflowData
	extends DataObject 
{
	
	/** The default workflow, i.e. nothing .*/
	public static String DEFAULTWORKFLOW = "Default";

	/**
	 * Converts a CSV string to a list of strings.
	 *
	 * @param str The CSV string to convert.
	 * @return See above.
	 */
	private List<String> CSVToList(String str)
	{
		List<String> list = new ArrayList<String>();
		String[] valueString = str.split(",");
		for (String keyword : valueString)
			if (!keyword.equals("[]"))  {
                list.add(keyword);
            }
		return list;
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param workflow The workflow object.
	 */
	public WorkflowData(Namespace workflow)
	{
		if (workflow == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(workflow);
	}
	
	/**
	 * Creates a new instance.  
	 * 
	 * @param nameSpace The namespace of the workflow.
	 * @param keywords The keywords of the workflow.
	 */
	public WorkflowData(String nameSpace, List<String> keywords)
	{
		setDirty(true);
		setValue(new NamespaceI());
		
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
   		workflow.setName(rtypes.rstring(nameSpace));
		workflow.setKeywords((String[]) keywords.toArray());
	}
	
	/**
	 * Creates a new instance.  
	 * 
	 * @param nameSpace The namespace of the workflow.
	 * @param keywords The keywords of the workflow.
	 */
	public WorkflowData(String nameSpace, String keywords)
	{
		setDirty(true);
		setValue(new NamespaceI());
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
   		workflow.setName(rtypes.rstring(nameSpace));
		workflow.setKeywords((String[]) CSVToList(keywords).toArray());
	}
	
	/** Creates a new instance. */
	public WorkflowData()
	{
		setDirty(true);
		setValue(new NamespaceI());
	}

	/**
	 * Returns the namespace of this workflow.
	 * 
	 * @return See above.
	 */
	public String getNameSpace()
	{
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
  		RString namespace = workflow.getName();
  		if (namespace != null)
            return namespace.getValue();
        return "";
	}
	
	/**
	 * Returns the keywords of this workflow.
	 * 
	 * @return See above.
	 */
	public String getKeywords()
	{
		Namespace workflow = (Namespace) asIObject();
		String keywordString = "";
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
  		String[] keywords = workflow.getKeywords();
  		for (int i = 0 ; i < keywords.length; i++)
  		{
  			keywordString = keywordString + keywords[i];
  			if (i < keywords.length-1)
  				keywordString = keywordString + ",";
  		}
  		return keywordString;
	}
	
	/**
	 * Returns the keywords of this workflow as a list.
	 * 
	 * @return See above.
	 */
	public List<String> getKeywordsAsList()
	{
		Namespace workflow = (Namespace) asIObject();
		List<String> keywordList = new ArrayList<String>();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
  		String[] keywords = workflow.getKeywords();
  		if (keywords != null)
  			for (String keyword : keywords)
  				keywordList.add(keyword);
        return keywordList;
	}
		
	/**
	 * Adds a new keyword to the workflow. 
	 * 
 	 * @param keyword See above.
	 */
	public void addKeyword(String keyword)
	{
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
  		if (contains(keyword))
			throw new IllegalArgumentException("Keyword already exists.");
		List<String> keywords = getKeywordsAsList();
		keywords.add(keyword);
		setKeywords(keywords);
	}
		
	/**
	 * Sets the keywords of the workflow. 
	 * 
 	 * @param keywords See above.
	 */
	public void setKeywords(String keywords)
	{
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
		setDirty(true);
		Object keywordObject =  CSVToList(keywords);
		List<String> keywordsList = (List<String>) keywordObject;
		String[] keywordsArray = new String[keywordsList.size()];
		for (int i = 0; i < keywordsList.size(); i++)
			keywordsArray[i]=keywordsList.get(i);
		workflow.setKeywords(keywordsArray);
	}
	
	/**
	 * Sets the keywords of the workflow. 
	 * 
 	 * @param keywords See above.
	 */
	public void setKeywords(List<String> keywords)
	{
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
		setDirty(true);
		String[] keywordString = new String[keywords.size()];
		for (int i = 0; i < keywords.size(); i++)
		  keywordString[i] = keywords.get(i);
		workflow.setKeywords(keywordString);
	}
	
	/**
	 * Returns <code>true</code> if the keyword exist in the workflow,
	 * <code>false</code> otherwise.
	 * 
	 * @param value keyword to test for existence.
	 * @return See above.
	 */
	public boolean contains(String value)
	{
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
		String[] keywords = workflow.getKeywords();
  		if (keywords == null)
			return false;
		for (String keyword : keywords)
		{
			if (value.equals(keyword))
				return true;
		}
		return false;
	}
	
	/**
	 * Sets the namespace of the workflow. 
	 * 
 	 * @param namespace See above.
	 */
	public void setNamespace(String namespace)
	{
		Namespace workflow = (Namespace) asIObject();
		if (workflow == null) 
			throw new IllegalArgumentException("No workflow specified.");
		setDirty(true);
		workflow.setName(rtypes.rstring(namespace));
	}
	
}
