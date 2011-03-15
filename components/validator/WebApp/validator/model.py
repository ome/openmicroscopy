from turbogears.database import PackageHub
from sqlobject import *

hub = PackageHub('validator')
__connection__ = hub

# class YourDataClass(SQLObject):
#     pass

