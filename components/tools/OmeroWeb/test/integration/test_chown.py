#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
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
Tests chown functionality of views.py
"""

from omero.model import ProjectI, DatasetI, TagAnnotationI
from omero.rtypes import rstring

import pytest
import time
from omeroweb.testlib import IWebTest, post, get_json
from django.core.urlresolvers import reverse

READANNOTATE = 'rwra--'


class TestChown(IWebTest):
    """
    Tests chown
    """

    DEFAULT_PERMS = READANNOTATE

    @classmethod
    def setup_class(cls):
        """Returns a logged in Django test client."""
        super(TestChown, cls).setup_class()
        # Add 2nd user to group
        gid = cls.sf.getAdminService().getEventContext().groupId
        cls.user2 = cls.new_user(group=gid)
        # Refresh client
        cls.ctx = cls.sf.getAdminService().getEventContext()
        cls.django_client = cls.new_django_client(cls.ctx.userName,
                                                  cls.ctx.userName)

    def get_django_client(self, credentials):
        if credentials == "user":
            return self.django_client
        else:
            return self.django_root_client

    @pytest.fixture
    def dataset(self):
        """Returns a new OMERO Dataset with required fields set."""
        dataset = DatasetI()
        dataset.name = rstring(self.uuid())
        return self.update.saveAndReturnObject(dataset)

    @pytest.fixture
    def project(self):
        """Returns a new OMERO Project with required fields set."""
        project = ProjectI()
        project.name = rstring(self.uuid())
        return self.update.saveAndReturnObject(project)

    @pytest.fixture
    def projects_dataset_image_tag(self):
        """
        Returns 2 new OMERO Projects, linked Dataset and linked Image populated
        by an L{test.integration.library.ITest} instance with required fields
        set. Also a Tag linked to both Projects.
        """
        project1 = ProjectI()
        project1.name = rstring(self.uuid())
        project2 = ProjectI()
        project2.name = rstring(self.uuid())
        dataset = DatasetI()
        dataset.name = rstring(self.uuid())
        image = self.new_image(name=self.uuid())
        dataset.linkImage(image)
        project1.linkDataset(dataset)
        project2.linkDataset(dataset)
        tag = TagAnnotationI()
        tag.textValue = rstring("ChownTag")
        project1.linkAnnotation(tag)
        project2.linkAnnotation(tag)
        return self.update.saveAndReturnArray([project1, project2])

    @pytest.mark.parametrize("credentials", ["admin"])
    def test_chown_dry_run(self, projects_dataset_image_tag, credentials):
        """
        Performs a chown POST, polls the activities json till done,
        then checks that Dataset has moved to new group and has new
        Project as parent.
        """

        def doDryRun(data):
            request_url = reverse("chownDryRun")
            rsp = post(django_client, request_url, data)
            jobId = rsp.content
            # Keep polling activities until dry-run job completed
            activities_url = reverse("activities_json")
            data = {"jobId": jobId}
            rsp = get_json(django_client, activities_url, data)
            while rsp["finished"] is not True:
                time.sleep(0.5)
                rsp = get_json(django_client, activities_url, data)
            return rsp

        django_client = self.get_django_client(credentials)
        pdit = projects_dataset_image_tag
        projectId = pdit[0].id.val
        projectId2 = pdit[1].id.val
        (dataset,) = pdit[0].linkedDatasetList()
        (image,) = dataset.linkedImageList()
        (tag,) = pdit[0].linkedAnnotationList()

        # If we try to move single Project, Dataset, Tag remain
        data = {"owner_id": self.user2.id.val, "Project": projectId}
        rsp = doDryRun(data)
        unlinked_anns = {
            'Files': [],
            'Tags': [],
            'Comments': [],
            'Others': 0
        }
        assert rsp['includedObjects'] == {'Projects': [projectId]}
        assert rsp['unlinkedAnnotations'] == unlinked_anns
        assert rsp['unlinkedChildren'] == {'Datasets': [{'id': dataset.id.val,
                                                         'name': dataset.name.val}]}
        assert rsp['unlinkedParents'] == {}

        # If we try to move both Projects all data moves
        data = {
            "owner_id": self.user2.id.val,
            "Project": "%s,%s" % (projectId, projectId2),
        }
        rsp = doDryRun(data)
        pids = [projectId, projectId2]
        pids.sort()
        assert rsp['includedObjects'] == {'Projects': pids,
                                          'Datasets': [dataset.id.val],
                                          'Images': [image.id.val]}
        assert rsp['unlinkedAnnotations'] == {'Files': [], 'Tags': [],
                                              'Comments': [], 'Others': 0}
        assert rsp['unlinkedChildren'] == {}
        assert rsp['unlinkedParents'] == {}

        # Move just the Dataset - Both Projects remain
        data = {
            "owner_id": self.user2.id.val,
            "Dataset": dataset.id.val
        }
        rsp = doDryRun(data)
        projs = [{'id': p.id.val, 'name': p.name.val} for p in pdit]
        assert rsp['includedObjects'] == {'Datasets': [dataset.id.val],
                                          'Images': [image.id.val]}
        assert rsp['unlinkedAnnotations'] == {'Files': [], 'Tags': [],
                                              'Comments': [], 'Others': 0}
        assert rsp['unlinkedChildren'] == {}
        assert rsp['unlinkedParents'] == {'Projects': projs}
