package hr.fer.zemris.custom.scripting.nodes;

/**
 * Classes that implement this interface must implement methods that do something with a specific kind of {@link Node}.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public interface INodeVisitor {

    /**
     * Determines what happens when a {@link TextNode} is reached.
     * 
     * @param node text node
     */
    void visitTextNode(final TextNode node);

    /**
     * Determines what happens when a {@link ForLoopNode} is reached.
     * 
     * @param node for loop node
     */
    void visitForLoopNode(final ForLoopNode node);

    /**
     * Determines what happens when a {@link EchoNode} is reached.
     * 
     * @param node echo node
     */
    void visitEchoNode(final EchoNode node);

    /**
     * Determines what happens when a {@link DocumentNode} is reached.
     * 
     * @param node document node
     */
    void visitDocumentNode(final DocumentNode node);

}
