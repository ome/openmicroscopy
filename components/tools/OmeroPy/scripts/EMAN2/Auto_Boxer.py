"""
 components/tools/OmeroPy/scripts/EMAN2/Auto_Boxer.py 

-----------------------------------------------------------------------------
  Copyright (C) 2006-2010 University of Dundee. All rights reserved.


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

This script is a "proof of principle" to demonstrate how EMAN2 can work with OMERO. 
This uses the auto-box functionality of EMAN2, which takes one or more user-defined particle ROIs, 
and uses this as the basis for picking additional particles from the image. 
In this script, OMERO is used as the source of the image, with the user-defined particles as ROIs on
the server. The image is saved 'locally' as a Tiff. The EMAN2 subclasses 
use this image, with the server ROI(s) to generate additional ROIs which are then saved back to 
the server.
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

from EMAN2 import *

from e2boxer import *

import numpy

import omero
import omero.scripts as scripts
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

# for saving as tiff.
try:
    from PIL import Image # see ticket:2597
except ImportError:
    import Image # see ticket:2597

# from http://blake.bcm.edu/emanwiki/EMAN2/BoxSize
goodBoxSizes = [32, 33, 36, 40, 42, 44, 48, 50, 52, 54, 56, 60, 64, 66, 70, 72, 81, 84, 96, 98, 100, 104, 105, 112, 120, 128,
130, 132, 140, 150, 154, 168, 180, 182, 192, 196, 208, 210, 220, 224, 240, 250, 256,
260, 288, 300, 330, 352, 360, 384, 416, 440, 448, 450, 480, 512]

# keep track of log strings. 
logStrings = []

def log(text):
    """
    Adds the text to a list of logs. Compiled into figure legend at the end.
    """
    #print text
    logStrings.append(text)

# Override this method in EMAN2db becuase the $HOME variable was not available to the scripting service (is now). 
def e2gethome() :
    """platform independent path with '/'"""
    if(sys.platform != 'win32'):
        url=os.getenv("HOME")
    else:
        url=os.getenv("HOMEPATH")
        url=url.replace("\\","/")
    if url is None:        # added these 5 lines...
        import pwd
        import os
        uid=os.geteuid()
        url=pwd.getpwuid(uid)[5]  # Home directory
    return url
#EMAN2db.e2gethome = e2gethome

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
        log("Dummy PanelObject: set_picking_data()")
        log("    peak_score: %s" % peak_score)
        log("    profile: %s" % profile)
        log("    profile_trough_point: %s" % profile_trough_point)


class OmeroSwarmTool(SwarmBoxer):
    '''
    Subclass the main Boxer class, to add data source, UI elements etc. 
    '''    
    
    def __init__(self,target,particle_diameter=128):
        # import sys
        # print >> sys.stderr, target.__class__
        SwarmBoxer.__init__(self,particle_diameter)
        self.target = weakref.ref(target)    # now, the target() method will return target
        window = DummyWindow()
        def getWindow():
            return window
        self.get_2d_window = getWindow
        #self.panel_object = SwarmPanel(self,self.particle_diameter)
        self.panel_object = OmeroSwarmPanel(self.particle_diameter)    # needs to implement set_picking_data(self.peak_score, self.profile, self.profile_trough_point)
        self.gui_mode = False


class Target():
    """
    Dummy target to get / save boxes to omero etc. 
    Replaces the functionality of emboxerbase.EMBoxerModule which is used in the workflow UI. 
    Takes a reference to an OMERO session in the constructor, which is then used to write the picked particles as 
    ROIs to the image, identified by imageId
    """
    def __init__(self, box_size, session, imageId, image_name):
        def getFileName():
            print "Target: getFileName()"
            return image_name
            #return "/Users/will/Documents/dev/EMAN2/06jul12a.mrc"
        self.current_file = getFileName
        #self.current_file = "/Users/will/Documents/dev/EMAN2/06jul12a.mrc"
        self.box_size = box_size
        self.box_list = EMBoxList(self)        # has methods like detect_collision()
        self.session = session
        self.imageId = imageId
        
        # create the service and image for adding ROIs later...
        self.updateService = self.session.getUpdateService()
        self.containerService = self.session.getContainerService()
        self.image = self.containerService.getImages("Image", [self.imageId], None)[0]
        self.imageY = self.image.getPrimaryPixels().getSizeY().getValue()
    
    # code from emboxerbase.EMBoxerModule
    def add_box(self, x, y, type):
        """
        add a box to the list
        If type = SwarmBoxer.REF_NAME then this is a reference box. 
        """
        #print "add_box() x: %d, y: %d" % (x, y)
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
        #    self.full_box_update()
    
    
    # code from emboxerbase.EMBoxerModule
    def add_boxes(self,boxes,update_gl=True):
        '''
        boxes should be a list like [[x,y,type],[x,y,type],....[int,int,string]]
        '''
        for b in boxes:
            print b
            x,y,typeString,v = b
            x -= self.box_size/2    # convert from centre of particle, to top-left of ROI
            y = self.imageY - y        # convert from bottom to top Y coordinates. 
            y -= self.box_size/2
            self.addRectangleRoi(x, y, typeString)
        # removed a lot of UI code from emboxerbase.EMBoxerModule
        self.box_list.add_boxes(boxes)
        
        
    # code from emboxerbase.EMBoxerModule
    def get_subsample_rate(self): 
        '''
        Image seems to be shrunk by this factor in order to make each particle approx 30 pixels square. 
        '''
        return int(math.ceil(float(self.box_size)/float(TEMPLATE_MIN)))
                
    # code from emboxerbase.EMBoxerModule
    def get_exclusion_image(self,mark_boxes=False):
        '''
        @mark_boxes if true the exclusion image is copied and the locations of the current boxes are painted in as excluded regions
        This is useful for autoboxers - they  obviously dont want to box any region that already has a box in it (such as a manual box,
        or a previously autoboxed box)
        '''
        exc_image = ScaledExclusionImageCache.get_image(self.current_file(), self.get_subsample_rate())     # class 'libpyEMData2.EMData'
        print "Target get_exclusion_image() mark_boxes: " + str(mark_boxes)    
        
        #display(exc_image)        # blank image
        
        if not mark_boxes: return exc_image
        
        else:
            #print "    Ignoring request to mark_boxes: returning unmasked image..."
            #return exc_image     # hack to avoid fixing the code below. Manually added boxes will not be excluded from being auto-boxed. 
            
            image = exc_image.copy()
            boxes = self.box_list.get_boxes()
            if len(boxes) > 0:
                sr = self.get_subsample_rate()
                global BinaryCircleImageCache
                mask = BinaryCircleImageCache.get_image_directly(int(self.box_size/(2*sr)))
                for box in self.box_list.get_boxes():
                    x,y = int(box.x/sr),int(box.y/sr)
                    log("excluding box: x:%d  y:%d" % (x, y))
                    # from EMAN2 import BoxingTools
                    BoxingTools.set_region(image,mask,x,y,0.1) # 0.1 is also the value set by the eraser - all that matters is that it's zon_zero
            
            return image
            
    # code from emboxerbase.EMBoxerModule 
    def detect_box_collision(self,data):
        log("target detect_box_collision() %s" % data)
        return self.box_list.detect_collision(data[0], data[1], self.box_size)

        
    def addRectangleRoi(self, x, y, roiText=None, colourString=None):
        """
        Adds a Rectangle (particle) to the current OMERO image, at point x, y. 
        Uses the self.image (OMERO image) and self.updateService
        """
        width = self.box_size
        height = self.box_size

        # create an ROI, add the rectangle and save
        roi = omero.model.RoiI()
        roi.setImage(self.image)
        if roiText:
            roi.description = rstring(roiText)        # use as a flag to identify ROI (e.g. to delete)
        r = self.updateService.saveAndReturnObject(roi)
        

        # create and save a rectangle shape
        rect = omero.model.RectI()
        rect.x = rdouble(x)
        rect.y = rdouble(y)
        rect.width = rdouble(width)
        rect.height = rdouble(height)
        rect.theZ = rint(0)
        rect.theT = rint(0)
        rect.locked = rbool(True)        # don't allow editing 
        rect.strokeWidth = rint(6)
        if roiText:
            rect.textValue = rstring(roiText)    # for display only
        if colourString:
            rect.strokeColor = rstring(colourString)

        # link the rectangle to the ROI and save it 
        rect.setRoi(r)
        r.addShape(rect)    
        self.updateService.saveAndReturnObject(rect)

def pickBoxSize(width, height):
    boxSize = (width + height)/2
    if boxSize not in goodBoxSizes:
        for size in goodBoxSizes:
            if boxSize < size: 
                boxSize = size
                break
    if boxSize > goodBoxSizes[-1]:
        boxSize = goodBoxSizes[-1]
    return boxSize

def getRectangles(roiService, updateService, imageId, boxSize=None, deleteType=None):
    """ Returns (x, y, width, height) of each rectange ROI in the image 
    
    @param deleteType     Delete ROIs that have this string as a text/description. 
    @param boxSize        If not None, return boxes of this size and update existing ROI rectangles
    """
    
    rectangles = []
    shapes = []        # string set. 
    
    result = roiService.findByImage(imageId, None)
    
    rectCount = 0
    for roi in result.rois:
        if roi.description and (roi.description.val == deleteType):
            for shape in roi.copyShapes():
                updateService.deleteObject(shape)
            updateService.deleteObject(roi)
            continue    # don't add this ROI to our list 
        for shape in roi.copyShapes():
            if type(shape) == omero.model.RectI:
                x = shape.getX().getValue()
                y = shape.getY().getValue()
                width = int(shape.getWidth().getValue())
                height = int(shape.getHeight().getValue())
                
                # box size for all other boxes is fixed by first box we process
                if boxSize == None:     
                    boxSize = pickBoxSize(width, height)
                    print "Picked box size: ", boxSize
                
                if width != boxSize or height != boxSize:
                    # need to update existing shape, keeping centre in same place
                    x = int(x + (width / 2) - (boxSize / 2))
                    y = int(y + (height / 2) - (boxSize / 2))
                    shape.setX(rdouble(x))
                    shape.setY(rdouble(y))
                    shape.setWidth(rdouble(boxSize))
                    shape.setHeight(rdouble(boxSize))
                    updateService.saveObject(shape)
                rectangles.append((int(x), int(y), int(boxSize), int(boxSize)))
                continue
                
    return rectangles


def downloadImage(session, imageId, imageName):
    """
    This method downloads the first (only?) plane of the OMERO image and saves it as a local image.
    
    @param session        The OMERO session
    @param imageId        The ID of the image to download
    @param imageName    The name of the image to write. If no path, saved in the current directory. 
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
    
    p = Image.fromarray(plane2D)
    #p.show()
    p.save(imageName)
    
    return (theX, theY)


def doAutoBoxing(session, parameterMap):
    
    imageIds = []
    boxSize = None
    
    if "Image_IDs" in parameterMap:
        for idCount, imageId in enumerate(parameterMap["Image_IDs"]):
            iId = long(imageId.getValue())
            imageIds.append(iId)
            
    if "Box_Size" in parameterMap:
        boxSize = parameterMap["Box_Size"]
        print "Using user-specified box_size: ", boxSize
    
    roiService = session.getRoiService()
    updateService = session.getUpdateService()
    queryService = session.getQueryService()
    
    for imageId in imageIds:
        
        # download the image as a local temp tiff image
        image_name = queryService.get("Image", imageId).getName().getValue()
        if not image_name.endswith(".tiff"):
            image_name = "%s.tiff" % image_name
        imgW, imgH = downloadImage(session, imageId, image_name)
    
        #showImage(image_name)
        log("image downloaded: %s" % image_name)
    
        # get list of ROI boxes as (x, y, width, height) on the image
        # if boxSize isn't None, boxes will be set to this size.
        # and delete any existing AUTO added ROIs. 
        boxes = getRectangles(roiService, updateService, imageId, boxSize, SwarmBoxer.AUTO_NAME)
        if len(boxes) == 0:
            log("No ROIs found in image: %s" % image_name)
            continue
    
        # use the width of the first box as the box_size (all should be same w,h)
        x,y,w,h = boxes[0]
        box_size = w
    
        # create a 'target' which will save the generated boxes as ROI rectangles to OMERO
        target = Target(box_size, session, imageId, image_name)
        omeroBoxer = OmeroSwarmTool(target, particle_diameter=box_size)        # pass target to Boxer
    
        # add the reference boxes to the boxer
        for box in boxes:
            x, y, w, h = box
            x += box_size/2        # convert from top-left of ROI (OMERO) to centre of particle (EMAN2) 
            y += box_size/2
            y = imgH - y        # convert distance from Top of image (OMERO) to distance from bottom (EMAN2) 
            omeroBoxer.add_ref(x,y,image_name)
    
        # perform auto-boxing - results are written back to server, as ROIs on the image. 
        omeroBoxer.auto_box(image_name)
        
        figLegend = "\n".join(logStrings)
        print figLegend
    return len(imageIds)
    
def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    client = scripts.client('Auto_Boxer.py', """Use EMAN2 to auto-box particles based on 1 or more user-picked particles (ROIs).
See http://trac.openmicroscopy.org.uk/omero/wiki/EmPreviewFunctionality""", 
    scripts.List("Image_IDs", optional=False, description="List of image IDs you want to auto-box.").ofType(rlong(0)),                  
    scripts.Long("Box_Size", description="Size of particle box. If not specified, determined from user ROIs"))
    
    session = client.getSession()
    
    try:
        # process the list of args above. 
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = client.getInput(key).getValue()

        imgCount = doAutoBoxing(session, parameterMap)
        image = imgCount==1 and "image" or "images"
        client.setOutput("Message", rstring("Auto-Boxing added boxes to %s %s" % (imgCount, image)))
    finally:
        client.closeSession()


if __name__ == "__main__":
    runAsScript()
    
    