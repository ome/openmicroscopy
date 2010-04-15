from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webtest import views

urlpatterns = patterns('',
    url( r'^$', views.index, name='webtest_index' ),
    url( r'^login/$', views.login, name='webtest_login' ),
    url( r'^logout/$', views.logout, name='webtest_logout' ),
    url( r'^metadata/(?P<iid>[0-9]+)/$', views.metadata, name='webtest_metadata' ),
    url( r'^img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="image_viewer"),
)
