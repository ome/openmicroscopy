/*
 * Created on May 12, 2005
 */
package org.openmicroscopy.omero.tests.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import pojos.DataObject;

import com.caucho.hessian.io.HessianOutput;


/**
 * @author josh
 */
public class Utils {

    /** only valid for DataObjects DOESNT HANDLE RECURSION BEWARE*/
    /*public static int fieldCount(Object obj) {
        int result = 0;

        if (null == obj){
            // result ++ ?? TODO
        }
        else if (obj instanceof Collection) {
            result += 1;
            for (Iterator i = ((Collection) obj).iterator(); i.hasNext();) {
                Object o = i.next();
                result += fieldCount(o);
            }
        } else if (obj instanceof String || obj instanceof Integer
                || obj instanceof Float || obj instanceof Double) {
            result += 1;
        } else if (obj instanceof DataObject ) {
            Field[] fields = null;
            try {
                fields = obj.getClass().getFields();
            } catch (NoClassDefFoundError e){ // This wasn't here. can remove. TODO
                result += 1;
            }
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                Object o = null;
                try {
                    o = f.get(obj);
                } catch (IllegalAccessError e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (null != o) {
                    result += fieldCount(o);
                }

            }
        } else {
            System.err.println("Unknown class: " + obj.getClass());
            result+=1;
        }

        return result;
    }
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
