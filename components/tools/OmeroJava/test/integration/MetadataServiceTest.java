/*
 * $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration;

import static omero.rtypes.rbool;
import static omero.rtypes.rdouble;
import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.api.JobHandle;
import ome.system.Login;
import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.AcquisitionMode;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.ContrastMethod;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.DoubleAnnotation;
import omero.model.DoubleAnnotationI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Filament;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.Job;
import omero.model.JobOriginalFileLink;
import omero.model.JobOriginalFileLinkI;
import omero.model.JobStatus;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.MapAnnotation;
import omero.model.MapAnnotationI;
import omero.model.NamedValue;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAcquisitionI;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.UploadJob;
import omero.model.UploadJobI;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellAnnotationLinkI;
import omero.model.WellSample;
import omero.model.XmlAnnotation;
import omero.model.XmlAnnotationI;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import omero.sys.Roles;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.ChannelAcquisitionData;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.InstrumentData;
import omero.gateway.model.LightSourceData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.XMLAnnotationData;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Collections of tests for the <code>IMetadata</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class MetadataServiceTest extends AbstractServerTest {

    /** Identifies the file annotation. */
    private static final String FILE_ANNOTATION = "ome.model.annotations.FileAnnotation";

    /** Identifies the file annotation. */
    private static final String MAP_ANNOTATION = "ome.model.annotations.MapAnnotation";
    
    /** Helper reference to the <code>IAdmin</code> service. */
    private IMetadataPrx iMetadata;

    /**
     * Initializes the various services.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        iMetadata = factory.getMetadataService();
    }

    /**
     * Tests the creation of file annotation with an original file and load it.
     * Loads the annotation using the <code>loadAnnotation</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1155")
    public void testLoadFileAnnotation() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        assertNotNull(of);

        FileAnnotationI fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        assertNotNull(data);

        List<Long> ids = new ArrayList<Long>();
        ids.add(data.getId().getValue());
        List<Annotation> annotations = iMetadata.loadAnnotation(ids);
        assertNotNull(annotations);
        Iterator<Annotation> i = annotations.iterator();
        Annotation annotation;
        FileAnnotationData faData;
        while (i.hasNext()) {
            annotation = i.next();
            if (annotation instanceof FileAnnotation) { // test creation of
                                                        // omero.gateway.model
                faData = new FileAnnotationData((FileAnnotation) annotation);
                assertNotNull(faData);
                assertEquals(faData.getFileID(), of.getId().getValue());
            }
        }
    }
    
	/**
	 * Tests the creation of map annotation and load it. Loads the annotation
	 * using the <code>loadAnnotation</code> method.
	 *
	 * @throws Exception
	 *             Thrown if an error occurred.
	 */
	@Test(groups = "ticket:1155")
	public void testLoadMapAnnotation() throws Exception {
		MapAnnotation ma = new MapAnnotationI();
		List<NamedValue> values = new ArrayList<NamedValue>();
		for (int i = 0; i < 3; i++)
			values.add(new NamedValue("name " + i, "value " + i));
		ma.setMapValue(values);
		MapAnnotation data = (MapAnnotation) iUpdate.saveAndReturnObject(ma);
		assertNotNull(data);

		List<Long> ids = new ArrayList<Long>();
		ids.add(data.getId().getValue());
		List<Annotation> annotations = iMetadata.loadAnnotation(ids);
		assertNotNull(annotations);
		Iterator<Annotation> i = annotations.iterator();
		Annotation annotation;
		MapAnnotationData maData;
		while (i.hasNext()) {
			annotation = i.next();
			if (annotation instanceof MapAnnotation) { // test creation of
														// omero.gateway.model
				maData = new MapAnnotationData((MapAnnotation) annotation);
				assertNotNull(maData);

				@SuppressWarnings("unchecked")
				List<NamedValue> list = (List<NamedValue>) maData.getContent();
				assertNotNull(list);
				assertEquals(3, list.size());
				for (int j = 0; j < 3; j++) {
					NamedValue v1 = values.get(j);
					NamedValue v2 = list.get(j);
					assertEquals(v1.name, v2.name);
					assertEquals(v1.value, v2.value);
				}
			}
		}
	}

    /**
     * Tests the creation of file annotation with an original file and load it.
     * Loads the annotation using the <code>loadAnnotations</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1155")
    public void testLoadAnnotationsFileAnnotation() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        assertNotNull(of);

        FileAnnotationI fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        assertNotNull(data);
        // link the image
        // create an image and link the annotation
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        ImageAnnotationLinkI link = new ImageAnnotationLinkI();
        link.setParent(image);
        link.setChild(data);
        iUpdate.saveAndReturnObject(link);

        List<Long> ids = new ArrayList<Long>();
        Parameters param = new Parameters();
        List<Long> nodes = new ArrayList<Long>();
        nodes.add(image.getId().getValue());
        Map<Long, List<IObject>> result = iMetadata.loadAnnotations(
                Image.class.getName(), nodes, Arrays.asList(FILE_ANNOTATION),
                ids, param);
        assertNotNull(result);
        List<IObject> l = result.get(image.getId().getValue());
        assertNotNull(l);
        Iterator<IObject> i = l.iterator();
        IObject o;
        FileAnnotationData faData;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileAnnotation) {
                faData = new FileAnnotationData((FileAnnotation) o);
                assertNotNull(faData);
                assertEquals(faData.getFileID(), of.getId().getValue());
            }
        }
    }
    
    /**
     * Tests the creation of map annotation and load it.
     * Loads the annotation using the <code>loadAnnotations</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1155")
    public void testLoadAnnotationsMapAnnotation() throws Exception {
    	MapAnnotation ma = new MapAnnotationI();
		List<NamedValue> values = new ArrayList<NamedValue>();
		for (int i = 0; i < 3; i++)
			values.add(new NamedValue("name " + i, "value " + i));
		ma.setMapValue(values);
		MapAnnotation data = (MapAnnotation) iUpdate.saveAndReturnObject(ma);
		assertNotNull(data);
		
        // link the image
        // create an image and link the annotation
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        ImageAnnotationLinkI link = new ImageAnnotationLinkI();
        link.setParent(image);
        link.setChild(data);
        iUpdate.saveAndReturnObject(link);

        List<Long> ids = new ArrayList<Long>();
        Parameters param = new Parameters();
        List<Long> nodes = new ArrayList<Long>();
        nodes.add(image.getId().getValue());
        Map<Long, List<IObject>> result = iMetadata.loadAnnotations(
                Image.class.getName(), nodes, Arrays.asList(MAP_ANNOTATION),
                ids, param);
        assertNotNull(result);
        List<IObject> l = result.get(image.getId().getValue());
        assertNotNull(l);
        Iterator<IObject> i = l.iterator();
        IObject o;
        MapAnnotationData maData;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof MapAnnotation) {
                maData = new MapAnnotationData((MapAnnotation) o);
                assertNotNull(maData);
                
                @SuppressWarnings("unchecked")
				List<NamedValue> list = (List<NamedValue>) maData.getContent();
				assertNotNull(list);
				assertEquals(3, list.size());
				for (int j = 0; j < 3; j++) {
					NamedValue v1 = values.get(j);
					NamedValue v2 = list.get(j);
					assertEquals(v1.name, v2.name);
					assertEquals(v1.value, v2.value);
				}
            }
        }
    }

    /**
     * Tests the creation of file annotation with an original file and load it.
     * Loads the annotation using the <code>loadSpecifiedAnnotations</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1155")
    public void testLoadSpecifiedAnnotationsFileAnnotation() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        assertNotNull(of);

        FileAnnotationI fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        assertNotNull(data);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
                FileAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);

        Iterator<Annotation> i = result.iterator();
        Annotation o;
        FileAnnotation r;
        int count = 0;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileAnnotation) {
                r = (FileAnnotation) o;
                count++;
                if (r.getId().getValue() == data.getId().getValue()) {
                    assertEquals(r.getFile().getId().getValue(), of.getId()
                            .getValue());
                    assertEquals(r.getFile().getName().getValue(), of.getName()
                            .getValue());
                    assertEquals(r.getFile().getPath().getValue(), of.getPath()
                            .getValue());
                }
            }
        }
        assertTrue(count > 0);
        assertEquals(count, result.size());
        // Same thing but this time passing ome.model.annotations.FileAnnotation
        result = iMetadata.loadSpecifiedAnnotations(FILE_ANNOTATION, include,
                exclude, param);
        assertNotNull(result);

        i = result.iterator();
        count = 0;
        while (i.hasNext()) {
            o = i.next();
            if (o != null && o instanceof FileAnnotation) {
                r = (FileAnnotation) o;
                count++;
                if (r.getId().getValue() == data.getId().getValue()) {
                    assertEquals(r.getFile().getId().getValue(), of.getId()
                            .getValue());
                    assertEquals(r.getFile().getName().getValue(), of.getName()
                            .getValue());
                    assertEquals(r.getFile().getPath().getValue(), of.getPath()
                            .getValue());
                }
            }
        }
        assertTrue(count > 0);
        assertEquals(count, result.size());
    }

    /**
     * Tests the retrieval of annotations using name space constraints.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsFileAnnotationNsConditions()
            throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        assertNotNull(of);

        String ns = "include";
        FileAnnotationI fa = new FileAnnotationI();
        fa.setFile(of);
        fa.setNs(rstring(ns));
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        assertNotNull(data);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        include.add(ns);
        List<String> exclude = new ArrayList<String>();

        // First test the include condition
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
                FileAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);

        Iterator<Annotation> i = result.iterator();
        Annotation o;
        FileAnnotation r;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileAnnotation) {
                r = (FileAnnotation) o;
                assertNotNull(r.getNs());
                assertEquals(ns, r.getNs().getValue());
            }
        }

        // now test the exclude condition
        include.clear();
        // List of name
        exclude.add(ns);
        result = iMetadata.loadSpecifiedAnnotations(
                FileAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);

        i = result.iterator();
        int count = 0;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileAnnotation) {
                r = (FileAnnotation) o;
                if (r.getNs() != null) {
                    if (ns.equals(r.getNs().getValue()))
                        count++;
                }
            }
        }
        assertEquals(count, 0);
    }

    /**
     * Tests the retrieval of annotations of different types i.e. tag, comment,
     * boolean, long and the conversion into the corresponding
     * <code>omero.gateway.model</code> object.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsVariousTypes() throws Exception {
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag"));
        TagAnnotation tagReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        // First test the include condition
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
                TagAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);

        Iterator<Annotation> i = result.iterator();
        int count = 0;
        TagAnnotationData tagData = null;
        Annotation annotation;
        while (i.hasNext()) {
            annotation = i.next();
            if (annotation instanceof TagAnnotation)
                count++;
            if (annotation.getId().getValue() == tagReturned.getId().getValue())
                tagData = new TagAnnotationData(tagReturned);

        }
        assertEquals(result.size(), count);
        assertNotNull(tagData);
        // comment
        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(rstring("comment"));
        CommentAnnotation commentReturned = (CommentAnnotation) iUpdate
                .saveAndReturnObject(comment);
        result = iMetadata.loadSpecifiedAnnotations(
                CommentAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);
        count = 0;
        TextualAnnotationData commentData = null;
        i = result.iterator();
        while (i.hasNext()) {
            annotation = i.next();
            if (annotation instanceof CommentAnnotation)
                count++;
            if (annotation.getId().getValue() == commentReturned.getId()
                    .getValue())
                commentData = new TextualAnnotationData(commentReturned);
        }
        assertEquals(result.size(), count);
        assertNotNull(commentData);

        // boolean
        BooleanAnnotation bool = new BooleanAnnotationI();
        bool.setBoolValue(rbool(true));
        BooleanAnnotation boolReturned = (BooleanAnnotation) iUpdate
                .saveAndReturnObject(bool);
        result = iMetadata.loadSpecifiedAnnotations(
                BooleanAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);
        count = 0;
        BooleanAnnotationData boolData = null;
        i = result.iterator();
        while (i.hasNext()) {
            annotation = i.next();
            if (annotation instanceof BooleanAnnotation)
                count++;
            if (annotation.getId().getValue() == boolReturned.getId()
                    .getValue())
                boolData = new BooleanAnnotationData(boolReturned);
        }
        assertEquals(result.size(), count);
        assertNotNull(boolData);

        // long
        LongAnnotation l = new LongAnnotationI();
        l.setLongValue(rlong(1));
        LongAnnotation lReturned = (LongAnnotation) iUpdate
                .saveAndReturnObject(l);
        result = iMetadata.loadSpecifiedAnnotations(
                LongAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);
        count = 0;
        LongAnnotationData lData = null;
        i = result.iterator();
        while (i.hasNext()) {
            annotation = i.next();
            if (annotation instanceof LongAnnotation)
                count++;
            if (annotation.getId().getValue() == lReturned.getId().getValue())
                lData = new LongAnnotationData(lReturned);
        }
        assertEquals(result.size(), count);
        assertNotNull(lData);
        // double
        DoubleAnnotation d = new DoubleAnnotationI();
        d.setDoubleValue(rdouble(1));
        DoubleAnnotation dReturned = (DoubleAnnotation) iUpdate
                .saveAndReturnObject(d);
        result = iMetadata.loadSpecifiedAnnotations(
                DoubleAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);
        count = 0;
        DoubleAnnotationData dData = null;
        i = result.iterator();
        while (i.hasNext()) {
            annotation = i.next();
            if (annotation instanceof DoubleAnnotation)
                count++;
            if (annotation.getId().getValue() == dReturned.getId().getValue())
                dData = new DoubleAnnotationData(dReturned);
        }
        assertEquals(result.size(), count);
        assertNotNull(dData);
    }

    /**
     * Tests the retrieval of tag sets
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadTagSetsNoOrphan() throws Exception {
        long self = iAdmin.getEventContext().userId;

        // Create a tag set.
        TagAnnotation tagSet = new TagAnnotationI();
        tagSet.setTextValue(rstring("tagSet"));
        tagSet.setNs(rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
        TagAnnotation tagSetReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tagSet);
        // create a tag and link it to the tag set
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag"));
        TagAnnotation tagReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
        link.setChild(tagReturned);
        link.setParent(tagSetReturned);
        // save the link.
        iUpdate.saveAndReturnObject(link);

        ParametersI param = new ParametersI();
        param.exp(rlong(self));
        param.noOrphan(); // no tag loaded

        List<IObject> result = iMetadata.loadTagSets(param);
        assertNotNull(result);
        Iterator<IObject> i = result.iterator();
        TagAnnotationData data;
        int count = 0;
        String ns;
        while (i.hasNext()) {
            data = new TagAnnotationData((TagAnnotation) i.next());
            ns = data.getNameSpace();
            if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                count++;
            }
        }
        assertEquals(result.size(), count);
    }

    /**
     * Tests the retrieval of tag sets
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadTagSetsAndOrphan() throws Exception {
        long self = iAdmin.getEventContext().userId;

        // Create a tag set.
        TagAnnotation tagSet = new TagAnnotationI();
        tagSet.setTextValue(rstring("tagSet"));
        tagSet.setNs(rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
        TagAnnotation tagSetReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tagSet);
        // create a tag and link it to the tag set
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag"));
        TagAnnotation tagReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
        link.setChild(tagReturned);
        link.setParent(tagSetReturned);

        tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag2"));
        TagAnnotation orphaned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);

        // save the link.
        iUpdate.saveAndReturnObject(link);
        List<Long> tagsIds = new ArrayList<Long>();
        tagsIds.add(orphaned.getId().getValue());

        ParametersI param = new ParametersI();
        param.exp(rlong(self));
        param.orphan(); // no tag loaded

        List<IObject> result = iMetadata.loadTagSets(param);
        assertNotNull(result);
        Iterator<IObject> i = result.iterator();
        TagAnnotationData data;
        int count = 0;
        int orphan = 0;
        String ns;
        while (i.hasNext()) {
            data = new TagAnnotationData((TagAnnotation) i.next());
            ns = data.getNameSpace();
            if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                count++;
            } else {
                if (tagsIds.contains(data.getId()))
                    orphan++;
            }
        }
        assertEquals(orphan, tagsIds.size());
        assertTrue(count > 0);
    }

    /**
     * Tests the retrieval of tag sets. The tag set has a tag and a comment.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadTagSets() throws Exception {
        long self = iAdmin.getEventContext().userId;

        // Create a tag set.
        TagAnnotation tagSet = new TagAnnotationI();
        tagSet.setTextValue(rstring("tagSet"));
        tagSet.setNs(rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
        TagAnnotation tagSetReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tagSet);
        // create a tag and link it to the tag set
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag"));
        TagAnnotation tagReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
        link.setChild(tagReturned);
        link.setParent(tagSetReturned);
        // save the link.
        iUpdate.saveAndReturnObject(link);

        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(rstring("comment"));
        comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
        link = new AnnotationAnnotationLinkI();
        link.setChild(comment);
        link.setParent(tagSetReturned);
        iUpdate.saveAndReturnObject(link);

        ParametersI param = new ParametersI();
        param.exp(rlong(self));
        param.orphan(); // no tag loaded

        List<IObject> result = iMetadata.loadTagSets(param);
        assertNotNull(result);
        Iterator<IObject> i = result.iterator();
        TagAnnotationData data;
        int count = 0;
        int orphan = 0;
        String ns;
        while (i.hasNext()) {
            tag = (TagAnnotation) i.next();
            data = new TagAnnotationData(tag);
            ns = data.getNameSpace();
            if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                if (data.getId() == tagSetReturned.getId().getValue()) {
                    assertEquals(tag.sizeOfAnnotationLinks(), 1);
                    assertEquals(data.getTags().size(), 1);
                    List l = tag.linkedAnnotationList();

                    assertEquals(l.size(), 1);
                    TagAnnotationData child = (TagAnnotationData) l.get(0);
                    assertEquals(child.getId(), tagReturned.getId().getValue());
                }
            }
        }
    }

    /**
     * Tests the retrieval of annotations used but not owned.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadAnnotationsUsedNotOwned() throws Exception {
        //
        IAdminPrx svc = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        Experimenter e1 = new ExperimenterI();
        e1.setOmeName(rstring(uuid));
        e1.setFirstName(rstring("integration"));
        e1.setLastName(rstring("tester"));
        e1.setLdap(rbool(false));
        Experimenter e2 = new ExperimenterI();
        e2.setOmeName(rstring(uuid2));
        e2.setFirstName(rstring("integration"));
        e2.setLastName(rstring("tester"));
        e2.setLdap(rbool(false));

        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(uuid));
        g.setLdap(rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rwrw--"));
        g = svc.getGroup(svc.createGroup(g));
        long id1 = newUserInGroupWithPassword(e1, g, uuid);
        long id2 = newUserInGroupWithPassword(e2, g, uuid2);
        svc.setDefaultGroup(svc.getExperimenter(id1), g);
        svc.setDefaultGroup(svc.getExperimenter(id2), g);
        client = new omero.client();
        ServiceFactoryPrx f = client.createSession(uuid2, uuid2);
        // Create a tag annotation as another user.
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag1"));
        IObject tagData = f.getUpdateService().saveAndReturnObject(tag);
        assertNotNull(tagData);
        // make sure we are not the owner of the tag.
        assertEquals(tagData.getDetails().getOwner().getId().getValue(), id2);
        client.closeSession();

        f = client.createSession(uuid, uuid);
        // Create an image.
        Image img = (Image) f.getUpdateService().saveAndReturnObject(
                mmFactory.simpleImage());
        // Link the tag and the image.
        ImageAnnotationLinkI link = new ImageAnnotationLinkI();
        link.setChild((Annotation) tagData);
        link.setParent(img);
        // Save the link
        f.getUpdateService().saveAndReturnObject(link);

        List<IObject> result = f
                .getMetadataService()
                .loadAnnotationsUsedNotOwned(TagAnnotation.class.getName(), id1);
        assertTrue(result.size() > 0);
        Iterator<IObject> i = result.iterator();
        IObject o;
        int count = 0;
        boolean found = false;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof Annotation) { // make sure only retrieve
                                           // annotations
                count++;
                if (o.getId().getValue() == tagData.getId().getValue())
                    found = true;
            }
        }
        assertTrue(found);
        assertEquals(result.size(), count);
        client.closeSession();
    }

    /**
     * Tests the retrieval of object linked to a given tag.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadTagContent() throws Exception {
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag1"));
        Annotation tagData = (Annotation) iUpdate.saveAndReturnObject(tag);
        Image img = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        // Link the tag and the image.
        ImageAnnotationLinkI link = new ImageAnnotationLinkI();
        link.setChild((Annotation) tagData.proxy());
        link.setParent(img);
        iUpdate.saveAndReturnObject(link);

        Project pData = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        ProjectAnnotationLinkI lp = new ProjectAnnotationLinkI();
        lp.setChild((Annotation) tagData.proxy());
        lp.setParent(pData);
        iUpdate.saveAndReturnObject(lp);

        Dataset dData = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        DatasetAnnotationLinkI dp = new DatasetAnnotationLinkI();
        dp.setChild((Annotation) tagData.proxy());
        dp.setParent(dData);
        iUpdate.saveAndReturnObject(dp);

        long self = iAdmin.getEventContext().userId;
        ParametersI param = new ParametersI();
        param.exp(rlong(self));
        Map result = iMetadata.loadTagContent(
                Arrays.asList(tagData.getId().getValue()), param);
        assertNotNull(result);
        List nodes = (List) result.get(tagData.getId().getValue());
        assertNotNull(nodes);
        Iterator<IObject> i = nodes.iterator();
        IObject o;
        int count = 0;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof Image) {
                if (o.getId().getValue() == img.getId().getValue())
                    count++;
            } else if (o instanceof Dataset) {
                if (o.getId().getValue() == dData.getId().getValue())
                    count++;
            } else if (o instanceof Project) {
                if (o.getId().getValue() == pData.getId().getValue())
                    count++;
            }
        }
        assertEquals(nodes.size(), count);
    }

    /**
     * Tests the retrieval of an instrument light sources of different types.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadInstrument() throws Exception {
        Instrument instrument;
        List<Detector> detectors;
        List<Filter> filters;
        List<FilterSet> filterSets;
        List<Objective> objectives;
        List<LightSource> lights;
        List<OTF> otfs;
        Detector detector;
        Filter filter;
        FilterSet fs;
        Objective objective;
        OTF otf;
        LightSource light;
        Laser laser;
        Iterator j;
        InstrumentData data;
        for (int i = 0; i < ModelMockFactory.LIGHT_SOURCES.length; i++) {
            instrument = mmFactory
                    .createInstrument(ModelMockFactory.LIGHT_SOURCES[i]);
            instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
            data = new InstrumentData(instrument);
            instrument = iMetadata
                    .loadInstrument(instrument.getId().getValue());
            data = new InstrumentData(instrument);
            assertTrue(instrument.sizeOfDetector() > 0);
            assertTrue(instrument.sizeOfDichroic() > 0);
            assertTrue(instrument.sizeOfFilter() > 0);
            assertTrue(instrument.sizeOfFilterSet() > 0);
            assertEquals(instrument.sizeOfLightSource(), 1);
            assertTrue(instrument.sizeOfObjective() > 0);
            assertTrue(instrument.sizeOfOtf() > 0);

            assertEquals(instrument.sizeOfDetector(), data.getDetectors()
                    .size());
            assertEquals(instrument.sizeOfDichroic(), data.getDichroics()
                    .size());
            assertEquals(instrument.sizeOfFilter(), data.getFilters().size());
            assertEquals(instrument.sizeOfFilterSet(), data.getFilterSets()
                    .size());
            assertEquals(instrument.sizeOfLightSource(), data.getLightSources()
                    .size());
            assertEquals(instrument.sizeOfObjective(), data.getObjectives()
                    .size());
            assertEquals(instrument.sizeOfOtf(), data.getOTF().size());

            detectors = instrument.copyDetector();
            j = detectors.iterator();
            while (j.hasNext()) {
                detector = (Detector) j.next();
                assertNotNull(detector.getType());
            }
            filters = instrument.copyFilter();
            j = filters.iterator();
            while (j.hasNext()) {
                filter = (Filter) j.next();
                assertNotNull(filter.getType());
                assertNotNull(filter.getTransmittanceRange());
            }
            filterSets = instrument.copyFilterSet();
            j = filterSets.iterator();
            while (j.hasNext()) {
                fs = (FilterSet) j.next();
                // assertNotNull(fs.getDichroic());
            }
            objectives = instrument.copyObjective();
            j = objectives.iterator();
            while (j.hasNext()) {
                objective = (Objective) j.next();
                assertNotNull(objective.getCorrection());
                assertNotNull(objective.getImmersion());
            }
            otfs = instrument.copyOtf();
            j = otfs.iterator();
            while (j.hasNext()) {
                otf = (OTF) j.next();
                objective = otf.getObjective();
                assertNotNull(otf.getPixelsType());
                assertNotNull(otf.getFilterSet());
                assertNotNull(objective);
                assertNotNull(objective.getCorrection());
                assertNotNull(objective.getImmersion());
            }
            lights = instrument.copyLightSource();
            j = lights.iterator();
            while (j.hasNext()) {
                light = (LightSource) j.next();
                if (light instanceof Laser) {
                    laser = (Laser) light;
                    assertNotNull(laser.getType());
                    assertNotNull(laser.getLaserMedium());
                    assertNotNull(laser.getPulse());
                } else if (light instanceof Filament) {
                    assertNotNull(((Filament) light).getType());
                } else if (light instanceof Arc) {
                    assertNotNull(((Arc) light).getType());
                }
            }
        }
    }

    /**
     * Tests the retrieval of an instrument light sources of different types.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadInstrumentWithMultipleLightSources() throws Exception {
        Instrument instrument = mmFactory
                .createInstrument(ModelMockFactory.LASER);
        instrument.addLightSource(mmFactory.createFilament());
        instrument.addLightSource(mmFactory.createArc());
        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
        instrument = iMetadata.loadInstrument(instrument.getId().getValue());
        assertNotNull(instrument);
        List<LightSource> lights = instrument.copyLightSource();
        assertEquals(3, lights.size());
        Iterator<LightSource> i = lights.iterator();
        LightSource src;
        Laser laser;
        while (i.hasNext()) {
            src = i.next();
            if (src instanceof Laser) {
                laser = (Laser) src;
                assertNotNull(laser.getType());
                assertNotNull(laser.getLaserMedium());
                assertNotNull(laser.getPulse());
            } else if (src instanceof Filament) {
                assertNotNull(((Filament) src).getType());
            } else if (src instanceof Arc) {
                assertNotNull(((Arc) src).getType());
            }
        }

    }

    /**
     * Tests the retrieval of channel acquisition data. One using an instrument
     * with one laser, the second time with a laser with a pump
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testLoadChannelAcquisitionData() throws Exception {
        // create an instrument.
        Boolean[] values = new Boolean[2];
        values[0] = Boolean.valueOf(false);
        values[1] = Boolean.valueOf(true);
        for (int k = 0; k < values.length; k++) {
            Image img = mmFactory.createImage();
            img = (Image) iUpdate.saveAndReturnObject(img);
            Pixels pixels = img.getPrimaryPixels();
            long pixId = pixels.getId().getValue();
            // method already tested in PixelsServiceTest
            // make sure objects are loaded.
            pixels = factory.getPixelsService().retrievePixDescription(pixId);
            String pump = null;
            if (values[k])
                pump = ModelMockFactory.FILAMENT;
            Instrument instrument = mmFactory.createInstrument(
                    ModelMockFactory.LASER, pump);

            instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
            assertNotNull(instrument);
            // retrieve the detector.
            ParametersI param = new ParametersI();
            param.addLong("iid", instrument.getId().getValue());
            String sql = "select d from Detector as d where d.instrument.id = :iid";
            Detector detector = (Detector) iQuery.findByQuery(sql, param);
            sql = "select d from FilterSet as d where d.instrument.id = :iid";
            FilterSet filterSet = (FilterSet) iQuery.findByQuery(sql, param);
            sql = "select d from Laser as d where d.instrument.id = :iid";
            List<IObject> lasers = iQuery.findAllByQuery(sql, param);
            Laser laser = (Laser) lasers.get(0);

            sql = "select d from Dichroic as d where d.instrument.id = :iid";
            Dichroic dichroic = (Dichroic) iQuery.findByQuery(sql, param);
            sql = "select d from Objective as d where d.instrument.id = :iid";
            Objective objective = (Objective) iQuery.findByQuery(sql, param);

            sql = "select d from OTF as d where d.instrument.id = :iid";
            OTF otf = (OTF) iQuery.findByQuery(sql, param);
            assertNotNull(otf);
            LogicalChannel lc;
            Channel channel;
            ContrastMethod cm;
            Illumination illumination;
            AcquisitionMode mode;
            List<IObject> types = factory.getPixelsService()
                    .getAllEnumerations(ContrastMethod.class.getName());
            cm = (ContrastMethod) types.get(0);

            types = factory.getPixelsService().getAllEnumerations(
                    Illumination.class.getName());
            illumination = (Illumination) types.get(0);
            types = factory.getPixelsService().getAllEnumerations(
                    AcquisitionMode.class.getName());
            mode = (AcquisitionMode) types.get(0);

            List<Long> ids = new ArrayList<Long>();
            for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
                channel = pixels.getChannel(i);
                lc = channel.getLogicalChannel();
                lc.setContrastMethod(cm);
                lc.setIllumination(illumination);
                lc.setMode(mode);
                lc.setOtf(otf);
                lc.setDetectorSettings(mmFactory
                        .createDetectorSettings(detector));
                lc.setFilterSet(filterSet);
                lc.setLightSourceSettings(mmFactory.createLightSettings(laser));
                lc.setLightPath(mmFactory.createLightPath(null, dichroic, null));
                lc = (LogicalChannel) iUpdate.saveAndReturnObject(lc);
                assertNotNull(lc);
                ids.add(lc.getId().getValue());
            }
            List<LogicalChannel> channels = iMetadata
                    .loadChannelAcquisitionData(ids);
            assertEquals(channels.size(), pixels.getSizeC().getValue());
            LogicalChannel loaded;
            Iterator<LogicalChannel> j = channels.iterator();
            LightSourceData l;
            while (j.hasNext()) {
                loaded = j.next();
                assertNotNull(loaded);
                ChannelAcquisitionData data = new ChannelAcquisitionData(loaded);
                assertEquals(data.getDetector().getId(), detector.getId()
                        .getValue());
                assertEquals(data.getFilterSet().getId(), filterSet.getId()
                        .getValue());
                l = (LightSourceData) data.getLightSource();
                assertEquals(l.getId(), laser.getId().getValue());
                assertNotNull(l.getLaserMedium());
                assertNotNull(l.getType());
                if (values[k]) {
                    assertNotNull(((Laser) l.asIObject()).getPump());
                }
                assertNotNull(loaded.getDetectorSettings());
                assertNotNull(loaded.getLightSourceSettings());
                assertNotNull(loaded.getDetectorSettings().getBinning());
                assertNotNull(loaded.getDetectorSettings().getDetector());
                assertNotNull(loaded.getDetectorSettings().getDetector()
                        .getType());
                assertNotNull(loaded.getLightPath());
                assertEquals(data.getLightPath().getDichroic().getId(),
                        dichroic.getId().getValue());
                assertNotNull(data.getContrastMethod());
                assertNotNull(data.getIllumination());
                assertNotNull(data.getMode());
                // OTF support

                assertEquals(data.getOTF().getId(), otf.getId().getValue());
                assertNotNull(loaded.getOtf());
                assertEquals(loaded.getOtf().getId().getValue(), otf.getId()
                        .getValue());
                assertNotNull(loaded.getOtf().getFilterSet());
                assertNotNull(loaded.getOtf().getObjective());
                assertEquals(loaded.getOtf().getFilterSet().getId().getValue(),
                        filterSet.getId().getValue());
                assertEquals(loaded.getOtf().getObjective().getId().getValue(),
                        objective.getId().getValue());
                assertNotNull(loaded.getOtf().getPixelsType());
            }
        }
    }

    /**
     * Tests the retrieval of tag sets. One with a tag, one without.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadEmptyTagSets() throws Exception {
        long self = iAdmin.getEventContext().userId;

        // Create a tag set.
        TagAnnotation tagSet = new TagAnnotationI();
        tagSet.setTextValue(rstring("tagSet"));
        tagSet.setNs(rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
        TagAnnotation tagSetReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tagSet);
        // create a tag and link it to the tag set
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag"));
        TagAnnotation tagReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
        link.setChild(tagReturned);
        link.setParent(tagSetReturned);
        // save the link.
        iUpdate.saveAndReturnObject(link);

        tagSet = new TagAnnotationI();
        tagSet.setTextValue(rstring("tagSet"));
        tagSet.setNs(rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
        TagAnnotation tagSetReturned_2 = (TagAnnotation) iUpdate
                .saveAndReturnObject(tagSet);

        ParametersI param = new ParametersI();
        param.exp(rlong(self));
        param.orphan(); // no tag loaded

        List<IObject> result = iMetadata.loadTagSets(param);
        assertNotNull(result);
        Iterator<IObject> i = result.iterator();
        TagAnnotationData data;
        String ns;
        int count = 0;
        while (i.hasNext()) {
            tag = (TagAnnotation) i.next();
            data = new TagAnnotationData(tag);
            ns = data.getNameSpace();
            if (ns != null && TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                if (data.getId() == tagSetReturned.getId().getValue()
                        || data.getId() == tagSetReturned_2.getId().getValue())
                    count++;
            }
        }
        assertEquals(count, 2);
    }

    /**
     * Tests the retrieval of tag sets with tags with a null ns and other with
     * not null ns. The ns is not the tagset namespace
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadTagsNamepaceNullAndNotNull() throws Exception {
        long self = iAdmin.getEventContext().userId;

        // Create a tag set.
        TagAnnotation tagSet = new TagAnnotationI();
        tagSet.setTextValue(rstring("tagSet"));
        tagSet.setNs(rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
        TagAnnotation tagSetReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tagSet);
        // create a tag and link it to the tag set
        TagAnnotation tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag"));
        TagAnnotation tagReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
        link.setChild(tagReturned);
        link.setParent(tagSetReturned);

        // save the link.
        iUpdate.saveAndReturnObject(link);

        List<Long> tagsIds = new ArrayList<Long>();

        tag = new TagAnnotationI();
        tag.setTextValue(rstring("tag2"));
        TagAnnotation orphaned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        tagsIds.add(orphaned.getId().getValue());

        tag = new TagAnnotationI();
        tag.setNs(rstring(""));
        tag.setTextValue(rstring("tag2"));
        orphaned = (TagAnnotation) iUpdate.saveAndReturnObject(tag);
        tagsIds.add(orphaned.getId().getValue());

        ParametersI param = new ParametersI();
        param.exp(rlong(self));
        param.orphan(); // no tag loaded

        List<IObject> result = iMetadata.loadTagSets(param);
        assertNotNull(result);
        Iterator<IObject> i = result.iterator();
        TagAnnotationData data;
        int count = 0;
        int orphan = 0;
        String ns;
        while (i.hasNext()) {
            data = new TagAnnotationData((TagAnnotation) i.next());
            ns = data.getNameSpace();
            if (ns != null) {
                if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                    if (tagSetReturned.getId().getValue() == data.getId())
                        count++;
                }
            }
            if (tagsIds.contains(data.getId()))
                orphan++;
        }
        assertEquals(orphan, tagsIds.size());
        assertEquals(count, 1);
    }

    /**
     * Tests the creation of file annotation with an original file and load it.
     * Loads the annotation using the <code>loadSpecifiedAnnotations</code>
     * method. Converts the file annotation into its corresponding Pojo Object
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsFileAnnotationConvertToPojo()
            throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        assertNotNull(of);

        FileAnnotationI fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        assertNotNull(data);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
                FileAnnotation.class.getName(), include, exclude, param);
        assertNotNull(result);

        Iterator<Annotation> i = result.iterator();
        Annotation o;
        int count = 0;
        FileAnnotationData pojo;
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileAnnotation) {
                pojo = new FileAnnotationData((FileAnnotation) o);
                count++;
                if (pojo.getId() == data.getId().getValue()) {
                    assertEquals(pojo.getFileID(), of.getId().getValue());
                    assertEquals(pojo.getFileName(), of.getName().getValue());
                    assertEquals(pojo.getFilePath(), of.getPath().getValue());
                }
            }
        }
        assertTrue(count > 0);
        assertEquals(count, result.size());
    }

    /**
     * Tests the retrieval of annotations with and without namespaces. Exclude
     * the annotation with a given name space.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationsFileAnnotationNS() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        assertNotNull(of);

        String ns = "include";
        FileAnnotationI fa = new FileAnnotationI();
        fa.setFile(of);
        fa.setNs(rstring(ns));
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        assertNotNull(data);

        fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data2 = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        assertNotNull(data2);

        Parameters param = new Parameters();

        // First test the include condition
        List<Annotation> result = iMetadata.loadSpecifiedAnnotations(
                FileAnnotation.class.getName(), new ArrayList<String>(),
                Arrays.asList(ns), param);
        assertNotNull(result);

        Iterator<Annotation> i = result.iterator();
        Annotation o;
        FileAnnotation r;
        FileAnnotationData pojo;
        while (i.hasNext()) {
            o = i.next();
            pojo = new FileAnnotationData((FileAnnotation) o);
            if (data2.getId().getValue() == pojo.getId()) {
                assertEquals(pojo.getFileName(), of.getName().getValue());
                assertEquals(pojo.getFilePath(), of.getPath().getValue());
            }
        }
    }

    /**
     * Tests the retrieval of a specified long annotation linked to images.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToImages() throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());

        LongAnnotation data1 = new LongAnnotationI();
        data1.setLongValue(rlong(1L));
        data1 = (LongAnnotation) iUpdate.saveAndReturnObject(data1);
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) img1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        LongAnnotation data2 = new LongAnnotationI();
        data2.setLongValue(rlong(1L));
        data2 = (LongAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ImageAnnotationLinkI();
        l.setParent((Image) img2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);

        // Add a comment annotation
        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(rstring("comment"));
        comment = (CommentAnnotation) iUpdate.saveAndReturnObject(comment);
        l = new ImageAnnotationLinkI();
        l.setParent((Image) img2.proxy());
        l.setChild((Annotation) comment.proxy());
        iUpdate.saveAndReturnObject(l);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(LongAnnotation.class
                        .getName(), include, exclude, Image.class.getName(),
                        Arrays.asList(img1.getId().getValue(), img2.getId()
                                .getValue()), param);

        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(img1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());
        result = map.get(img2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Tests the retrieval of a specified comment annotation linked to datasets.
     * All Types covered by other tests.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToDatasets() throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        Dataset d1 = new DatasetI();
        d1.setName(rstring(name));
        d1 = (Dataset) iUpdate.saveAndReturnObject(d1);

        Dataset d2 = new DatasetI();
        d2.setName(rstring(name));
        d2 = (Dataset) iUpdate.saveAndReturnObject(d2);

        CommentAnnotation data1 = new CommentAnnotationI();
        data1.setTextValue(rstring("1"));
        data1 = (CommentAnnotation) iUpdate.saveAndReturnObject(data1);
        DatasetAnnotationLink l = new DatasetAnnotationLinkI();
        l.setParent((Dataset) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        CommentAnnotation data2 = new CommentAnnotationI();
        data2.setTextValue(rstring("1"));
        data2 = (CommentAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new DatasetAnnotationLinkI();
        l.setParent((Dataset) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);

        LongAnnotation c = new LongAnnotationI();
        c.setLongValue(rlong(1L));
        c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
        l = new DatasetAnnotationLinkI();
        l.setParent((Dataset) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(CommentAnnotation.class
                        .getName(), include, exclude, Dataset.class.getName(),
                        Arrays.asList(d1.getId().getValue(), d2.getId()
                                .getValue()), param);

        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Tests the retrieval of a specified term annotation linked to projects.
     * All Types covered by other tests.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToProjects() throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        Project d1 = new ProjectI();
        d1.setName(rstring(name));
        d1 = (Project) iUpdate.saveAndReturnObject(d1);

        Project d2 = new ProjectI();
        d2.setName(rstring(name));
        d2 = (Project) iUpdate.saveAndReturnObject(d2);

        TermAnnotation data1 = new TermAnnotationI();
        data1.setTermValue(rstring("Term 1"));
        data1 = (TermAnnotation) iUpdate.saveAndReturnObject(data1);
        ProjectAnnotationLink l = new ProjectAnnotationLinkI();
        l.setParent((Project) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        TermAnnotation data2 = new TermAnnotationI();
        data2.setTermValue(rstring("Term 1"));
        data2 = (TermAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ProjectAnnotationLinkI();
        l.setParent((Project) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);

        LongAnnotation c = new LongAnnotationI();
        c.setLongValue(rlong(1L));
        c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
        l = new ProjectAnnotationLinkI();
        l.setParent((Project) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(TermAnnotation.class
                        .getName(), include, exclude, Project.class.getName(),
                        Arrays.asList(d1.getId().getValue(), d2.getId()
                                .getValue()), param);

        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Tests the retrieval of a specified tag annotation linked to screen. All
     * Types covered by other tests.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToScreens() throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        Screen d1 = new ScreenI();
        d1.setName(rstring(name));
        d1 = (Screen) iUpdate.saveAndReturnObject(d1);

        Screen d2 = new ScreenI();
        d2.setName(rstring(name));
        d2 = (Screen) iUpdate.saveAndReturnObject(d2);

        TagAnnotation data1 = new TagAnnotationI();
        data1.setTextValue(rstring("Tag 1"));
        data1 = (TagAnnotation) iUpdate.saveAndReturnObject(data1);
        ScreenAnnotationLink l = new ScreenAnnotationLinkI();
        l.setParent((Screen) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        TagAnnotation data2 = new TagAnnotationI();
        data2.setTextValue(rstring("Tag 1"));
        data2 = (TagAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ScreenAnnotationLinkI();
        l.setParent((Screen) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);

        LongAnnotation c = new LongAnnotationI();
        c.setLongValue(rlong(1L));
        c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
        l = new ScreenAnnotationLinkI();
        l.setParent((Screen) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(
                        TagAnnotation.class.getName(), include, exclude,
                        Screen.class.getName(), Arrays.asList(d1.getId()
                                .getValue(), d2.getId().getValue()), param);

        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Tests the retrieval of a specified boolean annotation linked to plates.
     * All Types covered by other tests.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToPlates() throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        Plate d1 = new PlateI();
        d1.setName(rstring(name));
        d1 = (Plate) iUpdate.saveAndReturnObject(d1);

        Plate d2 = new PlateI();
        d2.setName(rstring(name));
        d2 = (Plate) iUpdate.saveAndReturnObject(d2);

        BooleanAnnotation data1 = new BooleanAnnotationI();
        data1.setBoolValue(rbool(true));
        data1 = (BooleanAnnotation) iUpdate.saveAndReturnObject(data1);
        PlateAnnotationLink l = new PlateAnnotationLinkI();
        l.setParent((Plate) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        BooleanAnnotation data2 = new BooleanAnnotationI();
        data1.setBoolValue(rbool(true));
        data2 = (BooleanAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new PlateAnnotationLinkI();
        l.setParent((Plate) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);

        LongAnnotation c = new LongAnnotationI();
        c.setLongValue(rlong(1L));
        c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
        l = new PlateAnnotationLinkI();
        l.setParent((Plate) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(BooleanAnnotation.class
                        .getName(), include, exclude, Plate.class.getName(),
                        Arrays.asList(d1.getId().getValue(), d2.getId()
                                .getValue()), param);

        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Tests the retrieval of a specified xml annotation linked to plates. All
     * Types covered by other tests.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToPlateAcquisitions()
            throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        PlateAcquisition d1 = new PlateAcquisitionI();
        d1.setName(rstring(name));
        Plate p1 = new PlateI();
        p1.setName(rstring(name));
        d1.setPlate(p1);
        d1 = (PlateAcquisition) iUpdate.saveAndReturnObject(d1);

        PlateAcquisition d2 = new PlateAcquisitionI();
        d2.setName(rstring(name));
        d2.setPlate(p1);
        d2 = (PlateAcquisition) iUpdate.saveAndReturnObject(d2);

        XmlAnnotation data1 = new XmlAnnotationI();
        data1.setTextValue(rstring("xml annotation"));
        data1 = (XmlAnnotation) iUpdate.saveAndReturnObject(data1);
        PlateAcquisitionAnnotationLink l = new PlateAcquisitionAnnotationLinkI();
        l.setParent((PlateAcquisition) d1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        XmlAnnotation data2 = new XmlAnnotationI();
        data1.setTextValue(rstring("xml annotation"));
        data2 = (XmlAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new PlateAcquisitionAnnotationLinkI();
        l.setParent((PlateAcquisition) d2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);

        LongAnnotation c = new LongAnnotationI();
        c.setLongValue(rlong(1L));
        c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
        l = new PlateAcquisitionAnnotationLinkI();
        l.setParent((PlateAcquisition) d2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(
                        XmlAnnotation.class.getName(), include, exclude,
                        PlateAcquisition.class.getName(), Arrays.asList(d1
                                .getId().getValue(), d2.getId().getValue()),
                        param);

        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(d1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());
        result = map.get(d2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Tests the retrieval of a specified file annotation linked to wells. All
     * Types covered by other tests.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToWells() throws Exception {
        Plate p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(2,
                1, 1, 1, false));

        ParametersI options = new ParametersI();
        options.addLong("plateID", p.getId().getValue());
        StringBuilder sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("left outer join fetch well.wellSamples as ws ");
        sb.append("left outer join fetch ws.image as img ");
        sb.append("where pt.id = :plateID");
        List results = iQuery.findAllByQuery(sb.toString(), options);

        Well w1 = (Well) results.get(0);
        Well w2 = (Well) results.get(1);

        FileAnnotation data1 = new FileAnnotationI();
        data1 = (FileAnnotation) iUpdate.saveAndReturnObject(data1);
        WellAnnotationLink l = new WellAnnotationLinkI();
        l.setParent((Well) w1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        FileAnnotation data2 = new FileAnnotationI();
        data2 = (FileAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new WellAnnotationLinkI();
        l.setParent((Well) w2.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);

        LongAnnotation c = new LongAnnotationI();
        c.setLongValue(rlong(1L));
        c = (LongAnnotation) iUpdate.saveAndReturnObject(c);
        l = new WellAnnotationLinkI();
        l.setParent((Well) w2.proxy());
        l.setChild((Annotation) c.proxy());
        iUpdate.saveAndReturnObject(l);

        Parameters param = new Parameters();
        List<String> include = new ArrayList<String>();
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(FileAnnotation.class
                        .getName(), include, exclude, Well.class.getName(),
                        Arrays.asList(w1.getId().getValue(), w2.getId()
                                .getValue()), param);

        assertNotNull(map);

        assertEquals(map.size(), 2);
        List<Annotation> result = map.get(w1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());
        result = map.get(w2.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Tests the retrieval of specified xml annotations linked to an image. The
     * one annotation has its ns set to <code>modulo</code> ns the other one
     * does not.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLoadSpecifiedAnnotationLinkedToImageWithModuloNS()
            throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());

        XmlAnnotation data1 = new XmlAnnotationI();
        data1.setTextValue(rstring("with modulo ns"));
        data1.setNs(rstring(XMLAnnotationData.MODULO_NS));
        data1 = (XmlAnnotation) iUpdate.saveAndReturnObject(data1);
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) img1.proxy());
        l.setChild((Annotation) data1.proxy());
        iUpdate.saveAndReturnObject(l);

        XmlAnnotation data2 = new XmlAnnotationI();
        data2.setTextValue(rstring("w/o modulo ns"));
        data2 = (XmlAnnotation) iUpdate.saveAndReturnObject(data2);
        l = new ImageAnnotationLinkI();
        l.setParent((Image) img1.proxy());
        l.setChild((Annotation) data2.proxy());
        iUpdate.saveAndReturnObject(l);
        Parameters param = new Parameters();
        List<String> include = Arrays.asList(XMLAnnotationData.MODULO_NS);
        List<String> exclude = new ArrayList<String>();

        Map<Long, List<Annotation>> map = iMetadata
                .loadSpecifiedAnnotationsLinkedTo(
                        XmlAnnotation.class.getName(), include, exclude,
                        Image.class.getName(),
                        Arrays.asList(img1.getId().getValue()), param);

        assertNotNull(map);

        assertEquals(map.size(), 1);
        List<Annotation> result = map.get(img1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data1.getId().getValue());

        // now exclude ns
        include = new ArrayList<String>();
        exclude = Arrays.asList(XMLAnnotationData.MODULO_NS);

        map = iMetadata.loadSpecifiedAnnotationsLinkedTo(
                XmlAnnotation.class.getName(), include, exclude,
                Image.class.getName(), Arrays.asList(img1.getId().getValue()),
                param);

        assertNotNull(map);

        assertEquals(map.size(), 1);
        result = map.get(img1.getId().getValue());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId().getValue(), data2.getId().getValue());
    }

    /**
     * Creates a new finished upload job, without persisting it.
     * @return the new job
     * @throws ServerError unexpected
     */
    private UploadJob getNewUploadJob() throws ServerError {
        final Roles roles = iAdmin.getSecurityRoles();
        final UploadJob uploadJob = new UploadJobI();
        uploadJob.setUsername(rstring(roles.rootName));
        uploadJob.setGroupname(rstring(roles.systemGroupName));
        uploadJob.setSubmitted(rtime(System.currentTimeMillis()));
        uploadJob.setScheduledFor(rtime(System.currentTimeMillis()));
        uploadJob.setStarted(rtime(System.currentTimeMillis()));
        uploadJob.setFinished(rtime(System.currentTimeMillis()));
        uploadJob.setMessage(rstring(getClass().getSimpleName()));
        uploadJob.setStatus((JobStatus) factory.getTypesService().getEnumeration(JobStatus.class.getName(), JobHandle.FINISHED));
        uploadJob.setType(rstring("Test"));
        return uploadJob;
    }

    /**
     * Assert that the collection of IObjects is exactly original files of the given IDs.
     * @param objects the IObjects expected to be original files
     * @param expectedIdArray the expected original file IDs
     */
    private static void assertOriginalFileIds(Collection<IObject> objects, Long... expectedIdArray) {
        final Set<Long> expectedIds = Sets.newHashSet(expectedIdArray);
        for (final IObject object : objects) {
            assertTrue(expectedIds.remove(((OriginalFile) object).getId().getValue()));
        }
        assertTrue(expectedIds.isEmpty());
    }

    /**
     * Test that the correct import logs are retrieved for given fileset and image IDs.
     * @throws ServerError unexpected
     * @throws CannotCreateSessionException unexpected
     * @throws PermissionDeniedException unexpected
     */
    @Test
    public void testLoadImportLog() throws ServerError, CannotCreateSessionException, PermissionDeniedException
    {
        /* set up import records where image 1 is in fileset 1 and images 2 and 3 are in fileset 2 */

        final long currentGroupId = iAdmin.getEventContext().groupId;
        final Map<String, String> groupContext = ImmutableMap.of(Login.OMERO_GROUP, Long.toString(currentGroupId));
        final IUpdatePrx iUpdateRoot = (IUpdatePrx) root.getSession().getUpdateService().ice_context(groupContext);

        final IMetadataPrx iMetadata = (IMetadataPrx) root.getSession().getMetadataService().ice_context(groupContext);

        Job uploadJob1 = getNewUploadJob();
        uploadJob1 = (Job) iUpdateRoot.saveAndReturnObject(uploadJob1);
        final long uploadJob1Id = uploadJob1.getId().getValue();

        Job uploadJob2 = getNewUploadJob();
        uploadJob2 = (Job) iUpdateRoot.saveAndReturnObject(uploadJob2);
        final long uploadJob2Id = uploadJob2.getId().getValue();

        OriginalFile importLog1 = new OriginalFileI();
        importLog1.setMimetype(rstring("application/omero-log-file"));
        importLog1.setName(rstring("import log"));
        importLog1.setPath(rstring("import one"));
        importLog1 = (OriginalFile) iUpdate.saveAndReturnObject(importLog1);
        final long importLog1Id = importLog1.getId().getValue();

        OriginalFile importLog2 = new OriginalFileI();
        importLog2.setMimetype(rstring("application/omero-log-file"));
        importLog2.setName(rstring("import log"));
        importLog2.setPath(rstring("import two"));
        importLog2 = (OriginalFile) iUpdate.saveAndReturnObject(importLog2);
        final long importLog2Id = importLog2.getId().getValue();

        JobOriginalFileLink jobOriginalFileLink;

        jobOriginalFileLink = new JobOriginalFileLinkI();
        jobOriginalFileLink.setParent(uploadJob1);
        jobOriginalFileLink.setChild(importLog1);
        iUpdate.saveAndReturnObject(jobOriginalFileLink);

        jobOriginalFileLink = new JobOriginalFileLinkI();
        jobOriginalFileLink.setParent(uploadJob2);
        jobOriginalFileLink.setChild(importLog2);
        iUpdate.saveAndReturnObject(jobOriginalFileLink);

        uploadJob1 = (Job) iQuery.find(UploadJob.class.getName(), uploadJob1Id);
        uploadJob2 = (Job) iQuery.find(UploadJob.class.getName(), uploadJob2Id);

        Fileset fileset1 = newFileset();
        fileset1.linkJob(uploadJob1);
        fileset1 = (Fileset) iUpdate.saveAndReturnObject(fileset1);
        final long fileset1Id = fileset1.getId().getValue();

        Fileset fileset2 = newFileset();
        fileset2.linkJob(uploadJob2);
        fileset2 = (Fileset) iUpdate.saveAndReturnObject(fileset2);
        final long fileset2Id = fileset2.getId().getValue();

        Image image1 = new ImageI();
        image1.setName(rstring("image alpha from fileset one"));
        image1.setFileset(fileset1);
        image1 = (Image) iUpdate.saveAndReturnObject(image1);
        final long image1Id = image1.getId().getValue();

        Image image2 = new ImageI();
        image2.setName(rstring("image alpha from fileset two"));
        image2.setFileset(fileset2);
        image2 = (Image) iUpdate.saveAndReturnObject(image2);
        final long image2Id = image2.getId().getValue();

        Image image3 = new ImageI();
        image3.setName(rstring("image beta from fileset two"));
        image3.setFileset(fileset2);
        image3 = (Image) iUpdate.saveAndReturnObject(image3);
        final long image3Id = image3.getId().getValue();

        Map<Long, List<IObject>> logFiles;

        /* test import log retrieval by fileset ID */

        logFiles = iMetadata.loadLogFiles(Fileset.class.getName(), ImmutableList.<Long>of());
        assertTrue(logFiles.isEmpty());

        logFiles = iMetadata.loadLogFiles(Fileset.class.getName(), ImmutableList.of(fileset1Id));
        assertTrue(logFiles.size() == 1);
        assertOriginalFileIds(logFiles.get(fileset1Id), importLog1Id);

        logFiles = iMetadata.loadLogFiles(Fileset.class.getName(), ImmutableList.of(fileset2Id));
        assertTrue(logFiles.size() == 1);
        assertOriginalFileIds(logFiles.get(fileset2Id), importLog2Id);

        logFiles = iMetadata.loadLogFiles(Fileset.class.getName(), ImmutableList.of(fileset1Id, fileset2Id));
        assertTrue(logFiles.size() == 2);
        assertOriginalFileIds(logFiles.get(fileset1Id), importLog1Id);
        assertOriginalFileIds(logFiles.get(fileset2Id), importLog2Id);

        /* test import log retrieval by image ID */

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.<Long>of());
        assertTrue(logFiles.isEmpty());

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.of(image1Id));
        assertTrue(logFiles.size() == 1);
        assertOriginalFileIds(logFiles.get(image1Id), importLog1Id);

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.of(image2Id));
        assertTrue(logFiles.size() == 1);
        assertOriginalFileIds(logFiles.get(image2Id), importLog2Id);

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.of(image3Id));
        assertTrue(logFiles.size() == 1);
        assertOriginalFileIds(logFiles.get(image3Id), importLog2Id);

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.of(image1Id, image2Id));
        assertTrue(logFiles.size() == 2);
        assertOriginalFileIds(logFiles.get(image1Id), importLog1Id);
        assertOriginalFileIds(logFiles.get(image2Id), importLog2Id);

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.of(image1Id, image3Id));
        assertTrue(logFiles.size() == 2);
        assertOriginalFileIds(logFiles.get(image1Id), importLog1Id);
        assertOriginalFileIds(logFiles.get(image3Id), importLog2Id);

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.of(image2Id, image3Id));
        assertTrue(logFiles.size() == 2);
        assertOriginalFileIds(logFiles.get(image2Id), importLog2Id);
        assertOriginalFileIds(logFiles.get(image3Id), importLog2Id);

        logFiles = iMetadata.loadLogFiles(Image.class.getName(), ImmutableList.of(image1Id, image2Id, image3Id));
        assertTrue(logFiles.size() == 3);
        assertOriginalFileIds(logFiles.get(image1Id), importLog1Id);
        assertOriginalFileIds(logFiles.get(image2Id), importLog2Id);
        assertOriginalFileIds(logFiles.get(image3Id), importLog2Id);
    }

    /**
     * Test that import logs cannot be retrieved for unexpected root node types.
     * @throws ServerError because of the given unexpected root node type
     */
    @Test(expectedExceptions = ApiUsageException.class)
    public void testLoadImportLogWrongRootType() throws ServerError {
        iMetadata.loadLogFiles(WellSample.class.getName(), ImmutableList.<Long>of());
    }
}
