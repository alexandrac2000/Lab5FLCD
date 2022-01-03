import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

public class Tree {

    private Grammar grammar;

    private ParserLL1 parser;

    public Tree(Grammar gr, ParserLL1 parser) {
        grammar = gr;
        this.parser = parser;
    }

    public String getTableOutput(final List<Integer> piProductions) {
        final TableEntityStructure<String, List<String>> p = parser.getProductionForIndex(piProductions.get(0));
        final Node root = new Node(p.getPosition());
        root.child = buildTree(p.getValue(), piProductions);
        final List<List<String>> tableRows = levelOrderTraversal(root);
        return tableRows.stream().map(row -> String.join(" | ", row)).collect(Collectors.joining("\n"));
    }



    private List<List<String>> levelOrderTraversal(final Node root) {
        final Queue<Node> queue = new ArrayDeque<>();
        queue.add(root);
        final List<Node> traversal = new ArrayList<>();
        final Map<Node, Node> parentOf = new HashMap<>();
        final Map<Node, Node> leftSOf = new HashMap<>();
        int currentNodeIndex = 1;
        while (!queue.isEmpty()) {
            final Node n = queue.remove();
            n.index = currentNodeIndex++;
            traversal.add(n);
            Node currentChild = n.child;
            if (currentChild != null) {
                queue.add(currentChild);
                parentOf.put(n.child, n);
                while (currentChild.rightSibling != null) {
                    leftSOf.put(currentChild.rightSibling, currentChild);
                    currentChild = currentChild.rightSibling;
                    queue.add(currentChild);
                    parentOf.put(currentChild, n);
                }
            }
        }

        final List<List<String>> rows = new ArrayList<>();
        for (final Node n : traversal) {
            rows.add(List.of(
                    String.valueOf(n.index),
                    n.value,
                    String.valueOf(parentOf.get(n) == null ? 0 : parentOf.get(n).index),
                    String.valueOf(leftSOf.get(n) == null ? 0 : leftSOf.get(n).index)
            ));
        }

        return rows;
    }

    private Node buildTree(final List<String> symbols, final List<Integer> piProductions) {
        if (symbols.isEmpty()) {
            return null;
        }
        final String symbol = symbols.get(0);

        if (grammar.isTerminal(symbol)) {
            final Node n = new Node(symbol);
            n.rightSibling = buildTree(symbols.subList(1, symbols.size()), piProductions);
            return n;
        } else if (grammar.isNonterminal(symbol)) {
            final Node n = new Node(symbol);
            piProductions.remove(0);
            final TableEntityStructure<String, List<String>> production = parser.getProductionForIndex(piProductions.get(0));
            n.child = buildTree(production.getValue(), piProductions);
            n.rightSibling = buildTree(symbols.subList(1, symbols.size()), piProductions);
            return n;
        } else {
            return new Node("ε");
        }
    }


    private static class Node {

        @ToString.Exclude
        private final String uuid = UUID.randomUUID().toString();
        private String value;
        private int index;
        private Node child;

        private Node rightSibling;

        public Node(String val) {
            value = val;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            final Node node = (Node) o;
            return Objects.equals(uuid, node.uuid);
        }

        @Override
        public int hashCode() {
            return uuid != null ? uuid.hashCode() : 0;
        }

    }

}
