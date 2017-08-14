#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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
Tests chgrp functionality of views.py
"""

from omero.model import ProjectI, DatasetI, TagAnnotationI
from omero.rtypes import rstring
from omero.gateway import BlitzGateway

import pytest
import time
from omeroweb.testlib import IWebTest, post, get_json
from django.core.urlresolvers import reverse
import json

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANNOTATE = 'rwra--'
COLLAB = 'rwrw--'


class TestChgrp(IWebTest):
    """
    Tests chgrp
    """

    @classmethod
    def setup_class(cls):
        """Returns a logged in Django test client."""
        super(TestChgrp, cls).setup_class()
        # Add user to secondary group
        cls.group2 = cls.new_group(
            experimenters=[cls.ctx.userName], perms=PRIVATE)
        # Refresh client
        cls.ctx = cls.sf.getAdminService().getEventContext()
        cls.django_client = cls.new_django_client(
            cls.ctx.userName, cls.ctx.userName)

    def get_django_client(self, credentials):
        if credentials == 'user':
            return self.django_client
        else:
            return self.django_root_client

    @pytest.fixture
    def dataset(self):
        """Returns a new OMERO Project with required fields set."""
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
        tag.textValue = rstring("ChgrpTag")
        project1.linkAnnotation(tag)
        project2.linkAnnotation(tag)
        return self.update.saveAndReturnArray([project1, project2])

    @pytest.mark.parametrize("credentials", ['user', 'admin'])
    def test_load_chgrp_groups(self, project, credentials):
        """
        A user in 2 groups should have options to move object
        from one group to another.
        """
        django_client = self.get_django_client(credentials)
        request_url = reverse('load_chgrp_groups')
        data = {
            "Project": project.id.val
        }
        data = get_json(django_client, request_url, data)

        assert 'groups' in data
        assert len(data['groups']) == 1
        assert data['groups'][0]['id'] == self.group2.id.val

    @pytest.mark.parametrize("credentials", ['user'])  # TODO - add 'admin'
    def test_chgrp_dry_run(self, projects_dataset_image_tag, credentials):
        """
        Performs a chgrp POST, polls the activities json till done,
        then checks that Dataset has moved to new group and has new
        Project as parent.
        """

        def doDryRun(data):
            request_url = reverse('chgrpDryRun')
            rsp = post(django_client, request_url, data)
            jobId = rsp.content
            # Keep polling activities until dry-run job completed
            activities_url = reverse('activities_json')
            data = {'jobId': jobId}
            rsp = get_json(django_client, activities_url, data)
            while rsp['finished'] is not True:
                time.sleep(0.5)
                rsp = get_json(django_client, activities_url, data)
            return rsp

        django_client = self.get_django_client(credentials)
        pdit = projects_dataset_image_tag
        projectId = pdit[0].id.val
        projectId2 = pdit[1].id.val
        dataset, = pdit[0].linkedDatasetList()
        image, = dataset.linkedImageList()
        tag, = pdit[0].linkedAnnotationList()

        # If we try to move single Project, Dataset, Tag remain
        data = {
            "group_id": self.group2.id.val,
            "Project": projectId
        }
        rsp = doDryRun(data)
        unlinked = {'Files': [],
                    'Tags': [{'id': tag.id.val,
                              'name': 'ChgrpTag'}],
                    'Datasets': [{'id': dataset.id.val,
                                  'name': dataset.name.val}],
                    'Comments': 0, 'Others': 0}
        assert 'includedObjects' in rsp
        assert rsp['includedObjects'] == {'Projects': [projectId]}
        assert 'unlinkedDetails' in rsp
        assert rsp['unlinkedDetails'] == unlinked

        # If we try to move both Projects all data moves
        data = {
            "group_id": self.group2.id.val,
            "Project": "%s,%s" % (projectId, projectId2)
        }
        rsp = doDryRun(data)
        pids = [projectId, projectId2]
        pids.sort()
        assert rsp['includedObjects'] == {'Projects': pids,
                                          'Datasets': [dataset.id.val],
                                          'Images': [image.id.val]}
        assert rsp['unlinkedDetails'] == {'Files': [], 'Tags': [],
                                          'Comments': 0, 'Others': 0}

    @pytest.mark.parametrize("credentials", ['user', 'admin'])
    def test_chgrp_new_container(self, dataset, credentials):
        """
        Performs a chgrp POST, polls the activities json till done,
        then checks that Dataset has moved to new group and has new
        Project as parent.
        """

        django_client = self.get_django_client(credentials)
        request_url = reverse('chgrp')
        projectName = "chgrp-project%s" % (self.uuid())
        data = {
            "group_id": self.group2.id.val,
            "Dataset": dataset.id.val,
            "new_container_name": projectName,
            "new_container_type": "project",
        }
        rsp = post(django_client, request_url, data)
        data = json.loads(rsp.content)
        expected = {"update": {"childless": {"project": [],
                                             "orphaned": False,
                                             "dataset": []},
                               "remove": {"project": [],
                                          "plate": [],
                                          "screen": [],
                                          "image": [],
                                          "dataset": [dataset.id.val]}}}
        assert data == expected

        activities_url = reverse('activities_json')

        data = get_json(django_client, activities_url)

        # Keep polling activities until no jobs in progress
        while data['inprogress'] > 0:
            time.sleep(0.5)
            data = get_json(django_client, activities_url)

        # individual activities/jobs are returned as dicts within json data
        for k, o in data.items():
            if hasattr(o, 'values'):    # a dict
                if 'report' in o:
                    print o['report']
                assert o['status'] == 'finished'
                assert o['job_name'] == 'Change group'
                assert o['to_group_id'] == self.group2.id.val

        # Dataset should now be in new group, contained in new Project
        conn = BlitzGateway(client_obj=self.client)
        userId = conn.getUserId()
        conn.SERVICE_OPTS.setOmeroGroup('-1')
        d = conn.getObject("Dataset", dataset.id.val)
        assert d is not None
        assert d.getDetails().group.id.val == self.group2.id.val
        p = d.getParent()
        assert p is not None
        assert p.getName() == projectName
        # Project owner should be current user
        assert p.getDetails().owner.id.val == userId

    @pytest.mark.parametrize("credentials", ['user', 'admin'])
    def test_chgrp_old_container(self, dataset, credentials):
        """
        Tests Admin moving user's Dataset to their Private group and
        linking it to an existing Project there.
        Bug from https://github.com/openmicroscopy/openmicroscopy/pull/3420
        """

        django_client = self.get_django_client(credentials)
        # user creates project in their target group
        project = ProjectI()
        projectName = "chgrp-target-%s" % self.client.getSessionId()
        project.name = rstring(projectName)
        ctx = {"omero.group": str(self.group2.id.val)}
        project = self.sf.getUpdateService().saveAndReturnObject(project, ctx)
        request_url = reverse('chgrp')

        data = {
            "group_id": self.group2.id.val,
            "Dataset": dataset.id.val,
            "target_id": "project-%s" % project.id.val,
        }
        rsp = post(django_client, request_url, data)
        data = json.loads(rsp.content)
        expected = {"update": {"childless": {"project": [],
                                             "orphaned": False,
                                             "dataset": []},
                               "remove": {"project": [],
                                          "plate": [],
                                          "screen": [],
                                          "image": [],
                                          "dataset": [dataset.id.val]}}}
        assert data == expected

        activities_url = reverse('activities_json')

        data = get_json(django_client, activities_url)

        # Keep polling activities until no jobs in progress
        while data['inprogress'] > 0:
            time.sleep(0.5)
            data = get_json(django_client, activities_url)

        # individual activities/jobs are returned as dicts within json data
        for k, o in data.items():
            if hasattr(o, 'values'):    # a dict
                if 'report' in o:
                    print o['report']
                assert o['status'] == 'finished'
                assert o['job_name'] == 'Change group'
                assert o['to_group_id'] == self.group2.id.val

        # Dataset should now be in new group, contained in Project
        conn = BlitzGateway(client_obj=self.client)
        userId = conn.getUserId()
        conn.SERVICE_OPTS.setOmeroGroup('-1')
        d = conn.getObject("Dataset", dataset.id.val)
        assert d is not None
        assert d.getDetails().group.id.val == self.group2.id.val
        p = d.getParent()
        assert p is not None
        assert p.getName() == projectName
        # Project owner should be current user
        assert p.getDetails().owner.id.val == userId
        assert p.getId() == project.id.val
