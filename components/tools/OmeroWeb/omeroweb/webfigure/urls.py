from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webfigure import views

webfigure_index = url( r'^$', views.index, name='webfigure_index' )
""" Home page - directed here after login. Displays links to other pages below """

webfigure_login = url( r'^login/$', views.login, name='webfigure_login' )
""" Login page - directed here if you are not logged in """

webfigure_logout = url( r'^logout/$', views.logout, name='webfigure_logout' )
""" Url to log user out - will then be directed to login page """

split_view_figure = url( r'^split_view_figure/$', views.split_view_figure, name="webfigure_split_view_figure")
"""
Displays images (one per row) split into individual channels (one per column) in a grid. All params are passed in request:
    - imageIds: comma-delimited list of IDs for the images to display
    - split_grey: if true, the split channels are all shown in greyscale
    - merged_names: if true, show the names of each channel above the merged image
"""

dataset_split_view = url( r'^dataset_split_view/(?P<datasetId>[0-9]+)/', views.dataset_split_view, name='webfigure_dataset_split_view' )
""" view a dataset as two panels of images, each with a different rendering setting """

image_dimensions = url( r'^image_dimensions/', views.image_dimensions, name='webfigure_image_dimensions' )
""" view an image in grid with the Z, C, T dimensions split over the x or y axes as chosen by user """

add_annotations = url( r'^add_annotations/$', views.add_annotations, name="webfigure_add_annotations")
""" 
post a comment annotation to images. parameters are in request:
     - imageIds=123,234 
     - comment=blah
"""

urlpatterns = patterns('django.views.generic.simple',
    webfigure_index,
    webfigure_login,
    webfigure_logout,
    split_view_figure,
    dataset_split_view,
    image_dimensions,
    add_annotations,
    
    # define the sym link for media. 
    #url( r'appmedia/webfigure/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media', 'webfigure').replace('\\','/') }, name="webfigure_media" ),
    
)