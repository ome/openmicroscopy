package integration;

import java.util.Arrays;
import java.util.Random;

import omero.ServerError;
import omero.api.IRenderingSettingsPrx;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.Folder;
import omero.model.Image;
import omero.model.ImportJob;
import omero.model.ImportJobI;
import omero.model.JobStatus;
import omero.model.LogicalChannel;
import omero.model.Namespace;
import omero.model.NamespaceI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Project;
import omero.model.Reagent;
import omero.model.RenderingDef;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.StageLabel;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.Well;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ome.api.JobHandle;

public class ObjectPropertiesTest extends AbstractServerTest {

    /**
     * Creates a name with length as a string
     * @param length length of the name to create in bytes
     * @return the name
     * @throws Exception unexpected
     */
    private String createName(int length) throws Exception {
        final Random rng = new Random();
        final char[] chars = new char[length];  // string length
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('a' + rng.nextInt(26));
        }
        final String string = new String(chars);
        return string;
    }

    /**
     * Sets up the testing user and group before each test
     * @throws Exception unexpected
     */
    @BeforeMethod
    private void createUserAndGroup() throws Exception {
        newUserAndGroup("rw----");
    }

    /**
     * Test to create a (tag) annotation and
     * save it with long name and long namespace
     * also has a negative test for names and namespaces >2KB
     * @throws Exception unexpected
     */
    @Test
    public void testAnnotationNameAndNsSaving() throws Exception {

        /* Tag annotation is used here as a good representative
         * to test the annotations in general */
        final TagAnnotation ann = new TagAnnotationI();

        /* for annotation name sizes of >2KB the test fails */
        /* createName() creates name with length in approx. bytes */
        String name = createName(1000000);
        ann.setName(omero.rtypes.rstring(name));
        try {
            iUpdate.saveAndReturnObject(ann);
            Assert.fail("Hibernate operation: could not insert:..."
                      + "ERROR: index row requires 1000016 bytes, maximum size is 8191; ");
        } catch (ServerError se) {
            /* expected */
        }

        /* similarly, for annotation namespace sizes of >2KB the test fails */
        String namespace = createName(1000000);
        ann.setNs(omero.rtypes.rstring(namespace));

        /* need to revert the name to 2KB size in order to be sure to test
         * for namespace failure, not both name and namespace failure */
        name = createName(2000);
        try {
            iUpdate.saveAndReturnObject(ann);
            Assert.fail("Hibernate operation: could not insert:..."
                      + "ERROR: index row requires 1000016 bytes, maximum size is 8191; ");
        } catch (ServerError se) {
            /* expected */
        }

        /* now set namespace with size 2KB (name was set to this size already above)
         * and let the test pass */

        namespace = createName(2000);
        ann.setName(omero.rtypes.rstring(name));
        ann.setNs(omero.rtypes.rstring(namespace));;
        TagAnnotation sent = (TagAnnotation) iUpdate.saveAndReturnObject(ann);
        String savedName = sent.getName().getValue().toString();
        String savedNamespace = sent.getNs().getValue().toString();
        long id = sent.getId().getValue();
        final TagAnnotation retrievedAnnotation = (TagAnnotation) iQuery.get("TagAnnotation", id);
        final String retrievedName = retrievedAnnotation.getName().getValue().toString();
        final String retrievedNamespace = retrievedAnnotation.getNs().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
        Assert.assertEquals(namespace, retrievedNamespace);
        Assert.assertEquals(namespace, savedNamespace);
    }

    /**
     * Test to create a channel and save it with long lookup table
     * and test saving logical channel with long name.
     * @throws Exception unexpected
     */
    @Test
    public void testChannelLUTLogChannelName() throws Exception {

        /* create some image which contains a
         * valid channel which contains a valid logical channel*/
        final Image img = mmFactory.createImage();
        final Image sentImage = (Image) iUpdate.saveAndReturnObject(img);

        /* get the channel from the image and set
         * the lut of the channel and the name of the logical channel */
        final Channel ch = sentImage.getPrimaryPixels().getChannel(0);
        final String lut = createName(1000000);
        ch.setLookupTable(omero.rtypes.rstring(lut));
        final String logChannelName = createName(1000000);
        ch.getLogicalChannel().setName(omero.rtypes.rstring(logChannelName));

        /* save the channel back and get the saved lut and logical channel name
         * and ids of channel and logical channel for later query*/
        Channel sent = (Channel) iUpdate.saveAndReturnObject(ch);
        String savedLut = sent.getLookupTable().getValue().toString();
        String savedLogChannelName = sent.getLogicalChannel().getName().getValue().toString();
        long id = sent.getId().getValue();
        long lChId = sent.getLogicalChannel().getId().getValue();

        /* query the DB for the lut and logical channel name */
        final Channel retrievedChannel = (Channel) iQuery.get("Channel", id);
        final LogicalChannel retrievedLogChannel = (LogicalChannel) iQuery.get("LogicalChannel", lChId);
        final String retrievedLut = retrievedChannel.getLookupTable().getValue().toString();
        final String retrievedLogChannelName = retrievedLogChannel.getName().getValue().toString();

        /* check that the originally created Lut, the saved lut,
         * and the lut retrieved by the query are the same. Do 
         * a similar check for the logical channel name */
        Assert.assertEquals(lut, retrievedLut);
        Assert.assertEquals(lut, savedLut);
        Assert.assertEquals(logChannelName, retrievedLogChannelName);
        Assert.assertEquals(logChannelName, savedLogChannelName);
    }

    /**
     * Test to create a dataset and save it with long name
     * @throws Exception unexpected
     */
    @Test
    public void testDatasetNameSaving() throws Exception {
        Dataset dat = mmFactory.simpleDataset();
        final String name = createName(1000000);
        dat.setName(omero.rtypes.rstring(name));
        Dataset sent = (Dataset) iUpdate.saveAndReturnObject(dat);
        String savedName = sent.getName().getValue().toString();
        long id = sent.getId().getValue();
        final Dataset retrievedDataset = (Dataset) iQuery.get("Dataset", id);
        final String retrievedName = retrievedDataset.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create a folder and save it with long name
     * @throws Exception unexpected
     */
    @Test
    public void testFolderNameSaving() throws Exception {
        Folder dat = mmFactory.simpleFolder();
        final String name = createName(1000000);
        dat.setName(omero.rtypes.rstring(name));
        Folder sent = (Folder) iUpdate.saveAndReturnObject(dat);
        String savedName = sent.getName().getValue().toString();
        long id = sent.getId().getValue();
        final Folder retrievedDataset = (Folder) iQuery.get("Folder", id);
        final String retrievedName = retrievedDataset.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create an image and save it with long name
     * @throws Exception unexpected
     */
    @Test
    public void testImageNameSaving() throws Exception {
        Image img = mmFactory.simpleImage();
        final String name = createName(1000000);
        img.setName(omero.rtypes.rstring(name));
        Image sent = (Image) iUpdate.saveAndReturnObject(img);
        String savedName = sent.getName().getValue().toString();
        long id = sent.getId().getValue();
        final Image retrievedImage = (Image) iQuery.get("Image", id);
        final String retrievedName = retrievedImage.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create a long import job image name
     * and a long import job image description and save these
     * @throws Exception unexpected
     */
    @Test
    public void testImportJobImageNameAndDescrSaving() throws Exception {

        /*login as root in order to be able to create an ImportJob */
        logRootIntoGroup();

        /* set up an ImportJob with all the compulsory parameters */

        final ImportJob importJob = new ImportJobI();
        importJob.setGroupname(omero.rtypes.rstring("GroupName"));
        importJob.setMessage(omero.rtypes.rstring("message"));
        importJob.setScheduledFor(omero.rtypes.rtime(System.currentTimeMillis()));
        importJob.setStatus((JobStatus) factory.getTypesService().getEnumeration(JobStatus.class.getName(), JobHandle.FINISHED));
        importJob.setSubmitted(omero.rtypes.rtime(System.currentTimeMillis()));
        importJob.setType(omero.rtypes.rstring("Test"));
        importJob.setUsername(omero.rtypes.rstring("username"));

        /* set a long name and a long description for the ImportJob */
        final String name = createName(1000000);
        importJob.setImageName(omero.rtypes.rstring(name));
        final String desc = createName(1000000);
        importJob.setImageDescription(omero.rtypes.rstring(desc));

        /* save the ImmportJob and get the saved name, description 
         * and id for later query */

        ImportJob sent = (ImportJob) iUpdate.saveAndReturnObject(importJob);
        final String savedName = sent.getImageName().getValue().toString();
        final String savedDesc = sent.getImageDescription().getValue().toString();
        final long id = sent.getId().getValue();
        final ImportJob retrievedImportJob = (ImportJob) iQuery.get("ImportJob", id);
        final String retrievedName = retrievedImportJob.getImageName().getValue().toString();
        final String retrievedDesc = retrievedImportJob.getImageDescription().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
        Assert.assertEquals(desc, retrievedDesc);
        Assert.assertEquals(desc, savedDesc);
    }

    /**
     * Test to create a namespace and save it
     * with a long name and a long display name
     * also has negative tests for
     * namespace names and display names >2KB
     * @throws Exception unexpected
     */
    @Test
    public void testNamespaceNameAndDisplayName() throws Exception {

        /*login as root in order to be able to create a Namespace */
        logRootIntoGroup();
        final Namespace ns = new NamespaceI();

        /* for namespace name sizes of >2KB the test fails */
        /* createName() creates name with length in approx. bytes */
        String name = createName(1000000);
        ns.setName(omero.rtypes.rstring(name));
        try {
            iUpdate.saveAndReturnObject(ns);
            Assert.fail("Hibernate operation: could not insert:..."
                      + "ERROR: index row requires 1000016 bytes, maximum size is 8191; ");
        } catch (ServerError se) {
            /* expected */
        }

        /* similarly, for namespace display name sizes of >2KB the test fails */
        String displayName = createName(1000000);
        ns.setDisplayName(omero.rtypes.rstring(displayName));

        /* need to revert the name to 2KB size in order to be sure to test
         * for displayName failure, not both name and displayName failure */
        name = createName(2000);
        try {
            iUpdate.saveAndReturnObject(ns);
            Assert.fail("Hibernate operation: could not insert:..."
                      + "ERROR: index row requires 1000016 bytes, maximum size is 8191; ");
        } catch (ServerError se) {
            /* expected */
        }

        /* now set displayName with size 2KB (name was set to this size already above)
         * and let the test pass */

        displayName = createName(2000);
        ns.setName(omero.rtypes.rstring(name));
        ns.setDisplayName(omero.rtypes.rstring(displayName));
        Namespace sent = (Namespace) iUpdate.saveAndReturnObject(ns);
        final String savedName = sent.getName().getValue().toString();
        final String savedDisplayName = sent.getDisplayName().getValue().toString();
        long id = sent.getId().getValue();
        final Namespace retrievedNamespace = (Namespace) iQuery.get("Namespace", id);
        final String retrievedName = retrievedNamespace.getName().getValue().toString();
        final String retrievedDisplayName = retrievedNamespace.getDisplayName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
        Assert.assertEquals(displayName, retrievedDisplayName);
        Assert.assertEquals(displayName, savedDisplayName);
    }

    /**
     * Test to create an original file and save it
     * with long name and with long hash (2KB),
     * also has a negative test for hash >2KB
     * @throws Exception unexpected
     */
    @Test
    public void testOriginalFileNameAndHash() throws Exception {
        final OriginalFile oFile = mmFactory.createOriginalFile();

        /* createName() creates name with length in approx. bytes */
        String name = createName(1000000);
        oFile.setName(omero.rtypes.rstring(name));

        /* for original file hash sizes of >2KB the test fails */
        String hash = createName(1000000);
        oFile.setHash(omero.rtypes.rstring(hash));
        try {
            iUpdate.saveAndReturnObject(oFile);
            Assert.fail("Hibernate operation: could not insert:..."
                      + "ERROR: index row requires 1000016 bytes, maximum size is 8191; ");
        } catch (ServerError se) {
            /* expected */
        }

        /* now set hash with size 2KB (name is fine at 1MB as set above)
         * and let the test pass */

        hash = createName(2000);
        oFile.setName(omero.rtypes.rstring(name));
        oFile.setHash(omero.rtypes.rstring(hash));
        OriginalFile sent = (OriginalFile) iUpdate.saveAndReturnObject(oFile);
        String savedName = sent.getName().getValue().toString();
        String savedHash = sent.getHash().getValue().toString();
        long id = sent.getId().getValue();
        final OriginalFile retrievedOFile = (OriginalFile) iQuery.get("OriginalFile", id);
        final String retrievedName = retrievedOFile.getName().getValue().toString();
        final String retrievedHash = retrievedOFile.getHash().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
        Assert.assertEquals(hash, retrievedHash);
        Assert.assertEquals(hash, savedHash);
    }

    /**
     * Test to create a plate and save it with long name
     * and a long status and create plate acquisition and save it 
     * with a long name
     * @throws Exception unexpected
     */
    @Test
    public void testPlateNameStatusAcquistitionName() throws Exception {

        /* First test for plate name and status */

        final Plate plate = mmFactory.createPlate(1, 1, 1, 1, false);
        final String name = createName(1000000);
        final String status = createName(1000000);
        plate.setName(omero.rtypes.rstring(name));
        plate.setStatus(omero.rtypes.rstring(status));
        Plate sentP = (Plate) iUpdate.saveAndReturnObject(plate);
        String savedName = sentP.getName().getValue().toString();
        String savedStatus = sentP.getStatus().getValue().toString();
        long idPlate = sentP.getId().getValue();
        final Plate retrievedPlate = (Plate) iQuery.get("Plate", idPlate);
        final String retrievedName = retrievedPlate.getName().getValue().toString();
        final String retrievedStatus = retrievedPlate.getStatus().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
        Assert.assertEquals(status, retrievedStatus);
        Assert.assertEquals(status, savedStatus);

        /* With the plate created in above test in hand
         * test for plate acquisition name.*/

        final PlateAcquisition acquisition = (PlateAcquisition) iQuery.findByQuery("FROM PlateAcquisition WHERE plate.id = :id", new ParametersI().addId(idPlate));
        final String acquisitionName = createName(1000000);
        acquisition.setName(omero.rtypes.rstring(acquisitionName));
        PlateAcquisition sentA = (PlateAcquisition) iUpdate.saveAndReturnObject(acquisition);
        String savedAcquisitionName = sentA.getName().getValue().toString();
        long idAcquisition = sentA.getId().getValue();
        final PlateAcquisition retrievedAcquisition = (PlateAcquisition) iQuery.get("PlateAcquisition", idAcquisition);
        final String retrievedAcquisitionName = retrievedAcquisition.getName().getValue().toString();
        Assert.assertEquals(acquisitionName, retrievedAcquisitionName);
        Assert.assertEquals(acquisitionName, savedAcquisitionName);
    }

    /**
     * Test to create a project and save it with long name
     * @throws Exception unexpected
     */
    @Test
    public void testProjectNameSaving() throws Exception {
        Project pro = mmFactory.simpleProject();
        final String name = createName(1000000);
        pro.setName(omero.rtypes.rstring(name));
        Project sent = (Project) iUpdate.saveAndReturnObject(pro);
        String savedName = sent.getName().getValue().toString();
        long id = sent.getId().getValue();
        final Project retrievedProject = (Project) iQuery.get("Project", id);
        final String retrievedName = retrievedProject.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create a reagent and save it with long name
     * @throws Exception unexpected
     */
    @Test
    public void testReagentNameSaving() throws Exception {

        /* first need to create a screen, then a plate
         * with reagent and link it to the screen to 
         * populate all necessary parameters of reagent
         */

        Screen screen = mmFactory.simpleScreenData().asScreen();
        Reagent reagent = mmFactory.createReagent();
        screen.addReagent(reagent);
        Plate p = mmFactory.createPlateWithReagent(1, 1, 1, reagent);
        screen.linkPlate(p);
        Screen sentS = (Screen) iUpdate.saveAndReturnObject(screen);
        long screenId = sentS.getId().getValue();

        /* now get the updated reagent back via a query */
        reagent = (Reagent) iQuery.findByQuery("FROM Reagent WHERE screen.id = :id", 
                new ParametersI().addId(screenId));

        /* create a reagent name and set it on the retrieved reagent object */

        final String name = createName(1000000);
        reagent.setName(omero.rtypes.rstring(name));
        Reagent sentR = (Reagent) iUpdate.saveAndReturnObject(reagent);
        String savedName = sentR.getName().getValue().toString();
        long id = sentR.getId().getValue();
        final Reagent retrievedReagent = (Reagent) iQuery.get("Reagent", id);
        final String retrievedName = retrievedReagent.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create a rendering definition and save it with long name
     * @throws Exception unexpected
     */
    @Test
    public void testRenderingDefinitionNameSaving() throws Exception {

        /* first prepare image and pixels to get valid rendering defs */
        Image img = mmFactory.createImage();
        Image sentI = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = sentI.getPrimaryPixels();

        /* now get the rendering defs back via a method and a query */
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        final RenderingDef rDef = (RenderingDef) iQuery.findByQuery("select rdef from RenderingDef as rdef where rdef.pixels.id = :id", 
                new ParametersI().addId(pixels.getId().getValue()));

        /* create a rendering defs name and set it on the retrieved rDef object */

        final String name = createName(1000000);
        rDef.setName(omero.rtypes.rstring(name));
        RenderingDef sentR = (RenderingDef) iUpdate.saveAndReturnObject(rDef);
        String savedName = sentR.getName().getValue().toString();
        long id = sentR.getId().getValue();
        final RenderingDef retrievedRDef = (RenderingDef) iQuery.get("RenderingDef", id);
        final String retrievedName = retrievedRDef.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create a roi and save it with long name
     * also contains negative test for roi names >2KB
     * @throws Exception unexpected
     */
    @Test
    public void testRoiNameSaving() throws Exception {

        /* first prepare image to get valid roi */
        Image img = mmFactory.createImageWithRoi();
        Image sentI = (Image) iUpdate.saveAndReturnObject(img);
        /* now get the roi back using a query */
        final Roi roi = (Roi) iQuery.findByQuery("select roi from Roi as roi where roi.image.id = :id",
                new ParametersI().addId(sentI.getId().getValue()));

        /* test for failure of names >2KB for roi name */
        String name = createName(1000000);
        roi.setName(omero.rtypes.rstring(name));
        try {
            iUpdate.saveAndReturnObject(roi);
            Assert.fail("Hibernate operation: could not insert:..."
                      + "ERROR: index row requires 1000016 bytes, maximum size is 8191; ");
        } catch (ServerError se) {
            /* expected */
        }

        /* now set name with size 2KB and let the test pass */

        name = createName(2000);
        roi.setName(omero.rtypes.rstring(name));
        Roi sentR = (Roi) iUpdate.saveAndReturnObject(roi);
        String savedName = sentR.getName().getValue().toString();
        long id = sentR.getId().getValue();
        final Roi retrievedRoi = (Roi) iQuery.get("Roi", id);
        final String retrievedName = retrievedRoi.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create a screen and save it with long name
     * and long protocol description and long reagentSet description
     * @throws Exception unexpected
     */
    @Test
    public void testScreenNameProtocolDescReagentSetDesc() throws Exception {

        /* create some screen which contains a
         * valid protocol and reagentSet descriptions */
        Screen screen = mmFactory.simpleScreen();

        /* set long name, protocol and reagent set descriptions */
        final String name = createName(1000000);
        screen.setName(omero.rtypes.rstring(name));
        final String protocolDesc = createName(1000000);
        screen.setProtocolDescription(omero.rtypes.rstring(protocolDesc));
        final String reagentSetDesc = createName(1000000);
        screen.setReagentSetDescription(omero.rtypes.rstring(reagentSetDesc));

        /* save the screen with the name, and protocol and reagent
         * set descriptions and get the saved values back */
        Screen sent = (Screen) iUpdate.saveAndReturnObject(screen);
        String savedName = sent.getName().getValue().toString();
        String savedProtocolDesc = sent.getProtocolDescription().getValue().toString();
        String savedReagentSetDesc = sent.getReagentSetDescription().getValue().toString();
        long id = sent.getId().getValue();

        /* query for the screen and check that the retrieved name,
         * protocol description and reagent description
         * match the ones which were created and saved */

        final Screen retrievedScreen = (Screen) iQuery.get("Screen", id);
        final String retrievedName = retrievedScreen.getName().getValue().toString();
        final String retrievedProtocolDesc = retrievedScreen.getProtocolDescription().getValue().toString();
        final String retrievedReagentSetDesc = retrievedScreen.getReagentSetDescription().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
        Assert.assertEquals(protocolDesc, retrievedProtocolDesc);
        Assert.assertEquals(protocolDesc, savedProtocolDesc);
        Assert.assertEquals(reagentSetDesc, retrievedReagentSetDesc);
        Assert.assertEquals(reagentSetDesc, savedReagentSetDesc);
    }

    /**
     * Test to create a stage label and save it with long name
     * @throws Exception unexpected
     */
    @Test
    public void testStageLabelNameSaving() throws Exception {
        StageLabel stageLabel = mmFactory.createStageLabel();
        final String name = createName(1000000);
        stageLabel.setName(omero.rtypes.rstring(name));
        StageLabel sent = (StageLabel) iUpdate.saveAndReturnObject(stageLabel);
        String savedName = sent.getName().getValue().toString();
        long id = sent.getId().getValue();
        final StageLabel retrievedStageLabel = (StageLabel) iQuery.get("StageLabel", id);
        final String retrievedName = retrievedStageLabel.getName().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }

    /**
     * Test to create a long external description on
     * a well and save it
     * @throws Exception unexpected
     */
    @Test
    public void testWellExternalDescription() throws Exception {

        /* create some plate which contains a valid well */
        final Plate plate = mmFactory.createPlate(1, 1, 1, 1, false);
        Plate sentP = (Plate) iUpdate.saveAndReturnObject(plate);

        /* get the well back using a query */
        final Well well = (Well) iQuery.findByQuery("select well from Well as well where well.plate.id = :id",
                new ParametersI().addId(sentP.getId().getValue()));

        /* create a long external description, set it on the well
         * save the well, check that the saved description
         * matches the originally created string */

        final String name = createName(1000000);
        well.setExternalDescription(omero.rtypes.rstring(name));
        Well sent = (Well) iUpdate.saveAndReturnObject(well);
        String savedName = sent.getExternalDescription().getValue().toString();
        long id = sent.getId().getValue();
        final Well retrievedWell = (Well) iQuery.get("Well", id);
        final String retrievedName = retrievedWell.getExternalDescription().getValue().toString();
        Assert.assertEquals(name, retrievedName);
        Assert.assertEquals(name, savedName);
    }
}
