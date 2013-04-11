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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.RemovedSessionException;
import ome.model.core.OriginalFile;
import ome.model.meta.ExperimenterGroup;
import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
// import omero.util.TempFileManager;
// Note: This cannot be imported because
// it's in the blitz pacakge. TODO

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.EmptyFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.dao.EmptyResultDataAccessException;
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

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @see #ScriptRepoHelper(String, File, Executor, Principal)
     */
    public ScriptRepoHelper(Executor ex, String sessionUuid, Roles roles) {
        this(new File(getDefaultScriptDir()), ex, new Principal(sessionUuid),
                roles);
        try {
            loadAll(true);
        } catch (RemovedSessionException rse) {
            log.error("Script failure!!! RemovedSession on startup: are we testing?");
        }
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
        this.dir = sanityCheck(log, dir);
        this.ex = ex;
        this.p = p;
    }

    /**
     * If we're in a testing scenario we need to ignore the fact that there
     * is no lib/script directory. Otherwise, all devs will need to mkdir -p
     * that directory both at the top-level and under blitz/ etc.
     */
    static File sanityCheck(Logger log, File dir) {

        String error = null;
        String testing = System.getProperty("omero.testing", "false").toLowerCase();
        testing = testing.toLowerCase();

        if (dir == null) {
            throw new InternalException("Null dir!");
        }

        if (!dir.exists()) {
            error = "Does not exist: ";
        } else if (!dir.canRead()) {
            error = "Cannot read: ";
        }

        if (error != null) {
            if (testing.equals("true")) {
                log.error(error + dir.getAbsolutePath());
                try {
                    //dir = TempFileManager.create_path("lib", "scripts", true);
                    dir = getTmpDir();
                } catch (IOException e) {
                    throw new InternalException(
                            "Failed to make temp path for testing");
                }
            } else {
                throw new InternalException(error + dir.getAbsolutePath());
            }
        }

        return dir;
    }

    /**
     * This method creates a temporary directory under
     * ${java.io.tmpdir}/tmp_lib_scripts" which can be
     * used during testing. This method would be better
     * implemeneted using omero.util.TempFileManager
     * but that's currently not possible for packaging
     * reasons.
     */
    static File getTmpDir() throws IOException {
        String tmpDirName = System.getProperty("java.io.tmpdir", null);
        File tmpDir = new File(tmpDirName);
        File libDir = new File(tmpDir, "tmp_lib_scripts");
        File dir = File.createTempFile("lib", "scripts", tmpDir);
        dir.delete();
        dir.mkdirs();
        return dir;
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
        return (Integer) ex.executeSql(new Executor.SimpleSqlWork(
                this, "countInDb") {
            @Transactional(readOnly = true)
            public Object doWork(SqlAction sql) {
                return countInDb(sql);
            }
        });
    }

    public int countInDb(SqlAction sql) {
        return sql.repoScriptCount(uuid);
    }

    @SuppressWarnings("unchecked")
    public List<Long> idsInDb() {
        return (List<Long>) ex
                .executeSql(new Executor.SimpleSqlWork(this,
                        "idsInDb") {
                    @Transactional(readOnly = true)
                    public Object doWork(SqlAction sql) {
                        return idsInDb(sql);
                    }
                });
    }

    public List<Long> idsInDb(SqlAction sql) {
        try {
            return sql.fileIdsInDb(uuid);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public boolean isInRepo(final long id) {
        return (Boolean) ex.executeSql(new Executor.SimpleSqlWork(
                this, "isInRepo", id) {
            @Transactional(readOnly = true)
            public Object doWork(SqlAction sql) {
                return isInRepo(sql, id);
            }
        });
    }

    public boolean isInRepo(SqlAction sql, final long id) {
        try {
            int count = sql.isFileInRepo(uuid, id);
            return count > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Long findInDb(final String path, final boolean scriptsOnly) {
        RepoFile repoFile = new RepoFile(dir, path);
        return findInDb(repoFile, scriptsOnly);
    }

    public Long findInDb(final RepoFile file, final boolean scriptsOnly) {
        return (Long) ex.executeSql(new Executor.SimpleSqlWork(
                this, "findInDb", file, scriptsOnly) {
            @Transactional(readOnly = true)
            public Object doWork(SqlAction sql) {
                return findInDb(sql, file, scriptsOnly);
            }
        });
    }

    /**
     * Looks to see if a path is contained in the repository.
     */
    public Long findInDb(SqlAction sql, RepoFile repoFile, boolean scriptsOnly) {
        return sql.findRepoFile(uuid, repoFile.dirname(), repoFile.basename(),
                scriptsOnly ? "text/x-python" : null);
    }

    @SuppressWarnings("unchecked")
    public Iterator<File> iterate() {
        return FileUtils.iterateFiles(dir, SCRIPT_FILTER, TrueFileFilter.TRUE);
    }

    /**
     * Walks all files in the repository (via {@link #iterate()} and adds them
     * if not found in the database.
     *
     * If modificationCheck is true, then a change in the sha1 for a file in
     * the repository will cause the old file to be removed from the repository
     * <pre>(uuid == null)</pre> and a new file created in its place.
     *
     * @param modificationCheck
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<OriginalFile> loadAll(final boolean modificationCheck) {
        final Iterator<File> it = iterate();
        final List<OriginalFile> rv = new ArrayList<OriginalFile>();
        return (List<OriginalFile>) ex.execute(p, new Executor.SimpleWork(this,
                "loadAll", modificationCheck) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {

                SqlAction sqlAction = getSqlAction();

                File f = null;
                RepoFile file = null;

                while (it.hasNext()) {
                    f = it.next();
                    file = new RepoFile(dir, f);
                    Long id = findInDb(sqlAction, file, false); // non-scripts count
                    String sha1 = null;
                    OriginalFile ofile = null;
                    if (id == null) {
                        ofile = addOrReplace(sqlAction, sf, file, null);
                    } else {

                        ofile = load(id, session, getSqlAction(), true); // checks for type & repo
                        if (ofile == null) {
                            continue; // wrong type or similar
                        }

                        if (modificationCheck) {
                            sha1 = file.sha1();
                            if (!sha1.equals(ofile.getHash())) {
                                ofile = addOrReplace(sqlAction, sf, file, id);
                            }
                        }
                    }
                    rv.add(ofile);
                }
                removeMissingFilesFromDb(sqlAction, session, rv);
                return rv;
            }});
    }

    /**
     *
     * @param repoFile
     * @param old
     * @return
     */
    public OriginalFile addOrReplace(final RepoFile repoFile, final Long old) {
        return (OriginalFile) ex.execute(p, new Executor.SimpleWork(this,
                "addOrReplace", repoFile, old) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return addOrReplace(getSqlAction(), sf, repoFile, old);
            }
        });
    }

    protected OriginalFile addOrReplace(SqlAction sqlAction, ServiceFactory sf,
            final RepoFile repoFile, final Long old) {

        if (old != null) {
            unregister(old, sqlAction);
            log.info("Unregistered " + old);
        }

        OriginalFile ofile = new OriginalFile();
        return update(repoFile, sqlAction, sf, ofile);
    }

    /**
     * Given the current files on disk, {@link #unregister(Long, Session)}
     * all files which have been removed from disk.
     */
    public long removeMissingFilesFromDb(SqlAction sqlAction, Session session, List<OriginalFile> filesOnDisk) {
        List<Long> idsInDb = idsInDb(sqlAction);
        if (idsInDb.size() != filesOnDisk.size()) {
            log.info(String.format(
                    "Script missing from disk: %s in db, %s on disk!",
                    idsInDb.size(), filesOnDisk.size()));
        }

        Set<Long> setInDb = new HashSet<Long>();
        Set<Long> setOnDisk = new HashSet<Long>();

        setInDb.addAll(idsInDb);
        for (OriginalFile f : filesOnDisk) {
            setOnDisk.add(f.getId());
        }

        // Now contains only those which are missing
        setInDb.removeAll(setOnDisk);

        for (Long l : setInDb) {
            unregister(l, sqlAction);
        }

        return setInDb.size();
    }

    /**
     * Unregisters a given file from the script repository by setting its
     * Repo uuid to null.
     */
    protected void unregister(final Long old, SqlAction sqlAction) {
        sqlAction.setFileRepo(old, null);
    }

    public OriginalFile update(final RepoFile repoFile, final Long id) {
        return (OriginalFile) ex.execute(p, new Executor.SimpleWork(this,
                "update", repoFile, id) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                OriginalFile ofile = load(id, session, getSqlAction(), true);
                return update(repoFile, getSqlAction(), sf, ofile);
            }
        });
    }

    private OriginalFile update(final RepoFile repoFile, SqlAction sqlAction,
            ServiceFactory sf, OriginalFile ofile) {
        ofile.setPath(repoFile.dirname());
        ofile.setName(repoFile.basename());
        ofile.setHash(repoFile.sha1());
        ofile.setSize(repoFile.length());
        ofile.setMimetype("text/x-python");
        ofile.getDetails().setGroup(
                new ExperimenterGroup(roles.getUserGroupId(), false));
        ofile = sf.getUpdateService().saveAndReturnObject(ofile);

        sqlAction.setFileRepo(ofile.getId(), uuid);

        return ofile;
    }

    public String read(String path) throws IOException {
        final RepoFile repo = new RepoFile(dir, path);
        return FileUtils.readFileToString(repo.file());
    }

    public RepoFile write(String path, String text) throws IOException {
        RepoFile repo = new RepoFile(dir, path);
        return write(repo, text);
    }

    public RepoFile write(RepoFile repo, String text) throws IOException {
        FileUtils.writeStringToFile(repo.file(), text); // truncates itself. ticket:2337
        return repo;
    }

    public OriginalFile load(final long id, final boolean check) {
        return (OriginalFile) ex.execute(p, new Executor.SimpleWork(this,
                "load", id) {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return load(id, session, getSqlAction(), check);
            }
        });
    }

    public OriginalFile load(final long id, Session s, SqlAction sqlAction, boolean check) {
        if (check) {
            String repo = sqlAction.scriptRepo(id);
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

        simpleDelete(null, ex, p, id);

        FileUtils.deleteQuietly(new File(dir, file.getPath()));

        return true;
    }

    /**
     * Unlike {@link #delete(long)} this method simply performs the DB delete
     * on the given original file id.
     *
     * @param context 
     *                  Call context which affecets which group the current user is in. 
     *                  Can be null to pass no call context.
     * @param executor
     * @param p
     * @param id
     *                  Id of the {@link OriginalFile} to delete.
     */
    public void simpleDelete(Map<String, String> context, final Executor executor,
        Principal p, final long id) {

        Deletion deletion = (Deletion) executor.execute(context, p,
                new Executor.SimpleWork(this, "deleteOriginalFile") {

                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        try {
                            EventContext ec = ((LocalAdmin) sf.getAdminService())
                                .getEventContextQuiet();
                            Deletion d = executor.getContext().getBean(
                                Deletion.class.getName(), Deletion.class);
                            int steps = d.start(ec, getSqlAction(), session,
                                "/OriginalFile", id, null);
                            if (steps > 0) {
                                for (int i = 0; i < steps; i++) {
                                    d.execute(i);
                                }
                                return d;
                            }
                        } catch (ome.conditions.ValidationException ve) {
                            log.debug("ValidationException on delete", ve);
                        }
                        catch (GraphException ge) {
                            log.debug("GraphException on delete", ge);
                        }
                        catch (Throwable e) {
                            log.warn("Throwable while deleting script " + id, e);
                        }
                        return null;
                    }

                });

        if (deletion != null) {
            deletion.deleteFiles();
            deletion.stop();
        } else {
            throw new ApiUsageException("Cannot delete "
                    + id + "\nIs in use by other objects");
        }
    }
}
