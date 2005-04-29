/*
 * Created on Feb 12, 2005
 */
package org.ome.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * storage mechanism for an RDF graph of LSID-referenced objects. Currently
 * stores Java Object Primitives and other LSObjects. // FIXME
 * 
 * @author josh
 */
public abstract class AbstractLSObject implements Serializable, LSObject {

    protected LSID lsid;

    protected Map currentValues = new HashMap();

    /** represents a diff to currentValues */
    protected Map oldValues = new HashMap();

    protected boolean clean = true;

    /** creates an LSObject with a given lsid. The LSID is immutable. */
    public AbstractLSObject(LSID lsid) {
        this.lsid = lsid;
    }

    /**
     * copy constructor. Each put uses the accessors in the subclasses for
     * validation
     */
    public AbstractLSObject(LSObject lsObj) {
        this(lsObj.getLSID());

        Map map = lsObj.getMap();
        if (null != map) {
            for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                this.put(key, map.get(key));
            }
        }
        this.save();
    }

    public boolean isOwner(String test){ 
        LSObject owner = (LSObject) this.get(Vocabulary.owner);
        
        if (null==owner){
            return false;
        }
        
        /* TODO test==null ==> false; should it be true if owner==null?? */
        if (owner.getLSID().toString().equals(Vocabulary.NS+test)){ 
            return true;
        }
        
        return false;
        
    }
    
    //	 FIXME lsid keys and runtime checks on object be sure to also change other
    // cast classes here
    public boolean put(String predicate, Object object) {
        if (object instanceof AbstractLSObject || object instanceof Float
                || object instanceof Double || object instanceof String
                || object instanceof Integer || object instanceof List) {
            currentValues.put(predicate, object);
            return true;
        }
        return false;
    }

    public Object get(String predicate) {
        return currentValues.get(predicate);
    }

    public LSID getLSID() {
        return lsid;
    }

    public Map getMap() {
        return currentValues;
    }

    public boolean save() {
        oldValues = new HashMap();
        clean = true;
        return true;
    }

    public boolean reset() {
        for (Iterator iter = oldValues.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            this.put(key, oldValues.get(key));
            oldValues.remove(key);
        }
        clean = true;
        return true;
    }

    public String toString() {
        return toNTriples();
    }

    public String toNTriples() {
        StringBuffer sb = new StringBuffer();
        String subj = "<" + lsid + "> ";

        for (Iterator iter = currentValues.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String pred = "<" + key + "> ";

            Object o = currentValues.get(key);
            if (null != o) {
                if (o instanceof LSObject) {
                    sb.append(subj);
                    sb.append(pred);
                    sb.append("<");
                    sb.append(((LSObject) o).getLSID().getURI());
                    sb.append("> . \n");
                } else if (o instanceof List) {
                    for (Iterator iter2 = ((List) o).iterator(); iter2
                            .hasNext();) {
                        Object element = iter2.next();
                        sb.append(subj);
                        sb.append(pred);
                        sb.append("<");
                        if (element instanceof LSObject){
                            sb.append(((LSObject)element).getLSID().getURI());
                        } else {
                            sb.append(element.toString());
                        }
                        sb.append("> . \n");
                    }
                } else {
                    sb.append(subj);
                    sb.append(pred);
                    sb.append("<");
                    sb.append(o.toString());
                    sb.append("> . \n");
                }
            } else {
                System.err.println("There should be no null objects in map:  "
                        + lsid + " & " + key); // FIXME
            }
        }

        if (sb.length()<1) {
            sb.append(subj);
        }
        return sb.toString();

    }

    //TODO should this show new or old values (will need to reimplement to show
    // new)
    public String toN3() {
        StringBuffer sb = new StringBuffer();

        sb.append("<");
        sb.append(lsid);
        sb.append(">");
        sb.append("\n");//TODO this bothers also missing <> and ""

        for (Iterator iter = currentValues.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            StringBuffer value = new StringBuffer();
            String space = "    ";

            Object o = currentValues.get(key);
            if (null != o) {
                if (o instanceof LSObject) {
                    value.append("<");
                    value.append(((LSObject) o).getLSID().getURI());
                    value.append(">");
                } else if (o instanceof List) {
                    for (Iterator iter2 = ((List) o).iterator(); iter2
                            .hasNext();) {
                        Object element = iter2.next();
                        value.append(element.toString());
                        if (iter2.hasNext()) {
                            value.append(",");
                            value.append(space);
                        }
                    }
                } else {
                    value.append(o.toString());
                }

                sb.append(space);
                sb.append("<");
                sb.append(key);
                sb.append(">");
                sb.append(space);
                sb.append(value.toString());
                sb.append(space);
                if (iter.hasNext()) {
                    sb.append(";");
                } else {
                    sb.append(".");
                }
                sb.append("\n");

                //TODD could add to out string "Predicate was ---> <old value>
            } else {
                System.err.println("There should be no null objects in map:  "
                        + lsid + " & " + key); // FIXME
            }
        }

        return sb.toString();
    }

}