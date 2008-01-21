/*
 * ome.dsl.Property
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.dsl;

// Java imports
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies

/**
 * reprents the <b>definition</b> of a property within a SemanticType
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OMERO-3.0
 */
public abstract class Property { // TODO need to define equality so that two
    // with the
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

    public final static Set<String> FIELDS = new HashSet<String>();
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
    }

    public final static Map<String, Class<? extends Property>> FIELDS2CLASSES = new HashMap<String, Class<? extends Property>>();
    static {
        FIELDS2CLASSES.put(REQUIRED, RequiredField.class);
        FIELDS2CLASSES.put(OPTIONAL, OptionalField.class);
        FIELDS2CLASSES.put(ONEMANY, OneManyField.class);
        FIELDS2CLASSES.put(ZEROMANY, ZeroManyField.class);
        FIELDS2CLASSES.put(MANYONE, ManyOneField.class);
        FIELDS2CLASSES.put(MANYZERO, ManyZeroField.class);
        FIELDS2CLASSES.put(ENTRY, EntryField.class);
        FIELDS2CLASSES.put(PARENT, ParentLink.class);
        FIELDS2CLASSES.put(CHILD, ChildLink.class);
        FIELDS2CLASSES.put(FROMPARENT, LinkParent.class);
        FIELDS2CLASSES.put(TOCHILD, LinkChild.class);
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

    public final static Map<String, String> JAVATYPES = new HashMap<String, String>();
    static {
        JAVATYPES.put(STRING, String.class.getName());
        JAVATYPES.put(BOOLEAN, Boolean.class.getName());
        JAVATYPES.put(INTEGER, Integer.class.getName());
        JAVATYPES.put(FLOAT, Float.class.getName());
        JAVATYPES.put(DOUBLE, Double.class.getName());
        JAVATYPES.put(LONG, Long.class.getName());
        JAVATYPES.put(TIMESTAMP, Timestamp.class.getName());
        JAVATYPES.put(TEXT, String.class.getName());
    }

    public final static Map<String, String> DBTYPES = new HashMap<String, String>();
    static {
        DBTYPES.putAll(JAVATYPES);
        DBTYPES.put(TEXT, TEXT);
    }

    /**
     * The {@link SemanticType} instance which this property belongs to
     */
    private SemanticType st;

    /**
     * The {@link SemanticType} instance which this property points at
     */
    private SemanticType actualType;

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

    private Boolean bidirectional;

    public void validate() {
        if (null == getName() || null == getType()) {
            throw new IllegalStateException(
                    "All propeties must have a name and a type. (" + this + ")");
        }
    }

    /**
     * creates a new property based on the element-valued key in FIELDS2CLASSES.
     * Used mainly by the xml reader
     */
    public static Property makeNew(String element, SemanticType st,
            Properties attributes) throws IllegalArgumentException,
            IllegalStateException {
        Class<? extends Property> klass = FIELDS2CLASSES.get(element);
        if (null == klass) {
            throw new IllegalArgumentException(
                    "FIELDS2CLASSES does not contain type " + element);
        }

        Property p;

        try {
            p = klass.getConstructor(
                    new Class[] { SemanticType.class, Properties.class })
                    .newInstance(new Object[] { st, attributes });
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot instantiate class " + klass, e);
        }
        return p;
    }

    @Override
    public String toString() {
        return "Property: " + getName() + " (" + getType() + ")";
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

    /**
     * Read-only property
     */
    public String getNameCapped() {
        return name.substring(0, 1).toUpperCase()
                + name.substring(1, name.length());
    }

    /**
     * Read-only property
     */
    public String getNameUpper() {
        return name.toUpperCase();
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        String t = JAVATYPES.get(type);
        if (t == null) {
            return type;
        }
        return t;
    }

    public void setActualType(SemanticType type) {
        this.actualType = type;
    }

    public SemanticType getActualType() {
        return this.actualType;
    }

    /**
     * Read-only variable
     */
    public String getDbType() {
        String t = DBTYPES.get(type);
        if (t == null) {
            return type;
        }
        return t;
    }

    /**
     * Read-only variable
     */
    public String getTypeAnnotation() {
        if (type.equals("text")) {
            return "@org.hibernate.annotations.Type(type=\"org.hibernate.type.TextType\")";
        } else {
            return "// No @Type annotation";
        }
    }

    /**
     * Read-only variable
     */
    public String getShortType() {
        return unqualify(type);
    }

    /**
     * Read-only value. Overwritten in subclasses
     */
    public String getFieldType() {
        return getType();
    }

    /**
     * Read-only value. Overwritten in subclasses
     */
    public String getFieldInitializer() {
        return "null";
    }

    // TODO remove
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

    /**
     * Read-only property
     */
    public String getShortTarget() {
        return unqualify(target);
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

    /**
     * Read-only field
     */
    public Boolean getNullable() {
        return !required;
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

    /**
     * Read-only variable
     */
    public String getInverseCapped() {
        if (inverse == null) {
            return null;
        }
        return inverse.substring(0, 1).toUpperCase()
                + inverse.substring(1, inverse.length());
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

    public void setBidirectional(Boolean bdir) {
        this.bidirectional = bdir;
    }

    public Boolean getBidirectional() {
        return this.bidirectional;
    }

    /**
     * @return
     * @see ticket:813
     */
    public String getDef() {
        if (type.equals(FLOAT)) {
            StringBuilder sb = new StringBuilder(32);
            sb.append("double precision");
            if (!getNullable()) {
                sb.append(" not null");
            }
            if (getUnique()) {
                sb.append(" unique");
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Read-only property, for subclassing
     */
    public boolean getIsLink() {
        return false;
    }

    /**
     * creates a Property and sets fields based on attributes USING DEFAULT
     * VALUES. Subclassees may override these values
     */
    public Property(SemanticType st, Properties attrs) {
        setST(st);
        setName(attrs.getProperty("name", null));
        setType(attrs.getProperty("type", null));
        setDefaultValue(attrs.getProperty("default", null));// TODO currently no
        // way to use this!!
        setTag(attrs.getProperty("tag", null));
        setTarget(attrs.getProperty("target", null));
        setInverse(attrs.getProperty("inverse", null)); // see
        // DslHandler.process()
        setBidirectional(Boolean.TRUE);// will be handle by
        // DslHandler.process()
        setRequired(Boolean.valueOf(attrs.getProperty("required", "false")));
        setUnique(Boolean.valueOf(attrs.getProperty("unique", "false"))); // TODO
        // wanted
        // to
        // use
        // KEYS.put(id,field)
        // !!
        setOrdered(Boolean.valueOf(attrs.getProperty("ordered", "false")));
        // TODO Mutability
        setInsert(Boolean.TRUE);
        setUpdate(Boolean.valueOf(attrs.getProperty("mutable", "true")));

        if (JAVATYPES.containsKey(type)) {
            setForeignKey(null);
        } else {
            setForeignKey(SemanticType.typeToColumn(st.getId()));
        }

    }

    private static String unqualify(String str) {
        if (str == null) {
            return null;
        }
        int idx = str.lastIndexOf(".");
        if (idx < 0) {
            return str;
        }
        return str.substring(idx + 1, str.length());
    }

}

// NOTE: For all the following be sure to check the defaults set on Property!
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ~ Simple
// ========
class OptionalField extends Property {
    public OptionalField(SemanticType st, Properties attrs) {
        super(st, attrs);
    }
}

class RequiredField extends OptionalField {
    public RequiredField(SemanticType st, Properties attrs) {
        super(st, attrs);
        setRequired(Boolean.TRUE);
    }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ~ 1-Many
// ========
class ZeroManyField extends Property {
    public ZeroManyField(SemanticType st, Properties attrs) {
        super(st, attrs);
        setRequired(Boolean.FALSE);
        setOne2Many(Boolean.TRUE);

        /* see ch. 7 hibernate doc on association mappings */
        if (getOrdered().booleanValue()) {
            setRequired(Boolean.TRUE); // FIXME here we need to change the
            // many2one!!
        }
    }

    @Override
    public String getFieldType() {
        StringBuilder sb = new StringBuilder();
        if (getOrdered()) {
            sb.append("java.util.List<");
        } else {
            sb.append("java.util.Set<");
        }
        sb.append(getType());
        sb.append(">");
        return sb.toString();
    }

    @Override
    public String getFieldInitializer() {
        StringBuilder sb = new StringBuilder();
        if (getOrdered()) {
            sb.append("new java.util.ArrayList<");
        } else {
            sb.append("new java.util.HashSet<");
        }
        sb.append(getType());
        sb.append(">()");
        return sb.toString();
    }

    @Override
    public void validate() {
        super.validate();
        if (getInverse() == null) {
            throw new IllegalArgumentException(
                    "\n"
                            + this.toString()
                            + ": invalid "
                            + this.getClass().getName()
                            + " property.\n"
                            + "\n All zeromany and onemany fields must provide either the \"inverse\" "
                            + "\n \"ordered\" or \"tag\" attribute E.g.\n"
                            + "\n"
                            + "<type id=...>\n"
                            + "\t<properties>\n"
                            + "\t\t<onemany name=\"example\" type=\"Example\" inverse=\"parent\">");
        }
    }
}

class OneManyField extends ZeroManyField {
    public OneManyField(SemanticType st, Properties attrs) {
        super(st, attrs);
        setRequired(Boolean.TRUE);
    }
}

abstract class AbstractLink extends ZeroManyField {

    public AbstractLink(SemanticType st, Properties attrs) {
        super(st, attrs);
        setTarget(attrs.getProperty("target", null));

    }

    @Override
    public void validate() {
        super.validate();
        if (getTarget() == null) {
            throw new IllegalArgumentException(
                    "Target must be set on all parent/child properties:" + this);
        }

    }
}

/** property from a child iobject to a link */
class ChildLink extends AbstractLink {
    public ChildLink(SemanticType st, Properties attrs) {
        super(st, attrs);
        setForeignKey("child");
        setInverse("parent");
    }

    @Override
    public boolean getIsLink() {
        return true;
    }
}

/** property from a parent iobject to a link */
class ParentLink extends AbstractLink {
    public ParentLink(SemanticType st, Properties attrs) {
        super(st, attrs);
        setForeignKey("parent");
        setInverse("child");
    }

    @Override
    public boolean getIsLink() {
        return true;
    }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ~ Many-1
// ========

class ManyZeroField extends Property {
    public ManyZeroField(SemanticType st, Properties attrs) {
        super(st, attrs);
    }
}

class ManyOneField extends ManyZeroField {
    public ManyOneField(SemanticType st, Properties attrs) {
        super(st, attrs);
        setRequired(Boolean.TRUE);
        if (getOrdered()) {
            setInsert(Boolean.FALSE);
            setUpdate(Boolean.FALSE);
        }
    }
}

/** property from a link to a parent iobject */
class LinkParent extends ManyOneField {
    public LinkParent(SemanticType st, Properties attrs) {
        super(st, attrs);
        setName("parent");
    }

    @Override
    public String getFieldType() {
        return "IObject";
    }

    @Override
    public boolean getIsLink() {
        return true;
    }
}

/** property from a link to a child iobject */
class LinkChild extends ManyOneField {
    public LinkChild(SemanticType st, Properties attrs) {
        super(st, attrs);
        setName("child");
    }

    @Override
    public String getFieldType() {
        return "IObject";
    }

    @Override
    public boolean getIsLink() {
        return true;
    }

}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ~ DIFFERENT SEMANTICS!!!
// ========================
class EntryField extends Property {
    public EntryField(SemanticType st, Properties attrs) {
        super(st, attrs);
        setType("string");
        setForeignKey(null);
    }

    @Override
    public void validate() {
        super.validate();
        if (!"java.lang.String".equals(getType())) {
            throw new IllegalStateException(
                    "Enum entries can only be of type \"java.lang.String\", not "
                            + getType());
        }
    }
}

class DetailsField extends Property {

    public DetailsField(SemanticType st, Properties attrs) {
        super(st, attrs);
        setName("details");
        setType("ome.model.internal.Details");
    }

    @Override
    public String getFieldInitializer() {
        return "new Details()";
    }
}