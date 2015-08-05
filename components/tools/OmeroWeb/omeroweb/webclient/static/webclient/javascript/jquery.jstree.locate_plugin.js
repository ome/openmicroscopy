/*global jQuery */
// wrap in IIFE and pass jQuery as $

(function ($, undefined) {
    "use strict";

    // The map to store lookups
    // This is a an object with no properties to avoid having to repeatedly
    // check 'hasOwnProperty'
    var locate_map = Object.create(null);

    // extending the defaults
    $.jstree.defaults.locate = {
        // The function to be used to get the key:value to add to the map
        // Called whenever an object is created
        locate_function: function(node) {
            return false;
        }
    };

    $.jstree.plugins.locate = function (options, parent) {

        this.locate_key = function(node) {
            /* Generate the key for a node
             *
            */
            var lf = this.settings.locate.locate_function;
            if (lf !== undefined && $.isFunction(lf)) {
                return lf.call(this, node);
            }
            return false;
        };

        this.locate_node = function (key, ancestor) {
            /* Return the nodes that match this id converted from
             * the object masquerading as a set into an array
             * key - Key to locate
             * ancestor - Restrict results to descendants of this node id
             * Ancestor is never included in results
            */

            var located = locate_map[key];
            // If nothing was found, return false
            if (located === undefined) {
                return false;
            }

            // Otherwise, convert the object masquerading as a set into an array
            var locatedArray = [];
            for (var property in located) {
                locatedArray.push(located[property]);
            }

            // Remove nodes that are not descended from ancestor node
            if (ancestor) {
                var inst = this;

                var traverse = function(state) {
                    if (!state) return false;
                    var parentNode = inst.get_node(inst.get_parent(state));
                    if (parentNode === ancestor) {
                        // Found, bail out
                        return true;
                    } else if (parentNode.id === '#') {
                        // Top of the tree, bail out
                        return false;
                    } else {
                        // Traverse upwards
                        return traverse(parentNode);
                    }
                };

                var descendantArray = [];
                $.each(locatedArray, function(index, loc) {
                     if (traverse(loc)) {
                        descendantArray.push(loc);
                     }
                });
                locatedArray = descendantArray;
            }

            return locatedArray;
        };

        this._locate_add = function(node) {
            // Update the mapping
            var inst = this;

            node = node.parents ? node : inst.get_node(node);
            // check node is valid
            if(!node || node.id === '#' || !node.parents) {
                return false;
            }

            // Ensure that a key was returned
            var key = this.locate_key(node);
            if (!key) {
                return false;
            }

            // If the map already does not have any values for this key, initalise an
            // object with no properties so we don't have to repeatedly check 'hasOwnProperty'
            if (!(key in locate_map)) {
                locate_map[key] = Object.create(null);
            }
            // This is an object masquerading as a set
            locate_map[key][node.id] = node;

            inst.trigger('locate_update.jstree', node);
            return node; // What to return if anything?
        };

        this._locate_remove = function(node) {
            // Update the mapping
            var inst = this;

            node = node.parents ? node : inst.get_node(node);
            // check node is valid
            if(!node || node.id === '#' || !node.parents) {
                return false;
            }

            // Ensure that a key was returned
            var key = this.locate_key(node);
            if (!key) {
                return false;
            }

            // If this key is present in the map, then
            // remove this node from it
            if (key in locate_map) {
                delete locate_map[key][node.id];
                // Remove key completely if there are no members left
                if ( Object.keys(locate_map[key]).length === 0) {
                    delete locate_map[key];
                }
            }
            return node; // What to return if anything?

        };

        this._locate_clear = function() {
            locate_map = Object.create(null);
        };

        // // *SPECIAL* FUNCTIONS
        // this.init = function (el, options) {
        //     // do not forget parent
        //     parent.init.call(this, el, options);
        // };
        // bind events if needed
        this.bind = function () {
            // call parent function first
            parent.bind.call(this);
            this.element
                // .on("create_node.jstree", function () {
                //     console.log('custom create_node.jstree event');
                //         console.log(this);
                //     })
                // .on("load_node.jstree", function () {
                //     console.log('custom load_node.jstree event');
                //         console.log(this);
                //     })
                // .on("model.jstree", function () {
                //     console.log('custom model.jstree event');
                //         console.log(this);
                //     });
                // .on("create_node.jstree", $.proxy(function (event, data) {
                //     console.log('proxied create_node.jstree event');
                //     var inst = this;
                //     // console.log(inst);
                //     // console.log('data');
                //     // console.log(data);

                //     var node = data.node;
                //     if (node.hasOwnProperty('data')) {
                //         console.log('Mapping ' + node.type + '-' + node.data.obj.id + ' to ' + node.id);
                //     }

                // }, this))
                // .on("load_node.jstree", $.proxy(function (event, data) {
                //     console.log('proxied load_node.jstree event');
                //     var inst = this;
                //     // console.log(inst);
                //     // console.log('data');
                //     // console.log(data);
                //     var node = data.node;
                //     console.log('Loading: ' + data.node.id);
                //     // $.each(data.nodes, function(index, nodeId) {
                //     //     var node = inst.get_node(nodeId);
                //     //     if (node.hasOwnProperty('data')) {
                //     //         console.log('Mapping ' + node.type + '-' + node.data.obj.id + ' to ' + node.id);
                //     //     }
                //     // });
                // }, this));
                .on("model.jstree", $.proxy(function (event, data) {
                    var inst = this;
                    $.each(data.nodes, function(index, nodeId) {
                        inst._locate_add(nodeId);
                    });
                }, this))

                .on("create_node.jstree", $.proxy(function (event, data) {
                    var inst = this;
                    inst._locate_add(data.node);
                }, this))

                .on("delete_node.jstree", $.proxy(function (event, data) {
                    var inst = this;

                    function traverse(state) {
                        inst._locate_remove(state);
                        if (inst.is_parent(state)) {
                            var node = inst.get_node(state);
                            $.each(node.children, function(index, child) {
                                traverse(child);
                            });
                        }
                    }

                    traverse(data.node.id);

                }, this));

        };
        // // unbind events if needed (all in jquery namespace are taken care of by the core)
        // this.unbind = function () {
        //     // do(stuff);
        //     // call parent function last
        //     parent.unbind.call(this);
        // };
        // this.teardown = function () {
        //     // do not forget parent
        //     parent.teardown.call(this);
        // };
        // // state management - get and restore
        // this.get_state = function () {
        //     // always get state from parent first
        //     var state = parent.get_state.call(this);
        //     // add own stuff to state
        //     state.sample = { 'var' : 'val' };
        //     return state;
        // };
        // this.set_state = function (state, callback) {
        //     // only process your part if parent returns true
        //     // there will be multiple times with false
        //     if(parent.set_state.call(this, state, callback)) {
        //         // check the key you set above
        //         if(state.sample) {
        //             // do(stuff); // like calling this.sample_function(state.sample.var);
        //             // remove your part of the state, call again and RETURN FALSE, the next cycle will be TRUE
        //             delete state.sample;
        //             this.set_state(state, callback);
        //             return false;
        //         }
        //         // return true if your state is gone (cleared in the previous step)
        //         return true;
        //     }
        //     // parent was false - return false too
        //     return false;
        // };

        // Copy node is special cased in case the copy_node.jstree event is
        // being used to copy any additional data attriute to the copied node
        // This data copy is not done by core jstree intentionally so it will
        // not be present when the core copy_node function fires the model.jstree
        // event
        this.copy_node = function (obj, par, node, pos, callback, is_loaded) {
            var copied_node = parent.copy_node.call(this, obj, par, node, pos, callback, is_loaded);
            // copy_node calls itself if it is presented with an array, ignore those cases
            if (!$.isArray(obj) &&
                (typeof copied_node == 'string' || copied_node instanceof String)) {
                this._locate_add(copied_node);
            }
            return copied_node;
        };

        // Refresh is special cased to remove the current mapping before the refresh
        // actually occurs as it will be rebuilt. If the nodes are identical then this
        // would not be necessary but if the client has nodes the server does not it
        // will be left out of sync
        this.refresh = function (skip_loading) {
            this._locate_clear();
            return parent.refresh.call(this, skip_loading);
        };

        // Refresh node is special cased to remove any current mapping before the refresh
        // node actually occurs. If the nodes are identical then this would not be necessary
        // but if the client has nodes the server does not it will be left out of sync
        this.refresh_node = function (obj) {
            var inst = this;

            function traverse(state) {
                if (inst.is_parent) {
                    var node = inst.get_node(state);
                    $.each(node.children, function(index, child) {
                        inst._locate_remove(child);
                        traverse(child);
                        // The node itself is not reloaded, just the children
                        // so remove here
                    });
                }
            }

            traverse(obj);
            return parent.refresh_node.call(this, obj);
        };
    };

    // you can include the plugin in all instances by default
    // $.jstree.defaults.plugins.push("locate");
})(jQuery);