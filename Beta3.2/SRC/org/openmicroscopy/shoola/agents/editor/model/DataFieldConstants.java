/*
 * org.openmicroscopy.shoola.agents.editor.model.DataFieldConstants
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.agents.editor.model;

// Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * This class contains static String attributes that are used to read
 * XML for Beta-3.0 and earlier versions of Editor XML files.
 * 
 * After Beta-3.0, these attributes are handled by the {@link Field} and
 * {@link AbstractParam} subclasses that model the data.
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DataFieldConstants {

	/**
	 * Attribute names.
	 */
	
	/**
	 * This is the name of the element that the user can edit.
	 * It is the user's principle way of identifying a field. 
	 */
	public static final String ELEMENT_NAME = "elementName";
	
	/**
	 * A description is some additional text that a user can add to any field.
	 */
	public static final String DESCRIPTION = "description";
	
	/**
	 * value is the default attribute for storing a variable for each field. 
	 * Initially all fields used this attribute. Eg. NumberField & TextField. 
	 * But some fields now use other attributes to store the "value" or values. 
	 * Eg DateTimeField uses UTCmillisecs to store a date. 
	 * 
	 * Each field that has a "default" attribute should also specify whether the 
	 * value of the default should be copied into "value" attribute or elsewhere.
	 * This is done by overriding the method FormField.getValueAttribute() which
	 * returns VALUE by default. 
	 */
	public static final String VALUE = "value";
	
	/**
	 * This attribute stores the name of the XML file that a protocol is saved in. 
	 * It is set as an attribute in the root field of a file
	 * when that file is saved. Then will persist in this field...
	 * Even after that protocol-title field is imported into another document. It will
	 * retain the name of the file that it came from. 
	 */
	public static final String PROTOCOL_FILE_NAME = "protocolFileName";
	
	/**
	 * This attribute is used to store the text that is found between the start and 
	 * end tags of imported XML documents. 
	 * Omero.Editor does not store any text as text nodes in XML, but when 
	 * XML is imported or exported, the value of that text is stored in this attribute
	 */
	public static final String TEXT_NODE_VALUE ="textNodeValue";
	
	/**
	 * This attribute is used to store a default value for a field. Could be a number
	 * or text. 
	 * The EditCopyDefaultValues edit command uses the value of the "default" attribute
	 * to copy into the attribute defined by FormField.getValueAttribute() for each field.
	 */
	public static final String DEFAULT = "default";
	
	/**
	 * Input type attribute is the type of field. eg "FixedStep" or "TextField". It is stored 
	 * under this attribute name in dataField, but when exported to XML, this becomes the 
	 * element name eg <FixedStep otherAttributes="here">
	 */
	public static final String INPUT_TYPE = "inputType";
	
	/**
	 * This attribute is used to store a comma-delimited list of drop-down options for 
	 * the DropDownMenu field. 
	 */
	public static final String DROPDOWN_OPTIONS = "dropdownOptions";
	
	/**
	 * This attribute stores a comma-delimited list of column names for the TableField. 
	 */
	public static final String TABLE_COLUMN_NAMES = "tableColumnNames";
	
	/**
	 * This attribute stores an integer of the number of rows in the TableField.
	 * This allows a table to created with the right number of rows, without testing
	 * to see if eg rowNumber4 is null etc. 
	 */
	public final static String TABLE_ROW_COUNT = "tableRowCount";
	
	/**
	 * This is the stem of an attribute name, used by TableField.
	 * It must be concatenated with an integer to become the attribute that stores row data.
	 * eg rowNumber0 attribute stores a comma delimited list of cell data for the first row
	 * of the table. 
	 */
	public final static String ROW_DATA_NUMBER = "rowNumber";	// concatenate this with row integer
	
	/**
	 * This attribute stores the units of a NumberField. eg "grams" or "%"
	 */
	public static final String UNITS = "units";
	
	/**
	 * This attribute is used to store a date-time as the time from the epoch 
	 * (Jan 01, 1970)
	 * in milliseconds. 
	 * @See java.util.Calendar.getTimeInMillis();
	 * Used by DateTimeField and is converted back into a Date-Time by the 
	 * calendar for 
	 * populating the omeroCal database.
	 */
	public static final String UTC_MILLISECS = "UTCMillisecs";
	
	/**
	 * The seconds attribute is used for storing a time duration for the TimeField.
	 * This was previously (before 8thMarch08_ stored in the "value" attribute 
	 * in the form "hh:mm:ss"
	 * The FormFieldTime class will check the "seconds" attribute first, and if
	 *  this is null, 
	 * it will check the "value" attribute and convert to seconds. 
	 * All data is now saved in the "seconds" attribute. 
	 * 
	 * The DateTime field also uses this to store a time (so it can be updated independently
	 * of the date). eg 14:00 = 50400 seconds.
	 */
	public static final String SECONDS = "seconds";	
	
	/**
	 * This attribute is used to store the difference in time between a DateTimeField and 
	 * the time that an alarm will go off for this event. 
	 * ie: stores a -ve value for "before", +ve is alarm after in seconds.
	 */
	public static final String ALARM_SECONDS = "alarmSeconds";	
	
	/**
	 * Used by the ImageField to store a file path to an image. 
	 * This is the absolute local file path.
	 */
	public static final String ABSOLUTE_IMAGE_PATH = "imagePath";
	
	/**
	 * Used by the ImageField to store a file path to an image. 
	 * This is a relative file path, FROM the editor file in which this field appears TO the image 
	 */
	public static final String RELATIVE_IMAGE_PATH = "relativeImagePath";
	
	/**
	 * This attribute stores an integer that is the image zoom (percentage) for the
	 * ImageField. eg imageZoom = "50" would display the image at 50% full size. 
	 */
	public static final String IMAGE_ZOOM = "imageZoom";
	
	/**
	 * Used by the LinkField to store an absolute file path to a local File. 
	 * eg Word.doc, PDF etc or Editor file. 
	 * This attribute is mutually exclusive with RELATIVE_FILE_PATH and URL_LINK
	 */
	public static final String ABSOLUTE_FILE_LINK = "absoluteFileLink";
	
	/**
	 * Used by the LinkField to store a file path to an local file, eg Word.doc, PDF etc or Editor file. 
	 * This is a relative file path, FROM the editor file in which this field appears TO the image.
	 * This attribute is mutually exclusive with ABSOLUTE_FILE_PATH and URL_LINK
	 */
	public static final String RELATIVE_FILE_LINK = "relativeFileLink";
	
	/**
	 * Used by the LinkField to store a URL. 
	 * This URL is part of the "experimental variables" and is specific to the LinkField.
	 * It is different from the URL "url" attribute, that exists as part of the template of all fields. 
	 * This attribute is mutually exclusive with ABSOLUTE_FILE_PATH and RELATIVE_FILE_PATH.
	 */
	public static final String URL_LINK = "urlLink";
	
	/**
	 * This attribute, with value = 'true' indicates that this field should be filled in, 
	 * in order for the document to be complete or valid. Ie, formField.isFieldFilled() 
	 * should be true when the Editor file is saved. 
	 * This allows users to specify a number of "Mandatory Fields" required to form a
	 * complete set of data. 
	 */
	public static final String REQUIRED_FIELD = "requiredField";
	
	/**
	 * This is a field associated with the protocolTitle field, where users can add
	 * words that summarize that protocol. 
	 * However, this is very rarely used, and is not utilized by any search methods etc. 
	 */
	public static final String KEYWORDS = "keywords";
	
	/**
	 * This attribute stores a boolean ("true" or "false") to define whether a 
	 * field is in the collapsed state (ie children are not shown on the UI). 
	 * This field may be null, which is equivalent to "false".
	 * Changes to this attribute are not considered "edits". Ie no undo/redo 
	 * and the file does not become "edited".
	 */
	public static final String SUBSTEPS_COLLAPSED ="substepsCollapsed";
	
	/**
	 * This is a display property, which causes the children of the field in question to 
	 * be displayed as adjacent columns, rather than as rows. 
	 * The sub-tree beneath each child will be displayed vertically (in rows) as normal. 
	 */
	public static final String DISPLAY_CHILDREN_HORIZONTALLY = "displayChildrenHorizontally";
	
	/**
	 * Stores a color as a string in the form "r:g:b";
	 */
	public static final String BACKGROUND_COLOUR = "backgroundColour";
	
	/**
	 * Allows the user to store a URL for a field. 
	 * If not a valid URL (if new URL(url) throws a MalformedURLException) then 
	 * http:// is added to the start of the url string. 
	 */
	public static final String URL = "url";
	
	/**
	 * This attribute indicates that a field has been "Locked" and it defines a 
	 * time stamp of when it was locked, in UTC milliseconds. 
	 * If the field is unlocked, this attribute will be null. 
	 */
	public static final String LOCKED_FIELD_UTC = "fieldLockedUTC";
	
	/**
	 * The name of the user locking the field. 
	 */
	public static final String LOCKED_FIELD_USER_NAME = "fieldLockedUserName";

	/**
	 * An attribute to describe the level of locking. If null, assume unlocked. 
	 * @see TEMPLATE_LOCKED
	 */
	public static final String LOCK_LEVEL = "lockLevel";

	/**
	 * This value in the LOCK_LEVEL attribute indicates that only the template is 
	 * locked and users can still edit the "experimental variables"
	 */
	public static final String LOCKED_TEMPLATE = "templateLocked";

	/**
	 * This value in the LOCK_LEVEL attribute indicates that NO editing is allowed.
	 * NB. Some attributes, such as collapsed state, may still change. 
	 */
	public static final String LOCKED_ALL_ATTRIBUTES = "allAttributesLocked";
	
	/**
	 * This attribute stores an ontology ID and a term ID. 
	 * The ontology identifier is the prefix (see http://www.obofoundry.org/) and the term
	 * number is a unique ID for a term in that ontology. 
	 * In addition, this attribute also stores the name of the term. This is not strictly
	 * necessary, since it can be retrieved via the Ontology Lookup Service. But it means
	 * that terms can be displayed without having to make this call. 
	 * Also, it means that XML files containing this term can be identified by keyword search. 
	 * 
	 * Example: ontolgoyTermId = "PATO:0000461   normal"
	 */
	public static final String ONTOLOGY_TERM_ID = "ontolgoyTermId";	
	
	public static final String OBSERVATION_ENTITY_TERM_IDNAME = "observationEntityTermID";
	public static final String OBSERVATION_ATTRIBUTE_TERM_IDNAME = "observationAttributeTermID";
	public static final String OBSERVATION_TYPE = "observationType";
	public static final String OBSERVATION_UNITS_TERM_ID = "observationUnitsTermID";
	
	// attribute values
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	
	/*
	 * FIELD TYPES
	 * These strings are used as values stored in the "inputType" attribute of each field. 
	 * When exported to XML, they form the name of each element, eg <TextField> 
	 */
	/**
	 * This type of field is used as the root of Editor files. 
	 * Therefore, each XML document will have the root element as ProtocolTitle 
	 */
	public static final String PROTOCOL_TITLE = "ProtocolTitle";
	
	/**
	 * This field has no variables. Therefore, used to store Name, Description, URL only. 
	 */
	public static final String FIXED_PROTOCOL_STEP = "FixedStep";
	
	/**
	 * A TextField stores a short text string. A default text string can also be stored. 
	 */
	public static final String TEXT_ENTRY_STEP = "TextField";
	
	/**
	 * A TextBox field stores a paragraph or longer piece of text. 
	 */
	public static final String MEMO_ENTRY_STEP = "TextBox";
	
	/**
	 * A Number Field stores a floating point number, and a default number can also be set. 
	 */
	public static final String NUMBER_ENTRY_STEP = "NumberField";
	
	/**
	 * A dropDown menu field allows users to store several options as a comma-delimited list.
	 * They can then choose one of these as a "value" and one as a "default" value. 
	 */
	public static final String DROPDOWN_MENU_STEP = "DropDownMenu";
	
	/**
	 * 
	 */
	public static final String CHECKBOX_STEP = "CheckBoxField";
	public static final String DATE = "DateField";		// This is deprecated -7th March 08.  Now use DateTimeField
	public static final String DATE_TIME_FIELD = "DateTimeField";
	public static final String TIME_FIELD = "TimeField";
	public static final String TABLE = "TableField";
	public static final String LINK_FIELD = "LinkField";
	public static final String IMAGE_FIELD = "ImageField";
	public static final String CUSTOM = "CustomField";
	public static final String OLS_FIELD = "OntologyLookupServiceField";
	public static final String OBSERVATION_DEFINITION = "ObservationDefinition";
	
	public static final String[] INPUT_TYPES = 
	{FIXED_PROTOCOL_STEP, TEXT_ENTRY_STEP,
	MEMO_ENTRY_STEP, DROPDOWN_MENU_STEP, CHECKBOX_STEP, NUMBER_ENTRY_STEP, DATE_TIME_FIELD, TIME_FIELD, LINK_FIELD, TABLE, 
	IMAGE_FIELD, OLS_FIELD, OBSERVATION_DEFINITION
	};
	//	 the names used for the UI - MUST be in SAME ORDER as INPUT_TYPES they correspond to 
	// this means you can change the UI names without changing INPUT_TYPES.
	public static final String[] UI_INPUT_TYPES = 	
	{ "Fixed", "Text  (single line)", "Text Box  (multi-line)", "Drop-down Menu", "Check-Box", "Number", "Date & Time", "Time", "Link", "Table", 
		"Image", "Ontology Term", "Phenote Observation"
		};

	/**
	 * When importing XML, need to distinguish between OMERO.editor elements and "Custom" XML elements
	 * generated elsewhere. 
	 * This method checks all the OMERO.editor input types, and returns true if found.
	 * 
	 * @param inputType		The String that defines the <inputType> of an imported element.
	 * @return		true if inputType equals a recognised OMERO.editor input type. 
	 */
	public static boolean isInputTypeRecognised(String inputType) {
		
		/*
		 * Check each of INPUT_TYPES, and check PROTOCOL_TITLE
		 */
		for (int i=0; i<INPUT_TYPES.length; i++) {
			if (inputType.equals(INPUT_TYPES[i]))
				return true;
		}
		if (inputType.equals(PROTOCOL_TITLE))
			return true;
		
		// Also, the deprecated Date Field.
		if (inputType.equals(DATE))
			return true;
		
		return false;
	}
	
	
	// old input types
	// need to be able to read elements that use attribute: inputType="Fixed Step" 
	// but should be saved as elements named  <FixedStep elementName="etc">  see input types below
	public static final String OLD_PROTOCOL_TITLE = "Protocol Title";
	public static final String OLD_FIXED_PROTOCOL_STEP = "Fixed Step";
	public static final String OLD_TEXT_ENTRY_STEP = "Text";
	public static final String OLD_MEMO_ENTRY_STEP = "Text Box";
	public static final String OLD_NUMBER_ENTRY_STEP = "Number";
	public static final String OLD_DROPDOWN_MENU_STEP = "Drop-down Menu";
	public static final String OLD_DATE = "Date";
	public static final String OLD_TABLE = "Table";
	public static final String OLD_CUSTOM = "Custom";
	
	
	// a method used by the Tree class to convert from old xml version to new
	public static String getNewInputTypeFromOldInputType(String oldInputType) {
		if (oldInputType.equals(OLD_PROTOCOL_TITLE)) return PROTOCOL_TITLE;
		if (oldInputType.equals(OLD_FIXED_PROTOCOL_STEP)) return FIXED_PROTOCOL_STEP;
		if (oldInputType.equals(OLD_TEXT_ENTRY_STEP)) return TEXT_ENTRY_STEP;
		if (oldInputType.equals(OLD_MEMO_ENTRY_STEP)) return MEMO_ENTRY_STEP;
		if (oldInputType.equals(OLD_NUMBER_ENTRY_STEP)) return NUMBER_ENTRY_STEP;
		if (oldInputType.equals(OLD_DROPDOWN_MENU_STEP)) return DROPDOWN_MENU_STEP;
		if (oldInputType.equals(OLD_DATE)) return DATE;
		if (oldInputType.equals(OLD_TABLE)) return TABLE;
		if (oldInputType.equals(OLD_CUSTOM)) return OLD_CUSTOM;
		
		return oldInputType;
	}
}
