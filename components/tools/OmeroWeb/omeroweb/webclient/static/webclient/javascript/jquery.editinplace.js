/*
 Copyright (c) 2008 Joseph Scott, http://josephscott.org/
 
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:
 
 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
 
// version: 0.1.2

(function( $ ) {
    $.fn.editable = function(form_url, save_url, field_id, options ) {
        // Defaults
        var opt = {
            field_id    : field_id,
            save_url    : save_url,
            form_url    : form_url,
            post_save   : function(input) { return input; },
            edit_event  : "click" 
        }; // defaults

        if( options ) {
            $.extend( opt, options );
        }
        
        this.each( function( ) {
            var self = this;
            $(this).bind( opt.edit_event, function( e ) {
                _editMode(this);
            } );
        } ); // this.each

        // Private functions
        var _editMode = function( self ) {
            $( self ).unbind( opt.edit_event );

            $.ajax({
                type: "GET",
                url: form_url,
                success: function(html) {                
                    $( self ).parent().fadeOut('fast', function(){                    
                        
                        $( self ).parent().hide();
                        $("#"+field_id).append($(html)).fadeIn();
                        $("#"+field_id).find("form").attr('id', "form-"+field_id)
                                                
                        $('<input id="save-'+field_id+'" type="submit" value="Save" />')
                        /*.bind( "click", function( e ) {
                            return _saveEdit( self );
                        } )*/
                        .appendTo($("#form-"+field_id));            
                        
                        $('<input id="cancel-'+field_id+'" type="button" value="Cancel" />')
                        .bind( "click", function( e ) {
                            _cancelEdit( self );
                        } )
                        .appendTo($("#form-"+field_id));
                        
                        $('#form-'+field_id).ajaxForm({ 
                            url: save_url,
                            dataType:  'json', 
                            success:   function (data) { 
                                if (data) {
                                    if (eval(data.bad)) {
                                        errors = eval(data.errs);
                                        $.each(errors,function(fieldname,errmsg) {
                                            $("#form-"+field_id + " input#id_" + fieldname).parent().find("p.error").html( errmsg ); //I want the error above the <p> holding the field
                                        });
                                        $('#save-'+field_id).prop("disabled", false);  // re-enable for re-submit
                                    } else {
                                        // If we're editing name...
                                        if (field_id.indexOf("name") > -1) {
                                            var $this = $("#id_name");
                                            if ($this.attr('name')!=null && $this.attr('name')!=""){
                                                var new_name = $this.attr('value');
                                                $("#"+field_id+"-"+$this.attr('name')).text(new_name);
                                                if (data.o_type != "well") {
                                                    // Check we have a jsTree (not in Search or History page etc)
                                                    if ($.jstree && $("#dataTree").jstree) {
                                                        // Update name in thumbnails...
                                                        var objId = field_id.replace("name","_icon"); // E.g. imagename-123 -> image_icon-123
                                                        $("#"+objId+" div.desc").text(new_name);
                                                        $("#"+objId+" div.image img").attr('title', new_name);  // tooltip
                                                        // And in jsTree
                                                        var node = $.jstree._focused().get_selected();
                                                        if (new_name.length > 30) {
                                                            new_name = '...' + new_name.substring(new_name.length-30, new_name.length);
                                                        }
                                                        $("#dataTree").jstree('set_text', $.jstree._focused().get_selected(), new_name);
                                                        // For images, set data and truncate if needed
                                                        node.children('a').attr('data-name', new_name);
                                                        OME.truncateNames();
                                                    } else {
                                                        // OR we may be in the search page: Update image name in table...
                                                        var objId = field_id.replace("name","");    // E.g. imagename-123
                                                        $("#"+objId+" td.desc a").text(new_name);
                                                        $("#"+objId+" td.image img").attr('title', new_name);
                                                    }
                                                }
                                            }
                                        }
                                        $("#form-"+field_id).find('textarea').each( function( ) {
                                            if ($(this).attr('name')!=null && $(this).attr('name')!=""){
                                                var processed_val = opt.post_save($('<div/>').text($(this).val()).html());
                                                if (processed_val.length === 0) {
                                                    processed_val = "Add Description";     // Reset to placeholder text
                                                }
                                                $("#"+field_id+"-"+$(this).attr('name')).html(processed_val.replace(/\n/g, "<br />"));
                                            }
                                        }); // this.each
                                        
                                        $("#form-"+field_id).remove( ).fadeOut( "fast" );
                                        $( self ).bind( opt.edit_event, function( e ) {
                                            _editMode( self );
                                        } );
                                        $( self ).parent().show().fadeIn( "fast" );
                                    }
                                } else {
                                    alert("Ajax error : no data received.");
                                }
                            },
                            beforeSubmit: function () { 
                                $('#save-'+field_id).prop("disabled", true); //Disable the submit button - can't click twice
                                $("#form-"+field_id).find("ul").each(function () {
                                    $(this).remove();
                                }); // this.each
                                return true;
                            },
                            error: function(responce) {
                                $( self ).bind( opt.edit_event, function( e ) {
                                    _editMode( self );
                                } );
                            }
                        });
                        
                    });
                },
                error: function(responce) {
                    $( self ).bind( opt.edit_event, function( e ) {
                        _editMode( self );
                    } );
                }
            });
        
        } // function _editMode
        
        var _cancelEdit = function( self ) {
            $("#form-"+field_id).remove().fadeOut( "fast" );
            $( self ).bind( opt.edit_event, function( e ) {
                _editMode( self );
            } );
            $( self ).parent().show().fadeIn( "fast" );        
        };
        
    }; // inplaceEdit
})( jQuery );
