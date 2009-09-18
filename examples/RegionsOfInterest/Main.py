import omero
import omero.model
from omero.rtypes import *

roi = omero.model.RoiI()
ellipse = omero.model.EllipseI()
ellipse.setCx(rdouble(1))
