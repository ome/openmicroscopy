#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Testing the gateway.getObject() and searchObjects() methods

   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_generated
   - author_testimg_tiny

"""

import omero
import time


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
        assert None == gateway.getObject("Annotation", tagId)

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
        assert gatewaywrapper.gateway.getObject("Annotation", ann.id) != None
        assert gatewaywrapper.gateway.getObject("Annotation", tag.id) != None

        # check Image, delete (wait) and check
        assert gatewaywrapper.gateway.getObject("Image", imageId) != None
        handle = gatewaywrapper.gateway.deleteObjects("Image", [imageId])
        gatewaywrapper.gateway._waitOnCmd(handle)
        assert gatewaywrapper.gateway.getObject("Image", imageId) == None

        # Comment should be deleted but not the Tag (becomes orphan)
        assert gatewaywrapper.gateway.getObject("Annotation", ann.id) == None
        assert gatewaywrapper.gateway.getObject("Annotation", tag.id) != None

        # Add the tag to project and delete (with Tags)
        assert gatewaywrapper.gateway.getObject("Project", projectId) != None
        project.linkAnnotation(tag)
        datasetIds = [d.getId() for d in project.listChildren()]
        assert len(datasetIds) > 0
        handle = gatewaywrapper.gateway.deleteObjects("Project", [projectId], deleteAnns=True, deleteChildren=True)
        gatewaywrapper.gateway._waitOnCmd(handle)
        assert gatewaywrapper.gateway.getObject("Project", projectId) == None
        assert gatewaywrapper.gateway.getObject("Annotation", tag.id) is None # Tag should be gone

        # check datasets gone too
        for dId in datasetIds:
            assert gatewaywrapper.gateway.getObject("Dataset", dId) is None

class TestFindObject (object):

    def testFindProject(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        project = gatewaywrapper.getTestProject()
        pName = project.getName()

        findProjects = list (gatewaywrapper.gateway.getObjects("Project", None, attributes={"name":pName}) )
        assert len(findProjects) > 0, "Did not find Project by name"
        for p in findProjects:
            assert p.getName() ==  pName, "All projects should have queried name"


    def testFindExperimenter(self, gatewaywrapper, author_testimg_tiny):
        omeName = author_testimg_tiny.getOwnerOmeName()
        group = author_testimg_tiny.getDetails().getGroup()
        groupName = group.getName()
        gatewaywrapper.loginAsAdmin()

        # findObjects
        findAuthor = list (gatewaywrapper.gateway.getObjects("Experimenter", None, attributes={"omeName":omeName}) )
        assert len(findAuthor) == 1, "Did not find Experimenter by omeName"
        assert findAuthor[0].omeName ==  omeName

        # findObject
        author = gatewaywrapper.gateway.getObject("Experimenter", None, attributes={"omeName":omeName})
        assert author != None
        assert author.omeName ==  omeName

        # find group
        grp = gatewaywrapper.gateway.getObject("ExperimenterGroup", None, attributes={"name":groupName})
        assert grp != None
        assert grp.getName() ==  groupName

    def testFindAnnotation(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # start by deleting any tag created by this method that may have been left behind
        tag_value = "FindThisTag"
        find_ns = "omero.gateway.test.test_find_annotations"
        find_tag = gatewaywrapper.gateway.getObjects("Annotation", attributes={"textValue":tag_value,
                                                                     "ns":find_ns})
        for t in find_tag:
            gatewaywrapper.gateway.deleteObjectDirect(t._obj)
        
        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(gatewaywrapper.gateway)
        tag.setNs(find_ns)
        tag.setValue(tag_value)
        tag.save()
        tagId = tag.getId()

        # findObject by name
        find_tag = gatewaywrapper.gateway.getObject("Annotation", attributes={"textValue":tag_value})
        assert find_tag != None
        assert find_tag.getValue() ==  tag_value

        # find by namespace
        find_tag = gatewaywrapper.gateway.getObject("Annotation", attributes={"ns":find_ns})
        assert find_tag != None
        assert find_tag.getNs() ==  find_ns

        # find by text value
        find_tag = gatewaywrapper.gateway.getObject("TagAnnotation", attributes={"textValue":tag_value})
        assert find_tag != None
        assert find_tag.getValue() ==  tag_value

        # create some other annotations... (not linked!)
        longAnn = omero.gateway.LongAnnotationWrapper(gatewaywrapper.gateway)
        longAnn.setValue(12345)
        longAnn.save()
        longId = longAnn.getId()
        boolAnn = omero.gateway.BooleanAnnotationWrapper(gatewaywrapper.gateway)
        boolAnn.setValue(True)
        boolAnn.save()
        boolId = boolAnn.getId()
        commAnn = omero.gateway.CommentAnnotationWrapper(gatewaywrapper.gateway)
        commAnn.setValue("This is a blitz gatewaytest Comment.")
        commAnn.save()
        commId = commAnn.getId()
        fileAnn = omero.gateway.FileAnnotationWrapper(gatewaywrapper.gateway)
        # An original file object needs to be linked to the annotation or it will
        # fail to be loaded on getObject(s).
        fileObj = omero.model.OriginalFileI(False)
        fileObj = omero.gateway.OriginalFileWrapper(gatewaywrapper.gateway, fileObj)
        fileObj.setName(omero.rtypes.rstring('a'))
        fileObj.setPath(omero.rtypes.rstring('a'))
        fileObj._obj.sha1 = omero.rtypes.rstring('a')
        fileObj.setSize(omero.rtypes.rlong(0))
        fileObj.save()
        fileAnn.setFile(fileObj)
        fileAnn.save()
        fileId = fileAnn.getId()
        doubleAnn = omero.gateway.DoubleAnnotationWrapper(gatewaywrapper.gateway)
        doubleAnn.setValue(1.23456)
        doubleAnn.save()
        doubleId = doubleAnn.getId()
        termAnn = omero.gateway.TermAnnotationWrapper(gatewaywrapper.gateway)
        termAnn.setValue("Metaphase")
        termAnn.save()
        termId = termAnn.getId()
        timeAnn = omero.gateway.TimestampAnnotationWrapper(gatewaywrapper.gateway)
        timeAnn.setValue(1000)
        timeAnn.save()
        timeId = timeAnn.getId()

        # list annotations of various types - check they include ones from above
        tags =  list( gatewaywrapper.gateway.getObjects("TagAnnotation") )
        for t in tags:
            assert t.OMERO_TYPE ==  tag.OMERO_TYPE
        assert tagId in [t.getId() for t in tags]
        longs =  list( gatewaywrapper.gateway.getObjects("LongAnnotation") )
        for l in longs:
            assert l.OMERO_TYPE ==  longAnn.OMERO_TYPE
        assert longId in [l.getId() for l in longs]
        bools =  list( gatewaywrapper.gateway.getObjects("BooleanAnnotation") )
        for b in bools:
            assert b.OMERO_TYPE ==  boolAnn.OMERO_TYPE
        assert boolId in [b.getId() for b in bools]
        comms =  list( gatewaywrapper.gateway.getObjects("CommentAnnotation") )
        for c in comms:
            assert c.OMERO_TYPE ==  commAnn.OMERO_TYPE
        assert commId in [c.getId() for c in comms]
        files =  list( gatewaywrapper.gateway.getObjects("FileAnnotation") )
        for f in files:
            assert f.OMERO_TYPE ==  fileAnn.OMERO_TYPE
        assert fileId in [f.getId() for f in files]
        doubles =  list( gatewaywrapper.gateway.getObjects("DoubleAnnotation") )
        for d in doubles:
            assert d.OMERO_TYPE ==  doubleAnn.OMERO_TYPE
        assert doubleId in [d.getId() for d in doubles]
        terms =  list( gatewaywrapper.gateway.getObjects("TermAnnotation") )
        for t in terms:
            assert t.OMERO_TYPE ==  termAnn.OMERO_TYPE
        assert termId in [t.getId() for t in terms]
        times =  list( gatewaywrapper.gateway.getObjects("TimestampAnnotation") )
        for t in times:
            assert t.OMERO_TYPE ==  timeAnn.OMERO_TYPE
        assert timeId in [t.getId() for t in times]

        # delete what we created
        gatewaywrapper.gateway.deleteObjectDirect(longAnn._obj)  # direct delete
        assert  gatewaywrapper.gateway.getObject("Annotation", longId) == None
        gatewaywrapper.gateway.deleteObjectDirect(boolAnn._obj)
        assert  gatewaywrapper.gateway.getObject("Annotation", boolId) == None
        gatewaywrapper.gateway.deleteObjectDirect(fileAnn._obj)
        assert  gatewaywrapper.gateway.getObject("Annotation", fileId) == None
        gatewaywrapper.gateway.deleteObjectDirect(commAnn._obj)
        assert  gatewaywrapper.gateway.getObject("Annotation", commId) == None
        gatewaywrapper.gateway.deleteObjectDirect(tag._obj)
        assert  gatewaywrapper.gateway.getObject("Annotation", tagId) == None


class TestGetObject (object):
    def testSearchObjects(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # search for Projects
        pros = list( gatewaywrapper.gateway.searchObjects(["Project"], "weblitz") )
        for p in pros:
            #assert p.getId() in projectIds
            assert p.OMERO_CLASS ==  "Project", "Should only return Projects"

        # P/D/I is default objects to search
        # pdis = list( gatewaywrapper.gateway.simpleSearch("weblitz") )   # method removed from blitz gateway
        #pdis.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        pdiResult = list( gatewaywrapper.gateway.searchObjects(None, "weblitz") )
        pdiResult.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        # can directly check that sorted lists are the same
        #for r1, r2 in zip(pdis, pdiResult):
        #    assert r1.OMERO_CLASS ==  r2.OMERO_CLASS
        #    assert r1.getId() ==  r2.getId()


    def testListProjects(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # params limit query by owner
        params = omero.sys.Parameters()
        params.theFilter = omero.sys.Filter()
        
        # should be no Projects owned by root (in the current group)
        params.theFilter.ownerId = omero.rtypes.rlong(0) # owned by 'root'
        pros = gatewaywrapper.gateway.getObjects("Project", None, params)
        assert len(list(pros)) ==  0, "Should be no Projects owned by root"
        
        # filter by current user should get same as above.
        params.theFilter.ownerId = omero.rtypes.rlong(gatewaywrapper.gateway.getEventContext().userId) # owned by 'author'
        pros = list( gatewaywrapper.gateway.getObjects("Project", None, params) )
        projects = list( gatewaywrapper.gateway.listProjects() )
        assert len(pros) == len(projects)  # check unordered lists are the same length & ids
        projectIds = [p.getId() for p in projects]
        for p in pros:
            assert p.getId() in projectIds


    def testListExperimentersAndGroups(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # experimenters
        # experimenters = list( gatewaywrapper.gateway.listExperimenters() ) # removed from blitz gateway
        exps = list( gatewaywrapper.gateway.getObjects("Experimenter") )  # all experimenters
        
        #self.assertEqual(len(exps), len(experimenters))  # check unordered lists are the same length & ids
        #eIds = [e.getId() for e in experimenters]
        for e in exps:
            #assert e.getId() in eIds
            for groupExpMap in e.copyGroupExperimenterMap():    # check iQuery has loaded groups
                assert e.id ==  groupExpMap.child.id.val

        # returns all experimenters except current user - now moved to webclient_gateway
        #allBarOne = list( gatewaywrapper.gateway.getExperimenters() )
        #assert len(allBarOne)+1 ==  len(exps)
        #for e in allBarOne:
        #    assert e.getId() in eIds
            
        # groups
        #groups = list( gatewaywrapper.gateway.listGroups() )     # now removed from blitz gateway.
        gps = list( gatewaywrapper.gateway.getObjects("ExperimenterGroup") )
        for grp in gps:
            grpExpMap = grp.copyGroupExperimenterMap()
        #self.assertEqual(len(gps), len(groups))  # check unordered lists are the same length & ids
        #gIds = [g.getId() for g in gps]
        #for g in groups:
        #    assert g.getId() in gIds
        
        # uses gateway.getObjects("ExperimenterGroup") - check this doesn't throw
        colleagues = gatewaywrapper.gateway.listColleagues()
        for e in colleagues:
            cName = e.getOmeName()

        # check we can find some groups
        exp = gatewaywrapper.gateway.getObject("Experimenter", attributes={'omeName': gatewaywrapper.USER.name})
        for groupExpMap in exp.copyGroupExperimenterMap():
            gName = groupExpMap.parent.name.val
            gId = groupExpMap.parent.id.val
            findG = gatewaywrapper.gateway.getObject("ExperimenterGroup", attributes={'name': gName})
            assert gId ==  findG.id, "Check we found the same group"

    def testGetExperimenter(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        noExp = gatewaywrapper.gateway.getObject("Experimenter", attributes={'omeName': "Dummy Fake Name"})
        assert noExp ==  None, "Should not find any matching experimenter"

        findExp = gatewaywrapper.gateway.getObject("Experimenter", attributes={'omeName': gatewaywrapper.USER.name})
        exp = gatewaywrapper.gateway.getObject("Experimenter", findExp.id) # uses iQuery
        assert exp.omeName ==  findExp.omeName
        
        # check groupExperimenterMap loaded for exp
        groupIds = []
        for groupExpMap in exp.copyGroupExperimenterMap():
            assert findExp.id ==  groupExpMap.child.id.val
            groupIds.append(groupExpMap.parent.id.val)
        #for groupExpMap in experimenter.copyGroupExperimenterMap():
        #    assert findExp.id ==  groupExpMap.child.id.val
            
        groupGen = gatewaywrapper.gateway.getObjects("ExperimenterGroup", groupIds)
        #gGen = gatewaywrapper.gateway.getExperimenterGroups(groupIds)  # removed from blitz gateway
        groups = list(groupGen)
        #gs = list(gGen)
        assert len(groups) ==  len(groupIds)
        for g in groups:
            assert g.getId() in groupIds
            for m in g.copyGroupExperimenterMap():  # check exps are loaded
                ex = m.child

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
        annotation = gatewaywrapper.gateway.getObject("CommentAnnotation", ann.id)
        assert "Test Comment" ==  annotation.textValue
        assert ann.OMERO_TYPE ==  annotation.OMERO_TYPE
        
        # test getObject throws exception if more than 1 returned
        threw = True
        try:
            gatewaywrapper.gateway.getObject("Annotation")
            threw = False
        except:
            threw = True
        assert threw, "getObject() didn't throw exception with >1 result"

        # get the Comment and Tag
        annGen = gatewaywrapper.gateway.getObjects("Annotation", [ann.id, tag.id])
        anns = list(annGen)
        assert len(anns) ==  2
        assert anns[0].ns in [ns, ns_tag]
        assert anns[1].ns in [ns, ns_tag]
        assert anns[0].OMERO_TYPE !=  anns[1].OMERO_TYPE
        
        
        # get all available annotation links on the image
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image")
        for al in annLinks:
            assert isinstance(al.getAnnotation(), omero.gateway.AnnotationWrapper)
            assert al.parent.__class__ ==  omero.model.ImageI
            
        # get selected links - On image only
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()])
        for al in annLinks:
            assert obj.getId() ==  al.parent.id.val
            assert al.parent.__class__ == omero.model.ImageI
            
        # get selected links - On image only
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()])
        for al in annLinks:
            assert obj.getId() ==  al.parent.id.val
            assert al.parent.__class__ == omero.model.ImageI

        # compare with getObjectsByAnnotations
        annImages = list( gatewaywrapper.gateway.getObjectsByAnnotations('Image', [tag.getId()]) )
        assert obj.getId() in [i.getId() for i in annImages]
            
        # params limit query by owner
        params = omero.sys.Parameters()
        params.theFilter = omero.sys.Filter()
        # should be no links owned by root (in the current group)
        params.theFilter.ownerId = omero.rtypes.rlong(0) # owned by 'root'
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()], params=params)
        assert len( list(annLinks)) ==  0, "No annotations on this image by root"
        # links owned by author
        eid = gatewaywrapper.gateway.getEventContext().userId
        params.theFilter.ownerId = omero.rtypes.rlong(eid) # owned by 'author'
        omeName = gatewaywrapper.gateway.getObject("Experimenter", eid).getName()
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()], params=params)
        for al in annLinks:
            assert al.getOwnerOmeName() ==  omeName
            
        # all links on Image with specific ns
        annLinks = gatewaywrapper.gateway.getAnnotationLinks("Image", ns=ns)
        for al in annLinks:
            assert al.getAnnotation().ns ==  ns
            
        # get all uses of the Tag - have to check various types separately
        annList = list( gatewaywrapper.gateway.getAnnotationLinks("Image", ann_ids=[tag.id]) )
        assert len(annList) ==  1
        for al in annList:
            assert al.getAnnotation().id ==  tag.id
        annList = list( gatewaywrapper.gateway.getAnnotationLinks("Dataset", ann_ids=[tag.id]) )
        assert len(annList) ==  1
        for al in annList:
            assert al.getAnnotation().id ==  tag.id
            
        # remove annotations
        obj.removeAnnotations(ns)
        dataset.unlinkAnnotations(ns_tag)  # unlink tag
        obj.removeAnnotations(ns_tag)      # delete tag
        
        
    def testGetImage (self, gatewaywrapper, author_testimg_tiny):
        testImage = author_testimg_tiny
        # This should return image wrapper
        image = gatewaywrapper.gateway.getObject("Image", testImage.id)
        pr = image.getProject()
        ds = image.getDataset()
        
        # test a few methods that involve lazy loading, rendering etc. 
        assert image.getSizeZ() ==  testImage.getSizeZ()
        assert image.getSizeY() ==  testImage.getSizeY()
        image.isGreyscaleRenderingModel()       # loads rendering engine
        testImage.isGreyscaleRenderingModel()
        assert image._re.getDefaultZ() ==  testImage._re.getDefaultZ()
        assert image._re.getDefaultT() ==  testImage._re.getDefaultT()
        assert image.getOwnerOmeName ==  testImage.getOwnerOmeName
        
        
    def testGetProject (self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        testProj = gatewaywrapper.getTestProject()
        p = gatewaywrapper.gateway.getObject("Project", testProj.getId())
        assert testProj.getName() ==  p.getName()
        assert testProj.getDescription() ==  p.getDescription()
        assert testProj.getId() ==  p.getId()
        assert testProj.OMERO_CLASS ==  p.OMERO_CLASS
        assert testProj.countChildren_cached() ==  p.countChildren_cached()
        assert testProj.getOwnerOmeName ==  p.getOwnerOmeName

    def testTraversal (self, author_testimg_tiny):
        image = author_testimg_tiny
        # This should return image wrapper
        pr = image.getProject()
        ds = image.getDataset()
        
        assert ds ==  image.getParent()
        assert image.listParents()[0] ==  image.getParent()
        assert ds ==  image.getParent(withlinks=True)[0]
        assert image.getParent(withlinks=True) ==  image.listParents(withlinks=True)[0]
        assert ds.getParent() ==  pr
        assert pr.getParent() ==  None
        assert len(pr.listParents()) ==  0

