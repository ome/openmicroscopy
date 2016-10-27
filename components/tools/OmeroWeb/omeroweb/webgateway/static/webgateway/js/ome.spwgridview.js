
(function(){

    if (OME === undefined) {
        window.OME = {};
    }

    OME.WellBirdsEye = function() {

        var $left_panel_tabs = $("#left_panel_tabs");
        var $left_panel_bottom = $("#left_panel_bottom");
        var $well_birds_eye = $("#well_birds_eye");

        var minX,
            maxX,
            minY,
            maxY;

        function showPanel() {
            var height = 300;
            $left_panel_tabs.css('bottom', height + 'px');
            $left_panel_bottom.css('height', height + 'px');
        }

        function getPos(attr) {
            return function(ws) {
                return ws.position[attr] ? ws.position[attr].value : undefined;
            }
        }
        function notUndef(p) {
            return p !== undefined;
        }

        return {
            clear: function() {
                $well_birds_eye.empty();
            },
            addWell: function(data) {
                showPanel();
                console.log('addWell', data);

                // first filter for well-samples that have positions
                data = data.filter(function(ws){ return ws.position !== undefined });

                var xVals = data.map(getPos('x')).filter(notUndef);
                var yVals = data.map(getPos('y')).filter(notUndef);
                minX = Math.min.apply(null, xVals);
                maxX = Math.max.apply(null, xVals);
                var midX = ((maxX - minX)/2) + minX;
                minY = Math.min.apply(null, yVals);
                maxY = Math.max.apply(null, yVals);

                var html = data.map(function(ws){
                    // check if min===max to avoid zero-division error
                    var x = maxX === minX ? 0.5 : (ws.position.x.value - minX)/(maxX - minX);
                    var y = maxY === minY ? 0.5 : (ws.position.y.value - minY)/(maxY - minY);
                    return '<img style="left: ' + (x * 100) + '%; top: ' + (y * 100) + '%" title="' + ws.name + '" data-imageId="' + ws.id + '" src="' + ws.thumb_url + '" />';
                }, "");
                $well_birds_eye.append(html.join(""));
            }
        }
    }

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