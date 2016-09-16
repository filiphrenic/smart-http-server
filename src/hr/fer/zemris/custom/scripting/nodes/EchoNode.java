package hr.fer.zemris.custom.scripting.nodes;

import hr.fer.zemris.custom.scripting.tokens.Token;

/**
 * This class is used to represent a echo node.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class EchoNode extends Node {

    private final Token[] tokens;

    /**
     * Constructs a new echo node.
     * Parameter tokens must contain valid token objects.
     * 
     * @param tokens array of tokens
     */
    public EchoNode(Token[] tokens) {
        this.tokens = tokens;
    }

    /**
     * Getter method for tokens property
     * 
     * @return an array of tokens
     */
    public Token[] getTokens() {
        return this.tokens;
    }

    /**
     * Given visitor visits this node.
     * 
     * @param visitor
     */
    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitEchoNode(this);
    }

    /**
     * Returns a text representation of echo node in <code>{$= (tokens) $}</code> form.
     * Example: <code>{$= i i * @sin "0.000" @decfmt $}</code>
     * 
     * @return a text representation of the node
     */
    public String asText() {
        String str = "{$= ";
        for (Token t : this.tokens) {
            str += t.asText() + " ";
        }
        str += "$}";
        return str;
    }
}
