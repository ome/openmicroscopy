#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2026 University of Dundee & Open Microscopy Environment.
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

"""Tests querying of Annotations with json api."""

from omeroweb.testlib import IWebTest, get_json
from django.urls import reverse
from omeroweb.api import api_settings
import pytest
import omero
from omero.model import ProjectI, DatasetI, OriginalFileI
from omero.gateway import BlitzGateway, TagAnnotationWrapper, \
    FileAnnotationWrapper, OriginalFileWrapper, \
    DatasetWrapper, ProjectWrapper
from omero.rtypes import wrap


def build_url(client, url_name, url_kwargs):
    """Build an absolute url using client response url."""
    response = client.request()
    # http://testserver/webclient/
    webclient_url = response.url
    url = reverse(url_name, kwargs=url_kwargs)
    url = webclient_url.replace("/webclient/", url)
    return url


class TestAnnotations(IWebTest):
    """Tests querying Annotations."""

    ns = "test_get_objects_annotation_comment"
    ns_map = "test_get_objects_map_namespace"
    ns_file = "test_get_objects_annotation_file"
    ann_types = ["CommentAnnotation", "TagAnnotation",
                 "LongAnnotation", "XmlAnnotation",
                 "DoubleAnnotation", "BooleanAnnotation",
                 "TimestampAnnotation", "TermAnnotation"]

    @pytest.fixture()
    def user1(self):
        """Return a new user in a read-annotate group."""
        group = self.new_group(perms="rwra--")
        user = self.new_client_and_user(group=group)
        return user

    @pytest.fixture()
    def dataset_anns(self, user1):
        """Create a bunch of Annotations."""
        conn = BlitzGateway(client_obj=user1[0])
        update = conn.getUpdateService()

        def create_dataset_with_annotations(name, dtype="Dataset"):
            if dtype == "Dataset":
                obj = DatasetI()
                obj.name = wrap(name)
                obj = update.saveAndReturnObject(obj)
                wrapper = DatasetWrapper(conn, obj)
            elif dtype == "Project":
                obj = ProjectI()
                obj.name = wrap(name)
                obj = update.saveAndReturnObject(obj)
                wrapper = ProjectWrapper(conn, obj)

            # Create total of 10 annotations on the object...
            # create Comment, Tag, Xml, etc
            for ann_type in self.ann_types:
                ann = getattr(omero.gateway, ann_type + "Wrapper")(conn)
                ann.setNs(self.ns)
                if ann_type in ("LongAnnotation", "DoubleAnnotation",
                                "TimestampAnnotation"):
                    ann.setValue(100.0)
                elif ann_type == "BooleanAnnotation":
                    ann.setValue(True)
                else:
                    ann.setValue("Test %s: %s" % (ann_type, name))
                wrapper.linkAnnotation(ann)

            # create MapAnnotation
            mapAnn = omero.gateway.MapAnnotationWrapper(conn)
            mapAnn.setValue([("key1", "value1"), ("key2", "value2")])
            mapAnn.setNs(self.ns_map)
            mapAnn.save()
            wrapper.linkAnnotation(mapAnn)

            # create FileAnnotation
            fileAnn = FileAnnotationWrapper(conn)
            fileObj = OriginalFileI()
            fileObj = OriginalFileWrapper(conn, fileObj)
            fileObj.setName(wrap("fileName"))
            fileObj.setPath(wrap("path/to/file"))
            fileObj.setHash(wrap("a"))
            fileObj.setSize(omero.rtypes.rlong(0))
            fileObj.save()
            fileAnn.setFile(fileObj)
            fileAnn.setNs(self.ns_file)
            fileAnn.save()
            wrapper.linkAnnotation(fileAnn)

            return wrapper

        dataset1 = create_dataset_with_annotations("Dataset 1")
        dataset2 = create_dataset_with_annotations("Dataset 2")
        project1 = create_dataset_with_annotations("Project 1",
                                                   dtype="Project")

        # Also add Tag to the Group
        tag = TagAnnotationWrapper(conn)
        tag.setNs("test_get_objects_group_tag")
        tag.setValue("Test Tag on Group")
        group = conn.getGroupFromContext()
        group.linkAnnotation(tag)

        return dataset1, dataset2, project1

    def test_dataset_anns(self, user1, dataset_anns):
        """Test Annotations on Datasets and Projects."""
        dataset1, dataset2, project1 = dataset_anns
        conn = BlitzGateway(client_obj=user1[0])
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]

        # Tests below simply check the NUMBER of Annotations returned,
        # not the content of the Annotations themselves.

        # List ALL annotations
        annotations_url = reverse("api_annotations",
                                  kwargs={"api_version": version})
        rsp = get_json(client, annotations_url)
        assert len(rsp["data"]) >= 31

        # get all annotations on one Dataset
        rsp = get_json(client, annotations_url, {"dataset": dataset1.id})
        assert len(rsp["data"]) == 10

        # annotation on group
        group_id = conn.getEventContext().groupId
        rsp = get_json(client, annotations_url,
                       {"experimentergroup": group_id})
        assert len(rsp["data"]) == 1

        # No annotations on PlateAcquisition (none created)
        rsp = get_json(client, annotations_url, {"plateacquisition": 1})
        assert len(rsp["data"]) == 0

        # We only want Tags on the Dataset
        rsp = get_json(client, annotations_url,
                       {"dataset": dataset1.id, "type": "tag"})
        assert len(rsp["data"]) == 1
        # alternative URL for TagAnnotations
        tagannotations_url = reverse("api_namedannotations",
                                     kwargs={"api_version": version,
                                             "ann_type": "tag"})
        rsp = get_json(client, tagannotations_url, {"dataset": dataset1.id})
        assert len(rsp["data"]) == 1

        # filter by namespace
        rsp = get_json(client, annotations_url, {"ns": self.ns})
        assert len(rsp["data"]) == 24

        # filter by namespace and type
        for ann_type in self.ann_types:
            short_type = ann_type.replace("Annotation", "").lower()
            rsp = get_json(client, annotations_url,
                           {"type": short_type, "ns": self.ns})
            assert len(rsp["data"]) == 3
            rsp = get_json(client, annotations_url,
                           {"type": short_type, "ns": self.ns_map})
            assert len(rsp["data"]) == 0

        # test File Annotation is loaded
        rsp = get_json(client, annotations_url,
                       {"dataset": dataset1.id, "type": "file"})
        assert len(rsp["data"]) == 1
        assert rsp["data"][0]["File"]["path"] == "path/to/file"
