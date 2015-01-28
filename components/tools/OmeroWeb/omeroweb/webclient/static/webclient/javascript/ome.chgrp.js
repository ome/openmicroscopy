

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

    var webindex_url,
        data_owners,
        chgrp_type,
        $chgrpform = $("#chgrp-form");


    // external entry point, called by jsTree right-click menu
    window.OME.handleChgrp = function(webindex, static_url) {
        webindex_url = webindex;
        // gid, gname, oid
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
        $.getJSON(url, function(data){
            data_owners = data.owners;  // save for later
            var ownernames = [];
            for (var o=0; o<data.owners.length; o++) {ownernames.push(data.owners[o][1]);}
            var headerTxt = "<p>Move data owned by " + ownernames.join(", ") + " to Group...</p>";
            $chgrpform.append(headerTxt);

            // List the target groups...
            var html = "";
            for (var i=0; i<data.groups.length; i++){
                var g = data.groups[i];
                html += "<div class='chgrpGroup' data-gid='"+ g.id + "'>";
                html += "<img src='" + permsIcon(g.perms) + "'/>";
                html += g.name + "<hr></div>";
            }
            // If no target groups found...
            if (data.groups.length === 0) {
                html = "<hr><p>No target groups found</p><hr>";
                if (data.owners.length === 1) {
                    html += "Owner of the data may only be in 1 group.";
                } else {
                    html += "Owners of the data may only be in 1 group,";
                    html += "or they are not all in any common groups to move data to.";
                }
            }
            $chgrpform.append(html);
        });
    };


    var checkFilesetSplit = function checkFilesetSplit () {
        // Check if chgrp will attempt to Split a Fileset. Hidden until user hits 'OK'
        $("#move_group_tree").hide();               // hide tree while we wait...
        $.jstree._focused().save_selected();        // 'Cancel' will roll back to this
        $.get(webindex_url + "fileset_check/chgrp?" + OME.get_tree_selection(), function(html){
            if($('div.split_fileset', html).length > 0) {
                $(html).appendTo($chgrpform);
                $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(1) span').text("Move All");
                var filesetId = $('input[name="fileset"]', html).val();     // TODO - handle > 1 filesetId
                if (chgrp_type == "Image") {
                    OME.select_fileset_images(filesetId);
                }
            } else {
                $("#move_group_tree").show();
            }
        });
    };


    // Handle clicking on specific group in chgrp dialog...
    $chgrpform.on( "click", ".chgrpGroup", function() {

        var $this = $(this),
            gid = $this.attr('data-gid'),
            chgrp_target_url = webindex_url + "load_chgrp_target/" + gid,
            dtype,
            dids;

        $(".chgrpGroup").remove();
        $chgrpform.append($this);

        // Add input to include 'group_id' in the POST data
        $("<input name='group_id' value='"+ gid +"'/>")
                .appendTo($chgrpform).addClass('removeMe').hide();

        // Add group & selected items to chgrp form
        var selobjs = OME.get_tree_selection().split("&");  // E.g. Image=1,2&Dataset=3
        for (var i = 0; i < selobjs.length; i++) {
            dtype = selobjs[i].split("=")[0];
            dids = selobjs[i].split("=")[1];
            $("<input name='"+ dtype +"' value='"+ dids +"'/>")
                .appendTo($chgrpform).addClass('removeMe').hide();
        }

        chgrp_type = dtype;     // This will be the dtype of last object
        var target_type;
        if (chgrp_type == "Dataset") target_type = "project";
        else if (chgrp_type == "Image") target_type = "dataset";
        else if (chgrp_type == "Plate") target_type = "screen";
        chgrp_target_url += "/"+target_type+"/";
        chgrp_target_url += "?owner=" + data_owners[0][0];  // ID of the (first) owner

        $("<div id='move_group_tree'></div>").appendTo($chgrpform);

        if (chgrp_type == "Project" || chgrp_type == "Screen") {
            $("#move_group_tree").html("<h1>"+ chgrp_type.capitalize() +" will be moved to group: " + gname +"</h1>");
        } else {
            // we load a tree - then give it basic selection / expansion behaviour. jsTree would have been overkill!?
            $("#move_group_tree").load(chgrp_target_url, function(){
                var node_click = function(){
                    $("#move_group_tree a").removeClass("jstree-clicked");
                    // only allow selection of correct nodes
                    if ($(this).parent().attr('rel') == target_type) {
                        $("a" ,$(this).parent()).addClass("jstree-clicked");
                    }
                    // toggle any children
                    $("ul" ,$(this).parent()).toggle();
                };
                $("#move_group_tree a").click(node_click);
                $("#move_group_tree ins").click(node_click);
            });
        }

        checkFilesetSplit();
    });



    // After we edit the chgrp dialog to handle Filesets, we need to clean-up
    var resetChgrpForm = function() {
        $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(1) span').text("OK");
        $("#move_group_tree").show();
        $("#chgrp_split_filesets").remove();
    };

    // set-up the dialog
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
                $chgrpform.submit();
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
    $chgrpform.ajaxForm({
        beforeSubmit: function(data){
            $chgrpform.dialog("close");
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

});
