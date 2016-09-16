package hr.fer.zemris.custom.collections;

/**
 * This class is a Adaptor that adapts methods of {@link ArrayBackedIndexedCollection} (adaptee) for easier use.
 * We adapt the {@link ArrayBackedIndexedCollection} class for easier use.
 * @author Filip Hrenić
 * @version 1.0
 */
public class ObjectStack {

	private final ArrayBackedIndexedCollection abic;
	
	/**
	 * Constructs a new empty ObjectStack.
	 */
	public ObjectStack(){
		this.abic = new ArrayBackedIndexedCollection();
	}
	
	/**
	 * Constructs a new empty ObjectStack with capacity of initialCapacity.
	 * @param initialCapacity capacity of the ObjectStack
	 */
	public ObjectStack( int initialCapacity ){
		this.abic = new ArrayBackedIndexedCollection( initialCapacity );
	}
	
	/**
	 * Returns true if the stack is empty.
	 * @return true if empty
	 */
	public boolean isEmpty(){
		return this.abic.isEmpty();
	}
	
	/**
	 * Returns the number of elements in stack.
	 * @return the number of elements in stack
	 */
	public int size(){
		return this.abic.size();
	}
	
	/**
	 * Pushes an item onto the top of this stack.
	 * @param value value to be pushed onto this stack
	 * @throws IllegalArgumentException if you try to add null to the stack
	 */
	public void push( Object value ){
		if ( value == null ) throw new IllegalArgumentException("You can't add null to the stack.");
		this.abic.add(value);
	}
	
	/**
	 * Removes the object at the top of this stack and returns that object as the value of this function.
	 * @return the object at the top of this stack
	 * @throws EmptyStackException if we try to pop from empty stack.
	 */
	public Object pop(){
		int s = this.abic.size();
		if( s == 0 ) throw new EmptyStackException("Stack is already empty.");
		
		Object top = this.abic.get( s-1 );
		this.abic.remove(s-1);
		return top;
	}
	
	/**
	 * Retrurns the object at the top of this stack.
	 * @return the object at the top of this stack
	 */
	public Object peek(){
		return this.abic.get( this.abic.size()-1 );
	}
	
	/**
	 * Clears all elements in the stack.
	 */
	public void clear(){
		this.abic.clear();
	}	
}
