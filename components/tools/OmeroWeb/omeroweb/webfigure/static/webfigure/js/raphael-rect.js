
var RectView = Backbone.View.extend({

    handle_wh: 6,
    default_line_attrs: {'stroke-width':0, 'stroke': '#4b80f9', 'cursor': 'default', 'fill-opacity':0.01, 'fill': '#fff'},
    selected_line_attrs: {'stroke':'#4b80f9', 'stroke-width':2 },
    handle_attrs: {'stroke':'#4b80f9', 'fill':'#fff', 'cursor': 'default', 'fill-opacity':1.0},

    // make a child on click
    events: {
        //'mousedown': 'selectShape'    // we need to handle this more manually (see below)
    },
    initialize: function(options) {
        // Here we create the shape itself, the drawing handles and
        // bind drag events to all of them to drag/resize the rect.

        var self = this;
        this.paper = options.paper;
        // this.manager = options.manager;

        // Set up our 'view' attributes (for rendering without updating model)
        this.x = this.model.get("x");
        this.y = this.model.get("y");
        this.width = this.model.get("width");
        this.height = this.model.get("height");

        // ---- Create Handles -----
        // map of centre-points for each handle
        this.handleIds = {'nw': [this.x, this.y],
            'n': [this.x+this.width/2,this.y],
            'ne': [this.x+this.width,this.y],
            'w': [this.x, this.y+this.height/2],
            'e': [this.x+this.width, this.y+this.height/2],
            'sw': [this.x, this.y+this.height],
            's': [this.x+this.width/2, this.y+this.height],
            'se': [this.x+this.width, this.y+this.height]
        };
        // draw handles
        self.handles = this.paper.set();
        var _handle_drag = function() {
            return function (dx, dy, mouseX, mouseY, event) {
                // on DRAG...

                // If drag on corner handle, retain aspect ratio. dx/dy = aspect
                var keep_ratio = true;  // event.shiftKey - used to be dependent on shift
                if (keep_ratio && this.h_id.length === 2) {     // E.g. handle is corner 'ne' etc
                    if (this.h_id === 'se' || this.h_id === 'nw') {
                        if (Math.abs(dx/dy) > this.aspect) {
                            dy = dx/this.aspect;
                        } else {
                            dx = dy*this.aspect;
                        }
                    } else {
                        if (Math.abs(dx/dy) > this.aspect) {
                            dy = -dx/this.aspect;
                        } else {
                            dx = -dy*this.aspect;
                        }
                    }
                }
                // Use dx & dy to update the location of the handle and the corresponding point of the parent
                var new_x = this.ox + dx;
                var new_y = this.oy + dy;
                if (this.h_id.indexOf('e') > -1) {    // if we're dragging an 'EAST' handle, update width
                    this.rect.width = new_x - self.x + self.handle_wh/2;
                }
                if (this.h_id.indexOf('s') > -1) {    // if we're dragging an 'SOUTH' handle, update height
                    this.rect.height = new_y - self.y + self.handle_wh/2;
                }
                if (this.h_id.indexOf('n') > -1) {    // if we're dragging an 'NORTH' handle, update y and height
                    this.rect.y = new_y + self.handle_wh/2;
                    this.rect.height = this.obottom - new_y;
                }
                if (this.h_id.indexOf('w') > -1) {    // if we're dragging an 'WEST' handle, update x and width
                    this.rect.x = new_x + self.handle_wh/2;
                    this.rect.width = this.oright - new_x;
                }
                this.rect.model.trigger("drag_resize", [this.rect.x, this.rect.y, this.rect.width, this.rect.height]);
                this.rect.updateShape();
                return false;
            };
        };
        var _handle_drag_start = function() {
            return function () {
                // START drag: simply note the location we started
                this.ox = this.attr("x");
                this.oy = this.attr("y");
                this.oright = self.width + this.ox;
                this.obottom = self.height + this.oy;
                this.aspect = self.model.get('width') / self.model.get('height');
                return false;
            };
        };
        var _handle_drag_end = function() {
            return function() {
                this.rect.model.trigger('drag_resize_stop', [this.rect.x, this.rect.y,
                    this.rect.width, this.rect.height]);
                return false;
            };
        };
        for (var key in this.handleIds) {
            var hx = this.handleIds[key][0];
            var hy = this.handleIds[key][1];
            var handle = this.paper.rect(hx-self.handle_wh/2, hy-self.handle_wh/2, self.handle_wh, self.handle_wh).attr(self.handle_attrs);
            handle.attr({'cursor': key + '-resize'});     // css, E.g. ne-resize
            handle.h_id = key;
            handle.rect = self;

            handle.drag(
                _handle_drag(),
                _handle_drag_start(),
                _handle_drag_end()
            );
            handle.mousedown(function(e){
                e.stopImmediatePropagation();
            });
            self.handles.push(handle);
        }
        self.handles.hide();     // show on selection


        // ----- Create the rect itself ----
        this.element = this.paper.rect();
        this.element.attr( self.default_line_attrs );
        // set "element" to the raphael node (allows Backbone to handle events)
        this.setElement(this.element.node);
        this.delegateEvents(this.events);   // we need to rebind the events

        // Handle drag
        this.element.drag(
            function(dx, dy) {
                // DRAG, update location and redraw
                // TODO - need some way to disable drag if we're not in select state
                //if (manager.getState() !== ShapeManager.STATES.SELECT) {
                //    return;
                //}
                self.x = dx+this.ox;
                self.y = this.oy+dy;
                self.dragging = true;
                self.model.trigger("drag_xy", [dx, dy]);
                self.updateShape();
                return false;
            },
            function() {
                // START drag: note the location of all points (copy list)
                this.ox = this.attr('x');
                this.oy = this.attr('y');
                return false;
            },
            function() {
                // STOP: save current position to model
                self.model.trigger('drag_xy_stop', [self.x-this.ox, self.y-this.oy]);
                self.dragging = false;
                return false;
            }
        );

        // If we're starting DRAG, don't let event propogate up to dragdiv etc.
        // https://groups.google.com/forum/?fromgroups=#!topic/raphaeljs/s06GIUCUZLk
        this.element.mousedown(function(e){
             e.stopImmediatePropagation();
             self.selectShape(e);
        });

        this.updateShape();  // sync position, selection etc.

        // Finally, we need to render when model changes
        this.model.on('change', this.render, this);
        this.model.on('destroy', this.destroy, this);

    },

    // render updates our local attributes from the Model AND updates coordinates
    render: function(event) {
        if (this.dragging) return;
        this.x = this.model.get("x");
        this.y = this.model.get("y");
        this.width = this.model.get("width");
        this.height = this.model.get("height");
        this.updateShape();
    },

    // used to update during drags etc. Also called by render()
    updateShape: function() {
        this.element.attr({'x':this.x, 'y':this.y, 'width':this.width, 'height':this.height});

        // TODO Draw diagonals on init - then simply update here (show if selected)
        // var path1 = "M" + this.x +","+ this.y +"l"+ this.width +","+ this.height,
        //     path2 = "M" + (this.x+this.width) +","+ this.y +"l-"+ this.width +","+ this.height;
        //     // rectangle plus 2 diagonal lines
        //     this.paper.path(path1).attr('stroke', '#4b80f9');
        //     this.paper.path(path2).attr('stroke', '#4b80f9');

        // if (this.manager.selected_shape_id === this.model.get("id")) {
        if (this.model.get('selected')) {
            this.element.attr( this.selected_line_attrs );  //.toFront();
            this.handles.show().toFront();
        } else {
            this.element.attr( this.default_line_attrs );    // this should be the shapes OWN line / fill colour etc.
            this.handles.hide();
        }

        this.handleIds = {'nw': [this.x, this.y],
        'n': [this.x+this.width/2,this.y],
        'ne': [this.x+this.width,this.y],
        'w': [this.x, this.y+this.height/2],
        'e': [this.x+this.width, this.y+this.height/2],
        'sw': [this.x, this.y+this.height],
        's': [this.x+this.width/2, this.y+this.height],
        'se': [this.x+this.width, this.y+this.height]};
        var hnd, h_id, hx, hy;
        for (var h=0, l=this.handles.length; h<l; h++) {
            hnd = this.handles[h];
            h_id = hnd.h_id;
            hx = this.handleIds[h_id][0];
            hy = this.handleIds[h_id][1];
            hnd.attr({'x':hx-this.handle_wh/2, 'y':hy-this.handle_wh/2});
        }
    },

    selectShape: function(event) {
        // pass back to model to update all selection
        this.model.handleClick(event);
    },

    // Destroy: remove Raphael elements and event listeners
    destroy: function() {
        this.element.remove();
        this.handles.remove();
        this.model.off('change', this.render, this);
    }
});
