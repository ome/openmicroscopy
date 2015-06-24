/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.security.Permissions;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import ome.security.SecuritySystem;
import ome.services.messages.LoginAttemptMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Listens for any {@link LoginAttemptMessage}. If there are more than some
 * number of failures, then throttling beings to reduce the number of possible
 * checks. The next successful check resets the count to 0. The state is not
 * stored between server restarts.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @see Permissions
 * @since 4.2.1
 */

public class LoginAttemptListener implements
        ApplicationListener<LoginAttemptMessage> {

    private final static Logger log = LoggerFactory
            .getLogger(LoginAttemptListener.class);

    private final LoadingCache<String, AtomicInteger> counts = CacheBuilder.newBuilder().build(
            new CacheLoader<String, AtomicInteger>() {
                @Override
                public AtomicInteger load(String key) {
                    return new AtomicInteger(0);
                }
            });

    private final int throttleCount;

    private final long throttleTime;

    public LoginAttemptListener(int throttleCount, long throttleTime) {
        this.throttleCount = throttleCount;
        this.throttleTime = throttleTime;
    }

    public void onApplicationEvent(LoginAttemptMessage lam) {

        if (lam.success == null) {
            return; // EARLY EXIT.
        }

        AtomicInteger ai = null;
        try {
            ai = counts.get(lam.user);
        } catch (ExecutionException e) {
            /* cannot occur unless loading thread is interrupted */
        }
        if (lam.success) {
            int previous = ai.getAndSet(0);
            if (previous > 0) {
                log.info(String.format(
                        "Resetting failed login count of %s for %s", previous,
                        lam.user));
            }
        } else {
            int value = ai.incrementAndGet();
            if (value > throttleCount) {
                log.warn(String.format(
                        "%s failed logins for %s. Throttling for %s", value,
                        lam.user, throttleTime));
                if (throttleTime > 0) {
                    try {
                        Thread.sleep(throttleTime); // TODO something nicer
                    } catch (InterruptedException e) {
                        log.debug("Interrupt while throttling for " + lam.user);
                    }
                }
            }
        }
    }

}
