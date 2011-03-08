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
	$.fn.editable = function(form_url, save_url, options ) {
		// Defaults
		var opt = {
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
                        $( self ).parent().parent().append($(html)).fadeIn();
                        
                        var input_save = $('<input id="save-'+self.id+'" type="submit" value="Save" />');
                        input_save.appendTo($( self ).parent().parent().find('div'));            
                        input_save.bind( "click", function( e ) {
                			return _saveEdit( self, orig_option_value );
                		} ); // save click

                        var input_cancel = $('<input id="cancel-'+self.id+'" type="button" value="Cancel" />');
                        input_cancel.appendTo($( self ).parent().parent().find('div'));
                        input_cancel.bind( "click", function( e ) {
                            _cancelEdit( self );
                        } );                        
                    });
                }
            });
        
		} // function _editMode
		
		var _cancelEdit = function( self ) {
			$( self ).parent().parent().find('div').remove( ).fadeOut( "fast" );

			$( self ).bind( opt.edit_event, function( e ) {
				_editMode( self );
			} );

			$( self ).parent().show().fadeIn( "fast" );
        
		};
		
		var _saveEdit = function( self, orig_option_value ) {
			alert('aaaaa')
		}; // _saveEdit


	}; // inplaceEdit
})( jQuery );
