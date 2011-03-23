#!/usr/bin/env python

"""
   gateway tests - Testing the gateway.getObject() and searchObjects() methods

"""

import unittest
import omero

import gatewaytest.library as lib

class GetObjectTest (lib.GTest):
    
    def setUp (self):
        super(GetObjectTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()


    def testSearchObjects(self):
        self.loginAsAuthor()

        # search for Projects
        projects = list( self.gateway.searchProjects("weblitz") )
        pros = list( self.gateway.searchObjects(["Project"], "weblitz") )
        self.assertEqual(len(pros), len(projects))  # check unordered lists are the same length & ids
        projectIds = [p.getId() for p in projects]
        for p in pros:
            self.assertTrue(p.getId() in projectIds)
            self.assertEqual(p.OMERO_CLASS, "Project", "Should only return Projects")

        # P/D/I is default objects to search
        pdis = list( self.gateway.simpleSearch("weblitz") )
        pdis.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        pdiResult = list( self.gateway.searchObjects(None, "weblitz") )
        pdiResult.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        # can directly check that sorted lists are the same
        for r1, r2 in zip(pdis, pdiResult):
            self.assertEqual(r1.OMERO_CLASS, r2.OMERO_CLASS)
            self.assertEqual(r1.getId(), r2.getId())


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
        experimenters = list( self.gateway.listExperimenters() )
        exps = list( self.gateway.getObjects("Experimenter", None) )
        
        self.assertEqual(len(exps), len(experimenters))  # check unordered lists are the same length & ids
        eIds = [e.getId() for e in experimenters]
        for e in exps:
            self.assertTrue(e.getId() in eIds)
            
        # groups
        groups = list( self.gateway.listGroups() )
        gps = list( self.gateway.getObjects("ExperimenterGroup", None) )
        
        self.assertEqual(len(gps), len(groups))  # check unordered lists are the same length & ids
        gIds = [g.getId() for g in gps]
        for g in groups:
            self.assertTrue(g.getId() in gIds)

        
    def testGetExperimenter(self):
        self.loginAsAdmin()
        e = self.gateway.findExperimenter(self.USER.name)
        exp = self.gateway.getObject("Experimenter", e.id) # uses iQuery
        experimenter = self.gateway.getExperimenter(e.id)  # uses IAdmin
        
        self.assertEqual(exp.getDetails().getOwner().omeName, experimenter.getDetails().getOwner().omeName)
        
        # groupExperimenterMap not loaded for exp
        #for groupExpMap in exp.copyGroupExperimenterMap():
            #self.assertEqual(e.id, groupExpMap.child.id.val)
        groupIds = []
        for groupExpMap in experimenter.copyGroupExperimenterMap():
            self.assertEqual(e.id, groupExpMap.child.id.val)
            groupIds.append(groupExpMap.parent.id.val)
            
        groupGen = self.gateway.getObjects("ExperimenterGroup", groupIds)
        gGen = self.gateway.getExperimenterGroups(groupIds)  # uses iQuery
        groups = list(groupGen)
        gs = list(gGen)
        self.assertEqual(len(groups), len(groupIds))
        for g in groups:
            self.assertTrue(g.getId() in groupIds)
            for m in g.copyGroupExperimenterMap():  # check exps are loaded
                ex = m.child
        for g in gs:
            self.assertTrue(g.getId() in groupIds)


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
        obj.linkAnnotation(ann)
        ann = obj.getAnnotation(ns)
        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(self.gateway)
        tag.setNs(ns_tag)
        tag.setValue("Test Tag")
        tag = obj.linkAnnotation(tag)
        dataset.linkAnnotation(tag)
        
        # get the Comment 
        a = self.gateway.getAnnotation(ann.id)
        annotation = self.gateway.getObject("CommentAnnotation", ann.id)
        self.assertEqual(a.id, annotation.id)
        self.assertEqual(a.ns, annotation.ns)
        self.assertEqual("Test Comment", annotation.textValue)
        self.assertEqual(a.OMERO_TYPE, annotation.OMERO_TYPE)
        self.assertEqual(ann.OMERO_TYPE, annotation.OMERO_TYPE)
        
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
            self.assertTrue(al.getAnnotation(), omero.gateway.AnnotationWrapper)
            self.assertTrue(al.getParent().__class__ == omero.model.ImageI)
            
        # get selected links - On image only
        annLinks = self.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()])
        for al in annLinks:
            self.assertEqual(obj.getId(), al.parent.id.val)
            self.assertTrue(al.getParent().__class__ == omero.model.ImageI)
            
        # get selected links - On image only
        annLinks = self.gateway.getAnnotationLinks("Image", parent_ids=[obj.getId()])
        for al in annLinks:
            self.assertEqual(obj.getId(), al.parent.id.val)
            self.assertTrue(al.getParent().__class__ == omero.model.ImageI)
            
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
        self.assertEqual(image.getWidth(), testImage.getWidth())
        self.assertEqual(image.getHeight(), testImage.getHeight())
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

if __name__ == '__main__':
    unittest.main()
