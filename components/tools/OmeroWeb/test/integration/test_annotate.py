#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
Tests adding & removing annotations
"""

from builtins import str
import pytest
import omero
import omero.clients
from time import sleep
import json

from omeroweb.testlib import IWebTest
from omeroweb.testlib import get, post, get_json

from django.core.urlresolvers import reverse


class TestMapAnnotations(IWebTest):

    def annotate_dataset(self, django_client, keyValues,
                         ds_id=None, ns=None, ann_id=None):
        """
        Adds a new Map Annotation to Dataset or edits existing annotation

        @param keyValues - List of [key: value] pairs.
        """
        request_url = reverse('annotate_map')
        data = {
            'mapAnnotation': json.dumps(keyValues),
        }
        if ns is not None:
            data['ns'] = ns
        if ds_id is not None:
            data['dataset'] = ds_id
        if ann_id is not None:
            data['annId'] = ann_id
        post(django_client, request_url, data)

    def test_annotate_map(self):
        """Test we can create and retrieve map annotations, filter by ns."""

        # Create User in a Read-Annotate group
        client1, user1 = self.new_client_and_user(perms='rwrw--')
        omeName = client1.sf.getAdminService().getEventContext().userName
        django_client = self.new_django_client(omeName, omeName)

        # User1 creates Dataset
        ds = self.make_dataset("user1_Dataset", client=client1)

        # Add 2 map annotations, one with ns specfied (default is 'client' ns)
        ns = 'test.annotate.map.ns'
        map_data_ns = [['testKey', 'someValue'], ['ns', ns]]
        self.annotate_dataset(django_client, map_data_ns, ds.id.val, ns)
        client_map_data = [['expect', 'client'], ['ns', 'to be used']]
        self.annotate_dataset(django_client, client_map_data, ds.id.val)

        # check maps got added
        request_url = reverse('api_annotations')
        data = {
            "dataset": ds.id.val,
            "type": "map",
        }
        # get both map annotations
        rsp = get_json(django_client, request_url, data)
        assert len(rsp['annotations']) == 2

        # now filter by custom ns
        data['ns'] = ns
        rsp = get_json(django_client, request_url, data)
        assert len(rsp['annotations']) == 1
        # check essential values
        ann = rsp['annotations'][0]
        ann_id = ann['id']
        assert ann["values"] == map_data_ns
        assert ann["ns"] == ns
        assert ann["link"]["parent"]["id"] == ds.id.val

        # update map annotation
        new_data = [['new', 'data']]
        self.annotate_dataset(django_client, new_data, None, None, ann_id)
        rsp = get_json(django_client, request_url, data)
        assert rsp['annotations'][0]['values'] == new_data

        # delete map annotation (set data as empty list)
        self.annotate_dataset(django_client, [], None, None, ann_id)

        # only one left
        client_ns = omero.constants.metadata.NSCLIENTMAPANNOTATION
        del data['ns']
        rsp = get_json(django_client, request_url, data)
        assert len(rsp['annotations']) == 1
        ann = rsp['annotations'][0]
        assert ann["values"] == client_map_data
        assert ann["ns"] == client_ns


def annotate_tags_dataset(django_client, dsId, tagIds):
    """
    Links the specified dataset and tags
    """
    # 'newtags-0-description': '',
    # 'newtags-0-tag': 'foobar',
    # 'newtags-0-tagset': '',
    request_url = reverse('annotate_tags')
    data = {
        'dataset': dsId,
        'filter_mode': 'any',
        'filter_owner_mode': 'all',
        'index': 0,
        'newtags-INITIAL_FORMS': 0,
        'newtags-MAX_NUM_FORMS': 1000,
        'newtags-TOTAL_FORMS': 0,
        'tags': ",".join([str(i) for i in tagIds])
    }
    post(django_client, request_url, data)


class TestTagging(IWebTest):
    """
    Tests adding and removing Tags with annotate_tags()
    """

    def test_create_tag(self):
        # Create User and Dataset in a Read-Annotate group
        client, user = self.new_client_and_user(perms='rwrw--')
        omeName = client.sf.getAdminService().getEventContext().userName
        django_client = self.new_django_client(omeName, omeName)
        ds = self.make_dataset("test_create_tag", client=client)

        tagname = "test_create_tag"
        desc = "The description of the new tag"
        request_url = reverse('annotate_tags')
        data = {
            'dataset': ds.id.val,
            'filter_mode': 'any',
            'filter_owner_mode': 'all',
            'index': 0,
            'newtags-0-tag': tagname,
            'newtags-0-description': desc,
            'newtags-INITIAL_FORMS': 0,
            'newtags-MAX_NUM_FORMS': 1000,
            'newtags-TOTAL_FORMS': 1,
            'tags': "",
        }
        post(django_client, request_url, data)

        # Check tag exists on Dataset
        request_url = reverse('api_annotations')
        data = {"dataset": ds.id.val}
        rsp = get_json(django_client, request_url, data)
        tagNames = [t['textValue'] for t in rsp['annotations']]
        assert tagNames == [tagname]

    def test_annotate_tag(self):

        # Create User in a Read-Annotate group
        client1, user1 = self.new_client_and_user(perms='rwrw--')
        # conn = omero.gateway.BlitzGateway(client_obj=client1)
        omeName = client1.sf.getAdminService().getEventContext().userName
        django_client1 = self.new_django_client(omeName, omeName)

        # User1 creates Tag and Datasets
        ds = self.make_dataset("user1_Dataset", client=client1)
        tag = self.make_tag("test_annotate_tag", client=client1)

        # User2...
        groupId = client1.sf.getAdminService().getEventContext().groupId
        client2, user2 = self.new_client_and_user(
            group=omero.model.ExperimenterGroupI(groupId, False))
        # ...creates Tag
        tag2 = self.make_tag("user2_tag", client=client2)

        # User1 adds 2 tags to Dataset
        annotate_tags_dataset(django_client1, ds.id.val,
                              [tag.id.val, tag2.id.val])

        # check tags got added
        request_url = reverse('api_annotations')
        data = {
            "dataset": ds.id.val
        }
        rsp = get_json(django_client1, request_url, data)

        tagIds = [t['id'] for t in rsp['annotations']]
        assert tag.id.val in tagIds
        assert tag2.id.val in tagIds

        # We can remove tags by not including them
        # E.g. move from Right to Left column in the UI
        annotate_tags_dataset(django_client1, ds.id.val, [tag2.id.val])

        # Since tag link deletion is async, we need to wait to be sure that
        # tag is removed.
        sleep(1)
        rsp = get_json(django_client1, request_url, data)
        tagIds = [t['id'] for t in rsp['annotations']]
        assert tag.id.val not in tagIds
        assert tag2.id.val in tagIds


class TestBatchAnnotate(IWebTest):
    """
    Tests adding and removing Tags with annotate_tags()
    """

    def test_batch_annotate_tag(self):

        # Create User in a Read-Annotate group
        client1, user1 = self.new_client_and_user(perms='rwrw--')
        omeName = client1.sf.getAdminService().getEventContext().userName
        django_client = self.new_django_client(omeName, omeName)

        # User1 creates Tag and 2 Datasets
        ds1 = self.make_dataset("batch_Dataset1", client=client1)
        ds2 = self.make_dataset("batch_Dataset2", client=client1)
        tag1 = self.make_tag("test_batch_annotate1", client=client1)
        tag2 = self.make_tag("test_batch_annotate2", client=client1)

        # Batch Annotate panel should have 'Tags'
        request_url = reverse('batch_annotate')
        request_url += '?dataset=%s&dataset=%s' % (ds1.id.val, ds2.id.val)
        rsp = get(django_client, request_url)
        assert b'2 objects' in rsp.content
        # can't check for 'Tags 0' since html splits these up
        assert b'<span class="annotationCount">0</span>' in rsp.content
        assert b'<span class="annotationCount">1</span>' not in rsp.content

        # Add 1 Tag to 1 Dataset
        annotate_tags_dataset(django_client, ds1.id.val, [tag1.id.val])
        rsp = get(django_client, request_url)
        assert b'<span class="annotationCount">1</span>' in rsp.content

        # Add 2 Tags to other Dataset
        annotate_tags_dataset(django_client, ds2.id.val,
                              [tag1.id.val, tag2.id.val])
        rsp = get(django_client, request_url)
        assert b'<span class="annotationCount">2</span>' in rsp.content


class TestFileAnnotations(IWebTest):
    """
    Tests listing file annotations
    """

    def test_add_fileannotations_form(self):

        # Create User in a Read-Annotate group
        client, user = self.new_client_and_user(perms='rwrw--')
        # conn = omero.gateway.BlitzGateway(client_obj=client)
        omeName = client.sf.getAdminService().getEventContext().userName
        django_client1 = self.new_django_client(omeName, omeName)

        # User creates Dataset
        ds = self.make_dataset("user1_Dataset", client=client)

        # Create File and FileAnnotation
        update = client.sf.getUpdateService()
        f = omero.model.OriginalFileI()
        f.name = omero.rtypes.rstring("")
        f.path = omero.rtypes.rstring("")
        f = update.saveAndReturnObject(f)
        fa = omero.model.FileAnnotationI()
        fa.setFile(f)
        fa = update.saveAndReturnObject(fa)

        # get form for annotating Dataset
        request_url = reverse('annotate_file')
        data = {
            "dataset": ds.id.val
        }
        rsp = get(django_client1, request_url, data)
        html = rsp.content

        expected_name = b"No name. ID %d" % fa.id.val
        assert expected_name in html

    @pytest.mark.parametrize("link_count", [1, 2])
    def test_batch_add_fileannotations(self, link_count):
        """Test adding file annotation to Project(s)."""
        client, user = self.new_client_and_user(perms='rwrw--')
        omeName = client.sf.getAdminService().getEventContext().userName
        django_client1 = self.new_django_client(omeName, omeName)

        # User creates 2 Projects
        pr1 = self.make_project("test_batch_file_ann1", client=client)
        pr2 = self.make_project("test_batch_file_ann2", client=client)
        pr_ids = [pr1.id.val, pr2.id.val]

        # Create File and FileAnnotation
        fname = "fname_%s" % client.getSessionId()
        update = client.sf.getUpdateService()
        f = omero.model.OriginalFileI()
        f.name = omero.rtypes.rstring(fname)
        f.path = omero.rtypes.rstring("")
        f = update.saveAndReturnObject(f)
        fa = omero.model.FileAnnotationI()
        fa.setFile(f)
        fa = update.saveAndReturnObject(fa)

        # get form for annotating both Projects
        request_url = reverse('annotate_file')
        data = {
            "project": pr_ids
        }
        rsp = get(django_client1, request_url, data)
        html = rsp.content.decode('utf-8')
        assert fname in html

        # Link File Annotation to 1 or 2 Projects
        post_data = {
            "project": pr_ids[0: link_count],
            "files": [fa.id.val]
        }
        post(django_client1, request_url, post_data)

        # Check for link to first Project
        api_ann_url = reverse('api_annotations')
        rsp = get_json(django_client1, api_ann_url, {"project": pr1.id.val})
        assert fa.id.val in [a['id'] for a in rsp['annotations']]

        # Annotation Form should NOT show file if linked to BOTH projects
        show_file = link_count == 1
        rsp = get(django_client1, request_url, data)
        html = rsp.content.decode('utf-8')
        assert (fname in html) == show_file

        # Remove file from both Projects
        remove_url = reverse('manage_action_containers',
                             kwargs={'action': 'remove', 'o_type': 'file',
                                     'o_id': fa.id.val})
        remove_data = {'parent': 'project-%s|project-%s' % (pr1.id.val,
                                                            pr2.id.val)}
        post(django_client1, remove_url, remove_data)

        # Check for NO link
        rsp = get_json(django_client1, api_ann_url, {"project": pr1.id.val})
        assert fa.id.val not in [a['id'] for a in rsp['annotations']]
