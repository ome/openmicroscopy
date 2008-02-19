#
# blitzcon/urls.py - django application url handling configuration
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

from django.conf.urls.defaults import *
#import os
#print os.getcwd()

urlpatterns = patterns('',
    (r'^media/(?P<path>.*)$', 'django.views.static.serve', {'document_root': 'blitzcon/media'}),
    (r'^(?P<client_base>[^/]+)/render_image/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/$', 'blitzcon.views.render_image'),
    (r'^(?P<client_base>[^/]+)/render_thumbnail/(?P<iid>[^/]+)/$', 'blitzcon.views.render_thumbnail'),
    (r'^(?P<client_base>[^/]+)/img_detail/(?P<iid>[^/]+)/(?:(?P<dsid>[^/]+)/)?$', 'blitzcon.views.image_viewer'),
    (r'^(?P<client_base>[^/]+)/ds_detail/(?P<dsid>[^/]+)/(?:(?P<prid>[^/]+)/)?$', 'blitzcon.views.dataset_viewer'),
    (r'^(?P<client_base>[^/]+)/pr_detail/(?P<prid>[^/]+)/(?:(?P<dsid>[^/]+)/)?$', 'blitzcon.views.project_viewer'),
    (r'^(?P<client_base>[^/]+)/page/(?P<page>[^/]+)/$', 'blitzcon.views.page'),
    # JSON methods
    (r'^(?P<client_base>[^/]+)/proj/list/$', 'blitzcon.views.listProjects_json'),
    (r'^(?P<client_base>[^/]+)/proj/(?P<pid>[^/]+)/children/$', 'blitzcon.views.listDatasets_json'),
    (r'^(?P<client_base>[^/]+)/dataset/(?P<did>[^/]+)/children/$', 'blitzcon.views.listImages_json'),
    (r'^(?P<client_base>[^/]+)/imgData/(?P<iid>[^/]+)/$', 'blitzcon.views.imageData_json'),
    #
    (r'^(?P<client_base>[^/]+)/disconnect/$', 'blitzcon.views.disconnect'),
    (r'^(?P<client_base>[^/]+)/(?:(?P<dsid>[^/]+)/)?(?:(?P<prid>[^/]+)/)?$', 'blitzcon.views.index'),
)

