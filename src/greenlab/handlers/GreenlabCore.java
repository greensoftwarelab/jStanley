package greenlab.handlers;

import static greenlab.utils.GreenlabConstants.JOULES;
import static greenlab.utils.GreenlabConstants.MB;
import static greenlab.utils.GreenlabConstants.MS;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.management.AttributeList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import greenlab.models.Invocation;
import greenlab.models.InvocationCost;
import greenlab.models.InvocationRoot;
import greenlab.models.Suggestion;
import greenlab.models.Variable;
import greenlab.utils.GreenlabConsole;


public class GreenlabCore {

	private List<String> analysisType;
	private GreenlabEnergyManager gem;
	private GreenlabHandler gh;
	private Map<String,Variable> variables;
	private List<MethodInvocation> indirectInvocations;
	private GreenlabConsole console;
	/**
	 * variable to store the gains (joules, mb, ms) for the best suggested collections
	 */
	private Map<Variable, Gains> gainsBest;
	/**
	 * variable to store the gains (joules, mb, ms) for the second best suggested collections
	 */
	private Map<Variable, Gains> gains2Best; 

	public GreenlabCore(GreenlabHandler greenlabHandler, String size, List<String> type) {
		this.analysisType = type;
		this.variables = new HashMap<String,Variable>();
		this.indirectInvocations = new ArrayList<MethodInvocation>();
		this.gem = new GreenlabEnergyManager(size);
		this.gh = greenlabHandler;
		this.console = new GreenlabConsole();
		this.gainsBest = new Hashtable<Variable, Gains>();
		this.gains2Best = new Hashtable<Variable, Gains>();
	}

	public void addVariable(IVariableBinding ivb, ITypeBinding itb, int linenumber, int charstart, int charend) {
		if(!this.variables.containsKey(ivb.getKey())) {
			this.variables.put(ivb.getKey(), new Variable(ivb, itb, linenumber, charstart, charend));
		}
	}

	public void addVariable(IVariableBinding ivb, ITypeBinding itb, int linenumber, int charstart, int charend, int parameterIndex) {
		if(!this.variables.containsKey(ivb.getKey())) {
			this.variables.put(ivb.getKey(), new Variable(ivb, itb, linenumber, charstart, charend, parameterIndex));
		}
	}

	public void addMethodDirectInvocation(IVariableBinding ivb, MethodInvocation method, MethodDeclaration md) {
		if(this.variables.containsKey(ivb.getKey())) {
			this.variables.get(ivb.getKey()).addMethodInvocation(method,md);
		}
	}

	public void addMethodIndirectInvocation(MethodInvocation method) {
		this.indirectInvocations.add(method);
	}

	public void resolveVariablesInvocations() {
		for (MethodInvocation method: this.indirectInvocations) {

			IBinding ib = method.getName().resolveBinding();
			IMethodBinding imb = (IMethodBinding) ib;

			for(Variable v : this.variables.values()) {
				for(Invocation iv : v.getInvocations().values()) {
					for(InvocationRoot ivr : iv.getRoots().values()) {
						if(ivr.getMethodDeclaration().resolveBinding().isEqualTo(imb)) {
							ivr.incrementExecutions();
						}
					}
				}
			}
		}

		this.resolveParametersInvocations();
	}

	private void resolveParametersInvocations() {
		for(Variable v: this.variables.values()) {
			if(!v.isParameter()) {
				for (MethodInvocation method: this.indirectInvocations) {
					List<?> arguments = method.arguments();
					int parameterIndex = 0;
					for (Object arg : arguments) {
						if(arg instanceof SimpleName) {
							SimpleName sn = (SimpleName) arg;
							ITypeBinding itb = sn.resolveTypeBinding();
							IBinding ib = sn.resolveBinding();
							if(gh.isListSetMap(itb) && ib.getKind() == IBinding.VARIABLE) {
								IVariableBinding ivb = (IVariableBinding) ib;
								if(v.getVariableBinding().isEqualTo(ivb)) {
									for(Variable v2 : this.variables.values()) {
										if(v2.isParameter() && v2.getParameterIndex() == parameterIndex) {
											v.updateInvocations(v2.getInvocations());
										}
									}
								}
							}
						}
						parameterIndex++;
					}
				}
			}
		}
	}

	public void normaliseVariablesCost() {
		for(Variable v : this.variables.values()) {
			if(!v.isParameter()) {
				v.setNormalisedMatrix(this.gem.normaliseVariableCost(v));
			}
		}
	}

	public void calculateSuggestions() {
		for(Variable v : this.variables.values()) {
			if(!v.isParameter()) {
				Map<String,List<InvocationCost>> normalisedmatrix = v.getNormalisedMatrix();
				Map<String,InvocationCost> grouped = new Hashtable<String, InvocationCost>();
				
				for(String key : normalisedmatrix.keySet()) {
					float groupTotalJoules = 0;
					float groupTotalMs = 0;
					float groupTotalMb = 0;
					float groupTotalNormalJoules = 0;
					float groupTotalNormalMs = 0;
					float groupTotalNormalMb = 0;
					InvocationCost newivc = null;

					for(InvocationCost ivc : normalisedmatrix.get(key)) {
						groupTotalJoules += ivc.getJoules();
						groupTotalMs += ivc.getMs();
						groupTotalMb += ivc.getMb();
						groupTotalNormalJoules += ivc.getNormalJoules();
						groupTotalNormalMs += ivc.getNormalMs();
						groupTotalNormalMb += ivc.getNormalMb();
					}
					newivc = new InvocationCost(key, null, groupTotalJoules, groupTotalMs, groupTotalMb);
					newivc.setNormalJoules(groupTotalNormalJoules);
					newivc.setNormalMs(groupTotalNormalMs);
					newivc.setNormalMb(groupTotalNormalMb);
					grouped.put(key,newivc);
				}
				
				List<InvocationCost> sorted = null;
				
				if(this.analysisType.size() == 1) {
					sorted = new ArrayList<InvocationCost>(grouped.values());

					if(this.analysisType.contains(JOULES)) {
						sorted.sort((i1, i2) -> i1.getNormalJoules() <= i2.getNormalJoules() ? -1 : 1);
					}else if(this.analysisType.contains(MS)){
						sorted.sort((i1, i2) -> i1.getNormalMs() <= i2.getNormalMs() ? -1 : 1);
					}else if(this.analysisType.contains(MB)){
						sorted.sort((i1, i2) -> i1.getNormalMb() <= i2.getNormalMb() ? -1 : 1);
					}
				}else {
					Map<String,InvocationCost> collectionNormalCost = new Hashtable<String, InvocationCost>();

					for (String k : grouped.keySet()) {
						
						float groupTotalJoules = 0;
						float groupTotalMs = 0;
						float groupTotalMb = 0;
						float groupTotalNormalJoules = 0;
						float groupTotalNormalMs = 0;
						float groupTotalNormalMb = 0;
						InvocationCost newivc = null;

						groupTotalNormalJoules += grouped.get(k).getNormalJoules();
						groupTotalJoules += grouped.get(k).getJoules();
						groupTotalNormalMs += grouped.get(k).getNormalMs();
						groupTotalMs += grouped.get(k).getMs();
						groupTotalNormalMb += grouped.get(k).getNormalMb();
						groupTotalMb += grouped.get(k).getMb();
						
						
						newivc = new InvocationCost(k, null, groupTotalJoules, groupTotalMs, groupTotalMb);
						newivc.setNormalJoules(groupTotalNormalJoules);
						newivc.setNormalMs(groupTotalNormalMs);
						newivc.setNormalMb(groupTotalNormalMb);
						collectionNormalCost.put(k,newivc);
					}
					
					sorted = new ArrayList<InvocationCost>(collectionNormalCost.values());
					
					if(this.analysisType.contains(JOULES) && this.analysisType.contains(MS) && this.analysisType.contains(MB)) {
						sorted.sort((i1, i2) -> i1.getNormalJoules()+i1.getNormalMs()+i1.getNormalMb() <= i2.getNormalJoules()+i2.getNormalMs()+i1.getNormalMb() ? -1 : 1);
					}else if(this.analysisType.contains(JOULES) && this.analysisType.contains(MS)) {
						sorted.sort((i1, i2) -> i1.getNormalJoules()+i1.getNormalMs() <= i2.getNormalJoules()+i2.getNormalMs() ? -1 : 1);
					}else if(this.analysisType.contains(JOULES) && this.analysisType.contains(MB)) {
						sorted.sort((i1, i2) -> i1.getNormalJoules()+i1.getNormalMb() <= i2.getNormalJoules()+i2.getNormalMb() ? -1 : 1);
					}else if(this.analysisType.contains(MS) && this.analysisType.contains(MB)) {
						sorted.sort((i1, i2) -> i1.getNormalMs()+i1.getNormalMb() <= i2.getNormalMs()+i2.getNormalMb() ? -1 : 1);
					}
				}
				if(sorted != null && !sorted.isEmpty()) {
					InvocationCost ivc = sorted.get(0);
					
					int maxSuggestions = 2;
					int i = 0;
					int j = 0;
					int index1 = -1, index2 = -1;
					while (j < maxSuggestions && i < sorted.size()-1) {
						// only add the suggestion if its type is different from the current one
						if (v.getType().compareTo(sorted.get(j).getType()) != 0) {
							v.addSuggestion(new Suggestion(sorted.get(j).getType(), sorted.get(j)));
							if (index1 == -1)
								index1 = j;
							else
								index2 = j;
							j++;
						}
						i++;
					}
					
					String classname = "";
					String blockname = "";
					
					if(v.getVariableBinding().getDeclaringClass() != null) {
						classname = v.getVariableBinding().getDeclaringClass().getName();
					}
					if(v.getVariableBinding().getDeclaringMethod() != null) {
						blockname = v.getVariableBinding().getDeclaringMethod().getName();
						if(v.getVariableBinding().getDeclaringMethod().getDeclaringClass() != null) {
							classname = v.getVariableBinding().getDeclaringMethod().getDeclaringClass().getName();
						}
					}
					
					if(!v.getType().contains(ivc.getType())) {
						InvocationCost real = null;
						for(InvocationCost g : sorted) {
							if(v.getType().contains(g.getType())) {
								real = g;
							}
						}
						
						if (index1 != -1) {
							Gains g = new Gains();
							if (this.analysisType.contains(JOULES)) {
								g.setJoules(real.getJoules()-sorted.get(index1).getJoules());
							}
							if (this.analysisType.contains(MS)) {
								g.setMs(real.getMs()-sorted.get(index1).getMs());						
							}
							if (this.analysisType.contains(MB)) {
								g.setMb(real.getMb()-sorted.get(index1).getMb());						
							}
							gainsBest.put(v, g);
						
							// this verification only makes sense if there is at least the first index, thus it is inside the previous if
							if (index2 != -1) {
								Gains g2 = new Gains();
								if (this.analysisType.contains(JOULES)) {
									g2.setJoules(real.getJoules()-sorted.get(index2).getJoules());
								}
								if (this.analysisType.contains(MS)) {			
									g2.setMs(real.getMs()-sorted.get(index2).getMs());
								}
								if (this.analysisType.contains(MB)) {				
									g2.setMb(real.getMb()-sorted.get(index2).getMb());
								}
								gains2Best.put(v, g2);
							}
						}
						
						console.log(">> " + classname + "@" + blockname + " -> " + v.getType() + " " + v.getName());
						if (index1 != -1) {
							console.log("  >> 1st - " + sorted.get(0).getType());
							console.log("    >> gain of " + gainsBest.get(v).getJoules() + " Joules");
							console.log("    >> gain of " + gainsBest.get(v).getMs() + " Ms");
						}
						if (index2 != -1) {
							console.log("  >> 2nd - " + sorted.get(1).getType());
							console.log("    >> gain of " + gains2Best.get(v).getJoules() + " Joules");
							console.log("    >> gain of " + gains2Best.get(v).getMs() + " Ms");
						}
					}
				}
			}
		}
	}
	
	public void makeSuggestions() throws CoreException{		
		for(Variable v : this.variables.values()) {
			if(!v.isParameter() && v.hasSuggestions()) {
				IMarker marker = v.getVariableBinding().getJavaElement().getResource().createMarker("greenlab.greenlabmarker");
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				
				DecimalFormat df = new DecimalFormat("#");
				df.setRoundingMode(RoundingMode.CEILING);
				ArrayList<Object> gainsStr1 = new AttributeList(3);
				String gainsMsg1 = "";
				
				if (gainsBest.containsKey(v)) {
					if (gainsBest.get(v).getJoules() != -1)
						gainsStr1.add("energy by ~" + df.format(gainsBest.get(v).getJoules()) + "%");
					if (gainsBest.get(v).getMs() != -1)
						gainsStr1.add("execution time by ~" + df.format(gainsBest.get(v).getMs()) + "%");
					if (gainsBest.get(v).getMb() != -1)
						gainsStr1.add("memory by ~" + df.format(gainsBest.get(v).getMb()) + "%");
				}
				
				for (Object m : gainsStr1)
					gainsMsg1 += m + ", ";
				
				if (gainsMsg1.length() > 2)
					gainsMsg1 = ": " + gainsMsg1.substring(0, gainsMsg1.length()-2);
				
					
				marker.setAttribute(IMarker.MESSAGE, "Savings available " + gainsMsg1);
				marker.setAttribute(IMarker.LINE_NUMBER, v.getLineNumber());
				marker.setAttribute(IMarker.CHAR_START, v.getCharStart());
				marker.setAttribute(IMarker.CHAR_END, v.getCharStart()+v.getCharEnd());
				marker.setAttribute("KEY",v.getVariableBinding().getKey() + v.getVariableTypeBinding().getKey());
				marker.setAttribute("TYPE", v.getType());
				// there may be no suggestions 
				if(v.getSuggestions().size() >= 1) {
					marker.setAttribute("S1", v.getSuggestions().get(0).getName());
					if (gainsBest.containsKey(v))
						marker.setAttribute("S1percentage", gainsBest.get(v));
					else
						marker.setAttribute("S1percentage", null);
					
					if(v.getSuggestions().size() == 2) {
						if (gainsBest.containsKey(v))
							marker.setAttribute("S2percentage", gains2Best.get(v));
						else
							marker.setAttribute("S2percentage", null);
						marker.setAttribute("S2", v.getSuggestions().get(1).getName());
					}
					else {
						marker.setAttribute("S2", null);
					}					
				}
				else {
					marker.setAttribute("S1", null);
					marker.setAttribute("S2", null);
				}
								
				marker.setAttribute(IJavaModelMarker.ID, 1234);
			}
		}
	}

	public void printProjectCosts() {
		float totalJoules = 0;
		float totalMs = 0;
//		float totalMb = 0;
		float suggestionJoules = 0;
		float suggestionMs = 0;
//		float suggestionMb = 0;

		for(Variable v : this.variables.values()) {
			if(!v.isParameter() && v.hasSuggestions()) {
				for(String key : v.getNormalisedMatrix().keySet()) {
					for(InvocationCost ivc : v.getNormalisedMatrix().get(key)) {
						if(ivc.isReal()) {
							totalJoules += ivc.getJoules();
							totalMs += ivc.getMs();
//							totalMb += ivc.getMb();
						}
					}
				}
				if(v.getSuggestions().get(0) != null) {
					suggestionJoules += v.getSuggestions().get(0).getCost().getJoules();
					suggestionMs += v.getSuggestions().get(0).getCost().getMs();
//					suggestionMb += v.getSuggestion().getCost().getMb();
				}
			}
		}
		console.log("\n\n>> project total cost");
		console.log("  >> Energy: " + totalJoules+JOULES);
		console.log("  >> Time: " + totalMs+MS);
//		console.log("  >> Memory: " + totalMb+MB);
		
		console.log("\n>> suggestion total cost");
		console.log("  >> Energy: " + suggestionJoules+JOULES);
		console.log("  >> Time: " + suggestionMs+MS);
//		console.log("  >> Memory: " + suggestionMb+MB);
	}
}
