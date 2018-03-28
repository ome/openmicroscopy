/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.hibernate;

import java.util.Arrays;
import java.util.Set;

import ome.model.IAnnotated;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.BasicAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.ObjectFactory;
import ome.tools.hibernate.ExtendedMetadata;

import org.hibernate.SessionFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ExtendedMetadataTest extends AbstractManagedContextTest {

    ExtendedMetadata.Impl metadata;

    @BeforeClass
    public void init() throws Exception {
        setUp();
        metadata = new ExtendedMetadata.Impl();
        metadata.setSessionFactory((SessionFactory)applicationContext.getBean("sessionFactory"));
        tearDown();
    }

    @Test
    public void testAnnotatedAreFound() throws Exception {
        Set<Class<IAnnotated>> anns = metadata.getAnnotatableTypes();
        assertTrue(anns.contains(Image.class));
        assertTrue(anns.contains(Project.class));
        // And several others
    }


    @Test
    public void testAnnotationsAreFound() throws Exception {
        Set<Class<Annotation>> anns = metadata.getAnnotationTypes();
        assertTrue(anns.toString(), anns.contains(Annotation.class));
        assertTrue(anns.toString(), anns.contains(BasicAnnotation.class));
        assertTrue(anns.toString(), anns.contains(LongAnnotation.class));
        // And several others
    }

    /**
     * Where a superclass has a relationship to a class (Annotation to some link type),
     * it is also necessary to be able to find the same relationship from a subclass
     * (e.g. FileAnnotation).
     */
    @Test
    public void testLinkFromSubclassToSuperClassRel() {
       assertNotNull(
               metadata.getRelationship("ImageAnnotationLink", "FileAnnotation"));
    }

    /**
     * For simplicity, the relationship map currently holds only the short
     * class names. Here we are adding a test which checks for the full ones
     * under "broken" to remember to re-evaluate.
     */
    @Test(groups = {"broken","fixme"})
    public void testAnnotatedAreFoundByFQN() throws Exception {
        Set<Class<IAnnotated>> anns = metadata.getAnnotatableTypes();
        assertTrue(anns.contains(Image.class));
        assertTrue(anns.contains(Project.class));
        // And several others
    }
    // ~ Locking
    // =========================================================================

    @Test
    public void testProjectLocksDataset() throws Exception {
        Project p = new Project();
        Dataset d = new Dataset();
        p.linkDataset(d);
        ILink l = (ILink) p.collectDatasetLinks(null).iterator().next();

        assertDoesntContain(metadata.getLockCandidates(p), d);
        assertContains(metadata.getLockCandidates(l), d);

    }

    @Test
    // Because Pixels does not have a reference to RenderingDef
    public void testRenderingDefLocksPixels() throws Exception {
        Pixels p = ObjectFactory.createPixelGraph(null);
        RenderingDef r = ObjectFactory.createRenderingDef();

        r.setPixels(p);

        assertContains(metadata.getLockCandidates(r), p);

    }

    @Test(groups = "ticket:357")
    // quirky because of defaultTag
    // see https://trac.openmicroscopy.org/ome/ticket/357
    public void testPixelsLocksImage() throws Exception {
        Pixels p = ObjectFactory.createPixelGraph(null);
        Image i = new Image();
        i.setName("locking");
        i.addPixels(p);

        assertContains(metadata.getLockCandidates(p), i);

    }

    @Test
    // omit locks for system types (TODO they shouldn't have permissions anyway)
    public void testExperimenterDoesntGetLocked() throws Exception {
        Experimenter e = new Experimenter();
        Project p = new Project();
        p.getDetails().setOwner(e);

        assertDoesntContain(metadata.getLockCandidates(p), e);
    }

    @Test
    public void testNoNulls() throws Exception {
        Project p = new Project();
        ProjectDatasetLink pdl = new ProjectDatasetLink();
        pdl.link(p, null);

        assertDoesntContain(metadata.getLockCandidates(pdl), null);

    }

    // ~ Unlocking
    // =========================================================================

    @Test
    public void testProjectCanBeUnlockedFromDataset() throws Exception {
        assertContains(metadata.getLockChecks(Project.class),
                ProjectDatasetLink.class.getName(), "parent");
    }

    @Test
    // Because Pixels does not have a reference to RenderingDef
    public void testPixelsCanBeUnlockedFromRenderingDef() throws Exception {
        assertContains(metadata.getLockChecks(Pixels.class), RenderingDef.class
                .getName(), "pixels");
    }

    @Test(groups = "ticket:357")
    // quirky because of defaultTag
    // see https://trac.openmicroscopy.org/ome/ticket/357
    public void testImageCanBeUnlockedFromPixels() throws Exception {
        assertContains(metadata.getLockChecks(Image.class), Pixels.class
                .getName(), "image");
    }

    // ~ Updating
    // =========================================================================

    @Test(groups = { "ticket:346", "broken" })
    public void testCreateEventImmutable() throws Exception {
        assertContains(metadata.getImmutableFields(Image.class),
                "details.creationEvent");
    }

    // ~ Counting
    // =========================================================================

    @Test(groups = { "ticket:657" })
    public void testCountQueriesAreCorrect() throws Exception {
        assertEquals(metadata.getCountQuery(DatasetImageLink.CHILD), metadata
                .getCountQuery(DatasetImageLink.CHILD),
                "select target.child.id, count(target) "
                        + "from ome.model.containers.DatasetImageLink target "
                        + "group by target.child.id");
        assertEquals(metadata.getCountQuery(Pixels.IMAGE), metadata
                .getCountQuery(Pixels.IMAGE),
                "select target.image.id, count(target) "
                        + "from ome.model.core.Pixels target "
                        + "group by target.image.id");

    }

    @Test(groups = { "ticket:657" })
    public void testTargetTypes() throws Exception {
        assertEquals(metadata.getTargetType(Pixels.IMAGE), Image.class);
        assertEquals(metadata.getTargetType(DatasetImageLink.CHILD),
                Image.class);
    }

    // ~ Relationships
    // =========================================================================

    @Test(groups = "ticket:2665")
    public void testRelationships() {
        String rel;
        rel = metadata.getRelationship(Pixels.class.getSimpleName(), Image.class.getSimpleName());
        assertEquals("image", rel);
        rel = metadata.getRelationship(Image.class.getSimpleName(), Pixels.class.getSimpleName());
        assertEquals("pixels", rel);
    }

    // ~ Helpers
    // =========================================================================

    private void assertContains(Object[] array, Object i) {
        if (!contained(array, i)) {
            fail(i + " not contained in " + Arrays.toString(array));
        }
    }

    private void assertDoesntContain(IObject[] array, IObject i) {
        if (contained(array, i)) {
            fail(i + " contained in " + Arrays.toString(array));
        }
    }

    private void assertContains(String[][] array, String t1, String t2) {
        boolean contained = false;

        for (int i = 0; i < array.length; i++) {
            String[] test = array[i];
            if (test[0].equals(t1) && test[1].equals(t2)) {
                contained |= true;
            }
        }
        assertTrue(contained);

    }

    private boolean contained(Object[] array, Object i) {
        boolean contained = false;
        for (Object object : array) {
            if (i == null) {
                if (object == null) {
                    contained = true;
                }
            } else {
                if (i.equals(object)) {
                    contained = true;
                }
            }
        }
        return contained;
    }

}
