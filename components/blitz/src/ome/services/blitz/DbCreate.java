/*   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

import java.io.File;

import ome.services.util.Executor;
import ome.services.util.Executor.SimpleStatelessWork;
import ome.system.OmeroContext;

import org.apache.commons.io.FileUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database creation driver used by {@link Entry} when called from the command
 * line.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class DbCreate implements Runnable {

    final private File f;

    final private Executor ex;

    public DbCreate(String[] args) {
        try {
            File dbFile = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-db")) {
                    dbFile = new File(args[i + 1]);
                    break;
                }
            }

            if (dbFile == null) {
                throw new RuntimeException("Usage: -db filename");
            }

            OmeroContext ctx = new OmeroContext(new String[] {
                    "classpath:ome/services/datalayer.xml",
                    "classpath:ome/services/sec-primitives.xml" });
            ex = (Executor) ctx.getBean("executor");
            f = dbFile;
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize DbCreate", e);
        }
    }

    public DbCreate(File f, Executor ex) {
        this.f = f;
        this.ex = ex;
    }

    public void run() {
        try {
            final String sql = FileUtils.readFileToString(f);
            ex.executeStateless(new SimpleStatelessWork(this, "DbCreate") {
                @Transactional(readOnly = false)
                public Object doWork(SimpleJdbcOperations jdbc) {
                    jdbc.update(sql);
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Could not create database", e);
        }
    }

}
