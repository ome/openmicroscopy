/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.ServerError;
import omero.ValidationException;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.grid.RepositoryPrx;
import omero.grid._RepositoryDisp;
import omero.model.Format;
import omero.model.FormatI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * 
 * @since Beta4.1
 */
public class PublicRepositoryI extends _RepositoryDisp {

    private final static Log log = LogFactory.getLog(PublicRepositoryI.class);

    private final long id;

    private final File root;

    private final Executor executor;

    private final Principal principal;

    public PublicRepositoryI(File root, long repoObjectId, Executor executor,
            Principal principal) throws Exception {
        this.id = repoObjectId;
        this.executor = executor;
        this.principal = principal;

        if (root == null || !root.isDirectory()) {
            throw new ValidationException(null, null,
                    "Root directory must be a existing, readable directory.");
        }
        this.root = root.getAbsoluteFile();

    }

    public OriginalFile root(Current __current) throws ServerError {
        return new OriginalFileI(this.id, false); // SHOULD BE LOADED.
    }

    public OriginalFile register(String path, Format fmt, Current __current)
            throws ServerError {

        if (path == null || fmt == null
                || (fmt.getId() == null && fmt.getValue() == null)) {
            throw new ValidationException(null, null,
                    "path and fmt are required arguments");
        }

        OriginalFile file = new OriginalFileI();
        file.setPath(rstring(path));
        file.setFormat(fmt);

        // Overwrites
        omero.RTime creation = omero.rtypes.rtime(System.currentTimeMillis());
        file.setCtime(creation);
        file.setAtime(creation);
        file.setMtime(creation);
        file.setSha1(rstring("UNKNOWN"));
        file.setName(rstring(path));
        file.setPath(rstring(path)); // NEED SOME CHECKING HERE.
        file.setSize(rlong(0));

        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile f = (ome.model.core.OriginalFile) mapper
                .reverse(file);
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "saveNewOriginalFile") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(f).getId();
            }
        });

        file.setId(rlong(id));
        file.unload();
        return file;

    }

    public void delete(String path, Current __current) throws ServerError {
        File file = checkPath(path);
        FileUtils.deleteQuietly(file);
    }

    @SuppressWarnings("unchecked")
    
    public List<OriginalFile> list(String path, Current __current) throws ServerError {
        File file = checkPath(path);
        List<File> files = Arrays.asList(file.listFiles());
        return filesToOriginalFiles(files);
    }

    public List<OriginalFile> listDirs(String path, Current __current)
            throws ServerError {
        File file = checkPath(path);
        List<File> files = Arrays.asList(file.listFiles((FileFilter)FileFilterUtils.directoryFileFilter()));
        return filesToOriginalFiles(files);
    }

    public List<OriginalFile> listFiles(String path, Current __current)
            throws ServerError {
        File file = checkPath(path);
        List<File> files = Arrays.asList(file.listFiles((FileFilter)FileFilterUtils.fileFileFilter()));
        return filesToOriginalFiles(files);
    }

    public OriginalFile load(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public RawPixelsStorePrx pixels(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public RawFileStorePrx read(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public void rename(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public RenderingEnginePrx render(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public ThumbnailStorePrx thumbs(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public void transfer(String srcPath, RepositoryPrx target,
            String targetPath, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public RawFileStorePrx write(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public List<OriginalFile> listKnown(String path, Current __current)
            throws ServerError {
        File file = checkPath(path);
        List<File> files = Arrays.asList(file.listFiles());
        return knownOriginalFiles(files);
    }

    public List<OriginalFile> listKnownDirs(String path, Current __current)
            throws ServerError {
        File file = checkPath(path);
        List<File> files = Arrays.asList(file.listFiles((FileFilter)FileFilterUtils.directoryFileFilter()));
        return knownOriginalFiles(files);
    }

    public List<OriginalFile> listKnownFiles(String path, Current __current)
            throws ServerError {
        File file = checkPath(path);
        List<File> files = Arrays.asList(file.listFiles((FileFilter)FileFilterUtils.fileFileFilter()));
        return knownOriginalFiles(files);
    }

    public Format format(String path, Current __current) throws ServerError {
        return getFileFormat(path);
    }

    //
    // Utilities
    //

    private File checkPath(String path) throws ValidationException {

        if (path == null || path.length() == 0) {
            throw new ValidationException(null, null, "Path is empty");
        }

        boolean found = false;
        File file = new File(path).getAbsoluteFile();
        while (true) {
            if (file.equals(root)) {
                found = true;
                break;
            }
            file = file.getParentFile();
            if (file == null) {
                break;
            }
        }

        if (!found) {
            throw new ValidationException(null, null, path + " is not within "
                    + root.getAbsolutePath());
        }

        return new File(path).getAbsoluteFile();
    }
    
    // Just return a dummy format for testing purposes.
    private Format getFileFormat(String path) {
        Format dummy = new FormatI();
        dummy.setValue(rstring("DUMMY-FORMAT"));
        return dummy;
    }

    private List<String> filesToPaths(Collection<File> files) {
        List rv = new ArrayList<String>();
        for (File f : files) {
            rv.add(f.getAbsolutePath());
        }
        return rv;
    }
    
    private List<OriginalFile> filesToOriginalFiles(Collection<File> files) {
        List rv = new ArrayList<OriginalFile>();
        for (File f : files) {
            rv.add(createOriginalFile(f));
        }
        return rv;
    }
    
    private List<OriginalFile> knownOriginalFiles(Collection<File> files) {
        List rv = new ArrayList<OriginalFile>();
/*        for (File f : files) {
            OriginalFile file = getOriginalFileOrNull(f.getAbsolutePath());
            if (file != null) {
                rv.add(file);
            }
        } */
        return rv;
    }
    

    private OriginalFile createOriginalFile(File f) {
        // How do I confirm if an OriginalFle is already registered?
        // Is that necessary here?
        OriginalFile file = new OriginalFileI();
        file.setPath(rstring(f.getAbsolutePath()));
        file.setSize(rlong(f.length()));
        file.setSha1(rstring("UNKNOWN"));
        // What more do I need to set here, times, details?
        
        // This needs to be unique - see #
        file.setName(rstring(f.getAbsolutePath()));
        
        // How should I get the format of a file?
        file.setFormat(getFileFormat(f.getAbsolutePath()));
        
        return file;
    }
    
    // THIS FAILS AT PRESENT. NEED TO SORT OUT THE RETURN TYPE FROM THE QUERY 
    //     ome.model.core.OriginalFile vs omero.model.OriginalFile
    // Very weak at present, returns one file.
    // No checking further for uniqueness
    private OriginalFile getOriginalFileOrNull(String path) {
        try {
            final String queryString = "from OriginalFile as o where o.path = '"
                    + path + "'";
            OriginalFile file = (OriginalFile) executor.execute(
                    principal, new Executor.SimpleWork(this,
                            "getOriginalFileOrNull") {

                        @Transactional(readOnly = true)
                        public Object doWork(Session session, ServiceFactory sf) {
                            return sf.getQueryService().findByQuery(
                                    queryString, null);
                        }
                    });
            return file;
        } catch (RuntimeException re) {
            return null;
        }
    }



}
