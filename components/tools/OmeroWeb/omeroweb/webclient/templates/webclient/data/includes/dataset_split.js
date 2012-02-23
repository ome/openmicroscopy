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
    
    var update_dataset_split_viewer = function() {
        
        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;
        
        // get the selected id etc
        var selected = datatree.data.ui.selected;
        var oid = selected.attr('id');
        var orel = selected.attr('rel').replace("-locked", "");
        
        // if the tab is visible and not loaded yet...
        $dataset_split_viewer = $("#dataset_split_viewer");
        var split_dataset_url;
        if ($dataset_split_viewer.is(":visible") && $dataset_split_viewer.is(":empty")) {
            if (orel=="image") {
                // handle selection update
            } else if (orel=="dataset") {
                split_dataset_url = '{% url webtest_index %}dataset_split_view/'+oid.split("-")[1]+'/';
            }
            $dataset_split_viewer.load(split_dataset_url);
        };
    };
    
    // update tabs when tree selection changes or tabs switch
    //$("#annotation_tabs").bind( "tabsshow", update_img_viewer);
    $('#center_panel_chooser select').bind('change', update_dataset_split_viewer);

    // on change of selection in tree, update which tabs are enabled
    $("#dataTree").bind("select_node.jstree", function(e, data) {

        // clear contents of all panels
        $("#dataset_split_viewer").empty();

        // update tab content
        update_dataset_split_viewer();
    });
    
    // update after we've loaded the document
    //update_img_viewer();
});

</script>