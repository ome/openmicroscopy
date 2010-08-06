import omero.util.script_utils as scriptUtil
from numpy import arange

def createTestImage(session):    
    gateway = session.createGateway()
    renderingEngine = session.createRenderingEngine()
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    
    plane2D = arange(256).reshape(16,16)
    pType = plane2D.dtype.name
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    image = scriptUtil.createNewImage(pixelsService, rawPixelStore, renderingEngine, pixelsType, gateway, [plane2D], "imageName", "description", dataset=None)
    
    gateway.close()
    renderingEngine.close()
    rawPixelStore.close()
    
    return image.getId().getValue()