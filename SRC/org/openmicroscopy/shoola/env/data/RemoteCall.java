package org.openmicroscopy.shoola.env.data;

//Java import 
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/** 
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class RemoteCall {
    
    private String  procedureName, sessionRef;
    private Vector  args;
    private Object  output;
    private Class   outputType;
    static final Map    baseTypes; // cf XML-RPC base types + our type
    static {
        baseTypes = new HashMap();
        baseTypes.put(Integer.class, null);
        baseTypes.put(String.class, null);
        baseTypes.put(Boolean.class, null);
        baseTypes.put(Double.class, null);
        baseTypes.put(Hashtable.class, null);
        baseTypes.put(Vector.class, null);
        baseTypes.put(DataTransferObject.class, null); // our type
    }
    
    RemoteCall(String procedureName, String sessionRef) {
        this.procedureName = procedureName;
        this.sessionRef = sessionRef;
        args = new Vector();
    }
    
    void addParam(Object param) {
        // role marshall
        verifyType(param);
        if (param instanceof DataTransferObject) 
            param = ((DataTransferObject)param).serialize();
        args.add(param);
    }
    
    void setOutputType(Class type) {
        verifyType(type);
        outputType = type;
    }
    
    void setOutput(Object output) {
        // role unmarshall
        this.output = output;
        if(outputType==DataTransferObject.class) { 
            try {
                DataTransferObject  result = 
                    (DataTransferObject)outputType.newInstance();   
                result.deserialize(output);
                this.output = result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
/* Check */
    private void verifyType(Object x) {
        Class type = x instanceof Class ? (Class)x : x.getClass(); 
        if( ! baseTypes.containsKey(type) )
            throw new RuntimeException("unsupported type"+type);
    }
    
/* Returns the session reference
 * 
 * @return the above mentioned type
 */    
    String getSessionRef() {
        return sessionRef;
    }
/* Returns the procedureName
 * 
 * @return the above mentioned type
 */    
    String getProcedureName() {
        return procedureName;
    }
/* Returns the procedureName
 * 
 * @return the above mentioned type
 */ 
    Vector getParameters() {
        return args;
    }
    public static void main(String[] args) {
        String s = "test";
        //verifyType(s);
        //verifyType(s.getClass());
       // if (s.getClass() instanceof Object) System.out.println("Object");
        //if (s.getClass() instanceof Class) System.out.println("Class");
    }
    
}
