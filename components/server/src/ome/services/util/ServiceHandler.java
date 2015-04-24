/*
 * ome.services.util.ServiceHandler 
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import ome.annotations.AnnotationUtils;
import ome.annotations.ApiConstraintChecker;
import ome.annotations.Hidden;
import ome.conditions.ApiUsageException;
import ome.conditions.ConcurrencyException;
import ome.conditions.DatabaseBusyException;
import ome.conditions.InternalException;
import ome.conditions.OptimisticLockException;
import ome.conditions.RootException;
import ome.conditions.TryAgain;
import ome.conditions.ValidationException;
import ome.security.basic.CurrentDetails;
import ome.services.messages.RegisterServiceCleanupMessage;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.PropertyValueException;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateSystemException;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * 
 */
public class ServiceHandler implements MethodInterceptor, ApplicationListener {

    /**
     * Maxiumum length of a string that will be returned.
     */
    private final static int MAX_STRING_LEN = 100;

    private static Logger log = LoggerFactory.getLogger(ServiceHandler.class);

    private final CurrentDetails cd;

    private final long methodTimeError;

    private final long methodTimeWarn;

    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof RegisterServiceCleanupMessage) {
            RegisterServiceCleanupMessage cleanup = (RegisterServiceCleanupMessage) arg0;
            cd.addCleanup(cleanup);
        }
    }

    public ServiceHandler(CurrentDetails cd) {
        this(cd, 5000, 15000);
    }

    public ServiceHandler(CurrentDetails cd, long methodTimeWarn, long methodTimeError) {
        this.cd = cd;
        this.methodTimeWarn = methodTimeWarn;
        this.methodTimeError = methodTimeError;
    }

    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable {
        if (arg0 == null) {
            throw new InternalException(
                    "Cannot act on null MethodInvocation. Stopping.");
        }

        Class implClass = arg0.getThis().getClass();
        Method mthd = arg0.getMethod();
        Object[] args = arg0.getArguments();

        ApiConstraintChecker.errorOnViolation(implClass, mthd, args);

        if (log.isInfoEnabled()) {
            // Method and arguments
            if (Executor.Work.class.isAssignableFrom(arg0.getThis().getClass())) {
                Executor.Work work = (Executor.Work) arg0.getThis();
                log.info(" Executor.doWork -- " + work.description());
            } else {
                log.info(" Meth:\t" + arg0.getMethod().getDeclaringClass()
                        + "." + arg0.getMethod().getName());
            }
            log.info(" Args:\t" + getArgumentsString(arg0));
        }

        // Results and/or Exceptions
        Object o;
        StringBuilder finalOutput = new StringBuilder();

        StopWatch stopWatch = new Slf4JStopWatch();
        try {

            o = arg0.proceed();
            finalOutput.append(" Rslt:\t");
            finalOutput.append(getResultsString(o, null));
            stopWatch.stop("omero.call.success." + implClass.getName() + "." + mthd.getName());
            return o;
        } catch (Throwable t) {
            finalOutput.append(" Excp:\t");
            finalOutput.append(t.toString());
            stopWatch.stop("omero.call.exception");
            throw getAndLogException(t);
        } finally {
            if (log.isInfoEnabled()) {
                log.info(finalOutput.toString()); // slf4j migration: toString()
            }

            // Logging long invocations. Very long invocations are indicative
            // of a server undergoing stress.
            long time = stopWatch.getElapsedTime();
            String msg = String.format("Method %s.%s invocation took %s",
                                       arg0.getMethod().getDeclaringClass(),
                                       arg0.getMethod().getName(), time);
            if (time > methodTimeError) {
                log.error(msg);
            } else if (time > methodTimeWarn) {
                log.warn(msg);
            }
            cleanup();
        }

    }

    protected void cleanup() {
        Set<RegisterServiceCleanupMessage> cleanups = cd.emptyCleanups();
        for (RegisterServiceCleanupMessage registerServiceCleanupMessage : cleanups) {
            try {
                log.info("Cleanup: " + registerServiceCleanupMessage.resource);
                registerServiceCleanupMessage.close();
            } catch (Exception e) {
                log.warn("Error while cleaning up", e);
            }
        }
    }

    protected Throwable getAndLogException(Throwable t) {
        if (null == t) {
            log.error("Exception thrown. (null)");
            return new InternalException("Exception thrown with null message");
        } else {
            String msg = " Wrapped Exception: (" + t.getClass().getName()
                    + "):\n" + t.getMessage();

            // Base type of the hierarchy that we are converting to.
            // Just rethrow.
            if (RootException.class.isAssignableFrom(t.getClass())) {
                return t;
            }

            //
            // Spring's transient exception hierarchy
            //
            if (DeadlockLoserDataAccessException.class.isAssignableFrom(
                    t.getClass())) {

                DeadlockLoserDataAccessException dldae = (DeadlockLoserDataAccessException) t;
                TryAgain ta = new TryAgain(dldae.getMessage(), 500L); // ticket:5639
                ta.setStackTrace(t.getStackTrace());
                printException("Deadlock exception thrown.", t);
                return ta;

            } else if (OptimisticLockingFailureException.class
                    .isAssignableFrom(t.getClass())) {

                OptimisticLockException ole = new OptimisticLockException(t
                        .getMessage());
                ole.setStackTrace(t.getStackTrace());
                printException("OptimisticLockingFailureException thrown.", t);
                return ole;

            } else if (ConcurrencyFailureException.class.isAssignableFrom(t.getClass())) {

                ConcurrencyFailureException cfe = (ConcurrencyFailureException) t;
                ConcurrencyException ce = new ConcurrencyException(cfe.getMessage(), 500);
                ce.setStackTrace(t.getStackTrace());
                printException("Unknown concurrency failure", t);
                return ce;

            } else if (TransientDataAccessResourceException.class.isAssignableFrom(t.getClass())) {

                ConcurrencyFailureException cfe = (ConcurrencyFailureException) t;
                ConcurrencyException ce = new ConcurrencyException(cfe.getMessage(), 500);
                ce.setStackTrace(t.getStackTrace());
                printException("Unknown transient failure", t);
                return ce;


            } else if (IllegalArgumentException.class.isAssignableFrom(t
                    .getClass())) {
                ApiUsageException aue = new ApiUsageException(t.getMessage());
                aue.setStackTrace(t.getStackTrace());
                printException("IllegalArgumentException thrown.", t);
                return aue;
            }

            else if (InvalidDataAccessResourceUsageException.class
                    .isAssignableFrom(t.getClass())) {
                ApiUsageException aue = new ApiUsageException(t.getMessage());
                aue.setStackTrace(t.getStackTrace());
                printException(
                        "InvalidDataAccessResourceUsageException thrown.", t);
                return aue;
            }

            else if (DataIntegrityViolationException.class.isAssignableFrom(t
                    .getClass())) {
                ValidationException ve = new ValidationException(t.getMessage());
                ve.setStackTrace(t.getStackTrace());
                printException("DataIntegrityViolationException thrown.", t);
                return ve;
            }

            else if (CannotCreateTransactionException.class.isAssignableFrom(t
                    .getClass())) {
                DatabaseBusyException dbe = new DatabaseBusyException(
                        "cannot create transaction", 5000L);
                dbe.setStackTrace(t.getStackTrace());
                printException("CannotCreateTransactionException thrown.", t);
                return dbe;
            }

            else if (HibernateObjectRetrievalFailureException.class
                    .isAssignableFrom(t.getClass())) {
                ValidationException ve = new ValidationException(t.getMessage());
                ve.setStackTrace(t.getStackTrace());
                printException(
                        "HibernateObjectRetrievealFailureException thrown.", t);
                return ve;
            }

            else if (HibernateSystemException.class.isAssignableFrom(t
                    .getClass())) {
                Throwable cause = t.getCause();
                if (cause == null || cause == t) {
                    return wrapUnknown(t, msg);
                } else if (PropertyValueException.class.isAssignableFrom(cause
                        .getClass())) {
                    ValidationException ve = new ValidationException(cause
                            .getMessage());
                    ve.setStackTrace(cause.getStackTrace());
                    printException("PropertyValueException thrown.", cause);
                    return ve;
                } else {
                    return wrapUnknown(t, msg);
                }
            }

            return wrapUnknown(t, msg);

        }

    }

    private Throwable wrapUnknown(Throwable t, String msg) {

        // If this is an Error, then we want to log a message
        // since these are most likely: AssertionError (bad assumptions),
        // LinkageError (bad jar versions), ThreadDeath, or one of the
        // VirtualMachineErrors: OutOfMemory, InternalError, StackOverflowError,
        // UnknownError
        if (t instanceof Error) {
            log.error("java.lang.Error: " + msg, t);
        }

        // Wrap all other exceptions in InternalException
        InternalException re = new InternalException(msg);
        re.setStackTrace(t.getStackTrace());
        printException("Unknown exception thrown.", t);
        return re;
    }

    /**
     * produces a String from the arguments array. Argument parameters marked as
     * {@link Hidden} will be replaced by "*******".
     */
    private String getArgumentsString(MethodInvocation mi) {
        String arguments;
        Object[] args = mi.getArguments();

        if (args == null || args.length < 1) {
            return "()";
        }

        String[] prnt = new String[args.length];
        for (int i = 0; i < prnt.length; i++) {
            prnt[i] = args[i] == null ? "null" : getResultsString(args[i], null);
        }

        Object[] allAnnotations = AnnotationUtils.findParameterAnnotations(mi
                .getThis().getClass(), mi.getMethod());

        for (int j = 0; j < allAnnotations.length; j++) {
            Annotation[][] anns = (Annotation[][]) allAnnotations[j];
            if (anns == null) {
                continue;
            }

            for (int i = 0; i < args.length; i++) {
                Annotation[] annotations = anns[i];

                for (Annotation annotation : annotations) {
                    if (Hidden.class.equals(annotation.annotationType())) {
                        prnt[i] = "********";
                    }
                }
            }
        }

        arguments = Arrays.asList(prnt).toString();
        return arguments;
    }

    /**
     * public for testing purposes.
     */
    public static String getResultsString(Object o, IdentityHashMap<Object, String> cache) {

        if (o == null) {
            return "null";
        }

        if (cache == null) {
            cache = new IdentityHashMap<Object, String>();
        } else {
            if (cache.containsKey(o)) {
                return (String) cache.get(o);
            }
        }

        if (o instanceof Collection) {
            int count = 0;
            StringBuilder sb = new StringBuilder(128);
            sb.append("(");
            Collection c = (Collection) o;
            for (Object obj : (c)) {
                if (count > 0) {
                    sb.append(", ");
                }
                if (count > 2) {
                    sb.append("... ");
                    sb.append(c.size() - 3);
                    sb.append(" more");
                    break;
                }
                sb.append(getResultsString(obj, cache));
                count++;
            }
            sb.append(")");
            return sb.toString();
        } else if (o instanceof Map) {
            Map map = (Map) o;
            int count = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Object k : map.keySet()) {
                if (count > 0) {
                    sb.append(", ");
                }
                if (count > 2) {
                    sb.append("... ");
                    sb.append(map.size() - 3);
                    sb.append(" more");
                    break;
                }
                sb.append(k);
                sb.append("=");
                cache.put(o, o.getClass().getName() + ":" + System.identityHashCode(o));
                sb.append(getResultsString(map.get(k), cache));
                count++;
            }
            sb.append("}");
            return sb.toString();
        } else if (o.getClass().isArray()) {
            int length = Array.getLength(o);
            if (length == 0) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder(128);
            sb.append("[");
            for (int i = 0; i < length; i++) {

                if (i != 0) {
                    sb.append(", ");
                }

                if (i > 2) {
                    sb.append("... ");
                    sb.append(i - 2);
                    sb.append(" more");
                    break;
                }
                sb.append(getResultsString(Array.get(o, i), cache));
            }
            sb.append("]");
            return sb.toString();
        } else {
            String s = o.toString();
            if (s == null) {
                return null;
            } else if (s.length() > MAX_STRING_LEN) {
                s = s.substring(0, MAX_STRING_LEN);
            }
            return s;
        }
    }

    private void printException(String msg, Throwable ex) {
        if (log.isWarnEnabled()) {
            log.warn(msg + "\n", ex);
        }
    }
}
