package greenlab.handlers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
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
						}
					}
				}
			}
		}else if(context.getCoveringNode().getNodeType() == ASTNode.FIELD_DECLARATION) {
		}else if(context.getCoveringNode().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
		}
		
		rwS1.replace(context.getCoveringNode(), nodePlaceholderS1, null);
		rwS2.replace(context.getCoveringNode(), nodePlaceholderS2, null);
		
		IJavaCompletionProposal pS1 = new ASTRewriteCorrectionProposal("Change " + myMarker.getAttribute("TYPE") + " by " + myMarker.getAttribute("S1"), context.getCompilationUnit(), rwS1, 0);
		IJavaCompletionProposal pS2 = new ASTRewriteCorrectionProposal("Change " + myMarker.getAttribute("TYPE") + " by " + myMarker.getAttribute("S2"), context.getCompilationUnit(), rwS2, 0);
		
		return new IJavaCompletionProposal[] {pS1,pS2};
	}
	
	private IMarker findVariableMarker(IInvocationContext c, String ivb, String itb) throws CoreException {
		IMarker m = null;
		
		IMarker[] markers = c.getCompilationUnit().getJavaProject().getProject().findMarkers("greenlab.greenlabmarker", true, IResource.DEPTH_INFINITE);
		System.out.println("cu -> " + c.getCompilationUnit().getElementName());
		for (IMarker iMarker : markers) {
			if(iMarker.getAttribute("KEY").equals(ivb+itb)) {
				m = iMarker;
			}
		}
		
		return m;
	}

}
