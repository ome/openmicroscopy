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
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.grid.RepositoryImportContainer;
import omero.grid._ManagedRepositoryOperations;
import omero.grid._ManagedRepositoryTie;
import omero.model.Experimenter;
import omero.model.Pixels;
import omero.util.IceMapper;

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

    public ManagedRepositoryI(String template, Executor executor,
            Principal principal, Registry reg) throws Exception {
        super(executor, principal);
        this.reg = reg;
        this.template = template;
        log.info("Repository template: " + this.template);
    }

    @Override
    public Ice.Object tie() {
        return new _ManagedRepositoryTie(this);
    }

    public List<Pixels> importMetadata(RepositoryImportContainer repoIC, Current __current) throws ServerError {
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
            ImportContainer ic = createImportContainer(repoIC);
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

    private void append(StringBuilder stacks, StringBuilder message,
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
    private ImportContainer createImportContainer(RepositoryImportContainer repoIC) {
        ImportContainer ic = new ImportContainer(new File(repoIC.file), repoIC.projectId,
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
     * Return a template based directory path.
     * (an option here would be to create the dir if it doesn't exist??)
     */
    public List<String> getCurrentRepoDir(List<String> paths, Current __current) throws ServerError {
        String repoPath = root.getAbsolutePath(); // root includes MRP
        String basePath = FilenameUtils.getFullPathNoEndSeparator(paths.get(0));
        for (String path : paths)
        {
            if (!path.startsWith(basePath))
            {
                basePath = FilenameUtils.getFullPathNoEndSeparator(basePath);
            }
        }

        EventContext ec = currentContext(__current);
        String name = ec.getCurrentUserName();

        //FIXME: Force user prefix for now
        repoPath = FilenameUtils.concat(repoPath, name);
        String dir;
        String[] elements = template.split("/");
        for (String part : elements) {
            String[] subelements = part.split("-");
            dir = getStringFromToken(subelements[0]);
            for (int i = 1; i < subelements.length; i++) {
                dir = dir + "-" + getStringFromToken(subelements[i]);
            }
            repoPath = FilenameUtils.concat(repoPath, dir);
        }

        //if file clashes in that directory
        String uniquePathElement = FilenameUtils.getName(basePath);
        String endPart = uniquePathElement;
        boolean clashes = false;
        for (String path: paths)
        {
            String relative = new File(basePath).toURI().relativize(new File(path).toURI()).getPath();
            if (new File(new File(repoPath, endPart), relative).exists()) {
                clashes = true;
                break;
            }
        }

        if (clashes) {
            int version = 0;
            while (new File(repoPath, endPart).exists()) {
                version++;
                endPart = uniquePathElement + "-" + Integer.toString(version);
            }
        }
        repoPath = FilenameUtils.concat(repoPath, endPart);

        for (int i=0; i<paths.size(); i++)
        {
            String path = paths.get(i);
            String relative = new File(basePath).toURI().relativize(new File(path).toURI()).getPath();
            path = FilenameUtils.concat(repoPath, relative);
            paths.set(i, path);
        }

        return paths;
    }

    // Helper method to provide a little more flexibility
    // when building a path from a template
    private String getStringFromToken(String token) {
        Calendar now = Calendar.getInstance();
        DateFormatSymbols dfs = new DateFormatSymbols();
        String rv;
        if (token.equals("%year%"))
            rv = Integer.toString(now.get(Calendar.YEAR));
        else if (token.equals("%month%"))
            rv = Integer.toString(now.get(Calendar.MONTH)+1);
        else if (token.equals("%monthname%"))
            rv = dfs.getMonths()[now.get(Calendar.MONTH)];
        else if (token.equals("%day%"))
            rv = Integer.toString(now.get(Calendar.DAY_OF_MONTH));
        else if (!token.endsWith("%") && !token.startsWith("%"))
            rv = token;
        else {
            log.warn("Ignored unrecognised token in template: " + token);
            rv = "";
        }
        return rv;
    }

}
