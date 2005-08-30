/*
 * ome.itests.ComparisonUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.itests;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import ome.util.ReflectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ClassificationData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.model.UserDetails;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class ComparisonUtils extends TestCase {

    private static Log log = LogFactory.getLog(ComparisonUtils.class);

    static boolean knownType(Object a){
        if (a instanceof ProjectData || 
                a instanceof DatasetData || 
                a instanceof ImageData|| 
                a instanceof CategoryGroupData||
                a instanceof CategoryData||
                a instanceof ClassificationData||
                a instanceof AnnotationData||
                a instanceof PixelsDescription||
                a instanceof UserDetails||
                a instanceof ProjectSummary||
                a instanceof DatasetSummaryLinked||
        		a instanceof ImageSummary)
            
            
            return true;
        return false;
    }
    
    public static String summary(Object a) {
        Map count = new HashMap();
        StringBuffer sb = new StringBuffer(summary(a, "\n", count));
        sb.append("\n-----------------------------\n");
        for (Iterator i = count.keySet().iterator(); i.hasNext();) {
            Class c = (Class) i.next();
            Set set = (Set) count.get(c);
            sb.append(set.size());
            sb.append("\t");
            sb.append(c);
            sb.append("\n");
        }
        return sb.toString();
        
    }

    public static String summary(Object a, String indent, Map map) {
        if (null == a)
            return indent+"Null!";
        String addTo = "  ";
        StringBuffer sb = new StringBuffer(200);
        sb.append(indent);
        sb.append(a.getClass());

        if (a instanceof Collection) {
            sb.append("  (");
            sb.append(((Collection) a).size());
            sb.append(")  ");
            for (Iterator it = ((Collection) a).iterator(); it.hasNext();) {
                Object o = it.next();
                sb.append(summary(o, indent+addTo, map));
            }
        } else if (a instanceof Map) {
            sb.append("  (");
            sb.append(((Map) a).keySet().size());
            sb.append(")  ");
            for (Iterator it = ((Map) a).keySet().iterator(); it.hasNext();) {
                Object key = (Object) it.next();
                sb.append(summary(key, indent+addTo+"K",map));
                sb.append(summary(((Map) a).get(key), indent+addTo+"V",map));
            }
        } else if (knownType(a)){
            if (!map.containsKey(a.getClass()))
                map.put(a.getClass(),new HashSet());
            ((Set)map.get(a.getClass())).add(a);
            
            sb.append("  ["+a.toString()+"]");
            Method[] methods = ReflectionUtils.getGettersAndSetters(a);
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().startsWith("get")) {
                    Object o = ReflectionUtils.invokeGetter(a, method);
                    sb.append(summary(o,indent+addTo,map));
                }

            }
        }
        return sb.toString();
    }

    /** dispatcher for all other types */
    public static boolean compare(Object a, Object b) {
        if (null == a && null == b)
            return true;
        if (null == a || null == b)
            return false;
        try {
            if (a instanceof Set)
                return compareSet((Set) a, (Set) b);
            //if (a instanceof List) return compareList((List)a,(List)b);
            if (a instanceof Map)
                return compareMap((Map) a, (Map) b);

            if (knownType(a)){
                return compareDataObject(a,b);
            }

            if (log.isDebugEnabled()) {
                log.debug("Falling back to equals(): don't know type "
                        + a.getClass());
            }

            return a.equals(b);

        } catch (ClassCastException cce) {
            handle(cce, a, b);
        }

        return false;
    }

    public static boolean compareSet(Set a, Set b) {
        if (null == a && null == b)
            return true;
        if (null == a || null == b)
            return false;
        if (a.size() != b.size())
            return false;
        return allAgainstAll(a, b);
    }

    public static boolean compareMap(Map a, Map b) {
        if (null == a && null == b)
            return true;
        if (null == a || null == b)
            return false;
        if (compareSet(a.keySet(), b.keySet()))
            return false;
        for (Iterator i = a.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            if (!compare(a.get(key), b.get(key)))
                return false;
        }
        return true;
    }

    /** we know that we have one of our classes */
    public static boolean compareDataObject(Object a, Object b) {
        if (null == a && null == b)
            return true;
        if (null == a || null == b)
            return false;
        Method[] methods = ReflectionUtils.getGettersAndSetters(a);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().startsWith("get")) {
                Object a_obj = ReflectionUtils.invokeGetter(a, method);
                Object b_obj = ReflectionUtils.invokeGetter(b, method);
                if (!compare(a_obj, b_obj))
                    return false;
            }
        }
        return true;
    }

    // Reflection replaces this.        
    //        if (!a.getName().equals(b.getName()))
    //            return false;
    //        if (!a.getDescription().equals(b.getDescription()))
    //            return false;
    //        if (a.getID() != b.getID())
    //            return false;
    //        if (! a.getOwnerEmail().equals(b.getOwnerEmail())) return false;
    //        if (! compareDs(a.getDatasets(),b.getDatasets())) return false;

    private static void handle(ClassCastException cce, Object a, Object b) {
        if (log.isDebugEnabled()) {
            log.debug("COMPARISON FAILED: Classes don't match: " + a.getClass()
                    + " <> " + b.getClass());
        }
    }

    /** we know that they are not null here */
    private static boolean allAgainstAll(Set set1, Set set2) {
        int count = 0;
        for (Iterator i = set1.iterator(); i.hasNext();) {
            Object a = i.next();
            boolean found = false;
            for (Iterator j = set2.iterator(); j.hasNext();) {
                DataObject b = (DataObject) j.next();
                if (ComparisonUtils.compare(a, b)) {
                    found = true;
                    break;//TODO possibility of false positive (empties, etc.)
                }
            }
            if (found)
                count++;
        }
        return count == set1.size();

    }

    /************************************************************
     * ******************************************************** *
     * ******************************************************** */
    public void testComparions() {

        assertTrue("Two nulls are always equal", compare(null, null));
        assertTrue("A null and anything aren't", compare(null, new Object()));

        ImageData i1 = new ImageData();
        ImageData i2 = new ImageData();
        assertTrue("Empty things should be equal", compare(i1, i2));

        Set s1 = new HashSet();
        Set s2 = new HashSet();
        s1.add(i1);
        s2.add(i2);
        assertTrue("Sets with equal things should be equals", compare(s1, s2));
        s1.add(i2);
        s2.add(i1);
        assertTrue("even if we screw with set semantics", compare(s1, s2));

    }
}
