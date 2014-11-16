/*global jQuery */
// wrap in IIFE and pass jQuery as $

(function ($, undefined) {
    "use strict";

    $.jstree.plugins.childcount = function (options, parent) {

        this.redraw_node = function(obj, deep, is_callback, force_render) {
            var inst = this;

            obj = parent.redraw_node.apply(this, arguments);

            if(obj) {
                var node = inst.get_node(obj);
                if (node.data !== undefined && node.data.obj.childCount > 0) {
                    var span = document.createElement('span');
                    $(span).addClass('children_count');
                    var text = document.createTextNode(node.data.obj.childCount);
                    span.appendChild(text);
                    $(obj).children('a.jstree-anchor').append(span);
                }
            }
            return obj;
        };
    };

    // you can include the plugin in all instances by default
    // $.jstree.defaults.plugins.push("childcount");
})(jQuery);