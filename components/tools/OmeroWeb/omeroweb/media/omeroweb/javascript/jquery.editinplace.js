(function( $ ) {
	$.fn.editable = function(form_url, save_url, field_id, options ) {
		// Defaults
		var opt = {
		    field_id            : field_id,
			save_url			: save_url,
            form_url            : form_url,
            edit_event			: "click"		
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
                            		            $("#"+field_id+"-"+$(this).attr('name')).text($(this).attr('value'));
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
