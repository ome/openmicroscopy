/*global jQuery */
// wrap in IIFE and pass jQuery as $

(function ($, undefined) {
    "use strict";

    $.jstree.plugins.ometools = function (options, parent) {

        this.get_omepath = function(obj) {
            /* Generate the path for a node as would be unique
             * in OMERO.
             * An array of objects with type and id.
            */
            var inst = this;

            var path = inst.get_path(obj, false, true);

            var omepath = [];
            $.each(path, function(index, nodeId) {
                 var node = inst.get_node(nodeId);
                 omepath.push({
                    'type': node.type,
                    'id': node.data.obj.id
                 });
            });

            return omepath;
        };

        this.find_omepath = function(omepath) {
            var inst = this;
            omepath = omepath.reverse();


             var potentials = inst.locate_node(omepath[0].type + '-' + omepath[0].id);
             var node = false;

             $.each(potentials, function(index, potential) {
                var n = potential;
                var match = true;

                $.each(omepath, function(index, pathObj) {
                    if (pathObj.type === n.type &&
                        pathObj.id === n.data.obj.id) {
                        n = inst.get_node(inst.get_parent(n));
                    } else {
                        // Break out of each
                        match = false;
                        return false;
                    }
                });

                if (match === true) {
                    node = potential;
                    // Break out of each
                    return false;
                }
             });

             return node;
        };

        this.omecompare = function(node1, node2) {
            var inst = this;
            node1 = inst.get_node(node1);
            node2 = inst.get_node(node2);
            return (node1.type === node2.type &&
                    node1.data.obj.id === node2.data.obj.id);
        };
    };

    // you can include the plugin in all instances by default
    // $.jstree.defaults.plugins.push("omepath");
})(jQuery);