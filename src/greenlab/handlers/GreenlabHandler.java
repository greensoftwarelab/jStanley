package greenlab.handlers;

import static greenlab.utils.GreenlabConstants.JOULES;
import static greenlab.utils.GreenlabConstants.MB;
import static greenlab.utils.GreenlabConstants.MS;
import static greenlab.utils.GreenlabConstants.SIZE_1M;
import static greenlab.utils.GreenlabConstants.SIZE_250K;
import static greenlab.utils.GreenlabConstants.SIZE_25K;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

public class GreenlabHandler extends AbstractHandler {
	
	private String size;
	private List<String> analysisType;
	private GreenlabCore gc;
	private List<FieldDeclaration> fieldDeclarations;
	private List<VariableDeclarationStatement> variableDeclaration;
	private List<Assignment> assignments;
	private List<MethodInvocation> methodInvocations;
	private List<MethodDeclaration> methodDeclarations;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
		
		if(event.getCommand().getId().equals("greenlab.dropdown.command")) {
			boolean is25k = (Boolean) commandService.getCommand("greenlab.dropdown.command.state.size.25k").getState("org.eclipse.ui.commands.toggleState").getValue();
			boolean is250k = (Boolean) commandService.getCommand("greenlab.dropdown.command.state.size.250k").getState("org.eclipse.ui.commands.toggleState").getValue();
			boolean is1m = (Boolean) commandService.getCommand("greenlab.dropdown.command.state.size.1m").getState("org.eclipse.ui.commands.toggleState").getValue();
			boolean isJoules = (Boolean) commandService.getCommand("greenlab.dropdown.command.state.metrics.joules").getState("org.eclipse.ui.commands.toggleState").getValue();
			boolean isMs = (Boolean) commandService.getCommand("greenlab.dropdown.command.state.metrics.ms").getState("org.eclipse.ui.commands.toggleState").getValue();
//			boolean isMb = (Boolean) commandService.getCommand("greenlab.dropdown.command.state.metrics.mb").getState("org.eclipse.ui.commands.toggleState").getValue();
			boolean isMb = false;
			this.analysisType = new ArrayList<String>();
			
			//by default use 25k dimension
			if(!is25k && !is250k && !is1m) {
				is25k = true;
				commandService.getCommand("greenlab.dropdown.command.state.size.25k").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(true));
			}
			
			//by default use joules metric
			if(!isJoules && !isMs && !isMb) {
				isJoules = true;
				commandService.getCommand("greenlab.dropdown.command.state.metrics.joules").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(true));
			}
			
			if(is25k) {
				this.size = SIZE_25K;
			}else if(is250k) {
				this.size = SIZE_250K;
			}else if(is1m) {
				this.size = SIZE_1M;
			}
			
			if(isJoules) {
				this.analysisType.add(JOULES);
			}
			if(isMs) {
				this.analysisType.add(MS);
			}
			if(isMb) {
				this.analysisType.add(MB);
			}
			
			try {
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		         IProject[] projects = workspaceRoot.getProjects();
		         for(int i = 0; i < projects.length; i++) {
		            IProject project = projects[i];
		            if(project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
		            	this.init();
		            	this.analyseProject(project);
		            }
		         }
			}catch(Exception e) {
				e.printStackTrace();
			}
					
		} else if(event.getCommand().getId().equals("greenlab.dropdown.command.state.size.25k")) {
			HandlerUtil.toggleCommandState(event.getCommand());
			commandService.getCommand("greenlab.dropdown.command.state.size.250k").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(false));
			commandService.getCommand("greenlab.dropdown.command.state.size.1m").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(false));
		} else if(event.getCommand().getId().equals("greenlab.dropdown.command.state.size.250k")) {
			HandlerUtil.toggleCommandState(event.getCommand());
			commandService.getCommand("greenlab.dropdown.command.state.size.25k").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(false));
			commandService.getCommand("greenlab.dropdown.command.state.size.1m").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(false));
		} else if(event.getCommand().getId().equals("greenlab.dropdown.command.state.size.1m")) {
			HandlerUtil.toggleCommandState(event.getCommand());
			commandService.getCommand("greenlab.dropdown.command.state.size.250k").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(false));
			commandService.getCommand("greenlab.dropdown.command.state.size.25k").getState("org.eclipse.ui.commands.toggleState").setValue(new Boolean(false));
		} else if(event.getCommand().getId().equals("greenlab.dropdown.command.state.metrics.joules") || event.getCommand().getId().equals("greenlab.dropdown.command.state.metrics.ms") || event.getCommand().getId().equals("greenlab.dropdown.command.state.metrics.mb")){
			HandlerUtil.toggleCommandState(event.getCommand());
		}

		return null;
	}
	
	private void init() {
		this.gc = new GreenlabCore(this,this.size,this.analysisType);
		this.fieldDeclarations = new ArrayList<FieldDeclaration>();
		this.variableDeclaration = new ArrayList<VariableDeclarationStatement>();
		this.assignments = new ArrayList<Assignment>();
		this.methodInvocations = new ArrayList<MethodInvocation>();
		this.methodDeclarations = new ArrayList<MethodDeclaration>();
	}
	
	@SuppressWarnings("deprecation")
	private void analyseProject(IProject project)  throws JavaModelException{
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				if(!mypackage.getPath().toString().contains("test")) {
					for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
						ASTParser parser = ASTParser.newParser(AST.JLS9);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setSource(unit);
						parser.setResolveBindings(true);
						CompilationUnit parse = (CompilationUnit) parser.createAST(null);
						
						GreenlabVisitor visitor = new GreenlabVisitor();
						parse.accept(visitor);
						
						fieldDeclarations.addAll(visitor.getFieldDeclarations());
						variableDeclaration.addAll(visitor.getDeclarationStatements());
						assignments.addAll(visitor.getAssignments());
						methodInvocations.addAll(visitor.getMethodInvocations());
						methodDeclarations.addAll(visitor.getMethodDeclarations());
					}
				}
			}
		}
		
		this.analyseProjectSourceCode();
		gc.resolveVariablesInvocations();
		gc.normaliseVariablesCost();
		gc.calculateSuggestions();
		gc.printProjectCosts();
	}

	private void analyseProjectSourceCode() {
		for(FieldDeclaration fd : fieldDeclarations) {
			List<?> fragments = fd.fragments();
			ITypeBinding t = fd.getType().resolveBinding();
			
			for(Object vdf : fragments) {
				if(vdf instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment n = (VariableDeclarationFragment) vdf;
					Expression exp = n.getInitializer();

					if(exp != null) {
						IVariableBinding ivb = n.resolveBinding();
						ITypeBinding itb = exp.resolveTypeBinding();

						if(this.isListSetMap(t)) {
							gc.addVariable(ivb,itb);
						}
					}
				}
			}
		}

		for(VariableDeclarationStatement vds : variableDeclaration) {
			List<?> fragments = vds.fragments();
			ITypeBinding t = vds.getType().resolveBinding();
			
			for(Object vdf : fragments) {
				if(vdf instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment n = (VariableDeclarationFragment) vdf;
					Expression exp = n.getInitializer();

					if(exp != null) {
						IVariableBinding ivb = n.resolveBinding();
						ITypeBinding itb = exp.resolveTypeBinding();
						if(this.isListSetMap(t)) {
							gc.addVariable(ivb,itb);
						}
					}
				}
			}
		}

		for(Assignment ass : assignments) {
			Expression expl = ass.getLeftHandSide();
			Expression expr = ass.getRightHandSide();

			if(expr != null && expl != null) {
				ITypeBinding t = expl.resolveTypeBinding();
				ITypeBinding itb = expr.resolveTypeBinding();

				if(expl.getNodeType() == ASTNode.SIMPLE_NAME && this.isListSetMap(t)) {
					SimpleName sn = (SimpleName) expl;
					IBinding ib = sn.resolveBinding();
					if(ib.getKind() == IBinding.VARIABLE) {
						IVariableBinding ivb = (IVariableBinding) ib;
						gc.addVariable(ivb,itb);
					}
				}
			}
		}
		
		for (MethodDeclaration md : methodDeclarations) {
			List<?> parameters = md.parameters();
			int parameterIndex = 0;
			for (Object param : parameters) {
				if(param instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration svd = (SingleVariableDeclaration) param;
					IVariableBinding ivb = svd.resolveBinding();
					ITypeBinding itb = svd.getType().resolveBinding();
					if(this.isListSetMap(itb)) {
						gc.addVariable(ivb, itb, parameterIndex);
					}
				}
				parameterIndex++;
			}
		}
		
		for(MethodInvocation method : methodInvocations) {
			ASTNode parent = method.getParent();
			MethodDeclaration md = null;
			
			if(parent instanceof MethodDeclaration) {
				md = (MethodDeclaration) parent;
			}else {
				while(!(parent instanceof MethodDeclaration) && parent != null) {
					parent = parent.getParent();
					if(parent instanceof MethodDeclaration) {
						md = (MethodDeclaration) parent;
					}
				}
			}
						
			Expression exp = method.getExpression();
			if(md != null && exp != null && !(exp instanceof ThisExpression) && this.isListSetMap(exp.resolveTypeBinding())) {
				if(exp.getNodeType() == ASTNode.SIMPLE_NAME) {
					SimpleName sn = (SimpleName) exp;
					IBinding ib = sn.resolveBinding();
					if(ib.getKind() == IBinding.VARIABLE) {
						IVariableBinding ivb = (IVariableBinding) ib;
						gc.addMethodDirectInvocation(ivb, method, md);
					}
				}
			}else if(method.getName() != null && method.getName().resolveBinding().getKind() == IBinding.METHOD) {
				gc.addMethodIndirectInvocation(method);
			}
		}
	}

	protected boolean isListSetMap(ITypeBinding itb) {
		boolean isValid = false;
		
		if(itb == null) {
			return isValid;
		}
		
		String itbname = itb.getName();
		
		String[] options = new String[] {"Set","Map","List","ArrayList","AttributeList","CopyOnWriteArrayList","LinkedList","RoleList", "RoleUnresolvedList","Stack","Vector",
				"ConcurrentSkipListSet","HashSet","LinkedHashSet","TreeSet",
				"ConcurrentHashMap","ConcurrentSkipListMap","HashMap","Hashtable","LinkedHashMap","Properties","SimpleBindings","TreeMap","UIDefaults","WeakHashMap"};

		for(int i = 0; i < options.length && !isValid; i++) {
			if(itbname.toLowerCase().contains(options[i].toLowerCase())) {
				isValid = true;
			}
		}

		return isValid;
	}
}
