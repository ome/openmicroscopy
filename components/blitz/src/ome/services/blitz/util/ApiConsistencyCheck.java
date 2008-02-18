package ome.services.blitz.util;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.system.Principal;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omero.RString;
import omero.RType;
import omero.ServerError;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Checks all servant definitions (see:
 * ome/services/blitz-servantDefinitions.xml) to guarantee that the RMI and the
 * Blitz APIs match.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ApiConsistencyCheck implements BeanPostProcessor {

    /**
     * 
     */
    public Object postProcessAfterInitialization(Object arg0, String arg1)
            throws BeansException {

        if (arg0 instanceof ServantDefinition) {

            List<String> differences = new ArrayList<String>();

            ServantDefinition sd = (ServantDefinition) arg0;
            Class ops = sd.getOperationsClass();
            Class api = sd.getServiceClass();

            Method[] opsMethods = ops.getDeclaredMethods();
            Method[] apiMethods = api.getDeclaredMethods();

            Map<String, Method> opsMap = map(opsMethods);
            Map<String, Method> apiMap = map(apiMethods);

            for (String name : opsMap.keySet()) {
                if (!apiMap.containsKey(name)) {
                    differences.add("Extra method: " + name);
                }
                Method opsMethod = opsMap.get(name);
                List<Class<?>> excs = Arrays.asList(opsMethod
                        .getExceptionTypes());
                if (!excs.contains(ServerError.class)) {
                    differences.add("Missing ServerError: " + name);
                }
            }

            for (String name : apiMap.keySet()) {
                Method opsMethod = opsMap.get(name);
                if (opsMethod == null) {
                    differences.add("Missing method: " + name);
                    continue;
                }

                Class[] opsParams = opsMethod.getParameterTypes();
                Class[] apiParams = apiMap.get(name).getParameterTypes();

                // Blitz always has one more for the Ice.Current
                if (opsParams.length - 1 != apiParams.length) {
                    differences.add(String.format(
                            "Native Java method has %d parameters "
                                    + "while Blitz method has %d",
                            apiParams.length, opsParams.length));
                    continue;
                }
                for (int i = 0; i < apiParams.length; i++) {
                    Class apiType = apiParams[i];
                    Class opsType = opsParams[i];
                    if (!matches(apiType, opsType)) {
                        differences.add(String.format(
                                "Parameter type mismatch: %s & %s", apiType,
                                opsType));
                        continue;
                    }
                }

            }

            if (differences.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String difference : differences) {
                    sb.append(difference);
                    sb.append("\n");
                }
                throw new ApiConsistencyException(sb.toString(), apiMap, opsMap);
            }
        }

        return arg0;
    }

    /**
     * No-op
     */
    public Object postProcessBeforeInitialization(Object arg0, String arg1)
            throws BeansException {
        return arg0;
    }

    /**
     * Defines what Class types match.
     * 
     * @param apiType
     * @param opsType
     */
    public static boolean matches(Class apiType, Class opsType) {
        if (apiType == opsType) {
            return true;
        }

        if (apiType.equals(opsType)) {
            return true;
        }

        if (apiType.equals(Long.class) && opsType.equals(long.class)) {
            return true;
        }

        if (IObject.class.isAssignableFrom(apiType)
                && omero.model.IObject.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (apiType.isArray()
                && (opsType.isArray() || Collection.class
                        .isAssignableFrom(opsType))) {
            return true;
        }

        if (Collection.class.isAssignableFrom(apiType)
                && List.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (Permissions.class.isAssignableFrom(apiType)
                && omero.model.Permissions.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (String.class.isAssignableFrom(apiType)
                && RString.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (Integer.class.isAssignableFrom(apiType)
                && int.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (Class.class.isAssignableFrom(apiType)
                && String.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (Parameters.class.isAssignableFrom(apiType)
                && omero.sys.Parameters.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (Filter.class.isAssignableFrom(apiType)
                && omero.sys.Filter.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (Principal.class.isAssignableFrom(apiType)
                && omero.sys.Principal.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (PlaneDef.class.isAssignableFrom(apiType)
                && omero.romio.PlaneDef.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (CodomainMapContext.class.isAssignableFrom(apiType)
                && omero.romio.CodomainMapContext.class
                        .isAssignableFrom(opsType)) {
            return true;
        }

        if (Details.class.isAssignableFrom(apiType)
                && omero.model.Details.class.isAssignableFrom(opsType)) {
            return true;
        }

        if (RType.class.isAssignableFrom(opsType)) {
            if (Object.class.equals(apiType)
                    || Timestamp.class.isAssignableFrom(apiType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Throws a {@link RuntimeException} since if there are two methods with the
     * same name then there's really no way the comparison can continue.
     */
    private Map<String, Method> map(Method[] methods) {
        Map<String, Method> map = new HashMap<String, Method>();
        for (Method method : methods) {
            String name = method.getName();
            if (map.containsKey(name)) {
                throw new RuntimeException("Method " + name
                        + " contained multiple times in API.");
            }
            map.put(name, method);
        }
        return map;
    }
}

class ApiConsistencyException extends RuntimeException {

    public ApiConsistencyException(String msg, Map<String, Method> api,
            Map<String, Method> ops) {
        super(string(msg, api.values(), ops.values()));
    }

    private static String string(String msg, Collection<Method> api,
            Collection<Method> ops) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(msg);
        sb.append("\n");
        sb.append("Method mismatch between:\n");
        sb.append("native Java:");
        sb.append(api.toString());
        sb.append("\n");
        sb.append("and Blitz:");
        sb.append(ops.toString());
        return sb.toString();
    }
}
