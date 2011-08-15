#!/usr/bin/env python

"""
   gateway tests - Testing the gateway.getObject() and searchObjects() methods

"""

import unittest
import omero
import time

import gatewaytest.library as lib

class DeleteObjectTest (lib.GTest):
    
    def setUp (self):
        super(DeleteObjectTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()

    def testDeleteAnnotation(self):

        self.loginAsAuthor()
        image = self.TESTIMG

        # create Tag on Image and try to delete Tag
        tag = omero.gateway.TagAnnotationWrapper(self.gateway)
        ns_tag = "omero.gateway.test.get_objects.test_delete_annotation_tag"
        tag.setNs(ns_tag)
        tag.setValue("Test Delete Tag")
        tag = image.linkAnnotation(tag)
        tagId = tag.getId()


        self.gateway.deleteObjects("Annotation", [tagId])
        time.sleep(5)   # time enough for delete queue

        self.assertEqual(None, self.gateway.getObject("Annotation", tagId))

    def testDeleteImage(self):
        
        self.loginAsAuthor()
        
        image = self.TESTIMG
        imageId = image.getId()
        project = self.getTestProject()
        projectId = project.getId()
        ns = "omero.gateway.test.get_objects.test_delete_image_comment"
        ns_tag = "omero.gateway.test.get_objects.test_delete_image_tag"
        
        # create Comment
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(ns)
        ann.setValue("Test Comment")
        ann = image.linkAnnotation(ann)
        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(self.gateway)
        tag.setNs(ns_tag)
        tag.setValue("Test Tag")
        tag = image.linkAnnotation(tag)
        
        # check the Comment 
        self.assertTrue(self.gateway.getObject("Annotation", ann.id) != None)
        self.assertTrue(self.gateway.getObject("Annotation", tag.id) != None)
        
        # check Image, delete (wait) and check
        self.assertTrue(self.gateway.getObject("Image", imageId) != None)
        self.gateway.deleteObjects("Image", [imageId])
        time.sleep(5)   # time enough for delete queue
        self.assertTrue(self.gateway.getObject("Image", imageId) == None)
        
        # Comment should be deleted but not the Tag (becomes orphan)
        self.assertTrue(self.gateway.getObject("Annotation", ann.id) == None)
        self.assertTrue(self.gateway.getObject("Annotation", tag.id) != None)
        
        # Add the tag to project and delete (with Tags)
        self.assertTrue(self.gateway.getObject("Project", projectId) != None)
        project.linkAnnotation(tag)
        datasetIds = [d.getId() for d in project.listChildren()]
        self.assertTrue(len(datasetIds) > 0)
        self.gateway.deleteObjects("Project", [projectId], deleteAnns=True, deleteChildren=True)
        time.sleep(5)   # time enough for delete queue
        self.assertTrue(self.gateway.getObject("Project", projectId) == None)
        self.assertTrue(self.gateway.getObject("Annotation", tag.id) == None) # Tag should be gone
        # check datasets gone too
        for dId in datasetIds:
            self.assertTrue(self.gateway.getObject("Dataset", dId) == None)


class FindObjectTest (lib.GTest):

    def setUp (self):
        super(FindObjectTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()


    def testFindProject(self):

        self.loginAsAuthor()

        project = self.getTestProject()
        pName = project.getName()

        findProjects = list (self.gateway.getObjects("Project", None, attributes={"name":pName}) )
        self.assertTrue(len(findProjects) > 0, "Did not find Project by name")
        for p in findProjects:
            self.assertEqual(p.getName(), pName, "All projects should have queried name")


    def testFindExperimenter(self):

        self.loginAsAdmin()

        omeName = self.TESTIMG.getOwnerOmeName()
        # findObjects
        findAuthor = list (self.gateway.getObjects("Experimenter", None, attributes={"omeName":omeName}) )
        self.assertTrue(len(findAuthor) == 1, "Did not find Experimenter by omeName")
        self.assertEqual(findAuthor[0].omeName, omeName)

        # findObject
        author = self.gateway.getObject("Experimenter", None, attributes={"omeName":omeName})
        self.assertTrue(author != None)
        self.assertEqual(author.omeName, omeName)

        # find group
        group = self.TESTIMG.getDetails().getGroup()
        groupName = group.getName()
        grp = self.gateway.getObject("ExperimenterGroup", None, attributes={"name":groupName})
        self.assertTrue(grp != None)
        self.assertEqual(grp.getName(), groupName)


    def testFindAnnotation(self):

        # create Tag
        find_ns = "omero.gateway.test.test_find_annotations"
        tag_value = "FindThisTag"
        tag = omero.gateway.TagAnnotationWrapper(self.gateway)
        tag.setNs(find_ns)
        tag.setValue(tag_value)
        tag.save()
        tagId = tag.getId()

        # findObject by name
        find_tag = self.gateway.getObject("Annotation", attributes={"textValue":tag_value})
        self.assertTrue(find_tag != None)
        self.assertEqual(find_tag.getValue(), tag_value)

        # find by namespace
        find_tag = self.gateway.getObject("Annotation", attributes={"ns":find_ns})
        self.assertTrue(find_tag != None)
        self.assertEqual(find_tag.getNs(), find_ns)

        # find by text value
        find_tag = self.gateway.getObject("TagAnnotation", attributes={"textValue":tag_value})
        self.assertTrue(find_tag != None)
        self.assertEqual(find_tag.getValue(), tag_value)

        # create some other annotations... (not linked!)
        longAnn = omero.gateway.LongAnnotationWrapper(self.gateway)
        longAnn.setValue(12345)
        longAnn.save()
        longId = longAnn.getId()
        boolAnn = omero.gateway.BooleanAnnotationWrapper(self.gateway)
        boolAnn.setValue(True)
        boolAnn.save()
        boolId = boolAnn.getId()
        commAnn = omero.gateway.CommentAnnotationWrapper(self.gateway)
        commAnn.setValue("This is a blitz gatewaytest Comment.")
        commAnn.save()
        commId = commAnn.getId()
        fileAnn = omero.gateway.FileAnnotationWrapper(self.gateway)
        fileAnn.save()
        fileId = fileAnn.getId()
        doubleAnn = omero.gateway.DoubleAnnotationWrapper(self.gateway)
        doubleAnn.setValue(1.23456)
        doubleAnn.save()
        doubleId = doubleAnn.getId()
        termAnn = omero.gateway.TermAnnotationWrapper(self.gateway)
        termAnn.setValue("Metaphase")
        termAnn.save()
        termId = termAnn.getId()
        timeAnn = omero.gateway.TimestampAnnotationWrapper(self.gateway)
        timeAnn.setValue(1000)
        timeAnn.save()
        timeId = timeAnn.getId()

        # list annotations of various types - check they include ones from above
        tags =  list( self.gateway.getObjects("TagAnnotation") )
        for t in tags:
            self.assertEqual(t.OMERO_TYPE, tag.OMERO_TYPE)
        self.assertTrue(tagId in [t.getId() for t in tags])
        longs =  list( self.gateway.getObjects("LongAnnotation") )
        for l in longs:
            self.assertEqual(l.OMERO_TYPE, longAnn.OMERO_TYPE)
        self.assertTrue(longId in [l.getId() for l in longs])
        bools =  list( self.gateway.getObjects("BooleanAnnotation") )
        for b in bools:
            self.assertEqual(b.OMERO_TYPE, boolAnn.OMERO_TYPE)
        self.assertTrue(boolId in [b.getId() for b in bools])
        comms =  list( self.gateway.getObjects("CommentAnnotation") )
        for c in comms:
            self.assertEqual(c.OMERO_TYPE, commAnn.OMERO_TYPE)
        self.assertTrue(commId in [c.getId() for c in comms])
        files =  list( self.gateway.getObjects("FileAnnotation") )
        for f in files:
            self.assertEqual(f.OMERO_TYPE, fileAnn.OMERO_TYPE)
        self.assertTrue(fileId in [f.getId() for f in files])
        doubles =  list( self.gateway.getObjects("DoubleAnnotation") )
        for d in doubles:
            self.assertEqual(d.OMERO_TYPE, doubleAnn.OMERO_TYPE)
        self.assertTrue(doubleId in [d.getId() for d in doubles])
        terms =  list( self.gateway.getObjects("TermAnnotation") )
        for t in terms:
            self.assertEqual(t.OMERO_TYPE, termAnn.OMERO_TYPE)
        self.assertTrue(termId in [t.getId() for t in terms])
        times =  list( self.gateway.getObjects("TimestampAnnotation") )
        for t in times:
            self.assertEqual(t.OMERO_TYPE, timeAnn.OMERO_TYPE)
        self.assertTrue(timeId in [t.getId() for t in times])

        # delete what we created
        self.gateway.deleteObjectDirect(longAnn._obj)  # direct delete
        self.assertTrue( self.gateway.getObject("Annotation", longId) == None)
        self.gateway.deleteObjectDirect(boolAnn._obj)
        self.assertTrue( self.gateway.getObject("Annotation", boolId) == None)
        self.gateway.deleteObjectDirect(fileAnn._obj)
        self.assertTrue( self.gateway.getObject("Annotation", fileId) == None)
        self.gateway.deleteObjectDirect(commAnn._obj)
        self.assertTrue( self.gateway.getObject("Annotation", commId) == None)
        self.gateway.deleteObjectDirect(tag._obj)
        self.assertTrue( self.gateway.getObject("Annotation", tagId) == None)


class GetObjectTest (lib.GTest):
    
    def setUp (self):
        super(GetObjectTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()


    def testSearchObjects(self):
        self.loginAsAuthor()

        # search for Projects
        #projects = list( self.gateway.searchProjects("weblitz") )   # method removed from blitz gateway
        pros = list( self.gateway.searchObjects(["Project"], "weblitz") )
        #self.assertEqual(len(pros), len(projects))  # check unordered lists are the same length & ids
        #projectIds = [p.getId() for p in projects]
        for p in pros:
            #self.assertTrue(p.getId() in projectIds)
            self.assertEqual(p.OMERO_CLASS, "Project", "Should only return Projects")

        # P/D/I is default objects to search
        # pdis = list( self.gateway.simpleSearch("weblitz") )   # method removed from blitz gateway
        #pdis.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        pdiResult = list( self.gateway.searchObjects(None, "weblitz") )
        pdiResult.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        # can directly check that sorted lists are the same
        #for r1, r2 in zip(pdis, pdiResult):
        #    self.assertEqual(r1.OMERO_CLASS, r2.OMERO_CLASS)
        #    self.assertEqual(r1.getId(), r2.getId())


    def testListProjects(self):
        self.loginAsAuthor()
        
        # params limit query by owner
        params = omero.sys.Parameters()
        params.theFilter = omero.sys.Filter()
        
        # should be no Projects owned by root (in the current group)
        params.theFilter.ownerId = omero.rtypes.rlong(0) # owned by 'root'
        pros = self.gateway.getObjects("Project", None, params)
        self.assertEqual(len(list(pros)), 0, "Should be no Projects owned by root")
        
        # filter by current user should get same as above.
        params.theFilter.ownerId = omero.rtypes.rlong(self.gateway.getEventContext().userId) # owned by 'author'
        pros = list( self.gateway.getObjects("Project", None, params) )
        projects = list( self.gateway.listProjects(only_owned=True) )
        self.assertEqual(len(pros), len(projects))  # check unordered lists are the same length & ids
        projectIds = [p.getId() for p in projects]
        for p in pros:
            self.assertTrue(p.getId() in projectIds)


    def testListExperimentersAndGroups(self):
        self.loginAsAdmin()
        
        # experimenters
        # experimenters = list( self.gateway.listExperimenters() ) # removed from blitz gateway
        exps = list( self.gateway.getObjects("Experimenter") )  # all experimenters
        
        #self.assertEqual(len(exps), len(experimenters))  # check unordered lists are the same length & ids
        #eIds = [e.getId() for e in experimenters]
        for e in exps:
            #self.assertTrue(e.getId() in eIds)
            for groupExpMap in e.copyGroupExperimenterMap():    # check iQuery has loaded groups
                self.assertEqual(e.id, groupExpMap.child.id.val)

        # returns all experimenters except current user - now moved to webclient_gateway
        #allBarOne = list( self.gateway.getExperimenters() )
        #self.assertEqual(len(allBarOne)+1, len(exps))
        #for e in allBarOne:
        #    self.assertTrue(e.getId() in eIds)
            
        # groups
        #groups = list( self.gateway.listGroups() )     # now removed from blitz gateway.
        gps = list( self.gateway.getObjects("ExperimenterGroup") )
        for grp in gps:
            grpExpMap = grp.copyGroupExperimenterMap()
        #self.assertEqual(len(gps), len(groups))  # check unordered lists are the same length & ids
        #gIds = [g.getId() for g in gps]
        #for g in groups:
        #    self.assertTrue(g.getId() in gIds)
        
        # uses gateway.getObjects("ExperimenterGroup") - check this doesn't throw
        colleagues = self.gateway.listColleagues()
        for e in colleagues:
            cName = e.getOmeName()

        # check we can find some groups
        exp = self.gateway.getObject("Experimenter", attributes={'omeName': self.USER.name})
        for groupExpMap in exp.copyGroupExperimenterMap():
            gName = groupExpMap.parent.name.val
            gId = groupExpMap.parent.id.val
            findG = self.gateway.getObject("ExperimenterGroup", attributes={'name': gName})
            self.assertEqual(gId, findG.id, "Check we found the same group")

    def testGetExperimenter(self):
        self.loginAsAdmin()

        # check that findExperimenter can be replaced by getObject()
        e = self.gateway.findExperimenter(self.USER.name)
        findExp = self.gateway.getObject("Experimenter", attributes={'omeName': self.USER.name})
        self.assertEqual(e.id, findExp.id, "Finding experimenter via omeName - should return single exp")

        noExp = self.gateway.getObject("Experimenter", attributes={'omeName': "Dummy Fake Name"})
        self.assertEqual(noExp, None, "Should not find any matching experimenter")
        noE = self.gateway.findExperimenter("Dummy Fake Name")
        self.assertEqual(noE, None, "Should not find any matching experimenter")

        exp = self.gateway.getObject("Experimenter", e.id) # uses iQuery
        experimenter = self.gateway.getExperimenter(e.id)  # uses IAdmin
        
        self.assertEqual(exp.getDetails().getOwner().omeName, experimenter.getDetails().getOwner().omeName)
        
        # check groupExperimenterMap loaded for exp
        for groupExpMap in exp.copyGroupExperimenterMap():
            self.assertEqual(e.id, groupExpMap.child.id.val)
        groupIds = []
        for groupExpMap in experimenter.copyGroupExperimenterMap():
            self.assertEqual(e.id, groupExpMap.child.id.val)
            groupIds.append(groupExpMap.parent.id.val)
            
        groupGen = self.gateway.getObjects("ExperimenterGroup", groupIds)
        #gGen = self.gateway.getExperimenterGroups(groupIds)  # removed from blitz gateway
        groups = list(groupGen)
        #gs = list(gGen)
        self.assertEqual(len(groups), len(groupIds))
        for g in groups:
            self.assertTrue(g.getId() in groupIds)
            for m in g.copyGroupExperimenterMap():  # check exps are loaded
                ex = m.child

    def testGetAnnotations(self):
        
        obj = self.TESTIMG
        dataset = self.getTestDataset()
        ns = "omero.gateway.test.get_objects.test_get_annotations_comment"
        ns_tag = "omero.gateway.test.get_objects.test_get_annotations_tag"
        
        self.loginAsAuthor()
        
        # create Comment
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(ns)
        ann.setValue("Test Comment")
        ann = obj.linkAnnotation(ann)
        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(self.gateway)
        tag.setNs(ns_tag)
        tag.setValue("Test Tag")
        tag = obj.linkAnnotation(tag)
        dataset.linkAnnotation(tag)
        
        # get the Comment 
        # a = self.gateway.getAnnotation(ann.id)   # method removed from blitz gateway
        annotation = self.gateway.getObject("CommentAnnotation", ann.id)
        #self.assertEqual(a.id, annotation.id)
        #self.assertEqual(a.ns, annotation.ns)
        self.assertEqual("Test Comment", annotation.textValue)
        #self.assertEqual(a.OMERO_TYPE, annotation.OMERO_TYPE)
        self.assertEqual(ann.OMERO_TYPE, annotation.OMERO_TYPE)
        
        # test getObject throws exception if more than 1 returned
        threw = True
        try:
            self.gateway.getObject("Annotation")
            threw = False
        except:
            threw = True
        self.assertTrue(threw, "getObject() didn't throw exception with >1 result")

        # get the Comment and Tag
        annGen = self.gateway.getObjects("Annotation", [ann.id, tag.id])
        anns = list(annGen)
        self.assertEqual(len(anns), 2)
        self.assertTrue(anns[0].ns in [ns, ns_tag])
        self.assertTrue(anns[1].ns in [ns, ns_tag])
        self.assertNotEqual(anns[0].OMERO_TYPE, anns[1].OMERO_TYPE)
        
        
        # get all available annotation links on the image
        annLinks = self.gateway.getAnnotationLinks("Image")
        for al in annLinks:
            self.assertTrue(isinstance(al.getAnnotation(), omero.gateway.AnnotationWrapper))
            self.assertEqual(al.parent.__class__, omero.model.ImageI)
            
        # get selected links - On image only
        annLinks = self.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()])
        for al in annLinks:
            self.assertEqual(obj.getId(), al.parent.id.val)
            self.assertTrue(al.parent.__class__ == omero.model.ImageI)
            
        # get selected links - On image only
        annLinks = self.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()])
        for al in annLinks:
            self.assertEqual(obj.getId(), al.parent.id.val)
            self.assertTrue(al.parent.__class__ == omero.model.ImageI)

        # compare with getObjectsByAnnotations
        annImages = list( self.gateway.getObjectsByAnnotations('Image', [tag.getId()]) )
        self.assertTrue(obj.getId() in [i.getId() for i in annImages])
            
        # params limit query by owner
        params = omero.sys.Parameters()
        params.theFilter = omero.sys.Filter()
        # should be no links owned by root (in the current group)
        params.theFilter.ownerId = omero.rtypes.rlong(0) # owned by 'root'
        annLinks = self.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()], params=params)
        self.assertEqual(len( list(annLinks)), 0, "No annotations on this image by root")
        # links owned by author
        eid = self.gateway.getEventContext().userId
        params.theFilter.ownerId = omero.rtypes.rlong(eid) # owned by 'author'
        omeName = self.gateway.getObject("Experimenter", eid).getName()
        annLinks = self.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()], params=params)
        for al in annLinks:
            self.assertEqual(al.getOwnerOmeName(), omeName)
            
        # all links on Image with specific ns
        annLinks = self.gateway.getAnnotationLinks("Image", ns=ns)
        for al in annLinks:
            self.assertEqual(al.getAnnotation().ns, ns)
            
        # get all uses of the Tag - have to check various types separately
        annList = list( self.gateway.getAnnotationLinks("Image", ann_ids=[tag.id]) )
        self.assertEqual(len(annList), 1)
        for al in annList:
            self.assertEqual(al.getAnnotation().id, tag.id)
        annList = list( self.gateway.getAnnotationLinks("Dataset", ann_ids=[tag.id]) )
        self.assertEqual(len(annList), 1)
        for al in annList:
            self.assertEqual(al.getAnnotation().id, tag.id)
            
        # remove annotations
        obj.removeAnnotations(ns)
        dataset.unlinkAnnotations(ns_tag)  # unlink tag
        obj.removeAnnotations(ns_tag)      # delete tag
        
        
    def testGetImage (self):
        testImage = self.TESTIMG
        # This should return image wrapper
        image = self.gateway.getObject("Image", testImage.id)
        pr = image.getProject()
        ds = image.getDataset()
        
        # test a few methods that involve lazy loading, rendering etc. 
        self.assertEqual(image.getSizeZ(), testImage.getSizeZ())
        self.assertEqual(image.getSizeY(), testImage.getSizeY())
        image.isGreyscaleRenderingModel()       # loads rendering engine
        testImage.isGreyscaleRenderingModel()
        self.assertEqual(image._re.getDefaultZ(), testImage._re.getDefaultZ())
        self.assertEqual(image._re.getDefaultT(), testImage._re.getDefaultT())
        self.assertEqual(image.getOwnerOmeName, testImage.getOwnerOmeName)
        
        
    def testGetProject (self):
        self.loginAsAuthor()
        testProj = self.getTestProject()
        p = self.gateway.getObject("Project", testProj.getId())
        self.assertEqual(testProj.getName(), p.getName())
        self.assertEqual(testProj.getDescription(), p.getDescription())
        self.assertEqual(testProj.getId(), p.getId())
        self.assertEqual(testProj.OMERO_CLASS, p.OMERO_CLASS)
        self.assertEqual(testProj.countChildren_cached(), p.countChildren_cached())
        self.assertEqual(testProj.getOwnerOmeName, p.getOwnerOmeName)

    def testTraversal (self):
        image = self.TESTIMG
        # This should return image wrapper
        pr = image.getProject()
        ds = image.getDataset()
        
        self.assertEqual(ds, image.getParent())
        self.assertEqual(image.listParents()[0], image.getParent())
        self.assertEqual(ds, image.getParent(withlinks=True)[0])
        self.assertEqual(image.getParent(withlinks=True), image.listParents(withlinks=True)[0])
        self.assertEqual(ds.getParent(), pr)
        self.assertEqual(pr.getParent(), None)
        self.assertEqual(len(pr.listParents()), 0)

if __name__ == '__main__':
    unittest.main()
