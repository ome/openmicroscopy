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
                                        $.each(errors,function(fieldname,errmsg)
                                        {
                                            $("#form-"+field_id + " input#id_" + fieldname).parent().find("p.error").html( errmsg ); //I want the error above the <p> holding the field
                                            });
                                        $('#save-'+field_id).attr("disabled","");
                                    } else {
                                        $("#form-"+field_id).find('input').each( function( ) {
                                            if ($(this).attr('name')!=null && $(this).attr('name')!=""){
                                                var new_name = $(this).attr('value');
                                                $("#"+field_id+"-"+$(this).attr('name')).text(new_name);
                                                // update tree object TODO: move it out of scope
                                                if (new_name.length > 30) {
                                                    new_name = '...' + new_name.substring(new_name.length-30, new_name.length)
                                                }
                                                window.parent.$("#dataTree").jstree('set_text', window.parent.$.jstree._focused().get_selected(), new_name);
                                            }
                                        }); // this.each
                                        $("#form-"+field_id).find('textarea').each( function( ) {
                                            if ($(this).attr('name')!=null && $(this).attr('name')!=""){
                                                $("#"+field_id+"-"+$(this).attr('name')).text($(this).val());
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
                                $('#save-'+field_id).attr("disabled","disabled"); //Disable the submit button - can't click twice
                                $("#form-"+field_id).find("ul").each(function () {
                                    $(this).remove();
                                }); // this.each
                                return true;
                            },
                            error: function(responce) {
                                $( self ).bind( opt.edit_event, function( e ) {
                                    _editMode( self );
                                } );
                                alert(responce.responceText);
                                
                            }
                        });
                        
                    });
                },
                error: function(responce) {
                    $( self ).bind( opt.edit_event, function( e ) {
                        _editMode( self );
                    } );
                    alert("Internal server error. Cannot edit.");
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
