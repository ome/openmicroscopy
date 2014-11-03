#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2014 Glencoe Software, Inc.
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
from omero.rtypes import rstring, rtime
import pytest
import test.integration.library as lib

import json

from urllib import urlencode

from django.test import Client
from django.core.urlresolvers import reverse


try:
    from PIL import Image , ImageDraw# see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597

from random import randint as rint
import tempfile

@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance.  With
    attached finalizer so that pytest will clean it up.
    """
    o = lib.ITest()
    o.setup_method(None)

    def finalizer():
        o.teardown_method(None)
    request.addfinalizer(finalizer)
    return o


@pytest.fixture(scope='function')
def client(request, itest):
    """Returns a new user client in a read-only group."""
    # Use group read-only permissions (not private) by default
    return itest.new_client(perms='rwr---')


@pytest.fixture(scope='function')
def image_with_channels(request, itest, client):
    """
    Returns a new foundational Image with Channel objects attached for
    view method testing.
    """
    pixels = itest.pix(client=client)
    for the_c in range(pixels.getSizeC().val):
        channel = omero.model.ChannelI()
        channel.logicalChannel = omero.model.LogicalChannelI()
        pixels.addChannel(channel)
    image = pixels.getImage()
    return client.getSession().getUpdateService().saveAndReturnObject(image)


@pytest.fixture(scope='function')
def new_tag(request, itest, client):
    """
    Returns a new Tag objects
    """
    tag = omero.model.TagAnnotationI()
    tag.textValue = rstring(itest.uuid())
    tag.ns = rstring("pytest")
    return client.getSession().getUpdateService().saveAndReturnObject(tag)

@pytest.fixture(scope='function')
def django_client(request, client):
    """Returns a logged in Django test client."""
    django_client = Client(enforce_csrf_checks=True)
    login_url = reverse('weblogin')

    response = django_client.get(login_url)
    assert response.status_code == 200
    csrf_token = django_client.cookies['csrftoken'].value

    data = {
        'server': 1,
        'username': client.getProperty('omero.user'),
        'password': client.getProperty('omero.pass'),
        'csrfmiddlewaretoken': csrf_token
    }
    response = django_client.post(login_url, data)
    assert response.status_code == 302

    def finalizer():
        logout_url = reverse('weblogout')
        data = {'csrfmiddlewaretoken': csrf_token}
        response = django_client.post(logout_url, data=data)
        assert response.status_code == 302
    request.addfinalizer(finalizer)
    return django_client

@pytest.fixture(scope='function')
def django_root_client(request, itest):
    """Returns a logged in Django test client."""
    django_client = Client(enforce_csrf_checks=True)
    login_url = reverse('weblogin')

    response = django_client.get(login_url)
    assert response.status_code == 200
    csrf_token = django_client.cookies['csrftoken'].value

    data = {
        'server': 1,
        'username': 'root',
        'password': itest.client.ic.getProperties().getProperty('omero.rootpass'),
        'csrfmiddlewaretoken': csrf_token
    }
    response = django_client.post(login_url, data)
    assert response.status_code == 302

    def finalizer():
        logout_url = reverse('weblogout')
        data = {'csrfmiddlewaretoken': csrf_token}
        response = django_client.post(logout_url, data=data)
        assert response.status_code == 302
    request.addfinalizer(finalizer)
    return django_client

class TestCsrf(object):
    """
    Tests to ensure that Django CSRF middleware for OMERO.web is enabled and
    working correctly.
    """

    # Client
    def test_csrf_middleware_enabled(self, client):
        """
        If the CSRF middleware is enabled login attempts that do not include
        the CSRF token should fail with an HTTP 403 (forbidden) status code.
        """
        # https://docs.djangoproject.com/en/dev/ref/contrib/csrf/#testing
        django_client = Client(enforce_csrf_checks=True)

        data = {
            'server': 1,
            'username': client.getProperty('omero.user'),
            'password': client.getProperty('omero.pass')
        }
        login_url = reverse('weblogin')
        _post_reponse(django_client, login_url, data)

        logout_url = reverse('weblogout')
        _post_reponse(django_client, logout_url, {})

    def test_forgot_password(self, itest, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        request_url = reverse('waforgottenpassword')
        data = {
            'username': "omename",
            'email': "email"
        }
        _post_reponse(django_client, request_url, {})
        _csrf_post_reponse(django_client, request_url, {})

    def test_add_and_rename_container(self, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        # Add project
        request_url = reverse("manage_action_containers", args=["addnewcontainer"])
        data = {
            'folder_type': 'project',
            'name': 'foobar'
        }
        _post_reponse(django_client, request_url, data)
        response = _csrf_post_reponse(django_client, request_url, data)
        pid = json.loads(response.content).get("id")

        # Add dataset to the project
        request_url = reverse("manage_action_containers", args=["addnewcontainer", "project", pid])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Rename project
        request_url = reverse("manage_action_containers", args=["savename", "project", pid])
        data = {
            'name': 'anotherfoobar'
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Change project description
        request_url = reverse("manage_action_containers", args=["savedescription", "project", pid])
        data = {
            'description': 'anotherfoobar'
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

    def test_move_data(self, itest, client, django_root_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        user_id = client.getSession().getAdminService().getEventContext().userId
        user = client.getSession().getAdminService().getExperimenter(user_id)
        group_id = itest.new_group(experimenters=[user]).id.val

        request_url = reverse('chgrp')
        data = {
            'image': image_with_channels.id.val,
            'group_id': group_id
        }

        _post_reponse(django_root_client, request_url, data)
        _csrf_post_reponse(django_root_client, request_url, data)

    def test_add_and_remove_comment(
            self, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        request_url = reverse('annotate_comment')
        data = {
            'comment': 'foobar',
            'image': image_with_channels.id.val
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Remove comment, see remove tag,
        # http://trout.openmicroscopy.org/merge/webclient/action/remove/[comment|tag|file]/ID/

    def test_add_edit_and_remove_tag(
            self, django_client, image_with_channels, new_tag):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        # Add tag
        request_url = reverse('annotate_tags')
        data = {
            'image': image_with_channels.id.val,
            'filter_mode': 'any',
            'filter_owner_mode': 'all',
            'index': 0,
            'newtags-0-description': '',
            'newtags-0-tag': 'foobar',
            'newtags-0-tagset': '',
            'newtags-INITIAL_FORMS': 0,
            'newtags-MAX_NUM_FORMS': 1000,
            'newtags-TOTAL_FORMS': 1,
            'tags': new_tag.id.val
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Edit tag, see save container name and description
        # http://trout.openmicroscopy.org/merge/webclient/action/savename/tag/ID/
        # http://trout.openmicroscopy.org/merge/webclient/action/savedescription/tag/ID/

        # Remove tag
        request_url = reverse("manage_action_containers", args=["remove", "tag", new_tag.id.val])
        data = {
            'index': 0,
            'parent': "image-%i" % image_with_channels.id.val
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Delete tag
        request_url = reverse("manage_action_containers", args=["delete", "tag", new_tag.id.val])
        _post_reponse(django_client, request_url, {})
        _csrf_post_reponse(django_client, request_url, {})

    def test_attach_file(self, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        # Due to EOF both posts must be test separately
        # Bad post
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.csrf')
            temp.write("Testing without csrf token")
            temp.seek(0)

            request_url = reverse('annotate_file')
            data = {
                'image': image_with_channels.id.val,
                'index': 0,
                'annotation_file': temp
            }
            _post_reponse(django_client, request_url, data)
        finally:
            temp.close()

        # Good post
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.csrf')
            temp.write("Testing csrf token")
            temp.seek(0)

            request_url = reverse('annotate_file')
            data = {
                'image': image_with_channels.id.val,
                'index': 0,
                'annotation_file': temp
            }
            _csrf_post_reponse(django_client, request_url, data)
        finally:
            temp.close()

        # link existing annotation is handled by the same request.

        # Remove file, see remove tag,
        # http://trout.openmicroscopy.org/merge/webclient/action/remove/[comment|tag|file]/ID/

    def test_paste_move_remove_deletamany_image(
            self, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        # Add dataset
        request_url = reverse("manage_action_containers", args=["addnewcontainer"])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        _post_reponse(django_client, request_url, data)
        response = _csrf_post_reponse(django_client, request_url, data)
        did = json.loads(response.content).get("id")

        # Copy image
        request_url = reverse("manage_action_containers", args=["paste", "image", image_with_channels.id.val])
        data = {
            'destination': "dataset-%i" % did
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Move image
        request_url = reverse("manage_action_containers", args=["move", "image", image_with_channels.id.val])
        data = {
            'destination': 'orphaned-0',
            'parent': 'dataset-%i' % did
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Remove image
        request_url = reverse("manage_action_containers", args=["remove", "image", image_with_channels.id.val])
        data = {
            'parent': 'dataset-%i' % did
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Delete image
        request_url = reverse("manage_action_containers", args=["deletemany"])
        data = {
            'child': 'on',
            'dataset': did
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

    def test_basket_actions(self, itest, client, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        user_to_share = itest.new_user()

        # Create discussion
        request_url = reverse("basket_action", args=["createdisc"])
        data = {
            'enable':'on',
            'members': user_to_share.id.val,
            'message':'foobar'
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # Create share
        request_url = reverse("basket_action", args=["createshare"])
        data = {
            'enable':'on',
            'image': image_with_channels.id.val,
            'members': user_to_share.id.val,
            'message':'foobar'
        }

        # edit share
        # create images
        images = [
            itest.createTestImage(session=client.getSession()),
            itest.createTestImage(session=client.getSession())]

        # put images into the basket
        session = django_client.session
        session['imageInBasket'] = [i.id.val for i in images]
        session.save()

        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        sid = client.getSession().getShareService().createShare("foobar", rtime(None), images, [user_to_share], [], True)

        request_url = reverse("manage_action_containers", args=["save", "share", sid])

        data = {
            'enable':'on',
            'image': [i.id.val for i in images],
            'members': user_to_share.id.val,
            'message':'another foobar'
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

        # remove image from share
        request_url = reverse("manage_action_containers", args=["removefromshare", "share", sid])
        data = {
            'source':images[1].id.val,
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)

    def test_edit_channel_names(
            self, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        query_string = data = {'channel0': 'foobar'}
        request_url = reverse(
            'edit_channel_names', args=[image_with_channels.id.val]
        )
        _csrf_get_reponse(django_client, request_url, query_string, status_code=405)
        _csrf_post_reponse(django_client, request_url, data)

    def test_copy_past_rendering_settings(self, itest, client, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = itest.createTestImage(session=client.getSession())

        # put image id into session
        session = django_client.session
        session['fromid'] = img.id.val
        session.save()

        request_url = reverse('webgateway.views.copy_image_rdef_json')
        data = {
            'toids': img.id.val
        }

        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)
        _csrf_get_reponse(django_client, request_url, data, status_code=405)

    def test_reset_rendering_settings(self, itest, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = itest.createTestImage()

        # Reset through webclient as it is calling directly webgateway.reset_image_rdef_json
        request_url = reverse('web_reset_image_rdef_json', args=[img.id.val])
        data = {
            'full': 'true'
        }

        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)
        _get_reponse(django_client, request_url, data)
        _csrf_get_reponse(django_client, request_url, data, status_code=405)

    def test_apply_owners_rendering_settings(self, itest, client, django_root_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = itest.createTestImage(session=client.getSession())

        request_url = reverse('webgateway.views.apply_owners_rdef_json')
        data = {
            'toids': img.id.val,
            'to_type': 'image'
        }

        _post_reponse(django_root_client, request_url, data, status_code=403)
        _csrf_post_reponse(django_root_client, request_url, data)

    def test_ome_tiff_script(self, itest, client, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = itest.createTestImage(session=client.getSession())

        request_url = reverse('ome_tiff_script', args=[img.id.val])
        
        _post_reponse(django_client, request_url, {})
        _csrf_post_reponse(django_client, request_url, {})
        _csrf_get_reponse(django_client, request_url, {}, status_code=405)

    def test_script(self, itest, client, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        img = itest.createTestImage(session=client.getSession())

        script_path = "omero/export_scripts/Batch_Image_Export.py"
        script = client.getSession().getScriptService().getScriptID(script_path)

        request_url = reverse('script_run', args=[script])
        data = {
            "Data_Type": "Image",
            "IDs": img.id.val,
            "Choose_T_Section": "Default-T (last-viewed)",
            "Choose_Z_Section": "Default-Z (last-viewed)",
            "Export_Individual_Channels":"on",
            "Export_Merged_Image": "on",
            "Folder_Name": "Batch_Image_Export",
            "Format": "JPEG",
            "Zoom": "100%"
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data)


    # ADMIN
    def test_myaccount(self, itest, client, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        user_id = client.getSession().getAdminService().getEventContext().userId
        user = client.getSession().getAdminService().getExperimenter(user_id)

        request_url = reverse('wamyaccount', args=["save"])
        data = {
            "omename": user.omeName.val,
            "first_name": user.omeName.val,
            "last_name": user.lastName.val,
            "institution": "foo bar",
            "default_group": user.copyGroupExperimenterMap()[0].parent.id.val
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data, status_code=302)

    def test_avatar(self, itest, client, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        user_id = client.getSession().getAdminService().getEventContext().userId
        user = client.getSession().getAdminService().getExperimenter(user_id)

        # Due to EOF both posts must be test separately
        # Bad post
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.png')

            img = Image.new("RGB", (200,200), "#FFFFFF")
            draw = ImageDraw.Draw(img)

            r,g,b = rint(0,255), rint(0,255), rint(0,255)
            for i in range(200):
                draw.line((i,0,i,200), fill=(int(r),int(g),int(b)))
            img.save(temp, "PNG")
            temp.seek(0)

            request_url = reverse('wamanageavatar', args=[user_id, "upload"])
            data = {
                'filename': 'avatar.png',
                "photo": temp
            }
            _post_reponse(django_client, request_url, data)
        finally:
            temp.close()

        # Good post
        try:
            temp = tempfile.NamedTemporaryFile(suffix='.png')

            img = Image.new("RGB", (200,200), "#FFFFFF")
            draw = ImageDraw.Draw(img)

            r,g,b = rint(0,255), rint(0,255), rint(0,255)
            for i in range(200):
                draw.line((i,0,i,200), fill=(int(r),int(g),int(b)))
            img.save(temp, "PNG")
            temp.seek(0)

            request_url = reverse('wamanageavatar', args=[user_id, "upload"])
            data = {
                'filename': 'avatar.png',
                "photo": temp
            }
            _csrf_post_reponse(django_client, request_url, data, status_code=302)
        finally:
            temp.close()

        # Crop avatar
        request_url = reverse('wamanageavatar', args=[user_id, "crop"])
        data = {
            'x1':50,
            'x2':150,
            'y1':50,
            'y2':150
        }
        _post_reponse(django_client, request_url, data)
        _csrf_post_reponse(django_client, request_url, data, status_code=302)

    def test_create_group(self, itest, django_root_client):
        uuid = itest.uuid()
        request_url = reverse('wamanagegroupid', args=["create"])
        data = {
            "name":uuid,
            "description":uuid,
            "permissions":0
        }
        _post_reponse(django_root_client, request_url, data)
        _csrf_post_reponse(django_root_client, request_url, data, status_code=302)

    def test_create_user(self, itest, django_root_client):
        uuid = itest.uuid()
        groupid = itest.new_group().id.val
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
        _post_reponse(django_root_client, request_url, data)
        _csrf_post_reponse(django_root_client, request_url, data, status_code=302)

    def test_edit_group(self, itest, django_root_client):
        group = itest.new_group(perms="rw----")
        request_url = reverse('wamanagegroupid', args=["save", group.id.val])
        data = {
            "name": group.name.val,
            "description": "description",
            "permissions": 0
        }
        _post_reponse(django_root_client, request_url, data)
        _csrf_post_reponse(django_root_client, request_url, data, status_code=302)

    def test_edit_user(self, itest, django_root_client):
        user = itest.new_user()
        request_url = reverse('wamanageexperimenterid', args=["save", user.id.val])
        data = {
            "omename": user.omeName.val,
            "first_name":user.firstName.val,
            "last_name":user.lastName.val,
            "default_group": user.copyGroupExperimenterMap()[0].parent.id.val,
            "other_groups": user.copyGroupExperimenterMap()[0].parent.id.val,
        }
        _post_reponse(django_root_client, request_url, data)
        _csrf_post_reponse(django_root_client, request_url, data, status_code=302)

    def test_change_password(self, itest, django_root_client):
        user = itest.new_user()
        request_url = reverse('wamanagechangepasswordid', args=[user.id.val])
        data = {
            "old_password": itest.client.ic.getProperties().getProperty('omero.rootpass'),
            "password":"new",
            "confirmation": "new"
        }
        _post_reponse(django_root_client, request_url, data)
        _csrf_post_reponse(django_root_client, request_url, data)

    def test_su(self, itest, django_root_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """

        user = itest.new_user()

        request_url = reverse('webgateway_su', args=[user.omeName.val])

        _csrf_get_reponse(django_root_client, request_url, {})
        _post_reponse(django_root_client, request_url, {})
        _csrf_post_reponse(django_root_client, request_url, {})

# Helpers
def _post_reponse(django_client, request_url, data, status_code=403):
    response = django_client.post(request_url, data=data)
    assert response.status_code == status_code
    return response

def _csrf_post_reponse(django_client, request_url, data, status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    data['csrfmiddlewaretoken'] = csrf_token
    return _post_reponse(django_client, request_url, data, status_code)

def _get_reponse(django_client, request_url, query_string, status_code=405):
    query_string = urlencode(query_string.items())
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code
    return response

def _csrf_get_reponse(django_client, request_url, query_string, status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    query_string['csrfmiddlewaretoken'] = csrf_token
    return _get_reponse(django_client, request_url, query_string, status_code)
