package hr.fer.zemris.custom.collections;

/**
 * ArrayBackedIndexCollection (referred to as ABIC)
 * This class contains various methods for manipulating a "custom made" array.
 * @author Filip Hrenić
 * @version 1.0
 */
public class ArrayBackedIndexedCollection {
	
	final static private int DEFAULT_CAPACITY = 16;
	
	private int size;
	private int capacity;
	private Object[] elements;
	
	/**
	 * Constructs a new empty ABIC with initial capacity = DEFAULT_CAPACITY.
	 */
	public ArrayBackedIndexedCollection(){
		this( DEFAULT_CAPACITY );
	}
		
	/**
	 * Constructs a new empty ABIC with initial capacity = initialCapacity.
	 * @param initialCapacity initial capacity of an ABIC
	 * @throws IllegalArgumentException if the given capacity is less than 1
	 */
	public ArrayBackedIndexedCollection( int initialCapacity ){
		if ( initialCapacity<1 ) throw new IllegalArgumentException("Capacity must be at least 1.");
		this.size = 0;
		this.capacity = initialCapacity;
		this.elements = new Object[ initialCapacity ];
	}
	
	/**
	 * Returns true if ABIC is empty.
	 * @return true if empty
	 */
	public boolean isEmpty(){
		return ( this.size == 0 );
	}
	
	/**
	 * Returns the number of elements in ABIC.
	 * @return the number of elements in ABIC
	 */
	public int size(){
		return this.size;
	}
	
	/**
	 * Adds value to the end of the ABIC
	 * @param value value that is added
	 * @throws IndexOutOfBoundsException Index is not valid.
	 */
	public void add( Object value ){
		this.insert( value, this.size );
	}
	
	/**
	 * Returns the object at given index.
	 * @param index index at which we want to get the object
	 * @return object that is indexed with index in ABIC 
	 * @throws IndexOutOfBoundsException if index is less than 0 or greater than current size
	 */
	public Object get( int index ){
		
		if ( index<0 || index>=this.size ) throw new IndexOutOfBoundsException("Index is not valid.");
		return this.elements[ index ];
		
	}
	
	/**
	 * Removes a object at given index. Also shifts all elements that are after given index to the left.
	 * @param index index of a element which we want to remove
	 * @throws IndexOutOfBoundsException if index is less than 0 or greater than current size
	 */
	public void remove( int index ){
		
		if ( index<0 || index>=this.size ) throw new IndexOutOfBoundsException("Index is not valid.");
		
		this.shift( index, 1 );
		
		this.size--;
		
	}
	
	/**
	 * Inserts a element at given index if possible. Shifts all elements from that index to the right.
	 * @param value value of the element to be inserted.
	 * @param index index at which we try to insert the element
	 * @throws IndexOutOfBoundsException if index is less than 0 or greater than current size
	 * @throws IllegalArgumentException if you try to add null value
	 */
	private void insert(Object value, int index){
		
		if ( value == null ) throw new IllegalArgumentException("You can't add null to the ABIC.");
		if ( index<0 || index>this.size ) throw new IndexOutOfBoundsException("Index is not valid");
		if ( this.size == this.capacity ) this.reallocate();
		
		this.shift( index, -1 );
		
		this.elements[ index ] = value;
		
		this.size++;
		
	}
	
	/**
	 * Returns the index of given object. If there isn't such object in the ABIC, returns -1.
	 * @param value searched object
	 * @return -1 if there is no such object, otherwise its index
	 */
	private int indexOf(Object value){
		for( int i=0; i<this.size; i++ ){
			if ( this.elements[i] == value ) return i;
		}
		return -1;
	}
	
	/**
	 * Test to see if ABIC contains given object.
	 * @param value searched object
	 * @return true if there is such object, false otherwise
	 */
	public boolean contains( Object value ){
		return this.indexOf(value) > -1;
	}
	
	/**
	 * Sets the ABIC to it's default settings.
	 */
	public void clear(){
		this.size = 0;
		this.capacity = DEFAULT_CAPACITY;
		this.elements = new Object[ this.capacity ];
	}
	
	/**
	 * Shifts the right part of the ABIC in the direction. ABIC is "partitioned" with the given index.
	 * @param index index from which we want to shift everything
	 * @param direction 1 = shift left, -1 = shift right
	 */
	private void shift( int index, int direction ){
		/* direction = -1 -> add
		 * direction = 1  -> remove
		 * if we are adding,   then we position to the rightmost index (size) so we can shift everything to the right
		 * if we are removing, then we position to the index at which we have to remove the object and simply shift everything to the left
		 */
		
		
		int howMuchTimes = this.size - index; // how much times do we shift the objects
		if ( direction == -1 )   // if adding
			index = this.size;
		else 				     // if removing
			howMuchTimes--; // we don't shift the removed
		
		while( howMuchTimes-->0 ){
			this.elements[ index ] = this.elements[ index+direction ];
			index += direction;
		}
		
	}
	
	/**
	 * Doubles the capacity of ABIC if there is no room for even one more object.
	 */
	private void reallocate(){
		this.capacity *= 2;
		Object[] newElements = new Object[ this.capacity ];
		System.arraycopy(elements, 0, newElements, 0, this.size);
		elements = newElements;
	}	
}
