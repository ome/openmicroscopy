/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo;

import static omero.rtypes.rstring;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.tools.javac.util.Pair;

import Ice.Current;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.StringTransformer;

import omero.ServerError;
import omero.grid.ImportLocation;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportSettings;
import omero.grid._ManagedRepositoryOperations;
import omero.grid._ManagedRepositoryTie;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.FilesetI;
import omero.model.FilesetJobLink;
import omero.model.FilesetVersionInfo;
import omero.model.FilesetVersionInfoI;
import omero.model.IndexingJobI;
import omero.model.Job;
import omero.model.MetadataImportJob;
import omero.model.MetadataImportJobI;
import omero.model.PixelDataJobI;
import omero.model.ThumbnailGenerationJob;
import omero.model.ThumbnailGenerationJobI;
import omero.model.UploadJob;
import omero.sys.EventContext;

/**
 * Extension of the PublicRepository API which only manages files
 * under ${omero.data.dir}/ManagedRepository.
 *
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 * @author Josh Moore, josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.5
 */
public class ManagedRepositoryI extends PublicRepositoryI
    implements _ManagedRepositoryOperations {

    private final static Log log = LogFactory.getLog(ManagedRepositoryI.class);
    
    private final static int parentDirsToRetain = 3;

    private final String template;

    private final ProcessContainer processes;

    /**
     * Fields used in date-time calculations.
     */
    private static final DateFormatSymbols DATE_FORMAT;

    static {
        DATE_FORMAT = new DateFormatSymbols();
    }

    /**
     * Creates a {@link ProcessContainer} internally that will not be managed
     * by background threads. Used primarily during testing.
     * @param template
     * @param dao
     */
    public ManagedRepositoryI(String template, RepositoryDao dao) throws Exception {
        this(template, dao, new ProcessContainer());
    }

    public ManagedRepositoryI(String template, RepositoryDao dao,
            ProcessContainer processes) throws Exception {
        super(dao);
        this.template = template;
        this.processes = processes;
        log.info("Repository template: " + this.template);
    }

    @Override
    public Ice.Object tie() {
        return new _ManagedRepositoryTie(this);
    }

    //
    // INTERFACE METHODS
    //

    /**
     * Return a template based directory path. The path will be created
     * by calling {@link #makeDir(String, boolean, Ice.Current)}. Any exception will
     * be handled by incrementing some part of the template to create a viable
     * directory.
     *
     * @FIXME For the moment only the top-level directory is being incremented.
     */
    public ImportProcessPrx importFileset(Fileset fs, ImportSettings settings,
            Ice.Current __current) throws omero.ServerError {

        if (fs == null || fs.sizeOfUsedFiles() < 1) {
            throw new omero.ApiUsageException(null, null, "No paths provided");
        }

        if (settings == null) {
            settings = new ImportSettings(); // All defaults.
        }

        final List<FsFile> paths = new ArrayList<FsFile>();
        for (FilesetEntry entry : fs.copyUsedFiles()) {
            paths.add(new FsFile(entry.getClientPath().getValue()));
        }

        // This is the first part of the string which comes after:
        // ManagedRepository/, e.g. %user%/%year%/etc.
        FsFile relPath = new FsFile(expandTemplate(template, __current));
        // at this point, relPath should not yet exist on the filesystem

        // Possibly modified relPath.
        relPath = createTemplateDir(relPath, __current);
        
        // The next part of the string which is chosen by the user:
        // /home/bob/myStuff
        FsFile basePath = commonRoot(paths);

        // If any two files clash in that chosen basePath directory, then
        // we want to suggest a similar alternative.
        ImportLocation location =
                suggestOnConflict(relPath, basePath, paths, __current);

        return createImportProcess(fs, location, settings, __current);
    }

    public ImportProcessPrx importPaths(List<String> paths,
            Ice.Current __current) throws ServerError {

        if (paths == null || paths.size() < 1) {
            throw new omero.ApiUsageException(null, null, "No paths provided");
        }

        final ImportContainer container = new ImportContainer(
                null /*file*/, null /*target*/, null /*userPixels*/,
                "Unknown" /*reader*/, paths.toArray(new String[0]),
                false /*spw*/);

        final ImportSettings settings = new ImportSettings();
        final Fileset fs = new FilesetI();
        container.fillData(new ImportConfig(), settings, fs);

        return importFileset(fs, settings, __current);
    }

    public List<ImportProcessPrx> listImports(Ice.Current __current) throws omero.ServerError {

        final List<Long> filesetIds = new ArrayList<Long>();
        final List<ImportProcessPrx> proxies = new ArrayList<ImportProcessPrx>();
        final EventContext ec = repositoryDao.getEventContext(__current);
        final List<ProcessContainer.Process> ps
            = processes.listProcesses(ec.memberOfGroups);

        for (final ProcessContainer.Process p : ps) {
            filesetIds.add(p.getFileset().getId().getValue());
        }

        final List<Fileset> filesets
            = repositoryDao.loadFilesets(filesetIds, __current);

        for (Fileset fs : filesets) {
            if (!fs.getDetails().getPermissions().canEdit()) {
                filesetIds.remove(fs.getId().getValue());
            }
        }

        for (final ProcessContainer.Process p : ps) {
            if (filesetIds.contains(p.getFileset().getId())) {
                proxies.add(p.getProxy());
            }
        }

        return proxies;
    }

    //
    // HELPERS
    //

    /**
     * Creating the process will register itself in an appropriate
     * container (i.e. a SessionI or similar) for the current
     * user and therefore this instance no longer needs to worry
     * about the maintenance of the object.
     */
    protected ImportProcessPrx createImportProcess(Fileset fs,
            ImportLocation location, ImportSettings settings, Current __current)
                throws ServerError {

        // Initialization version info
        final ImportConfig config = new ImportConfig();
        final FilesetVersionInfo serverVersionInfo = new FilesetVersionInfoI();
        serverVersionInfo.setBioformatsReader(rstring("Unknown"));
        config.fillVersionInfo(serverVersionInfo);

        // Create and validate jobs
        if (fs.sizeOfJobLinks() != 1) {
            throw new omero.ValidationException(null, null,
                    "Found more than one job link. "+
                    "Link only updateJob on creation!");
        }
        final FilesetJobLink jobLink = fs.getFilesetJobLink(0);
        final Job job = jobLink.getChild();
        if (job == null) {
            throw new omero.ValidationException(null, null,
                    "Found null-UploadJob on creation");
        }
        if (!(job instanceof UploadJob)) {
            throw new omero.ValidationException(null, null,
                    "Found non-UploadJob on creation: "+
                    job.getClass().getName());
        }

        MetadataImportJob metadata = new MetadataImportJobI();
        metadata.setVersionInfo(serverVersionInfo);
        fs.linkJob(metadata);

        fs.linkJob(new PixelDataJobI());

        if (settings.doThumbnails != null && settings.doThumbnails.getValue()) {
            ThumbnailGenerationJob thumbnail = new ThumbnailGenerationJobI();
            fs.linkJob(thumbnail);
        }

        fs.linkJob(new IndexingJobI());

        // Create CheckedPath objects for use by saveFileset
        final int size = fs.sizeOfUsedFiles();
        final List<CheckedPath> checked = new ArrayList<CheckedPath>();
        for (int i = 0; i < size; i++) {
            final String path = location.sharedPath + FsFile.separatorChar + location.usedFiles.get(i);
            checked.add(checkPath(path, __current));
        }

        final Fileset managedFs = repositoryDao.saveFileset(getRepoUuid(), fs, checked, __current);
        // Since the fileset saved validly, we create a session for the user
        // and return the process.

        final ManagedImportProcessI proc = new ManagedImportProcessI(this, managedFs,
                location, settings, __current);
        processes.addProcess(proc);
        return proc.getProxy();
    }

    /**
     * From a list of paths, calculate the common root path that all share. In
     * the worst case, that may be "/". May not include the last element, the filename.
     * @param some paths
     * @return the paths' common root
     */
    protected FsFile commonRoot(List<FsFile> paths) {
        final List<String> commonRoot = new ArrayList<String>();
        int index = 0;
        boolean isCommon = false;
        while (true) {
            String component = null;
            for (final FsFile path : paths) {
                final List<String> components = path.getComponents();
                if (components.size() <= index + 1)  // prohibit very last component
                    isCommon = false;  // not long enough
                else if (component == null) {
                    component = components.get(index);
                    isCommon = true;  // first path
                } else  // subsequent long-enough path
                    isCommon = component.equals(components.get(index));
                if (!isCommon)
                    break;
            }
            if (isCommon)
                commonRoot.add(paths.get(0).getComponents().get(index++));
            else
                break;
        }
        return new FsFile(commonRoot);
    }

    /**
     * Turn the current template into a relative path. Makes use of the data
     * returned by {@link #replacementMap(Ice.Current)}.
     *
     * @param curr
     * @return
     */
    protected String expandTemplate(final String template, Ice.Current curr) {

        if (template == null) {
            return ""; // EARLY EXIT.
        }

        final Map<String, String> map = replacementMap(curr);
        final StrSubstitutor strSubstitutor = new StrSubstitutor(
                new StrLookup() {
                    @Override
                    public String lookup(final String key) {
                        return map.get(key);
                    }
                }, "%", "%", '%');
        return strSubstitutor.replace(template);
    }

    /**
     * Generates a map with most of the fields (as strings) from the
     * {@link EventContext} for the current user as well as fields from
     * a current {@link Calendar} instance. Implementors need only
     * provide the fields that are used in their templates. Any keys that
     * cannot be found by {@link #expandeTemplate(String, Ice.Current)} will
     * remain untouched.
     *
     * @param curr
     * @return
     */
    protected Map<String, String> replacementMap(Ice.Current curr) {
        final EventContext ec = this.repositoryDao.getEventContext(curr);
        final Map<String, String> map = new HashMap<String, String>();
        final Calendar now = Calendar.getInstance();
        map.put("user", ec.userName);
        map.put("userId", Long.toString(ec.userId));
        map.put("group", ec.groupName);
        map.put("groupId", Long.toString(ec.groupId));
        map.put("year", Integer.toString(now.get(Calendar.YEAR)));
        map.put("month", String.format("%02d", now.get(Calendar.MONTH)+1));
        map.put("monthname", DATE_FORMAT.getMonths()[now.get(Calendar.MONTH)]);
        map.put("day", String.format("%02d", now.get(Calendar.DAY_OF_MONTH)));
        map.put("session", ec.sessionUuid);
        map.put("sessionId", Long.toString(ec.sessionId));
        map.put("eventId", Long.toString(ec.eventId));
        map.put("perms", ec.groupPermissions.toString());
        map.put("filesetId", Long.toString(System.currentTimeMillis() - 1359540000000L));  // TODO: new file set ID
        return map;
    } 

    /**
     * Take the relative path created by
     * {@link #expandTemplate(String, Ice.Current)} and call
     * {@link makeDir(String, boolean, Ice.Current)} on each element of the path
     * starting at the top, until all the directories have been created.
     * After any exception, append an increment so that a writeable directory
     * exists for the current caller context.
     */
    protected FsFile createTemplateDir(FsFile relPath, Ice.Current curr) throws ServerError {
        final List<String> givenPath = relPath.getComponents();
        if (givenPath.isEmpty())
            throw new IllegalArgumentException("no template directory");
        final List<String> adjustedPath = new ArrayList<String>(givenPath.size());
        for (final String givenComponent : givenPath) {
//            int version = 0;  // TODO: should be obsolete
//            while (true) {
//                if (version == 0)
                    adjustedPath.add(givenComponent);
//                else
//                    adjustedPath.add(givenComponent + "__" + version);
                try {
                    makeDir(new FsFile(adjustedPath).toString(), false, curr);
//                    break;  // success
                } catch (omero.ServerError e) {
//                    log.debug("Error on createTemplateDir", e);
//                    adjustedPath.remove(adjustedPath.size() - 1);
//                    if (version++ > 10000) {
//                        log.debug("too many version increments in creation of template directory", e);
//                        throw e;
//                    }
//                }
            }
        }
        // without version suffixing, should now equal relPath
        return new FsFile(adjustedPath);
    }

    /** Return value for {@link #trimPaths}. */
    private static class Paths {
        final FsFile basePath;
        final List<FsFile> fullPaths;
        
        Paths(FsFile basePath, List<FsFile> fullPaths) {
            this.basePath = basePath;
            this.fullPaths = fullPaths;
        }
    }
    
    /**
     * Trim off the start of long client-side paths.
     * @param basePath the common root
     * @param fullPaths the full paths from the common root down to the filename
     * @return possibly trimmed common root and full paths
     */
    protected Paths trimPaths(FsFile basePath, List<FsFile> fullPaths) {
        int smallestPathLength;
        if (fullPaths.isEmpty())
            smallestPathLength = 1; /* imaginary file name */
        else {
            smallestPathLength = Integer.MAX_VALUE;
            for (final FsFile path : fullPaths) {
                final int pathLength = path.getComponents().size();
                if (smallestPathLength > pathLength)
                    smallestPathLength = pathLength;
            }
        }
        final List<String> basePathComponents = basePath.getComponents();
        int baseDirsToTrim = smallestPathLength - parentDirsToRetain - (1 /* file name */);
        if (baseDirsToTrim < 0)
            return new Paths(basePath, fullPaths);
        basePath = new FsFile(basePathComponents.subList(baseDirsToTrim, basePathComponents.size()));
        final List<FsFile> trimmedPaths = new ArrayList<FsFile>(fullPaths.size());
        for (final FsFile path : fullPaths) {
            final List<String> pathComponents = path.getComponents();
            trimmedPaths.add(new FsFile(pathComponents.subList(baseDirsToTrim, pathComponents.size())));
        }
        return new Paths(basePath, trimmedPaths);
    }
    
    /**
     * Take a relative path that the user would like to see in his or her
     * upload area, and check that none of the suggested paths currently
     * exist in that location. If they do, then append an incrementing version
     * number to the path ("/my/path/" becomes "/my-1/path" then "/my-2 /path")
     * at the highest part of the path possible.
     *
     * @param relPath Path parsed from the template
     * @param basePath Common base of all the listed paths ("/my/path")
     * @return {@link ImportLocation} instance with the suggested new basePath in the
     *          case of conflicts.
     */
    protected ImportLocation suggestOnConflict(FsFile relPath,
            FsFile basePath, List<FsFile> paths, Ice.Current __current)
            throws omero.ServerError {
        final Paths trimmedPaths = trimPaths(basePath, paths);
        basePath = trimmedPaths.basePath;
        paths = trimmedPaths.fullPaths;
        
        // sanitize paths (should already be sanitary; could introduce conflicts)
        final StringTransformer sanitizer = serverPaths.getPathSanitizer();
        relPath = relPath.transform(sanitizer);
        basePath = basePath.transform(sanitizer);
        int index = paths.size();
        while (--index >= 0)
            paths.set(index, paths.get(index).transform(sanitizer));
        
        // Static elements which will be re-used throughout
        final ManagedImportLocationI data = new ManagedImportLocationI(); // Return value

        // State that will be updated per loop.
        // TODO: this becomes obsolete, the template should guarantee uniqueness
        Integer version = null;

        OUTER:
        while (true) {
            // note common root of used files, including any non-conflict suffix at the end
            final FsFile endPart;
            if (version != null) {
                final List<String> components = new ArrayList<String>(basePath.getComponents());
                final int toSuffix = components.size() - 1;
                if (toSuffix < 0)
                    throw new IllegalArgumentException("no last component to suffix");
                components.set(toSuffix, components.get(toSuffix) + "__" + version);
                endPart = new FsFile(components);
            } else 
                endPart = basePath;

            // check for conflict, adjust version number
            for (final FsFile path : paths) {
                final FsFile relative = path.getPathFrom(basePath);
                final FsFile repoPath = FsFile.concatenate(relPath, endPart, relative);
                if ((serverPaths.getServerFileFromFsFile(repoPath)).exists()) {
                    if (version == null) {
                        version = 1;
                    } else {
                        version = version + 1;
                    }
                    continue OUTER;
                }
            }
        
            // try actually making directories, for failure adjust version number
            final FsFile newBase = FsFile.concatenate(relPath, endPart);
            data.sharedPath = newBase.toString();
            data.usedFiles = new ArrayList<String>(paths.size());
            data.checkedPaths = new ArrayList<CheckedPath>(paths.size());
            for (final FsFile path : paths) {
                final String relativeToEnd = path.getPathFrom(endPart).toString();
                data.usedFiles.add(relativeToEnd);
                final String fullRepoPath = data.sharedPath + FsFile.separatorChar + relativeToEnd;
                data.checkedPaths.add(new CheckedPath(this.serverPaths, fullRepoPath));
            }
            try {
                makeDir(data.sharedPath, true, __current);
                break;  // success
            } catch (omero.ServerError se) {
                log.debug("Trying next directory", se);
                // This directory apparently belongs to some other group
                // or is not readable in the current context.
                if (version == null) {
                    version = 1;
                } else {
                    version = version + 1;
                }
            }
        }

        // Assuming we reach here, then we need to make
        // sure that the directory exists since the call
        // to saveFileset() requires the parent dirs to
        // exist.
        for (CheckedPath checked : data.checkedPaths) {
            makeDir(checked.getRelativePath(), true, __current);
        }

        return data;
    }
}
