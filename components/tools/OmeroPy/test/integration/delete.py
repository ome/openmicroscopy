#!/usr/bin/env python

"""
   Integration test for delete testing

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import traceback
import integration.library as lib
import omero
import omero.callbacks
import Ice
import sys
import os

class TestDelete(lib.ITest):

    def testBasicUsage(self):
        img = omero.model.ImageI()
        img.name = omero.rtypes.rstring("delete test")
        img.acquisitionDate = omero.rtypes.rtime(0)
        tag = omero.model.TagAnnotationI()
        img.linkAnnotation( tag )

        img = self.client.sf.getUpdateService().saveAndReturnObject( img )

        command = omero.api.delete.DeleteCommand("/Image", img.id.val, None)
        handle = self.client.sf.getDeleteService().queueDelete([command])
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)
        errors = None
        count = 10
        while errors is None:
            errors = callback.block(500)
            count -= 1
            self.assert_( count != 0 )
        self.assertEquals(0, errors)
    
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
            commands.append(omero.api.delete.DeleteCommand("/Image", img.id.val, None))
        
        handle = self.client.sf.getDeleteService().queueDelete(commands)
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)
        errors = None
        count = 10
        while errors is None:
            errors = callback.block(500)
            count -= 1
            self.assert_( count != 0 )
        self.assertEquals(0, errors)

    def testDeleteProjectWithoutContent(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        delete = self.client.sf.getDeleteService()
        
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
        dc = omero.api.delete.DeleteCommand('/Project', long(project.id.val), op)
        handle = delete.queueDelete([dc])
        
        cb = omero.callbacks.DeleteCallbackI(self.client, handle)
        while cb.block(500) is None:
            pass
        
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
        
        cmd = omero.api.delete.DeleteCommand("/Image", iid, None)
        
        handle = delete.queueDelete([cmd])
        cbString = str(handle)
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)
        while callback.block(500) is not None: # ms.
            pass

        err = handle.errors()
        callback.close()

        self.assertEquals(0, err)
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
            handle1 = omero.api.delete.DeleteHandlePrx.checkedCast(cl1.ic.stringToProxy(cbString))
            self.fail("exception Ice.ObjectNotExistException was not thrown")
        except Ice.ObjectNotExistException:
            pass

        # join session and double check
        cl2 = omero.client(host=host, port=port)
        sf2 = cl2.joinSession(uuid)

        try:
            handle2 = omero.api.delete.DeleteHandlePrx.checkedCast(cl2.ic.stringToProxy(cbString))
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
            commands.append(omero.api.delete.DeleteCommand("/Image", img.id.val, None))

        handle = delete.queueDelete(commands)
        cbString = str(handle)
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)

        count = 0
        while True:
            count += 1
            rv = callback.block(500)
            if rv is not None:
                break
            elif count > 50:
                self.fail("Too many loops")
        callback.close()

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
            delete_reports = delete_handle.report()
            rv = []
            for report in delete_reports:
                if report.error:
                    rv.append(report.error)
                elif report.warning:
                    rv.append(report.warning)
            return "; ".join(rv)
            
        images = list()
        for i in range(0,10):
            img = self.createTestImage(session = self.client.sf)
            iid = img.getId().getValue()
            
            oFile = omero.model.OriginalFileI()
            oFile.setName(omero.rtypes.rstring('companion_file.txt'));
            oFile.setPath(omero.rtypes.rstring('/my/path/to/the/file/'));
            oFile.setSize(omero.rtypes.rlong(7471));
            oFile.setSha1(omero.rtypes.rstring("pending"));
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
            commands.append(omero.api.delete.DeleteCommand("/Image", iid, None))
        
        handle = self.client.sf.getDeleteService().queueDelete(commands)
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)
        
        while callback.block(500) is None: # ms.
            err = handle.errors()
            if err > 0:
                print 'Failed', err
                print _formatReport(handle)
            else:
                print 'In progress'
                print _formatReport(handle)

        err = handle.errors()
        if err > 0:
            print 'Failed', err
            print _formatReport(handle)
        else:
            print 'finished', err
            print _formatReport(handle)
            callback.close()

    def test3639(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        group = self.client.sf.getAdminService().getGroup(self.client.sf.getAdminService().getEventContext().groupId)
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        
        images = list()
        for i in range(0,5):
            img = self.createTestImage(session=self.client.sf)
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
        dc = omero.api.delete.DeleteCommand('/Dataset', long(dataset.id.val), op)
        handlers.append(str(delete_o.queueDelete([dc])))
        
        imageToDelete = images[2]
        images.remove(imageToDelete)
        dc2 = omero.api.delete.DeleteCommand('/Image', long(imageToDelete), {})
        handlers.append(str(delete_o.queueDelete([dc2])))
        
        def _formatReport(delete_handle):
            """
            Added as workaround to the changes made in #3006.
            """
            delete_reports = delete_handle.report()
            rv = []
            for report in delete_reports:
                if report.error:
                    rv.append(report.error)
                elif report.warning:
                    rv.append(report.warning)
            if len(rv) > 0:
                return "; ".join(rv)
            return None

        failure = list();
        in_progress = 0

        while(len(handlers)>0):
            for cbString in handlers:
                try:
                    handle = omero.api.delete.DeleteHandlePrx.checkedCast(client_o.ic.stringToProxy(cbString))
                    cb = omero.callbacks.DeleteCallbackI(client_o, handle)
                    if cb.block(500) is None: # ms.
                        err = handle.errors()
                        if err > 0:
                            r = _formatReport(handle)
                            if r is not None:
                                failure.append(r)
                            else:
                                failure.append("No report!!!")
                        else:
                            print "in progress", _formatReport(handle)
                            in_progress+=1

                    else:
                        err = handle.errors()
                        if err > 0:
                            r = _formatReport(handle)
                            if r is not None:
                                failure.append(r)
                            else:
                                failure.append("No report!!!")

                        else:
                            r = _formatReport(handle)
                            if r is not None:
                                failure.append(r)
                            cb.close()
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

        img = omero.model.ImageI()
        img.name = omero.rtypes.rstring("delete tagset test")
        img.acquisitionDate = omero.rtypes.rtime(0)

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

        command = omero.api.delete.DeleteCommand("/Annotation", tagset.id.val, None)
        handle = self.client.sf.getDeleteService().queueDelete([command])
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)
        errors = None
        count = 10

        errs = []
        while callback.block(500) is None: # ms.
            delete_reports = handle.report()
            for report in delete_reports:
                if report.error:
                    errs.append(report.error)
                if report.warning:
                    print report.warning

        self.assertEquals([], errs)
        self.assertEquals(None, query.find("TagAnnotation", tagset.id.val))
        self.assertEquals(tag.id.val, query.find("TagAnnotation", tag.id.val).id.val)

    def test7314(self):
        """
        Test the delete of an original file when a file annotation is present
        """
        o = self.client.upload(__file__)
        fa = omero.model.FileAnnotationI()
        fa.file = o
        fa = self.update.saveAndReturnObject(fa)

        command = omero.api.delete.DeleteCommand("/OriginalFile", o.id.val, None)
        handle = self.client.sf.getDeleteService().queueDelete([command])
        callback = omero.callbacks.DeleteCallbackI(self.client, handle)
        errors = None
        count = 10
        while errors is None:
            errors = callback.block(500)
            count -= 1
            self.assert_( count != 0 )
        reports = "\n\t".join([r.error for r in handle.report()])
        self.assertEquals(0, errors, "Found errors:\n\t" + reports)


if __name__ == '__main__':
    if "TRACE" in os.environ:
        import trace
        tracer = trace.Trace(ignoredirs=[sys.prefix, sys.exec_prefix], trace=1)
        tracer.runfunc(unittest.main)
    else:
        unittest.main()
