import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Victor Sanni Fall 2021
 */

public class Sudi {
    static final double bigNum = -40.0;  // penalty for unseen words

    static Graph<String, Double> mainGraph = new AdjacencyMapGraph<>(); // contains states with transition values
    static HashMap<String, HashMap<String, Double>> observationMap = new HashMap<>();  // contains states with the words observed in each state and each word's probability
    static HashMap<String, HashMap<String, Double>> transitionMap = new HashMap<>();  // contains states to other states a state transitions to number of transitions
    static String tags = "";
    /**
     * ALgorithm to train model
     *
     * Iterate over file of words and tags. Convert words to lowercase. Get tag for a word.
     *
     * for transitions, use hashmap of states to hashmap of otherStates to how many times we have seen a transition from state to otherState
     * When we see a transition from a state to another state, if we have not seen such a transition before,
     * add map of otherState with value 1 to map of state
     * else if we have seen such a transition, increment the value of the otherState in the state map by 1
     * if word is at index 0 (first word), state = '#' and otherState is index 1 of the line in the tagTrainer file
     * if word is at the end of the line (index line.length - 1), do nothing transition-wise.
     *
     * After we have read from the entire file, we want to process transitions
     * Iterate over the keys in the transitions map. For each key, get the sum of the values of all its otherStates.
     * This is the total number of transitions per state
     * When you get the total per state, update the values in the otherStates map per state by log(original value/total for the state).
     * Do in place to optimize memory
     * Two loops per state: first to get total, second to update transition values to log.
     *
     * After map is done, we want to load the results into a graph for easy documentation
     * Iterate over the transitions map. For each state, first add it to the graph if it isn't already.
     * For each otherState in the state's own map, insert the otherState into the graph if it isn't already.
     * Create a directed edge between the state and the otherState in the graph, with label as the transition value
     *
     * for observations, use hashmap of states to hashmap of words to probabilities.
     * for each word in a line, get its tag from the tag file.
     * If the word doesn't exist as a key in the state value map, add the word with a probability of 1 for that state
     * If the word already exists, increment its probability by 1
     * When done, do step 2 for transitions to update probabilities with log values
     * Do everything in place for memory optimization. No need to load this into graph, keep it as map.
     */

    public static void trainModel(String wordTrainerPath, String tagTrainerPath) throws IOException {
        BufferedReader wordTrainer = new BufferedReader(new FileReader(wordTrainerPath));  // Load file of words
        BufferedReader tagTrainer = new BufferedReader(new FileReader(tagTrainerPath));  // Load file of tags per word

        String line; String lineTag;
        while ((line = wordTrainer.readLine()) != null && (lineTag = tagTrainer.readLine()) != null){
            String[] perWord = line.toLowerCase().split(" ");
            String[] perTag = lineTag.split(" ");

            for (int i = 0; i < perTag.length; i++){ // iterate until last word

                // First, handle transitions

                // special case: beginning of line
                if (i == 0){
                    if (!transitionMap.containsKey("#")) {// If it is the first line
                        HashMap<String, Double> otherStateMap = new HashMap<>();
                        otherStateMap.put(perTag[i], 1.0);
                        transitionMap.put("#", otherStateMap);
                    }
                    else if (transitionMap.containsKey("#") && !transitionMap.get("#").containsKey(perTag[i])){  // if this is not the first line, but we have never seen this tag start a sentence before
                        transitionMap.get("#").put(perTag[i], 1.0);
                    }
                    else {  // we have seen this tag start a sentence before, so just increment the value
                        double curr = transitionMap.get("#").get(perTag[i]);
                        transitionMap.get("#").put(perTag[i], curr + 1.0);
                    }
                }

                // general case: until end of line
                else{
                    if (!transitionMap.containsKey(perTag[i - 1])) {// If it is the first time we see the previous tag transiting to a new tag
                        HashMap<String, Double> otherStateMap = new HashMap<>();
                        otherStateMap.put(perTag[i], 1.0);
                        transitionMap.put(perTag[i - 1], otherStateMap);
                    }
                    else if (transitionMap.containsKey(perTag[i - 1]) && !transitionMap.get(perTag[i - 1]).containsKey(perTag[i])){  // if this is not a new tag, but this is a transition happening for the first time
                        transitionMap.get(perTag[i - 1]).put(perTag[i], 1.0);
                    }
                    else {  // we have seen this particular transitions before, so just increment the transition value
                        double curr = transitionMap.get(perTag[i - 1]).get(perTag[i]);
                        transitionMap.get(perTag[i - 1]).put(perTag[i], curr + 1.0);
                    }
                }

                // Then, handle observations
                if (!observationMap.containsKey(perTag[i])){ // if this is the first time we are seeing a state
                    HashMap<String, Double> wordMap = new HashMap<>();
                    wordMap.put(perWord[i], 1.0);
                    observationMap.put(perTag[i], wordMap);
                }
                else if (observationMap.containsKey(perTag[i]) && !observationMap.get(perTag[i]).containsKey(perWord[i])){  // We have seen the state before, but we have never seen the word in this state
                    observationMap.get(perTag[i]).put(perWord[i], 1.0);
                }
                else {  // we have previously seen the word in this state
                    double curr = observationMap.get(perTag[i]).get(perWord[i]);
                    observationMap.get(perTag[i]).put(perWord[i], curr + 1.0);
                }
            }
        }
        // We have created transition and observation maps with frequencies. We then iterate to change the frequencies to probabilities
        for (String state : transitionMap.keySet()){
            double stateTotal = getVal(transitionMap.get(state));  // gives us the total value for each state
            for (String otherState : transitionMap.get(state).keySet()){
                double curr = transitionMap.get(state).get(otherState);
                transitionMap.get(state).put(otherState, Math.log(curr/stateTotal));  // replace the frequencies with the log of the probability
            }
        }

        for (String state : observationMap.keySet()){
            double stateTotal = getVal(observationMap.get(state));  // gives us the total value for each state
            for (String otherState : observationMap.get(state).keySet()){
                double curr = observationMap.get(state).get(otherState);
                observationMap.get(state).put(otherState, Math.log(curr/stateTotal));  // replace the frequencies with the log of the probability
            }
        }

        for (String state : transitionMap.keySet()){  // put transition data into graph
            if (!mainGraph.hasVertex(state)) mainGraph.insertVertex(state);  // if state not already in graph, create state vertex
            for (String otherState : transitionMap.get(state).keySet()){
                if (!mainGraph.hasVertex(otherState)) mainGraph.insertVertex(otherState);  //if other state not in graph, create other state vertex
                mainGraph.insertDirected(state, otherState, transitionMap.get(state).get(otherState));  // insert directed edge between state and other state
            }
        }

        wordTrainer.close();
        tagTrainer.close();
    }

    /**
     *
     * @param sudiGraph  @param observationUpper  @param sentence  @return
     *
     * ALgorithm for viterbi implementation
     *
     * Say we have an AdjacencyMapGraph with states (str) as keys and maps of states it is connected with (str) to the values of each transition (double).
     * This is the SudiGraph.
     * We also have a stateMap with states as keys and maps of words to word values (double) as values.
     * create a list of maps to store calculations for every word in the sentence
     * Iterate until we have no words left in the sentence
     *
     * for the first word, currState = #
     * for each state # is connected to:
     * if the word is in the stateMap for that state, calculate score as the word value + SudiGraph edge label + 0.0
     * if the word is not in the stateMap for that state, word value = - 100.0
     * add to list of maps at index 0 a map of states to a map of the previous state (#) to the current value if such a map oes not already exist.
     * if such a map exists, put the state as key and the map of the previous state (#) to the current score for that state as value.
     *
     * For subsequent words, get keySet of map at list of maps index i - 1. This is the result for the previous word.
     * for each state in the keySet, get current value from list of maps at index i - 1.
     * for each other state the state has a directed edge with in the sudiGraph, calculate score as word value + edge label + current value
     * add to list of maps at index i a map of states to map of the previous state to the score obtained if such a map doesn't already exist.
     * If a map exists, put the state as key and the map of the previous state to the obtained score only if the
     *      obtained score is better than the already existing score if there is an already existing score for that state.
     *
     * At the end (after iterating through all words in the sentence,
     * Iterate through list of maps but backwards
     * Get the last index map. Iterate over the maps values to find the best score
     * Get the key (previous state) for the best score. Add the current state to backpointers stack.
     * Go to the next (backwards) index and search for the previous state that produced the best score from the previous sentence.
     * Add that state to the stack.
     * Continue until we reach first index in the list of maps.
     */

    public static String viterbi(Graph<String, Double> sudiGraph, HashMap<String, HashMap<String, Double>> observationUpper, String sentence){
        String[] lineUpper = sentence.split(" ");
        String[] line = sentence.toLowerCase().split(" ");
        String start = "#";
        String firstWord = line[0];
        List<HashMap<String, HashMap<String, Double>>> mapList = new ArrayList<>();  //List of maps which we will use for backpointing. Contains a list of maps, with each map containing another map with key as present state and value a map of the state it came from to the score
        HashMap<String, HashMap<String, Double>> observations = new HashMap<>();  // copy map of observations, convert each word to lowercase

        // loop over map to copy
        for (String state : observationUpper.keySet()){
            HashMap<String, Double> eachState = new HashMap<>();
            for (String word : observationUpper.get(state).keySet()){
                eachState.put(word.toLowerCase(), observationUpper.get(state).get(word));
                observations.put(state, eachState);
            }
        }


        // First work with only the initial word
        for (String state : sudiGraph.outNeighbors(start)){ // for each state connected to the initial state
            double stateScore;
            if (observations.get(state).containsKey(firstWord)) stateScore = observations.get(state).get(firstWord) + sudiGraph.getLabel(start, state);  // calculate initial score
            else {stateScore = bigNum + sudiGraph.getLabel(start, state);}  // word not observed in the current state

            HashMap<String, Double> stateMapValue = new HashMap<>();
            stateMapValue.put(start, stateScore);

            if (mapList.size() == 0) {  // initialize map in the list of maps
                HashMap<String, HashMap<String, Double>> stateMap = new HashMap<>();
                stateMap.put(state, stateMapValue);
                mapList.add(stateMap);
            }
            else {
                mapList.get(0).put(state, stateMapValue);
            }
        }

        // Then work with other words until the sentence ends
        for (int i = 1; i <= line.length - 1; i++){  // iterate over words in sentence until last word
            String currWord = line[i]; // the word we are working with
            Map<String, HashMap<String, Double>> prevStates = mapList.get(i - 1);  //get map of states for previous word
            Set<String> mapSet = prevStates.keySet();  // set of current possible states for that word

            for (String state : mapSet){
                double currScore = 0.0;
                for (String prev : prevStates.get(state).keySet()) currScore = prevStates.get(state).get(prev); // get the score from the map


                for (String nextState : sudiGraph.outNeighbors(state)){  // iterate over the out neighbours of every viable state
                    double thisScore = bigNum;  // if the word is not observed in that state
                    if (observations.get(nextState).containsKey(currWord)) {  // if word is in observations map for the next state
                        thisScore = observations.get(nextState).get(currWord);  // get the word score as observed in that state
                    }
                    double newScore = currScore + thisScore + sudiGraph.getLabel(state, nextState);  // calculate score

                    if (mapList.size() <= i) {  // no map created yet in the list of maps for this new word
                        HashMap<String, Double> prevToScore = new HashMap<>();
                        prevToScore.put(state, newScore);
                        HashMap<String, HashMap<String, Double>> newMapToList = new HashMap<>();
                        newMapToList.put(nextState, prevToScore);
                        mapList.add(newMapToList);
                    }
                    else{
                     // keep only the best score per next state
                        if (!mapList.get(i).containsKey(nextState) || newScore > getVal(mapList.get(i).get(nextState)) ){ // if we have only one possible state to go to or this next state has a better score than all others
                            HashMap<String, Double> prevToScore = new HashMap<>();
                            prevToScore.put(state, newScore);
                            mapList.get(i).put(nextState, prevToScore);
                        }
                    }
                }
            }
        }

        // When we are done with words
        int i = mapList.size() - 1;
        double bestScore = -100.0;  // use relatively small number such that the first score must be greater than. Then, update this number with the first score
        String parentState = "";
        Stack<String> stateStack = new Stack<>();
        Map<String, HashMap<String, Double>> lastIndex = mapList.get(i);

        // get the best score of the last word
        for (String state : lastIndex.keySet()){
            for (String prevState : lastIndex.get(state).keySet()){
                if (lastIndex.get(state).get(prevState) > bestScore) {
                    bestScore = lastIndex.get(state).get(prevState);  // update scores with current best
                    parentState = state;  // update previous state of current best score
                }
            }
        }

        for (int num = i; num >= 0; num--){  // back pointers, add to stack
            String currState = "";
            for (String state : mapList.get(num).keySet()){
                if (Objects.equals(state, parentState)){  // since states do not have duplicates, we should find only one correct state
                    stateStack.add(state);
                    currState = getStrVal(mapList.get(num).get(state));

                }

            }
            parentState = currState;
        }

        // Build output string with tags
        String product = "";
        for (int idx = 0; idx <= lineUpper.length - 1; idx++){
            try {
                String tag = stateStack.pop();
                product += lineUpper[idx] + "/" + tag + " ";
                tags += tag + " ";
            }
            catch (EmptyStackException e) {
                //System.out.println("Is this really an English sentence?");

            }
        }

        return product;
    }

    // Method to iterate over a map which has key/value pair. Returns the sum of all values. For abstraction.
    public static Double getVal(HashMap<String, Double> map){
        double sol = 0.0;
        for (String key : map.keySet()){
            sol += map.get(key);
        }
        return sol;
    }

    // Method to iterate over a map which has only one key/value pair. Returns the key. For abstraction. Theta(1)
    public static String getStrVal(HashMap<String, Double> map){
        String sol = "";
        for (String key : map.keySet()){
            sol = key;
        }
        return sol;
    }

    public static void console(){
        // Console based testing
        boolean typeOn = true;
        while (typeOn){
            System.out.println("Enter sentence:");
            Scanner in = new Scanner(System.in);
            String sentence = in.nextLine();
            System.out.println(Sudi.viterbi(mainGraph, observationMap, sentence));
        }
    }

    public static void fileTest(String wordTest, String fileTest) throws IOException {
        BufferedReader testWords = new BufferedReader(new FileReader(wordTest));
        BufferedReader testTags = new BufferedReader(new FileReader(fileTest));

        String testString = ""; String testStringTag = "";
        String lineRead; String lineTagRead;
        while ((lineRead = testWords.readLine()) != null && (lineTagRead = testTags.readLine()) != null) {

            Sudi.viterbi(mainGraph, observationMap, lineRead);
            testStringTag += lineTagRead;  // read the tags per line

        }

        String[] tagList = testStringTag.split("[^A-Z]+");
        String[] othertagList = Sudi.tags.split("[^A-Z]+");
        int wrongNums = 0;

        for (int i=0; i < tagList.length; i++) {
            if (Objects.equals(tagList[i], othertagList[i])) {
                continue;
            }
            wrongNums++;
        }

        System.out.println("The wrong tags are " +wrongNums+ " out of " +tagList.length);
    }

    public static void main(String[] args) throws IOException {
        Sudi.trainModel("texts/simple-train-sentences.txt", "texts/simple-train-tags.txt");
        Sudi.console();
        //Sudi.fileTest("texts/simple-test-sentences.txt", "texts/simple-test-tags.txt");

    }
}
