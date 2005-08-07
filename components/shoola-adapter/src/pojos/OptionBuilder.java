/*
 * pojos.OptionBuilder
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

package pojos;

import java.util.HashMap;
import java.util.Map;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * generates Maps for service calls. 
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
public class OptionBuilder
    
{
	//TODO Get these values from omero-common
    private static final String ANNOTATOR = "Annotator";
    private static final String LEAF = "Leaves";
    private static final String EXPERIMENTER = "Experimenter";
    
    private Map options = new HashMap();

    public OptionBuilder(){
    	this.leaves();
    }

    /* ==============================
     * Containers with / without Imgs
     * ============================== */
    
    
    public OptionBuilder leaves(){
    	options.put(LEAF,Boolean.TRUE);
    	return this;
    }
    
    public OptionBuilder noLeaves(){
    	options.put(LEAF,Boolean.FALSE);
    	return this;
    }
    
    /* ==============================
     * With / Without Annotations
     * ============================== */
    
    public OptionBuilder noAnnotations(){
    	remove(ANNOTATOR);
    	return this;
    }
    
    public OptionBuilder annotationsFor(Integer i){
    	options.put(ANNOTATOR,i);
    	return this;
    }
    
    public OptionBuilder allAnnotations(){
    	options.put(ANNOTATOR,null);
    	return this;
    }

    /* ==============================
     * Filtered by Experimenter
     * ============================== */
    
    
    public OptionBuilder exp(Integer i){
    	options.put(EXPERIMENTER,i);
    	return this;
    }
    
    public OptionBuilder allExps(){
    	remove(EXPERIMENTER);
    	return this;
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
