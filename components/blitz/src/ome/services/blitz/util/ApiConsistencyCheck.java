/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RObject;
import omero.RString;
import omero.RTime;
import omero.RType;
import omero.ServerError;
import omero.api._ServiceInterfaceOperations;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Checks all servant definitions (see:
 * ome/services/blitz-servantDefinitions.xml) to guarantee that the RMI and the
 * Blitz APIs match.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/894">ticket:894</a>
 */
public class ApiConsistencyCheck implements BeanPostProcessor {

    /**
     * 
     */
    public Object postProcessAfterInitialization(Object arg0, String arg1)
            throws BeansException {

        if (arg0 instanceof BlitzOnly) {
            return arg0; // EARLY EXIT!
        }

        if (arg0 instanceof Ice.TieBase) {
            Ice.TieBase tie = (Ice.TieBase) arg0;
            if (tie.ice_delegate() instanceof BlitzOnly) {
                return arg0; // EARLY EXIT!
            }
        }

        if (arg0 instanceof _ServiceInterfaceOperations) {
            final _ServiceInterfaceOperations sio = (_ServiceInterfaceOperations) arg0;
            final Class ops = si(sio.getClass());
            String opsName = ops.getName();
            String apiName = opsName.replaceAll("omero", "ome").replaceFirst(
                    "_", "").replace("Operations", "");
            Class api;
            try {
                api = Class.forName(apiName);
            } catch (ClassNotFoundException e) {
                throw new FatalBeanException("No known API interface: "
                        + apiName);
            }

            final List<String> differences = new ArrayList<String>();
            final Method[] opsMethods = ops.getDeclaredMethods();
            final Method[] apiMethods = api.getDeclaredMethods();

            final Map<String, Method> opsMap = map(opsMethods);
            final Map<String, Method> apiMap = map(apiMethods);
            compareMethodNames(opsMap, apiMap, differences);

            for (final String name : apiMap.keySet()) {

                final Method apiMethod = apiMap.get(name);
                final Method opsMethod = opsMap.get(name);

                if (opsMethod == null) {
                    differences.add("Missing method: " + name);
                    continue;
                }

                final Class[] opsParams = opsMethod.getParameterTypes();
                final Class[] apiParams = apiMethod.getParameterTypes();

                // Blitz always has one more for the Ice.Current
                if (opsParams.length - 2 != apiParams.length) {
                    differences.add(String.format(
                            "Native Java method has %d parameters "
                                    + "while Blitz method has %d",
                            apiParams.length, opsParams.length));
                    continue;
                }

                // Check actual values
                for (int i = 0; i < apiParams.length; i++) {
                    Class apiType = apiParams[i];
                    Class opsType = opsParams[i + 1];
                    if (!matches(apiType, opsType)) {
                        differences.add(String.format(
                                "Parameter type mismatch in %s: %s <> %s",
                                apiMethod, apiType, opsType));
                        continue;
                    }
                }

                // Now check the return type

                Class opsReturn = opsMethod.getReturnType();
                if (!void.class.equals(opsReturn)) {
                    differences.add("Async calls must return void: "
                            + opsMethod);
                }
                Class apiReturn = apiMethod.getReturnType();
                Class amdReturn = amdResponse(opsMethod);

                if (!matches(apiReturn, amdReturn)) {
                    differences.add(String.format(
                            "Return type mismatch in %s: %s <> %s", apiMethod,
                            apiReturn, amdReturn));
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

    private void compareMethodNames(final Map<String, Method> opsMap,
            final Map<String, Method> apiMap, final List<String> differences) {
        for (String name : opsMap.keySet()) {
            if (!apiMap.containsKey(name)) {
                differences.add("Extra method: " + name);
            }
            Method opsMethod = opsMap.get(name);
            List<Class<?>> excs = Arrays.asList(opsMethod.getExceptionTypes());
            if (!excs.contains(ServerError.class)) {
                differences.add("Missing ServerError: " + name);
            }
        }
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

        // Check for equality
        if (apiType == opsType || apiType.equals(opsType)) {
            return true;
        }

        final ApiCheck check = new ApiCheck(apiType, opsType);

        //
        // Blacklist. If any of these match, we return false.
        //

        if (check.matches(Integer.class, int.class)
                || check.matches(Long.class, long.class)
                || check.matches(Double.class, double.class)
                || check.matches(Float.class, float.class)) {
            return false;
        }

        //
        // Whitelist. If any one these match, we return true.
        //

        if (apiType.isArray()
                && (opsType.isArray() || Collection.class
                        .isAssignableFrom(opsType))) {
            return true;
        }

        if (check.matches(Collection.class, List.class)
                || check.matches(Map.class, Map.class)
                || check.matches(CodomainMapContext.class,
                        omero.romio.CodomainMapContext.class)
                || check.matches(Date.class, RTime.class)
                || check.matches(Details.class, omero.model.Details.class)
                || check.matches(Class.class, String.class)
                || check.matches(EventContext.class,
                        omero.sys.EventContext.class)
                || check.matches(Filter.class, omero.sys.Filter.class)
                || check.matches(Integer.class, RInt.class)
                || check.matches(IObject.class, omero.model.IObject.class)
                || check.matches(IObject.class, RObject.class)
                || check.matches(List.class, RList.class)
                || check.matches(Long.class, RLong.class)
                || check.matches(Parameters.class, omero.sys.Parameters.class)
                || check.matches(PlaneDef.class, omero.romio.PlaneDef.class)
                || check.matches(Permissions.class,
                        omero.model.Permissions.class)
                || check.matches(Principal.class, omero.sys.Principal.class)
                || check.matches(RGBBuffer.class, omero.romio.RGBBuffer.class)
                || check.matches(Roles.class, omero.sys.Roles.class)
                || check.matches(String.class, RString.class)) {
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
            name = name.replaceFirst("_async", "");
            if (map.containsKey(name)) {
                throw new RuntimeException("Method " + name
                        + " contained multiple times in API.");
            }
            map.put(name, method);
        }
        return map;
    }

    /**
     * Find the direct descendent of
     * {@link omero.api._ServiceInterfaceOperations}
     */
    private Class si(Class k) {

        if (!_ServiceInterfaceOperations.class.isAssignableFrom(k)) {
            return null;
        } else {

            Class sc = k.getSuperclass();
            if (sc != null) {
                sc = si(sc);
                if (sc != null) {
                    return sc;
                }
            }

            for (Class iface : k.getInterfaces()) {
                if (iface.equals(_ServiceInterfaceOperations.class)) {
                    return k;
                } else {
                    Class rv = si(iface);
                    if (rv != null) {
                        return rv;
                    }
                }
            }

        }
        return null;
    }

    /**
     * Checks the parameter type of the ice_response() method of the AMD
     * callback.
     */
    Class amdResponse(Method m) {
        // The first parameter is always the AMD class
        Class amd = m.getParameterTypes()[0];
        Method[] methods = amd.getMethods();
        Method response = null;
        for (Method method : methods) {
            if (method.getName().equals("ice_response")) {
                if (response != null) {
                    throw new RuntimeException(
                            "2 ice_response() methods found: " + m);
                } else {
                    response = method;
                }
            }
        }
        if (response == null) {
            throw new RuntimeException("No ice_response() method found: " + m);
        }
        Class[] responseTypes = response.getParameterTypes();
        if (responseTypes.length > 1) {
            throw new RuntimeException("More than one response type for " + m);
        } else if (responseTypes.length == 1) {
            return responseTypes[0];
        } else {
            return void.class;
        }

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

/**
 * Class to be used as a simple white or black list for checking consistency. To
 * perform a white list, create an {@link ApiCheck} with the start value of
 * false. Then
 * 
 */
class ApiCheck {

    final Class apiType;
    final Class opsType;

    public ApiCheck(Class api, Class ops) {
        this.apiType = api;
        this.opsType = ops;
    }

    boolean matches(Class apiTest, Class opsTest) {
        return apiTest.isAssignableFrom(apiType)
                && opsTest.isAssignableFrom(opsType);
    }
}
