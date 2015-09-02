/*
 * util.mocks.MockedCall
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package util.mocks;

/**
 * Represents a call on a mock object. A <code>MockedCall</code> is composed
 * by:
 * <ul>
 * <li>A {@link MethodSignature} to encapsultate the method declaration
 * information.</li>
 * <li>The actual arguments of the method call. There will be none of those if
 * the method declares no parameters.</li>
 * <li>The actual return value of the method call. There will be none if the
 * method has no return value.</li>
 * </ul>
 * <p>
 * The method signature has to be defined with exactly the same class objects as
 * the original return type and parameter types. However, this class requires
 * the actual arguments to be objects, so if you have a primitive type you must
 * convert it into the corresponding wrapping object before invoking
 * {@link #setArgs(Object[])} (i.e., <code>int</code> into
 * <code>Integer</code>, and so on). The same applies to the return value,
 * which is set by calling {@link #setResult(Object)}.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class MockedCall {

    /** This method's signature. */
    private MethodSignature signature;

    /**
     * The actual arguments, if any, of this method call. The array is indexed
     * in the same order as the parameters were declared. Zero-length for
     * no-params methods.
     */
    private Object[] args;

    /**
     * The result of this method call. Set to <code>null</code> if the return
     * type is <code>void</code>.
     */
    private Object result;

    /**
     * Any exception this method call should throw. Set to <code>null</code>
     * if there's no exception.
     */
    private Throwable exception;

    /**
     * Creates an object to represent the method call defined by the specified
     * method signature, arguments and return value. If there is any primitive
     * type, you have to wrap that value with the corresponding wrapper object
     * and, in this case, you may not pass <code>null</code>.
     * 
     * @param ms
     *            This method's signature.
     * @param args
     *            The actual arguments of this method call. The array must be
     *            indexed in the same order as the parameters were declared. You
     *            mustn't use this constructor if the method signature declares
     *            no parameters.
     * @param retVal
     *            The return value of this method call. You mustn't use this
     *            constructor if the method signature declares a
     *            <code>void</code> return type.
     */
    public MockedCall(MethodSignature ms, Object[] args, Object retVal) {
        // checkSignature(ms, hasParams, isVoid)
        checkSignature(ms, true, false);
        signature = ms;
        this.args = new Object[signature.numberOfParameters()];
        setArgs(args);
        setResult(retVal);
    }

    /**
     * Creates an object to represent the method call defined by the specified
     * method signature and arguments. Use this constructor if the method
     * declares a <code>void</code> return type. If there is any primitive
     * type, you have to wrap that value with the corresponding wrapper object
     * and, in this case, you may not pass <code>null</code>.
     * 
     * @param ms
     *            This method's signature.
     * @param args
     *            The actual arguments of this method call. The array must be
     *            indexed in the same order as the parameters were declared. You
     *            mustn't use this constructor if the method signature declares
     *            no parameters.
     */
    public MockedCall(MethodSignature ms, Object[] args) {
        // checkSignature(ms, hasParams, isVoid)
        checkSignature(ms, true, true);
        signature = ms;
        this.args = new Object[signature.numberOfParameters()];
        setArgs(args);
    }

    /**
     * Creates an object to represent the method call defined by the specified
     * method signature and return value. Use this constructor if the method
     * declares no parameters. If the return value is a primitive type, you have
     * to wrap that value with the corresponding wrapper object and, in this
     * case, you may not pass <code>null</code>.
     * 
     * @param ms
     *            This method's signature.
     * @param retVal
     *            The return value of this method call. You mustn't use this
     *            constructor if the method signature declares a
     *            <code>void</code> return type.
     */
    public MockedCall(MethodSignature ms, Object retVal) {
        // checkSignature(ms, hasParams, isVoid)
        checkSignature(ms, false, false);
        signature = ms;
        this.args = new Object[signature.numberOfParameters()];
        setResult(retVal);
    }

    /**
     * Creates an object to represent the method call defined by the specified
     * method signature. Use this constructor if the method declares a
     * <code>void</code> return type and no parameters.
     * 
     * @param ms
     *            This method's signature.
     */
    public MockedCall(MethodSignature ms) {
        // checkSignature(ms, hasParams, isVoid)
        checkSignature(ms, false, true);
        signature = ms;
        this.args = new Object[signature.numberOfParameters()];
    }

    /**
     * Retrieves the return value of the method call represented by this object.
     * You mustn't call this method if the method signature declares a
     * <code>void</code> return type.
     * 
     * @return The return value of the method call represented by this object.
     */
    public Object getResult() {
        if (signature.isReturnTypeVoid()) {
            throw new IllegalArgumentException(
                    "This method has a void return type.");
        }
        return result;
    }

    /**
     * Associates an exception to the method call represented by this object.
     * 
     * @param t
     *            The exception.
     */
    public void setException(Throwable t) {
        if (t == null) {
            throw new NullPointerException("No exception.");
        }
        exception = t;
    }

    /**
     * Tells whether the method call represented by this object has an
     * exception.
     * 
     * @return <code>true</code> if there's an exception, <code>false</code>
     *         otherwise.
     */
    public boolean hasException() {
        return exception != null;
    }

    /**
     * Retrieves the exception, if any, associated to the method call
     * represented by this object. This method will return <code>null</code>
     * if there's no exception associated to the method call represented by this
     * object.
     * 
     * @see #hasException()
     * @return The exception, if any, associated to the method call represented
     *         by this object.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Tells whether the passed <code>MockedCall</code> can be considered the
     * same as the current object.
     * 
     * @param mc
     *            Another <code>MockedCall</code>.
     * @return <code>true</code> if, and only if, <code>mc</code> and the
     *         current object have both the same signature and the same actual
     *         call arguments.
     */
    public boolean isSameCall(MockedCall mc) {
        boolean b = false;
        if (mc != null && signature.equals(mc.signature)) {
            b = true;
            // B/c of setArgs and constructor implementations, we know that the
            // args array always have the same number of elements as specified
            // by the signature object (this number may also be 0: no params).
            for (int i = 0; i < args.length; ++i) {
                if (!isSameObject(args[i], mc.args[i])) {
                    b = false;
                    break;
                }
            }
        }
        return b;
    }

    /**
     * Overrides the same method in <code>Object</code> to return a string in
     * the format: "name(arg1, ..., argN)".
     * 
     * @return A string representation of this method call.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(signature.getName());
        buf.append("(");
        if (args.length != 0) {
            int i = 0;
            while (i < args.length - 1) {
                buf.append(args[i++]);
                buf.append(", ");
            }
            buf.append(args[i]);
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * Checks that <code>ms</code> is not <code>null</code> and conforms to
     * the options specified by the boolean parameters. If a check fails, a
     * runtime exception is thrown.
     * 
     * @param ms
     *            The method signature.
     * @param hasParams
     *            Pass <code>true</code> to verify that the signature declares
     *            parameters, <code>false</code> to verify that the signature
     *            has no parameters.
     * @param isVoid
     *            Pass <code>true</code> to verify that the signature declares
     *            a <code>void</code> return type, <code>false</code> to
     *            verify that the return type is not <code>void</code>.
     */
    private void checkSignature(MethodSignature ms, boolean hasParams,
            boolean isVoid) {
        if (ms == null) {
            throw new NullPointerException("No method signature was provided.");
        }
        if (hasParams != ms.hasParameters()) {
            throw new IllegalArgumentException("Wrong parameters declaration.");
        }
        if (isVoid != ms.isReturnTypeVoid()) {
            throw new IllegalArgumentException("Wrong return type declaration.");
        }
    }

    /**
     * Sets the arguments of this method call. If there is any primitive type,
     * you have to wrap that value with the corresponding wrapper object and, in
     * this case, you may not pass <code>null</code>. You mustn't call this
     * method if the method signature declares no parameters.
     * 
     * @param args
     *            The actual arguments of this method call. The array must be
     *            indexed in the same order as the parameters were declared.
     */
    private void setArgs(Object[] args) {
        // Constructor has already checked that method has params.
        if (args == null || args.length != signature.numberOfParameters()) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }
        for (int i = 0; i < args.length; ++i) {
            if (!signature.isValidArgument(args[i], i)) {
                throw new IllegalArgumentException("Invalid argument type: "
                        + i);
            }
            this.args[i] = args[i];
        }
    }

    /**
     * Sets the result of this method call. If the method return type is a
     * primitive type, you have to wrap the value with the corresponding wrapper
     * object and, in this case, you may not pass <code>null</code>. You
     * mustn't call this method if the method signature declares a
     * <code>void</code> return type.
     * 
     * @param result
     *            The return value of this method call.
     */
    private void setResult(Object result) {
        // Constructor has already checked that return type is not void.
        if (!signature.isValidReturnValue(result)) {
            throw new IllegalArgumentException("Invalid return type.");
        }
        this.result = result;
    }

    /**
     * Tells whether <code>p1</code> and <code>p2</code> may be considered
     * to be the same object.
     * 
     * @param p1
     *            The first object.
     * @param p2
     *            The second object.
     * @return <code>true</code> if the passed objects hold the same state,
     *         <code>false</code> otherwise.
     */
    private boolean isSameObject(Object p1, Object p2) {
        boolean b = p1 == p2;
        if (!b) {
            if (p1 == null) {
                b = false;
            } else {
                // Both p1 and p2 are not null.
                b = p2.equals(p1);
            }
        }
        return b;
    }

}
