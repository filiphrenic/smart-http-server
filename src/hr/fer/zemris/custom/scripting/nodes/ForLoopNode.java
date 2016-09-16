package hr.fer.zemris.custom.scripting.nodes;

import hr.fer.zemris.custom.scripting.tokens.*;

/**
 * This class is used to represent a for loop.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class ForLoopNode extends Node {

    private final TokenVariable variable;
    private final Token startExpression;
    private final Token endExpression;
    private final Token stepExpression;

    /**
     * Constructs a new for loop node.
     * 
     * @param variable a valid {@link TokenVariable}
     * @param startExpression a valid {@link Token} that represents a start expression
     * @param endExpression a valid {@link Token} that represents a end expression
     * @param stepExpression a valid {@link Token} that represents a step expression, can be null
     */
    public ForLoopNode(TokenVariable variable, Token startExpression, Token endExpression, Token stepExpression) {
        this.variable = variable;
        this.startExpression = startExpression;
        this.endExpression = endExpression;
        this.stepExpression = stepExpression;
    }

    /**
     * Getter method for variable property.
     * 
     * @return value of variable
     */
    public TokenVariable getVariable() {
        return this.variable;
    }

    /**
     * Getter method for start expression property.
     * 
     * @return value of start expression
     */
    public Token getStartExpression() {
        return this.startExpression;
    }

    /**
     * Getter method for end expression property.
     * 
     * @return value of end expression
     */
    public Token getEndExpression() {
        return this.endExpression;
    }

    /**
     * Getter method for step expression property.
     * 
     * @return value of step expression
     */
    public Token getStepExpression() {
        return this.stepExpression;
    }

    /**
     * Given visitor visits this node.
     * 
     * @param visitor
     */
    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitForLoopNode(this);
    }

    /**
     * Returns a text representation of the for loop node.
     * {$ FOR + value of the variable + value of the start exp + value of the end exp + value of step exp (if not null)
     * + $}
     * 
     * @example: {$ FOR i 1 10 1 $}
     * @return a text representation
     */
    @Override
    public String asText() {
        String str = "{$ FOR " + variable.asText() + " " + startExpression.asText() + " " + endExpression.asText();
        if (stepExpression != null) {
            str += " " + stepExpression.asText();
        }
        str += " $}";

        for (int i = 0, n = numberOfChildren(); i < n; i++) {
            str += getChild(i).asText();
        }

        str += "{$END$}";

        return str;
    }
}
