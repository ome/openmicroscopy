/**
*  plugin for displaying ROIs over an image canvas *
*  Requires Raphael      http://raphaeljs.com/
*  and scale.raphael.js  http://shapevent.com/scaleraphael/
*/

$.fn.roi_display = function(options) {
    return this.each(function(){

        var self = this;
        var viewerId = this.id;

        var $viewportimg = $(this);
        var width = $viewportimg.attr('width');   // 0 initially
        var height = $viewportimg.attr('height');

        var tiles =  (options.tiles ? options.tiles : false);

        var canvas_class = (options.canvas_class ? options.canvas_class : 'weblitz-viewport-roi');

        if (!tiles) {
            // add our ROI canvas as a sibling to the image plane. Parent is the 'draggable' div
            var $dragdiv = $viewportimg.parent();
            var canvas_name = (options.canvas_name ? options.canvas_name : viewerId + '-roi');
            var $canvas =   $('<div id="'+canvas_name+'" class="'+canvas_class+'">').appendTo($dragdiv);
        } else {
            var canvas_name = (options.canvas_name ? options.canvas_name : viewerId + '-tiles-roi');
            var $canvas = $('#'+viewerId + '-tiles-roi')
        }

        if (options != null) {
            var orig_width = options.width;
            var orig_height = options.height;
            var json_url = options.json_url;
        }

        var roi_json = null;              // load ROI data as json when needed
        var active_rois = {};             // show only the active ROIs
        var external_rois = null;          // ROIs specified using an external software
        var original_shapes_backup = {};  // backup of the original configuration of shapes
        this.theZ = null;
        this.theT = null;
        var rois_displayed = false;         // flag to toggle visability.
        var roi_label_displayed = true;     // show/hide labels within shapes

        var selected_shape_id = null;  // html page is kept in sync with this
        var selectedClone = null;      // a highlighted shape cloned from currently selected shape

        // for keeping track of objects - E.g. de-select all.
        var shape_objects = new Array();

        // Creates Raphael canvas. Uses scale.raphael.js to provide paper.scaleAll(ratio);
        var paper = new ScaleRaphael(canvas_name, orig_width, orig_height);

        // convert given id to a number if possible
        var resolve_id = function(id) {
            if(isNaN(parseInt(id)))
                return id;
            else
                return parseInt(id);
        };

        // break long labels into multiple lines
        var formatShapeText = function(text_string) {
            var rows = parseInt(Math.sqrt(text_string.length / 6));     // rough ratio: cols = rows * 6
            var cols = parseInt(text_string.length/rows) + 1;
            if (text_string.length > cols) {
                var lines = [];
                var full_words = text_string.split(" ");
                var words = [];
                // first handle any words that are too long
                for (var w=0; w<full_words.length; w++) {
                    var full_word = full_words[w];
                    while (full_word.length > cols) {
                        words.push(full_word.substring(0, cols));
                        full_word = full_word.substring(cols);
                    }
                    words.push(full_word);
                }
                // now stitch words back into lines
                var line = "";
                for (var w=0; w<words.length; w++) {
                    var word = words[w];
                    if (line.length == 0) {
                        line = word;
                    }
                    else if (word.length + line.length > cols) {
                        lines.push(line);
                        line = word;
                    }
                    else {
                        line += (" " + word);
                    }
                }
                // handle the tail end
                if (line.length > 0)
                    lines.push(line);
                return lines.join("\n");
            }
            return text_string;
        }

        var draw_shape = function(shape) {
            var newShape = null;
            if (shape['type'] == 'Ellipse') {
              newShape = paper.ellipse(shape['cx'], shape['cy'], shape['rx'], shape['ry']);
            }
            else if (shape['type'] == 'Rectangle') {
              newShape = paper.rect(shape['x'], shape['y'], shape['width'], shape['height']);
            }
            else if (shape['type'] == 'Point') {
              newShape = paper.ellipse( shape['cx'], shape['cy'], 2, 2);
            }
            else if (shape['type'] == 'Line') {
              // define line as 'path': Move then Line: E.g. "M10 10L90 90"
              newShape = paper.path("M"+ shape['x1'] +" "+ shape['y1'] +"L"+ shape['x2'] +" "+ shape['y2'] );
            }
            else if (shape['type'] == 'PolyLine') {
              newShape = paper.path( shape['points'] );
            }
            else if (shape['type'] == 'Polygon') {
              newShape = paper.path( shape['points'] );
            }
            else if (shape['type'] == 'Label') {
              if (shape['textValue']) {
                  newShape = paper.text(shape['x'], shape['y'], shape['textValue'].escapeHTML()).attr({'text-anchor':'start'});
              }
            }
            // handle transforms. Insight supports: translate(354.05 83.01) and rotate(0 407.0 79.0)
            if (shape['transform']) {
                if (shape['transform'].substr(0, 'translate'.length) === 'translate'){
                    var tt = shape['transform'].replace('translate(', '').replace(')', '').split(" ");
                    var tx = parseInt(tt[0]);   // only int is supported by Raphael
                    var ty = parseInt(tt[1]);
                    newShape.translate(tx,ty);
                }
                else if (shape['transform'].substr(0, 'rotate'.length) === 'rotate'){
                    var tt = shape['transform'].replace('rotate(', '').replace(')', '').split(" ");
                    var deg = parseFloat(tt[0]);
                    var rotx = parseFloat(tt[1]);
                    var roty = parseFloat(tt[2]);
                    newShape.rotate(deg, rotx, roty);
                }
                else if (shape['transform'].substr(0, 'matrix'.length) === 'matrix'){
                    var tt = shape['transform'].replace('matrix(', '').replace(')', '').split(" ");
                    var a1 = parseFloat(tt[0]);
                    var a2 = parseFloat(tt[1]);
                    var b1 = parseFloat(tt[2]);
                    var b2 = parseFloat(tt[3]);
                    var c1 = parseFloat(tt[4]);
                    var c2 = parseFloat(tt[5]);
                    var tmatrix = "m"+a1+","+a2+","+b1+","+b2+","+c1+","+c2;
                    newShape.transform(tmatrix);
                }
            }
            return newShape;
        }

        var get_tool_tip = function(shape) {
            var toolTip = "";
            if (shape['type'] == 'Ellipse') {
              toolTip = "cx:"+ shape['cx'] +" cy:"+ shape['cy'] +" rx:"+ shape['rx'] + " ry: "+  shape['ry'];
            }
            else if (shape['type'] == 'Rectangle') {
              toolTip = "x:"+ shape['x'] +" y:"+ shape['y'] +
                " width:"+ shape['width'] + " height: "+  shape['height'];
            }
            else if (shape['type'] == 'Point') {
              toolTip = "cx:"+ shape['cx'] +" cy:"+ shape['cy'];
            }
            else if (shape['type'] == 'Line') {
              toolTip = "x1:"+ shape['x1'] +" y1:"+ shape['y1'] +" x2:"+ shape['x2'] +" y2:"+ shape['y2'];
            }
            else if (shape['type'] == 'PolyLine') {
            }
            else if (shape['type'] == 'Polygon') {
            }
            else if (shape['type'] == 'Label') {
            }
            return toolTip;
        }

        // if the currently selected shape is visible - highlight it
        display_selected = function() {
            // *NB: For some reason, can't overlay text with selectedClone.
            // So, for text shapes, we highlight by editing attributes instead.
            if ((selectedClone != null) && (selectedClone.type != 'text')) {
                if (selectedClone.node.parentNode.parentNode) selectedClone.remove();
            }
            if (selected_shape_id == null) return;

            selectedClone = null;
            for (var i=0; i<shape_objects.length; i++) {
                var s = shape_objects[i];
                var shape_id = resolve_id(s.id);
                if (shape_id == selected_shape_id) {
                    if (s.type == 'text') {
                        selectedClone = null;
                        strokeWidth = Math.ceil(s.attr('font-size')/10);
                        s.attr({'stroke': '#00a8ff', 'stroke-width': strokeWidth});
                    } else {
                        strokeWidth = (s.attr('stroke-width') > 0) ? Math.ceil(s.attr('stroke-width')/2) : 1;
                        selectedClone = s.clone();
                        selectedClone.attr({'stroke': '#00a8ff', 'stroke-width': strokeWidth,
                                            'fill-opacity': 0});
                    }
                } else {
                    if (s.type == 'text') {
                        s.attr({'stroke': null, 'stroke-width': null}); // remove stroke
                    }
                }
            }
            return selectedClone;
        }

        this.set_selected_shape = function(shape_id) {
            selected_shape_id = shape_id;
            $viewportimg.trigger("shape_click", [shape_id]);
            var sel_shape = display_selected();
            var sel_x;
            var sel_y;
            // we will only get the shape if currently displayed (current Z/T section)
            if (sel_shape===null) {
                // otherwise we have to work it out by drawing it
                var bb = null;
                for (var r=0; r<roi_json.length; r++) {
                    if (bb != null)   break;
                    var roi = roi_json[r];
                    var shapes = roi['shapes'];
                    var shape = null;
                    for (var s=0; s<shapes.length; s++) {
                        shape = shapes[s];
                        if (shape['id'] == selected_shape_id) {
                            var newShape = draw_shape(shape);
                            bb = newShape.getBBox();
                            newShape.remove();
                            if (shape['type'] == 'Label'){
                                // bug in BBox for text
                                sel_x = shape['x'] + (bb.width/2);
                                sel_y = shape['y'] + (bb.height/2);
                            } else {
                                sel_x = bb.x + (bb.width/2);
                                sel_y = bb.y + (bb.height/2);
                            }
                        }
                    }
                }
            } else {
                var bb = sel_shape.getBBox();
                sel_x = bb.x + (bb.width/2);
                sel_y = bb.y + (bb.height/2);
            }
            return {'x':sel_x, 'y':sel_y};
        }

        // called when user clicks on ROI
        handle_shape_click = function(event) {
            var shape = this;
            var shape_id = resolve_id(shape.id);
            self.set_selected_shape(shape_id);
        }

        // load the ROIs from json call and display
        load_rois = function(display_rois, filter, roi_id, shape_id, shape_config, refr_rois, hide_ome_rois) {
            if (json_url == undefined) return;

            $.getJSON(json_url+'?callback=?', function(data) {
                roi_json = data;

                // plot the rois
                if (display_rois) {
                  rois_displayed = true;
                  refresh_rois(undefined, undefined, filter);
                }
                $viewportimg.trigger("rois_loaded");
                if (roi_id && shape_id && shape_config)
                    $viewportimg.trigger("push_shape", [roi_id, shape_id, shape_config, refr_rois, hide_ome_rois]);
            });
        }

        /*
        If filter is not 'undefined' use the given ROI and shape IDs to build the list of active
        elements that will be shown by the web viewer.
        Filter is an associative array like
          {
           12: [1,2,3],
           13: []
          }
        where keys are the ID of the ROIs and values lists with IDs of the selected shapes for
        the given ROIs. If the value of a key is an empty list, all shapes related to that ROI
        will be considered as active.
        If the filter is 'undefined' set all ROIs and shapes coming from the DB as active.
        The active_rois object will be used to determinate which shapes will be displayed by the
        user interface when a change on the viewport occurs (like changing the Z or the T value).
         */
        filter_rois = function (filter) {
            var global_rois = [];
            $.merge(global_rois, roi_json);
            if (external_rois)
                $.merge(global_rois, external_rois);

            if (filter != undefined) {
                for (var r=0; r<global_rois.length; r++) {
                    // check if ROI is in filter
                    if (filter.hasOwnProperty(global_rois[r].id)) {
                        if (!active_rois.hasOwnProperty(global_rois[r].id))
                            active_rois[global_rois[r].id] = [];
                        // check if one or more shapes of the current ROI are in filter
                        var shapes = global_rois[r]['shapes'];
                        for (s=0; s<shapes.length; s++) {
                            if (filter[global_rois[r].id].indexOf(shapes[s].id) != -1 &&
                                active_rois[global_rois[r].id].indexOf(shapes[s].id) == -1) {
                                active_rois[global_rois[r].id].push(shapes[s].id);
                            }
                        }
                    }
                }
            } else {
                for (var r=0; r<global_rois.length; r++) {
                    if (!active_rois.hasOwnProperty(global_rois[r].id)) {
                        active_rois[global_rois[r].id] = [];
                    }
                    var shapes = global_rois[r]['shapes'];
                    for (s=0; s<shapes.length; s++) {
                        if (active_rois[global_rois[r].id].indexOf(shapes[s].id) == -1)
                            active_rois[global_rois[r].id].push(shapes[s].id);
                    }
                }
            }
        }

        /*
        Use active_rois to actually filter and retrieve ROIs and shapes that are going to be
        visualized in the viewport from the list of ROIs retrieved from the server.
        The list of ROIs coming from the server, referenced as 'roi_json', won't be modified.
         */
        get_active_rois = function () {
            var act_rois = [];

            // merge ROIs coming from OMERO server and external ROIs
            var global_rois = [];
            $.merge(global_rois, roi_json);
            if (external_rois)
                $.merge(global_rois, external_rois);
            for (var r=0; r<global_rois.length; r++) {
                if (active_rois.hasOwnProperty(global_rois[r].id)) {
                    var roi = {"id": global_rois[r].id};
                    var shapes = global_rois[r].shapes;
                    if (active_rois[global_rois[r].id].length == 0) {
                        // No filter for the shapes, append all of them
                        roi['shapes'] = shapes;
                        // Update filter as well, this will make possible to selectively disable shapes
                        for (s = 0; s < shapes.length; s++) {
                            active_rois[global_rois[r].id].push(shapes[s].id);
                        }
                    }
                    else {
                        roi['shapes'] = [];
                        for (s=0; s<shapes.length; s++) {
                            // Add only active shapes
                            if (active_rois[global_rois[r].id].indexOf(shapes[s].id) != -1) {
                                roi['shapes'].push(shapes[s]);
                            }
                        }
                    }
                    act_rois.push(roi);
                }
            }
            return act_rois;
        }

        // get the filter that describes currently active ROIs and shapes
        this.get_current_rois_filter = function() {
            if (typeof active_rois != "undefined") {
                if (Object.keys(active_rois).length == 0) {
                    return undefined;
                } else {
                    return active_rois;
                }
            } else {
                return undefined;
            }
        }

        // activate ROI with ID 'roi_id' and its related shapes
        this.activate_roi = function (roi_id) {
            var roi_id = resolve_id(roi_id);
            if (!active_rois.hasOwnProperty(roi_id)) {
                active_rois[roi_id] = [];
            }
        }

        // deactivate ROI with ID 'roi_id' and its related shapes
        this.deactivate_roi = function (roi_id) {
            var roi_id = resolve_id(roi_id);
            if (active_rois.hasOwnProperty(roi_id)) {
                delete active_rois[roi_id];
            }
        }

        // activate shape with ID 'shape_id' related to ROI with ID 'roi_id'
        this.activate_shape = function (roi_id, shape_id) {
            var roi_id = resolve_id(roi_id);
            var shape_id = resolve_id(roi_id);
            if (active_rois.hasOwnProperty(roi_id)) {
                if (active_rois[roi_id].indexOf(shape_id) == -1)
                    active_rois[roi_id].push(shape_id);
            } else {
                this.activate_roi(roi_id);
                this.activate_shape(roi_id, shape_id);
            }
        }

        // deactivate shape with ID 'shape_id' related to ROI with ID 'roi_id'
        this.deactivate_shape = function(roi_id, shape_id) {
            var roi_id = resolve_id(roi_id);
            var shape_id = resolve_id(shape_id);
            if (active_rois.hasOwnProperty(roi_id)) {
                if (active_rois[roi_id].indexOf(shape_id) != -1) {
                    active_rois[roi_id].splice(active_rois[roi_id].indexOf(shape_id), 1);
                }
                // If no shape remains, delete the ROI from active_rois list
                if (active_rois[roi_id].length == 0) {
                    this.deactivate_roi(roi_id);
                }
            }
        }

        // returns the ROI data as json. May be null if not yet loaded!
        this.get_roi_json = function() {
            return roi_json;
        }

        // return the data of external ROI as json. May be null if not loaded!
        this.get_external_rois_json = function() {
            return external_rois;
        }

        // get json of all ROI, OME ones and external ones, as a unique list
        this.get_full_roi_json = function() {
            if (!roi_json && !external_rois)
                return null;
            else if (roi_json && external_rois)
                return $.merge(roi_json, external_rois);
            else if (roi_json && !external_rois)
                return roi_json;
            else if (!roi_json && external_rois)
                return external_rois;
        }

        /*
        Check if given ROI and SHAPE ID can be used to define a new external ROI.
        ROI ID is valid if it is not alredy in use by an OME ROI.
        SHAPE ID is valid if it is not alredy in use by another shape for external ROI with given ROI ID.
         */
        var check_ext_shape_id = function(roi_id, shape_id) {
            // check if ROI ID is already used by one on OMERO's ROIs...
            for (var rx=0; rx<roi_json.length; rx++) {
                if (roi_json[rx]["id"] == roi_id) {
                    console.error("ID " + roi_id + " already used by one of OMERO ROIs");
                    return false;
                }
            }
            // ... if roi_id is used by an external ROI, check shape_id
            for (var rx=0; rx<external_rois.length; rx++) {
                if (external_rois[rx]["id"] == roi_id) {
                    var shapes = external_rois[rx]["shapes"];
                    for (var sx=0; sx<shapes.length; sx++) {
                        if (shapes[sx]["id"] == shape_id) {
                            console.error("Shape ID " + shape_id + " already in use for ROI " + roi_id);
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        // Check if there is another shape with on the same Z and T planes for this ROI
        var check_ext_shape_planes = function(roi_id, shape_z, shape_t) {
            for (var rx=0; rx<external_rois.length; rx++) {
                if (external_rois[rx]["id"] == roi_id) {
                    var shapes = external_rois[rx]["shapes"];
                    for(var sx=0; sx<shapes.length; sx++) {
                        if (shapes[sx]["theZ"] == shape_z && shapes[sx]["theT"] == shape_t) {
                            console.error("Z plane " + shape_z + " and T plane " + shape_t + " already used");
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        // configure a shape obtained using function in ome.roiutils.js as a proper shape for OMERO.web viewer
        var configure_shape = function(shape_id, shape_config) {
            shape_config["id"] = shape_id;
        }

        // build a proper ROI definition that can be used by OMERO.web viewer
        var build_roi_description = function(roi_id, shapes) {
            return {
                "id": roi_id,
                "shapes": typeof shapes !== "undefined" ? shapes : []
            };
        }

        // append a shape to ROI with ID roi_id
        var append_shape = function(roi_id, shape_config, rois_collection) {
            for (var x=0; x<rois_collection.length; x++) {
                if (rois_collection[x]["id"] == roi_id) {
                    // ROI with ID roi_id already exists, append shape
                    rois_collection[x]["shapes"].push(shape_config);
                    return;
                }
            }
            var roi = build_roi_description(roi_id, [shape_config]);
            rois_collection.push(roi);
        };

        /*
        Add a SHAPE definition to OMERO.web viewer. Shape will be associated to ROI with ID roi_id
        and will obtain ID shape_id. Shape is described using shape_config dictionary.
        If refresh_rois is TRUE, automatically refresh viewport.
        If hide_ome_rois is TRUE, hide OME ROI when refreshing viewport.
         */
        this.push_shape = function(roi_id, shape_id, shape_config, refresh_rois, hide_ome_rois) {
            if (roi_json == null) {
                load_rois(false, undefined, roi_id, shape_id, shape_config, refresh_rois, hide_ome_rois);
            } else {
                $viewportimg.trigger("push_shape", [roi_id, shape_id, shape_config, refresh_rois,
                                                    hide_ome_rois]);
            }
        };

        $viewportimg.on("push_shape", function (event, roi_id, shape_id, shape_config, refresh_rois,
                                                hide_ome_rois) {
            var roi_id = resolve_id(roi_id);
            var shape_id = resolve_id(shape_id);

            // initialize external_rois when used for the first time
            if (!external_rois) {
                external_rois = [];
            }

            var check_shape_id = check_ext_shape_id(roi_id, shape_id);
            var check_shape_planes = check_ext_shape_planes(roi_id, shape_config['theZ'],
                shape_config['theT']);

            if (check_shape_id && check_shape_planes) {
                // add ID to the shape
                configure_shape(shape_id, shape_config);
                // append shape to proper ROI
                append_shape(roi_id, shape_config, external_rois);
                this.activate_roi(roi_id);
            }

            // refresh current ROIs (False by default)
            var refresh = typeof refresh_rois !== "undefined" ? refresh_rois : false;
            if (refresh) {
                // if hide_ome_rois is true, deactivate all ROIs coming from OMERO before refresh
                var hide_oroi = typeof hide_ome_rois !== "undefined" ? hide_ome_rois : false;
                if (hide_oroi && active_rois) {
                    for (var r = 0; r < roi_json.length; r++) {
                        this.deactivate_roi(roi_json[r]["id"]);
                    }
                }
                this.refresh_active_rois();
            }
        });

        /*
        Remove SHAPE with ID shape_id from ROI with ID roi_id from external ROI list.
        If refresh is TRUE, automatically refresh viewport.
         */
        this.remove_shape = function(roi_id, shape_id, refresh) {
            if (! external_rois) {
                console.warn("There are no external ROIs, nothing to do");
                return;
            }
            for(var r=0; r<external_rois.length; r++) {
                var roi = external_rois[r];
                if (roi["id"] == resolve_id(roi_id)) {
                    for(var s=0; s<roi["shapes"].length; s++) {
                        var shape = roi["shapes"][s];
                        if (shape["id"] == resolve_id(shape_id)) {
                            roi["shapes"].splice(roi["shapes"].indexOf(shape), 1);

                            // If it was the last shape for the current ROI, delete the ROI as well
                            if(roi["shapes"].length == 0) {
                                console.warn("No shape connected, removing ROI " + roi_id);
                                this.remove_roi(roi_id, refresh);
                            } else {
                                // refresh ROIs, if needed
                                var refresh = typeof refresh_rois !== "undefined" ? refresh_rois : false;
                                if (refresh)
                                    this.refresh_active_rois();

                                return;
                            }
                        }
                    }
                    console.warn("There is no Shape with ID " + shape_id + " for ROI " + roi_id);
                    return;
                }
            }
            console.warn("There is no ROI with ID " + roi_id);
        };
        /*
        Remove ROI with ID roi_id from external ROI list.
        If refresh is TRUE, automatically refresh viewport.
         */
        this.remove_roi = function(roi_id, refresh) {
            if (! external_rois) {
                console.warn("There are no external ROIs, nothing to do");
                return;
            }
            for (var r=0; r<external_rois.length; r++) {
                var roi = external_rois[r];
                if (roi["id"] == resolve_id(roi_id)) {
                    external_rois.splice(external_rois.indexOf(roi), 1);
                    this.deactivate_roi(resolve_id(roi_id));

                    var refresh = typeof refresh !== "undefined" ? refresh : false;
                    if (refresh) {
                        if (Object.keys(active_rois).length != 0) {
                            this.refresh_active_rois();
                        } else {
                            this.hide_rois();
                        }
                    }
                    return;
                }
            }
            console.warn("There is no ROI with ID " + roi_id);
        }

        /*
        Clears paper and draws ROIs (if rois_displayed) for the given T and Z. NB: indexes are 1-based.
        Only shapes in 'active_rois' are going to be displayed.
        */
        this.refresh_rois = function(theZ, theT, rois_filter) {

            if (typeof theZ != 'undefined') this.theZ = theZ;
            if (typeof theT != 'undefined') this.theT = theT;

            paper.clear();
            shape_objects.length = 0;
            if (!rois_displayed) return;
            // build the filter for active ROIs and shapes
            filter_rois(rois_filter);
            // apply the filter and get the description of ROIs and shapes that will be displayed
            var rois = get_active_rois();
            if (rois == null) return;

            for (var r=0; r<rois.length; r++) {
                var roi = rois[r];
                var shapes = roi['shapes'];
                var shape = null;
                for (var s=0; s<shapes.length; s++) {
                    shape = shapes[s];
                    if (((shape.theT == this.theT-1)
                         || typeof shape.theT === "undefined")
                        && ((shape.theZ == this.theZ-1)
                            || typeof shape.theZ === "undefined")) {
                        var newShape = draw_shape(shape);
                        var toolTip = get_tool_tip(shape);
                        // Add text - NB: text is not 'attached' to shape in any way.
                        if (newShape != null) {
                            if (shape['type'] == 'PolyLine') {
                                newShape.attr({'fill-opacity': 0});
                            }
                            if ((shape['textValue'] != null) && (shape['textValue'].length > 0)) {
                                // Show text
                                if (shape['type'] == 'Label') {
                                    var txt = newShape; // if shape is label itself, use it
                                    if (shape['strokeColor']) txt.attr({'fill': shape['strokeColor']}); // this is Insight's behavior
                                    txt.attr({'stroke': null });
                                } else if (roi_label_displayed) {
                                    // otherwise, add a new label in the centre of the shape.
                                    var bb = newShape.getBBox();
                                    var textx = bb.x + (bb.width/2);
                                    var texty = bb.y + (bb.height/2);
                                    var text_string = formatShapeText(shape['textValue'].escapeHTML())
                                    var txt = paper.text(textx, texty, text_string);    // draw a 'dummy' paragraph to work out it's dimensions
                                    var newY = (texty-txt.getBBox().height/2)+9;
                                    // moving the existing text to newY doesn't seem to work - instead, remove and draw a new one
                                    txt.remove();
                                    txt = paper.text(textx, newY, formatShapeText(shape['textValue'].escapeHTML()))
                                               .attr({'cursor':'default', 'fill': shape['strokeColor']}); // this is Insight's behavior
                                    txt_box = txt.getBBox();
                                    var txt_w = txt_box.width*1.3;
                                    var txt_h = txt_box.height*1.3;
                                    txt.toFront();
                                    // clicking the text should do the same as clicking the shape
                                    txt.id = shape['id'] + "_shape_text";
                                    txt.click(handle_shape_click);

                                }

                                // handle other text-specific attributes...
                                var txtAttr = {};
                                if (shape['fontFamily']) {  // model: serif, sans-serif, cursive, fantasy, monospace. #5072
                                    // raphael supports all these exactly - so we can pass directly.
                                    txtAttr['font-family'] = shape['fontFamily'];
                                }
                                if (shape['fontSize']) {
                                    txtAttr['font-size'] = shape['fontSize'];
                                }
                                if (shape['fontStyle']) { // model: Normal, Italic, Bold, Bolditalic
                                    var fs = shape['fontStyle'];
                                    if ((fs == 'Bold') || (fs == 'BoldItalic')) {
                                        txtAttr['font-weight'] = 'bold';
                                    }
                                    if ((fs == 'Italic') || (fs == 'BoldItalic')) {
                                        txtAttr['font-style'] = 'italic';
                                    }
                                }
                                if (txt) txt.attr(txtAttr);
                            }
                            if (shape['type'] != 'Label') {
                                // these shape attributes are not applied to text
                                if (shape['fillColor'] && shape['type'] != 'PolyLine') {
                                    // don't show fills on PolyLines
                                    newShape.attr({'fill': shape['fillColor']});
                                    if (shape['fillAlpha']) { newShape.attr({'fill-opacity': shape['fillAlpha']})}
                                }
                                else {
                                    // need *some* fill so that shape is clickable
                                    newShape.attr({'fill':'#000', 'fill-opacity': 0.01 });
                                }
                                if (shape['strokeAlpha']) { newShape.attr({'stroke-opacity': shape['strokeAlpha']}); }
                                if (shape['strokeColor']) { newShape.attr({'stroke': shape['strokeColor']}); }
                                else { newShape.attr({'stroke': '#ffffff'}); }  // white is default
                            }
                            newShape.attr({'cursor':'default'});
                            if (shape['strokeWidth']) { newShape.attr({'stroke-width': shape['strokeWidth']}); }
                            newShape.click(handle_shape_click);
                            newShape.attr({ title: toolTip });
                            newShape.id = shape['id'] + "_shape";
                            shape_objects.push(newShape);
                        }

                    }
                }
            }
            // if the new display includes selected-shape - show it
            display_selected();
        }

        // refresh the viewport using 'active_rois' as filter
        this.refresh_active_rois = function (theZ, theT) {
            rois_displayed = true;
            refresh_rois(theZ, theT, active_rois);
        }

        // loads the ROIs if needed and displays them
        this.show_rois = function(theZ, theT, filter) {
            this.theZ = theZ;
            this.theT = theT;
          if (roi_json == null) {
              load_rois(true, filter);      // load and display
          } else {
              rois_displayed = true;
              this.refresh_rois(undefined, undefined, filter);
          }
        }

        // hides the ROIs from display
        this.hide_rois = function(hide_ome_rois, hide_external_rois) {
            var hide_ome_rois = typeof hide_ome_rois !== "undefined" ? hide_ome_rois : true;
            var hide_external_rois = typeof hide_external_rois !== "undefined" ? hide_external_rois : true;

            if (!hide_ome_rois && !hide_external_rois) {
                console.warn("HIDE_OME_ROIS and HIDE_EXTERNAL_ROIS are False, nothing to do");
                return;
            }

            if (hide_ome_rois && hide_external_rois) {
                rois_displayed = false;
                active_rois = {};
                this.refresh_rois();
            } else {
                if (!hide_ome_rois) {
                    for (var r = 0; r < external_rois.length; r++) {
                        delete(active_rois[external_rois[r].id]);
                    }
                }
                else if (!hide_external_rois) {
                    for (var r = 0; r < roi_json.length; r++) {
                        delete(active_rois[roi_json[r].id]);
                    }
                }
                if (Object.keys(active_rois).length == 0) {
                    rois_displayed = false;
                    this.refresh_rois();
                }
                else {
                    this.refresh_active_rois();
                }
            }
        }

        // Retrieve shape with ID shape_id from ROI with ID roi_di from roi_list
        get_shape = function(roi_id, shape_id, roi_list) {
            for (var rx=0; rx<roi_list.length; rx++) {
                if (roi_list[rx].id == roi_id) {
                    var shapes = roi_list[rx].shapes;
                    for (var sx=0; sx<shapes.length; sx++) {
                        if (shapes[sx].id == shape_id)
                            return shapes[sx];
                    }
                    console.error("ROI with ID " + roi_id + "has no shape with ID " + shape_id);
                }
            }
            return null;
        }

        // Get backup key for given roi_id and shape_id
        get_backup_key = function(roi_id, shape_id) {
            return roi_id + "::" + shape_id;
        }

        /*
        Save a backup copy of shape_config using shape_id and roi_id to build backup key.
        If a backup for given shape_id and roi_id already exists, do nothing.
         */
        backup_shape = function(roi_id, shape_id, shape_conf) {
            var backup_key = get_backup_key(roi_id, shape_id);
            if (!(backup_key in original_shapes_backup)) {
                // clone the shape_conf object as "original" and keep a reference to updated object (used to restore)
                original_shapes_backup[backup_key] = $.extend({}, shape_conf);
            }
        }

        /*
        Restore from backup SHAPE with ID shape_id for ROI with id roi_id.
        If refresh_view is TRUE, automatically refresh viewport.
         */
        this.restore_shape = function(roi_id, shape_id, refresh_view) {
            var refresh_view = typeof refresh_view !== "undefined" ? refresh_view : true;
            var backup_key = get_backup_key(roi_id, shape_id);
            if (backup_key in original_shapes_backup) {
                var original_shape = original_shapes_backup[backup_key];
                this.update_shape_text(roi_id, shape_id, original_shape.textValue, original_shape.fontFamily,
                    original_shape.fontSize, original_shape.fontStyle, false);
                this.update_shape_config(roi_id, shape_id, original_shape.strokeColor, original_shape.strokeAlpha,
                    original_shape.strokeWidth, original_shape.fillColor, original_shape.fillAlpha, false);
                delete original_shapes_backup[backup_key];

                if (refresh_view) {
                    this.refresh_active_rois();
                }
            }
        }

        // Restore all shapes with a backup
        this.restore_shapes = function(refresh_view) {
            var refresh_view = typeof refresh_view !== "undefined" ? refresh_view : true;

            for (var bk_key in original_shapes_backup) {
                var sh_id = bk_key.split("::");
                this.restore_shape(sh_id[0], sh_id[1], false);
            }

            if (refresh_view) {
                this.refresh_active_rois();
            }
        }

        /*
        Update text for SHAPE with ID shape_id related to ROI with ID roi_id.
        Only input values that are not "undefined" will be updated, previuos value will be kept
        for all values passed as "undefined".
         */
        this.update_shape_text = function (roi_id, shape_id, text_value, font_family, font_size, font_style,
                                           refresh_view) {
            var refresh_view = typeof refresh_view !== "undefined" ? refresh_view : true;

            // look for shape in OME ROIs and external ones
            var sh = get_shape(roi_id, shape_id, roi_json);
            if (!sh)
                sh = get_shape(roi_id, shape_id, external_rois);

            if (sh) {
                backup_shape(roi_id, shape_id, sh);

                sh.textValue = typeof text_value !== "undefined" ? text_value : sh.textValue;
                sh.fontFamily = typeof font_family !== "undefined" ? font_family : sh.fontFamily;
                sh.fontSize = typeof font_size !== "undefined" ? font_size : sh.fontSize;
                sh.fontStyle = typeof font_style !== "undefined" ? font_style : sh.fontStyle;

                if(refresh_view) {
                    this.refresh_active_rois();
                }
            } else {
                console.error("Unable to find a shape for ROI ID " + roi_id + " and SHAPE ID " + shape_id);
            }
        }

        /*
        Update shape configuration for SHAPE with ID shape_id related to ROI with ID roi_id.
        Only input values that are not "undefined" will be updated, previuos value will be kept
        for all values passed as "undefined".
         */
        this.update_shape_config = function(roi_id, shape_id, stroke_color, stroke_alpha,
                                            stroke_width, fill_color, fill_alpha, refresh_view) {
            var refresh_view = typeof refresh_view !== "undefined" ? refresh_view : true;

            // look for shape in OME ROIs and external ones
            var sh = get_shape(roi_id, shape_id, roi_json);
            if (!sh)
                sh = get_shape(roi_id, shape_id, external_rois);

            if (sh) {
                backup_shape(roi_id, shape_id, sh);

                sh.strokeWidth = typeof stroke_width !== "undefined" ? stroke_width : sh.strokeWidth;
                sh.strokeAlpha = typeof stroke_alpha !== "undefined" ? stroke_alpha : sh.strokeAlpha;
                sh.strokeColor = typeof stroke_color !== "undefined" ? stroke_color : sh.strokeColor;
                sh.fillAlpha = typeof fill_alpha !== "undefined" ? fill_alpha : sh.fillAlpha;
                sh.fillColor = typeof fill_color !== "undefined" ? fill_color : sh.fillColor;

                if(refresh_view) {
                    this.refresh_active_rois();
                }
            } else {
                console.error("Unable to find a shape for ROI ID " + roi_id + " and SHAPE ID " + shape_id);
            }
        }

        this.show_labels = function(visible, filter) {
            roi_label_displayed = visible;
            this.refresh_rois(undefined, undefined, filter);
        }

        // sets the Zoom of the ROI paper (canvas)
        this.setRoiZoom = function(percent) {
            paper.scaleAll(percent/100);
        }

    });

}
