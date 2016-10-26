
(function(){

    // Used by WellIndexForm in forms.py
    window.changeField = function changeField(field) {

        var datatree = $.jstree.reference('#dataTree');
        var $container = $("#content_details");

        var containerType = $container.data('type');
        var containerId = $container.data('id');
        var containerPath = $container.data('path');
        containerPath = JSON.parse(containerPath);
        var containerNode = datatree.find_omepath(containerPath);

        if (!containerNode) {
            console.log('WARNING: Had to guess container');
            containerNode = OME.getTreeBestGuess(containerType, containerId);
        }

        // Set the field for that node in the tree and reload the tree section
        datatree.set_field(containerNode, field);

        // Reselect the same node to trigger update
        datatree.deselect_all(true);
        datatree.select_node(containerNode);

        return false;
    }

    var primaryIndex = -1;
    if (OME === undefined) {
        window.OME = {};
    }
    OME.handleClickSelection = function (event, target, elementsSelector) {
        
        var $clickedImage = target || $(event.target);
        
        var thumbs = $(elementsSelector);
        var selIndex = thumbs.index($clickedImage);

        if (event && event.shiftKey ) {
            if ( primaryIndex == -1 ) {
                primaryIndex = selIndex;
                $clickedImage.parent().addClass("ui-selected");
                return;
            }
            
            // select range
            var start = Math.min(primaryIndex,selIndex);
            var end = Math.max(primaryIndex,selIndex);
            
            thumbs.slice(start, end+1).parent().addClass("ui-selected");
            
        }
        else if (event && event.metaKey) {
            if ( primaryIndex == -1 ) {
                primaryIndex = selIndex;
            }
            
            if($clickedImage.parent().hasClass("ui-selected")) {
                $clickedImage.parent().removeClass("ui-selected");
            } else {
                $clickedImage.parent().addClass("ui-selected");
            }
        }
        else {
            thumbs.parent().removeClass("ui-selected");
            $clickedImage.parent().addClass("ui-selected");
            primaryIndex = selIndex;
        }
    }

})();