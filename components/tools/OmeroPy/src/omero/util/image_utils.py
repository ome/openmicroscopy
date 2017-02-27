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
Utility methods for dealing with scripts.
"""

try:
    from PIL import Image, ImageDraw, ImageFont  # see ticket:2597
except ImportError:
    import Image
    import ImageDraw
    import ImageFont  # see ticket:2597

import os.path
import omero.gateway
import StringIO
from omero.rtypes import rint

GATEWAYPATH = omero.gateway.THISPATH


def get_font(fontsize):
    """
    Returns a PIL ImageFont Sans-serif true-type font of the specified size
    or a pre-compiled font of fixed size if the ttf font is not found

    @param fontsize:	The size of the font you want
    @return: 	A PIL Font
    """

    font_path = os.path.join(GATEWAYPATH, "pilfonts", "FreeSans.ttf")
    try:
        font = ImageFont.truetype(font_path, fontsize)
    except:
        font = ImageFont.load('%s/pilfonts/B%0.2d.pil' % (GATEWAYPATH, 24))
    return font


def paste_image(image, canvas, x, y):
    """
    Pastes the image onto the canvas at the specified coordinates
    Image and canvas are instances of PIL 'Image'

    @param image:		The PIL image to be pasted. Image
    @param canvas:		The PIL image on which to paste. Image
    @param x:			X coordinate (left) to paste
    @param y: 			Y coordinate (top) to paste
    """

    x_right = image.size[0] + x
    y_bottom = image.size[1] + y
    # make a tuple of topleft-x, topleft-y, bottomRight-x, bottomRight-y
    pastebox = (x, y, x_right, y_bottom)
    canvas.paste(image, pastebox)


def paint_thumbnail_grid(thumbnail_store, length, spacing, pixel_ids,
                         col_count, bg=(255, 255, 255), left_label=None,
                         text_color=(0, 0, 0), fontsize=None, top_label=None):
    """
    Retrieves thumbnails for each pixelId, and places them in a grid,
    with White background.
    Option to add a vertical label to the left of the canvas
    Creates a PIL 'Image' which is returned

    @param thumbnail_store:  The omero thumbnail store.
    @param length:			 Length of longest thumbnail side, int
    @param spacing:			 The spacing between thumbnails and around the
                             edges. int
    @param pixel_ids:		 List of pixel IDs. [long]
    @param col_count:		 The number of columns. int
    @param bg:				 Background colour as (r,g,b).
                             Default is white (255, 255, 255)
    @param left_label: 		 Optional string to display vertically to the left.
    @param text_color:		 The color of the text as (r,g,b).
                             Default is black (0, 0, 0)
    @param fontsize:		 Size of the font.
                             Default is calculated based on thumbnail length,
                             int
    @return: 			    The PIL Image canvas.
    """
    mode = "RGB"
    # work out how many rows and columns are needed for all the images
    img_count = len(pixel_ids)

    row_count = (img_count / col_count)
    # check that we have enough rows and cols...
    while (col_count * row_count) < img_count:
        row_count += 1

    left_space = top_space = spacing
    min_width = 0

    text_height = 0
    if left_label or top_label:
        # if no images (no rows), need to make at least one row to show label
        if left_label is not None and row_count == 0:
            row_count = 1
        if fontsize is None:
            fontsize = length / 10 + 5
        font = get_font(fontsize)
        if left_label:
            text_width, text_height = font.getsize(left_label)
            left_space = spacing + text_height + spacing
        if top_label:
            text_width, text_height = font.getsize(top_label)
            top_space = spacing + text_height + spacing
            min_width = left_space + text_width + spacing

    # work out the canvas size needed, and create a white canvas
    cols_needed = min(col_count, img_count)
    v = left_space + cols_needed * (length + spacing)
    canvas_width = max(min_width, v)
    canvas_height = top_space + row_count * (length + spacing) + spacing
    mode = "RGB"
    size = (canvas_width, canvas_height)
    canvas = Image.new(mode, size, bg)

    # to write text up the left side, need to write it on horizontal canvas
    # and rotate.
    if left_label:
        label_canvas_width = canvas_height
        label_canvas_height = text_height + spacing
        label_size = (label_canvas_width, label_canvas_height)
        text_canvas = Image.new(mode, label_size, bg)
        draw = ImageDraw.Draw(text_canvas)
        text_width = font.getsize(left_label)[0]
        text_x = (label_canvas_width - text_width) / 2
        draw.text((text_x, spacing), left_label, font=font, fill=text_color)
        vertical_canvas = text_canvas.rotate(90)
        paste_image(vertical_canvas, canvas, 0, 0)
        del draw

    if top_label is not None:
        label_canvas_width = canvas_width
        label_canvas_height = text_height + spacing
        label_size = (label_canvas_width, label_canvas_height)
        text_canvas = Image.new(mode, label_size, bg)
        draw = ImageDraw.Draw(text_canvas)
        draw.text((spacing, spacing), top_label, font=font, fill=text_color)
        paste_image(text_canvas, canvas, left_space, 0)
        del draw

    # loop through the images, getting a thumbnail and placing it on a new row
    # and column
    r = 0
    c = 0
    thumbnail_map = thumbnail_store.getThumbnailByLongestSideSet(rint(length),
                                                                 pixel_ids)
    for pixels_id in pixel_ids:
        if pixels_id in thumbnail_map:
            thumbnail = thumbnail_map[pixels_id]
            # check we have a thumbnail (won't get one if image is invalid)
            if thumbnail:
                # make an "Image" from the string-encoded thumbnail
                thumb_image = Image.open(StringIO.StringIO(thumbnail))
                # paste the image onto the canvas at the correct coordinates
                # for the current row and column
                x = c * (length + spacing) + left_space
                y = r * (length + spacing) + top_space
                paste_image(thumb_image, canvas, x, y)

        # increment the column, and if we're at the last column, start a new
        # row
        c = c + 1
        if c == col_count:
            c = 0
            r = r + 1

    return canvas


def int_to_rgba(rgba):
    """
    Returns a tuple of (r,g,b,a) from an integer color
    r, g, b, a are 0-255.

    @param rgba:		A color as integer. Int
    @return:		A tuple of (r,g,b,a)
    """
    a = check_rgb_range(rgba % 256)
    b = check_rgb_range(rgba / 256 % 256)
    g = check_rgb_range(rgba / 256 / 256 % 256)
    r = check_rgb_range(rgba / 256 / 256 / 256 % 256)
    if a == 0:
        a = 255
    return (r, g, b, a)


def check_rgb_range(value):
    """
    Checks that the value is between 0 and 255. Returns integer value
    If the value is not valid, return 255
    (better to see something than nothing!)

    @param value:		The value to check.
    @return:			An integer between 0 and 255
    """
    try:
        v = int(value)
        if 0 <= v <= 255:
            return v
    except:
        return 255


def get_zoom_factor(image_size, max_width, max_height):
    """
    Returns the factor by which the Image has to be shrunk
    so that its dimensions are less that max_width and max_height
    e.g. if the image must be half-sized, this method returns 2.0 (float)

    @param image_size:  Size of the image as tuple (width, height)
    @param max_width:   The max width after zooming
    @param max_height:  The max height after zooming
    @return: The factor by which to shrink the image to be
             within max width and height
    """
    image_width, imageheight = image_size
    zoom_width = float(image_width) / float(max_width)
    zoom_height = float(imageheight) / float(max_height)
    return max(zoom_width, zoom_height)


def resize_image(image, max_width, max_height):
    """
    Resize the image so that it is as big as possible,
    within the dimensions max_width, max_height

    @param image: The PIL Image to zoom
    @param max_width: The max width of the zoomed image
    @param max_height: The max height of the zoomed image
    @return: The zoomed image. PIL Image.
    """
    image_width, image_height = image.size
    if image_width == max_width and image_height == max_height:
        return image
    # find which axis requires the biggest zoom (smallest relative max
    # dimension)
    zoom_width = float(image_width) / float(max_width)
    zoom_height = float(image_height) / float(max_height)
    zoom = max(zoom_width, zoom_height)
    if zoom_width >= zoom_height:  # size is defined by width
        max_height = int(image_height // zoom)  # calculate the new height
    else:
        max_width = int(image_width // zoom)
    return image.resize((max_width, max_height))
