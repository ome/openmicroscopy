package tree;

public class DataFieldConstants {

	// attribute types
	// changes to the attributes are reflected in XML element saving.
	// any significant changes should be given a new XML version number (in XMLModel)
	
	public static final String ELEMENT_NAME = "elementName";
	public static final String DESCRIPTION = "description";
	public static final String VALUE = "value";
	public static final String PROTOCOL_FILE_NAME = "protocolFileName";
	public static final String TEXT_NODE_VALUE ="textNodeValue";
	public static final String DEFAULT = "default";
	public static final String INPUT_TYPE = "inputType";
	public static final String DROPDOWN_OPTIONS = "dropdownOptions";
	public static final String TABLE_COLUMN_NAMES = "tableColumnNames";
	public final static String TABLE_ROW_COUNT = "tableRowCount";
	public final static String ROW_DATA_NUMBER = "rowNumber";	// concatenate this with row integer
	public static final String UNITS = "units";
	public static final String KEYWORDS = "keywords";
	public static final String SUBSTEPS_COLLAPSED ="substepsCollapsed"; // "true" or "false"
	public static final String BACKGROUND_COLOUR = "backgroundColour";
	public static final String URL = "url";
	
	public static final String ONTOLOGY_ID = "ontologyId";
	public static final String ONTOLOGY_TERM_ID = "ontolgoyTermId";
	public static final String ONTOLOGY_TERM_NAME = "ontologyTermName";
	public static final String ONTOLOGY_TERM_DEF = "ontologyTermDef";
	
	public static final String OBSERVATION_ENTITY_TERM_IDNAME = "observationEntityTermID";
	public static final String OBSERVATION_ATTRIBUTE_TERM_IDNAME = "observationAttributeTermID";
	public static final String OBSERVATION_TYPE = "observationType";
	public static final String OBSERVATION_UNITS_TERM_ID = "observationUnitsTermID";
	
	// attribute values
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	// input types
	public static final String PROTOCOL_TITLE = "ProtocolTitle";
	public static final String FIXED_PROTOCOL_STEP = "FixedStep";
	public static final String TEXT_ENTRY_STEP = "TextField";
	public static final String MEMO_ENTRY_STEP = "TextBox";
	public static final String NUMBER_ENTRY_STEP = "NumberField";
	public static final String DROPDOWN_MENU_STEP = "DropDownMenu";
	public static final String CHECKBOX_STEP = "CheckBoxField";
	public static final String DATE = "DateField";
	public static final String TIME_FIELD = "TimeField";
	public static final String TABLE = "TableField";
	public static final String CUSTOM = "CustomField";
	public static final String OLS_FIELD = "OntologyLookupServiceField";
	public static final String OBSERVATION_DEFINITION = "ObservationDefinition";
	public static final String[] INPUT_TYPES = 
	{FIXED_PROTOCOL_STEP, TEXT_ENTRY_STEP,
	MEMO_ENTRY_STEP, DROPDOWN_MENU_STEP, CHECKBOX_STEP, NUMBER_ENTRY_STEP, DATE, TIME_FIELD, TABLE, 
	OLS_FIELD, OBSERVATION_DEFINITION
	};
	//	 the names used for the UI - MUST be in SAME ORDER as INPUT_TYPES they correspond to 
	// this means you can change the UI names without changing INPUT_TYPES.
	public static final String[] UI_INPUT_TYPES = 	
	{ "Fixed", "Text  (single line)", "Text Box  (multi-line)", "Drop-down Menu", "Check-Box", "Number", "Date", "Time", "Table", 
		"Ontology Term", "Phenote Observation"
		};

}
