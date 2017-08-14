#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Testing the gateway.getObject() and deleteObjects() methods

   Copyright 2013-2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_generated
   - author_testimg_tiny

"""

import omero
import uuid
import pytest

from omero.gateway.scripts import dbhelpers
from omero.rtypes import wrap
from omero.testlib import ITest
from omero.gateway import BlitzGateway, KNOWN_WRAPPERS
from omero.model import DatasetI, \
    ImageI, \
    PlateI, \
    ScreenI, \
    WellI, \
    WellSampleI


class TestDeleteObject (object):

    def testDeleteAnnotation(self, author_testimg_generated):
        image = author_testimg_generated
        gateway = image._conn

        # create Tag on Image and try to delete Tag
        tag = omero.gateway.TagAnnotationWrapper(gateway)
        ns_tag = "omero.gateway.test.get_objects.test_delete_annotation_tag"
        tag.setNs(ns_tag)
        tag.setValue("Test Delete Tag")
        tag = image.linkAnnotation(tag)
        tagId = tag.getId()
        handle = gateway.deleteObjects("Annotation", [tagId])
        gateway._waitOnCmd(handle)
        assert gateway.getObject("Annotation", tagId) is None

    def testDeleteImage(self, gatewaywrapper, author_testimg_generated):
        image = author_testimg_generated
        imageId = image.getId()
        project = gatewaywrapper.getTestProject()
        projectId = project.getId()
        ns = "omero.gateway.test.get_objects.test_delete_image_comment"
        ns_tag = "omero.gateway.test.get_objects.test_delete_image_tag"

        # create Comment
        ann = omero.gateway.CommentAnnotationWrapper(gatewaywrapper.gateway)
        ann.setNs(ns)
        ann.setValue("Test Comment")
        ann = image.linkAnnotation(ann)

        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(gatewaywrapper.gateway)
        tag.setNs(ns_tag)
        tag.setValue("Test Tag")
        tag = image.linkAnnotation(tag)

        # check the Comment
        assert gatewaywrapper.gateway.getObject(
            "Annotation", ann.id) is not None
        assert gatewaywrapper.gateway.getObject(
            "Annotation", tag.id) is not None

        # check Image, delete (wait) and check
        assert gatewaywrapper.gateway.getObject("Image", imageId) is not None
        handle = gatewaywrapper.gateway.deleteObjects("Image", [imageId])
        gatewaywrapper.gateway._waitOnCmd(handle)
        assert gatewaywrapper.gateway.getObject("Image", imageId) is None

        # Comment should be deleted but not the Tag (becomes orphan)
        assert gatewaywrapper.gateway.getObject("Annotation", ann.id) is None
        assert gatewaywrapper.gateway.getObject(
            "Annotation", tag.id) is not None

        # Add the tag to project and delete (with Tags)
        assert gatewaywrapper.gateway.getObject(
            "Project", projectId) is not None
        project.linkAnnotation(tag)
        datasetIds = [d.getId() for d in project.listChildren()]
        assert len(datasetIds) > 0
        handle = gatewaywrapper.gateway.deleteObjects(
            "Project", [projectId], deleteAnns=True, deleteChildren=True)
        gatewaywrapper.gateway._waitOnCmd(handle)
        assert gatewaywrapper.gateway.getObject("Project", projectId) is None
        assert gatewaywrapper.gateway.getObject("Annotation", tag.id) is None
        # Tag should be gone

        # check datasets gone too
        for dId in datasetIds:
            assert gatewaywrapper.gateway.getObject("Dataset", dId) is None


class TestFindObject (object):

    def testIllegalObjTypeInt(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        with pytest.raises(AttributeError):
            gatewaywrapper.gateway.getObject(1, 1L)

    def testObjTypeUnicode(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        a = gatewaywrapper.getTestProject()
        b = gatewaywrapper.gateway.getObject(u'Project', a.getId())
        assert a.getId() == b.getId()

    def testObjTypeString(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        a = gatewaywrapper.getTestProject()
        b = gatewaywrapper.gateway.getObject('Project', a.getId())
        assert a.getId() == b.getId()

    def testFindProject(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        project = gatewaywrapper.getTestProject()
        pName = project.getName()

        findProjects = list(gatewaywrapper.gateway.getObjects(
            "Project", None, attributes={"name": pName}))
        assert len(findProjects) > 0, "Did not find Project by name"
        for p in findProjects:
            assert p.getName() == pName, \
                "All projects should have queried name"

    def testFindExperimenter(self, gatewaywrapper, author_testimg_tiny):
        omeName = author_testimg_tiny.getOwnerOmeName()
        group = author_testimg_tiny.getDetails().getGroup()
        groupName = group.getName()
        gatewaywrapper.loginAsAdmin()

        # findObjects
        findAuthor = list(gatewaywrapper.gateway.getObjects(
            "Experimenter", None, attributes={"omeName": omeName}))
        assert len(findAuthor) == 1, "Did not find Experimenter by omeName"
        assert findAuthor[0].omeName == omeName

        # findObject
        author = gatewaywrapper.gateway.getObject(
            "Experimenter", None, attributes={"omeName": omeName})
        assert author is not None
        assert author.omeName == omeName

        # find group
        grp = gatewaywrapper.gateway.getObject(
            "ExperimenterGroup", None, attributes={"name": groupName})
        assert grp is not None
        assert grp.getName() == groupName

    def testFindAnnotation(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # start by deleting any tag created by this method that may have been
        # left behind
        tag_value = "FindThisTag"
        find_ns = "omero.gateway.test.test_find_annotations"
        find_tag = gatewaywrapper.gateway.getObjects(
            "Annotation", attributes={"textValue": tag_value, "ns": find_ns})
        ids = [t._obj.id.val for t in find_tag]
        if ids:
            gatewaywrapper.gateway.deleteObjects("Annotation", ids, wait=True)

        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(gatewaywrapper.gateway)
        tag.setNs(find_ns)
        tag.setValue(tag_value)
        tag.save()
        tagId = tag.getId()

        # findObject by name
        find_tag = gatewaywrapper.gateway.getObject(
            "Annotation", attributes={"textValue": tag_value})
        assert find_tag is not None
        assert find_tag.getValue() == tag_value

        # find by namespace
        find_tag = gatewaywrapper.gateway.getObject(
            "Annotation", attributes={"ns": find_ns})
        assert find_tag is not None
        assert find_tag.getNs() == find_ns

        # find by text value
        find_tag = gatewaywrapper.gateway.getObject(
            "TagAnnotation", attributes={"textValue": tag_value})
        assert find_tag is not None
        assert find_tag.getValue() == tag_value

        # create some other annotations... (not linked!)
        longAnn = omero.gateway.LongAnnotationWrapper(gatewaywrapper.gateway)
        longAnn.setValue(12345)
        longAnn.save()
        longId = longAnn.getId()
        boolAnn = omero.gateway.BooleanAnnotationWrapper(
            gatewaywrapper.gateway)
        boolAnn.setValue(True)
        boolAnn.save()
        boolId = boolAnn.getId()
        commAnn = omero.gateway.CommentAnnotationWrapper(
            gatewaywrapper.gateway)
        commAnn.setValue("This is a blitz gatewaytest Comment.")
        commAnn.save()
        commId = commAnn.getId()
        fileAnn = omero.gateway.FileAnnotationWrapper(gatewaywrapper.gateway)
        # An original file object needs to be linked to the annotation or it
        # will fail to be loaded on getObject(s).
        fileObj = omero.model.OriginalFileI()
        fileObj = omero.gateway.OriginalFileWrapper(
            gatewaywrapper.gateway, fileObj)
        fileObj.setName(omero.rtypes.rstring('a'))
        fileObj.setPath(omero.rtypes.rstring('a'))
        fileObj.setHash(omero.rtypes.rstring('a'))
        fileObj.setSize(omero.rtypes.rlong(0))
        fileObj.save()
        fileAnn.setFile(fileObj)
        fileAnn.save()
        fileId = fileAnn.getId()
        doubleAnn = omero.gateway.DoubleAnnotationWrapper(
            gatewaywrapper.gateway)
        doubleAnn.setValue(1.23456)
        doubleAnn.save()
        doubleId = doubleAnn.getId()
        termAnn = omero.gateway.TermAnnotationWrapper(gatewaywrapper.gateway)
        termAnn.setValue("Metaphase")
        termAnn.save()
        termId = termAnn.getId()
        timeAnn = omero.gateway.TimestampAnnotationWrapper(
            gatewaywrapper.gateway)
        timeAnn.setValue(1000)
        timeAnn.save()
        timeId = timeAnn.getId()

        # list annotations of various types - check they include ones from
        # above
        tags = list(gatewaywrapper.gateway.getObjects("TagAnnotation"))
        for t in tags:
            assert t.OMERO_TYPE == tag.OMERO_TYPE
        assert tagId in [t.getId() for t in tags]
        longs = list(gatewaywrapper.gateway.getObjects("LongAnnotation"))
        for l in longs:
            assert l.OMERO_TYPE == longAnn.OMERO_TYPE
        assert longId in [l.getId() for l in longs]
        bools = list(gatewaywrapper.gateway.getObjects("BooleanAnnotation"))
        for b in bools:
            assert b.OMERO_TYPE == boolAnn.OMERO_TYPE
        assert boolId in [b.getId() for b in bools]
        comms = list(gatewaywrapper.gateway.getObjects("CommentAnnotation"))
        for c in comms:
            assert c.OMERO_TYPE == commAnn.OMERO_TYPE
        assert commId in [c.getId() for c in comms]
        files = list(gatewaywrapper.gateway.getObjects("FileAnnotation"))
        for f in files:
            assert f.OMERO_TYPE == fileAnn.OMERO_TYPE
        assert fileId in [f.getId() for f in files]
        doubles = list(gatewaywrapper.gateway.getObjects("DoubleAnnotation"))
        for d in doubles:
            assert d.OMERO_TYPE == doubleAnn.OMERO_TYPE
        assert doubleId in [d.getId() for d in doubles]
        terms = list(gatewaywrapper.gateway.getObjects("TermAnnotation"))
        for t in terms:
            assert t.OMERO_TYPE == termAnn.OMERO_TYPE
        assert termId in [t.getId() for t in terms]
        times = list(gatewaywrapper.gateway.getObjects("TimestampAnnotation"))
        for t in times:
            assert t.OMERO_TYPE == timeAnn.OMERO_TYPE
        assert timeId in [t.getId() for t in times]

        # delete what we created
        gatewaywrapper.gateway.deleteObjects(
            "Annotation", [longId, boolId, fileId, commId, tagId], wait=True)
        assert gatewaywrapper.gateway.getObject("Annotation", longId) is None
        assert gatewaywrapper.gateway.getObject("Annotation", boolId) is None
        assert gatewaywrapper.gateway.getObject("Annotation", fileId) is None
        assert gatewaywrapper.gateway.getObject("Annotation", commId) is None
        assert gatewaywrapper.gateway.getObject("Annotation", tagId) is None


class TestGetObject (object):

    def testSearchObjects(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # search for Projects
        pros = list(gatewaywrapper.gateway.searchObjects(
            ["Project"], "weblitz"))
        for p in pros:
            # assert p.getId() in projectIds
            assert p.OMERO_CLASS == "Project", "Should only return Projects"

        # P/D/I is default objects to search
        # pdis = list( gatewaywrapper.gateway.simpleSearch("weblitz") )   #
        # method removed from blitz gateway
        # pdis.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        pdiResult = list(gatewaywrapper.gateway.searchObjects(
            None, "weblitz"))
        pdiResult.sort(key=lambda r: "%s%s" % (r.OMERO_CLASS, r.getId()))
        # can directly check that sorted lists are the same
        # for r1, r2 in zip(pdis, pdiResult):
        #    assert r1.OMERO_CLASS ==  r2.OMERO_CLASS
        #    assert r1.getId() ==  r2.getId()

    def testListProjects(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # params limit query by owner
        params = omero.sys.Parameters()
        params.theFilter = omero.sys.Filter()
        conn = gatewaywrapper.gateway

        # should be no Projects owned by root (in the current group)
        params.theFilter.ownerId = omero.rtypes.rlong(0)  # owned by 'root'
        pros = conn.getObjects("Project", None, params)
        assert len(list(pros)) == 0, "Should be no Projects owned by root"

        # Also filter by owner using opts dict
        pros = conn.getObjects("Project", None, opts={'owner': 0})
        assert len(list(pros)) == 0, "Should be no Projects owned by root"

        # filter by current user should get same as above. # owned by 'author'
        params.theFilter.ownerId = omero.rtypes.rlong(
            conn.getEventContext().userId)
        pros = list(conn.getObjects(
            "Project", None, params))
        projects = list(conn.listProjects())
        # check unordered lists are the same length & ids
        assert len(pros) == len(projects)
        projectIds = [p.getId() for p in projects]
        for p in pros:
            assert p.getId() in projectIds

    def testPagination(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        params = omero.sys.ParametersI()
        # Only 3 images available
        limit = 2
        params.page(0, limit)
        pros = list(gatewaywrapper.gateway.getObjects(
            "Project", None, params))
        assert len(pros) == limit

        # Also using opts dict
        pros = list(gatewaywrapper.gateway.getObjects(
            "Project", None, opts={'offset': 0, 'limit': 2}))
        assert len(pros) == limit

    def testGetDatasetsByProject(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        allDs = list(gatewaywrapper.gateway.getObjects("Dataset"))

        # Get Datasets by project.listChildren()...
        project = gatewaywrapper.getTestProject()
        dsIds = [d.id for d in project.listChildren()]

        # Get Datasets, filtering by project
        p = {'project': project.id}
        datasets = list(gatewaywrapper.gateway.getObjects("Dataset", opts=p))

        # Check that not all Datasets are in Project (or test is invalid)
        assert len(allDs) > len(dsIds)
        # Should get same result both methods
        assert len(datasets) == len(dsIds)
        for d in datasets:
            assert d.id in dsIds

    def testListExperimentersAndGroups(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # experimenters
        # experimenters = list( gatewaywrapper.gateway.listExperimenters() ) #
        # removed from blitz gateway
        # all experimenters
        exps = list(gatewaywrapper.gateway.getObjects("Experimenter"))

        # self.assertEqual(len(exps), len(experimenters))  # check unordered
        # lists are the same length & ids
        # eIds = [e.getId() for e in experimenters]
        for e in exps:
            # assert e.getId() in eIds
            # check iQuery has loaded at least one group
            loaded = False
            for groupExpMap in e.copyGroupExperimenterMap():
                if groupExpMap is not None and \
                        e.id == groupExpMap.child.id.val:
                    loaded = True
            assert loaded

        # returns all experimenters except current user - now moved to
        # webclient_gateway
        # allBarOne = list( gatewaywrapper.gateway.getExperimenters() )
        # assert len(allBarOne)+1 ==  len(exps)
        # for e in allBarOne:
        #    assert e.getId() in eIds

        # groups
        # groups = list( gatewaywrapper.gateway.listGroups() )
        # now removed from blitz gateway.
        gps = list(gatewaywrapper.gateway.getObjects("ExperimenterGroup"))
        for grp in gps:
            grp.copyGroupExperimenterMap()
        # self.assertEqual(len(gps), len(groups))  # check unordered lists are
        # the same length & ids
        # gIds = [g.getId() for g in gps]
        # for g in groups:
        #    assert g.getId() in gIds

        # uses gateway.getObjects("ExperimenterGroup") - check this doesn't
        # throw
        colleagues = gatewaywrapper.gateway.listColleagues()
        for e in colleagues:
            e.getOmeName()

        # check we can find some groups
        exp = gatewaywrapper.gateway.getObject(
            "Experimenter", attributes={'omeName': gatewaywrapper.USER.name})
        for groupExpMap in exp.copyGroupExperimenterMap():
            gName = groupExpMap.parent.name.val
            gId = groupExpMap.parent.id.val
            findG = gatewaywrapper.gateway.getObject(
                "ExperimenterGroup", attributes={'name': gName})
            assert gId == findG.id, "Check we found the same group"

    def testGetExperimenter(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        noExp = gatewaywrapper.gateway.getObject(
            "Experimenter", attributes={'omeName': "Dummy Fake Name"})
        assert noExp is None, "Should not find any matching experimenter"

        findExp = gatewaywrapper.gateway.getObject(
            "Experimenter", attributes={'omeName': gatewaywrapper.USER.name})
        exp = gatewaywrapper.gateway.getObject(
            "Experimenter", findExp.id)  # uses iQuery
        assert exp.omeName == findExp.omeName

        # check groupExperimenterMap loaded for exp
        groupIds = []
        for groupExpMap in exp.copyGroupExperimenterMap():
            assert findExp.id == groupExpMap.child.id.val
            groupIds.append(groupExpMap.parent.id.val)
        # for groupExpMap in experimenter.copyGroupExperimenterMap():
        #    assert findExp.id ==  groupExpMap.child.id.val

        groupGen = gatewaywrapper.gateway.getObjects(
            "ExperimenterGroup", groupIds)
        # gGen = gatewaywrapper.gateway.getExperimenterGroups(groupIds)  #
        # removed from blitz gateway
        groups = list(groupGen)
        # gs = list(gGen)
        assert len(groups) == len(groupIds)
        for g in groups:
            assert g.getId() in groupIds
            for m in g.copyGroupExperimenterMap():  # check exps are loaded
                assert m.child

    def testGetAnnotations(self, gatewaywrapper, author_testimg_tiny):
        obj = author_testimg_tiny
        dataset = gatewaywrapper.getTestDataset()
        ns = "omero.gateway.test.get_objects.test_get_annotations_comment"
        ns_tag = "omero.gateway.test.get_objects.test_get_annotations_tag"

        # create Comment
        ann = omero.gateway.CommentAnnotationWrapper(gatewaywrapper.gateway)
        ann.setNs(ns)
        ann.setValue("Test Comment")
        ann = obj.linkAnnotation(ann)
        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(gatewaywrapper.gateway)
        tag.setNs(ns_tag)
        tag.setValue("Test Tag")
        tag = obj.linkAnnotation(tag)
        dataset.linkAnnotation(tag)

        # get the Comment
        annotation = gatewaywrapper.gateway.getObject(
            "CommentAnnotation", ann.id)
        assert "Test Comment" == annotation.textValue
        assert ann.OMERO_TYPE == annotation.OMERO_TYPE

        # test getObject throws exception if more than 1 returned
        threw = True
        try:
            gatewaywrapper.gateway.getObject("Annotation")
            threw = False
        except:
            threw = True
        assert threw, "getObject() didn't throw exception with >1 result"

        # get the Comment and Tag
        annGen = gatewaywrapper.gateway.getObjects(
            "Annotation", [ann.id, tag.id])
        anns = list(annGen)
        assert len(anns) == 2
        assert anns[0].ns in [ns, ns_tag]
        assert anns[1].ns in [ns, ns_tag]
        assert anns[0].OMERO_TYPE != anns[1].OMERO_TYPE

        # get all available annotation links on the image
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image")
        for al in annLinks:
            assert isinstance(al.getAnnotation(),
                              omero.gateway.AnnotationWrapper)
            assert al.parent.__class__ == omero.model.ImageI

        # get selected links - On image only
        annLinks = gatewaywrapper.gateway.getAnnotationLinks(
            "Image", parent_ids=[obj.getId()])
        for al in annLinks:
            assert obj.getId() == al.parent.id.val
            assert al.parent.__class__ == omero.model.ImageI

        # get selected links - On image only
        annLinks = gatewaywrapper.gateway.getAnnotationLinks(
            "Image", parent_ids=[obj.getId()])
        for al in annLinks:
            assert obj.getId() == al.parent.id.val
            assert al.parent.__class__ == omero.model.ImageI

        # compare with getObjectsByAnnotations
        annImages = list(gatewaywrapper.gateway.getObjectsByAnnotations(
            'Image', [tag.getId()]))
        assert obj.getId() in [i.getId() for i in annImages]

        # params limit query by owner
        params = omero.sys.Parameters()
        params.theFilter = omero.sys.Filter()
        # should be no links owned by root (in the current group)
        params.theFilter.ownerId = omero.rtypes.rlong(0)  # owned by 'root'
        annLinks = gatewaywrapper.gateway.getAnnotationLinks(
            "Image", parent_ids=[obj.getId()], params=params)
        assert len(list(annLinks)) == 0, \
            "No annotations on this image by root"
        # links owned by author
        eid = gatewaywrapper.gateway.getEventContext().userId
        params.theFilter.ownerId = omero.rtypes.rlong(eid)  # owned by 'author'
        omeName = gatewaywrapper.gateway.getObject(
            "Experimenter", eid).getName()
        annLinks = gatewaywrapper.gateway.getAnnotationLinks(
            "Image", parent_ids=[obj.getId()], params=params)
        for al in annLinks:
            assert al.getOwnerOmeName() == omeName

        # all links on Image with specific ns
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image", ns=ns)
        for al in annLinks:
            assert al.getAnnotation().ns == ns

        # get all uses of the Tag - have to check various types separately
        annList = list(gatewaywrapper.gateway.getAnnotationLinks(
            "Image", ann_ids=[tag.id]))
        assert len(annList) == 1
        for al in annList:
            assert al.getAnnotation().id == tag.id
        annList = list(gatewaywrapper.gateway.getAnnotationLinks(
            "Dataset", ann_ids=[tag.id]))
        assert len(annList) == 1
        for al in annList:
            assert al.getAnnotation().id == tag.id

        # remove annotations
        obj.removeAnnotations(ns)
        dataset.unlinkAnnotations(ns_tag)  # unlink tag
        obj.removeAnnotations(ns_tag)      # delete tag

    def testGetImage(self, gatewaywrapper, author_testimg_tiny):
        testImage = author_testimg_tiny
        # This should return image wrapper
        image = gatewaywrapper.gateway.getObject("Image", testImage.id)

        # test a few methods that involve lazy loading, rendering etc.
        assert image.getSizeZ() == testImage.getSizeZ()
        assert image.getSizeY() == testImage.getSizeY()
        image.isGreyscaleRenderingModel()       # loads rendering engine
        testImage.isGreyscaleRenderingModel()
        assert image._re.getDefaultZ() == testImage._re.getDefaultZ()
        assert image._re.getDefaultT() == testImage._re.getDefaultT()
        assert image.getOwnerOmeName == testImage.getOwnerOmeName
        assert image.getThumbVersion() is not None

    @pytest.mark.parametrize("load_pixels", [True, False])
    @pytest.mark.parametrize("load_channels", [True, False])
    def testGetImageLoadPixels(self, load_pixels, load_channels,
                               gatewaywrapper, author_testimg_tiny):
        testImage = author_testimg_tiny
        conn = gatewaywrapper.gateway
        # By default (no opts), don't load pixels
        image = conn.getObject("Image", testImage.id)
        assert not image._obj.isPixelsLoaded()

        # parametrized opts...
        opts = {'load_pixels': load_pixels, 'load_channels': load_channels}
        image = conn.getObject("Image", testImage.id, opts=opts)
        # pixels are also loaded if load_channels
        pix_loaded = load_pixels or load_channels
        assert image._obj.isPixelsLoaded() == pix_loaded
        if pix_loaded:
            pixels = image._obj._pixelsSeq[0]
            assert pixels.getPixelsType().isLoaded()
            if load_channels:
                assert pixels.isChannelsLoaded()
                for c in pixels.copyChannels():
                    lc = c.getLogicalChannel()
                    assert lc.getPhotometricInterpretation().isLoaded()
            else:
                assert not pixels.isChannelsLoaded()

    def testGetProject(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        testProj = gatewaywrapper.getTestProject()
        p = gatewaywrapper.gateway.getObject("Project", testProj.getId())
        assert testProj.getName() == p.getName()
        assert testProj.getDescription() == p.getDescription()
        assert testProj.getId() == p.getId()
        assert testProj.OMERO_CLASS == p.OMERO_CLASS
        assert testProj.countChildren_cached() == p.countChildren_cached()
        assert testProj.getOwnerOmeName == p.getOwnerOmeName

    def testTraversal(self, author_testimg_tiny):
        image = author_testimg_tiny
        # This should return image wrapper
        pr = image.getProject()
        ds = image.getParent()

        assert image.listParents()[0] == image.getParent()
        assert ds == image.getParent(withlinks=True)[0]
        assert image.getParent(withlinks=True) == \
            image.listParents(withlinks=True)[0]
        assert ds.getParent() == pr
        assert pr.getParent() is None
        assert len(pr.listParents()) == 0

    @pytest.mark.parametrize("orphaned", [True, False])
    @pytest.mark.parametrize("load_pixels", [False, False])
    def testListOrphans(self, orphaned, load_pixels, gatewaywrapper):
        # We login as 'User', since they have no other orphaned images
        gatewaywrapper.loginAsUser()
        conn = gatewaywrapper.gateway
        eid = conn.getUserId()

        # Create 5 orphaned images
        iids = []
        for i in range(0, 5):
            img = gatewaywrapper.createTestImage(imageName=str(uuid.uuid1()))
            iids.append(img.id)
        # Create image in Dataset, to check this isn't found
        dataset = DatasetI()
        dataset.name = wrap('testListOrphans')
        image = ImageI()
        image.name = wrap('testListOrphans')
        dataset.linkImage(image)
        dataset = conn.getUpdateService().saveAndReturnObject(dataset)

        try:
            # Only test listOrphans() if orphaned
            if orphaned:
                # Pagination
                params = omero.sys.ParametersI()
                params.page(1, 3)
                findImagesInPage = list(conn.listOrphans("Image", eid=eid,
                                                         params=params))
                assert len(findImagesInPage) == 3

                # No pagination (all orphans)
                findImages = list(conn.listOrphans("Image",
                                                   loadPixels=load_pixels))
                assert len(findImages) == 5
                for p in findImages:
                    assert p._obj.pixelsLoaded == load_pixels

            # Test getObjects() with 'orphaned' option
            opts = {'orphaned': orphaned, 'load_pixels': load_pixels}
            getImages = list(conn.getObjects("Image", opts=opts))
            assert orphaned == (len(getImages) == 5)
            for p in getImages:
                assert p._obj.pixelsLoaded == load_pixels

            # Simply check this doesn't fail See https://github.com/
            # openmicroscopy/openmicroscopy/pull/4950#issuecomment-264142956
            dsIds = [d.id for d in conn.listOrphans("Dataset")]
            assert dataset.id.val in dsIds
        finally:
            # Cleanup - Delete what we created
            conn.deleteObjects('Image', iids, deleteAnns=True, wait=True)
            conn.deleteObjects('Dataset', [dataset.id.val],
                               deleteChildren=True, wait=True)

    def testOrderById(self, gatewaywrapper):
        gatewaywrapper.loginAsUser()
        imageIds = list()
        for i in range(0, 3):
            iid = gatewaywrapper.createTestImage(
                "%s-testOrderById" % i).getId()
            imageIds.append(iid)

        images = gatewaywrapper.gateway.getObjects(
            "Image", imageIds, respect_order=True)
        resultIds = [i.id for i in images]
        assert imageIds == resultIds, "Images not ordered by ID"
        imageIds.reverse()
        reverseImages = gatewaywrapper.gateway.getObjects(
            "Image", imageIds, respect_order=True)
        reverseIds = [i.id for i in reverseImages]
        assert imageIds == reverseIds, "Images not ordered by ID"
        wrappedIds = [wrap(i) for i in imageIds]
        reverseImages = gatewaywrapper.gateway.getObjects(
            "Image", wrappedIds, respect_order=True)
        reverseIds = [i.id for i in reverseImages]
        assert imageIds == reverseIds, "fails when IDs is list of rlongs"
        invalidIds = imageIds[:]
        invalidIds[1] = 0
        reverseImages = gatewaywrapper.gateway.getObjects(
            "Image", invalidIds, respect_order=True)
        reverseIds = [i.id for i in reverseImages]
        assert len(imageIds) - 1 == len(reverseIds), \
            "One image not found by ID: 0"

        # Delete to clean up
        handle = gatewaywrapper.gateway.deleteObjects(
            'Image', imageIds, deleteAnns=True)
        try:
            gatewaywrapper.gateway._waitOnCmd(handle)
        finally:
            handle.close()


class TestLeaderAndMemberOfGroup(object):

    @pytest.fixture(autouse=True)
    def setUp(self):
        """ Create a group with owner & member"""
        dbhelpers.USERS['group_owner'] = dbhelpers.UserEntry(
            'group_owner', 'ome',
            firstname='Group',
            lastname='Owner',
            groupname="ownership_test",
            groupperms='rwr---',
            groupowner=True)
        dbhelpers.USERS['group_member'] = dbhelpers.UserEntry(
            'group_member', 'ome',
            firstname='Group',
            lastname='Member',
            groupname="ownership_test",
            groupperms='rwr---',
            groupowner=False)
        dbhelpers.bootstrap(onlyUsers=True)

    def testGetGroupsLeaderOfAsLeader(self, gatewaywrapper):
        gatewaywrapper.doLogin(dbhelpers.USERS['group_owner'])
        assert gatewaywrapper.gateway.isLeader()
        grs = [g.id for g in gatewaywrapper.gateway.getGroupsLeaderOf()]
        assert len(grs) > 0
        exp = gatewaywrapper.gateway.getObject(
            "Experimenter", attributes={'omeName': 'group_owner'})
        assert exp.sizeOfGroupExperimenterMap() > 0
        filter_system_groups = [gatewaywrapper.gateway.getAdminService()
                                .getSecurityRoles().userGroupId]
        leaderOf = list()
        for groupExpMap in exp.copyGroupExperimenterMap():
            gId = groupExpMap.parent.id.val
            if groupExpMap.owner.val and gId not in filter_system_groups:
                leaderOf.append(gId)
        assert(leaderOf == grs)

    def testGetGroupsLeaderOfAsMember(self, gatewaywrapper):
        gatewaywrapper.doLogin(dbhelpers.USERS['group_member'])
        assert not gatewaywrapper.gateway.isLeader()
        with pytest.raises(StopIteration):
            gatewaywrapper.gateway.getGroupsLeaderOf().next()

    def testGetGroupsMemberOf(self, gatewaywrapper):
        gatewaywrapper.doLogin(dbhelpers.USERS['group_member'])
        assert not gatewaywrapper.gateway.isLeader()
        grs = [g.id for g in gatewaywrapper.gateway.getGroupsMemberOf()]
        assert len(grs) > 0
        exp = gatewaywrapper.gateway.getObject(
            "Experimenter", attributes={'omeName': "group_member"})
        assert exp.sizeOfGroupExperimenterMap() > 0
        filter_system_groups = [gatewaywrapper.gateway.getAdminService()
                                .getSecurityRoles().userGroupId]
        memberOf = list()
        for groupExpMap in exp.copyGroupExperimenterMap():
            gId = groupExpMap.parent.id.val
            if not groupExpMap.owner.val and gId not in filter_system_groups:
                memberOf.append(gId)
        assert memberOf == grs

    def testGroupSummaryAsOwner(self, gatewaywrapper):
        gatewaywrapper.doLogin(dbhelpers.USERS['group_owner'])

        expGr = gatewaywrapper.gateway.getObject(
            "ExperimenterGroup", attributes={'name': 'ownership_test'})

        leaders, colleagues = expGr.groupSummary()
        assert len(leaders) == 1
        assert len(colleagues) == 1
        assert leaders[0].omeName == "group_owner"
        assert colleagues[0].omeName == "group_member"

        leaders, colleagues = expGr.groupSummary(exclude_self=True)
        assert len(leaders) == 0
        assert len(colleagues) == 1
        assert colleagues[0].omeName == "group_member"

    def testGroupSummaryAsMember(self, gatewaywrapper):
        gatewaywrapper.doLogin(dbhelpers.USERS['group_member'])

        expGr = gatewaywrapper.gateway.getObject(
            "ExperimenterGroup", attributes={'name': 'ownership_test'})

        leaders, colleagues = expGr.groupSummary()
        assert len(leaders) == 1
        assert len(colleagues) == 1
        assert leaders[0].omeName == "group_owner"
        assert colleagues[0].omeName == "group_member"

        leaders, colleagues = expGr.groupSummary(exclude_self=True)
        assert len(leaders) == 1
        assert leaders[0].omeName == "group_owner"
        assert len(colleagues) == 0

    def testGroupSummaryAsOwnerDeprecated(self, gatewaywrapper):
        gatewaywrapper.doLogin(dbhelpers.USERS['group_owner'])

        summary = gatewaywrapper.gateway.groupSummary()
        assert len(summary["leaders"]) == 1
        assert len(summary["colleagues"]) == 1
        assert summary["leaders"][0].omeName == "group_owner"
        assert summary["colleagues"][0].omeName == "group_member"

        summary = gatewaywrapper.gateway.groupSummary(exclude_self=True)
        assert len(summary["leaders"]) == 0
        assert len(summary["colleagues"]) == 1
        assert summary["colleagues"][0].omeName == "group_member"

    def testGroupSummaryAsMemberDeprecated(self, gatewaywrapper):
        gatewaywrapper.doLogin(dbhelpers.USERS['group_member'])

        summary = gatewaywrapper.gateway.groupSummary()
        assert len(summary["leaders"]) == 1
        assert len(summary["colleagues"]) == 1
        assert summary["leaders"][0].omeName == "group_owner"
        assert summary["colleagues"][0].omeName == "group_member"

        summary = gatewaywrapper.gateway.groupSummary(exclude_self=True)
        assert len(summary["leaders"]) == 1
        assert summary["leaders"][0].omeName == "group_owner"
        assert len(summary["colleagues"]) == 0


class TestListParents(ITest):

    def testSupportedObjects(self):
        """
        Check that we are testing all objects where listParents() is supported.

        If this test fails, need to update tested_wrappers and add
        corresponding tests below
        """
        tested_wrappers = ['plate', 'image', 'dataset', 'experimenter', 'well']
        for key, wrapper in KNOWN_WRAPPERS.items():
            if (hasattr(wrapper, 'PARENT_WRAPPER_CLASS') and
                    wrapper.PARENT_WRAPPER_CLASS is not None):
                assert key in tested_wrappers

    def testListParentsPDI(self):
        """Test listParents() for Image in Dataset"""

        # Set up PDI
        client, exp = self.new_client_and_user()
        p = self.make_project(name="ListParents Test", client=client)
        d = self.make_dataset(name="ListParents Test", client=client)
        i = self.make_image(name="ListParents Test", client=client)
        self.link(p, d, client=client)
        self.link(d, i, client=client)

        conn = BlitzGateway(client_obj=client)
        image = conn.getObject("Image", i.id.val)

        # Traverse from Image -> Project
        dataset = image.listParents()[0]
        assert dataset.id == d.id.val

        project = dataset.listParents()[0]
        assert project.id == p.id.val
        # Project has no parent
        assert len(project.listParents()) == 0

    def testListParentsSPW(self):
        """Test listParents() for Image in WellSample"""

        client, exp = self.new_client_and_user()
        conn = BlitzGateway(client_obj=client)

        # setup SPW-WS-Img...
        s = ScreenI()
        s.name = wrap('ScreenA')
        p = PlateI()
        p.name = wrap('PlateA')
        s.linkPlate(p)
        w = WellI()
        w.column = wrap(0)
        w.row = wrap(0)
        p.addWell(w)
        s = client.sf.getUpdateService().saveAndReturnObject(s)
        p = s.linkedPlateList()[0]
        w = p.copyWells()[0]
        i = self.make_image(name="SPW listParents", client=client)
        ws = WellSampleI()
        ws.image = i
        ws.well = WellI(w.id.val, False)
        w.addWellSample(ws)
        ws = client.sf.getUpdateService().saveAndReturnObject(ws)

        # Traverse from Image -> Screen
        image = conn.getObject("Image", i.id.val)
        wellSample = image.listParents()[0]

        well = wellSample.listParents()[0]
        assert well.id == w.id.val

        plate = well.listParents()[0]
        assert plate.id == p.id.val

        screen = plate.listParents()[0]
        assert screen.id == s.id.val
        # Screen has no parent
        assert len(screen.listParents()) == 0

    def testExperimenterListParents(self):
        """Test listParents() for Experimenter in ExperimenterGroup."""

        client, exp = self.new_client_and_user()
        conn = BlitzGateway(client_obj=client)

        userGroupId = conn.getAdminService().getSecurityRoles().userGroupId
        exp = conn.getUser()
        groups = exp.listParents()
        assert len(groups) == 2
        gIds = [g.id for g in groups]
        assert userGroupId in gIds

        # ExperimenterGroup has no parent
        assert len(groups[0].listParents()) == 0
