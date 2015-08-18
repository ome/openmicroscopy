/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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

package integration.chgrp;

import integration.AbstractServerTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;
import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
import ome.xml.model.OME;
import omero.RString;
import omero.api.IContainerPrx;
import omero.api.IMetadataPrx;
import omero.api.IUpdatePrx;
import omero.cmd.Chgrp2;
import omero.cmd.Delete2;
import omero.cmd.OK;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.gateway.util.Requests;
import omero.model.Arc;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Detector;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Filament;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.Pixels;
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import omero.util.TempFileManager;
import pojos.ChannelAcquisitionData;
import pojos.ImageData;
import pojos.InstrumentData;
import pojos.LightSourceData;

/**
 */
public class MultiImageFilesetMoveTest extends AbstractServerTest {

    ExperimenterGroup secondGroup;
    
    private IMetadataPrx iMetadata;

    @BeforeClass
    public void setupSecondGroup() throws Exception {
        EventContext ec = iAdmin.getEventContext();
        secondGroup = newGroupAddUser("rwrw--", ec.userId);
        iAdmin.getEventContext(); // Refresh.
    }

    static class Fixture {
        final List<Dataset> datasets;
        final List<Image> images;
        Fixture(List<Dataset> datasets, List<Image> images) {
            this.datasets = datasets;
            this.images = images;
        }
        DatasetImageLink link(IUpdatePrx iUpdate, int datasetIndex, int imageIndex) throws Exception {
            DatasetImageLink link = new DatasetImageLinkI();
            link.setParent((Dataset) datasets.get(datasetIndex).proxy());
            link.setChild((Image) images.get(imageIndex).proxy());
            link = (DatasetImageLink) iUpdate.saveAndReturnObject(link);
            return link;
        }
    }

    protected List<Image> importMIF(int seriesCount) throws Throwable {
        File fake = TempFileManager.create_path("importMIF",
                String.format("&series=%d.fake", seriesCount));
        List<Pixels> pixels = importFile(importer, fake, null, false, null);
        assertEquals(seriesCount, pixels.size());
        List<Image> images = new ArrayList<Image>();
        for (Pixels pixel : pixels) {
            images.add(pixel.getImage());
        }
        return images;
    }

    /**
     * Creates a list of the given number of {@link Dataset} instances with
     * names of the form "name [1]", "name [2]", etc. and
     * returns them in a list.
     * 
     * @param count
     * @param baseName
     * @return
     * @throws Throwable
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<Dataset> createDatasets(int count, String baseName)
            throws Throwable {

        List<IObject> rv = new ArrayList<IObject>();
        for (int i = 0; i < count; i++) {
            Dataset dataset = new DatasetI();
            String suffix = " [" + (i + 1) + "]";
            RString name = omero.rtypes.rstring(baseName + suffix);
            dataset.setName(name);
            rv.add(dataset);
        }
        return (List) iUpdate.saveAndReturnArray(rv);
    }


    protected Fixture createFixture(int datasetCount, int imageCount) throws Throwable {
        List<Dataset> datasets = createDatasets(datasetCount, "MIF");
        List<Image> images = importMIF(imageCount);
        return new Fixture(datasets, images);
    }

    /**
     * Simplest example of the MIF chgrp edge case: a single fileset containing
     * 2 images is split among 2 datasets. Each sibling CANNOT be moved
     * independently of the other.
     */
    @Test(groups = {"fs", "integration"})
    public void testBasicProblem() throws Throwable {
        Fixture f = createFixture(2, 2);
        f.link(iUpdate, 0, 0);
        f.link(iUpdate, 1, 1);

        long img0 = f.images.get(0).getId().getValue();
        long img1 = f.images.get(1).getId().getValue();
        long fs0 = f.images.get(0).getFileset().getId().getValue();
        long fs1 = f.images.get(1).getFileset().getId().getValue();
        assertEquals(fs0, fs1);

        final Chgrp2 mv = Requests.chgrp("Image", img0, secondGroup.getId().getValue());

        Response rsp = doChange(client, factory, mv, false); // Don't pass
        // However, it should still be possible to delete the 2 images
        // and have the fileset cleaned up.
        List<Long> ids = new ArrayList<Long>();
        ids.add(img0);
        ids.add(img1);
        Delete2 dc = Requests.delete("Image", ids);
        callback(true, client, dc);
        assertDoesNotExist(new FilesetI(fs0, false));
    }

    /**
     * Creates a MIF images. Moves the fileset.
     */
    @Test(groups = {"fs", "integration"})
    public void testMoveFilesetAsRoot() throws Throwable {
    	int imageCount = 2;
    	List<Image> images = importMIF(imageCount);
        long fs0 = images.get(0).getFileset().getId().getValue();

        final Chgrp2 mv = Requests.chgrp("Fileset", fs0, secondGroup.getId().getValue());

        Response rsp = doChange(client, factory, mv, true);
        OK err = (OK) rsp;
        assertNotNull(err);
    }
    
    
    /**
     * Creates a MIF images with acquisition data. Moves the fileSet
     */
    @Test(groups = {"fs", "integration"})
    public void testMoveMIFWithAcquisitionData() throws Throwable {
    	int imageCount = 1;
    	Fileset set = new FilesetI();
    	set.setTemplatePrefix(omero.rtypes.rstring("fake"));
    	List<Long> ids = new ArrayList<Long>();
    	for (int i = 0; i < imageCount; i++) {
    		File f = File.createTempFile(
    		        RandomStringUtils.random(100, false, true),".ome.xml");
    		f.deleteOnExit();
    		XMLMockObjects xml = new XMLMockObjects();
    		XMLWriter writer = new XMLWriter();
    		OME ome = xml.createImageWithAcquisitionData();
    		writer.writeFile(f, ome, true);
    		List<Pixels> pixels = null;
    		try {
    			pixels = importFile(f, "ome.xml");
    		} catch (Throwable e) {
    			throw new Exception("cannot import image", e);
    		}
    		Pixels p = pixels.get(0);
    		ids.add(p.getImage().getId().getValue());
    	}
    	//Load the image
    	IContainerPrx iContainer = factory.getContainerService();
    	List<Image> images = iContainer.getImages(Image.class.getName(),
    			ids, new Parameters());
    	Iterator<Image> j = images.iterator();
    	while (j.hasNext()) {
    		set.addImage(j.next());
		}
    	set = (Fileset) iUpdate.saveAndReturnObject(set);
    	final Chgrp2 mv = Requests.chgrp("Fileset", set.getId().getValue(), secondGroup.getId().getValue());
    	Response rsp = doChange(client, factory, mv, true);
    	OK err = (OK) rsp;
    	assertNotNull(err);
    	disconnect();
    	
    	// Reconnect in second group to check group.id for all objects in graph
    	long gid2 = secondGroup.getId().getValue();
    	loginUser(new ExperimenterGroupI(secondGroup.getId().getValue(),
    			false));
    	iContainer = factory.getContainerService();
    	images = iContainer.getImages(Image.class.getName(),
    			ids, new Parameters());
    	
    	//for each image
    	Iterator<Image> i = images.iterator();
    	while (i.hasNext()) {
    		Image img = i.next();
    		assertEquals(img.getDetails().getGroup().getId().getValue(), gid2);
    		Pixels pixels = img.getPrimaryPixels();
        	long pixId = pixels.getId().getValue();
        	//method already tested in PixelsServiceTest
        	//make sure objects are loaded.
        	pixels = factory.getPixelsService().retrievePixDescription(pixId);
        	List<Long> lcIds = new ArrayList<Long>();
        	LogicalChannel lc;
        	Channel channel;
        	for (int i1 = 0; i1 < pixels.getSizeC().getValue(); i1++) {
    			channel = pixels.getChannel(i1);
    			lc = channel.getLogicalChannel();
    			lcIds.add(lc.getId().getValue());
        	}
        	iMetadata = factory.getMetadataService();
        	List<LogicalChannel> channels = iMetadata.loadChannelAcquisitionData(
        			lcIds);
        	assertEquals(channels.size(), pixels.getSizeC().getValue());
        	LogicalChannel loaded;
        	Iterator<LogicalChannel> lci = channels.iterator();
        	LightSourceData l;
        	while (lci.hasNext()) {
        		loaded = lci.next();
        		assertNotNull(loaded);
        		assertEquals(loaded.getDetails().getGroup().getId().getValue(), gid2);
            	ChannelAcquisitionData data = new ChannelAcquisitionData(loaded);
            	assertEquals(data.getDetector().asIObject().getDetails().getGroup().getId().getValue(), gid2);
//            	assertEquals(data.getFilterSet().asIObject().getDetails().getGroup().getId().getValue(), gid2);		// getFilterSet() is null
            	l = (LightSourceData) data.getLightSource();
            	assertEquals(l.asIObject().getDetails().getGroup().getId().getValue(), gid2);
            	assertEquals(loaded.getDetectorSettings().getDetails().getGroup().getId().getValue(), gid2);
            	assertEquals(loaded.getLightSourceSettings().getDetails().getGroup().getId().getValue(), gid2);
            	assertNotNull(loaded.getDetectorSettings().getBinning());	// No Group on Binning
            	assertEquals(loaded.getDetectorSettings().getDetector().getDetails().getGroup().getId().getValue(), gid2);
            	assertNotNull(loaded.getDetectorSettings().getDetector().getType());
            	assertEquals(loaded.getLightPath().getDetails().getGroup().getId().getValue(), gid2);
            	assertEquals(data.getLightPath().getDichroic().asIObject().getDetails().getGroup().getId().getValue(), gid2);
            	assertNotNull(data.getContrastMethod());
            	assertNotNull(data.getIllumination());
            	assertNotNull(data.getMode());

//            	//OTF support            	
//            	assertNotNull(loaded.getOtf());		// null
//            	assertNotNull(loaded.getOtf().getFilterSet());
//            	assertEquals(loaded.getOtf().getObjective().getDetails().getGroup().getId().getValue(), gid2);
//            	assertEquals(loaded.getOtf().getFilterSet().getDetails().getGroup().getId().getValue(), gid2);
//            	assertNotNull(loaded.getOtf().getPixelsType());

    		}
        	
        	
        	Instrument instrument = iMetadata.loadInstrument(
        			img.getInstrument().getId().getValue());
        	InstrumentData data = new InstrumentData(instrument);
        	assertEquals(data.asIObject().getDetails().getGroup().getId().getValue(), gid2);
    		assertTrue(instrument.sizeOfDetector() > 0);
    		assertTrue(instrument.sizeOfDichroic() > 0);
    		assertTrue(instrument.sizeOfFilter() > 0);
    		assertTrue(instrument.sizeOfFilterSet() > 0);
    		assertEquals(instrument.sizeOfLightSource(), 5);
    		assertTrue(instrument.sizeOfObjective() > 0);
//    		assertTrue(instrument.sizeOfOtf() > 0);
    		
    		assertEquals(instrument.sizeOfDetector(),
    			data.getDetectors().size());
    		assertEquals(instrument.sizeOfDichroic(),
    			data.getDichroics().size());
    		assertEquals(instrument.sizeOfFilter(),
    			data.getFilters().size());
    		assertEquals(instrument.sizeOfFilterSet(),
    			data.getFilterSets().size());
    		assertEquals(instrument.sizeOfLightSource(),
    			data.getLightSources().size());
    		assertEquals(instrument.sizeOfObjective(),
    			data.getObjectives().size());
    		assertEquals(instrument.sizeOfOtf(),
    			data.getOTF().size());
    		
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
        	
        	Iterator j1;
    		detectors = instrument.copyDetector();
    		j1 = detectors.iterator();
    		while (j1.hasNext()) {
    			detector = (Detector) j1.next();
    			assertEquals(detector.getDetails().getGroup().getId().getValue(), gid2);
			}
    		filters = instrument.copyFilter();
    		j1 = filters.iterator();
    		while (j1.hasNext()) {
				filter = (Filter) j1.next();
				assertEquals(filter.getTransmittanceRange().getDetails().getGroup().getId().getValue(), gid2);
			}
    		filterSets = instrument.copyFilterSet();
    		j1 = filterSets.iterator();
    		while (j1.hasNext()) {
				fs = (FilterSet) j1.next();
				assertEquals(fs.getDetails().getGroup().getId().getValue(), gid2);
			}
    		objectives = instrument.copyObjective();
    		j1 = objectives.iterator();
    		while (j1.hasNext()) {
				objective = (Objective) j1.next();
				assertEquals(objective.getDetails().getGroup().getId().getValue(), gid2);
				assertNotNull(objective.getCorrection());
				assertNotNull(objective.getImmersion());
//				assertEquals(objective.getImmersion().getDetails().getGroup().getId().getValue(), gid2);
			}
//    		otfs = instrument.copyOtf();
//    		j1 = otfs.iterator();
//    		while (j1.hasNext()) {
//				otf = (OTF) j1.next();
//				objective = otf.getObjective();
//				assertNotNull(otf.getPixelsType());
//				assertNotNull(otf.getFilterSet());
//				assertNotNull(objective);
//				assertNotNull(objective.getCorrection());
//				assertNotNull(objective.getImmersion());
//			}
    		lights = instrument.copyLightSource();
    		j1 = lights.iterator();
    		while (j1.hasNext()) {
    			light = (LightSource) j1.next();
				if (light instanceof Laser) {
					laser = (Laser) light;
					assertNotNull(laser.getType());
					assertNotNull(laser.getLaserMedium());
//					assertNotNull(laser.getPulse());
				}
			}
    	
    	
    	}
    }
    
    /**
     * Creates a MIF images and link to a dataset
     */
    @Test(groups = {"fs", "integration"})
    public void testMoveDatasetWithMIF() throws Throwable {
    	int imageCount = 2;
    	List<Image> images = importMIF(imageCount);
    	Dataset dataset = new DatasetI();
        dataset.setName(omero.rtypes.rstring("testMoveDatasetWithMIF"));
        dataset = (Dataset) iUpdate.saveAndReturnObject(dataset);
        Iterator<Image> i = images.iterator();
        List<IObject> links = new ArrayList<IObject>();
        while (i.hasNext()) {
        	DatasetImageLink link = new DatasetImageLinkI();
        	link.setChild((Image) i.next().proxy());
        	link.setParent((Dataset) dataset.proxy());
			links.add(link);
		}
        long filesetID = images.get(0).getFileset().getId().getValue();
        iUpdate.saveAndReturnArray(links);
        final Chgrp2 dc = Requests.chgrp("Dataset", dataset.getId().getValue(), secondGroup.getId().getValue());

    	doAllChanges(client, factory, true, dc);
    	disconnect();
    	loginUser(new ExperimenterGroupI(secondGroup.getId().getValue(),
    			false));
    	//load the dataset
    	IContainerPrx iContainer = factory.getContainerService();
    	ParametersI param = new ParametersI();
    	param.leaves();
    	List<IObject> values = iContainer.loadContainerHierarchy(
        		Dataset.class.getName(),
        		Arrays.asList(dataset.getId().getValue()), param);
    	assertEquals(1, values.size());
    	dataset = (Dataset) values.get(0);
    	assertEquals(imageCount, dataset.sizeOfImageLinks());
    	List<DatasetImageLink> imageLinks = dataset.copyImageLinks();
    	for (DatasetImageLink link : imageLinks) {
    		Image img = link.getChild();
    		assertEquals(img.getFileset().getId().getValue(), filesetID);
    	}
    }
}
