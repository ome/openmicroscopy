
$(function(){


    var figureModel = new FigureModel( {'canvas_width': 2000, 'canvas_height': 2000,
            'paper_width': 612, 'paper_height': 792});


    // Override 'Backbone.sync'...
    Backbone.sync = function(method, model, options, error) {
        figureModel.set("unsaved", true);
    };

    // UI Model (not saved - just used to coordinate UI status)
    // var uiState = new UiState( {model:figureModel} )

    var view = new FigureView( {model: figureModel});   // uiState: uiState
    var svgView = new SvgView( {model: figureModel});
    new RightPanelView({model: figureModel});

    $("#zoom_slider").slider({
        max: 400,
        min: 40,
        value: 75,
        slide: function(event, ui) {
            figureModel.set('curr_zoom', ui.value);
        }
    });

    // Do this after other setup above
    figureModel.set('curr_zoom', 75);


    // Undo Model and View
    var undoManager = new UndoManager({'figureModel':figureModel}),
    undoView = new UndoView({model:undoManager});
    // Finally, start listening for changes to panels
    undoManager.listenToCollection(figureModel.panels);


    var FigureRouter = Backbone.Router.extend({

        routes: {
            "": "index",
            "figure/:id": "loadFigure"
        },

        clearFigure: function() {

            // Arrive at 'home' page, either starting here OR we hit 'new' figure...
            // ...so start by clearing any existing Figure (save first if needed)
            var self = this;
            if (figureModel.get("unsaved") && confirm("Save current Figure to OMERO?")) {
                figureModel.save_to_OMERO({}, function() {
                    figureModel.unset('fileId');
                });
            } else {
                figureModel.unset('fileId');
            }
            figureModel.delete_all();
            figureModel.unset("figureName");
            figureModel.trigger('reset_undo_redo');

            return false;
        },

        index: function() {
            this.clearFigure();
            figureModel.set('unsaved', false);
        },

        loadFigure: function(id) {
            this.clearFigure();

            var fileId = parseInt(id, 10);
            figureModel.load_from_OMERO(fileId);
        }
    });

    app = new FigureRouter();
    Backbone.history.start();

});
