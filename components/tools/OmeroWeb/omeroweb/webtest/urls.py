#!/usr/bin/env python
# -*- coding: utf-8 -*-
from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webtest import views

import os

urlpatterns = patterns('django.views.generic.simple',

    # index 'home page' of the webtest app
    url( r'^$', views.index, name='webtest_index' ),

    # 'Hello World' example from tutorial on http://trac.openmicroscopy.org.uk/ome/wiki/OmeroWeb
    url( r'^dataset/(?P<datasetId>[0-9]+)/$', views.dataset, name="webtest_dataset" ),

    # Another simple example - shows a stack preview for an image with multiple Z sections
    url( r'^stack_preview/(?P<imageId>[0-9]+)/$', views.stack_preview, name="webtest_stack_preview" ),

    # Displays images (one per row) one channel per column in a grid. Params are passed in request, E.g. imageIds
    url( r'^split_view_figure/$', views.split_view_figure, name="webtest_split_view_figure"),
    url( r'^split_view_figure_plugin/$', views.split_view_figure, 
            {"template":"webtest/webclient_plugins/split_view_figure.html"}, name="webtest_split_view_figure_plugin"),
    url( r'^split_view_fig_include/$', views.split_view_figure, 
            {"template":"webtest/webclient_plugins/split_view_fig_include.html"}, name="webtest_split_view_fig_include"),

    # View a dataset as two panels of images, each with a different rendering setting
    url( r'^dataset_split_view/(?P<datasetId>[0-9]+)/', views.dataset_split_view, name='webtest_dataset_split_view' ),
    url( r'^dataset_split_include/(?P<datasetId>[0-9]+)/', views.dataset_split_view,
            {"template":"webtest/webclient_plugins/dataset_split_include.html"}, name='webtest_dataset_split_include' ),

    # view an image in grid with the Z, C, T dimensions split over the x or y axes as chosen by user.
    url( r'^image_dimensions/(?P<imageId>[0-9]+)/', views.image_dimensions, name='webtest_image_dimensions' ),

    # Viewer overlays individual channels from the same image (or different images) and manipulate them separately..
    # translate, scale etc relative to one-another.
    url( r'^channel_overlay_viewer/(?P<imageId>[0-9]+)/', views.channel_overlay_viewer, name='webtest_channel_overlay_viewer' ),
    # this is the url for rendering planes for the viewer
    url( r'^render_channel_overlay/', views.render_channel_overlay, name='webtest_render_channel_overlay' ),

    # Show a panel of ROI thumbnails for an image
    url( r'^image_rois/(?P<imageId>[0-9]+)/', views.image_rois, name='webtest_image_rois' ),

    # post a comment annotation to images. parameters are in request: imageIds=123,234  comment=blah 
    # ns=Namespace replace=true (replaces existing comment with same ns if found)
    url( r'^add_annotations/$', views.add_annotations, name="webtest_add_annotations"),
    
    # examples of using the webgateway base templates
    url(r'^webgateway_templates/(?P<base_template>[a-z0-9_]+)/', views.webgateway_templates, name='webgateway_templates'),
    # same as webgateway base examples, except that these pgaes use webclient components that require login
    url(r'^webclient_templates/(?P<base_template>[a-z0-9_]+)/', views.webclient_templates, name='webclient_templates'),
    
    url( r'^img_detail/(?:(?P<iid>[0-9]+)/)?$', views.image_viewer, name="webtest_image_viewer"),

    # Test the speed of rendering all planes in an Image OR images in a Plate or Dataset (E.g. to compare FS)
    url( r'^render_performance/(?P<obj_type>[a-z]+)/(?P<id>[0-9]+)/', views.render_performance, name='webtest_render_performance' ),

    # create a test image. Extra params in request. E.g. createTestImage/?sizeX=50&sizeY=50&dataset=123
    # NB: Requires numpy to be installed on webserver machine
    url(r'^createTestImage/', views.createTestImage, name="webtest_createTestImage"),
)
