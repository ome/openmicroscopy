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
import omero.RBool;
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
import omero.constants.POJOSTARTTIME;

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

    public static final String LEAF = POJOLEAVES.value;

    public static final String EXPERIMENTER = POJOEXPERIMENTER.value;

    public static final String GROUP = POJOGROUP.value;

    public static final String START_TIME = POJOSTARTTIME.value;

    public static final String END_TIME = POJOENDTIME.value;

    public static final String OFFSET = POJOOFFSET.value;

    public static final String LIMIT = POJOLIMIT.value;

    public PojoOptions() {
        this.map = new HashMap<String, RType>();
        this.noLeaves();
    }

    /**
     * builds a PojoOptions from a passed map. Empty maps and null maps have the
     * same effect. Further they <b>override</b> the defaults. For defaults,
     * use {@see #PojoOptions() they null-arg constructor}.
     * 
     * @param map
     */
    public PojoOptions(Map<String, RType> map) {
        this.map = new HashMap<String, RType>();
        if (null == map) {
            throw new ClientError("Illegal argument");
        }
        copy(map);
    }

    protected void copy(Map<String, RType> map) {
        String[] s = new String[] { LEAF, EXPERIMENTER, GROUP, START_TIME,
                END_TIME, OFFSET, LIMIT };
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

    public PojoOptions leaves() {
        map.put(LEAF, new RBool(Boolean.TRUE));
        return this;
    }

    public PojoOptions noLeaves() {
        this.map.remove(LEAF);
        return this;
    }

    public boolean isLeaves() {
        return this.map.containsKey(LEAF);
    }

    /*
     * ============================== Filtered by Experimenter
     * ==============================
     */

    public PojoOptions exp(RLong i) {
        this.map.put(EXPERIMENTER, i);
        return this;
    }

    public PojoOptions allExps() {
        remove(EXPERIMENTER);
        return this;
    }

    public boolean isExperimenter() {
        return this.map.containsKey(EXPERIMENTER);
    }

    public RLong getExperimenter() {
        return (RLong) this.map.get(EXPERIMENTER);
    }

    /*
     * ============================== Filtered by Start/End Time
     * ==============================
     */

    public PojoOptions startTime(RTime startTime) {
        this.map.put(START_TIME, startTime);
        return this;
    }

    public PojoOptions endTime(RTime endTime) {
        this.map.put(END_TIME, endTime);
        return this;
    }

    public PojoOptions allTimes() {
        remove(START_TIME);
        remove(END_TIME);
        return this;
    }

    public boolean isStartTime() {
        return this.map.containsKey(START_TIME);
    }

    public boolean isEndTime() {
        return this.map.containsKey(END_TIME);
    }

    public RTime getStartTime() {
        return (RTime) this.map.get(START_TIME);
    }

    public RTime getEndTime() {
        return (RTime) this.map.get(END_TIME);
    }

    /*
     * ============================== Pagination ==============================
     */

    public PojoOptions paginate(RInt offset, RInt limit) {
        this.map.put(OFFSET, offset);
        this.map.put(LIMIT, limit);
        return this;
    }

    public PojoOptions noPagination() {
        remove(OFFSET);
        remove(LIMIT);
        return this;
    }

    public boolean isPagination() {
        return this.map.containsKey(OFFSET);
    }

    public RInt getOffset() {
        return (RInt) this.map.get(OFFSET);
    }

    public RInt getLimit() {
        return (RInt) this.map.get(LIMIT);
    }

    /*
     * ============================== Filtered by Group
     * ==============================
     */

    public PojoOptions grp(RLong i) {
        this.map.put(GROUP, i);
        return this;
    }

    public PojoOptions allGrps() {
        remove(GROUP);
        return this;
    }

    public boolean isGroup() {
        return this.map.containsKey(GROUP);
    }

    public RLong getGroup() {
        return (RLong) this.map.get(GROUP);
    }

    /*
     * ============================== Helpers ==============================
     */

    protected void remove(String key) {
        if (this.map.containsKey(key)) {
            this.map.remove(key);
        }
    }

    public Map<String, RType> map() {
        return this.map;
    }

}
