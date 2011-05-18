/**
* jQuery AOP - jQuery plugin to add features of aspect-oriented programming (AOP) to jQuery.
* http://jquery-aop.googlecode.com/
*
* Licensed under the MIT license:
* http://www.opensource.org/licenses/mit-license.php
*
* Version: 1.3
*
* Cross-frame type detection based on Daniel Steigerwald's code (http://daniel.steigerwald.cz)
* http://gist.github.com/204554
*
*/

(function() {

	var _after			= 1;
	var _afterThrow		= 2;
	var _afterFinally	= 3;
	var _before			= 4;
	var _around			= 5;
	var _intro			= 6;
	var _regexEnabled = true;
	var _arguments = 'arguments';
	var _undef = 'undefined';

	var getType = (function() {
	 
		var toString = Object.prototype.toString,
			toStrings = {},
			nodeTypes = { 1: 'element', 3: 'textnode', 9: 'document', 11: 'fragment' },
			types = 'Arguments Array Boolean Date Document Element Error Fragment Function NodeList Null Number Object RegExp String TextNode Undefined Window'.split(' ');
	 
		for (var i = types.length; i--; ) {
			var type = types[i], constructor = window[type];
			if (constructor) {
				try { toStrings[toString.call(new constructor)] = type.toLowerCase(); }
				catch (e) { }
			}
		}
	 
		return function(item) {
			return item == null && (item === undefined ? _undef : 'null') ||
				item.nodeType && nodeTypes[item.nodeType] ||
				typeof item.length == 'number' && (
					item.callee && _arguments ||
					item.alert && 'window' ||
					item.item && 'nodelist') ||
				toStrings[toString.call(item)];
		};
	 
	})();

	var isFunc = function(obj) { return getType(obj) == 'function'; };

	/**
	 * Private weaving function.
	 */
	var weaveOne = function(source, method, advice) {

		var old = source[method];

		// Work-around IE6/7 behavior on some native method that return object instances
		if (advice.type != _intro && !isFunc(old)) {
			var oldObject = old;
			old = function() {
				var code = arguments.length > 0 ? _arguments + '[0]' : '';

				for (var i=1;i<arguments.length;i++) {
					code += ',' + _arguments + '[' + i + ']';
				}

				return eval('oldObject(' + code + ');');
			};
		}

		var aspect;
		if (advice.type == _after || advice.type == _afterThrow || advice.type == _afterFinally)
			aspect = function() {
				var returnValue, exceptionThrown = null;

				try {
					returnValue = old.apply(this, arguments);
				} catch (e) {
					exceptionThrown = e;
				}

				if (advice.type == _after)
					if (exceptionThrown == null)
						returnValue = advice.value.apply(this, [returnValue, method]);
					else
						throw exceptionThrown;
				else if (advice.type == _afterThrow && exceptionThrown != null)
					returnValue = advice.value.apply(this, [exceptionThrown, method]);
				else if (advice.type == _afterFinally)
					returnValue = advice.value.apply(this, [returnValue, exceptionThrown, method]);

				return returnValue;
			};
		else if (advice.type == _before)
			aspect = function() {
				advice.value.apply(this, [arguments, method]);
				return old.apply(this, arguments);
			};
		else if (advice.type == _intro)
			aspect = function() {
				return advice.value.apply(this, arguments);
			};
		else if (advice.type == _around) {
			aspect = function() {
				var invocation = { object: this, args: Array.prototype.slice.call(arguments) };
				return advice.value.apply(invocation.object, [{ arguments: invocation.args, method: method, proceed : 
					function() {
						return old.apply(invocation.object, invocation.args);
					}
				}] );
			};
		}

		aspect.unweave = function() { 
			source[method] = old;
			pointcut = source = aspect = old = null;
		};

		source[method] = aspect;

		return aspect;

	};

	/**
	 * Private method search
	 */
	var search = function(source, pointcut, advice) {
		
		var methods = [];

		for (var method in source) {

			var item = null;

			// Ignore exceptions during method retrival
			try {
				item = source[method];
			}
			catch (e) { }

			if (item != null && method.match(pointcut.method) && isFunc(item))
				methods[methods.length] = { source: source, method: method, advice: advice };

		}

		return methods;
	};

	/**
	 * Private weaver and pointcut parser.
	 */
	var weave = function(pointcut, advice) {

		var source = typeof(pointcut.target.prototype) != _undef ? pointcut.target.prototype : pointcut.target;
		var advices = [];

		// If it's not an introduction and no method was found, try with regex...
		if (advice.type != _intro && typeof(source[pointcut.method]) == _undef) {

			// First try directly on target
			var methods = search(pointcut.target, pointcut, advice);

			// No method found, re-try directly on prototype
			if (methods.length == 0)
				methods = search(source, pointcut, advice);

			for (var i in methods)
				advices[advices.length] = weaveOne(methods[i].source, methods[i].method, methods[i].advice);

		} 
		else
		{
			// Return as an array of one element
			advices[0] = weaveOne(source, pointcut.method, advice);
		}

		return _regexEnabled ? advices : advices[0];

	};

	jQuery.aop = 
	{
		/**
		 * Creates an advice after the defined point-cut. The advice will be executed after the point-cut method 
		 * has completed execution successfully, and will receive one parameter with the result of the execution.
		 * This function returns an array of weaved aspects (Function).
		 *
		 * @example jQuery.aop.after( {target: window, method: 'MyGlobalMethod'}, function(result) { 
		 *                alert('Returned: ' + result); 
		 *                return result;
		 *          } );
		 * @result Array<Function>
		 *
		 * @example jQuery.aop.after( {target: String, method: 'indexOf'}, function(index) { 
		 *                alert('Result found at: ' + index + ' on:' + this); 
		 *                return index;
		 *          } );
		 * @result Array<Function>
		 *
		 * @name after
		 * @param Map pointcut Definition of the point-cut to apply the advice. A point-cut is the definition of the object/s and method/s to be weaved.
		 * @option Object target Target object to be weaved. 
		 * @option String method Name of the function to be weaved. Regex are supported, but not on built-in objects.
		 * @param Function advice Function containing the code that will get called after the execution of the point-cut. It receives one parameter
		 *                        with the result of the point-cut's execution. The function can choose to return this same value or a different one.
		 *
		 * @type Array<Function>
		 * @cat Plugins/General
		 */
		after : function(pointcut, advice)
		{
			return weave( pointcut, { type: _after, value: advice } );
		},

		/**
		 * Creates an advice after the defined point-cut only for unhandled exceptions. The advice will be executed 
		 * after the point-cut method only if the execution failed and an exception has been thrown. It will receive one 
		 * parameter with the exception thrown by the point-cut method.
		 * This function returns an array of weaved aspects (Function).
		 *
		 * @example jQuery.aop.afterThrow( {target: String, method: 'indexOf'}, function(exception) { 
		 *                alert('Unhandled exception: ' + exception); 
		 *                return -1;
		 *          } );
		 * @result Array<Function>
		 *
		 * @example jQuery.aop.afterThrow( {target: calculator, method: 'Calculate'}, function(exception) { 
		 *                console.log('Unhandled exception: ' + exception);
		 *                throw exception;
		 *          } );
		 * @result Array<Function>
		 *
		 * @name afterThrow
		 * @param Map pointcut Definition of the point-cut to apply the advice. A point-cut is the definition of the object/s and method/s to be weaved.
		 * @option Object target Target object to be weaved. 
		 * @option String method Name of the function to be weaved. Regex are supported, but not on built-in objects.
		 * @param Function advice Function containing the code that will get called after the execution of the point-cut. It receives one parameter
		 *                        with the exception thrown by the point-cut method.
		 *
		 * @type Array<Function>
		 * @cat Plugins/General
		 */
		afterThrow : function(pointcut, advice)
		{
			return weave( pointcut, { type: _afterThrow, value: advice } );
		},

		/**
		 * Creates an advice after the defined point-cut. The advice will be executed after the point-cut method 
		 * regardless of its success or failure, and it will receive two parameters: one with the 
		 * result of a successful execution or null, and another one with the exception thrown or null.
		 * This function returns an array of weaved aspects (Function).
		 *
		 * @example jQuery.aop.afterFinally( {target: window, method: 'MyGlobalMethod'}, function(result, exception) {
		 *                if (exception == null)
		 *                    return 'Returned: ' + result;
		 *                else
		 *                    return 'Unhandled exception: ' + exception;
		 *          } );
		 * @result Array<Function>
		 *
		 * @name afterFinally
		 * @param Map pointcut Definition of the point-cut to apply the advice. A point-cut is the definition of the object/s and method/s to be weaved.
		 * @option Object target Target object to be weaved. 
		 * @option String method Name of the function to be weaved. Regex are supported, but not on built-in objects.
		 * @param Function advice Function containing the code that will get called after the execution of the point-cut regardless of its success or failure.
		 *                        It receives two parameters, the first one with the result of a successful execution or null, and the second one with the 
		 *                        exception or null.
		 *
		 * @type Array<Function>
		 * @cat Plugins/General
		 */
		afterFinally : function(pointcut, advice)
		{
			return weave( pointcut, { type: _afterFinally, value: advice } );
		},


		/**
		 * Creates an advice before the defined point-cut. The advice will be executed before the point-cut method 
		 * but cannot modify the behavior of the method, or prevent its execution.
		 * This function returns an array of weaved aspects (Function).
		 *
		 * @example jQuery.aop.before( {target: window, method: 'MyGlobalMethod'}, function() { 
		 *                alert('About to execute MyGlobalMethod'); 
		 *          } );
		 * @result Array<Function>
		 *
		 * @example jQuery.aop.before( {target: String, method: 'indexOf'}, function(index) {
		 *                alert('About to execute String.indexOf on: ' + this);
		 *          } );
		 * @result Array<Function>
		 *
		 * @name before
		 * @param Map pointcut Definition of the point-cut to apply the advice. A point-cut is the definition of the object/s and method/s to be weaved.
		 * @option Object target Target object to be weaved. 
		 * @option String method Name of the function to be weaved. Regex are supported, but not on built-in objects.
		 * @param Function advice Function containing the code that will get called before the execution of the point-cut.
		 *
		 * @type Array<Function>
		 * @cat Plugins/General
		 */
		before : function(pointcut, advice)
		{
			return weave( pointcut, { type: _before, value: advice } );
		},


		/**
		 * Creates an advice 'around' the defined point-cut. This type of advice can control the point-cut method execution by calling
		 * the functions '.proceed()' on the 'invocation' object, and also, can modify the arguments collection before sending them to the function call.
		 * This function returns an array of weaved aspects (Function).
		 *
		 * @example jQuery.aop.around( {target: window, method: 'MyGlobalMethod'}, function(invocation) {
		 *                alert('# of Arguments: ' + invocation.arguments.length); 
		 *                return invocation.proceed(); 
		 *          } );
		 * @result Array<Function>
		 *
		 * @example jQuery.aop.around( {target: String, method: 'indexOf'}, function(invocation) { 
		 *                alert('Searching: ' + invocation.arguments[0] + ' on: ' + this); 
		 *                return invocation.proceed(); 
		 *          } );
		 * @result Array<Function>
		 *
		 * @example jQuery.aop.around( {target: window, method: /Get(\d+)/}, function(invocation) {
		 *                alert('Executing ' + invocation.method); 
		 *                return invocation.proceed(); 
		 *          } );
		 * @desc Matches all global methods starting with 'Get' and followed by a number.
		 * @result Array<Function>
		 *
		 *
		 * @name around
		 * @param Map pointcut Definition of the point-cut to apply the advice. A point-cut is the definition of the object/s and method/s to be weaved.
		 * @option Object target Target object to be weaved. 
		 * @option String method Name of the function to be weaved. Regex are supported, but not on built-in objects.
		 * @param Function advice Function containing the code that will get called around the execution of the point-cut. This advice will be called with one
		 *                        argument containing one function '.proceed()', the collection of arguments '.arguments', and the matched method name '.method'.
		 *
		 * @type Array<Function>
		 * @cat Plugins/General
		 */
		around : function(pointcut, advice)
		{
			return weave( pointcut, { type: _around, value: advice } );
		},

		/**
		 * Creates an introduction on the defined point-cut. This type of advice replaces any existing methods with the same
		 * name. To restore them, just unweave it.
		 * This function returns an array with only one weaved aspect (Function).
		 *
		 * @example jQuery.aop.introduction( {target: window, method: 'MyGlobalMethod'}, function(result) {
		 *                alert('Returned: ' + result);
		 *          } );
		 * @result Array<Function>
		 *
		 * @example jQuery.aop.introduction( {target: String, method: 'log'}, function() {
		 *                alert('Console: ' + this);
		 *          } );
		 * @result Array<Function>
		 *
		 * @name introduction
		 * @param Map pointcut Definition of the point-cut to apply the advice. A point-cut is the definition of the object/s and method/s to be weaved.
		 * @option Object target Target object to be weaved. 
		 * @option String method Name of the function to be weaved.
		 * @param Function advice Function containing the code that will be executed on the point-cut. 
		 *
		 * @type Array<Function>
		 * @cat Plugins/General
		 */
		introduction : function(pointcut, advice)
		{
			return weave( pointcut, { type: _intro, value: advice } );
		},
		
		/**
		 * Configures global options.
		 *
		 * @name setup
		 * @param Map settings Configuration options.
		 * @option Boolean regexMatch Enables/disables regex matching of method names.
		 *
		 * @example jQuery.aop.setup( { regexMatch: false } );
		 * @desc Disable regex matching.
		 *
		 * @type Void
		 * @cat Plugins/General
		 */
		setup: function(settings)
		{
			_regexEnabled = settings.regexMatch;
		}
	};

})();
