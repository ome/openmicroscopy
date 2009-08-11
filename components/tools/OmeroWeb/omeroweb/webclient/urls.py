#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

import os.path

from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webclient import views
#from omeroweb.webclient.feeds import RssShareFeed, AtomShareFeed

'''feeds = {
    'rss': RssShareFeed,
    'atom': AtomShareFeed,
}'''

urlpatterns = patterns('',
    
    url( r'^$', views.index, name="webindex" ),
    url( r'^context/$', views.index_context, name="index_context" ),
    url( r'^last_imports/$', views.index_last_imports, name="index_last_imports" ),
    url( r'^most_recent/$', views.index_most_recent, name="index_most_recent" ),
    url( r'^tag_cloud/$', views.index_tag_cloud, name="index_tag_cloud" ),
    
    url( r'^login/$', views.login, name="weblogin" ),
    url( r'^logout/$', views.logout, name="weblogout" ),
    url( r'^active_group/$', views.change_active_group, name="change_active_group" ),
    
    # manage mydata, userdata, groupdata
    url( r'^(?P<whos>((?i)mydata|userdata|groupdata))/$', views.manage_data, name="manage_data" ),
    url( r'^(?P<whos>((?i)mydata|userdata|groupdata))/(?P<o1_type>((?i)orphaned|ajaxorphaned))/$', views.manage_data, name="manage_data_orphaned" ),
    url( r'^(?P<whos>((?i)mydata|userdata|groupdata))/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/$', views.manage_data, name="manage_data_t_id" ),
    url( r'^(?P<whos>((?i)mydata|userdata|groupdata))/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/$', views.manage_data, name="manage_data_t_id_t_id" ),
    url( r'^(?P<whos>((?i)mydata|userdata|groupdata))/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-zA-Z]+)/(?P<o3_id>[0-9]+)/$', views.manage_data, name="manage_data_t_id_t_id_t_id" ),
        
    # direct access
    url( r'^(?P<o1_type>((?i)project|dataset|image))/(?P<o1_id>[0-9]+)/$', views.manage_data, name="manage_group_data_t_id" ),
    url( r'^(?P<o1_type>((?i)project|dataset))/(?P<o1_id>[0-9]+)/(?P<o2_type>((?i)dataset|image))/(?P<o2_id>[0-9]+)/$', views.manage_data, name="manage_group_data_t_id_t_id" ),
    url( r'^(?P<o1_type>((?i)project))/(?P<o1_id>[0-9]+)/(?P<o2_type>((?i)dataset))/(?P<o2_id>[0-9]+)/(?P<o3_type>((?i)image))/(?P<o3_id>[0-9]+)/$', views.manage_data, name="manage_group_data_t_id_t_id_t_id" ),
    
    url( r'^hierarchy/$', views.manage_container_hierarchies, name="manage_container_hierarchies" ),
    url( r'^hierarchy/(?P<o_type>[a-zA-Z]+)/(?P<o_id>[0-9]+)/$', views.manage_container_hierarchies, name="manage_container_hierarchies_id" ),
    url( r'^tree_details/(?P<c_type>[a-zA-Z]+)/(?P<c_id>[0-9]+)/$', views.manage_tree_details, name="manage_tree_details" ),
    
    url( r'^image_zoom/(?P<iid>[0-9]+)/$', views.manage_image_zoom, name="manage_image_zoom" ),
    
    url( r'^action/([a-zA-Z]+)/$', views.manage_action_containers, name="manage_action_containers" ),
    url( r'^action/([a-zA-Z]+)/([a-zA-Z]+)/([0-9]+)/$', views.manage_action_containers, name="manage_action_containers_id" ),    
    
    url( r'^metadata/(?P<o_type>[a-zA-Z]+)/(?P<o_id>[0-9]+)/$', views.manage_metadata, name="manage_metadata" ),
    
    url( r'^annotations/([a-zA-Z]+)/([0-9]+)/$', views.manage_annotations, name="manage_annotations" ),
    url( r'^annotation/([a-zA-Z]+)/([0-9]+)/$', views.manage_annotation, name="manage_annotation" ),
    
    url( r'^tag/$', views.manage_data_by_tag, name="manage_data_by_tag" ),
    url( r'^tag/([0-9]+)/$', views.manage_data_by_tag, name="manage_data_by_tag1" ),
    url( r'^tag/([0-9]+)/([0-9]+)/$', views.manage_data_by_tag, name="manage_data_by_tag2" ),
    url( r'^tag/([0-9]+)/([0-9]+)/([0-9]+)/$', views.manage_data_by_tag, name="manage_data_by_tag3" ),
    url( r'^tag/([0-9]+)/([0-9]+)/([0-9]+)/([0-9]+)/$', views.manage_data_by_tag, name="manage_data_by_tag4" ),
    url( r'^tag/([0-9]+)/([0-9]+)/([0-9]+)/([0-9]+)/([0-9]+)/$', views.manage_data_by_tag, name="manage_data_by_tag5" ),
    url( r'^autocompletetags/$', views.autocomplete_tags, name="autocomplete_tags" ),
    
    url( r'^render_thumbnail/(?P<iid>[0-9]+)/$', views.render_thumbnail, name="render_thumbnail" ),
    url( r'^render_thumbnail/(?P<iid>[0-9]+)/(?P<share_id>[0-9]+)/$', views.render_thumbnail, name="render_thumbnail_shareid" ),
    url( r'^render_thumbnail/size/(?P<size>[0-9]+)/(?P<iid>[0-9]+)/$', views.render_thumbnail_resize, name="render_thumbnail_resize" ),
    url( r'^render_thumbnail/size/(?P<size>[0-9]+)/(?P<iid>[0-9]+)/(?P<share_id>[0-9]+)/$', views.render_thumbnail_resize, name="render_thumbnail_resize_shareid" ),
    url( r'^render_thumbnail/details/(?P<iid>[0-9]+)/$', views.render_thumbnail_details, name="render_thumbnail_details" ),
    url( r'^render_thumbnail/big/(?P<iid>[0-9]+)/$', views.render_big_thumbnail, name="render_big_thumbnail" ),
    url( r'^shares/$', views.manage_shares, name="manage_shares" ),
    url( r'^share/([a-zA-Z]+)/$', views.manage_share, name="manage_share" ),
    url( r'^share/(?P<action>[a-zA-Z]+)/(?P<oid>[0-9]+)/$', views.manage_share, name="manage_share_action" ),
    url( r'^share_content/([0-9]+)/$', views.load_share_content, name="load_share_content" ),
    url( r'^share_owner_content/([0-9]+)/$', views.load_share_owner_content, name="load_share_owner_content" ),
    
    url( r'^basket/$', views.basket_action, name="basket"),
    url( r'^basket/empty/$', views.empty_basket, name="empty_basket"),
    url( r'^basket/update/$', views.update_basket, name="update_basket"),
    url( r'^basket/([a-zA-Z]+)/$', views.basket_action, name="basket_action"),
    url( r'^basket/(?P<action>[a-zA-Z]+)/(?P<oid>[0-9]+)/$', views.basket_action, name="basket_action_id"),
    
    url( r'^clipboard/$', views.update_clipboard, name="update_clipboard"),
    
    url( r'^search/$', views.search, name="search"),
    
    url( r'^history/$', views.history, name="history"),
    url( r'^history/(\d{4})/(\d{1,2})/$', views.history, name="history_date"),
    url( r'^history/(\d{4})/(\d{1,2})/(\d{1,2})/$', views.history_details, name="history_details"),
    
    url( r'^import/$', views.importer, name="importer"),
    url( r'^upload/$', views.flash_uploader, name="flash_uploader"), 
    
    url( r'^myaccount/$', views.myaccount, name="myaccount"),
    url( r'^myaccount/(?P<action>[a-zA-Z]+)/$', views.myaccount, name="myaccount_action"),
    
    url( r'^help/$', views.help, name="help" ),
    
    url( r'^myphoto/$', views.myphoto, name="myphoto"),
    url( r'^userphoto/(?P<oid>[0-9]+)/$', views.load_photo, name="load_photo"),
    url( r'^render_image/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$', views.render_image, name="render_image"),
    url( r'^render_image/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/(?P<share_id>[0-9]+)/$', views.render_image, name="render_image_shareid"),
    url( r'^img_detail/(?P<iid>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$', views.image_viewer, name="image_viewer"),
    url( r'^imgData/(?P<iid>[0-9]+)/$', views.imageData_json, name="imageData_json"),
    url( r'^imgData/(?P<iid>[0-9]+)/(?P<share_id>[0-9]+)/$', views.imageData_json, name="imageData_json_shareid"),
    
    url( r'^spellchecker/$', views.spellchecker, name="spellchecker"), 
    
    #( r'^feeds/(?P<url>.*)/$', 'django.contrib.syndication.views.feed',{'feed_dict': feeds}),
    
    #test ROI
    url( r'^test/$', views.test, name="test"), 
    url( r'^histogram/(?P<oid>[0-9]+)/$', views.histogram, name="histogram"), 
    
    url( r'^static/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media').replace('\\','/') }, name="webstatic" ),
)
