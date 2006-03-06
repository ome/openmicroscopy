/*
 * ome.dsl.Property
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.dsl;

//Java imports
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies


/** reprents the <b>definition</b> of a property within a SemanticType
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OMERO-3.0
 */
public abstract class Property { // TODO need to define equality so that two with the
						  // same name isn't allowed within one type./
	
	// FIELD identifiers 
	public final static String REQUIRED = "required";
	public final static String OPTIONAL = "optional";
	public final static String ONEMANY = "onemany";
	public final static String ZEROMANY = "zeromany";
	public final static String MANYONE = "manyone";
	public final static String MANYZERO = "manyzero";
	public final static String ENTRY = "entry";
    public final static String CHILD = "child";
    public final static String PARENT = "parent";
    public final static String TOCHILD = "to_child";
    public final static String FROMPARENT = "from_parent";

    public final static String LISTITEM = "listitem";
    
	public final static Set FIELDS = new HashSet();
	static {
		FIELDS.add(REQUIRED);
		FIELDS.add(OPTIONAL);
		FIELDS.add(ONEMANY);
		FIELDS.add(ZEROMANY);
		FIELDS.add(MANYONE);
		FIELDS.add(MANYZERO);
		FIELDS.add(ENTRY);
        FIELDS.add(CHILD);
        FIELDS.add(PARENT);
        FIELDS.add(FROMPARENT);
        FIELDS.add(TOCHILD);        
        FIELDS.add(LISTITEM);
	}
	public final static Map FIELDS2CLASSES = new HashMap();
	static {
		FIELDS2CLASSES.put(REQUIRED,RequiredField.class);
		FIELDS2CLASSES.put(OPTIONAL,OptionalField.class);
		FIELDS2CLASSES.put(ONEMANY,OneManyField.class);
		FIELDS2CLASSES.put(ZEROMANY,ZeroManyField.class);
		FIELDS2CLASSES.put(MANYONE,ManyOneField.class);
		FIELDS2CLASSES.put(MANYZERO,ManyZeroField.class);
		FIELDS2CLASSES.put(ENTRY,EntryField.class);
        FIELDS2CLASSES.put(PARENT,ParentLink.class);
        FIELDS2CLASSES.put(CHILD,ChildLink.class);
        FIELDS2CLASSES.put(FROMPARENT,LinkParent.class);
        FIELDS2CLASSES.put(TOCHILD,LinkChild.class);
        FIELDS2CLASSES.put(LISTITEM,ListItem.class);
	}
	
	// VALUE-Type identifiers
	public final static String STRING = "string";
	public final static String BOOLEAN = "boolean";
	public final static String INTEGER = "int";
	public final static String FLOAT = "float";
	public final static String DOUBLE = "double";
    public final static String LONG = "long";
	public final static String TIMESTAMP = "timestamp";
    public final static String TEXT = "text";
	public final static Map VALUES = new HashMap(); 
	static {
		VALUES.put(STRING,String.class.getName());
		VALUES.put(BOOLEAN,Boolean.class.getName());
		VALUES.put(INTEGER,Integer.class.getName());
		VALUES.put(FLOAT,Float.class.getName());
		VALUES.put(DOUBLE,Double.class.getName());
        VALUES.put(LONG,Long.class.getName());
		VALUES.put(TIMESTAMP,Timestamp.class.getName());
        VALUES.put(TEXT,TEXT);
	}
	
    private SemanticType st;
    
	// String based values.
	private String name;
	private String type;
	private String defaultValue;
    private String foreignKey;
    private String tag;
    private String inverse;
    private String target;
    
	// Specialties
	private Boolean required;
	private Boolean unique;
	private Boolean ordered;
    private Boolean insert;
    private Boolean update;
	
	// Mappings
	private Boolean one2Many;
	
	public void validate(){
		if (null==getName() || null==getType()){
			throw new IllegalStateException(
                    "All propeties must have a name and a type. ("+this+")");
		}
	}

	/** creates a new property based on the element-valued key in FIELDS2CLASSES. 
     * Used mainly by the xml reader */
	public static Property makeNew(String element, SemanticType st, Properties attributes) 
    throws IllegalArgumentException, IllegalStateException{
		Class klass = (Class) FIELDS2CLASSES.get(element);
		if (null==klass){
			throw new IllegalArgumentException(
                    "FIELDS2CLASSES does not contain type "+element);
		}
		
		Property p;
		
		try {
			p = (Property) klass.getConstructor(
                    new Class[]{SemanticType.class,Properties.class})
                    .newInstance(new Object[]{st, attributes});
		} catch (Exception e) {
			throw new IllegalStateException("Cannot instantiate class "+klass,e);
		}
		return p;
	}
	
	public String toString(){
		return "Property: "+getName()+" ("+getType()+")";
	}

	//
	// Getters and Setters
	//


    public void setST(SemanticType st) {
        this.st = st;
    }

    public SemanticType getST() {
        return st;
    }
    
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
    
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	public Boolean getUnique() {
		return unique;
	}

	public void setOrdered(Boolean ordered) {
		this.ordered = ordered;
	}
	
	public Boolean getOrdered() {
		return ordered;
	}

    public void setInverse(String inverse) {
        this.inverse = inverse;
    }
    
    public String getInverse() {
        return inverse;
    }

    public void setInsert(Boolean insert) {
        this.insert = insert;
    }
    
    public Boolean getInsert() {
        return insert;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }
    
    public Boolean getUpdate() {
        return update;
    }
   
	public void setForeignKey(String foreignKey) {
		this.foreignKey = foreignKey;
	}

	public String getForeignKey() {
		return foreignKey;
	}

	public void setOne2Many(Boolean one2Many) {
		this.one2Many = one2Many;
	}

	public Boolean getOne2Many() {
		return one2Many;
	}

    /** creates a Property and sets fields based on attributes USING DEFAULT VALUES. Subclassees may override these values */
    public Property(SemanticType st, Properties attrs){
        setST(st);
        setName(attrs.getProperty("name",null));
        setType(attrs.getProperty("type",null));
        setDefaultValue(attrs.getProperty("default",null));//TODO currently no way to use this!!
        setTag(attrs.getProperty("tag",null));
        setTarget(attrs.getProperty("target",null));
        setInverse(attrs.getProperty("inverse",null));
        setRequired(Boolean.valueOf(attrs.getProperty("required","false")));
        setUnique(Boolean.valueOf(attrs.getProperty("unique","false"))); // TODO wanted to use KEYS.put(id,field) !! 
        setOrdered(Boolean.valueOf(attrs.getProperty("ordered","false")));

        // TODO Mutability
        setInsert( Boolean.TRUE );
        setUpdate( Boolean.valueOf( attrs.getProperty( "mutable","true" ) ) );
        
        if (VALUES.containsKey(getType())){
            setForeignKey(null);
            setType((String) VALUES.get(getType()));
        } else {
            setForeignKey(SemanticType.typeToColumn(st.getId()));
        }
        
    }
    
}

// NOTE: For all the following be sure to check the defaults set on Property!
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ~ Simple
// ========
class OptionalField extends Property {
    public OptionalField(SemanticType st, Properties attrs){
        super(st,attrs);
    }
}

class RequiredField extends OptionalField {
	public RequiredField(SemanticType st, Properties attrs){
		super(st, attrs);
		setRequired(Boolean.TRUE);
	}
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ~ 1-Many
// ========
class ZeroManyField extends Property {
    public ZeroManyField(SemanticType st, Properties attrs){
        super(st, attrs);
        setRequired(Boolean.FALSE);
        setOne2Many(Boolean.TRUE);
        
        /* see ch. 7 hibernate doc on association mappings */
        if (getOrdered().booleanValue()) 
        {
            setRequired(Boolean.TRUE); // FIXME here we need to change the many2one!!
        } 
    }
    
    public void validate()
    {
        if ( getInverse() == null 
                && ! getOrdered().booleanValue() && getTag() == null )
            throw new IllegalArgumentException("\n"+
                    this.toString()+": invalid "+this.getClass().getName()+" property.\n"+
                    "\n All zeromany and onemany fields must provide either the \"inverse\" " +
                    "\n \"ordered\" or \"tag\" attribute E.g.\n" +
                    "\n" +
                    "<type id=...>\n" +
                    "\t<properties>\n" +
                    "\t\t<onemany name=\"example\" type=\"Example\" inverse=\"parent\">"
                    );
    }
}

class OneManyField extends ZeroManyField {
	public OneManyField(SemanticType st, Properties attrs){
		super(st, attrs);
		setRequired(Boolean.TRUE);
	}
}

abstract class AbstractLink extends ZeroManyField {
    public AbstractLink(SemanticType st, Properties attrs){
        super(st, attrs);
        setTarget( attrs.getProperty("target",null) );

    }
    
    public void validate()
    {
        if (getTarget() == null){
            throw new IllegalArgumentException(
                    "Target must be set on all parent/child properties:"+this);
        }

    }
}

/** property from a child iobject to a link */
class ChildLink extends AbstractLink {
    public ChildLink(SemanticType st, Properties attrs){
        super(st, attrs);
        setForeignKey("parent");
        setInverse("child");
    }
}

/** property from a parent iobject to a link */
class ParentLink extends AbstractLink {
    public ParentLink(SemanticType st, Properties attrs){
        super(st, attrs);
        setForeignKey("child");
        setInverse("parent");
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~ Many-1
//========

class ManyZeroField extends Property {
    public ManyZeroField(SemanticType st, Properties attrs){
        super(st, attrs);
    }
}

class ManyOneField extends ManyZeroField {
	public ManyOneField(SemanticType st, Properties attrs){
		super(st, attrs);
		setRequired(Boolean.TRUE);
	}
}

/** property from a link to a parent iobject */
class LinkParent extends ManyOneField {
    public LinkParent(SemanticType st, Properties attrs){
        super(st, attrs);
        setName("parent");
    }
}

/** property from a link to a child iobject */
class LinkChild extends ManyOneField {
    public LinkChild(SemanticType st, Properties attrs){
        super(st, attrs);
        setName("child");
    }
}

class ListItem extends ManyOneField {
    public ListItem( SemanticType st, Properties attrs ) {
        super( st, attrs );
        setInsert( Boolean.FALSE );
        setUpdate( Boolean.FALSE );
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ~ DIFFERENT SEMANTICS!!!
// ========================
class EntryField extends Property {
	public EntryField(SemanticType st, Properties attrs){
		super(st, attrs);
		setType("string");
        setForeignKey(null);
	}

	public void validate(){
		if ( ! "string".equals( getType() ) )
        {
			throw new IllegalStateException("Enum entries can only be of type \"string\"");
		}
		super.validate();
	}
}