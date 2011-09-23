from omero.gateway import BlitzGateway
from omero.rtypes import *
host = 'localhost'
user = 'will'
password = 'ome'
imageId = 101
datasetId = 101
plateId = 1
conn = BlitzGateway(user, password, host=host, port=4064)
conn.connect()

# list all Projects available to me, and their Datasets and Images. 

for project in conn.listProjects():
    print project.getName()
    for dataset in project.listChildren():
        print "   ", dataset.getName()
        for image in dataset.listChildren():
            print "      -", image.getName()


# Retrieve the datasets owned by the user currently logged in.

datasets = conn.getObjects("Dataset")
print "\nList Datasets:"
for d in datasets:
    print d.getName(), d.getOwnerOmeName()


# Retrieve the images contained in a dataset.

dataset = conn.getObject("Dataset", datasetId)
print "\nImages in Dataset:", dataset.getName()
for i in dataset.listChildren():
    print i.getName(), i.getId()


# Retrieve an image by Image ID.

image = conn.getObject("Image", imageId)
print "\nImage:"
print image.getName(), image.getDescription()
# Retrieve information about an image.
print " X:", image.getSizeX()
print " Y:", image.getSizeY()
print " Z:", image.getSizeZ()
print " C:", image.getSizeC()
print " T:", image.getSizeT()
# render the first timepoint, mid Z section
z = image.getSizeZ() / 2
t = 0
renderedImage = image.renderImage(z, t)
#renderedImage.show()               # popup (use for debug only)
#renderedImage.save("test.jpg")     # save in the current folder


# Retrieve Screening data

screens = conn.getObjects("Screen")
print "\nScreens:"
for s in screens:
    print s.getName(), s.getId()


# Retrieve Wells within a Plate

plate = conn.getObject("Plate", plateId)
print "\nWells in Plate:", plate.getName()
for well in plate.listChildren():
    print "  Well: ", well.row, well.column