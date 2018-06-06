package greenlab.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

public class GreenlabQuickFixProcessor implements IQuickFixProcessor{

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return problemId == 1234;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}
		
		AST ast = context.getASTRoot().getAST();
		ASTRewrite rw = ASTRewrite.create(ast);
		ASTNode node = ast.newSimpleName("Test");
		rw.replace(context.getCoveringNode(), node, null);
		
		System.out.println("covering" + context.getCoveringNode().toString());

		IJavaCompletionProposal p = new ASTRewriteCorrectionProposal("Change LinkedList by ArrayList", context.getCompilationUnit(), rw, 100);
		return new IJavaCompletionProposal[] {p};
	}

}
