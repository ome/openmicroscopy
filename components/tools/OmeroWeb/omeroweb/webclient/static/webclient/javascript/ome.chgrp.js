

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
        static_url,
        data_owners,
        chgrp_type,
        target_type,
        // gets populated with selected objects and possibly also
        // Filesets IDs if filesets would be split.
        dryRunTargetObjects,
        $chgrpform = $("#chgrp-form"),
        $group_chooser,
        $move_group_tree,
        $newbtn,
        $okbtn;


    // external entry point, called by jsTree right-click menu
    window.OME.handleChgrp = function(webindex, staticurl) {
        webindex_url = webindex;
        static_url = staticurl;
        // gid, gname, oid
        $chgrpform.dialog({"title": "Move to Group",
            height: 450,
            width: 400});
        $chgrpform.dialog('open');
        $chgrpform.empty();

        // Containers to handle everything after fileset check
        $group_chooser = $("<div id='group_chooser'></div>").appendTo($chgrpform);
        $move_group_tree = $("<div id='move_group_tree'></div>").appendTo($chgrpform);

        // first we check filesets...
        checkFilesetSplit();

        //...while we load groups
        // Need to find which groups we can move selected objects to.
        // Object owner must be member of target group.
        var url = webindex_url + "load_chgrp_groups/?" + OME.get_tree_selection();
        $.getJSON(url, function(data){
            data_owners = data.owners;  // save for later
            var ownernames = [];
            for (var o=0; o<data.owners.length; o++) {ownernames.push(data.owners[o][1]);}
            var headerTxt = "<p>Move data owned by " + ownernames.join(", ") + ".</p>" +
                            "<h1>Please choose target group below:</h1>";
            $group_chooser.append(headerTxt);

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
            $group_chooser.append(html);
        });
    };

    var permsIcon = function(perms) {
        if (perms.write) return static_url + "/image/group_green16.png";
        if (perms.annotate) return static_url + "/image/group_orange16.png";
        if (perms.read) return static_url + "/image/group_red16.png";
        return static_url + "/image/personal16.png";
    };

    var checkFilesetSplit = function checkFilesetSplit () {
        // Check if chgrp will attempt to Split a Fileset. Hidden until user hits 'OK'
        $group_chooser.hide();                      // hide group_chooser while we wait...
        $.jstree._focused().save_selected();        // 'Cancel' will roll back to this
        var sel = OME.get_tree_selection(),
            selImages = (sel.indexOf('Image') > -1);
            dtype = sel.split('=')[0];
        var ids = sel.split('=')[1];
        dryRunTargetObjects = {};
        dryRunTargetObjects[dtype] = ids;
        $.get(webindex_url + "fileset_check/chgrp/?" + sel, function(html){
            if($('div.split_fileset', html).length > 0) {
                $(html).appendTo($chgrpform);
                $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(2) span').text("Move All");
                var filesetIds = [];
                $('input[name="fileset"]', html).each(function(){
                    filesetIds.push($(this).val());
                });
                if (selImages) {
                    OME.select_fileset_images(filesetIds);
                }
                dryRunTargetObjects['Fileset'] = filesetIds.join(",");
            } else {
                $group_chooser.show();
            }
        });
    };

    // We do a chgrp 'dryRun' to check for loss of annotations etc.
    var dryRun = function(targetObjects, groupId) {
        var dryRunUrl = webindex_url + "chgrpDryRun/",
            data = $.extend({}, targetObjects, {'group_id': groupId});
        // Show message and start dry-run
        var msg = "<p><img alt='Loading' src='" + static_url + "/../webgateway/img/spinner.gif'> " +
                  "Checking which linked objects will be moved...</p>";
        var $dryRunSpinner = $(msg).appendTo($group_chooser);
        $.post(dryRunUrl, data, function(jobId){
            // keep polling for dry-run completion...
            var getDryRun = function() {
                var url = webindex_url + "activities_json/",
                    data = {'jobId': jobId};
                $.get(url, data, function(dryRunData) {
                    if (dryRunData.finished) {
                        // Handle chgrp errors by showing message...
                        if (dryRunData.error) {
                            var errHtml = "<img style='vertical-align: middle; position:relative; top:-3px' src='" +
                                static_url + "/../webgateway/img/failed.png'> ";
                            // In messages, replace Image[123] with link to image
                            var getLinkHtml = function(imageId) {
                                var id = imageId.replace("Image[", "").replace("]", "");
                                return "<a href='" + webindex_url + "?show=image-" + id + "'>" + imageId + "</a>";
                            };
                            errHtml += dryRunData.error.replace(/Image\[([0-9]*)\]/g, getLinkHtml);
                            $dryRunSpinner.html(errHtml);
                            $okbtn.hide();
                            return;
                        }
                        var html = "<b style='font-weight: bold'>Move:</b> ",
                            move = [], count,
                            unlink = [], unlinked;
                        ["Projects", "Datasets", "Screens",
                         "Plates", "Wells", "Images"].forEach(function(otype){
                            if (otype in dryRunData.includedDetails) {
                                count = dryRunData.includedDetails[otype].length;
                                if (count === 1) otype = otype.slice(0, -1);  // remove s
                                move.push(count + " " + otype);
                            }
                        });
                        html += move.join(", ");

                        ["Datasets", "Plates", "Images", "Tags", "Files"].forEach(function(otype){
                            if (otype in dryRunData.unlinkedDetails) {
                                unlinked = dryRunData.unlinkedDetails[otype];
                                count = unlinked.length;
                                if (count === 0) return;
                                if (count === 1) otype = otype.slice(0, -1);  // remove s
                                var namesList = [], names;
                                unlinked.forEach(function(u){
                                    namesList.push(u.name);
                                });
                                names = namesList.join(", ");
                                names = " <i title='" + namesList.join("\n") + "'>(" + names.slice(0, 40) + (names.length > 40 ? "..." : "") + ")</i>";
                                unlink.push(count + " " + otype + names);
                            }
                        });
                        if (dryRunData.unlinkedDetails.Comments > 0) {
                            count = dryRunData.unlinkedDetails.Comments;
                            unlink.push(count + " Comment" + (count > 1 ? "s" : ""));
                        }
                        if (dryRunData.unlinkedDetails.Others > 0) {
                            count = dryRunData.unlinkedDetails.Others;
                            unlink.push(count + " Other" + (count > 1 ? "s" : ""));
                        }
                        if (unlink.length > 0) {
                            html += "<br><b style='font-weight: bold'>Not included:</b> " + unlink.join(", ");
                        }
                        $dryRunSpinner.html(html);
                    } else {
                        // try again...
                        setTimeout(getDryRun, 200);
                    }
                });
            };
            getDryRun();
        });
    };

    // Called from "New..." button, simply add input to form (and hide tree)
    var newContainer = function newContainer() {

        if ($("input[name='new_container_name']", $chgrpform).length > 0) {
            return;     // already clicked 'New'!
        }
        var ownerId = data_owners[0][0];
        $move_group_tree.hide();
        $("<p>New " + target_type.capitalize() + " name: <input name='new_container_name'/></p>")
                .appendTo($chgrpform).click();
        // Hidden input
        $("<input name='new_container_type' value='" + target_type + "'/>")
                .appendTo($chgrpform).hide();
    };


    // Handle clicking on specific group in chgrp dialog...
    $chgrpform.on( "click", ".chgrpGroup", function() {

        var $this = $(this),
            gid = $this.attr('data-gid'),
            chgrp_target_url = webindex_url + "load_chgrp_target/" + gid,
            dtype,
            dids;

        // Remove all groups (except the chosen one)
        $(".chgrpGroup").remove();
        $group_chooser.append($this);

        // Add hidden inputs to include 'group_id' in the POST data
        $("<input name='group_id' value='"+ gid +"'/>")
                .appendTo($chgrpform).hide();

        // Add group & selected items to chgrp form
        var selobjs = OME.get_tree_selection().split("&");  // E.g. Image=1,2&Dataset=3
        for (var i = 0; i < selobjs.length; i++) {
            dtype = selobjs[i].split("=")[0];
            dids = selobjs[i].split("=")[1];
            $("<input name='"+ dtype +"' value='"+ dids +"'/>")
                .appendTo($chgrpform).hide();
        }

        chgrp_type = dtype;     // This will be the dtype of last object
        if (chgrp_type == "Dataset") target_type = "project";
        else if (chgrp_type == "Image") target_type = "dataset";
        else if (chgrp_type == "Plate") target_type = "screen";
        chgrp_target_url += "/"+target_type+"/";
        chgrp_target_url += "?owner=" + data_owners[0][0];  // ID of the (first) owner


        if (chgrp_type == "Project" || chgrp_type == "Screen") {
            // Don't need to show anything
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
                $newbtn.show();
            });
        }

        // Now we know target group, can do dry-run to check lost annotations etc...
        dryRun(dryRunTargetObjects, gid);
    });


    // After we edit the chgrp dialog to handle Filesets, we need to clean-up
    var resetChgrpForm = function() {
        $('span', $okbtn).text("OK");
        $okbtn.show();
        $newbtn.hide();
        $("#move_group_tree").show();
        $("#chgrp_split_filesets").remove();
    };

    // set-up the dialog
    $chgrpform.dialog({
        dialogClass: 'chgrp_confirm_dialog',
        autoOpen: false,
        resizable: true,
        height: 350,
        width:520,
        modal: true,
        buttons: {
            "New...": function() {
                newContainer();
            },
            "OK": function() {
                var $thisBtn = $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(2) span');
                // If we have split filesets, on the first click 'OK', we ask 'Move All'?
                if ($("#chgrp_split_filesets .split_fileset").length > 0 && $thisBtn.text() == 'Move All') {
                    $("#group_chooser").show();
                    $("#chgrp_split_filesets").hide();
                    $thisBtn.text('OK');
                    return false;
                }
                $chgrpform.submit();
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

    $newbtn = $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(1)');
    $newbtn.hide();
    $okbtn = $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(2)');

    // handle chgrp 
    $chgrpform.ajaxForm({
        beforeSubmit: function(data, $form){
            // Don't submit if we haven't populated the form with group etc.
            if (data.length === 0) {
                OME.alert_dialog("Please choose target group.");
                return false;
            }
            if ($("input[name='group_id']", $form).length === 0) return false;
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
