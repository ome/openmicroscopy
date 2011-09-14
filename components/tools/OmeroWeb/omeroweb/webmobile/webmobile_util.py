
from datetime import datetime, timedelta
from django.core.urlresolvers import reverse

import omero
from omero.rtypes import rlong, rint, rtime
from omero.gateway import ImageWrapper, ProjectWrapper, AnnotationWrapper, BlitzObjectWrapper, DatasetWrapper
from webclient.webclient_gateway import OmeroWebGateway


        
def listMostRecentObjects(conn, limit, obj_types=None, eid=None):
    """
    Get the most recent objects supported by the timeline service: obj_types are 
    ['Project', 'Dataset', 'Image', 'Annotation'), Specifying the 
    number of each you want 'limit', belonging to the specified experimenter 'eid'
    or current Group if 'eid' is None.
    """
    
    from datetime import datetime
    now = datetime.now()
    
    tm = conn.getTimelineService()
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    if eid:
        f.ownerId = rlong(eid)
    else:
        f.groupId = rlong(conn.getEventContext().groupId)
    f.limit = rint(limit)
    p.theFilter = f

    types = {'Image':ImageWrapper, 'Project':ProjectWrapper, 'Dataset':DatasetWrapper}
    recent = tm.getMostRecentObjects(None, p, False)

    recentItems = []
    for r in recent: # 'Image', 'Project', 'Dataset', 'Annotation', 'RenderingSettings'
        if obj_types and r not in obj_types:
            continue
        for value in recent[r]:
            if r == 'Annotation':
                recentItems.append(AnnotationWrapper._wrap(conn, value))
            elif r in types:
                recentItems.append( types[r](conn, value) )
            else:
                pass    # RenderingSettings
                # recentItems.append( BlitzObjectWrapper(conn, value) )  
    
    if obj_types == None or 'Annotation' in obj_types:
        anns = listMostRecentAnnotations(conn, limit, eid)
        recentItems.extend(anns)
        
    recentItems.sort(key = lambda x: x.updateEventDate())
    recentItems.reverse()
    return recentItems
    

def listMostRecentAnnotations (conn, limit, eid=None):
    """
    Retrieve most recent annotations available
    
    @return:    Generator yielding BlitzObjectWrapper
    @rtype:     L{BlitzObjectWrapper} generator
    """
    
    tm = conn.getTimelineService()
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    if eid:
        f.ownerId = rlong(eid)
    else:
        f.groupId = rlong(conn.getEventContext().groupId)
    f.limit = rint(limit)
    p.theFilter = f
    anns = []
    
    # get ALL parent-types, child-types, namespaces:
    annTypes = ['LongAnnotation', 'TagAnnotation', 'CommentAnnotation'] # etc. Only query 1 at at time! 
    for at in annTypes:
        for a in tm.getMostRecentAnnotationLinks(None, [at], None, p):
            # TODO: maybe load parent for each link here
            wrapper = AnnotationWrapper._wrap(conn, a.child, a)
            anns.append(wrapper)
    return anns


def formatTimeAgo(eventDate):
    """
    Formats a datetime wrt 'now'. 
    E.g. '3 hours 25 mins ago', 'Yesterday at 18:30', 'Friday 14 Jan at 10.44'
    """
    
    now = datetime.now()
    year = now.year
    month = now.month
    day = now.day
    one_day = timedelta(1)
    yesterday_start = datetime(year, month, day) - timedelta(1)
    ago = now - eventDate
    
    if ago < one_day:
        hh, rem = divmod(ago.seconds, 3600)
        mm, rem = divmod(rem, 60)
        if hh == 0: 
            return "%s mins ago" % mm
        return "%s hours %s mins ago" % (hh, mm)
    elif eventDate > yesterday_start:
        return "Yesterday at %s" % eventDate.strftime("%H:%M")
    else:
        return "%s" % eventDate.strftime("%A, %d %b at %H:%M")
        
        
class RecentEvent(object):
    
    def __init__(self, blitzWrapper, roi=None):
        
        self.obj = blitzWrapper
        self.display_type = None
        self.url = None
        self.parent_type = None     # for annotations
        self.parent_id = None
        self.parent_name = None     # only set if parent is loaded
        self.link_created = None
        self.link_owner = None
        self.roi_owner = None       # only set if ROI is set
        
        # Pick a suitable display name for the object. E.g. 'Image', 'Comment' etc. 
        annTypes = {omero.gateway.CommentAnnotationWrapper:'Comment', 
                omero.gateway.FileAnnotationWrapper:'File', 
                omero.gateway.TimestampAnnotationWrapper:'Timestamp',
                omero.gateway.BooleanAnnotationWrapper:'Boolean',
                omero.gateway.TagAnnotationWrapper:'Tag',
                omero.gateway.LongAnnotationWrapper:'Long',
                omero.gateway.DoubleAnnotationWrapper:'Double'}
        modelTypes = {omero.model.ImageI:'Image', 
                    omero.model.DatasetI:'Dataset',
                    omero.model.ProjectI:'Project'}
        # Annotations..
        if self.obj.__class__ in annTypes:
            self.display_type = annTypes[self.obj.__class__]
            try:
                self.parent_id = blitzWrapper.link.parent.id.val
                self.parent_type = modelTypes[blitzWrapper.link.parent.__class__]
                self.parent_name = blitzWrapper.link.parent.name.val
                cEvt = blitzWrapper.link.details.creationEvent._time.val
                self.link_created = formatTimeAgo(datetime.fromtimestamp(cEvt/1000))
                self.link_owner = "%s %s" % (blitzWrapper.link.details.owner.firstName.val, blitzWrapper.link.details.owner.lastName.val)
            except: 
                pass
                
            # Handle special case of Long annotations being 'Ratings'
            if self.display_type == 'Long' and self.obj.ns:
                if self.obj.ns == "openmicroscopy.org/omero/insight/rating":
                    self.display_type = 'Rating'
        else:
            self.display_type = blitzWrapper.OMERO_CLASS
            
        # Create a suitable link, either to Object itself, or to the Parent (for comments, rating)
        def getLinkToObject(modelObject):
            if modelObject.__class__ == omero.model.ImageI:
                return reverse('webmobile_image', kwargs={'imageId':modelObject.id.val}) 
            if modelObject.__class__ == omero.model.DatasetI:
                return reverse('webmobile_dataset_details', kwargs={'id':modelObject.id.val})
            if modelObject.__class__ == omero.model.ProjectI:
                return reverse('webmobile_project_details', kwargs={'id':modelObject.id.val})
        
        if self.display_type in ['Image', 'Project', 'Dataset']:
            self.url = getLinkToObject(blitzWrapper._obj)
        elif self.display_type in ['Rating', 'Comment', 'Tag']:
            if blitzWrapper.link:
                self.url = getLinkToObject(blitzWrapper.link.parent)
        
        # for Images where we also have an ROI, set the creation date of the ROI.
        if roi:
            cEvt = roi.details.creationEvent._time.val
            self.timeAgo = formatTimeAgo(datetime.fromtimestamp(cEvt/1000))
            self.roi_owner = "%s %s" % (roi.details.owner.firstName.val, roi.details.owner.lastName.val)
        else:
            self.timeAgo = formatTimeAgo(blitzWrapper.updateEventDate())


def listCollabAnnotations(conn, myData=True, limit=10):
    """
    Lists the most recent annotations BY other users on YOUR images etc. 
    """
    
    eid = conn.getEventContext().userId
    queryService = conn.getQueryService()
    params = omero.sys.ParametersI()
    params.addLong("eid", eid);
    f = omero.sys.Filter()
    f.limit = rint(limit)
    params.theFilter = f
    
    if myData:  # want annotation links NOT owned, where parent IS owned
        selection = "where owner.id = :eid and linkOwner.id != :eid "
    else:   # want annotation links owned by me, on data NOT owned by me
        selection = "where owner.id != :eid and linkOwner.id = :eid "
        
    query = "select al from ImageAnnotationLink as al " \
                    "join fetch al.child as annot " \
                    "join fetch al.parent as parent " \
                    "join fetch parent.details.owner as owner " \
                    "join fetch al.details.owner as linkOwner " \
                    "join fetch al.details.creationEvent as event %s" \
                    "order by event desc" % selection
    imageLinks = queryService.findAllByQuery(query, params)
                        
    return [RecentEvent (AnnotationWrapper._wrap(conn, a.child, a) ) for a in imageLinks]
    

def listRois(conn, eid=None, limit=10):
    """
    List the most recently created ROIs, optionally filtering by owner. 
    Returns a list of RecentObjects wrapping the linked Images. 
    """
    
    queryService = conn.getQueryService()
    params = omero.sys.ParametersI()
    f = omero.sys.Filter()
    f.limit = rint(limit)
    params.theFilter = f
    
    if eid is not None:
        params.map["eid"] = rlong(long(eid))
        eidSelect = "where roi.details.owner.id=:eid "
    else: eidSelect = ""
    
    query = "select roi from Roi as roi " \
                    "join fetch roi.image as image " \
                    "join fetch roi.details.owner " \
                    "join fetch roi.details.creationEvent as event %s" \
                    "order by event desc" % eidSelect
        
    rois = queryService.findAllByQuery(query, params)
    return [RecentEvent (ImageWrapper(conn, r.image), r) for r in rois]