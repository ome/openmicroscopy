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
    var splitview_plugin_index = {{ forloop.counter }};

    var update_dataset_split_viewer = function() {
        
        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;
        
        // get the selected id etc
        var selected = datatree.data.ui.selected;
        var oid = selected.attr('id');
        var orel = selected.attr('rel').replace("-locked", "");
        
        // if the tab is visible and not loaded yet...
        $split_view_panel = $("#split_view_panel");
        var split_view_url;
        if ($split_view_panel.is(":visible") && $split_view_panel.is(":empty")) {
            // if image(s) selected, show typical split-view figure
            if (orel=="image") {
                var iids = [];
                selected.each(function() {
                    var dtype = this.id.split("-")[0];
                    if (dtype == "image") {
                        iids.push(this.id.split("-")[1]);
                    };
                });
                split_view_url = '{% url webtest_split_view_figure_plugin %}?imageIds='+ iids.join(",") + "&width=120";
            // if it's a dataset, show 2 rendering settings side by side.
            } else if (orel=="dataset") {
                split_view_url = '{% url webtest_index %}dataset_split_view/'+oid.split("-")[1]+'/';
            }
            $split_view_panel.load(split_view_url);
        };
    };
    
    // update plugin when we switch between plugins
    $('#center_panel_chooser').bind('center_plugin_changed.ome', update_dataset_split_viewer);

    // on change of selection in tree, update which plugins are enabled
    $("#dataTree").bind("select_node.jstree deselect_node.jstree", function(e, data) {

        // clear contents of all panels
        $("#split_view_panel").empty();

        var selected = data.inst.get_selected();
        if (selected.length == 0) {
            set_center_plugin_enabled(splitview_plugin_index, false);
            return;
        }
        var orel = selected.attr('rel').replace("-locked", "");

        // update enabled state... split-view supports multiple 'image' or single dataset
        if ((orel=="image") && (selected.length > 0)) {
            set_center_plugin_enabled(splitview_plugin_index, true);
        } else if ((orel=="dataset") && (selected.length == 1)) {
            set_center_plugin_enabled(splitview_plugin_index, true);
        } else {
            // otherwise, disable this option
            set_center_plugin_enabled(splitview_plugin_index, false);
        }

        // update tab content
        update_dataset_split_viewer();
    });
    
    // update after we've loaded the document
    //update_img_viewer();
});

</script>