package greenlab.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class GreenlabVisitor extends ASTVisitor {

	private List<VariableDeclarationStatement> variableDeclaration = new ArrayList<VariableDeclarationStatement>();
	private List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();
	private List<Assignment> assignments = new ArrayList<Assignment>();
	private List<MethodInvocation> methodInvocations = new ArrayList<MethodInvocation>();
	private List<MethodDeclaration> methodDeclarations = new ArrayList<MethodDeclaration>();

	@Override
	public boolean visit(Assignment node) {
		if(!this.assignments.contains(node)) {
			this.assignments.add(node);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if(!this.fieldDeclarations.contains(node)) {
			this.fieldDeclarations.add(node);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if(!this.variableDeclaration.contains(node)) {
			this.variableDeclaration.add(node);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if(!this.methodInvocations.contains(node)) {
			this.methodInvocations.add(node);
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		if(!this.methodDeclarations.contains(node)) {
			this.methodDeclarations.add(node);
		}
		return super.visit(node);
	}

	public List<VariableDeclarationStatement> getDeclarationStatements() {
		return this.variableDeclaration;
	}

	public List<FieldDeclaration> getFieldDeclarations() {
		return this.fieldDeclarations;
	}

	public List<Assignment> getAssignments() {
		return this.assignments;
	}
	
	public List<MethodInvocation> getMethodInvocations() {
		return this.methodInvocations;
	}
	
	public List<MethodDeclaration> getMethodDeclarations() {
		return this.methodDeclarations;
	}
}
