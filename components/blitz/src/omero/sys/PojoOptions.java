/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.sys;

import java.util.HashMap;
import java.util.Map;

import omero.ClientError;
import omero.RInt;
import omero.RLong;
import omero.RTime;
import omero.RType;
import omero.constants.POJOENDTIME;
import omero.constants.POJOEXPERIMENTER;
import omero.constants.POJOGROUP;
import omero.constants.POJOLEAVES;
import omero.constants.POJOLIMIT;
import omero.constants.POJOOFFSET;
import omero.constants.POJOORPHAN;
import omero.constants.POJOSTARTTIME;
import static omero.rtypes.*;

/**
 * generates Maps for Pojo service calls.
 * 
 * The server will make the same assumptions about missing keys as it would
 * about a null <code>option</code> instance.
 * 
 * @author Jean-Marie Burel, j.burel at dundee.ac.uk
 * @author Josh Moore, josh at glencoesoftware.com
 * @since OME2.2
 */
public class PojoOptions extends Parameters {

	/** Field identifying the <code>leaf</code> parameter. */
    public static final String LEAF = POJOLEAVES.value;

    /** Field identifying the <code>Experimenter</code> parameter. */
    public static final String EXPERIMENTER = POJOEXPERIMENTER.value;

    /** Field identifying the <code>group</code> parameter. */
    public static final String GROUP = POJOGROUP.value;

    /** Field identifying the <code>start time</code> parameter. */
    public static final String START_TIME = POJOSTARTTIME.value;

    /** Field identifying the <code>end time</code> parameter. */
    public static final String END_TIME = POJOENDTIME.value;

    /** Field identifying the <code>offset</code> parameter. */
    public static final String OFFSET = POJOOFFSET.value;

    /** Field identifying the <code>limit</code> parameter. */
    public static final String LIMIT = POJOLIMIT.value;

    /** Field identifying the <code>orphan</code> parameter. */
    public static final String ORPHAN = POJOORPHAN.value;
    
    /** Creates a default instance. */
    public PojoOptions() {
        this.map = new HashMap<String, RType>();
        this.noLeaves();
    }

    /**
     * Builds a PojoOptions from a passed map. Empty maps and null maps have the
     * same effect. Further they <b>override</b> the defaults. For defaults,
     * use {@link #PojoOptions()} they null-arg constructor.
     * 
     * @param map The map to handle.
     */
    public PojoOptions(Map<String, RType> map) {
        this.map = new HashMap<String, RType>();
        if (null == map) {
            throw new ClientError("Illegal argument");
        }
        copy(map);
    }

    /**
     * Copies the passed map.
     * 
     * @param map The map to copy.
     */
    protected void copy(Map<String, RType> map) {
        String[] s = new String[] { LEAF, EXPERIMENTER, GROUP, START_TIME,
                END_TIME, OFFSET, LIMIT, ORPHAN };
        for (int i = 0; i < s.length; i++) {
            if (map.containsKey(s[i])) {
                this.map.put(s[i], map.get(s[i]));
            }
        }
    }

    /*
     * ============================== Containers with / without Imgs
     * ==============================
     */

    /**
     * Sets the <code>leaf</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions leaves() {
        map.put(LEAF, rbool(Boolean.TRUE));
        return this;
    }

    /**
     * Sets the <code>leaf</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions noLeaves() {
        this.map.remove(LEAF);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the <code>leaf</code>
     * parameter, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isLeaves() {
        return this.map.containsKey(LEAF);
    }
    
    /**
     * Sets the <code>orphan</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions orphan() {
    	 map.put(ORPHAN, rbool(Boolean.TRUE));
         return this;
    }
    
    /**
     * Sets the <code>orphan</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions noOrphan() {
        this.map.remove(ORPHAN);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the <code>orphan</code>
     * parameter, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isOrphan() {
        return this.map.containsKey(ORPHAN);
    }
    
    /*
     * ============================== Filtered by Experimenter
     * ==============================
     */

    /**
     * Sets the value of the <code>experimenter</code> parameter.
     * 
     * @param i The Id of the experimenter.
     * @return Returns the current object.
     */
    public PojoOptions exp(RLong i) {
        this.map.put(EXPERIMENTER, i);
        return this;
    }

    /**
     * Removes the <code>experimenter</code> parameter from the map.
     * 
     * @return Returns the current object.
     */
    public PojoOptions allExps() {
        remove(EXPERIMENTER);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the 
     * <code>experimenter</code> parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isExperimenter() {
        return this.map.containsKey(EXPERIMENTER);
    }

    /**
     * Returns the value of the  <code>experimenter</code> parameter.
     * 
     * @return See above.
     */
    public RLong getExperimenter() {
        return (RLong) this.map.get(EXPERIMENTER);
    }

    /*
     * ============================== Filtered by Start/End Time
     * ==============================
     */

    /**
     * Sets the value of the <code>start time</code> parameter.
     * 
     * @param startTime The time to set.
     * @return Returns the current object.
     */
    public PojoOptions startTime(RTime startTime) {
        this.map.put(START_TIME, startTime);
        return this;
    }

    /**
     * Sets the value of the <code>end time</code> parameter.
     * 
     * @param endTime The time to set.
     * @return Returns the current object.
     */
    public PojoOptions endTime(RTime endTime) {
        this.map.put(END_TIME, endTime);
        return this;
    }

    /**
     * Removes the time parameters from the map.
     * 
     * @return Returns the current object.
     */
    public PojoOptions allTimes() {
        remove(START_TIME);
        remove(END_TIME);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the 
     * <code>start time</code> parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isStartTime() {
        return this.map.containsKey(START_TIME);
    }

    /**
     * Returns <code>true</code> if the map contains the 
     * <code>end time</code> parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isEndTime() {
        return this.map.containsKey(END_TIME);
    }

    /**
     * Returns the value of the  <code>start time</code> parameter.
     * 
     * @return See above.
     */
    public RTime getStartTime() {
        return (RTime) this.map.get(START_TIME);
    }

    /**
     * Returns the value of the  <code>end time</code> parameter.
     * 
     * @return See above.
     */
    public RTime getEndTime() {
        return (RTime) this.map.get(END_TIME);
    }

    /*
     * ============================== Pagination ==============================
     */

    /**
     * Sets the value of the <code>offset</code> and <code>limit</code>
     * parameters.
     * 
     * @param offset The value of the <code>offset</code>.
     * @param limit The value of the <code>limit</code>.
     * @return See above.
     */
    public PojoOptions paginate(RInt offset, RInt limit) {
        this.map.put(OFFSET, offset);
        this.map.put(LIMIT, limit);
        return this;
    }

    /**
     * Removes the <code>offset</code> and <code>limit</code> parameters 
     * from the map.
     * 
     * @return Returns the current object.
     */
    public PojoOptions noPagination() {
        remove(OFFSET);
        remove(LIMIT);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the 
     * <code>offset</code> parameter, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isPagination() {
        return this.map.containsKey(OFFSET);
    }

    /**
     * Returns the value of the <code>offset</code> parameter.
     * 
     * @return See above.
     */
    public RInt getOffset() {
        return (RInt) this.map.get(OFFSET);
    }

    /**
     * Returns the value of the <code>limit</code> parameter.
     * 
     * @return See above.
     */
    public RInt getLimit() {
        return (RInt) this.map.get(LIMIT);
    }

    /*
     * ============================== Filtered by Group
     * ==============================
     */

    /**
     * Sets the value of the  <code>group</code> parameter.
     * 
     * @param i The value to set.
     * @return See above.
     */
    public PojoOptions grp(RLong i) {
        this.map.put(GROUP, i);
        return this;
    }

    /**
     * Removes the <code>group</code> parameter from the map.
     * 
     * @return Returns the current object.
     */
    public PojoOptions allGrps() {
        remove(GROUP);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the 
     * <code>group</code> parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isGroup() {
        return this.map.containsKey(GROUP);
    }

    /**
     * Returns the value of the <code>group</code> parameter.
     * 
     * @return See above.
     */
    public RLong getGroup() {
        return (RLong) this.map.get(GROUP);
    }

    /*
     * ============================== Helpers ==============================
     */

    /**
     * Removes the passed from the map.
     * 
     * @param key The key to remove.
     */
    protected void remove(String key) {
        if (this.map.containsKey(key)) {
            this.map.remove(key);
        }
    }

    /**
     * Returns the map.
     * 
     * @return See above.
     */
    public Map<String, RType> map() {
        return this.map;
    }

}
