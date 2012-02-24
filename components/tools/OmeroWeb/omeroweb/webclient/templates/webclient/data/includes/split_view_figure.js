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
    
    var update_split_view_figure = function() {
        
        // this may have been called before datatree was initialised...
        var datatree = $.jstree._focused();
        if (!datatree) return;
        
        // get the selected id etc
        var selected = datatree.data.ui.selected;
        var orel = selected.attr('rel').replace("-locked", "");
        
        // if the tab is visible and not loaded yet...
        $split_view_fig = $("#split_view_fig_panel");
        var split_view_fig_url;
        if ($split_view_fig.is(":visible") && $split_view_fig.is(":empty")) {
            if (orel=="image") {
                var iids = [];
                selected.each(function() {
                    var dtype = this.id.split("-")[0];
                    if (dtype == "image") {
                        iids.push(this.id.split("-")[1]);
                    };
                });
                split_view_fig_url = '{% url webtest_split_view_figure %}?imageIds='+ iids.join(",") + "&width=150";
            }
            $split_view_fig.load(split_view_fig_url);
        };
    };
    
    // update tabs when tree selection changes or tabs switch
    $('#center_panel_chooser select').bind('change', update_split_view_figure);

    // on change of selection in tree, update which tabs are enabled
    $("#dataTree").bind("select_node.jstree", function(e, data) {

        // clear contents of all panels
        $("#split_view_fig_panel").empty();

        // update tab content
        update_split_view_figure();
    });
    
    // update after we've loaded the document
    //update_img_viewer();
});

</script>