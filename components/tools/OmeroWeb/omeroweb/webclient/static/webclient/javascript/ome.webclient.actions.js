//
// Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

jQuery.fn.hide_if_empty = function() {
    if ($(this).children().length === 0) {
        $(this).hide();
    } else {
        $(this).show();
    }
  return this;
};

OME.addToBasket = function(selected, prefix) {
    var datatree = $.jstree.reference('#dataTree');
    var productListQuery = new Array("action=add");
    if (selected != null && selected.length > 0) {
        $.each(selected, function(index, sel) {
            var node = datatree.get_node(sel);
            productListQuery.push(node.type + '=' + node.data.obj.id);
        });
    } else {
        OME.alert_dialog("Please select at least one element.");
        return;
    }
    $.ajax({
        type: "POST",
        url: prefix, //this.href,
        data: productListQuery.join("&"),
        success: function(responce){
            if(responce.match(/(Error: ([A-z]+))/gi)) {
                OME.alert_dialog(responce);
            } else {
                OME.calculateCartTotal(responce);
            }
        }
    });
};

// called from OME.tree_selection_changed() below
OME.handle_tree_selection = function(data, event) {

    var selected = undefined;
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
OME.select_fileset_images = function(filesetId) {
    var datatree = $.jstree.reference('#dataTree');
    // This is only used when deleting filesets to select them all
    // Fundamentally it can not really work as the images may have
    // been split into many datasets. Given that it doesn't really
    // work, just select visual fields which are in the fileset
    $("#dataTree li[data-fileset="+filesetId+"]").each(function(){
        datatree.select_node(this);
    });
};

// actually called when share is edited, to refresh right-hand panel
OME.share_selection_changed = function(share_id) {
    $("body")
        .data("selected_objects.ome", [{"id": share_id}])
        .trigger("selection_change.ome");
};

// Standard ids are in the form TYPE-ID, web extensions may add an
// additional -SUFFIX
OME.table_selection_changed = function($selected) {
    //TODO Use write select_objs function.
    // Guess this is for search and such where there is no tree?
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
    //TODO Use write selected_objs function
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


// This is called by the Pagination controls at the bottom of icon or table pages.
OME.doPagination = function(view, page) {
    var datatree = $.jstree.reference('#dataTree');

    var $container;
    if (view == "icon") {
        $container = $("#content_details");
    }
    else if (view == "table") {
        $container = $("#image_table");
    }

    var containerId = $container.data('id');
    var containerType = $container.data('type');
    var containerPath = $container.data('path');
    containerPath = JSON.parse(containerPath);
    var containerNode = datatree.find_omepath(containerPath);

    if (!containerNode) {
        console.log('WARNING: Had to guess container');
        containerNode = OME.getTreeBestGuess(containerType, containerId);
    }

    // Set the page for that node in the tree and reload the tree section
    datatree.change_page(containerNode, page);
    // Reselect the same node to trigger update
    datatree.deselect_all(true);
    datatree.select_node(containerNode);

    return false;
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

    $('.tag_annotation_wrapper, .file_ann_wrapper, .ann_comment_wrapper, #custom_annotations tr')
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
        thumbs_selector = "#dataIcons img",
        search_selector = ".search_thumb",
        spw_selector = "#spw img";
    // handle Dataset thumbs, search rusults and SPW thumbs
    if (options.imageId) {
        thumbs_selector = "#image_icon-" + options.imageId + " img";
        search_selector = "#image-" + options.imageId + " img.search_thumb";
        spw_selector += "#image-" + options.imageId;
    }
    $(thumbs_selector + ", " + spw_selector + ", " + search_selector).each(function(){
        var $this = $(this),
            base_src = $this.attr('src').split('?')[0];
        $this.attr('src', base_src + "?_="+rdm);
    });

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

OME.nodeHasPermission = function(node, permission, activeGroup) {
    // Special case isOwned permission for now as it is not part of permsCss
    // perhaps it should be. Also, there are no perms on experimenter so it
    // is necessary to compare the active user to the id of the user in question

    // Require that all nodes have the necessary permissions
    if ($.isArray(node)) {
        for (index in node) {
            if (!OME.nodeHasPermission(node[index], permission, activeGroup)) {
                return false;
            }
        }
        // All must have had the permission
        return true;
    }

    if (permission === 'isOwned') {
        if (node.data.obj.hasOwnProperty('isOwned')) {
            return node.data.obj.isOwned
        } else if (node.type === 'experimenter' && node.data.id == activeGroup) {
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

OME.iconHasPermission = function(icon, permission, activeGroup) {
    // Special case isOwned permission for now as it is not part of permsCss
    // perhaps it should be. Also, there are no perms on experimenter so it
    // is necessary to compare the active user to the id of the user in question

    // Require that all nodes have the necessary permissions
    if ($.isArray(icon)) {
        for (index in node) {
            if (!OME.iconHasPermission(node[index], permission, activeGroup)) {
                return false;
            }
        }
        // All must have had the permission
        return true;
    }

    if (permission === 'isOwned') {
        if (icon.data().hasOwnProperty('owned')) {
            if (icon.data('owned') === 'True') {
                return true;
            }

        //TODO node.data.id == activeGroup ({{ active_group.id }}). Surely this is comparing
        // an experimenter id to a group id???
        } else if (icon.data('type') === 'experimenter' && icon.data('id') == activeGroup) {
            return true;
        }
        return false;
    }

    // Check if the icon data has permissions data
    if (node.data().hasOwnProperty('perms')) {
        var perms = node.data('perms');
        // Determine if this icon has this permission
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

    selected_objs = [];
    if (selected_tree_nodes != undefined && selected_tree_nodes.length > 0) {
        var inst = $.jstree.reference('#dataTree');
        $.each(selected_tree_nodes, function(index, val) {
            var node = inst.get_node(val);
            var oid = node.type + '-' + node.data.obj.id;
            var selected_obj = {
                'id': oid,
                'rel': node.type,
                'class': node.data.obj.permsCss
            };
            // If it's an image it will have a filesetId
            if (node.type === 'image') {
                selected_obj['fileset'] = node.data.obj.filesetId;
            }

            selected_objs.push(selected_obj);
        });
    } else if (selected_icons != undefined && selected_icons.length > 0) {
        selected_icons.each(function(index, el) {
            var $el = $(el);
            var oid = $el.data('type') + '-' + $el.data('id');
            //TODO Fill in class
            var selected_obj = {
                'id': oid,
                'rel': $el.data('type'),
                'class': $el.data('perms')
            };

            // If it's an image it will have a filesetId
            if ($el.data('type') === 'image') {
                selected_obj['fileset'] = $el.data('fileset');
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
    // TODO What about orphaned selection?
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

jQuery.fn.tooltip_init = function() {
    $(this).tooltip({
        items: '.tooltip',
        content: function() {
            return $(this).parent().children("span.tooltip_html").html();
        },
        track: true,
        show: false,
        hide: false
    });
  return this;
};
