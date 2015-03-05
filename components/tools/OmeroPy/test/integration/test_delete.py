#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test for delete testing

"""

import traceback
import library as lib
import pytest
import omero
import omero.callbacks
import Ice
import sys
import os
from time import time
from omero.rtypes import rstring, rtime, rlist, rlong


class TestDelete(lib.ITest):

    def testBasicUsage(self):
        img = omero.model.ImageI()
        img.name = rstring("delete test")
        img.acquisitionDate = rtime(0)
        tag = omero.model.TagAnnotationI()
        img.linkAnnotation(tag)

        img = self.client.sf.getUpdateService().saveAndReturnObject(img)

        command = omero.cmd.Delete("/Image", img.id.val, None)
        handle = self.client.sf.submit(command)
        self.waitOnCmd(self.client, handle)

    def testDeleteMany(self):
        images = list()
        for i in range(0, 5):
            img = omero.model.ImageI()
            img.name = rstring("delete test")
            img.acquisitionDate = rtime(0)
            tag = omero.model.TagAnnotationI()
            img.linkAnnotation(tag)

            images.append(
                self.client.sf.getUpdateService().saveAndReturnObject(img))

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
        for i in range(0, 5):
            img = omero.model.ImageI()
            img.name = rstring("test-delete-image-%i" % i)
            img.acquisitionDate = rtime(0)
            tag = omero.model.TagAnnotationI()
            img.linkAnnotation(tag)
            images.append(update.saveAndReturnObject(img).id.val)

        # create dataset
        dataset = self.make_dataset('DS-test-2936-%s' % uuid)

        # create project
        project = self.make_project('PR-test-2936-%s' % uuid)

        # put dataset in project
        self.link(project, dataset)

        # put image in dataset
        for iid in images:
            self.link(omero.model.DatasetI(dataset.id.val, False),
                      omero.model.ImageI(iid, False))

        op = dict()
        op["/TagAnnotation"] = "KEEP"
        op["/TermAnnotation"] = "KEEP"
        op["/FileAnnotation"] = "KEEP"
        op["/Dataset"] = "KEEP"
        op["/Image"] = "KEEP"

        dc = omero.cmd.Delete('/Project', long(project.id.val), op)
        handle = self.client.sf.submit(dc)
        self.waitOnCmd(self.client, handle)

        assert not query.find('Project', project.id.val)
        assert dataset.id.val == query.find('Dataset', dataset.id.val).id.val

        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = dataset.id

        sql = "select im from Image im "\
              "left outer join fetch im.datasetLinks dil "\
              "left outer join fetch dil.parent d " \
              "where d.id = :oid " \
              "order by im.id asc"
        res = query.findAllByQuery(sql, p)
        assert 5 == len(res)
        for e in res:
            if e.id.val not in images:
                self.assertRaises(
                    'Image %i is not in the [%s]'
                    % (e.id.val, ",".join(images)))

    def testCheckIfDeleted(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        userName = self.client.sf.getAdminService().getEventContext().userName
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        img = omero.model.ImageI()
        img.name = rstring("to delete - test")
        img.acquisitionDate = rtime(0)
        tag = omero.model.TagAnnotationI()
        img.linkAnnotation(tag)

        iid = update.saveAndReturnObject(img).id.val

        cmd = omero.cmd.Delete("/Image", iid, None)
        handle = self.client.sf.submit(cmd)
        callback = self.waitOnCmd(self.client, handle)
        cbString = str(handle)

        callback.close(True)  # Don't close handle

        assert not query.find("Image", iid)

        # create new session and double check
        import os
        import Ice
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        host = c.ic.getProperties().getProperty('omero.host')
        port = int(c.ic.getProperties().getProperty('omero.port'))
        cl1 = omero.client(host=host, port=port)
        cl1.createSession(userName, userName)

        with pytest.raises(Ice.ObjectNotExistException):
            omero.cmd.HandlePrx.checkedCast(
                cl1.ic.stringToProxy(cbString))

        # join session and double check
        cl2 = omero.client(host=host, port=port)
        cl2.joinSession(uuid)

        with pytest.raises(Ice.ObjectNotExistException):
            omero.cmd.HandlePrx.checkedCast(
                cl2.ic.stringToProxy(cbString))

    def testCheckIfDeleted2(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        # dataset with many images
        images = list()
        for i in range(0, 50):
            img = omero.model.ImageI()
            img.name = rstring("test-delete-image-%i" % i)
            img.acquisitionDate = rtime(0)
            tag = omero.model.TagAnnotationI()
            img.linkAnnotation(tag)
            images.append(update.saveAndReturnObject(img))

        # create dataset
        dataset = self.make_dataset('DS-test-%s' % uuid)

        # put image in dataset
        for img in images:
            self.link(omero.model.DatasetI(dataset.id.val, False),
                      omero.model.ImageI(img.id.val, False))

        commands = list()
        for img in images:
            commands.append(omero.cmd.Delete("/Image", img.id.val, None))
        doall = omero.cmd.DoAll()
        doall.requests = commands

        handle = self.client.sf.submit(doall)
        callback = self.waitOnCmd(self.client, handle, ms=1000, loops=50)

        callback.close(True)

        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = dataset.id

        sql = "select im from Image im "\
              "left outer join fetch im.datasetLinks dil "\
              "left outer join fetch dil.parent d " \
              "where d.id = :oid " \
              "order by im.id asc"
        assert 0 == len(query.findAllByQuery(sql, p))

    def testOddMessage(self):
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
        for i in range(0, 10):
            img = self.new_image()
            img = update.saveAndReturnObject(img)
            iid = img.getId().getValue()

            oFile = omero.model.OriginalFileI()
            oFile.setName(rstring('companion_file.txt'))
            oFile.setPath(rstring('/my/path/to/the/file/'))
            oFile.setSize(rlong(7471))
            oFile.setHash(rstring("pending"))
            oFile.setMimetype(rstring('Companion/Deltavision'))

            ofid = update.saveAndReturnObject(oFile).id.val

            store.setFileId(ofid)
            binary = 'aaa\naaaa\naaaaa'
            store.write(binary, 0, 0)
            of = store.save()

            fa = omero.model.FileAnnotationI()
            fa.setNs(rstring(omero.constants.namespaces.NSCOMPANIONFILE))
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
        group = self.client.sf.getAdminService().getGroup(
            self.client.sf.getAdminService().getEventContext().groupId)
        update = self.client.sf.getUpdateService()

        images = list()
        for i in range(0, 5):
            img = self.new_image()
            img = update.saveAndReturnObject(img)
            images.append(img.id.val)

        p = omero.sys.Parameters()
        p.map = {}
        p.map["oids"] = rlist([rlong(s) for s in images])

        # create dataset
        dataset = self.make_dataset('DS-test-2936-%s' % uuid)

        # put image in dataset
        for iid in images:
            self.link(omero.model.DatasetI(dataset.id.val, False),
                      omero.model.ImageI(iid, False))

        # log in as group owner:
        client_o, owner = self.new_client_and_user(group=group, owner=True)
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

        failure = list()
        in_progress = 0
        r = None

        while(len(handlers) > 0):
            for cbString in handlers:
                try:
                    with pytest.raises(Ice.ObjectNotExistException):
                        handle = omero.cmd.HandlePrx.checkedCast(
                            client_o.ic.stringToProxy(cbString))
                        cb = omero.callbacks.CmdCallbackI(client_o, handle)
                        if not cb.block(500):  # ms.
                            # No errors possible if in progress(
                            # (since no response)
                            print "in progress", _formatReport(handle)
                            in_progress += 1
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
                                cb.close(True)  # Close handle
                            handlers.remove(cbString)
                except Exception:
                    if r is not None:
                        failure.append(traceback.format_exc())

        if len(failure) > 0:
            assert False, ";".join(failure)
        assert not query_o.find('Dataset', dataset.id.val)

    def test5793(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        query = self.client.sf.getQueryService()

        img = self.new_image(name="delete tagset test")

        tag = omero.model.TagAnnotationI()
        tag.textValue = rstring("tag %s" % uuid)
        tag = self.client.sf.getUpdateService().saveAndReturnObject(tag)

        img.linkAnnotation(tag)
        img = self.client.sf.getUpdateService().saveAndReturnObject(img)

        tagset = omero.model.TagAnnotationI()
        tagset.textValue = rstring("tagset %s" % uuid)
        tagset.linkAnnotation(tag)
        tagset = self.client.sf.getUpdateService().saveAndReturnObject(tagset)

        tag = tagset.linkedAnnotationList()[0]

        command = omero.cmd.Delete("/Annotation", tagset.id.val, None)
        handle = self.client.sf.submit(command)
        self.waitOnCmd(self.client, handle)

        assert not query.find("TagAnnotation", tagset.id.val)
        assert tag.id.val == query.find("TagAnnotation", tag.id.val).id.val

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

        with pytest.raises(omero.ServerError):
            self.client.sf.getQueryService().get("FileAnnotation", fa.id.val)

    def testDeleteOneDatasetFilesetErr(self):
        """
        Simple example of the MIF delete bad case:
        a single fileset containing 2 images is split among 2 datasets.
        Delete one dataset, delete fails.
        """
        client, user = self.new_client_and_user(perms="rw----")
        datasets = self.createDatasets(
            2, "testDeleteOneDatasetFilesetErr", client=client)
        images = self.importMIF(2, client=client)
        for i in range(2):
            self.link(datasets[i].proxy(), images[i].proxy(), client)

        query = client.sf.getQueryService()

        # Now delete one dataset
        delete = omero.cmd.Delete("/Dataset", datasets[0].id.val, None)
        self.doAllSubmit([delete], client)

        # The dataset should be deleted, but not any images.
        assert not query.find("Dataset", datasets[0].id.val)
        assert images[0].id.val == query.find("Image", images[0].id.val).id.val
        assert images[1].id.val == query.find("Image", images[1].id.val).id.val

    def testDeleteOneImageFilesetErr(self):
        """
        Simple example of the MIF delete good case:
        two images in a MIF.
        Delete one image, the delete should fail.
        """
        client, user = self.new_client_and_user(perms="rw----")
        images = self.importMIF(2, client=client)

        # Lookup the fileset
        query = client.sf.getQueryService()

        # Now delete one image
        delete = omero.cmd.Delete("/Image", images[0].id.val, None)
        self.doAllSubmit([delete], client, test_should_pass=False)

        # Neither image should be deleted.
        assert images[0].id.val == query.find("Image", images[0].id.val).id.val
        assert images[1].id.val == query.find("Image", images[1].id.val).id.val

    def testDeleteDatasetFilesetOK(self):
        """
        Simple example of the MIF delete good case:
        a single fileset containing 2 images in one dataset.
        Delete the dataset, the delete should succeed.
        """
        client, user = self.new_client_and_user(perms="rw----")
        query = client.sf.getQueryService()

        ds = self.make_dataset("testDeleteDatasetFilesetOK", client)
        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val

        for i in range(2):
            self.link(ds.proxy(), images[i].proxy(), client)

        # Now delete the dataset, should succeed
        delete = omero.cmd.Delete("/Dataset", ds.id.val, None)
        self.doAllSubmit([delete], client)

        # The dataset, fileset and both images should be deleted.
        assert not query.find("Dataset", ds.id.val)
        assert not query.find("Fileset", fsId)
        assert not query.find("Image", images[0].id.val)
        assert not query.find("Image", images[1].id.val)

    def testDeleteAllDatasetsFilesetOK(self):
        """
        Simple example of the MIF delete bad case:
        a single fileset containing 2 images is split among 2 datasets.
        Delete all datasets, delete succeeds.
        """
        client, user = self.new_client_and_user(perms="rw----")
        query = client.sf.getQueryService()

        datasets = self.createDatasets(
            2, "testDeleteAllDatasetsFilesetOK", client=client)
        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val

        for i in range(2):
            self.link(datasets[i].proxy(), images[i].proxy(), client)

        # Now delete all datasets, should succeed
        delete1 = omero.cmd.Delete("/Dataset", datasets[0].id.val, None)
        delete2 = omero.cmd.Delete("/Dataset", datasets[1].id.val, None)
        self.doAllSubmit([delete1, delete2], client)

        # Both datasets, the fileset and both images should be deleted.
        assert not query.find("Dataset", datasets[0].id.val)
        assert not query.find("Dataset", datasets[1].id.val)
        assert not query.find("Fileset", fsId)
        assert not query.find("Image", images[0].id.val)
        assert not query.find("Image", images[1].id.val)

    def testDeleteAllImagesFilesetOK(self):
        """
        Simple example of the MIF delete good case:
        two images in a MIF.
        Delete all images, the delete should succeed.
        """
        client, user = self.new_client_and_user(perms="rw----")
        query = client.sf.getQueryService()
        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val

        # Now delete all images, should succeed
        delete1 = omero.cmd.Delete("/Image", images[0].id.val, None)
        delete2 = omero.cmd.Delete("/Image", images[1].id.val, None)
        self.doAllSubmit([delete1, delete2], client)

        # The fileset and both images should be deleted.
        assert not query.find("Fileset", fsId)
        assert not query.find("Image", images[0].id.val)
        assert not query.find("Image", images[1].id.val)

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
        assert not query.find("Fileset", fsId)
        assert not query.find("Image", images[0].id.val)
        assert not query.find("Image", images[1].id.val)

    def testDeleteImagesTwoFilesetsErr(self):
        """
        If we try to partially delete 2 Filesets, both should be returned
        by the delete error
        """
        client, user = self.new_client_and_user(perms="rw----")
        # 2 filesets, each with 2 images
        imagesFsOne = self.importMIF(2, client=client)
        imagesFsTwo = self.importMIF(2, client=client)

        # delete should fail...
        delete1 = omero.cmd.Delete("/Image", imagesFsOne[0].id.val, None)
        delete2 = omero.cmd.Delete("/Image", imagesFsTwo[0].id.val, None)
        self.doAllSubmit([delete1, delete2], client, test_should_pass=False)

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

        ds = self.make_dataset("testDeleteDatasetTwoFilesetsErr", client)
        self.importMIF(2, client=client)
        for i in (imagesFsOne, imagesFsTwo):
            self.link(ds.proxy(), i[0].proxy(), client)

        # delete should remove only the Dataset
        delete = omero.cmd.Delete("/Dataset", ds.id.val, None)
        self.doAllSubmit([delete], client)

        query = client.sf.getQueryService()

        # The dataset should be deleted.
        assert not query.find("Dataset", ds.id.val)

        # Neither image should be deleted.
        for i in (imagesFsOne[0], imagesFsTwo[0]):
            assert i.id.val == query.find("Image", i.id.val).id.val

    def testDeleteProjectWithOneEmptyDataset(self):
        """
        P->D
        Delete P

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        query = self.client.sf.getQueryService()

        p = self.make_project()
        d = self.make_dataset()
        self.link(p, d)
        self.delete([p])

        assert not query.find("Project", p.id.val)
        assert not query.find("Dataset", d.id.val)

    def testDeleteProjectWithEmptyDatasetLinkedToAnotherProjectDefault(self):
        """
        P1->D
        P2->D
        Delete P1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        query = self.client.sf.getQueryService()

        p1 = self.make_project()
        p2 = self.make_project()
        d = self.make_dataset()
        self.link(p1, d)
        self.link(p2, d)
        self.delete([p1])

        assert query.find("Project", p2.id.val)
        assert not query.find("Project", p1.id.val)
        assert query.find("Dataset", d.id.val)

    def testDeleteProjectWithEmptyDatasetLinkedToAnotherProjectHard(self):
        """
        P1->D
        P2->D
        Delete P1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        query = self.client.sf.getQueryService()

        p1 = self.make_project()
        p2 = self.make_project()
        d = self.make_dataset()
        self.link(p1, d)
        self.link(p2, d)

        delete = omero.cmd.Delete(
            type="/Project", id=p1.id.val, options={"/Dataset": "HARD"})
        self.doSubmit(delete, self.client)

        assert query.find("Project", p2.id.val)
        assert not query.find("Project", p1.id.val)
        assert not query.find("Dataset", d.id.val)

    def testDeleteProjectWithDatasetLinkedToAnotherProject(self):
        """
        P1->D->I
        P2->D->I
        Delete P1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        p1 = self.make_project()
        p2 = self.make_project()
        d = self.make_dataset()
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(p1, d)
        self.link(p2, d)
        self.link(d, i)
        self.delete([p1])

        assert not query.find("Project", p1.id.val)
        assert query.find("Project", p2.id.val)
        assert query.find("Dataset", d.id.val)
        assert query.find("Image", i.id.val)

    def testDeleteDatasetLinkedToTwoProjects(self):
        """
        P1->D->I
        P2->D->I
        Delete D

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        p1 = self.make_project()
        p2 = self.make_project()
        d = self.make_dataset()
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(p1, d)
        self.link(p2, d)
        self.link(d, i)
        self.delete([d])

        assert query.find("Project", p1.id.val)
        assert query.find("Project", p2.id.val)
        assert not query.find("Image", i.id.val)
        assert not query.find("Dataset", d.id.val)

    def testDeleteDatasetWithImageLinkedToAnotherDatasetDefault(self):
        """
        D1->I
        D2->I
        Delete D1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        d1 = self.make_dataset()
        d2 = self.make_dataset()
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(d1, i)
        self.link(d2, i)
        self.delete([d1])

        assert not query.find("Dataset", d1.id.val)
        assert query.find("Dataset", d2.id.val)
        assert query.find("Image", i.id.val)

    def testDeleteDatasetWithImageLinkedToAnotherDatasetHard(self):
        """
        D1->I
        D2->I
        Delete D1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        d1 = self.make_dataset()
        d2 = self.make_dataset()
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(d1, i)
        self.link(d2, i)

        delete = omero.cmd.Delete(
            type="/Dataset", id=d1.id.val, options={"/Image": "HARD"})
        self.doSubmit(delete, self.client)

        assert not query.find("Dataset", d1.id.val)
        assert query.find("Dataset", d2.id.val)
        assert not query.find("Image", i.id.val)

    def testStepsDuringDelete(self):
        img = omero.model.ImageI()
        img.name = rstring("delete test")
        img.acquisitionDate = rtime(0)

        img = self.client.sf.getUpdateService().saveAndReturnObject(img)

        command = omero.cmd.Delete("/Image", img.id.val, None)
        handle = self.client.sf.submit(command)

        end_by = time() + 5

        latest_step = 0

        try:
            while time() < end_by:
                # still within five seconds of request submission
                status = handle.getStatus()
                # current step increases monotonically
                assert latest_step <= status.currentStep
                latest_step = status.currentStep
                if status.stopTime > 0:
                    # request stops after last step commenced
                    assert status.currentStep == status.steps - 1
                    return
        except:
            handle.close()

        raise Exception('delete did not complete within five seconds')


if __name__ == '__main__':
    if "TRACE" in os.environ:
        import trace
        tracer = trace.Trace(ignoredirs=[sys.prefix, sys.exec_prefix], trace=1)
        tracer.runfunc(pytest.main)
    else:
        pytest.main()
