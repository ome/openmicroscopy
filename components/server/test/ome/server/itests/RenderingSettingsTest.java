package ome.server.itests;

import ome.api.IRenderingSettings;
import ome.model.containers.Category;
import ome.model.containers.CategoryImageLink;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RenderingSettingsTest extends AbstractManagedContextTest {

    private IRenderingSettings rsx;

    private Pixels p1;

    private Pixels p2;

    // =========================================================================

    @BeforeClass
    protected void init() throws Exception {
        rsx = factory.getRenderingSettingsService();

        p1 = makePixels();
        p2 = makePixels();

    }

    @Test
    public void testApply() {

        Long from = p1.getId();
        Long to = p2.getId();

        rsx.applySettingsToPixels(from, to);

        rsx.resetDefaultsInImage(p2.getImage().getId());
    }

    @Test
    public void testApplyToCollections() {

        Project p1 = new Project();
        p1.setName("prtest1");

        Project p2 = new Project();
        p2.setName("prtest2");

        Dataset d1 = new Dataset();
        d1.setName("dstest1");
        p1.linkDataset(d1);

        Dataset d2 = new Dataset();
        d2.setName("dstest2");
        p2.linkDataset(d2);

        Category c1 = new Category();
        c1.setName("ctest1");

        Category c2 = new Category();
        c2.setName("ctest2");

        // and using proxies works
        p1 = iUpdate.saveAndReturnObject(p1);
        p2 = iUpdate.saveAndReturnObject(p2);

        d1 = iUpdate.saveAndReturnObject(d1);
        d2 = iUpdate.saveAndReturnObject(d2);

        c1 = iUpdate.saveAndReturnObject(c1);
        c2 = iUpdate.saveAndReturnObject(c2);

        ProjectDatasetLink link1 = new ProjectDatasetLink();
        link1.link(p1, d1);
        ProjectDatasetLink link2 = new ProjectDatasetLink();
        link2.link(p2, d2);

        DatasetImageLink ilink1 = new DatasetImageLink();
        ilink1.link(d1, iQuery.get(Image.class, p1.getId()));
        DatasetImageLink ilink2 = new DatasetImageLink();
        ilink2.link(d2, iQuery.get(Image.class, p2.getId()));

        CategoryImageLink clink1 = new CategoryImageLink();
        clink1.link(c1, iQuery.get(Image.class, p1.getId()));

        CategoryImageLink clink2 = new CategoryImageLink();
        clink2.link(c2, iQuery.get(Image.class, p2.getId()));

        iUpdate.saveAndReturnObject(link1);
        iUpdate.saveAndReturnObject(link2);

        iUpdate.saveAndReturnObject(ilink1);
        iUpdate.saveAndReturnObject(ilink2);

        iUpdate.saveAndReturnObject(clink1);
        iUpdate.saveAndReturnObject(clink2);

        rsx.applySettingsToProject(p1.getId(), p2.getId());
        rsx.applySettingsToDataset(d1.getId(), d2.getId());
        rsx.applySettingsToCategory(c1.getId(), c2.getId());

        rsx.resetDefaultsInDataset(d2.getId());
        rsx.resetDefaultsInCategory(c2.getId());

    }

}
