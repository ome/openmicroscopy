#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Metadata plugin

   Copyright 2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import logging
import re
import sys

import omero
from omero.cli import BaseControl
from omero.cli import CLI
from omero.cli import ProxyStringType
from omero.constants import namespaces
from omero.gateway import BlitzGateway
# from omero.util.populate_roi import PlateAnalysisCtxFactory
from omero.util import populate_metadata

HELP = """metadata methods

Provides access to and editing of the metadata which
is typically shown in the right handle panel of the
GUI clients.
"""


ANNOTATION_TYPES = [t for t in dir(omero.model)
                    if re.match('[A-Za-z0-9]+Annotation$', t)]


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
        return self.wrap(self.obj_wrapper.getParent())

    def get_parents(self):
        return self.wrap(self.obj_wrapper.listParents())

    def get_roi_count(self):
        return self.obj_wrapper.getROICount()

    def get_original(self):
        return self.obj_wrapper.loadOriginalMetadata()

    def get_bulkanns(self):
        return self.wrap(self.obj_wrapper.listAnnotations(
            namespaces.NSBULKANNOTATIONS))

    def get_allanns(self):
        return self.wrap(self.obj_wrapper.listAnnotations())

    def wrap(self, obj):
        if obj is None:
            return None
        try:
            return [self.__class__(o) for o in obj]
        except TypeError:
            return self.__class__(obj)

    def __getattr__(self, name):
        return getattr(self.obj_wrapper, name)

    def __str__(self):
        return "<Metadata%s>" % self.obj_wrapper


class MetadataControl(BaseControl):

    POPULATE_CONTEXTS = (
        ("csv", populate_metadata.ParsingContext),
        ("bulkmap", populate_metadata.BulkToMapAnnotationContext),
        ("deletemap", populate_metadata.DeleteMapAnnotationContext),
    )

    def _configure(self, parser):
        parser.add_login_arguments()

        sub = parser.sub()
        summary = parser.add(sub, self.summary)
        original = parser.add(sub, self.original)
        measures = parser.add(sub, self.measures)
        bulkanns = parser.add(sub, self.bulkanns)
        mapanns = parser.add(sub, self.mapanns)
        allanns = parser.add(sub, self.allanns)
        populate = parser.add(sub, self.populate)

        for x in (summary, original, bulkanns, measures, mapanns, allanns,
                  populate):
            x.add_argument("obj",
                           type=ProxyStringType(),
                           help="Object in Class:ID format")

        for x in (bulkanns, mapanns, allanns, populate):
            x.add_argument("--report", action="store_true", help=(
                "Show additional information"))

        bulkanns.add_argument(
            "--parents", action="store_true",
            help="Also search parents for bulk annotations")

        populate.add_argument(
            "--context", default=self.POPULATE_CONTEXTS[0][0],
            choices=[a[0] for a in self.POPULATE_CONTEXTS])
        dry_or_not = populate.add_mutually_exclusive_group()
        dry_or_not.add_argument("-n", "--dry-run", action="store_true")
        dry_or_not.add_argument("-f", "--force", action="store_false",
                                dest="dry_run")
        populate.add_argument("--file", help="Input file")
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

    def _format_ann(self, md, obj, indent=None):
        "Format an annotation as a string, optionally pretty-printed"
        if indent is None:
            s = obj.get_name()
        else:
            s = "%s%s" % ("  " * indent, obj.get_name())
            pre = "\n%s" % ("  " * (indent + 1))
            s += "%sns: %s" % (pre, obj.getNs())
            s += "%sdescription: %s" % (pre, obj.getDescription())
            s += "%sdate: %s" % (pre, obj.getDate().isoformat())

            if obj.get_type() == 'FileAnnotation':
                f = md.wrap(obj.getFile())
                s += "%sfile: %s" % (pre, f.get_name())
                pre = "\n%s" % ("  " * (indent + 2))
                s += "%sname: %s" % (pre, f.getName())
                s += "%ssize: %s" % (pre, f.getSize())

            elif obj.get_type() == 'MapAnnotation':
                ma = obj.getValue()
                s += "%svalue:" % pre
                pre = "\n%s" % ("  " * (indent + 2))
                for k, v in ma:
                    s += "%s%s=%s" % (pre, k, v)

            else:
                v = obj.getValue()
                s += "%svalue: %s" % (pre, v)

        return s

    # READ METHODS

    def summary(self, args):
        "Provide a general summary of available metadata"
        md = self._load(args)
        name = md.get_name()
        line = "-" * len(name)
        self.ctx.out(name)
        self.ctx.out(line)
        try:
            self.ctx.out("Name: %s" % md.name)
        except AttributeError:
            pass
        try:
            self.ctx.out("Roi count: %s" % md.get_roi_count())
        except AttributeError:
            pass
        self.ctx.out("Bulk annotations: %s" % len(md.get_bulkanns()))
        try:
            parent = md.get_parent().get_name()
            self.ctx.out("Parent: %s" % parent)
            otherparents = [p.get_name() for p in md.get_parents()]
            otherparents = [p for p in otherparents if p != parent]
            if otherparents:
                self.ctx.out("Other Parents: %s" % ",".join(otherparents))
        except AttributeError:
            pass
        try:
            source, global_om, series_om = md.get_original()
            self.ctx.out("Source metadata: %s" % bool(source))
            self.ctx.out("Global metadata: %s" % bool(global_om))
            self.ctx.out("Series metadata: %s" % bool(series_om))
        except AttributeError:
            pass

        counts = {}
        anns = md.get_allanns()
        for a in anns:
            try:
                counts[a.get_type()] += 1
            except KeyError:
                counts[a.get_type()] = 1
        for t in sorted(ANNOTATION_TYPES):
            if counts.get(t):
                self.ctx.out("%ss: %s" % (t, counts[t]))

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

        def output_bulkann(mdobj, indent=None):
            anns = mdobj.get_bulkanns()
            if indent is not None:
                self.ctx.out("%s%s" % (
                    '  ' * indent, mdobj.get_name()))
                indent += 1
            for a in anns:
                self.ctx.out(self._format_ann(md, a, indent))
            if args.parents:
                for p in mdobj.get_parents():
                    output_bulkann(p, indent)

        md = self._load(args)
        if args.report:
            output_bulkann(md, 0)
        else:
            output_bulkann(md)

    def measures(self, args):
        ("Provide a list of the NSMEASUREMENT tables linked "
         "to the given object")
        md = self._load(args)
        print md

    def mapanns(self, args):
        "Provide a list of all MapAnnotations linked to the given object"
        md = self._load(args)
        mas = md.get_allanns()
        for ma in mas:
            if ma.get_type() == 'MapAnnotation':
                if args.report:
                    self.ctx.out(self._format_ann(md, ma, 0))
                else:
                    self.ctx.out(self._format_ann(md, ma))

    def allanns(self, args):
        "Provide a list of all annotations linked to the given object"
        md = self._load(args)
        for a in md.get_allanns():
            if args.report:
                self.ctx.out(self._format_ann(md, a, 0))
            else:
                self.ctx.out(self._format_ann(md, a))

    # WRITE

    def populate(self, args):
        client = self.ctx.conn(args)
        # TODO: Configure logging properly
        if args.report:
            populate_metadata.log.setLevel(logging.DEBUG)
        else:
            populate_metadata.log.setLevel(logging.INFO)
        context_class = dict(self.POPULATE_CONTEXTS)[args.context]
        ctx = context_class(client, args.obj, args.file)
        ctx.parse()
        if not args.dry_run:
            ctx.write_to_omero()

    #    factory = PlateAnalysisCtxFactory(client.sf)
    #    ctx = factory.get_analysis_ctx(args.plate.id.val)
    #    count = ctx.get_measurement_count()
    #    if not count:
    #        self.ctx.die(100, "No measurements found")
    #    for i in range(count):
    #        if args.dry_run:
    #            self.ctx.out(
    #                "Measurement %d has %s result files." % (
    #                    i, ctx.get_result_file_count(i)))
    #        else:
    #            if args.measurement is not None:
    #                if args.measurement != i:
    #                    continue
    #            meas = ctx.get_measurement_ctx(i)
    #            meas.parse_and_populate()

try:
    register("metadata", MetadataControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("metadata", MetadataControl, HELP)
        cli.invoke(sys.argv[1:])
