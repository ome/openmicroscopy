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

import java.io.File;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import loci.formats.FormatReader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.services.blitz.gateway.services.util.ServiceUtilities;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.StringTransformer;

import omero.ResourceError;
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
 * @since 5.0
 */
public class ManagedRepositoryI extends PublicRepositoryI
    implements _ManagedRepositoryOperations {

    private final static Log log = LogFactory.getLog(ManagedRepositoryI.class);
    
    private final static int parentDirsToRetain = 3;
    
    /* This class is used in the server-side creation of import containers.
     * The suggestImportPaths method sanitizes the paths in due course.
     * From the server side, we cannot imitate ImportLibrary.createImport
     * in applying client-side specifics to clean up the path. */
    private static ClientFilePathTransformer nopClientTransformer = 
            new ClientFilePathTransformer(new StringTransformer() {
                // @Override  since JDK6
                public String apply(String from) {
                    return from;
                }
    });

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
     * by calling {@link #makeDir(String, boolean, Ice.Current)}.
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

        try {
            createTemplateDir(relPath, __current);
        } catch (Throwable t) {
            System.err.println(this.stackTraceAsString(t));
        }

        final Class<? extends FormatReader> readerClass = getReaderClass(fs, __current);
        
        // The next part of the string which is chosen by the user:
        // /home/bob/myStuff
        FsFile basePath = commonRoot(paths);

        // If any two files clash in that chosen basePath directory, then
        // we want to suggest a similar alternative.
        ImportLocation location =
                suggestImportPaths(relPath, basePath, paths, readerClass, __current);

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
        try {
            container.fillData(new ImportConfig(), settings, fs, nopClientTransformer);
        } catch (IOException e) {
            // impossible
            ServiceUtilities.handleException(e, "IO exception from operation without IO");
        }

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
     * Get the suggested BioFormats Reader for the given Fileset.
     * @param fs a fileset
     * @param __current the current ICE context
     * @return a reader class, or null if none could be found
     */
    protected Class<? extends FormatReader> getReaderClass(Fileset fs, Current __current) {
        for (final Job job : fs.linkedJobList()) {
            if (job instanceof UploadJob) {
                final FilesetVersionInfo versionInfo = ((UploadJob) job).getVersionInfo(__current);
                final String readerName = versionInfo.getBioformatsReader(__current).getValue();
                final Class<?> potentialReaderClass;
                try {
                    potentialReaderClass = Class.forName(readerName);
                } catch (ClassNotFoundException e) {
                    continue;
                }
                if (FormatReader.class.isAssignableFrom(potentialReaderClass)) {
                    return (Class<? extends FormatReader>) potentialReaderClass;
                }
            }
        }
        return null;
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
        // TODO: new import set ID
        map.put("importSetId", Long.toString(System.currentTimeMillis() - 1360000000000L));
        return map;
    } 

    /**
     * Take the relative path created by
     * {@link #expandTemplate(String, Ice.Current)} and call
     * {@link makeDir(String, boolean, Ice.Current)} on each element of the path
     * starting at the top, until all the directories have been created.
     */
    protected void createTemplateDir(FsFile relPath, Ice.Current curr) throws ServerError {
        final List<String> givenPath = relPath.getComponents();
        if (givenPath.isEmpty())
            throw new IllegalArgumentException("no template directory");
        for (int prefixSize = 1; prefixSize <= givenPath.size(); prefixSize++) {
            final List<String> pathPrefix = givenPath.subList(0, prefixSize);
            makeDir(new FsFile(pathPrefix).toString(), false, curr);
        }
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
     * @param readerClass BioFormats reader for data, may be null
     * @return possibly trimmed common root and full paths
     */
    protected Paths trimPaths(FsFile basePath, List<FsFile> fullPaths, 
            Class<? extends FormatReader> readerClass) {
        // find how many common parent directories to retain according to BioFormats
        Integer commonParentDirsToRetain = null;
        final String[] localStylePaths = new String[fullPaths.size()];
        int index = 0;
        for (final FsFile fsFile : fullPaths)
            localStylePaths[index++] = serverPaths.getServerFileFromFsFile(fsFile).getAbsolutePath();
        try {
            commonParentDirsToRetain = readerClass.newInstance().getRequiredDirectories(localStylePaths);
        } catch (Exception e) { }
        
        final List<String> basePathComponents = basePath.getComponents();
        final int baseDirsToTrim;
        if (commonParentDirsToRetain == null) {
            // no help from BioFormats
            
            // find the length of the shortest path, including file name
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
            
            // plan to trim to try to retain a certain number of parent directories
            baseDirsToTrim = smallestPathLength - parentDirsToRetain - (1 /* file name */);
        }
        else
            // plan to trim the common root according to BioFormats' suggestion
            baseDirsToTrim = basePathComponents.size() - commonParentDirsToRetain;
        if (baseDirsToTrim < 0)
            return new Paths(basePath, fullPaths);
        // actually do the trimming
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
     * upload area, and provide an import location instance whose paths
     * correspond to existing directories corresponding to the sanitized
     * file paths.
     * @param relPath Path parsed from the template
     * @param basePath Common base of all the listed paths ("/my/path")
     * @param reader BioFormats reader for data, may be null
     * @return {@link ImportLocation} instance
     */
    protected ImportLocation suggestImportPaths(FsFile relPath, FsFile basePath, List<FsFile> paths,
            Class<? extends FormatReader> reader, Ice.Current __current) throws omero.ServerError {
        final Paths trimmedPaths = trimPaths(basePath, paths, reader);
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

        // try actually making directories
        final FsFile newBase = FsFile.concatenate(relPath, basePath);
        data.sharedPath = newBase.toString();
        data.usedFiles = new ArrayList<String>(paths.size());
        data.checkedPaths = new ArrayList<CheckedPath>(paths.size());
        for (final FsFile path : paths) {
            final String relativeToEnd = path.getPathFrom(basePath).toString();
            data.usedFiles.add(relativeToEnd);
            final String fullRepoPath = data.sharedPath + FsFile.separatorChar + relativeToEnd;
            data.checkedPaths.add(new CheckedPath(this.serverPaths, fullRepoPath));
        }

        makeDir(data.sharedPath, true, __current);

        // Assuming we reach here, then we need to make
        // sure that the directory exists since the call
        // to saveFileset() requires the parent dirs to
        // exist.
        for (CheckedPath checked : data.checkedPaths) {
            makeDir(checked.getRelativePath(), true, __current);
        }

        return data;
    }

    /**
     * Checks for the top-level user directory restriction before calling
     * {@link PublicRepositoryI#makeCheckedDirs(LinkedList<CheckedPath>, bolean, Current)}
     */
    protected void makeCheckedDirs(final LinkedList<CheckedPath> paths,
            boolean parents, Current __current) throws ResourceError,
            ServerError {
        
        CheckedPath checked = paths.get(0);
        if (checked.isRoot) {
            // This shouldn't happen but just in case.
            throw new ResourceError(null, null, "Cannot re-create root!");
        } else if (checked.parent().isRoot) {
            // This is a top-level directory. This must equal
            // "%USERNAME%_%USERID%", in which case if it doesn't exist, it will
            // be created for the user in the "user" group so that it is
            // visible globally.
            String userDirectory = getUserDirectoryName(__current);
            if (!userDirectory.equals(checked.getName())) {
                throw new omero.ValidationException(null, null, String.format(
                        "User-directory name mismatch! (%s<>%s)",
                        userDirectory, checked.getName()));
                        
            }
            // Now that we know that this is the right directory for the
            // current user, we make sure that the directory exists and
            // is in the user group.
            repositoryDao.createOrFixUserDir(getRepoUuid(), checked, __current);
        }
        
        
        super.makeCheckedDirs(paths, parents, __current);
    }

    protected String getUserDirectoryName(Current __current) {
        EventContext ec = repositoryDao.getEventContext(__current);
        return String.format("%s_%s", ec.userName, ec.userId);
    }
}
