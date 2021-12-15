
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


public class ParserLL1 {
    private final Grammar grammar;
    private final Map<String, Set<String>> first;
    private final Map<String, Set<String>> follow;
    private String fileName;

    private ParsingTable parsingTable = new ParsingTable();
    private static Stack<List<String>> rules = new Stack<>();
    private Map<TableEntityStructure<String, List<String>>, Integer> productionsNumbered = new HashMap<>();
    private Stack<String> alpha = new Stack<>();
    private Stack<String> beta = new Stack<>();
    private Stack<String> pi = new Stack<>();

    public ParserLL1(String fileName) throws FileNotFoundException {
        this.grammar = Grammar.ReadGrammar(fileName);
        this.first = first(grammar);
        this.follow = follow(grammar);
        constructParsingTable();
    }
public  Map<TableEntityStructure<String, List<String>>, Integer> getProductionsNumbered(){
        return productionsNumbered;
}
    public static Map<String, Set<String>> first(Grammar grammar) {
        Map<String, Set<String>> firstM = new HashMap<>();
        Set<String> nonTerminals = grammar.getNonterminals();
        Set<String> terminals = grammar.getTerminals();
        boolean isIdentical;

        for (String t : terminals) {
            LinkedHashSet<String> terminalSet = new LinkedHashSet<>();
            terminalSet.add(t);
            firstM.put(t, terminalSet);
        }

        for (String nonT : nonTerminals) {
            firstM.put(nonT, new LinkedHashSet<>());
            try {
                Production p = grammar.getProductionsForNonterminal(nonT);
                for (var rules : p.getRules()) {
                    var firstChar = rules.get(0);
                    if (grammar.getTerminals().contains(firstChar)) {
                        firstM.get(nonT).add(firstChar);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        do {
            isIdentical = true;

            for (Map.Entry<String, Set<String>> entry : firstM.entrySet()) {
                String entity = entry.getKey();
                Set<String> firstEntries = entry.getValue();
                if (grammar.getTerminals().contains(entity)) {
                    continue;
                }
                try {
                    var productionsOfNonTerminal = grammar.getProductionsForNonterminal(entity);
                    for (var rules : productionsOfNonTerminal.getRules()) {
                        isIdentical = !firstEntries.addAll(firstCat(rules, firstM)) && isIdentical;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (!isIdentical);
        return firstM;
    }

    private static Set<String> firstCat(List<String> grammarEntityList, Map<String, Set<String>> map) {
        Set<String> resultSet = new LinkedHashSet<>();

        for (String entity : grammarEntityList) {
            if (map.get(entity).size() == 0) {
                return new LinkedHashSet<>();
            }

            resultSet.addAll(map.get(entity));
            if (!map.get(entity).contains("ε")) {
                resultSet.remove("ε");
                return resultSet;
            }
        }
        return resultSet;
    }

    public static Map<String, Set<String>> follow(Grammar grammar) {
        Map<String, Set<String>> firstS = first(grammar);
        Map<String, Set<String>> followS = new HashMap<>();
        Map<String, Set<String>> oldFollowS;

        for (String nonT : grammar.getNonterminals()) {
            if (!nonT.equals(grammar.getStartingSymbol())) {
                followS.put(nonT, new LinkedHashSet<>());
            }
        }
        Set<String> startingPointSet = new LinkedHashSet<>();
        startingPointSet.add("ε");
        followS.put(grammar.getStartingSymbol(), startingPointSet);
        boolean updated;
        do {
            updated = false;
            oldFollowS = followS;
            followS = new HashMap<>();

            for (String nonT : grammar.getNonterminals()) {
                Set<String> nonTerminalInitialSet = new LinkedHashSet<>(oldFollowS.get(nonT));
                followS.put(nonT, nonTerminalInitialSet);
                for (String lhsNonTerminal : grammar.getProductions().stream().map(Production::getStartingNonTerminalSymbol).collect(Collectors.toList())) {
                    try {
                        for (List<String> rules : grammar.getProductionsForNonterminal(lhsNonTerminal).getRules()) {
                            int position;
                            for (position = 0; position < rules.size() - 1; position++) {
                                if (rules.get(position).equals(nonT)) {
                                    List<String> beta = rules.subList(position + 1, rules.size());
                                    Set<String> firstOfBeta = firstCat(beta, firstS);
                                    updated = followS.get(nonT).addAll(firstOfBeta) || updated;
                                    if (firstOfBeta.contains("ε")) {
                                        updated = followS.get(nonT).addAll(oldFollowS.get(lhsNonTerminal)) || updated;
                                    }
                                }
                            }
                            if (rules.size() == 0) {
                                throw new RuntimeException("Problem with grammar entity");
                            }

                            if (rules.get(rules.size() - 1).equals(nonT)) {
                                updated = followS.get(nonT).addAll(oldFollowS.get(lhsNonTerminal)) || updated;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        } while (updated);
        return followS;
    }

    private String displayPiProductions(Stack<String> pi) {
        StringBuilder sb = new StringBuilder();

        for (String productionIndexString : pi) {
            if (productionIndexString.equals("ε")) {
                continue;
            }
            Integer productionIndex = Integer.parseInt(productionIndexString);
            this.getProductionsNumbered().forEach((key, value) ->{
                if (productionIndex.equals(value))
                    sb.append(value).append(": ").append(key.getPosition()).append(" -> ").append(key.getValue()).append("\n");
            });
        }

        return sb.toString();
    }


    private void constructParsingTable() {
        numberingProductions();

        List<String> columnSymbols = new LinkedList<>(grammar.getTerminals());
        columnSymbols.add("$");
        columnSymbols.remove("ε");

        // M(a, a) = pop
        // M($, $) = acc

        parsingTable.put(new TableEntityStructure<>("$", "$"), new TableEntityStructure<>(Collections.singletonList("acc"), -1));
        for (String terminal: grammar.getTerminals()) {
            if(!terminal.equals("ε")) {
                parsingTable.put(new TableEntityStructure<>(terminal, terminal), new TableEntityStructure<>(Collections.singletonList("pop"), -1));
            }
        }




        productionsNumbered.forEach((key, value) -> {
            String rowSymbol = key.getPosition();
            List<String> rule = key.getValue();
            TableEntityStructure<List<String>, Integer> parseTableValue = new TableEntityStructure<>(rule, value);

            for (String columnSymbol : columnSymbols) {
                TableEntityStructure<String, String> parseTableKey = new TableEntityStructure<>(rowSymbol, columnSymbol);

                // if our column-terminal is exactly first of rule
                if (rule.get(0).equals(columnSymbol) && !columnSymbol.equals("ε"))
                    parsingTable.put(parseTableKey, parseTableValue);

                    // if the first symbol is a non-terminal and it's first contain our column-terminal
                else if (grammar.getNonterminals().contains(rule.get(0)) && first.get(rule.get(0)).contains(columnSymbol)) {
                    if (!parsingTable.containsKey(parseTableKey)) {
                        parsingTable.put(parseTableKey, parseTableValue);
                    }
                }
                else {
                    // if the first symbol is ε then everything if FOLLOW(rowSymbol) will be in parse table
                    if (rule.get(0).equals("ε")) {
                        for (String b : follow.get(rowSymbol)) {
                            if (b.equals("ε")) {
                                b = "$";
                            }
                            parsingTable.put(new TableEntityStructure<>(rowSymbol, b), parseTableValue);
                        }

                        // if ε is in FIRST(rule)
                    } else {
                        Set<String> firsts = new HashSet<>();
                        for (String symbol : rule)
                            if (grammar.getNonterminals().contains(symbol))
                                firsts.addAll(first.get(symbol));
                        if (firsts.contains("ε")) {
                            for (String b : first.get(rowSymbol)) {
                                if (b.equals("ε"))
                                    b = "$";
                                parseTableKey = new TableEntityStructure<>(rowSymbol, b);
                                if (!parsingTable.containsKey(parseTableKey)) {
                                    parsingTable.put(parseTableKey, parseTableValue);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void printStringOfProductions(List<String> sequence){
        boolean result = this.parse(sequence);
        if(result){
            System.out.println("Sequence " + sequence + " is accepted");
            System.out.println(displayPiProductions(this.pi));
        } else{
            System.out.println("Sequence " + sequence + " is not accepted");
        }
    }

    public boolean parse(List<String> w) {
        initializeStacks(w);
        System.out.println(w);

        boolean go = true;
        boolean result = true;

        while (go) {
            String betaHead = beta.peek();
            String alphaHead = alpha.peek();

            if (betaHead.equals("$") && alphaHead.equals("$")) {
                return result;
            }

            TableEntityStructure<String, String> heads = new TableEntityStructure<>(betaHead, alphaHead);
            TableEntityStructure<List<String>, Integer> parseTableEntry = parsingTable.get(heads);

            if (parseTableEntry == null) {
                go = false;
                result = false;
            } else {
                List<String> production = parseTableEntry.getPosition();
                Integer productionPos = parseTableEntry.getValue();

                if (productionPos == -1 && production.get(0).equals("acc")) {
                    go = false;
                } else if (productionPos == -1 && production.get(0).equals("pop")) {
                    beta.pop();
                    alpha.pop();
                } else {
                    beta.pop();
                    if (!production.get(0).equals("ε")) {
                        pushAsChars(production, beta);
                    }
                    pi.push(productionPos.toString());
                }
            }
        }

        System.out.println(this.displayPiProductions(pi));
        return result;
    }


    private void initializeStacks(List<String> w) {
        alpha.clear();
        alpha.push("$");
        pushAsChars(w, alpha);

        beta.clear();
        beta.push("$");
        beta.push(grammar.getStartingSymbol());

        pi.clear();
        pi.push("ε");
    }

    private void pushAsChars(List<String> sequence, Stack<String> stack) {
        for (int i = sequence.size() - 1; i >= 0; i--) {
            stack.push(sequence.get(i));
        }
    }

    private void numberingProductions() {
        int index = 1;
        for (Production production: grammar.getProductions())
            for (List<String> rule: production.getRules())
                productionsNumbered.put(new TableEntityStructure<>(production.getStartingNonTerminalSymbol(), rule), index++);
        System.out.println(productionsNumbered);
    }

}
