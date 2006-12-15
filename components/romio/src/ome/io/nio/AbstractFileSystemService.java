/*
 * ome.io.nio.AbstractFileSystemService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.util.Formatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author callan
 *
 */
public class AbstractFileSystemService
{
    
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(AbstractFileSystemService.class);
    
    public final static String ROOT_DEFAULT = File.separator + "OMERO" + File.separator;
    
    public final static String PIXELS_PATH =  "Pixels" + File.separator;
    
    public final static String FILES_PATH = "Files" + File.separator;
    
    public final static String THUMBNAILS_PATH = "Thumbnails" + File.separator;
    
    private final String root;

    public AbstractFileSystemService(String path)
    {
    	if ( log.isDebugEnabled() )
    	{
    		log.debug("Using root path: '" + path + "'");
    	}
    	
        this.root = path;
        
        File rootDirectory = new File(this.root);
        if (
                ! rootDirectory.isDirectory() ||
                ! rootDirectory.canRead() || 
                ! rootDirectory.canWrite()
            )
            throw new IllegalArgumentException("Invalid directory specified for file system service.");
    }
    
    /**
     * Makes sure that for a given path, it's subpath exists. For example,
     * given the path "/foo/bar/foobar.txt" the method will make sure the
     * directory structure "/foo/bar" exists.
     * @param path the path to check for subpath existance.
     */
    protected void createSubpath(String path)
    {
    	File file = new File(path);
    	if (!file.exists())
    	{
    		File directory = new File(file.getParent());
    		if (!directory.exists())
    			directory.mkdirs();
    	}
    }
    
    public String getPixelsPath(Long id)
    {
        return getPath(PIXELS_PATH, id);
    }
    
    public String getFilesPath(Long id)
    {
        return getPath(FILES_PATH, id);
    }
    
    public String getThumbnailPath(Long id)
    {
        return getPath(THUMBNAILS_PATH, id);
    }
    
    private String getPath(String prefix, Long id)
    {
        String suffix = "";
        Long remaining = id;
        Long dirno = 0L;
        
        if (id == null)
            throw new NullPointerException("Expecting a not-null id.");

        while (remaining > 999)
        {
            remaining /= 1000;
            
            if (remaining > 0)
            {
                Formatter formatter = new Formatter();
                dirno = remaining % 1000;
                suffix = formatter.format("Dir-%03d", dirno)
                                  .out().toString() + File.separator + suffix;
            }
        }
        
        return root + prefix + suffix + id;
    }
}
