package ticsantee.smarthome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

public class SmartHome {

	/**
	 * @param args
	 */
	
	static class Debug extends BaseBuiltin {
		public String getName(){
			return "Debug";
		}
		public int getArgLength(){
			return 0;
		}
		public void headAction(Node[] args, int length, RuleContext context){
			
			System.out.println("the rule is running");
		}
		public boolean bodyCall(Node[] args, int length, RuleContext context)	{
			return true;
		}
	}
	

	public static OntModel model ;

	public static void main(String[] args) {

		// Load the ontology into the program

		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		model.read("file:SmartHome.owl");

		Scanner in = null;
		do
		{
			System.out.println("Please enter a sensor event in the form of <sensor_name>.<event>");
			in = new Scanner(System.in);
			String event = in.nextLine();
			System.out.println(event);
			String sensor = event.split("\\.")[0];
			String state = sensor + "_" + event.split("\\.")[1];

			updateproperty(sensor,"hasCurrentState", state);

			reason();
		}
		while(in != null);
	}

	public static void reason()
	{
		BuiltinRegistry.theRegistry.register(new Debug());
		
		/*****************************************************/
		/**                 Clean Ontology	                **/
		/*****************************************************/
		
        removeProperty("johndoe", "detectedIn");
		
		/*****************************************************/
		/**          FirstReasoningStep :	Inference	    **/
		/*****************************************************/
		
		Resource configuration =model.createResource();
		configuration.addProperty(ReasonerVocabulary.PROPruleMode,"hybrid");

		configuration.addProperty(ReasonerVocabulary.PROPruleSet,"rules");

		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
		InfModel infModel = ModelFactory.createInfModel(reasoner,model);

		infModel.prepare();
		model.add(infModel);
		
		/*****************************************************/
		/**          SecondReasoningStep :	Query			**/
		/*****************************************************/
		
		System.out.println("query");
		
		String demoURI ="http://www.owl-ontologies.com/SmartHome.owl#";
		
		Resource a = model.getResource(demoURI + "johndoe");
		Property p = model.getProperty(demoURI, "detectedIn");
		StmtIterator i = infModel.listStatements(a, p, (RDFNode)null);
		while (i.hasNext()) {
			Resource node = i.nextStatement().getResource();
			StringTokenizer temp = new StringTokenizer(node.toString(),"#");
			String st = temp.nextToken();
		    st= temp.nextToken();
		    System.out.println("johndoe detected in " + st);
		}
	}

	public static void saveOWL(String file) {
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		model.write(fos);
	}

	public static Individual findIndividual(String IndividualName, OntModel model)
	{
		boolean find=false;
		Iterator<?> iInd=model.listIndividuals();
		Individual oInd=null;

		while((!find)&&(iInd.hasNext()))
		{
			oInd=(Individual) iInd.next();
			if (oInd.getLocalName().equalsIgnoreCase(IndividualName))
			{ 
				find=true;
				return oInd;
			}
		}
		if (oInd.getLocalName().equalsIgnoreCase(IndividualName)) return oInd; else return null ;
	}

	public static void updateproperty(String ind1, String prop, String ind2)
	{
		removeProperty(ind1,prop);
		addProperty(ind1, prop, ind2);
	}

	public static void addProperty(String ind1, String prop, String ind2)
	{	
		Individual ind1_=findIndividual(ind1,model);
		Individual ind2_=findIndividual(ind2,model);
		addProretieValueToIndividu(ind1_,ind1_.getNameSpace(), prop, ind2_,model);
		System.out.println("property added " + "ind1= " + ind1 + " prop= " + prop + " ind2= " + ind2);
	}

	public static void addpropertyvalue(String ind1, String prop, String value)
	{
		Individual ind1_ = findIndividual(ind1,model);
		addProretieValueToIndividu(ind1_,ind1_.getNameSpace(), prop, value,model);
		System.out.println("property added " + "ind1_= " + ind1_ + " prop= " + prop + " value= " + value);
	}

	public static void addProretieValueToIndividu(Individual ind,String nameSpace,String proprietyName,String proprietyValue, OntModel model)
	{
		OntProperty property = model.getOntProperty(nameSpace+proprietyName);
		ind.addProperty(property,proprietyValue); 	 
	}
	
	//	public void addProretieValueToIndividu(Individual ind,String nameSpace,String proprietyName,boolean proprietyValue, OntModel model)
	//	{
	//		
	//		OntProperty property = model.getOntProperty(nameSpace+proprietyName);
	//		System.out.println(property);
	//		System.out.println(ind);
	//		System.out.println(proprietyValue);
	//		SmartHome.model.add(ind, property, proprietyValue);
	//		//Bouha.model.createStatement(ind, property, proprietyValue);
	//		//Statement stmt = Bouha.model.createStatement(ind, property, proprietyValue);
	//		//Bouha.model.add(stmt);
	//	 	//ind.addProperty(property, proprietyValue);
	//	}
	
	public static void addProretieValueToIndividu(Individual ind,String nameSpace,String proprietyName,Individual proprietyValue, OntModel model)
	{
		OntProperty property = model.getOntProperty(nameSpace + proprietyName);
		System.out.println(property);
		System.out.println(proprietyValue);
		ind.addProperty(property,proprietyValue); 	 
	}

	public static void removeProperty(String individual, String property)
	{
		Individual ind = findIndividual(individual,model);

		OntProperty p1 = model.getOntProperty(ind.getNameSpace() + property);
		NodeIterator ni = ind.listPropertyValues(p1);
		if(ni.hasNext())
		{
			RDFNode node1 = ni.nextNode();
			System.out.println("find delete " + "p1= " + p1 + " node1= " + node1);
			ind.removeProperty(p1,node1);
		}
	}

	public static void getPropertyValue(String userName, String propertyName){		

		Individual ind = findIndividual(userName,model);
		if (ind != null){
			OntProperty p = model.getOntProperty(ind.getNameSpace()+propertyName);
			NodeIterator ni = ind.listPropertyValues(p);
			while (ni.hasNext()) {
				System.out.println(userName + " " + propertyName + " " + nameSpaceToName(ni.nextNode().toString()));
			}
		}
	}

	public static String nameSpaceToName(String s) {
		return s.toString().substring(s.toString().indexOf("#")+1,s.toString().length());
	}

}
