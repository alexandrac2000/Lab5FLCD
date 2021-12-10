import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class ParserLL1 {
    private  Grammar grammar;
    private  Map<String, Set<String>> first;
    private Map<String, Set<String>> follow;
    private String fileName;


    public ParserLL1(String fileName) throws FileNotFoundException {
        this.grammar = Grammar.ReadGrammar(fileName);
        this.first = new HashMap<>();
        this.follow = new HashMap<>();
    }

    public static Map<String, Set<String>> first(Grammar grammar) {
        Map<String, Set<String>> firstM = new HashMap<>();
        Set<String> nTerminals = grammar.getNonterminals();
        Set<String> terminals = grammar.getTerminals();
        boolean same;

        for (String t : terminals) {
            LinkedHashSet<String> tSet = new LinkedHashSet<>();
            tSet.add(t);
            firstM.put(t, tSet);
        }

        for (String nonT : nTerminals) {
            firstM.put(nonT, new LinkedHashSet<>());
            try {
                Production production = grammar.getProductionsForNonterminal(nonT);
                for (var rules : production.getRules()) {
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
            same = true;

            for (Map.Entry<String, Set<String>> entry : firstM.entrySet()) {
                String entity = entry.getKey();
                Set<String> firstEntries = entry.getValue();
                if (grammar.getTerminals().contains(entity)==true) {
                    continue;
                }
                try {
                    var productionsOfNonT = grammar.getProductionsForNonterminal(entity);
                    for (var rules : productionsOfNonT.getRules()) {
                        same = !firstEntries.addAll(firstCat(rules, firstM)) && same;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (!same);
        return firstM;
    }

    private static Set<String> firstCat(List<String> grammarEntityList, Map<String, Set<String>> map) {
        Set<String> resultsSt = new LinkedHashSet<>();

        for (String e : grammarEntityList) {
            if (map.get(e).size() == 0) {
                return new LinkedHashSet<>();
            }

            resultsSt.addAll(map.get(e));
            if (!map.get(e).contains("ε")) {
                resultsSt.remove("ε");
                return resultsSt;
            }
        }
        return resultsSt;
    }

    public static Map<String, Set<String>> follow(Grammar grammar) {
        Map<String, Set<String>> firstS = first(grammar);
        Map<String, Set<String>> followS = new HashMap<>();
        Map<String, Set<String>> oldF;

        for (String nonT : grammar.getNonterminals()) {
            if (!nonT.equals(grammar.getStartingSymbol())) {
                followS.put(nonT, new LinkedHashSet<>());
            }
        }

        Set<String> startingPSet = new LinkedHashSet<>();
        startingPSet.add("ε");
        followS.put(grammar.getStartingSymbol(), startingPSet);
        boolean updatedSets;

        do {
            updatedSets = false;
            oldF = followS;
            followS = new HashMap<>();

            for (String nonT : grammar.getNonterminals()) {
                Set<String> nonTInitialS = new LinkedHashSet<>(oldF.get(nonT));
                followS.put(nonT, nonTInitialS);
                for (String lhsNonTerminal : grammar.getProductions().stream().map(Production::getStartingNonTerminalSymbol).collect(Collectors.toList())) {
                    try {
                        for (List<String> rls : grammar.getProductionsForNonterminal(lhsNonTerminal).getRules()) {
                            int pos;
                            for (pos = 0; pos < rls.size() - 1; pos++) {
                                if (rls.get(pos).equals(nonT)) {
                                    List<String> betaList = rls.subList(pos + 1, rls.size());
                                    Set<String> firstB = firstCat(betaList, firstS);
                                    updatedSets = followS.get(nonT).addAll(firstB) || updatedSets;
                                    if (firstB.contains("ε")) {
                                        updatedSets = followS.get(nonT).addAll(oldF.get(lhsNonTerminal)) || updatedSets;
                                    }
                                }
                            }
                            if (rls.size() == 0) {
                                throw new RuntimeException("Problems with entity");
                            }

                            if (rls.get(rls.size() - 1).equals(nonT)) {
                                updatedSets = followS.get(nonT).addAll(oldF.get(lhsNonTerminal)) || updatedSets;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        } while (updatedSets);
        return followS;
    }
}
