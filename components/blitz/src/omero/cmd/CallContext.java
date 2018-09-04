/*
 *   Copyright 2012-2013 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd;

import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ome.security.basic.CurrentDetails;
import ome.system.OmeroContext;
import omero.SecurityViolation;

/**
 * Interceptor which takes any context provided by the
 * client and calls setContext on CurrentDetails. This
 * allows users to dynamically change, for example, the
 * call group without modifying the whole session.
 *
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/3529">Trac ticket #3529</a>
 */
public class CallContext implements MethodInterceptor {

    public static final String FILENAME_KEY = "omero.logfilename";

    public static final String TOKEN_KEY = "omero.logfilename.token";

    private static Logger log = LoggerFactory.getLogger(CallContext.class);

    private final CurrentDetails cd;

    private final String token;

    private final Ice.Current current;

    public CallContext(OmeroContext ctx, String token, Ice.Current current) {
        this.cd = ctx.getBean(CurrentDetails.class);
        this.token = token;
        this.current = current;
    }

    public CallContext(OmeroContext ctx, String token) {
        this(ctx, token, null);
    }

    public CallContext(CurrentDetails cd, String token) {
        this.cd = cd;
        this.token = token;
        this.current = null;
    }

    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(final MethodInvocation arg0) throws Throwable {

        if (arg0 != null) {
            final Object[] args = arg0.getArguments();
            if (args != null && args.length > 0) {
                final Object last = args[args.length-1];
                if (Ice.Current.class.isAssignableFrom(last.getClass())) {
                    final Ice.Current current = (Ice.Current) last;
                    final Map<String, String> ctx = current.ctx;
                    if (ctx != null && ctx.size() > 0) {
                        cd.setContext(ctx);
                        // Don't trust user-passed values
                        if (!checkLogFile(ctx, true)) {
                            if (this.current != null) {
                                // fall back to the service-wide values
                                checkLogFile(this.current.ctx, true);
                            }
                        }
                    }
                }
            }
        }

        try {
            return arg0.proceed();
        } finally {
            cd.setContext(null);
            MDC.clear();
        }
    }

    /**
     * If "omero.logfilename" is in the passed {@link Map}, then set it in the
     * {@link MDC} IFF requireToken is false or the token is present and matches
     * the original token set on this instance.
     */
    private boolean checkLogFile(Map<String, String> ctx, boolean requireToken)
        throws SecurityViolation {

        if (ctx == null) {
            return false;
        }

        String filename = ctx.get(FILENAME_KEY);
        if (filename == null) {
            return false;
        }

        if (requireToken) {
            String token = ctx.get(TOKEN_KEY);
            if (!this.token.equals(token)) {
                log.error("Found bad token: user={} != server={}",
                        token, this.token);
                throw new SecurityViolation(null, null,
                        String.format("Setting the %s value is"
                        + " not permitted without a secure"
                        + " server token!", FILENAME_KEY));
            }
        }

        MDC.put("fileset", filename);
        return true;
    }
}
