/*
 * org.openmicroscopy.omero.util
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

package org.openmicroscopy.omero.util;

//Java imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries
import com.caucho.hessian.io.HessianOutput;

//Application-internal dependencies

//TODO
//import net.sf.acegisecurity.Authentication;
//import net.sf.acegisecurity.context.ContextHolder;
//import net.sf.acegisecurity.context.security.SecureContext;
//import net.sf.acegisecurity.context.security.SecureContextImpl;
//import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;

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
 */
public class Utils {

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
    
//    public static void setUserAuth(){
//        Authentication auth = 
//            new UsernamePasswordAuthenticationToken(
//                "Josh","Moore");
//        setAuth(auth);
//    }
//    
//    public static void setAdminAuth(){
//        Authentication auth = 
//            new UsernamePasswordAuthenticationToken(
//                "admin","admin");
//        setAuth(auth);
//    }
//    
//    public static void setAuth(Authentication auth){
//        SecureContext secureContext = new SecureContextImpl();
//        secureContext.setAuthentication(auth);
//        ContextHolder.setContext(secureContext);
//    }
}
