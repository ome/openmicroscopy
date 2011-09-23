from omero.gateway import BlitzGateway
from omero.rtypes import *
import os
user = 'will'
pw = 'ome'
host = 'localhost'
datasetId = 101
projectId = 51
conn = BlitzGateway(user, pw, host=host, port=4064)
conn.connect()

# Create a new Dataset

ds = omero.gateway.DatasetWrapper(conn, omero.model.DatasetI())
ds.setName("New Dataset")
ds.save()

# Link to Project

project = conn.getObject("Project", projectId)
link = omero.model.ProjectDatasetLinkI()
link.setParent(omero.model.ProjectI(project.getId(), False))
link.setChild(omero.model.DatasetI(ds.getId(), False))
conn.getUpdateService().saveObject(link) 

#How to create a file annotation and link to a Dataset

dataset = conn.getObject("Dataset", datasetId)
#Create a local file. E.g. could be result of some analysis
tempFileName = "fileToUpload.txt"
f = open(tempFileName, 'w')
try:
    f.write("Some text to write to local file")
finally:
    f.close()
# create the original file and file annotation (uploads the file etc.)
namespace = "omero.training.write_data"
print "\nCreating an OriginalFile and FileAnnotation"
fileAnn = conn.createFileAnnfromLocalFile(tempFileName, mimetype="text/plain", ns=namespace, desc=None)
print "Attaching FileAnnotation to Dataset: ", fileAnn.getId(), fileAnn.getFile(), fileAnn.getFile().getName()
dataset.linkAnnotation(fileAnn)     # link it to dataset.
os.remove(tempFileName)             # delete our local copy


#Load all the file annotations with a given namespace

nsToInclude = [namespace]
nsToExclude = []
metadataService = conn.getMetadataService()
annotations = metadataService.loadSpecifiedAnnotations('omero.model.FileAnnotation', nsToInclude, nsToExclude, None)
for ann in annotations:
    print ann.getId().getValue(), ann.file.name.val


# Get all the annotations on an object

print "\nAnnotations on Dataset:", dataset.getName()
for ann in dataset.listAnnotations():
    print ann


# Get first annotation with specified namespace and download file

ann = dataset.getAnnotation(namespace)
# download
downloadFileName = "download"
f = open(downloadFileName, 'w')
print "\nDownloading file to ", downloadFileName
try:
    for chunk in ann.getFileInChunks():
        print "Chunk", chunk
        f.write(chunk)
finally:
    f.close()
os.remove(downloadFileName)     # clean up