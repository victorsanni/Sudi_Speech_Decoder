import java.util.*;

/**
 * @author Victor Sanni Fall 2021
 * Hardcoded small tests to determine the accuracy of my model
 */
public class SudiTest {

    public static void main(String[] args) {
        Graph<String, Double> mainGraph = new AdjacencyMapGraph<>();
        mainGraph.insertVertex("#");
        mainGraph.insertVertex("ADJ");
        mainGraph.insertVertex("ADV");
        mainGraph.insertVertex("CNJ");
        mainGraph.insertVertex("DET");
        mainGraph.insertVertex("EX");
        mainGraph.insertVertex("FW");
        mainGraph.insertVertex("MOD");
        mainGraph.insertVertex("N");
        mainGraph.insertVertex("NP");
        mainGraph.insertVertex("NUM");
        mainGraph.insertVertex("PRO");
        mainGraph.insertVertex("P");
        mainGraph.insertVertex("TO");
        mainGraph.insertVertex("UH");
        mainGraph.insertVertex("V");
        mainGraph.insertVertex("VD");
        mainGraph.insertVertex("VG");
        mainGraph.insertVertex("VN");
        mainGraph.insertVertex("WH");

        mainGraph.insertDirected("#", "NP", -1.6);
        mainGraph.insertDirected("#", "DET", -0.9);
        mainGraph.insertDirected("#", "PRO", -1.2);
        mainGraph.insertDirected("#", "MOD", -2.3);

        mainGraph.insertDirected("NP", "VD", -0.7);
        mainGraph.insertDirected("NP", "V", -0.7);

        mainGraph.insertDirected("DET", "N", 0.0);

        mainGraph.insertDirected("VD", "DET", -1.1);
        mainGraph.insertDirected("VD", "PRO", -0.4);

        mainGraph.insertDirected("N", "VD", -1.4);
        mainGraph.insertDirected("N", "V", -0.3);

        mainGraph.insertDirected("PRO", "VD", -1.6);
        mainGraph.insertDirected("PRO", "V", -0.5);
        mainGraph.insertDirected("PRO", "MOD", -1.6);

        mainGraph.insertDirected("V", "DET", -0.2);
        mainGraph.insertDirected("V", "PRO", -1.9);

        mainGraph.insertDirected("MOD", "PRO", -0.7);
        mainGraph.insertDirected("MOD", "V", -0.7);

        HashMap<String, HashMap<String, Double>> observations = new HashMap<>();

        HashMap<String, Double> npObs = new HashMap<>();
        npObs.put("Jobs", -0.7); npObs.put("Will", -0.7);
        observations.put("NP", npObs);

        HashMap<String, Double> detObs = new HashMap<>();
        detObs.put("a", -1.3); detObs.put("many", -1.7); detObs.put("one", -1.7); detObs.put("the", -1.0);
        observations.put("DET", detObs);

        HashMap<String, Double> vdObs = new HashMap<>();
        vdObs.put("saw", -1.1); vdObs.put("were", -1.1); vdObs.put("wore", -1.1);
        observations.put("VD", vdObs);

        HashMap<String, Double> nObs = new HashMap<>();
        nObs.put("color", -2.4); nObs.put("cook", -2.4); nObs.put("fish", -1.0); nObs.put("jobs", -2.4);
        nObs.put("mine", -2.4); nObs.put("saw", -1.7); nObs.put("uses", -2.4);
        observations.put("N", nObs);

        HashMap<String, Double> proObs = new HashMap<>();
        proObs.put("I", -1.9); proObs.put("many", -1.9); proObs.put("me", -1.9);
        proObs.put("mine", -1.9);  proObs.put("you", -0.8);
        observations.put("PRO", proObs);

        HashMap<String, Double> vObs = new HashMap<>();
        vObs.put("color", -2.1); vObs.put("cook", -1.4); vObs.put("eats", -2.1);
        vObs.put("fish", -2.1);  vObs.put("has", -1.4); vObs.put("uses", -2.1);
        observations.put("V", vObs);

        HashMap<String, Double> modObs = new HashMap<>();
        modObs.put("can", -0.7); modObs.put("will", -0.7);
        observations.put("MOD", modObs);

        Set<String> setTest = new HashSet<>();
        setTest.add("I fish"); setTest.add("Will eats the fish");
        setTest.add("Will you cook the fish"); setTest.add("One cook uses a saw");
        setTest.add("A saw has many uses"); setTest.add("You saw me color a fish");
        setTest.add("Jobs wore one color"); setTest.add("The jobs were mine");
        setTest.add("The mine has many fish"); setTest.add("You can cook many");

        // My own sentences
        setTest.add("Can I cook");  // works well
        setTest.add("Can you color a fish"); // works well
        setTest.add("Cook a fish");  // does not work, verb starts sentence, unusual situation
        setTest.add("Can a cook fish");  // does not work, modifier is not directly connected to determiner

        for (String sentence : setTest){
            String modified = Sudi.viterbi(mainGraph, observations, sentence);
            System.out.println(modified);
        }
    }
}
