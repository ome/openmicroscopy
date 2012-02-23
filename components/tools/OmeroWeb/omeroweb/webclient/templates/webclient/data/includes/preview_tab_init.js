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
    var preview_tab_index = {{ forloop.counter }};

    var update_preview_tab = function() {
        
        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;
        
        // get the selected id etc
        var selected = datatree.data.ui.selected;
        var oid = selected.attr('id');
        var orel = selected.attr('rel').replace("-locked", "");
        if (orel!="image") return;
        
        // if the tab is visible and not loaded yet...
        $metadata_preview = $("#preview_tab");
        if ($metadata_preview.is(":visible") && $metadata_preview.is(":empty")) {
            var url = null;
            var pr = selected.parent().parent();
            if (pr.length>0 && pr.attr('rel').replace("-locked", "")==="share") {
                url = '{% url webindex %}metadata_preview/'+oid.split("-")[1]+'/'+pr.attr("id").split("-")[1]+'/';
            } else {
                url = '{% url webindex %}metadata_preview/'+oid.split("-")[1]+'/';
            }
            $metadata_preview.load(url);
        };
    };
    
    // update tabs when tree selection changes or tabs switch
    $("#annotation_tabs").bind( "tabsshow", update_preview_tab);

    // on change of selection in tree, update which tabs are enabled
    $("#dataTree").bind("select_node.jstree", function(e, data) {

        // clear contents of all panels
        $("#preview_tab").empty();

        // update enabled status
        var selected = data.inst.get_selected();
        if (selected.length > 1) {
            // handle batch annotation - select a different tab
            $("#annotation_tabs").tabs("select", 0);
        } else {
            // only enable this tab if we have an image.
            var orel = selected.attr('rel').replace("-locked", "");
            if(orel=="image") {
                $("#annotation_tabs").tabs("enable", preview_tab_index);
            } else {
                // not enabled - select a different tab
                $("#annotation_tabs").tabs("select", 0);
            }
        }

        // update tab content
        update_preview_tab();
    });
    
    // update after we've loaded the document
    update_preview_tab();
});

</script>