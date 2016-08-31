
$(function(){

    var figureModel = new FigureModel( {'canvas_width': 2000, 'canvas_height': 2000,
            'paper_width': 612, 'paper_height': 792});

    // var figureFiles = new FileList();
    // figureFiles.fetch();

    // Backbone.unsaveSync = function(method, model, options, error) {
    //     figureModel.set("unsaved", true);
    // };

    // Override 'Backbone.sync'...
    Backbone.ajaxSync = Backbone.sync;

    Backbone.getSyncMethod = function(model) {
        if(model.syncOverride || (model.collection && model.collection.syncOverride))
        {
            return function(method, model, options, error) {
                figureModel.set("unsaved", true);
            };
        }
        return Backbone.ajaxSync;
    };

    // Override 'Backbone.sync' to default to localSync,
    // the original 'Backbone.sync' is still available in 'Backbone.ajaxSync'
    Backbone.sync = function(method, model, options, error) {
        return Backbone.getSyncMethod(model).apply(this, [method, model, options, error]);
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


    // TODO - move this into FigureView
    // Heavy lifting of PDF generation handled by OMERO.script...
    $("#create_figure_pdf").click(function(){

        // Status is indicated by showing / hiding 3 buttons
        var $create_figure_pdf = $(this),
            $pdf_inprogress = $("#pdf_inprogress"),
            $pdf_download = $("#pdf_download");
        $create_figure_pdf.hide();
        $pdf_download.hide();
        $pdf_inprogress.show();

        // Turn panels into json
        var p_json = [];
        figureModel.panels.each(function(m) {
            p_json.push(m.toJSON());
        });

        var url = MAKE_WEBFIGURE_URL,
            data = {
            pageWidth: figureModel.get('paper_width'),
            pageHeight: figureModel.get('paper_height'),
            panelsJSON: JSON.stringify(p_json)
        }

        // Start the Figure_To_Pdf.py script
        $.post( url, data)
            .done(function( data ) {

                // {"status": "in progress", "jobId": "ProcessCallback/64be7a9e-2abb-4a48-9c5e-6d0938e1a3e2 -t:tcp -h 192.168.1.64 -p 64592"}
                var jobId = data.jobId;

                // Now we keep polling for script completion, every second...

                var i = setInterval(function (){

                    $.getJSON(ACTIVITIES_JSON_URL, function(act_data) {

                            var pdf_job = act_data[jobId];

                            // We're waiting for this flag...
                            if (pdf_job.status == "finished") {
                                clearInterval(i);

                                // Update UI
                                $create_figure_pdf.show();
                                $pdf_inprogress.hide();
                                var fa_id = pdf_job.results.File_Annotation.id,
                                    fa_download = WEBINDEX_URL + "annotation/" + fa_id + "/";
                                $pdf_download.attr('href', fa_download).show();
                            }

                            if (act_data.inprogress == 0) {
                                clearInterval(i);
                            }

                        }).error(function() {
                            clearInterval(i);
                        });

                }, 1000);
            });
        return false;
    });


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
            $('#addImagesModal').modal();
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
