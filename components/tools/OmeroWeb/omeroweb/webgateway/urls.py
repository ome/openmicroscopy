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
    (r'^render_thumbnail/(?P<iid>[^/]+)/$', 'webgateway.views.render_thumbnail'),
    # JSON methods
    (r'^proj/list/$', 'webgateway.views.listProjects_json'),
    (r'^proj/(?P<pid>[^/]+)/detail/$', 'webgateway.views.projectDetail_json'),
    (r'^proj/(?P<pid>[^/]+)/children/$', 'webgateway.views.listDatasets_json'),
    (r'^dataset/(?P<did>[^/]+)/detail/$', 'webgateway.views.datasetDetail_json'),
    (r'^dataset/(?P<did>[^/]+)/children/$', 'webgateway.views.listImages_json'),
    (r'^imgData/(?P<iid>[^/]+)/(?:(?P<key>[^/]+)/)?$', 'webgateway.views.imageData_json'),
    (r'^search/$', 'webgateway.views.search_json'),

    (r'^saveImgRDef/(?P<iid>[^/]+)/$', 'webgateway.views.save_image_rdef_json'),
                       
    # Debug stuff
    (r'^dbg_connectors/$', 'webgateway.views.dbg_connectors'),

)

