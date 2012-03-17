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
    var rois_tab_index = {{ forloop.counter }};

    var update_rois_tab = function() {
        
        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;
        
        // get the selected id etc
        var selected = datatree.data.ui.selected;
        var oid = selected.attr('id');
        var orel = selected.attr('rel').replace("-locked", "");
        if (orel!="image") return;
        
        // if the tab is visible and not loaded yet...
        $image_roi_tab = $("#image_roi_tab");
        if ($image_roi_tab.is(":visible") && $image_roi_tab.is(":empty")) {
            var roi_url = '{% url webtest_index %}image_rois/'+oid.split("-")[1]+'/';
            $image_roi_tab.load(roi_url);
        };
    };
    
    // update tabs when tree selection changes or tabs switch
    $("#annotation_tabs").bind( "tabsshow", update_rois_tab);

    // on change of selection in tree, update which tabs are enabled
    $("#dataTree").bind("select_node.jstree deselect_node.jstree", function(e, data) {

        // clear contents of all panels
        $("#image_roi_tab").empty();

        var selected = data.inst.get_selected();
        var orel = selected.attr('rel').replace("-locked", "");

        // we only care about changing selection if this tab is selected...
        var select_tab = $("#annotation_tabs").tabs( "option", "selected" );
        if (rois_tab_index == select_tab) {
            // we don't want to select this tab if multiple selected
            if ((orel!="image") || (selected.length > 1)) {
                $("#annotation_tabs").tabs("select", 0);
            }
        }

        // update enabled state
        if((orel!="image") || (selected.length > 1)) {
            $("#annotation_tabs").tabs("disable", rois_tab_index);
        } else {
            $("#annotation_tabs").tabs("enable", rois_tab_index);
        }

        // update tab content
        update_rois_tab();
    });
    
    // update after we've loaded the document
    //update_rois_tab();
});

</script>