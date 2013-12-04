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

/**
 * Interceptor which takes any context provided by the
 * client and calls setContext on CurrentDetails. This
 * allows users to dynamically change, for example, the
 * call group without modifying the whole session.
 *
 * @see ticket:3529
 */
public class CallContext implements MethodInterceptor {

    private static Logger log = LoggerFactory.getLogger(CallContext.class);

    private final CurrentDetails cd;

    public CallContext(OmeroContext ctx) {
        this.cd = ctx.getBean(CurrentDetails.class);
    }

    public CallContext(CurrentDetails cd) {
        this.cd = cd;
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
                            MDC.put("fileset", ctx.get("omero.logfilename"));
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
