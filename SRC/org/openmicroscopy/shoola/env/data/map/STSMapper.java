/*
 * org.openmicroscopy.shoola.env.data.map.STSMapper
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.map;

//Java imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class STSMapper
{
	
	public static final String GLOBAL_GRANULARITY = "G";
	public static final String DATASET_GRANULARITY = "D";
	public static final String IMAGE_GRANULARITY = "I";
	public static final String FEATURE_GRANULARITY = "F";
	
   	private static final HashMap granularities;
   	
   	static {
		granularities = new HashMap();
		granularities.put(GLOBAL_GRANULARITY, "id");
		granularities.put(DATASET_GRANULARITY, "dataset_id");
		granularities.put(IMAGE_GRANULARITY, "image_id");
		granularities.put(FEATURE_GRANULARITY, "feature_id");
   	}
   	
   	/** 
   	 * Return a criteria containing the information
   	 * required to call createAttribute(typeName, objectID).
   	 * 
   	 * @param granularity	The granularity of the attribute.
     * @param targetID		The ID of the target.
   	 */
   	public static Criteria buildCreateNew(String granularity, int objectID) 
   	{
   		Criteria c = new Criteria();
   		if (granularity.equals(DATASET_GRANULARITY) || 
   			granularity.equals(FEATURE_GRANULARITY))
			c.addWantedField("id");
   		else if (granularity.equals(IMAGE_GRANULARITY))	
			c.addWantedField(":all:");
		c.addFilter("id", new Integer(objectID));
   		return c;
   	}
   	
   	/**
   	 * Returns a Criteria object which contains the amount of information
     * required to call <code>count()</code>: that is, the granularity of
     * the attribute desired and the target ID.
     * 
     * @param granularity	The granularity of the attribute to count.
     * @param targetID		The ID of the target to count.
     * @return A criteria conforming to the above parameters.
   	 */
	public static Criteria buildCountCriteria(String granularity, int targetID)	
	{
		Criteria c = new Criteria();
	   	// TODO fix to actually count
	   	String column = (String) granularities.get(granularity);
	   	if (column != null) c.addFilter(column, new Integer(targetID));
	   	return c;
	}
	
	/** 
	 *
	 * @param granularity
	 * @param targetIDs
	 * @return
	 */
	public static Criteria buildDefaultCountCriteria(String granularity, 
											Number[] targetIDs)
	{
		if (targetIDs == null || targetIDs.length == 0)	return null;
		Criteria c = new Criteria();
		String column = (String) granularities.get(granularity);
		if (column != null) c.addFilter(column, "IN", Arrays.asList(targetIDs));
		return c;
	}
	
	/**
     * Returns a Criteria object which contains the default amount of
	 * information to be returned in an attribute-- all the primitive fields,
     * and all references with just the ID object returned (such that
     * successive calls may be made to the server to retrieve those attributes
     * as well)
     *
     * @param granularity	The type of target to specify.
     * @param targetID		The ID of the target to filter by.
     * @return A Criteria object with the default depth with the specified
     *         parameters.
     */
	public static Criteria buildDefaultRetrieveCriteria(String granularity,
												 int targetID)
	{
		Criteria c = new Criteria();
    
	   	// all non-references; has-ones with just ID's; no has-manys
	   	c.addWantedField(":all:");
    
	   	c.addWantedField("semantic_type");
        c.addWantedField("semantic_type","name");
        c.addWantedField("semantic_type","granularity");
	   	c.addWantedField("semantic_type", "semantic_elements");
	   	c.addWantedField("semantic_type.semantic_elements", "id");
	   	c.addWantedField("semantic_type.semantic_elements", "name");
	   	c.addWantedField("semantic_type.semantic_elements", "data_column");
	   	c.addWantedField("semantic_type.semantic_elements.data_column", "id");
	   	c.addWantedField("semantic_type.semantic_elements.data_column",
						"sql_type");
	   	c.addWantedField("semantic_type.semantic_elements.data_column",
						"reference_semantic_type");
    
		String column = (String) granularities.get(granularity);
		if (column != null) c.addFilter(column, new Integer(targetID));
	   	return c;
	}

	/**
	 * Retrieves and fills in children.
	 * @param granularity	The granularity of the ST to query.
	 * @param childString	The tree of attributes to retrieve (OTF.instrument).
	 * @param targetIDs		The IDs to target.
	 * @return The desired criteria.
	 */
	public static Criteria buildDefaultRetrieveCriteria(String granularity,
												 String childString,
												 Number[] targetIDs)
	{
		if (targetIDs == null || targetIDs.length == 0)	return null;
		Criteria c = new Criteria();
		boolean found = false;
	    if (childString.indexOf(".") == -1)
		   return buildDefaultRetrieveCriteria(granularity, targetIDs);
    
		childString = childString.substring(childString.indexOf(".")+1);
    
		c = buildDefaultRetrieveCriteria(granularity, targetIDs);
		int nextIndex = 0;
		while (!found) {
			nextIndex = childString.indexOf(".", nextIndex);
			if (nextIndex == -1) {
			   c.addWantedField(childString, ":all:");
			   found = true;
			} else {
			   String substr = childString.substring(0, nextIndex);
			   c.addWantedField(substr, ":all:");
			}
		   nextIndex++;
		}
		return c;
	}
    
	/**
	 * Returns a Criteria object which contains the default amount of
	 * information to be returned in an attribute-- all the primitive fields,
	 * and all references with just the ID object returned (such that
	 * successive calls may be made to the server to retrieve those attributes
	 * as well)
	 *
	 * @param granularity	The type of target to specify.
	 * @param targetID		The ID of the target to filter by.
	 * @return A Criteria object with the default depth with the specified
	 *         parameters.
	 */
	public static Criteria buildDefaultRetrieveCriteria(String granularity,
												 Number[] targetIDs)
	{
		if (targetIDs == null || targetIDs.length == 0) return null;
		Criteria c = new Criteria();
    
		// all non-references; has-ones with just ID's; no has-manys
   		c.addWantedField(":all:");
        c.addWantedField("semantic_type",":all:");
		String column = (String) granularities.get(granularity);
		if (column != null) c.addFilter(column, "IN", Arrays.asList(targetIDs));
		return c;
	}

	/**
	 * 
	 * @param typeName
	 * @return An appropriate Criteria object.
	 */
	public static Criteria buildRetrieveSingleTypeCriteria(String typeName)
	{
		if (typeName == null)	return new Criteria();
	   	Criteria c = buildBasicTypeCriteria();
	   	c.addFilter("name", typeName);
	   	return c;
	}
    
    /**
     * 
     * @param granularity	The type of target to specify.
     * @return An appropriate Criteria object.
     */
	public static Criteria buildRetrieveTypeCriteria(String granularity)
	{
		if (granularity == null)	return new Criteria();
	   	Criteria c = buildBasicTypeCriteria();
	  	c.addFilter("granularity", granularity);
	   	return c;
	}
    
	/**
	 * Returns a Criteria object which will extract the appropriate amount
	 * of information to get SemanticTypes.  Passing null will form the
	 * Criteria to get all SemanticTypes.
	 * 
	 * @return An appropriate Criteria object.
	 */
	private static Criteria buildBasicTypeCriteria()
	{
		Criteria c = new Criteria();

   		c.addWantedField("id");
   		c.addWantedField("name");
        c.addWantedField("granularity");
   		c.addWantedField("description");

   		c.addWantedField("semantic_elements");
   		c.addWantedField("semantic_elements", "id");
   		c.addWantedField("semantic_elements", "name");
   		c.addWantedField("semantic_elements", "description");
   		c.addWantedField("semantic_elements", "data_column");
   		c.addWantedField("semantic_elements", "semantic_type");

   		c.addWantedField("semantic_elements.data_column", "id");
   		c.addWantedField("semantic_elements.data_column", "column_name");
   		c.addWantedField("semantic_elements.data_column", "sql_type");
   		c.addWantedField("semantic_elements.data_column", 
						"reference_semantic_type");
   		c.addWantedField("semantic_elements.data_column", "data_table");

   		c.addWantedField("semantic_elements.data_column.data_table", "id");
   		c.addWantedField("semantic_elements.data_column.data_table",
						"table_name");

   		c.addWantedField("semantic_elements.data_column." +
						   "reference_semantic_type", "id");
   		c.addWantedField("semantic_elements.data_column." +
						   "reference_semantic_type", "name");

   		return c;
	}
	
	
	public static Criteria buildRetrieveCriteriaWithMEXs(List mexes)
	{
	    if (mexes == null || mexes.size() == 0) return null;
	    Criteria c = new Criteria();

	    // all non-references; has-ones with just ID's; no has-manys
	    c.addWantedField(":all:");

	    c.addFilter("module_execution_id", "IN", mexes);
	    return c;
	}
   	
	public static Criteria buildTrajectoryCriteriaWithMEXs(List mexes)
	{
	    if (mexes == null || mexes.size() == 0) return null;
	    Criteria c = new Criteria();

	    // all non-references; has-ones with just ID's; no has-manys
	    c.addWantedField("id");
	    c.addWantedField("Name");
	    c.addWantedField("TotalDistance");
	    c.addWantedField("AverageVelocity");
	    c.addWantedField("TrajectoryEntries");
	    
	    c.addWantedField("TrajectoryEntries","Order");
	    c.addWantedField("TrajectoryEntries","DeltaX");
	    c.addWantedField("TrajectoryEntries","DeltaY");
	    c.addWantedField("TrajectoryEntries","DeltaZ");
	    c.addWantedField("TrajectoryEntries","Distance");
	    c.addWantedField("TrajectoryEntries","Velocity");
	    
	    // how to get features?
		   
		c.addFilter("module_execution_id", "IN", mexes);
	    return c;
	}
	
	public static Criteria buildTrajectoryEntryCriteriaWithMEXs(List mexes)
	{
	    if (mexes == null || mexes.size() == 0) return null;
	    Criteria c = new Criteria();

	    // all non-references; has-ones with just ID's; no has-manys
	    c.addWantedField("id");
	    
	    c.addWantedField("Order");
	    c.addWantedField("DeltaX");
	    c.addWantedField("DeltaY");
	    c.addWantedField("DeltaZ");
	    c.addWantedField("Distance");
	    c.addWantedField("Velocity");
	    c.addWantedField("Trajectory");
	    c.addWantedField("feature");
	    
	    c.addWantedField("Trajectory","id");
	    c.addWantedField("Trajectory","Name");
	    c.addWantedField("Trajectory","TotalDistance");
	    c.addWantedField("Trajectory","AverageVelocity");
	    c.addWantedField("feature","id");
	    c.addWantedField("feature","image");
	    c.addWantedField("feature.image","id");
	    // how to get features?
		   
		c.addFilter("module_execution_id", "IN", mexes);
	    return c;
	}
}
