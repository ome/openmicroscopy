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
    $("#dataTree").bind("select_node.jstree", function(e, data) {

        var selected = data.inst.get_selected();
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
        var oid = selected.attr('id');
        var orel = selected.attr('rel').replace("-locked", "");
        var crel = $("div#content_details").attr('rel');
        if (!oid) return;
        
        update = {'url': null, 'rel': null, 'empty':false };
        var prefix = "{% url webindex %}";
        if (oid.indexOf("orphaned")>=0) {
            if(oid!==crel) {
                update['rel'] = oid;
                update['url'] = prefix+'load_data/'+orel+'/?view=icon';
            } else {
                update['empty'] = true;
            }
        } else if(oid.indexOf("experimenter")<0) {
            if ($.inArray(orel, ["project", "screen"]) > -1) {
                update['empty'] = true;
            } else if($.inArray(orel, ["plate"]) > -1) {
                if (inst.is_leaf(selected)) {
                    update['rel'] = oid;
                    update['url'] = prefix+'load_data/'+orel+'/'+oid.split("-")[1]+'/';
                } else {
                    update['empty'] = true;
                }
            } else if($.inArray(orel, ["acquisition"]) > -1 && oid!==crel) {
                var plate = inst._get_parent(selected).attr('id').replace("-", "/");
                update['rel'] = oid;
                update['url'] = prefix+'load_data/'+plate+'/'+orel+'/'+oid.split("-")[1]+'/';
            } else if($.inArray(orel, ["dataset"]) > -1 && oid!==crel) {
                update['rel'] = oid;
                update['url'] = prefix+'load_data/'+orel+'/'+oid.split("-")[1]+'/?view=icon';
            } else if($.inArray(orel, ["share"]) > -1 && oid!==crel) {
                update['rel'] = oid;
                update['url'] = prefix+'load_public/'+oid.split("-")[1]+'/?view=icon';
            } else if($.inArray(orel, ["tag"]) > -1 && oid!==crel) {
                update['rel'] = oid;
                update['url'] = prefix+'load_tags/?view=icon&o_type=tag&o_id='+oid.split("-")[1];
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
            } 
        } else {
            update['empty'] = true;
        }
        
        if (update.rel!==null && update.url!==null){
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/common/image/spinner.gif"/></p>');
            $("div#content_details").attr('rel', update.rel);
            $("div#content_details").load(update.url, function() {
                syncPanels(selected);
            });
        } else if (update.empty){
            $("div#content_details").empty();
            $("div#content_details").removeAttr('rel');
        }
        
        syncPanels(selected); // update selected thumbs
    });

});

</script>