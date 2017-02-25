#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

"""
Utility methods for manipulating roi.
"""

from numpy import asarray, int32, math, zeros, hstack, vstack
import omero.util.script_utils as script_utils


def get_line_data(pixels, x1, y1, x2, y2, line_w=2, the_z=0, the_c=0, the_t=0):
    """
    Grabs pixel data covering the specified line, and rotates it horizontally
    so that x1,y1 is to the left,
    Returning a numpy 2d array. Used by Kymograph.py script.
    Uses PIL to handle rotating and interpolating the data. Converts to numpy
    to PIL and back (may change dtype.)

    @param pixels:          PixelsWrapper object
    @param x1, y1, x2, y2:  Coordinates of line
    @param line_w:          Width of the line we want
    @param the_z:           Z index within pixels
    @param the_c:           Channel index
    @param the_t:           Time index
    """

    size_x = pixels.getSizeX()
    size_y = pixels.getSizeY()

    line_x = x2-x1
    line_y = y2-y1

    rads = math.atan(float(line_x)/line_y)

    # How much extra Height do we need, top and bottom?
    extra_h = abs(math.sin(rads) * line_w)
    bottom = int(max(y1, y2) + extra_h/2)
    top = int(min(y1, y2) - extra_h/2)

    # How much extra width do we need, left and right?
    extra_w = abs(math.cos(rads) * line_w)
    left = int(min(x1, x2) - extra_w)
    right = int(max(x1, x2) + extra_w)

    # What's the larger area we need? - Are we outside the image?
    pad_left, pad_right, pad_top, pad_bottom = 0, 0, 0, 0
    if left < 0:
        pad_left = abs(left)
        left = 0
    x = left
    if top < 0:
        pad_top = abs(top)
        top = 0
    y = top
    if right > size_x:
        pad_right = right-size_x
        right = size_x
    w = int(right - left)
    if bottom > size_y:
        pad_bottom = bottom-size_y
        bottom = size_y
    h = int(bottom - top)
    tile = (x, y, w, h)

    # get the Tile
    plane = pixels.getTile(the_z, the_c, the_t, tile)

    # pad if we wanted a bigger region
    if pad_left > 0:
        data_h, data_w = plane.shape
        pad_data = zeros((data_h, pad_left), dtype=plane.dtype)
        plane = hstack((pad_data, plane))
    if pad_right > 0:
        data_h, data_w = plane.shape
        pad_data = zeros((data_h, pad_right), dtype=plane.dtype)
        plane = hstack((plane, pad_data))
    if pad_top > 0:
        data_h, data_w = plane.shape
        pad_data = zeros((pad_top, data_w), dtype=plane.dtype)
        plane = vstack((pad_data, plane))
    if pad_bottom > 0:
        data_h, data_w = plane.shape
        pad_data = zeros((pad_bottom, data_w), dtype=plane.dtype)
        plane = vstack((plane, pad_data))

    pil = script_utils.numpy_to_image(plane, (plane.min(), plane.max()), int32)

    # Now need to rotate so that x1,y1 is horizontally to the left of x2,y2
    to_rotate = 90 - math.degrees(rads)

    if x1 > x2:
        to_rotate += 180
    # filter=Image.BICUBIC see
    # http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2172449/
    rotated = pil.rotate(to_rotate, expand=True)
    # rotated.show()

    # finally we need to crop to the length of the line
    length = int(math.sqrt(math.pow(line_x, 2) + math.pow(line_y, 2)))
    rot_w, rot_h = rotated.size
    crop_x = (rot_w - length)/2
    crop_x2 = crop_x + length
    crop_y = (rot_h - line_w)/2
    crop_y2 = crop_y + line_w
    cropped = rotated.crop((crop_x, crop_y, crop_x2, crop_y2))
    return asarray(cropped)


def points_string_to_xy_list(string):
    """
    Method for converting the string returned from
    omero.model.ShapeI.getPoints() into list of (x,y) points
    e.g. "points[309,427, 366,503, 190,491]"
    """
    point_lists = string.strip().split("points")
    if len(point_lists) < 2:
        if len(point_lists) == 1 and point_lists[0]:
            xys = point_lists[0].split()
            xy_list = [tuple(map(int, xy.split(','))) for xy in xys]
            return xy_list
        raise ValueError("Unrecognised ROI shape 'points' string: %s" % string)

    first_list = point_lists[1]
    xy_list = []
    for xy in first_list.strip(" []").split(", "):
        x, y = xy.split(",")
        xy_list.append((int(x.strip()), int(y.strip())))
    return xy_list
