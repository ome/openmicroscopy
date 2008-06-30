/*
 * ome.util.builders.PojoOptions
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.builders;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * generates Maps for Pojo service calls.
 * 
 * The server will make the same assumptions about missing keys as it would
 * about a null <code>option</code> instance.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME2.2
 */
public class PojoOptions

{
    public static final String LEAF = "leaves";

    public static final String EXPERIMENTER = "experimenter";

    public static final String GROUP = "group";

    public static final String START_TIME = "startTime";

    public static final String END_TIME = "endTime";

    public static final String OFFSET = "offset";

    public static final String LIMIT = "limit";

    private final Map options = new HashMap();

    public PojoOptions() {
        this.noLeaves();
    }

    /**
     * builds a PojoOptions from a passed map. Empty maps and null maps have the
     * same effect. Further they <b>override</b> the defaults. For defaults,
     * use {@see #PojoOptions() they null-arg constructor}.
     * 
     * @param map
     */
    public PojoOptions(Map map) {
        if (null != map) {
            copy(map);
        } else {
            copy(new PojoOptions().map());
        }
    }

    protected void copy(Map map) {
        String[] s = new String[] { LEAF, EXPERIMENTER, GROUP, START_TIME,
                END_TIME };
        for (int i = 0; i < s.length; i++) {
            if (map.containsKey(s[i])) {
                this.options.put(s[i], map.get(s[i]));
            }
        }
    }

    /*
     * ============================== Containers with / without Imgs
     * ==============================
     */

    public PojoOptions leaves() {
        options.put(LEAF, Boolean.TRUE);
        return this;
    }

    public PojoOptions noLeaves() {
        options.remove(LEAF);
        return this;
    }

    public boolean isLeaves() {
        return options.containsKey(LEAF);
    }

    /*
     * ============================== Filtered by Experimenter
     * ==============================
     */

    public PojoOptions exp(Long i) {
        options.put(EXPERIMENTER, i);
        return this;
    }

    public PojoOptions allExps() {
        remove(EXPERIMENTER);
        return this;
    }

    public boolean isExperimenter() {
        return options.containsKey(EXPERIMENTER);
    }

    public Long getExperimenter() {
        return (Long) options.get(EXPERIMENTER);
    }

    /*
     * ============================== Filtered by Start/End Time
     * ==============================
     */

    public PojoOptions startTime(Timestamp startTime) {
        options.put(START_TIME, startTime);
        return this;
    }

    public PojoOptions endTime(Timestamp endTime) {
        options.put(END_TIME, endTime);
        return this;
    }

    public PojoOptions allTimes() {
        remove(START_TIME);
        remove(END_TIME);
        return this;
    }

    public boolean isStartTime() {
        return options.containsKey(START_TIME);
    }

    public boolean isEndTime() {
        return options.containsKey(END_TIME);
    }

    public Timestamp getStartTime() {
        return (Timestamp) options.get(START_TIME);
    }

    public Timestamp getEndTime() {
        return (Timestamp) options.get(END_TIME);
    }

    /*
     * ============================== Pagination ==============================
     */

    public PojoOptions paginate(int offset, int limit) {
        options.put(OFFSET, offset);
        options.put(LIMIT, limit);
        return this;
    }

    public PojoOptions noPagination() {
        remove(OFFSET);
        remove(LIMIT);
        return this;
    }

    public boolean isPagination() {
        return options.containsKey(OFFSET);
    }

    public Integer getOffset() {
        return (Integer) options.get(OFFSET);
    }

    public Integer getLimit() {
        return (Integer) options.get(LIMIT);
    }

    /*
     * ============================== Filtered by Group
     * ==============================
     */

    public PojoOptions grp(Long i) {
        options.put(GROUP, i);
        return this;
    }

    public PojoOptions allGrps() {
        remove(GROUP);
        return this;
    }

    public boolean isGroup() {
        return options.containsKey(GROUP);
    }

    public Long getGroup() {
        return (Long) options.get(GROUP);
    }

    /*
     * ============================== Helpers ==============================
     */

    protected void remove(String key) {
        if (options.containsKey(key)) {
            options.remove(key);
        }
    }

    public Map map() {
        return options;
    }

}
