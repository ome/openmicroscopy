#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 components/tools/OmeroPy/src/omero/util/imageUitl.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2009 University of Dundee. All rights reserved.


  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

------------------------------------------------------------------------------

A collection of utility methods based on the Python Imaging Library (PIL)
used for making figures. 

@author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.1
 
"""


try:
    from PIL import Image, ImageDraw, ImageFont # see ticket:2597
except ImportError:
    import Image, ImageDraw, ImageFont # see ticket:2597

import os.path
import omero.gateway
import StringIO
from omero.rtypes import *

GATEWAYPATH = omero.gateway.THISPATH

def getFont(fontsize):
	""" 
	Returns a PIL ImageFont Sans-serif true-type font of the specified size 
	or a pre-compiled font of fixed size if the ttf font is not found 
	
	@param fontsize:	The size of the font you want
	@return: 	A PIL Font
	"""
		
	fontPath = os.path.join(GATEWAYPATH, "pilfonts", "FreeSans.ttf")
	try:
		font  =  ImageFont.truetype ( fontPath, fontsize )	
	except:
		font = ImageFont.load('%s/pilfonts/B%0.2d.pil' % (GATEWAYPATH, 24) )
	return font
	

def pasteImage(image, canvas, x, y):
	"""
	Pastes the image onto the canvas at the specified coordinates
	Image and canvas are instances of PIL 'Image' 
	
	@param image:		The PIL image to be pasted. Image
	@param canvas:		The PIL image on which to paste. Image
	@param x:			X coordinate (left) to paste
	@param y: 			Y coordinate (top) to paste
	"""
	
	xRight = image.size[0] + x
	yBottom = image.size[1] + y
	# make a tuple of topleft-x, topleft-y, bottomRight-x, bottomRight-y
	pasteBox = (x, y, xRight, yBottom)
	canvas.paste(image, pasteBox)
 	

def getThumbnail(thumbnailStore, pixelsId, length):
	""" 
	Returns a thumbnail (as string) from the pixelsId, the longest side is 'length'  
	
	@param thumbnailStore: 	The Omero thumbnail store
	@param pixelsId:		The ID of the pixels. long
	@param length:		Length of longest side. int
	@return:		The thumbnail as a String, or None if not found (invalid image)
	"""
	if not thumbnailStore.setPixelsId(pixelsId):
		thumbnailStore.needDefaults()
		thumbnailStore.setPixelsId(pixelsId)
	try:
		return thumbnailStore.getThumbnailByLongestSide(rint(length))	# returns string (api says Ice::ByteSeq)
	except:
		return None
		
def getThumbnailSet(thumbnailStore, length, pixelIds):
	""" 
	Returns map of thumbnails whose keys are the pixels id and the values are the image, the longest side is 'length'  
	
	@param thumbnailStore: 	The Omero thumbnail store
	@param pixelIds:		The collection of pixels ID.
	@param length:		Length of longest side. int
	@return: See above
	"""	
	try:
		return thumbnailStore.getThumbnailByLongestSideSet(rint(length), pixelIds)	# returns string (api says Ice::ByteSeq)
	except:
		return None
	
def paintThumbnailGrid(thumbnailStore, length, spacing, pixelIds, colCount, bg=(255,255,255), 
			leftLabel=None, textColour=(0,0,0), fontsize=None, topLabel=None):
	""" 
	Retrieves thumbnails for each pixelId, and places them in a grid, with White background. 
	Option to add a vertical label to the left of the canvas
	Creates a PIL 'Image' which is returned 
	
	@param thumbnailStore:		The omero thumbnail store. 
	@param length:			Length of longest thumbnail side. int
	@param spacing:			The spacing between thumbnails and around the edges. int
	@param pixelIds:		List of pixel IDs. [long]
	@param colCount:		The number of columns. int
	@param bg:				Background colour as (r,g,b). Default is white (255,255,255)
	@param leftLabel: 		Optional string to display vertically to the left.
	@param textColour:		The colour of the text as (r,g,b). Default is black (0,0,0)
	@param fontsize:		Size of the font. Defualt is calculated based on thumbnail length. int
	@return: 			The PIL Image canvas. 
	"""
	mode = "RGB"
	# work out how many rows and columns are needed for all the images
	imgCount = len(pixelIds)
	
	rowCount = (imgCount/colCount)
	while (colCount * rowCount) < imgCount:	# check that we have enough rows and cols...
		rowCount += 1
		
	leftSpace = topSpace = spacing
	minWidth = 0
	
	textHeight = 0
	if leftLabel or topLabel:
		# if no images (no rows), need to make at least one row to show label
		if leftLabel is not None and rowCount == 0: rowCount = 1
		if fontsize == None: 
			fontsize = length/10 + 5
		font = getFont(fontsize)
		if leftLabel:
			textWidth, textHeight = font.getsize(leftLabel)
			leftSpace = spacing + textHeight + spacing
		if topLabel:
			textWidth, textHeight = font.getsize(topLabel)
			topSpace = spacing + textHeight + spacing
			minWidth = leftSpace + textWidth + spacing

	# work out the canvas size needed, and create a white canvas
	colsNeeded = min(colCount, imgCount)
	canvasWidth = max(minWidth, (leftSpace + colsNeeded * (length+spacing)))
	canvasHeight = topSpace + rowCount * (length+spacing) + spacing
	mode = "RGB"
	size = (canvasWidth, canvasHeight)
	canvas = Image.new(mode, size, bg)
	
	# to write text up the left side, need to write it on horizontal canvas and rotate.
	if leftLabel:
		labelCanvasWidth = canvasHeight
		labelCanvasHeight = textHeight + spacing
		labelSize = (labelCanvasWidth, labelCanvasHeight)
		textCanvas = Image.new(mode, labelSize, bg)
		draw = ImageDraw.Draw(textCanvas)
		textWidth = font.getsize(leftLabel)[0]
		textX = (labelCanvasWidth - textWidth) / 2
		draw.text((textX, spacing), leftLabel, font=font, fill=textColour)
		verticalCanvas = textCanvas.rotate(90)
		pasteImage(verticalCanvas, canvas, 0, 0)
		del draw
	
	if topLabel is not None:
		labelCanvasWidth = canvasWidth
		labelCanvasHeight = textHeight + spacing
		labelSize = (labelCanvasWidth, labelCanvasHeight)
		textCanvas = Image.new(mode, labelSize, bg)
		draw = ImageDraw.Draw(textCanvas)
		draw.text((spacing, spacing), topLabel, font=font, fill=textColour)
		pasteImage(textCanvas, canvas, leftSpace, 0)
		del draw
		
	# loop through the images, getting a thumbnail and placing it on a new row and column
	r = 0
	c = 0
	thumbnailMap = getThumbnailSet(thumbnailStore, length, pixelIds)
	for pixelsId in pixelIds:
		if pixelsId in thumbnailMap:
			thumbnail = thumbnailMap[pixelsId]#getThumbnail(thumbnailStore, pixelsId, length)
			if thumbnail:	# check we have a thumbnail (won't get one if image is invalid)
				thumbImage = Image.open(StringIO.StringIO(thumbnail))	# make an "Image" from the string-encoded thumbnail
				# paste the image onto the canvas at the correct coordinates for the current row and column 
				x = c*(length+spacing) + leftSpace
				y = r*(length+spacing) + topSpace
				pasteImage(thumbImage, canvas, x, y)
				
		# increment the column, and if we're at the last column, start a new row
		c = c + 1
		if c == colCount:
			c = 0
			r = r + 1
			
	return canvas
	

def checkRGBRange(value):
	""" 
	Checks that the value is between 0 and 255. Returns integer value 
	If the value is not valid, return 255 (better to see something than nothing! )
	
	@param value:		The value to check.
	@return:			An integer between 0 and 255
	"""
	try:
		v = int(value)
		if 0 <= v <= 255:
			return v
	except:
		return 255
	
	
def RGBIntToRGBA(RGB):
	""" 
	Returns a tuple of (r,g,b,a) from an integer colour
	r, g, b, a are 0-255. 
	
	@param RGB:		A colour as integer. Int
	@return:		A tuple of (r,g,b,a)
	"""
	r = checkRGBRange((RGB >> 16) & 0xFF)
	g = checkRGBRange((RGB >> 8) & 0xFF)
	b = checkRGBRange((RGB >> 0) & 0xFF)
	a = checkRGBRange((RGB >> 24) & 0xFF)
	if a == 0:
		a = 255
	return (r,g,b,a)
	
	
def RGBIntToRGB(RGB):
	"""
	Returns a tuple of (r,g,b) from an integer colour
	r, g, b are 0-255.
	
	@param RGB:		A colour as integer. Int
	@return:		A tuple of (r,g,b)
	"""
	r,g,b,a = RGBIntToRGBA(RGB)
	return (r,g,b)
	

def getZoomFactor(imageSize, maxW, maxH):
	""" 
	Returns the factor by which the Image has to be shrunk so that it's dimensions are less that maxW and maxH 
	E.g. If the image must be half-sized, this method returns 2.0 (float)
	
	@param imageSize: 		Size of the image as tuple (width, height)
	@param maxW:			The max width after zooming
	@param maxH:			The max height after zooming
	@return:			The factor by which to shrink the image to be within max width and height
	"""
	imageW, imageH = imageSize
	zoomW = float(imageW) / float(maxW)
	zoomH = float(imageH) / float(maxH)
	return max(zoomW, zoomH)
	
	
def resizeImage(image, maxW, maxH):
	""" 
	Resize the image so that it is as big as possible, within the dimensions maxW, maxH 
	
	@param image:		The PIL Image to zoom
	@param maxW:		The max width of the zoomed image
	@param maxH:		The max height of the zoomed image
	@return:		The zoomed image. PIL Image. 
	"""
	imageW, imageH = image.size
	if imageW == maxW and imageH == maxH:
		return image
	# find which axis requires the biggest zoom (smallest relative max dimension)
	zoomW = float(imageW) / float(maxW)
	zoomH = float(imageH) / float(maxH)
	zoom = max(zoomW, zoomH)
	if zoomW >= zoomH:	# size is defined by width
		maxH = int(imageH//zoom)	# calculate the new height
	else:
		maxW = int(imageW//zoom)
	return image.resize((maxW, maxH))
