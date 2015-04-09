/*
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee. All rights reserved.
 *
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.model.Pixels;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests import methods exposed by the ImportLibrary.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 4.5 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 4.5
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
        ImportLibrary library = new ImportLibrary(importer, new OMEROWrapper(
                config));
        ImportContainer ic = getCandidates(f).getContainers().get(0);
        List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
        assertNotNull(pixels);
        assertEquals(pixels.size(), 1);
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
        assertNotNull(candidates);
        assertNotNull(candidates.getContainers().get(0));
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
        ImportLibrary library = new ImportLibrary(importer, new OMEROWrapper(
                config));
        ImportContainer ic = getCandidates(f).getContainers().get(0);

        // FIXME: Using importImage here to keep the tests working
        // but this is not the method under test (which has been removed)
        List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
        assertNotNull(pixels);
        assertEquals(pixels.size(), 1);
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
        ImportLibrary library = new ImportLibrary(importer, new OMEROWrapper(
                config));
        ImportContainer ic = getCandidates(f).getContainers().get(0);
        ic = new ImportContainer(f, null, null, null, ic.getUsedFiles(), null);
        List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
        assertNotNull(pixels);
        assertEquals(pixels.size(), 1);
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
}
