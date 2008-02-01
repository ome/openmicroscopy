/**
 * Range widget plugin for JQuery.
 *
 * Todo: better documentation and example usage.
 *
 * Depends on jquery, jQuerySpinBtn
 *
 * Author: C. Neves <carlos@glencoesoftware.com>
 *
 * Copyright (c) 2008 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 */

(function ($) {
  $.fn.rangewidget = function (cfg) {
    return this.each(function () {
      this.cfg = {
	min: cfg && !isNaN(parseInt(cfg.min)) ? parseInt(cfg.min) : null,
	max: cfg && !isNaN(parseInt(cfg.max)) ? parseInt(cfg.max) : null,
	lblStart: cfg && cfg.lblStart ? cfg.lblStart : 'Start',
	lblEnd: cfg && cfg.lblEnd ? cfg.lblEnd : 'End'
      };

      if (!(cfg && cfg.lblStart && this.cfg.min)) {
	this.cfg.lblStart += ' >= ' + this.cfg.min;
      }
      if (!(cfg && cfg.lblEnd && this.cfg.max)) {
	this.cfg.lblEnd += ' <= ' + this.cfg.max;
      }
      var self = $(this);
      self.addClass('rangewidget');
      self.append('<table></table>');
      self.find('table').append('<tr></tr>').find('tr:last')
	.append('<td>'+this.cfg.lblStart+'</td>')
	.append('<td>'+this.cfg.lblEnd+'</td>');
      self.find('table').append('<tr></tr>').find('tr:last')
	.append('<td><input type="text" id="'+this.id+'-start" /></td>')
	.append('<td><input type="text" id="'+this.id+'-end" /></td>');
      self.find('table').append('<tr></tr>').find('tr:last')
	.append('<td colspan="2"><div>Slider here</div></td>');

      self.find('input').SpinButton();

      //var boundary_check = function () {
      //  if (this.cfg.min && 
      //}

    });
  };
 })(jQuery);

