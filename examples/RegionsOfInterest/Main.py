import omero
import omero.clients
from omero.rtypes import *

roi = omero.model.ROII()
ellipse = omero.model.EllipseI()
ellipse.setCx(rdouble(1))
