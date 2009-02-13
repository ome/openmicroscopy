 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.OntologyTermParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 */
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This class models the data for an Ontology Term parameter. 
 * Ontology terms are defined by an Ontology identifier (eg GO) and 
 * a term id (number). 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class OntologyTermParam 
extends AbstractParam 
{

	/**
	 * This defines a parameter that is an ontology term
	 */
	public static final String 		ONTOLOGY_TERM_PARAM = "ONTOLOGYTERM";
	
	/**
	 * This attribute stores an ontology ID. 
	 * The ontology identifier is the prefix (see http://www.obofoundry.org/) 
	 *
	 * Example: ontolgoyId = "PATO"
	 */
	public static final String ONTOLOGY_ID = "ontolgoyId";
	
	/**
	 * This attribute stores an ontology term ID. 
	 * This is a unique ID for a term in the ontology defined by 
	 * {@link #ONTOLOGY_ID}.
	 * 
	 * Example: termId = "0000461"
	 */
	public static final String TERM_ID = "termId";
	
	/**
	 * This attribute stores an ontology term name. 
	 * This is not strictly necessary, since it can be retrieved via the 
	 * Ontology Lookup Service from the {@link #ONTOLOGY_ID} and
	 * {@link #TERM_ID}. But it means that terms can be displayed 
	 * without having to make this call. 
	 * Also, it means that XML files containing this term can be 
	 * identified by keyword search. 
	 * 
	 * Example: termName = "normal"
	 */
	public static final String TERM_NAME = "termName";
	
	/**
	 * This (colon) is used to separate the ontology ID and the term ID when
	 * exporting the term as a text string. eg GO:0000266
	 */
	public static final String ONTOLOGY_SEPARATOR = ":";
	
	/**
	 * This (space) is used to separate the term ID and the term name when
	 * exporting the term as a text string. eg GO:0000266
	 */
	public static final String TERM_SEPARATOR = " ";
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public OntologyTermParam() 
	{
		super(ONTOLOGY_TERM_PARAM);
	}
	
	/**
	 * Returns the "ontology:term  name" String
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		
		String text = getParamValue();

		return super.toString() + " " + (text == null ? "" : text);
	}
	
	
	
	/**
	 * Implemented as specified by the {@link IParam} interface.
	 * This is used to output the value of this parameter as a text parameter.
	 * 
	 *  @see IParam#getParamValue()
	 */
	public String getParamValue() 
	{
		String id = getAttribute (ONTOLOGY_ID);
		String term = getAttribute(TERM_ID);
		String name = getAttribute(TERM_NAME);
		
		if (id == null && term == null && name == null) {
			return null;
		}
		
		return (id == null ? "" : id + ONTOLOGY_SEPARATOR) +
			(term == null ? "" : term + TERM_SEPARATOR) +
			(name == null ? "" : name);
	}
	
	/**
	 * Sets the ontology ID, term ID and term Name by parsing a string of 
	 * the format "GO:0000266 mitochondrial fission"
	 * 
	 * @param ontologyTerm
	 * @return	true if the given string fits the expected format.
	 */
	public boolean setOntologyTerm(String ontologyTerm)
	{
		int colonIndex = ontologyTerm.indexOf(ONTOLOGY_SEPARATOR);
		if (colonIndex < 2) return false; 
		
		// get the ontology ID, from before the colon
		String ontologyId = ontologyTerm.substring(0, colonIndex);
		
		//truncate the term, to leave the termID and termName. 
		ontologyTerm = ontologyTerm.substring(colonIndex+1);
		
		int spaceIndex = ontologyTerm.indexOf(TERM_SEPARATOR);
		if (spaceIndex < 1) return false;
		
		// get the term ID from before the space. 
		String termId = ontologyTerm.substring(0, spaceIndex);
		
		String termName = ontologyTerm.substring(spaceIndex+1, 
														ontologyTerm.length());
		
		setAttribute (ONTOLOGY_ID, ontologyId);
		setAttribute(TERM_ID, termId);
		setAttribute(TERM_NAME, termName);
		
		return true;
	}

	/**
	 * Implemented as specified by the {@link IParam} interface. 
	 * Parameter is filled if {@link #ONTOLOGY_ID} and {@link #TERM_ID} are
	 * not null. 
	 * 
	 * @see IParam#isParamFilled()
	 */
	public boolean isParamFilled() 
	{
		boolean filled = ((getAttribute(ONTOLOGY_ID) != null) &&
				(getAttribute(TERM_ID) != null));
		
		return filled;
	}

}