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
from omero.cli import ProxyStringType
from omero.gateway import BlitzGateway

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

    def original(self):
        return self.obj_wrapper.loadOriginalMetadata()

    def __str__(self):
        return "<Metadata%s>" % self.obj_wrapper


class MetadataControl(BaseControl):

    def _configure(self, parser):
        parser.add_login_arguments()
        sub = parser.sub()
        summary = parser.add(sub, self.summary)
        original = parser.add(sub, self.original)

        for x in (summary, original,):
            x.add_argument("obj",
                           type=ProxyStringType(),
                           help="Object in Class:ID format")

    def _load(self, args):
        client = self.ctx.conn(args)
        conn = BlitzGateway(client_obj=client)
        conn.SERVICE_OPTS.setOmeroGroup('-1')
        klass = args.obj.ice_staticId().split("::")[-1]
        oid = args.obj.id.val
        wrapper = conn.getObject(klass, oid)
        return Metadata(wrapper)

    def summary(self, args):
        "Provide a general summary of available metadata"
        md = self._load(args)
        print md

    def original(self, args):
        "Print the original metadata in ini format"
        md = self._load(args)
        source, global_om, series_om = md.original()
        om = (("Global", global_om),
              ("Series", series_om))

        for name, tuples in om:
            # Matches the OMERO4 original_metadata.txt format
            self.ctx.out("[%sMetadata]" % name)
            for k, v in tuples:
                self.ctx.out("%s=%s" % (k, v))

try:
    register("metadata", MetadataControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("metadata", MetadataControl, HELP)
        cli.invoke(sys.argv[1:])
