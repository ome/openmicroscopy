/****
Last Modified: 13/05/07 00:25:28

 GoogieSpell
     Google spell checker for your own web-apps :)
 Copyright Amir Salihefendic 2006
     LICENSE
     GPL (see gpl.txt for more information)
     This basically means that you can't use this script with/in proprietary software!
     There is another license that permits you to use this script with proprietary software. Check out:... for more info.
     AUTHOR
         4mir Salihefendic (http://amix.dk) - amix@amix.dk
 VERSION
     4.0
****/
var GOOGIE_CUR_LANG = null;
var GOOGIE_DEFAULT_LANG = "en";

function GoogieSpell(img_dir, server_url) {
    var cookie_value;
    var lang;
    cookie_value = getCookie('language');

    if(cookie_value != null)
        GOOGIE_CUR_LANG = cookie_value;
    else
        GOOGIE_CUR_LANG = GOOGIE_DEFAULT_LANG;

    this.img_dir = img_dir;
    this.server_url = server_url;

    this.org_lang_to_word = {"da": "Dansk", "de": "Deutsch", "en": "English",
                                             "es": "Espa&#241;ol", "fr": "Fran&#231;ais", "it": "Italiano", 
                                             "nl": "Nederlands", "pl": "Polski", "pt": "Portugu&#234;s",
                                             "fi": "Suomi", "sv": "Svenska"};
    this.lang_to_word = this.org_lang_to_word;
    this.langlist_codes = AJS.keys(this.lang_to_word);

    this.show_change_lang_pic = true;
    this.change_lang_pic_placement = "left";

    this.report_state_change = true;

    this.ta_scroll_top = 0;
    this.el_scroll_top = 0;

    this.lang_chck_spell = "Check spelling";
    this.lang_revert = "Revert to";
    this.lang_close = "Close";
    this.lang_rsm_edt = "Resume editing";
    this.lang_no_error_found = "No spelling errors found";
    this.lang_no_suggestions = "No suggestions";
    
    this.show_spell_img = true;
    this.decoration = true;
    this.use_close_btn = true;
    this.edit_layer_dbl_click = true;
    this.report_ta_not_found = true;

    //Extensions
    this.custom_ajax_error = null;
    this.custom_no_spelling_error = null;
    this.custom_menu_builder = []; //Should take an eval function and a build menu function
    this.custom_item_evaulator = null; //Should take an eval function and a build menu function
    this.extra_menu_items = [];
    this.custom_spellcheck_starter = null;
    this.main_controller = true;

    //Observers
    this.lang_state_observer = null;
    this.spelling_state_observer = null;
    this.show_menu_observer = null;
    this.all_errors_fixed_observer = null;

    //Focus links - used to give the text box focus
    this.use_focus = false;
    this.focus_link_t = null;
    this.focus_link_b = null;

    //Counters
    this.cnt_errors = 0;
    this.cnt_errors_fixed = 0;

    //Set document on click to hide the language and error menu
    var fn = function(e) {
        var elm = AJS.getEventElm(e);
        if(elm.googie_action_btn != "1" && this.isLangWindowShown())
            this.hideLangWindow();
        if(elm.googie_action_btn != "1" && this.isErrorWindowShown())
            this.hideErrorWindow();
    };
    AJS.AEV(document, "click", AJS.$b(fn, this));
}

GoogieSpell.prototype.decorateTextarea = function(id) {
    if(typeof(id) == "string")
        this.text_area = AJS.$(id);
    else
        this.text_area = id;

    var r_width, r_height;

    if(this.text_area != null) {
        if(!AJS.isDefined(this.spell_container) && this.decoration) {
            var table = AJS.TABLE();
            var tbody = AJS.TBODY();
            var tr = AJS.TR();
            if(AJS.isDefined(this.force_width))
                r_width = this.force_width;
            else
                r_width = this.text_area.offsetWidth + "px";

            if(AJS.isDefined(this.force_height))
                r_height = this.force_height;
            else
                r_height = "";

            var spell_container = AJS.TD();
            this.spell_container = spell_container;

            tr.appendChild(spell_container);

            tbody.appendChild(tr);
            table.appendChild(tbody);

            AJS.insertBefore(table, this.text_area);

            //Set width
            AJS.setHeight(table, spell_container, r_height);
            AJS.setWidth(table, spell_container, r_width);

            spell_container.style.textAlign = "right";
        }

        this.checkSpellingState();
    }
    else 
        if(this.report_ta_not_found)
            alert("Text area not found");
}

//////
// API Functions (the ones that you can call)
/////
GoogieSpell.prototype.setSpellContainer = function(elm) {
    this.spell_container = AJS.$(elm);
}

GoogieSpell.prototype.setLanguages = function(lang_dict) {
    this.lang_to_word = lang_dict;
    this.langlist_codes = AJS.keys(lang_dict);
}

GoogieSpell.prototype.setForceWidthHeight = function(width, height) {
    /***
        Set to null if you want to use one of them
    ***/
    this.force_width = width;
    this.force_height = height;
}

GoogieSpell.prototype.setDecoration = function(bool) {
    this.decoration = bool;
}

GoogieSpell.prototype.dontUseCloseButtons = function() {
    this.use_close_btn = false;
}

GoogieSpell.prototype.appendNewMenuItem = function(name, call_back_fn, checker) {
    this.extra_menu_items.push([name, call_back_fn, checker]);
}

GoogieSpell.prototype.appendCustomMenuBuilder = function(eval, builder) {
    this.custom_menu_builder.push([eval, builder]);
}

GoogieSpell.prototype.setFocus = function() {
    try {
        this.focus_link_b.focus();
        this.focus_link_t.focus();
        return true;
    }
    catch(e) {
        return false;
    }
}

GoogieSpell.prototype.getValue = function(ta) {
    return ta.value;
}

GoogieSpell.prototype.setValue = function(ta, value) {
    ta.value = value;
}


//////
// Set functions (internal)
/////
GoogieSpell.prototype.setStateChanged = function(current_state) {
    this.state = current_state;
    if(this.spelling_state_observer != null && this.report_state_change)
        this.spelling_state_observer(current_state, this);
}

GoogieSpell.prototype.setReportStateChange = function(bool) {
    this.report_state_change = bool;
}


//////
// Request functions
/////
GoogieSpell.prototype.getGoogleUrl = function() {
    return this.server_url + GOOGIE_CUR_LANG;
}

GoogieSpell.escapeSepcial = function(val) {
    return val.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

GoogieSpell.createXMLReq = function (text) {
    return '<?xml version="1.0" encoding="utf-8" ?><spellrequest textalreadyclipped="0" ignoredups="0" ignoredigits="1" ignoreallcaps="1"><text>' + text + '</text></spellrequest>';
}

GoogieSpell.prototype.spellCheck = function(ignore) {
    var me = this;

    this.cnt_errors_fixed = 0;
    this.cnt_errors = 0;
    this.setStateChanged("checking_spell");

    if(this.main_controller)
        this.appendIndicator(this.spell_span);

    this.error_links = [];
    this.ta_scroll_top = this.text_area.scrollTop;

    try { this.hideLangWindow(); }
    catch(e) {}

    this.ignore = ignore;

    if(this.getValue(this.text_area) == '' || ignore) {
        if(!me.custom_no_spelling_error)
            me.flashNoSpellingErrorState();
        else
            me.custom_no_spelling_error(me);
        me.removeIndicator();
        return ;
    }
    
    this.createEditLayer(this.text_area.offsetWidth, this.text_area.offsetHeight);

    this.createErrorWindow();
    AJS.getBody().appendChild(this.error_window);

    try { netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserRead"); } 
    catch (e) { }

    if(this.main_controller)
        this.spell_span.onclick = null;

    this.orginal_text = this.getValue(this.text_area);

    //Create request
    var d = AJS.getRequest(this.getGoogleUrl());
    var reqdone = function(res_txt) {
        var r_text = res_txt;
        me.results = me.parseResult(r_text);

        if(r_text.match(/<c.*>/) != null) {
            //Before parsing be sure that errors were found
            me.showErrorsInIframe();
            me.resumeEditingState();
        }
        else {
            if(!me.custom_no_spelling_error)
                me.flashNoSpellingErrorState();
            else
                me.custom_no_spelling_error(me);
        }
        me.removeIndicator();
    };

    d.addCallback(reqdone);
    reqdone = null;

    var reqfailed = function(res_txt, req) {
        if(me.custom_ajax_error)
            me.custom_ajax_error(req);
        else
            alert("An error was encountered on the server. Please try again later.");

        if(me.main_controller) {
            AJS.removeElement(me.spell_span);
            me.removeIndicator();
        }
        me.checkSpellingState();
    };
    d.addErrback(reqfailed);
    reqfailed = null;

    var req_text = GoogieSpell.escapeSepcial(this.orginal_text);
    d.sendReq(GoogieSpell.createXMLReq(req_text));
}


//////
// Spell checking functions
/////
GoogieSpell.prototype.parseResult = function(r_text) {
    /***
     Retunrs an array
     result[item] -> ['attrs'], ['suggestions']
        ***/
    var re_split_attr_c = /\w+="(\d+|true)"/g;
    var re_split_text = /\t/g;

    var matched_c = r_text.match(/<c[^>]*>[^<]*<\/c>/g);
    var results = new Array();

    if(matched_c == null)
        return results;
    
    for(var i=0; i < matched_c.length; i++) {
        var item = new Array();
        this.errorFound();

        //Get attributes
        item['attrs'] = new Array();
        var split_c = matched_c[i].match(re_split_attr_c);
        for(var j=0; j < split_c.length; j++) {
            var c_attr = split_c[j].split(/=/);
            var val = c_attr[1].replace(/"/g, '');
            if(val != "true")
                item['attrs'][c_attr[0]] = parseInt(val);
            else {
                item['attrs'][c_attr[0]] = val;
            }
        }

        //Get suggestions
        item['suggestions'] = new Array();
        var only_text = matched_c[i].replace(/<[^>]*>/g, "");
        var split_t = only_text.split(re_split_text);
        for(var k=0; k < split_t.length; k++) {
        if(split_t[k] != "")
            item['suggestions'].push(split_t[k]);
        }
        results.push(item);
    }
    return results;
}

//////
// Counters
/////
GoogieSpell.prototype.errorFixed = function() { 
    this.cnt_errors_fixed++; 
    if(this.all_errors_fixed_observer)
        if(this.cnt_errors_fixed == this.cnt_errors) {
            this.hideErrorWindow();
            this.all_errors_fixed_observer();
        }
}
GoogieSpell.prototype.errorFound = function() { this.cnt_errors++; }

//////
// Error menu functions
/////
GoogieSpell.prototype.createErrorWindow = function() {
    this.error_window = AJS.DIV();
    this.error_window.className = "googie_window";
    this.error_window.googie_action_btn = "1";
}

GoogieSpell.prototype.isErrorWindowShown = function() {
    return this.error_window != null && this.error_window.style.visibility == "visible";
}

GoogieSpell.prototype.hideErrorWindow = function() {
    try {
        this.error_window.style.visibility = "hidden";
        if(this.error_window_iframe)
            this.error_window_iframe.style.visibility = "hidden";
    }
    catch(e) {}
}

GoogieSpell.prototype.updateOrginalText = function(offset, old_value, new_value, id) {
    var part_1 = this.orginal_text.substring(0, offset);
    var part_2 = this.orginal_text.substring(offset+old_value.length);
    this.orginal_text = part_1 + new_value + part_2;
    this.setValue(this.text_area, this.orginal_text);
    var add_2_offset = new_value.length - old_value.length;
    for(var j=0; j < this.results.length; j++) {
        //Don't edit the offset of the current item
        if(j != id && j > id){
            this.results[j]['attrs']['o'] += add_2_offset;
        }
    }
}

GoogieSpell.prototype.saveOldValue = function(elm, old_value) {
    elm.is_changed = true;
    elm.old_value = old_value;
}

GoogieSpell.prototype.createListSeparator = function() {
    var e_col = AJS.TD(" ");
    e_col.googie_action_btn = "1";
    e_col.style.cursor = "default";
    e_col.style.fontSize = "3px";
    e_col.style.borderTop = "1px solid #ccc";
    e_col.style.paddingTop = "3px";

    return AJS.TR(e_col);
}

GoogieSpell.prototype.correctError = function(id, elm, l_elm, /*optional*/ rm_pre_space) {
    var old_value = elm.innerHTML;
    var new_value = l_elm.innerHTML;
    var offset = this.results[id]['attrs']['o'];

    if(rm_pre_space) {
        var pre_length = elm.previousSibling.innerHTML;
        elm.previousSibling.innerHTML = pre_length.slice(0, pre_length.length-1);
        old_value = " " + old_value;
        offset--;
    }

    this.hideErrorWindow();

    this.updateOrginalText(offset, old_value, new_value, id);

    elm.innerHTML = new_value;
    
    elm.style.color = "green";
    elm.is_corrected = true;

    this.results[id]['attrs']['l'] = new_value.length;

    if(!AJS.isDefined(elm.old_value))
        this.saveOldValue(elm, old_value);
    
    this.errorFixed();
}

GoogieSpell.prototype.showErrorWindow = function(elm, id) {
    if(this.show_menu_observer)
        this.show_menu_observer(this);
    var me = this;

    var abs_pos = AJS.absolutePosition(elm);
    abs_pos.y -= this.edit_layer.scrollTop;
    this.error_window.style.visibility = "visible";

    AJS.setTop(this.error_window, (abs_pos.y+20));
    AJS.setLeft(this.error_window, (abs_pos.x));

    this.error_window.innerHTML = "";

    var table = AJS.TABLE({'class': 'googie_list'});
    table.googie_action_btn = "1";
    var list = AJS.TBODY();

    //Check if we should use custom menu builder, if not we use the default
    var changed = false;
    if(this.custom_menu_builder != []) {
        for(var k=0; k<this.custom_menu_builder.length; k++) {
            var eb = this.custom_menu_builder[k];
            if(eb[0]((this.results[id]))){
                changed = eb[1](this, list, elm);
                break;
            }
        }
    }
    if(!changed) {
        //Build up the result list
        var suggestions = this.results[id]['suggestions'];
        var offset = this.results[id]['attrs']['o'];
        var len = this.results[id]['attrs']['l'];

        if(suggestions.length == 0) {
            var row = AJS.TR();
            var item = AJS.TD({'style': 'cursor: default;'});
            var dummy = AJS.SPAN();
            dummy.innerHTML = this.lang_no_suggestions;
            AJS.ACN(item, AJS.TN(dummy.innerHTML));
            item.googie_action_btn = "1";
            row.appendChild(item);
            list.appendChild(row);
        }

        for(i=0; i < suggestions.length; i++) {
            var row = AJS.TR();
            var item = AJS.TD();
            var dummy = AJS.SPAN();
            dummy.innerHTML = suggestions[i];
            item.appendChild(AJS.TN(dummy.innerHTML));
            
            var fn = function(e) {
                var l_elm = AJS.getEventElm(e);
                this.correctError(id, elm, l_elm);
            };

            AJS.AEV(item, "click", AJS.$b(fn, this));

            item.onmouseover = GoogieSpell.item_onmouseover;
            item.onmouseout = GoogieSpell.item_onmouseout;
            row.appendChild(item);
            list.appendChild(row);
        }

        //The element is changed, append the revert
        if(elm.is_changed && elm.innerHTML != elm.old_value) {
            var old_value = elm.old_value;
            var revert_row = AJS.TR();
            var revert = AJS.TD();

            revert.onmouseover = GoogieSpell.item_onmouseover;
            revert.onmouseout = GoogieSpell.item_onmouseout;
            var rev_span = AJS.SPAN({'class': 'googie_list_revert'});
            rev_span.innerHTML = this.lang_revert + " " + old_value;
            revert.appendChild(rev_span);

            var fn = function(e) { 
                this.updateOrginalText(offset, elm.innerHTML, old_value, id);
                elm.is_corrected = true;
                elm.style.color = "#b91414";
                elm.innerHTML = old_value;
                this.hideErrorWindow();
            };
            AJS.AEV(revert, "click", AJS.$b(fn, this));

            revert_row.appendChild(revert);
            list.appendChild(revert_row);
        }
        
        //Append the edit box
        var edit_row = AJS.TR();
        var edit = AJS.TD({'style': 'cursor: default'});

        var edit_input = AJS.INPUT({'style': 'width: 120px; margin:0; padding:0', 'value': elm.innerHTML});
        edit_input.googie_action_btn = "1";

        var onsub = function () {
            if(edit_input.value != "") {
                if(!AJS.isDefined(elm.old_value))
                    this.saveOldValue(elm, elm.innerHTML);

                this.updateOrginalText(offset, elm.innerHTML, edit_input.value, id);
                elm.style.color = "green"
                elm.is_corrected = true;
                elm.innerHTML = edit_input.value;
                
                this.hideErrorWindow();
            }
            return false;
        };
        onsub = AJS.$b(onsub, this);
        
        var ok_pic = AJS.IMG({'src': '/static/webclient/image/googiespell/ok.gif', 'style': 'width: 32px; height: 16px; margin-left: 2px; margin-right: 2px; cursor: pointer;'});
        var edit_form = AJS.FORM({'style': 'margin: 0; padding: 0; cursor: default;'}, edit_input, ok_pic);

        edit_form.googie_action_btn = "1";
        edit.googie_action_btn = "1";

        AJS.AEV(edit_form, "submit", onsub);
        AJS.AEV(ok_pic, "click", onsub);
        
        edit.appendChild(edit_form);
        edit_row.appendChild(edit);
        list.appendChild(edit_row);

        //Append extra menu items
        if(this.extra_menu_items.length > 0)
            AJS.ACN(list, this.createListSeparator());

        var loop = function(i) {
                if(i < me.extra_menu_items.length) {
                    var e_elm = me.extra_menu_items[i];

                    if(!e_elm[2] || e_elm[2](elm, me)) {
                        var e_row = AJS.TR();
                        var e_col = AJS.TD(e_elm[0]);

                        e_col.onmouseover = GoogieSpell.item_onmouseover;
                        e_col.onmouseout = GoogieSpell.item_onmouseout;

                        var fn = function() {
                            return e_elm[1](elm, me);
                        };
                        AJS.AEV(e_col, "click", fn);

                        AJS.ACN(e_row, e_col);
                        AJS.ACN(list, e_row);

                    }
                    loop(i+1);
                }
        }
        loop(0);
        loop = null;

        //Close button
        if(this.use_close_btn) {
            AJS.ACN(list, this.createCloseButton(this.hideErrorWindow));
        }
    }

    table.appendChild(list);
    this.error_window.appendChild(table);

    //Dummy for IE - dropdown bug fix
    if(AJS.isIe() && !this.error_window_iframe) {
        var iframe = AJS.IFRAME({'style': 'position: absolute; z-index: 0;'});
        AJS.ACN(AJS.getBody(), iframe);
        this.error_window_iframe = iframe;
    }
    if(AJS.isIe()) {
        var iframe = this.error_window_iframe;
        AJS.setTop(iframe, this.error_window.offsetTop);
        AJS.setLeft(iframe, this.error_window.offsetLeft);

        AJS.setWidth(iframe, this.error_window.offsetWidth);
        AJS.setHeight(iframe, this.error_window.offsetHeight);

        iframe.style.visibility = "visible";
    }

    //Set focus on the last element
    var link = this.createFocusLink('link');
    list.appendChild(AJS.TR(AJS.TD({'style': 'text-align: right; font-size: 1px; height: 1px; margin: 0; padding: 0;'}, link)));
    link.focus();
}


//////
// Edit layer (the layer where the suggestions are stored)
//////
GoogieSpell.prototype.createEditLayer = function(width, height) {
    this.edit_layer = AJS.DIV({'class': 'googie_edit_layer'});

    //Set the style so it looks like edit areas
    this.edit_layer.className = this.text_area.className;
    this.edit_layer.style.border = "1px solid #999";
    this.edit_layer.style.backgroundColor = "#f7f7f7";
    this.edit_layer.style.padding = "3px";
    this.edit_layer.style.margin = "0px";

    AJS.setWidth(this.edit_layer, (width-8));

    if(AJS.nodeName(this.text_area) != "input" || this.getValue(this.text_area) == "") {
        this.edit_layer.style.overflow = "auto";
        AJS.setHeight(this.edit_layer, (height-6));
    }
    else {
        this.edit_layer.style.overflow = "hidden";
    }

    if(this.edit_layer_dbl_click) {
        var me = this;
        var fn = function(e) {
            if(AJS.getEventElm(e).className != "googie_link" && !me.isErrorWindowShown()) {
                me.resumeEditing();
                var fn1 = function() {
                    me.text_area.focus();
                    fn1 = null;
                };
                AJS.callLater(fn1, 10);
            }
            return false;
        };
        this.edit_layer.ondblclick = fn;
        fn = null;
    }
}

GoogieSpell.prototype.resumeEditing = function() {
    this.setStateChanged("spell_check");
    this.switch_lan_pic.style.display = "inline";

    if(this.edit_layer)
        this.el_scroll_top = this.edit_layer.scrollTop;

    this.hideErrorWindow();

    if(this.main_controller)
        this.spell_span.className = "googie_no_style";

    if(!this.ignore) {
        //Remove the EDIT_LAYER
        try {
            this.edit_layer.parentNode.removeChild(this.edit_layer);
            if(this.use_focus) {
                AJS.removeElement(this.focus_link_t);
                AJS.removeElement(this.focus_link_b);
            }
        }
        catch(e) {
        }

        AJS.showElement(this.text_area);

        if(this.el_scroll_top != undefined)
            this.text_area.scrollTop = this.el_scroll_top;
    }

    this.checkSpellingState(false);
}

GoogieSpell.prototype.createErrorLink = function(text, id) {
    var elm = AJS.SPAN({'class': 'googie_link'});
    var me = this;
    var d = function (e) {
        me.showErrorWindow(elm, id);
        d = null;
        return false;
    };
    AJS.AEV(elm, "click", d);

    elm.googie_action_btn = "1";
    elm.g_id = id;
    elm.is_corrected = false;
    elm.oncontextmenu = d;
    elm.innerHTML = text;
    return elm;
}

GoogieSpell.createPart = function(txt_part) {
    if(txt_part == " ")
        return AJS.TN(" ");
    var result = AJS.SPAN();

    var is_first = true;
    var is_safari = (navigator.userAgent.toLowerCase().indexOf("safari") != -1);

    var part = AJS.SPAN();
    txt_part = GoogieSpell.escapeSepcial(txt_part);
    txt_part = txt_part.replace(/\n/g, "<br>");
    txt_part = txt_part.replace(/    /g, " &nbsp;");
    txt_part = txt_part.replace(/^ /g, "&nbsp;");
    txt_part = txt_part.replace(/ $/g, "&nbsp;");
    
    part.innerHTML = txt_part;

    return part;
}

GoogieSpell.prototype.showErrorsInIframe = function() {
    var output = AJS.DIV();
    output.style.textAlign = "left";
    var pointer = 0;
    var results = this.results;

    if(results.length > 0) {
        for(var i=0; i < results.length; i++) {
            var offset = results[i]['attrs']['o'];
            var len = results[i]['attrs']['l'];
            
            var part_1_text = this.orginal_text.substring(pointer, offset);
            var part_1 = GoogieSpell.createPart(part_1_text);
            output.appendChild(part_1);
            pointer += offset - pointer;
            
            //If the last child was an error, then insert some space
            var err_link = this.createErrorLink(this.orginal_text.substr(offset, len), i);
            this.error_links.push(err_link);
            output.appendChild(err_link);
            pointer += len;
        }
        //Insert the rest of the orginal text
        var part_2_text = this.orginal_text.substr(pointer, this.orginal_text.length);

        var part_2 = GoogieSpell.createPart(part_2_text);
        output.appendChild(part_2);
    }
    else
        output.innerHTML = this.orginal_text;

    var me = this;
    if(this.custom_item_evaulator)
        AJS.map(this.error_links, function(elm){me.custom_item_evaulator(me, elm)});
    
    AJS.ACN(this.edit_layer, output);

    //Hide text area
    this.text_area_bottom = this.text_area.offsetTop + this.text_area.offsetHeight;

    AJS.hideElement(this.text_area);

    AJS.insertBefore(this.edit_layer, this.text_area);

    if(this.use_focus) {
        this.focus_link_t = this.createFocusLink('focus_t');
        this.focus_link_b = this.createFocusLink('focus_b');

        AJS.insertBefore(this.focus_link_t, this.edit_layer);
        AJS.insertAfter(this.focus_link_b, this.edit_layer);
    }

    this.edit_layer.scrollTop = this.ta_scroll_top;
}


//////
// Choose language menu
//////
GoogieSpell.prototype.createLangWindow = function() {
    this.language_window = AJS.DIV({'class': 'googie_window'});
    AJS.setWidth(this.language_window, 100);

    this.language_window.googie_action_btn = "1";

    //Build up the result list
    var table = AJS.TABLE({'class': 'googie_list'});
    AJS.setWidth(table, "100%");
    var list = AJS.TBODY();

    this.lang_elms = new Array();

    for(i=0; i < this.langlist_codes.length; i++) {
        var row = AJS.TR();
        var item = AJS.TD();
        item.googieId = this.langlist_codes[i];
        this.lang_elms.push(item);
        var lang_span = AJS.SPAN();
        lang_span.innerHTML = this.lang_to_word[this.langlist_codes[i]];
        item.appendChild(AJS.TN(lang_span.innerHTML));

        var fn = function(e) {
            var elm = AJS.getEventElm(e);
            this.deHighlightCurSel();

            this.setCurrentLanguage(elm.googieId);

            if(this.lang_state_observer != null) {
                this.lang_state_observer();
            }

            this.highlightCurSel();
            this.hideLangWindow();
        };
        AJS.AEV(item, "click", AJS.$b(fn, this));

        item.onmouseover = function(e) { 
            var i_it = AJS.getEventElm(e);
            if(i_it.className != "googie_list_selected")
                i_it.className = "googie_list_onhover";
        };
        item.onmouseout = function(e) { 
            var i_it = AJS.getEventElm(e);
            if(i_it.className != "googie_list_selected")
                i_it.className = "googie_list_onout"; 
        };

        row.appendChild(item);
        list.appendChild(row);
    }

    //Close button
    if(this.use_close_btn) {
        list.appendChild(this.createCloseButton(this.hideLangWindow));
    }

    this.highlightCurSel();

    table.appendChild(list);
    this.language_window.appendChild(table);
}

GoogieSpell.prototype.setCurrentLanguage = function(lan_code) {
    GOOGIE_CUR_LANG = lan_code;

    //Set cookie
    var now = new Date();
    now.setTime(now.getTime() + 365 * 24 * 60 * 60 * 1000);
    setCookie('language', lan_code, now);
}

GoogieSpell.prototype.isLangWindowShown = function() {
    return this.language_window != null && this.language_window.style.visibility == "visible";
}

GoogieSpell.prototype.hideLangWindow = function() {
    try {
        this.language_window.style.visibility = "hidden";
        this.switch_lan_pic.className = "googie_lang_3d_on";
    }
    catch(e) {}
}

GoogieSpell.prototype.deHighlightCurSel = function() {
    this.lang_cur_elm.className = "googie_list_onout";
}

GoogieSpell.prototype.highlightCurSel = function() {
    if(GOOGIE_CUR_LANG == null)
        GOOGIE_CUR_LANG = GOOGIE_DEFAULT_LANG;
    for(var i=0; i < this.lang_elms.length; i++) {
        if(this.lang_elms[i].googieId == GOOGIE_CUR_LANG) {
            this.lang_elms[i].className = "googie_list_selected";
            this.lang_cur_elm = this.lang_elms[i];
        }
        else {
            this.lang_elms[i].className = "googie_list_onout";
        }
    }
}

GoogieSpell.prototype.showLangWindow = function(elm, ofst_top, ofst_left) {
    if(this.show_menu_observer)
        this.show_menu_observer(this);
    if(!AJS.isDefined(ofst_top))
        ofst_top = 20;
    if(!AJS.isDefined(ofst_left))
        ofst_left = 100;

    this.createLangWindow();
    AJS.getBody().appendChild(this.language_window);

    var abs_pos = AJS.absolutePosition(elm);
    AJS.showElement(this.language_window);
    AJS.setTop(this.language_window, (abs_pos.y+ofst_top));
    AJS.setLeft(this.language_window, (abs_pos.x+ofst_left-this.language_window.offsetWidth));

    this.highlightCurSel();
    this.language_window.style.visibility = "visible";
}

GoogieSpell.prototype.createChangeLangPic = function() {
    var img = AJS.IMG({'src': '../image/googiespell/change_lang.gif', 'alt': "Change language"});
    img.googie_action_btn = "1";
    var switch_lan = AJS.SPAN({'class': 'googie_lang_3d_on', 'style': 'padding-left: 6px;'}, img);

    var fn = function(e) {
        var elm = AJS.getEventElm(e);
        if(AJS.nodeName(elm) == 'img')
            elm = elm.parentNode;
        if(elm.className == "googie_lang_3d_click") {
            elm.className = "googie_lang_3d_on";
            this.hideLangWindow();
        }
        else {
            elm.className = "googie_lang_3d_click";
            this.showLangWindow(switch_lan);
        }
    }

    AJS.AEV(switch_lan, "click", AJS.$b(fn, this));
    return switch_lan;
}

GoogieSpell.prototype.createSpellDiv = function() {
    var chk_spell = AJS.SPAN({'class': 'googie_check_spelling_link'});

    chk_spell.innerHTML = this.lang_chck_spell;
    var spell_img = null;
    if(this.show_spell_img)
        spell_img = AJS.IMG({'src': '/static/webclient/image/googiespell/spellc.gif'});
    return AJS.SPAN(spell_img, " ", chk_spell);
}


//////
// State functions
/////
GoogieSpell.prototype.flashNoSpellingErrorState = function(on_finish) {
    var no_spell_errors;

    if(on_finish) {
        var fn = function() {
            on_finish();
            this.checkSpellingState();
        };
        no_spell_errors = fn;
    }
    else
        no_spell_errors = this.checkSpellingState;

    this.setStateChanged("no_error_found");

    if(this.main_controller) {
        AJS.hideElement(this.switch_lan_pic);

        var dummy = AJS.IMG({'src': '/static/webclient/image/googiespell/blank.gif', 'style': 'height: 16px; width: 1px;'});
        var rsm = AJS.SPAN();
        rsm.innerHTML = this.lang_no_error_found;

        AJS.RCN(this.spell_span, AJS.SPAN(dummy, rsm));

        this.spell_span.className = "googie_check_spelling_ok";
        this.spell_span.style.textDecoration = "none";
        this.spell_span.style.cursor = "default";

        AJS.callLater(AJS.$b(no_spell_errors, this), 1200, [false]);
    }
}

GoogieSpell.prototype.resumeEditingState = function() {
    this.setStateChanged("resume_editing");

    //Change link text to resume
    if(this.main_controller) {
        AJS.hideElement(this.switch_lan_pic);
        var dummy = AJS.IMG({'src': '/static/webclient/image/googiespell/blank.gif', 'style': 'height: 16px; width: 1px;'});
        var rsm = AJS.SPAN();
        rsm.innerHTML = this.lang_rsm_edt;
        AJS.RCN(this.spell_span, AJS.SPAN(dummy, rsm));
    
        var fn = function(e) {
            this.resumeEditing();
        }
        this.spell_span.onclick = AJS.$b(fn, this);

        this.spell_span.className = "googie_resume_editing";
    }

    try { this.edit_layer.scrollTop = this.ta_scroll_top; }
    catch(e) { }
}

GoogieSpell.prototype.checkSpellingState = function(fire) {
    if(!AJS.isDefined(fire) || fire)
        this.setStateChanged("spell_check");

    if(this.show_change_lang_pic)
        this.switch_lan_pic = this.createChangeLangPic();
    else
        this.switch_lan_pic = AJS.SPAN();

    var span_chck = this.createSpellDiv();
    var fn = function() {
        this.spellCheck();
    };

    if(this.custom_spellcheck_starter)
        span_chck.onclick = this.custom_spellcheck_starter;
    else {
        span_chck.onclick = AJS.$b(fn, this);
    }

    this.spell_span = span_chck;
    if(this.main_controller) {
        if(this.change_lang_pic_placement == "left")
            AJS.RCN(this.spell_container, span_chck, " ", this.switch_lan_pic);
        else
            AJS.RCN(this.spell_container, this.switch_lan_pic, " ", span_chck);
    }
}


//////
// Misc. functions
/////
GoogieSpell.item_onmouseover = function(e) {
    var elm = AJS.getEventElm(e);
    if(elm.className != "googie_list_revert" && elm.className != "googie_list_close")
        elm.className = "googie_list_onhover";
    else
        elm.parentNode.className = "googie_list_onhover";
}
GoogieSpell.item_onmouseout = function(e) {
    var elm = AJS.getEventElm(e);
    if(elm.className != "googie_list_revert" && elm.className != "googie_list_close")
        elm.className = "googie_list_onout";
    else
        elm.parentNode.className = "googie_list_onout";
}

GoogieSpell.prototype.createCloseButton = function(c_fn) {
    return this.createButton(this.lang_close, 'googie_list_close', AJS.$b(c_fn, this));
}

GoogieSpell.prototype.createButton = function(name, css_class, c_fn) {
    var btn_row = AJS.TR();
    var btn = AJS.TD();

    btn.onmouseover = GoogieSpell.item_onmouseover;
    btn.onmouseout = GoogieSpell.item_onmouseout;

    var spn_btn;
    if(css_class != "") {
        spn_btn = AJS.SPAN({'class': css_class});
        spn_btn.innerHTML = name;
    }
    else {
        spn_btn = AJS.TN(name);
    }
    btn.appendChild(spn_btn);
    AJS.AEV(btn, "click", c_fn);
    btn_row.appendChild(btn);

    return btn_row;
}

GoogieSpell.prototype.removeIndicator = function(elm) {
    try { AJS.removeElement(this.indicator); }
    catch(e) {}
}

GoogieSpell.prototype.appendIndicator = function(elm) {
    var img = AJS.IMG({'src': '/static/webclient/image/googiespell/indicator.gif', 'style': 'margin-right: 5px;'});
    AJS.setWidth(img, 16);
    AJS.setHeight(img, 16);
    this.indicator = img;
    img.style.textDecoration = "none";
    try {
        AJS.insertBefore(img, elm);
    }
    catch(e) {}
}

GoogieSpell.prototype.createFocusLink = function(name) {
    return AJS.A({'href': 'javascript:;', name: name});
}
