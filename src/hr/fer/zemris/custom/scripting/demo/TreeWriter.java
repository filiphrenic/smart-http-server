package hr.fer.zemris.custom.scripting.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import hr.fer.zemris.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.custom.scripting.nodes.TextNode;
import hr.fer.zemris.custom.scripting.parser.SmartScriptParser;

/**
 * This class is used to demonstrate the {@link SmartScriptParser}.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class TreeWriter {

    public static void main(String[] args) {

        if (args.length != 1) {
            throw new IllegalArgumentException("You must provide a path to the script.");
        }

        final String path = args[0];
        if (!path.endsWith(".smscr")) {
            throw new IllegalArgumentException("File must be a smart script (extension .smscr)");
        }

        String document = "";
        try {
            List<String> rows = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            for (String row : rows) {
                document += row + "\r\n";
            }
        } catch (IOException e) {
            System.err.println("error while reading from file");
        }

        SmartScriptParser parser = new SmartScriptParser(document);
        INodeVisitor visitor = new WriterVisitor();
        parser.getDocumentNode().accept(visitor);

    }

    /**
     * Writes out every node using asText() method to standard output.
     * 
     * @author Filip Hrenić
     * @version 1.0
     */
    private static class WriterVisitor implements INodeVisitor {

        @Override
        public void visitTextNode(TextNode node) {
            System.out.println(node.asText());
        }

        @Override
        public void visitForLoopNode(ForLoopNode node) {
            System.out.println(node.asText());
        }

        @Override
        public void visitEchoNode(EchoNode node) {
            System.out.println(node.asText());
        }

        @Override
        public void visitDocumentNode(DocumentNode node) {
            System.out.println(node.asText());
        }

    }

}
