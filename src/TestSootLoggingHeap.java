import java.io.File;
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

//		logFieldAccValueMethod = logClass.getMethod("void logFieldAccValue(java.lang.Object,java.lang.String,boolean,boolean,java.lang.Object)").makeRef();
//	    Scene.v().loadNecessaryClasses();

        //start working
	    PackManager.v().runPacks();

	    PackManager.v().writeOutput();
	}

	@Override
	protected void internalTransform(Body b, String phaseName,
		Map<String, String> options) {

//      part 2 working
//      we don't instrument Log class
//		if(!b.getMethod().getDeclaringClass().getName().equals("Log")){	
//			Iterator<Unit> it = b.getUnits().snapshotIterator();
//		    while(it.hasNext()){
//		    	Stmt stmt = (Stmt)it.next();
//				if (stmt.containsFieldRef()) {
//					String name = stmt.getFieldRef().getField().toString();
//					boolean isStatic = stmt.getFieldRef().getField().isStatic();
//					boolean isWrite = (((DefinitionStmt)stmt).getLeftOp() instanceof FieldRef);
//					List<Value> args = new ArrayList<>();
//					Object o = stmt.getFieldRef().getField().getClass();
//					args.add(StringConstant.v(o.toString()));
//					args.add(StringConstant.v(name));
//					args.add(DIntConstant.v(isStatic?1:0, BooleanType.v()));
//					args.add(DIntConstant.v(isWrite?1:0, BooleanType.v()));
//					InvokeExpr printExpr = Jimple.v().newStaticInvokeExpr(logFieldAccMethod, args);
//					InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(printExpr);
//					b.getUnits().insertBefore(invokeStmt, stmt);
//				}
//		    }
//		}
		
//      part 2 object attempt
//       we don't instrument Log class
		if(!b.getMethod().getDeclaringClass().getName().equals("Log")){	
			Iterator<Unit> it = b.getUnits().snapshotIterator();
			boolean flag = false;
		    while(it.hasNext()){
		    	boolean isWrite;
		    	boolean isStatic;
		    	String name;
		    	List<Value> args;
		    	
		    	Stmt stmt = (Stmt)it.next();
		    	
		    	if (stmt.containsFieldRef()) {
					
					name = stmt.getFieldRef().getField().toString();
					isStatic = stmt.getFieldRef().getField().isStatic();
					isWrite = (((DefinitionStmt)stmt).getLeftOp() instanceof FieldRef);
					args = new ArrayList<>();
					Object o = stmt.getFieldRef().getField().getClass();
					
					if(!isStatic) {
						Local locThis = Jimple.v().newLocal("this", b.getMethod().getDeclaringClass().getType());
						b.getLocals().add(locThis);
						b.getUnits().add(Jimple.v().newIdentityStmt(locThis, Jimple.v().newThisRef(b.getMethod().getDeclaringClass().getType())));
						o = b.getThisLocal();
					}					
					args.add(StringConstant.v(o.toString()));
					args.add(StringConstant.v(name));
					args.add(DIntConstant.v(isStatic?1:0, BooleanType.v()));
					args.add(DIntConstant.v(isWrite?1:0, BooleanType.v()));

					InvokeExpr printExpr = Jimple.v().newStaticInvokeExpr(logFieldAccMethod, args);					
					InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(printExpr);
					b.getUnits().insertBefore(invokeStmt, stmt);
				}
		    }
		}
		
//      part 3 bonus part
//      we don't instrument Log class
//		if(!b.getMethod().getDeclaringClass().getName().equals("Log")){	
//			Iterator<Unit> it = b.getUnits().snapshotIterator();
//		    while(it.hasNext()){
//		    	Stmt stmt = (Stmt)it.next();
//				if (stmt.containsFieldRef()) {
//					String name = stmt.getFieldRef().getField().toString();
//					boolean isStatic = stmt.getFieldRef().getField().isStatic();
//					boolean isWrite = (((DefinitionStmt)stmt).getLeftOp() instanceof FieldRef);
//					List<Value> args = new ArrayList<>();
//					Object o = stmt.getFieldRef().getField().getClass();
//					args.add(StringConstant.v(o.toString()));
//					args.add(StringConstant.v(name));
//					args.add(DIntConstant.v(isStatic?1:0, BooleanType.v()));
//					args.add(DIntConstant.v(isWrite?1:0, BooleanType.v()));
//					List<ValueBox> box = stmt.getUseBoxes();
//					
////					System.out.println(stmt + " " + isStatic);
////					for(ValueBox v: box) {
////						System.out.println(v.getValue());
////					}
//					
//					args.add(StringConstant.v(box.toString()));
//					InvokeExpr printExpr = Jimple.v().newStaticInvokeExpr(logFieldAccValueMethod, args);
//					InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(printExpr);
//					b.getUnits().insertBefore(invokeStmt, stmt);
//				}
//		    }
//		}
	}
}