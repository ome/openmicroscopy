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
	}
	
	// VALUE-Type identifiers
	public final static String STRING = "string";
	public final static String BOOLEAN = "boolean";
	public final static String INTEGER = "int";
	public final static String FLOAT = "float";
	public final static String DOUBLE = "double";
    public final static String LONG = "long";
	public final static String TIMESTAMP = "timestamp";
	public final static Map VALUES = new HashMap(); 
	static {
		VALUES.put(STRING,String.class);
		VALUES.put(BOOLEAN,Boolean.class);
		VALUES.put(INTEGER,Integer.class);
		VALUES.put(FLOAT,Float.class);
		VALUES.put(DOUBLE,Double.class);
        VALUES.put(LONG,Long.class);
		VALUES.put(TIMESTAMP,Timestamp.class);
	}
	
	// String based values.
	private String name;
	private String type;
	private String defaultValue;
    private String extracode;
	
	// Specialties
	private Boolean required;
	private Boolean unique;
	private Boolean mutable;
	private Boolean foreignKey;
	private Boolean ordered;
    private Boolean inverse;
	
	// Mappings
	private Boolean one2Many;
	
	public void validate(){
		if (null==getName() || null==getType()){
			throw new IllegalStateException("All propeties must have a name and a type. ("+this+")");
		}
	}

	/** creates a new property based on the element-valued key in FIELDS2CLASSES. Used mainly by the xml reader */
	public static Property makeNew(String element, Properties attributes) throws IllegalArgumentException, IllegalStateException{
		Class klass = (Class) FIELDS2CLASSES.get(element);
		if (null==klass){
			throw new IllegalArgumentException("FIELDS2CLASSES does not contain type "+element);
		}
		
		Property p;
		
		try {
			p = (Property) klass.getConstructor(new Class[]{Properties.class}).newInstance(new Object[]{attributes});
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

    public void setExtracode(String extracode) {
        this.extracode = extracode;
    }

    public String getExtracode() {
        return extracode;
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

	public void setMutable(Boolean mutable) {
		this.mutable = mutable;
	}

	public Boolean getMutable() {
		return mutable;
	}

	public void setOrdered(Boolean ordered) {
		this.ordered = ordered;
	}
	
	public Boolean getOrdered() {
		return ordered;
	}

    public void setInverse(Boolean inverse) {
        this.inverse = inverse;
    }
    
    public Boolean getInverse() {
        return inverse;
    }
    
	public void setForeignKey(Boolean foreignKey) {
		this.foreignKey = foreignKey;
	}

	public Boolean getForeignKey() {
		return foreignKey;
	}

	public void setOne2Many(Boolean one2Many) {
		this.one2Many = one2Many;
	}

	public Boolean getOne2Many() {
		return one2Many;
	}

    /** creates a Property and sets fields based on attributes USING DEFAULT VALUES. Subclassees may override these values */
    public Property(Properties attrs){
        setName(attrs.getProperty("name",null));
        setType(attrs.getProperty("type",null));
        setDefaultValue(attrs.getProperty("default",null));//TODO currently no way to use this!!
        setRequired(Boolean.valueOf(attrs.getProperty("required","false")));
        setUnique(Boolean.valueOf(attrs.getProperty("unique","false"))); // TODO wanted to use KEYS.put(id,field) !! 
        setMutable(Boolean.valueOf(attrs.getProperty("mutable","true")));
        setOrdered(Boolean.valueOf(attrs.getProperty("ordered","false")));
        setInverse(Boolean.valueOf(attrs.getProperty("inverse","false")));
        setExtracode("");
        if (VALUES.containsKey(getType())){
            setForeignKey(Boolean.FALSE);
            setType(((Class)VALUES.get(getType())).getName());
        } else {
            setForeignKey(Boolean.TRUE);
        }
        
    }
    
}

// NOTE: For all the following be sure to check the defaults set on Property!
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class OptionalField extends Property {
    public OptionalField(Properties attrs){
        super(attrs);
    }
}

class RequiredField extends OptionalField {
	public RequiredField(Properties attrs){
		super(attrs);
		setRequired(Boolean.TRUE);
	}
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class ZeroManyField extends Property {
    public ZeroManyField(Properties attrs){
        super(attrs);
        setRequired(Boolean.FALSE);
        setOne2Many(Boolean.TRUE);
        
        /* see ch. 7 hibernate doc on association mappings */
        if (getOrdered().booleanValue()) 
        {
            setRequired(Boolean.TRUE); // FIXME here we need to change the many2one!!
        } else {
            setInverse(Boolean.valueOf(attrs.getProperty("inverse","true")));    
        }
        
    }
}

class OneManyField extends ZeroManyField {
	public OneManyField(Properties attrs){
		super(attrs);
		setRequired(Boolean.TRUE);
	}
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class ManyZeroField extends Property {
    public ManyZeroField(Properties attrs){
        super(attrs);
    }
}

class ManyOneField extends ManyZeroField {
	public ManyOneField(Properties attrs){
		super(attrs);
		setRequired(Boolean.TRUE);
	}
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// DIFFERENT SEMANTICS!!!
class EntryField extends Property {
	public EntryField(Properties attrs){
		super(attrs);
		setType("string");
        setForeignKey(Boolean.FALSE);
	}

	public void validate(){
		if ("string"!=getType()){
			throw new IllegalStateException("Enum entries can only be of type \"string\"");
		}
		super.validate();
	}
}
/** property from a link to a parent iobject */
class LinkParent extends Property {
    public LinkParent(Properties attrs){
        super(attrs);
        setName("parent");
	setRequired(Boolean.TRUE);
        setExtracode(
                "public "+getType()+" parent(){"+ 
                    "return ("+getType()+") getParent();}");

    }
}

/** property from a link to a child iobject */
class LinkChild extends Property {
    public LinkChild(Properties attrs){
        super(attrs);
        setName("child");
	setRequired(Boolean.TRUE);
        setExtracode(
        "public "+getType()+" child(){"+ 
            "return ("+getType()+") getChild();}");
    }
}

abstract class AbstractLink extends Property {
    protected abstract void linkStep(StringBuffer sb);
    public AbstractLink(Properties attrs){
        super(attrs);
        setOne2Many(Boolean.TRUE);
        
        String target = attrs.getProperty("target",null);
        if (target == null){
            throw new IllegalArgumentException(
                    "Target must be set on all parent/child properties:"+this);
        }

        // TODO Not managing two links to same table!!
        String simpleName = target.substring(
                target.lastIndexOf(".")+1,target.length());
        
        StringBuffer sb = new StringBuffer();
        sb.append("public void add"); sb.append(simpleName);
        sb.append(" (");sb.append(target);sb.append(" addition){\n");
        sb.append(getType());sb.append(" link = new ");
        sb.append(getType());sb.append("();\n");
        linkStep(sb);
        
        String getter = attrs.getProperty("name");
        getter = getter.substring(0,1).toUpperCase()+getter.substring(1);
        sb.append("\tthis.get"+getter+"().add(link);");
        sb.append("}\n");
        setExtracode(sb.toString());
    }
}

/** property from a child iobject to a link */
class ChildLink extends AbstractLink {
    protected void linkStep(StringBuffer sb){
        sb.append("\tlink.link(this, addition);\n");
    }
    public ChildLink(Properties attrs){
        super(attrs);
    }
}

/** property from a parent iobject to a link */
class ParentLink extends AbstractLink {
    protected void linkStep(StringBuffer sb){
        sb.append("\tlink.link(addition,this);\n");
    }
    public ParentLink(Properties attrs){
        super(attrs);
    }
}
