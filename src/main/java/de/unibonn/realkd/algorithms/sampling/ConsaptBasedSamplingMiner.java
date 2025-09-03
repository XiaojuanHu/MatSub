/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.unibonn.realkd.algorithms.sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ForwardingSortedSet;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.Propositions;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;
import edu.uab.consapt.sampling.TwoStepPatternSampler;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import static de.unibonn.realkd.common.IndexSets.intersection;
import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupSampler;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.algorithms.emm.EMMParameters;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.algorithms.emm.TargetAttributePropositionFilter;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;


/**
 * Algorithm that wraps consapt based sampling of patterns with subsequent local
 * optimization (via pruning).
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * @author Pavel Tokmakov
 * 
 * @version 0.6.0
 * 
 * @since 0.1.0
 *
 * @param <T>
 *            the type of patterns produced by this algorithm
 */
public class ConsaptBasedSamplingMiner<T extends Pattern<?>> extends AbstractMiningAlgorithm<T> {

	/**
	 * Mutable result data structure with limited size that allows efficient
	 * elimination of duplicates as well as efficient expulsion of worst element
	 * (in order to update with better element).
	 *
	 */
	private static class ResultSet<T extends Pattern<?>> extends ForwardingSortedSet<T> {

		private final int maximumSize;

		private final TreeSet<T> delegate;

		public ResultSet(Comparator<Pattern<?>> order, int maximumSize) {
			/*
			 * have to wrap comparator to break ties of non-identical objects
			 * because otherwise TreeSet would discard non-identical patterns
			 * with same quality
			 */
			Comparator<? super T> identityAwareComparator = (p, q) -> {
				int primaryComparison = order.compare(p, q);
				return (primaryComparison != 0) ? primaryComparison : p.equals(q) ? 0 : p.hashCode() - q.hashCode();
			};
			this.delegate = new TreeSet<>(identityAwareComparator);
			this.maximumSize = maximumSize;
		}

		@Override
		protected SortedSet<T> delegate() {
			return delegate;
		}

		@Override
		public boolean add(T p) {
			if (size() < maximumSize) {
				return delegate.add(p);
			}
			T last = delegate.last();
			if (delegate.comparator().compare(last, p) <= 0) {
				return false;
			}
			delegate.pollLast();
			return delegate.add(p);
		}

	    //@Override
                public void removeLast() {
                        delegate.pollLast();
                }

	    public void removeFirst() {
                        delegate.pollFirst();
                }
	    //@Override
                public SortedSet<T> patterns() {
                        return delegate;
                }

	    @Override
	    public T first() {
		return delegate.first();
                }

	    @Override
            public T last() {
                return delegate.last();
                }

	     @Override
            public int size() {
                return delegate.size();
                }

	    @Override
	    public void clear() {
		delegate.clear();
	    }

	}

	// private static final Logger LOGGER =
	// Logger.getLogger(ConsaptBasedSamplingMiner.class.getName());

	private final Function<LogicalDescriptor, ? extends T> toPattern;
	private final Function<? super T, LogicalDescriptor> toDescriptor;
	private final Integer numberOfResults;
	private final Integer numberOfSeeds;
	private final SinglePatternPostProcessor postProcessor;
	private final PropositionalContext propositionalLogic;
	private final PatternOptimizationFunction targetFunction;
	private final TwoStepPatternSampler sampler;
	private final BiFunction<LogicalDescriptor, ? super T, ? extends T> toPatternWithPrevious;

	public ConsaptBasedSamplingMiner(Function<LogicalDescriptor, T> toPattern,
			Function<Pattern<?>, LogicalDescriptor> toDescriptor, PropositionalContext propositionalLogic,
			TwoStepPatternSampler consaptSampler, PatternOptimizationFunction targetFunction,
			SinglePatternPostProcessor postProcessor, Integer numberOfResults, Integer numberOfSeeds) {
		this(toPattern, (d, p) -> toPattern.apply(d), toDescriptor, propositionalLogic, consaptSampler, targetFunction,
				postProcessor, numberOfResults, numberOfSeeds);
	}

	public ConsaptBasedSamplingMiner(Function<LogicalDescriptor, T> toPattern,
			BiFunction<LogicalDescriptor, ? super T, ? extends T> toPatternWithPrevious,
			Function<? super T, LogicalDescriptor> toDescriptor, PropositionalContext propositionalLogic,
			TwoStepPatternSampler consaptSampler, PatternOptimizationFunction targetFunction,
			SinglePatternPostProcessor postProcessor, Integer numberOfResults, Integer numberOfSeeds) {

		this.toPattern = toPattern;
		this.toPatternWithPrevious = toPatternWithPrevious;
		this.toDescriptor = toDescriptor;

		this.propositionalLogic = propositionalLogic;
		this.postProcessor = postProcessor;
		this.sampler = consaptSampler;
		this.targetFunction = targetFunction;
		this.numberOfResults = numberOfResults;
		this.numberOfSeeds = numberOfSeeds;
	}

	public final PropositionalContext getPropositionalLogic() {
		return propositionalLogic;
	}

	@Override
	protected void onStopRequest() {
		if (this.sampler != null) {
			this.sampler.setStop(true);
		}
	}

	public int numberOfResults() {
		return numberOfResults;
	}

    public synchronized int increment(int c) {
        c++;
	return c;
    }

	public final Collection<T> concreteCall() {
		ResultSet<T> results = new ResultSet<T>(targetFunction.preferenceOrder(), numberOfResults);
		//Set<Future<ResultSet<T>>> parres = new HashSet<Future<ResultSet<T>>>();
		List<ResultSet<T>> parres = Collections.synchronizedList(new ArrayList<>());
		int numthreads = ExceptionalSubgroupSampler.numberOfThreadsParameter();
		int numSeeds = numberOfSeeds/numthreads;
		Runnable task = () -> {
		    ResultSet<T> myres = new ResultSet<T>(targetFunction.preferenceOrder(), numberOfResults);
			    int seedCounter = 0;
		    //long mytask = Thread.currentThread().getId();
		while (!stopRequested() && seedCounter < numSeeds) {
		    
			// sample plain set of proposition indices through unsafe API
		    try {
                        PlainItemSet plainPattern = sampler.getNext();
			if (stopRequested()) {
				continue;
			}

			List<Proposition> propositions = new ArrayList<>();
			for (PlainItem item : plainPattern) {
				Proposition proposition = propositionalLogic.propositions().get(Integer.parseInt(item.getName()));
				propositions.add(proposition);
			}

			LogicalDescriptor description = LogicalDescriptors.create(propositionalLogic.population(), propositions);
			T pattern = toPattern.apply(description);
			T pruned = postProcessor.prune(pattern, targetFunction, toPatternWithPrevious, toDescriptor);
			myres.add(pruned);
			//System.out.println("SVL: test quality "+targetFunction.apply(pruned).doubleValue()+" "+targetFunction.apply(myres.first()).doubleValue());

			if (targetFunction.apply(myres.first()).doubleValue()>0.0)
			    if (targetFunction.apply(pruned).doubleValue() > 0.001*targetFunction.apply(myres.first()).doubleValue()) { 
				//System.out.println("SVL Search for the best subgroup progress "+seedCounter);
				seedCounter++;
			}
		    }//try
		    catch (NullPointerException e) {
			continue;
		    }//catch
		}//while
		    parres.add(myres);
		    double mybest = targetFunction.apply(myres.first()).doubleValue();
		    System.out.println("best quality: "+mybest);
		};
		ExecutorService myexec = Executors.newFixedThreadPool(numthreads);
                for (int m = 0; m <numthreads; m++)
                    myexec.execute(task);
                try {
                    System.out.println("attempt to shutdown executor");
                    myexec.shutdown();
                    myexec.awaitTermination(365, TimeUnit.DAYS);
                }
                catch (InterruptedException e) {
                    System.err.println("tasks interrupted");
                }
                finally {
                    if (!myexec.isTerminated()) {
                        System.err.println("cancel non-finished tasks");
                    }
                    myexec.shutdownNow();
                    System.out.println("shutdown finished");
                }
                for (int p = 0; p<numthreads; p++){
                    for (T patcount : parres.get(p).patterns())
                        results.add(patcount);
                }

		//SVL: orthogonal subgroups start here
		Set<IndexSet> bestsupports = new HashSet<IndexSet>();
		List<Integer> decoy = Collections.synchronizedList(new ArrayList<>());
		int diffres = numberOfResults-1;
		if (numberOfResults > 5) {diffres = 0;}
		for (int j = 0; j < diffres; j++) {
		    System.out.println("SVL seeking orthogonal subgroup "+(j+1));
		    for (int i = 1; i < numberOfResults-j; i++)
			results.removeLast();
		    for (int q = 0; q < numthreads; q++)
			parres.get(q).clear();

		    int resindex = -1;
		    T ref = results.last();
		    bestsupports.clear();
		    for (T pati : results.patterns())
			bestsupports.add(toDescriptor.apply(pati).supportSet());
		    decoy.clear();
		Runnable task2 = () -> {
		    decoy.add(1);
		    int myindex = decoy.size()-1;
		    System.out.println("SVL: myindex "+myindex);
		    int seedCounter = 0;
		//LogicalDescriptor refdescriptor = toDescriptor.apply(ref);
		//if(j==0){
		//    IndexSet refsupport = toDescriptor.apply(ref).supportSet();
		//}
		//else {
		//    IndexSet refsupport = IndexSets.union(refsupport,toDescriptor.apply(ref).supportSet());
		//}
		while (!stopRequested() && seedCounter < numSeeds ) {

                        // sample plain set of proposition indices through unsafe API
		    try {
                        PlainItemSet plainPattern = sampler.getNext();
                        if (stopRequested()) {
                                continue;
                        }

                        List<Proposition> propositions = new ArrayList<>();
                        for (PlainItem item : plainPattern) {
                                Proposition proposition = propositionalLogic.propositions().get(Integer.parseInt(item.getName()));
                                propositions.add(proposition);
                        }

                        LogicalDescriptor description = LogicalDescriptors.create(propositionalLogic.population(), propositions);
                        T pattern = toPattern.apply(description);
                        T pruned = postProcessor.prune(pattern, targetFunction, toPatternWithPrevious, toDescriptor);
			//System.out.println("compare3 "+targetFunction.preferenceOrder().compare(ref,results.first()));
			IndexSet prunesupport = toDescriptor.apply(pruned).supportSet();
			boolean isdifferent = true;
			IndexSet intersec = prunesupport;
			for (IndexSet refsupport : bestsupports) {
			    intersec = IndexSets.intersection(refsupport, prunesupport);
			    double norm = new Double(Math.min(refsupport.size(),prunesupport.size()));
			    isdifferent = isdifferent && intersec.size()/norm<0.5;
			    //System.out.println("compare "+ intersec.size()+" prune size "+prunesupport.size()+" different? "+isdifferent);                       
			}
			if(isdifferent) {
			    parres.get(myindex).add(pruned);
			    if(targetFunction.preferenceOrder().compare(ref,pruned) > 0){
				System.out.println("SVL A better pattern is found at "+ seedCounter);
			    }
			    if (targetFunction.apply(parres.get(myindex).first()).doubleValue()>0.0)
				if(targetFunction.apply(pruned).doubleValue() > targetFunction.apply(parres.get(myindex).first()).doubleValue()*0.001) {
				    seedCounter++;
				}
			    if(myindex == 0)
				System.out.println("SVL Search for orthogonal subgroups progress: "+seedCounter+" seeds out of "+numSeeds);
			}
		    }//try
		    catch (NullPointerException e) {
			continue;
		    }//catch
		} //while cycle
		    };//task2

		ExecutorService myexec2 = Executors.newFixedThreadPool(numthreads);
                for (int m = 0; m <numthreads; m++)
                    myexec2.execute(task2);
                try {
                    System.out.println("attempt to shutdown executor");
                    myexec2.shutdown();
                    myexec2.awaitTermination(365, TimeUnit.DAYS);
                }
                catch (InterruptedException e) {
                    System.err.println("tasks interrupted");
                }
                finally {
                    if (!myexec2.isTerminated()) {
                        System.err.println("cancel non-finished tasks");
                    }
                    myexec2.shutdownNow();
                    System.out.println("shutdown finished");
                }

		for (int p = 0; p<numthreads; p++){
                    for (T patcount : parres.get(p).patterns())
                        results.add(patcount);
                }
		
                } //j cycle
		//SVL orthogonal subgroups end here
		
		double best = targetFunction.apply(results.first()).doubleValue();
		double worst = targetFunction.apply(results.last()).doubleValue();
		System.out.println("best and worst quality: "+best+", "+worst);
		SortedSet<T> bestpatterns = results.patterns();
		int patcount=0;
		boolean implied = false;
		boolean implies = false;
		Set<Proposition> proptemp = new HashSet<Proposition>();
		Set<Proposition> proptemp2 = new HashSet<Proposition>();
		Set<Proposition> allpropset = new HashSet<Proposition>();
		allpropset.addAll(propositionalLogic.propositions());
		List<Proposition> allproparray = new ArrayList<>();
		allproparray.addAll(allpropset);
		double degeneracy_thresh = 0.999999999;
		/*Parameter<DataTable> datatableParameter = MiningParameters.dataTableParameter(Workspaces.workspace());
		Parameter<List<Attribute<?>>> targets = EMMParameters.getEMMTargetAttributesParameter(datatableParameter);
		Predicate<Proposition> targetFilter = new TargetAttributePropositionFilter(datatableParameter, targets);
		Parameter<Set<Attribute<? extends Object>>> descriptorAttributeFilterParameter =
		    EMMParameters.getEMMDescriptorAttributesParameter(datatableParameter,targets);
                Predicate<Proposition> additionalAttributeFilter = prop -> !((prop instanceof AttributeBasedProposition)
                                && descriptorAttributeFilterParameter.current()
                                                .contains(((AttributeBasedProposition<?>) prop).attribute()));
		Predicate<Proposition> filterPredicate = additionalAttributeFilter.and(targetFilter);
		allpropset.addAll(propositionalLogic.propositions().stream().filter(filterPredicate).collect(Collectors.toList()));*/
		for(T pat : bestpatterns) {
		    patcount++;
		    System.out.println("Subgroup "+patcount);
		    double patqual = targetFunction.apply(pat).doubleValue();
		    LogicalDescriptor descr = toDescriptor.apply(pat);
		    //IndexSet patsupp = descr.supportSet();
		    int patsize = descr.supportSet().size();
		    Collection<Proposition> props = descr.elements();
		    for (Proposition propcurr : props) {
			proptemp.clear();
			LogicalDescriptor descrminus = descr.generalization(propcurr);
			T patminus = toPattern.apply(descrminus);
			//IndSystem.out.println("First in pair "+propcurr.name());exSet patminussupp = descrminus.supportSet();
			//IndexSet intersec = IndexSets.intersection(patsupp, patminussupp);
			double patminussize = new Double(descrminus.supportSet().size());
			double propscore = 1-patsize/patminussize;
			double propqualscore = 1-targetFunction.apply(patminus).doubleValue()/patqual;
			System.out.println("proposition "+propcurr.name()+": support score "+propscore+", quality score "+propqualscore);
			System.out.println("Degenerate propositions:");
			double propcursize = new Double(propcurr.supportSet().size());
			//for (int j = 0; j < propositionalLogic.propositions().size(); j++) {
			//Proposition proposition = propositionalLogic.propositions().get(j);
			for (Proposition proptest3 : allpropset) {
			    if (((AttributeBasedProposition<?>) propcurr).attribute().caption().
				equals(((AttributeBasedProposition<?>) proptest3).attribute().caption()))
				allproparray.remove(proptest3);
			}
			for (Proposition proposition : allproparray) {
			    IndexSet overlap = IndexSets.intersection(propcurr.supportSet(), proposition.supportSet());
			    int overlapsize = overlap.size();
			    double maxsize = new Double(Math.max(propcursize,proposition.supportSet().size()));
			    double score = overlapsize/maxsize;
			    LogicalDescriptor descrreplace = descrminus.specialization(proposition);
			    T patreplace = toPattern.apply(descrreplace);
			    int patreplacesize = descrreplace.supportSet().size();
			    IndexSet intersec = IndexSets.intersection(descr.supportSet(),descrreplace.supportSet());
			    double maxreplace = new Double(Math.max(patsize,patreplacesize));
			    double replacescore = intersec.size()/maxreplace;
			    if (replacescore>degeneracy_thresh ) {
				implied = false;
				for (Proposition proptest : proptemp) {
				    implied = implied || proptest.implies(proposition);
				    //if(proptest.implies(proposition))
					//System.out.println(proptest.name()+" implies "+proposition.name());
				}
				if(!(implied)){
				    proptemp2.clear();
				    proptemp2.addAll(proptemp);
				    for (Proposition proptest2 : proptemp2)
					if(proposition.implies(proptest2))
					    proptemp.remove(proptest2);
				    proptemp.add(proposition);
				    //System.out.println(proposition.name()+" replacement in subgroup score "+replacescore);
				}
				
			    }// if (replacescore>degeneracy_thresh )
			}//for (Proposition proposition : allproparray)
			for (Proposition prop : proptemp)
			    System.out.println(prop.name());
			allproparray.clear();
			allproparray.addAll(allpropset);
			System.out.println("");
		    }//propcurr: prop
		}//T pat : bestpatterns

		System.out.println("Two-proposition degeneracies");
		allproparray.clear();
		allproparray.addAll(allpropset);
		int allpropsize = allproparray.size();
		//System.out.println("SVL allproparray size before "+allpropsize);
		patcount = 0;
		List<Proposition> proprem = new ArrayList<>();
		Set<Proposition> propselect1 = new HashSet<>();
		Set<Proposition> propselect2 = new HashSet<>();
		for(T pat : bestpatterns) {
                    patcount++;
                    System.out.println("Subgroup "+patcount);
		    LogicalDescriptor descr = toDescriptor.apply(pat);
                    int patsize = descr.supportSet().size();
		    List<Proposition> props = new ArrayList<>();
                    props.addAll(descr.elements());
		    int propssize = props.size();
		    if (propssize < 2) {
			System.out.println("Subgroup has size < 2");
		    }
		    else{
			for (int i = 0; i<propssize; i++) {
			    Proposition propcurr = props.get(i);
			    LogicalDescriptor descrminus = descr.generalization(propcurr);
			    for (Proposition proptest3 : allpropset) {
				if (((AttributeBasedProposition<?>) propcurr).attribute().caption().
				    equals(((AttributeBasedProposition<?>) proptest3).attribute().caption())){
				    allproparray.remove(proptest3);
				}
			    }//for (Proposition proptest3 : allpropset)
			    //allproparray.remove(propcurr);
			    for (int j=i+1; j<propssize; j++) {
				Proposition propcurr2 = props.get(j);
				proprem.clear();
				for (Proposition proptest3 : allpropset) {
				    if (((AttributeBasedProposition<?>) propcurr2).attribute().caption().
					equals(((AttributeBasedProposition<?>) proptest3).attribute().caption())){
					allproparray.remove(proptest3);
					proprem.add(proptest3);
				    }
				}//for (Proposition proptest3 : allpropset)
				//allproparray.remove(propcurr2);
				allpropsize = allproparray.size();
				//System.out.println("SVL allproparray size after "+allpropsize);
				propselect1.clear();
				propselect2.clear();
				for (int k = 0; k<allpropsize; k++) {
				    Proposition prop1 = allproparray.get(k);
				    proptemp.clear();
				    for (int l = k; l<allpropsize; l++) {
					Proposition prop2 = allproparray.get(l); 
					LogicalDescriptor descrreplace = ((descrminus.generalization(propcurr2)).specialization(prop1)).specialization(prop2);
					int patreplacesize = descrreplace.supportSet().size();
					IndexSet intersec = IndexSets.intersection(descr.supportSet(),descrreplace.supportSet());
					double maxreplace = new Double(Math.max(patsize,patreplacesize));
					double replacescore = intersec.size()/maxreplace;
					if (replacescore>degeneracy_thresh) {
					    implied = false;
					    for (Proposition proptest : proptemp) {
						implied = implied || proptest.implies(prop2);
					    }
					    if(!(implied)){
						proptemp2.clear();
						proptemp2.addAll(proptemp);
						for (Proposition proptest2 : proptemp2)
						    if(prop2.implies(proptest2))
							proptemp.remove(proptest2);
						proptemp.add(prop2);
					    }
					}//if (replacescore>degeneracy_thresh && !(proposition.name().equals(propcurr.name())))                           
				    }//prop2 : allpropset
				    propselect2.addAll(proptemp);
				    if(!(proptemp.isEmpty()))
					propselect1.add(prop1);
				}//prop1 : allpropset
				allproparray.addAll(proprem);
				for (Proposition propcount2 : propselect2) {
				    proptemp.clear();
				    for (Proposition propcount1 : propselect1) {
					LogicalDescriptor descrreplace =
					    ((descrminus.generalization(propcurr2)).specialization(propcount1)).specialization(propcount2);
                                        int patreplacesize = descrreplace.supportSet().size();
                                        IndexSet intersec = IndexSets.intersection(descr.supportSet(),descrreplace.supportSet());
                                        double maxreplace = new Double(Math.max(patsize,patreplacesize));
                                        double replacescore = intersec.size()/maxreplace;
                                        if (replacescore>degeneracy_thresh) {
                                            implied = false;
                                            for (Proposition proptest : proptemp) {
                                                implied = implied || proptest.implies(propcount1);
                                            }
                                            if(!(implied)){
                                                proptemp2.clear();
                                                proptemp2.addAll(proptemp);
                                                for (Proposition proptest2 : proptemp2)
                                                    if(propcount1.implies(proptest2))
                                                        proptemp.remove(proptest2);
                                                proptemp.add(propcount1);
                                            }
                                        }
				    }
				    for (Proposition proptest3 : proptemp)
                                        System.out.println("("+propcurr.name()+")"+"AND"+"("+propcurr2.name()+")"+" replaced by "+
                                                           "("+propcount2.name()+")"+"AND"+"("+proptest3.name()+")");
				}//(Proposition propcount2 : propselect2)
			    }//propcurr2 : descrminus.elements()
			    allproparray.clear();
			    allproparray.addAll(allpropset);
			}//for (Proposition propcurr : props)
		    }//!(props.size()<2)
		}//for(T pat : bestpatterns)
		
		return results;
	}

	@Override
	public String caption() {
		return "Consapt based sampling algorithm";
	}

	@Override
	public String description() {
		return "General consapt based sampling algorithm that can be wrapped by more specific implementations";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.OTHER;
	}

}
