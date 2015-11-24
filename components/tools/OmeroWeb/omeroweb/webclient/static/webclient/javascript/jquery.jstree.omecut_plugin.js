/*global jQuery */
// wrap in IIFE and pass jQuery as $

(function ($, undefined) {
    "use strict";

    // Variable to store the ome specific cut status
    var omecut = false;
    // It is impossible to manipulate these so have to have a custom set
    var ccp_node = false;
    var ccp_mode = false;
    var ccp_inst = false;

    // extending the defaults
    $.jstree.defaults.omecut = {
        // The function to be used to get the key:value to add to the map
        // Called whenever an object is created
        path_url: false
    };

    $.jstree.plugins.omecut = function (options, parent) {

        this._clear_omecut = function() {
            omecut = false;
            ccp_node = false;
            ccp_mode = false;
            ccp_inst = false;
        };

        this._get_node_data = function(node) {
            var new_node_data = {
                // Clone the data
                'data': JSON.parse(JSON.stringify(node.data)),
                'text': node.text,
                'type': node.type,
                // If it has children, we wish it to be loadable, but not loaded
                // so just give it boolean instead of actual nodes
                'children': this.is_parent(node),
                'li_attr': {
                    'class': node.type,
                    'data-id': node.data.obj.id
                }
            };
            return new_node_data;
        };

        // Save to special variable on cut, mutually exclusive with standard paste buffer
        this.cut = function (obj) {
            // Copied from jstree.js
            if(!obj) { obj = this._data.core.selected.concat(); }
            if(!$.isArray(obj)) { obj = [obj]; }
            if(!obj.length) { return false; }
            var tmp = [], o, t1, t2;
            for(t1 = 0, t2 = obj.length; t1 < t2; t1++) {
                o = this.get_node(obj[t1]);
                if(o && o.id && o.id !== '#') { tmp.push(o); }
            }
            if(!tmp.length) { return false; }

            var inst = this;

            // Empty standard buffer in case it has copy data in it
            inst.clear_buffer();

            // Prepare the cut buffer
            var omecut_buffer = [];

            // Get the contents of the nodes for storage
            // These are stored as the objects jstree will need to create new nodes
            $.each(tmp, function(index, node) {
                var new_node_data = inst._get_node_data(node);

                omecut_buffer.push(new_node_data);
            });

            // Set the buffers including the ome flag
            ccp_mode = 'move_node';
            ccp_inst = inst;
            ccp_node = omecut_buffer;
            omecut = true;

            // Unless this is being cut from an orphaned directory (as those
            // remain in place when cut) select the new parent first regardless
            // of if it is being moved or deleted
            var firstParent = inst.get_node(inst.get_parent(tmp[0]));
            if (firstParent.type !== 'orphaned' && firstParent.type !== 'experimenter') {
                inst.deselect_all(true);
                inst.select_node(firstParent);
            }

            // Replicate existing cut functionality
            // TODO Handle this as a single ajax query by adding view functionality for getting multiple paths
            // Handle this as a separate each for now
            $.each(tmp, function(index, node) {
                var parent = inst.get_node(inst.get_parent(node));
                // remove node...
                inst.delete_node(node);
                

                // Objects which were already orphaned require no action except to be added to the paste buffer
                if (parent.type !== 'experimenter' &&
                    parent.type !== 'orphaned') {

                    // Do the unlinking. Result will tell us whether object is orphaned
                    // If orphaned, move object under 'orphaned' or 'experimenter'
                    $.when(unlinkNode(inst, node, parent)).done(function(rsp) {

                        // Response contains any remaining parent links in the form
                        // e.g. {"dataset":{"10":{"image":[1,2,3]}}}
                        var orphaned = true;
                        if (parent.type in rsp) {
                            for (var pid in rsp[parent.type]) {
                                var children = rsp[parent.type][pid];
                                if (node.type in children && children[node.type].indexOf(node.data.obj.id) > -1) {
                                    orphaned = false;
                                }
                            }
                        }
                        if (orphaned) {
                            // Get the experimenter that owns this object
                            // This handles the multi-experimenters shown case
                            var ownerExperimenter = inst.locate_node('experimenter-' + activeUserId())[0],
                                newParent;
                            var new_node_data = inst._get_node_data(node);
                            // Newly orphaned objects get moved to the appropriate location

                            if (node.type === 'dataset' || node.type === 'plate') {
                                newParent = ownerExperimenter;
                            } else if (node.type === 'image') {
                                // Get the orphaned directory for this experimenter
                                newParent = inst.locate_node('orphaned-' + ownerExperimenter.data.obj.id)[0];
                            }
                            inst.create_node(newParent, new_node_data);
                        } else {
                            // Remove node from other identical nodes as well
                            updateParentRemoveNode(inst, node, parent);

                            // Update the child counts
                            OME.updateNodeChildCount(inst, parent);
                        }
                        // Remove thumbs from the central panel
                        var e = {'type': 'delete_node'};
                        var data = {'node': node,
                                    'old_parent': parent};
                        update_thumbnails_panel(e, data);
                    });
                }
            });

            // TODO Probably should be called inside success of ajax success function, but do it here for now
            // until there is only one ajax call
            /**
             * triggered when nodes are added to the buffer for moving
             * @event
             * @name cut.jstree
             * @param {Array} node
             */
            this.trigger('cut', { "node" : obj });
        };

        // Ensure that special variable is cleared on copy
        this.copy = function (obj) {
            var inst = this;
            inst._clear_omecut();
            parent.copy.call(this, obj);
        };

        this.can_paste = function () {
            var inst = this;
            return (omecut && ccp_node.length > 0) || parent.can_paste.call(this);
        };

        this.paste = function (obj, pos) {
            var inst = this;

            // If we've just done an 'omecut' (above), then custom paste ONLY creates new links
            if (omecut && ccp_node.length > 0) {
                obj = this.get_node(obj);
                var newNodes = [];
                $.each(ccp_node, function(index, new_node_data) {

                    // If this is being pasted from an orphaned state then that
                    // orphan now needs to be removed
                    var located = inst.locate_node(new_node_data.type + '-' + new_node_data.data.obj.id);
                    if (located.length === 1) {
                        var parent = inst.get_node(inst.get_parent(located[0]));
                        if (parent.type === 'experimenter' || parent.type === 'orphaned') {
                            inst.delete_node(located);
                            // Update child count
                            OME.updateNodeChildCount(inst, parent);
                        }
                    }

                    // Create the node
                    var nodeId = inst.create_node(obj, new_node_data, pos);
                    var node = inst.get_node(nodeId);

                    // Remove potential duplicate node
                    removeDuplicate(inst, node, obj);

                    // Persist link
                    $.when(linkNode(inst, node, obj)).done(function() {
                        // Update the central panel
                        var e = {'type': 'create_node'};
                        var data = {'node': node,
                                    'parent': obj};
                        update_thumbnails_panel(e, data);
                    });

                    // Add to other identical nodes as well
                    updateParentInsertNode(inst, node, obj, pos);

                    newNodes.push(node);
                });

                /**
                 * triggered when paste is invoked
                 * @event
                 * @name paste.jstree
                 * @param {String} parent the ID of the receiving node
                 * @param {Array} node the nodes in the buffer
                 * @param {String} mode the performed operation - "copy_node" or "move_node"
                 */
                this.trigger('paste', { "parent" : obj.id, "node" : newNodes, "mode" : 'move_node' });

            // Do ordinary paste
            } else {
                parent.paste.call(this, obj, pos);
            }
            // Clear ome paste buffer
            inst._clear_omecut();

        };

        this.get_buffer = function() {
            if (omecut) {
                return { 'mode' : ccp_mode, 'node' : ccp_node, 'inst' : ccp_inst };
            }
            return parent.get_buffer.call(this);
        };

        // Types plugin does not work unless the nodes really exist so
        // we have to manually check in the case of this being an omecut
        this.check = function (chk, obj, par, pos, more) {
            // If this is an omecut and the object does not have an instance (i.e. it
            // is an attempt to check the stored data, not a node)
            if (omecut &&
                obj instanceof Object &&
                !('inst' in obj)) {

                var inst = this;
                par = inst.get_node(par);
                var rules = inst.get_rules(par);
                if (rules.valid_children != -1) {
                    var validChildren = rules.valid_children;
                    // Check if the child type is allowed
                    if (validChildren.indexOf(obj.type) != -1) {
                        return true;
                    }
                }
                return false;
            }
            return parent.check.call(this, chk, obj, par, pos, more);
        };
    };

    // you can include the plugin in all instances by default
    // $.jstree.defaults.plugins.push("omecut");
})(jQuery);