{% comment %}
<!--
  Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
  All rights reserved.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
{% endcomment %}

<script>

$(document).ready(function() {
    
    var syncPanels = function(get_selected) {
        var toSelect = new Array();
        get_selected.each(function(i) {
            var _this = $(this)
            if ($.inArray(_this.attr("rel").replace("-locked", ""), ["image"]) >= 0) toSelect[i]=_this.attr("id").split("-")[1];
        });
        $(".ui-selectee", $("ul.ui-selectable")).each(function(){
            var selectee = $(this);
            if ($.inArray(selectee.attr('id'), toSelect) != -1) {
                if(!selectee.hasClass('ui-selected')) {
                    selectee.addClass('ui-selected');
                }
            } else {
                selectee.removeClass('ui-selected');
            }
        });
    }

    // on change of selection in tree, update center panel
    var update_thumbnails_panel = function() {

        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;

        // get the selected id etc
        var selected = datatree.data.ui.selected;

        if (selected.length == 0) {
            $("div#content_details").empty();
            $("div#content_details").removeAttr('rel');
            return;
        }
        if (selected.length > 1) {
            // if any non-images are selected, clear the centre panel
            if (selected.filter('li:not([id|=image])').length > 0) {
                $("div#content_details").empty();
                $("div#content_details").removeAttr('rel');
            }
            return;
        }
        // handle single object selection...
        var oid = selected.attr('id');                              // E.g. 'dataset-501'
        var orel = selected.attr('rel').replace("-locked", "");     // E.g. 'dataset'
        var page = selected.data("page") || null;                   // Check for pagination

        // Check what we've currently loaded: E.g. 'dataset-501'
        var crel = $("div#content_details").attr('rel');
        if (!oid) return;
        
        update = {'url': null, 'rel': null, 'empty':false };
        var prefix = "{% url webindex %}";
        
        // Show nothing for Experimenter, Project or Screen...
        if ($.inArray(orel, ["experimenter", "project", "screen"])>-1) {
            update['empty'] = true;
        } else if (oid.indexOf("orphaned")>=0) {
            if (oid!==crel) {           // check we've not already loaded orphaned
                update['rel'] = oid;
                update['url'] = prefix+'load_data/'+orel+'/?view=icon';
            }
        } else if(orel == "plate") {
            if (datatree.is_leaf(selected)) {   // Load Plate if it's a 'leaf' (No PlateAcquisition)...
                update['rel'] = oid;
                update['url'] = prefix+'load_data/'+orel+'/'+oid.split("-")[1]+'/';
            } else {
                update['empty'] = true;
            }
        } else if(orel == "acquisition"){
            if (oid!==crel) {
                var plate = datatree._get_parent(selected).attr('id').replace("-", "/");
                update['rel'] = oid;
                update['url'] = prefix+'load_data/'+plate+'/'+orel+'/'+oid.split("-")[1]+'/';
            }
        } else if(orel == "dataset") {
            if (oid!==crel) {
                update['rel'] = oid;
                update['url'] = prefix+'load_data/'+orel+'/'+oid.split("-")[1]+'/?view=icon';
                if (page) update['url'] += "&page="+page;
            }
        } else if(orel == "share") {
            if (oid!==crel) {
                update['rel'] = oid;
                update['url'] = prefix+'load_public/'+oid.split("-")[1]+'/?view=icon';
            }
        //} else if($.inArray(orel, ["tag"]) > -1 && oid!==crel) {
        //    update['rel'] = oid;
        //    update['url'] = prefix+'load_tags/?view=icon&o_type=tag&o_id='+oid.split("-")[1];
        } else if(orel=="image") {
            var pr = selected.parent().parent();
            if (pr.length>0 && pr.attr('id')!==crel) {
                if(pr.attr('rel').replace("-locked", "")==="share" && pr.attr('id')!==crel) {
                    update['rel'] = pr.attr('id');
                    update['url'] = prefix+'load_public/'+pr.attr('id').split("-")[1]+'/?view=icon';
                } else if (pr.attr('rel').replace("-locked", "")=="tag") {
                    update['rel'] = pr.attr('id');
                    update['url'] = prefix+'load_tags/'+pr.attr('rel').replace("-locked", "")+'/'+pr.attr("id").split("-")[1]+'/?view=icon';
                } else if (pr.attr('rel').replace("-locked", "")!=="orphaned") {
                    update['rel'] = pr.attr('id');
                    update['url'] = prefix+'load_data/'+pr.attr('rel').replace("-locked", "")+'/'+pr.attr("id").split("-")[1]+'/?view=icon';
                } else {
                    update['rel'] = pr.attr("id");
                    update['url'] = prefix+'load_data/'+pr.attr('rel').replace("-locked", "")+'/?view=icon';
                }
            }
        } else {
            update['empty'] = true;
        }

        var $content_details = $("#content_details");
        
        // if nothing to show - clear panel
        if (update.empty) {
            $("div#content_details").empty();
            $("div#content_details").removeAttr('rel');
        }
        // only load data if panel is visible, otherwise clear panel
        else if (update.rel!==null && update.url!==null){
            if ($content_details.is(":visible")) {
                $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/common/image/spinner.gif"/></p>');
                $("div#content_details").attr('rel', update.rel);
                $("div#content_details").load(update.url, function() {
                    syncPanels(selected);
                });
            } else {
                $("div#content_details").empty();
                $("div#content_details").removeAttr('rel');
            }
        }
        
        syncPanels(selected); // update selected thumbs
    };
    
    // on change of selection in tree OR switching pluginupdate center panel
    $("#dataTree").bind("select_node.jstree", update_thumbnails_panel);
    
    $('#center_panel_chooser select').bind('change', update_thumbnails_panel);

});

</script>