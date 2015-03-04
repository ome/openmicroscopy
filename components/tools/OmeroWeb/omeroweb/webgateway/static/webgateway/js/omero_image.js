


(function(){

    function show_change(obj, val, klass) {
        if (obj.value != val) {
            $(obj).addClass(klass);
        } else {
            $(obj).removeClass(klass);
        }
    }
    function hidePicker () {
        if ($(".picker").get(0) && $(".picker").get(0).hide_picker) {
            $(".picker").get(0).hide_picker();
        }
    }

    window.resetRDCW = function (viewport) {
        viewport.reset_channels();
        syncRDCW(viewport);
    };

    window.copyRdefs = function(viewport) {
        var rdefQry = viewport.getQuery();
        // also need pixelsType to know if we can manually paste to target
        var pr = viewport.loadedImg.pixel_range.join(":");
        rdefQry = rdefQry + "&pixel_range=" + pr;
        // Need imageId for 'apply to all'
        rdefQry = rdefQry + "&imageId=" + viewport.loadedImg.id;
        // save to session
        $.getJSON(viewport.viewport_server + "/copyImgRDef/?" + rdefQry);
    };

    window.pasteRdefs = function (viewport) {

        var doPaste = function(data) {
            var channels = data.c.split(",");       // c=1|3336:38283$FF0000,2|1649:17015$00FF00
            if (channels.length != viewport.getChannels().length ||
                data.pixel_range != viewport.loadedImg.pixel_range.join(":")) {
                    // images are not compatible - just 'fail silently'
                    return;
            }
            // only pick what we need
            var pasteData = {'c': data.c,
                'm': data.m};
            viewport.setQuery(pasteData);
            viewport.doload();        // loads image
            syncRDCW(viewport);       // update rdef table
            viewport.channelChange(); // triggers channel btn update

            // add to undo/redo queue and update undo/redo buttons.
            viewport.save_channels();
            updateUndoRedo(viewport);
        };

        // check session via /getImgRDef/ json call
        $.getJSON(viewport.viewport_server + "/getImgRDef/",
            function(data){
                if (data.rdef) {
                    doPaste(data.rdef);
                }
            }
        );
    };

    window.resetImageDefaults = function (viewport, obj, callback) {
        viewport.viewportmsg.html("Resetting...").show();
        $.getJSON(viewport.viewport_server + '/imgData/' + viewport.loadedImg.id + '/?getDefaults=true',
            function(data){
                viewport.loadedImg._load(data);

                // seems we need to do a lot of work to update UI
                viewport.doload();        // loads image
                syncRDCW(viewport);       // update rdef table
                viewport.channelChange(); // triggers channel btn update

                // add to undo/redo queue and update undo/redo buttons.
                viewport.save_channels();
                updateUndoRedo(viewport);

                if (callback) {
                    callback();
                }
            }
        );
    };

    window.setImageDefaults = function (viewport, obj, callback, skip_apply) {
        if (!skip_apply) applyRDCW(viewport);
        var old = $(obj).html();
        gs_modalJson(viewport.viewport_server + '/saveImgRDef/'+viewport.loadedImg.id+'/?'+viewport.getQuery(true),
            {},
            function(success, rv) {
                $(obj).html(old).prop('disabled', false);
                if (!(success && rv)) {
                    alert('Setting image defaults failed.');
                }
                if (callback) {
                    callback();
                }
                viewport.setSaved();
                updateUndoRedo(viewport);
            });
        return false;
    };

    window.zindex_automator = function(klass, basez, wspace) {
        if (!wspace) {
            wspace = $(klass);
        }
        var sorter = function (a,b) {
            return parseInt(a.css('z-index'), 10)-parseInt(b.css('z-index'), 10);
        };
        var tofront = function (e) {
            var self = this;
            var z = basez;
            var objs = [];
            $(klass).each(function () {
                if (this != self) {
                    objs.push($(this));
                }
            });
            $.each(objs.sort(sorter), function () {
                this.css('z-index', z);
                z++;
            });
            $(self).css('z-index', z);
        };
        $.each(wspace, function () {
            $(this).bind('opening', tofront);
            $(this).bind('mousedown', tofront);
        });
    };

    window.channelChange = function (ev, obj, idx, ch) {
        if (ch.active) {
            // similar buttons beside image and in rdef table
            $('#wblitz-ch'+idx).addClass('pressed');
            $('#rd-wblitz-ch'+idx).addClass('pressed');
        } else {
            $('#wblitz-ch'+idx).removeClass('pressed');
            $('#rd-wblitz-ch'+idx).removeClass('pressed');
        }
        //var t = $('#rd-wblitz-ch'+idx).get(0);
        //if (t != undefined) t.checked=ch.active;
        $('#wblitz-ch'+idx).css('background-color', "#"+OME.rgbToHex(ch.color)).attr('title', ch.label);
    };


    window.imageChange = function (viewport) {
        $('#wblitz-t-curr').html(viewport.getTPos());
        $('#wblitz-z-curr').html(viewport.getZPos());
        $('#wblitz-t-count').html(viewport.getTCount());
        $('#wblitz-z-count').html(viewport.getZCount());

        if (viewport.hasLinePlot() || $('#wblitz-lp-enable').prop('checked')) {
            viewport.refreshPlot();
        }
    };

    window.syncChannelsActive = function(viewport) {
        var channels = viewport.getChannels();
        for (i=0; i<channels.length; i++) {
            var $chbx = $('#rd-wblitz-ch'+i);
            if ($chbx.length > 0) {     // in case this is called before UI is built
                $chbx.get(0).checked = channels[i].active;
            }
        }
    };

    window.syncRDCW = function(viewport) {
        var cb;
        var channels = viewport.getChannels();
        for (i=0; i<channels.length; i++) {
            $('#rd-wblitz-ch'+i).css('background-color', toRGB(channels[i].color));
            $('#wblitz-ch'+i+'-cwslider .ui-slider-range').css('background-color', toRGB(channels[i].color));
            var w = channels[i].window;
            $('#wblitz-ch'+i+'-cwslider')
                .slider( "option", "min", Math.min(w.min, w.start) )   // extend range if needed
                .slider( "option", "max", Math.max(w.max, w.end) );
                $('#wblitz-ch'+i+'-color').attr('data-color', toRGB(channels[i].color));//$('#wblitz-ch'+i).css('background-color'));
                $('#wblitz-ch'+i+'-cw-start').val(channels[i].window.start).change();
                $('#wblitz-ch'+i+'-cw-end').val(channels[i].window.end).change();
        }
        hidePicker();

        updateUndoRedo(viewport);
        $('#rd-wblitz-rmodel').prop('checked', viewport.isGreyModel());
        syncChannelsActive(viewport);
    };

    window.updateUndoRedo = function(viewport) {
        // update disabled status of undo/redo buttons
        if (viewport.has_channels_undo()) {
            $('#rdef-undo-btn').removeAttr('disabled').removeClass("button-disabled");
        } else {
            $('#rdef-undo-btn').attr("disabled", "disabled").addClass("button-disabled");
        }
        if (viewport.has_channels_redo()) {
            $('#rdef-redo-btn').removeAttr('disabled').removeClass("button-disabled");
        } else {
            $('#rdef-redo-btn').attr("disabled", "disabled").addClass("button-disabled");
        }
        var canSaveRdef = viewport.loadedImg.perms.canAnnotate;
        if (viewport.getSaved() || !canSaveRdef) {
            $("#rdef-setdef-btn").attr("disabled", "disabled").addClass("button-disabled");
        } else {
            $("#rdef-setdef-btn").removeAttr('disabled').removeClass("button-disabled");
        }
    };

    var on_batchCopyRDefs = false;
    // TODO: try not to rely on global variables!
    window.applyRDCW = function(viewport, final) {
        if (on_batchCopyRDefs) {
            return batchCopyRDefs_action('ok');
        }
        for (var i=0; i<viewport.getCCount(); i++) {
            viewport.setChannelActive(i, $('#rd-wblitz-ch'+i).get(0).checked, true);
            viewport.setChannelColor(i, $('#wblitz-ch'+i+'-color').attr('data-color'), true);
            var noreload = ((i+1) < viewport.getCCount());    // prevent reload, except on the last loop
            viewport.setChannelWindow(i, $('#wblitz-ch'+i+'-cw-start').get(0).value, $('#wblitz-ch'+i+'-cw-end').get(0).value, noreload);
        }

        if (final) {
            viewport.forget_bookmark_channels();
            $('#rdef-postit').hide();
        }
        viewport.save_channels();
        syncRDCW(viewport);
    };


    window.setModel = function(viewport, model) {
        // this may turn channels on/off
        viewport.setModel(model);
        viewport.save_channels();
        syncRDCW(viewport);     // update undo/redo etc
    };

    /**
    * Gets called when an image is initially loaded.
    * This is the place to sync everything; rendering model, quality, channel buttons, etc.
    */
    window._refresh_cb = function (ev, viewport) {
        /* Sync inputs with initial values */

        $('#wblitz-rmodel').prop('checked', viewport.isGreyModel());
        $('#wblitz-invaxis').prop('checked', viewport.loadedImg.rdefs.invertAxis);

        var q = viewport.getQuality();
        if (q) {
            var qr = $('#wblitz-quality > option[value="' + q.toFixed(1) + '"]');
            if (qr.length) {
                qr.prop('selected',true);
            }
        }

        /* Prepare the channels box and the rendering definition for the channels */
        var box = $('#wblitz-channels-box');
        var channels = viewport.getChannels();
        box.empty();

        var doToggle = function(index) {
            return function() {
                viewport.toggleChannel(index);
                viewport.save_channels();
                updateUndoRedo(viewport);
            };
        };
        for (i=0; i<channels.length; i++) {
            $('<button id="wblitz-ch'+i+
                '"class="squared' + (channels[i].active?' pressed':'') +
                '"style="background-color: #' + OME.rgbToHex(channels[i].color) +
                '"title="' + channels[i].label +
                '">'+channels[i].label+'</button>')
            .appendTo(box)
            .bind('click', doToggle(i));
        }

        // disable 'split' view for single channel images.
        if (channels.length < 2) {
            $("#wblitz input[value='split']").prop('disabled', true);
        }

        /* Image details */
        var tmp = viewport.getMetadata();
        $('#wblitz-image-name').html(tmp.imageName);
        $('#wblitz-image-description-content').html(tmp.imageDescription.replace(/\n/g, '<br />'));
        $('#wblitz-image-author').html(tmp.imageAuthor);
        $('#wblitz-image-pub').html(tmp.projectName);
        $('#wblitz-image-pubid').html(tmp.projectId);
        $('#wblitz-image-timestamp').html(tmp.imageTimestamp);

        $("#bulk-annotations").hide();
        $("#bulk-annotations").next().hide();
        if (tmp.wellId) {
            // Load bulk annotations for plate
            var onAnnotations = function(result) {
                if (result.data && result.data.rows) {
                    var table = $("#bulk-annotations").show().next().show().children("table");
                    for (var col = 0; col < result.data.columns.length; col++) {
                        var label = result.data.columns[col];
                        var value = '';
                        for (var r = 0; r < result.data.rows.length; r++) {
                          value += result.data.rows[r][col] + '<br />';
                        }
                        var row = $('<tr><td class="title"></td><td></td></tr>');
                        row.addClass(col % 2 == 1 ? 'odd' : 'even');
                        $('td:first-child', row).html(label + ":&nbsp;");
                        $('td:last-child', row).html(value);
                        table.append(row);
                    }
                }
            };
            $.getJSON(PLATE_WELLS_URL_999.replace('999', tmp.wellId) +
                '?query=Well-' + tmp.wellId +
                '&callback=?',
                onAnnotations);
            $.getJSON(PLATE_LINKS_URL_999.replace('999', tmp.wellId) +
                '?query=Well-' + tmp.wellId +
                '&callback=?',
                onAnnotations);
        }

        // TODO: this used anywhere?
        // {% block xtra_metadata %}{% endblock %}

        /*$('#wblitz-shortname').attr('title', tmp.imageName).html(gs_text_trim(tmp.imageName, 15, true));*/

        tmp = viewport.getSizes();
        $('#wblitz-image-width').html(tmp.width);
        $('#wblitz-image-height').html(tmp.height);
        $('#wblitz-image-z-count').html(tmp.z);
        $('#wblitz-image-t-count').html(tmp.t);
        tmp = viewport.getPixelSizes();
        $('#wblitz-image-pixel-size-x').html(tmp.x===null?'-':(tmp.x.lengthformat()));
        $('#wblitz-image-pixel-size-y').html(tmp.y===null?'-':(tmp.y.lengthformat()));
        $('#wblitz-image-pixel-size-z').html(tmp.z===null?'-':(tmp.z.lengthformat()));

        if (tmp.x!==0) {
            $("#wblitz-scalebar").prop("disabled", false);
        }
        /* Fill in the Rendering Details box */

        $(".picker").unbind('prepared').unbind('showing').unbind('hiding');
        // $('#rdef-postit ul').not('ul:last-child').remove();

        var template = '' +
          '<tr class="$cls rdef-window">' +
          '<td><button id="rd-wblitz-ch$idx0" class="rd-wblitz-ch squared $class" style="background-color: #$col" ' +
            'title="$label">$l</button></td>' +
          '<td><table><tr id="wblitz-ch$idx0-cw" class="rangewidget"></tr></table></td>' +
          '<td><button id="wblitz-ch$idx0-color" class="picker squarred" title="Choose Color">&nbsp;</button></td>' +
          '</tr>';

        tmp = $('#rdef-postit table tr:first');
        tmp.siblings().remove();

        var start_cb = function (i) {
            return function (e) {
                var new_start = e.target.value,
                    $sl = $('#wblitz-ch'+i+'-cwslider'),
                    end = $sl.slider('values')[1],
                    min = $sl.slider( "option", "min" );
                $sl.slider('values', 0, Math.min(new_start, end));    // ensure start < end
                $sl.slider( "option", "min", Math.min(min, new_start) );   // extend range if needed
                show_change($('#wblitz-ch'+i+'-cw-start').get(0), channels[i].window.start, 'changed');
            };
        };
        var end_cb = function (i) {
            return function (e) {
                var new_end = e.target.value,
                    $sl = $('#wblitz-ch'+i+'-cwslider'),
                    start = $sl.slider('values')[0],
                    max = $sl.slider( "option", "max" );
                $sl.slider('values', 1, Math.max(new_end, start));    // ensure end > start
                $sl.slider( "option", "max", Math.max(max, new_end) );   // extend range if needed
                show_change($('#wblitz-ch'+i+'-cw-end').get(0), channels[i].window.end, 'changed');
            };
        };
        var slide_start_cb = function() {
            // Note starting values, so we can tell which handle/value changed
            // This is for floating point data where ui.values may not be what we set
            return function(event, ui) {
                $(this).data('channel_start', ui.values[0])
                    .data('channel_end', ui.values[1]);
            };
        };
        var slide_cb = function() {
            return function(event, ui) {
                // Only update the value that changed
                var s = $(this).data('channel_start');
                var e = $(this).data('channel_end');
                if (ui.values[0] !== s) {
                    $('#wblitz-ch'+$(event.target).data('channel-idx')+'-cw-start').val(ui.values[0]).change();
                } else if (ui.values[1] !== e) {
                    $('#wblitz-ch'+$(event.target).data('channel-idx')+'-cw-end').val(ui.values[1]).change();
                }
            };
        };
        var stop_cb = function() {
            return function(event, ui) {
                applyRDCW(viewport);
            };
        };
        var keyup_cb = function() {
            return function(event){
                if (event.keyCode === 13){
                    applyRDCW(viewport);
                }
            };
        };

        var init_ch_slider = function(i, channels) {
            var min = Math.min(channels[i].window.min, channels[i].window.start),  // range may extend outside min/max pixel
                max = Math.max(channels[i].window.max, channels[i].window.end),
                start = channels[i].window.start,
                end = channels[i].window.end,
                ptype = viewport.loadedImg.meta.pixelsType,
                step = 1;
            if (ptype == "float") {
                var STEPS = 100;
                step = (max - min) / STEPS;
            }

            $('#wblitz-ch'+i+'-cwslider').slider({
                range: true,
                step: step,
                min: min,
                max: max,
                values: [ start, end ],
                start: slide_start_cb(),
                slide: slide_cb(),
                stop: stop_cb(),
                }).data('channel-idx', i);
        };

        for (i=channels.length-1; i>=0; i--) {

            var btnClass = channels[i].active?'pressed':'';
            if (OME.isDark(channels[i].color)) {
                btnClass += " fontWhite";
            }

            var lbl = channels[i].label;
            if (lbl.length > 7) {
                lbl = lbl.slice(0, 5) + "...";
            }
            tmp.after(template
                .replace(/\$class/g, btnClass)
                .replace(/\$col/g, OME.rgbToHex(channels[i].color))
                .replace(/\$label/g, channels[i].label)
                .replace(/\$l/g, lbl)
                .replace(/\$idx0/g, i) // Channel Index, 0 based
                .replace(/\$idx1/g, i+1) // Channel Index, 1 based
                .replace(/\$cwl/g, channels[i].label) // Wavelength
                .replace(/\$cls/g, i/2!=parseInt(i/2, 10)?'even':'odd') // class
            );
            $('#wblitz-ch'+(i)+'-cw').rangewidget({
                min: channels[i].window.min,
                max: channels[i].window.max,
                template: '<td width="10%"><span class="min" title="min: $min">$start</span></td><td><div class="rangeslider" id="wblitz-ch'+i+'-cwslider"></div></td> <td width="10%"><span class="max" title="max: $max">$end</span></td>',
                lblStart: '',
                lblEnd: ''});
            init_ch_slider(i, channels);
            $('#wblitz-ch'+i+'-cw-start').val(channels[i].window.start).unbind('change').bind('change', start_cb(i));
            $('#wblitz-ch'+i+'-cw-start').keyup(keyup_cb());
            $('#wblitz-ch'+i+'-cw-end').val(channels[i].window.end).unbind('change').bind('change', end_cb(i));
            $('#wblitz-ch'+i+'-cw-end').keyup(keyup_cb());
        }

        // bind clicking on channel checkboxes
        $(".rd-wblitz-ch").each(function(i){
            $(this).bind('click', doToggle(i));
        });


        /* Prepare color picker buttons */
        $(".picker")
            .colorbtn()
            .bind('showing', function () {
                var t = $(this).parents('.postit'),
                    offset;
                if (t.length) {
                  offset = t.offset();
                  offset.left += t.width();
                } else {
                  offset = {'top':'300px', 'left': window.innerWidth-250+'px'};
                }
                $('#cbpicker-box').css(offset);
                $('.picker-selected').html('&nbsp;');
                $(this).parent().siblings('.picker-selected').html('&gt;');
            })
            .bind('hiding', function () {$(this).parent().siblings('.picker-selected').html('&nbsp;');})
            .bind('prepared', function () {
                zindex_automator('.postit', 210, $('#cbpicker-box'));
            })
            .bind('changed', function () {
                applyRDCW(viewport);
            });

        // Don't see any obvious bugs when these are removed.
        // They are both bound to appropriate triggers on viewport.
        //projectionChange(null,null, true);
        //modelChange();

        syncRDCW(viewport);

        $('#wblitz-workarea > .box > div.row').show();
    };

}());
