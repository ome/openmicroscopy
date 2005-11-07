/*
 * Created on Jun 15, 2005
 */
package ome.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * @author josh
 * @deprecated
 * @DEV.TODO this needs to be refactored to use BeanUtils; don't use. * 
 */
public class ReflectionUtils extends TestCase {

    private static Log log = LogFactory.getLog(ReflectionUtils.class);

    public static void findFieldsOfClass(Class target, Object o, String path,
            Log log, Set done) {
        if (null == path||path.equals(""))
            path = "\nthis";
        if (null==done)
            done = new HashSet();
        if (done.contains(o))
            return;
        done.add(o);
        
        if (target.isInstance(o)) {
            log.info(path + ";" + "\n----------------------\n" + o.toString()+" < "+o.getClass());
        } else if (o instanceof Set){
            for (Iterator it = ((Set)o).iterator(); it.hasNext();) {
                Object element = (Object) it.next();
                findFieldsOfClass(target,element,path,log,done);
            }
        } else {
            Method[] accessors = getGettersAndSetters(o);
            log.debug(accessors);
            for (int i = 0; i < accessors.length; i++) {
                Method method = accessors[i];
                if (method.getName().startsWith("get")) {
                    log.debug("Trying "+method);
                    Object obj = invokeGetter(o, method);
                    if (null != obj) {
                        findFieldsOfClass(target, obj, path + ".\n"
                                + method.getName() + "()", log, done);
                    }
                }
            }
        }
    }

    public static Method[] getGettersAndSetters(Object obj) {
        Method[] methods, superMethods = null;
        methods = obj.getClass().getDeclaredMethods();
        Package pkg = obj.getClass().getPackage();
        if (null != pkg
                && pkg.toString().indexOf("ome.model2") > -1) {//FIXME not valid
            superMethods = obj.getClass().getSuperclass().getDeclaredMethods();
        }
        List goodMethods = checkGettersAndSetters(methods);
        goodMethods.addAll(checkGettersAndSetters(superMethods));
        return (Method[]) goodMethods.toArray(new Method[goodMethods.size()]);
    }

    static List checkGettersAndSetters(Method[] methods) {
        List goodMethods = new ArrayList();

        if (null == methods)
            return goodMethods;

        for (int i = 0; i < methods.length; i++) {
            boolean ok = true;
            Method method = methods[i];
            int mod = method.getModifiers();
            if (!Modifier.isPublic(mod) || Modifier.isStatic(mod)) {
                ok = false;
            }

            if (method.getName().startsWith("get")) {
                if (0 != method.getParameterTypes().length) {
                    ok = false;
                }
            } else if (method.getName().startsWith("set")) {
                // No constaints yet on setters.
            } else {
                ok = false;
            }

            if (ok)
                goodMethods.add(method);
        }
        return goodMethods;
    }

    public static Method getSetterForGetter(Method[] methods, Method getter) {
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().startsWith("set")) {
                if (method.getName().substring(1).equals(
                        getter.getName().substring(1))) {
                    return method;
                }
            }
        }
        return null;
    }

    /** call getter and return object. 
     * @DEV.TODO there maybe be cases where an exception is ok
     * @param object on which to call getter
     * @param getter method for some field
     * @return value stored in field
     */
    public static Object invokeGetter(Object target, Method getter) {
        RuntimeException re = new RuntimeException(
                "Error trying to get value: " + getter.toString());
        Object result = null;
        try {
            result = getter.invoke(target, new Object[] {});
        } catch (IllegalArgumentException e) {
            re.initCause(e);
            throw re;
        } catch (IllegalAccessException e) {
            re.initCause(e);
            throw re;
        } catch (InvocationTargetException e) {
            re.initCause(e);
            throw re;
        }
        return result;
    }

    public static void setToNull(Object obj, Method setter) {
        RuntimeException re = new RuntimeException(
                "Error trying to set to null: " + setter.toString());

        try {
            setter.invoke(obj, new Object[] { null });
        } catch (IllegalArgumentException e) {
            re.initCause(e);
            throw re;
        } catch (IllegalAccessException e) {
            re.initCause(e);
            throw re;
        } catch (InvocationTargetException e) {
            re.initCause(e);
            throw re;
        }
    }

}
