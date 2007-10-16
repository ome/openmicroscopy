package xmlMVC;

import java.util.Iterator;
import java.util.Stack;

// iterator used for traversing whole data structure tree.
// used for eg setting exp-variables editable or disabled.

public class DataFieldIterator implements Iterator {

	Stack<Iterator> stack = new Stack<Iterator>();
	
	public DataFieldIterator(Iterator iterator) {
		stack.push(iterator);
	}
	
	public Object next() {
		if (hasNext()) {
			Iterator iterator = stack.peek();
			DataFieldNode node = (DataFieldNode) iterator.next();
			
			stack.push(node.createIterator());
			
			return node;
		} else {
			return null;
		}
	}
	
	public boolean hasNext() {
		if (stack.empty()) {
			return false;
		} else {
			Iterator iterator = stack.peek();
			if (!iterator.hasNext()) {
				stack.pop();
				return hasNext();
			} else {
				return true;
			}
		}
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
