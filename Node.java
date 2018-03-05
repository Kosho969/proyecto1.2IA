import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Node {
    String identifier;

    Map<Set<String>,Object> probabilities;
    Map<Set<String>,Object> factor = new HashMap<Set<String>, Object>();

    Set<String> dependencies;

    public Node(String id) {
        this.identifier = id;
        this.probabilities = new HashMap<Set<String>, Object>();
        this.dependencies = new HashSet<String>(); 
    }

    public Node() {
        this.identifier = "";
        this.probabilities = new HashMap<Set<String>, Object>();
        this.dependencies = new HashSet<String>();
    }

    public Node(String id, Set<String> dependencies) {
        this.identifier = id;
        this.probabilities = new HashMap<Set<String>, Object>();
        this.dependencies = dependencies; 
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<Set<String>, Object> getProbabilities() {
        return probabilities;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public void addDependency(String dependency) {
        this.dependencies.add(dependency);
    }

    public void addProbability(Set<String> key, double value) {
        this.probabilities.put(key,value);
    }

    @Override
    public String toString() {
        return "Node:\n"
                + "ID: "+ identifier + "\n"
                + "Probabilities: "+ probabilities + "\n"
                + "Dependencies: "+ dependencies + "\n \n";
    }

    // TODO: Completar
    // public boolean validarCompletudProbabilidades();

    public void getFactorAsString(){
    	
        for (Map.Entry<Set<String>, Object> probability: probabilities.entrySet()){
        	double real = (double)probability.getValue();
        	Set<String> setPositive = new LinkedHashSet<String>();
        	Set<String> setNegative = new LinkedHashSet<String>();
        	if((dependencies.isEmpty())== false){
        		setPositive.add(this.identifier);
        		setNegative.add("!"+this.identifier);
        		setNegative.addAll(probability.getKey());
        	}else{
        		setNegative.add("!"+this.identifier);
        	}
        	
        	setPositive.addAll(probability.getKey());
        	
        	factor.put(setPositive, real);
        	factor.put(setNegative, 1-real);
        	//System.out.println(factor);
        }
        System.out.println(factor);
    }
}
