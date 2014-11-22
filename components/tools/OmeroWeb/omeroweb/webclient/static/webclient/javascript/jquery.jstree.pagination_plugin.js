/*global jQuery */
// wrap in IIFE and pass jQuery as $

(function ($, undefined) {
    "use strict";

    // The map to store pages
    // This is a an object with no properties to avoid having to repeatedly
    // check 'hasOwnProperty'
    var page_map = Object.create(null);

    // extending the defaults
    $.jstree.defaults.pagination = {
        // No configurable options at this time
    };

    $.jstree.plugins.pagination = function (options, parent) {

        this.change_page = function(node, page) {
            this._set_page(node, page);
            this.refresh_node(node);
        }

        this.get_page = function(node) {
            if (node.id in page_map) {
                return page_map[node.id];
            }
            return 1;
        }

        this._set_page = function(node, page) {
            /* Set the page for a node
             *
            */
            page_map[node.id] = page;
        }

        this._remove_page = function(node) {
            // Update the mapping
            var inst = this;

            // check node is valid
            if(!node) {
                return false;
            }

            // If this key is present in the map, then
            // remove this node from it
            if (node.id in page_map) {
                delete page_map[node.id];
            }
            return node; // TODO What to return if anything?

        };

        this._clear_pages = function() {
            page_map = Object.create(null);
        }

        // bind events if needed
        this.bind = function () {
            // call parent function first
            parent.bind.call(this);
            this.element
                .on("delete_node.jstree", $.proxy(function (event, data) {
                    var inst = this;

                    function traverse(state) {
                        inst._remove_page(state);
                        if (inst.is_parent(state)) {
                            $.each(state.children, function(index, child) {
                                traverse(inst.get_node(child));
                            });
                        }
                    };

                    traverse(data.node);

                }, this));

        };

        // Refresh is special cased to remove the current pages
        this.refresh = function (skip_loading) {
            this._clear_pages();
            return parent.refresh.call(this, skip_loading);
        };
    };

    // you can include the plugin in all instances by default
    // $.jstree.defaults.plugins.push("pagination");
})(jQuery);