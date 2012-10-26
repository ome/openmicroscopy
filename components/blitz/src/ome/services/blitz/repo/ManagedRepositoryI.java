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

import java.io.File;
import java.net.URI;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.normalize;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.fire.Registry;

import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.grid.Import;
import omero.grid.RepositoryImportContainer;
import omero.grid._ManagedRepositoryOperations;
import omero.grid._ManagedRepositoryTie;
import omero.model.OriginalFile;
import omero.model.Pixels;

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
     * Fields used in date-time calculations. Static version should
     * decrease the number of calls to <code>Calendar.getInstance()</code>
     */
    private static final DateFormatSymbols DATE_FORMAT;
    private static final Calendar NOW;

    static {
        NOW = Calendar.getInstance();
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
     * Return a template based directory path.
     * (an option here would be to create the dir if it doesn't exist??)
     */
    public Import prepareImport(java.util.List<String> paths, Ice.Current __current)
            throws omero.ServerError {

        // This is the first part of the string which comes after:
        // ManagedRepository/, e.g. ${user}/${year}/etc.
        final String relPath = expandTemplate(__current);

        // The next part of the string which is chosen by the user:
        // /home/bob/myStuff
        String basePath = commonRoot(paths);

        // If any two files clash in that chosen basePath directory, then
        // we want to suggest a similar alternative.
        return suggestOnConflict(root.normPath, relPath, basePath, paths, __current);
    }


    public RawFileStorePrx uploadUsedFile(Import importData, String usedFile, Ice.Current __current)
            throws omero.ServerError {
        File f = new File(root.file, usedFile);
        f.getParentFile().mkdirs(); // FIXME: this should likely be done by CheckedPath
        return file(f.getAbsolutePath(), "rw", __current);
    }

    public List<Pixels> importMetadata(Import importData,
            RepositoryImportContainer repoIC, Current __current) throws ServerError {

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
            ImportContainer ic = createImportContainer(repoIC, __current);
            pix = library.importImageInternal(ic, 0, 0, 1);
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


    public void cancelImport(Import importData, Ice.Current __current)
               throws ServerError {
        throw new omero.InternalException(null, null, "NYI"); // FIXME
    }

    //
    // HELPERS
    //

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
     * Create an ImportContainer from a RepositoryImportContainer
     */
    protected ImportContainer createImportContainer(RepositoryImportContainer repoIC,
            Ice.Current __current) throws omero.ValidationException {
        File f = new File(root.file, repoIC.file);
        CheckedPath cp = checkPath(f.getAbsolutePath(), __current);
        ImportContainer ic = new ImportContainer(cp.file, repoIC.projectId,
			    repoIC.target, false, null, repoIC.reader, repoIC.usedFiles, repoIC.isSPW);
		ic.setBfImageCount(repoIC.bfImageCount);
		ic.setBfPixels(repoIC.bfPixels);
		ic.setBfImageNames(repoIC.bfImageNames);
        // Assuming that if the array is not null all values are not null.
        if (repoIC.userPixels == null || repoIC.userPixels.length == 0) {
            ic.setUserPixels(null);
        }
        else {
            Double[] userPixels = new Double[repoIC.userPixels.length];
            for (int i=0; i < userPixels.length; i++) {
                userPixels[i] = repoIC.userPixels[i];
            }
            ic.setUserPixels(userPixels);
        }
		ic.setCustomImageName(repoIC.customImageName);
		ic.setCustomImageDescription(repoIC.customImageDescription);
		ic.setCustomPlateName(repoIC.customPlateName);
		ic.setCustomPlateDescription(repoIC.customPlateDescription);
		ic.setDoThumbnails(repoIC.doThumbnails);
		ic.setCustomAnnotationList(repoIC.customAnnotationList);
        return ic;
    }

    /**
     * From a list of paths, calculate the common root path that all share. In
     * the worst case, that may be "/".
     * @param paths
     * @return
     */
    protected String commonRoot(List<String> paths) {
        String[] parts = splitLastElement(paths.get(0));

        OUTER: while (true)
        {
            for (String path : paths)
            {
                if (!path.startsWith(parts[0]))
                {
                    parts = splitLastElement(parts[0]);
                    if (".".equals(parts[0]) || "/".equals(parts[0])) {
                        break OUTER;
                    }
                    continue OUTER;
                }
            }
            break;
        }
        return parts[0];
    }

    /**
     * Turn the current template into a relative path. By default this is
     * prefixed with the user's name.
     * @param curr
     * @return
     */
    protected String expandTemplate(Ice.Current curr) {
        String relPath = "";
        String dir = null;
        String[] elements = template.split("/");
        for (String part : elements) {
            String[] subelements = part.split("-");
            dir = getStringFromToken(subelements[0], NOW, curr);
            for (int i = 1; i < subelements.length; i++) {
                dir = dir + "-" + getStringFromToken(subelements[i], NOW, curr);
            }
            relPath = concat(relPath, dir);
        }
        return relPath;
    }

    /**
     * Helper method to provide a little more flexibility
     * when building a path from a template
     */
    protected String getStringFromToken(String token, Calendar now,
            Ice.Current curr) {

        String rv;
        if ("%year%".equals(token))
            rv = Integer.toString(now.get(Calendar.YEAR));
        else if ("%month%".equals(token))
            rv = Integer.toString(now.get(Calendar.MONTH)+1);
        else if ("%monthname%".equals(token))
            rv = DATE_FORMAT.getMonths()[now.get(Calendar.MONTH)];
        else if ("%day%".equals(token))
            rv = Integer.toString(now.get(Calendar.DAY_OF_MONTH));
        else if ("%user%".equals(token))
            rv = this.repositoryDao.getEventContext(curr).userName;
        else if ("%group%".equals(token))
            rv = this.repositoryDao.getEventContext(curr).groupName;
        else if (!token.endsWith("%") && !token.startsWith("%"))
            rv = token;
        else {
            log.warn("Ignored unrecognised token in template: " + token);
            rv = "";
        }
        return rv;
    }

    /**
     * Take a relative path that the user would like to see in his or her
     * upload area, and check that none of the suggested paths currently
     * exist in that location. If they do, then append an incrementing version
     * number to the path ("/my/path/" becomes "/my/path-1" then "/my/path-2").
     *
     * @param trueRoot Absolute path of the root directory (with true FS
     *          prefix, e.g. "/OMERO/ManagedRepo")
     * @param relPath Path parsed from the template
     * @param basePath Common base of all the listed paths ("/my/path")
     * @return Suggested new basePath in the case of conflicts.
     */
    protected Import suggestOnConflict(String trueRoot, String relPath,
            String basePath, List<String> paths, Ice.Current __current)
            throws omero.ApiUsageException {

        // Static elements which will be re-used throughout
        final Import data = new Import(); // Return value
        final String[] parts = splitLastElement(basePath);
        final String nonEndPart = parts[0];
        final String uniquePathElement = parts[1];
        final File relUpToLast = new File(new File(relPath), nonEndPart);
        final File trueUpToLast = new File(new File(trueRoot, relPath), nonEndPart);
        final URI baseUri = new File(basePath).toURI();

        // State that will be updated per loop.
        Integer version = null;

        OUTER:
        while (true) {

            String endPart = uniquePathElement + (version == null ? "" :
                "-" + Integer.toString(version));
    
            for (String path: paths)
            {
                URI pathUri = new File(path).toURI();
                String relative = baseUri.relativize(pathUri).getPath();
                if (new File(new File(trueUpToLast, endPart), relative).exists()) {
                    if (version == null) {
                        version = 1;
                    } else {
                        version = version + 1;
                    }
                    continue OUTER;
                }
            }
        
            final File newBase = new File(relUpToLast, endPart);
            data.sharedPath = normalize(newBase.toString());
            data.usedFiles = new ArrayList<String>(paths.size());
            for (String path : paths) {
                URI pathUri = new File(path).toURI();
                String relative = baseUri.relativize(pathUri).getPath();
                path = normalize(new File(newBase, relative).toString());
                data.usedFiles.add(path);
            }
    
            try {
                data.directory = repositoryDao.createUserDirectory(getRepoUuid(),
                    normalize(relUpToLast.toString()), endPart, currentUser(__current));
                break;
            } catch (ome.conditions.SecurityViolation sv) {
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
            throws omero.ApiUsageException {
        long size = new File(root.file, path).length();
        File file = new File(path);
        OriginalFile of = repositoryDao.createUserFile(getRepoUuid(),
                file.getParent(), file.getName(), size, currentUser(__current));
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
     * @param path Non-null, preferably normalized path string.
     * @return A String-array of size 2 with non-null values for path and name.
     */
    protected String[] splitLastElement(String normalizedPath) {

        if ("/".equals(normalizedPath)) {
            return new String[]{"/", "/"}; // EARLY EXIT
        }

        final String[] rv = new String[2];
        final File f = new File(normalizedPath);
        rv[1] = f.getName();
        if (f.getParentFile() == null) {
            rv[0] = "."; // i.e. relative to "here"
        } else {
            rv[0] = f.getParentFile().toString();
        }
        return rv;
    }
}
