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
                    var lp_width = $("#left_panel").width() - left - 65;  // margins
                    var fullWidth = anchor.width(); // we start with full text from redraw_node() above

                    if (fullWidth > lp_width) {
                        // Calculate the reduction required, +3 for the '...'
                        var letterWidth = fullWidth / node.text.length;
                        var truncated = node.text.slice(3 + Math.floor((fullWidth - lp_width) / letterWidth));
                        // update the text
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