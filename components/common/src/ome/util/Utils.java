/*
 * ome.util
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

package ome.util;

//Java imports
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries
import com.caucho.burlap.io.BurlapOutput;
import com.caucho.hessian.io.HessianOutput;

//Application-internal dependencies


/** 
 * various tools needed throughout Omero. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * @DEV.TODO Grinder issues should be moved to test component to reduce deps.
 */
public class Utils {

    protected final static String CGLIB_IDENTIFIER = "$$EnhancerByCGLIB$$";
    
    /** 
     * finds the "true" class identified by a given Class object. This is 
     * necessary because of possibly proxied instances.
     * @param source Regular or CGLIB-based class.
     * @return the regular Java class.
     */
    public static Class trueClass(Class source)
    {
        String s = source.getName();
        if (s.contains(CGLIB_IDENTIFIER)) { // TODO any other test?
            try
            {
                return Class.forName(
                        s.substring(0,s.indexOf(CGLIB_IDENTIFIER)));
            } catch (ClassNotFoundException e)
            {
                throw new RuntimeException( /* TODO */
                        "Classname contains "+CGLIB_IDENTIFIER+
                        " but base class cannout be found.");
            }
        }
        return source;
    }
    
    /** instantiates an object using the trueClass.
     * 
     * @param source Regular or CGLIB-based class.
     * @return the regular Java instance.
     */
    public static Object trueInstance(Class source)
    {
        Class trueClass = trueClass(source);
        Object result;
        try
        {
            result = trueClass.newInstance();
        } catch (InstantiationException e)
        {
            throw new RuntimeException(
                    "Failed to instantiate "+trueClass,e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(
                    "Not allowed to create class:"+trueClass,e);
        }
        return result;
    }
    
    /** primarily used in Grinder to test the message
     * returning from the various web services 
     * @param obj
     * @return
     */
     public static int structureSize(Object obj) {
        int result = -1;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            HessianOutput out = new HessianOutput(os);

            out.writeObject(obj);
            result = os.size();
            os.close();
        } catch (IOException e) {
            result = -2;
        }
        return result;
    }
     
     /** primarly used n Grinder to serialize Shoola objects for comparison 
     * @throws IOException*/
     public static void writeXmlToFile(Object obj, String filename) throws IOException {
         OutputStream os = new FileOutputStream(filename);
         BurlapOutput out = new BurlapOutput(os);
         out.writeObject(obj);
         os.close();
     }

     /** primarily used in Grinder to discover what methods to call
      * 
      * @param clazz
      * @return
      */
    public static String[] getObjectVoidMethods(Class clazz){
        Set set = new HashSet();
        
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getReturnType().equals(Object.class)){
                if (method.getParameterTypes().length == 0){
                    set.add(method.getName());
                }
            }
            
        }
        
        return (String[]) set.toArray(new String[set.size()]);
    }

}
