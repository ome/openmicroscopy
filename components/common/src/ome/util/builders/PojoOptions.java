/*
 * ome.util.builders.PojoOptions
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.util.builders;

import java.util.HashMap;
import java.util.Map;

import ome.model.containers.Dataset;
import ome.model.core.Image;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * generates Maps for Pojo service calls. 
 * 
 * The server will make the same assumptions about missing keys
 * as it would about a null <code>option</code> instance.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Josh Moore&nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class PojoOptions
    
{
    public static final String FIELDS = "fields";
    public static final String COUNTS = "counts";
    public static final String LEAF = "leaves";
    public static final String EXPERIMENTER = "experimenter";
    public static final String GROUP = "group";
    
    private Map options = new HashMap();

    public PojoOptions(){
    	this.noLeaves().countsForUser()
        .countFields(new String[]{
                Dataset.ANNOTATIONS,
                Image.ANNOTATIONS,
                Image.CATEGORYLINKS});
    }
    
    /** builds a PojoOptions from a passed map. Empty maps and null maps have the same effect.
     * Further they <b>override</b> the defaults. For defaults, use {@see #PojoOptions() they null-arg constructor}.
     * @param map
     */
    public PojoOptions(Map map){ 
    	if (null != map){
            copy(map);
    	} else {
    	    copy(new PojoOptions().map()); 
        }
    }
    
    protected void copy(Map map){
        String[] s = new String[]{FIELDS,COUNTS,LEAF,EXPERIMENTER,GROUP};
        for (int i = 0; i < s.length; i++) {
            if (map.containsKey(s[i]))
                this.options.put(s[i], map.get(s[i]));
        }
    }

    /* ==============================
     * Containers with / without Imgs
     * ============================== */
    
    
    public PojoOptions leaves(){
    	options.put(LEAF,Boolean.TRUE);
    	return this;
    }
    
    public PojoOptions noLeaves(){
    	options.remove(LEAF);
    	return this;
    }

    public boolean isLeaves(){
   		return options.containsKey(LEAF);
    }
    
    /* ==============================
     * With / Without Counts
     * ============================== */
    
    public PojoOptions countFields(String[] fields)
    {
        options.put(FIELDS,fields);
        return this;
    }
    
    public String[] countFields(){
        return (String[]) options.get(FIELDS);
    }
    
    public boolean hasCountFields(){
        return options.containsKey(FIELDS) && 
            options.get(FIELDS) != null;
    }
    
    public PojoOptions noCounts(){
    	remove(COUNTS);
    	return this;
    }
    
    public PojoOptions countsFor(Integer i){
    	options.put(COUNTS,i);
    	return this;
    }

    public PojoOptions countsForUser(){
        options.put(COUNTS, Boolean.TRUE);
        return this;
    }
    
    public PojoOptions allCounts(){
    	options.put(COUNTS,null);
    	return this;
    }

    public boolean isCounts(){
    	return options.containsKey(COUNTS);
    }

    public boolean isCountsForUser(){
        return isCounts() && options.get(COUNTS) instanceof Boolean;
    }
    
    public boolean isAllCounts(){
    	return isCounts() && options.get(COUNTS) == null;
    }
    
    public Integer getCounts(){
    	return (Integer) options.get(COUNTS);
    }
    
    /* ==============================
     * Filtered by Experimenter
     * ============================== */
    
    public PojoOptions exp(Integer i){
    	options.put(EXPERIMENTER,i);
    	return this;
    }
    
    public PojoOptions allExps(){
    	remove(EXPERIMENTER);
    	return this;
    }
    
    public boolean isExperimenter(){
    	return options.containsKey(EXPERIMENTER);
    }
    
    public Integer getExperimenter(){
    	return (Integer) options.get(EXPERIMENTER);
    }

    /* ==============================
     * Filtered by Group
     * ============================== */

    /* FIXME : This is currently disabled. 
    public PojoOptions grp(Integer i){
        options.put(GROUP,i);
        return this;
    }
    
    public PojoOptions allGrps(){
        remove(GROUP);
        return this;
    }
    
    public boolean isGroup(){
        return options.containsKey(GROUP);
    }
    
    public Integer getGroup(){
        return (Integer) options.get(GROUP);
    }
    */
    
    /* ==============================
     * Helpers
     * ============================== */
    
    protected void remove(String key){
    	if (options.containsKey(key)){
    		options.remove(key);
    	}
    }

    public Map map(){
    	return options;
    }

    
}
