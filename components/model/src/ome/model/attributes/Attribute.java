/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.model.attributes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.IAttribute;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.model.meta.ExternalInfo;

public class Attribute implements IAttribute, java.io.Serializable {

    private Long id;
    private Details details = new Details();
    private ExternalInfo reference = null;
    private Integer version = 0;

    /** default constructor */
    public Attribute() {
    }

    /** default constructor which sets id */
    public Attribute(Long id) {
        setId(id);
        getDetails().setContext(this);
    }

    /** constructor which sets id and permits the "unloading" of the object */
    public Attribute(Long id, boolean loaded) {
        this(id);
        if (!loaded) {
            unload();
        }
    }

    protected void preGetter(String field) {
        errorIfUnloaded();
    }

    protected void postGetter(String field) {
    }

    protected void preSetter(String field, Object value) {
        errorIfUnloaded();
    }

    protected void postSetter(String field, Object value) {
    }

    protected void throwNullCollectionException(String propertyName) {
        throw new ApiUsageException(
                "Error updating collection:"
                        + propertyName
                        + "\n"
                        + "Collection is currently null. This can be seen\n"
                        + "by testing \"sizeOf"
                        + propertyName
                        + " < 0\". This implies\n"
                        + "that this collection was unloaded. Please refresh this object\n"
                        + "in order to update this collection.\n");
    }

    // Property accessors
    /**
     * * The DB unique identifier for this object. You are not responsible for
     * setting the id; however, it can be useful for creating "unloaded"
     * versions of your objects.
     * 
     */

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * * The details of this object correspond to low-level system information.
     * Owner, permissions,
     * 
     */

    public Details getDetails() {
        try {
            preGetter(DETAILS);
            return this.details;
        } finally {
            postGetter(DETAILS);
        }

    }

    public void setDetails(Details details) {
        try {
            preSetter(DETAILS, details);
            this.details = details;
        } finally {
            postSetter(DETAILS, details);
        }
    }

    public Integer getVersion() {
        try {
            preGetter(VERSION);
            return this.version;
        } finally {
            postGetter(VERSION);
        }

    }

    public void setVersion(Integer version) {
        try {
            preSetter(VERSION, version);
            this.version = version;
        } finally {
            postSetter(VERSION, version);
        }
    }

    public ExternalInfo getReference() {
        try {
            preGetter(REFERENCE);
            return this.reference;
        } finally {
            postGetter(REFERENCE);
        }

    }

    public void setReference(ExternalInfo reference) {
        try {
            preSetter(REFERENCE, reference);
            this.reference = reference;
        } finally {
            postSetter(REFERENCE, reference);
        }
    }

    public final static String OWNER_FILTER = "Attribute_owner_filter";
    public final static String GROUP_FILTER = "Attribute_group_filter";
    public final static String EVENT_FILTER = "Attribute_event_filter";
    public final static String PERMS_FILTER = "Attribute_perms_filter";

    private static final long serialVersionUID = 0000000030000010301L;

    public boolean isValid() {
        return ome.util.Validator.validate(this).isValid();
    }

    public ome.util.Validation validate() {
        return ome.util.Validator.validate(this);
    }

    public ome.util.Filterable newInstance() {
        return new Attribute();
    }

    public boolean acceptFilter(ome.util.Filter __filter) {
        this.id = (Long) __filter.filter(ID, id);
        this.details = (Details) __filter.filter(DETAILS, details);
        this.version = (Integer) __filter.filter(VERSION, version);
        this.reference = (ExternalInfo) __filter.filter(REFERENCE, reference);
        return true;
    }

    @Override
    public String toString() {
        return "Attribute"
                + (getId() == null ? ":Hash_" + this.hashCode() : ":Id_"
                        + getId());
    }

    // FIELD-FIELDS

    public Set fields() {
        return Attribute.FIELDS;
    }

    public final static String ID = "Attribute_id";
    public final static String DETAILS = "Attribute_details";
    public final static String VERSION = "Attribute_version";
    public final static String REFERENCE = "Attribute_reference";
    public final static Set FIELDS;
    static {
        Set raw = new HashSet();
        raw.add(ID);
        raw.add(DETAILS);
        raw.add(VERSION);
        raw.add(REFERENCE);
        FIELDS = java.util.Collections.unmodifiableSet(raw);
    }

    // Dynamic Getter/Setter
    protected Map _dynamicFields;

    public Object retrieve(String field) {
        if (field == null) {
            return null;
        } else if (field.equals(ID)) {
            return getId();
        } else if (field.equals(DETAILS)) {
            return getDetails();
        } else if (field.equals(VERSION)) {
            return getVersion();
        } else if (field.equals(REFERENCE)) {
            return getReference();
        } else {
            if (_dynamicFields != null) {
                return _dynamicFields.get(field);
            }
            return null;
        }
    }

    public void putAt(String field, Object value) {
        if (field == null) {
            return;
        } else if (field.equals(ID)) {
            setId((Long) value);
        } else if (field.equals(DETAILS)) {
            setDetails((Details) value);
        } else if (field.equals(VERSION)) {
            setVersion((Integer) value);
        } else if (field.equals(REFERENCE)) {
            setReference((ExternalInfo) value);
        } else {
            if (_dynamicFields == null) {
                _dynamicFields = new HashMap();
            }

            _dynamicFields.put(field, value);
        }
    }

    protected boolean _loaded = true;

    public boolean isLoaded() {
        return _loaded;
    }

    public void unload() {
        _loaded = false;
        this.details = null;
        this.version = null;
        this.reference = null;

    }

    protected void errorIfUnloaded() {
        if (!_loaded) {
            throw new IllegalStateException("Object unloaded:" + this);
        }
    }

    private transient ome.model.internal.GraphHolder _graphHolder;

    public final ome.model.internal.GraphHolder getGraphHolder() {
        if (_graphHolder == null) {
            _graphHolder = new ome.model.internal.GraphHolder();
        }
        return _graphHolder;
    }

    // SERIALIZATION
    /** the serialVersionID constant is set by mapping.vm */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
    }

}

class StringAttribute extends Attribute {

    String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

class FileAttribute extends Attribute {

    OriginalFile file;

    public OriginalFile getFile() {
        return file;
    }

    public void setFile(OriginalFile file) {
        this.file = file;
    }
}