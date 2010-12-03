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

from django.conf.urls.defaults import *

appmedia = (r'^appmedia/(?P<path>.*)$', 'django.views.static.serve', {'document_root': 'webgateway/media'})
""" 
path to media files for webgateway 
    - path: path to media file
"""

render_image = (r'^render_image/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/$', 'webgateway.views.render_image')
"""
Returns a jpeg of the OMERO image. See L{views.render_image}. Rendering settings can be specified
in the request parameters. See L{.views.getImgDetailsFromReq} for details. 
    Params in render_image/<iid>/<z>/<t>/ are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
"""

render_split_channel = (r'^render_split_channel/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/$', 'webgateway.views.render_split_channel')
"""
Returns a jpeg of OMERO Image with channels split into different panes in a grid. See L{views.render_split_channel}.
Rendering settings can be specified in the request parameters (as above).
    Params in render_split_channel/<iid>/<z>/<t> are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
"""

render_row_plot = (r'^render_row_plot/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<y>[^/]+)/(?:(?P<w>[^/]+)/)?$', 
    'webgateway.views.render_row_plot')
"""
Returns a gif graph of pixel values for a row of an image plane. See L{views.render_row_plot}.
Channels can be turned on/off using request. E.g. c=-1,2,-3,-4
    Params in render_row_plot/<iid>/<z>/<t>/<y>/<w> are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
    - y:    Y position of pixel row
    - w:    Optional line width of plot
"""

render_col_plot = (r'^render_col_plot/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<x>[^/]+)/(?:(?P<w>[^/]+)/)?$', 
    'webgateway.views.render_col_plot')
"""
Returns a gif graph of pixel values for a column of an image plane. See L{views.render_col_plot}.
Channels can be turned on/off using request. E.g. c=-1,2,-3,-4
    Params in render_col_plot/<iid>/<z>/<t>/<x>/<w> are:
    - iid:  Image ID
    - z:    Z index
    - t:    T index
    - x:    X position of pixel column
    - w:    Optional line width of plot
"""

render_thumbnail = (r'^render_thumbnail/(?P<iid>[^/]+)/(?:(?P<w>[^/]+)/)?(?:(?P<h>[^/]+)/)?$', 'webgateway.views.render_thumbnail')
"""
Returns a thumbnail jpeg of the OMERO Image, optionally scaled to max-width and max-height.
See L{views.render_thumbnail}. Uses current rendering settings. 
    Params in render_thumbnail/<iid>/<w>/<h> are:
    - iid:  Image ID
    - w:    Optional max width
    - h:    Optional max height
"""

render_ome_tiff = (r'^render_ome_tiff/(?P<ctx>[^/]+)/(?P<cid>[^/]+)/$', 'webgateway.views.render_ome_tiff')
"""
Generates an OME-TIFF of an Image (or zip for multiple OME-TIFFs) and returns the file or redirects 
to a temp file location. See L{views.render_ome_tiff}
    Params in render_ome_tiff/<ctx>/<cid> are:
    - ctx:      The container context. 'p' for Project, 'd' for Dataset or 'i' Image. 
    - cid:      ID of container.
"""

render_movie = (r'^render_movie/(?P<iid>[^/]+)/(?P<axis>[zt])/(?P<pos>[^/]+)/$', 'webgateway.views.render_movie')
"""
Generates a movie file from the image, spanning Z or T frames. See L{views.render_movie}
Returns the file or redirects to temp file location. 
    Params in render_movie/<iid>/<axis>/<pos> are:
    - iid:      Image ID
    - axis:     'z' or 't' dimension that movie plays
    - pos:      The T index (for 'z' movie) or Z index (for 't' movie)
"""

urlpatterns = patterns('',
    appmedia,
    render_image,
    render_split_channel,
    render_row_plot,
    render_col_plot,
    render_thumbnail,
    render_ome_tiff,
    render_movie,

    # Template views
    (r'^test/$', 'webgateway.views.test'),

    # JSON methods
    (r'^proj/list/$', 'webgateway.views.listProjects_json'),
    (r'^proj/(?P<pid>[^/]+)/detail/$', 'webgateway.views.projectDetail_json'),
    (r'^proj/(?P<pid>[^/]+)/children/$', 'webgateway.views.listDatasets_json'),
    (r'^dataset/(?P<did>[^/]+)/detail/$', 'webgateway.views.datasetDetail_json'),
    url(r'^dataset/(?P<did>[^/]+)/children/$', 'webgateway.views.listImages_json', name="webgateway_listimages_json"),
    (r'^imgData/(?P<iid>[^/]+)/(?:(?P<key>[^/]+)/)?$', 'webgateway.views.imageData_json'),
    url(r'^search/$', 'webgateway.views.search_json', name="webgateway_search_json"),
    (r'^img_detail/(?P<iid>[0-9]+)/$', "webgateway.views.full_viewer"),

    (r'^saveImgRDef/(?P<iid>[^/]+)/$', 'webgateway.views.save_image_rdef_json'),
    (r'^resetImgRDef/(?P<iid>[^/]+)/$', 'webgateway.views.reset_image_rdef_json'),
    (r'^compatImgRDef/(?P<iid>[^/]+)/$', 'webgateway.views.list_compatible_imgs_json'),
    (r'^copyImgRDef/$', 'webgateway.views.copy_image_rdef_json'),

    url(r'^su/(?P<user>[^/]+)/$', 'webgateway.views.su', name="webgateway_su"),
    
    # Debug stuff
    (r'^dbg_connectors/$', 'webgateway.views.dbg_connectors'),

)

