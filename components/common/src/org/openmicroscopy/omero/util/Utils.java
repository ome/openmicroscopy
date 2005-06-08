/*
 * Created on May 12, 2005
 */
package org.openmicroscopy.omero.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.caucho.hessian.io.HessianOutput;

//TODO
//import net.sf.acegisecurity.Authentication;
//import net.sf.acegisecurity.context.ContextHolder;
//import net.sf.acegisecurity.context.security.SecureContext;
//import net.sf.acegisecurity.context.security.SecureContextImpl;
//import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;


/** provides tools for testing. 
 * @author josh
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
