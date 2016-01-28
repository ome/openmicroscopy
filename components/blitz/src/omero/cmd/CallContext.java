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
 * @see <a href="http://trac.openmicroscopy.org/ome/ticket/3529">Trac ticket #3529</a>
 */
public class CallContext implements MethodInterceptor {

    private static Logger log = LoggerFactory.getLogger(CallContext.class);

    private final CurrentDetails cd;

    private final String token;

    public CallContext(OmeroContext ctx, String token) {
        this.cd = ctx.getBean(CurrentDetails.class);
        this.token = token;
    }

    public CallContext(CurrentDetails cd, String token) {
        this.cd = cd;
        this.token = token;
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
                        if (ctx.containsKey("omero.logfilename")) {
                            if (ctx.containsKey("omero.logfilename.token")
                                    && token.equals(ctx
                                            .get("omero.logfilename.token"))) {
                                MDC.put("fileset", ctx.get("omero.logfilename"));
                            } else {
                                throw new SecurityViolation(null, null,
                                        "Setting the omero.logfilename value is"
                                        + " not permitted without a secure"
                                        + " server token!");
                            }
                        }
                    }
                }
            }
        }

        try {
            return arg0.proceed();
        } finally {
            MDC.clear();
        }
    }
}
