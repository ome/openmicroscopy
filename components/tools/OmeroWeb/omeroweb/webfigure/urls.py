from django.conf.urls.defaults import *

from omeroweb.webfigure import views

urlpatterns = patterns('django.views.generic.simple',

    # index 'home page' of the webfigure app
    url( r'^$', views.index, name='webfigure_index' ),

    # Send json to OMERO to create pdf using scripting service
    url( r'^make_web_figure/', views.make_web_figure, name='make_web_figure'),

    # Save json to file annotation
    url( r'^save_web_figure/', views.save_web_figure, name='save_web_figure'),

    # Get json from file (file annotation Id)
    url( r'^load_web_figure/(?P<fileId>[0-9]+)/$', views.load_web_figure, name='load_web_figure'),

    # List file annotations of saved Figures
    url( r'^list_web_figures/', views.list_web_figures, name='list_web_figures'),
)