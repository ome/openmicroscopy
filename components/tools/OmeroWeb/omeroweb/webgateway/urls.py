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

urlpatterns = patterns('',
    (r'^appmedia/(?P<path>.*)$', 'django.views.static.serve', {'document_root': 'webgateway/media'}),
    (r'^render_image/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/$', 'webgateway.views.render_image'),
    (r'^render_split_channel/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/$', 'webgateway.views.render_split_channel'),
    (r'^render_row_plot/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<y>[^/]+)/(?:(?P<w>[^/]+)/)?$', 'webgateway.views.render_row_plot'),
    (r'^render_col_plot/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<x>[^/]+)/(?:(?P<w>[^/]+)/)?$', 'webgateway.views.render_col_plot'),
    (r'^render_thumbnail/(?P<iid>[^/]+)/(?:(?P<w>[^/]+)/)?(?:(?P<h>[^/]+)/)?$', 'webgateway.views.render_thumbnail'),
    (r'^render_ome_tiff/(?P<ctx>[^/]+)/(?P<cid>[^/]+)/$', 'webgateway.views.render_ome_tiff'),
    (r'^render_movie/(?P<iid>[^/]+)/(?P<axis>[zt])/(?P<pos>[^/]+)/$', 'webgateway.views.render_movie'),

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

