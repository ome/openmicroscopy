

  // Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
  // All rights reserved.

  // This program is free software: you can redistribute it and/or modify
  // it under the terms of the GNU Affero General Public License as
  // published by the Free Software Foundation, either version 3 of the
  // License, or (at your option) any later version.

  // This program is distributed in the hope that it will be useful,
  // but WITHOUT ANY WARRANTY; without even the implied warranty of
  // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  // GNU Affero General Public License for more details.

  // You should have received a copy of the GNU Affero General Public License
  // along with this program.  If not, see <http://www.gnu.org/licenses/>.


$(function() {

    if (typeof window.OME === "undefined") { window.OME={}; }

    var webindex_url;

    // Handle clicking on specific group in chgrp dialog...
    $( "#chgrp-form" ).on( "click", ".chgrpGroup", function() {

        var $this = $(this),
            gid = $this.attr('data-gid'),
            chgrp_target_url = webindex_url + "load_chgrp_target/" + gid;

        $(".chgrpGroup").remove();
        $("#chgrp-form").append($this);
    });


    window.OME.handleChgrp = function(webindex, static_url) {
        webindex_url = webindex;
        // gid, gname, oid
        var $chgrpform = $("#chgrp-form");
        $chgrpform.dialog({"title": "Move to Group",
            height: 450,
            width: 400});
        $chgrpform.dialog('open');
        $chgrpform.empty();

        var permsIcon = function(perms) {
            if (perms.write) return static_url + "/image/group_green16.png";
            if (perms.annotate) return static_url + "/image/group_orange16.png";
            if (perms.read) return static_url + "/image/group_red16.png";
            return static_url + "/image/personal16.png";
        };

        // Need to find which groups we can move selected objects to.
        // Object owner must be member of target group.
        var url = webindex_url + "load_chgrp_groups?" + OME.get_tree_selection();
        console.log(url);
        $.getJSON(url, function(data){
            var headerTxt = "<p>Move data owned by " + data.owners.join(", ") + " to Group...</p>";
            $chgrpform.append(headerTxt);

            var html = "";
            for (var i=0; i<data.groups.length; i++){
                var g = data.groups[i];
                html += "<div class='chgrpGroup' data-gid='"+ g.id + "'>";
                html += "<img src='" + permsIcon(g.perms) + "'/>";
                html += g.name + "<hr></div>";
            }
            $chgrpform.append(html);
        });

        // // Add group & selected items to chgrp form
        // $('input.removeMe', $chgrpform).remove();   // cleanup (horrible!)
        // var selobjs = OME.get_tree_selection().split("&");  // E.g. Image=1,2&Dataset=3
        // for (var i=0;i<selobjs.length;i++) {
        //     var dtype = selobjs[i].split("=")[0],
        //         dids = selobjs[i].split("=")[1];
        //     $("<input name='"+ dtype +"' value='"+ dids +"'/>")
        //         .appendTo($chgrpform).addClass('removeMe').hide();
        // }

        // var chgrp_target_url = "{% url 'webindex' %}load_chgrp_target/"+gid;
        // var chgrp_type = oid.split("-")[0];
        // var target_type;
        // if (chgrp_type == "dataset") target_type = "project";
        // else if (chgrp_type == "image") target_type = "dataset";
        // else if (chgrp_type == "plate") target_type = "screen";
        // chgrp_target_url += "/"+target_type+"/";

        // if (chgrp_type == "project" || chgrp_type == "screen") {
        //     $("#move_group_tree").html("<h1>"+ chgrp_type.capitalize() +" will be moved to group: " + gname +"</h1>");
        // } else {
        //     // we load a tree - then give it basic selection / expansion behaviour. jsTree would have been overkill!?
        //     $("#move_group_tree").load(chgrp_target_url, function(){
        //         var node_click = function(){
        //             $("#move_group_tree a").removeClass("jstree-clicked");
        //             // only allow selection of correct nodes
        //             if ($(this).parent().attr('rel') == target_type) {
        //                 $("a" ,$(this).parent()).addClass("jstree-clicked");
        //             }
        //             // toggle any children
        //             $("ul" ,$(this).parent()).toggle();
        //         };
        //         $("#move_group_tree a").click(node_click);
        //         $("#move_group_tree ins").click(node_click);
        //     });
        // }

        // // Check if chgrp will attempt to Split a Fileset. Hidden until user hits 'OK'
        // $("#move_group_tree").hide();               // hide tree while we wait...
        // $.jstree._focused().save_selected();        // 'Cancel' will roll back to this
        // $.get("{% url 'fileset_check' 'chgrp' %}?" + OME.get_tree_selection(), function(html){
        //     if($('div.split_fileset', html).length > 0) {
        //         $(html).appendTo($chgrpform)
        //         $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(1) span').text("Move All");
        //         var filesetId = $('input[name="fileset"]', html).val();     // TODO - handle > 1 filesetId
        //         if (chgrp_type == "image") {
        //             OME.select_fileset_images(filesetId);
        //         }
        //     } else {
        //         $("#move_group_tree").show();
        //     }
        // });
    };


    // After we edit the chgrp dialog to handle Filesets, we need to clean-up
    var resetChgrpForm = function() {
        $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(1) span').text("OK");
        $("#move_group_tree").show();
        $("#chgrp_split_filesets").remove();
    };

    var $chgrpform = $("#chgrp-form");
    $chgrpform.dialog({
        dialogClass: 'chgrp_confirm_dialog',
        autoOpen: false,
        resizable: true,
        height: 310,
        width:420,
        modal: true,
        buttons: {
            "OK": function() {
                var $thisBtn = $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(1) span');
                // If we have split filesets, on the first click 'OK', we ask 'Move All'?
                if ($("#chgrp_split_filesets .split_fileset").length > 0) {
                    if ($thisBtn.text() == 'Move All') {
                        $("#move_group_tree").show();
                        $("#chgrp_split_filesets").hide();
                        $thisBtn.text('OK');
                        return false;
                    }
                }
                $("#chgrp-form").submit();
                resetChgrpForm();
            },
            "Cancel": function() {
                resetChgrpForm();
                var datatree = $.jstree._focused();
                datatree.deselect_all();
                datatree.reselect();        // revert to previous selection
                $( this ).dialog( "close" );
            }
        }
    });
    // handle chgrp 
    $("#chgrp-form").ajaxForm({
        beforeSubmit: function(data){
            $("#chgrp-form").dialog("close");
            var chgrp_target = $("#move_group_tree a.jstree-clicked");
            if (chgrp_target.length == 1){
                data.push({'name':'target_id', 'value': chgrp_target.parent().attr('id')});
            }
        },
        success: function() {
            OME.showActivities();
            var selected = $.jstree._focused().get_selected();
            $("#dataTree").jstree('remove', selected);
        }
    });
    // chgrp form behavior
    $("#chgrp-form .group_option").click(function(){
        $("#chgrp-form .group_option").removeClass('selected');
        $(this).addClass('selected');
        $("input[type='radio']", $(this)).prop('checked', true);
    });
    $("#chgrp-form .group_option :first").click();  // select first option

});
