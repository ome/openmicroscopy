"""
 components/tools/OmeroPy/scripts/thumbnailFigure.py

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

This script uses ROIs to measure FRAP on an image, and does simple FRAP calculations. 

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
import omero.scripts as scripts
import omero.util.script_utils as scriptUtil
import omero.util.figureUtil as figUtil
from omero.rtypes import *
import omero.util.imageUtil as imgUtil
import StringIO
import omero.clients
from omero.romio import PlaneDef
from datetime import date
# imports for making the chart and saving as PDF
reportLab = False

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597

try:
	from reportlab.pdfgen import canvas
	from reportlab.graphics.shapes import *
	from reportlab.graphics.charts.lineplots import LinePlot
	from reportlab.graphics.charts.textlabels import Label
	from reportlab.graphics import renderPDF
	from reportlab.graphics.widgets.markers import makeMarker
	from reportlab.pdfgen import canvas
	from reportlab.lib.pagesizes import A4
	reportLab = True
except:
	reportLab = False
#from reportlab.graphics import renderPM


JPEG = "image/jpeg"
PNG = "image/png"
PDF = "application/pdf"

logLines = []	# make a log / legend of the figure
def log(text):
	""" Adds lines of text to the logLines list, so they can be collected into a figure legend. """
	# print text
	logLines.append(text)
	

def average(numbers):
	"""
	Returns the average of the list of numbers
	"""
	if len(numbers) == 0:
		return 0
	total = 0
	for n in numbers:
		total += n
	return float(total) / float(len(numbers))
	
		
def getEllipses(roiService, imageId, textValues):
	"""
	Returns a map of textValue: shapeMap. 
	shapeMap is a map of t: shape. 
	
	@param roiService:		The Omero ROI service
	@param imageId:			The ID of the Image
	@param textValues:		A list of text values for the ROIs we want. E.g. "FRAP", "Whole", "Base"
							At least one shape of an ROI must have the correct text value for the ROI to be picked. 
	"""
	
	ellipseMap = {}
	
	result = roiService.findByImage(imageId, None)
	
	for roi in result.rois:
		shapeMap = {} # map shapes by Time
		roiName = None
		for shape in roi.copyShapes():
			if type(shape) == omero.model.EllipseI:
				# if any of the shapes have a matching text-value, use that value for the map
				if shape.getTextValue().getValue() in textValues:
					roiName = shape.getTextValue().getValue()
				cx = int(shape.getCx().getValue())
				cy = int(shape.getCy().getValue())
				rx = int(shape.getRx().getValue())
				ry = int(shape.getRy().getValue())
				z = int(shape.getTheZ().getValue())
				t = int(shape.getTheT().getValue())
				shapeMap[t] = (cx, cy, rx, ry, z)
		if roiName:
			ellipseMap[roiName] = shapeMap
			
	return ellipseMap
	
	
def getEllipsePixels(ellipse):
	"""
	Returns a list of (x,y) tuples for all the points within the given ellipse. 
	
	@param ellipse:		The ellipse defined as a tuple (cx, cy, rx, ry, z)
	@returns:			A list of (x,y) points for the ellipse
	"""
	cx, cy, rx, ry, z = ellipse
	# find bounding box of ellipse
	xStart = cx - rx
	xEnd = cx + rx
	yStart = cy - ry
	yEnd = cy + ry
	
	points = []
	for x in range(xStart, xEnd):
		line = []
		for y in range(yStart, yEnd):
			dx = x - cx
			dy = y - cy
			r = float(dx*dx)/float(rx*rx) + float(dy*dy)/float(ry*ry)
			if r <= 1:
				points.append((x,y))
	return points
		
		
def analyseEllipses(ellipses, pixels, rawPixelStore, theC, theT, theZ):
	"""
	Returns a list of average pixel intensities corresponding to the ellipses, which must all exist on
	a single plane, defined by pixels, and C, T, Z indexes. 
	
	@param ellipses:		A list of the ellipses to analyse, each defined as (cx, cy, rx, ry, z)
	@param pixels:			The pixels object, with PixelsType object loaded, so that getPixelsType() can be called.
	@param rawPixelStore:	The Omero rawPixelsStore
	@param theC:			C index for plane to analyse
	@param theT:			T index for plane to analyse
	@param theZ:			Z index for plane to analyse
	"""
	plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
	
	results = []
	
	for ellipse in ellipses:
		points = getEllipsePixels(ellipse)
		totalValue = 0
		for p in points:
			x, y = p
			value = plane2D[y][x]
			totalValue += value
		average = totalValue/len(points)
		results.append(average)
		
	return results


def getPlaneImage(re, pixelsId, theZ, theT):
	"""
	Returns a PIL Image for the plane defined by pixelsId, theZ and theT
	
	@param re:		The Omero RenderingEngine
	@param pixelsId:	The Pixels ID
	@param theZ:		Z index of plane
	@param theT:		T index of plane
	@return:			PIL Image
	"""
	pDef = PlaneDef()
	pDef.t = theT
	pDef.z = theZ
	
	re.lookupPixels(pixelsId)
	re.lookupRenderingDef(pixelsId)
	re.load()
	
	imageData = re.renderCompressed(pDef)
	imagePlane = Image.open(StringIO.StringIO(imageData))
	
	return imagePlane
	
	
def addEllipse(imagePlane, ellipse, zoomFactor):
	
	"""
	Draws an ellipse on the Image, first multiplying the dimensions of the ellipse by the zoomFactor. 
	
	@param imagePlane:		The PIL Image to draw on. 
	@param ellipse:			The ellipse defined by (cx, cy, rx, ry, z) 
	@param zoomFactor:		Multiply the dimensions of the ellipse by this before drawing.
	"""
	cx, cy, rx, ry, z = ellipse
	
	x = int(zoomFactor*(cx-rx))
	y = int(zoomFactor*(cy-ry))
	w = int(zoomFactor*(cx+rx))
	h = int(zoomFactor*(cy+ry))

	xy = (x,y,w,h)
	print xy
	draw = ImageDraw.Draw(imagePlane)
	draw.ellipse(xy, outline=(255,0,0))
	
	return imagePlane
	
	
def makeFrapFigure(session, commandArgs):
	"""
	Main method called to make the figure. 
	Returns fileID object of the child of the fileAnnotation
	"""
	roiService = session.getRoiService()
	queryService = session.getQueryService()
	updateService = session.getUpdateService()
	rawFileStore = session.createRawFileStore()
	rawPixelStore = session.createRawPixelsStore()
	renderingEngine = session.createRenderingEngine()
	
	imageId = commandArgs["imageId"]
	
	theC = 0
	if "theC" in commandArgs:
		theC = commandArgs["theC"]
	
	image = queryService.get("Image", imageId)
	imageName = image.getName().getValue()
	
	query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
	pixels = queryService.findByQuery(query_string, None)
	
	#pixels = image.getPrimaryPixels()
	pixelsId = pixels.getId().getValue()
	
	
	#sizeX = pixels.getSizeX().getValue()
	#sizeY = pixels.getSizeY().getValue()
	#sizeZ = pixels.getSizeZ().getValue()
	#sizeC = pixels.getSizeC().getValue()
	#sizeT = pixels.getSizeT().getValue()
	
	bypassOriginalFile = True
	rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)

	roiLabels = ["FRAP", "Base", "Whole"]
	
	roiMap = getEllipses(roiService, imageId, roiLabels)
	
	for l in roiLabels:
		if l not in roiMap.keys():
			print "ROI: '%s' not found. Cannot calculate FRAP" % l
			return
			
			
	frapMap = roiMap["FRAP"]
	baseMap = roiMap["Base"]
	wholeMap = roiMap["Whole"]
	
	# make a list of the t indexes that have all 3 of the Shapes we need. 
	# and a list of the roiShapes for easy access.
	tIndexes = []
	frapROI = []
	baseROI = []
	wholeROI = []
	for t in frapMap.keys():
		if t in baseMap.keys() and t in wholeMap.keys():
			tIndexes.append(t)	
			frapROI.append(frapMap[t])	
			baseROI.append(baseMap[t])	
			wholeROI.append(wholeMap[t])
	tIndexes.sort()
	
	log("T Indexes, " + ",".join([str(t) for t in tIndexes]))
	
	# get the actual plane times. 
	timeMap = figUtil.getTimes(queryService, pixelsId, tIndexes, theZ=0, theC=0)
	timeList = []
	for t in tIndexes:
		if t in timeMap:	
			timeList.append(timeMap[t])
		else:	# handles images which don't have PlaneInfo
			timeMap[t] = t
			timeList.append(t)
			
	log("Plane times (secs), " + ",".join([str(t) for t in timeList]))
	
	# lists of averageIntensity for the 3 ROIs 
	frapValues = []
	baseValues = []
	wholeValues = []
	
	frapBleach = None
	
	theZ = 0
	for i, t in enumerate(tIndexes):
		shapes = [frapROI[i], baseROI[i], wholeROI[i]]
		theZ = frapROI[i][4]	# get theZ from the FRAP ROI
		# get a list of the average values of pixels in the three shapes. 
		averages = analyseEllipses(shapes, pixels, rawPixelStore, theC, t, theZ)
		if frapBleach == None:	
			frapBleach = averages[0]
		else:
			frapBleach = min(frapBleach, averages[0])
		frapValues.append(averages[0])
		baseValues.append(averages[1])
		wholeValues.append(averages[2])

	log("FRAP Values, " + ",".join([str(v) for v in frapValues]))
	log("Base Values, " + ",".join([str(v) for v in baseValues]))
	log("Whole Values, " + ",".join([str(v) for v in wholeValues]))
	
	# find the time of the bleach event (lowest intensity )
	tBleach = frapValues.index(frapBleach)
	log("Pre-bleach frames, %d" % tBleach)
	if tBleach == 0:
		print "No pre-bleach images. Can't calculate FRAP"
		return
		
	# using frames before and after tBleach - calculate bleach ranges etc. 
	frapPre = average(frapValues[:tBleach]) - average(baseValues[:tBleach])
	wholePre = average(wholeValues[:tBleach]) - average(baseValues[:tBleach])
	wholePost = average(wholeValues[tBleach:]) - average(baseValues[tBleach:])

	# use these values to get a ratio of FRAP intensity / pre-Bleach intensity * (corrected by intensity of 'Whole' ROI)
	frapNormCorr = []
	for i in range(len(tIndexes)):
		frapNormCorr.append( (float(frapValues[i] - baseValues[i]) / frapPre) * (wholePre / float(wholeValues[i] - baseValues[i])) )
	
	log("FRAP Corrected, " + ",".join([str(v) for v in frapNormCorr]))
	
	# work out the range of recovery (bleach -> plateau) and the time to reach half of this after bleach. 
	frapBleachNormCorr = frapNormCorr[tBleach]
	plateauNormCorr = average(frapNormCorr[-5:])
	plateauMinusBleachNormCorr = plateauNormCorr - frapBleachNormCorr
	mobileFraction = plateauMinusBleachNormCorr / float(1 - frapBleachNormCorr)
	immobileFraction = 1 - mobileFraction
	halfMaxNormCorr = plateauMinusBleachNormCorr /2 + frapBleachNormCorr
	
	log("Corrected Bleach Intensity, %f" % frapBleachNormCorr)
	log("Corrected Plateau Intensity, %f" % plateauNormCorr)
	log("Plateau - Bleach, %f" % plateauMinusBleachNormCorr)
	log("Mobile Fraction, %f" % mobileFraction)
	log("Immobile Fraction, %f" % immobileFraction)
	log("Half Recovered Intensity, %f" % halfMaxNormCorr)

	# Define the T-half for this FRAP. In place of fitting an exact curve to the
	# data, find the two time-points that the half Max of recovery sits between
	# and find the T-half using a linear approximation between these two points.
	# The T-half is this solved for halfMaxNormCorr - timestamp(tBleach)
	th = None
	for t in tIndexes[tBleach:]:
		if halfMaxNormCorr < frapNormCorr[t]:
			th = tIndexes[t]
			break
	
	y1 = frapNormCorr[th-1]
	y2 = frapNormCorr[th]
	
	
	x1 = timeList[th-1]
	x2 = timeList[th]
	m1 = (y2-y1)/(x2-x1); #Gradient of the line
	c1 = y1-m1*x1;  #Y-intercept
	tHalf = (halfMaxNormCorr-c1)/m1 - timeList[tBleach]
	
	log("Bleach time, %f seconds" % timeList[tBleach])
	log("T-Half, %f seconds" % tHalf)
	
	figLegend = "\n".join(logLines)
	print figLegend
	
	# make PIL image of the last frame before FRAP
	spacer = 5
	frames = []
	ellipses = [frapROI[tBleach-1], frapROI[tBleach], frapROI[-1]]
	frames.append(getPlaneImage(renderingEngine, pixelsId, theZ, tIndexes[tBleach-1]))
	frames.append(getPlaneImage(renderingEngine, pixelsId, theZ, tIndexes[tBleach]))
	frames.append(getPlaneImage(renderingEngine, pixelsId, theZ, tIndexes[-1]))
	figW = 450
	font = imgUtil.getFont(16)
	fontH = font.getsize("FRAP")[1]
	labels = ["Pre-Bleach", "Bleach", "Recovery"]
	imgW = (figW - (2 * spacer) ) / len(frames)
	# shrink the images by width, or maintain height if shrink not needed. 
	smallImages = [imgUtil.resizeImage(img, imgW, img.size[1]) for img in frames]
	zoomOut = 1/imgUtil.getZoomFactor(img.size, imgW, img.size[1])
	figH = smallImages[0].size[1] + spacer + fontH 
	frapCanvas = Image.new("RGB", (figW, figH), (255,255,255))
	draw = ImageDraw.Draw(frapCanvas)
	y = spacer + fontH
	x = 0
	for l, img in enumerate(frames):
		label = labels[l]
		indent = (imgW - font.getsize(label)[0]) / 2
		draw.text((x+indent, 0), label, font=font, fill=(0,0,0))
		roiImage = addEllipse(smallImages[l], ellipses[l], zoomOut)
		imgUtil.pasteImage(roiImage, frapCanvas, x, y)
		x += spacer + imgW
	#frapCanvas.show()		# bug-fixing only
	fileName = imageName + ".png"
	frapCanvas.save(fileName, "PNG")
	
	format = PNG
	output = fileName
	
	# if reportLab has imported...
	if reportLab:
		# we are going to export a PDF, not a JPEG
		format = PDF
		output = imageName + ".pdf"
		
		# create a plot of curve fitted to:  y = 1 - e(It)
		# where thalf = ln 0.5 / -I
		# http://www.embl.de/eamnet/frap/html/halftime.html
		import math
		i = 1/float(tHalf) * math.log(0.5)
		fittedPoints = []
		for t in timeList[3:]:
			print math.exp(t * i)
			f = frapBleachNormCorr + ((plateauNormCorr-frapBleachNormCorr) * (1 - math.exp(t * i)))
			fittedPoints.append(f)
		print fittedPoints
		log("Fitted: , " + str(fittedPoints))
		
		# create a plot of the FRAP data
		figHeight = 450
		figWidth = 400
		drawing = Drawing(figWidth, figHeight)
		lp = LinePlot()
		lp.x = 50
		lp.y = 50
		lp.height = 300
		lp.width = 300
		lp.data = [zip(timeList, frapNormCorr), zip(timeList[3:], fittedPoints)]
		lp.lines[0].strokeColor = colors.red
		lp.lines[0].symbol = makeMarker('Circle')
		lp.lines[1].strokeColor = colors.green
		lp.lines[1].symbol = makeMarker('Circle')
		
		drawing.add(lp)
	
		drawing.add(String(200,25, 'Time (seconds)', fontSize=12, textAnchor="middle"))
		drawing.add(String(200,figHeight-25, imageName, fontSize=12, textAnchor="middle"))
		drawing.add(String(200,figHeight-50, 'T(1/2) = %f' % tHalf, fontSize=12, textAnchor="middle"))
	
		# create an A4 canvas to make the pdf figure 
		figCanvas = canvas.Canvas(output, pagesize=A4)
		pasteX = 100
		pasteY = 75
		# add the FRAP image
		figCanvas.drawImage(fileName, pasteX-25, pasteY)
		# add the FRAP data plot
		renderPDF.draw(drawing, figCanvas, pasteX, 300, showBoundary=True)
		figCanvas.save()
	
	fileId = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, output, format, figLegend)
	return fileId
	

def runAsScript():
	client = scripts.client('frapFigure.py', 'Create a figure of FRAP data for an image.', 
	scripts.Long("imageId").inout(),		# IDs of the image we want to analyse
	scripts.Long("theC", optional=True).inout(),		# Channel we want to analyse. Default is 0
	scripts.Long("fileAnnotation").out());  	# script returns a file annotation
	
	session = client.getSession()
	commandArgs = {}
	
	for key in client.getInputKeys():
		if client.getInput(key):
			commandArgs[key] = client.getInput(key).getValue()
	
	fileId = makeFrapFigure(session, commandArgs)
	client.setOutput("fileAnnotation",fileId)
	
	
if __name__ == "__main__":
	runAsScript()
