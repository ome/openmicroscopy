/*
 * ome.services.OmeroFilePathResolver
 *
 *   Copyright 2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger log = LoggerFactory.getLogger(FilePathResolver.class);

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
            // FIXME: In order to enable multi-server FS proper
            // redirecting will need to happen at the pixel buffer
            // layer as is currently happening in RawPixelsStoreI etc.
            // In other words, far before we reach this code, we should
            // already have been re-directed to the proper repo.
        {
            File f = new File(repo == null ? omeroDataDir : sql.findRepoRootPath(repo));
            f = new File(f, namePathRepo.get(1));
            f = new File(f, namePathRepo.get(0));
            String originalFilePath = f.getAbsolutePath();
            log.info("Metadata only file, resulting path: " +
                    originalFilePath);
            return originalFilePath;
        }
        return null;
    }
}
