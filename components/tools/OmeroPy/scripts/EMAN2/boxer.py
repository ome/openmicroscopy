"""

This script is a "proof of principle" to demonstrate how Spider and EMAN2 can work with OMERO. 
This uses the auto-box functionality of EMAN2, which takes one or more user-defined particle ROIs, 
and uses this as the basis for picking additional particles from the image. 
In this script, OMERO is used as the source of the image, with the user-defined particles as ROIs on
the server. Spider is used to turn the plane array into a Spider image. The EMAN2 subclasses 
use this, with the server ROI(s) to generate additional ROIs which are then saved back to 
the server. 
The script below is currently implemented as a client-side script, but could easily be 
converted into a server-side script, with EMAN2 and Spider installed on the server. 

"""
from EMAN2 import *

from e2boxer import *

import numpy
import omero
from omero.rtypes import *
import omero.util.script_utils as scriptUtil
from Spider.Spiderarray import array2spider

from Tkinter import Tk, Label
import Image, ImageTk


class DummyWindow():
	"""
	Just need a blank class to replace the need for a UI
	"""
	def updateGL(self):
		pass


class OmeroSwarmPanel():
	"""
	Another blank class to replace the need for a UI
	"""
	
	def __init__(self, box_size):
		self.box_size = box_size
		
	def set_picking_data(self, peak_score, profile, profile_trough_point):
		print "Dummy PanelObject: set_picking_data()"
		print "    peak_score: " + str(peak_score)
		print "    profile: " + str(profile)
		print "    profile_trough_point: " + str(profile_trough_point)


class OmeroSwarmTool(SwarmBoxer):
	'''
	Subclass the main Boxer class, to add data source, UI elements etc. 
	'''	
	
	def __init__(self,target,particle_diameter=128):
		# import sys
		# print >> sys.stderr, target.__class__
		SwarmBoxer.__init__(self,particle_diameter)
		self.target = weakref.ref(target)	# now, the target() method will return target
		window = DummyWindow()
		def getWindow():
			return window
		self.get_2d_window = getWindow
		#self.panel_object = SwarmPanel(self,self.particle_diameter)
		self.panel_object = OmeroSwarmPanel(self.particle_diameter)	# needs to implement set_picking_data(self.peak_score, self.profile, self.profile_trough_point)
		self.gui_mode = False


class Target():
	"""
	Dummy target to get / save boxes to omero etc. 
	Replaces the functionality of emboxerbase.EMBoxerModule which is used in the workflow UI. 
	Takes a reference to an OMERO session in the constructor, which is then used to write the picked particles as 
	ROIs to the image, identified by imageId
	"""
	def __init__(self, box_size, session, imageId):
		def getFileName():
			return "/Users/will/Documents/dev/EMAN2/06jul12a.mrc"
		self.current_file = getFileName
		#self.current_file = "/Users/will/Documents/dev/EMAN2/06jul12a.mrc"
		self.box_size = box_size
		self.box_list = EMBoxList(self)		# has methods like detect_collision()
		self.session = session
		self.imageId = imageId
		
		# create the service and image for adding ROIs later...
		self.updateService = self.session.getUpdateService()
		gateway = self.session.createGateway()
		self.image = gateway.getImage(self.imageId)
		self.imageY = self.image.getPrimaryPixels().getSizeY().getValue()
	
	# code from emboxerbase.EMBoxerModule
	def add_box(self, x, y, type):
		"""
		add a box to the list
		If type = SwarmBoxer.REF_NAME then this is a reference box. 
		"""
		print "add_box() x: %d, y: %d" % (x, y)
		box_num = self.box_list.add_box(x,y,type=type)
		
		
	# code from emboxerbase.EMBoxerModule
	def clear_boxes(self,type,cache=False):
		self.box_list.clear_boxes(type,cache=cache)
	
	
	# code from emboxerbase.EMBoxerModule
	def get_box(self,box_number):
		'''
		@param box_number the number of the box for which you want to get
		'''
		return self.box_list.get_box(box_number)
	
	
	# code from emboxerbase.EMBoxerModule
	def set_box(self,box,box_number,update_display=False):
		'''
		@param box_number the number of the box for which you want to get
		'''
		self.box_list.set_box(box,box_number)
		#if update_display:
		#	self.full_box_update()
	
	
	# code from emboxerbase.EMBoxerModule
	def add_boxes(self,boxes,update_gl=True):
		'''
		boxes should be a list like [[x,y,type],[x,y,type],....[int,int,string]]
		'''
		for b in boxes:
			print b
			x,y,typeString,v = b
			x -= self.box_size/2	# convert from centre of particle, to top-left of ROI
			y = self.imageY - y		# convert from bottom to top Y coordinates. 
			y -= self.box_size/2
			self.addRectangleRoi(x, y, typeString)
		# removed a lot of UI code from emboxerbase.EMBoxerModule
		self.box_list.add_boxes(boxes)
		
		
	# code from emboxerbase.EMBoxerModule
	def get_subsample_rate(self): 
		'''
		
		'''
		return int(math.ceil(float(self.box_size)/float(TEMPLATE_MIN)))
				
	# code from emboxerbase.EMBoxerModule
	def get_exclusion_image(self,mark_boxes=False):
		'''
		@mark_boxes if true the exclusion image is copied and the locations of the current boxes are painted in as excluded regions
		This is useful for autoboxers - they  obviously dont want to box any region that already has a box in it (such as a manual box,
		or a previously autoboxed box)
		'''
		exc_image = ScaledExclusionImageCache.get_image(self.current_file(), self.get_subsample_rate()) 	# class 'libpyEMData2.EMData'
		print "Target get_exclusion_image() mark_boxes: " + str(mark_boxes)	
		
		#display(exc_image)		# blank image
		
		if not mark_boxes: return exc_image
		
		else:
			#print "    Ignoring request to mark_boxes: returning unmasked image..."
			#return exc_image 	# hack to avoid fixing the code below. Manually added boxes will not be excluded from being auto-boxed. 
			
			image = exc_image.copy()
			boxes = self.box_list.get_boxes()
			if len(boxes) > 0:
				sr = self.get_subsample_rate()
				global BinaryCircleImageCache
				mask = BinaryCircleImageCache.get_image_directly(int(self.box_size/(2*sr)))
				for box in self.box_list.get_boxes():
					x,y = int(box.x/sr),int(box.y/sr)
					print "excluding box: x:%d  y:%d" % (x, y)
					# from EMAN2 import BoxingTools
					BoxingTools.set_region(image,mask,x,y,0.1) # 0.1 is also the value set by the eraser - all that matters is that it's zon_zero
			
			return image
			
	# code from emboxerbase.EMBoxerModule 
	def detect_box_collision(self,data):
		print "target detect_box_collision() " + str(data)
		return self.box_list.detect_collision(data[0], data[1], self.box_size)

		
	def addRectangleRoi(self, x, y, roiText=None):
		"""
		Adds a Rectangle (particle) to the current OMERO image, at point x, y. 
		Uses the self.image (OMERO image) and self.updateService
		"""
		width = self.box_size
		height = self.box_size

		# create an ROI, add the rectangle and save
		roi = omero.model.RoiI()
		roi.setImage(self.image)
		r = self.updateService.saveAndReturnObject(roi)

		# create and save a rectangle shape
		rect = omero.model.RectI()
		rect.x = rdouble(x)
		rect.y = rdouble(y)
		rect.width = rdouble(width)
		rect.height = rdouble(height)
		rect.theZ = rint(0)
		rect.theT = rint(0)
		if roiText:
			rect.textValue = rstring(roiText)

		# link the rectangle to the ROI and save it 
		rect.setRoi(r)
		r.addShape(rect)	
		self.updateService.saveAndReturnObject(rect)


def getRectangles(session, imageId):
	""" Returns (x, y, width, height) of each rectange ROI in the image """
	
	rectangles = []
	shapes = []		# string set. 
	
	roiService = session.getRoiService()
	result = roiService.findByImage(imageId, None)
	
	rectCount = 0
	for roi in result.rois:
		for shape in roi.copyShapes():
			if type(shape) == omero.model.RectI:
				x = shape.getX().getValue()
				y = shape.getY().getValue()
				width = shape.getWidth().getValue()
				height = shape.getHeight().getValue()
				rectangles.append((int(x), int(y), int(width), int(height)))
				continue
	return rectangles


def downloadImage(session, imageId, imageName):
	"""
	This method downloads the first (only?) plane of the OMERO image and saves it as a local image.
	
	@param session		The OMERO session
	@param imageId		The ID of the image to download
	@param imageName	The name of the image to write. If no path, saved in the current directory. 
	"""
	queryService = session.getQueryService()
	rawPixelStore = session.createRawPixelsStore()

	# get pixels with pixelsType
	query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
	pixels = queryService.findByQuery(query_string, None)
	theX = pixels.getSizeX().getValue()
	theY = pixels.getSizeY().getValue()

	# get the plane
	theZ, theC, theT = (0,0,0)
	pixelsId = pixels.getId().getValue()
	bypassOriginalFile = True
	rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
	plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
	
	plane2D.resize((theY, theX))		# not sure why we have to resize (y, x)
	p = Image.fromarray(plane2D)
	#p.show()
	p.save(imageName)
	
	return (theX, theY)

if __name__ == "__main__":
	
	# start by logging in to server
	client = omero.client("localhost")
	session = client.createSession("root", "omero")
	
	imageId = 1
	
	# download the image as a local temp image
	#image_name = "tempImage.dat"
	image_name = "newTestImage.tiff"
	imgW, imgH = downloadImage(session, imageId, image_name)
	
	#showImage(image_name)
	print "image downloaded"
	
	
	# get list of ROI boxes as (x, y, width, height) on the image
	boxes = getRectangles(session, imageId)
	if len(boxes) == 0:
		print "No ROIs found - exiting!"
		import sys
		sys.exit()
	
	# use the width of the first box as the box_size (all should be same w,h)
	x,y,w,h = boxes[0]
	box_size = w
	
	# create a 'target' which will save the generated boxes as ROI rectangles to OMERO
	target = Target(box_size, session, imageId)
	omeroBoxer = OmeroSwarmTool(target, particle_diameter=box_size)		# pass target to Boxer
	
	# add the reference boxes to the boxer
	for box in boxes:
		x, y, w, h = box
		x += box_size/2		# convert from top-left of ROI (OMERO) to centre of particle (EMAN2) 
		y += box_size/2
		y = imgH - y		# convert distance from Top of image (OMERO) to distance from bottom (EMAN2) 
		omeroBoxer.add_ref(x,y,image_name)
	
	# perform auto-boxing - results are written back to server, as ROIs on the image. 
	omeroBoxer.auto_box(image_name)
	
	