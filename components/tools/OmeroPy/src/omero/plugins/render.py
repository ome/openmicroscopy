#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2015-2016 University of Dundee & Open Microscopy Environment.
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

import sys
import time

from omero.cli import BaseControl
from omero.cli import CLI
from omero.cli import ProxyStringType
from omero.gateway import BlitzGateway
from omero.model import Image
from omero.model import Plate
from omero.model import Screen
from omero.util import pydict_text_io


DESC = {
    "COPY": "Copy rendering setting to multiple objects",
    "INFO": "Show details of a rendering setting",
    "EDIT": "Edit a rendering setting",
    "LIST": "List available rendering settings",
    "JPEG": "Render as JPEG",
}

HELP = """Tools for working with rendering settings

Examples:

    # %(INFO)s
    bin/omero render info RenderingDef:1
    bin/omero render info Image:123

    # %(EDIT)s
    bin/omero render edit Image:1 <YAML or JSON file>
    where the input file contains a top-level channels key (required), and
    an optional top-level greyscale key (True: greyscale, False: colour).
    Channel elements are index:dictionaries of the form:

    channels:
      <index>: (Channel-index, int, 1-based)
        color: <HTML RGB triplet>
        label: <Channel name>
        min: <Minimum (float)>
        max: <Maximum (float)>
        active: <Active (bool)>
      <index>:
        ...
    greyscale: <(bool)>

    # Omitted fields will keep their current values, omitted channel-indices
    # will be turned off.
    bin/omero render edit --copy Screen:1 <YAML or JSON file>
    # Optimised for bulk-rendering, edits the first image and copies the
    # rendering settings to the rest. Note using this flag may have different
    # results from not using it if the images had different settings to begin
    # with and you are only overridding a subset of the settings (all images
    # will end up with the same full rendering settings)
    bin/omero render edit --skipthumbs ...
    # Update rendering settings but don't regenerate thumbnails

    # %(LIST)s
    bin/omero render list Image:456

    # %(COPY)s
    bin/omero render copy RenderingDef:1 Image:123
    bin/omero render copy Image:456: Image:789
    bin/omero render copy Image:456: Image:222 Image:333
    bin/omero render copy Image:456: Plate:1
    bin/omero render copy Image:456: Screen:2
    bin/omero render copy Image:456: Dataset:3

    # %(JPEG)s
    bin/omero render jpeg Image:5 > test.jpg

    # ...optionally setting parameters
    bin/omero render jpeg --z=4 Image:6 > test.jpg

""" % DESC


class ChannelObject(object):

    def __init__(self, channel):
        try:
            self.init_from_channel(channel)
        except AttributeError:
            self.init_from_dict(channel)

    def init_from_channel(self, channel):
        self.emWave = channel.getEmissionWave()
        self.label = channel.getLabel()
        self.color = channel.getColor()
        self.min = channel.getWindowMin()
        self.max = channel.getWindowMax()
        self.start = channel.getWindowStart()
        self.end = channel.getWindowEnd()
        self.active = channel.isActive()

    def init_from_dict(self, d):
        if not d:
            d = {}
        self.emWave = None
        self.label = d.get('label', None)
        self.color = d.get('color', None)
        self.min = float(d['min']) if 'min' in d else None
        self.max = float(d['max']) if 'max' in d else None
        self.start = None
        self.end = None
        self.active = bool(d.get('active', True))

    def __str__(self):
        try:
            color = self.color.getHtml()
        except AttributeError:
            color = self.color
        sb = ""
        sb += ",".join([
            "active=%s" % self.active,
            "color=%s" % color,
            "label=%s" % self.label,
            "min=%s" % self.min,
            "start=%s" % self.start,
            "end=%s" % self.end,
            "max=%s" % self.max,
        ])
        return sb


class RenderObject(object):

    def __init__(self, image):
        """
        Based on omeroweb.webgateway.marshal
        """
        assert image
        image.loadRenderOptions()
        self.image = image
        self.name = image.name or ''
        self.type = image.getPixelsType()
        reOK = image._prepareRenderingEngine()
        if not reOK:
            raise Exception(
                "Failed to prepare Rendering Engine for %s" % image)

        self.tiles = image._re.requiresPixelsPyramid()
        self.width = None
        self.height = None
        self.levels = None
        self.zoomLevelScaling = None
        if self.tiles:
            self.width, self.height = image._re.getTileSize()
            self.levels = image._re.getResolutionLevels()
            self.zoomLevelScaling = image.getZoomLevelScaling()

        """
        self.nominalMagnification = \
            image.getObjectiveSettings() is not None \
            and image.getObjectiveSettings() \
            .getObjective().getNominalMagnification() \
        or None


    try:
        rv.update({
            'interpolate': interpolate,
            'size': {'width': image.getSizeX(),
                     'height': image.getSizeY(),
                     'z': image.getSizeZ(),
                     't': image.getSizeT(),
                     'c': image.getSizeC()},
            'pixel_size': {'x': image.getPixelSizeX(),
                           'y': image.getPixelSizeY(),
                           'z': image.getPixelSizeZ()},
            })
        """

        self.range = image.getPixelRange()
        self.channels = map(lambda x: ChannelObject(x), image.getChannels())
        self.model = image.isGreyscaleRenderingModel() and \
            'greyscale' or 'color'
        self.projection = image.getProjection()
        self.defaultZ = image._re.getDefaultZ()
        self.defaultT = image._re.getDefaultT()
        self.invertAxis = image.isInvertedAxis()

    def __str__(self):
        sb = "rdefv1: model=%s, z=%s, t=%s\n" % (
            self.model, self.defaultZ, self.defaultT)
        sb += "tiles: %s\n" % (self.tiles,)
        for idx, ch in enumerate(self.channels):
            sb += "ch%s: %s\n" % (idx, ch)
        return sb


class RenderControl(BaseControl):

    def _configure(self, parser):
        parser.add_login_arguments()
        sub = parser.sub()
        info = parser.add(sub, self.info, DESC["INFO"])
        copy = parser.add(sub, self.copy, DESC["COPY"])
        edit = parser.add(sub, self.edit, DESC["EDIT"])
        # list = parser.add(sub, self.list, DESC["LIST"])
        # jpeg = parser.add(sub, self.jpeg, DESC["JPEG"])
        # jpeg.add_argument(
        #    "--out", default="-",
        #    help="Local filename to be saved to. '-' for stdout")

        render_type = ProxyStringType("Image")
        render_help = ("rendering def source of form <object>:<id>. "
                       "Image is assumed if <object>: is omitted.")

        for x in (info, copy, edit):
            x.add_argument("object", type=render_type, help=render_help)
        edit.add_argument(
            "--copy", help="Batch edit images by copying rendering settings",
            action="store_true")

        for x in (copy, edit):
            x.add_argument(
                "--skipthumbs", help="Don't re-generate thumbnails",
                action="store_true")

        copy.add_argument("target", type=render_type, help=render_help,
                          nargs="+")
        edit.add_argument(
            "channels",
            help="Rendering definition, local file or OriginalFile:ID")

    def _lookup(self, gateway, type, oid):
        # TODO: move _lookup to a _configure type
        obj = gateway.getObject(type, oid)
        if not obj:
            self.ctx.die(110, "No such %s: %s" % (type, oid))
        return obj

    def render_images(self, gateway, object, batch=100):
        if isinstance(object, list):
            for x in object:
                for rv in self.render_images(gateway, x, batch):
                    yield rv
        elif isinstance(object, Screen):
            scr = self._lookup(gateway, "Screen", object.id)
            for plate in scr.listChildren():
                for rv in self.render_images(gateway, plate._obj, batch):
                    yield rv
        elif isinstance(object, Plate):
            plt = self._lookup(gateway, "Plate", object.id)
            rv = []
            for well in plt.listChildren():
                for idx in range(0, well.countWellSample()):
                    img = well.getImage(idx)
                    if batch == 1:
                        yield img
                    else:
                        rv.append(img)
                        if len(rv) == batch:
                            yield rv
                            rv = []
            if rv:
                yield rv
        elif isinstance(object, Image):
            img = self._lookup(gateway, "Image", object.id)
            if batch == 1:
                yield img
            else:
                yield [img]
        else:
            self.ctx.die(111, "TBD: %s" % object.__class__.__name__)

    def info(self, args):
        client = self.ctx.conn(args)
        gateway = BlitzGateway(client_obj=client)
        for img in self.render_images(gateway, args.object, batch=1):
            print RenderObject(img)

    def copy(self, args):
        client = self.ctx.conn(args)
        gateway = BlitzGateway(client_obj=client)
        self._copy(gateway, args.object, args.target, args.skipthumbs)

    def _copy(self, gateway, obj, target, skipthumbs):
        for src_img in self.render_images(gateway, obj, batch=1):
            for targets in self.render_images(gateway, target):
                batch = dict()
                for target in targets:
                    if target.id == src_img.id:
                        self.ctx.err("Skipping: Image:%s itself" % target.id)
                    else:
                        batch[target.id] = target

                if not batch:
                    continue

                rv = gateway.applySettingsToSet(src_img.id, "Image",
                                                batch.keys())
                for missing in rv[False]:
                    self.ctx.err("Error: Image:%s" % missing)
                    del batch[missing]

                if not skipthumbs:
                    self._generate_thumbs(batch.values())

    def update_channel_names(self, gateway, obj, namedict):
        for targets in self.render_images(gateway, obj):
            iids = [img.id for img in targets]
            counts = gateway.setChannelNames("Image", iids, namedict)
            if counts:
                self.ctx.dbg("Updated channel names for %d/%d images" % (
                    counts['updateCount'], counts['imageCount']))

    def _generate_thumbs(self, images):
        for img in images:
            start = time.time()
            img.getThumbnail(size=(96,), direct=False)
            stop = time.time()
            self.ctx.dbg("Image:%s got thumbnail in %2.2fs" % (
                img.id, stop - start))

    def edit(self, args):
        client = self.ctx.conn(args)
        gateway = BlitzGateway(client_obj=client)
        newchannels = {}
        data = pydict_text_io.load(
            args.channels, session=client.getSession())
        if 'channels' not in data:
            self.ctx.die(104, "ERROR: No channels found in %s" % args.channels)

        for chindex, chdict in data['channels'].iteritems():
            try:
                cindex = int(chindex)
            except Exception as e:
                self.ctx.err('ERROR: %s' % e)
                self.ctx.die(
                    105, "Invalid channel index: %s" % chindex)

            try:
                cobj = ChannelObject(chdict)
                if (cobj.min is None) != (cobj.max is None):
                    raise Exception('Both or neither of min and max required')
                newchannels[cindex] = cobj
                print '%d:%s' % (cindex, cobj)
            except Exception as e:
                self.ctx.err('ERROR: %s' % e)
                self.ctx.die(
                    105, "Invalid channel description: %s" % chdict)

        try:
            greyscale = data['greyscale']
        except KeyError:
            greyscale = None

        namedict = {}
        cindices = []
        rangelist = []
        colourlist = []
        for (i, c) in newchannels.iteritems():
            if c.label:
                namedict[i] = c.label
            if not c.active:
                continue
            cindices.append(i)
            rangelist.append([c.min, c.max])
            colourlist.append(c.color)

        if namedict:
            self.update_channel_names(gateway, args.object, namedict)

        for img in self.render_images(gateway, args.object, batch=1):
            img.setActiveChannels(
                cindices, windows=rangelist, colors=colourlist)
            if greyscale is not None:
                if greyscale:
                    img.setGreyscaleRenderingModel()
                else:
                    img.setColorRenderingModel()

            img.saveDefaults()
            self.ctx.dbg("Updated rendering settings for Image:%s" % img.id)
            if not args.skipthumbs:
                self._generate_thumbs([img])

            if args.copy:
                # Edit first image only, copy to rest
                self._copy(gateway, img._obj, args.object, args.skipthumbs)
                break


try:
    register("render", RenderControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("render", RenderControl, HELP)
        cli.invoke(sys.argv[1:])
