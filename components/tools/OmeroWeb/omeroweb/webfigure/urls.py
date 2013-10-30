from django.conf.urls.defaults import *

from omeroweb.webfigure import views

urlpatterns = patterns('django.views.generic.simple',

    # index 'home page' of the webfigure app
    url( r'^$', views.index, name='webfigure_index' ),
    url( r'^make_web_figure/', views.make_web_figure, name='make_web_figure'),
)