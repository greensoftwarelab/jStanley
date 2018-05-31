package greenlab.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class Variable {
	
	private int parameterIndex;
	private IVariableBinding ivb;
	private ITypeBinding itb;
	private Map<String,Invocation> invocations;
	private Map<String,List<InvocationCost>> normalisedMatrix;
	private Suggestion suggestion;
	
	public Variable(IVariableBinding ivb, ITypeBinding itb) {
		this.ivb = ivb;
		this.itb = itb;
		this.parameterIndex = -1;
		this.invocations = new HashMap<String,Invocation>();
		this.normalisedMatrix = new HashMap<String,List<InvocationCost>>();
		this.suggestion = null;
	}
	
	public Variable(IVariableBinding ivb, ITypeBinding itb, int parameterIndex) {
		this.ivb = ivb;
		this.itb = itb;
		this.parameterIndex = parameterIndex;
		this.invocations = new HashMap<String,Invocation>();
		this.normalisedMatrix = new HashMap<String,List<InvocationCost>>();
		this.suggestion = null;
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
	
	public boolean isParameter() {
		return this.parameterIndex != -1;
	}

	public int parameterIndex() {
		return this.parameterIndex;
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
	
	public Suggestion getSuggestion() {
		return this.suggestion;
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
	
	public void setSuggestion(Suggestion s) {
		this.suggestion = s;
	}
}
