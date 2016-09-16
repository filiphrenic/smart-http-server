package hr.fer.zemris.custom.scripting.tokens;

/**
 * This class is used for storing function tokens inside of echo and forloop nodes.
 * @author Filip Hrenić
 * @version 1.0
 */
public class TokenFunction extends Token {

	private final String name;

	/**
	 * Setter method for name property.
	 * Given name must be a string starting with a letter which can be followed by letters, numbers and underscores.
	 * @param name desired name of the token
	 */
	public TokenFunction( String name ){
		this.name = name;
	}

	/**
	 * Getter method for name property.
	 * @return name of the token
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * Returns the name of the token preceded by a @ sign.
	 * @return the name
	 */
	@Override
	public String asText(){
		return "@" + this.name;
	}
}
