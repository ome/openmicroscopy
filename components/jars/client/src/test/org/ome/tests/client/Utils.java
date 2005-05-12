/*
 * Created on May 12, 2005
 */
package org.ome.tests.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import pojos.DataObject;

import com.caucho.hessian.io.HessianOutput;


/**
 * @author josh
 */
public class Utils {

    /** only valid for DataObjects */
    public static int fieldCount(Object obj) {
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

}
