/*
 *   Copyright 2017 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.concurrent.atomic.AtomicBoolean;

import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.transaction.annotation.Transactional;

/**
 * Hook run by the context as early as possible to detect if the DB
 * connection is read-only. This object can be used by other objects
 * to skip certain steps. Later actions may flip the state of the status.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class ReadOnlyStatus {

    public final static Logger log = LoggerFactory.getLogger(ReadOnlyStatus.class);

    private final Executor ex;

    private final AtomicBoolean isReadOnly;

    public ReadOnlyStatus(boolean isReadOnly, Executor executor) {
        this.isReadOnly= new AtomicBoolean(isReadOnly);
        this.ex = executor;
        doCheck();
    }

    private void readOnlyTx() {
        ex.executeSql(
            new Executor.SimpleSqlWork(this, "checkReadOnly") {
                @Transactional(readOnly = false)
                public Boolean doWork(SqlAction sql) {
                    return true;
                }
            });
    }

    protected void doCheck() {

        if (isReadOnly.get()) {
            log.debug("read only set before check");
        } else {
            try {
                readOnlyTx();
            } catch (Exception e) {
                log.debug("exception during read-only check", e);
                isReadOnly.set(true);
            }
        }

        String state = isReadOnly.get() ? "only" : "write";
        log.info("Server is in read-{} mode", state);
    }

    public boolean isReadOnly() {
        return isReadOnly.get();
    }

}
