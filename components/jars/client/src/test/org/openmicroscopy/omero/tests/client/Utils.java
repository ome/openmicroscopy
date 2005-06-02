/*
 * Created on May 12, 2005
 */
package org.openmicroscopy.omero.tests.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.caucho.hessian.io.HessianOutput;


/**
 * @author josh
 */
public class Utils {

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
