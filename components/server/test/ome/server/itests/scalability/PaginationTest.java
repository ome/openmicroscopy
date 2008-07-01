package ome.server.itests.scalability;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.util.builders.PojoOptions;

import org.testng.annotations.Test;

@Test(groups = { "integration" })
public class PaginationTest extends AbstractManagedContextTest {

    static int BIG_BATCH = 50;// 0;
    static int BIG_SIZE = 250;// 00;

    @Test
    public void testGetImagesFromDatasetPagination() {
        PojoOptions po = new PojoOptions().leaves();// .exp(exp);
        Dataset d = loadOrCreateDataset(BIG_BATCH, BIG_SIZE);
        Set<Long> datasetIds = Collections.singleton(d.getId());
        runGetImagesTest(Dataset.class, datasetIds, po, BIG_SIZE, 5);
    }

    @Test
    public void testGetImagesFromProjectPagination() {
        PojoOptions po = new PojoOptions().leaves();// .exp(exp);
        Project p = loadOrCreateProject(BIG_BATCH, BIG_SIZE);
        Set<Long> projectIds = Collections.singleton(p.getId());
        runGetImagesTest(Project.class, projectIds, po, BIG_SIZE, 5);
    }

    // Helpers
    // ===========================================================

    private void runGetImagesTest(Class kls, Set<Long> containerIds,
            PojoOptions po, int size, int pages) {
        long start;
        start = System.currentTimeMillis();
        Set<Image> images = iPojos.getImages(kls, containerIds, po.map());
        assertTrue(images.size() + " items", images.size() >= size);
        long getImagesTime = System.currentTimeMillis() - start;

        // For checking
        Set<Long> allImageIds = new HashSet<Long>();
        for (Image img : images) {
            allImageIds.add(img.getId());
        }
        Set<Long> pagedImageIds = new HashSet<Long>();

        // Try with pagination
        long[] pageTimes = new long[pages];
        assertTrue(size % pages == 0);
        int page_size = size / pages;
        for (int i = 0; i < pages; i++) {
            po.paginate(i * page_size, page_size);
            start = System.currentTimeMillis();
            images = iPojos.getImages(kls, containerIds, po.map());
            pageTimes[i] = System.currentTimeMillis() - start;
            assertTrue(images.size() + " items", images.size() == page_size);
            for (Image img : images) {
                assertTrue(img + " - extra", allImageIds.contains(img.getId()));
                pagedImageIds.add(img.getId());
            }
        }

        // Make sure we found all of them.
        Set<Long> test = new HashSet<Long>(allImageIds);
        test.removeAll(pagedImageIds);
        assertTrue("Remainder:" + test.toString(), test.size() == 0);
    }

    private Dataset loadOrCreateDataset(int batch, int size) {
        int loops = size / batch;
        Dataset d = (Dataset) iQuery.findByQuery("select d from Dataset d "
                + "join fetch d.imageLinks " + "where size(d.imageLinks) = "
                + size, new Parameters(new Filter().page(0, 1)));
        if (d == null) {
            d = new Dataset("big dataset");
            d = iUpdate.saveAndReturnObject(d);
            for (int i = 0; i < loops; i++) {
                DatasetImageLink[] links = new DatasetImageLink[batch];
                for (int j = 0; j < batch; j++) {
                    links[j] = new DatasetImageLink();
                    links[j].link(d.proxy(), new Image("image in big dataset "
                            + i * i));
                }
                iUpdate.saveArray(links);
            }
        }
        return d;
    }

    private Project loadOrCreateProject(int batch, int size) {
        int loops = size / batch;
        Project p = (Project) iQuery.findByQuery(
                "select p from Project p join p.datasetLinks pdl join pdl.child d "
                        + "join d.imageLinks "
                        + "where size(p.datasetLinks) = " + batch
                        + " and size(d.imageLinks) = " + size, new Parameters(
                        new Filter().page(0, 1)));
        if (p == null) {
            p = new Project("big project");
            p = iUpdate.saveAndReturnObject(p);
            for (int i = 0; i < loops; i++) {
                Dataset d = new Dataset("dataset in big project");
                ProjectDatasetLink pdl = new ProjectDatasetLink(p.proxy(), d);
                pdl = iUpdate.saveAndReturnObject(pdl);
                d = pdl.child();
                DatasetImageLink[] links = new DatasetImageLink[batch];
                for (int j = 0; j < batch; j++) {
                    links[j] = new DatasetImageLink();
                    links[j].link(d.proxy(), new Image("image in big project "
                            + i * i));
                }
                iUpdate.saveArray(links);
            }
        }
        return p;
    }
}
