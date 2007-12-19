package ols;

import tree.DataField;

public class Observation {
	
	String observationName;
	
	String dataType;
	
	String entityTermId;
	String attributeTermId;
	String unitTermId;

	public static final String[] OBSERVATION_TYPES = {"Text", "True/False", "Number"};
	
	
	
	public Observation(DataField dataField) {
		
		observationName = dataField.getName().trim();	// gets extra lines etc from formatting
		dataType = dataField.getAttribute(DataField.OBSERVATION_TYPE);
		entityTermId = dataField.getAttribute(DataField.OBSERVATION_ENTITY_TERM_ID);
		attributeTermId = dataField.getAttribute(DataField.OBSERVATION_ATTRIBUTE_TERM_ID);
		unitTermId = dataField.getAttribute(DataField.OBSERVATION_UNITS_TERM_ID);
		
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
