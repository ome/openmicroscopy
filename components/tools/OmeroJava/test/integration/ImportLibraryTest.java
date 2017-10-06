/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loci.formats.in.FakeReader;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.util.ChecksumAlgorithmMapper;
import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.HandlePrx;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportSettings;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryPrx;
import omero.model.ChecksumAlgorithmI;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.model.Pixels;
import omero.model.enums.ChecksumAlgorithmMurmur3128;
import omero.sys.ParametersI;

import org.apache.commons.lang.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

/**
 * Tests import methods exposed by the ImportLibrary.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0.0
 */
@Test(groups = { "import", "integration", "fs" })
public class ImportLibraryTest extends AbstractServerTest {

    /**
     * Tests the <code>ImportImage</code> method using an import container
     * returned by the import candidates method.
     *
     * @param permissions
     *            The permissions of the group.
     * @param userRole
     *            The role of the user e.g. group owner.
     * @param name
     *            The name of the file to import.
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    private void importImage(String permissions, int userRole, String name)
            throws Throwable {
        // create a new group and user
        login(permissions, userRole);
        File f = File.createTempFile(name + ModelMockFactory.FORMATS[0], "."
                + ModelMockFactory.FORMATS[0]);
        mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
        f.deleteOnExit();
        ImportConfig config = new ImportConfig();
        ImportLibrary library = new ImportLibrary(createImporter(), new OMEROWrapper(
                config));
        ImportContainer ic = getCandidates(f).getContainers().get(0);
        List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
        Assert.assertNotNull(pixels);
        Assert.assertEquals(1, pixels.size());
    }

    /**
     * Tests the <code>ImportImage</code> method using an import container
     * returned by the import candidates method.
     *
     * @param permissions
     *            The permissions of the group.
     * @param userRole
     *            The role of the user e.g. group owner.
     * @param name
     *            The name of the file to import.
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    private void importCandidates(String permissions, int userRole, String name)
            throws Throwable {
        login(permissions, userRole);
        File f = File.createTempFile(name + ModelMockFactory.FORMATS[0], "."
                + ModelMockFactory.FORMATS[0]);
        mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
        f.deleteOnExit();
        ImportCandidates candidates = getCandidates(f);
        Assert.assertNotNull(candidates);
        Assert.assertNotNull(candidates.getContainers().get(0));
    }

    /**
     * Tests the <code>testImportMetadataOnly</code> method using an import
     * container returned by the import candidates method.
     *
     * @param permissions
     *            The permissions of the group.
     * @param userRole
     *            The role of the user e.g. group owner.
     * @param name
     *            The name of the file to import.
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    private void importMetadataAfterUploadToRepository(String permissions,
            int userRole, String name) throws Throwable {
        login(permissions, userRole);
        File f = File.createTempFile(name + ModelMockFactory.FORMATS[0], "."
                + ModelMockFactory.FORMATS[0]);
        mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
        f.deleteOnExit();
        ImportConfig config = new ImportConfig();
        ImportLibrary library = new ImportLibrary(createImporter(), new OMEROWrapper(
                config));
        ImportContainer ic = getCandidates(f).getContainers().get(0);

        // FIXME: Using importImage here to keep the tests working
        // but this is not the method under test (which has been removed)
        List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
        Assert.assertNotNull(pixels);
        Assert.assertEquals(1, pixels.size());
        // omero.grid.Import data = library.uploadFilesToRepository(ic);
        // List<Pixels> pixels = repo.importMetadata(data);
        // assertNotNull(pixels);
        // assertEquals(pixels.size(), 1);
    }

    /**
     * Tests the <code>ImportImage</code> method using an import container
     * returned by the import candidates method.
     *
     * @param permissions
     *            The permissions of the group.
     * @param userRole
     *            The role of the user e.g. group owner.
     * @param name
     *            The name of the file to import.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void importImageCreateImportContainer(String permissions,
            int userRole, String name) throws Throwable {
        login(permissions, userRole);
        File f = File.createTempFile(name + ModelMockFactory.FORMATS[0], "."
                + ModelMockFactory.FORMATS[0]);
        mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
        f.deleteOnExit();
        ImportConfig config = new ImportConfig();
        ImportLibrary library = new ImportLibrary(createImporter(), new OMEROWrapper(
                config));
        ImportContainer ic = getCandidates(f).getContainers().get(0);
        ic = new ImportContainer(f, null, null, null, ic.getUsedFiles(), null);
        List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
        Assert.assertNotNull(pixels);
        Assert.assertEquals(1, pixels.size());
    }

    /**
     * Overridden to initialize the list.
     *
     * @see AbstractServerTest#setUp()
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Overridden to delete the files.
     *
     * @see AbstractServerTest#tearDown()
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception {
    }

    /**
     * Returns the import candidates corresponding to the specified file.
     *
     * @param f
     *            The file to handle.
     * @return See above.
     */
    private ImportCandidates getCandidates(File f) throws Exception {
        ImportConfig config = new ImportConfig();
        OMEROWrapper reader = new OMEROWrapper(config);
        String[] paths = new String[1];
        paths[0] = f.getAbsolutePath();
        IObserver o = new IObserver() {
            public void update(IObservable importLibrary, ImportEvent event) {

            }
        };
        return new ImportCandidates(reader, paths, o);
    }

    /**
     * Tests the import of an image into a <code>RW----</code> group by a
     * general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWByMember() throws Throwable {
        importImage("rw----", MEMBER, "testImportImageRWByMember");
    }

    /**
     * Tests the import of an image into a <code>RW----</code> group by a
     * group's owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWByGroupOwner() throws Throwable {
        importImage("rw----", GROUP_OWNER, "testImportImageRWByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RW----</code> group by an
     * administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWByAdmin() throws Throwable {
        importImage("rw----", ADMIN, "testImportImageRWByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a
     * general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRByMember() throws Throwable {
        importImage("rwr---", MEMBER, "testImportImageRWRByMember");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a
     * group's owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRByGroupOwner() throws Throwable {
        importImage("rwr---", GROUP_OWNER, "testImportImageRWRByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by an
     * administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRByAdmin() throws Throwable {
        importImage("rwr---", ADMIN, "testImportImageRWRByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWRA--</code> group by a
     * general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRAByMember() throws Throwable {
        importImage("rwra--", MEMBER, "testImportImageRWRAByMember");
    }

    /**
     * Tests the import of an image into a <code>RWRA--</code> group by a
     * group's owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRAByGroupOwner() throws Throwable {
        importImage("rwra--", GROUP_OWNER, "testImportImageRWRAByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWRA--</code> group by an
     * administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRAByAdmin() throws Throwable {
        importImage("rwra--", ADMIN, "testImportImageRWRAByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWRW--</code> group by a
     * general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRWByMember() throws Throwable {
        importImage("rwrw--", MEMBER, "testImportImageRWRWByMember");
    }

    /**
     * Tests the import of an image into a <code>RWRW--</code> group by a
     * group's owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRWByGroupOwner() throws Throwable {
        importImage("rwrw--", GROUP_OWNER, "testImportImageRWRWByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWRW--</code> group by an
     * administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageRWRWByAdmin() throws Throwable {
        importImage("rwrw--", ADMIN, "testImportImageRWRWByAdmin");
    }

    // Test import candidates
    /**
     * Tests the import candidates method for <code>RWRW--</code> group logged
     * in as a general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRWByMember() throws Throwable {
        importCandidates("rwrw--", MEMBER, "testImportCandidatesRWRWByMember");
    }

    /**
     * Tests the import candidates method for <code>RWRW--</code> group logged
     * in as a group owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRWByGroupOwner() throws Throwable {
        importCandidates("rwrw--", GROUP_OWNER,
                "testImportCandidatesRWRWByGroupOwner");
    }

    /**
     * Tests the import candidates method for <code>RWRW--</code> group logged
     * in as a administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRWByAdmin() throws Throwable {
        importCandidates("rwrw--", ADMIN, "testImportCandidatesRWRWByAdmin");
    }

    /**
     * Tests the import candidates method for <code>RWRA--</code> group logged
     * in as a general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRAByMember() throws Throwable {
        importCandidates("rwra--", MEMBER, "testImportCandidatesRWRAByMember");
    }

    /**
     * Tests the import candidates method for <code>RWRA--</code> group logged
     * in as a group owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRAByGroupOwner() throws Throwable {
        importCandidates("rwra--", GROUP_OWNER,
                "testImportCandidatesRWRAByGroupOwner");
    }

    /**
     * Tests the import candidates method for <code>RWRA--</code> group logged
     * in as a administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRAByAdmin() throws Throwable {
        importCandidates("rwra--", ADMIN, "testImportCandidatesRWRAByAdmin");
    }

    /**
     * Tests the import candidates method for <code>RWR---</code> group logged
     * in as a general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRByMember() throws Throwable {
        importCandidates("rwr---", MEMBER, "testImportCandidatesRWRByMember");
    }

    /**
     * Tests the import candidates method for <code>RWR---</code> group logged
     * in as a group owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRByGroupOwner() throws Throwable {
        importCandidates("rwr---", GROUP_OWNER,
                "testImportCandidatesRWRByGroupOwner");
    }

    /**
     * Tests the import candidates method for <code>RWR---</code> group logged
     * in as a administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWRByAdmin() throws Throwable {
        importCandidates("rwr---", ADMIN, "testImportCandidatesRWRByAdmin");
    }

    /**
     * Tests the import candidates method for <code>RW----</code> group logged
     * in as a general member.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWByMember() throws Throwable {
        importCandidates("rw----", MEMBER, "testImportCandidatesRWByMember");
    }

    /**
     * Tests the import candidates method for <code>RW----</code> group logged
     * in as a group owner.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWByGroupOwner() throws Throwable {
        importCandidates("rw----", GROUP_OWNER,
                "testImportCandidatesRWByGroupOwner");
    }

    /**
     * Tests the import candidates method for <code>RW----</code> group logged
     * in as a administrator.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportCandidatesRWByAdmin() throws Throwable {
        importCandidates("rw----", ADMIN, "testImportCandidatesRWByAdmin");
    }

    // Test import image with created import container.
    /**
     * Tests the import of an image into a <code>RW----</code> group by a
     * general member. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWByMember()
            throws Throwable {
        importImageCreateImportContainer("rw----", MEMBER,
                "testImportImageCreateImportContainerRWByMember");
    }

    /**
     * Tests the import of an image into a <code>RW----</code> group by a group
     * owner. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWByGroupOwner()
            throws Throwable {
        importImageCreateImportContainer("rw----", GROUP_OWNER,
                "testImportImageCreateImportContainerRWByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RW----</code> group by an
     * administrator. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWByAdmin()
            throws Throwable {
        importImageCreateImportContainer("rw----", ADMIN,
                "testImportImageCreateImportContainerRWByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a
     * general member. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRByMember()
            throws Throwable {
        importImageCreateImportContainer("rwr---", MEMBER,
                "testImportImageCreateImportContainerRWRByMember");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a group
     * owner. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRByGroupOwner()
            throws Throwable {
        importImageCreateImportContainer("rwr---", GROUP_OWNER,
                "testImportImageCreateImportContainerRWRByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by an
     * administrator. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRByAdmin()
            throws Throwable {
        importImageCreateImportContainer("rwr---", ADMIN,
                "testImportImageCreateImportContainerRWRByGroupAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a
     * general member. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRAByMember()
            throws Throwable {
        importImageCreateImportContainer("rwra--", MEMBER,
                "testImportImageCreateImportContainerRWRAByMember");
    }

    /**
     * Tests the import of an image into a <code>RWRA--</code> group by a group
     * owner. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRAByGroupOwner()
            throws Throwable {
        importImageCreateImportContainer("rwra--", GROUP_OWNER,
                "testImportImageCreateImportContainerRWRAByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by an
     * administrator. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRAByAdmin()
            throws Throwable {
        importImageCreateImportContainer("rwra--", ADMIN,
                "testImportImageCreateImportContainerRWRAByGroupAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a
     * general member. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRWByMember()
            throws Throwable {
        importImageCreateImportContainer("rwrw--", MEMBER,
                "testImportImageCreateImportContainerRWRWByMember");
    }

    /**
     * Tests the import of an image into a <code>RWRW--</code> group by a group
     * owner. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRWByGroupOwner()
            throws Throwable {
        importImageCreateImportContainer("rwrw--", GROUP_OWNER,
                "testImportImageCreateImportContainerRWRWByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWRW--</code> group by an
     * administrator. This time the import container object is created.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportImageCreateImportContainerRWRWByAdmin()
            throws Throwable {
        importImageCreateImportContainer("rwrw--", ADMIN,
                "testImportImageCreateImportContainerRWRWByGroupAdmin");
    }

    // Test import the metadata after uploading the file to the repository
    /**
     * Tests the import of an image into a <code>RWRW--</code> group by an
     * administrator. The image is first uploaded to the repository, followed by
     * a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRWByAdmin()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwrw--", ADMIN,
                "testImportMetadataAfterUploadToRepositoryRWRWByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWRW--</code> group by a group
     * owner. The image is first uploaded to the repository, followed by a
     * metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRWByGroupOwner()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwrw--", GROUP_OWNER,
                "testImportMetadataAfterUploadToRepositoryRWRWByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWRW--</code> group by a
     * general member. The image is first uploaded to the repository, followed
     * by a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRWByMember()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwrw--", MEMBER,
                "testImportMetadataAfterUploadToRepositoryRWRWByMember");
    }

    /**
     * Tests the import of an image into a <code>RWRA--</code> group by an
     * administrator. The image is first uploaded to the repository, followed by
     * a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRAByAdmin()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwra--", ADMIN,
                "testImportMetadataAfterUploadToRepositoryRWRAByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWRA--</code> group by a group
     * owner. The image is first uploaded to the repository, followed by a
     * metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRAByGroupOwner()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwra--", GROUP_OWNER,
                "testImportMetadataAfterUploadToRepositoryRWRAByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWRA--</code> group by a
     * general member. The image is first uploaded to the repository, followed
     * by a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRAByMember()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwra--", MEMBER,
                "testImportMetadataAfterUploadToRepositoryRWRAByMember");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by an
     * administrator. The image is first uploaded to the repository, followed by
     * a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRByAdmin()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwr---", ADMIN,
                "testImportMetadataAfterUploadToRepositoryRWRByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a group
     * owner. The image is first uploaded to the repository, followed by a
     * metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRByGroupOwner()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwr---", GROUP_OWNER,
                "testImportMetadataAfterUploadToRepositoryRWRByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RWR---</code> group by a
     * general member. The image is first uploaded to the repository, followed
     * by a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWRByMember()
            throws Throwable {
        importMetadataAfterUploadToRepository("rwr---", MEMBER,
                "testImportMetadataAfterUploadToRepositoryRWRByMember");
    }

    /**
     * Tests the import of an image into a <code>RW---</code> group by an
     * administrator. The image is first uploaded to the repository, followed by
     * a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWByAdmin()
            throws Throwable {
        importMetadataAfterUploadToRepository("rw----", ADMIN,
                "testImportMetadataAfterUploadToRepositoryRWByAdmin");
    }

    /**
     * Tests the import of an image into a <code>RW----</code> group by a group
     * owner. The image is first uploaded to the repository, followed by a
     * metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWByGroupOwner()
            throws Throwable {
        importMetadataAfterUploadToRepository("rw----", GROUP_OWNER,
                "testImportMetadataAfterUploadToRepositoryRWByGroupOwner");
    }

    /**
     * Tests the import of an image into a <code>RW----</code> group by a
     * general member. The image is first uploaded to the repository, followed
     * by a metadata import.
     *
     * @throws Throwable
     *             Thrown if an error occurred.
     */
    @Test
    public void testImportMetadataAfterUploadToRepositoryRWByMember()
            throws Throwable {
        importMetadataAfterUploadToRepository("rw----", MEMBER,
                "testImportMetadataAfterUploadToRepositoryRWByMember");
    }

    /**
     * Test that an imported image is placed into a target dataset even if the dataset is edited when thumbnails are generated.
     * @throws Exception unexpected
     */
    @Test
    public void testImportTargetEditedDuringImport() throws Exception {
        login("rw----", AbstractServerTest.MEMBER);

        /* create the target dataset */
        Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset());
        final long datasetId = dataset.getId().getValue();
        final long groupId = dataset.getDetails().getGroup().getId().getValue();

        /* prepare a new description for the dataset */
        final String newDescription = "this dataset's description is edited mid-import";
        Assert.assertNotEquals(dataset.getDescription().getValue(), newDescription);

        /* prepare import settings for a fake image that has plenty of planes */
        final File imageFile = File.createTempFile("testImportTargetEditedDuringImport", "&sizeZ=16&sizeT=16.fake");
        imageFile.deleteOnExit();
        final ImportContainer container = new ImportContainer(imageFile, dataset.proxy(), null, FakeReader.class.getName(),
                new String[] {imageFile.toString()}, false);
        final Fileset fs = new FilesetI();
        final ImportSettings settings = new ImportSettings();
        container.fillData(settings, fs, new ClientFilePathTransformer(Functions.<String>identity()), null);
        settings.checksumAlgorithm = new ChecksumAlgorithmI();
        settings.checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmMurmur3128.value));

        /* use the settings to start an import process into a managed repository */
        ManagedRepositoryPrx managedRepository = null;
        for (final RepositoryPrx repository : client.getSession().sharedResources().repositories().proxies) {
            managedRepository = ManagedRepositoryPrxHelper.checkedCast(repository);
            if (managedRepository != null) {
                break;
            }
        }
        final ImportProcessPrx proc = managedRepository.importFileset(fs, settings);

        /* upload the fake image file */
        final Map<String, String> uploadContext = new HashMap<>();
        uploadContext.put("omero.fs.mode", "rw");
        final RawFileStorePrx rfs = proc.getUploader(0, uploadContext);
        rfs.write(ArrayUtils.EMPTY_BYTE_ARRAY, 0, 0);
        rfs.close(uploadContext);

        /* verify the uploaded file */
        final ChecksumProvider cp = new ChecksumProviderFactoryImpl().getProvider(
                ChecksumAlgorithmMapper.getChecksumType(proc.getImportSettings().checksumAlgorithm));
        final HandlePrx handle = proc.verifyUpload(Collections.singletonList(cp.checksumAsString()));

        /* monitor the server-side import process */
        @SuppressWarnings("serial")
        final CmdCallbackI callback = new CmdCallbackI(client, handle) {

            /* as root, note the target dataset */
            private final Map<String, String> context = ImmutableMap.of("omero.group", Long.toString(groupId));
            private final Dataset dataset = (Dataset) root.getSession().getQueryService().get("Dataset", datasetId, context);

            @Override
            public void step(int step, int total, Ice.Current current) {
                if (step == 1 /* generate thumbnails */) {
                    try {
                        /* once notified of import step 1, root uses their session to edit the target dataset's description */
                        dataset.setDescription(omero.rtypes.rstring(newDescription));
                        root.getSession().getUpdateService().saveObject(dataset, context);
                    } catch (ServerError se) {
                        Assert.fail("unexpected exception", se);
                    }
                }
            }
        };
        callback.loop(100, scalingFactor);
        Assert.assertNotNull(assertCmd(callback, true));
        handle.close();

        /* check that the image is in the target dataset */
        final DatasetImageLink link = (DatasetImageLink) iQuery.findByQuery(
                "FROM DatasetImageLink WHERE parent.id = :id AND child.name IS :name",
                new ParametersI().addId(datasetId).add("name", omero.rtypes.rstring(imageFile.getName())));
        Assert.assertNotNull(link);

        /* check that root was successful in editing the dataset's description */
        dataset = (Dataset) iQuery.get("Dataset", datasetId);
        Assert.assertEquals(dataset.getDescription().getValue(), newDescription);
    }
}
