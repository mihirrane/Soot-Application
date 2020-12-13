import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import heros.util.SootThreadGroup;
import soot.*;
import soot.dava.internal.javaRep.DIntConstant;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;
import soot.jimple.ThisRef;

public class TestSootLoggingHeap extends BodyTransformer {

	private static SootMethodRef logFieldAccMethod;
	private static SootMethodRef logFieldAccValueMethod;

	public static void main(String[] args)	{

		String mainclass = "HelloThread";

		//output Jimple
		//Options.v().set_output_format(1);

//		//set classpath
	    String javapath = System.getProperty("java.class.path");
	    String jredir = System.getProperty("java.home")+"/lib/rt.jar";
	    String path = javapath+File.pathSeparator+jredir;
	    Scene.v().setSootClassPath(path);

        //add an intra-procedural analysis phase to Soot
	    TestSootLoggingHeap analysis = new TestSootLoggingHeap();
	    PackManager.v().getPack("jtp").add(new Transform("jtp.TestSootLoggingHeap", analysis));

        //load and set main class
	    Options.v().set_app(true);
	    SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
	    Scene.v().setMainClass(appclass);
		SootClass logClass = Scene.v().loadClassAndSupport("Log");
		logFieldAccMethod = logClass.getMethod("void logFieldAcc(java.lang.Object,java.lang.String,boolean,boolean)").makeRef();
	    Scene.v().loadNecessaryClasses();

        //start working
	    PackManager.v().runPacks();

	    PackManager.v().writeOutput();
	}

	@Override
	protected void internalTransform(Body b, String phaseName,
		Map<String, String> options) {

//      part 2 working
//      we don't instrument Log class
		if(!b.getMethod().getDeclaringClass().getName().equals("Log")){	
			Iterator<Unit> it = b.getUnits().snapshotIterator();
		    while(it.hasNext()){
		    	Stmt stmt = (Stmt)it.next();
				if (stmt.containsFieldRef()) {
					// find name of the field
					String name = stmt.getFieldRef().getField().toString();
					// check if the field is static
					boolean isStatic = stmt.getFieldRef().getField().isStatic();
					// check if the field is being written onto
					boolean isWrite = (((DefinitionStmt)stmt).getLeftOp() instanceof FieldRef);
					// reference of the object 
					Object o = stmt.getFieldRef().getField().getClass();
					
					// list of arguments to passes to the log class
					List<Value> args = new ArrayList<>();
					
					// adding all arguments required by LogFieldAccMethod
					args.add(StringConstant.v(o.toString()));
					args.add(StringConstant.v(name));
					args.add(DIntConstant.v(isStatic?1:0, BooleanType.v()));
					args.add(DIntConstant.v(isWrite?1:0, BooleanType.v()));
					
					// Call LogAccFieldMethod
					InvokeExpr printExpr = Jimple.v().newStaticInvokeExpr(logFieldAccMethod, args);
					InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(printExpr);
					
					// Add this static Invoke Method before actual code in HelloThread
					b.getUnits().insertBefore(invokeStmt, stmt);
				}
		    }
		}
	}
}