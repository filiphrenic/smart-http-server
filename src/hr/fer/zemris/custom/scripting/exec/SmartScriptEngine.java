package hr.fer.zemris.custom.scripting.exec;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Stack;

import hr.fer.zemris.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.custom.scripting.nodes.TextNode;
import hr.fer.zemris.custom.scripting.tokens.Token;
import hr.fer.zemris.custom.scripting.tokens.TokenConstantDouble;
import hr.fer.zemris.custom.scripting.tokens.TokenConstantInteger;
import hr.fer.zemris.custom.scripting.tokens.TokenConstantString;
import hr.fer.zemris.custom.scripting.tokens.TokenFunction;
import hr.fer.zemris.custom.scripting.tokens.TokenOperator;
import hr.fer.zemris.custom.scripting.tokens.TokenVariable;
import hr.fer.zemris.webserver.RequestContext;

/**
 * @author Filip Hrenić
 * @version 1.0
 */
public class SmartScriptEngine {

    private final DocumentNode documentNode;
    private final RequestContext requestContext;
    private final ObjectMultistack multistack = new ObjectMultistack();
    private final INodeVisitor visitor = new INodeVisitor() {

        /**
         * Calls accept on all of it's children.
         */
        @Override
        public void visitDocumentNode(DocumentNode node) {
            for (int i = 0, n = node.numberOfChildren(); i < n; i++) {
                node.getChild(i).accept(this);
            }
        }

        /**
         * Writes the node's text to the context's output stream.
         */
        @Override
        public void visitTextNode(TextNode node) {
            try {
                requestContext.write(node.getValue());
            } catch (IOException e) {
                System.err.println("Error while writing to output stream.");
            }
        }

        /**
         * Runs a standard for loop. In each iteration calls accept method on all node's children.
         */
        @Override
        public void visitForLoopNode(ForLoopNode node) {
            ValueWrapper currValue = new ValueWrapper(node.getStartExpression().asText());
            Object endValue = new ValueWrapper(node.getEndExpression().asText()).getValue();
            Object stepValue = new ValueWrapper(node.getStepExpression().asText()).getValue();
            final String name = node.getVariable().getName();

            multistack.push(name, currValue);

            while (currValue.numCompare(endValue) <= 0) {
                for (int i = 0, n = node.numberOfChildren(); i < n; i++) {
                    node.getChild(i).accept(this);
                }

                currValue = multistack.pop(name);
                currValue.increment(stepValue);
                multistack.push(name, currValue);
            }

            multistack.pop(name);
        }

        @Override
        public void visitEchoNode(EchoNode node) {
            Token[] tokens = node.getTokens();
            Stack<Object> stack = new Stack<>();

            for (final Token t : tokens) {
                if (t instanceof TokenConstantDouble) {
                    stack.push(((TokenConstantDouble) t).getValue());
                } else if (t instanceof TokenConstantInteger) {
                    stack.push(((TokenConstantInteger) t).getValue());
                } else if (t instanceof TokenConstantString) {
                    stack.push(((TokenConstantString) t).getValue());
                } else if (t instanceof TokenVariable) {
                    String name = ((TokenVariable) t).getName();
                    stack.push(multistack.peek(name).getValue());
                } else if (t instanceof TokenOperator) {

                    String symbol = ((TokenOperator) t).getSymbol();
                    Object v2 = new ValueWrapper(stack.pop()).getValue();
                    ValueWrapper v1 = new ValueWrapper(stack.pop());

                    switch (symbol) {
                        case "+":
                            v1.increment(v2);
                            break;
                        case "-":
                            v1.decrement(v2);
                            break;
                        case "*":
                            v1.multiply(v2);
                            break;
                        case "/":
                            v1.divide(v2);
                            break;
                    }

                    stack.push(v1.getValue());

                } else if (t instanceof TokenFunction) {
                    final String f = ((TokenFunction) t).getName();

                    if (f.equalsIgnoreCase("sin")) {
                        ValueWrapper v = new ValueWrapper(stack.pop());
                        Double x = Double.parseDouble(v.getValue().toString());
                        stack.push(Math.sin(x * Math.PI / 180));

                    } else if (f.equalsIgnoreCase("decfmt")) {
                        String decfmt = stack.pop().toString();
                        DecimalFormat format = new DecimalFormat(decfmt);
                        Object value = new ValueWrapper(stack.pop()).getValue();
                        stack.push(format.format(value));

                    } else if (f.equalsIgnoreCase("dup")) {
                        Object x = stack.peek();
                        stack.push(x);

                    } else if (f.equalsIgnoreCase("swap")) {
                        Object a = stack.pop();
                        Object b = stack.pop();
                        stack.push(a);
                        stack.push(b);

                    } else if (f.equalsIgnoreCase("setMimeType")) {
                        String mimeType = stack.pop().toString();
                        requestContext.setMimeType(mimeType);

                    } else if (f.equalsIgnoreCase("paramget")) {
                        String dValue = stack.pop().toString();
                        String name = stack.pop().toString();

                        String value = requestContext.getParameter(name);
                        stack.push((value == null ? dValue : value));

                    } else if (f.equalsIgnoreCase("pparamget")) {
                        String dValue = stack.pop().toString();
                        String name = stack.pop().toString();

                        String value = requestContext.getPersistentParameter(name);
                        stack.push((value == null ? dValue : value));

                    } else if (f.equalsIgnoreCase("pparamset")) {
                        String name = stack.pop().toString();
                        String value = stack.pop().toString();

                        requestContext.setPersistentParameter(name, value);

                    } else if (f.equalsIgnoreCase("pparamdel")) {
                        String name = stack.pop().toString();
                        requestContext.removePersistentParameter(name);

                    } else if (f.equalsIgnoreCase("tparamget")) {
                        String dValue = stack.pop().toString();
                        String name = stack.pop().toString();

                        String value = requestContext.getTemporaryParameter(name);
                        stack.push((value == null ? dValue : value));

                    } else if (f.equalsIgnoreCase("tparamset")) {
                        String name = stack.pop().toString();
                        String value = stack.pop().toString();

                        requestContext.setTemporaryParameter(name, value);

                    } else if (f.equalsIgnoreCase("tparamdel")) {
                        String name = stack.pop().toString();
                        requestContext.removeTemporaryParameter(name);

                    } else {
                        stack.push("unknown function name: " + f);
                    }
                }
            }

            Stack<String> newStack = new Stack<>();
            while (!stack.isEmpty()) {
                newStack.push(stack.pop().toString());
            }

            while (!newStack.isEmpty()) {
                try {
                    requestContext.write(newStack.pop());
                } catch (IOException e) {
                    System.err.println("Error while writing to context's output stream.");
                }
            }
        }
    };

    /**
     * Creates a new {@link SmartScriptEngine} that can execute the script.
     * 
     * @param documentNode document node to visit
     * @param requestContext context to whose output stream we will output data
     */
    public SmartScriptEngine(DocumentNode documentNode, RequestContext requestContext) {
        this.documentNode = documentNode;
        this.requestContext = requestContext;
    }

    /**
     * Executes this smart script engine.
     */
    public void execute() {
        documentNode.accept(visitor);
    }
}
