package greenlab.handlers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

public class GreenlabQuickFixProcessor implements IQuickFixProcessor{
	
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return problemId == 1234;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}
		
		AST astS1 = context.getASTRoot().getAST();
		AST astS2 = context.getASTRoot().getAST();
		ASTRewrite rwS1 = ASTRewrite.create(astS1);
		ASTRewrite rwS2 = ASTRewrite.create(astS2);
		ASTNode nodeOriginal = context.getCoveringNode();
		ASTNode nodePlaceholderS1 = ASTNode.copySubtree(astS1, nodeOriginal);
		ASTNode nodePlaceholderS2 = ASTNode.copySubtree(astS2, nodeOriginal);
		IMarker myMarker = null;
		
		if(nodeOriginal.getNodeType() == ASTNode.ASSIGNMENT) {
			System.out.println("ASTNode.ASSIGNMENT");
			Assignment assPlaceholderS1 = (Assignment) nodePlaceholderS1;
			Assignment assPlaceholderS2 = (Assignment) nodePlaceholderS2;
			Assignment assOriginal = (Assignment) nodeOriginal;
			
			if(assOriginal.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
				SimpleName sn = (SimpleName) assOriginal.getLeftHandSide();
				IBinding ib = sn.resolveBinding();
				
				if(ib.getKind() == IBinding.VARIABLE) {
					IVariableBinding ivb = (IVariableBinding) ib;
					ITypeBinding itb = assOriginal.getRightHandSide().resolveTypeBinding();

					myMarker = this.findVariableMarker(context, ivb.getKey(), itb.getKey());
					
					if(assOriginal.getRightHandSide().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
						if(((ClassInstanceCreation) assOriginal.getRightHandSide()).getType().isSimpleType()) {
							SimpleType stS1 = (SimpleType) ((ClassInstanceCreation) assPlaceholderS1.getRightHandSide()).getType();
							SimpleType stS2 = (SimpleType) ((ClassInstanceCreation) assPlaceholderS2.getRightHandSide()).getType();
							stS1.setName(astS1.newSimpleName((String) myMarker.getAttribute("S1")));
							stS2.setName(astS2.newSimpleName((String) myMarker.getAttribute("S2")));
						}else if(((ClassInstanceCreation) assOriginal.getRightHandSide()).getType().isParameterizedType()) {
							ParameterizedType ptS1 = (ParameterizedType) ((ClassInstanceCreation) assPlaceholderS1.getRightHandSide()).getType();
							ParameterizedType ptS2 = (ParameterizedType) ((ClassInstanceCreation) assPlaceholderS2.getRightHandSide()).getType();
							if(ptS1.getType().isSimpleType()) {
								SimpleType stS1 = (SimpleType) ptS1.getType();
								SimpleType stS2 = (SimpleType) ptS2.getType();
								stS1.setName(astS1.newSimpleName((String) myMarker.getAttribute("S1")));
								stS2.setName(astS2.newSimpleName((String) myMarker.getAttribute("S2")));
							}
						}
					}
				}
			}
		}else if(context.getCoveringNode().getNodeType() == ASTNode.FIELD_DECLARATION) {
			System.out.println("ASTNode.FIELD_DECLARATION");
			FieldDeclaration fd = (FieldDeclaration) nodeOriginal;
			FieldDeclaration fdS1 = (FieldDeclaration) nodePlaceholderS1;
			FieldDeclaration fdS2 = (FieldDeclaration) nodePlaceholderS2;
			
			int fIndex = 0;

			for(Object vdf : fd.fragments()) {
				if(vdf instanceof VariableDeclarationFragment) {
					Expression expOriginal = ((VariableDeclarationFragment) vdf).getInitializer();
					Expression expS1 = ((VariableDeclarationFragment) fdS1.fragments().get(fIndex)).getInitializer();
					Expression expS2 = ((VariableDeclarationFragment) fdS2.fragments().get(fIndex)).getInitializer();

					if(expOriginal != null) {
						IVariableBinding ivb = ((VariableDeclarationFragment) vdf).resolveBinding();
						ITypeBinding itb = expOriginal.resolveTypeBinding();
						
						myMarker = this.findVariableMarker(context, ivb.getKey(), itb.getKey());
						
						if(expOriginal.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
							if(((ClassInstanceCreation) expOriginal).getType().isSimpleType()) {
								SimpleType stS1 = (SimpleType) ((ClassInstanceCreation) expS1).getType();
								SimpleType stS2 = (SimpleType) ((ClassInstanceCreation) expS2).getType();
								stS1.setName(astS1.newSimpleName((String) myMarker.getAttribute("S1")));
								stS2.setName(astS2.newSimpleName((String) myMarker.getAttribute("S2")));
							}else if(((ClassInstanceCreation) expOriginal).getType().isParameterizedType()) {
								ParameterizedType ptS1 = (ParameterizedType) ((ClassInstanceCreation) expS1).getType();
								ParameterizedType ptS2 = (ParameterizedType) ((ClassInstanceCreation) expS2).getType();
								if(ptS1.getType().isSimpleType()) {
									SimpleType stS1 = (SimpleType) ptS1.getType();
									SimpleType stS2 = (SimpleType) ptS2.getType();
									stS1.setName(astS1.newSimpleName((String) myMarker.getAttribute("S1")));
									stS2.setName(astS2.newSimpleName((String) myMarker.getAttribute("S2")));
								}
							}
						}
					}
				}
				fIndex++;
			}
			
		}else if(context.getCoveringNode().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			VariableDeclarationFragment fd = (VariableDeclarationFragment) nodeOriginal;
			VariableDeclarationFragment fdS1 = (VariableDeclarationFragment) nodePlaceholderS1;
			VariableDeclarationFragment fdS2 = (VariableDeclarationFragment) nodePlaceholderS2;
			
			Expression expOriginal = ((VariableDeclarationFragment) fd).getInitializer();
			Expression expS1 = ((VariableDeclarationFragment) fdS1).getInitializer();
			Expression expS2 = ((VariableDeclarationFragment) fdS2).getInitializer();

			if(expOriginal != null) {
				IVariableBinding ivb = ((VariableDeclarationFragment) fd).resolveBinding();
				ITypeBinding itb = expOriginal.resolveTypeBinding();
				
				myMarker = this.findVariableMarker(context, ivb.getKey(), itb.getKey());
				
				if(expOriginal.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
					if(((ClassInstanceCreation) expOriginal).getType().isSimpleType()) {
						SimpleType stS1 = (SimpleType) ((ClassInstanceCreation) expS1).getType();
						SimpleType stS2 = (SimpleType) ((ClassInstanceCreation) expS2).getType();
						stS1.setName(astS1.newSimpleName((String) myMarker.getAttribute("S1")));
						stS2.setName(astS2.newSimpleName((String) myMarker.getAttribute("S2")));
					}else if(((ClassInstanceCreation) expOriginal).getType().isParameterizedType()) {
						ParameterizedType ptS1 = (ParameterizedType) ((ClassInstanceCreation) expS1).getType();
						ParameterizedType ptS2 = (ParameterizedType) ((ClassInstanceCreation) expS2).getType();
						if(ptS1.getType().isSimpleType()) {
							SimpleType stS1 = (SimpleType) ptS1.getType();
							SimpleType stS2 = (SimpleType) ptS2.getType();
							stS1.setName(astS1.newSimpleName((String) myMarker.getAttribute("S1")));
							stS2.setName(astS2.newSimpleName((String) myMarker.getAttribute("S2")));
						}
					}
				}
			}

		}
		
		rwS1.replace(context.getCoveringNode(), nodePlaceholderS1, null);
		rwS2.replace(context.getCoveringNode(), nodePlaceholderS2, null);
		
		if(myMarker != null) {
			IJavaCompletionProposal pS1 = new ASTRewriteCorrectionProposal("Change " + myMarker.getAttribute("TYPE") + " by " + myMarker.getAttribute("S1"), context.getCompilationUnit(), rwS1, 0);
			IJavaCompletionProposal pS2 = new ASTRewriteCorrectionProposal("Change " + myMarker.getAttribute("TYPE") + " by " + myMarker.getAttribute("S2"), context.getCompilationUnit(), rwS2, 0);
			
			return new IJavaCompletionProposal[] {pS1,pS2};
		}
		
		return null;
	}
	
	private IMarker findVariableMarker(IInvocationContext c, String ivb, String itb) throws CoreException {
		IMarker m = null;
		
		IMarker[] markers = c.getCompilationUnit().getJavaProject().getProject().findMarkers("greenlab.greenlabmarker", true, IResource.DEPTH_INFINITE);
		for (IMarker iMarker : markers) {
			if(iMarker.getAttribute("KEY").equals(ivb+itb)) {
				m = iMarker;
			}
		}
		
		return m;
	}

}
