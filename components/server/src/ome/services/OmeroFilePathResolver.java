/*
 * ome.services.OmeroFilePathResolver
 *
 *   Copyright 2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.io.nio.AbstractFileSystemService;
import ome.io.nio.FilePathResolver;
import ome.model.core.Pixels;
import ome.util.SqlAction;

/**
 * OMERO server based resolver for file paths.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO4.3
 */
public class OmeroFilePathResolver implements FilePathResolver
{
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(FilePathResolver.class);

    /** SQL action instance for this class. */
    protected final SqlAction sql;

    /** The server's OMERO data directory. */
    protected final String omeroDataDir;

    /**
     * Constructor.
     * @param sql SQL action instance for this class.
     * @param omeroDataDir The server's OMERO data directory.
     */
    public OmeroFilePathResolver(SqlAction sql, String omeroDataDir)
    {
        this.sql = sql;
        this.omeroDataDir = omeroDataDir;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.FilePathResolver#getOriginalFilePath(ome.io.nio.AbstractFileSystemService, ome.model.core.Pixels)
     */
    public String getOriginalFilePath(AbstractFileSystemService service,
            Pixels pixels)
    {
        List<String> namePathRepo =
            sql.getPixelsNamePathRepo(pixels.getId());
        String name = namePathRepo.get(0);
        String path = namePathRepo.get(1);
        String repo = namePathRepo.get(2);
        if (name != null && path != null) // && repo == null)
            // FIXME: ignoring repo as a workaround for all the corruptions
            // seen during testing. In order to enable multi-server FS
            // proper redirecting will need to happen at the pixel buffer
            // layer as is currently happening in RawPixelsStoreI etc.
            // In other words, far before we reach this code, we should
            // already have been re-directed to the proper repo.
        {
            // A "null" repo signifies the default repository
            File f = new File(omeroDataDir);
            f = new File(f, namePathRepo.get(1));
            f = new File(f, namePathRepo.get(0));
            String originalFilePath = f.getAbsolutePath();
            log.info("Metadata only file, resulting path: " +
                    originalFilePath);
            return originalFilePath;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.FilePathResolver#getPixelsParams(ome.model.core.Pixels)
     */
    public Map<String, String> getPixelsParams(Pixels pixels)
    {
        return sql.getPixelsParams(pixels.getId());
    }
}
