from django.conf.urls.defaults import *

from omeroweb.webfigure import views

urlpatterns = patterns('django.views.generic.simple',

    # index 'home page' of the webfigure app
    url( r'^$', views.index, name='webfigure_index' ),
    url( r'^make_web_figure/', views.make_web_figure, name='make_web_figure'),
    url( r'^save_web_figure/', views.save_web_figure, name='save_web_figure'),
    url( r'^load_web_figure/(?P<fileId>[0-9]+)/$', views.load_web_figure, name='load_web_figure'),
)