
//   Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
//   All rights reserved.

//   This program is free software: you can redistribute it and/or modify
//   it under the terms of the GNU Affero General Public License as
//   published by the Free Software Foundation, either version 3 of the
//   License, or (at your option) any later version.

//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU Affero General Public License for more details.

//   You should have received a copy of the GNU Affero General Public License
//   along with this program.  If not, see <http://www.gnu.org/licenses/>.


//   Here we setup and configure the jstree

// jQuery load callback...
$(function() {

    // Select jstree and then cascade handle events and setup the tree.
    var jstree = $("#dataTree")
    .on('changed.jstree', function (e, data) {
        var inst = data.instance;
        buttonsShowHide(inst.get_selected(true), inst);

        // Load on selection, but not open because that breaks key navigation
        if (data.node &&
            inst.is_parent(data.node) &&
            !inst.is_loaded(data.node) &&
            !inst.is_loading(data.node)) {
            inst.load_node(data.node);
        }

        OME.tree_selection_changed(data, e);
    })
    .on('copy_node.jstree', function(e, data) {
        /**
        * Fired when a node is pasted
        * Updates the server, adding the new link
        */
        var inst = data.instance;

        // The data is not cloned when the node is copied, do that manually
        data.node.data = JSON.parse(JSON.stringify(data.original.data));

        // Copy the data for any child nodes in the copy as well
        for(var i = 0; i < data.original.children_d.length; i++) {
            var originalData = inst.get_node(data.original.children_d[i]).data;
            originalData = JSON.parse(JSON.stringify(originalData));
            inst.get_node(data.node.children_d[i]).data = originalData;
        }

        // Remove potential duplicate node
        var childExists = removeDuplicate(inst, data.node, data.parent);

        // Persist
        if (!childExists) {
            $.when(linkNode(inst, data.node, inst.get_node(data.parent))).done(function() {
                update_thumbnails_panel(e, data);
            });
        } else {
            update_thumbnails_panel(e, data);
        }

        // Update the child count
        OME.updateNodeChildCount(inst, data.parent);

        // Add to other identical nodes as well
        updateParentInsertNode(inst, data.node, inst.get_node(data.parent), data.position);
    })
    .on('move_node.jstree', function(e, data) {
        /**
        * Fired when a node is moved
        * Updates the server, removing the old link and adding the new
        */
        var inst = data.instance;
        // Remove potential duplicate node
        var childExists = removeDuplicate(inst, data.node, data.parent);

        // trigger ome remove
        $("body").trigger('removed_node.jstree.ome', data.node.data.obj);

        // Persist
        var linkPromise;
        // If the move is orphaning an object, do not persist the link
        if (data.parent.type !== 'experimenter' &&
            data.parent.type !== 'orphaned' && !childExists) {
            linkPromise = linkNode(inst, data.node, inst.get_node(data.parent));
        }
        var unlinkPromise = unlinkNode(inst, data.node, inst.get_node(data.old_parent));

        $.when(linkPromise, unlinkPromise).done(function() {
            update_thumbnails_panel(e, data);
        });

        // Add/Remove node to/from other identical nodes as well
        updateParentInsertNode(inst, data.node, inst.get_node(data.parent), data.position);
        updateParentRemoveNode(inst, data.node, inst.get_node(data.old_parent));

        // Update the child counts
        OME.updateNodeChildCount(inst, data.parent);
        OME.updateNodeChildCount(inst, data.old_parent);

    })
    .on('delete_node.jstree', function(e, data) {
        /**
        * Fired when a node is deleted
        * Simply removes the node, we do not update the server here as there
        * is a need to delete nodes without persisting. E.g. when a dataset
        * is updated to match another instance of itself elsewhere in the tree
        */
        var inst = data.instance;
        // Update the child count
        OME.updateNodeChildCount(inst, data.parent);
    })
    .on('create_node.jstree', function(e, data) {
        /**
        * Fired when a node is created
        * Simply adds the node, we do not update the server here as there
        * is a need to create nodes without persisting. E.g. when a dataset
        * is updated to match another instance of itself elsewhere in the tree
        */
        var inst = data.instance;
        // Update the child count
        OME.updateNodeChildCount(inst, data.parent);
    })
    .on('loaded.jstree', function(e, data) {
        /**
        * Fired when the tree is loaded and ready for action
        */
        var inst = data.instance;

        // Global variable specifies what to select
        var nodeIds = WEBCLIENT.initially_select;
        if (nodeIds.length === 0) {
            // If not found, just select root node
            inst.select_node('ul > li:first');
        } else {
            // We load hierachy for first item...
            var paramSplit = nodeIds[0].split('-');

            var payload = {};
            payload[paramSplit[0]] = paramSplit[1];

            // AJAX Query to get the path of the item we wish to 'show'
            $.ajax({
                url: WEBCLIENT.URLS.api_paths_to_object,
                data : payload,
                dataType: "json",
                type: "GET",
                success: function(json) {
                    data = json.paths;
                    // Use the open_node callback mechanism to facilitate loading the tree to the
                    // point indicated by the path, starting from the top, 'experimenter'.
                    if (data.length === 0) return;

                    var getTraverse = function(path) {
                        var traverse = function(index, parentNode) {
                            // Get this path component
                            var comp = path[index];
                            var lastIndex = path.length - 1;
                            // Get the node for this path component
                            var node = inst.locate_node(comp.type + '-' + comp.id, parentNode)[0];

                            // if we've failed to find root, we might be showing "All Members". Try again...
                            if (index === 0 && !node) {
                                node = inst.locate_node(comp.type + '-' + '-1', parentNode)[0];
                            }
                            
                            // If at any point the node doesn't exist, simply give up as the path has
                            // become invalid
                            if (!node) {
                                return;
                            }
                            // If we have a 'childPage' greater than 0, need to paginate
                            if (comp.childPage) {
                                inst._set_page(node, comp.childPage);
                            }

                            if (index < lastIndex) {
                                inst.open_node(node, function() {
                                    traverse(index += 1, node);
                                });
                            // Otherwise select it
                            } else {
                                inst.select_node(node);
                                inst.open_node(node);
                                // we also focus the node, to scroll to it and setup hotkey events
                                $("#" + node.id).children('.jstree-anchor').focus();
                                // Handle multiple selection. E.g. extra images in same dataset
                                for(var n=1; n<nodeIds.length; n++) {
                                    node = inst.locate_node(nodeIds[n], parentNode)[0];
                                    if(node) {
                                        inst.select_node(node);
                                    }
                                }
                            }
                        };
                        return traverse;
                    }
                    var i;
                    for (i=0; i < (data.length); i++) {
                        var path = data[i];
                        var traverse = getTraverse(path)
                        // Start traversing at the start of the path with no parent node
                        try {
                            traverse(0, undefined);
                        } finally {
                        }
                    }
                },

                error: function(json) {
                    // Global error handling is sufficient here
                }
            });
        }

        // Update the URL to remove the parameters as they serve to preload data this one
        // time only
        // history.pushState({}, '', window.location.pathname);
    })
    .on("click.jstree", ".jstree-anchor", function (e) {
        e.preventDefault();
        var datatree = $.jstree.reference($('#dataTree'));
        // Expand on click (not select because of key navigation)
        if (datatree.is_parent(this)) {
            datatree.open_node(this);
        }
    })
    .on("dblclick.jstree", ".jstree-anchor", function (e) {
        e.preventDefault();
        var datatree = $.jstree.reference($('#dataTree'));
        var node = datatree.get_node(this);
        if (node) {
            if (node.type === 'image') {
                //Open the image viewer for this image
                OME.openPopup(WEBCLIENT.URLS.webindex + "img_detail/" + node.data.obj.id);
            }
        }
    })
    .on('keydown.jstree', '.jstree-anchor', function (e) {
        var datatree = $.jstree.reference($('#dataTree')),
            prev, next;

        switch(e.which) {
            // Up
            case 38:
                e.preventDefault();
                prev = datatree.get_prev_dom(this);
                if(prev && prev.length) {
                    datatree.deselect_all();
                    datatree.select_node(prev);
                }
                break;
            // Down
            case 40:
                e.preventDefault();
                next = datatree.get_next_dom(this);
                if(next && next.length) {
                    datatree.deselect_all();
                    datatree.select_node(next);
                }
                break;
            // Left
            case 37:
                e.preventDefault();
                if(!datatree.is_open(this)) {
                    prev = datatree.get_parent(this);
                    if(prev && prev.length) {
                        datatree.deselect_all();
                        datatree.select_node(prev);
                    }
                }
                break;
            // Right
            case 39:
                e.preventDefault();
                // opening of node is handled by jsTree, we just select...
                if(!datatree.is_closed(this)) {
                    next = datatree.get_next_dom(this);
                    if(next && next.length) {
                        datatree.deselect_all();
                        datatree.select_node(next);
                    }
                }
                break;
        }

    })
    .on('refresh.jstree', function(){
        var datatree = $.jstree.reference($('#dataTree'));

        // Use the cached selection in refreshPathsReverse to restore the selection after refresh
        $.each(refreshPathsReverse, function(index, refreshPathReverse) {
            // If all parts of the path match the located node then select it
            var locatedNodes = datatree.locate_node(refreshPathReverse[0][0] +
                               '-' +
                               refreshPathReverse[0][1]);
            $.each(locatedNodes, function(index, node) {
                var traverseNode = node;
                var matched = true;
                $.each(refreshPathReverse, function(index, pathComponent) {
                    if (traverseNode &&
                        traverseNode.type === pathComponent[0] &&
                        traverseNode.data.obj.id === pathComponent[1]) {
                        // Update traverseNode to be its own parent
                        traverseNode = datatree.get_node(datatree.get_parent(traverseNode));
                    } else {
                        matched = false;
                        // Exit refreshPathReverse each loop
                        return false;                            }
                });

                if (matched) {
                    datatree.select_node(node);
                    // Exit locatedNodes each loop
                    return false;
                }
            });

        });
        // Clear refreshPathsReverse after selection has been restored
        refreshPathsReverse = [];
    })

    // Setup jstree
    .jstree({
        'plugins': ['types', 'contextmenu', 'dnd', 'sort', 'locate',
                    'ometools', 'conditionalselect', 'pagination', 'fields',
                    'truncatetext', 'childcount', 'omecut'],
        // The jstree core
        'locate' : {
            // Returns a key for this node
            'locate_function': function(node) {
                // In some cases, this function is called before the data attribute exists
                // These should be ignored, this will be called again later when it is
                // populated.
                if (!node.hasOwnProperty('data') ||
                    node.data === undefined ||
                    node.data === null) {
                    return false;
                }
                return node.type + '-' + node.data.obj.id;
            }
        },

        'conditionalselect' : {
            // Checks if a selection should be allowed
            'conditionalselect_function': function(node) {
                var inst = this;
                var selected = inst.get_selected(true);
                // As this function will previously have prevented cross-select, just
                // check the first selection instead.
                if (selected.length > 0 && selected[0].type !== node.type) {
                    return false;
                }

                // Also disallow the selection if it is a multi-select and the new target
                // is already selected
                selected = inst.get_selected(true);
                var allowSelect = true;
                $.each(selected, function(index, sel) {
                    if (sel.type === node.type && sel.data.obj.id === node.data.obj.id) {
                        allowSelect = false;
                        // Break out of each
                        return false;
                    }
                });

                return allowSelect;

            }
        },
        'omecut': {
            'path_url': WEBCLIENT.URLS.api_paths_to_object
        },
        'core' : {
            'themes': {
                'dots': false,
                'variant': 'ome'
            },
            'force_text': true,
            // Make use of function for 'data' because there are some scenarios in which
            // an ajax call is not used to get the data. Namely, the all-user view
            'data' : function(node, callback, payload) {
                // Get the data for this query
                if (payload === undefined) {
                    payload = {};
                }
                // We always use the parent id to fitler. E.g. experimenter id, project id etc.
                // Exception to this for orphans as in the case of api_images, id is a dataset
                if (node.hasOwnProperty('data') && node.type != 'orphaned') {
                    // NB: In case of loading Tags, we don't want to use 'id' for top level
                    // since that will filter by tag.
                    // TODO: fix inconsistency between url apis by using 'owner'
                    var tagroot = (WEBCLIENT.URLS.tree_top_level === WEBCLIENT.URLS.api_tags_and_tagged &&
                            node.type === 'experimenter');

                    if (node.data.hasOwnProperty('obj')) {
                        // Allows to load custom parameters to QUERY_STRING
                        if (node.data.obj.hasOwnProperty('extra')) {
                            $.extend(payload, node.data.obj.extra)
                        }
                    }

                    if (!tagroot && node.data.hasOwnProperty('obj')) {
                        // Allows to load custom parameters to QUERY_STRING
                        payload['id'] = node.data.obj.id;
                    }

                    if (tagroot) {
                        // Don't show tags that are in tagsets
                        payload['orphaned'] = true;
                    }
                }

                // Work back up the tree to obtain the id of the user we are viewing,
                // this is useful in the case of orphaned image listing in particular.
                // It may also be appropriate to use it to filter the queries in other
                // places as well.
                var inst = this;

                // This path does not include the root node so the first entry is always
                // the experimenter node except on inital load in which case it is false
                var path = inst.get_path(node, false, true);
                // Include the experimenter_id if we are loading an experimenter or
                // orphaned node
                if (path && (node.type === 'experimenter' || node.type === 'orphaned')) {
                    payload['experimenter_id'] = inst.get_node(path[0]).data.obj.id;
                }

                // If this is a node which can have paged results then either specify that
                // we want the specific page, or use default first page

                // Disable paging for node without counter
                var nopageTypes = ['project', 'screen', 'plate', 'tagset', 'tag'];
                if (nopageTypes.indexOf(node.type) > -1) {
                    // TODO: temporary workaround to not paginate datasets,
                    // plates and acquisitions
                    // see center_plugin.thumbs.js.html
                    payload['page'] = 0;
                } else {
                    // Attempt to get the current page desired if there is one
                    var page = inst.get_page(node);
                    payload['page'] = page;
                }

                // Specify that orphans are specifically sought
                if (node.type === 'orphaned') {
                    payload['orphaned'] = true;
                }

                // Extra data needed for showing thumbs in centre panel
                if (node.type === 'dataset' || node.type === 'orphaned' || node.type === 'tag') {
                    payload['sizeXYZ'] = true;
                    payload['date'] = true;
                    if (node.type !== 'tag') {
                        payload['thumbVersion'] = true;
                    }
                }

                // Always add the group_id from the current context
                payload['group'] = WEBCLIENT.active_group_id;


                // Configure URL for request
                // Get the type of the node being expanded
                // Figure out what type of children it should have
                // Request the list of children from that url, adding any relevant filters
                var url;
                if (node.type === 'experimenter') {
                    // This will be set to containers or tags url, depending on page we're on 
                    url = WEBCLIENT.URLS.tree_top_level;
                } else if (node.type === 'map') {
                    url = WEBCLIENT.URLS.tree_map_level;
                } else if (node.type === 'tagset') {
                    url = WEBCLIENT.URLS.tree_top_level;
                } else if (node.type === 'tag') {
                    url = WEBCLIENT.URLS.tree_top_level;
                } else if (node.type === 'project') {
                    url = WEBCLIENT.URLS.api_datasets;
                } else if (node.type === 'dataset') {
                    url = WEBCLIENT.URLS.api_images;
                } else if (node.type === 'screen') {
                    url = WEBCLIENT.URLS.api_plates;
                } else if (node.type === 'plate') {
                    url = WEBCLIENT.URLS.api_plate_acquisitions;
                } else if (node.type === 'orphaned') {
                    url = WEBCLIENT.URLS.api_images;
                } else if (node.id === '#') {
                    // Here we handle root of jsTree
                    // Experimenhter ID is set for user ID or -1 for entire group
                    url = WEBCLIENT.URLS.api_experimenter;
                }

                if (url === undefined) {
                    return;
                }

                $.ajax({
                    url: url,
                    data: payload,
                    cache: false,
                    success: function (data, textStatus, jqXHR) {
                        callback.call(this, data);
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        // Global error handling is sufficient here
                    },
                    // Converter is required because the JSON format being returned is not
                    // jstree specific.
                    'converters' : {
                        "text json": function (json) {
                            var data = JSON.parse(json),
                                jstree_data = [],
                                node;

                            // Add experimenters to the jstree data structure
                            // This handles multiple experimenters in the tree
                            // if (data.hasOwnProperty('experimenters')) {
                            //     $.each(data.experimenters, function(index, value) {
                            //         var node = {
                            //             'data': {'id': value.id, 'obj': value},
                            //             'text': value.firstName + ' ' + value.lastName,
                            //             'children': true,
                            //             'type': 'experimenter',
                            //             'state': {
                            //             },
                            //             'li_attr': {
                            //                 'data-id': value.id
                            //             }
                            //         };

                            //         // Add 'state' opened for the current user by default
                            //         {% if active_user %}
                            //             if (value.id == {{ active_user.getId }}) {
                            //                 node.state['opened'] = true;
                            //             }
                            //         {% endif %}

                            //         jstree_data.push(node);
                            //     });
                            // }
                            function makeNode(value, type) {
                                var rv = {
                                    'data': {'id': value.id, 'obj': value},
                                    'text': value.name,
                                    'children': value.childCount ? true : false,
                                    'type': type,
                                    'state': value.state ? value.state : {'opened': false},
                                    'li_attr': {
                                        'data-id': value.id
                                    },
                                    'extra': value.extra
                                };
                                if (type === 'experimenter') {
                                    rv.text = value.firstName + ' ' + value.lastName;
                                    rv.state = value.state ? value.state : {'opened': true},
                                    rv.children = true;
                                } else if (type === 'tag') {
                                    // We don't count children for Tags (too expensive?) Assume they have children
                                    rv.children = true;
                                    rv.type = value.set ? 'tagset' : 'tag';
                                    rv.text = value.value;
                                }
                                return rv;
                            }

                            if (data.hasOwnProperty('experimenter')) {
                                node = makeNode(data.experimenter, 'experimenter');
                                jstree_data.push(node);
                            }

                            if (data.hasOwnProperty('maps')) {
                                $.each(data.maps, function(index, value) {
                                    var node = makeNode(value, 'map');
                                    jstree_data.push(node);
                                });
                            }

                            // Add tags to the jstree data structure
                            if (data.hasOwnProperty('tags')) {
                                $.each(data.tags, function(index, value) {
                                    var node = makeNode(value, 'tag');
                                    jstree_data.push(node);
                                });
                            }

                            // Add projects to the jstree data structure
                            if (data.hasOwnProperty('projects')) {
                                $.each(data.projects, function(index, value) {
                                    var node = makeNode(value, 'project');
                                    jstree_data.push(node);
                                });
                            }

                            // Add datasets to the jstree data structure
                            if (data.hasOwnProperty('datasets')) {
                                $.each(data.datasets, function(index, value) {
                                    var node = makeNode(value, 'dataset');
                                    jstree_data.push(node);
                                });
                            }

                            // Add images to the jstree data structure
                            if (data.hasOwnProperty('images')) {
                                $.each(data.images, function(index, value) {
                                    var node = makeNode(value, 'image');
                                    jstree_data.push(node);
                                });
                            }

                            // Add screens to the jstree data structure
                            if (data.hasOwnProperty('screens')) {
                                $.each(data.screens, function(index, value) {
                                    var node = makeNode(value, 'screen');
                                    jstree_data.push(node);
                                });
                            }

                            // Add plates to the jstree data structure
                            if (data.hasOwnProperty('plates')) {
                                $.each(data.plates, function(index, value) {
                                    var node = makeNode(value, 'plate');
                                    jstree_data.push(node);
                                });
                            }

                            // Add acquistions (runs) to the jstree data structure
                            if (data.hasOwnProperty('acquisitions')) {
                                $.each(data.acquisitions, function(index, value) {
                                    var node = makeNode(value, 'acquisition');
                                    jstree_data.push(node);
                                });
                            }

                            // Add wells to the jstree data structure
                            if (data.hasOwnProperty('wells')) {
                                $.each(data.wells, function(index, value) {
                                    var node = makeNode(value, 'well');
                                    jstree_data.push(node);
                                });
                            }

                            if (data.hasOwnProperty('orphaned')) {
                                node = {
                                    'data': {'obj': data.orphaned},
                                    'text': data.orphaned.name,
                                    'children': data.orphaned.childCount > 0 ? true : false,
                                    'type': 'orphaned'
                                };
                                jstree_data.push(node);
                            }

                            return jstree_data;
                        }

                    }
                });
            },
            'check_callback': function(operation, node, node_parent, node_position, more) {
                // This is used to check if we can drag and drop, paste etc.
                // Before this (and thus before the copy and the paste) the nodes children
                // are loaded. This is important as it allows us to weed out potential
                // conflicts in the copy, i.e. where a link to the object already exists
                // Called once per item being moved
                // TODO Should be impossible to delete a link from orphans
                var inst = $.jstree.reference(node);
                var oldParent;

                // Sometimes '#' pops up in the destination of d'n'd, discount this
                if (node_parent.id === '#') {
                    return false;
                }

                // Discount moves to a duplicate for both dnd checking and actual copy/move
                // Also ensure sourec and destination are linkable
                if (operation === 'copy_node' || operation === 'move_node') {
                    oldParent = inst.get_node(node.parent);
                    if (oldParent.type === node_parent.type &&
                        oldParent.data.obj.id === node_parent.data.obj.id) {
                        return false;
                    }
                }

                // On actual copy/move allow all valid
                if (more && more.core &&
                    (operation === 'copy_node' || operation === 'move_node')) {
                    // Check that the user has permission to list on the new parent
                    // or that their user themselves is the new parent
                    // or that their 'orphaned' directory is the new parent
                    if (!OME.nodeHasPermission(node_parent, 'canLink') &&
                        node_parent.type !== 'experimenter' &&
                        node_parent.type !== 'orphaned' &&
                        node_parent.data.obj.id !== WEBCLIENT.active_group_id) {
                        return false;
                    }
                    return true;
                // For dnd checking if it can copy/move
                } else if (operation === 'copy_node' || operation === 'move_node') {
                    // Only allow 'drop' if we 'canLink' or target is 'experimenter'
                    if (!OME.nodeHasPermission(node_parent, 'canLink') &&
                            node_parent.type !== 'experimenter') {
                        return false;
                    }
                    // If we are about to make object an orphan, don't allow to
                    // drop while dragging over parent, or between parent and it's children
                    if (node_parent.type === 'experimenter') {
                        if (oldParent && more) {
                            var pIdx = $("#"+oldParent.id).index();
                            if (pIdx === node_position && more.pos === "b") {
                                return false;
                            }
                            if (pIdx + 1 === node_position && more.pos === "a") {
                                return false;
                            }
                        }
                    }

                    var nodeRules = inst.get_rules(node);
                    var parentRules = inst.get_rules(node_parent);
                    if (parentRules.valid_children != -1 &&
                        parentRules.valid_children.indexOf(node.type) > -1) {
                        return true;
                    }
                    return false;
                }
                // Default to allowing all operations
                // 'copy_node', 'move_node' Handled above
                // 'delete_node', 'rename_node': true
                return true;
            }
        },
        'types' : {
            '#' : {
                'valid_children': ['experimenter']
            },
            'default': {
                'draggable': false
            },
            'experimenter': {
                'icon' : WEBCLIENT.URLS.static_webclient + 'image/icon_user.png',
                'valid_children': ['project','dataset','screen','plate', 'tag', 'tagset']
            },
            'map': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/left_sidebar_icon_tag.png',
                'valid_children': ['project', 'screen'],
                'draggable': false
            },
            'tagset': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/left_sidebar_icon_tags.png',
                'valid_children': ['tagset','tag']
            },
            'tag': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/left_sidebar_icon_tag.png',
                'valid_children': ['project', 'dataset', 'image', 'screen', 'plate', 'acquisition', 'well'],
                'draggable': true
            },
            'project': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/folder16.png',
                'valid_children': ['dataset']
            },
            'dataset': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/folder_image16.png',
                'valid_children': ['image'],
                'draggable': !WEBCLIENT.TAG_TREE
            },
            'image': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/image16.png',
                'draggable': !WEBCLIENT.TAG_TREE
            },
            'screen': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/folder_screen16.png',
                'valid_children': ['plate']
            },
            'plate': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/folder_plate16.png',
                'valid_children': ['acquisition'],
                'draggable': !WEBCLIENT.TAG_TREE
            },
            'acquisition': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/run16.png',
            },
            'well': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/well16.png',
            },
            'orphaned': {
                'icon': WEBCLIENT.URLS.static_webclient + 'image/folder_yellow16.png',
                'valid_children': ['image']
            }

        },
        'dnd': {
            'is_draggable': function(nodes) {
                var inst = $.jstree.reference(nodes[0]);
                // Check if the node types are draggable and the particular nodes have the
                // 'canLink' permission. All must pass
                // Don't allow dragging of any object from under a tag
                for (var index in nodes) {
                    var node = nodes[index];
                    if (!inst.get_rules(node).draggable ||
                          !OME.nodeHasPermission(node, 'canLink') ||
                            inst.get_node(node.parent).type === 'tag'
                        ) {
                        return false;
                    }
                }
                return true;
            }
        },
        'contextmenu': {
            'select_node': true,
            'show_at_node': false,
            'items' : function(node){
                var config = {};

                config["create"] = {
                    "label" : "Create new",
                    "_disabled": true,
                };

                var tagTree = (WEBCLIENT.URLS.tree_top_level === WEBCLIENT.URLS.api_tags_and_tagged);
                if (tagTree) {
                    config["create"]["submenu"] = {
                        "tagset": {
                            "label"     : "Tag Set",
                            "_disabled" : true,
                            "icon"      : WEBCLIENT.URLS.static_webclient + 'image/left_sidebar_icon_tags.png',
                            action      : function (node) {OME.handleNewContainer("tagset"); },
                        },
                        "tag": {
                            "label"     : "Tag",
                            "_disabled" : true,
                            "icon"      : WEBCLIENT.URLS.static_webclient + 'image/left_sidebar_icon_tag.png',
                            action      : function (node) {OME.handleNewContainer("tag"); },
                        }
                    };
                } else {
                    config["create"]["submenu"] = {
                        "project": {
                            "label" : "Project",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/folder16.png',
                            action: function (node) {OME.handleNewContainer("project"); },
                        },
                        "dataset": {
                            "label" : "Dataset",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/folder_image16.png',
                            action: function (node) {OME.handleNewContainer("dataset"); },
                          },
                          "screen": {
                            "label" : "Screen",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/folder_screen16.png',
                            action: function (node) {OME.handleNewContainer("screen"); },
                          }
                    };
                }

                config["ccp"] = {
                    "label"     : "Edit",
                    "action"    : false,
                    "_disabled" : true,
                    "submenu"   : {
                        "cut"   :{
                            "label" : "Cut Link",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_cut_16.png',
                            "action": function(data) {
                                var inst = $.jstree.reference(data.reference);
                                var objs = inst.get_selected(true);
                                inst.cut(objs);
                                // Always disable paste button immediatly after using it
                                enableToolbarButton('paste', false);
                            }
                        },
                        "copy"  : {
                            "label" : "Copy Link",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_copy_16.png',


                            "action": function (data) {
                                var inst = $.jstree.reference(data.reference);
                                var objs = inst.get_selected(true);
                                inst.copy(objs);
                            }

                        },
                        "paste": {
                            "label" : "Paste Link",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_paste_16.png',
                            "action": function(data) {
                                var inst = $.jstree.reference(data.reference);
                                var obj = inst.get_node(data.reference);
                                // Paste whatever is in the paste buffer into obj
                                inst.paste(obj);
                                // Always disable paste button immediatly after using it
                                enableToolbarButton('paste', false);
                            }
                        }
                    }
                };

                config["delete"] = {
                    "label" : "Delete",
                    "_disabled": true,
                    "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_delete_16.png',
                    "action": function(){
                        var deleteUrl = WEBCLIENT.URLS.deletemany,
                            filesetCheckUrl = WEBCLIENT.URLS.fileset_check;
                        OME.handleDelete(deleteUrl, filesetCheckUrl, WEBCLIENT.USER.id);
                    }
                };

                config["chgrp"] = {
                    "label" : "Move to Group...",
                    "_disabled": true,
                    "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_user_16.png',
                    "action": function() {
                        // TODO - make sure this works with new jsTree
                        OME.handleChgrp(WEBCLIENT.URLS.webindex, WEBCLIENT.URLS.static_webclient);
                    }
                };
                
                config["share"] = {
                    "label" : "Create share",
                    "_disabled": function(){
                        var selected = $.jstree.reference('#dataTree').get_selected(true);
                        var enabled = true;
                        $.each(selected, function(index, node) {
                            if (node.type != 'image' || !OME.nodeHasPermission(node, 'canLink')) {
                                enabled = false;
                                // Break out of $.each
                                return false;
                            }
                        });
                        return !enabled;
                    },
                    "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_toolbar_share2.png',
                    "action": function(){
                        // We get_selected() within createShare()
                        OME.createShare();
                    }
                };

                config["renderingsettings"] = {
                    "label" : "Rendering Settings...",
                    "_disabled": true,
                    "action" : false,
                    "submenu" : {
                        "copy_rdef"  : {
                            "label" : "Copy",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_copy_16.png',
                            "action": function() {
                                var inst = $.jstree.reference('#dataTree');
                                OME.copyRenderingSettings(WEBCLIENT.URLS.copy_image_rdef_json,
                                    inst.get_selected(true));
                            }
                        },
                        "paste_rdef": {
                            "label" : "Paste and Save",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_paste_16.png',
                            "action": function() {
                                var inst = $.jstree.reference('#dataTree');
                                OME.pasteRenderingSettings(WEBCLIENT.URLS.copy_image_rdef_json,
                                    inst.get_selected(true));
                            }
                        },
                        "reset_rdef": {
                            "label" : "Set Imported and Save",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_paste_16.png',
                            "action": function() {
                                var inst = $.jstree.reference('#dataTree');
                                OME.resetRenderingSettings(WEBCLIENT.URLS.reset_rdef_json,
                                    inst.get_selected(true));
                            }
                        },
                        "owner_rdef": {
                            "label" : "Set Owner's and Save",
                            "_disabled": true,
                            "icon"  : WEBCLIENT.URLS.static_webclient + 'image/icon_basic_paste_16.png',
                            "action": function() {
                                var inst = $.jstree.reference('#dataTree');
                                OME.applyOwnerRenderingSettings(WEBCLIENT.URLS.reset_owners_rdef_json,
                                    inst.get_selected(true));
                            }
                        }
                    }
                };

                // List of permissions related disabling
                // use canLink, canDelete etc classes on each node to enable/disable right-click menu

                var userId = WEBCLIENT.active_user_id,
                    canCreate = (userId === WEBCLIENT.USER.id || userId === -1),
                    canLink = OME.nodeHasPermission(node, 'canLink'),
                    parentAllowsCreate = (node.type === "orphaned" || node.type === "experimenter");


                // Although not everything created here will go under selected node,
                // we still don't allow creation if linking not allowed
                if(canCreate && (canLink || parentAllowsCreate)) {
                    // Enable tag or P/D/I submenus created above
                    config["create"]["_disabled"] = false;
                    if (tagTree) {
                        config["create"]["submenu"]["tagset"]["_disabled"] = false;
                        config["create"]["submenu"]["tag"]["_disabled"] = false;
                    } else {
                        config["create"]["submenu"]["project"]["_disabled"] = false;
                        config["create"]["submenu"]["dataset"]["_disabled"] = false;
                        config["create"]["submenu"]["screen"]["_disabled"] = false;
                    }
                }

                // Disable delete if no canDelete permission
                if (OME.nodeHasPermission(node, 'canDelete')) {
                    config["delete"]["_disabled"] = false;
                }

                // Enable 'Move to group' if 'canChgrp'
                if(OME.nodeHasPermission(node, 'canChgrp')) {
                    // Can chgrp everything except Plate 'run', 'tag' and 'tagset'
                    if (["acquisition", "tag", "tagset"].indexOf(node.type) === -1) {
                        config["chgrp"]["_disabled"] = false;
                    }
                }

                if (canLink) {
                    var to_paste = false,
                        buffer = this.get_buffer(),
                        parent_id = node.parent,
                        parent_type = this.get_node(parent_id).type,
                        node_type = node.type;

                    if(this.can_paste() && buffer.node) {
                        to_paste = buffer.node[0].type;
                    }

                    // Currently we allow to Cut, even if we don't delete parent link!
                    // E.g. can Cut orphaned Image or orphaned Dataset. TODO: review this!
                    var canCut = (["dataset", "image", "plate", "tag"].indexOf(node_type) > -1);
                    // In Tag tree. Don't allow cut under tag
                    if (tagTree && node_type !== "tag") {
                        canCut = false;
                    }

                    // Currently we only allow Copy if parent is compatible?! TODO: review this!
                    var canCopy = ((node_type === "dataset" && parent_type === "project") ||
                                    (node_type === "image" && parent_type === "dataset") ||
                                    (node_type === "plate" && parent_type === "screen") ||
                                    (node_type === "tag" && parent_type === "tagset"));
                    // In Tag tree, can't Copy anything except tag
                    if (tagTree && node_type !== "tag"){
                        canCopy = false;
                    }

                    var canPaste = ((node_type === "project" && to_paste === "dataset") ||
                                    (node_type === "dataset" && to_paste === "image") ||
                                    (node_type === "screen" && to_paste === "plate") ||
                                    (node_type === "tagset" && to_paste === "tag"));
                    if (canCut || canCopy || canPaste){
                        config["ccp"]["_disabled"] = false;
                        config["ccp"]["submenu"]["cut"]["_disabled"] = !canCut;
                        config["ccp"]["submenu"]["copy"]["_disabled"] = !canCopy;
                        config["ccp"]["submenu"]["paste"]["_disabled"] = !canPaste;
                    }
                }

                // If 'canAnnotate' we can Paste rdefs to various nodes
                if (OME.nodeHasPermission(node, 'canAnnotate')) {
                    if (node.type === 'dataset' ||
                        node.type === 'plate' ||
                        node.type === 'acquisition' ||
                        node.type === 'image') {

                        config['renderingsettings']['_disabled'] = false;
                        config['renderingsettings']["submenu"]['paste_rdef']['_disabled'] = false;
                        config['renderingsettings']["submenu"]['reset_rdef']['_disabled'] = false;
                        config['renderingsettings']["submenu"]['owner_rdef']['_disabled'] = false;
                    }
                }
                // Only enable copying if an image is the node
                if (node.type === 'image') {
                    config['renderingsettings']['_disabled'] = false;
                    config['renderingsettings']["submenu"]['copy_rdef']['_disabled'] = false;
                }
                return config;
            }

        },
        // TODO Performance of sort may not be realistic. The tree is mostly ordered correctly
        // already, only insertions need to be corrected manually.
        'sort': function(nodeId1, nodeId2) {
            var inst = this;
            var node1 = inst.get_node(nodeId1);
            var node2 = inst.get_node(nodeId2);

            function getRanking(node) {
                // return rank based on 'omero.client.ui.tree.type_order' list
                // first type is ranked 1 (the highest), last  is the lowest
                var rank = WEBCLIENT.UI.TREE.type_order.indexOf(node.type);
                if (rank > -1) {
                    return rank;
                }
                // types not specified in 'omero.client.ui.tree.type_order'
                // are sorted as loaded to jquery based on sql
                return WEBCLIENT.UI.TREE.type_order.length + 1;
            }

            function sortingStrategy(node1, node2) {
                // sorting strategy

                // If the nodes are experimenters and one of them is the current user.
                if(node1.type === 'experimenter') {
                    if (node1.data.obj.id === WEBCLIENT.USER.id) {
                        return -1;
                    } else if (node2.data.obj.id === WEBCLIENT.USER.id) {
                        return 1;
                    }
                }
                var name1 = node1.text.toLowerCase();
                var name2 = node2.text.toLowerCase();

                // If names are same, sort by ID
                if (name1 === name2) {
                    return node1.data.obj.id <= node2.data.obj.id ? -1 : 1;
                }
                return name1 <= name2 ? -1 : 1;
            }

            // if sorting list is turned off mix object and sort by name
            if (WEBCLIENT.UI.TREE.type_order.indexOf('false') > -1) {
                return sortingStrategy(node1, node2);
            }
            // If the nodes are the same type then just compare lexicographically
            if (node1.type === node2.type && node1.text && node2.text) {
                return sortingStrategy(node1, node2);
            // Otherwise explicitly order the type that might be siblings
            } else {

                var ranking1 = getRanking(node1);
                var ranking2 = getRanking(node2);
                return ranking1 <= ranking2 ? -1 : 1;
            }
        }
    });
});