{% comment %}
/**
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
*/
{% endcomment %}


<script type="text/javascript">
/**
 * This script is included in the main containers.html page as well as the container_tags.html and public.html pages,
 * adding itself as a selection listener to the jsTree in each case.
 * It loads appropriate data into the right-hand 'general' tab on selection changes in the jsTree.
**/

$(function () {
    
    $("#annotation_tabs").tabs({cache: true});

    // this is called on change in jstree selection, or switching tabs
    var update_metadata_general_tab = function() {
        var datatree = $.jstree._focused();
        var selected = datatree.data.ui.selected;
        
        var $metadata_general = $("#metadata_general");
        var $metadata_acquisition = $("#metadata_tab");
        var prefix = '{% url webindex %}';
        
        if(selected.length == 0) {
            return
        }
        if (selected.length > 1) {
            // handle batch annotation...
            var productListQuery = new Array(); 
            selected.each( function(i){
                productListQuery[i] = $(this).attr('id').replace("-","=");
            });
            var query = '{% url batch_annotate %}'+"?"+productListQuery.join("&")
            $metadata_general.load(query);
            
        } else {
            $("#annotation_tabs").tabs("enable", 0);    // always want metadata_general enabled
            var url = null;
            var oid = selected.attr('id');
            var orel = selected.attr('rel').replace("-locked", "");
            if (typeof oid =="undefined" || oid==false) return
            
            // handle loading of GENERAL tab
            if ($metadata_general.is(":visible") && $metadata_general.is(":empty")) {
                // orphaned
                if (oid.indexOf("orphaned")>=0) {
                    $metadata_general.html('<p>This is virtual container with orphaned images. These images are not linked anywhere. Just drag them to the selected container.</p>');
                    //return;
                // experimenter
                } else if (oid.indexOf("experimenter")>=0) {
                    $metadata_general.html('<p>'+selected.children().eq(1).text()+'</p>');
                // everything else
                } else {
                    if(orel=="image") {
                        var pr = selected.parent().parent();
                        if (pr.length>0 && pr.attr('rel').replace("-locked", "")==="share") {
                            url = prefix+'metadata_details/'+orel+'/'+oid.split("-")[1]+'/'+pr.attr("id").split("-")[1]+'/';
                        } else {
                            url = prefix+'metadata_details/'+orel+'/'+oid.split("-")[1]+'/';
                        }
                    } else {
                        url = prefix+'metadata_details/'+orel+'/'+oid.split("-")[1]+'/';
                    }
                }
                if (url !== null) {
                    $metadata_general.load(url);
                }
            }
        }
    }

    // update tabs when tree selection changes or tabs switch
    $("#annotation_tabs").bind( "tabsshow", update_metadata_general_tab);

    // on change of selection in tree, clear tab
    $("#dataTree").bind("select_node.jstree", function(e, data) {

        // clear contents of panel
        $("#metadata_general").empty();
        
        var selected = data.inst.get_selected();
        if (selected.length > 1) {
            // handle batch annotation - select first tab
            $("#annotation_tabs").tabs("select", 0);
        }

        update_metadata_general_tab();     // update
    });

});
</script>