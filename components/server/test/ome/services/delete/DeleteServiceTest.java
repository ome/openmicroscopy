/*
 *   Copyright 20078 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import ome.api.IDelete;
import ome.conditions.ApiUsageException;
import ome.formats.importer.util.TinyImportFixture;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

@Test(groups = { "integration" })
public class DeleteServiceTest extends AbstractManagedContextTest {

    @Test
    public void testSimpleDelete() throws Exception {
        Image i = makeImage();
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), true);
    }

    @Test
    public void testDeleteWithOtherDatasets() throws Exception {
        Image i = makeImage();
        Dataset d = new Dataset();
        d.setName("deletes");
        DatasetImageLink link = new DatasetImageLink();
        link.link(d, i.proxy());
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), true);
    }

    public void testDeleteWithAnnotation() throws Exception {
        Image i = makeImage();
        TextAnnotation ta = new TextAnnotation();
        ta.setName("");
        ta.setTextValue("");
        ImageAnnotationLink link = new ImageAnnotationLink();
        link.link(i.proxy(), ta);
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), true);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testDeleteWithOtherDatasetsNoForce() throws Exception {
        Image i = makeImage();
        Dataset d = new Dataset();
        d.setName("deletes");
        DatasetImageLink link = new DatasetImageLink();
        link.link(d, i.proxy());
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), false);
    }

    public void testDeletePreviewList() throws Exception {
        Image i = makeImage();
        Dataset d = new Dataset();
        d.setName("deletes");
        DatasetImageLink link = new DatasetImageLink();
        link.link(d, i.proxy());
        this.factory.getUpdateService().saveObject(link);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        assertTrue(srv.previewImageDelete(i.getId(), false).size() > 1);
    }

    private Image makeImage() throws Exception {
        TinyImportFixture tiny = new TinyImportFixture(this.factory);
        tiny.setUp();
        tiny.doImport();
        Dataset d = tiny.getDataset();
        Image i = this.factory.getQueryService().findByQuery(
                "select i from Image i " + "join i.datasetLinks dil "
                        + "join dil.parent d where d.id = :id",
                new Parameters().addId(d.getId()));
        return i;
    }

}
