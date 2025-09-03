package de.unibonn.realkd.algorithms.emm;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.table.attribute.DefaultMetricAttribute;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * Proposition predicate for EMM algorithms that wish to filter out propositions
 * that relate to their target attributes.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.2.1
 * 
 */
//SVL filters propositions that have no overlap with an IndexSet
public class supportPropositionFilter implements Predicate<Proposition> {

    private final Parameter<List<Attribute<?>>> targetAttributesParameter;

    private final Parameter<String> qualityfunctionparams;

    private final Parameter<String> hardcutoffparams;

    public supportPropositionFilter(Parameter<List<Attribute<?>>> targetAttributesParameter, Parameter<String> qualityfunctionparams, Parameter<String> hardcutoffparams) {
	this.targetAttributesParameter = targetAttributesParameter;
	this.qualityfunctionparams = qualityfunctionparams;
	this.hardcutoffparams = hardcutoffparams;
	}

	/**
	 * @return false if and only if proposition relates to current value of
	 *         target attributes parameter
	 */
    
	public boolean test(Proposition proposition) {
		if (!(proposition instanceof AttributeBasedProposition)) {
			return true;
		}
		if ((hardcutoffparams.current().split("\\s+")).length==0) {
		    return true;
		}
		List<String> cutparams = new ArrayList<>();
                String[] split = hardcutoffparams.current().split("\\s+");
                for (String str : split) {
		    cutparams.add(str);
                }

		List<Double> qfparams = new ArrayList<>();
		split = qualityfunctionparams.current().split("\\s+");
		for (String str : split) {
		    qfparams.add(Double.parseDouble(str));
		}

		if (cutparams.size()>qfparams.size()) {
		    //System.out.println("SVL cutparams.size()>qfparams.size() "+(cutparams.size()>qfparams.size()));
		    return true;
		}

		List<Attribute<?>> targetAttributes = targetAttributesParameter.current();
		if (cutparams.size()>targetAttributes.size()) {
		    //System.out.println("SVL cutparams.size()>targetAttributes.size()"+(cutparams.size()>targetAttributes.size()));
		    return true;
		}

		IndexSet intersec = proposition.supportSet();
		if (intersec.size()==0) return false;
		IndexSet intersec2 = null;
		int qfparamindex = 0;
		double border = 0.0;
		double upper = 0.0;
		double lower = 0.0;
		IndexSet attrsupport = null;
		for (int i = 0; i<cutparams.size(); i++) {
		    if (!(targetAttributes.get(i) instanceof DefaultMetricAttribute))
			return true;

		    switch (cutparams.get(i)) {
		    case "above" :
			if (qfparamindex>qfparams.size()-1) return true; 
			border = (qfparams.get(qfparamindex)).doubleValue();
			qfparamindex++;
			attrsupport = ((DefaultMetricAttribute) targetAttributes.get(i)).abovethresh(border);
			intersec2 = IndexSets.intersection(intersec, attrsupport);
			intersec = intersec2;
			break;
		    case "below" :
			if (qfparamindex>qfparams.size()-1) return true;
			border = (qfparams.get(qfparamindex)).doubleValue();
			qfparamindex++;
			attrsupport = ((DefaultMetricAttribute) targetAttributes.get(i)).belowthresh(border);
			intersec2 = IndexSets.intersection(intersec, attrsupport);
			intersec = intersec2;
			break;
		    case "within" :
			if (qfparamindex>qfparams.size()-2) return true;
			upper = new Double(qfparams.get(qfparamindex));
			lower = new Double(qfparams.get(qfparamindex+1));
			qfparamindex = qfparamindex + 2;
		        attrsupport = ((DefaultMetricAttribute) targetAttributes.get(i)).withininterval(upper, lower);
			intersec2 = IndexSets.intersection(intersec, attrsupport);
			intersec = intersec2;
			/*for (int j : attrsupport){
                            double val1 = ((Attribute<Double>) targetAttributes.get(i)).value(j).doubleValue();
			    System.out.println("SVL within "+proposition.name()+" "+val1);
			    }*/
			break;
		    default :
			qfparamindex++;
		    }
		    if (intersec.size()==0) {
			/*double max = 0.0;
			for (int j : proposition.supportSet()){
			    double val1 = ((Attribute<Double>) targetAttributes.get(i)).value(j).doubleValue();
			    if(i==1) {
				System.out.println("SVL runprop "+proposition.name()+" "+val1+" "+upper+" "+lower);
				if (val1 > upper)
				    System.out.println("SVL runprop2 "+IndexSets.intersection(proposition.supportSet(),((DefaultMetricAttribute) targetAttributes.get(0)).abovethresh(border)).size());
			    }
			    max = Math.max(max, val1);
			    }*/
			//System.out.println("SVL prop "+proposition.name()+" "+proposition.supportSet().size()+" "+i+" "+intersec2.size());
			return false;
		    }
		}
		//System.out.println("SVL allowed proposition "+proposition.name());
		return true;
	}

}
