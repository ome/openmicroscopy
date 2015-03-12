/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.fs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loci.formats.FormatException;
import loci.formats.IFormatReader;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import ome.io.nio.PixelsService;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.system.Login;
import ome.util.SqlAction;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.UsedFilesRequest;
import omero.cmd.HandleI.Cancel;
import omero.cmd.UsedFilesResponse;
import omero.cmd.UsedFilesResponsePreFs;
import omero.constants.annotation.file.ORIGINALMETADATA;
import omero.constants.namespaces.NSCOMPANIONFILE;

/**
 * Lists the IDs of the original files associated with an image.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class UsedFilesRequestI extends UsedFilesRequest implements IRequest {

    private static final long serialVersionUID = -1572148877023558009L;

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private final PixelsService pixelsService;

    private final Map<String, File> repositoryRoots = new HashMap<String, File>();

    private Helper helper;
    private Session session;
    private Long filesetId;
    private Long pixelsId;
    private IFormatReader reader;

    /**
     * Construct a new used files request.
     * @param pixelsService the pixels service
     */
    public UsedFilesRequestI(PixelsService pixelsService) {
        this.pixelsService = pixelsService;
    }

    /* FIT USED FILES INTO A SINGLE REQUEST STEP */

    @Override
    public Map<String, String> getCallContext() {
        return new HashMap<String, String>(ALL_GROUPS_CONTEXT);
    }

    @Override
    public void init(Helper helper) {
        this.helper = helper;
        this.session = helper.getSession();
        helper.setSteps(1);
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        try {
            switch (step) {
            case 0:
                return determineResponse();
            default:
                final Exception e = new IllegalArgumentException("used files request has no step " + step);
                throw helper.cancel(new ERR(), e, "bad-step");
            }
        } catch (Cancel c) {
            throw c;
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "used-files-fail");
        }
    }

    @Override
    public void finish() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw helper.cancel(new ERR(), e, "used-files-fail");
            } finally {
                reader = null;
            }
        }
    }

    @Override
    protected void finalize() {
        finish();
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 0) {
            helper.setResponseIfNull((Response) object);
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    /* ACTUALLY QUERY FOR USED FILES AND CONSTRUCT RESPONSE */

    /**
     * Set up private fields and generate appropriate response.
     * @return the response to the request
     * @throws Cancel if a response cannot be provided
     */
    private Response determineResponse() throws Cancel {
        final String hql = "SELECT fileset.id FROM Image WHERE id = :id";
        final Query query = session.createQuery(hql).setParameter("id", imageId);
        @SuppressWarnings("unchecked")
        final List<Object> results = query.list();
        if (results.isEmpty()) {
            final Exception e = new IllegalArgumentException("cannot read image " + imageId);
            throw helper.cancel(new ERR(), e, "bad-image");
        }
        filesetId = (Long) results.get(0);
        try {
            findPixels();
        } catch (Cancel c) {
            throw c;
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "used-files-fail");
        }
        return filesetId == null ? determineResponsePreFs() : determineResponseFs();
    }

    /**
     * Find the image's primary pixels object and, if an FS image, open a Bio-Formats reader for it.
     * @throws Cancel if there are no pixels for the image
     * @throws FormatException if the pixels service could not open a Bio-Formats reader for the image
     * @throws IOException if the pixels service could not open a Bio-Formats reader for the image
     */
    private void findPixels() throws Cancel, FormatException, IOException {
        final String hql = "FROM Image WHERE id = :id";
        final Query query = session.createQuery(hql).setParameter("id", imageId);
        final Image image = (Image) query.uniqueResult();
        if (image.sizeOfPixels() < 1) {
            final Exception e = new IllegalArgumentException("no pixels for image " + imageId);
            throw helper.cancel(new ERR(), e, "bad-image");
        }
        final Pixels pixels = image.getPrimaryPixels();
        pixelsId = pixels.getId();
        if (filesetId != null) {
            reader = pixelsService.getBfReader(pixels);
        }
    }

    /**
     * @param repo a repository identifier
     * @return the root of the repository on the filesystem
     */
    private File getRepositoryRoot(String repo) {
        File root = repositoryRoots.get(repo);
        if (root == null) {
            final String hql = "SELECT path || name FROM OriginalFile WHERE mimetype = 'Repository' AND hash = :repo";
            final Query query = session.createQuery(hql).setParameter("repo", repo);
            final String repositoryRootDirectory = (String) query.uniqueResult();
            root = new File(repositoryRootDirectory);
            repositoryRoots.put(repo, root);
        }
        return root;
    }

    /**
     * @param repo a repository
     * @param file a file path in the repository
     * @return the path of the file on the filesystem
     */
    private File getUnderlyingFile(String repo, String file) {
        /* note: cannot use ServerFilePathTransformer from within a Request */
        final File repoRoot = getRepositoryRoot(repo);
        if ('/' != File.separatorChar) {
            file = file.replace('/', File.separatorChar);
        }
        return new File(repoRoot, file);
    }

    /**
     * @return the paths and IDs of the files in the FS image's fileset
     */
    private Map<File, Long> getFileIdsOfFileset() {
        /* useful for translating filenames from Bio-Formats to IDs of OriginalFile objects */
        final SqlAction jdbc = helper.getSql();
        final Map<File, Long> fileIds = new HashMap<File, Long>();
        final String hql = "SELECT originalFile.id, originalFile.path || originalFile.name FROM FilesetEntry " +
                "WHERE fileset.id = :id";
        final Query query = session.createQuery(hql).setParameter("id", filesetId);
        @SuppressWarnings("unchecked")
        final List<Object[]> results = query.list();
        for (final Object[] result : results) {
            final Long fileId = (Long) result[0];
            final String fileName = (String) result[1];
            final String repo = jdbc.fileRepo(fileId);
            final File realFile = getUnderlyingFile(repo, fileName);
            fileIds.put(realFile, fileId);
        }
        return fileIds;
    }

    /**
     * @return the response to the request, for FS images
     */
    private UsedFilesResponse determineResponseFs() {
        final Map<File, Long> fileIds = getFileIdsOfFileset();
        final Set<File> allFileNamesAllSeries = namesToFiles(reader.getUsedFiles(false));
        final Set<File> companionFileNamesAllSeries = namesToFiles(reader.getUsedFiles(true));
        final Set<File> binaryFileNamesAllSeries = Sets.difference(allFileNamesAllSeries, companionFileNamesAllSeries);
        final Set<File> allFileNamesThisSeries = namesToFiles(reader.getSeriesUsedFiles(false));
        final Set<File> companionFileNamesThisSeries = namesToFiles(reader.getSeriesUsedFiles(true));
        final Set<File> binaryFileNamesThisSeries = Sets.difference(allFileNamesThisSeries, companionFileNamesThisSeries);
        final Set<File> companionFileNamesOtherSeries = Sets.difference(companionFileNamesAllSeries, companionFileNamesThisSeries);
        final Set<File> binaryFileNamesOtherSeries = Sets.difference(binaryFileNamesAllSeries, binaryFileNamesThisSeries);
        final List<Long> binaryFileIdsThisSeries = mapList(fileIds, binaryFileNamesThisSeries);
        final List<Long> binaryFileIdsOtherSeries = mapList(fileIds, binaryFileNamesOtherSeries);
        final List<Long> companionFileIdsThisSeries = mapList(fileIds, companionFileNamesThisSeries);
        final List<Long> companionFileIdsOtherSeries = mapList(fileIds, companionFileNamesOtherSeries);
        return new UsedFilesResponse(binaryFileIdsThisSeries, binaryFileIdsOtherSeries,
                companionFileIdsThisSeries, companionFileIdsOtherSeries);
    }

    /**
     * @return the IDs of the pre-FS image's archived files
     */
    private List<Long> getArchivedFilesPreFs() {
        final String hql = "SELECT DISTINCT parent.id FROM PixelsOriginalFileMap WHERE child.id = :id";
        final Query query = session.createQuery(hql).setParameter("id", pixelsId);
        return objectsToLongs(query.list());
    }

    /**
     * @return the IDs and names of the pre-FS image's companion files
     */
    private Map<Long, String> getCompanionFilesPreFs() {
        final Map<Long, String> fileNames = new HashMap<Long, String>();
        final String hql = "SELECT DISTINCT fa.file.id, fa.file.name FROM ImageAnnotationLink ial, FileAnnotation fa " +
                "WHERE ial.parent.id = :id AND ial.child = fa AND fa.ns = :ns";
        final Query query = session.createQuery(hql).setParameter("id", imageId).setParameter("ns", NSCOMPANIONFILE.value);
        @SuppressWarnings("unchecked")
        final List<Object[]> results = query.list();
        for (final Object[] result : results) {
            final Long fileId = (Long) result[0];
            final String fileName = (String) result[1];
            fileNames.put(fileId, fileName);
        }
        return fileNames;
    }

    /**
     * @return the response to the request, for non-FS images
     */
    private UsedFilesResponsePreFs determineResponsePreFs() {
        final List<Long> archivedFiles = getArchivedFilesPreFs();
        final List<Long> realCompanionFiles = new ArrayList<Long>();
        final List<Long> originalMetadataFiles = new ArrayList<Long>();
        for (final Map.Entry<Long, String> companionFile : getCompanionFilesPreFs().entrySet()) {
            final Long id = companionFile.getKey();
            final String name = companionFile.getValue();
            (ORIGINALMETADATA.value.equals(name) ? originalMetadataFiles : realCompanionFiles).add(id);
        }
        return new UsedFilesResponsePreFs(archivedFiles, realCompanionFiles, originalMetadataFiles);
    }

    /**
     * Convert {@code Collection<?>} to {@code List<Long>}.
     * @param objects a collection of {@link Long}s
     * @return a list of the same {@link Long}s
     */
    private static List<Long> objectsToLongs(Collection<?> objects) {
        final List<Long> longs = new ArrayList<Long>(objects.size());
        for (final Object object : objects) {
            longs.add((Long) object);
        }
        return longs;
    }

    /**
     * Convert filenames to {@link File} instances.
     * @param names filenames
     * @return the corresponding {@link File} objects, or an empty list if passed {@code null}
     */
    private static Set<File> namesToFiles(String... names) {
        final Set<File> files = new HashSet<File>();
        if (names != null) {
            for (final String name : names) {
                files.add(new File(name));
            }
        }
        return files;
    }

    /**
     * Apply a mapping function to a collection of items.
     * @param mapping the mapping function to apply
     * @param input a collection of items
     * @return the map value for each item, in order
     */
    private static <X, Y> List<Y> mapList(Map<X, Y> mapping, Collection<X> input) {
        final List<Y> output = new ArrayList<Y>(input.size());
        for (final X item : input) {
            output.add(mapping.get(item));
        }
        return output;
    }
}
