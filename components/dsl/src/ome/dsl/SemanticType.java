/*
 * ome.dsl.SemanticType
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.dsl;

// Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies

/**
 * represents a SemanticType <b>definition</b>.
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OMERO-3.0
 */
public abstract class SemanticType {

    // TYPE identifiers
    public final static String ABSTRACT = "abstract";

    public final static String TYPE = "type";

    public final static String CONTAINER = "container";

    public final static String RESULT = "result";

    public final static String LINK = "link";

    public final static String ENUM = "enum";

    public final static Set TYPES = new HashSet();
    static {
        TYPES.add(ABSTRACT);
        TYPES.add(TYPE);
        TYPES.add(CONTAINER);
        TYPES.add(RESULT);
        TYPES.add(LINK);
        TYPES.add(ENUM);
    }

    public final static Map TYPES2CLASSES = new HashMap();
    static {
        TYPES2CLASSES.put(ABSTRACT, AbstractType.class);
        TYPES2CLASSES.put(TYPE, BaseType.class);
        TYPES2CLASSES.put(CONTAINER, ContainerType.class);
        TYPES2CLASSES.put(LINK, LinkType.class);
        TYPES2CLASSES.put(RESULT, ResultType.class);
        TYPES2CLASSES.put(ENUM, EnumType.class);
    }

    // all properties
    private List<Property> properties = new ArrayList<Property>();

    /** future class name; required for all types */
    private String id;

    private String table;

    /** optional item */
    private String superclass;

    private String discriminator;

    // possible interfaces
    private Boolean abstrakt;

    private Boolean annotated;

    private Boolean described;

    private Boolean global;

    private Boolean immutable;

    private Boolean named;

    /**
     * sets the the various properties available in attrs USING DEFAULTS IF NOT
     * AVAILABLE. Subclasses may override these values.
     */
    public SemanticType(Properties attrs) {
        setId(attrs.getProperty("id", getId()));
        setTable(typeToColumn(getId()));
        if (null == getId()) {
            throw new IllegalStateException("All types must have an id");
        }

        setSuperclass(attrs.getProperty("superclass"));
        setDiscriminator(attrs.getProperty("discriminator"));

        setAnnotated(Boolean.valueOf(attrs.getProperty("annotated", "false")));
        setAbstract(Boolean.valueOf(attrs.getProperty("abstract", "false")));
        setDescribed(Boolean.valueOf(attrs.getProperty("described", "false")));
        setGlobal(Boolean.valueOf(attrs.getProperty("global", "false")));
        setImmutable(Boolean.valueOf(attrs.getProperty("immutable", "false")));
        setNamed(Boolean.valueOf(attrs.getProperty("named", "false")));

        // TODO add "UnsupportedOperation for any other properties in attrs.
        // same in Property

    }

    public void validate() {
        // Left empty in-case anyone forgets to override.
    }

    /**
     * creates a new type based on the element-valued key in TYPES2CLASSES. Used
     * mainly by the xml reader
     */
    public static SemanticType makeNew(String element, Properties attributes)
            throws IllegalArgumentException, IllegalStateException {
        Class klass = (Class) TYPES2CLASSES.get(element);

        if (null == klass) {
            throw new IllegalArgumentException(
                    "TYPES2CLASSES does not contain type " + element);
        }

        SemanticType st;

        try {
            st = (SemanticType) klass.getConstructor(
                    new Class[] { Properties.class }).newInstance(
                    new Object[] { attributes });
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot instantiate class " + klass, e);
        }

        return st;
    }

    @Override
    public String toString() {
        String result = "\n" + getId();
        for (Iterator it = getProperties().iterator(); it.hasNext();) {
            Property element = (Property) it.next();
            result += "\n  " + element.toString();
        }
        return result;
    }

    public static String typeToColumn(String type) {
        return type.substring(type.lastIndexOf(".") + 1).toLowerCase();
    }

    //
    // Getters and Setters
    //

    public void setAbstract(Boolean abstrakt) {
        this.abstrakt = abstrakt;
    }

    public Boolean getAbstract() {
        return abstrakt;
    }

    public void setAnnotated(Boolean annotated) {
        this.annotated = annotated;
    }

    public Boolean getAnnotated() {
        return annotated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getLastDotInId() {
        int idx = id.lastIndexOf(".");
        if (idx < 0) {
            throw new RuntimeException(id + " doesn't have a package. "
                    + "Use of default package prohibited.");
        }
        return idx;
    }

    /**
     * Read-only property
     */
    public String getPackage() {
        return id.substring(0, getLastDotInId());
    }

    /**
     * Read-only property
     */
    public String getShortname() {
        return id.substring(getLastDotInId() + 1, id.length());
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }

    public String getSuperclass() {
        return superclass;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setNamed(Boolean named) {
        this.named = named;
    }

    public Boolean getNamed() {
        return named;
    }

    public void setDescribed(Boolean described) {
        this.described = described;
    }

    public Boolean getDescribed() {
        return described;
    }

    public void setImmutable(Boolean immutable) {
        this.immutable = immutable;
    }

    public Boolean getImmutable() {
        return immutable;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Property> getProperties() {
        return properties;
    }

    /**
     * Read-only method which currently filters out {@link EntryField}
     * 
     * @return
     */
    public List<Property> getClassProperties() {
        List<Property> rv = new ArrayList<Property>();
        for (Property property : getProperties()) {
            if (!(property instanceof EntryField)) {
                rv.add(property);
            }
        }
        return rv;
    }

    public List<Property> getRequiredSingleProperties() {
        List<Property> rv = new ArrayList<Property>();
        for (Property property : getClassProperties()) {
            boolean req = property.getRequired() == null ? false : property
                    .getRequired();
            boolean col = property.getOne2Many() == null ? false : property
                    .getOne2Many();
            if (req && !col) {
                rv.add(property);
            }
        }
        return rv;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }

    public Boolean getGlobal() {
        return global;
    }

    /**
     * Read-only field
     */
    public String getDetails() {
        if (!getGlobal()) {
            if (!getImmutable()) {
                return "MutableDetails";
            }
            return "Details";
        }
        return "GlobalDetails";
    }

    /**
     * Read-only property to be overwritten by subclasses
     */
    public boolean getIsLink() {
        return false;
    }

    /**
     * Read-only property to be overwritten by subclasses
     */
    public boolean getIsEnum() {
        return false;
    }

    /**
     * Helper method
     */
    public static String unqualify(String klass) {
        if (klass == null) {
            return null;
        }
        int idx = klass.lastIndexOf(".");
        if (idx < 0) {
            return klass;
        } else {
            return klass.substring(idx + 1, klass.length());
        }
    }
}

//
//
// Subclasses which implement specific logic. (Essentially aliases for multiple
// properties)
//
//

class BaseType extends SemanticType {
    public BaseType(Properties attrs) {
        super(attrs);
    }
}

class AbstractType extends SemanticType {
    public AbstractType(Properties attrs) {
        super(attrs);
        this.setAbstract(Boolean.TRUE);
    }
}

class ContainerType extends SemanticType {
    public ContainerType(Properties attrs) {
        super(attrs);
        // TODO
    }
}

class LinkType extends SemanticType {
    public LinkType(Properties attrs) {
        super(attrs);
    }

    @Override
    public boolean getIsLink() {
        return true;
    }
}

class ResultType extends SemanticType {
    public ResultType(Properties attrs) {
        super(attrs);
        // TODO
    }
}

class EnumType extends SemanticType {

    public EnumType(Properties attrs) {
        super(attrs);
        Properties props = new Properties();
        props.setProperty("name", "value");
        props.setProperty("type", "string");
        props.setProperty("unique", "true");
        RequiredField value = new RequiredField(this, props);
        getProperties().add(value);
        setImmutable(Boolean.TRUE);
    }

    @Override
    public boolean getIsEnum() {
        return true;
    }
    // TODO: only value? at least value?
    // public void validate(){
    // for (Iterator it = getProperties().iterator(); it.hasNext();) {
    // if (it.next().getClass()!=EntryField.class){
    // throw new IllegalStateException("EnumTypes can only contain
    // EntryProperties.");
    // }
    // }
    // super.validate();
    // }
}
