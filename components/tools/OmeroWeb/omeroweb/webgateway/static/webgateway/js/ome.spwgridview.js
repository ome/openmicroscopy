
(function(){

    if (OME === undefined) {
        window.OME = {};
    }

    // Can be called from anywhere, E.g. center_plugin.thumbs
    OME.emptyWellBirdsEye = function() {
        $("#well_birds_eye").empty()
            .off("click");
    };
    OME.hideWellBirdsEye = function() {
        $("#left_panel_tabs").css('bottom', '0');
        $("#left_panel_bottom").css('height', '0');
        // Also clear content
        OME.emptyWellBirdsEye();
    };


    OME.WellBirdsEye = function(opts) {

        var $left_panel_tabs = $("#left_panel_tabs");
        var $left_panel_bottom = $("#left_panel_bottom");
        var $well_birds_eye = $("#well_birds_eye");

        function selectionChanged() {
            var imageIds = [];
            $('.ui-selected', $well_birds_eye).each(function(ws){
                imageIds.push(parseInt(this.getAttribute('data-imageId'), 10));
            });
            if (opts.callback) {
                opts.callback(imageIds);
            }
        };

        // Drag selection on WellSample images
        $("#well_birds_eye_container").selectable({
            filter: 'img',
            distance: 2,
            stop: function(){
                selectionChanged();
            }
        });
        // Handle click on image
        $well_birds_eye.on( "click", "img", function(event) {
            if (event.metaKey) {
                // Ctrl click - simply toggle clicked image
                $(event.target).toggleClass('ui-selected');
            } else {
                // select ONLY clicked image
                $("img", $well_birds_eye).removeClass('ui-selected');
                $(event.target).addClass('ui-selected');
            }
            selectionChanged();
        });

        function showPanel() {
            var height = 240;
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

        // 'public' methods returned...
        return {
            clear: function() {
                $well_birds_eye.empty();
            },
            setSelected: function(imageIds) {
                $("img", $well_birds_eye).removeClass('ui-selected');
                imageIds.forEach(function(iid){
                    $("img[data-imageId=" + iid + "]", $well_birds_eye).addClass('ui-selected');
                });
            },
            addWell: function(data) {
                showPanel();

                var minX,
                    maxX,
                    minY,
                    maxY;

                // first filter for well-samples that have positions
                data = data.filter(function(ws){ return ws.position !== undefined });

                var xVals = data.map(getPos('x')).filter(notUndef);
                var yVals = data.map(getPos('y')).filter(notUndef);
                minX = Math.min.apply(null, xVals);
                maxX = Math.max.apply(null, xVals);
                var midX = ((maxX - minX)/2) + minX;
                minY = Math.min.apply(null, yVals);
                maxY = Math.max.apply(null, yVals);

                // Resize the well_birds_eye according to extent of field positions...
                var whRatio = 1;
                if (maxX !== minX || maxY !== minY) {
                    whRatio = (maxX - minX) / (maxY - minY);
                }
                var width = 200;
                var height = 200;
                var top = 4;
                if (whRatio > 1) {
                    height = 200/whRatio;
                    top = ((200 - height) / 2) + 4;
                } else {
                    width = whRatio * 200;
                }
                $well_birds_eye.css({'width': width + 'px', 'height': height + 'px', 'top': top + 'px'});

                // Add images, positioned by percent...
                var html = data.map(function(ws){
                    // check if min===max to avoid zero-division error
                    var x = (maxX === minX) ? 0.5 : (ws.position.x.value - minX)/(maxX - minX);
                    var y = (maxY === minY) ? 0.5 : (ws.position.y.value - minY)/(maxY - minY);
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