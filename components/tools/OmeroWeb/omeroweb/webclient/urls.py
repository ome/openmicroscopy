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
    
    ( r'(?i)^$', views.index ),
    ( r'(?i)^context/$', views.index_context ),
    ( r'(?i)^my_data/$', views.index_my_data ),
    ( r'(?i)^last_imports/$', views.index_last_imports ),
    ( r'(?i)^most_recent/$', views.index_most_recent ),
    
    ( r'(?i)^login/$', views.login ),
    ( r'(?i)^logout/$', views.logout ),
    ( r'(?i)^active_group/$', views.change_active_group ),
    
    ( r'(?i)^mydata/$', views.manage_my_data ),
    ( r'(?i)^mydata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/$', views.manage_my_data ),
    ( r'(?i)^mydata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/$', views.manage_my_data ),
    ( r'(?i)^mydata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-zA-Z]+)/(?P<o3_id>[0-9]+)/$', views.manage_my_data ),
    ( r'(?i)^mydata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-zA-Z]+)/(?P<o3_id>[0-9]+)/(?P<action>[a-zA-Z]+)/$', views.manage_my_data ),
    ( r'(?i)^userdata/$', views.manage_user_containers ),
    ( r'(?i)^userdata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/$', views.manage_user_containers ),
    ( r'(?i)^userdata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/$', views.manage_user_containers ),
    ( r'(?i)^userdata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-zA-Z]+)/(?P<o3_id>[0-9]+)/$', views.manage_user_containers ),
    ( r'(?i)^groupdata/$', views.manage_group_containers ),
    ( r'(?i)^groupdata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/$', views.manage_group_containers ),
    ( r'(?i)^groupdata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/$', views.manage_group_containers ),
    ( r'(?i)^groupdata/(?P<o1_type>[a-zA-Z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-zA-Z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-zA-Z]+)/(?P<o3_id>[0-9]+)/$', views.manage_group_containers ),
    
    # direct access
    ( r'(?i)^(?P<o1_type>((?i)project|dataset|image))/(?P<o1_id>[0-9]+)/$', views.manage_group_containers ),
    ( r'(?i)^(?P<o1_type>((?i)project|dataset))/(?P<o1_id>[0-9]+)/(?P<o2_type>((?i)dataset|image))/(?P<o2_id>[0-9]+)/$', views.manage_group_containers ),
    ( r'(?i)^(?P<o1_type>((?i)project))/(?P<o1_id>[0-9]+)/(?P<o2_type>((?i)dataset))/(?P<o2_id>[0-9]+)/(?P<o3_type>((?i)image))/(?P<o3_id>[0-9]+)/$', views.manage_group_containers ),
    
    ( r'(?i)^hierarchy/$', views.manage_container_hierarchy ),
    ( r'(?i)^hierarchy/(?P<o_type>[a-zA-Z]+)/(?P<o_id>[0-9]+)/$', views.manage_container_hierarchy ),
    ( r'(?i)^tree_details/(?P<c_type>[a-zA-Z]+)/(?P<c_id>[0-9]+)/$', views.manage_tree_details ),
    
    ( r'(?i)^image_zoom/(?P<iid>[0-9]+)/$', views.manage_image_zoom ),
    
    ( r'(?i)^action/([a-zA-Z]+)/$', views.manage_action_containers ),
    ( r'(?i)^action/([a-zA-Z]+)/([a-zA-Z]+)/([0-9]+)/$', views.manage_action_containers ),    
    
    ( r'(?i)^metadata/(?P<o_type>[a-zA-Z]+)/(?P<o_id>[0-9]+)/$', views.manage_metadata ),
    
    ( r'(?i)^annotations/([a-zA-Z]+)/([0-9]+)/$', views.manage_annotations ),
    ( r'(?i)^annotation/([a-zA-Z]+)/([0-9]+)/$', views.manage_annotation ),
    ( r'(?i)^render_thumbnail/(?P<iid>[0-9]+)/$', views.render_thumbnail ),
    ( r'(?i)^render_thumbnail/details/(?P<iid>[0-9]+)/$', views.render_thumbnail_details ),
    ( r'(?i)^render_thumbnail/(?P<size>[0-9]+)/(?P<iid>[0-9]+)/$', views.render_thumbnail_resize ),
    ( r'(?i)^render_thumbnail/big/(?P<iid>[0-9]+)/$', views.render_big_thumbnail ),
    ( r'(?i)^share/$', views.manage_shares ),
    ( r'(?i)^share/([a-zA-Z]+)/$', views.manage_share ),
    ( r'(?i)^share/(?P<action>[a-zA-Z]+)/(?P<oid>[0-9]+)/$', views.manage_share ),
    ( r'(?i)^share_content/([0-9]+)/$', views.load_share_content ),
    
    ( r'(?i)^empty/$', views.empty_basket ),
    ( r'(?i)^basket/$', views.basket_action ),
    ( r'(?i)^basket/update/$', views.update_basket ),
    ( r'(?i)^basket/([a-zA-Z]+)/$', views.basket_action ),
    ( r'(?i)^basket/(?P<action>[a-zA-Z]+)/(?P<oid>[0-9]+)/$', views.basket_action ),
    
    ( r'(?i)^search/$', views.search ),
    ( r'(?i)^history/(.*)/(\d{4})/(\d{1,2})/$', views.history ),
    ( r'(?i)^history/(.*)/(\d{4})/(\d{1,2})/(\d{1,2})/$', views.history_details ),
    ( r'(?i)^impexp/(.*)/$', views.impexp ),
    ( r'(?i)^myaccount/(.*)/$', views.myaccount ),
    ( r'(?i)^help/(.*)/$', views.help ),
    ( r'(?i)^static/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media').replace('\\','/') } ),
    
    (r'(?i)^userphoto/$', views.load_photo),
    (r'(?i)^userphoto/(?P<oid>[0-9]+)/$', views.load_photo),
    (r'(?i)^render_image/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$', views.render_image),
    (r'(?i)^img_detail/(?P<iid>[0-9]+)/(?:(?P<dsid>[0-9]+)/)?$', views.image_viewer),
    (r'(?i)^imgData/(?P<iid>[0-9]+)/$', views.imageData_json),

    (r'(?i)^spellchecker/$', views.spellchecker), 
    
    #(r'(?i)^feeds/(?P<url>.*)/$', 'django.contrib.syndication.views.feed',{'feed_dict': feeds}),
    
    #( r'(?i)^image/(?P<image_id>[0-9]+)/$', views.manage_image ),
    
    #( r'(?P<c_type>[a-zA-Z]+)/(?P<c_id>[0-9]+)/$', views.manage_container_data ),
    #( r'(?P<c_type>[a-zA-Z]+)/(?P<c_id>[0-9]+)/$', views.manage_container_data ),
    
    

)
