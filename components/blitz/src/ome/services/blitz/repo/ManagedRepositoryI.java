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

import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.normalize;

import java.io.File;
import java.net.URI;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.fire.Registry;
import ome.system.Principal;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.grid.ImportLocation;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportSettings;
import omero.grid._ManagedRepositoryOperations;
import omero.grid._ManagedRepositoryTie;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.FilesetJobLink;
import omero.model.FilesetVersionInfo;
import omero.model.FilesetVersionInfoI;
import omero.model.Job;
import omero.model.MetadataImportJob;
import omero.model.MetadataImportJobI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.UploadJob;
import omero.sys.EventContext;

import static omero.rtypes.rstring;

/**
 * Extension of the PublicRepository API which onle manages files
 * under ${omero.data.dir}/ManagedRepository.
 *
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5
 */
public class ManagedRepositoryI extends PublicRepositoryI
    implements _ManagedRepositoryOperations {

    private final static Log log = LogFactory.getLog(ManagedRepositoryI.class);

    private final String template;

    /**
     * The Registry provides access to internal OMERO conections (i.e. within the
     * firewall).
     */
    private final Registry reg;

    /**
     * Fields used in date-time calculations.
     */
    private static final DateFormatSymbols DATE_FORMAT;

    static {
        DATE_FORMAT = new DateFormatSymbols();
    }

    public ManagedRepositoryI(String template, RepositoryDao dao, Registry reg) throws Exception {
        super(dao);
        this.reg = reg;
        this.template = template;
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
     * by calling {@link #makeDir(String, Ice.Current)}. Any exception will
     * be handled by incrementing some part of the template to create a viable
     * directory.
     *
     * @FIXME For the moment only the top-level directory is being incremented.
     */
    public ImportProcessPrx prepareImport(Fileset fs, ImportSettings settings,
            Ice.Current __current) throws omero.ServerError {

        if (fs == null || fs.sizeOfUsedFiles() < 1) {
            throw new omero.ApiUsageException(null, null, "No paths provided");
        }

        final List<String> paths = new ArrayList<String>();
        for (FilesetEntry entry : fs.copyUsedFiles()) {
            paths.add(entry.getClientPath().getValue());
        }

        // This is the first part of the string which comes after:
        // ManagedRepository/, e.g. %user%/%year%/etc.
        String relPath = expandTemplate(template, __current);

        // Possibly modified relPath.
        relPath = createTemplateDir(relPath, __current);

        // The next part of the string which is chosen by the user:
        // /home/bob/myStuff
        String basePath = commonRoot(paths);

        // If any two files clash in that chosen basePath directory, then
        // we want to suggest a similar alternative.
        ImportLocation location =
                suggestOnConflict(root.normPath, relPath, basePath, paths, __current);

        return createImportProcess(fs, location, settings, __current);
    }

    /** Now an internal, trusted method */
    public List<Pixels> importMetadata(CheckedPath cp, ImportLocation location,
            ImportSettings settings, Current __current) throws ServerError {

        ServiceFactoryPrx sf = null;
        OMEROMetadataStoreClient store = null;
        OMEROWrapper reader = null;
        List<Pixels> pix = null;
        boolean error = false;
        try {
            final ImportConfig config = new ImportConfig();
            final String sessionUuid = __current.ctx.get(omero.constants.SESSIONUUID.value);
            final String clientUuid = UUID.randomUUID().toString();

            sf = reg.getInternalServiceFactory(
                    sessionUuid, "unused", 3, 1, clientUuid);
            reader = new OMEROWrapper(config);
            store = new OMEROMetadataStoreClient();
            store.initialize(sf);
            ImportLibrary library = new ImportLibrary(store, reader);
            pix = library.importImageInternal(settings, 0, 0, 1, cp.file);
        }
        catch (ServerError se) {
            error = false;
            throw se;
        }
        catch (Throwable t) {
            error = false;
            throw new omero.InternalException(stackTraceAsString(t), null, t.getMessage());
        }
        finally {
            Throwable readerErr = null;
            Throwable storeErr = null;
            Throwable sfPrxErr = null;
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable e){
                readerErr = e;
            }
            try {
                if (store != null) {
                    store.logout();
                }
            } catch (Throwable e) {
                storeErr = e;
            }
            try {
                if (sf != null) {
                    sf.destroy();
                }
            } catch (Throwable e) {
                sfPrxErr = e;
            }
            if (readerErr != null || storeErr != null || sfPrxErr != null) {
                StringBuilder stacks = new StringBuilder();
                StringBuilder message = new StringBuilder();
                append(stacks, message, readerErr);
                append(stacks, message, storeErr);
                append(stacks, message, sfPrxErr);
                if (error) {
                    // In order to throw the original error just log this
                    log.error(String.format(
                            "Error on importMetadata cleanup lost. %s\n%s\n",
                            stacks.toString(), message.toString()));
                } else {
                    throw new omero.InternalException(
                            stacks.toString(), null, message.toString());
                }
            }
        }
        return pix;
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
        final UploadJob upload = (UploadJob) job;
        // Client can optionally set version

        final MetadataImportJob metadata = new MetadataImportJobI();
        metadata.setVersionInfo(serverVersionInfo);
        fs.linkJob(metadata);
        // TODO: other jobs

        // Create CheckedPath objects for use by saveFileset
        final Principal currentUser = currentUser(__current);
        final int size = fs.sizeOfUsedFiles();
        final List<CheckedPath> checked = new ArrayList<CheckedPath>();
        for (int i = 0; i < size; i++) {
            final String path = location.usedFiles.get(i);
            checked.add(checkPath(path, __current));
        }

        final Fileset managedFs = repositoryDao.saveFileset(getRepoUuid(), fs, checked, currentUser);
        final ManagedImportProcessI proc = new ManagedImportProcessI(this, managedFs,
                location, settings, __current);
        return proc.getProxy();
    }

    protected void append(StringBuilder stacks, StringBuilder message,
            Throwable err) {
        if (err != null) {
            message.append("=========================");
            message.append(err.toString());
            message.append(err.getMessage());
            stacks.append("==========================");
            stacks.append(stackTraceAsString(err));
        }
    }

    /**
     * From a list of paths, calculate the common root path that all share. In
     * the worst case, that may be "/".
     * @param paths
     * @return
     */
    protected String commonRoot(List<String> paths) {
        List<String> parts = splitElements(paths.get(0));
        String first = concat(parts.subList(0, parts.size()-1));

        OUTER: while (true)
        {
            for (String path : paths)
            {
                if (!path.startsWith(first))
                {
                    parts = splitElements(first);
                    first = concat(parts.subList(0, parts.size()-1));
                    if ("".equals(first)) {
                        first = "/";
                    }

                    if (".".equals(first) || "/".equals(first)) {
                        break OUTER;
                    }
                    continue OUTER;
                }
            }
            break;
        }
        return first;
    }

    /**
     * Turn the current template into a relative path. Makes uses of the data
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
        map.put("month", Integer.toString(now.get(Calendar.MONTH)+1));
        map.put("monthname", DATE_FORMAT.getMonths()[now.get(Calendar.MONTH)]);
        map.put("day", Integer.toString(now.get(Calendar.DAY_OF_MONTH)));
        map.put("session", ec.sessionUuid);
        map.put("sessionId", Long.toString(ec.sessionId));
        map.put("eventId", Long.toString(ec.eventId));
        map.put("perms", ec.groupPermissions.toString());
        return map;        
    } 

    /**
     * Take the relative path created by
     * {@link #expandTemplate(String, Ice.Current)} and call
     * {@link makeDir(String, Ice.Current)} on each element of the path
     * starting at the top, until all the directories have been created.
     * After any exception, append an increment so that a writeable directory
     * exists for the current caller context.
     */
    protected String createTemplateDir(String relPath, Ice.Current curr) {
        String[] parts = relPath.split("/");
        String dir = ".";
        int version = 0;
        for (int i = 0; i < parts.length; i++) {
            dir = FilenameUtils.concat(dir, parts[i]);
            while (true) {
                try {
                    if (version == 0) {
                        makeDir(dir, curr);
                    } else {
                        makeDir(dir+"__"+version, curr);
                    }
                    break;
                }
                catch (ServerError e) { // FIXME need specific error here!
                    log.debug("Error on createTemplateDir", e);
                    version += 1;
                }
            }
        }
        return dir;
    }

    /**
     * Take a relative path that the user would like to see in his or her
     * upload area, and check that none of the suggested paths currently
     * exist in that location. If they do, then append an incrementing version
     * number to the path ("/my/path/" becomes "/my-1/path" then "/my-2 /path")
     * at the highest part of the path possible.
     *
     * @param trueRoot Absolute path of the root directory (with true FS
     *          prefix, e.g. "/OMERO/ManagedRepo")
     * @param relPath Path parsed from the template
     * @param basePath Common base of all the listed paths ("/my/path")
     * @return {@link Import} instance with the suggested new basePath in the
     *          case of conflicts.
     */
    protected ImportLocation suggestOnConflict(String trueRoot, String relPath,
            String basePath, List<String> paths, Ice.Current __current)
            throws omero.ApiUsageException {

        // Static elements which will be re-used throughout
        final ImportLocation data = new ImportLocation(); // Return value
        final List<String> parts = splitElements(basePath);
        final File relFile = new File(relPath);
        final File trueFile = new File(trueRoot, relPath);
        final URI baseUri = new File(basePath).toURI();

        // State that will be updated per loop.
        Integer version = null;

        OUTER:
        while (true) {

            String suffix = (version == null ? null :
                "-" + Integer.toString(version));
            String endPart = concatSuffix1(parts, suffix);

            for (String path: paths)
            {
                URI pathUri = new File(path).toURI();
                String relative = baseUri.relativize(pathUri).getPath();
                if (new File(new File(trueFile, endPart), relative).exists()) {
                    if (version == null) {
                        version = 1;
                    } else {
                        version = version + 1;
                    }
                    continue OUTER;
                }
            }
        
            final File newBase = new File(relFile, endPart);
            data.sharedPath = normalize(newBase.toString());
            data.usedFiles = new ArrayList<String>(paths.size());
            for (String path : paths) {
                URI pathUri = new File(path).toURI();
                String relative = baseUri.relativize(pathUri).getPath();
                path = normalize(new File(newBase, relative).toString());
                data.usedFiles.add(path);
            }
    
            try {
                makeDir(normalize(relFile.toString()) + "/" + endPart,
                        __current);
                break;
            } catch (omero.ServerError se) {
                log.debug("Trying next directory", se);
                // This directory apparently belongs to some other group
                // or is not readable in the current context.
                if (version == null) {
                    version = 1;
                } else {
                    version = version + 1;
                }
                continue;
            }
        }

        return data;
    }

    public OriginalFile createOriginalFile(String path, Ice.Current __current)
            throws omero.ServerError {
        CheckedPath checked = checkPath(path, __current).mustExist();
        OriginalFile of = repositoryDao.register(getRepoUuid(), checked, null,
                __current);
        return of;
    }

    /**
     * Given a path with ending separator or not, this will split everything
     * after the final slash and store it under index==1 while everything else
     * will be stored under index==0. If there is no separator at all, "."
     * will be used for the path value. Therefore, it should be possible to
     * use {@link #concat()} to rejoin the parts. In the special case of the
     * root file ("/"), "/" will be returned both for part and name.
     *
     * TODO: Example changing handling of "/" to "./".
     *
     * @param path Non-null, preferably normalized path string.
     * @return A String-list with all path elements split.
     */
    protected List<String> splitElements(String normalizedPath) {

        if ("/".equals(normalizedPath)) {
            return Arrays.asList("/", "/"); // EARLY EXIT
        }

        File f = new File(normalizedPath);
        File p = f.getParentFile();
        if (f.getParentFile() == null) {
            return Arrays.asList(".", f.getName()); // EARLY EXIT
        }

        final LinkedList<String> rv = new LinkedList<String>();

        while (f != null) {
            if ("/".equals(f.getPath())) {
                // Keep initial slash
                rv.set(0, "/"+rv.get(0));
            } else {
                rv.addFirst(f.getName());
            }
            f = p;
            if (p != null) {
                p = p.getParentFile();
            }
        }

        return rv;
    }

    /**
     * Call {@link #concatSuffix1(List<String>, String)} with a null second argument.
     * @param elements
     * @return
     */
    protected String concat(List<String> elements) {
        return concatSuffix1(elements, null);
    }

    /**
     * Join all the elements with a "/".
     *
     * If the suffix argument is non-null, then append it to the first element
     * which is to be concatenated.
     * @param elements
     * @param suffix
     * @return
     */
    protected String concatSuffix1(List<String> elements, String suffix) {
        StringBuilder sb = new StringBuilder();
        boolean prepend = false;
        for (String elt : elements) {
            if (prepend) {
                sb.append("/");
            } else {
                prepend = true;
                if (suffix != null) {
                    elt = elt+suffix;
                }
            }
            sb.append(elt);
        }
        return sb.toString();
    }
}
