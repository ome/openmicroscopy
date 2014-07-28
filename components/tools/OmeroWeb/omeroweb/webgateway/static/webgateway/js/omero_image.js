


(function(){

    function show_change(obj, val, klass) {
        if (obj.value != val) {
            $(obj).addClass(klass);
        } else {
            $(obj).removeClass(klass);
        }
    }
    function hidePicker () {
        $(".picker").get(0) && $(".picker").get(0).hide_picker && $(".picker").get(0).hide_picker();
        /*$('.picker-selected').html('&nbsp;');*/
    }

    window.syncRDCW = function(viewport) {
        var cb;
        var channels = viewport.getChannels();
        for (i=0; i<channels.length; i++) {
            $('#rd-wblitz-ch'+i).get(0).checked = channels[i].active;
            $('#wblitz-ch'+i+'-cwslider .ui-slider-range').css('background-color', toRGB(channels[i].color));
            var w = channels[i].window;
            $('#wblitz-ch'+i+'-cwslider')
                .slider( "option", "min", Math.min(w.min, w.start) )   // extend range if needed
                .slider( "option", "max", Math.max(w.max, w.end) );
                $('#wblitz-ch'+i+'-color').css('background-color', toRGB(channels[i].color));//$('#wblitz-ch'+i).css('background-color'));
                $('#wblitz-ch'+i+'-cw-start').val(channels[i].window.start).change();
                $('#wblitz-ch'+i+'-cw-end').val(channels[i].window.end).change();
        }
        hidePicker();
        $('#rdef-undo-btn').attr('disabled', viewport.has_channels_undo()?'':'true');
        $('#rdef-redo-btn').attr('disabled', viewport.has_channels_redo()?'':'true');
        //$('#rdef-default-btn').attr('disabled',viewport.has_channels_undo()?'':'true');
        $('#rd-wblitz-rmodel').attr('checked', !viewport.isGreyModel());
    }

    var on_batchCopyRDefs = false;
    // TODO: try not to rely on global variables!
    window.applyRDCW = function(viewport, final) {
        if (on_batchCopyRDefs) {
            return batchCopyRDefs_action('ok');
        }
        viewport.setModel($('#rd-wblitz-rmodel').get(0).checked?'c':'g');
        for (var i=0; i<viewport.getCCount(); i++) {
            viewport.setChannelActive(i, $('#rd-wblitz-ch'+i).get(0).checked, true);
            viewport.setChannelColor(i, $('#wblitz-ch'+i+'-color').css('background-color'), true);
            var noreload = ((i+1) < viewport.getCCount());    // prevent reload, except on the last loop
            viewport.setChannelWindow(i, $('#wblitz-ch'+i+'-cw-start').get(0).value, $('#wblitz-ch'+i+'-cw-end').get(0).value, noreload);
        }

        if (final) {
            viewport.forget_bookmark_channels();
            $('#rdef-postit').hide();
        }
        viewport.save_channels();
        syncRDCW(viewport);
    }

    /**
    * Gets called when an image is initially loaded.
    * This is the place to sync everything; rendering model, quality, channel buttons, etc.
    */
    window._refresh_cb = function (ev, viewport) {
        /* Sync inputs with initial values */

        $('#wblitz-rmodel').attr('checked', !viewport.isGreyModel());
        $('#wblitz-invaxis').attr('checked', viewport.loadedImg.rdefs.invertAxis);
        //$('#rd-wblitz-rmodel').attr('checked', !viewport.isGreyModel());

        var q = viewport.getQuality();
        if (q) {
            var qr = $('#wblitz-quality > [value="+q.toFixed(1)+"]');
            if (qr.length) {
                qr.attr('selected','selected');
            }
        }

        /* Prepare the channels box and the rendering definition for the channels */
        var box = $('#wblitz-channels-box');
        var channels = viewport.getChannels();
        box.empty();

        var doToggle = function(index) {
            return function() {
                viewport.toggleChannel(index);
            }
        }
        for (i=0; i<channels.length; i++) {
            $('<button id="wblitz-ch'+i+'"\
                class="squared' + (channels[i].active?' pressed':'') + '"\
                style="background-color: #'+channels[i].color+'"\
                title="'+channels[i].label+'"\
                >'+channels[i].label+'</button>')
            .appendTo(box)
            .bind('click', doToggle(i));
        }

        // disable 'split' view for single channel images.
        if (channels.length < 2) {
            $("input[value='split']").attr('disabled', 'disabled');
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
                    for (var col in result.data.columns) {
                        var label = result.data.columns[col];
                        var value = '';
                        for (var row in result.data.rows) {
                          value += result.data.rows[row][col] + '<br />';
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
        $('#wblitz-image-pixel-size-x').html(tmp.x==0?'-':(tmp.x.lengthformat()));
        $('#wblitz-image-pixel-size-y').html(tmp.y==0?'-':(tmp.y.lengthformat()));
        $('#wblitz-image-pixel-size-z').html(tmp.z==0?'-':(tmp.z.lengthformat()));

        /* Fill in the Rendering Details box */

        $(".picker").unbind('prepared').unbind('showing').unbind('hiding');
        $('#rdef-postit ul').not('ul:last-child').remove();

        var template = '<tr class="$cls">'
        + '<td><input id="rd-wblitz-ch$idx0" type="checkbox" onchange="rdChanSelHelper(this)" $act></td>'
        + '<td>$cwl</td>'
        + '<td><button id="wblitz-ch$idx0-color" class="picker squarred">&nbsp;</button></td>'
        + '<td class="picker-selected">&nbsp;</td></tr>'
        + '<tr class="$cls rdef-window">'
        + '<td colspan="5"><div id="wblitz-ch$idx0-cw" class="rangewidget"></div></td>'
        +'</tr>'
        + '<tr class="$cls rdef-window">'
        + '<td colspan="5"><div class="rangeslider" id="wblitz-ch$idx0-cwslider"></div></td>'
        + '</tr>';

        tmp = $('#rdef-postit table tr:first');
        tmp.siblings().remove();
        for (i=channels.length-1; i>=0; i--) {
            tmp.after(template
                .replace(/\$act/g, channels[i].active?'checked':'')
                .replace(/\$idx0/g, i) // Channel Index, 0 based
                .replace(/\$idx1/g, i+1) // Channel Index, 1 based
                .replace(/\$cwl/g, channels[i].label) // Wavelength
                .replace(/\$cls/g, i/2!=parseInt(i/2)?'even':'odd') // class
            );
            $('#wblitz-ch'+(i)+'-cw').rangewidget({
                min: channels[i].window.min,
                max: channels[i].window.max,
                template: '<span class="min">min: $min</span> $start - $end <span class="max">max: $max</span>',
                lblStart: '',
                lblEnd: ''});
            $('#wblitz-ch'+i+'-cwslider').slider({
                range: true,
                min: Math.min(channels[i].window.min, channels[i].window.start+1),  // range may extend outside min/max pixel
                max: Math.max(channels[i].window.max, channels[i].window.end-1),
                values: [ channels[i].window.start+1, channels[i].window.end-1 ],
                slide: function(event, ui) {
                    $('#wblitz-ch'+$(event.target).data('channel-idx')+'-cw-start').val(ui.values[0]).change();
                    $('#wblitz-ch'+$(event.target).data('channel-idx')+'-cw-end').val(ui.values[1]).change();
                }
                }).data('channel-idx', i);
            cb = function (i) {
                return function (e) {
                    var new_start = e.target.value,
                    $sl = $('#wblitz-ch'+i+'-cwslider'),
                    end = $sl.slider('values')[1]
                    min = $sl.slider( "option", "min" );
                    $sl.slider('values', 0, Math.min(new_start, end));    // ensure start < end
                    $sl.slider( "option", "min", Math.min(min, new_start) );   // extend range if needed
                    show_change($('#wblitz-ch'+i+'-cw-start').get(0), channels[i].window.start, 'changed');
                };
            };
            $('#wblitz-ch'+i+'-cw-start').val(channels[i].window.start).unbind('change').bind('change', cb(i));
            cb = function (i) {
                return function (e) {
                    var new_end = e.target.value,
                    $sl = $('#wblitz-ch'+i+'-cwslider'),
                    start = $sl.slider('values')[0]
                    max = $sl.slider( "option", "max" );
                    $sl.slider('values', 1, Math.max(new_end, start));    // ensure end > start
                    $sl.slider( "option", "max", Math.max(max, new_end) );   // extend range if needed
                    show_change($('#wblitz-ch'+i+'-cw-end').get(0), channels[i].window.end, 'changed');
                };
            };
            $('#wblitz-ch'+i+'-cw-end').val(channels[i].window.end).unbind('change').bind('change', cb(i));
        };


        /* Prepare color picker buttons */
        $(".picker")
            .colorbtn()
            .bind('showing', function () {
                var t = $(this).parents('.postit');
                var offset = t.offset();
                offset.left += t.width();
                $('#cbpicker-box').css(offset);
                $('.picker-selected').html('&nbsp;');
                $(this).parent().siblings('.picker-selected').html('&gt;');
            })
            .bind('hiding', function () {$(this).parent().siblings('.picker-selected').html('&nbsp;')})
            .bind('prepared', function () {
                zindex_automator('.postit', 210, $('#cbpicker-box'));
            })
            .bind('changed', function () {
                $("#rd-wblitz-rmodel").attr('checked', true);     // if we're updating color - show color (not greyscale)
                $(this).parents('tr:first').next().find('.ui-slider-range').css('background-color', $(this).css('background-color'));
            });

        //projectionChange(null,null, true);
        //viewport.trigger('projectionChange');

        //modelChange();
        //viewport.trigger('modelChange');
        syncRDCW(viewport);

        $('#wblitz-workarea > .box > div.row').show();
    };

}());