#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#


import datetime
import traceback
import logging

from django.conf import settings
from django import template

register = template.Library()

logger = logging.getLogger('custom_tags')

#==============================================================================
class BaseCalc(object):
    """
    src: http://djangosnippets.org/snippets/1350/
    A smarter {% if %} tag for django templates.

    While retaining current Django functionality, it also handles equality,
    greater than and less than operators. Some common case examples::

        {% if articles|length >= 5 %}...{% endif %}
        {% if "ifnotequal tag" != "beautiful" %}...{% endif %}
    """
    
    def __init__(self, var1, var2=None, negate=False):
        self.var1 = var1
        self.var2 = var2
        self.negate = negate

    def resolve(self, context):
        try:
            var1, var2 = self.resolve_vars(context)
            outcome = self.calculate(var1, var2)
        except:
            outcome = False
        if self.negate:
            return not outcome
        return outcome

    def resolve_vars(self, context):
        var2 = self.var2 and self.var2.resolve(context)
        return self.var1.resolve(context), var2

    def calculate(self, var1, var2):
        raise NotImplementedError()


class Or(BaseCalc):
    def calculate(self, var1, var2):
        return var1 or var2


class And(BaseCalc):
    def calculate(self, var1, var2):
        return var1 and var2


class Equals(BaseCalc):
    def calculate(self, var1, var2):
        return var1 == var2


class Greater(BaseCalc):
    def calculate(self, var1, var2):
        return var1 > var2


class GreaterOrEqual(BaseCalc):
    def calculate(self, var1, var2):
        return var1 >= var2


class In(BaseCalc):
    def calculate(self, var1, var2):
        return var1 in var2

#==============================================================================
OPERATORS = {
    '=': (Equals, True),
    '==': (Equals, True),
    '!=': (Equals, False),
    '>': (Greater, True),
    '>=': (GreaterOrEqual, True),
    '<=': (Greater, False),
    '<': (GreaterOrEqual, False),
    'or': (Or, True),
    'and': (And, True),
    'in': (In, True),
}
BOOL_OPERATORS = ('or', 'and')


class IfParser(object):
    error_class = ValueError

    def __init__(self, tokens):
        self.tokens = tokens

    def _get_tokens(self):
        return self._tokens

    def _set_tokens(self, tokens):
        self._tokens = tokens
        self.len = len(tokens)
        self.pos = 0

    tokens = property(_get_tokens, _set_tokens)

    def parse(self):
        if self.at_end():
            raise self.error_class('No variables provided.')
        var1 = self.get_bool_var()
        while not self.at_end():
            op, negate = self.get_operator()
            var2 = self.get_bool_var()
            var1 = op(var1, var2, negate=negate)
        return var1

    def get_token(self, eof_message=None, lookahead=False):
        negate = True
        token = None
        pos = self.pos
        while token is None or token == 'not':
            if pos >= self.len:
                if eof_message is None:
                    raise self.error_class()
                raise self.error_class(eof_message)
            token = self.tokens[pos]
            negate = not negate
            pos += 1
        if not lookahead:
            self.pos = pos
        return token, negate

    def at_end(self):
        return self.pos >= self.len

    def create_var(self, value):
        return TestVar(value)

    def get_bool_var(self):
        """
        Returns either a variable by itself or a non-boolean operation (such as
        ``x == 0`` or ``x < 0``).

        This is needed to keep correct precedence for boolean operations (i.e.
        ``x or x == 0`` should be ``x or (x == 0)``, not ``(x or x) == 0``).
        """
        var = self.get_var()
        if not self.at_end():
            op_token = self.get_token(lookahead=True)[0]
            if isinstance(op_token, basestring) and (op_token not in
                                                     BOOL_OPERATORS):
                op, negate = self.get_operator()
                return op(var, self.get_var(), negate=negate)
        return var

    def get_var(self):
        token, negate = self.get_token('Reached end of statement, still '
                                       'expecting a variable.')
        if isinstance(token, basestring) and token in OPERATORS:
            raise self.error_class('Expected variable, got operator (%s).' %
                                   token)
        var = self.create_var(token)
        if negate:
            return Or(var, negate=True)
        return var

    def get_operator(self):
        token, negate = self.get_token('Reached end of statement, still '
                                       'expecting an operator.')
        if not isinstance(token, basestring) or token not in OPERATORS:
            raise self.error_class('%s is not a valid operator.' % token)
        if self.at_end():
            raise self.error_class('No variable provided after "%s".' % token)
        op, true = OPERATORS[token]
        if not true:
            negate = not negate
        return op, negate

class TemplateIfParser(IfParser):
    error_class = template.TemplateSyntaxError

    def __init__(self, parser, *args, **kwargs):
        self.template_parser = parser
        return super(TemplateIfParser, self).__init__(*args, **kwargs)

    def create_var(self, value):
        return self.template_parser.compile_filter(value)


class SmartIfNode(template.Node):
    def __init__(self, var, nodelist_true, nodelist_false=None):
        self.nodelist_true, self.nodelist_false = nodelist_true, nodelist_false
        self.var = var

    def render(self, context):
        if self.var.resolve(context):
            return self.nodelist_true.render(context)
        if self.nodelist_false:
            return self.nodelist_false.render(context)
        return ''

    def __repr__(self):
        return "<Smart If node>"

    def __iter__(self):
        for node in self.nodelist_true:
            yield node
        if self.nodelist_false:
            for node in self.nodelist_false:
                yield node

    def get_nodes_by_type(self, nodetype):
        nodes = []
        if isinstance(self, nodetype):
            nodes.append(self)
        nodes.extend(self.nodelist_true.get_nodes_by_type(nodetype))
        if self.nodelist_false:
            nodes.extend(self.nodelist_false.get_nodes_by_type(nodetype))
        return nodes

@register.tag('if')
def smart_if(parser, token):
    """
    A smarter {% if %} tag for django templates.

    While retaining current Django functionality, it also handles equality,
    greater than and less than operators. Some common case examples::

        {% if articles|length >= 5 %}...{% endif %}
        {% if "ifnotequal tag" != "beautiful" %}...{% endif %}

    Arguments and operators _must_ have a space between them, so
    ``{% if 1>2 %}`` is not a valid smart if tag.

    All supported operators are: ``or``, ``and``, ``in``, ``=`` (or ``==``),
    ``!=``, ``>``, ``>=``, ``<`` and ``<=``.
    """
    bits = token.split_contents()[1:]
    var = TemplateIfParser(parser, bits).parse()
    nodelist_true = parser.parse(('else', 'endif'))
    token = parser.next_token()
    if token.contents == 'else':
        nodelist_false = parser.parse(('endif',))
        parser.delete_first_token()
    else:
        nodelist_false = None
    return SmartIfNode(var, nodelist_true, nodelist_false)

#==============================================================================

@register.filter
def hash(value, key):
    return value[key]

@register.filter
def truncateafter(value, arg):
    """
    Truncates a string after a given number of chars  
    Argument: Number of chars to truncate after
    """
    try:
        length = int(arg)
    except ValueError: # invalid literal for int()
        return value # Fail silently.
    if not isinstance(value, basestring):
        value = str(value)
    if (len(value) > length):
        return value[:length] + "..."
    else:
        return value

@register.filter
def truncatebefor(value, arg):
    """
    Truncates a string after a given number of chars  
    Argument: Number of chars to truncate befor
    """
    try:
        length = int(arg)
    except ValueError: # invalid literal for int()
        return value # Fail silently.
    if not isinstance(value, basestring):
        value = str(value)
    if (len(value) > length):
        return "..."+value[len(value)-length:]
    else:
        return value

@register.filter
def warphtml(value, arg):
    try:
        length = int(arg)
    except ValueError: # invalid literal for int()
        return value # Fail silently.
    
    if not isinstance(value, basestring):
        value = str(value)  
    try: 
        l = len(value) 
        if l < length: 
            return value
        elif l >= length: 
            splited = [] 
            for v in range(0,len(value),length): 
                splited.append(value[v:v+length]+"\n") 
            return "".join(splited) 
    except Exception, x:
        logger.error(traceback.format_exc())
        return value

@register.filter
def shortening(value, arg):
    try:
        length = int(arg)
    except ValueError: # invalid literal for int()
        return value # Fail silently.
    front = length/2-3
    end = length/2-3
    
    if not isinstance(value, basestring):
        value = str(value)  
    try: 
        l = len(value) 
        if l < length: 
            return value
        elif l >= length: 
            return value[:front]+"..."+value[l-end:]
    except Exception, x:
        logger.error(traceback.format_exc())
        return value

# makes settings available in template
@register.tag
def setting ( parser, token ): 
    try:
        tag_name, option = token.split_contents()
    except ValueError:
        raise template.TemplateSyntaxError, "%r tag requires a single argument" % token.contents[0]
    return SettingNode( option )

class SettingNode ( template.Node ): 
    def __init__ ( self, option ): 
        self.option = option

    def render ( self, context ): 
        # if FAILURE then FAIL silently
        try:
            return str(settings.__getattr__(self.option))
        except:
            return ""

class PluralNode(template.Node):
    def __init__(self, quantity, single, plural):
        self.quantity = template.Variable(quantity)
        self.single = template.Variable(single)
        self.plural = template.Variable(plural)

    def render(self, context):
        if self.quantity.resolve(context) == 1:
            return u'%s' % self.single.resolve(context)
        else:
            return u'%s' % self.plural.resolve(context)

@register.tag(name="plural")
def do_plural(parser, token):
    """
    Usage: {% plural quantity name_singular name_plural %}

    This simple version only works with template variable since we will use blocktrans for strings.
    """
    
    try:
        # split_contents() knows not to split quoted strings.
        tag_name, quantity, single, plural = token.split_contents()
    except ValueError:
        raise template.TemplateSyntaxError, "%r tag requires exactly three arguments" % token.contents.split()[0]

    return PluralNode(quantity, single, plural)
