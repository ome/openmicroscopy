// conditional select
(function ($, undefined) {
    "use strict";

    // extending the defaults
    $.jstree.defaults.conditionalselect = {
        // The function to be used to conditionally select
        conditionalselect_function: function(node) {
            return true;
        }
    };

    $.jstree.plugins.conditionalselect = function (options, parent) {
        // own function
        this.select_node = function (obj, supress_event, prevent_open) {
            if(this.settings.conditionalselect.conditionalselect_function.call(this, this.get_node(obj))) {
                parent.select_node.call(this, obj, supress_event, prevent_open);
            }
        };
    };
})(jQuery);