package greenlab.models;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class InvocationRoot {
	
	private MethodDeclaration md;
	private int invocationsCount;
	private int executionsCount;

	public InvocationRoot(MethodDeclaration md) {
		this.md = md;
		this.invocationsCount = 0;
		this.executionsCount = 0;
	}
	
	public String getName() {
		return this.md.resolveBinding().getName();
	}
	
	public int getInvocationsCount() {
		return this.invocationsCount;
	}
	
	public int getExecutionsCount() {
		return this.executionsCount;
	}
	
	public MethodDeclaration getMethodDeclaration() {
		return this.md;
	}

	public void incrementInvocations() {
		this.invocationsCount++;
	}
	
	public void incrementExecutions() {
		this.executionsCount++;
	}
	
	public void incrementInvocations(int c) {
		this.invocationsCount += c;
	}
	
	public void incrementExecutions(int c) {
		this.executionsCount += c;
	}
}