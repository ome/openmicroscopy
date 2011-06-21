
import omero
import xml.sax
from omero.gateway import XmlAnnotationWrapper

def getSpimData (conn, image):
    """
    Returns a map of SPIM data according to the specification at http://www.ome-xml.org/wiki/SPIM/InitialSupport
    where extra Objectives are stored in the Instrument, multiple Images are linked to the same 'spim-set' annotation, one
    Image for each SPIM angle. 
    Extra Objective attributes, SPIM angles and Stage Positions stored in XML annotations with 3 different namespaces.  
    """
    
    instrument = image.getInstrument()
    
    obs = []
    for o in instrument.getObjectives():
        ob = []
        ob.append( ('Model', o.model ) )
        ob.append( ('Manufacturer', o.manufacturer ) )
        ob.append( ('Serial Number', o.serialNumber ) )
        ob.append( ('Nominal Magnification', o.nominalMagnification ) )
        obs.append(ob)
       
    images = [] 
    objExtras = []
    spimAngles = []
    stagePositions = {}     # iid: []
    
    def getLinkedImages(annId):
        query = "select i from Image as i join i.annotationLinks as a_link join a_link.child as a where a.id='%s'" % annId
        imgs = conn.getQueryService().findAllByQuery(query, None)
        return [omero.gateway.ImageWrapper(conn, i) for i in imgs]
    
    # get the Objective attributes and Spim-set annotations (spim-set also linked to other images)
    for ann in image.listAnnotations():
        if isinstance(ann, XmlAnnotationWrapper):
            xmlText = ann.textValue
            if ann.ns == "ome-xml.org:additions:post2010-06:objective":
                elementNames = ['ObjectiveAdditions']
            elif ann.ns == "ome-xml.org:additions:post2010-06:spim:set":
                elementNames = ['SpimImage']
                # also get the other images annotated with this 
                images = getLinkedImages(ann.id)
            else:
                continue

            handler = AnnXmlHandler(elementNames)
            xml.sax.parseString(xmlText, handler)
            
            if ann.ns == "ome-xml.org:additions:post2010-06:objective":
                objExtras.extend(handler.attributes)
            elif ann.ns == "ome-xml.org:additions:post2010-06:spim:set":
                spimAngles.extend(handler.attributes)
    
    # for All images, get the spim-position data. 
    for i in images:
        spos = []
        for ann in i.listAnnotations():
            if isinstance(ann, XmlAnnotationWrapper) and ann.ns == "ome-xml.org:additions:post2010-06:spim:positions":
                xmlText = ann.textValue
                handler = AnnXmlHandler(['StagePosition'])
                xml.sax.parseString(xmlText, handler)
                spos.extend(handler.attributes)
        
        stagePositions[i.id] = spos
        
    #print "Images"
    #print images
    #print "Object Extras"
    #print objExtras
    #print "Stage Positions"
    #print stagePositions
    #print "Spim Angles"
    #print spimAngles
    
    if len(objExtras) == 0 and len(stagePositions) == 0 and len(spimAngles) == 0:
        return None
        
    return {'images':images, 'obs': obs, 'objExtras':objExtras, 'stagePositions':stagePositions, 'spimAngles': spimAngles}



class AnnXmlHandler(xml.sax.handler.ContentHandler):
    """ Parse XML to get Objective attributes """
    def __init__(self, elementNames):
        self.inElement = False
        self.elementNames = elementNames
        self.attributes = []
 
    def startElement(self, name, attributes):
        if name in self.elementNames:
            kv = {}
            for k, v in attributes.items():
                kv[str(k)] = str(v) 
            self.attributes.append(kv)
            self.inElement = True
            self.buffer = ""
 
    def characters(self, data):
        if self.inElement:
            self.buffer += data
 
    # if we're ending an element that we're interested in, save the text in map
    def endElement(self, name):
        self.inElement = False