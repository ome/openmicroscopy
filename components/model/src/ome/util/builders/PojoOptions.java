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
	/** Field identifying the <code>leaf</code> parameter. */
    public static final String LEAF = "leaves";

    /** Field identifying the <code>Experimenter</code> parameter. */
    public static final String EXPERIMENTER = "experimenter";

    /** Field identifying the <code>group</code> parameter. */
    public static final String GROUP = "group";

    /** Field identifying the <code>start time</code> parameter. */
    public static final String START_TIME = "startTime";

    /** Field identifying the <code>end time</code> parameter. */
    public static final String END_TIME = "endTime";

    /** Field identifying the <code>offset</code> parameter. */
    public static final String OFFSET = "offset";

    /** Field identifying the <code>limit</code> parameter. */
    public static final String LIMIT = "limit";
    
    /** Field identifying the <code>orphan</code> parameter. */
    public static final String ORPHAN = "orphan";
    
    /** Field identifying the <code>acquisition data</code> parameter. */
    public static final String ACQUISITION_DATA = "acquisitionData";

    /** The map hosting the parameters. */
    private final Map<String, Object> options = new HashMap<String, Object>();

    /** Creates a default instance. */
    public PojoOptions() {
        this.noLeaves();
    }

    /**
     * Builds a PojoOptions from a passed map. Empty maps and null maps have the
     * same effect. Further they <b>override</b> the defaults. For defaults,
     * use {@link #PojoOptions()} they null-arg constructor.
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

    /**
     * Copies the passed map.
     * 
     * @param map The map to copy.
     */
    protected void copy(Map map) {
        String[] s = new String[] { LEAF, EXPERIMENTER, GROUP, START_TIME,
                END_TIME, OFFSET, LIMIT, ORPHAN, ACQUISITION_DATA };
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

    /**
     * Sets the <code>leaf</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions leaves() {
        options.put(LEAF, Boolean.TRUE);
        return this;
    }

    /**
     * Sets the <code>leaf</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions noLeaves() {
        options.remove(LEAF);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the <code>leaf</code>
     * parameter, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isLeaves() {
        return options.containsKey(LEAF);
    }

    /**
     * Sets the <code>orphan</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions orphan() {
    	 options.put(ORPHAN, Boolean.TRUE);
         return this;
    }
    
    /**
     * Sets the <code>orphan</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions noOrphan() {
        options.remove(ORPHAN);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the <code>orphan</code>
     * parameter, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isOrphan() {
        return options.containsKey(ORPHAN);
    }
    
    /*
     * =========================
     * This is only relevant when images are retrieved.
     */
    /**
     * Sets the <code>acquisition data</code> parameter to <code>true</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions acquisitionData() {
    	 options.put(ACQUISITION_DATA, Boolean.TRUE);
         return this;
    }
    
    /**
     * Sets the <code>acquisition data</code> parameter to <code>false</code>.
     * 
     * @return Returns the current object.
     */
    public PojoOptions noAcquisitionData() {
        options.remove(ACQUISITION_DATA);
        return this;
    }

    /**
     * Returns <code>true</code> if the map contains the 
     * <code>acquisition data</code> parameter, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isAcquisitionData() {
        return options.containsKey(ACQUISITION_DATA);
    }
    
    /*
     * ============================== 
     * Filtered by Experimenter
     * ==============================
     */

    /**
     * Sets the value of the <code>experimenter</code> parameter.
     * 
     * @param i The Id of the experimenter.
     * @return Returns the current object.
     */
    public PojoOptions exp(Long i) {
        options.put(EXPERIMENTER, i);
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
        return options.containsKey(EXPERIMENTER);
    }

    /**
     * Returns the value of the  <code>experimenter</code> parameter.
     * 
     * @return See above.
     */
    public Long getExperimenter() {
        return (Long) options.get(EXPERIMENTER);
    }

    /*
     * ============================== 
     * Filtered by Start/End Time
     * ==============================
     */

    /**
     * Sets the value of the <code>start time</code> parameter.
     * 
     * @param startTime The time to set.
     * @return Returns the current object.
     */
    public PojoOptions startTime(Timestamp startTime) {
        options.put(START_TIME, startTime);
        return this;
    }

    /**
     * Sets the value of the <code>end time</code> parameter.
     * 
     * @param endTime The time to set.
     * @return Returns the current object.
     */
    public PojoOptions endTime(Timestamp endTime) {
        options.put(END_TIME, endTime);
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
        return options.containsKey(START_TIME);
    }

    /**
     * Returns <code>true</code> if the map contains the 
     * <code>end time</code> parameter, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isEndTime() {
        return options.containsKey(END_TIME);
    }

    /**
     * Returns the value of the  <code>start time</code> parameter.
     * 
     * @return See above.
     */
    public Timestamp getStartTime() {
        return (Timestamp) options.get(START_TIME);
    }

    /**
     * Returns the value of the  <code>end time</code> parameter.
     * 
     * @return See above.
     */
    public Timestamp getEndTime() {
        return (Timestamp) options.get(END_TIME);
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
    public PojoOptions paginate(int offset, int limit) {
        options.put(OFFSET, offset);
        options.put(LIMIT, limit);
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
        return options.containsKey(OFFSET);
    }

    /**
     * Returns the value of the <code>offset</code> parameter.
     * 
     * @return See above.
     */
    public Integer getOffset() {
        return (Integer) options.get(OFFSET);
    }

    /**
     * Returns the value of the <code>limit</code> parameter.
     * 
     * @return See above.
     */
    public Integer getLimit() {
        return (Integer) options.get(LIMIT);
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
    public PojoOptions grp(Long i) {
        options.put(GROUP, i);
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
        return options.containsKey(GROUP);
    }

    /**
     * Returns the value of the <code>group</code> parameter.
     * 
     * @return See above.
     */
    public Long getGroup() {
        return (Long) options.get(GROUP);
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
        if (options.containsKey(key)) {
            options.remove(key);
        }
    }

    /**
     * Returns the map.
     * 
     * @return See above.
     */
    public Map map() {
        return options;
    }

}
