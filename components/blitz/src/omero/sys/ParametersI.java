/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.sys;

import static omero.rtypes.rbool;
import static omero.rtypes.rint;
import static omero.rtypes.rlist;
import static omero.rtypes.rlong;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RTime;
import omero.RType;

/**
 * Helper subclass of {@link omero.sys.Parameters} for simplifying method
 * parameter creation.
 * 
 * As of 4.0, this takes over for PojoOptions. See ticket:67.
 */
public class ParametersI extends omero.sys.Parameters {

    /**
     * Default constructor creates the {@link #map} instance to prevent later
     * {@link NullPointerException}s. To save memory, it is possible to pass
     * null to {@link ParametersI#ParametersI(Map)}.
     */
    public ParametersI() {
        this.map = new HashMap<String, RType>();
    }

    /**
     * Uses (and does not copy) the given {@code Map<String, RType>} as the
     * named parameter store in this instance. Be careful if either null is
     * passed or if this instance is being used in a multi-threaded environment.
     * No synchronization takes place.
     * @param map the named parameter store to use
     */
    public ParametersI(Map<String, RType> map) {
        this.map = map;
    }

    // ~ Parameters.theFilter.limit & offset
    // =========================================================================

    /**
     * Nulls both the {@link Filter#limit} and {@link Filter#offset} values.
     * @return this instance, for method chaining
     */
    public Parameters noPage() {
        if (this.theFilter != null) {
            this.theFilter.limit = null;
            this.theFilter.offset = null;
        }
        return this;
    }

    /**
     * Sets both the {@link Filter#limit} and {@link Filter#offset} values by
     * wrapping the arguments in {@link RInt} and passing the values to
     * {@link #page(RInt, RInt)}
     * @param offset the offset (to start from)
     * @param limit the limit (maximum to return)
     * @return this instance, for method chaining
     */
    public ParametersI page(int offset, int limit) {
        return this.page(rint(offset), rint(limit));
    }

    /**
     * Creates a {@link Filter} if necessary and sets both {@link Filter#limit}
     * and {@link Filter#offset}.
     * @param offset the offset (to start from)
     * @param limit the limit (maximum to return)
     * @return this instance, for method chaining
     */
    public ParametersI page(RInt offset, RInt limit) {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.limit = limit;
        this.theFilter.offset = offset;
        return this;
    }

    /**
     * Returns <code>true</code> if the filter contains a <code>limit</code>
     * <em>OR</em> a <code>offset</code>, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isPagination() {
        if (this.theFilter != null) {
            return (null != this.theFilter.limit)
                    || (null != this.theFilter.offset);
        }
        return false;
    }

    /**
     * Returns the value of the <code>offset</code> parameter.
     * 
     * @return See above.
     */
    public RInt getOffset() {
        if (this.theFilter != null) {
            return this.theFilter.offset;
        }
        return null;
    }

    /**
     * Returns the value of the <code>limit</code> parameter.
     * 
     * @return See above.
     */
    public RInt getLimit() {
        if (this.theFilter != null) {
            return this.theFilter.limit;
        }
        return null;
    }
    
    public ParametersI unique() {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.unique = rbool(true);
        return this;
    }
    
    public ParametersI noUnique() {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.unique = rbool(false);
        return this;
    }
    
    public omero.RBool getUnique() {
        if (this.theFilter != null) {
            return this.theFilter.unique;
        }
        return null;
    }

    // ~ Parameters.theFilter.ownerId & groupId
    // =========================================================================

    /**
     * Sets the value of the <code>experimenter</code> parameter.
     * 
     * @param i
     *            The Id of the experimenter.
     * @return Returns the current object.
     */
    public ParametersI exp(RLong i) {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.ownerId = i;
        return this;
    }

    /**
     * Removes the <code>experimenter</code> parameter from the map.
     * 
     * @return Returns the current object.
     */
    public ParametersI allExps() {
        if (this.theFilter != null) {
            this.theFilter.ownerId = null;
        }
        return this;
    }

    /**
     * Returns <code>true</code> if the filter contains and <code>ownerId</code>
     * parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isExperimenter() {
        if (this.theFilter != null) {
            return null != this.theFilter.ownerId;
        }
        return false;
    }

    /**
     * Returns the value of the <code>experimenter</code> parameter.
     * 
     * @return See above.
     */
    public RLong getExperimenter() {
        if (this.theFilter != null) {
            return this.theFilter.ownerId;
        }
        return null;
    }

    /**
     * Sets the value of the <code>group</code> parameter.
     * 
     * @param i
     *            The value to set.
     * @return See above.
     */
    public ParametersI grp(RLong i) {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.groupId = i;
        return this;
    }

    /**
     * Removes the <code>group</code> parameter from the map.
     * 
     * @return Returns the current object.
     */
    public ParametersI allGrps() {
        if (this.theFilter != null) {
            this.theFilter.groupId = null;
        }
        return this;
    }

    /**
     * Returns <code>true</code> if the filter contains an <code>groupId</code>,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isGroup() {
        if (this.theFilter != null) {
            return null != this.theFilter.groupId;
        }
        return false;
    }

    /**
     * Returns the value of the <code>group</code> parameter.
     * 
     * @return See above.
     */
    public RLong getGroup() {
        if (this.theFilter != null) {
            return this.theFilter.groupId;
        }
        return null;
    }

    // ~ Parameters.theFilter.startTime, endTime
    // =========================================================================

    /**
     * Sets the value of the <code>start time</code> parameter.
     * 
     * @param startTime
     *            The time to set.
     * @return Returns the current object.
     */
    public ParametersI startTime(RTime startTime) {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.startTime = startTime;
        return this;
    }

    /**
     * Sets the value of the <code>end time</code> parameter.
     * 
     * @param endTime
     *            The time to set.
     * @return Returns the current object.
     */
    public ParametersI endTime(RTime endTime) {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.endTime = endTime;
        return this;
    }

    /**
     * Removes the time parameters from the map.
     * 
     * @return Returns the current object.
     */
    public ParametersI allTimes() {
        if (this.theFilter != null) {
            this.theFilter.startTime = null;
            this.theFilter.endTime = null;
        }
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the <code>start time</code>
     * parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isStartTime() {
        if (this.theFilter != null) {
            return null != this.theFilter.startTime;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the map contains the <code>end time</code>
     * parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isEndTime() {
        if (this.theFilter != null) {
            return null != this.theFilter.endTime;
        }
        return false;
    }

    /**
     * Returns the value of the <code>start time</code> parameter.
     * 
     * @return See above.
     */
    public RTime getStartTime() {
        if (this.theFilter != null) {
            return this.theFilter.startTime;
        }
        return null;
    }

    /**
     * Returns the value of the <code>end time</code> parameter.
     * 
     * @return See above.
     */
    public RTime getEndTime() {
        if (this.theFilter != null) {
            return this.theFilter.endTime;
        }
        return null;
    }

    // ~ Parameters.theOption.leaves, orphan, acquisitionData
    // =========================================================================

    /**
     * Sets the <code>leaf</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public ParametersI leaves() {
        if (this.theOptions == null) {
            this.theOptions = new Options();
        }
        this.theOptions.leaves = rbool(true);
        return this;
    }

    /**
     * Sets the <code>leaf</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public ParametersI noLeaves() {
        if (this.theOptions == null) {
            this.theOptions = new Options();
        }
        this.theOptions.leaves = rbool(false);
        return this;
    }

    public omero.RBool getLeaves() {
        if (this.theOptions != null) {
            return this.theOptions.leaves;
        }
        return null;
    }

    /**
     * Sets the <code>orphan</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public ParametersI orphan() {
        if (this.theOptions == null) {
            this.theOptions = new Options();
        }
        this.theOptions.orphan = rbool(true);
        return this;
    }

    /**
     * Sets the <code>orphan</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public ParametersI noOrphan() {
        if (this.theOptions == null) {
            this.theOptions = new Options();
        }
        this.theOptions.orphan = rbool(false);
        return this;
    }

    public omero.RBool getOrphan() {
        if (this.theOptions != null) {
            return this.theOptions.orphan;
        }
        return null;
    }

    /**
     * Sets the <code>acquisition data</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public ParametersI acquisitionData() {
        if (this.theOptions == null) {
            this.theOptions = new Options();
        }
        this.theOptions.acquisitionData = rbool(true);
        return this;
    }

    /**
     * Sets the <code>acquisition data</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public ParametersI noAcquisitionData() {
        if (this.theOptions == null) {
            this.theOptions = new Options();
        }
        this.theOptions.acquisitionData = rbool(false);
        return this;
    }

    public omero.RBool getAcquisitionData() {
        if (this.theOptions != null) {
            return this.theOptions.acquisitionData;
        }
        return null;
    }

    // ~ Parameters.map
    // =========================================================================

    public ParametersI add(String name, RType r) {
        this.map.put(name, r);
        return this;
    }

    public ParametersI addId(long id) {
        add(ome.parameters.Parameters.ID, rlong(id));
        return this;
    }

    public ParametersI addId(RLong id) {
        add(ome.parameters.Parameters.ID, id);
        return this;
    }

    public ParametersI addIds(Collection<Long> longs) {
        addLongs(ome.parameters.Parameters.IDS, longs);
        return this;
    }

    public ParametersI addLong(String name, long l) {
        add(name, rlong(l));
        return this;
    }

    public ParametersI addLong(String name, RLong l) {
        add(name, l);
        return this;
    }

    public ParametersI addLongs(String name, Collection<Long> longs) {
        RList rlongs = rlist();
        for (Long l : longs) {
            rlongs.add(rlong(l));
        }
        this.map.put(name, rlongs);
        return this;
    }

    // ~ Deprecated
    // =========================================================================

    /**
     * Pre-4.0, pojoOptions.map() was a common idiom for passing the {@link Map
     * <String, RType} into methods, to keep those uses valid, the
     * {@link #map()} method is defined.
     * @deprecated use {@link #map()} instead, to be removed in 5.3
     */
    @Deprecated
    public ParametersI map() {
        return this;
    }

}
