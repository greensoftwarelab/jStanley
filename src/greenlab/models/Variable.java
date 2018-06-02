package greenlab.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class Variable {
	
	private int parameterIndex;
	private int linenumber;
	private int charstart;
	private int charend;
	private IVariableBinding ivb;
	private ITypeBinding itb;
	private Map<String,Invocation> invocations;
	private Map<String,List<InvocationCost>> normalisedMatrix;
	private List<Suggestion> suggestions;
	
	public Variable(IVariableBinding ivb, ITypeBinding itb, int linenumber, int charstart, int charend) {
		this.ivb = ivb;
		this.itb = itb;
		this.parameterIndex = -1;
		this.linenumber = linenumber;
		this.charstart = charstart;
		this.charend = charend;
		this.invocations = new HashMap<String,Invocation>();
		this.normalisedMatrix = new HashMap<String,List<InvocationCost>>();
		this.suggestions = new ArrayList<Suggestion>();
	}
	
	public Variable(IVariableBinding ivb, ITypeBinding itb, int linenumber, int charstart, int charend, int parameterIndex) {
		this.ivb = ivb;
		this.itb = itb;
		this.parameterIndex = parameterIndex;
		this.linenumber = linenumber;
		this.charstart = charstart;
		this.charend = charend;
		this.invocations = new HashMap<String,Invocation>();
		this.normalisedMatrix = new HashMap<String,List<InvocationCost>>();
		this.suggestions = new ArrayList<Suggestion>();
	}

	public String getName() {
		return this.ivb.getName();
	}
	
	public String getType() {
		return this.itb.getName();
	}
	
	public int getParameterIndex() {
		return this.parameterIndex;
	}
	
	public int getLineNumber() {
		return this.linenumber;
	}
	
	public int getCharStart() {
		return this.charstart;
	}
	
	public int getCharEnd() {
		return this.charend;
	}
	
	public boolean isParameter() {
		return this.parameterIndex != -1;
	}
	
	public boolean hasSuggestions() {
		return !this.suggestions.isEmpty();
	}

	public IVariableBinding getVariableBinding() {
		return this.ivb;
	}
	
	public Map<String,Invocation> getInvocations(){
		return this.invocations;
	}
	
	public Map<String,List<InvocationCost>> getNormalisedMatrix(){
		return this.normalisedMatrix;
	}
	
	public List<Suggestion> getSuggestions() {
		return this.suggestions;
	}
	
	public void addMethodInvocation(MethodInvocation method, MethodDeclaration md) {
		Invocation iv = this.invocations.get(method.resolveMethodBinding().getKey());
		if(iv == null) {
			iv = new Invocation(method);
		}
		iv.addMethodRoot(md);
		this.invocations.put(method.resolveMethodBinding().getKey(),iv);
	}
	
	public void updateInvocations(Map<String, Invocation> newinvocations) {
		for(Invocation iv: newinvocations.values()) {
			Invocation iv2 = this.invocations.get(iv.getMethodInvocation().resolveMethodBinding().getKey());
			if(iv2 == null) {
				this.invocations.put(iv.getMethodInvocation().resolveMethodBinding().getKey(),iv);
			}else {
				iv2.updateInvocationsRoots(iv.getRoots());
			}
		}
	} 
	
	public void setNormalisedMatrix(Map<String,List<InvocationCost>> nm) {
		this.normalisedMatrix = nm;
	}
	
	public void addSuggestion(Suggestion s) {
		this.suggestions.add(s);
	}
}
