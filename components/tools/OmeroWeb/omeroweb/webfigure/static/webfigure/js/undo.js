// --------------- UNDO MANAGER ----------------------

/*global Backbone:true */

var UndoManager = Backbone.Model.extend({
    defaults: function(){
        return {
            undo_pointer: -1
        };
    },
    initialize: function(opts) {
        this.figureModel = opts.figureModel;    // need for setting selection etc
        this.listenTo(this.figureModel, 'reset_undo_redo', this.resetQueue);
        this.undoQueue = [];
        this.undoInProgress = false;
        //this.undo_pointer = -1;
        // Might need to undo/redo multiple panels/objects
        this.undo_functions = [];
        this.redo_functions = [];
    },
    resetQueue: function() {
        console.log('resetQueue');
        this.undoQueue = [];
        this.set('undo_pointer', -1);
    },
    canUndo: function() {
        return this.get('undo_pointer') >= 0;
    },
    undo: function() {
        var pointer = this.get('undo_pointer');
        if (pointer < 0) {
            return;
        }
        this.undoQueue[pointer].undo();
        this.set('undo_pointer',pointer-1); // trigger change
    },
    canRedo: function() {
        return this.get('undo_pointer')+1 < this.undoQueue.length;
    },
    redo: function() {
        var pointer = this.get('undo_pointer');
        if (pointer+1 >= this.undoQueue.length) {
            return;
        }
        this.undoQueue[pointer+1].redo();
        this.set('undo_pointer', pointer+1); // trigger change event
    },
    postEdit: function(undo) {
        var pointer = this.get('undo_pointer');
        // remove any undo ahead of current position
        if (this.undoQueue.length > pointer+1) {
            this.undoQueue = this.undoQueue.slice(0, pointer+1);
        }
        this.undoQueue.push(undo);
        this.set('undo_pointer', pointer+1); // trigger change event
    },

    // START here - Listen to 'add' events...
    listenToCollection: function(collection) {
        var self = this;
        // Add listener to changes in current models
        collection.each(function(m){
            self.listenToModel(m);
        });
        collection.on('add', function(m) {
            // start listening for change events on the model
            self.listenToModel(m);
            if (!self.undoInProgress){
                // post an 'undo'
                self.handleAdd(m, collection);
            }
        });
        collection.on('remove', function(m) {
            if (!self.undoInProgress){
                // post an 'undo'
                self.handleRemove(m, collection);
            }
        });
    },

    handleRemove: function(m, collection) {
        var self = this;
        self.postEdit( {
            name: "Undo Remove",
            undo: function() {
                self.undoInProgress = true;
                collection.add(m);
                self.figureModel.notifySelectionChange();
                self.undoInProgress = false;
            },
            redo: function() {
                self.undoInProgress = true;
                m.destroy();
                self.figureModel.notifySelectionChange();
                self.undoInProgress = false;
            }
        });
    },

    handleAdd: function(m, collection) {
        var self = this;
        self.postEdit( {
            name: "Undo Add",
            undo: function() {
                self.undoInProgress = true;
                m.destroy();
                self.figureModel.notifySelectionChange();
                self.undoInProgress = false;
            },
            redo: function() {
                self.undoInProgress = true;
                collection.add(m);
                self.figureModel.notifySelectionChange();
                self.undoInProgress = false;
            }
        });
    },

    listenToModel: function(model) {
        model.on("change", this.handleChange, this);
    },

    // Here we do most of the work, buiding Undo/Redo Edits when something changes
    handleChange: function(m) {

        var self = this;

        // Make sure we don't listen to changes coming from Undo/Redo
        if (self.undoInProgress) {
            return;     // Don't undo the undo!
        }

        // Ignore changes to certain attributes
        var ignore_attrs = ["selected", "id"];  // change in id when new Panel is saved

        var undo_attrs = {},
            redo_attrs = {},
            a;
        for (a in m.changed) {
            if (ignore_attrs.indexOf(a) < 0) {
                undo_attrs[a] = m.previous(a);
                redo_attrs[a] = m.get(a);
            }
        }

        // in case we only got 'ignorable' changes
        if (_.size(redo_attrs) == 0) {
            return;
        }

        // We add each change to undo_functions array, which may contain several
        // changes that happen at "the same time" (E.g. multi-drag)
        self.undo_functions.push(function(){
            m.save(undo_attrs);
        });
        self.redo_functions.push(function(){
            m.save(redo_attrs);
        });

        // this could maybe moved to FigureModel itself
        var set_selected = function(selected) {
            for (var i=0; i<selected.length; i++) {
                if (i == 0) {
                    self.figureModel.setSelected(selected[i], true);
                } else {
                    self.figureModel.addSelected(selected[i]);
                }
            }
        }

        // This is used to copy the undo/redo_functions lists
        // into undo / redo operations to go into our Edit below
        var createUndo = function(callList) {
            var undos = [];
            for (var u=0; u<callList.length; u++) {
                undos.push(callList[u]);
            }
            // get the currently selected panels
            var selected = self.figureModel.getSelected();
            return function() {
                self.undoInProgress = true;
                for (var u=0; u<undos.length; u++) {
                    undos[u]();
                }
                set_selected(selected);     // restore selection
                self.undoInProgress = false;
            }
        }

        // if we get multiple changes in rapid succession,
        // clear any existing timeout and re-create.
        if (typeof self.createEditTimeout != 'undefined') {
            clearTimeout(self.createEditTimeout);
        }
        // Only the last change will call createEditTimeout
        self.createEditTimeout = setTimeout(function() {
            self.postEdit( {
                name: "Undo...",
                undo: createUndo(self.undo_functions),
                redo: createUndo(self.redo_functions)
            });
            self.undo_functions = [];
            self.redo_functions = [];
        }, 10);
    }
});

var UndoView = Backbone.View.extend({
    
    el: $("#undoRedo"),
    
    events: {
      "click .undo": "undo",
      "click .redo": "redo"
    },

    // NB: requires backbone.mousetrap
    keyboardEvents: {
        'mod+z': 'undo',
        'mod+y': 'redo'
    },
    
    initialize: function() {
      this.model.on('change', this.render, this);
      this.undoEl = $(".undo", this.$el);
      this.redoEl = $(".redo", this.$el);
      this.render();
    },
    
    render: function() {
        if (this.model.canUndo()) {
            this.undoEl.removeAttr('disabled');
        } else {
            this.undoEl.attr('disabled', 'disabled').tooltip('hide');
        }
        if (this.model.canRedo()) {
            this.redoEl.removeAttr('disabled');
        } else {
            this.redoEl.attr('disabled', 'disabled').tooltip('hide');
        }
        return this;
    },
    
    undo: function() {
        this.model.undo();
        return false;
    },
    redo: function() {
        this.model.redo();
        return false;
    }
});