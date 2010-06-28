from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webemdb import views

urlpatterns = patterns('',
    url( r'^$', views.index, name='webemdb_index' ),
    url( r'^login/$', views.login, name='webemdb_login' ),
    url( r'^logout/$', views.logout, name='webemdb_logout' ),
    
    url( r'^entry/(?P<entryId>[0-9]+)/$', views.entry, name='webemdb_entry' ),
    url( r'^entry/(?P<entryId>[0-9]+)/gif/(?P<fileId>[0-9]+)/$', views.gif, name='webemdb_gif' ),
    url( r'^entry/(?P<entryId>[0-9]+)/xml/(?P<fileId>[0-9]+)/$', views.xml, name='webemdb_xml' ),
    url( r'^img/(?P<imageId>[0-9]+)/map/(?P<fileId>[0-9]+)/$', views.map, name='webemdb_map' ),
)
