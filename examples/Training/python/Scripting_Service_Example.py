from omero.gateway import BlitzGateway
import omero.scripts as scripts
from omero.rtypes import *
dataTypes = [rstring('Image')]      # this script only takes Images (not Datasets etc.)
client = scripts.client("Scripting_Service_Example.py", 
    """Example script to use as a template for getting started with the scripting service.""",
    scripts.String("Data_Type", optional=False, grouping="01",
        description="The data you want to work with.", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="02",
        description="List of Image IDs").ofType(rlong(0)),
)

conn = BlitzGateway(client_obj=client)

ids = unwrap(client.getInput("IDs"))
imageId = ids[0]





message = "Script ran with Image ID: %s" % imageId
client.setOutput("Message", rstring(message))
client.closeSession()
