def goodeval(fn, fnList, *args):
	if(fn in fnList):
		callFn = fn+'(';
		for argCnt in range(0,len(args)):
			callFn = callFn+str(args[argCnt])
			if(argCnt<len(args)-1):
				callFn = callFn+',';
		callFn = callFn+')';
		print callFn
		exec(callFn);
	
			
			