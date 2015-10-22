//
// Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
// All rights reserved.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

// Events
// 1) Item removed from the view (either deleted/moved in the tree or in the centre)
// 2) Item added to the view (either created/copied/moved in the tree or in the centre)
// 3) Selection changed in the tree or in the centre

/*global OME:true */
if (typeof OME === "undefined") {
    OME = {};
}

OME.multi_key = function() {
    if (navigator.appVersion.indexOf("Mac")!=-1) {
        return "meta";
    } else {
        return "ctrl";
    }
};

OME.getURLParameter = function(key) {
    /* Return single value for parameter with specified key
     * Does not handle multi-value parameters
     * Returns false if there are no parameters or it is not present
    */

    // If there are no parameters, just return false
    if (window.location.search.length === 0) {
        return false;
    }

    // Remove the leading '?'
    var search = window.location.search.substring(1);

    // Break them up
    var searchParams = search.split('&');

    for (var i = 0; i < searchParams.length; i++) {
        var paramSplit = searchParams[i].split('=');
        if (paramSplit[0] === key) {
            return paramSplit[1];
        }
    }
    return false;
};

jQuery.fn.hide_if_empty = function() {
    if ($(this).children().length === 0) {
        $(this).hide();
    } else {
        $(this).show();
    }
  return this;
};

// Function to enable measuring of a specific text element in jquery
$.fn.textWidth = function(text, font) {
    if (!$.fn.textWidth.fakeEl) $.fn.textWidth.fakeEl = $('<span>').appendTo(document.body);
    var htmlText = text || this.val() || this.text();
    htmlText = $.fn.textWidth.fakeEl.text(htmlText).html(); //encode to Html
    htmlText = htmlText.replace(/\s/g, "&nbsp;"); //replace trailing and leading spaces
    $.fn.textWidth.fakeEl.html(htmlText).css('font', font || this.css('font'));
    return $.fn.textWidth.fakeEl.width();
};

// called from OME.tree_selection_changed() below
OME.handle_tree_selection = function(data, event) {

    var selected;
    if (typeof data != 'undefined') {
        selected = data.selected;
    }

    // Update the DOM recorded selection
    OME.writeSelectedObjs(selected);

    // Trigger selection changed event
    $("body").trigger("selection_change.ome", data);

    // Instead of using selection_change.ome to trigger syncThumbSelection
    // and update_thumbnails_panel, just run them instead

    // Check the functions exist, they might not if the central panel has not been loaded
    if (typeof(syncThumbSelection) === "function") {
        // safe to use the function
        syncThumbSelection(data, event);
    }
    if (typeof(update_thumbnails_panel) === "function") {
        // safe to use the function
        update_thumbnails_panel(event, data);
    }
};

// called on selection and deselection changes in jstree
OME.tree_selection_changed = function(data, evt) {
    // handle case of deselection immediately followed by selection - Only fire on selection
    if (typeof OME.select_timeout != 'undefined') {
        clearTimeout(OME.select_timeout);
    }
    OME.select_timeout = setTimeout(function() {
        OME.handle_tree_selection(data, evt);
    }, 10);
};

// Short-cut to setting selection to [], with option to force refresh.
// (by default, center panel doesn't clear when nothing is selected)
OME.clear_selected = function(force_refresh) {
    var refresh = (force_refresh === true);
    $("body")
        .data("selected_objects.ome", [])
        .trigger("selection_change.ome", [refresh]);
};

// select all images from the specified fileset (if currently visible)
OME.select_fileset_images = function(filesetIds) {
    // This is only used for chgrp of filesets to select them all
    // It only selects images that have been loaded in the tree,
    // in preparation for removing them from the tree on "OK".
    // However, it will not update child counts on datasets that
    // have not been loaded in tree.
    var datatree = $.jstree.reference('#dataTree');
    filesetIds.forEach(function(fsId){
        $("#dataTree li").each(function(){
            var node = datatree.get_node(this),
                fsId = node.data.obj.filesetId;
            if (filesetIds.indexOf(fsId) > -1) {
                datatree.select_node(node);
            }
        });
    });
};

// actually called when share is edited, to refresh right-hand panel
OME.share_selection_changed = function(share_id) {
    $("body").trigger("selection_change.ome");
};


// Standard ids are in the form TYPE-ID, web extensions may add an
// additional -SUFFIX
OME.table_selection_changed = function($selected) {
    // This is for search and such where there is no tree
    var selected_objs = [];
    if (typeof $selected != 'undefined') {
        $selected.each(function(i){
            var id_split = this.id.split('-');
            var id_obj = id_split.slice(0, 2).join('-');
            var id_suffix = id_split.slice(2).join('-');
            selected_objs.push( {"id":id_obj, "id_suffix":id_suffix} );
        });
    }
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
};

// handles selection for 'clicks' on table (search, history & basket)
// including multi-select for shift and meta keys
OME.handleTableClickSelection = function(event) {

    var $clickedRow = $(event.target).parents('tr:first');
    var rows = $("table#dataTable tbody tr");
    var selIndex = rows.index($clickedRow.get(0));

    if ( event.shiftKey ) {
        // get existing selected items
        var $s = $("table#dataTable tbody tr.ui-selected");
        if ($s.length === 0) {
            $clickedRow.addClass("ui-selected");
            OME.table_selection_changed($clickedRow);
            return;
        }
        var sel_start = rows.index($s.first());
        var sel_end = rows.index($s.last());

        // select all rows between new and existing selections
        var new_start, new_end;
        if (selIndex < sel_start) {
            new_start = selIndex;
            new_end = sel_start;
        } else if (selIndex > sel_end) {
            new_start = sel_end+1;
            new_end = selIndex+1;
        // or just from the first existing selection to new one
        } else {
            new_start = sel_start;
            new_end = selIndex;
        }
        for (var i=new_start; i<new_end; i++) {
            rows.eq(i).addClass("ui-selected");
        }
    }
    else if (event.metaKey) {
        if ($clickedRow.hasClass("ui-selected")) {
            $clickedRow.removeClass("ui-selected");
        }
        else {
            $clickedRow.addClass("ui-selected");
        }
    }
    else {
        rows.removeClass("ui-selected");
        $clickedRow.addClass("ui-selected");
    }
    // update right hand panel etc
    OME.table_selection_changed($("table#dataTable tbody tr.ui-selected"));
};

// called from click events on plate. Selected wells
OME.well_selection_changed = function($selected, well_index, plate_class) {

    var selected_objs = [];
    $selected.each(function(i){
        selected_objs.push( {"id":$(this).attr('id').replace("=","-"),
                "rel":$(this).attr('rel'),
                "index":well_index,
                "class":plate_class} );     // assume every well has same permissions as plate
    });

    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
};


// handle deleting of Tag, File, Comment
// on successful delete via AJAX, the parent .domClass is hidden
OME.removeItem = function(event, domClass, url, parentId, index) {
    var removeId = $(event.target).attr('id');
    var dType = removeId.split("-")[1]; // E.g. 461-comment
    // /webclient/action/remove/comment/461/?parent=image-257
    var $parent = $(event.target).parents(domClass);
    var $annContainer = $parent.parent();
    var r = 'Remove ';
    if (dType === 'comment') r = 'Delete ';
    var confirm_remove = OME.confirm_dialog(r + dType + '?',
        function() {
            if(confirm_remove.data("clicked_button") == "OK") {
                $.ajax({
                    type: "POST",
                    url: url,
                    data: {'parent':parentId, 'index':index},
                    dataType: 'json',
                    success: function(r){
                        if(eval(r.bad)) {
                            OME.alert_dialog(r.errs);
                        } else {
                            // simply remove the item (parent class div)
                            //console.log("Success function");
                            $parent.remove();
                            $annContainer.hide_if_empty();
                        }
                    }
                });
            }
        }
    );
    return false;
};

OME.deleteItem = function(event, domClass, url) {
    var deleteId = $(event.target).attr('id');
    var dType = deleteId.split("-")[1]; // E.g. 461-comment
    // /webclient/action/delete/file/461/?parent=image-257
    var $parent = $(event.target).parents("."+domClass);
    var $annContainer = $parent.parent();
    var confirm_remove = OME.confirm_dialog('Delete '+ dType + '?',
        function() {
            if(confirm_remove.data("clicked_button") == "OK") {
                $.ajax({
                    type: "POST",
                    url: url,
                    dataType:'json',
                    success: function(r){
                        if(eval(r.bad)) {
                            OME.alert_dialog(r.errs);
                        } else {
                            // simply remove the item (parent class div)
                            $parent.remove();
                            $annContainer.hide_if_empty();
                            window.parent.OME.refreshActivities();
                        }
                    }
                });
            }
        }
    );
    event.preventDefault();
    return false;
};

// Used to filter annotations in the metadata_general and batch_anntotate panels.
// Assumes a single #annotationFilter select on the page.
OME.filterAnnotationsAddedBy = function() {
    var $this = $("#annotationFilter"),
        val = $this.val(),
        userId = $this.attr('data-userId');

    // select made smaller if only 'Show all' text
    if (val === "all") {
        $this.css('width', '80px');
    } else {
        $this.css('width', '180px');
    }

    $('.tag_annotation_wrapper, .keyValueTable, .file_ann_wrapper, .ann_comment_wrapper, #custom_annotations tr')
            .each(function() {
        var $ann = $(this),
            addby = $ann.attr('data-added-by').split(",");
        var show = false;
        switch (val) {
            case "me":
                show = ($.inArray(userId, addby) > -1);
                break;
            case "others":
                for (var i=0; i<addby.length; i++) {
                    if (addby[i] !== userId) {
                        show = true;
                    }
                }
                break;
            default:    // 'all'
                show = true;
        }
        if (show) {
            $ann.show();
        } else {
            $ann.hide();
        }
    });
};

// More code that is shared between metadata_general and batch_annotate panels
// Called when panel loaded. Does exactly what it says on the tin.
OME.initToolbarDropdowns = function() {
    // -- Toolbar buttons - show/hide dropdown options --
    $(".toolbar_dropdown ul").css('visibility', 'hidden');
    // show on click
    var $toolbar_dropdownlists = $(".toolbar_dropdown ul");
    $(".toolbar_dropdown button").click(function(e) {
        // hide any other lists that might be showing...
        $toolbar_dropdownlists.css('visibility', 'hidden');
        // then show this one...
        $("ul", $(this).parent()).css('visibility', 'visible');
        e.preventDefault();
        return false;
    });
    // on hover-out, hide drop-down menus
    $toolbar_dropdownlists.hover(function(){}, function(){
        $(this).css('visibility', 'hidden');
    });

    // For Figure scripts, we need a popup:
    $("#figScriptList li a").click(function(event){
        if (!$(this).parent().hasClass("disabled")) {
            OME.openScriptWindow(event, 800, 600);
        }
        event.preventDefault();
        return false;
    });
};

// Simply add query to thumbnail src to force refresh.
// By default we do ALL thumbnails, but can also specify ID
OME.refreshThumbnails = function(options) {
    options = options || {};
    var rdm = Math.random(),
        // thumbs_selector = "#dataIcons img",
        search_selector = ".search_thumb",
        spw_selector = "#spw img";
    // handle Dataset thumbs, search rusults and SPW thumbs
    if (options.imageId) {
        // thumbs_selector = "#image_icon-" + options.imageId + " img";
        search_selector = "#image-" + options.imageId + " img.search_thumb";
        spw_selector += "#image-" + options.imageId;
    }
    // Try SPW data or Search data by directly updating thumb src...
    var $thumbs = $(spw_selector + ", " + search_selector);
    if ($thumbs.length > 0){
        $thumbs.each(function(){
            var $this = $(this),
                base_src = $this.attr('src').split('?')[0];
            $this.attr('src', base_src + "?_="+rdm);
        });
    } else if (window.update_thumbnails_panel) {
        // ...Otherwise update thumbs via jsTree
        // (avoids revert of src on selection change)
        var type = 'refreshThumbnails',
            data = {};
        if (options.imageId) {
            type = "refreshThumb";
            data = {'imageId': options.imageId};
        }
        var e = {'type': type};
        update_thumbnails_panel(e, data);
    }

    // Update viewport via global variable
    if (!options.ignorePreview && OME.preview_viewport && OME.preview_viewport.loadedImg.id) {
        OME.preview_viewport.load(OME.preview_viewport.loadedImg.id);
    }
};


OME.truncateNames = (function(){
    var insHtml;
    // Resizing of left panel dynamically truncates image names
    // NB: no images loaded when page first laods. Do everything on resize...
    var truncateNames = function() {
        if (!insHtml) {
            // use the first image to get the html
            var $ins = $('.jstree li[rel^="image"] a ins').first();
            if ($ins.length > 0) {
                insHtml = $ins.get(0).outerHTML;
            }
        }
        // get the panel width, and number of chars that will fit
        var lp_width = $("#left_panel").width() - 20;  // margin
        // Go through all images, truncating names...
        // When we find matching size for a name length, save it...
        var maxChars;
        $('.jstree li[rel^="image"] a').each(function(){
            var $this = $(this),
                ofs = $this.offset(),
                name = $this.attr('data-name'),
                truncatedName,
                chars = name.length;
            name = name.escapeHTML();
            // if we know maxChars and we're longer than that...
            if (maxChars && name.length > maxChars) {
                chars = maxChars;
                truncatedName = "..." + name.slice(-chars);
                $this.html(insHtml + truncatedName);
            } else {
                // if needed, trim the full name until it fits and save maxChars
                $this.html(insHtml + name);
                var w = $this.width() + ofs.left;
                while (w > lp_width && chars > 2) {
                    chars = chars-2;
                    truncatedName = "..." + name.slice(-chars);
                    $this.html(insHtml + truncatedName);
                    w = $this.width() + ofs.left;
                    maxChars = chars;
                }
            }
        });
    };
    return truncateNames;
}());

// Handle deletion of selected objects in jsTree in container_tags.html and containers.html
OME.handleDelete = function(deleteUrl, filesetCheckUrl, userId) {
    var datatree = $.jstree.reference($('#dataTree'));
    var selected = datatree.get_selected(true);

    var del_form = $( "#delete-dialog-form" );
    del_form.dialog( "open" )
        .removeData("clicked_button");
    // clear previous stuff from form
    $.removeData(del_form, "clicked_button");
    $("#delete_contents_form").show();
    del_form.unbind("dialogclose");
    del_form.find("input[type='checkbox']").prop('checked', false);

    // set up form - process all the objects for data-types and children
    var ajax_data = [];
    var askDeleteContents = false;
    var dtypes = {};
    // Parent to select after deletion
    var firstParent = datatree.get_node(datatree.get_parent(selected[0]));

    var disabledNodes = [];

    function traverse(state) {
        // Check if this state is one that we are looking for
        var n = datatree.get_node(state);
        disabledNodes.push(n);

        if (n.children) {
            $.each(n.children, function(index, child) {
                 traverse(child);
            });
        }
        datatree.disable_node(n);

    }
    var notOwned = false;
    $.each(selected, function(index, node) {
        // Add the nodes that are to be deleted
        ajax_data.push(node.type + '=' + node.data.obj.id);
        // What types are being deleted and how many (for pluralization)
        var dtype = node.type;
        if (dtype in dtypes) {
            dtypes[dtype] += 1;
        } else {
            dtypes[dtype] = 1;
        }
        // If the node type is not 'image' then ask about deleting contents
        if (!askDeleteContents && node.type != 'image') {
            askDeleteContents = true;
        }
        if (node.data.obj.ownerId !== userId) {
            notOwned = true;
        }

        // Disable the nodes marked for deletion
        // Record them so they can easily be removed/re-enabled later
        disabledNodes.push(node);
        if (node.children) {
            $.each(node.children, function(index, child) {
                 traverse(child);
            });
        }
        datatree.disable_node(node);
    });

    if (notOwned) {
        $("#deleteOthersWarning").show();
    } else {
        $("#deleteOthersWarning").hide();
    }

    var type_strings = [];
    for (var key in dtypes) {
        type_strings.push(key.replace("acquisition", "Run").capitalize() + (dtypes[key]>1 && "s" || ""));
    }
    var type_str = type_strings.join(" & ");    // For delete dialog: E.g. 'Project & Datasets'
    $("#delete_type").text(type_str);
    if (!askDeleteContents) $("#delete_contents_form").hide();  // don't ask about deleting contents

    // callback when delete dialog is closed
    del_form.bind("dialogclose", function(event, ui) {
        if (del_form.data("clicked_button") == "Yes") {
            var delete_anns = $("#delete_anns").prop('checked');
            var delete_content = true;      // $("#delete_content").prop('checked');
            if (delete_content) ajax_data[ajax_data.length] = 'child=on';
            if (delete_anns) ajax_data[ajax_data.length] = 'anns=on';
            var url = deleteUrl;

            $.ajax({
                url: url,
                data : ajax_data.join("&"),
                dataType: "json",
                type: "POST",
                success: function(r){
                    // Update the central panel in case delete removes an icon
                    $.each(selected, function(index, node) {
                        var e = {'type': 'delete_node'};
                        var data = {'node': node,
                                    'old_parent': firstParent};
                        update_thumbnails_panel(e, data);
                    });

                    datatree.delete_node(selected);

                    // Update the central panel with new selection
                    datatree.deselect_all();
                    // Don't select plate during 'Run' delete - tries to load partially deleted data
                    if (firstParent.type !== "plate") {
                        datatree.select_node(firstParent);
                    }
                    $.each(disabledNodes, function(index, node) {
                        //TODO Make use of server calculated update like chgrp?
                        updateParentRemoveNode(datatree, node, firstParent);
                        removeDuplicateNodes(datatree, node);
                    });

                    OME.refreshActivities();
                },
                error: function(response) {
                    $.each(disabledNodes, function(index, node) {
                        datatree.enable_node(node);
                    });
                }
            });
        } else {
            // Cancelled, re-enable nodes
            $.each(disabledNodes, function(index, node) {
                 datatree.enable_node(node);
            });
        }
    });

    // Check if delete will attempt to partially delete a Fileset.
    var $deleteYesBtn = $('.delete_confirm_dialog .ui-dialog-buttonset button:nth-child(1)'),
        $deleteNoBtn = $('.delete_confirm_dialog .ui-dialog-buttonset button:nth-child(2) span');
    $.get(filesetCheckUrl + OME.get_tree_selection(), function(html){
        if($('div.split_fileset', html).length > 0) {
            var $del_form_content = del_form.children().hide();
            del_form.append(html);
            $deleteYesBtn.hide();
            $deleteNoBtn.text("Cancel");
            // On dialog close, clean-up what we changed above
            del_form.bind("dialogclose", function(event, ui) {
                $deleteYesBtn.show();
                $deleteNoBtn.text("No");
                $(".split_filesets_info", del_form).remove();
                $del_form_content.show();
            });
        }
    });
};

OME.nodeHasPermission = function(node, permission) {
    /*
    * Check the permissions on a node
    */

    // Require that all nodes have the necessary permissions
    if ($.isArray(node)) {
        for (var index in node) {
            if (!OME.nodeHasPermission(node[index], permission)) {
                return false;
            }
        }
        // All must have had the permission
        return true;
    }

    if (permission === 'isOwned') {
        if (node.data.obj.hasOwnProperty('ownerId') && node.data.obj.ownerId === currentUserId()) {
            return node.data.obj.isOwned;
        } else if (node.type === 'experimenter' && node.data.id == currentUserId()) {
            return true;
        }
        return false;
    }

    // Check if the node data has permissions data
    if (node.data.obj.hasOwnProperty('permsCss')) {
        var perms = node.data.obj.permsCss;
        // Determine if this node has this permission
        if (perms.indexOf(permission) > -1) {
            return true;
        }
    }
    return false;
};


OME.writeSelectedObjs = function(selected_tree_nodes, selected_icons) {
/***
 * Write the current selection to the dom
*/

    // Here we handle data coming from jsTree. Nodes have data object from json
    var selected_objs = [];
    if (selected_tree_nodes !== undefined && selected_tree_nodes.length > 0) {
        var inst = $.jstree.reference('#dataTree');
        $.each(selected_tree_nodes, function(index, val) {
            var node = inst.get_node(val);
            var oid = node.type + '-' + node.data.obj.id;
            var selected_obj = {
                'id': oid,
                'rel': node.type,
                'class': node.data.obj.permsCss
            };
            if (node.data.obj.shareId) {
                selected_obj.shareId = node.data.obj.shareId;
            }
            // If it's an image it will have a filesetId
            if (node.type === 'image') {
                selected_obj.fileset = node.data.obj.filesetId;
            }

            selected_objs.push(selected_obj);
        });
    // Or we have data from thumbnails. Data is from data-attr on DOM
    } else if (selected_icons !== undefined && selected_icons.length > 0) {
        selected_icons.each(function(index, el) {
            var $el = $(el);
            var oid = $el.data('type') + '-' + $el.data('id');
            var selected_obj = {
                'id': oid,
                'rel': $el.data('type'),
                'class': $el.data('perms')
            };
            if ($el.data("share")) {
                selected_obj.shareId = $el.data("share");
            }
            // If it's an image it will have a filesetId
            if ($el.data('type') === 'image') {
                selected_obj.fileset = $el.data('fileset');
            }

            selected_objs.push(selected_obj);
        });
    }

    $("body").data("selected_objects.ome", selected_objs);
};

OME.getTreeBestGuess = function(targetType, targetId) {
    /***
    * Get a tree node that is of the correct type and id
    * that is in the current selection hierarchy
    * This can mean that the target is selected, an ancestor is selected,
    * or that it has a selected descendant
    ***/
    var datatree = $.jstree.reference('#dataTree');

    // Find the matching child nodes from the tree
    // Locate any matching nodes and then find the one (or take the first
    // as there could be multiple) that has the currently selected parent
    var locatedNodes = datatree.locate_node(targetType + '-' + targetId);

    if (!locatedNodes) {
        datatree.deselect_all();
        return;
    }

    // Get the current jstree selection
    var selectedNodes = datatree.get_selected();
    var node;
    var parentNodeIds = [];

    var traverseUp = function(nodeId) {
        // Got to the root, give up
        if (nodeId === '#') {
            return false;
        } else {
            // Found a node that was selected
            if (selectedNodes.indexOf(nodeId) != -1) {
                return true;
            // Not found, recurse upwards
            } else {
                return traverseUp(datatree.get_parent(nodeId));
            }
        }
    };

    var traverseDown = function(nodeId) {
        if (selectedNodes.indexOf(nodeId) != -1) {
            return true;
        }

        var n = datatree.get_node(nodeId);
        // Got to a leaf, give up
        if (n.type === 'image'){
            return false;
        // Not found, recurse downwards
        } else {
            var ret = false;
            $.each(n.children, function(index, val) {
                 if (traverseDown(val)) {
                    ret = true;
                    // Breat out of each
                    return false;
                 }
            });
            return ret;
        }
    };

    // Find a node that matches our target that has a selected parent
    // Keep in mind that this will return the first potential node which
    // has a selected parent.
    // WARNING: This may not give expected results with multiselects
    $.each(locatedNodes, function(index, val) {
         if (traverseUp(val.id)) {
            node = val;
            // Break out of each
            return false;
         }
         // It is possible that the selection is below the item we are looking for
         // so look for a selection below as well to indicate the best guess
         if (val.type != 'image' && traverseDown(val.id)) {
            node = val;
            // Break out of each
            return false;
         }
    });

    return node;

};

OME.getTreeImageContainerBestGuess = function(imageId) {

    var datatree = $.jstree.reference('#dataTree');
    var selectType = 'image';

    // Find the matching child nodes from the tree
    // Locate any matching nodes and then find the one (or take the first
    // as there could be multiple) that has the currently selected parent
    var locatedNodes = datatree.locate_node(selectType + '-' + imageId);

    if (!locatedNodes) {
        datatree.deselect_all();
        return;
    }

    // Get the current jstree selection
    var selectedNodes = datatree.get_selected(true);
    var containerNode;
    var parentNodeIds = [];

    // Double check that it is either a single dataset selection or a multi
    // image selection. Get all the possible parent nodes
    if (selectedNodes.length === 1 && selectedNodes[0].type === 'dataset') {
        parentNodeIds.push(selectedNodes[0].id);
    } else if (selectedNodes.length >= 1 && selectedNodes[0].type === 'image') {
        // Get the parents of the selected nodes
        $.each(selectedNodes, function(index, selectedNode) {
            parentNodeIds.push(datatree.get_parent(selectedNode));
        });
    }

    // webclient allows multiselect which is not bounded by a
    // single container. Take the first located node that has a correct
    // parent and has a selection.

    // Get the first of the located nodes that has one of these selected nodes as a parent
    $.each(locatedNodes, function(index, locatedNode) {
        var locatedNodeParentId = datatree.get_parent(locatedNode);

        // If there was no selection, just guess that the first parent we come to is ok
        if (parentNodeIds.length === 0) {
            containerNode = datatree.get_node(locatedNodeParentId);
            // Break out of $.each
            return false;

        // If this located node's parent is valid
        } else if ($.inArray(locatedNodeParentId, parentNodeIds) != -1) {
            // This is the container we need
            containerNode = datatree.get_node(locatedNodeParentId);
            // Break out of $.each
            return false;
        }

    });

    return containerNode;
};

OME.formatScriptName = function(name) {
    // format script name by replacing '_' with ' '
    name = name.replace(/_/g, " ");
    if (name.indexOf(".") > 0) {
        name = name.slice(0, name.indexOf(".")) + "...";
    }
    return name;
};

OME.showScriptList = function(event) {
    // We're almost always going to be triggered from an anchor
    event.preventDefault();

    // show menu - load if empty
    // $('#scriptList').css('visibility', 'visible');
    $('#scriptList').show();
    if ($("#scriptList li").length === 0){  // if none loaded yet...
        var $scriptLink = $(this);
        var $scriptSpinner = $("#scriptSpinner").show();
        var script_list_url = $(this).attr('href');
        $.get(script_list_url, function(data) {

            var build_ul = function(script_data) {
                var html = "";
                for (var i=0; i<script_data.length; i++) {
                    var li = script_data[i],   // dict of 'name' and 'ul' for menu items OR 'id' for scripts
                        name = li.name;
                    if (li.id) {
                        name = OME.formatScriptName(name);
                        html += "<li><a href='" + event.data.webindex + "script_ui/"+ li.id + "/'>" + name + "</a></li>";
                    } else {
                        html += "<li class='menuItem'><a href='#'>" + name + "</a>";
                        // sub-menus have a 'BACK' button at the top
                        html += "<ul><li class='menu_back'><a href='#'>back</a></li>" + build_ul(li.ul) + "</ul>";
                        html += "</li>";
                    }
                }
                return html;
            };

            var html = "<ul class='menulist'>" + build_ul(data) + "</ul>";

            $('#scriptList').append($(html));

            $('#scriptList ul ul').hide();
            $scriptSpinner.hide();
      }, "json");
    }
};

OME.hideScriptList = function() {
    $("#scriptList").hide();
};

OME.toggleFileAnnotationCheckboxes = function(event) {
    var checkboxes = $("#fileanns_container input[type=checkbox]");
    checkboxes.toggle().prop("checked", false);
    checkboxes.parents("li").toggleClass("selected", false);
};

OME.fileAnnotationCheckboxChanged = function(event) {
    $(event.target).parents("li").toggleClass("selected");
};

OME.fileAnnotationCheckboxDynamicallyAdded = function() {
    var checkboxesAreVisible = $(
        "#fileanns_container input[type=checkbox]:visible"
    ).length > 0;
    if (checkboxesAreVisible) {
        $("#fileanns_container input[type=checkbox]:not(:visible)").toggle();
    }
};

jQuery.fn.tooltip_init = function() {
    $(this).tooltip({
        items: '.tooltip',
        content: function() {
            return $(this).parent().find("span.tooltip_html").html();
        },
        track: true,
        show: false,
        hide: false
    });
  return this;
};
