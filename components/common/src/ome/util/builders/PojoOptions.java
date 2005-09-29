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
	//TODO Get these values from omero-common
    private static final String ANNOTATOR = "annotator";
    private static final String LEAF = "leaves";
    private static final String EXPERIMENTER = "experimenter";
    
    private Map options = new HashMap();

    public PojoOptions(){
    	this.leaves();
    }
    
    public PojoOptions(Map map){
    	String[] s = new String[]{ANNOTATOR,LEAF,EXPERIMENTER};
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
    	options.put(LEAF,Boolean.FALSE);
    	return this;
    }

    public boolean isLeaves(){
   		return ((Boolean) options.get(LEAF)).booleanValue();
    }
    
    /* ==============================
     * With / Without Annotations
     * ============================== */
    
    public PojoOptions noAnnotations(){
    	remove(ANNOTATOR);
    	return this;
    }
    
    public PojoOptions annotationsFor(Integer i){
    	options.put(ANNOTATOR,i);
    	return this;
    }
    
    public PojoOptions allAnnotations(){
    	options.put(ANNOTATOR,null);
    	return this;
    }

    public boolean isAnnotation(){
    	return options.containsKey(ANNOTATOR);
    }
    
    public Integer getAnnotator(){
    	return (Integer) options.get(ANNOTATOR);
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
    
    public Integer getExperimenter(){
    	return (Integer) options.get(EXPERIMENTER);
    }

    
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
