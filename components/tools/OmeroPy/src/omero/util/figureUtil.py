"""
 components/tools/OmeroPy/src/omero/util/figureUitl.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2009 University of Dundee. All rights reserved.


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

A collection of utility methods used by Figure scripts for producing 
publication type of figures. 

@author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.1
 
"""

def getDatasetsProjectsFromImages(queryService, imageIds):
	""" 
	Query returns a map where each key is the imageId and the value is a list of (projectName, datasetName) tuples. 
	
	@param queryService: 	The Omero query service
	@param imageIds:		A list of image IDs. [long]
	@return:			A map imageId:[(projectName, datasetName)]
	"""
	ids = ",".join([str(i) for i in imageIds])
	
	query_string = "select i from Image i join fetch i.datasetLinks idl join fetch idl.parent d join fetch d.projectLinks pl join fetch pl.parent where i.id in (%s)" % ids

	images = queryService.findAllByQuery(query_string, None)
	results = {}
	
	for i in images:	# order of images not same as imageIds
		pdList = []
		imageId = i.getId().getValue()
		for link in i.iterateDatasetLinks():
			dataset = link.parent
			dName = dataset.getName().getValue()
			for dpLink in dataset.iterateProjectLinks():
				project = dpLink.parent
				pName = project.getName().getValue()
				pdList.append((pName, dName))
		results[imageId] = pdList
	return results
	

def getTagsFromImages(metadataService, imageIds):
	""" 
	Query returns a map of key = imageId, value = [tagNames] for the image
	
	@param metadataService:		The Omero metadata service
	@param imageIds:			A list of image IDs. [long]
	@return:				A map of imageId:[tagName]
	"""
	
	types = ["ome.model.annotations.TagAnnotation"]
	annotations = metadataService.loadAnnotations("Image", imageIds, types, None, None)
    
	tagsMap = {}
	for i in imageIds:
		annots = annotations[i]
		tags = [a.getTextValue().getValue() for a in annots]
		tagsMap[i] = tags
	return tagsMap
	
	
def getTimes(queryService, pixelsId, tIndexes):
	"""
	Get the time in seconds (float) for the first plane (C = 0 & Z = 0) at 
	each time-point for the defined pixels. 
	
	@param queryService:	The Omero queryService
	@param pixelsId:		The ID of the pixels object. long
	@param tIndexes:		List of time indexes. [int]
	@return:			List of times in seconds. 
	"""
	indexes = ",".join([str(t) for t in tIndexes])
	query = "from PlaneInfo as Info where Info.theT in (%s) and Info.theZ in (0) and Info.theC in (0) and pixels.id='%d' order by Info.deltaT" % (indexes, pixelsId)
	infoList = queryService.findAllByQuery(query,None)
	if len(infoList) >0:
		return [info.deltaT.getValue() for info in infoList]
		
	
def formatTime(seconds, timeUnits):
	"""
	Returns a string formatting of the time (in seconds)
	according to the chosen timeUnits: "SECS", "MINS", "HOURS", "MINS_SECS", "HOURS_MINS"
	
	@param seconds:		Time in seconds. float or int
	@param timeUnits:	A string denoting the format. One of the choices above. 
	@return:		A string, such as "10 secs" or "3:20 hrs:mins"	
	"""
	if timeUnits == "SECS":
		return "%d sec" % int(round(seconds))
	elif timeUnits == "MINS":
		mins = float(seconds) / float(60)
		return "%d min" % int(round(mins))
	elif timeUnits == "HOURS":
		hrs = float(seconds) / float(3600)
		return "%d hour" % int(round(hrs))
	elif timeUnits == "MINS_SECS":
		mins = seconds / 60
		secs = round(seconds % 60)
		return "%d:%02d mins:secs" % (mins, secs)
	elif timeUnits == "HOURS_MINS":
		hrs = seconds / 3600
		mins = round((seconds % 3600)/60)
		return "%d:%02d hrs:mins" % (hrs, mins)
	
	
def getTimeLabels(queryService, pixelsId, tIndexes, sizeT, timeUnits):
	"""
	Returns a list of time labels e.g. "10 min", "20 min" for the first plane at 
	each t-index (C=0 and Z=0). If no planeInfo is available, returns plane number/total e.g "3/10"
	
	@param queryService:		The Omero query service
	@param pixelsId:			The ID of the pixels you want info for
	@param tIndexes:			List of t-index to get the times for
	@param sizeT:				The T dimension size of the pixels. Used if no plane info
	@param timeUnits:		Format choice of "SECS", "MINS", "HOURS", "MINS_SECS", "HOURS_MINS". String
	@return:				A list of strings, ordered by time. 
	"""
	seconds = getTimes(queryService, pixelsId, tIndexes)
	
	if seconds == None:
		return ["%d/%d" % (t+1, sizeT) for t in tIndexes]
	
	return [formatTime(s,timeUnits) for s in seconds]

