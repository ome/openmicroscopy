#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for delete testing

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import traceback
import test.integration.library as lib
import omero
import omero.callbacks
import Ice
import sys
import os
from omero.cmd import DoAll

class TestDelete(lib.ITest):

    def testBasicUsage(self):
        img = omero.model.ImageI()
        img.name = omero.rtypes.rstring("delete test")
        img.acquisitionDate = omero.rtypes.rtime(0)
        tag = omero.model.TagAnnotationI()
        img.linkAnnotation( tag )

        img = self.client.sf.getUpdateService().saveAndReturnObject( img )

        command = omero.cmd.Delete("/Image", img.id.val, None)
        handle = self.client.sf.submit(command)
        self.waitOnCmd(self.client, handle)

    def testDeleteMany(self):
        images = list()
        for i in range(0,5):
            img = omero.model.ImageI()
            img.name = omero.rtypes.rstring("delete test")
            img.acquisitionDate = omero.rtypes.rtime(0)
            tag = omero.model.TagAnnotationI()
            img.linkAnnotation( tag )

            images.append(self.client.sf.getUpdateService().saveAndReturnObject( img ))

        commands = list()
        for img in images:
            commands.append(omero.cmd.Delete("/Image", img.id.val, None))
        doall = omero.cmd.DoAll()
        doall.requests = commands

        handle = self.client.sf.submit(doall)
        self.waitOnCmd(self.client, handle)

    def testDeleteProjectWithoutContent(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        images = list()
        for i in range(0,5):
            img = omero.model.ImageI()
            img.name = omero.rtypes.rstring("test-delete-image-%i" % i)
            img.acquisitionDate = omero.rtypes.rtime(0)
            tag = omero.model.TagAnnotationI()
            img.linkAnnotation( tag )
            images.append(update.saveAndReturnObject( img ).id.val)
            
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = omero.rtypes.rstring('DS-test-2936-%s' % (uuid))
        dataset = update.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = omero.rtypes.rstring('PR-test-2936-%s' % (uuid))
        project = update.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        update.saveAndReturnObject(link)
        # put image in dataset
        for iid in images:
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(iid, False)
            update.saveAndReturnObject(dlink)
                
        op = dict()
        op["/TagAnnotation"] = "KEEP"
        op["/TermAnnotation"] = "KEEP"
        op["/FileAnnotation"] = "KEEP"
        op["/Dataset"] = "KEEP"
        op["/Image"] = "KEEP"

        dc = omero.cmd.Delete('/Project', long(project.id.val), op)
        handle = self.client.sf.submit(dc)
        cb = self.waitOnCmd(self.client, handle)

        self.assertEquals(None, query.find('Project', project.id.val))
        self.assertEquals(dataset.id.val, query.find('Dataset', dataset.id.val).id.val)

        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = dataset.id
        
        sql = "select im from Image im "\
                "left outer join fetch im.datasetLinks dil "\
                "left outer join fetch dil.parent d " \
                "where d.id = :oid " \
                "order by im.id asc"
        res = query.findAllByQuery(sql, p)
        self.assertEquals(5, len(res))       
        for e in res:
            if e.id.val not in images:
                self.assertRaises('Image %i is not in the [%s]' % (e.id.val, ",".join(images)))
    
    def testCheckIfDeleted(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        userName = self.client.sf.getAdminService().getEventContext().userName
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        delete = self.client.sf.getDeleteService()
        
        img = omero.model.ImageI()
        img.name = omero.rtypes.rstring("to delete - test")
        img.acquisitionDate = omero.rtypes.rtime(0)
        tag = omero.model.TagAnnotationI()
        img.linkAnnotation( tag )
        
        iid = update.saveAndReturnObject( img ).id.val
        
        cmd = omero.cmd.Delete("/Image", iid, None)
        handle = self.client.sf.submit(cmd)
        callback = self.waitOnCmd(self.client, handle)
        cbString = str(handle)

        callback.close(True) # Don't close handle

        self.assertEquals(None, query.find("Image", iid))

        # create new session and double check
        import os
        import Ice
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        host = c.ic.getProperties().getProperty('omero.host')
        port = int(c.ic.getProperties().getProperty('omero.port'))
        cl1 = omero.client(host=host, port=port)
        sf1 = cl1.createSession(userName,userName)

        try:
            handle1 = omero.cmd.HandlePrx.checkedCast(cl1.ic.stringToProxy(cbString))
            self.fail("exception Ice.ObjectNotExistException was not thrown")
        except Ice.ObjectNotExistException:
            pass

        # join session and double check
        cl2 = omero.client(host=host, port=port)
        sf2 = cl2.joinSession(uuid)

        try:
            handle2 = omero.cmd.HandlePrx.checkedCast(cl2.ic.stringToProxy(cbString))
            self.fail("exception Ice.ObjectNotExistException was not thrown")
        except Ice.ObjectNotExistException:
            pass

    def testCheckIfDeleted2(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        userName = self.client.sf.getAdminService().getEventContext().userName
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        delete = self.client.sf.getDeleteService()

        #dataset with many images
        images = list()
        for i in range(0,50):
            img = omero.model.ImageI()
            img.name = omero.rtypes.rstring("test-delete-image-%i" % i)
            img.acquisitionDate = omero.rtypes.rtime(0)
            tag = omero.model.TagAnnotationI()
            img.linkAnnotation( tag )
            images.append(update.saveAndReturnObject( img ))

        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = omero.rtypes.rstring('DS-test-%s' % (uuid))
        dataset = update.saveAndReturnObject(dataset)
        # put image in dataset
        for img in images:
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(img.id.val, False)
            update.saveAndReturnObject(dlink)

        commands = list()
        for img in images:
            commands.append(omero.cmd.Delete("/Image", img.id.val, None))
        doall = omero.cmd.DoAll()
        doall.requests = commands

        handle = self.client.sf.submit(doall)
        callback = self.waitOnCmd(self.client, handle, ms=1000, loops=50)
        cbString = str(handle)

        callback.close(True)

        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = dataset.id

        sql = "select im from Image im "\
                "left outer join fetch im.datasetLinks dil "\
                "left outer join fetch dil.parent d " \
                "where d.id = :oid " \
                "order by im.id asc"
        self.assertEquals(0, len(query.findAllByQuery(sql, p)))

    def testOddMessage(self):
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        store = self.client.sf.createRawFileStore()

        def _formatReport(delete_handle):
            """
            Added as workaround to the changes made in #3006.
            """
            delete_report = delete_handle.getResponse()
            if isinstance(delete_report, omero.cmd.ERR):
                return str(delete_report)
            elif delete_report.warning:
                return delete_report.warning
            return ""

        images = list()
        for i in range(0,10):
            img = self.new_image()
            img = update.saveAndReturnObject(img)
            iid = img.getId().getValue()
            
            oFile = omero.model.OriginalFileI()
            oFile.setName(omero.rtypes.rstring('companion_file.txt'));
            oFile.setPath(omero.rtypes.rstring('/my/path/to/the/file/'));
            oFile.setSize(omero.rtypes.rlong(7471));
            oFile.setHash(omero.rtypes.rstring("pending"));
            oFile.setMimetype(omero.rtypes.rstring('Companion/Deltavision'));

            ofid = update.saveAndReturnObject(oFile).id.val;
                        
            store.setFileId(ofid);
            binary = 'aaa\naaaa\naaaaa'
            pos = 0
            rlen = 0
            store.write(binary, 0, 0)
            of = store.save()
            
            fa = omero.model.FileAnnotationI()
            fa.setNs(omero.rtypes.rstring(omero.constants.namespaces.NSCOMPANIONFILE))
            fa.setFile(of)
            l_ia = omero.model.ImageAnnotationLinkI()
            l_ia.setParent(img)
            l_ia.setChild(fa)
            l_ia = update.saveAndReturnObject(l_ia)

            images.append(iid)
            
        commands = list()
        for iid in images:
            commands.append(omero.cmd.Delete("/Image", iid, None))
        doall = omero.cmd.DoAll()
        doall.requests = commands

        handle = self.client.sf.submit(doall)
        callback = self.waitOnCmd(self.client, handle)
        callback.close(True)

    def test3639(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        group = self.client.sf.getAdminService().getGroup(self.client.sf.getAdminService().getEventContext().groupId)
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        
        images = list()
        for i in range(0,5):
            img = self.new_image()
            img = update.saveAndReturnObject(img)
            images.append(img.id.val)
        
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oids"] = omero.rtypes.rlist([omero.rtypes.rlong(s) for s in images])
            
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = omero.rtypes.rstring('DS-test-2936-%s' % (uuid))
        dataset = update.saveAndReturnObject(dataset)
        
        # put image in dataset
        for iid in images:
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(iid, False)
            update.saveAndReturnObject(dlink)
        
        
        #log in as group owner:
        client_o, owner = self.new_client_and_user(group=group,admin=True)
        delete_o = client_o.sf.getDeleteService()
        query_o = client_o.sf.getQueryService()
        
        
        handlers = list()        
        op = dict()
        op["/Image"] = "KEEP"
        dc = omero.cmd.Delete('/Dataset', long(dataset.id.val), op)
        handlers.append(str(client_o.sf.submit(dc)))

        imageToDelete = images[2]
        images.remove(imageToDelete)
        dc2 = omero.cmd.Delete('/Image', long(imageToDelete), {})
        handlers.append(str(client_o.sf.submit(dc2)))

        def _formatReport(delete_handle):
            """
            Added as workaround to the changes made in #3006.
            """
            delete_report = delete_handle.getResponse()
            rv = []
            if isinstance(delete_report, omero.cmd.ERR):
                rv.append(str(delete_report))
            else:
                if delete_report.warning:
                    rv.append(delete_report.warning)
            if len(rv) > 0:
                return "; ".join(rv)
            return None

        failure = list();
        in_progress = 0
        r = None

        while(len(handlers)>0):
            for cbString in handlers:
                try:
                    handle = omero.cmd.HandlePrx.checkedCast(client_o.ic.stringToProxy(cbString))
                    cb = omero.callbacks.CmdCallbackI(client_o, handle)
                    if not cb.block(500): # ms.
                        # No errors possible if in progress (since no response)
                        print "in progress", _formatReport(handle)
                        in_progress+=1
                    else:
                        rsp = cb.getResponse()
                        if isinstance(rsp, omero.cmd.ERR):
                            r = _formatReport(handle)
                            if r is not None:
                                failure.append(r)
                            else:
                                failure.append("No report!!!")
                        else:
                            r = _formatReport(handle)
                            if r is not None:
                                failure.append(r)
                            cb.close(True) # Close handle
                        handlers.remove(cbString)
                except Ice.ObjectNotExistException:
                    pass
                except Exception, x:
                    if r is not None:
                        failure.append(traceback.format_exc())

        if len(failure) > 0:
            self.fail(";".join(failure))
        self.assertEquals(None, query_o.find('Dataset', dataset.id.val))

    def test5793(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        img = self.new_image(name="delete tagset test")

        tag = omero.model.TagAnnotationI()
        tag.textValue = omero.rtypes.rstring("tag %s" % uuid)
        tag = self.client.sf.getUpdateService().saveAndReturnObject( tag )

        img.linkAnnotation( tag )
        img = self.client.sf.getUpdateService().saveAndReturnObject( img )

        tagset = omero.model.TagAnnotationI()
        tagset.textValue = omero.rtypes.rstring("tagset %s" % uuid)
        tagset.linkAnnotation(tag)
        tagset = self.client.sf.getUpdateService().saveAndReturnObject( tagset )

        tag = tagset.linkedAnnotationList()[0]

        command = omero.cmd.Delete("/Annotation", tagset.id.val, None)
        handle = self.client.sf.submit(command)
        callback = self.waitOnCmd(self.client, handle)

        self.assertEquals(None, query.find("TagAnnotation", tagset.id.val))
        self.assertEquals(tag.id.val, query.find("TagAnnotation", tag.id.val).id.val)

    def test7314(self):
        """
        Test the delete of an original file when a file annotation is present
        """
        o = self.client.upload(__file__)
        fa = omero.model.FileAnnotationI()
        fa.file = o.proxy()
        fa = self.update.saveAndReturnObject(fa)

        command = omero.cmd.Delete("/OriginalFile", o.id.val, None)
        handle = self.client.sf.submit(command)
        self.waitOnCmd(self.client, handle)
        self.assertRaises(omero.ServerError, \
                self.client.sf.getQueryService().get, "FileAnnotation", fa.id.val)

    def testDeleteOneDatasetFilesetErr(self):
        """
        Simple example of the MIF delete bad case:
        a single fileset containing 2 images is split among 2 datasets.
        Delete one dataset, delete fails.
        """
        client, user = self.new_client_and_user(perms="rw----")
        update = client.sf.getUpdateService()
        datasets = self.createDatasets(2, "testDeleteOneDatasetFilesetErr", client=client)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(datasets[i].proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Lookup the fileset
        query = client.sf.getQueryService()
        img = query.get('Image', images[0].id.val)    # load first image
        filesetId =  img.fileset.id.val

        # Now delete one dataset
        delete = omero.cmd.Delete("/Dataset", datasets[0].id.val, None)
        rsp = self.doAllSubmit([delete], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        # The delete should fail due to the fileset
        ### self.assertTrue('Fileset' in rsp.constraints, "delete should fail due to 'Fileset' constraints")
        ### failedFilesets = rsp.constraints['Fileset']
        ### self.assertEqual(len(failedFilesets), 1, "delete should fail due to a single Fileset")
        ### self.assertEqual(failedFilesets[0], filesetId, "delete should fail due to this Fileset")

        # Neither image or the dataset should be deleted.
        self.assertEquals(datasets[0].id.val, query.find("Dataset", datasets[0].id.val).id.val)
        self.assertEquals(images[0].id.val, query.find("Image", images[0].id.val).id.val)
        self.assertEquals(images[1].id.val, query.find("Image", images[1].id.val).id.val)

    def testDeleteOneImageFilesetErr(self):
        """
        Simple example of the MIF delete good case:
        two images in a MIF.
        Delete one image, the delete should fail.
        """
        client, user = self.new_client_and_user(perms="rw----")
        update = client.sf.getUpdateService()
        images = self.importMIF(2, client=client)

        # Lookup the fileset
        query = client.sf.getQueryService()
        img = query.get('Image', images[0].id.val)    # load first image
        filesetId =  img.fileset.id.val

        # Now delete one image
        delete = omero.cmd.Delete("/Image", images[0].id.val, None)
        rsp = self.doAllSubmit([delete], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        ### # The delete should fail due to the fileset
        ### self.assertTrue('Fileset' in rsp.constraints, "delete should fail due to 'Fileset' constraints")
        ### failedFilesets = rsp.constraints['Fileset']
        ### self.assertEqual(len(failedFilesets), 1, "delete should fail due to a single Fileset")
        ### self.assertEqual(failedFilesets[0], filesetId, "delete should fail due to this Fileset")

        # Neither image should be deleted.
        self.assertEquals(images[0].id.val, query.find("Image", images[0].id.val).id.val)
        self.assertEquals(images[1].id.val, query.find("Image", images[1].id.val).id.val)

    def testDeleteDatasetFilesetOK(self):
        """
        Simple example of the MIF delete good case:
        a single fileset containing 2 images in one dataset.
        Delete the dataset, the delete should succeed.
        """
        client, user = self.new_client_and_user(perms="rw----")
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()
        ds = omero.model.DatasetI()
        ds.name = omero.rtypes.rstring("testDeleteDatasetFilesetOK")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now delete the dataset, should succeed
        delete = omero.cmd.Delete("/Dataset", ds.id.val, None)
        self.doAllSubmit([delete], client)

        # The dataset, fileset and both images should be deleted.
        self.assertEquals(None, query.find("Dataset", ds.id.val))
        self.assertEquals(None, query.find("Fileset", fsId))
        self.assertEquals(None, query.find("Image", images[0].id.val))
        self.assertEquals(None, query.find("Image", images[1].id.val))

    def testDeleteAllDatasetsFilesetOK(self):
        """
        Simple example of the MIF delete bad case:
        a single fileset containing 2 images is split among 2 datasets.
        Delete all datasets, delete succeeds.
        """
        client, user = self.new_client_and_user(perms="rw----")
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()
        datasets = self.createDatasets(2, "testDeleteAllDatasetsFilesetOK", client=client)
        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(datasets[i].proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now delete all datasets, should succeed
        delete1 = omero.cmd.Delete("/Dataset", datasets[0].id.val, None)
        delete2 = omero.cmd.Delete("/Dataset", datasets[1].id.val, None)
        self.doAllSubmit([delete1,delete2], client)

        # Both datasets, the fileset and both images should be deleted.
        self.assertEquals(None, query.find("Dataset", datasets[0].id.val))
        self.assertEquals(None, query.find("Dataset", datasets[1].id.val))
        self.assertEquals(None, query.find("Fileset", fsId))
        self.assertEquals(None, query.find("Image", images[0].id.val))
        self.assertEquals(None, query.find("Image", images[1].id.val))

    def testDeleteAllImagesFilesetOK(self):
        """
        Simple example of the MIF delete good case:
        two images in a MIF.
        Delete all images, the delete should succeed.
        """
        client, user = self.new_client_and_user(perms="rw----")
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()
        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val

        # Now delete all images, should succeed
        delete1 = omero.cmd.Delete("/Image", images[0].id.val, None)
        delete2 = omero.cmd.Delete("/Image", images[1].id.val, None)
        self.doAllSubmit([delete1,delete2], client)

        # The fileset and both images should be deleted.
        self.assertEquals(None, query.find("Fileset", fsId))
        self.assertEquals(None, query.find("Image", images[0].id.val))
        self.assertEquals(None, query.find("Image", images[1].id.val))

    def testDeleteFilesetOK(self):
        """
        Simple example of the MIF delete good case:
        a single fileset containing 2 images.
        Delete the fileset, the delete should succeed.
        """
        client, user = self.new_client_and_user(perms="rw----")
        query = client.sf.getQueryService()
        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val

        # Now delete the fileset, should succeed
        delete = omero.cmd.Delete("/Fileset", fsId, None)
        self.doAllSubmit([delete], client)

        # The dataset, fileset and both images should be deleted.
        self.assertEquals(None, query.find("Fileset", fsId))
        self.assertEquals(None, query.find("Image", images[0].id.val))
        self.assertEquals(None, query.find("Image", images[1].id.val))

    def testDeleteImagesTwoFilesetsErr(self):
        """
        If we try to partially delete 2 Filesets, both should be returned
        by the delete error
        """
        client, user = self.new_client_and_user(perms="rw----")
        # 2 filesets, each with 2 images
        imagesFsOne = self.importMIF(2, client=client)
        imagesFsTwo = self.importMIF(2, client=client)

        # Lookup the filesets
        qs = client.sf.getQueryService()
        filesetOneId = qs.get('Image', imagesFsOne[0].id.val).fileset.id.val
        filesetTwoId = qs.get('Image', imagesFsTwo[0].id.val).fileset.id.val

        # delete should fail...
        delete1 = omero.cmd.Delete("/Image", imagesFsOne[0].id.val, None)
        delete2 = omero.cmd.Delete("/Image", imagesFsTwo[0].id.val, None)
        rsp = self.doAllSubmit([delete1,delete2], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        # ...due to the filesets
        ### self.assertTrue('Fileset' in rsp.constraints, "Delete should fail due to 'Fileset' constraints")
        ### failedFilesets = rsp.constraints['Fileset']
        ### self.assertEqual(len(failedFilesets), 2, "Delete should fail due to a Two Filesets")
        ### self.assertTrue(filesetOneId in failedFilesets)
        ### self.assertTrue(filesetTwoId in failedFilesets)

    def testDeleteDatasetTwoFilesetsErr(self):
        """
        If we try to partially delete 2 Filesets, both should be returned
        by the delete error
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms="rw----")
        # 2 filesets, each with 2 images
        imagesFsOne = self.importMIF(2, client=client)
        imagesFsTwo = self.importMIF(2, client=client)

        update = client.sf.getUpdateService()
        ds = omero.model.DatasetI()
        ds.name = omero.rtypes.rstring("testDeleteDatasetTwoFilesetsErr")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in (imagesFsOne, imagesFsTwo):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(i[0].proxy())
            link = update.saveAndReturnObject(link)

        # Lookup the filesets
        qs = client.sf.getQueryService()
        filesetOneId = qs.get('Image', imagesFsOne[0].id.val).fileset.id.val
        filesetTwoId = qs.get('Image', imagesFsTwo[0].id.val).fileset.id.val

        # delete should fail...
        delete = omero.cmd.Delete("/Dataset", ds.id.val, None)
        rsp = self.doAllSubmit([delete], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        # ...due to the filesets
        ### self.assertTrue('Fileset' in rsp.constraints, "Delete should fail due to 'Fileset' constraints")
        ### failedFilesets = rsp.constraints['Fileset']
        ### self.assertEqual(len(failedFilesets), 2, "Delete should fail due to a Two Filesets")
        ### self.assertTrue(filesetOneId in failedFilesets)
        ### self.assertTrue(filesetTwoId in failedFilesets)

if __name__ == '__main__':
    if "TRACE" in os.environ:
        import trace
        tracer = trace.Trace(ignoredirs=[sys.prefix, sys.exec_prefix], trace=1)
        tracer.runfunc(unittest.main)
    else:
        unittest.main()
