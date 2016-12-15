#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2014-2015 Glencoe Software, Inc.
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
Simple integration tests to ensure that the CSRF middleware is enabled and
working correctly.
"""

import omero
import omero.clients
from omero.rtypes import rstring
from omeroweb.testlib import IWebTest
from omeroweb.testlib import _csrf_post_response, _post_response
from omeroweb.testlib import _csrf_get_response, _get_response

from django.test import Client
from django.core.urlresolvers import reverse


try:
    from PIL import Image, ImageDraw  # see ticket:2597
except ImportError:  # see ticket:2597
    import Image
    import ImageDraw

from random import randint as rint
import tempfile


class TestCsrf(IWebTest):
    """
    Tests to ensure that Django CSRF middleware for OMERO.web is enabled and
    working correctly.
    """

    def image_with_channels(self):
        """
        Returns a new foundational Image with Channel objects attached for
        view method testing.
        """
        pixels = self.pix(client=self.client)
        for the_c in range(pixels.getSizeC().val):
            channel = omero.model.ChannelI()
            channel.logicalChannel = omero.model.LogicalChannelI()
            pixels.addChannel(channel)
        image = pixels.getImage()
        return self.sf.getUpdateService().saveAndReturnObject(image)

    def new_tag(self):
        """
        Returns a new Tag objects
        """
        tag = omero.model.TagAnnotationI()
        tag.textValue = rstring(self.uuid())
        tag.ns = rstring("pytest")
        return self.sf.getUpdateService().saveAndReturnObject(tag)

    # Client
    def test_csrf_middleware_enabled(self):
        """
        If the CSRF middleware is enabled login attempts that do not include
        the CSRF token should fail with an HTTP 403 (forbidden) status code.
        """
        # https://docs.djangoproject.com/en/dev/ref/contrib/csrf/#testing
        django_client = Client(enforce_csrf_checks=True)

        data = {
            'server': 1,
            'username': self.client.getProperty('omero.user'),
            'password': self.client.getProperty('omero.pass')
        }
        login_url = reverse('weblogin')
        _post_response(django_client, login_url, data)

        logout_url = reverse('weblogout')
        _post_response(django_client, logout_url, {})

    def test_forgot_password(self):

        request_url = reverse('waforgottenpassword')
        data = {
            'username': "omename",
            'email': "email"
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

    def test_move_data(self):

        group_id = self.new_group(experimenters=[self.user]).id.val

        request_url = reverse('chgrp')
        data = {
            'image': self.image_with_channels().id.val,
            'group_id': group_id
        }

        _post_response(self.django_root_client, request_url, data)
        _csrf_post_response(self.django_root_client, request_url, data)

    def test_add_and_remove_comment(self):

        request_url = reverse('annotate_comment')
        data = {
            'comment': 'foobar',
            'image': self.image_with_channels().id.val
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

        # Remove comment, see remove tag,
        # http://localhost/webclient/action/remove/[comment|tag|file]/ID/

    def test_attach_file(self):

        # Due to EOF both posts must be test separately
        # Bad post
        img = self.image_with_channels()
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.csrf')
            temp.write("Testing without csrf token")
            temp.seek(0)

            request_url = reverse('annotate_file')
            data = {
                'image': img.id.val,
                'index': 0,
                'annotation_file': temp
            }
            _post_response(self.django_client, request_url, data)
        finally:
            temp.close()

        # Good post
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.csrf')
            temp.write("Testing csrf token")
            temp.seek(0)

            request_url = reverse('annotate_file')
            data = {
                'image': img.id.val,
                'index': 0,
                'annotation_file': temp
            }
            _csrf_post_response(self.django_client, request_url, data)
        finally:
            temp.close()

        # link existing annotation is handled by the same request.

        # Remove file, see remove tag,
        # http://localhost/webclient/action/remove/[comment|tag|file]/ID/

    def test_edit_channel_names(self):

        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        img = self.image_with_channels()
        query_string = data = {'channel0': 'foobar'}
        request_url = reverse(
            'edit_channel_names', args=[img.id.val]
        )
        _csrf_get_response(self.django_client, request_url, query_string,
                           status_code=405)
        _csrf_post_response(self.django_client, request_url, data)

    def test_copy_past_rendering_settings(self):

        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = self.createTestImage(session=self.sf)

        # put image id into session
        session = self.django_client.session
        session['fromid'] = img.id.val
        session.save()

        request_url = reverse('webgateway.views.copy_image_rdef_json')
        data = {
            'toids': img.id.val
        }

        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)
        _csrf_get_response(self.django_client, request_url, data,
                           status_code=405)

    def test_reset_rendering_settings(self):

        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = self.createTestImage(session=self.sf)

        # Reset through webclient as it is calling directly
        # webgateway.reset_image_rdef_json
        request_url = reverse('reset_rdef_json')
        data = {
            'toids': img.id.val,
            'to_type': 'image'
        }

        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)
        _get_response(self.django_client, request_url, data)
        _csrf_get_response(self.django_client, request_url, data,
                           status_code=405)

    def test_apply_owners_rendering_settings(self):

        img = self.createTestImage(session=self.sf)

        request_url = reverse('reset_owners_rdef_json')
        data = {
            'toids': img.id.val,
            'to_type': 'image'
        }

        _post_response(self.django_client, request_url, data,
                       status_code=403)
        _csrf_post_response(self.django_client, request_url, data)

    def test_ome_tiff_script(self):

        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = self.createTestImage(session=self.sf)

        request_url = reverse('ome_tiff_script', args=[img.id.val])

        _post_response(self.django_client, request_url, {})
        _csrf_post_response(self.django_client, request_url, {})
        _csrf_get_response(self.django_client, request_url, {},
                           status_code=405)

    def test_script(self):

        img = self.createTestImage(session=self.sf)

        script_path = "omero/export_scripts/Batch_Image_Export.py"
        script = self.sf.getScriptService().getScriptID(script_path)

        request_url = reverse('script_run', args=[script])
        data = {
            "Data_Type": "Image",
            "IDs": img.id.val,
            "Choose_T_Section": "Default-T (last-viewed)",
            "Choose_Z_Section": "Default-Z (last-viewed)",
            "Export_Individual_Channels": "on",
            "Export_Merged_Image": "on",
            "Folder_Name": "Batch_Image_Export",
            "Format": "JPEG",
            "Zoom": "100%"
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

    # ADMIN
    def test_myaccount(self):

        request_url = reverse('wamyaccount', args=["save"])
        data = {
            "omename": self.user.omeName.val,
            "first_name": self.user.omeName.val,
            "last_name": self.user.lastName.val,
            "institution": "foo bar",
            "default_group": self.group.id.val
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data,
                            status_code=302)

    def test_avatar(self):

        user_id = self.user.id.val

        # Due to EOF both posts must be test separately
        # Bad post
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.png')

            img = Image.new("RGB", (200, 200), "#FFFFFF")
            draw = ImageDraw.Draw(img)

            r, g, b = rint(0, 255), rint(0, 255), rint(0, 255)
            for i in range(200):
                draw.line((i, 0, i, 200), fill=(int(r), int(g), int(b)))
            img.save(temp, "PNG")
            temp.seek(0)

            request_url = reverse('wamanageavatar', args=[user_id, "upload"])
            data = {
                'filename': 'avatar.png',
                "photo": temp
            }
            _post_response(self.django_client, request_url, data)
        finally:
            temp.close()

        # Good post
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.png')

            img = Image.new("RGB", (200, 200), "#FFFFFF")
            draw = ImageDraw.Draw(img)

            r, g, b = rint(0, 255), rint(0, 255), rint(0, 255)
            for i in range(200):
                draw.line((i, 0, i, 200), fill=(int(r), int(g), int(b)))
            img.save(temp, "PNG")
            temp.seek(0)

            request_url = reverse('wamanageavatar', args=[user_id, "upload"])
            data = {
                'filename': 'avatar.png',
                "photo": temp
            }
            _csrf_post_response(self.django_client, request_url, data,
                                status_code=302)
        finally:
            temp.close()

        # Crop avatar
        request_url = reverse('wamanageavatar', args=[user_id, "crop"])
        data = {
            'x1': 50,
            'x2': 150,
            'y1': 50,
            'y2': 150
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data,
                            status_code=302)

    def test_create_group(self):
        uuid = self.uuid()
        request_url = reverse('wamanagegroupid', args=["create"])
        data = {
            "name": uuid,
            "description": uuid,
            "permissions": 0
        }
        _post_response(self.django_root_client, request_url, data)
        _csrf_post_response(self.django_root_client, request_url, data,
                            status_code=302)

    def test_create_user(self):
        uuid = self.uuid()
        groupid = self.new_group().id.val
        request_url = reverse('wamanageexperimenterid', args=["create"])
        data = {
            "omename": uuid,
            "first_name": uuid,
            "last_name": uuid,
            "active": "on",
            "default_group": groupid,
            "other_groups": groupid,
            "password": uuid,
            "confirmation": uuid
        }
        _post_response(self.django_root_client, request_url, data)
        _csrf_post_response(self.django_root_client, request_url, data,
                            status_code=302)

    def test_edit_group(self):
        group = self.new_group(perms="rw----")
        request_url = reverse('wamanagegroupid', args=["save", group.id.val])
        data = {
            "name": group.name.val,
            "description": "description",
            "permissions": 0
        }
        _post_response(self.django_root_client, request_url, data)
        _csrf_post_response(self.django_root_client, request_url, data,
                            status_code=302)

    def test_edit_user(self):
        user = self.new_user()
        request_url = reverse('wamanageexperimenterid',
                              args=["save", user.id.val])
        data = {
            "omename": user.omeName.val,
            "first_name": user.firstName.val,
            "last_name": user.lastName.val,
            "default_group": user.copyGroupExperimenterMap()[0].parent.id.val,
            "other_groups": user.copyGroupExperimenterMap()[0].parent.id.val,
        }
        _post_response(self.django_root_client, request_url, data)
        _csrf_post_response(self.django_root_client, request_url, data,
                            status_code=302)

    def test_edit_group_by_owner(self):

        self.add_groups(experimenter=self.user, groups=[self.group],
                        owner=True)

        request_url = reverse('wamanagegroupownerid',
                              args=["save", self.group.id.val])
        data = {
            "members": self.user.id.val,
            "owners": self.user.id.val,
            "permissions": 0
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data,
                            status_code=302)

    def test_change_password(self):
        user = self.new_user()
        request_url = reverse('wamanagechangepasswordid', args=[user.id.val])
        data = {
            "old_password": self.root.ic.getProperties().getProperty(
                'omero.rootpass'),
            "password": "new",
            "confirmation": "new"
        }
        _post_response(self.django_root_client, request_url, data)
        _csrf_post_response(self.django_root_client, request_url, data)

    def test_su(self):

        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        user = self.new_user()

        request_url = reverse('webgateway_su', args=[user.omeName.val])

        _csrf_get_response(self.django_root_client, request_url, {})
        _post_response(self.django_root_client, request_url, {})
        _csrf_post_response(self.django_root_client, request_url, {})
