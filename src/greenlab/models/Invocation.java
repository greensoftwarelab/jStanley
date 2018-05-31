package greenlab.models;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class Invocation {

	private MethodInvocation method;
	private Map<String,InvocationRoot> roots;
	
	public Invocation(MethodInvocation method) {
		this.method = method;
		this.roots = new HashMap<String,InvocationRoot>();
	}
	
	public void addMethodRoot(MethodDeclaration md) {
		InvocationRoot ivr = this.roots.get(md.resolveBinding().getKey());
		if(ivr == null) {
			ivr = new InvocationRoot(md);
		}
		ivr.incrementInvocations();
		roots.put(md.resolveBinding().getKey(), ivr);
	}

	public String getName() {
		return this.method.resolveMethodBinding().getName();
	}

	public Map<String,InvocationRoot> getRoots(){
		return this.roots;
	}

	public MethodInvocation getMethodInvocation() {
		return this.method;
	}

	public int getTotalInvocations() {
		int total = 0;
		for(InvocationRoot root : this.roots.values()) {
			if(root.getExecutionsCount() == 0) {
				total += root.getInvocationsCount();
			}else {
				total += root.getExecutionsCount() * root.getInvocationsCount();
			}
		}
		return total;
	}
	
	public void updateInvocationsRoots(Map<String, InvocationRoot> newroots) {
		for(InvocationRoot ivr: newroots.values()) {
			InvocationRoot ivr2 = this.roots.get(ivr.getMethodDeclaration().resolveBinding().getKey());
			if(ivr2 == null) {
				this.roots.put(ivr.getMethodDeclaration().resolveBinding().getKey(), ivr);
			}
		}
	}
}
