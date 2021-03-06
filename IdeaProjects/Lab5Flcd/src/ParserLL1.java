
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


public class ParserLL1 {
    private static Stack<List<String>> rules = new Stack<>();
    private  Grammar grammar;
    private  Map<String, Set<String>> theSetofFirsts;
    private  Map<String, Set<String>> theSetOfFollow;
    private String fileName;
    private ParsingTable parsingTable = new ParsingTable();
    private Map<TableEntityStructure<String, List<String>>, Integer> productionsNumbered = new HashMap<>();
    private Stack<String> alpha = new Stack<>();
    private Stack<String> beta = new Stack<>();
    private Stack<String> pi = new Stack<>();

    public ParserLL1(String fileName) throws FileNotFoundException {
        this.grammar = Grammar.ReadGrammar(fileName);
        this.theSetofFirsts = first(grammar);
        this.theSetOfFollow = follow(grammar);
        createParseTable();
    }

    public static Map<String, Set<String>> first(Grammar grammar) {
        Map<String, Set<String>> firstMap = new HashMap<>();
        Set<String> nonT = grammar.getNonterminals();
        Set<String> terminals = grammar.getTerminals();
        boolean isIdentical;
//for every terminal we create a linked hashSet in which
// we add the terminal t,in the firstmap we put at the key t,the tset
        for (String t : terminals) {
            LinkedHashSet<String> tSet = new LinkedHashSet<>();
            tSet.add(t);
            firstMap.put(t, tSet);
        }



//for each nonterminal we add a new linked hash set at its key in the first map
        for (String nonTerminal : nonT) {
            firstMap.put(nonTerminal, new LinkedHashSet<>());
            try { //get the production for the given nonterminal
                Production p = grammar.getProductionsForNonterminal(nonTerminal);
                for (var rules : p.getRules()) {
                    //if the grammar contains the first character from the rules as a terminal,we added it in the first
                    //map at the nonterminal
                    var firstChar = rules.get(0);
                    if (grammar.getTerminals().contains(firstChar)) {
                        firstMap.get(nonTerminal).add(firstChar);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        do {
            isIdentical = true;
            for (Map.Entry<String, Set<String>> entry : firstMap.entrySet()) {
                String entity = entry.getKey();
                Set<String> firstEntries = entry.getValue();
                if (grammar.getTerminals().contains(entity)) {
                    continue;
                }
                try {
                    var productionsOfNonTerminal = grammar.getProductionsForNonterminal(entity);
                    for (var rules : productionsOfNonTerminal.getRules()) {
                        isIdentical = !firstEntries.addAll(firstCat(rules, firstMap)) && isIdentical;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (!isIdentical);
        return firstMap;
    }

    private static Set<String> firstCat(List<String> grammarEntityList, Map<String, Set<String>> map) {
        Set<String> theSetOfResults = new LinkedHashSet<>();

        for (String entity : grammarEntityList) {
            if (map.get(entity).size() == 0) {
                return new LinkedHashSet<>();
            }

            theSetOfResults.addAll(map.get(entity));
            if (!map.get(entity).contains("??")) {
                theSetOfResults.remove("??");
                return theSetOfResults;
            }
        }
        return theSetOfResults;
    }

    public static Map<String, Set<String>> follow(Grammar grammar) {
        Map<String, Set<String>> firstS = first(grammar);
        Map<String, Set<String>> followS = new HashMap<>();
        Map<String, Set<String>> previousFollowSets;

        for (String nonT : grammar.getNonterminals()) {
                 if (!nonT.equals(grammar.getStartingSymbol())) {
                followS.put(nonT, new LinkedHashSet<>());
            }
        }
        Set<String> startingPointSet = new LinkedHashSet<>();
        startingPointSet.add("??");
        followS.put(grammar.getStartingSymbol(), startingPointSet);
        boolean updated;
        do {
            updated = false;
            previousFollowSets = followS;
            followS = new HashMap<>();

            for (String nonT : grammar.getNonterminals()) {
                Set<String> nonTInitialSet = new LinkedHashSet<>(previousFollowSets.get(nonT));
                followS.put(nonT, nonTInitialSet);
                for (String lefthandsideNonTerminal : grammar.getProductions().stream().map(Production::getStartingNonTerminalSymbol).collect(Collectors.toList())) {
                    try {
                        for (List<String> rules : grammar.getProductionsForNonterminal(lefthandsideNonTerminal).getRules()) {
                            int position;
                            for (position = 0; position < rules.size() - 1; position++) {
                                if (rules.get(position).equals(nonT)) {
                                    List<String> beta = rules.subList(position + 1, rules.size());
                                    Set<String> firstOfBeta = firstCat(beta, firstS);
                                    updated = followS.get(nonT).addAll(firstOfBeta) || updated;
                                    if (firstOfBeta.contains("??")) {
                                        updated = followS.get(nonT).addAll(previousFollowSets.get(lefthandsideNonTerminal)) || updated;
                                    }
                                }
                            }
                            if (rules.size() == 0) {
                                throw new RuntimeException("Problem with grammar entity");
                            }

                            if (rules.get(rules.size() - 1).equals(nonT)) {
                                updated = followS.get(nonT).addAll(previousFollowSets.get(lefthandsideNonTerminal)) || updated;
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
            if (productionIndexString.equals("??")) {
                continue;
            }
            Integer productionIndex = Integer.parseInt(productionIndexString);
            this.getProductionsNumbered().forEach((key, value) -> {
                if (productionIndex.equals(value))
                    sb.append(value).append(": ").append(key.getPosition()).append(" -> ").append(key.getValue()).append("\n");
            });
        }

        return sb.toString();
    }

    private Map<TableEntityStructure<String, List<String>>, Integer> getProductionsNumbered() {
        return productionsNumbered;
    }


    private void createParseTable() {
        numberingProductions();

        List<String> columnSymbols = new LinkedList<>(grammar.getTerminals());
        columnSymbols.add("$");
        columnSymbols.remove("??");

        // M(a, a) = pop
        // M($, $) = acc

        parsingTable.put(new TableEntityStructure<>("$", "$"), new TableEntityStructure<>(Collections.singletonList("acc"), -1));
        for (String terminal : grammar.getTerminals()) {
            if (!terminal.equals("??")) {
                parsingTable.put(new TableEntityStructure<>(terminal, terminal), new TableEntityStructure<>(Collections.singletonList("pop"), -1));
            }
        }


//        1) M(A, a) = (??, i), if:
//            a) a ??? first(??)
//            b) a != ??
//            c) A -> ?? production with index i
//
//        2) M(A, b) = (??, i), if:
//            a) ?? ??? first(??)
//            b) whichever b ??? follow(A)
//            c) A -> ?? production with index i
        productionsNumbered.forEach((key, value) -> {
            String rowSymbol = key.getPosition();
            List<String> rule = key.getValue();
            TableEntityStructure<List<String>, Integer> parseTableValue = new TableEntityStructure<>(rule, value);

            for (String colSym : columnSymbols) {
                TableEntityStructure<String, String> parseTableKey = new TableEntityStructure<>(rowSymbol, colSym);

                // if our column-terminal is exactly first of rule
                if (rule.get(0).equals(colSym) && !colSym.equals("$"))
                    parsingTable.put(parseTableKey, parseTableValue);

                    // if the first symbol is a non-terminal and it's first contain our column-terminal
                else if (grammar.getNonterminals().contains(rule.get(0)) && theSetofFirsts.get(rule.get(0)).contains(colSym)) {
                    if (!parsingTable.containsKey(parseTableKey)) {
                        parsingTable.put(parseTableKey, parseTableValue);
                    } else {
                        throw new RuntimeException("Exists a value at key: " + parseTableKey + " with v: "
                                + parsingTable.get(parseTableKey) + ". Tried to add: " + parseTableValue + ". Grammar is not LL(1)");
                    }
                } else {
                    // if the first symbol is ?? then everything if FOLLOW(rowSymbol) will be in parse table
                    if (rule.get(0).equals("??")) {
                        for (String b : theSetOfFollow.get(rowSymbol)) {
                            if (b.equals("??")) {
                                b = "$";
                            }
                            parsingTable.put(new TableEntityStructure<>(rowSymbol, b), parseTableValue);
                        }

                        // if ?? is in FIRST(rule)
                    } else {
                        Set<String> firsts = new LinkedHashSet<>();
                        for (String symbol : rule)
                            if (grammar.getNonterminals().contains(symbol))
                                firsts.addAll(theSetofFirsts.get(symbol));
                        if (firsts.contains("??")) {
                            for (String b : theSetOfFollow.get(rowSymbol)) {
                                if (b.equals("??"))
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

    public boolean printStringOfProductions(List<String> sequence) {
        boolean result = this.parse(sequence);
        if (result) {
            System.out.println("Sequence " + sequence + " is accepted");
            System.out.println(displayPiProductions(this.pi));
        } else {
            System.out.println("Sequence " + sequence + " is not accepted");
        }
        return result;
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
                    if (!production.get(0).equals("??")) {
                        pushAsChars(production, beta);
                    }
                    pi.push(productionPos.toString());
                }
            }
        }

        System.out.println(this.displayPiProductions(pi));
        return result;
    }

    public TableEntityStructure<String, List<String>> getProductionForIndex(int index) {
        for (var key : productionsNumbered.keySet()) {
            if (productionsNumbered.get(key) == index) {
                return key;
            }
        }
        return null;
    }

    private void initializeStacks(List<String> w) {
        alpha.clear();
        alpha.push("$");
        System.out.println(w);
        pushAsChars(w, alpha);

        beta.clear();
        beta.push("$");
        beta.push(grammar.getStartingSymbol());

        pi.clear();
        pi.push("??");
    }

    private void pushAsChars(List<String> sequence, Stack<String> stack) {
        for (int i = sequence.size() - 1; i >= 0; i--) {
            stack.push(sequence.get(i));
        }
    }

    private void numberingProductions() {
        int index = 1;
        for (Production production : grammar.getProductions())
            for (List<String> rule : production.getRules())
                productionsNumbered.put(new TableEntityStructure<>(production.getStartingNonTerminalSymbol(), rule), index++);
    }

    public List<Integer> getPiProductions() {
        final List<Integer> piProductions = new ArrayList<>();
        while (!pi.isEmpty()) {
            final String value = pi.pop();
            if (!"??".equals(value)) {
                piProductions.add(Integer.valueOf(value));
            }
        }
        Collections.reverse(piProductions);
        return piProductions;
    }

}

