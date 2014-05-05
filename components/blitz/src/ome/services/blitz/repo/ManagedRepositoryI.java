/*
 * Copyright (C) 2012-2014 Glencoe Software, Inc. All rights reserved.
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

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loci.formats.FormatReader;

import ome.api.local.LocalAdmin;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.model.core.OriginalFile;
import ome.services.blitz.gateway.services.util.ServiceUtilities;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FilePathNamingValidator;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.util.ChecksumAlgorithmMapper;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import omero.RString;
import omero.ResourceError;
import omero.ServerError;
import omero.grid.ImportLocation;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportSettings;
import omero.grid._ManagedRepositoryOperations;
import omero.grid._ManagedRepositoryTie;
import omero.model.ChecksumAlgorithm;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.FilesetI;
import omero.model.FilesetJobLink;
import omero.model.IndexingJobI;
import omero.model.Job;
import omero.model.MetadataImportJob;
import omero.model.MetadataImportJobI;
import omero.model.PixelDataJobI;
import omero.model.ThumbnailGenerationJob;
import omero.model.ThumbnailGenerationJobI;
import omero.model.UploadJob;
import omero.sys.EventContext;
import omero.util.IceMapper;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import Ice.Current;

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

    private final static Logger log = LoggerFactory.getLogger(ManagedRepositoryI.class);
    
    private final static int parentDirsToRetain = 3;
    
    /* This class is used in the server-side creation of import containers.
     * The suggestImportPaths method sanitizes the paths in due course.
     * From the server side, we cannot imitate ImportLibrary.createImport
     * in applying client-side specifics to clean up the path. */
    private static final ClientFilePathTransformer nopClientTransformer =
            new ClientFilePathTransformer(new Function<String, String>() {
                // @Override  since JDK6
                public String apply(String from) {
                    return from;
                }
    });

    /* used for generating %monthname% for path templates */
    private static final DateFormatSymbols DATE_FORMAT = new DateFormatSymbols();

    /* referenced by only the bare-bones ManagedRepositoryI constructor used in testing */
    private static final String ALL_CHECKSUM_ALGORITHMS =
            Joiner.on(',').join(Collections2.transform(ChecksumAlgorithmMapper.getAllChecksumAlgorithms(),
                    ChecksumAlgorithmMapper.CHECKSUM_ALGORITHM_NAMER));

    private final FilePathNamingValidator filePathNamingValidator;

    private final String template;

    private final ProcessContainer processes;

    private final String rootSessionUuid;

    /**
     * Creates a {@link ProcessContainer} internally that will not be managed
     * by background threads. Used primarily during testing.
     * @param template
     * @param dao
     */
    public ManagedRepositoryI(String template, RepositoryDao dao) throws Exception {
        this(template, dao, new ProcessContainer(), new ChecksumProviderFactoryImpl(),
                ALL_CHECKSUM_ALGORITHMS, FilePathRestrictionInstance.UNIX_REQUIRED.name, null);
    }

    public ManagedRepositoryI(String template, RepositoryDao dao,
            ProcessContainer processes,
            ChecksumProviderFactory checksumProviderFactory,
            String checksumAlgorithmSupported,
            String pathRules,
            String rootSessionUuid) throws ServerError {
        super(dao, checksumProviderFactory, checksumAlgorithmSupported, pathRules);
        this.template = template;
        this.processes = processes;
        this.filePathNamingValidator = new FilePathNamingValidator(this.filePathRestrictions);
        this.rootSessionUuid = rootSessionUuid;
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
        if (settings.checksumAlgorithm == null) {
            throw new omero.ApiUsageException(null, null, "must specify checksum algorithm");
        }

        final List<FsFile> paths = new ArrayList<FsFile>();
        for (FilesetEntry entry : fs.copyUsedFiles()) {
            paths.add(new FsFile(entry.getClientPath().getValue()));
        }

        // This is the first part of the string which comes after:
        // ManagedRepository/, e.g. %user%/%year%/etc.
        final EventContext ec = repositoryDao.getEventContext(__current);
        final String templatePath = expandTemplate(template, ec);
        // check for the // split between root- and user-owned directories
        final FsFile rootPath, userPath;
        final int splitPoint = templatePath.lastIndexOf("//");
        if (splitPoint < 0) {
            rootPath = new FsFile();
            userPath = new FsFile(templatePath);
        } else {
            rootPath = new FsFile(templatePath.substring(0, splitPoint));
            userPath = new FsFile(templatePath.substring(splitPoint));
        }

        // at this point, the template path should not yet exist on the filesystem
        createTemplateDir(rootPath, userPath, __current);

        final FsFile relPath = FsFile.concatenate(rootPath, userPath);
        fs.setTemplatePrefix(rstring(relPath.toString() + FsFile.separatorChar));

        final Class<? extends FormatReader> readerClass = getReaderClass(fs, __current);
        
        // The next part of the string which is chosen by the user:
        // /home/bob/myStuff
        FsFile basePath = commonRoot(paths);

        // If any two files clash in that chosen basePath directory, then
        // we want to suggest a similar alternative.
        ImportLocation location =
                suggestImportPaths(relPath, basePath, paths, readerClass, settings.checksumAlgorithm, __current);

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
            settings.checksumAlgorithm = this.checksumAlgorithms.get(0);
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

    public List<ChecksumAlgorithm> listChecksumAlgorithms(Current __current) {
        return this.checksumAlgorithms;
    }

    public ChecksumAlgorithm suggestChecksumAlgorithm(List<ChecksumAlgorithm> supported, Current __current) {
        final Set<String> supportedNames =
                new HashSet<String>(Lists.transform(supported, ChecksumAlgorithmMapper.CHECKSUM_ALGORITHM_NAMER));
        for (final ChecksumAlgorithm configured : listChecksumAlgorithms(__current)) {
            if (supportedNames.contains(ChecksumAlgorithmMapper.CHECKSUM_ALGORITHM_NAMER.apply(configured))) {
                return configured;
            }
        }
        return null;
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
        final Map<String, RString> serverVersionInfo = new HashMap<String, RString>();
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

        if (location instanceof ManagedImportLocationI) {
            OriginalFile of = ((ManagedImportLocationI) location).getLogFile().asOriginalFile(IMPORT_LOG_MIMETYPE);
            of = persistLogFile(of, __current);
            job.linkOriginalFile((omero.model.OriginalFile) new IceMapper().map(of));
        }

        // Create CheckedPath objects for use by saveFileset
        final int size = fs.sizeOfUsedFiles();
        final List<CheckedPath> checked = new ArrayList<CheckedPath>();
        for (int i = 0; i < size; i++) {
            final String path = location.sharedPath + FsFile.separatorChar + location.usedFiles.get(i);
            checked.add(checkPath(path, settings.checksumAlgorithm, __current));
        }

        final Fileset managedFs = repositoryDao.saveFileset(getRepoUuid(), fs, settings.checksumAlgorithm, checked, __current);
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
                final Map<String, RString> versionInfo = ((UploadJob) job).getVersionInfo(__current);
                if (versionInfo == null || !versionInfo.containsKey(ImportConfig.VersionInfo.BIO_FORMATS_READER.key)) {
                    continue;
                }
                final String readerName = versionInfo.get(ImportConfig.VersionInfo.BIO_FORMATS_READER.key).getValue();
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
    protected String expandTemplate(final String template, EventContext ec) {

        if (template == null) {
            return ""; // EARLY EXIT.
        }

        final Map<String, String> map = replacementMap(ec);
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
    protected Map<String, String> replacementMap(EventContext ec) {
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
        map.put("time", String.format("%02d-%02d-%02d.%03d",
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND), now.get(Calendar.MILLISECOND)));
        map.put("session", ec.sessionUuid);
        map.put("sessionId", Long.toString(ec.sessionId));
        map.put("eventId", Long.toString(ec.eventId));
        map.put("perms", ec.groupPermissions.toString());
        return map;
    } 

    /**
     * Take the relative path created by
     * {@link #expandTemplate(String, Ice.Current)} and call
     * {@link makeDir(String, boolean, Ice.Current)} on each element of the path
     * starting at the top, until all the directories have been created.
     * The full path must not already exist, although a prefix of it may.
     */
    protected void createTemplateDir(FsFile rootPath, FsFile userPath, Ice.Current curr) throws ServerError {
        final int userPathSize = userPath.getComponents().size();
        if (userPathSize == 0) {
            throw new omero.ApiUsageException(null, null, "no directories in managed repository template path");
        }
        if (!FsFile.emptyPath.equals(rootPath)) {
            final Current rootCurr = sudo(curr, rootSessionUuid);
            makeDir(rootPath.toString(), true, rootCurr);
            userPath = FsFile.concatenate(rootPath, userPath);
        }
        if (userPathSize > 1) {
            final List<String> userPathPrefix = userPath.getComponents().subList(0, userPathSize - 1);
            makeDir(new FsFile(userPathPrefix).toString(), true, curr);
        }
        makeDir(userPath.toString(), false, curr);
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
     * @param checksumAlgorithm the checksum algorithm to use in verifying the integrity of uploaded files
     * @return {@link ImportLocation} instance
     */
    protected ImportLocation suggestImportPaths(FsFile relPath, FsFile basePath, List<FsFile> paths,
            Class<? extends FormatReader> reader, ChecksumAlgorithm checksumAlgorithm, Ice.Current __current) 
                    throws omero.ServerError {
        final Paths trimmedPaths = trimPaths(basePath, paths, reader);
        basePath = trimmedPaths.basePath;
        paths = trimmedPaths.fullPaths;

        // validate paths
        this.filePathNamingValidator.validateFilePathNaming(relPath);
        this.filePathNamingValidator.validateFilePathNaming(basePath);
        for (final FsFile path : paths) {
            this.filePathNamingValidator.validateFilePathNaming(path);
        }

        // Static elements which will be re-used throughout
        final ManagedImportLocationI data = new ManagedImportLocationI(); // Return value
        data.logFile = checkPath(relPath.toString()+".log", checksumAlgorithm, __current);

        // try actually making directories
        final FsFile newBase = FsFile.concatenate(relPath, basePath);
        data.sharedPath = newBase.toString();
        data.usedFiles = new ArrayList<String>(paths.size());
        data.checkedPaths = new ArrayList<CheckedPath>(paths.size());
        for (final FsFile path : paths) {
            final String relativeToEnd = path.getPathFrom(basePath).toString();
            data.usedFiles.add(relativeToEnd);
            final String fullRepoPath = data.sharedPath + FsFile.separatorChar + relativeToEnd;
            data.checkedPaths.add(new CheckedPath(this.serverPaths, fullRepoPath,
                    this.checksumProviderFactory, checksumAlgorithm));
        }

        // Assuming we reach here, then we need to make
        // sure that the directory exists since the call
        // to saveFileset() requires the parent dirs to
        // exist.
        List<CheckedPath> dirs = new ArrayList<CheckedPath>();
        Set<String> seen = new HashSet<String>();
        dirs.add(checkPath(data.sharedPath, checksumAlgorithm, __current));
        for (CheckedPath checked : data.checkedPaths) {
            if (!seen.contains(checked.getRelativePath())) {
                dirs.add(checked.parent());
                seen.add(checked.getRelativePath());
            }
        }
        repositoryDao.makeDirs(this, dirs, true, __current);

        return data;
    }

    /**
     * Checks for the top-level user directory restriction before calling
     * {@link PublicRepositoryI#makeCheckedDirs(LinkedList<CheckedPath>, boolean, Current)}
     */
    protected void makeCheckedDirs(final LinkedList<CheckedPath> paths,
            boolean parents, Session s, ServiceFactory sf, SqlAction sql)
                    throws ResourceError, ServerError {

        final ome.system.EventContext _ec
            = ((LocalAdmin) sf.getAdminService()).getEventContextQuiet();
        final EventContext ec = IceMapper.convert(_ec);
        final String expanded = expandTemplate(template, ec);
        final FsFile asfsfile = new FsFile(expanded);
        final List<String> components = asfsfile.getComponents();
        final List<CheckedPath> pathsToFix = new ArrayList<CheckedPath>();

        // hard-coded assumptions: the first element of the template must match
        // user_id and the last is unique in someway (and therefore won't be
        // handled specially.
        for (int i = 0; i < paths.size(); i++) {

            CheckedPath checked = paths.get(i);
            if (checked.isRoot) {
                // This shouldn't happen but just in case.
                throw new ResourceError(null, null, "Cannot re-create root!");
            }
            
            if (i>0 && i>(components.size()-1)) {
                // we always check at least one path element, but after that
                // we only need to check as far as one less than the size of
                // the template
                break;
            }

            pathsToFix.add(checked);
        }
        
        super.makeCheckedDirs(paths, parents, s, sf, sql);
        
        // Now that we know that these are the right directories for
        // the current user, we make sure that the directories are in
        // the user group.
        repositoryDao.createOrFixUserDir(getRepoUuid(), pathsToFix, s, sf, sql);
    }

    protected String getUserDirectoryName(EventContext ec) {
        return String.format("%s_%s", ec.userName, ec.userId);
    }
}
