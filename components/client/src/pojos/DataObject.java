/*
 * pojos.DataObject
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

// Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.IAnnotated;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.UrlAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/**
 * Abstract superclass for objects that hold <i>OMEDS</i> data. Delegates
 * getters and setters to <code>IObject</code> instances. Modifications are
 * propagated. <b>Not thread-safe.</b>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.more@gmx.de"> josh.moore@gmx.de</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME2.2
 */
public abstract class DataObject {

	/**
     * Converts the collection of {@link IObject}s into the corresponding
     * {@link DataObject}s
     * 
     * @param iObjects The map to handle.
     * @return See above.
     */
    public static Set asPojos(Collection iObjects) {
        Set<DataObject> result = new HashSet<DataObject>();
        IObject obj;
        for (Iterator it = iObjects.iterator(); it.hasNext();) {
        	obj = (IObject) it.next();
            result.add(asPojo(obj));
        }
        return result;
    }

    /**
     * Converts the map of {@link IObject}s into the corresponding
     * {@link DataObject}s
     * 
     * @param iObjects The map to handle.
     * @return See above.
     */
    public static Map asPojos(Map iObjects) {
        Map result = new HashMap();
        Object key, value;
        Object convertedKey, convertedValue;
        for (Iterator it = iObjects.keySet().iterator(); it.hasNext();) {
            key = it.next();
            value = iObjects.get(key);
            convertedKey = null;
            convertedValue = null;
            if (key instanceof IObject) {
                convertedKey = asPojo((IObject) key);
            } else if (key instanceof Collection) {
                convertedKey = asPojos((Collection) key);
            } else if (key instanceof Map) {
                convertedKey = asPojos((Map) key);
            }

            if (value instanceof IObject) {
                convertedValue = asPojo((IObject) iObjects.get(key));
            } else if (value instanceof Collection) {
                convertedValue = asPojos((Collection) iObjects.get(key));
            } else if (value instanceof Map) {
                convertedValue = asPojos((Map) iObjects.get(key));
            }

            result.put(null == convertedKey ? key : convertedKey,
                    null == convertedValue ? value : convertedValue);
        }
        return result;
    }

    /**
     * Converts the passed {@link IObject}  into its corresponding 
     * Pojo object.
     * 
     * @param obj	The  object to convert.
     * @return See above.
     */
    public static DataObject asPojo(IObject obj) {
        DataObject converted = null;
        if (obj instanceof Project) {
            converted = new ProjectData((Project) obj);
        } else if (obj instanceof Dataset) {
            converted = new DatasetData((Dataset) obj);
        } else if (obj instanceof UrlAnnotation) {
            converted = new URLAnnotationData((UrlAnnotation) obj);
        } else if (obj instanceof TagAnnotation) {
        	converted = new TagAnnotationData((TagAnnotation) obj);
        } else if (obj instanceof TextAnnotation) {
        	converted = new TextualAnnotationData((TextAnnotation) obj);	
        } else if (obj instanceof LongAnnotation) {
        	LongAnnotation ann = (LongAnnotation) obj;
        	if (RatingAnnotationData.INSIGHT_RATING_NS.equals(ann.getNs()))
        		converted = new RatingAnnotationData(ann);
        	//TODO create a generic long annotation data
        } else if (obj instanceof FileAnnotation) {
        	converted = new FileAnnotationData((FileAnnotation) obj);		
        	
        } else if (obj instanceof Image) {
            converted = new ImageData((Image) obj);
        } else if (obj instanceof CategoryGroup) {
            converted = new CategoryGroupData((CategoryGroup) obj);
        } else if (obj instanceof Category) {
            converted = new CategoryData((Category) obj);
        } else if (obj instanceof Pixels) {
            converted = new PixelsData((Pixels) obj);
        } else if (obj instanceof Experimenter) {
            converted = new ExperimenterData((Experimenter) obj);
        } else if (obj instanceof ExperimenterGroup) {
            converted = new GroupData((ExperimenterGroup) obj);
        } else if (obj != null) {
            throw new IllegalArgumentException("Unknown type: "
                    + obj.getClass().getName());
        }
        return converted;
    }

    /** delegate IObject */
    private IObject value = null;

    /** lazily-loaded owner */
    private ExperimenterData owner = null;

    /** lazily-loaded permissions */
    private PermissionData permissions = null;

    /**
     * NOTE: IObject-views are mutable. we can't ensure non-dirtiness, only
     * non-cleanness
     */
    private boolean dirty = false;

    /**
     * Sets the {@link IObject}.
     * 
     * @param value The value to set.
     */
    protected void setValue(IObject value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "IObject delegate for DataObject cannot be null.");
        }

        this.value = value;
    }

    /** 
     * Returns <code>true</code> if setter value has modified the value of 
     * the stored IObject, <code>false</code> otherwise.
     * 
     *  @return See above.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets to <code>true</code> if the value has been modified, 
     * <false> otherwise.
     * 
     * @param dirty The value to set.
     */
    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    // Common properties

    /** 
     * Returns the database id of the IObject or <code>-1</code>
     * if <code>null</code> 
     * 
     * @return See above.
     */
    public long getId() {
        return value.getId() == null ? -1 : value.getId().longValue();
    }

    /**
     * Sets the database id.
     * 
     * @param id The value to set.
     */
    public void setId(long id) {
        setDirty(true);
        value.setId(new Long(id));
    }

    /**
     * Returns the version of the object if the object is immutable, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    protected int getVersion() {
        if (value instanceof IMutable) {
            IMutable m = (IMutable) value;
            return m.getVersion() == null ? 0 : m.getVersion().intValue();
        }
        return 0;
    }

    /**
     * Sets the version of the object if it is immutable.
     * 
     * @param version The value to set.
     */
    protected void setVersion(int version) {
        if (value instanceof IMutable) {
            IMutable m = (IMutable) value;
            setDirty(true);
            m.setVersion(new Integer(version));
        }
    }

    /**
     * Returns <code>true</code> if the object is loaded, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isLoaded() {
        return value.isLoaded();
    }

    /**
     * Returns the collection of filters.
     * 
     * @return See above.
     */
    protected Set getFiltered() {
        return new HashSet(value.getDetails().filteredSet());
    }

    /**
     * Returns <code>true</code> if the passed value is a filter,
     * <code>false</code> otherwise.
     * 
     * @param fieldName The value to handle.
     * @return See above.
     */
    public boolean isFiltered(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        return value.getDetails().filteredSet() == null ? false : value
                .getDetails().filteredSet().contains(fieldName);
    }

    /**
     * Returns the owner of the object.
     * 
     * @return See above.
     */
    public ExperimenterData getOwner() {
        if (owner == null) {
            owner = new ExperimenterData(asIObject().getDetails().getOwner());
        }
        return owner;
    }

    /**
     * Returns the permission of the object.
     * 
     * @return See above.
     */
    public PermissionData getPermissions() {
        if (permissions == null) {
            permissions = new PermissionData(getDetails().getPermissions());
        }
        return permissions;
    }

    /**
     * Overridden to return the name of the class and the object id.
     * @see Object#toString()
     */
    public String toString() {
        return getClass().getName() + " (id=" + getId() + ")";
    }

    // ~ Helpers
    // =========================================================================

    protected int nullSafe(Integer i) {
        return i == null ? 0 : i.intValue();
    }

    protected long nullSafe(Long l) {
        return l == null ? 0L : l.longValue();
    }

    protected double nullSafe(Double d) {
        return d == null ? 0.0 : d.doubleValue();
    }

    protected float nullSafe(Float f) {
        return f == null ? 0.0f : f.floatValue();
    }

    protected Timestamp timeOfEvent(Event event) {
        if (event == null || !event.isLoaded() || event.getTime() == null) {
            throw new IllegalStateException("Event does not contain timestamp.");
        }
        return new Timestamp(event.getTime().getTime());
    }

    protected boolean nullDetails() {
        return asIObject().getDetails() == null;
    }

    protected Details getDetails() {
        return asIObject().getDetails();
    }

    // ~ VIEWS
    // =========================================================================
    // These methods should never a null value
    // since single setter checks for null.

    /**
     * not null; no exceptions.
     * 
     * @return not null IObject
     */
    public IObject asIObject() {
        return value;
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public IAnnotated asIAnnotated() {
        return (IAnnotated) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public Experimenter asExperimenter() {
        return (Experimenter) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public ExperimenterGroup asGroup() {
        return (ExperimenterGroup) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public Annotation asAnnotation() {
        return (Annotation) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public Image asImage() {
        return (Image) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public Dataset asDataset() {
        return (Dataset) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public Category asCategory() {
        return (Category) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public CategoryGroup asCategoryGroup() {
        return (CategoryGroup) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public Project asProject() {
        return (Project) asIObject();
    }

    /**
     * not null; may through class-cast exception
     * 
     * @throws ClassCastException
     * @return not null IObject
     */
    public Pixels asPixels() {
        return (Pixels) asIObject();
    }

}

/**
 * Encapsulates the logic to update one set based on the changes made to a copy
 * of that set. Provides two iterators for acting on the changes: <code>
 * SetMutator m = new SetMutator( oldSet, updatedSet );
 * while ( m.hasDeletions() )
 * {
 *      DataObject d = m.nextDeletion();
 * }
 * 
 * while ( m.hasAdditions() )
 * {
 *      DataObject d = m.nextAddition();
 * }
 * return m.result();
 * </code>
 */
class SetMutator<E> {

    private final List<E> _old, _new, del, add;

    private final Iterator<E> r, a;

    /** null-safe constructor */
    public SetMutator(Collection<E> originalSet, Collection<E> targetSet) {

        _old = originalSet == null ? new ArrayList<E>() : new ArrayList<E>(
                originalSet);
        _new = targetSet == null ? new ArrayList<E>() : new ArrayList<E>(
                targetSet);

        del = new ArrayList<E>(_old);
        del.removeAll(_new);
        r = del.iterator();

        add = new ArrayList<E>(_new);
        add.removeAll(_old);
        a = add.iterator();

    }

    public boolean moreDeletions() {
        return r.hasNext();
    }

    public DataObject nextDeletion() {
        return (DataObject) r.next();
    }

    public boolean moreAdditions() {
        return a.hasNext();
    }

    public DataObject nextAddition() {
        return (DataObject) a.next();
    }

    public List<E> result() {
        return new ArrayList<E>(_new);
    }

}
