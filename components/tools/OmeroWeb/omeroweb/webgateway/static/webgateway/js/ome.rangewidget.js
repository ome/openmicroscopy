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
	min: cfg && !isNaN(parseFloat(cfg.min)) ? parseFloat(cfg.min) : null,
	max: cfg && !isNaN(parseFloat(cfg.max)) ? parseFloat(cfg.max) : null,
	lblStart: cfg && cfg.lblStart != undefined ? cfg.lblStart : 'Start',
	lblEnd: cfg && cfg.lblEnd != undefined ? cfg.lblEnd : 'End',
        template: cfg && cfg.template ? cfg.template : null
      };

      if (!(cfg && cfg.lblStart && this.cfg.min)) {
        if (cfg.lblStart != '') {
          this.cfg.lblStart += ' >= ' + this.cfg.min;
        } else {
          this.cfg.lblStart = this.cfg.min;
        }
      }
      if (!(cfg && cfg.lblEnd && this.cfg.max)) {
        if (cfg.lblEnd != '') {
          this.cfg.lblEnd += ' <= ' + this.cfg.max;
        } else {
          this.cfg.lblEnd = this.cfg.max;
        }
      }
      // try to format if values aren't integers
      try {
        if (this.cfg.lblStart != parseInt(this.cfg.lblStart)) {
          this.cfg.lblStart = this.cfg.lblStart.toFixed(3);
        }
        if (this.cfg.lblEnd != parseInt(this.cfg.lblEnd)) {
          this.cfg.lblEnd = this.cfg.lblEnd.toFixed(3);
        }
      }
      catch(err) {}

      var self = $(this);
      self.addClass('rangewidget');
      if (this.cfg.template == null) {
        self.append('<table></table>');
        self.find('table').append('<tr></tr>').find('tr:last')
  	.append('<td>'+this.cfg.lblStart+'</td>')
  	.append('<td>'+this.cfg.lblEnd+'</td>');
        self.find('table').append('<tr></tr>').find('tr:last')
  	.append('<td><input type="text" id="'+this.id+'-start" /></td>')
        .append('<td><input type="text" id="'+this.id+'-end" /></td>');
      } else {
        self.append(this.cfg.template
        .replace(/\$min/g,  '<span id="'+this.id+'-min" >'+this.cfg.lblStart+'</span>')
        .replace(/\$max/g, '<span id="'+this.id+'-max" >'+this.cfg.lblEnd+'</span>')
        .replace(/\$start/g, '<input type="text" id="'+this.id+'-start" />')
        .replace(/\$end/g, '<input type="text" id="'+this.id+'-end" />')
        );
      }
      self.find('input').SpinButton();

      //var boundary_check = function () {
      //  if (this.cfg.min && 
      //}

    });
  };
 })(jQuery);

