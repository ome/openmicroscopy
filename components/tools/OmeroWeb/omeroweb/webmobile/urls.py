from django.conf.urls.defaults import *
from django.views.static import serve
import os

from omeroweb.webmobile import views

urlpatterns = patterns('',
    url( r'^$', views.index, name='webmobile_index' ),
    url( r'^login/$', views.login, name='webmobile_login' ),
    url( r'^logout/$', views.logout, name='webmobile_logout' ),
    url( r'^img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="image_viewer"),
    
    url( r'^dataset/(?P<id>[0-9]+)/$', views.dataset, name='webmobile_dataset' ),
    url( r'^viewer/(?P<imageId>[0-9]+)/$', views.viewer, name='webmobile_viewer' ),
    
    url(r'^appmedia/(?P<path>.*)$', 'django.views.static.serve', {'document_root': 'webmobile/media'}, name="mobile_static"),
)

