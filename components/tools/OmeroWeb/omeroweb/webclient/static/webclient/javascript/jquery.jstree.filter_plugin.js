

(function ($, undefined) {
    "use strict";

    $.jstree.plugins.filter = function (options, parent) {

        this.filter = function(obj, filterString) {
            // we are filtering images within a single parent obj (Dataset)
            if (!obj || !obj.id || obj.id === "#") {return false;}
            obj = this.get_node(obj);

            var selectedNodeIds = [],
                inst = this;

            // If parent node not selected,
            // get IDs of currently selected nodes
            if (!this.is_selected(obj)) {
                this.get_selected(true).forEach(function(n){
                    selectedNodeIds.push(n.type + "-" + n.data.obj.id);
                });
                this.deselect_all(true);
            }

            // One-time callback when refresh completes (triggers 'load_node')
            $("#dataTree").one("load_node.jstree", function reselectNodes() {
                // Try to reselect images that have not been filtered
                selectedNodeIds.forEach(function(id){
                    var n = inst.locate_node(id, obj)[0];
                    if (n) {
                        inst.select_node(n);
                    }
                });
                // if nothing was re-selected, need to trigger refresh
                if (inst.get_selected().length === 0) {
                    inst.select_node(obj, true);      // silent
                    inst.deselect_all();
                }
            });

            // add this to the data object...
            obj.data.obj.filter = filterString;

            // refresh parent node (will re-load filtered data)
            this.refresh_node(obj);
        };
    };

})(jQuery);