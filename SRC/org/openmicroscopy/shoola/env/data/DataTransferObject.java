package org.openmicroscopy.shoola.env.data;

// Java imports
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/** 
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

abstract class DataTransferObject {
    
    static final Map    serializeTypes;
    static {
        serializeTypes = new HashMap();
        serializeTypes.put(Integer.class, null);
        serializeTypes.put(String.class, null);
        serializeTypes.put(Boolean.class, null);
        serializeTypes.put(Double.class, null);
        serializeTypes.put(Float.class, null);
        serializeTypes.put(Long.class, null);
        serializeTypes.put(boolean.class, null);
        serializeTypes.put(int.class, null);
        serializeTypes.put(double.class, null);
        serializeTypes.put(float.class, null);
        serializeTypes.put(long.class, null);
        serializeTypes.put(DataTransferObject.class, null); // our type
    }
    
    Map serialize() {
        Map map = new Hashtable();
        Class c = this.getClass(), type;
        Field[] fields = c.getDeclaredFields();
        try {
            for (int i=0; i<fields.length; ++i) {
                type = fields[i].getType();
                if (type.isArray()) {
                    verifyType(type.getComponentType());
                    Vector  v = new Vector();
                    Object  array = fields[i].get(this);
                    int     length = Array.getLength(array);
                    for(int k=0; k<length; ++k) 
                        v.add(serializeField(Array.get(array, k), map));
                } else {
                    serializeField(fields[i], map);
                }  
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }
    
    private void serializeField(Field field, Map map) {
        field.setAccessible(true);
        Class type = field.getType();
        verifyType(type);
        try {
            if (type==DataTransferObject.class) {
                DataTransferObject dto = (DataTransferObject)field.get(this);
                map.put(field.getName(), dto.serialize());
            } else {
                map.put(field.getName(), field.get(this)); 
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Object serializeField(Object field, Map map) {
        /*field.setAccessible(true);
        Class type = field.getType();
        verifyType(type);
        if (type==DataTransferObject.class) {
            DataTransferObject dto = (DataTransferObject)field.get(this);
            map.put(field.getName(), dto.serialize());
        } else {
            map.put(field.getName(), field.get(this)); 
        }
         */
        return null;
    }
    
/* deserialize the result of the XML-RPC call
 * 
 * @param   output   XML-RPC result
 */
    void deserialize(Object output) {
    }
   
    private void verifyType(Class type) {
        if( ! serializeTypes.containsKey(type) )
                throw new RuntimeException("unsupported type"+type);
    }
    

}
