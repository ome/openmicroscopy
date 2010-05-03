/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scripts;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ome.conditions.InternalException;
import ome.model.core.OriginalFile;
import ome.model.meta.ExperimenterGroup;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.util.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.EmptyFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.type.StringType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Transactional;

/**
 * Strategy used by the ScriptRepository for registering, loading, and saving
 * files.
 *
 * @since Beta4.2
 */
public class ScriptRepoHelper {

    /**
     * Id used by all script repositories. Having a well defined string allows
     * for various repositories to all provide the same functionality.
     */
    public final static String SCRIPT_REPO = "ScriptRepo";

    /**
     * {@link IOFileFilter} instance used during {@link #iterate()} to find the
     * matching scripts in the given directory.
     */
    public final static IOFileFilter SCRIPT_FILTER = new AndFileFilter(Arrays
            .asList(new FileFilter[] { EmptyFileFilter.NOT_EMPTY,
                    HiddenFileFilter.VISIBLE, CanReadFileFilter.CAN_READ,
                    new WildcardFileFilter("*.py") }));

    private final String uuid;

    private final File dir;

    private final Executor ex;

    private final Principal p;

    private final Roles roles;

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see #ScriptRepoHelper(String, File, Executor, Principal)
     */
    public ScriptRepoHelper(Executor ex, String sessionUuid, Roles roles) {
        this(new File(getDefaultScriptDir()), ex, new Principal(sessionUuid),
                roles);
    }

    /**
     * @see #ScriptRepoHelper(String, File, Executor, Principal)
     */
    public ScriptRepoHelper(File dir, Executor ex, Principal p, Roles roles) {
        this(SCRIPT_REPO, dir, ex, p, roles);
    }

    /**
     *
     * @param uuid
     *            Allows setting a non-default uuid for this script service.
     *            Primarily for testing, since services rely on the repository
     *            name for finding one another.
     * @param dir
     *            The directory used by the repo as its root. Other constructors
     *            use {@link #getDefaultScriptDir()} internally.
     * @param ex
     * @param p
     */
    public ScriptRepoHelper(String uuid, File dir, Executor ex, Principal p,
            Roles roles) {
        this.roles = roles;
        this.uuid = uuid;
        this.dir = dir;
        this.ex = ex;
        this.p = p;
        if (dir == null) {
            throw new InternalException("Null dir!");
        }
        if (!dir.exists()) {
            throw new InternalException("Does not exist: "
                    + dir.getAbsolutePath());
        }
        if (!dir.canRead()) {
            throw new InternalException("Cannot read: " + dir.getAbsolutePath());
        }
    }

    /**
     * Directory which will be used as the root of this repository if no
     * directory is passed to a constructor. Equivalent to "lib/scripts" from
     * the current directory.
     */
    public static String getDefaultScriptDir() {
        File current = new File(".");
        File lib = new File(current, "lib");
        File scripts = new File(lib, "scripts");
        return scripts.getAbsolutePath();
    }

    /**
     * Returns the actual root of this repository.
     *
     * @see #getDefaultScriptDir()
     */
    public String getScriptDir() {
        return dir.getAbsolutePath();
    }

    /**
     * Uuid of this repository. In the normal case, this will equal
     * {@link #SCRIPT_REPO}.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the number of files which match {@link #SCRIPT_FILTER} in
     * {@link #dir}. Uses {@link #iterate()} internally.
     */
    public int countOnDisk() {
        int size = 0;
        Iterator<File> it = iterate();
        while (it.hasNext()) {
            File f = it.next();
            if (f.canRead() && f.isFile() && !f.isHidden()) {
                size++;
            }
        }
        return size;
    }

    public int countInDb() {
        return (Integer) ex.executeStateless(new Executor.SimpleStatelessWork(
                this, "countInDb") {
            @Transactional(readOnly = true)
            public Object doWork(SimpleJdbcOperations jdbc) {
                return countInDb(jdbc);
            }
        });
    }

    public int countInDb(SimpleJdbcOperations jdbc) {
        return jdbc.queryForInt("select count(id) from originalfile "
                + "where repo = ?", uuid);
    }

    @SuppressWarnings("unchecked")
    public List<Long> idsInDb() {
        return (List<Long>) ex
                .executeStateless(new Executor.SimpleStatelessWork(this,
                        "idsInDb") {
                    @Transactional(readOnly = true)
                    public Object doWork(SimpleJdbcOperations jdbc) {
                        return idsInDb(jdbc);
                    }
                });
    }

    public List<Long> idsInDb(SimpleJdbcOperations jdbc) {
        try {
            return (List<Long>) jdbc.query("select id from originalfile "
                    + "where repo = ?", new RowMapper<Long>() {
                public Long mapRow(ResultSet arg0, int arg1)
                        throws SQLException {
                    return arg0.getLong(1);
                }
            }, uuid);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public boolean isInRepo(final long id) {
        return (Boolean) ex.executeStateless(new Executor.SimpleStatelessWork(
                this, "isInRepo", id) {
            @Transactional(readOnly = true)
            public Object doWork(SimpleJdbcOperations jdbc) {
                return isInRepo(jdbc, id);
            }
        });
    }

    public boolean isInRepo(SimpleJdbcOperations jdbc, final long id) {
        try {
            int count = jdbc.queryForInt("select count(id) from originalfile "
                    + "where repo = ? and id = ?", uuid, id);
            return count > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Long findInDb(final String path, final boolean relative) {
        return (Long) ex.executeStateless(new Executor.SimpleStatelessWork(
                this, "findInDb", path) {
            @Transactional(readOnly = true)
            public Object doWork(SimpleJdbcOperations jdbc) {
                return findInDb(jdbc, path, relative);
            }
        });
    }

    /**
     * Looks to see if a path is contained in the repository.
     */
    public Long findInDb(SimpleJdbcOperations jdbc, String path,
            boolean relative) {

        final RepoFile repoFile = build(path, relative);

        try {
            return jdbc.queryForLong("select id from originalfile "
                    + "where repo = ? and path = ? and name = ?",
                    uuid, repoFile.dirname(), repoFile.basename());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Iterator<File> iterate() {
        return FileUtils.iterateFiles(dir, SCRIPT_FILTER, TrueFileFilter.TRUE);
    }

    /**
     *
     * @param modificationCheck
     * @return
     */
    public List<OriginalFile> loadAll(boolean modificationCheck) {
        final Iterator<File> it = iterate();
        final List<OriginalFile> rv = new ArrayList<OriginalFile>();
        File f = null;
        FsFile file = null;
        while (it.hasNext()) {
            f = it.next();
            file = new FsFile(f);
            rv.add(load(file, modificationCheck));
        }
        return rv;
    }

    public OriginalFile load(FsFile file, boolean modificationCheck) {
        Long id;
        String sha1;
        OriginalFile obj;
        id = findInDb(file.path, false);
        if (id == null) {
            obj = addOrReplace(file, null);
        } else {
            obj = load(id, false);
            if (modificationCheck) {
                sha1 = file.sha1();
                if (!sha1.equals(obj.getSha1())) {
                    obj = addOrReplace(file, id);
                }
            }
        }
        return obj;
    }

    protected OriginalFile addOrReplace(final FsFile fsFile, final Long old) {
        final RepoFile repoFile = new RepoFile(dir, fsFile);
        return (OriginalFile) ex.execute(p, new Executor.SimpleWork(this,
                "addOrReplace", fsFile.path, old) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                if (old != null) {
                    session.createSQLQuery(
                            "update originalfile set repo = ? where id = ?")
                            .setParameter(0, null, new StringType())
                            .setParameter(1, old).executeUpdate();
                }

                OriginalFile ofile = new OriginalFile();
                ofile.setPath(repoFile.dirname());
                ofile.setName(repoFile.basename());
                ofile.setSha1(fsFile.sha1());
                ofile.setSize(fsFile.length());
                ofile.setMimetype("text/x-python");
                ofile.getDetails().setGroup(
                        new ExperimenterGroup(roles.getUserGroupId(), false));
                ofile = sf.getUpdateService().saveAndReturnObject(ofile);

                session.createSQLQuery(
                        "update originalfile set repo = ? where id = ?")
                        .setParameter(0, uuid).setParameter(1, ofile.getId())
                        .executeUpdate();
                return ofile;
            }
        });
    }

    public String read(String path, boolean relative) throws IOException {
        final RepoFile repo = build(path, relative);
        return FileUtils.readFileToString(repo.file());
    }

    public RepoFile write(String path, String text, boolean relative,
            boolean load) throws IOException {
        File f = null;
        if (relative) {
            f = new File(dir, path);
        } else {
            f = new File(path);
        }
        FileUtils.writeStringToFile(f, text);
        FsFile fs = new FsFile(f.getAbsolutePath());
        RepoFile repo = new RepoFile(dir, fs);
        if (load) {
            load(fs, true);
        }
        return repo;
    }

    public OriginalFile load(final long id, final boolean check) {
        return (OriginalFile) ex.execute(p, new Executor.SimpleWork(this,
                "load", id) {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return load(id, session, check);
            }
        });
    }

    public OriginalFile load(final long id, Session s, boolean check) {
        if (check) {
            String repo = (String) s.createSQLQuery(
                    "select repo from OriginalFile where id = ?")
                    .setParameter(0, id)
                    .uniqueResult();
            if (!uuid.equals(repo)) {
                return null;
            }
        }
        return (OriginalFile) s.get(OriginalFile.class, id);
    }

    /**
     * Checks if
     */
    public void modificationCheck() {
        loadAll(true);
    }

    public boolean delete(long id) {

        final OriginalFile file = load(id, true);
        if (file == null) {
            return false;
        }

        ex.execute(p, new Executor.SimpleWork(this, "delete", id) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                sf.getUpdateService().deleteObject(file);
                return null;
            }
        });

        FileUtils.deleteQuietly(new File(dir, file.getPath()));
        return true;
    }

    // Filetype classes
    // =========================================================================

    public RepoFile build(String path, boolean relative) {
        File f = null;
        if (relative) {
            f = new File(dir, path);
        } else {
            f = new File(path);
        }
        final FsFile fsFile = new FsFile(f.getAbsolutePath());
        final RepoFile repoFile = new RepoFile(dir, fsFile);
        return repoFile;
    }

    /**
     * File type wrapper for paths which are intended for being stored in the
     * database as a part of this repository.
     */
    public static class RepoFile {

        final public FsFile fs;
        final public String rel;
        final public String root;
        final private String absPath;

        public RepoFile(File root, FsFile file) {
            this.fs = file;
            this.root = FilenameUtils.normalize(root.getAbsolutePath());
            this.rel = fs.path.substring((int) this.root.length());
            this.absPath = new File(root, rel).getAbsolutePath();
        }

        public boolean matches(File file) {
            return FilenameUtils.equalsNormalizedOnSystem(absPath, file
                    .getAbsolutePath());
        }

        public File file() {
            return new File(fs.path);
        }

        public String basename() {
            return FilenameUtils.getName(rel);
        }

        public String dirname() {
            return FilenameUtils.getFullPath(rel);
        }
        @Override
        public String toString() {
            return super.toString() + ":" + this.rel;
        }

    }

    /**
     * File type wrapper for actual OS files.
     */
    public static class FsFile {

        final public String path;
        final public String name;

        public FsFile(String path) {
            this.path = FilenameUtils.normalize(path);
            this.name = FilenameUtils.getName(this.path);
        }

        FsFile(File file) {
            this(file.getAbsolutePath());
        }

        long length() {
            return new File(path).length();
        }

        String sha1() {
            return Utils.bytesToHex(Utils.pathToSha1(path));
        }

        @Override
        public String toString() {
            return super.toString() + ":" + this.path;
        }

    }
}
