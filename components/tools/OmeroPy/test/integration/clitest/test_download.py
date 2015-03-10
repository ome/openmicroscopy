#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

import pytest
import omero
from omero.plugins.download import DownloadControl
from omero.cli import NonZeroReturnCode
from test.integration.clitest.cli import CLITest
from omero.rtypes import rstring
from omero.model import NamedValue as NV


class TestDownload(CLITest):

    def setup_method(self, method):
        super(TestDownload, self).setup_method(method)
        self.cli.register("download", DownloadControl, "TEST")
        self.args += ["download"]

    def create_original_file(self, content, client=None):
        """
        Create an original file and upload it onto the server
        """

        if client is None:
            update = self.update
            sf = self.sf
        else:
            update = client.sf.getUpdateService()
            sf = client.sf

        ofile = omero.model.OriginalFileI()
        ofile.name = rstring("")
        ofile.path = rstring("")
        ofile = update.saveAndReturnObject(ofile)

        rfs = sf.createRawFileStore()
        try:
            rfs.setFileId(ofile.id.val)
            rfs.write(content, 0, len(content))
            ofile = rfs.save()
            assert len(content) == ofile.size.val
            return ofile
        finally:
            rfs.close()

    def setup_user_and_two_groups(self):
        group1 = self.new_group(perms='rw----')
        group2 = self.new_group(perms='rw----')
        user = self.new_user()
        self.add_groups(user, [group1, group2], owner=True)
        return user, group1, group2

    # OriginalFile tests
    # ========================================================================
    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testNonExistingOriginalFile(self, tmpdir, prefix):
        ofile = self.create_original_file("test")
        self.args += ['%s%s' % (prefix, str(ofile.id.val + 1)), '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testOriginalFileTmpfile(self, prefix, tmpdir):
        ofile = self.create_original_file("test")
        tmpfile = tmpdir.join('test')
        self.args += ['%s%s' % (prefix, str(ofile.id.val)), str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            assert f.read() == "test"

    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testOriginalFileStdout(self, prefix, capsys):
        ofile = self.create_original_file("test")
        self.args += ['%s%s' % (prefix, str(ofile.id.val)), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testOriginalFileMultipleGroups(self, prefix, capsys):
        user, group1, group2 = self.setup_user_and_two_groups()
        client = self.new_client(user=user)
        ofile = self.create_original_file("test")
        self.set_context(client, group2.id.val)
        self.args += ['%s%s' % (prefix, str(ofile.id.val)), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    # FileAnnotation tests
    # ========================================================================
    def testNonExistingFileAnnotation(self, tmpdir):
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        self.args += ['FileAnnotation:%s' % str(fa.id.val + 1), '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testFileAnnotationTmpfile(self, tmpdir):
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        tmpfile = tmpdir.join('test')
        self.args += ['FileAnnotation:%s' % str(fa.id.val), str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            assert f.read() == "test"

    def testFileAnnotationStdout(self, capsys):
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        self.args += ['FileAnnotation:%s' % str(fa.id.val), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    def testFileAnnotationMultipleGroups(self, capsys):
        user, group1, group2 = self.setup_user_and_two_groups()
        client = self.new_client(user=user)
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        self.set_context(client, group2.id.val)
        self.args += ['FileAnnotation:%s' % str(fa.id.val), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    # Image tests
    # ========================================================================
    def testNonExistingImage(self, tmpdir):
        image = self.importSingleImageWithCompanion()
        self.args += ["Image:%s" % str(image.id.val + 1), '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testImage(self, tmpdir):
        filename = self.OmeroPy / ".." / ".." / ".." / \
            "components" / "common" / "test" / "tinyTest.d3d.dv"
        with open(filename) as f:
            bytes1 = f.read()
        pix_ids = self.import_image(filename)
        pixels = self.query.get("Pixels", long(pix_ids[0]))
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % pixels.getImage().id.val, str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            bytes2 = f.read()
        assert bytes1 == bytes2

    def testSingleImageWithCompanion(self, tmpdir):
        image = self.importSingleImageWithCompanion()
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % image.id.val, str(tmpfile)]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testMIF(self, tmpdir):
        images = self.importMIF(2)
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % images[0].id.val, str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            bytes = f.read()
        assert not bytes

    def testImageNoFileset(self, tmpdir):
        pixels = self.pix()
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % pixels.getImage().id.val, str(tmpfile)]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testImageMultipleGroups(self, tmpdir):
        user, group1, group2 = self.setup_user_and_two_groups()
        client = self.new_client(user=user)
        filename = self.OmeroPy / ".." / ".." / ".." / \
            "components" / "common" / "test" / "tinyTest.d3d.dv"
        with open(filename) as f:
            bytes1 = f.read()
        pix_ids = self.import_image(filename)
        pixels = self.query.get("Pixels", long(pix_ids[0]))
        tmpfile = tmpdir.join('test')
        self.set_context(client, group2.id.val)
        self.args += ["Image:%s" % pixels.getImage().id.val, str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            bytes2 = f.read()
        assert bytes1 == bytes2

    # Download policy
    # ========================================================================

    class PolicyFixture(object):

        def __init__(self, cfg, for_user,
                     image, ofile, plate):
            self.cfg = cfg
            self.for_user = for_user
            self.image = image
            self.ofile = ofile
            self.plate = plate

        def __str__(self):
            return "%s_%s_%s_%s_%s" % (
                self.cfg, self.for_user,
                self.image and 1 or 0,
                self.ofile and 1 or 0,
                self.plate and 1 or 0)

    POLICY_NONE = "-read,-write,-image,-plate"
    POLICY_NORDR = "-read,+write,+image,-plate"
    POLICY_NOSPW = "+read,+write,+image,-plate"  # Default
    POLICY_ALL = "+read,+write,+image,+plate"

    POLICY_FIXTURES = (
        PolicyFixture(POLICY_NONE, "owner", False, False, False),
        PolicyFixture(POLICY_NONE, "admin", False, False, False),
        PolicyFixture(POLICY_NONE, "member", False, False, False),
        PolicyFixture(POLICY_NORDR, "owner", True, True, False),
        PolicyFixture(POLICY_NORDR, "admin", True, True, False),
        PolicyFixture(POLICY_NORDR, "member", False, False, False),
        PolicyFixture(POLICY_NOSPW, "owner", True, True, False),
        PolicyFixture(POLICY_NOSPW, "admin", True, True, False),
        PolicyFixture(POLICY_NOSPW, "member", True, True, False),
        PolicyFixture(POLICY_ALL, "owner", True, True, True),
        PolicyFixture(POLICY_ALL, "admin", True, True, True),
        PolicyFixture(POLICY_ALL, "owner", True, True, True),
    )

    @pytest.mark.parametrize('fixture', POLICY_FIXTURES,
                             ids=POLICY_FIXTURES)
    def testPolicyGlobalRestriction(self, tmpdir, fixture):

        # Check that the config we have is at least tested
        # by *some* fixture
        cfg = self.root.sf.getConfigService()
        cfg = cfg.getConfigValue("omero.policy.binary_access")
        assert cfg in [x.cfg for x in self.POLICY_FIXTURES]

        # But if this isn't a check for this particular
        # config, then skip.

        if cfg != fixture.cfg:
            pytest.skip("Found binary access policy: %s" % cfg)

        group = self.new_group(perms='rwr---')
        self.do_restrictions(fixture, tmpdir, group)

    @pytest.mark.parametrize('fixture', POLICY_FIXTURES,
                             ids=POLICY_FIXTURES)
    def testPolicyGroupRestriction(self, tmpdir, fixture):
        parts = fixture.cfg.split(",")
        config = [NV("omero.policy.binary_access", x) for x in parts]
        group = self.new_group(perms='rwr---', config=config)
        self.do_restrictions(fixture, tmpdir, group)

    def do_restrictions(self, fixture, tmpdir, group):

        tmpfile = tmpdir.join('%s.test' % fixture)

        upper = self.new_client(group=group)
        upper_u = upper.sf.getUpdateService()
        plate = omero.model.PlateI()
        plate.name = rstring("do_restrictions")
        plate = upper_u.saveAndReturnObject(plate)
        ofile = self.create_original_file("test", upper)
        image = self.importSingleImage(client=upper)

        if fixture.for_user == "owner":
            downer = upper
        else:
            system = fixture.for_user == "admin"
            downer = self.new_client(group=group,
                                     system=system)

        downer_q = downer.sf.getQueryService()
        for kls, oid, will_pass in (
            ("OriginalFile", ofile.id.val, fixture.ofile),
            ("Image", image.id.val, fixture.image),
            ("Plate", plate.id.val, fixture.plate),
        ):

            obj = downer_q.get(kls, oid)
            perms = obj.details.permissions
            restricted = perms.isRestricted(
                omero.constants.permissions.BINARYACCESS)
            assert will_pass != restricted

            if "Plate" != kls:  # Plate is not implemented
                self.args = ["download"]
                self.args += self.login_args(downer)
                self.args += ["%s:%s" % (kls, oid), str(tmpfile)]
                if will_pass:
                    self.cli.invoke(self.args, strict=True)
                else:
                    with pytest.raises(NonZeroReturnCode):
                        self.cli.invoke(self.args, strict=True)
