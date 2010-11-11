/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ome.model.annotations.Annotation;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.BasicAnnotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.DatasetAnnotationLink;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.ListAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.NumericAnnotation;
import ome.model.annotations.ProjectAnnotationLink;
import ome.model.annotations.RoiAnnotationLink;
import ome.model.annotations.TermAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.TimestampAnnotation;
import ome.model.annotations.TypeAnnotation;
import ome.model.annotations.XmlAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.roi.Roi;
import ome.model.roi.Shape;
import ome.security.basic.CurrentDetails;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;
import omero.model.Plate;
import omero.model.PlateAnnotationLink;
import omero.model.Reagent;
import omero.model.ReagentAnnotationLink;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenPlateLink;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellReagentLink;
import omero.model.WellSample;
import omero.model.WellSampleAnnotationLink;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class MockDeleteTest extends MockObjectTestCase {

    public static class MockCurrentDetails extends CurrentDetails {
        @Override
        public EventContext getCurrentEventContext() {
            return createEventContext(false);
        }
    }

    OmeroContext specXml;

    Mock emMock;

    @BeforeMethod
    public void setup() {
        StaticApplicationContext sac = new StaticApplicationContext();

        ConstructorArgumentValues cav = new ConstructorArgumentValues();
        cav.addGenericArgumentValue(ExtendedMetadata.class);
        RootBeanDefinition mock = new RootBeanDefinition(Mock.class, cav, null);

        RootBeanDefinition em = new RootBeanDefinition();
        em.setFactoryBeanName("mock");
        em.setFactoryMethodName("proxy");

        RootBeanDefinition cd = new RootBeanDefinition(MockCurrentDetails.class);

        sac.registerBeanDefinition("currentDetails", cd);
        sac.registerBeanDefinition("mock", mock);
        sac.registerBeanDefinition("extendedMetadata", em);
        sac.refresh();

        emMock = sac.getBean("mock", Mock.class);
        emMock.expects(atLeastOnce())
                .method("getAnnotationTypes")
                .will(returnValue(new HashSet<Class<?>>(Arrays
                        .<Class<?>> asList(Annotation.class,
                                BasicAnnotation.class,
                                BooleanAnnotation.class,
                                NumericAnnotation.class,
                                DoubleAnnotation.class,
                                LongAnnotation.class,
                                TermAnnotation.class,
                                TimestampAnnotation.class,
                                ListAnnotation.class,
                                TextAnnotation.class,
                                CommentAnnotation.class,
                                ome.model.annotations.TagAnnotation.class,
                                XmlAnnotation.class,
                                TypeAnnotation.class,
                                FileAnnotation.class))));
        specXml = new OmeroContext(
                new String[] { "classpath:ome/services/delete/spec.xml" }, sac);
    }

    //
    // Helpers
    //

    protected Object getOp(DeleteEntry de) throws Exception {
        Field field = DeleteEntry.class.getDeclaredField("operation");
        field.setAccessible(true);
        return field.get(de);
    }


    protected static EventContext createEventContext(boolean admin) {
        Mock m = new Mock(EventContext.class);
        MockDeleteTest t = new MockDeleteTest();
        m.expects(t.once()).method("isCurrentUserAdmin").will(t.returnValue(admin));
        m.expects(t.once()).method("getCurrentUserId").will(t.returnValue(1L));
        EventContext ec = (EventContext) m.proxy();
        return ec;
    }


    protected DeleteEntry findEntry(BaseDeleteSpec spec, String name) {
        Integer idx = null;
        DeleteEntry entry = null;
        // Find the right entry for /Image
        idx = null;
        for (int i = 0; i < spec.entries.size(); i++) {
            entry = spec.entries.get(i);
            if (entry.getName().equals(name)) {
                idx = i;
                break;
            }
        }
        assertNotNull(idx);
        return entry;
    }

    static Map<String, Map<String, String>> relationships = new HashMap<String, Map<String, String>>();
    static {
        Map<String, String> values = new HashMap<String, String>();
        values.put("Shape", "shapes");
        values.put("RoiAnnotationLink", "annotationLinks");
        relationships.put("Roi", values);
        values = new HashMap<String, String>();
        values.put("AnnotationAnnotationLink", "parent");
        values.put("OriginalFile", "file");
        relationships.put("Annotation", values);
        values = new HashMap<String, String>();
        values.put("Annotation", "child");
        relationships.put("RoiAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("ProjectDatasetLink", "datasetLinks");
        values.put("ProjectAnnotationLink", "annotationLinks");
        relationships.put("Project", values);

        values = new HashMap<String, String>();
        values.put("Project", "parent");
        values.put("Annotation", "child");
        relationships.put("ProjectAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("Dataset", "child");
        values.put("Project", "parent");
        relationships.put("ProjectDatasetLink", values);

        values = new HashMap<String, String>();
        values.put("ProjectDatasetLink", "projectLinks");
        values.put("DatasetAnnotationLink", "annotationLinks");
        values.put("DatasetImageLink", "imageLinks");
        relationships.put("Dataset", values);

        values = new HashMap<String, String>();
        values.put("Dataset", "parent");
        values.put("Annotation", "child");
        relationships.put("DatasetAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("Dataset", "parent");
        values.put("Image", "child");
        relationships.put("DatasetImageLink", values);

        values = new HashMap<String, String>();
        values.put("DatasetImageLink", "datasetLinks");
        values.put("ImageAnnotationLink", "annotationLinks");
        values.put("Pixels", "pixels");
        relationships.put("Image", values);

        // SPW

        values = new HashMap<String, String>();
        values.put("ScreenPlateLink", "plateLinks");
        values.put("ScreenAnnotationLink", "annotationLinks");
        values.put("Reagent", "reagent");
        relationships.put("Screen", values);

        values = new HashMap<String, String>();
        values.put("Annotation", "child");
        relationships.put("ScreenAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("Plate", "child");
        relationships.put("ScreenPlateLink", values);

        values = new HashMap<String, String>();
        values.put("ReagentAnnotationLink", "annotationLinks");
        relationships.put("Reagent", values);

        values = new HashMap<String, String>();
        values.put("Annotation", "child");
        relationships.put("ReagentAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("Well", "wells");
        values.put("PlateAcquisition", "plateAcquisition");
        values.put("PlateAnnotationLink", "annotationLinks");
        relationships.put("Plate", values);

        values = new HashMap<String, String>();
        values.put("Well", "wells");
        values.put("Annotation", "child");
        relationships.put("PlateAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("PlateAcquisitionAnnotationLink", "annotationLinks");
        relationships.put("PlateAcquisition", values);

        values = new HashMap<String, String>();
        values.put("Annotation", "child");
        relationships.put("PlateAcquisitionAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("WellSample", "wellSamples");
        values.put("WellAnnotationLink", "annotationLinks");
        values.put("WellReagentLink", "reagentLinks");
        relationships.put("Well", values);

        values = new HashMap<String, String>();
        values.put("Reagent", "child");
        relationships.put("WellReagentLink", values);

        values = new HashMap<String, String>();
        values.put("Annotation", "child");
        relationships.put("WellAnnotationLink", values);

        values = new HashMap<String, String>();
        values.put("WellSampleAnnotationLink", "annotationLinks");
        values.put("Image", "image");
        relationships.put("WellSample", values);

        values = new HashMap<String, String>();
        values.put("Annotation", "child");
        relationships.put("WellSampleAnnotationLink", values);

    }

    protected void prepareGetRelationship() {
        emMock.expects(atLeastOnce()).method("getRelationship").will(new Stub(){

            public StringBuffer describeTo(StringBuffer arg0) {
                arg0.append("calls getRelationship");
                return arg0;
            }

            public Object invoke(Invocation arg0) throws Throwable {
                String k1 = (String) arg0.parameterValues.get(0);
                String k2 = (String) arg0.parameterValues.get(1);
                Map<String, String> v = relationships.get(k1);
                if (v == null) {
                    fail("Unknown rel: " + k1 + "->" + k2);
                }
                return v.get(k2);
            }});
    }

    protected void prepareGetHibernateClass() {
        emMock.expects(atLeastOnce()).method("getHibernateClass").will(new Stub() {

            public StringBuffer describeTo(StringBuffer arg0) {
                arg0.append("return a hibernate class");
                return arg0;
            }

            public Object invoke(Invocation arg0) throws Throwable {
                String name = (String) arg0.parameterValues.get(0);
                if (name.equals("Roi")) {
                    return Roi.class;
                } else if (name.equals("Shape")) {
                    return Shape.class;
                } else if (name.equals("Annotation")) {
                    return Annotation.class;
                } else if (name.equals("RoiAnnotationLink")) {
                    return RoiAnnotationLink.class;
                } else if (name.equals("AnnotationAnnotationLink")) {
                    return AnnotationAnnotationLink.class;
                } else if (name.equals("BooleanAnnotation")) {
                    return BooleanAnnotation.class;
                } else if (name.equals("BooleanAnnotation")) {
                    return BooleanAnnotation.class;
                } else if (name.equals("Project")) {
                    return Project.class;
                } else if (name.equals("ProjectAnnotationLink")) {
                    return ProjectAnnotationLink.class;
                } else if (name.equals("ProjectDatasetLink")) {
                    return ProjectDatasetLink.class;
                } else if (name.equals("Dataset")) {
                    return Dataset.class;
                } else if (name.equals("DatasetAnnotationLink")) {
                    return DatasetAnnotationLink.class;
                } else if (name.equals("DatasetImageLink")) {
                    return DatasetImageLink.class;
                } else if (name.equals("Image")) {
                    return Image.class;
                } else if (name.equals("ImageAnnotationLink")) {
                    return ImageAnnotationLink.class;
                } else if (name.equals("Screen")) {
                    return Screen.class;
                } else if (name.equals("ScreenAnnotationLink")) {
                    return ScreenAnnotationLink.class;
                } else if (name.equals("ScreenPlateLink")) {
                    return ScreenPlateLink.class;
                } else if (name.equals("Plate")) {
                    return Plate.class;
                } else if (name.equals("PlateAnnotationLink")) {
                    return PlateAnnotationLink.class;
                } else if (name.equals("Well")) {
                    return Well.class;
                } else if (name.equals("WellAnnotationLink")) {
                    return WellAnnotationLink.class;
                } else if (name.equals("WellReagentLink")) {
                    return WellReagentLink.class;
                } else if (name.equals("WellSample")) {
                    return WellSample.class;
                } else if (name.equals("WellSampleAnnotationLink")) {
                    return WellSampleAnnotationLink.class;
                } else if (name.equals("Reagent")) {
                    return Reagent.class;
                } else if (name.equals("ReagentAnnotationLink")) {
                    return ReagentAnnotationLink.class;
                } else {
                    fail("Unknown: " + name);
                    return null;
                }
            }
        });
    }


}
