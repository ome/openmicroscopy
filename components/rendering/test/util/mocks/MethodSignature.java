/*
 * util.mocks.MethodSignature
 *
 *   Copyright 2006-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package util.mocks;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

/**
 * Helper class to represent a method signature in a {@link MockedCall}.
 * <p>
 * The method signature has to be defined with exactly the same class objects as
 * the original return type and parameter types &#151; use
 * <code>int.class</code>, <code>boolean.class</code>, etc., for primitive
 * types and <code>void.class</code> to specify a <code>void</code> return
 * type.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class MethodSignature {

    /** Public visibility flag. */
    public static final int PUBLIC = 0;

    /** Package visibility flag. */
    public static final int PACKAGE = 1;

    /** Protected visibility flag. */
    public static final int PROTECTED = 2;

    /** Private visibility flag. */
    public static final int PRIVATE = 3;

    /**
     * Maps each Java primitive type to the corresponding object wrapper type.
     */
    private static final Map primitiveTypes = new HashMap();
    static {
        primitiveTypes.put(boolean.class, Boolean.class);
        primitiveTypes.put(byte.class, Byte.class);
        primitiveTypes.put(char.class, Character.class);
        primitiveTypes.put(short.class, Short.class);
        primitiveTypes.put(int.class, Integer.class);
        primitiveTypes.put(long.class, Long.class);
        primitiveTypes.put(float.class, Float.class);
        primitiveTypes.put(double.class, Double.class);
    }

    /**
     * Holds the visibility flag for this method. The constructor makes sure
     * that the value is set to one of the static flags defined by this class.
     */
    private int visibility;

    /**
     * The method return type. The constructor makes sure that it is not
     * <code>null</code>.
     */
    private Class returnType;

    /**
     * The method name. The constructor makes sure that it has at least one
     * character.
     */
    private String name;

    /**
     * The type of each method parameter, in the same order as they were
     * declared. The constructor makes sure that this is either a <code>0</code>-length
     * array (if the method declares no parameters) or, if the length is bigger
     * than <code>0</code>, all elements are not <code>null</code>.
     */
    private Class[] paramTypes;

    /**
     * Creates a new instance to represent a given method signature.
     * 
     * @param visibility
     *            The method visibility. Must be one of the flags defined by
     *            this class.
     * @param returnType
     *            The method return type. Mustn't be <code>null</code>. Pass
     *            <code>void.class</code>, if the method returns no value.
     * @param name
     *            The method name. Must contain one character at least.
     * @param paramTypes
     *            The type of each method parameter, in the same order as they
     *            were declared. Must have elements and those mustn't be
     *            <code>null</code>. For methods that have no paramters, use
     *            the other constructor.
     */
    public MethodSignature(int visibility, Class returnType, String name,
            Class[] paramTypes) {
        if (visibility < PUBLIC || PRIVATE < visibility) {
            throw new IllegalArgumentException("Invalid visibility argument.");
        }
        if (returnType == null) {
            throw new IllegalArgumentException("Invalid returnType argument.");
        }
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Invalid name argument.");
        }
        if (paramTypes == null) {
            paramTypes = new Class[0];
        }
        for (int i = 0; i < paramTypes.length; ++i) {
            if (paramTypes[i] == null) {
                throw new IllegalArgumentException("Null paramType: " + i);
            }
        }
        this.visibility = visibility;
        this.returnType = returnType;
        this.name = name;
        this.paramTypes = paramTypes;
    }

    /**
     * Creates a new instance to represent a given method signature. This
     * constructor has to be used for methods that declare no parameter.
     * 
     * @param visibility
     *            The method visibility. Must be one of the flags defined by
     *            this class.
     * @param returnType
     *            The method return type. Mustn't be <code>null</code>. Pass
     *            <code>void.class</code>, if the method returns no value.
     * @param name
     *            The method name. Must contain one character at least.
     */
    public MethodSignature(int visibility, Class returnType, String name) {
        this(visibility, returnType, name, null); // No-params method.
    }

    /**
     * Returns the method name.
     * 
     * @return See above.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of parameters declared by the method. Returns
     * <code>0</code> for a no-params method, that is, if
     * {@link #hasParameters()} returns <code>false</code>.
     * 
     * @return See above.
     */
    public int numberOfParameters() {
        return paramTypes.length;
    }

    /**
     * Tells whether or not the method returns a value.
     * 
     * @return <code>true</code> if the method return type is
     *         <code>void</code>, <code>false</code> otherwise.
     */
    public boolean isReturnTypeVoid() {
        return void.class == returnType;
    }

    /**
     * Tells whether or not the type of the specified argument is a valid return
     * type. Primitive types are matched against the corresponding wrapper type
     * and, in this case, a <code>null</code> argument will never match.
     * Non-primitive types are matched against the argument class and, in this
     * case, a <code>null</code> argument will always match. This method will
     * always return <code>false</code> in the case of a <code>void</code>
     * return type.
     * 
     * @param retVal
     *            The value whose type we want to test.
     * @return <code>true</code> if <code>retVal</code> is compatible with
     *         the the return type of this method signature, <code>false</code>
     *         otherwise.
     */
    public boolean isValidReturnValue(Object retVal) {
        if (isReturnTypeVoid()) {
            return false;
        }
        return matches(retVal, returnType);
    }

    /**
     * Tells whether or not the method declares parmeters.
     * 
     * @return <code>true</code> if the method declares parmeters,
     *         <code>false</code> otherwise.
     */
    public boolean hasParameters() {
        return paramTypes.length != 0;
    }

    /**
     * Tells whether or not the type of the specified argument is a valid type
     * for the <code>argIndex</code> method parameter. Primitive types are
     * matched against the corresponding wrapper type and, in this case, a
     * <code>null</code> argument will never match. Non-primitive types are
     * matched against the argument class and, in this case, a <code>null</code>
     * argument will always match.
     * 
     * @param arg
     *            The value whose type we want to test.
     * @param argIndex
     *            The index of the method parameter. Pass <code>0</code> for
     *            the first parameter, <code>1</code> for the second, and so
     *            on.
     * @return <code>true</code> if <code>arg</code> is compatible with the
     *         the type of the <code>argIndex</code> method parameter in this
     *         method signature, <code>false</code> otherwise.
     */
    public boolean isValidArgument(Object arg, int argIndex) {
        if (argIndex < 0 || paramTypes.length < argIndex) {
            return false;
        }
        return matches(arg, paramTypes[argIndex]);
    }

    /**
     * Tells whether or not this object is the same as the specified argument.
     * 
     * @param methodSignature
     *            The object to compare.
     * @return <code>true</code> if this object and
     *         <code>methodSignature</code> hold the same state,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object methodSignature) {
        boolean b = methodSignature != null && methodSignature.getClass() == MethodSignature.class;
        if (b) {
            MethodSignature ms = (MethodSignature) methodSignature;
            b = ms.visibility == visibility && ms.returnType == returnType
                    && ObjectUtils.equals(name, ms.name) && ms.paramTypes.length == paramTypes.length;
            if (b) {
                for (int i = 0; i < paramTypes.length; ++i) {
                    if (paramTypes[i] != ms.paramTypes[i]) {
                        b = false;
                        break;
                    }
                }
            }
        }
        return b;
    }

    /**
     * Tells whether or not the type of <code>value</code> matches the
     * specified <code>type</code>. Primitive types are matched against the
     * corresponding wrapper type and, in this case, a <code>null value</code>
     * will never match. Non-primitive types are matched against the class of
     * <code>value</code> and, in this case, a <code>null value</code> will
     * always match.
     * 
     * @param value
     *            The object whose type we want to match against
     *            <code>type</code>.
     * @param type
     *            The type to match against.
     * @return <code>true</code> for a successful match, <code>false</code>
     *         otherwise.
     */
    private boolean matches(Object value, Class type) {
        if (primitiveTypes.containsKey(type)) {
            if (value == null) {
                return false;
            }
            Class wrapperClass = (Class) primitiveTypes.get(type);
            return wrapperClass == value.getClass();
        }
        // Else:
        if (value == null) {
            return true;
        }
        return type.isAssignableFrom(value.getClass());
    }

}
