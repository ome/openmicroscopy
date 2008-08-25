/*
 *   Copyright 20078 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.Arrays;
import java.util.HashSet;

import ome.api.IDelete;
import ome.api.ThumbnailStore;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.formats.importer.util.TinyImportFixture;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

@Test(groups = { "integration" })
public class DeleteServiceTest extends AbstractManagedContextTest {

    @Test
    public void testSimpleDelete() throws Exception {
        Image i = makeImage(true);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), true);
    }

    @Test
    public void testDeleteWithOtherDatasets() throws Exception {
        Image i = makeImage(true);
        Dataset d = new Dataset();
        d.setName("deletes");
        DatasetImageLink link = new DatasetImageLink();
        link.link(d, i.proxy());
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), true);
    }

    public void testDeleteWithAnnotation() throws Exception {
        Image i = makeImage(true);
        TextAnnotation ta = new TextAnnotation();
        ta.setNs("");
        ta.setTextValue("");
        ImageAnnotationLink link = new ImageAnnotationLink();
        link.link(i.proxy(), ta);
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), true);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testDeleteWithOtherDatasetsNoForce() throws Exception {
        Image i = makeImage(true);
        Dataset d = new Dataset();
        d.setName("deletes");
        DatasetImageLink link = new DatasetImageLink();
        link.link(d, i.proxy());
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), false);
    }

    public void testDeletePreviewList() throws Exception {
        Image i = makeImage(true);
        Dataset d = new Dataset();
        d.setName("deletes");
        DatasetImageLink link = new DatasetImageLink();
        link.link(d, i.proxy());
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        assertTrue(srv.previewImageDelete(i.getId(), false).size() > 1);
    }

    public void testDeleteByIds() throws Exception {
        Image i1 = makeImage(false);
        Image i2 = makeImage(false);
        Image i3 = makeImage(false);
        IDelete srv = this.factory.getDeleteService();
        srv.deleteImages(new HashSet<Long>(Arrays.asList(i1.getId(),
                i2.getId(), i3.getId())), false);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testDeleteByIdsWithOtherDatasetNoForce() throws Exception {
        Image i1 = makeImage(false);
        Image i2 = makeImage(false);
        Image i3 = makeImage(false);
        Dataset containsAll = new Dataset("containsAll");
        containsAll.linkImage(i1);
        containsAll.linkImage(i2);
        containsAll.linkImage(i3);
        iUpdate.saveObject(containsAll);
        IDelete srv = this.factory.getDeleteService();
        srv.deleteImages(new HashSet<Long>(Arrays.asList(i1.getId(),
                i2.getId(), i3.getId())), false);
    }

    public void testDeleteByIdsWithOtherDatasetForce() throws Exception {
        Image i1 = makeImage(false);
        Image i2 = makeImage(false);
        Image i3 = makeImage(false);
        Dataset containsAll = new Dataset("containsAll");
        containsAll.linkImage(i1);
        containsAll.linkImage(i2);
        containsAll.linkImage(i3);
        iUpdate.saveObject(containsAll);
        IDelete srv = this.factory.getDeleteService();
        srv.deleteImages(new HashSet<Long>(Arrays.asList(i1.getId(),
                i2.getId(), i3.getId())), true);
    }

    public void testDeleteByDataset() throws Exception {
        Image i1 = makeImage(false);
        Image i2 = makeImage(false);
        Image i3 = makeImage(false);
        Dataset containsAll = new Dataset("containsAll");
        containsAll.linkImage(i1);
        containsAll.linkImage(i2);
        containsAll.linkImage(i3);
        iUpdate.saveObject(containsAll);
        IDelete srv = this.factory.getDeleteService();
        srv.deleteImagesByDataset(containsAll.getId(), false);
    }

    public void testDeleteByDatasetWithOtherDatasetForce() throws Exception {
        Image i1 = makeImage(true);
        Image i2 = makeImage(true);
        Image i3 = makeImage(true);
        Dataset containsAll = new Dataset("containsAll");
        containsAll.linkImage(i1);
        containsAll.linkImage(i2);
        containsAll.linkImage(i3);
        iUpdate.saveObject(containsAll);
        IDelete srv = this.factory.getDeleteService();
        srv.deleteImagesByDataset(containsAll.getId(), true);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testDeleteByDatasetWithOtherDatasetNoForce() throws Exception {
        Image i1 = makeImage(true);
        Image i2 = makeImage(true);
        Image i3 = makeImage(true);
        Dataset containsAll = new Dataset("containsAll");
        containsAll.linkImage(i1);
        containsAll.linkImage(i2);
        containsAll.linkImage(i3);
        iUpdate.saveObject(containsAll);
        IDelete srv = this.factory.getDeleteService();
        srv.deleteImagesByDataset(containsAll.getId(), false);
    }

    // Multiuser tests
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void testDeleteByDatasetWithTwoImagesFromDifferentUsers()
            throws Exception {

        Experimenter e1 = loginNewUser();
        Image i1 = makeImage(false);

        Experimenter e2 = loginNewUser();
        Image i2 = makeImage(false);

        Dataset containsAll = new Dataset("containsAll");
        containsAll.linkImage(i1);
        containsAll.linkImage(i2);
        iUpdate.saveObject(containsAll);

        IDelete srv = this.factory.getDeleteService();
        srv.deleteImagesByDataset(containsAll.getId(), true);
    }

    public void testDeleteImageAfterViewedByAnotherUser() throws Exception {

        Experimenter e1 = loginNewUser();
        Image i1 = makeImage(false);
        Pixels p1 = i1.iteratePixels().next();

        Experimenter e2 = loginNewUser();
        ThumbnailStore tb = this.factory.createThumbnailService();
        tb.setPixelsId(p1.getId());
        tb.resetDefaults();
        tb.setPixelsId(p1.getId());
        tb.createThumbnails();

        loginUser(e1.getOmeName());
        factory.getDeleteService().deleteImage(i1.getId(), false);
    }

    // Helpers
    // =========================================================================

    private Image makeImage(boolean withDataset) throws Exception {
        TinyImportFixture tiny = new TinyImportFixture(this.factory);
        tiny.setUp();
        tiny.doImport();
        Dataset d = tiny.getDataset();
        Image i = this.factory
                .getQueryService()
                .findByQuery(
                        "select i from Image i join fetch i.datasetLinks dil "
                                + "join fetch dil.parent d "
                                + "join fetch d.imageLinks "
                                + "join fetch i.pixels p " + "where d.id = :id",
                        new Parameters().addId(d.getId()));
        if (!withDataset) {
            for (DatasetImageLink link : i.unmodifiableDatasetLinks()) {
                iUpdate.deleteObject(link);
            }
            iUpdate.deleteObject(d);
            i.clearDatasetLinks();
        }
        return i;
    }

}
