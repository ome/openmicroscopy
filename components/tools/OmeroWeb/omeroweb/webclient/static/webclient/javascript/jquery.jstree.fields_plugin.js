/*global jQuery */
// wrap in IIFE and pass jQuery as $

// This is identical to the pagination plugin in every way except it uses a different variable
// and function names. Mostly this is used because fields are indexed from zero where pages are
// indexed from 1 so it can be a little confusing if mixed

(function ($, undefined) {
    "use strict";

    // The map to store fields
    // This is a an object with no properties to avoid having to repeatedly
    // check 'hasOwnProperty'
    var field_map = Object.create(null);

    // extending the defaults
    $.jstree.defaults.fields = {
        // No configurable options at this time
    };

    $.jstree.plugins.fields = function (options, parent) {

        this.get_field = function(node) {
            if (node.id in field_map) {
                return field_map[node.id];
            }
            return 0;
        }

        this.set_field = function(node, field) {
            /* Set the field for a node
             *
            */
            field_map[node.id] = field;
        }

        this._remove_field = function(node) {
            // Update the mapping
            var inst = this;

            // check node is valid
            if(!node) {
                return false;
            }

            // If this key is present in the map, then
            // remove this node from it
            if (node.id in field_map) {
                delete field_map[node.id];
            }
            return node; // TODO What to return if anything?

        };

        this._clear_fields = function() {
            field_map = Object.create(null);
        }

        // bind events if needed
        this.bind = function () {
            // call parent function first
            parent.bind.call(this);
            this.element
                .on("delete_node.jstree", $.proxy(function (event, data) {
                    var inst = this;

                    function traverse(state) {
                        inst._remove_field(state);
                        if (inst.is_parent(state)) {
                            $.each(state.children, function(index, child) {
                                traverse(inst.get_node(child));
                            });
                        }
                    };

                    traverse(data.node);

                }, this));

        };

        // Refresh is special cased to remove the current fields
        this.refresh = function (skip_loading) {
            this._clear_fields();
            return parent.refresh.call(this, skip_loading);
        };
    };

    // you can include the plugin in all instances by default
    // $.jstree.defaults.plugins.push("pagination");
})(jQuery);