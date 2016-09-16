package hr.fer.zemris.custom.scripting.nodes;

/**
 * This class is used to represent a piece of text data.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class TextNode extends Node {

    private final String text;

    /**
     * Constructs a new text node with given text.
     * 
     * @param text value of the text node
     */
    public TextNode(String text) {
        this.text = text;
    }

    /**
     * Getter method for the text property.
     * 
     * @return value of text
     */
    public String getValue() {
        return this.text;
    }

    /**
     * Given visitor visits this node.
     * 
     * @param visitor
     */
    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitTextNode(this);
    }

    /**
     * Returns the text as it is.
     * 
     * @return value of text
     */
    @Override
    public String asText() {
        return this.text;
    }

}
