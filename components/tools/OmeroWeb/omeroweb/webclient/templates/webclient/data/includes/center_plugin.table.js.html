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

    // this script is an 'include' within a django for-loop, so we can get our index:
    var table_plugin_index = {{ forloop.counter }};

    // updates the selected images in the table
    var syncTableSelection = function(get_selected) {
        $("#dataTable .ui-selectee").removeClass('ui-selected');    // clear selection
        get_selected.each(function(i) {
            var dtype = $(this).attr("rel").replace("-locked", "");
            if (dtype == "image") {
                var imgId = $(this).attr("id").split("-")[1];
                $("#image_row-"+imgId).addClass('ui-selected');
            }
        });
    }

    // on change of selection in tree etc, update table
    var update_image_table = function() {

        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;

        // get the selected id etc
        var selected = datatree.data.ui.selected;

        var $image_table = $("#image_table");
        if (selected.length == 0) {
            $image_table.empty();
            $image_table.removeAttr('rel');
            return;
        }
        if (selected.length > 1) {
            // if any non-images are selected, clear the centre panel
            if (selected.filter('li:not([id|=image])').length > 0) {
                $image_table.empty();
                $image_table.removeAttr('rel');
                return;
            }
        }
        // handle single object (or multi-image) selection...
        var oid = selected.attr('id');                              // E.g. 'dataset-501'
        var orel = selected.attr('rel').replace("-locked", "");     // E.g. 'dataset'
        var page = selected.data("page") || null;                   // Check for pagination

        if (!oid) return;

        // Check what we've currently loaded: E.g. 'dataset-501'
        var crel = $image_table.attr('rel');
        var cpage = $("div#page").attr('rel') || null;
        
        var update = {'url': null, 'rel': null, 'empty':false };
        var prefix = "{% url webindex %}";
        
        if (orel == "orphaned") {
            update['rel'] = oid;
            update['url'] = prefix+'load_data/'+orel+'/?view=table';
        } else if(orel == "dataset") {
            update['rel'] = oid;
            update['url'] = prefix+'load_data/'+orel+'/'+oid.split("-")[1]+'/?view=table';
        } else if(orel == "share") {
            update['rel'] = oid;
            update['url'] = prefix+'load_public/'+oid.split("-")[1]+'/?view=table';
        //} else if($.inArray(orel, ["tag"]) > -1 && oid!==crel) {
        //    update['rel'] = oid;
        //    update['url'] = prefix+'load_tags/?view=icon&o_type=tag&o_id='+oid.split("-")[1];
        } else if(orel=="image") {
            var pr = selected.parent().parent();
            if (page == null) {
                page = pr.data("page") || null;
            }
            if (pr.length>0 && pr.attr('id')!==crel) {
                if(pr.attr('rel').replace("-locked", "")==="share" && pr.attr('id')!==crel) {
                    update['rel'] = pr.attr('id');
                    update['url'] = prefix+'load_public/'+pr.attr('id').split("-")[1]+'/?view=table';
                } else if (pr.attr('rel').replace("-locked", "")=="tag") {
                    update['rel'] = pr.attr('id');
                    update['url'] = prefix+'load_tags/'+pr.attr('rel').replace("-locked", "")+'/'+pr.attr("id").split("-")[1]+'/?view=table';
                } else if (pr.attr('rel').replace("-locked", "")!=="orphaned") {
                    update['rel'] = pr.attr('id');
                    update['url'] = prefix+'load_data/'+pr.attr('rel').replace("-locked", "")+'/'+pr.attr("id").split("-")[1]+'/?view=table';
                } else {
                    update['rel'] = pr.attr("id");
                    update['url'] = prefix+'load_data/'+pr.attr('rel').replace("-locked", "")+'/?view=table';
                }
            }
        } else {
            update['empty'] = true;
        }

        // need to refresh if page or E.g. dataset has changed
        var need_refresh = ((oid!==crel) || (page!==cpage));

        // if nothing to show - clear panel
        if (update.empty) {
            $image_table.empty();
            $image_table.removeAttr('rel');
        }
        // only load data if panel is visible, otherwise clear panel
        else if (update.rel!==null && update.url!==null && need_refresh){
            if (page) update['url'] += "&page="+page;
            if ($image_table.is(":visible")) {
                $image_table.html('<p>Loading data... please wait <img src ="../../static/webgateway/img/spinner.gif"/></p>');
                $image_table.attr('rel', update.rel);
                $image_table.load(update.url, function() {
                    syncTableSelection(selected);
                });
            } else {
                $image_table.empty();
                $image_table.removeAttr('rel');
            }
        }
        
        syncTableSelection(selected); // update selected images in table
    };


    // on change of selection in tree, update which plugins are enabled
    $("#dataTree").bind("select_node.jstree deselect_node.jstree", function(e, data) {

        var selected = data.inst.get_selected();
        // if nothing selected, disable, clear view and we're done.
        if (selected.length == 0) {
            set_center_plugin_enabled(table_plugin_index, false);
            $("#image_table").empty();
            return;
        }
        
        var orel = selected.attr('rel').replace("-locked", "");

        // update enabled state... table supports various types...
        if ($.inArray(orel, ["image", "dataset", "orphaned", "share"])>-1) {
            set_center_plugin_enabled(table_plugin_index, true);
        } else {
            // otherwise, disable this option
            set_center_plugin_enabled(table_plugin_index, false);
        }

        update_image_table();
    });
    
    $('#center_panel_chooser').bind('change', update_image_table);

});

</script>