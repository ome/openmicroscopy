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
import java.util.Map;
import java.util.Set;

import ome.annotations.AnnotationUtils;
import ome.annotations.ApiConstraintChecker;
import ome.annotations.Hidden;
import ome.conditions.ApiUsageException;
import ome.conditions.DatabaseBusyException;
import ome.conditions.InternalException;
import ome.conditions.OptimisticLockException;
import ome.conditions.RootException;
import ome.conditions.ValidationException;
import ome.security.basic.CurrentDetails;
import ome.services.messages.RegisterServiceCleanupMessage;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.PropertyValueException;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateSystemException;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * 
 */
public class ServiceHandler implements MethodInterceptor, ApplicationListener {

    private static Log log = LogFactory.getLog(ServiceHandler.class);

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

        StopWatch stopWatch = new CommonsLogStopWatch();
        try {

            o = arg0.proceed();
            finalOutput.append(" Rslt:\t");
            finalOutput.append(getResultsString(o));
            stopWatch.stop("omero.call.success");
            return o;
        } catch (Throwable t) {
            finalOutput.append(" Excp:\t");
            finalOutput.append(t.toString());
            stopWatch.stop("omero.call.exception");
            throw getAndLogException(t);
        } finally {
            if (log.isInfoEnabled()) {
                log.info(finalOutput);
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

            if (RootException.class.isAssignableFrom(t.getClass())) {
                return t;
            } else if (OptimisticLockingFailureException.class
                    .isAssignableFrom(t.getClass())) {
                OptimisticLockException ole = new OptimisticLockException(t
                        .getMessage());
                ole.setStackTrace(t.getStackTrace());
                printException("OptimisticLockingFailureException thrown.", t);
                return ole;
            }

            else if (IllegalArgumentException.class.isAssignableFrom(t
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

            else {
                return wrapUnknown(t, msg);
            }

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
            prnt[i] = args[i] == null ? "null" : getResultsString(args[i]);
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
    public String getResultsString(Object o) {
        if (o == null) {
            return "null";
        }

        else if (o instanceof Collection) {
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
                sb.append(obj);
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
                sb.append(getResultsString(map.get(k)));
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
                sb.append(Array.get(o, i));
            }
            sb.append("]");
            return sb.toString();
        } else {
            return o.toString();
        }
    }

    private void printException(String msg, Throwable ex) {
        if (log.isWarnEnabled()) {
            log.warn(msg + "\n", ex);
        }
    }
}
