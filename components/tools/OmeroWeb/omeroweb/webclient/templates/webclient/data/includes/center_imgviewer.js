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
    
    var update_img_viewer = function() {
        
        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;
        
        // get the selected id etc
        var selected = datatree.data.ui.selected;
        var oid = selected.attr('id');
        var orel = selected.attr('rel').replace("-locked", "");
        if (orel!="image") return;
        
        // if the tab is visible and not loaded yet...
        $image_viewer_panel = $("#image_viewer_panel");
        if ($image_viewer_panel.is(":visible") && $image_viewer_panel.is(":empty")) {
            var url = null;
            var pr = selected.parent().parent();
            if (pr.length>0 && pr.attr('rel').replace("-locked", "")==="share") {
                //url = '{% url webindex %}metadata_preview/'+oid.split("-")[1]+'/'+pr.attr("id").split("-")[1]+'/';
            } else {
                url = '{% url webindex %}metadata_preview/'+oid.split("-")[1]+'/';
            }
            $image_viewer_panel.load(url);
        };
    };
    
    // update tabs when tree selection changes or tabs switch
    //$("#annotation_tabs").bind( "tabsshow", update_img_viewer);
    $('#center_panel_chooser select').bind('change', update_img_viewer);

    // on change of selection in tree, update which tabs are enabled
    $("#dataTree").bind("select_node.jstree", function(e, data) {

        // clear contents of all panels
        $("#image_viewer_panel").empty();

        // update tab content
        update_img_viewer();
    });
    
    // update after we've loaded the document
    //update_img_viewer();
});

</script>