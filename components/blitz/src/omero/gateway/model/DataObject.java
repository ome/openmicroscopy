/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package omero.gateway.model;

import static omero.rtypes.rlong;

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
import omero.model.Annotation;
import omero.model.BooleanAnnotation;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.Dataset;
import omero.model.Details;
import omero.model.Event;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.LongAnnotation;
import omero.model.Permissions;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Project;
import omero.model.Screen;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.model.Well;
import omero.model.WellSample;

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
     * @param iObjects
     *            The map to handle.
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
     * @param iObjects
     *            The map to handle.
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
     * Converts the passed {@link IObject} into its corresponding Pojo object.
     *
     * @param obj
     *            The object to convert.
     * @return See above.
     */
    public static DataObject asPojo(IObject obj) {
        DataObject converted = null;
        if (obj instanceof Project) {
            converted = new ProjectData((Project) obj);
        } else if (obj instanceof Dataset) {
            converted = new DatasetData((Dataset) obj);
        } else if (obj instanceof TermAnnotation) {
            converted = new TermAnnotationData((TermAnnotation) obj);
        } else if (obj instanceof TagAnnotation) {
            converted = new TagAnnotationData((TagAnnotation) obj);
        } else if (obj instanceof CommentAnnotation) {
            converted = new TextualAnnotationData((CommentAnnotation) obj);
        } else if (obj instanceof LongAnnotation) {
            LongAnnotation ann = (LongAnnotation) obj;
            if (RatingAnnotationData.INSIGHT_RATING_NS.equals(ann.getNs().getValue())) {
                converted = new RatingAnnotationData(ann);
            } else {
                converted = new LongAnnotationData(ann);
            }
        } else if (obj instanceof BooleanAnnotation) {
            converted = new BooleanAnnotationData((BooleanAnnotation) obj);
        } else if (obj instanceof FileAnnotation) {
            converted = new FileAnnotationData((FileAnnotation) obj);
        } else if (obj instanceof Image) {
            converted = new ImageData((Image) obj);
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
     * @param value
     *            The value to set.
     */
    protected void setValue(IObject value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "IObject delegate for DataObject cannot be null.");
        }

        this.value = value;
    }

    /**
     * Returns <code>true</code> if setter value has modified the value of the
     * stored IObject, <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets to <code>true</code> if the value has been modified, <false>
     * otherwise.
     *
     * @param dirty
     *            The value to set.
     */
    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    // Common properties

    /**
     * Returns the database id of the IObject or <code>-1</code> if
     * <code>null</code>
     *
     * @return See above.
     */
    public long getId() {
        return value.getId() == null ? -1 : (value.getId() == null ? -1 : value
                .getId().getValue());
    }

    /**
     * Sets the database id.
     *
     * @param id
     *            The value to set.
     */
    public void setId(long id) {
        setDirty(true);
        value.setId(rlong(id));
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
     * @param version
     *            The value to set.
     */
    protected void setVersion(int version) {
        if (value instanceof IMutable) {
            IMutable m = (IMutable) value;
            setDirty(true);
            m.setVersion(new Integer(version));
        }
    }

    /**
     * Returns <code>true</code> if the object is loaded, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isLoaded() {
        return value.isLoaded();
    }

    /**
     * Returns the owner of the object.
     *
     * @return See above.
     */
    public ExperimenterData getOwner() {
        if (owner == null) {
            Experimenter experimenter = asIObject().getDetails().getOwner();
            owner = experimenter != null ? new ExperimenterData(experimenter) : null;
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
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName() + " (id=" + getId() + ")";
    }

    // ~ Helpers
    // =========================================================================

    protected int nullSafe(Integer i) {
        return i == null ? 0 : i.intValue();
    }

    protected int nullSafe(omero.RInt i) {
        return i == null ? 0 : i.getValue();
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

    protected float nullSafe(omero.RFloat f) {
        return f == null ? 0.0f : f.getValue();
    }

    protected double nullSafe(omero.RDouble d) {
        return d == null ? 0.0d : d.getValue();
    }

    protected double nullSafe(omero.model.Length l) {
        return l == null ? 0.0d : l.getValue();
    }

    protected Timestamp timeOfEvent(Event event) {
        if (event == null || !event.isLoaded() || event.getTime() == null) {
            return null;
        }
        return new Timestamp(event.getTime().getValue());
    }

    /**
     * Returns <code>true</code> if the details are <code>null</code>
     * otherwise <code>false</code> otherwise.
     *
     * @return See above.
     */
    protected boolean nullDetails() {
        return asIObject().getDetails() == null;
    }

    /**
     * Returns the details of the object.
     *
     * @return See above.
     */
    protected Details getDetails() {
        return asIObject().getDetails();
    }

    /**
     * Returns the creation time of the object.
     *
     * @return See above.
     */
    public Timestamp getCreated() {
        if (nullDetails()) return null;
        return timeOfEvent(getDetails().getCreationEvent());
    }

    /**
     * Returns the updated time of the object.
     *
     * @return See above.
     */
    public Timestamp getUpdated() {
        if (nullDetails()) return null;
        return timeOfEvent(getDetails().getUpdateEvent());
    }

    /**
     * Returns the id of the group.
     *
     * @return See above.
     */
    public long getGroupId() {
        Details d = getDetails();
        if (d == null) return -1;
        if (d.getGroup() == null) return -1;
        return d.getGroup().getId().getValue();
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
     * Returns the hosted IObject as an Experimenter. Not null; may through
     * class-cast exception
     *
     * @throws ClassCastException
     * @return not null IObject
     */
    public Experimenter asExperimenter() {
        return (Experimenter) asIObject();
    }

    /**
     * Returns the hosted IObject as an Experimenter Group. Not null; may
     * through class-cast exception
     *
     * @throws ClassCastException
     * @return not null IObject
     */
    public ExperimenterGroup asGroup() {
        return (ExperimenterGroup) asIObject();
    }

    /**
     * Returns the hosted IObject as an Annotation. Not null; may through
     * class-cast exception
     *
     * @throws ClassCastException
     * @return not null IObject
     */
    public Annotation asAnnotation() {
        return (Annotation) asIObject();
    }

    /**
     * Returns the hosted IObject as an Image. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return not null IObject
     */
    public Image asImage() {
        return (Image) asIObject();
    }

    /**
     * Returns the hosted IObject as a Dataset. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return not null IObject
     */
    public Dataset asDataset() {
        return (Dataset) asIObject();
    }

    /**
     * Returns the hosted IObject as a Project. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return See above
     */
    public Project asProject() {
        return (Project) asIObject();
    }

    /**
     * Returns the hosted IObject as a Pixels. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return See above
     */
    public Pixels asPixels() {
        return (Pixels) asIObject();
    }

    /**
     * Returns the hosted IObject as a Screen. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return See above
     */
    public Screen asScreen() {
        return (Screen) asIObject();
    }

    /**
     * Returns the hosted IObject as a Plate. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return See above
     */
    public Plate asPlate() {
        return (Plate) asIObject();
    }

    /**
     * Returns the hosted IObject as a Well. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return See above
     */
    public Well asWell() {
        return (Well) asIObject();
    }

    /**
     * Returns the hosted IObject as a Well. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return See above
     */
    public WellSample asWellSample() {
        return (WellSample) asIObject();
    }

    /**
     * Returns the hosted IObject as a Well. Not null; may through class-cast
     * exception
     *
     * @throws ClassCastException
     * @return See above
     */
    public Channel asChannel() {
        return (Channel) asIObject();
    }

    /**
     * Returns <code>true</code> if the object can be annotated 
     * <code>false</code> otherwise, depending on permissions level.
     *
     * @return See above.
     */
    public boolean canAnnotate()
    {
        Permissions p = asIObject().getDetails().getPermissions();
        if (p == null) return false;
        return p.canAnnotate();
    }

    /**
     * Returns <code>true</code> if the object can be edited by the user
     * currently logged in <code>false</code> otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    public boolean canEdit()
    {
        Permissions p = asIObject().getDetails().getPermissions();
        if (p == null) return false;
        return p.canEdit();
    }

    /**
     * Returns <code>true</code> if the object can be linked e.g. image
     * add to dataset, by the user currently logged in,
     * <code>false</code> otherwise, depending on
     * permissions level.
     *
     * @return See above.
     */
    public boolean canLink()
    {
        Permissions p = asIObject().getDetails().getPermissions();
        if (p == null) return false;
        return p.canLink();
    }

    /**
     * Returns <code>true</code> if the object can be deleted by the user 
     * currently logged in,
     * <code>false</code> otherwise, depending on permissions level.
     *
     * @return See above.
     */
    public boolean canDelete()
    {
        Permissions p = asIObject().getDetails().getPermissions();
        if (p == null) return false;
        return p.canDelete();
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
