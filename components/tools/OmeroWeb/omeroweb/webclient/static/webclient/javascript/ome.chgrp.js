

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
        var sel = OME.get_tree_selection(),
            selImages = (sel.indexOf('Image') > -1),
            dtype = sel.split('=')[0],
            ids = sel.split('=')[1];
        dryRunTargetObjects = {};
        dryRunTargetObjects[dtype] = ids;
        $.get(webindex_url + "fileset_check/chgrp/?" + sel, function(html){
            html = $.trim(html);
            if($('div.split_fileset', html).length > 0) {
                $(html).appendTo($chgrpform);
                $('.chgrp_confirm_dialog .ui-dialog-buttonset button:nth-child(2) span').text("Move All");
                var filesetIds = [];
                $('input[name="fileset"]', html).each(function(){
                    filesetIds.push(parseInt($(this).val(), 10));
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
        var msg = "<p style='margin-bottom:0'><img alt='Loading' src='" + static_url + "/../webgateway/img/spinner.gif'> " +
                  "Checking which linked objects will be moved...</p>";
        var $dryRunSpinner = $(msg).appendTo($group_chooser);
        $group_chooser.append('<hr>');
        $.post(dryRunUrl, data, function(jobId){
            // keep polling for dry-run completion...
            var getDryRun = function() {
                var url = webindex_url + "activities_json/",
                    data = {'jobId': jobId};
                $.get(url, data, function(dryRunData) {
                    if (dryRunData.finished) {
                        // Handle chgrp errors by showing message...
                        if (dryRunData.error) {
                            var errMsg = dryRunData.error;
                            // More assertive error message
                            errMsg = errMsg.replace("may not move", "Cannot move");
                            var errHtml = "<img style='vertical-align: middle; position:relative; top:-3px' src='" +
                                static_url + "/../webgateway/img/failed.png'> ";
                            // In messages, replace Image[123] with link to image
                            var getLinkHtml = function(imageId) {
                                var id = imageId.replace("Image[", "").replace("]", "");
                                return "<a href='" + webindex_url + "?show=image-" + id + "'>" + imageId + "</a>";
                            };
                            errHtml += errMsg.replace(/Image\[([0-9]*)\]/g, getLinkHtml);
                            $dryRunSpinner.html(errHtml);
                            $okbtn.hide();
                            return;
                        }
                        var html = "<b style='font-weight: bold'>Move:</b> ",
                            move = [], count,
                            unlink = [], unlinked;
                        ["Projects", "Datasets", "Screens",
                         "Plates", "Wells", "Images"].forEach(function(otype){
                            if (otype in dryRunData.includedObjects) {
                                count = dryRunData.includedObjects[otype].length;
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
        $(".split_filesets_info", $chgrpform).remove();
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
                // If we have split filesets, first submission is to confirm 'Move All'?
                // We hide the split_filesets info panel and rename submit button to 'OK'
                if ($(".split_filesets_info .split_fileset", $chgrpform).length > 0 && $thisBtn.text() == 'Move All') {
                    $("#group_chooser").show();
                    $(".split_filesets_info", $chgrpform).hide();
                    $thisBtn.text('OK');
                    return false;
                }
                $chgrpform.submit();
            },
            "Cancel": function() {
                resetChgrpForm();
                // TODO - handle this in new jsTree. Reset to original selection if "Move All" has changed selection
                // var datatree = $.jstree._focused();
                // datatree.deselect_all();
                // datatree.reselect();        // revert to previous selection
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
        success: function(data) {
            var inst = $.jstree.reference('#dataTree');
            var remove = data.update.remove;
            var childless = data.update.childless;

            var removalClosure = [];
            var unremovedParentClosure;
            var removeType = function(type, ids) {
                $.each(ids, function(index, id) {
                    var removeLocated = inst.locate_node(type + '-' + id);
                    if (removeLocated) {
                        $.each(removeLocated, function(index, val) {
                            if (unremovedParentClosure !== undefined &&
                                val.id === unremovedParentClosure.id) {
                                // The new selection is also to be deleted, so select its parent
                                unremovedParentClosure = inst.get_node(inst.get_parent(val));
                            }
                            else if (inst.is_selected(val)) {
                                // This node was selected, mark its parent to be selected instead
                                unremovedParentClosure = inst.get_node(inst.get_parent(val));
                            }
                        // Accumulate nodes for deletion so the new selection can occur before delete
                        removalClosure.push(val);
                        });
                    }
                });
            };

            // Find and remove
            // This is done in a specific order so that the correct node can be selected
            var typeOrder = ['image', 'acquisition', 'dataset', 'plate', 'project', 'screen'];
            $.each(typeOrder, function(index, type) {
                if (remove.hasOwnProperty(type)) {
                    removeType(type, remove[type]);
                }
            });

            // Select the closest parent that was not part of the chgrp
            inst.deselect_all(true);
            inst.select_node(unremovedParentClosure);

            // Update the central panel in case chgrp removes an icon
            $.each(removalClosure, function(index, node) {
                inst.delete_node(node);
                var e = {'type': 'delete_node'};
                var data = {'node': node,
                            'old_parent': inst.get_parent(node)};
                update_thumbnails_panel(e, data);
            });

            function markChildless(ids, dtype) {
                $.each(ids, function(index, id) {
                    var childlessLocated = inst.locate_node(property + '-' + id);
                    // If some nodes were found, make them childless
                    if (childlessLocated) {
                        $.each(childlessLocated, function(index, node) {
                            node.state.loaded = true;
                            inst.redraw_node(node);
                        });

                    }
                });
            }

            // Find and mark childless
            for (var property in childless) {
                if (childless.hasOwnProperty(property)) {
                    markChildless(childless[property], property);
                }

            }

            OME.showActivities();
        }
    });

});
