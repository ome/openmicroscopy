#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Metadata plugin

   Copyright 2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero import ClientError
from omero.cli import BaseControl
from omero.cli import CLI
from omero.cli import ProxyStringType
from omero.gateway import BlitzGateway
from omero.util.populate_roi import PlateAnalysisCtxFactory


HELP = """metadata methods

Provides access to and editing of the metadata which
is typically shown in the right handle panel of the
GUI clients.
"""


class Metadata(object):
    """
    A general helper object which providers higher-level
    accessors and mutators for a particular object.
    """

    def __init__(self, obj_wrapper):
        """
        Takes an initialized omero.gateway Wrapper object.
        """
        assert obj_wrapper
        self.obj_wrapper = obj_wrapper

    def get_type(self):
        t = self.obj_wrapper._obj.ice_staticId()
        return t.split("::")[-1]

    def get_id(self):
        return self.obj_wrapper.getId()

    def get_name(self):
        otype = self.get_type()
        oid = self.get_id()
        return "%s:%s" % (otype, oid)

    def get_parent(self):
        return self.__class__(self.obj_wrapper.getParent())

    def get_roi_count(self):
        return self.obj_wrapper.getROICount()

    def get_original(self):
        return self.obj_wrapper.loadOriginalMetadata()

    def __getattr__(self, name):
        return getattr(self.obj_wrapper, name)

    def __str__(self):
        return "<Metadata%s>" % self.obj_wrapper


class MetadataControl(BaseControl):

    def _configure(self, parser):
        parser.add_login_arguments()
        sub = parser.sub()
        summary = parser.add(sub, self.summary)
        original = parser.add(sub, self.original)
        measures = parser.add(sub, self.measures)
        bulkanns = parser.add(sub, self.bulkanns)

        for x in (summary, original, bulkanns, measures):
            x.add_argument("obj",
                           type=ProxyStringType(),
                           help="Object in Class:ID format")

        populate = parser.add(sub, self.populate)
        dry_or_not = populate.add_mutually_exclusive_group()
        dry_or_not.add_argument("-n", "--dry-run", action="store_true")
        dry_or_not.add_argument("-f", "--force", action="store_false",
                                dest="dry_run")
        populate.add_argument(
            "plate", type=ProxyStringType("Plate"))
        populate.add_argument(
            "--measurement", type=int,
            default=None, help=(
                "Index of the measurement to populate. By default, all"
            ))

    def _load(self, args):
        client = self.ctx.conn(args)
        conn = BlitzGateway(client_obj=client)
        conn.SERVICE_OPTS.setOmeroGroup('-1')
        klass = args.obj.ice_staticId().split("::")[-1]
        oid = args.obj.id.val
        wrapper = conn.getObject(klass, oid)
        return Metadata(wrapper)

    # READ METHODS

    def summary(self, args):
        "Provide a general summary of available metadata"
        md = self._load(args)
        name = md.get_name()
        line = "-" * len(name)
        self.ctx.out(name)
        self.ctx.out(line)
        self.ctx.out("Name: %s" % md.name)
        self.ctx.out("Roi count: %s" % md.get_roi_count())
        self.ctx.out("Parent: %s" % md.get_parent().get_name())

    def original(self, args):
        "Print the original metadata in ini format"
        md = self._load(args)
        source, global_om, series_om = md.get_original()
        om = (("Global", global_om),
              ("Series", series_om))

        for name, tuples in om:
            # Matches the OMERO4 original_metadata.txt format
            self.ctx.out("[%sMetadata]" % name)
            for k, v in tuples:
                self.ctx.out("%s=%s" % (k, v))

    def bulkanns(self, args):
        ("Provide a list of the NSBULKANNOTATION tables linked "
         "to the given object")
        md = self._load(args)
        print md

    def measures(self, args):
        ("Provide a list of the NSMEASUREMENT tables linked "
         "to the given object")
        md = self._load(args)
        print md

    # WRITE

    def populate(self, args):
        client = self.ctx.conn(args)
        factory = PlateAnalysisCtxFactory(client.sf)
        ctx = factory.get_analysis_ctx(args.plate.id.val)
        count = ctx.get_measurement_count()
        for i in range(count):
            if args.dry_run:
                self.ctx.out(
                    "Measurement %d has %s result files." % (
                        i, ctx.get_result_file_count(i)))
            else:
                if args.measurement is not None:
                    if args.measurement != i:
                        continue
                meas = ctx.get_measurement_ctx(i)
                meas.parse_and_populate()

try:
    register("metadata", MetadataControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("metadata", MetadataControl, HELP)
        cli.invoke(sys.argv[1:])
