/****
 Last Modified: 13/05/07 00:25:39
   Copyright Amir Salihefendic 2006
 LICENSE
    GPL (see gpl.txt for more information)
 AUTHOR
    4mir Salihefendic (http://amix.dk) - amix@amix.dk
 VERSION
    4.0
****/

//////
// Support dependent text boxes
/////
function GoogieSpellMultiple(img_dir, server_url) {
  this.img_dir = img_dir;
  this.server_url = server_url;
  this.dependent = false;
  this.g_elms = [];
  this.spell_container = null;
  this.extra_menu_items = [];
  this.use_close_btn = false;

  //Change all
  var change_all = function(elm, current_googie) {
    for(var i=0; i < this.g_elms.length; i++) {
      var g_elm = this.g_elms[i];
      for(var j=0; j < g_elm.error_links.length; j++) {
        var err_elm = g_elm.error_links[j];
        if(err_elm.innerHTML == elm.old_value) {
          g_elm.correctError(err_elm.g_id, err_elm, elm);
        }
      }
    }
  };

  var change_eval = function(elm, current_googie) {
    if(elm.is_corrected)
      return true;
    return false;
  };

  //Don't show ignore all if it's an duplicate
  var ignore_all_eval = function(elm, current_googie) {
    if(current_googie.results[elm.g_id]['attrs']['duplicate'] == "true")
      return false;
    return true;
  };

  this.appendNewMenuItem("Change all", AJS.$b(change_all, this), change_eval);
  this.appendNewMenuItem("Ignore", AJS.$b(this.ignore, this), null);
  this.appendNewMenuItem("Ignore all", AJS.$b(this.ignoreAll, this), ignore_all_eval);

  this.state = "created";

  this.no_more_errors_callback = null;
  this.elm_evaluator = function() {
    return true;
  }
  
  this.getValue = function(ta){
    return ta.value;
  }
  this.setValue = function(ta, value){
    ta.value = value;
  }

  //Counters
  this.cnt_no_errors = null;
  this.cnt_ta_spellchecked = 0;
  this.cnt_done_checking = null;
}

//////
// API Functions
/////
/**
  Can return: 
  - created: Just created
  - spell_check: Spell check is shown and waiting for user input
  - resume_editing: Resume editing is shown to the user
  - checking_spell: Spell checking ongoing
  - spell_cheked: Spell check is complete
**/
GoogieSpellMultiple.prototype.getState = function() {
  return this.state;
}

GoogieSpellMultiple.prototype.noMoreErrorsCallback = function(fn) {
  this.no_more_errors_callback = fn;
}

GoogieSpellMultiple.prototype.noErrorsFound = function() {
  return this.cnt_no_errors != null && this.cnt_no_errors == this.g_elms.length;
}

GoogieSpellMultiple.prototype.isReqDone = function() {
  return this.cnt_done_checking == null || this.cnt_done_checking == this.g_elms.length;
}

GoogieSpellMultiple.prototype.isSpellChecking = function() {
  return this.state == "checking_spell";
}

GoogieSpellMultiple.prototype.setDependent = function() {
  this.dependent = true;
}

GoogieSpellMultiple.prototype.setCustomAjaxError = function(fn) {
  this.custom_ajax_error = fn;
}

GoogieSpellMultiple.prototype.setElementEvaulator = function(fn) {
  this.elm_evaluator = fn;
}

GoogieSpellMultiple.prototype.setSpellContainer = function(elm) {
  this.spell_container = AJS.getElement(elm);
}

GoogieSpellMultiple.prototype.useCloseButtons = function() {
  this.use_close_btn = true;
}

GoogieSpellMultiple.prototype.setGetValue = function(fn) {
    this.getValue = fn;
}

GoogieSpellMultiple.prototype.setSetValue = function(fn) {
    this.setValue = fn;
}

GoogieSpellMultiple.prototype.appendNewMenuItem = function(name, call_back_fn, checker) {
  this.extra_menu_items.push([name, call_back_fn, checker]);
}

GoogieSpellMultiple.prototype.countNoErrorsUp = function() { 
  if(!this.cnt_no_errors)
    this.cnt_no_errors = 1;
  else
    this.cnt_no_errors++;
}

GoogieSpellMultiple.prototype.countSpellCheckingFinished = function() {
  if(!this.cnt_done_checking)
    this.cnt_done_checking = 1;
  else
    this.cnt_done_checking++;
}

GoogieSpellMultiple.prototype.resetCounters = function() {
  this.cnt_done_checking = null;
  this.cnt_no_errors = null;
  this.cnt_ta_spellchecked = 0;
}

GoogieSpellMultiple.prototype.spellCheckObserver = function(state, current_googie) {
  if(state == "checking_spell") {
    this.resetCounters();
    this.cnt_done_checking = 0;
    this.state = "checking_spell";

    for(var i=0; i < this.g_elms.length; i++) {
      var elm = this.g_elms[i];

      if(elm.state == "spell_check") {
        elm.setReportStateChange(false);
        elm.spellCheck(!this.elm_evaluator(elm));
        elm.setReportStateChange(true);
      }
    }
  }
  else if(state == "spell_check") {
    this.state = "spell_check";
    for(var i=0; i < this.g_elms.length; i++) {
      var elm = this.g_elms[i];
      //If some is in resume_editing, then resume them
      if(elm.spell_span != null && elm.state != "spell_check" && elm.state != "no_error_found" && elm.state != "checking_spell") {
        elm.setReportStateChange(false);
        elm.resumeEditing();
        elm.setReportStateChange(true);
      }
    }
  }
  else if(state == "resume_editing") {
    this.state = "resume_editing";

    this.countSpellCheckingFinished();
    this.spellcheckingFinished();

    var elm = this.g_elms[0];
    elm.setReportStateChange(false);
    elm.resumeEditingState();
    elm.setReportStateChange(true);
  }
}

GoogieSpellMultiple.prototype.spellcheckingFinished = function() {
  if(this.cnt_done_checking == this.g_elms.length) {
    for(var i=0; i < this.g_elms.length; i++) {
      var elm = this.g_elms[i];
      if(elm.setFocus())
        break;
    }
  }
}

GoogieSpellMultiple.prototype.spellCheckState = function() {
  for(var i=0; i < this.g_elms.length; i++) {
    var elm = this.g_elms[i];
    elm.setReportStateChange(false);
    elm.resumeEditing();
    elm.setReportStateChange(true);
  }
}

GoogieSpellMultiple.prototype.showMenuObserver = function(me, googie) {
  for(var i=0; i < me.g_elms.length; i++) {
    var elm = me.g_elms[i];
    if(elm != googie) {
      elm.hideLangWindow();
      elm.hideErrorWindow();
    }
  }
}

GoogieSpellMultiple.prototype.ignore = function(elm, current_googie) {
  AJS.swapDOM(elm, AJS.TN(elm.innerHTML));
  if(!elm.is_corrected)
    current_googie.errorFixed();
  elm.is_corrected = true;
}

GoogieSpellMultiple.prototype.ignoreAll = function(elm) {
  for(var i=0; i < this.g_elms.length; i++) {
    var g_elm = this.g_elms[i];
    for(var j=0; j < g_elm.error_links.length; j++) {
      var err_elm = g_elm.error_links[j];
      if(err_elm.innerHTML == elm.innerHTML) {
        try { AJS.swapDOM(err_elm, AJS.TN(err_elm.innerHTML)); }
        catch(e) { }
        if(err_elm.is_corrected == false) {
          g_elm.errorFixed();
          err_elm.is_corrected = true;
        }
      }
    }
  }
}

GoogieSpellMultiple.prototype._taSpellChecked = function() {
  this.cnt_ta_spellchecked++;
  if(this.no_more_errors_callback)
    if(this.cnt_ta_spellchecked+this.cnt_no_errors == this.g_elms.length)
      this.no_more_errors_callback();
}


GoogieSpellMultiple.prototype.decorateTextareas = function(/*ids or elms*/) {
  var me = this;
  for(var i=0; i<arguments.length; i++) {
    var elm = AJS.$(arguments[i]);
    if(elm != null) {
      //Create new GoogieSpell object
      var googie = new GoogieSpell(this.img_dir, this.server_url);

      googie.use_focus = true;

      if(this.g_elms.length != 0)
        googie.main_controller = false;

      googie.edit_layer_dbl_click = false;
      googie.report_ta_not_found = true;

      if(this.dependent) {
        googie.spelling_state_observer = function(state, current_googie) {
          me.spellCheckObserver(state, current_googie);
        };

        googie.custom_no_spelling_error = function(obj) {
          obj.state = "spell_check";
          me.countNoErrorsUp();

          if(me.cnt_no_errors == me.g_elms.length) {
            me.g_elms[0].flashNoSpellingErrorState(AJS.$b(me.spellCheckState, me));
          }

          me.countSpellCheckingFinished();
          me.spellcheckingFinished();
        };
      }

      if(this.custom_ajax_error)
        googie.custom_ajax_error = this.custom_ajax_error;
      
      googie.show_menu_observer = function(p) {me.showMenuObserver(me, p)};

      //Duplicate entry funcs
      var dupe_eval = function(parse_item) {
        if(parse_item['attrs']['duplicate'] == "true")
          return true;
        return false;
      };
      var dupe_menu = function(g, table, c_elm) {
        var rm_elm = function() {
          g.correctError(c_elm.g_id, c_elm, {'innerHTML': ""}, true);
        };
        AJS.ACN(table, g.createButton("Remove duplicate", "", rm_elm));
        AJS.ACN(table, g.createListSeparator());

        for(var k=0; k < me.extra_menu_items.length; k++) {
          var m_item = me.extra_menu_items[k];
          if(!m_item[2] || m_item[2](c_elm, g))
            AJS.ACN(table, g.createButton(m_item[0], "", AJS.$p(m_item[1], c_elm, g)));
        }

        AJS.ACN(table, g.createCloseButton(g.hideErrorWindow));
        return true;
      };

      //Auto correct funcs
      var ac_eval = function(g, c_elm) {
        var parse_item = g.results[c_elm.g_id];
        if(parse_item['attrs']['autoChange'] == 1) {
          g.correctError(c_elm.g_id, c_elm, {'innerHTML': parse_item['suggestions'][0]}, false);
        }
      };

      googie.custom_item_evaulator = ac_eval;
      googie.appendCustomMenuBuilder(dupe_eval, dupe_menu);
      
      if(this.spell_container) {
        googie.setDecoration(false);
        googie.setSpellContainer(this.spell_container);
        googie.custom_spellcheck_starter = function() {
          me.spellCheckObserver("checking_spell");
        };
      }

      //All errors fixed
      var fn_all_fixed = function() {
        me._taSpellChecked();
      };
      googie.all_errors_fixed_observer = fn_all_fixed;

      googie.extra_menu_items = this.extra_menu_items;
      googie.use_close_btn = this.use_close_btn;

      googie.getValue = this.getValue;
      googie.setValue = this.setValue;

      googie.decorateTextarea(elm);
      this.g_elms.push(googie);
    }
  }
  this.state = "spell_check";
}
