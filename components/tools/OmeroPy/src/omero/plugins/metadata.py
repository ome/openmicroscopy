#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Metadata plugin

   Copyright 2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import logging
import mimetypes
import os
import re
import sys
import warnings

import omero
from omero.cli import BaseControl
from omero.cli import CLI
from omero.cli import ProxyStringType
from omero.constants import namespaces
from omero.gateway import BlitzGateway
from omero.util import populate_metadata, populate_roi, pydict_text_io
from omero.util.metadata_utils import NSBULKANNOTATIONSCONFIG
from omero.util.metadata_utils import NSBULKANNOTATIONSRAW
from omero.grid import LongColumn
from omero.model.enums import UnitsLength

HELP = """Metadata utilities

Provides access to and editing of the metadata which
is typically shown in the right-hand panel of the
GUI clients.
"""


ANNOTATION_TYPES = [t for t in dir(omero.model)
                    if re.match('[A-Za-z0-9]+Annotation$', t)]


def guess_mimetype(filename):
    mt = mimetypes.guess_type(filename, strict=False)[0]
    if not mt and os.path.splitext(filename) in ('yml', 'yaml'):
        mt = "application/x-yaml"
    if not mt:
        mt = "application/octet-stream"
    return mt


class ObjectLoadException(Exception):
    """
    Raised when a requested object could not be loaded
    """
    def __init__(self, klass, oid):
        self.klass = klass
        self.oid = oid
        super(ObjectLoadException, self).__init__(
            "Failed to get object %s:%s" % (klass, oid))


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
        return self.get_allanns(namespaces.NSBULKANNOTATIONS)

    def get_measures(self):
        return self.get_allanns(namespaces.NSMEASUREMENT)

    def get_allanns(self, ns=None, anntype=None):
        # Sooner or later we're going to end up with an object that has 1000s
        # of annotations, and since listAnnotations() returns a generator we
        # might as well do the same
        for a in self.obj_wrapper.listAnnotations(ns):
            aw = self.wrap(a)
            if anntype and aw.get_type() != anntype:
                continue
            yield aw

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
        parser.add(sub, self.testtables)
        rois = parser.add(sub, self.rois)
        populate = parser.add(sub, self.populate)
        populateroi = parser.add(sub, self.populateroi)
        pixelsize = parser.add(sub, self.pixelsize)

        populate.add_argument("--batch",
                              type=long,
                              default=1000,
                              help="Number of objects to process at once")
        self._add_wait(populate)

        for x in (summary, original, bulkanns, measures, mapanns, allanns,
                  rois, populate, populateroi, pixelsize):
            x.add_argument("obj",
                           type=ProxyStringType(),
                           help="Object in Class:ID format")

        for x in (bulkanns, measures, mapanns, allanns,
                  rois, populate, populateroi):
            x.add_argument("--report", action="store_true", help=(
                "Show additional information"))

        for x in (rois, populate, populateroi):
            dry_or_not = x.add_mutually_exclusive_group()
            dry_or_not.add_argument("-n", "--dry-run", action="store_true")
            dry_or_not.add_argument("-f", "--force", action="store_false",
                                    dest="dry_run")

        for x in (bulkanns, measures, mapanns, allanns):
            x.add_argument(
                "--parents", action="store_true",
                help="Also search parents for annotations")

        for x in (mapanns, allanns):
            x.add_argument(
                "--ns", default=None, help="Restrict to this namespace")
            x.add_argument(
                "--nsre",
                help="Restrict to this namespace (regular expression)")

        rois.add_argument(
            "--delete", action="store_true", help="Delete all ROIs")

        populate.add_argument(
            "--context", default=self.POPULATE_CONTEXTS[0][0],
            choices=[a[0] for a in self.POPULATE_CONTEXTS])

        datafile = populate.add_mutually_exclusive_group()
        datafile.add_argument("--file", help="Input file")
        datafile.add_argument(
            "--fileid", type=long, help="Input OriginalFile ID")

        cfgfile = populate.add_mutually_exclusive_group()
        cfgfile.add_argument("--cfg", help="YAML configuration file")
        cfgfile.add_argument(
            "--cfgid", type=long, help="YAML configuration OriginalFile ID")

        populate.add_argument("--attach", action="store_true", help=(
            "Upload input or configuration files and attach to parent object"))

        populate.add_argument("--localcfg", help=(
            "Local configuration file or a JSON object string"))

        populateroi.add_argument(
            "--measurement", type=int, default=None,
            help="Index of the measurement to populate. By default, all")

        pixelsize.add_argument(
            "--x", type=float, default=None, help="Physical pixel size X")
        pixelsize.add_argument(
            "--y", type=float, default=None, help="Physical pixel size Y")
        pixelsize.add_argument(
            "--z", type=float, default=None, help="Physical pixel size Z")
        pixelsize.add_argument(
            "--unit", default="micrometer",
            help="Unit (nanometer, micrometer, etc.) (default: micrometer)")

    def _clientconn(self, args):
        client = self.ctx.conn(args)
        conn = BlitzGateway(client_obj=client)
        return client, conn

    def _load(self, args, die_on_failure=True):
        # In most cases we want to die immediately if an object can't be
        # loaded. To raise an exception instead pass die_on_failure=False
        # and catch ObjectLoadException
        client, conn = self._clientconn(args)
        conn.SERVICE_OPTS.setOmeroGroup('-1')
        klass = args.obj.ice_staticId().split("::")[-1]
        oid = args.obj.id.val
        wrapper = conn.getObject(klass, oid)
        if not wrapper:
            e = ObjectLoadException(klass, oid)
            if die_on_failure:
                self.ctx.die(100, str(e))
            raise e
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
        self.ctx.out("Bulk annotations: %d" %
                     sum(1 for a in md.get_bulkanns()))
        self.ctx.out("Measurement tables: %d" %
                     sum(1 for a in md.get_measures()))
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
        try:
            source, global_om, series_om = md.get_original()
        except AttributeError:
            self.ctx.die(100, 'Failed to get original metadata for %s' %
                         md.get_name())

        om = (("Global", global_om),
              ("Series", series_om))

        for name, tuples in om:
            # Matches the OMERO4 original_metadata.txt format
            self.ctx.out("[%sMetadata]" % name)
            for k, v in tuples:
                self.ctx.out("%s=%s" % (k, v))

    def _output_ann(self, mdobj, func, parents, indent):
        try:
            # Dereference the generator here so that we can catch
            # NotImplementedError
            anns = list(func(mdobj))
        except NotImplementedError:
            self.ctx.err('WARNING: Failed to get annotations for %s' %
                         mdobj.get_name())
            anns = []
        if indent is not None:
            self.ctx.out("%s%s" % (
                '  ' * indent, mdobj.get_name()))
            indent += 1
        for a in anns:
            self.ctx.out(self._format_ann(mdobj, a, indent))
        if parents:
            for p in mdobj.get_parents():
                self._output_ann(p, func, parents, indent)

    def bulkanns(self, args):
        ("Provide a list of the NSBULKANNOTATION tables linked "
         "to the given object")
        md = self._load(args)
        indent = None
        if args.report:
            indent = 0
        self._output_ann(
            md, lambda md: md.get_bulkanns(), args.parents, indent)

    def measures(self, args):
        ("Provide a list of the NSMEASUREMENT tables linked "
         "to the given object")
        md = self._load(args)
        indent = None
        if args.report:
            indent = 0
        self._output_ann(
            md, lambda md: md.get_measures(), args.parents, indent)

    def mapanns(self, args):
        "Provide a list of all MapAnnotations linked to the given object"
        def get_anns(md):
            for a in md.get_allanns(args.ns, 'MapAnnotation'):
                if args.nsre and not re.match(args.nsre, a.get_ns()):
                    continue
                yield a

        md = self._load(args)
        indent = None
        if args.report:
            indent = 0
        self._output_ann(md, get_anns, args.parents, indent)

    def allanns(self, args):
        "Provide a list of all annotations linked to the given object"
        def get_anns(md):
            for a in md.get_allanns(args.ns):
                if args.nsre and not re.match(args.nsre, a.get_ns()):
                    continue
                yield a

        md = self._load(args)
        indent = None
        if args.report:
            indent = 0
        self._output_ann(md, get_anns, args.parents, indent)

    def testtables(self, args):
        "Tests whether tables can be created and initialized"
        client, conn = self._clientconn(args)

        sf = client.getSession()
        sr = sf.sharedResources()
        table = sr.newTable(1, 'testtables')
        if table is None:
            self.ctx.die(100, "Failed to create Table")

        # If we have a table...
        initialized = False
        try:
            table.initialize([LongColumn('ID', '', [])])
            initialized = True
        except:
            pass
        finally:
            table.close()

        try:
            orig_file = table.getOriginalFile()
            conn.deleteObject(orig_file)
        except:
            # Anything else to do here?
            pass

        if not initialized:
            self.ctx.die(100, "Failed to initialize Table")

    # WRITE

    def populate(self, args):
        "Add metadata (bulk-annotations) to an object"
        md = self._load(args)
        client, conn = self._clientconn(args)
        # TODO: Configure logging properly
        if args.report:
            populate_metadata.log.setLevel(logging.DEBUG)
        else:
            populate_metadata.log.setLevel(logging.INFO)

        context_class = dict(self.POPULATE_CONTEXTS)[args.context]

        if args.localcfg:
            localcfg = pydict_text_io.load(
                args.localcfg, session=client.getSession())
        else:
            localcfg = {}

        fileid = args.fileid
        cfgid = args.cfgid

        if args.attach and not args.dry_run:
            if args.file:
                fileann = conn.createFileAnnfromLocalFile(
                    args.file, mimetype=guess_mimetype(args.file),
                    ns=NSBULKANNOTATIONSRAW)
                fileid = fileann.getFile().getId()
                md.linkAnnotation(fileann)

            if args.cfg:
                cfgann = conn.createFileAnnfromLocalFile(
                    args.cfg, mimetype=guess_mimetype(args.cfg),
                    ns=NSBULKANNOTATIONSCONFIG)
                cfgid = cfgann.getFile().getId()
                md.linkAnnotation(cfgann)

        # Note some contexts only support a subset of these args
        ctx = context_class(client, args.obj, file=args.file, fileid=fileid,
                            cfg=args.cfg, cfgid=cfgid, attach=args.attach,
                            options=localcfg)
        ctx.parse()
        if not args.dry_run:
            wait = args.wait
            if not wait:
                loops = 0
                ms = 0
            else:
                ms = 5000
                loops = int((wait * 1000) / ms) + 1
            ctx.write_to_omero(batch_size=args.batch, loops=loops, ms=ms)

    def rois(self, args):
        "Manage ROIs"
        md = self._load(args)
        if args.delete:
            graphspec = "/%s/Roi:%d" % (md.get_type(), md.get_id())
            cmd = ["delete", graphspec]
            if args.report:
                cmd += ["--report"]
            if args.dry_run:
                cmd += ["--dry-run"]
            return self.ctx.invoke(cmd)

        client = self.ctx.conn(args)
        params = omero.sys.ParametersI()
        params.addId(md.get_id())
        if md.get_type() == "Screen":
            q = """SELECT r.id FROM Roi r, WellSample ws, Plate p,
                   ScreenPlateLink spl WHERE
                   spl.child=p AND r.image=ws.image AND ws.well.plate=p AND
                   spl.parent.id=:id"""
        elif md.get_type() == "Plate":
            q = """SELECT r.id FROM Roi r, WellSample ws WHERE
                   r.image=ws.image AND ws.well.plate.id=:id"""
        elif md.get_type() == "PlateAcquisition":
            q = """SELECT r.id FROM Roi r, WellSample ws WHERE
                   r.image=ws.image AND ws.plateAcquisition.id=:id"""
        elif md.get_type() == "Well":
            q = """SELECT r.id FROM Roi r, WellSample ws WHERE
                   r.image=ws.image AND ws.well.id=:id"""
        elif md.get_type() == "Project":
            q = """SELECT r.id FROM Roi r, DatasetImageLink dil,
                   ProjectDatasetLink pdl WHERE dil.child=r.image AND
                   dil.parent=pdl.child AND pdl.parent.id=:id"""
        elif md.get_type() == "Dataset":
            q = """SELECT r.id FROM Roi r, DatasetImageLink dil WHERE
                   dil.child=r.image AND dil.parent.id=:id"""
        elif md.get_type() == "Image":
            q = """SELECT r.id FROM Roi r WHERE r.image.id=:id"""
        else:
            raise Exception("Not implemented for type %s" % md.get_type())
        roiids = client.getSession().getQueryService().projection(q, params)
        roiids = [r[0].val for r in roiids]
        if roiids:
            self.ctx.out('\n'.join('Roi:%d' % rid for rid in roiids))

    def populateroi(self, args):
        "Add ROIs to an object"
        md = self._load(args)
        client = self.ctx.conn(args)
        # TODO: Configure logging properly
        if args.report:
            populate_roi.log.setLevel(logging.DEBUG)
        else:
            populate_roi.log.setLevel(logging.INFO)
        factory = populate_roi.PlateAnalysisCtxFactory(client.sf)
        ctx = factory.get_analysis_ctx(md.get_id())
        count = ctx.get_measurement_count()
        if not count:
            self.ctx.die(100, "No measurements found")
        if args.measurement is not None and args.measurement >= count:
            self.ctx.die(
                100, "Invalid measurement index: %d" % args.measurement)
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

    def pixelsize(self, args):
        "Set physical pixel size"
        if not args.x and not args.y and not args.z:
            self.ctx.die(100, "No pixel sizes specified.")

        unit = getattr(UnitsLength, args.unit.upper())
        if not unit:
            self.ctx.die(100, "%s is not recognized as valid unit."
                              % args.unit)

        md = self._load(args)
        client, conn = self._clientconn(args)

        if md.get_type() == "Screen":
            q = """SELECT pix FROM Pixels pix, WellSample ws, Plate p,
                   ScreenPlateLink spl WHERE
                   spl.child=p AND pix.image=ws.image AND ws.well.plate=p AND
                   spl.parent.id=:id"""
        elif md.get_type() == "Plate":
            q = """SELECT pix FROM Pixels pix, WellSample ws WHERE
                   pix.image=ws.image AND ws.well.plate.id=:id"""
        elif md.get_type() == "PlateAcquisition":
            q = """SELECT pix FROM Pixels pix, WellSample ws WHERE
                   pix.image=ws.image AND ws.plateAcquisition.id=:id"""
        elif md.get_type() == "Well":
            q = """SELECT pix FROM Pixels pix, WellSample ws WHERE
                   pix.image=ws.image AND ws.well.id=:id"""
        elif md.get_type() == "Project":
            q = """SELECT pix FROM Pixels pix, DatasetImageLink dil,
                   ProjectDatasetLink pdl WHERE dil.child=pix.image AND
                   dil.parent=pdl.child AND pdl.parent.id=:id"""
        elif md.get_type() == "Dataset":
            q = """SELECT pix FROM Pixels pix, DatasetImageLink dil WHERE
                   dil.child=pix.image AND dil.parent.id=:id"""
        elif md.get_type() == "Image":
            q = """SELECT pix FROM Pixels pix WHERE pix.image.id=:id"""
        else:
            raise Exception("Not implemented for type %s" % md.get_type())

        ctx = {'omero.group': '-1'}

        params = omero.sys.ParametersI()
        params.addId(md.get_id())
        pixels = conn.getQueryService().findAllByQuery(q, params, ctx)

        if not pixels:
            self.ctx.die(100, "Failed to get Pixel object(s)")

        for pixel in pixels:
            if args.x:
                pixel.setPhysicalSizeX(omero.model.LengthI(args.x, unit))
            if args.y:
                pixel.setPhysicalSizeY(omero.model.LengthI(args.y, unit))
            if args.z:
                pixel.setPhysicalSizeZ(omero.model.LengthI(args.z, unit))

        groupId = pixels[0].getDetails().getGroup().getId().getValue()
        ctx = {'omero.group': str(groupId)}
        conn.getUpdateService().saveArray(pixels, ctx)


try:
    if "OMERO_DEV_PLUGINS" in os.environ:
        warnings.warn(
            "This module is deprecated as of OMERO 5.4.8. Use the metadata"
            " CLI plugin available from"
            " https://pypi.org/project/omero-metadata/ instead.",
            DeprecationWarning)
        register("metadata", MetadataControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("metadata", MetadataControl, HELP)
        cli.invoke(sys.argv[1:])
