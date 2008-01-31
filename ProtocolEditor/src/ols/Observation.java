package ols;

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */


import tree.DataField;
import tree.DataFieldConstants;
import tree.IAttributeSaver;

public class Observation {
	
	String observationName;
	
	String dataType;
	
	String entityTermId;
	String attributeTermId;
	String unitTermId;

	public static final String[] OBSERVATION_TYPES = {"Text", "True/False", "Number"};
	
	
	
	public Observation(IAttributeSaver dataField) {
		
		observationName = dataField.getAttribute(DataFieldConstants.ELEMENT_NAME).trim();	// gets extra lines etc from formatting
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
