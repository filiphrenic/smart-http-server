package hr.fer.zemris.custom.scripting.nodes;

/**
 * This class is used to store all children nodes. It is a parent to all other nodes.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class DocumentNode extends Node {

    /**
     * Given visitor visits this node.
     * 
     * @param visitor
     */
    @Override
    public void accept(final INodeVisitor visitor) {
        visitor.visitDocumentNode(this);
    }

    @Override
    public String asText() {
        String text = "";
        for (int i = 0, n = numberOfChildren(); i < n; i++) {
            text += getChild(i).asText();
        }
        return text;
    }
}
