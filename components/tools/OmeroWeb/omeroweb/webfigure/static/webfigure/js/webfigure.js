

    // ----------------------- Backbone MODEL --------------------------------------------


    // ------------------------ Panel -----------------------------------------
    // Simple place-holder for each Panel. Will have E.g. imageId, rendering options etc
    // Attributes can be added as we need them.
    var Panel = Backbone.Model.extend({

        defaults: {
            x: 100,     // coordinates on the 'paper'
            y: 100,
            width: 512,
            height: 512,
            zoom: 100,
            dx: 0,    // pan x & y within viewport
            dy: 0,
            labels: [],
            selected: false
        },

        initialize: function() {

        },

        hide_scalebar: function() {
            // keep all scalebar properties, except 'show'
            var sb = $.extend(true, {}, this.get('scalebar'));
            sb.show = false;
            this.save('scalebar', sb);
        },

        save_scalebar: function(new_sb) {
            // update only the attributes of scalebar we're passed
            var old_sb = $.extend(true, {}, this.get('scalebar') || {});
            var sb = $.extend(true, old_sb, new_sb);
            this.save('scalebar', sb);
        },

        // takes a list of labels, E.g [{'text':"t", 'size':10, 'color':'FF0000', 'position':"top"}]
        add_labels: function(labels) {
            var oldLabs = this.get("labels");
            // Need to clone the list of labels...
            var labs = [];
            for (var i=0; i<oldLabs.length; i++) {
                labs.push( $.extend(true, {}, oldLabs[i]) );
            }
            // ... then add new labels ...
            for (var j=0; j<labels.length; j++) {
                // check that we're not adding a white label outside panel (on a white background)
                if (_.contains(["top", "bottom", "left", "right"], labels[j].position) &&
                        labels[j].color == "FFFFFF") {
                    labels[j].color = "000000";
                }
                labs.push( $.extend(true, {}, labels[j]) );
            }
            // ... so that we get the changed event triggering OK
            this.save('labels', labs);
        },

        create_labels_from_channels: function(options) {
            var newLabels = [];
            _.each(this.get('channels'), function(c){
                if (c.active) {
                    newLabels.push({
                        'text': c.label,
                        'size': options.size,
                        'position': options.position,
                        'color': options.color || c.color
                    });
                }
            });
            this.add_labels(newLabels);
        },

        get_label_key: function(label) {
            return label.text + '_' + label.size + '_' + label.color + '_' + label.position;
        },

        // labels_map is {labelKey: {size:s, text:t, position:p, color:c}} or {labelKey: false} to delete
        // where labelKey specifies the label to edit. "l.text + '_' + l.size + '_' + l.color + '_' + l.position"
        edit_labels: function(labels_map) {

            var oldLabs = this.get('labels');
            // Need to clone the list of labels...
            var labs = [],
                lbl, lbl_key;
            for (var i=0; i<oldLabs.length; i++) {
                lbl = oldLabs[i];
                lbl_key = this.get_label_key(lbl);
                // for existing label that matches...
                if (labels_map.hasOwnProperty(lbl_key)) {
                    if (labels_map[lbl_key]) {
                        // replace with the new label
                        lbl = $.extend(true, {}, labels_map[lbl_key]);
                        labs.push( lbl );
                    }
                    // else 'false' are ignored (deleted)
                } else {
                    // otherwise leave un-edited
                    lbl = $.extend(true, {}, lbl);
                    labs.push( lbl );
                }
            }
            // ... so that we get the changed event triggering OK
            this.save('labels', labs);
        },

        save_channel: function(cIndex, attr, value) {

            var oldChs = this.get('channels');
            // Need to clone the list of channels...
            var chs = [];
            for (var i=0; i<oldChs.length; i++) {
                chs.push( $.extend(true, {}, oldChs[i]) );
            }
            // ... then set new value ...
            chs[cIndex][attr] = value;
            // ... so that we get the changed event triggering OK
            this.save('channels', chs);
        },

        toggle_channel: function(cIndex, active){

            if (typeof active == "undefined"){
                active = !this.get('channels')[cIndex].active;
            }
            this.save_channel(cIndex, 'active', active);
        },

        // When a multi-select rectangle is drawn around several Panels
        // a resize of the rectangle x1, y1, w1, h1 => x2, y2, w2, h2
        // will resize the Panels within it in proportion.
        // This might be during a drag, or drag-stop (save=true)
        multiselectdrag: function(x1, y1, w1, h1, x2, y2, w2, h2, save){

            var shift_x = function(startX) {
                return ((startX - x1)/w1) * w2 + x2;
            };
            var shift_y = function(startY) {
                return ((startY - y1)/h1) * h2 + y2;
            };

            var newX = shift_x( this.get('x') ),
                newY = shift_y( this.get('y') ),
                newW = shift_x( this.get('x')+this.get('width') ) - newX,
                newH = shift_y( this.get('y')+this.get('height') ) - newY;

            // Either set the new coordinates...
            if (save) {
                this.save( {'x':newX, 'y':newY, 'width':newW, 'height':newH} );
            } else {
                // ... Or update the UI Panels
                // both svg and DOM views listen for this...
                this.trigger('drag_resize', [newX, newY, newW, newH] );
            }
        },

        // Drag resizing - notify the PanelView without saving
        drag_resize: function(x, y, w, h) {
            this.trigger('drag_resize', [x, y, w, h] );
        },

        // Drag moving - notify the PanelView & SvgModel with/without saving
        drag_xy: function(dx, dy, save) {
            // Ignore any drag_stop events from simple clicks (no drag)
            if (dx === 0 && dy === 0) {
                return;
            }
            var newX = this.get('x') + dx,
                newY = this.get('y') + dy,
                w = this.get('width'),
                h = this.get('height');

            // Either set the new coordinates...
            if (save) {
                this.save( {'x':newX, 'y':newY} );
            } else {
                // ... Or update the UI Panels
                // both svg and DOM views listen for this...
                this.trigger('drag_resize', [newX, newY, w, h] );
            }

            // we return new X and Y so FigureModel knows where panels are
            return {'x':newX, 'y':newY};
        },

        get_centre: function() {
            return {'x':this.get('x') + (this.get('width')/2),
                'y':this.get('y') + (this.get('height')/2)};
        },

        get_img_src: function() {
            var cStrings = [];
            _.each(this.get('channels'), function(c, i){
                if (c.active) {
                    cStrings.push(1+i + "|" + c.window.start + ":" + c.window.end + "$" + c.color);
                }
            });
            var renderString = cStrings.join(","),
                imageId = this.get('imageId'),
                theZ = this.get('theZ'),
                theT = this.get('theT');

            return '/webgateway/render_image/' + imageId +
                    "/" + theZ + "/" + theT + '/?c=' + renderString;
        },

        // used by the PanelView and ImageViewerView to get the size and
        // offset of the img within it's frame
        get_vp_img_css: function(zoom, frame_w, frame_h, dx, dy) {

            var orig_w = this.get('orig_width'),
                orig_h = this.get('orig_height');
            if (typeof dx == 'undefined') dx = this.get('dx');
            if (typeof dy == 'undefined') dy = this.get('dy');
            zoom = zoom || 100;

            var img_x = 0,
                img_y = 0,
                img_w = frame_w * (zoom/100),
                img_h = frame_h * (zoom/100),
                orig_ratio = orig_w / orig_h,
                wh = frame_w / frame_h;
            if (Math.abs(orig_ratio - wh) < 0.01) {
                // ignore...
            // if viewport is wider than orig, offset y
            } else if (orig_ratio < wh) {
                img_h = img_w / orig_ratio;
            } else {
                img_w = img_h * orig_ratio;
            }
            img_y = (img_h - frame_h)/2;
            img_x = (img_w - frame_w)/2;

            img_x = ((dx * frame_w) / orig_w) - img_x;
            img_y = ((dy * frame_h) / orig_h) - img_y;

            return {'left':img_x, 'top':img_y, 'width':img_w, 'height':img_h};
        },

    });

    // ------------------------ Panel Collection -------------------------
    var PanelList = Backbone.Collection.extend({
        model: Panel,

        getSelected: function() {
            return this.filter(function(panel){
                return panel.get('selected'); 
            });
        },

        localStorage: new Backbone.LocalStorage("figureShop-backbone")
    });


    // ------------------------- Figure Model -----------------------------------
    // Has a PanelList as well as other attributes of the Figure
    var FigureModel = Backbone.Model.extend({

        defaults: {
            'curr_zoom': 100
        },

        initialize: function() {
            this.panels = new PanelList();      //this.get("shapes"));

            // wrap selection notification in a 'debounce', so that many rapid
            // selection changes only trigger a single re-rendering 
            this.notifySelectionChange = _.debounce( this.notifySelectionChange, 10);
        },

        align_left: function() {
            var selected = this.getSelected(),
                x_vals = [];
            for (var i=0; i<selected.length; i++) {
                x_vals.push(selected[i].get('x'));
            }
            var min_x = Math.min.apply(window, x_vals);

            for (var j=0; j<selected.length; j++) {
                selected[j].set('x', min_x);
            }
        },

        align_top: function() {
            var selected = this.getSelected(),
                y_vals = [];
            for (var i=0; i<selected.length; i++) {
                y_vals.push(selected[i].get('y'));
            }
            var min_y = Math.min.apply(window, y_vals);

            for (var j=0; j<selected.length; j++) {
                selected[j].set('y', min_y);
            }
        },

        align_grid: function() {
            var sel = this.getSelected(),
                top_left = this.get_top_left_panel(sel),
                top_x = top_left.get('x'),
                top_y = top_left.get('y'),
                grid = [],
                row = [top_left],
                next_panel = top_left;

            // populate the grid, getting neighbouring panel each time
            while (next_panel) {
                c = next_panel.get_centre();
                next_panel = this.get_panel_at(c.x + next_panel.get('width'), c.y, sel);

                // if next_panel is not found, reached end of row. Try start new row...
                if (typeof next_panel == 'undefined') {
                    grid.push(row);
                    // next_panel is below the first of the current row
                    c = row[0].get_centre();
                    next_panel = this.get_panel_at(c.x, c.y + row[0].get('height'), sel);
                    row = [];
                }
                if (next_panel) {
                    row.push(next_panel);
                }
            }

            var spacer = top_left.get('width')/20,
                new_x = top_x,
                new_y = top_y,
                max_h = 0;
            for (var r=0; r<grid.length; r++) {
                row = grid[r];
                for (var c=0; c<row.length; c++) {
                    panel = row[c];
                    panel.save({'x':new_x, 'y':new_y});
                    max_h = Math.max(max_h, panel.get('height'));
                    new_x = new_x + spacer + panel.get('width');
                }
                new_y = new_y + spacer + max_h;
                new_x = top_x;
            }
        },

        get_panel_at: function(x, y, panels) {
            for(var i=0; i<panels.length; i++) {
                p = panels[i];
                if ((p.get('x') < x && (p.get('x')+p.get('width')) > x) &&
                        (p.get('y') < y && (p.get('y')+p.get('height')) > y)) {
                    return p;
                }
            }
        },

        get_top_left_panel: function(panels) {
            // top-left panel is one where x + y is least
            var p, top_left;
            for(var i=0; i<panels.length; i++) {
                p = panels[i];
                if (i === 0) {
                    top_left = p;
                } else {
                    if ((p.get('x') + p.get('y')) < (top_left.get('x') + top_left.get('y'))) {
                        top_left = p;
                    }
                }
            }
            return top_left;
        },

        align_size: function(width, height) {
            var sel = this.getSelected(),
                ref = this.get_top_left_panel(sel),
                ref_width = width ? ref.get('width') : false,
                ref_height = height ? ref.get('height') : false,
                new_w, new_h,
                p;

            for (var i=0; i<sel.length; i++) {
                p = sel[i];
                if (ref_width && ref_height) {
                    new_w = ref_width;
                    new_h = ref_height;
                } else if (ref_width) {
                    new_w = ref_width;
                    new_h = (ref_width/p.get('width')) * p.get('height');
                } else if (ref_height) {
                    new_h = ref_height;
                    new_w = (ref_height/p.get('height')) * p.get('width');
                }
                p.save({'width':new_w, 'height':new_h});
            }
        },

        // This can come from multi-select Rect OR any selected Panel
        // Need to notify ALL panels and Multi-select Rect.
        drag_xy: function(dx, dy, save) {
            if (dx === 0 && dy === 0) return;

            var minX = 10000,
                minY = 10000,
                xy;
            // First we notidy all Panels
            var selected = this.getSelected();
            for (var i=0; i<selected.length; i++) {
                xy = selected[i].drag_xy(dx, dy, save);
                minX = Math.min(minX, xy.x);
                minY = Math.min(minY, xy.y);
            }
            // Notify the Multi-select Rect of it's new X and Y
            this.trigger('drag_xy', [minX, minY, save]);
        },


        // This comes from the Multi-Select Rect.
        // Simply delegate to all the Panels
        multiselectdrag: function(x1, y1, w1, h1, x2, y2, w2, h2, save) {
            var selected = this.getSelected();
            for (var i=0; i<selected.length; i++) {
                selected[i].multiselectdrag(x1, y1, w1, h1, x2, y2, w2, h2, save);
            }
        },

        // If already selected, do nothing (unless clearOthers is true)
        setSelected: function(item, clearOthers) {
            if ((!item.get('selected')) || clearOthers) {
                this.clearSelected(false);
                item.set('selected', true);
                this.notifySelectionChange();
            }
        },

        select_all:function() {
            this.panels.each(function(p){
                p.set('selected', true);
            });
            this.notifySelectionChange();
        },

        addSelected: function(item) {
            item.set('selected', true);
            this.notifySelectionChange();
        },

        clearSelected: function(trigger) {
            this.panels.each(function(p){
                p.set('selected', false);
            });
            if (trigger !== false) {
                this.notifySelectionChange();
            }
        },

        getSelected: function() {
            return this.panels.getSelected();
        },

        // Go through all selected and destroy them - trigger selection change
        deleteSelected: function() {
            var selected = this.getSelected();
            for (var i=0; i<selected.length; i++) {
                selected[i].destroy();
            }
            this.notifySelectionChange();
        },

        notifySelectionChange: function() {
            this.trigger('change:selection');
        }

    });


    // -------------------------- Backbone VIEWS -----------------------------------------


    // var SelectionView = Backbone.View.extend({
    var FigureView = Backbone.View.extend({

        el: $("#body"),

        initialize: function(opts) {

            // Delegate some responsibility to other views
            new AlignmentToolbarView({model: this.model});

            // set up various elements and we need repeatedly
            this.$main = $('main');
            this.$canvas = $("#canvas");
            this.$canvas_wrapper = $("#canvas_wrapper");
            this.$paper = $("#paper");

            var self = this;

            // Render on changes to the model
            this.model.on('change:paper_width', this.render, this);

            // If a panel is added...
            this.model.panels.on("add", this.addOne, this);

            // Select a different size paper
            $("#paper_size_chooser").change(function(){
                var wh = $(this).val().split(","),
                    w = wh[0],
                    h = wh[1];
                self.model.set({'paper_width':w, 'paper_height':h});
            });

            // respond to zoom changes
            this.listenTo(this.model, 'change:curr_zoom', this.setZoom);

            // refresh current UI
            this.setZoom();
            this.reCentre();

            // 'Auto-render' on init.
            this.render();

        },

        events: {
            "click .add_panel": "addPanel"
        },

        keyboardEvents: {
            'backspace': 'deleteSelectedPanels',
            'command+a': 'select_all',
            'command+c': 'copy_selected_panels',
            'command+v': 'paste_panels'
        },

        copy_selected_panels: function() {
            var s = this.model.getSelected();
            this.clipboard_data = cd = [];
            _.each(s, function(m) {
                var copy = m.toJSON();
                delete copy.id;
                cd.push(copy);
            });
        },

        paste_panels: function() {
            if (!this.clipboard_data) return;

            var self = this;
            this.model.clearSelected();

            // first work out the bounding box of clipboard panels
            var top, left, bottom, right;
            _.each(this.clipboard_data, function(m, i) {
                var t = m.y,
                    l = m.x,
                    b = t + m.height,
                    r = l + m.width;
                if (i === 0) {
                    top = t; left = l; bottom = b; right = r;
                } else {
                    top = Math.min(top, t);
                    left = Math.min(left, l);
                    bottom = Math.max(bottom, b);
                    right = Math.max(right, r);
                }
            });
            var height = bottom - top,
                width = right - left,
                offset_x = 0,
                offset_y = 0;

            // if pasting a 'row', paste below. Paste 'column' to right.
            if (width > height) {
                offset_y = height + height/20;  // add a spacer
            } else {
                offset_x = width + width/20;
            }

            // apply offset to clipboard data & paste
            _.each(this.clipboard_data, function(m) {
                m.x = m.x + offset_x;
                m.y = m.y + offset_y;
                self.model.panels.create(m);
            });
            // only pasted panels are selected - simply trigger...
            this.model.notifySelectionChange();
        },

        clipboard_data: undefined,

        select_all: function() {
            this.model.select_all();
            return false;
        },

        deleteSelectedPanels: function(ev) {
            this.model.deleteSelected();
            return false;
        },

        addPanel: function() {

            var self = this,
                iIds;
            var idInput = prompt("Please enter Image ID(s):");

            if (!idInput || idInput.length === 0)    return;

            this.model.clearSelected();

            // test for E.g: http://localhost:8000/webclient/?show=image-25|image-26|image-27
            if (idInput.indexOf('?') > 10) {
                iIds = idInput.split('image-').slice(1);
            } else {
                iIds = idInput.split(',');
            }

            // approx work out number of columns to layout new panels
            var colCount = Math.ceil(Math.sqrt(iIds.length)),
                rowCount = Math.ceil(iIds.length/colCount),
                col = 0,
                row = 0,
                px, py, spacer;

            for (var i=0; i<iIds.length; i++) {
                var imgId = iIds[i];

                if (parseInt(imgId, 10) > 0) {
                    var c = this.getCentre();
                    // Get the json data for the image...
                    $.getJSON('/webgateway/imgData/' + parseInt(imgId, 10) + '/', function(data){
                        // just pick what we need, add x & y etc...
                        // Need to work out where to start (px,py) now that we know size of panel
                        // (assume all panels are same size)
                        px = px || c.x - (colCount * data.size.width)/2;
                        py = py || c.y - (rowCount * data.size.height)/2;
                        spacer = spacer || data.size.width/20;
                        var n = {
                            'imageId': data.id,
                            'name': data.meta.imageName,
                            'width': data.size.width,
                            'height': data.size.height,
                            'sizeZ': data.size.z,
                            'theZ': data.rdefs.defaultZ,
                            'sizeT': data.size.t,
                            'theT': data.rdefs.defaultT,
                            'channels': data.channels,
                            'orig_width': data.size.width,
                            'orig_height': data.size.height,
                            'x': px,
                            'y': py,
                            'datasetName': data.meta.datasetName,
                            'datasetId': data.meta.datasetId,
                            'pixel_size': data.pixel_size.x,
                        };
                        // create Panel (and select it)
                        self.model.panels.create(n).set('selected', true);
                        self.model.notifySelectionChange();

                        // update px, py for next panel
                        col += 1;
                        px += data.size.width + spacer;
                        if (col == colCount) {
                            row += 1;
                            col = 0;
                            py += data.size.height + spacer;
                            px = undefined; // recalculate next time
                        }
                    });
                }
            }
        },

        // User has zoomed the UI - work out new sizes etc...
        // We zoom the main content 'canvas' using css transform: scale()
        // But also need to resize the canvas_wrapper manually.
        setZoom: function() {
            var curr_zoom = this.model.get('curr_zoom'),
                zoom = curr_zoom * 0.01,
                newWidth = parseInt(this.orig_width * zoom, 10),
                newHeight = parseInt(this.orig_height * zoom, 10),
                scale = "scale("+zoom+", "+zoom+")";

            // We want to stay centered on the same spot...
            var curr_centre = this.getCentre(true);

            // Scale canvas via css
            this.$canvas.css({"transform": scale, "-webkit-transform": scale});

            // Scale canvas wrapper manually
            var canvas_w = this.model.get('canvas_width'),
                canvas_h = this.model.get('canvas_height');
            var scaled_w = canvas_w * zoom,
                scaled_h = canvas_h * zoom;
            this.$canvas_wrapper.css({'width':scaled_w+"px", 'height': scaled_h+"px"});
            // and offset the canvas to stay visible
            var margin_top = (scaled_h - canvas_h)/2,
                margin_left = (scaled_w - canvas_w)/2;
            this.$canvas.css({'top': margin_top+"px", "left": margin_left+"px"});

            // ...apply centre from before zooming
            if (curr_centre) {
                this.setCentre(curr_centre);
            }

            // Show zoom level in UI
            $("#zoom_input").val(curr_zoom);
        },

        // Centre the viewport on the middle of the paper
        reCentre: function() {
            var paper_w = this.model.get('paper_width'),
                paper_h = this.model.get('paper_height');
            this.setCentre( {'x':paper_w/2, 'y':paper_h/2} );
        },

        // Get the coordinates on the paper of the viewport center.
        // Used after zoom update (but BEFORE the UI has changed)
        getCentre: function(previous) {
            // Need to know the zoom BEFORE the update
            var m = this.model,
                curr_zoom = m.get('curr_zoom');
            if (previous) {
                curr_zoom = m.previous('curr_zoom');
            }
            if (curr_zoom === undefined) {
                return;
            }
            var viewport_w = this.$main.width(),
                viewport_h = this.$main.height(),
                co = this.$canvas_wrapper.offset(),
                mo = this.$main.offset(),
                offst_left = co.left - mo.left,
                offst_top = co.top - mo.top,
                cx = -offst_left + viewport_w/2,
                cy = -offst_top + viewport_h/2,
                zm_fraction = curr_zoom * 0.01;

            var paper_left = (m.get('canvas_width') - m.get('paper_width'))/2,
                paper_top = (m.get('canvas_height') - m.get('paper_height'))/2;
            return {'x':(cx/zm_fraction)-paper_left, 'y':(cy/zm_fraction)-paper_top};
        },

        // Scroll viewport to place a specified paper coordinate at the centre
        setCentre: function(cx_cy, speed) {
            var m = this.model,
                paper_left = (m.get('canvas_width') - m.get('paper_width'))/2,
                paper_top = (m.get('canvas_height') - m.get('paper_height'))/2;
            var curr_zoom = m.get('curr_zoom'),
                zm_fraction = curr_zoom * 0.01,
                cx = (cx_cy.x+paper_left) * zm_fraction,
                cy = (cx_cy.y+paper_top) * zm_fraction,
                viewport_w = this.$main.width(),
                viewport_h = this.$main.height(),
                offst_left = cx - viewport_w/2,
                offst_top = cy - viewport_h/2;
            speed = speed || 0;
            this.$main.animate({
                scrollLeft: offst_left,
                scrollTop: offst_top
            }, speed);
        },

        // Add a panel to the view
        addOne: function(panel) {
            var view = new PanelView({model:panel});    // uiState:this.uiState
            this.$paper.append(view.render().el);
        },

        // Render is called on init()
        // Update any changes to sizes of paper or canvas
        render: function() {
            var m = this.model,
                zoom = m.get('curr_zoom') * 0.01;

            var paper_w = m.get('paper_width'),
                paper_h = m.get('paper_height'),
                canvas_w = m.get('canvas_width'),
                canvas_h = m.get('canvas_height'),
                paper_left = (canvas_w - paper_w)/2,
                paper_top = (canvas_h - paper_h)/2;

            this.$paper.css({'width': paper_w, 'height': paper_h,
                    'left': paper_left, 'top': paper_top});
            $("#canvas").css({'width': this.model.get('canvas_width'),
                    'height': this.model.get('canvas_height')});

            return this;
        }
    });


    var AlignmentToolbarView = Backbone.View.extend({

        el: $("#alignment-toolbar"),

        model:FigureModel,

        events: {
            "click .aleft": "align_left",
            "click .agrid": "align_grid",
            "click .atop": "align_top",

            "click .awidth": "align_width",
            "click .aheight": "align_height",
            "click .asize": "align_size",
        },

        initialize: function() {
            this.listenTo(this.model, 'change:selection', this.render);
            this.$buttons = $("button", this.$el);
        },

        align_left: function() {
            this.model.align_left();
        },

        align_grid: function() {
            this.model.align_grid();
        },

        align_width: function() {
            this.model.align_size(true, false);
        },
        align_height: function() {
            this.model.align_size(false, true);
        },
        align_size: function() {
            this.model.align_size(true, true);
        },

        align_top: function() {
            this.model.align_top();
        },

        render: function() {
            if (this.model.getSelected().length > 1) {
                this.$buttons.removeAttr("disabled");
            } else {
                this.$buttons.attr("disabled", "disabled");
            }
        }
    });



    // -------------------------Panel View -----------------------------------
    // A Panel is a <div>, added to the #paper by the FigureView below.
    var PanelView = Backbone.View.extend({
        tagName: "div",
        className: "imagePanel",
        template: _.template($('#figure_panel_template').html()),
        label_template: _.template($('#label_template').html()),
        label_table_template: _.template($('#label_table_template').html()),
        scalebar_template: _.template($('#scalebar_panel_template').html()),

        initialize: function(opts) {
            // we render on Changes in the model OR selected shape etc.
            this.model.on('destroy', this.remove, this);
            this.listenTo(this.model, 
                'change:x change:y change:width change:height change:zoom change:dx change:dy',
                this.render_layout);
            this.listenTo(this.model, 'change:scalebar change:pixel_size', this.render_scalebar);
            this.listenTo(this.model, 'change:channels change:theZ change:theT', this.render_image);
            this.listenTo(this.model, 'change:labels', this.render_labels);
            // This could be handled by backbone.relational, but do it manually for now...
            // this.listenTo(this.model.channels, 'change', this.render);
            // During drag, model isn't updated, but we trigger 'drag'
            this.model.on('drag_resize', this.drag_resize, this);

            this.render();
        },

        events: {
            // "click .img_panel": "select_panel"
        },

        // During drag, we resize etc
        drag_resize: function(xywh) {
            var x = xywh[0],
                y = xywh[1],
                w = xywh[2],
                h = xywh[3];
            this.update_resize(x, y, w, h);
        },

        render_layout: function() {
            var x = this.model.get('x'),
                y = this.model.get('y'),
                w = this.model.get('width'),
                h = this.model.get('height');

            this.update_resize(x, y, w, h);
        },

        update_resize: function(x, y, w, h) {

            // update layout of panel on the canvas
            this.$el.css({'top': y +'px',
                        'left': x +'px',
                        'width': w +'px',
                        'height': h +'px'});

            // update the img within the panel
            var zoom = this.model.get('zoom'),
                vp_css = this.model.get_vp_img_css(zoom, w, h);
            this.$img_panel.css(vp_css);

            // update length of scalebar
            var sb = this.model.get('scalebar');
            if (sb && sb.show) {
                // this.$scalebar.css('width':);
                var sb_pixels = sb.length / this.model.get('pixel_size');
                var panel_scale = vp_css.width / this.model.get('orig_width'),
                    sb_width = panel_scale * sb_pixels;
                this.$scalebar.css('width', sb_width);
            }
        },

        render_image: function() {
            var src = this.model.get_img_src();
            this.$img_panel.attr('src', src);
        },

        render_labels: function() {

            $('.label_layout', this.$el).remove();  // clear existing labels

            var labels = this.model.get('labels'),
                self = this,
                positions = {
                    'top':[], 'bottom':[], 'left':[], 'right':[],
                    'topleft':[], 'topright':[],
                    'bottomleft':[], 'bottomright':[]
                };

            // group labels by position
            _.each(labels, function(l) {
                positions[l.position].push(l);
            });

            // Render template for each position and append to Panel.$el
            var html = "";
            _.each(positions, function(lbls, p) {
                var json = {'position':p, 'labels':lbls};
                if (lbls.length === 0) return;
                if (p == 'left' || p == 'right') {
                    html += self.label_table_template(json);
                } else {
                    html += self.label_template(json);
                }
            });
            self.$el.append(html);

            return this;
        },

        render_scalebar: function() {

            if (this.$scalebar) {
                this.$scalebar.remove();
            }
            var sb = this.model.get('scalebar');
            if (sb && sb.show) {
                var sb_json = {};
                sb_json.position = sb.position;
                sb_json.color = sb.color;
                sb_json.width = sb.pixels;  // TODO * scale

                var sb_html = this.scalebar_template(sb_json);
                this.$el.append(sb_html);
            }
            this.$scalebar = $(".scalebar", this.$el);

            // update scalebar size wrt current sizes
            this.render_layout();
        },

        render: function() {

            // Have to handle potential nulls, since the template doesn't like them!
            var json = {'imageId': this.model.get('imageId')};
            // need to add the render string, E.g: 1|110:398$00FF00,2|...

            var html = this.template(json);
            this.$el.html(html);

            this.$img_panel = $(".img_panel", this.$el);    // cache for later

            this.render_image();
            this.render_labels();
            this.render_scalebar();     // also calls render_layout()

            return this;
        }
    });


    // The 'Right Panel' is the floating Info, Preview etc display.
    // It listens to selection changes on the FigureModel and updates it's display
    // By creating new Sub-Views

    var RightPanelView = Backbone.View.extend({

        initialize: function(opts) {
            // we render on selection Changes in the model
            this.listenTo(this.model, 'change:selection', this.render);

            // this.render();
            new LabelsPanelView({model: this.model});
        },

        render: function() {
            var selected = this.model.getSelected();

            if (this.vp) {
                this.vp.clear().remove();
            }
            if (selected.length > 0) {
                this.vp = new ImageViewerView({models: selected}); // auto-renders on init
                $("#viewportContainer").append(this.vp.el);
            }

            if (this.ipv) {
                this.ipv.remove();
            }
            if (selected.length > 0) {
                this.ipv = new InfoPanelView({models: selected});
                $("#infoTab").append(this.ipv.render().el);
            }

            if (this.ctv) {
                this.ctv.remove();
            }
            if (selected.length > 0) {
                this.ctv = new ChannelToggleView({models: selected});
                $("#channelToggle").empty().append(this.ctv.render().el);
            }

        }
    });


    var LabelsPanelView = Backbone.View.extend({

        model: FigureModel,

        template: _.template($("#labels_form_inner_template").html()),

        el: $("#labelsTab"),

        initialize: function(opts) {
            this.listenTo(this.model, 'change:selection', this.render);

            // one-off build 'New Label' form, with same template as used for 'Edit Label' forms
            var json = {'l': {'text':'', 'size':12, 'color':'000000'}, 'position':'top', 'edit':false};
            $('.new-label-form', this.$el).html(this.template(json));
            $('.btn-sm').tooltip({container: 'body', placement:'bottom', toggle:"tooltip"});

            this.render();
        },

        events: {
            "submit .new-label-form": "handle_new_label",
            "click .dropdown-menu a": "select_dropdown_option",
        },

        // Handles all the various drop-down menus in the 'New' AND 'Edit Label' forms
        select_dropdown_option: function(event) {
            var $a = $(event.target),
                $span = $a.children('span');
            // For the Label Text, handle this differently...
            if ($a.attr('data-label')) {
                $('.new-label-form .label-text', this.$el).val( $a.attr('data-label') );
                return;
            }
            // All others, we take the <span> from the <a> and place it in the <button>
            if ($span.length === 0) $span = $a;  // in case we clicked on <span>
            var $li = $span.parent().parent(),
                $button = $li.parent().prev();
            $span = $span.clone();
            $('span:first', $button).replaceWith($span);
            $button.trigger('change');      // can listen for this if we want to 'submit' etc
        },

        // submission of the New Label form
        handle_new_label: function(event) {
            var $form = $(event.target),
                label_text = $('.label-text', $form).val(),
                font_size = $('.font-size', $form).text().trim(),
                position = $('.label-position span:first', $form).attr('data-position'),
                color = $('.label-color span:first', $form).attr('data-color');

            if (label_text.length === 0) {
                alert("Please enter some text for the label");
                return false;
            }

            var selected = this.model.getSelected();

            if (label_text == '[channels]' || label_text == '[channels + colors]') {

                // if we didn't choose 'color' from channels, use picked color
                var ch_color = (label_text.indexOf('colors') == -1 ? color : false);
                _.each(selected, function(m) {
                    m.create_labels_from_channels({color:ch_color, position:position, size:font_size});
                });
                return false;
            }

            var label = {
                text: label_text,
                size: parseInt(font_size, 10),
                position: position,
                color: color
            };

            _.each(selected, function(m) {
                if (label_text === "[image-name]") {
                    var pathnames = m.get('name').split('/');
                    label.text = pathnames[pathnames.length-1];
                } else if (label_text === "[dataset-name]") {
                    label.text = m.get('datasetId') ? m.get('datasetName') : "No/Many Datasets";
                }
                m.add_labels([label]);
            });
            return false;
        },

        render: function() {

            var selected = this.model.getSelected();

            // html is already in place for 'New Label' form - simply show/hide
            if (selected.length === 0) {
                $(".new-label-form", this.$el).hide();
            } else {
                $(".new-label-form", this.$el).show();
            }

            // show selected panels labels below
            var old = this.sel_labels_panel;

            if (selected.length > 0) {
                this.sel_labels_panel = new SelectedPanelsLabelsView({models: selected});
                this.sel_labels_panel.render();
                $("#selected_panels_labels").empty().append(this.sel_labels_panel.$el);
            }
            if (old) {
                old.remove();
            }

            // show scalebar form for selected panels
            var old_sb = this.scalebar_form;
            // if (old_sb) {
            //     old_sb.remove();
            // }
            var $scalebar_form = $("#scalebar_form");

            if (selected.length > 0) {
                this.scalebar_form = new ScalebarFormView({models: selected});
                this.scalebar_form.render();
                $scalebar_form.empty().append(this.scalebar_form.$el);
            }
            if (old_sb) {
                old_sb.remove();
            }

            return this;
        }

    });


    // Created new for each selection change
    var SelectedPanelsLabelsView = Backbone.View.extend({

        template: _.template($("#labels_form_template").html()),
        inner_template: _.template($("#labels_form_inner_template").html()),

        initialize: function(opts) {

            // prevent rapid repetative rendering, when listening to multiple panels
            this.render = _.debounce(this.render);

            this.models = opts.models;
            var self = this;

            _.each(this.models, function(m){
                self.listenTo(m, 'change:labels', self.render);
            });
        },

        events: {
            "submit .edit-label-form": "handle_label_edit",
            "change .btn": "dropdown_btn_changed",
            "click .delete-label": "handle_label_delete",
        },

        handle_label_delete: function(event) {

            var $form = $(event.target).parent(),
                key = $form.attr('data-key'),
                deleteMap = {};

            deleteMap[key] = false;

            _.each(this.models, function(m, i){
                m.edit_labels(deleteMap);
            });
            return false;
        },

        // Automatically submit the form when a dropdown is changed
        dropdown_btn_changed: function(event) {
            $(event.target).closest('form').submit();
        },

        // Use the label 'key' to specify which labels to update
        handle_label_edit: function(event) {

            var $form = $(event.target),
                label_text = $('.label-text', $form).val(),
                font_size = $('.font-size', $form).text().trim(),
                position = $('.label-position span:first', $form).attr('data-position'),
                color = $('.label-color span:first', $form).attr('data-color'),
                key = $form.attr('data-key');

            var new_label = {text:label_text, size:font_size, position:position, color:color};

            var newlbls = {};
            newlbls[key] = new_label;

            _.each(this.models, function(m, i){
                m.edit_labels(newlbls);
            });
            return false;
        },

        render: function() {

            var self = this,
                positions = {'top':{}, 'bottom':{}, 'left':{}, 'right':{},
                    'topleft':{}, 'topright':{}, 'bottomleft':{}, 'bottomright':{}};
            _.each(this.models, function(m, i){
                // group labels by position
                _.each(m.get('labels'), function(l) {
                    // remove duplicates by mapping to unique key
                    var key = m.get_label_key(l),
                        ljson = $.extend(true, {}, l);
                        ljson.key = key;
                    positions[l.position][key] = ljson;
                });
            });

            this.$el.empty();

            // Render template for each position and append to $el
            var html = "";
            _.each(positions, function(lbls, p) {

                lbls = _.map(lbls, function(label, key){ return label; });

                var json = {'position':p, 'labels':lbls};
                if (lbls.length === 0) return;
                json.inner_template = self.inner_template;
                html += self.template(json);
            });
            self.$el.append(html);

            return this;
        }
    });


    // Created new for each selection change
    var ScalebarFormView = Backbone.View.extend({

        template: _.template($("#scalebar_form_template").html()),

        initialize: function(opts) {

            // prevent rapid repetative rendering, when listening to multiple panels
            this.render = _.debounce(this.render);

            this.models = opts.models;
            var self = this;

            _.each(this.models, function(m){
                self.listenTo(m, 'change:scalebar change:pixel_size', self.render);
            });

            // this.$el = $("#scalebar_form");
        },

        events: {
            "submit .scalebar_form": "update_scalebar",
            "change .btn": "dropdown_btn_changed",
            "click .hide_scalebar": "hide_scalebar",
            "click .pixel_size_display": "edit_pixel_size",
            "keypress .pixel_size_input"  : "enter_pixel_size",
            "blur .pixel_size_input"  : "save_pixel_size",
        },

        // simply show / hide editing field
        edit_pixel_size: function() {
            $('.pixel_size_display', this.$el).hide();
            $(".pixel_size_input", this.$el).css('display','inline-block').focus();
        },
        done_pixel_size: function() {
            $('.pixel_size_display', this.$el).show();
            $(".pixel_size_input", this.$el).css('display','none').focus();
        },

        // If you hit `enter`, set pixel_size
        enter_pixel_size: function(e) {
            if (e.keyCode == 13) {
                this.save_pixel_size(e);
            }
        },

        // on 'blur' or 'enter' we save...
        save_pixel_size: function(e) {
            // save will re-render, but only if number has changed - in case not...
            this.done_pixel_size();

            var val = $(e.target).val();
            if (val.length === 0) return;
            var pixel_size = parseFloat(val);
            if (isNaN(pixel_size)) return;
            _.each(this.models, function(m, i){
                m.save('pixel_size', pixel_size);
            });
        },

        // Automatically submit the form when a dropdown is changed
        dropdown_btn_changed: function(event) {
            $(event.target).closest('form').submit();
        },

        hide_scalebar: function() {
            _.each(this.models, function(m, i){
                m.hide_scalebar();
            });
        },

        // called when form changes
        update_scalebar: function(event) {

            var $form = $('#scalebar_form form');

            var length = $('.scalebar-length', $form).val(),
                position = $('.label-position span:first', $form).attr('data-position'),
                color = $('.label-color span:first', $form).attr('data-color');

            _.each(this.models, function(m, i){
                var sb = {show: true};
                if (length != '-') sb.length = parseInt(length, 10);
                if (position != '-') sb.position = position;
                if (color != '-') sb.color = color;

                m.save_scalebar(sb);
            });
            return false;
        },

        render: function() {

            var json = {show: false},
                hidden = false,
                sb;

            _.each(this.models, function(m, i){
                // start with json data from first Panel
                if (!json.pixel_size) {
                    json.pixel_size = m.get('pixel_size');
                } else {
                    pix_sze = m.get('pixel_size');
                    // account for floating point imprecision when comparing
                    if (json.pixel_size.toFixed(10) != pix_sze.toFixed(10)) json.pixel_size = '-';
                }
                sb = m.get('scalebar');
                // ignore scalebars if not visible
                if (sb) {
                    if (!json.length) {
                        json.length = sb.length;
                        json.units = sb.units;
                        json.position = sb.position;
                        json.color = sb.color;
                    }
                    else {
                        if (json.length != sb.length) json.length = '-';
                        if (json.units != sb.units) json.units = '-';
                        if (json.position != sb.position) json.position = '-';
                        if (json.color != sb.color) json.color = '-';
                    }
                }
                // if any panels don't have scalebar - we allow to add
                if(!sb || !sb.show) hidden = true;
            });

            if (this.models.length === 0 || hidden) {
                json.show = true;
            }
            json.length = json.length || 10;
            json.units = json.units || 'um';
            json.position = json.position || 'bottomright';
            json.color = json.color || 'FFFFFF';

            var html = this.template(json);
            this.$el.html(html);

            return this;
        }
    });


    var InfoPanelView = Backbone.View.extend({

        template: _.template($("#info_panel_template").html()),
        xywh_template: _.template($("#xywh_panel_template").html()),

        initialize: function(opts) {
            // if (opts.models) {
            if (opts.models.length > 1) {
                this.models = opts.models;
                var self = this;
                _.each(this.models, function(m){
                    self.listenTo(m, 'change:x change:y change:width change:height', self.render);
                });
            } else if (opts.models.length == 1) {
                this.model = opts.models[0];
                this.listenTo(this.model, 'change:x change:y change:width change:height', this.render);
                this.listenTo(this.model, 'drag_resize', this.drag_resize);
            }
            // } 
        },

        // just update x,y,w,h by rendering ONE template
        drag_resize: function(xywh) {
            $("#xywh_table").remove();
            var json = {'x': xywh[0], 'y':xywh[1], 'width':xywh[2], 'height':xywh[3]},
                xywh_html = this.xywh_template(json);
            this.$el.append(xywh_html);
        },

        // render BOTH templates
        render: function() {
            var json;
            if (this.model) {
                json = this.model.toJSON();
                json.width = json.width.toFixed(0);
                json.height = json.height.toFixed(0);
            } else if (this.models) {
                var title = this.models.length + " Panels Selected...";
                _.each(this.models, function(m, i){
                    // start with json data from first Panel
                    if (!json) {
                        json = m.toJSON();
                        json.name = title;
                        json.width = json.width.toFixed(0);
                        json.height = json.height.toFixed(0);
                    } else {
                        // compare json summary so far with this Panel
                        var this_json = m.toJSON(),
                            attrs = ["imageId", "orig_width", "orig_height", "sizeT", "sizeZ"];
                        _.each(attrs, function(a){
                            if (json[a] != this_json[a]) {
                                json[a] = "-";
                            }
                        });
                        // Show the min x & y. Format & compare width & height
                        json.x = Math.min(json.x, this_json.x);
                        json.y = Math.min(json.y, this_json.y);
                        if (json.width != this_json.width.toFixed(0)) json.width = "-";
                        if (json.height != this_json.height.toFixed(0)) json.height = "-";
                    }
                });
            }

            if (json) {
                var html = this.template(json),
                    xywh_html = this.xywh_template(json);
                this.$el.html(html + xywh_html);
            }
            return this;
        }

    });


    var ImageViewerView = Backbone.View.extend({

        template: _.template($('#viewport_template').html()),

        className: "imageViewer",

        initialize: function(opts) {

            // prevent rapid repetative rendering, when listening to multiple panels
            this.render = _.debounce(this.render);

            this.full_size = 250;

            this.models = opts.models;
            var self = this,
                zoom_sum = 0,
                theZ_sum = 0,
                theT_sum = 0;
            this.sizeZ = this.models[0].get('sizeZ');
            this.sizeT = this.models[0].get('sizeT');

            _.each(this.models, function(m){
                self.listenTo(m, 'change:width change:height change:channels change:zoom change:theZ change:theT', self.render);
                zoom_sum += m.get('zoom');
                theZ_sum += m.get('theZ');
                theT_sum += m.get('theT');
                if (self.sizeZ != m.get('sizeZ')) {
                    self.sizeZ = undefined;
                }
                if (self.sizeT != m.get('sizeT')) {
                    self.sizeT = undefined;
                }
            });

            this.zoom_avg = zoom_sum/ this.models.length;
            this.theZ_avg = theZ_sum/ this.models.length;
            this.theT_avg = theT_sum/ this.models.length;

            $("#vp_zoom_slider").slider({
                max: 800,
                min: 100,
                value: self.zoom_avg,
                slide: function(event, ui) {
                    self.update_img_css(ui.value, 0, 0);
                },
                stop: function( event, ui ) {
                    self.zoom_avg = ui.value;
                    _.each(self.models, function(m){
                        m.save('zoom', ui.value);
                    });
                }
            });
            this.$vp_zoom_value = $("#vp_zoom_value");

            var Z_disabled = false,
                sizeZ = self.sizeZ;
            if (!sizeZ || sizeZ === 1) {    // undefined or 1
                Z_disabled = true;
                sizeZ = 1;
            }
            $("#vp_z_slider").slider({
                orientation: "vertical",
                max: sizeZ,
                disabled: Z_disabled,
                min: 1,             // model is 0-based, UI is 1-based
                value: self.theZ_avg + 1,
                slide: function(event, ui) {
                    $("#vp_z_value").text(ui.value + "/" + self.sizeZ);
                },
                stop: function( event, ui ) {
                    _.each(self.models, function(m){
                        m.save('theZ', ui.value - 1);
                    });
                }
            });

            var T_disabled = false,
                sizeT = self.sizeT;
            if (!sizeT || sizeT === 1) {    // undefined or 1
                T_disabled = true;
                sizeT = 1;
            }
            $("#vp_t_slider").slider({
                max: sizeT,
                disabled: T_disabled,
                min: 1,             // model is 0-based, UI is 1-based
                value: self.theT_avg + 1,
                slide: function(event, ui) {
                    $("#vp_t_value").text(ui.value + "/" + self.sizeT);
                },
                stop: function( event, ui ) {
                    _.each(self.models, function(m){
                        m.save('theT', ui.value - 1);
                    });
                }
            });

            this.render();
        },

        events: {
            "mousedown .vp_img": "mousedown",
            "mousemove .vp_img": "mousemove",
            "mouseup .vp_img": "mouseup",
        },

        mousedown: function(event) {
            this.dragging = true;
            this.dragstart_x = event.clientX;
            this.dragstart_y = event.clientY;
            return false;
        },

        mouseup: function(event) {
            var dx = event.clientX - this.dragstart_x,
                dy = event.clientY - this.dragstart_y;
            this.update_img_css(this.zoom_avg, dx, dy, true);
            this.dragging = false;
            return false;
        },

        mousemove: function(event) {
            if (this.dragging) {
                var dx = event.clientX - this.dragstart_x,
                    dy = event.clientY - this.dragstart_y;
                this.update_img_css(this.zoom_avg, dx, dy);
            }
            return false;
        },

        // called by the parent View before .remove()
        clear: function() {
            // clean up zoom slider etc
            $( "#vp_zoom_slider" ).slider( "destroy" );
            $("#vp_z_slider").slider("destroy");
            $("#vp_t_slider").slider("destroy");
            this.$vp_zoom_value.text('');
            return this;
        },

        update_img_css: function(zoom, dx, dy, save) {

            if (this.$vp_img) {
                var frame_w = this.$vp_frame.width() + 2,
                    frame_h = this.$vp_frame.height() + 2;
                dx = (dx / frame_w) * this.models[0].get('orig_width');
                dy = (dy / frame_h) * this.models[0].get('orig_height');
                dx += this.dx;
                dy += this.dy;
                this.$vp_img.css( this.models[0].get_vp_img_css(zoom, frame_w, frame_h, dx, dy) );
                this.$vp_zoom_value.text(zoom + "%");

                if (save) {
                    this.dx = dx;
                    this.dy = dy;
                    _.each(this.models, function(m){
                        m.save('dx', dx);
                        m.save('dy', dy);
                    });
                }
            }
        },

        render: function() {

            if (this.models.length === 0);

            // only show viewport if original w / h ratio is same for all models
            var model = this.models[0];
            var orig_wh,
                sum_wh = 0,
                sum_zoom = 0,
                sum_theZ = 0,
                max_theZ = 0,
                sum_theT = 0,
                max_theT = 0,
                sum_dx = 0,
                sum_dy = 0,
                imgs_css = [],
                same_wh = true;

            // first, work out frame w & h - use average w/h ratio of all selected panels
            _.each(this.models, function(m){
                var wh = m.get('orig_width') / m.get('orig_height');
                if (!orig_wh) {
                    orig_wh = wh;
                } else if (orig_wh != wh) {
                    same_wh = false;
                }
                sum_wh += (m.get('width')/ m.get('height'));
                sum_zoom += m.get('zoom');
                sum_theZ += m.get('theZ');
                sum_theT += m.get('theT');
                max_theZ = Math.max(max_theZ, m.get('theZ'));
                max_theT = Math.max(max_theT, m.get('theT'));
            });
            // Only continue if panels are all same w/h ratio
            if (!same_wh) return;

            // get average viewport frame w/h & zoom
            var wh = sum_wh/this.models.length,
                zoom = sum_zoom/this.models.length,
                theZ = sum_theZ/this.models.length,
                theT = sum_theT/this.models.length;
            if (wh <= 1) {
                frame_h = this.full_size;
                frame_w = this.full_size * wh;
            } else {
                frame_w = this.full_size;
                frame_h = this.full_size / wh;
            }

            // Now get img src & positioning css for each panel, 
            _.each(this.models, function(m){
                sum_dx += m.get('dx');
                sum_dy += m.get('dy');
                var src = m.get_img_src(),
                    img_css = model.get_vp_img_css(m.get('zoom'), frame_w, frame_h, m.get('dx'), m.get('dy'));
                img_css.src = src;
                imgs_css.push(img_css);
            });

            // save these average offsets in hand for dragging (apply to all panels)
            this.dx = sum_dx/this.models.length;
            this.dy = sum_dy/this.models.length;

            var json = {};

            json.opacity = 1 / imgs_css.length;
            json.imgs_css = imgs_css;
            json.frame_w = frame_w;
            json.frame_h = frame_h;
            json.sizeZ = this.sizeZ || "-";
            json.theZ = theZ+1;
            json.sizeT = this.sizeT || "-";
            json.theT = theT+1;
            if (max_theZ != theZ) {
                json.theZ = "-";
            }
            if (max_theT != theT) {
                json.theT = "-";
            }
            var html = this.template(json);
            this.$el.html(html);

            this.$vp_frame = $(".vp_frame", this.$el);  // cache for later
            this.$vp_img = $(".vp_img", this.$el);
            this.$vp_zoom_value.text(zoom + "%");

            return this;
        }
    });

    // Coloured Buttons to Toggle Channels on/off.
    var ChannelToggleView = Backbone.View.extend({
        tagName: "div",
        template: _.template($('#channel_toggle_template').html()),

        initialize: function(opts) {
            // This View may apply to a single PanelModel or a list
            if (opts.models.length > 1) {
                this.models = opts.models;
                var self = this;
                _.each(this.models, function(m){
                    self.listenTo(m, 'change:channels', self.render);
                });
            } else if (opts.models.length == 1) {
                this.model = opts.models[0];
                this.listenTo(this.model, 'change:channels', this.render);
            }
        },

        events: {
            "click .channel-btn": "toggle_channel",
            "click .dropdown-menu a": "pick_color"
        },

        pick_color: function(e) {
            var color = e.currentTarget.getAttribute('data-color'),
                idx = $(e.currentTarget).parent().parent().attr('data-index');
            if (this.model) {
                this.model.save_channel(idx, 'color', color);
            } else if (this.models) {
                _.each(this.models, function(m){
                    m.save_channel(idx, 'color', color);
                });
            }
        },

        toggle_channel: function(e) {
            var idx = e.currentTarget.getAttribute('data-index');

            if (this.model) {
                this.model.toggle_channel(idx);
            } else if (this.models) {
                // 'flat' means that some panels have this channel on, some off
                var flat = $(e.currentTarget).hasClass('ch-btn-flat');
                _.each(this.models, function(m){
                    if(flat) {
                        m.toggle_channel(idx, true);
                    } else {
                        m.toggle_channel(idx);
                    }
                });
            }
        },

        render: function() {
            var json, html;
            if (this.model) {
                json = {'channels': this.model.get('channels')};
                html = this.template(json);
                this.$el.html(html);
            } else if (this.models) {

                // Comare channels from each Panel Model to see if they are
                // compatible, and compile a summary json.
                json = [];
                var compatible = true;

                _.each(this.models, function(m, i){
                    var chs = m.get('channels');
                    // start with a copy of the first image channels
                    if (json.length === 0) {
                        _.each(chs, function(c) {
                            json.push($.extend(true, {}, c));
                        });
                    } else{
                        // compare json summary so far with this channels
                        if (json.length != chs.length) {
                            compatible = false;
                        } else {
                            // if attributes don't match - show 'null' state
                            _.each(chs, function(c, i) {
                                if (json[i].color != c.color) {
                                    json[i].color = 'ccc';
                                }
                                if (json[i].active != c.active) {
                                    json[i].active = undefined;
                                }
                            });
                        }
                    }

                });
                if (compatible) {
                    html = this.template({'channels':json});
                    this.$el.html(html);
                }
            }
            return this;
        }
    });

    // -------------- Selection Overlay Views ----------------------


    // SvgView uses ProxyRectModel to manage Svg Rects (raphael)
    // This converts between zoomed coordiantes of the html DOM panels
    // and the unzoomed SVG overlay.
    // Attributes of this model apply to the SVG canvas and are updated from
    // the PanelModel.
    // The SVG RectView (Raphael) notifies this Model via trigger 'drag' & 'dragStop'
    // and this is delegated to the PanelModel via trigger or set respectively.
    var ProxyRectModel = Backbone.Model.extend({

        initialize: function(opts) {
            this.panelModel = opts.panel;    // ref to the genuine PanelModel
            this.figureModel = opts.figure;

            this.renderFromModel();

            // Refresh c
            this.listenTo(this.figureModel, 'change:curr_zoom', this.renderFromModel);
            this.listenTo(this.panelModel, 'change:x change:y change:width change:height', this.renderFromModel);
            // when PanelModel is being dragged, but NOT by this ProxyRectModel...
            this.listenTo(this.panelModel, 'drag_resize', this.renderFromTrigger);
            this.listenTo(this.panelModel, 'change:selected', this.renderSelection);
            this.panelModel.on('destroy', this.clear, this);
            // listen to a trigger on this Model (triggered from Rect)
            this.listenTo(this, 'drag_xy', this.drag_xy);
            this.listenTo(this, 'drag_xy_stop', this.drag_xy_stop);
            this.listenTo(this, 'drag_resize', this.drag_resize);
            // listen to change to this model - update PanelModel
            this.listenTo(this, 'drag_resize_stop', this.drag_resize_stop);
        },

        // return the SVG x, y, w, h (converting from figureModel)
        getSvgCoords: function(coords) {
            var zoom = this.figureModel.get('curr_zoom') * 0.01,
                paper_top = (this.figureModel.get('canvas_height') - this.figureModel.get('paper_height'))/2,
                paper_left = (this.figureModel.get('canvas_width') - this.figureModel.get('paper_width'))/2,
                rect_x = (paper_left + 1 + coords.x) * zoom,
                rect_y = (paper_top + 1 + coords.y) * zoom,
                rect_w = coords.width * zoom,
                rect_h = coords.height * zoom;
            return {'x':rect_x, 'y':rect_y, 'width':rect_w, 'height':rect_h};
        },

        // return the Model x, y, w, h (converting from SVG coords)
        getModelCoords: function(coords) {
            var zoom = this.figureModel.get('curr_zoom') * 0.01,
                paper_top = (this.figureModel.get('canvas_height') - this.figureModel.get('paper_height'))/2,
                paper_left = (this.figureModel.get('canvas_width') - this.figureModel.get('paper_width'))/2,
                x = (coords.x/zoom) - paper_left - 1,
                y = (coords.y/zoom) - paper_top - 1,
                w = coords.width/zoom,
                h = coords.height/zoom;
            return {'x':x>>0, 'y':y>>0, 'width':w>>0, 'height':h>>0};
        },

        // called on trigger from the RectView, on drag of the whole rect OR handle for resize.
        // we simply convert coordinates and delegate to figureModel
        drag_xy: function(xy, save) {
            var zoom = this.figureModel.get('curr_zoom') * 0.01,
                dx = xy[0]/zoom,
                dy = xy[1]/zoom;

            this.figureModel.drag_xy(dx, dy, save);
        },

        // As above, but this time we're saving the changes to the Model
        drag_xy_stop: function(xy) {
            this.drag_xy(xy, true);
        },

        // Called on trigger from the RectView on resize. 
        // Need to convert from Svg coords to Model and notify the PanelModel without saving.
        drag_resize: function(xywh) {
            var coords = this.getModelCoords({'x':xywh[0], 'y':xywh[1], 'width':xywh[2], 'height':xywh[3]});
            this.panelModel.drag_resize(coords.x, coords.y, coords.width, coords.height);
        },

        // As above, but need to update the Model on changes to Rect (drag stop etc)
        drag_resize_stop: function(xywh) {
            var coords = this.getModelCoords({'x':xywh[0], 'y':xywh[1], 'width':xywh[2], 'height':xywh[3]});
            this.panelModel.save(coords);
        },

        // Called when the FigureModel zooms or the PanelModel changes coords.
        // Refreshes the RectView since that listens to changes in this ProxyModel
        renderFromModel: function() {
            this.set( this.getSvgCoords({
                'x': this.panelModel.get('x'),
                'y': this.panelModel.get('y'),
                'width': this.panelModel.get('width'),
                'height': this.panelModel.get('height')
            }) );
        },

        // While the Panel is being dragged (by the multi-select Rect), we need to keep updating
        // from the 'multiselectDrag' trigger on the model. RectView renders on change
        renderFromTrigger:function(xywh) {
            var c = this.getSvgCoords({
                'x': xywh[0],
                'y': xywh[1],
                'width': xywh[2],
                'height': xywh[3]
            });
            this.set( this.getSvgCoords({
                'x': xywh[0],
                'y': xywh[1],
                'width': xywh[2],
                'height': xywh[3]
            }) );
        },

        // When PanelModel changes selection - update and RectView will render change
        renderSelection: function() {
            this.set('selected', this.panelModel.get('selected'));
        },

        // Handle click (mousedown) on the RectView - changing selection.
        handleClick: function(event) {
            if (event.shiftKey) {
                this.figureModel.addSelected(this.panelModel);
            } else {
                this.figureModel.setSelected(this.panelModel);
            }
        },

        clear: function() {
            this.destroy();
        }

    });


    // This model underlies the Rect that is drawn around multi-selected panels
    // (only shown if 2 or more panels selected)
    // On drag or resize, we calculate how to move or resize the seleted panels.
    var MultiSelectRectModel = ProxyRectModel.extend({

        defaults: {
            x: 0,
            y: 0,
            width: 0,
            height: 0
        },

        initialize: function(opts) {
            this.figureModel = opts.figureModel;

            // listen to a trigger on this Model (triggered from Rect)
            this.listenTo(this, 'drag_xy', this.drag_xy);
            this.listenTo(this, 'drag_xy_stop', this.drag_xy_stop);
            this.listenTo(this, 'drag_resize', this.drag_resize);
            this.listenTo(this, 'drag_resize_stop', this.drag_resize_stop);
            this.listenTo(this.figureModel, 'change:selection', this.updateSelection);
            this.listenTo(this.figureModel, 'change:curr_zoom', this.updateSelection);

            // also listen for drag_xy coming from a selected panel
            this.listenTo(this.figureModel, 'drag_xy', this.update_xy);
        },


        // Need to re-draw on selection AND zoom changes
        updateSelection: function() {

            var min_x = 100000, max_x = -10000,
                min_y = 100000, max_y = -10000;

            var selected = this.figureModel.getSelected();
            if (selected.length < 2){

                this.set({
                    'x': 0,
                    'y': 0,
                    'width': 0,
                    'height': 0,
                    'selected': false
                });
                return;
            }

            for (var i=0; i<selected.length; i++) {
                var panel = selected[i],
                    x = panel.get('x'),
                    y = panel.get('y'),
                    w = panel.get('width'),
                    h = panel.get('height');
                min_x = Math.min(min_x, x);
                max_x = Math.max(max_x, x+w);
                min_y = Math.min(min_y, y);
                max_y = Math.max(max_y, y+h);
            }

            this.set( this.getSvgCoords({
                'x': min_x,
                'y': min_y,
                'width': max_x - min_x,
                'height': max_y - min_y
            }) );

            // Rect SVG will be notified and re-render
            this.set('selected', true);
        },


        // Called when we are notified of drag_xy on one of the Panels
        update_xy: function(dxdy) {
            if (! this.get('selected')) return;     // if we're not visible, ignore

            var svgCoords = this.getSvgCoords({
                'x': dxdy[0],
                'y': dxdy[1],
                'width': 0,
                'height': 0,
            });
            this.set({'x':svgCoords.x, 'y':svgCoords.y});
        },

        // RectView drag is delegated to Panels to update coords (don't save)
        drag_xy: function(dxdy, save) {
            // we just get [x,y] but we need [x,y,w,h]...
            var x = dxdy[0] + this.get('x'),
                y = dxdy[1] + this.get('y');
            var xywh = [x, y, this.get('width'), this.get('height')];
            this.notifyModelofDrag(xywh, save);
        },

        // As above, but Save is true since we're done dragging
        drag_xy_stop: function(dxdy, save) {
            this.drag_xy(dxdy, true);
            // Have to keep our proxy model in sync
            this.set({
                'x': dxdy[0] + this.get('x'),
                'y': dxdy[1] + this.get('y')
            });
        },

        // While the multi-select RectView is being dragged, we need to calculate the new coords
        // of all selected Panels, based on the start-coords and the current coords of
        // the multi-select Rect.
        drag_resize: function(xywh, save) {
            this.notifyModelofDrag(xywh, save);
        },

        // RectView dragStop is delegated to Panels to update coords (with save 'true')
        drag_resize_stop: function(xywh) {
            this.notifyModelofDrag(xywh, true);

            this.set({
                'x': xywh[0],
                'y': xywh[1],
                'width': xywh[2],
                'height': xywh[3]
            });
        },

        // While the multi-select RectView is being dragged, we need to calculate the new coords
        // of all selected Panels, based on the start-coords and the current coords of
        // the multi-select Rect.
        notifyModelofDrag: function(xywh, save) {
            var startCoords = this.getModelCoords({
                'x': this.get('x'),
                'y': this.get('y'),
                'width': this.get('width'),
                'height': this.get('height')
            });
            var dragCoords = this.getModelCoords({
                'x': xywh[0],
                'y': xywh[1],
                'width': xywh[2],
                'height': xywh[3]
            });

            // var selected = this.figureModel.getSelected();
            // for (var i=0; i<selected.length; i++) {
            //     selected[i].multiselectdrag(startCoords.x, startCoords.y, startCoords.width, startCoords.height,
            //         dragCoords.x, dragCoords.y, dragCoords.width, dragCoords.height, save);
            this.figureModel.multiselectdrag(startCoords.x, startCoords.y, startCoords.width, startCoords.height,
                    dragCoords.x, dragCoords.y, dragCoords.width, dragCoords.height, save);
            // };
        },

        // Ignore mousedown
        handleClick: function(event) {

        }
    });

    // var ProxyRectModelList = Backbone.Collection.extend({
    //     model: ProxyRectModel
    // });

    var SvgView = Backbone.View.extend({

        initialize: function(opts) {

            var self = this,
                canvas_width = this.model.get('canvas_width'),
                canvas_height = this.model.get('canvas_height');

            // Create <svg> canvas
            this.raphael_paper = Raphael("canvas_wrapper", canvas_width, canvas_height);

            // this.panelRects = new ProxyRectModelList();

            // Add global click handler
            $("#canvas_wrapper>svg").mousedown(function(event){
                self.handleClick(event);
            });

            // If a panel is added...
            this.model.panels.on("add", this.addOne, this);
            this.listenTo(this.model, 'change:curr_zoom', this.setZoom);

            var multiSelectRect = new MultiSelectRectModel({figureModel: this.model}),
                rv = new RectView({'model':multiSelectRect, 'paper':this.raphael_paper});
            rv.selected_line_attrs = {'stroke-width': 1, 'stroke':'#4b80f9'};
        },

        // A panel has been added - We add a corresponding Raphael Rect 
        addOne: function(m) {

            var rectModel = new ProxyRectModel({panel: m, figure:this.model});
            new RectView({'model':rectModel, 'paper':this.raphael_paper});
        },

        // TODO
        remove: function() {
            // TODO: remove from svg, remove event handlers etc.
        },

        // We simply re-size the Raphael svg itself - Shapes have their own zoom listeners
        setZoom: function() {
            var zoom = this.model.get('curr_zoom') * 0.01,
                newWidth = this.model.get('canvas_width') * zoom,
                newHeight = this.model.get('canvas_height') * zoom;

            this.raphael_paper.setSize(newWidth, newHeight);
        },

        // Any mouse click (mousedown) that isn't captured by Panel Rect clears selection
        handleClick: function(event) {
            this.model.clearSelected();
        }
    });

