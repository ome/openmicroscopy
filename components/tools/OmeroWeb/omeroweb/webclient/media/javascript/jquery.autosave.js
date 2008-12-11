/**
 * Autosave jQuery plugin
 *
 * Copyright (c) 2008 Rik Lomas (rikrikrik.com)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 */
;(function($) {
	$.fn.autosave = function(options) {
		var opts = $.extend({}, $.fn.autosave.options, options);
		var ev = false;
		var doSave = false;
		var ti = 0;
		var ci = 0;
		var ri = 0;
	
		function setEvents ()
		{
			$( $.fn.autosave.options.saving ).hide();
			
			$( $.fn.autosave.options.autosave ).click(function () {
				$.fn.autosave.go();
				return false;
			});
			
			$( $.fn.autosave.options.restore ).click(function () {
				$.fn.autosave.restore();
				return false;
			});
			
			$( $.fn.autosave.options.removeCookies ).click(function () {
				$.fn.autosave.removeAllCookies();
				return false;
			});
		
			$(window).unload(function () {
				$.fn.autosave.go();
				return true;
			});
		
			setInterval(function () {
				if (doSave) {
					$.fn.autosave.go();	
					doSave = false;
				} 
			}, $.fn.autosave.options.interval);
			ev = true;
		}
		
		return this.filter(':text, :radio, :checkbox, select, textarea').each(function () {
			if ($(this).is(':text, textarea')) {
				$.fn.autosave.values.text[ti] = this;
				$(this).keyup(function () {
					doSave = true;
				});
				ti++;
			} else if ($(this).is('select')) {
				$.fn.autosave.values.text[ti] = this;
				$(this).change(function () {
					doSave = true;
				});
				ti++;
			} else if ($(this).is(':checkbox')) {
				$.fn.autosave.values.check[ci] = this;
				$(this).click(function () {
					doSave = true;
				});
				ci++;
			} else {
				$.fn.autosave.values.radio[ri] = this;
				$(this).click(function () {
					doSave = true;
				});
				ri++;
			}
		
			if (!ev) { setEvents(); }
		});
	};

	$.fn.autosave.values = {
		'text': {},
		'check': {},
		'radio': {}
	};

	$.fn.autosave.options = {
		'autosave': '.autosave',
		'restore': '.autosave_restore',
		'removeCookies': '.autosave_removecookies',
		'saving': '.autosave_saving',
		'interval': 10000,
		'unique': '',
		'onBeforeSave': function () { },
		'onAfterSave': function () { },
		'onBeforeRestore': function () { },
		'onAfterRestore': function () { },
		'cookieCharMaxSize': 2000,
		'cookieExpiryLength': 1
	};

	$.fn.autosave.go = function () {
		
		$.fn.autosave.options.onBeforeSave();
		
		var m = $.fn.autosave.values;
		var u = $.fn.autosave.options.unique;
		
		function saveCookie (i, j, content)
		{
			$.cookie('autosave_'+u+i+'_'+j, content, { expires: $.fn.autosave.options.cookieExpiryLength });
		}
	
		function removeBiggerCookies (i)
		{
			var j = 1;
			while ($.cookie('autosave_'+u+i+'_'+j) !== null && j < 20)
			{
				$.cookie('autosave_'+u+i+'_'+j, null);
			}
		}
	
		for (i in m.text)
		{
			var content;
			var j = 0;
		
			content = $(m.text[i]).val();
			size = content.length;
		
			if (size < $.fn.autosave.options.cookieCharMaxSize)
			{
				saveCookie(i, 0, content);
			}
			else
			{
				removeBiggerCookies(i);
				for (var k = 0; k < size; k += $.fn.autosave.options.cookieCharMaxSize)
				{
					saveCookie(i, j, content.substr(k, $.fn.autosave.options.cookieCharMaxSize));
					j += 1;
				}
			}
		}
	
		var cookiecheck = '';
		for (i in m.check)
		{
			var content = $(m.check[i]).attr('checked') ? '1' : '0';
			cookiecheck += content + ',';
		}
		$.cookie('autosave_'+u+'_check', cookiecheck);
	
		var cookieradio = '';
		for (i in m.radio)
		{
			if($(m.radio[i]).is(':checked'))
			{
				cookieradio += i + ',';
			}
		}
		$.cookie('autosave_'+u+'_radio', cookieradio);
	
		$.fn.autosave.saving(); 
		
		$.fn.autosave.options.onAfterSave();
		
	};

	$.fn.autosave.restore = function () {
		
		$.fn.autosave.options.onBeforeRestore();
		
		var m = $.fn.autosave.values;
		var u = $.fn.autosave.options.unique;
	
		for (i in m.text)
		{
			var j = 0;
			var restored = '';
			while ($.cookie('autosave_'+u+i+'_'+j) !== null && j < 20)
			{
				restored += $.cookie('autosave_'+u+i+'_'+j);
				j += 1;
			}
			$(m.text[i]).val(restored);
		}
	
		var cookiecheck = $.cookie('autosave_'+u+'_check').split(',');
		if (cookiecheck !== null)
		{
			cookiecheck.pop(); // Get rid of last element
			for (i in m.check)
			{
				var chek = (cookiecheck[i] == '1') ? 'checked' : '';
				$(m.check[i]).attr('checked', chek);
			}	
		}
			
		var cookieradio = $.cookie('autosave_'+u+'_radio').split(',');
		if (cookieradio !== null)
		{
			cookieradio.pop(); // Get rid of last element
			for (i in cookieradio)
			{
				$(m.radio[cookieradio[i]]).attr('checked', 'checked');
			}
		}
				
		$.fn.autosave.options.onAfterRestore();
		
	};

	$.fn.autosave.removeAllCookies = function () {
		var u = $.fn.autosave.options.unique;
	
		for (var i = 0; i < 200; i++)
		{
			var j = 0;
			while ($.cookie('autosave_'+u+i+'_'+j) !== null && j < 20)
			{
				$.cookie('autosave_'+u+i+'_'+j, null);
			}
		}
	
		$.cookie('autosave_'+u+'_check', null);
		$.cookie('autosave_'+u+'_radio', null);
	};

	$.fn.autosave.saving = function () {
		$( $.fn.autosave.options.saving ).show().fadeTo(1000, 1).fadeOut(500);
	};
	
})(jQuery);
