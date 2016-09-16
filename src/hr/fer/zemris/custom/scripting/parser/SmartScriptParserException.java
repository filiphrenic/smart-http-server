package hr.fer.zemris.custom.scripting.parser;

/**
 * This class is a form of <code>Throwable</code> that indicates conditions that a {@link SmartScriptParser} might want to catch.
 * It is thrown when there's an error in document parsing.
 * @author Filip Hrenić
 * @version 1.0
 */
public class SmartScriptParserException extends RuntimeException {

	private static final long serialVersionUID = -7146405271722626823L;
	
	/**
	 * Constructs a new exception with <code>null</code> as its detail message.
	 */
	public SmartScriptParserException(){
		super();
	}

	/**
	 * Constructs a new exception with a specified detail message.
	 * @param message the detail message
	 */
	public SmartScriptParserException( String message ){
		super( message );
	}
	
}
