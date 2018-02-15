#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# webgateway/urls.py - django application url handling configuration
#
# Copyright (c) 2007, 2008, 2009 Glencoe Software, Inc. All rights reserved.
#
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

from django.conf.urls import url
from omeroweb.webgateway import views

webgateway = url(r'^$', views.index, name="webgateway")
"""
Returns a main prefix
"""

annotations = url(r'^annotations/(?P<objtype>[\w.]+)/(?P<objid>\d+)/$',
                  views.annotations,
                  name="webgateway_annotations")
"""
Retrieve annotations for object specified by object type and identifier,
optionally traversing object model graph.
"""

table_query = url(r'^table/(?P<fileid>\d+)/query/$',
                  views.table_query,
                  name="webgateway_table_query")
"""
Query a table specified by fileid
"""

object_table_query = url(
    r'^table/(?P<objtype>[\w.]+)/(?P<objid>\d+)/query/$',
    views.object_table_query,
    name="webgateway_object_table_query")
"""
Query bulk annotations table attached to an object specified by
object type and identifier, optionally traversing object model graph.
"""

render_image = url(
    r'^render_image/(?P<iid>[0-9]+)/(?:(?P<z>[0-9]+)/)?(?:(?P<t>[0-9]+)/)?$',
    views.render_image)
"""
Returns a jpeg of the OMERO image. See L{views.render_image}. Rendering
settings can be specified in the request parameters. See
L{views.getImgDetailsFromReq} for details.
Params in render_image/<iid>/<z>/<t>/ are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
"""

render_image_region = url(
    r'^render_image_region/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$',
    views.render_image_region)
"""
Returns a jpeg of the OMERO image, rendering only a region specified in query
string as region=x,y,width,height. E.g. region=0,512,256,256 See
L{views.render_image_region}.
Rendering settings can be specified in the request parameters.
Params in render_image/<iid>/<z>/<t>/ are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
"""

render_split_channel = url(
    r'^render_split_channel/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$',
    views.render_split_channel)
"""
Returns a jpeg of OMERO Image with channels split into different panes in a
grid. See L{views.render_split_channel}.
Rendering settings can be specified in the request parameters (as above).
Params in render_split_channel/<iid>/<z>/<t> are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
"""

render_row_plot = url(
    r'^render_row_plot/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/'
    '(?P<y>[0-9]+)/(?:(?P<w>[0-9]+)/)?$',
    views.render_row_plot)
"""
Returns a gif graph of pixel values for a row of an image plane. See
L{views.render_row_plot}.
Channels can be turned on/off using request. E.g. c=-1,2,-3,-4
Params in render_row_plot/<iid>/<z>/<t>/<y>/<w> are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
    - y:    Y position of pixel row
    - w:    Optional line width of plot
"""

render_col_plot = url(
    r'^render_col_plot/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)'
    '/(?P<x>[0-9]+)/(?:(?P<w>[0-9]+)/)?$',
    views.render_col_plot)
"""
Returns a gif graph of pixel values for a column of an image plane. See
L{views.render_col_plot}.
Channels can be turned on/off using request. E.g. c=-1,2,-3,-4
Params in render_col_plot/<iid>/<z>/<t>/<x>/<w> are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
    - x:    X position of pixel column
    - w:    Optional line width of plot
"""

render_thumbnail = url(
    r'^render_thumbnail/(?P<iid>[0-9]+)'
    '/(?:(?P<w>[0-9]+)/)?(?:(?P<h>[0-9]+)/)?$',
    views.render_thumbnail)
"""
Returns a thumbnail jpeg of the OMERO Image, optionally scaled to max-width
and max-height.
See L{views.render_thumbnail}. Uses current rendering settings.
Query string can be used to specify Z or T section. E.g. ?z=10.
Params in render_thumbnail/<iid>/<w>/<h> are:
    - iid:  Image ID
    - w:    Optional max width
    - h:    Optional max height
"""

render_roi_thumbnail = url(
    r'^render_roi_thumbnail/(?P<roiId>[0-9]+)/?$',
    views.render_roi_thumbnail)
"""
Returns a thumbnail jpeg of the OMERO ROI. See L{views.render_roi_thumbnail}.
Uses current rendering settings.
"""

render_shape_thumbnail = url(
    r'^render_shape_thumbnail/(?P<shapeId>[0-9]+)/?$',
    views.render_shape_thumbnail)
"""
Returns a thumbnail jpeg of the OMERO Shape. See
L{views.render_shape_thumbnail}. Uses current rendering settings.
"""

render_shape_mask = url(r'^render_shape_mask/(?P<shapeId>[0-9]+)/$',
                     views.render_shape_mask)
"""
Returns a mask for the specified shape
"""

render_birds_eye_view = url(
    r'^render_birds_eye_view/(?P<iid>[0-9]+)/(?:(?P<size>[0-9]+)/)?$',
    views.render_birds_eye_view)
"""
Returns a bird's eye view JPEG of the OMERO Image.
See L{views.render_birds_eye_view}. Uses current rendering settings.
Params in render_birds_eye_view/<iid>/ are:
    - iid:   Image ID
    - size:  Maximum size of the longest side of the resulting bird's eye
             view.
"""

render_ome_tiff = url(r'^render_ome_tiff/(?P<ctx>[^/]+)/(?P<cid>[0-9]+)/$',
                   views.render_ome_tiff)
"""
Generates an OME-TIFF of an Image (or zip for multiple OME-TIFFs) and returns
the file or redirects to a temp file location. See L{views.render_ome_tiff}
Params in render_ome_tiff/<ctx>/<cid> are:
    - ctx:      The container context. 'p' for Project, 'd' for Dataset or 'i'
Image.
    - cid:      ID of container.
"""

render_movie = url(
    r'^render_movie/(?P<iid>[0-9]+)/(?P<axis>[zt])/(?P<pos>[0-9]+)/$',
    views.render_movie)
"""
Generates a movie file from the image, spanning Z or T frames. See
L{views.render_movie}
Returns the file or redirects to temp file location.
Params in render_movie/<iid>/<axis>/<pos> are:
    - iid:      Image ID
    - axis:     'z' or 't' dimension that movie plays
    - pos:      The T index (for 'z' movie) or Z index (for 't' movie)
"""

# json methods...

listProjects_json = url(r'^proj/list/$', views.listProjects_json)
"""
json method: returning list of all projects available to current user. See
L{views.listProjects_json} .
List of E.g. {"description": "", "id": 651, "name": "spim"}
"""

projectDetail_json = url(r'^proj/(?P<pid>[0-9]+)/detail/$',
                      views.projectDetail_json)
"""
json method: returns details of specified Project. See
L{views.projectDetail_json}. Returns E.g
{"description": "", "type": "Project", "id": 651, "name": "spim"}
    - webgateway/proj/<pid>/detail params are:
    - pid:  Project ID
"""

listDatasets_json = url(r'^proj/(?P<pid>[0-9]+)/children/$',
                     views.listDatasets_json)
"""
json method: returns list of Datasets belonging to specified Project. See
L{views.listDatasets_json}. Returns E.g
list of {"child_count": 4, "description": "", "type": "Dataset", "id": 901,
         "name": "example"}
    - webgateway/proj/<pid>/children params are:
    - pid:  Project ID
"""

datasetDetail_json = url(r'^dataset/(?P<did>[0-9]+)/detail/$',
                      views.datasetDetail_json)
"""
json method: returns details of specified Dataset. See
L{views.datasetDetail_json}. Returns E.g
{"description": "", "type": "Dataset", "id": 901, "name": "example"}
    - webgateway/dataset/<did>/detail params are:
    - did:  Dataset ID
"""

webgateway_listimages_json = url(
    r'^dataset/(?P<did>[0-9]+)/children/$', views.listImages_json,
    name="webgateway_listimages_json")
"""
json method: returns list of Images belonging to specified Dataset. See
L{views.listImages_json}. Returns E.g list of
{"description": "", "author": "Will Moore", "date": 1291325060.0,
 "thumb_url": "/webgateway/render_thumbnail/4701/", "type": "Image",
 "id": 4701, "name": "spim.png"}
    - webgateway/dataset/<did>/children params are:
      - did:  Dataset ID
    - request variables:
      - thumbUrlPrefix: view key whose reverse url is to be used as prefix for
                        thumb_url instead of default
                        views.render_thumbnail
      - tiled: if set with anything other than an empty string will add
               information on whether each image is tiled on this server

"""

webgateway_listwellimages_json = url(
    r'^well/(?P<did>[0-9]+)/children/$',
    views.listWellImages_json,
    name="webgateway_listwellimages_json")
"""
json method: returns list of Images belonging to specified Well. See
L{views.listWellImages_json}. Returns E.g list of
{"description": "", "author": "Will Moore", "date": 1291325060.0,
 "thumb_url": "/webgateway/render_thumbnail/4701/", "type": "Image",
 "id": 4701, "name": "spim.png"}
    - webgateway/well/<did>/children params are:
    - did:  Well ID
"""

webgateway_plategrid_json = url(
    r'^plate/(?P<pid>[0-9]+)/(?:(?P<field>[0-9]+)/)?$',
    views.plateGrid_json, name="webgateway_plategrid_json")
"""
"""


webgateway_get_thumbnails_json = url(
    r'^get_thumbnails/(?:(?P<w>[0-9]+)/)?$',
    views.get_thumbnails_json)
"""
Returns a set of thumbnail base64 encoded of the OMERO Images,
optionally scaled to max-longest-side.
Image ids are specified in query string as list, e.g. id=1&id=2.
"""

webgateway_get_thumbnail_json = url(
    r'^get_thumbnail/(?P<iid>[0-9]+)'
    '/(?:(?P<w>[0-9]+)/)?(?:(?P<h>[0-9]+)/)?$',
    views.get_thumbnail_json)
"""
Returns a thumbnail base64 encoded of the OMERO Images,
optionally scaled to max-width and max-height.
See L{views.render_thumbnail}. Uses current rendering settings.
Query string can be used to specify Z or T section. E.g. ?z=10.
Params in render_thumbnail/<iid>/<w>/<h> are:
    - iid:  Image ID
    - w:    Optional max width
    - h:    Optional max height
"""

imageData_json = url(r'^imgData/(?P<iid>[0-9]+)/(?:(?P<key>[^/]+)/)?$',
                  views.imageData_json)
"""
json method: returns details of specified Image. See L{views.imageData_json}.
Returns E.g
{"description": "", "type": "Dataset", "id": 901, "name": "example"}
    - webgateway/imgData/<iid>/<key> params are:
    - did:  Dataset ID
    - key:  Optional key of selected attributes to return. E.g. meta,
            pixel_range, rdefs, split_channel, size etc
"""

wellData_json = url(r'^wellData/(?P<wid>[0-9]+)/$',
                    views.wellData_json,
                    name='webgateway_wellData_json')
"""
json method: returns details of specified Well. See L{views.wellData_json}.
    - webgateway/wellData/<wid>/ params are:
    - wid:  Well ID
"""

webgateway_search_json = url(r'^search/$', views.search_json,
                             name="webgateway_search_json")
"""
json method: returns search results. All parameters in request. See
L{views.search_json}
"""

get_rois_json = url(r'^get_rois_json/(?P<imageId>[0-9]+)/$',
                    views.get_rois_json,
                    name='webgateway_get_rois_json')
"""
gets all the ROIs for an Image as json. Image-ID is request: imageId=123
[{'id':123, 'shapes':[{'type':'Rectangle', 'theZ':5, 'theT':0, 'x':250,
                       'y':100, 'width':10 'height':45} ]
"""

get_shape_json = url(
    r'^get_shape_json/(?P<roiId>[0-9]+)/(?P<shapeId>[0-9]+)/$',
    views.get_shape_json, name='webgateway_get_shape_json')
"""
gets a Shape as json. ROI-ID, Shape-ID is request: roiId=123 and shapeId=123
{'type':'Rectangle', 'theZ':5, 'theT':0, 'x':250, 'y':100, 'width':10,
'height':45}
"""

histogram_json = url(
    r'^histogram_json/(?P<iid>[0-9]+)/channel/(?P<theC>[0-9]+)/',
    views.histogram_json,
    name="histogram_json")
"""
Gets a histogram of 256 columns (grey levels) for the chosen
channel of an image. A single plane is specified by ?theT=1&theZ=2.
"""

full_viewer = url(r'^img_detail/(?P<iid>[0-9]+)/$',
                  views.full_viewer,
                  name="webgateway_full_viewer")
"""
Returns html page displaying full image viewer and image details, rendering
settings etc.
See L{views.full_viewer}.
    - webgateway/img_detail/<iid>/ params are:
    - iid:  Image ID.
"""

save_image_rdef_json = url(r'^saveImgRDef/(?P<iid>[0-9]+)/$',
                        views.save_image_rdef_json)
"""
Saves rendering definition (from request parameters) on the image. See
L{views.save_image_rdef_json}.
For rendering parameters, see L{views.getImgDetailsFromReq} for details.
Returns 'true' if worked OK.
    - webgateway/saveImgRDef/<iid>/ params are:
    - iid:  Image ID.
"""

get_image_rdef_json = url(r'^getImgRDef/$', views.get_image_rdef_json,
                          name="get_image_rdef_json")
"""
Gets rendering definition from the 'session' if saved.
Returns json dict of 'c', 'm', 'z', 't'.
"""

listLuts_json = url(r'^luts/$', views.listLuts_json)
"""
json method: returning list of all lookup tables available
for rendering engine.
E.g. list of {path: "/luts/", size: 800, id: 37, name: "cool.lut"},
"""

list_compatible_imgs_json = url(r'^compatImgRDef/(?P<iid>[0-9]+)/$',
                             views.list_compatible_imgs_json)
"""
json method: returns list of IDs for images that have channels compatible with
the specified image, such that rendering settings can be copied from the image
to those returned. Images are selected from the same project that the
specified image is in.
    - webgateway/compatImgRDef/<iid>/ params are:
    - iid:  Image ID.
"""

copy_image_rdef_json = url(r'^copyImgRDef/$', views.copy_image_rdef_json,
                           name="copy_image_rdef_json")
"""
Copy the rendering settings from one image to a list of images, specified in
request by 'fromid' and list of 'toids'. See L{views.copy_image_rdef_json}
"""

reset_rdef_json = url(r'^resetRDef/$', views.reset_rdef_json,
                      name="reset_rdef_json")
"""
Reset the images within specified objects to their rendering settings at
import time"
Objects defined in request by E.g. totype=dataset&toids=1&toids=2
"""

reset_owners_rdef_json = url(r'^applyOwnersRDef/$',
                             views.reset_rdef_json,
                             {'toOwners': True},
                             name="reset_owners_rdef_json")
"""
Apply the owner's rendering settings to the specified objects.
Objects defined in request by E.g. totype=dataset&toids=1&toids=2
"""

webgateway_su = url(r'^su/(?P<user>[^/]+)/$', views.su,
                    name="webgateway_su")
"""
Admin method to switch to the specified user, identified by username: <user>
Returns 'true' if switch went OK.
"""

download_as = url(r'^download_as/(?:(?P<iid>[0-9]+)/)?$',
                  views.download_as, name="download_as")

archived_files = url(r'^archived_files/download/(?:(?P<iid>[0-9]+)/)?$',
                     views.archived_files, name="archived_files")
"""
This url will download the Original Image File(s) archived at import time. If
it's a single file, this will be downloaded directly. For multiple files, they
are assembled into a zip file on the fly, and this is downloaded.
"""

original_file_paths = url(r'^original_file_paths/(?P<iid>[0-9]+)/$',
                          views.original_file_paths,
                          name="original_file_paths")
"""
Get a json dict of original file paths.
'repo' is a list of path/name strings for original files in managed repo
'client' is a list of paths for original files on the client when imported
"""

open_with_options = url(r'^open_with/$', views.open_with_options,
                        name='open_with_options')
"""
This makes the settings.OPEN_WITH configuration available via json
"""


get_image_rdefs_json = url(r'^get_image_rdefs_json/(?P<img_id>[0-9]+)/$',
                           views.get_image_rdefs_json,
                           name="webgateway_get_image_rdefs_json")
"""
This url will retrieve all rendering definitions for a given image (id)
"""


urlpatterns = [
    webgateway,
    render_image,
    render_image_region,
    render_split_channel,
    render_row_plot,
    render_col_plot,
    render_roi_thumbnail,
    render_shape_thumbnail,
    render_shape_mask,
    render_thumbnail,
    render_birds_eye_view,
    render_ome_tiff,
    render_movie,
    webgateway_get_thumbnails_json,
    webgateway_get_thumbnail_json,
    # Template views
    # JSON methods
    listProjects_json,
    projectDetail_json,
    listDatasets_json,
    datasetDetail_json,
    webgateway_listimages_json,
    webgateway_listwellimages_json,
    webgateway_plategrid_json,
    imageData_json,
    wellData_json,
    webgateway_search_json,
    get_rois_json,
    get_shape_json,
    histogram_json,
    # image viewer
    full_viewer,
    # rendering def methods
    save_image_rdef_json,
    get_image_rdef_json,
    get_image_rdefs_json,
    listLuts_json,
    list_compatible_imgs_json,
    copy_image_rdef_json,
    reset_rdef_json,
    reset_owners_rdef_json,
    download_as,
    # download archived_files
    archived_files,
    original_file_paths,
    # switch user
    webgateway_su,
    # bulk annotations
    annotations,
    table_query,
    object_table_query,
    open_with_options,
]
