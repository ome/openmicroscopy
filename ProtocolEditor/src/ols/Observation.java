package ols;

import tree.DataField;
import tree.DataFieldConstants;

public class Observation {
	
	String observationName;
	
	String dataType;
	
	String entityTermId;
	String attributeTermId;
	String unitTermId;

	public static final String[] OBSERVATION_TYPES = {"Text", "True/False", "Number"};
	
	
	
	public Observation(DataField dataField) {
		
		observationName = dataField.getName().trim();	// gets extra lines etc from formatting
		dataType = dataField.getAttribute(DataFieldConstants.OBSERVATION_TYPE);
		entityTermId = dataField.getAttribute(DataFieldConstants.OBSERVATION_ENTITY_TERM_IDNAME);
		attributeTermId = dataField.getAttribute(DataFieldConstants.OBSERVATION_ATTRIBUTE_TERM_IDNAME);
		unitTermId = dataField.getAttribute(DataFieldConstants.OBSERVATION_UNITS_TERM_ID);
		
	}
	
	public String getObservationName() {
		return observationName;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public String getEntityTermId() {
		return entityTermId;
	}
	
	public String getAttributeTermId() {
		return attributeTermId;
	}
	
	public String getUnitsTermId() {
		return unitTermId;
	}
}
