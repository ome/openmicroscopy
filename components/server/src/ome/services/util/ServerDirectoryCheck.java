/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;

/**
 * Hook run by the context at startup to create needed directories for the server.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.3.0
 */
public class ServerDirectoryCheck implements Runnable {

    public final static Logger log = LoggerFactory
            .getLogger(ServerDirectoryCheck.class);

    private final String omeroDataDir, omeroManagedDir;
    private final ReadOnlyStatus readOnly;

    public ServerDirectoryCheck(String omeroDataDir, String omeroManagedDir, ReadOnlyStatus readOnly) {
        this.omeroDataDir = omeroDataDir;
        this.omeroManagedDir = omeroManagedDir;
        this.readOnly = readOnly;
    }

    /**
     * Ensures that directories exist corresponding to,
     * <ul>
     *   <li><code>${omero.data.dir}/FullText</code></li>
     *   <li><code>${omero.managed.dir}</code></li>
     * </ul>
     */
    public void run() {
        checkDirectory(omeroDataDir + File.separator + "FullText");
        checkDirectory(omeroManagedDir);
    }

    /**
     * Ensure that the given directory exists, creating it if necessary.
     * @param directoryName the name of the required directory
     */
    private void checkDirectory(String directoryName) {
        final File directory = new File(directoryName);
        if (directory.listFiles() == null) {
            if (readOnly.isReadOnlyRepo()) {
                throw new BeanCreationException("required directory " + directory + " cannot be read but repository is read-only");
            } else {
                directory.mkdirs();
                log.info("Created " + directory);
            }
        }
    }
}
