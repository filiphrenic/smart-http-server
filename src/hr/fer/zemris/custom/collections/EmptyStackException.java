package hr.fer.zemris.custom.collections;

/**
 * This class is a form of <code>Throwable</code> that indicates conditions that a reasonable application might want to catch.
 * @author Filip Hrenić
 * @version 1.0
 */
public class EmptyStackException extends RuntimeException{

	private static final long serialVersionUID = 5019575938076748881L;
	
	/**
	 * Constructs a new empty stack exception.
	 */
	public EmptyStackException(){
		super();
	}
	
	/**
	 * Constructs a new empty stack exception with specified detail message.
	 * @param message the detail message of exception
	 */
	public EmptyStackException(String message){
		super(message);
	}
	
	

}
