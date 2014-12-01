/*global jQuery */
// wrap in IIFE and pass jQuery as $

(function ($, undefined) {
    "use strict";

    $.jstree.plugins.truncatetext = function (options, parent) {

        this.redraw_node = function(obj, deep, is_callback, force_render) {
            var inst = this;
            obj = parent.redraw_node.apply(this, arguments);

            if(obj) {
                var node = inst.get_node(obj),
                    $obj = $(obj),
                    anchor = $obj.children('a.jstree-anchor');

                // Timout allows element to be painted so offset() is available
                setTimeout(function(){
                    var offset = $("#" + node.id).offset();
                    var left = offset ? offset.left : 36;

                    // Truncate where necessary
                    var lp_width = $("#left_panel").width() - left - 75;  // margins

                    if (anchor.textWidth(node.text) > lp_width) {
                        // Optimize by calculating the estimated reduction required
                        var letterWidth = anchor.textWidth('a');
                        // +3 for the '...'
                        var truncated = node.text.slice(3 + Math.floor((anchor.textWidth(node.text) - lp_width) / letterWidth));

                        // If it's still too long, iterate until it isn't
                        while (anchor.textWidth(truncated) > lp_width) {
                            truncated = truncated.slice(1);
                        }

                        anchor.contents().filter(function(){
                            return this.nodeType == 3;
                        }).replaceWith(document.createTextNode('...' + truncated));
                    }
                },10);
            }
            return obj;
        };
    };

    // you can include the plugin in all instances by default
    // $.jstree.defaults.plugins.push("truncatetext");
})(jQuery);