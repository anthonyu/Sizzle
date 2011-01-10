package sizzle.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import sizzle.parser.syntaxtree.ArrayType;
import sizzle.parser.syntaxtree.Assignment;
import sizzle.parser.syntaxtree.Block;
import sizzle.parser.syntaxtree.BreakStatement;
import sizzle.parser.syntaxtree.BytesLiteral;
import sizzle.parser.syntaxtree.Call;
import sizzle.parser.syntaxtree.CharLiteral;
import sizzle.parser.syntaxtree.Comparison;
import sizzle.parser.syntaxtree.Component;
import sizzle.parser.syntaxtree.Composite;
import sizzle.parser.syntaxtree.Conjunction;
import sizzle.parser.syntaxtree.ContinueStatement;
import sizzle.parser.syntaxtree.Declaration;
import sizzle.parser.syntaxtree.DoStatement;
import sizzle.parser.syntaxtree.EmitStatement;
import sizzle.parser.syntaxtree.ExprList;
import sizzle.parser.syntaxtree.ExprStatement;
import sizzle.parser.syntaxtree.Expression;
import sizzle.parser.syntaxtree.Factor;
import sizzle.parser.syntaxtree.FingerprintLiteral;
import sizzle.parser.syntaxtree.FloatingPointLiteral;
import sizzle.parser.syntaxtree.ForExprStatement;
import sizzle.parser.syntaxtree.ForStatement;
import sizzle.parser.syntaxtree.ForVarDecl;
import sizzle.parser.syntaxtree.Function;
import sizzle.parser.syntaxtree.FunctionType;
import sizzle.parser.syntaxtree.Identifier;
import sizzle.parser.syntaxtree.IdentifierList;
import sizzle.parser.syntaxtree.IfStatement;
import sizzle.parser.syntaxtree.Index;
import sizzle.parser.syntaxtree.IntegerLiteral;
import sizzle.parser.syntaxtree.MapType;
import sizzle.parser.syntaxtree.Node;
import sizzle.parser.syntaxtree.NodeChoice;
import sizzle.parser.syntaxtree.NodeSequence;
import sizzle.parser.syntaxtree.NodeToken;
import sizzle.parser.syntaxtree.Operand;
import sizzle.parser.syntaxtree.OutputType;
import sizzle.parser.syntaxtree.Pair;
import sizzle.parser.syntaxtree.PairList;
import sizzle.parser.syntaxtree.Program;
import sizzle.parser.syntaxtree.ProtoFieldDecl;
import sizzle.parser.syntaxtree.ProtoMember;
import sizzle.parser.syntaxtree.ProtoMemberList;
import sizzle.parser.syntaxtree.ProtoTupleType;
import sizzle.parser.syntaxtree.Regexp;
import sizzle.parser.syntaxtree.RegexpList;
import sizzle.parser.syntaxtree.ResultStatement;
import sizzle.parser.syntaxtree.ReturnStatement;
import sizzle.parser.syntaxtree.Selector;
import sizzle.parser.syntaxtree.SimpleExpr;
import sizzle.parser.syntaxtree.SimpleMember;
import sizzle.parser.syntaxtree.SimpleMemberList;
import sizzle.parser.syntaxtree.SimpleTupleType;
import sizzle.parser.syntaxtree.Start;
import sizzle.parser.syntaxtree.Statement;
import sizzle.parser.syntaxtree.StatementExpr;
import sizzle.parser.syntaxtree.StaticVarDecl;
import sizzle.parser.syntaxtree.StringLiteral;
import sizzle.parser.syntaxtree.SwitchStatement;
import sizzle.parser.syntaxtree.Term;
import sizzle.parser.syntaxtree.TimeLiteral;
import sizzle.parser.syntaxtree.TupleType;
import sizzle.parser.syntaxtree.Type;
import sizzle.parser.syntaxtree.TypeDecl;
import sizzle.parser.syntaxtree.VarDecl;
import sizzle.parser.syntaxtree.WhenStatement;
import sizzle.parser.syntaxtree.WhileStatement;
import sizzle.parser.visitor.GJDepthFirst;
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBytes;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleString;
import sizzle.types.SizzleTable;
import sizzle.types.SizzleType;

class TableDescription {
	private String aggregator;
	private SizzleType type;
	private List<String> parameters;

	public TableDescription(final String aggregator, final SizzleType type) {
		this(aggregator, type, null);
	}

	public TableDescription(final String aggregator, final SizzleType type, final List<String> parameters) {
		this.aggregator = aggregator;
		this.type = type;
		this.parameters = parameters;
	}

	/**
	 * @return the name
	 */
	public String getAggregator() {
		return this.aggregator;
	}

	/**
	 * @return the parameters
	 */
	public List<String> getParameters() {
		return this.parameters;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setAggregator(final String aggregator) {
		this.aggregator = aggregator;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(final List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the types
	 */
	public SizzleType getType() {
		return this.type;
	}

	/**
	 * @param types
	 *            the types to set
	 */
	public void setTypes(final SizzleType type) {
		this.type = type;
	}
}

public class CodeGeneratingVisitor extends GJDepthFirst<String, SymbolTable> {
	private final TypeCheckingVisitor typechecker;
	private final NameFindingVisitor namefinder;
	private final StaticDeclarationCodeGeneratingVisitor staticdeclarator;
	private final StaticInitializationCodeGeneratingVisitor staticinitializer;

	private final HashMap<String, TableDescription> tables;

	private final String name;
	private final StringTemplateGroup stg;

	public CodeGeneratingVisitor(final String name, final StringTemplateGroup stg) throws IOException {
		this.typechecker = new TypeCheckingVisitor();
		this.namefinder = new NameFindingVisitor();
		this.staticdeclarator = new StaticDeclarationCodeGeneratingVisitor(this);
		this.staticinitializer = new StaticInitializationCodeGeneratingVisitor(this);

		this.tables = new HashMap<String, TableDescription>();
		this.tables.put("stdout", new TableDescription("stdout", new SizzleString()));
		this.tables.put("stderr", new TableDescription("stderr", new SizzleString()));
		this.tables.put("output", new TableDescription("output", new SizzleBytes()));

		this.name = name;
		this.stg = stg;
	}

	@Override
	public String visit(final Start n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	@Override
	public String visit(final Program n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Program");

		st.setAttribute("name", this.name);
		if (argu.get("input").equals(new SizzleString())) {
			st.setAttribute("inputFormatClass", "org.apache.hadoop.mapreduce.lib.input.TextInputFormat");
			st.setAttribute("keyClass", "org.apache.hadoop.io.LongWritable");
			st.setAttribute("valueClass", "org.apache.hadoop.io.Text");
		} else if (argu.get("input").equals(new SizzleBytes())) {
			throw new RuntimeException("unimplemented");
		}

		st.setAttribute("staticDeclarations", this.staticdeclarator.visit(n, argu));
		st.setAttribute("staticStatements", this.staticinitializer.visit(n, argu));

		final List<String> statements = new ArrayList<String>();
		for (final Node node : n.f0.nodes) {
			final String statement = node.accept(this, argu);
			if (statement != null)
				statements.add(statement);
		}
		st.setAttribute("statements", statements);

		final List<String> tables = new ArrayList<String>();
		for (final Entry<String, TableDescription> entry : this.tables.entrySet()) {
			final String id = entry.getKey();
			final TableDescription description = entry.getValue();
			final String parameters = description.getParameters() == null ? "" : description.getParameters().get(0);
			final SizzleType type = description.getType();

			final StringBuilder src = new StringBuilder();
			for (final Class<?> c : argu.getAggregators(description.getAggregator(), type))
				src.append(", new " + c.getCanonicalName() + "(" + parameters + ")");

			tables.add("this.tables.put(\"" + id + "\", new sizzle.aggregators.Table(" + src.toString().substring(2) + "));");
		}

		st.setAttribute("tables", tables);

		return st.toString();
	}

	@Override
	public String visit(final Declaration n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	@Override
	public String visit(final TypeDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StaticVarDecl n, final SymbolTable argu) {
		// this is handled by the static code generator
		return null;
	}

	@Override
	public String visit(final VarDecl n, final SymbolTable argu) {
		final SizzleType type = argu.get(n.f0.f0.tokenImage);

		if (n.f2.present()) {
			argu.setId(n.f0.f0.tokenImage);
			n.f2.node.accept(this, argu);
			argu.setId(null);
		}

		if (type instanceof SizzleTable)
			return null;

		final StringTemplate st = this.stg.getInstanceOf("VarDecl");

		st.setAttribute("id", n.f0.f0.tokenImage);

		// TODO: make templates for types
		st.setAttribute("type", type.toJavaType());

		if (n.f3.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f3.node;

			switch (nodeChoice.which) {
			case 0: // initializer
				SizzleType t;
				try {
					t = this.typechecker.visit((Expression) ((NodeSequence) nodeChoice.choice).elementAt(1), argu.cloneNonLocals());
				} catch (final IOException e) {
					throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
				}

				String src = ((NodeSequence) nodeChoice.choice).elementAt(1).accept(this, argu);

				if (!type.assigns(t)) {
					final SizzleFunction f = argu.getCast(t, type);

					if (f.hasName()) {
						src = f.getName() + "(" + src + ")";
					} else if (f.hasMacro()) {
						src = CodeGeneratingVisitor.expand("X" + f.getMacro(), src.split(","));
					}
				}

				st.setAttribute("initializer", src);
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is a " + nodeChoice.choice.getClass().getSimpleName().toString());
			}
		}

		return st.toString();
	}

	@Override
	public String visit(final Type n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	@Override
	public String visit(final Component n, final SymbolTable argu) {
		// intentionally ignoring the identifier
		return n.f1.accept(this, argu);
	}

	@Override
	public String visit(final ArrayType n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ArrayType");

		st.setAttribute("type", n.f2.accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final TupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleTupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoTupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoFieldDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final MapType n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("MapType");

		st.setAttribute("key", n.f2.accept(this, argu));

		st.setAttribute("value", n.f2.accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final OutputType n, final SymbolTable argu) {
		final String id = argu.getId();

		final String aggregator = n.f1.f0.tokenImage;

		final SizzleTable t = (SizzleTable) argu.get(id);

		if (n.f2.present()) {
			final String parameter = ((NodeSequence) n.f2.node).nodes.get(1).accept(this, argu);
			this.tables.put(id, new TableDescription(aggregator, t.getType(), Arrays.asList(parameter)));
		} else {
			this.tables.put(id, new TableDescription(aggregator, t.getType()));
		}

		return null;
	}

	@Override
	public String visit(final ExprList n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ExprList");

		final List<String> expressions = new ArrayList<String>();

		expressions.add(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				expressions.add(((NodeSequence) node).elementAt(1).accept(this, argu));

		st.setAttribute("expressions", expressions);

		return st.toString();
	}

	@Override
	public String visit(final FunctionType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Statement n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	@Override
	public String visit(final Assignment n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Block n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Block");

		final List<String> statements = new ArrayList<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				statements.add(node.accept(this, argu));

		st.setAttribute("statements", statements);

		return st.toString();
	}

	@Override
	public String visit(final BreakStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ContinueStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final DoStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final EmitStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("EmitStatement");

		if (n.f2.present()) {
			final List<String> indices = new ArrayList<String>();

			for (final Node node : n.f2.nodes)
				indices.add(((NodeSequence) node).elementAt(1).accept(this, argu));

			st.setAttribute("indices", indices);
		}

		st.setAttribute("id", Character.toString('"') + n.f1.f0.tokenImage + '"');

		st.setAttribute("expression", n.f4.f0.accept(this, argu));

		if (n.f5.present())
			st.setAttribute("weight", ((NodeSequence) n.f5.node).elementAt(1).accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final ExprStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ExprStatement");

		st.setAttribute("expression", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;
			switch (nodeChoice.which) {
			case 0:
				st.setAttribute("operator", "++");
				break;
			case 1:
				st.setAttribute("operator", "--");
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is a " + nodeChoice.choice.getClass().getSimpleName().toString());
			}
		}

		return st.toString();
	}

	@Override
	public String visit(final ForStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ForStatement");

		SymbolTable symtab;
		try {
			symtab = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		this.typechecker.visit(n.f2, symtab);

		st.setAttribute("declaration", n.f2.accept(this, symtab));
		st.setAttribute("expression", n.f4.accept(this, symtab));
		st.setAttribute("exprstmt", n.f6.accept(this, symtab));
		st.setAttribute("statement", n.f8.accept(this, symtab));

		return st.toString();
	}

	@Override
	public String visit(final ForVarDecl n, final SymbolTable argu) {
		final VarDecl varDecl = new VarDecl(n.f0, n.f1, n.f2, n.f3, new NodeToken(";"));

		return varDecl.accept(this, argu);
	}

	@Override
	public String visit(final ForExprStatement n, final SymbolTable argu) {
		final ExprStatement exprStatement = new ExprStatement(n.f0, n.f1, new NodeToken(";"));

		return exprStatement.accept(this, argu);
	}

	@Override
	public String visit(final IfStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("IfStatement");

		st.setAttribute("expression", n.f2.f0.accept(this, argu));
		st.setAttribute("statement", n.f4.f0.accept(this, argu));

		if (n.f5.present())
			st.setAttribute("elseStatement", ((NodeSequence) n.f5.node).nodes.elementAt(1).accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final ResultStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ReturnStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SwitchStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final WhenStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("WhenStatement");

		st.setAttribute("expression", n.f3.accept(this, argu));
		st.setAttribute("statement", n.f5.accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final IdentifierList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final WhileStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Expression n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				operators.add("||");
				operands.add(((NodeSequence) node).elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Conjunction n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				operators.add("&&");
				operands.add(((NodeSequence) node).elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Comparison n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			final Vector<Node> nodes = ((NodeSequence) n.f1.node).nodes;
			final NodeChoice nodeChoice = (NodeChoice) nodes.elementAt(0);
			switch (nodeChoice.which) {
			case 0:
				operators.add(" == ");
				break;
			case 1:
				operators.add(" != ");
				break;
			case 2:
				operators.add(" < ");
				break;
			case 3:
				operators.add(" <= ");
				break;
			case 4:
				operators.add(" > ");
				break;
			case 5:
				operators.add(" >= ");
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
			operands.add(nodes.elementAt(1).accept(this, argu));

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final SimpleExpr n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) node;
				operators.add(((NodeToken) ((NodeChoice) nodeSequence.elementAt(0)).choice).tokenImage);
				operands.add(nodeSequence.elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Term n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) node;
				operators.add(((NodeToken) ((NodeChoice) nodeSequence.elementAt(0)).choice).tokenImage);
				operands.add(nodeSequence.elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Factor n, final SymbolTable argu) {
		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.nodes.get(0);

			switch (nodeChoice.which) {
			case 1: // index
			case 2: // call
				argu.setOperand(n.f0);
				final String accept = n.f1.nodes.elementAt(0).accept(this, argu);
				argu.setOperand(null);
				return accept;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		} else {
			return n.f0.accept(this, argu);
		}
	}

	@Override
	public String visit(final Selector n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Index n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Index");

		try {
			final SizzleType t = this.typechecker.visit(argu.getOperand(), argu.cloneNonLocals());
			if (t instanceof SizzleMap)
				st.setAttribute("map", true);
			else if (t instanceof SizzleArray)
				st.setAttribute("map", false);
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		st.setAttribute("operand", argu.getOperand().accept(this, argu));

		st.setAttribute("index", n.f1.accept(this, argu));

		if (n.f2.present())
			st.setAttribute("slice", ((NodeSequence) n.f2.node).elementAt(1).accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final Call n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Call");

		final SizzleFunction f = argu.getFunction(this.namefinder.visit(argu.getOperand()).toArray()[0].toString(), this.typechecker.check(n, argu));

		if (f.hasMacro()) {
			st.setAttribute("call", CodeGeneratingVisitor.expand(f.getMacro(), ((ExprList) n.f1.node).accept(this, argu).split(",")));
		} else if (f.hasName()) {
			st.setAttribute("operand", f.getName());

			if (n.f1.present())
				st.setAttribute("parameters", n.f1.node.accept(this, argu));
		}

		return st.toString();

	}

	@Override
	public String visit(final RegexpList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Regexp n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Operand n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	@Override
	public String visit(final Composite n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Composite");

		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;
			switch (nodeChoice.which) {
			case 0: // pair list
				st.setAttribute("pairlist", nodeChoice.choice.accept(this, argu));
				break;
			case 1: // expression list
				final SizzleType t;
				try {
					t = this.typechecker.visit((ExprList) nodeChoice.choice, argu.cloneNonLocals());
				} catch (final IOException e) {
					throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
				}

				st.setAttribute("type", t.toJavaType());
				st.setAttribute("exprlist", nodeChoice.choice.accept(this, argu));
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		return st.toString();
	}

	@Override
	public String visit(final PairList n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("PairList");

		final List<String> pairs = new ArrayList<String>();

		pairs.add(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				pairs.add(((NodeSequence) node).elementAt(1).accept(this, argu));

		st.setAttribute("pairs", pairs);

		return st.toString();
	}

	@Override
	public String visit(final Pair n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Pair");

		st.setAttribute("map", argu.getId());
		st.setAttribute("key", n.f0.accept(this, argu));
		st.setAttribute("value", n.f2.accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final Function n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StatementExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Identifier n, final SymbolTable argu) {
		// TODO: support protobufs/sequence files/avro here
		final String id = n.f0.tokenImage;

		if (argu.hasType(id))
			return argu.getType(id).toString();

		// otherwise return the identifier template
		final StringTemplate st = this.stg.getInstanceOf("Identifier");

		st.setAttribute("id", id);

		return st.toString();
	}

	@Override
	public String visit(final IntegerLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage + 'l';
	}

	@Override
	public String visit(final FingerprintLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final FloatingPointLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage + "d";
	}

	@Override
	public String visit(final CharLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StringLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage;
	}

	@Override
	public String visit(final BytesLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final TimeLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	private static String expand(final String template, final String... parameters) {
		String replaced = template;

		for (int i = 0; i < parameters.length; i++)
			replaced = replaced.replace("${" + i + "}", parameters[i]);

		return replaced;
	}

	// private static String expand(final String template, final String[]
	// parameters) {
	// String replaced = template;
	//
	// for (int i = 0; i < parameters.length; i++)
	// replaced = replaced.replace("${" + i + "}", parameters[i]);
	//
	// return replaced;
	// }

	// private String name;
	//
	// private final NameFindingVisitor namefinder;
	// private final TypeCheckingVisitor typefinder;
	// private final IndexeeFindingVisitor indexeefinder;
	// private StaticDeclarationCodeGeneratingVisitor staticfinder;
	// private String staticDeclarations;
	//
	// private final Map<String, AggregatorDescription> aggregators;
	// private final Map<String, String> codeMap;
	//
	//
	// public CodeGeneratingVisitor(final String name) {
	// this();
	//
	// this.staticfinder = new
	// StaticDeclarationCodeGeneratingVisitor(this.namefinder);
	//
	// this.name = name;
	// }
	//
	// // TODO: really support implicit cast on assignment?
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Assignment n, final SymbolTable argu) {
	// final String id = n.f0.f0.tokenImage;
	//
	// argu.setInitializerType(argu.get(id));
	// final String source = n.f0.accept(this, argu) + " = " + n.f2.accept(this,
	// argu);
	// argu.setInitializerType(null);
	//
	// return source;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final AssignmentList n, final SymbolTable argu) {
	// final StringBuilder source = new StringBuilder(n.f0.accept(this, argu));
	//
	// if (n.f1.present())
	// for (final Node node : n.f1.nodes)
	// source.append(((AssignmentRest) node).accept(this, argu));
	//
	// return source.toString();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final AssignmentRest n, final SymbolTable argu) {
	// return n.f1.accept(this, argu);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final AssignmentStatement n, final SymbolTable argu)
	// {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Atom n, final SymbolTable argu) {
	// switch (n.f0.which) {
	// case 0: // identifier
	// return ((Identifier) n.f0.choice).accept(this, argu);
	// case 1: // string
	// return ((StringLiteral) n.f0.choice).accept(this, argu);
	// case 2: // integer
	// return ((IntegerLiteral) n.f0.choice).accept(this, argu);
	// case 3: // float
	// return ((FloatingPointLiteral) n.f0.choice).accept(this, argu);
	// default:
	// throw new RuntimeException("unexpected choice " + n.f0.which + " is " +
	// n.f0.choice.getClass());
	// }
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Block n, final SymbolTable argu) {
	// final StringBuilder source = new StringBuilder("{\n");
	//
	// if (n.f1.present()) {
	// final List<Node> arrayList = n.f1.nodes;
	// for (final Node i : arrayList)
	// source.append(((Statement) i).accept(this, argu) + "\n");
	// }
	//
	// source.append("}");
	//
	// return source.toString();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final CallExpression n, final SymbolTable argu) {
	// final String id = n.f0.f0.tokenImage;
	//
	// if (n.f2.present()) {
	// final SizzleFunction function = argu.getFunction(id,
	// this.typefinder.check((ExpressionList) n.f2.node, argu));
	//
	// final String parameters = ((ExpressionList) n.f2.node).accept(this,
	// argu);
	//
	// if (function.hasMacro())
	// return CodeGeneratingVisitor.expand(function.getMacro(),
	// parameters.split(","));
	// else
	// return function.getCanonicalName() + "(" + parameters + ")";
	// } else {
	// final SizzleFunction function = argu.getFunction(id);
	// return function.getCanonicalName() + "()";
	// }
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final ComparisonExpression n, final SymbolTable argu)
	// {
	// final String lhs = n.f0.accept(this, argu);
	//
	// if (!n.f1.present())
	// return lhs;
	//
	// SizzleType type;
	// try {
	// type = this.typefinder.visit(n.f0, argu.cloneNonLocals());
	// } catch (final IOException e) {
	// throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
	// }
	// final List<Node> nodes = ((NodeSequence) n.f1.node).nodes;
	//
	// final String operator = ((NodeToken) ((NodeChoice)
	// nodes.get(0)).choice).tokenImage;
	//
	// final String rhs = ((PlusExpression) nodes.get(1)).accept(this, argu);
	// if (operator.equals("==") || operator.equals("!="))
	// if (type.getClass().equals(SizzleArray.class))
	// return "java.util.Arrays.equals(" + lhs + ", " + rhs + ")";
	// else if (type.getClass().equals(SizzleString.class))
	// return (operator.equals("!=") ? "!" : "") + rhs + ".equals(" + lhs + ")";
	// else if (Character.isUpperCase(type.toJavaType().toCharArray()[0]))
	// return (operator.equals("!=") ? "!" : "") + rhs + ".equals(" + lhs + ")";
	// else
	// return lhs + " " + operator + " " + " " + rhs;
	// else
	// return lhs + " " + operator + " " + rhs;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Expression n, final SymbolTable argu) {
	// final StringBuilder expression = new StringBuilder(n.f0.accept(this,
	// argu));
	//
	// if (n.f1.present())
	// for (final Node node : n.f1.nodes)
	// expression.append(" " + ((NodeToken) ((NodeChoice) ((NodeSequence)
	// node).nodes.get(0)).choice).tokenImage + " "
	// + ((ComparisonExpression) ((NodeSequence)
	// node).nodes.get(1)).accept(this, argu));
	//
	// return expression.toString();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final ExpressionList n, final SymbolTable argu) {
	// final StringBuilder rest = new StringBuilder();
	// if (n.f1.present())
	// for (final Node i : n.f1.nodes)
	// rest.append(", " + ((ExpressionRest) i).accept(this, argu));
	//
	// return n.f0.accept(this, argu) + rest.toString();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final ExpressionRest n, final SymbolTable argu) {
	// return n.f1.accept(this, argu);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final FloatingPointLiteral n, final SymbolTable argu)
	// {
	// return n.f0.tokenImage;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final ForStatement n, final SymbolTable argu) {
	// // when they say "for loops [...] take on their familiar form", I assume
	// // they mean K&R form, e.g. declarations not allowed in initializer.
	// String src = "for (";
	//
	// if (n.f2.present())
	// src += ((AssignmentList) n.f2.node).accept(this, argu);
	//
	// src += "; ";
	//
	// if (n.f4.present())
	// src += ((ExpressionList) n.f4.node).accept(this, argu);
	//
	// src += "; ";
	//
	// if (n.f6.present())
	// src += ((ExpressionList) n.f6.node).accept(this, argu);
	//
	// return src + ")\n" + n.f8.accept(this, argu);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final IdentifierList n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final IdentifierRest n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final IfStatement n, final SymbolTable argu) {
	// String src = "if (" + n.f2.accept(this, argu) + ")\n" + n.f4.accept(this,
	// argu) + "\n";
	//
	// if (n.f5.present())
	// src += "else\n" + ((Statement) ((NodeSequence)
	// n.f5.node).elementAt(1)).accept(this, argu);
	//
	// return src;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final IndexExpression n, final SymbolTable argu) {
	// final String id = n.f0.accept(this, argu);
	// final SizzleType type = argu.get(n.f0.f0.tokenImage);
	//
	// if (n.f1.present()) {
	// final String index = ((NodeSequence) n.f1.node).nodes.get(1).accept(this,
	// argu);
	// if (type.getClass().equals(SizzleArray.class))
	// return "(" + id + ".length > " + index + " ? " + id + "[(int)" + index +
	// "]" + " : null)";
	// else if (type.getClass().equals(SizzleMap.class))
	// return id + ".get(" + index + ")";
	// else
	// throw new RuntimeException("wtf");
	// } else
	// return id;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Initializer n, final SymbolTable argu) {
	// switch (n.f0.which) {
	// case 0: // expression
	// final SizzleType t = argu.getInitializerType();
	// final SizzleType u = this.typefinder.visit(((Expression) n.f0.choice),
	// argu);
	//
	// if (!t.assigns(u) && argu.hasCast(u, t))
	// return argu.getCast(u, t).getCanonicalName() + "(" + ((Expression)
	// n.f0.choice).accept(this, argu) + ")";
	// return ((Expression) n.f0.choice).accept(this, argu);
	// case 1: // map or array
	// final NodeOptional nodeOptional = (NodeOptional) ((NodeSequence)
	// n.f0.choice).nodes.get(1);
	// if (nodeOptional.present()) {
	// final NodeChoice node = (NodeChoice) nodeOptional.node;
	// switch (node.which) {
	// case 0: // pair list
	// argu.setStaticInitializer(((PairList) node.choice).accept(this, argu));
	// return "new " + argu.getInitializerType().toJavaType() + "()";
	// case 1: // expression list
	// return "new " + argu.getInitializerType().toJavaType() + " { " +
	// ((ExpressionList) node.choice).accept(this, argu) + " }";
	// default:
	// throw new RuntimeException("unexpected choice " + n.f0.which + " is " +
	// n.f0.choice.getClass());
	// }
	// } else
	// return "";
	// default:
	// throw new RuntimeException("unexpected choice " + n.f0.which + " is " +
	// n.f0.choice.getClass());
	// }
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final IntegerLiteral n, final SymbolTable argu) {
	// return n.f0.tokenImage;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final MemberExpression n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final ParentheticalExpression n, final SymbolTable
	// argu) {
	// return "(" + n.f1.accept(this, argu) + ")";
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final PlusExpression n, final SymbolTable argu) {
	// final StringBuilder src = new StringBuilder();
	//
	// src.append(n.f0.accept(this, argu));
	//
	// if (n.f1.present())
	// for (final Node i : n.f1.nodes) {
	// final NodeSequence nodeSequence = (NodeSequence) i;
	// src.append(" " + ((NodeToken) ((NodeChoice)
	// nodeSequence.nodes.get(0)).choice).tokenImage + " " +
	// nodeSequence.nodes.get(1).accept(this, argu));
	// }
	//
	// return src.toString();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final ProtoStatement n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final SliceExpression n, final SymbolTable argu) {
	// if (n.f1.present()) {
	// final String lower = ((NodeSequence)
	// n.f1.nodes.get(0)).elementAt(1).accept(this, argu);
	// final String upper = ((NodeSequence)
	// n.f1.nodes.get(0)).elementAt(3).accept(this, argu);
	// final String id = n.f0.accept(this, argu);
	// return "(" + lower + " > " + upper + " ? java.util.Arrays.copyOfRange(" +
	// id + ", " + upper + ", " + upper + ") : java.util.Arrays.copyOfRange("
	// + id + ", " + lower + ", " + upper + "))";
	// } else
	// return "";
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Start n, final SymbolTable argu) {
	// this.staticDeclarations = this.staticfinder.visit(n, argu);
	// return n.f0.accept(this, argu);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Statement n, final SymbolTable argu) {
	// switch (n.f0.which) {
	// case 3: // non-static variable declaration
	// return ((VariableDeclaration) n.f0.choice).accept(this, argu);
	// case 4: // block
	// return ((Block) n.f0.choice).accept(this, argu);
	// case 6: // emit statement
	// return ((EmitStatement) n.f0.choice).accept(this, argu);
	// case 7: // if statement
	// return ((IfStatement) n.f0.choice).accept(this, argu);
	// case 9: // for statement
	// return ((ForStatement) n.f0.choice).accept(this, argu);
	// case 10: // when statement
	// return ((WhenStatement) n.f0.choice).accept(this, argu);
	// case 11: // proto statement
	// return ((ProtoStatement) n.f0.choice).accept(this, argu);
	// default:
	// throw new RuntimeException("unexpected choice " + n.f0.which + " is " +
	// n.f0.choice.getClass());
	// }
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final StringLiteral n, final SymbolTable argu) {
	// return n.f0.tokenImage;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Term n, final SymbolTable argu) {
	// switch (n.f0.which) {
	// case 0: // call
	// return ((CallExpression) n.f0.choice).accept(this, argu);
	// case 2: // wide index into array
	// return ((SliceExpression) n.f0.choice).accept(this, argu);
	// case 3: // index into array
	// return ((IndexExpression) n.f0.choice).accept(this, argu);
	// case 4: // expressions in parantheses
	// return ((ParentheticalExpression) n.f0.choice).accept(this, argu);
	// case 5: // atom
	// return ((Atom) n.f0.choice).accept(this, argu);
	// default:
	// throw new RuntimeException("unexpected choice " + n.f0.which + " is " +
	// n.f0.choice.getClass());
	// }
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final TimesExpression n, final SymbolTable argu) {
	// final StringBuilder src = new StringBuilder();
	//
	// src.append(n.f0.accept(this, argu));
	//
	// if (n.f1.present())
	// for (final Node i : n.f1.nodes) {
	// final NodeSequence nodeSequence = (NodeSequence) i;
	// src.append(" " + ((NodeToken) ((NodeChoice)
	// nodeSequence.nodes.get(0)).choice).tokenImage + " " +
	// nodeSequence.nodes.get(1).accept(this, argu));
	// }
	//
	// return src.toString();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final Typle n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final TypleList n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final TypleRest n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final UnaryExpression n, final SymbolTable argu) {
	// String expression = n.f0.accept(this, argu);
	//
	// if (n.f1.present())
	// expression += ((NodeToken) ((NodeChoice) n.f1.node).choice).tokenImage;
	//
	// return expression;
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final StaticVariableDeclaration n, final SymbolTable
	// argu) {
	// return "";
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final WhenStatement n, final SymbolTable argu) {
	// SymbolTable st;
	// try {
	// st = argu.cloneNonLocals();
	// } catch (final IOException e) {
	// throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
	// }
	//
	// final StringBuilder src = new StringBuilder();
	// for (final Node node : n.f2.nodes) {
	// // String quant;
	// // switch (((NodeChoice)((NodeSequence)node).nodes.get(2)).which) {
	// // case 0:
	// // quant = "some";
	// // break;
	// // case 1:
	// // quant = "each";
	// // break;
	// // case 2:
	// // quant = "all";
	// // break;
	// // default:
	// // throw new RuntimeException("unexpected choice " +
	// // ((NodeChoice)((NodeSequence)node).nodes.get(2)).which + " is " +
	// // ((NodeChoice)((NodeSequence)node).nodes.get(2)).choice.getClass());
	// // }
	//
	// final SizzleType type = st.getType(((Identifier) ((NodeSequence)
	// node).nodes.get(3)).f0.tokenImage);
	//
	// final Set<String> ids = this.namefinder.visit((IdentifierList)
	// ((NodeSequence) node).nodes.get(0));
	//
	// for (final String id : ids)
	// st.set(id, type);
	//
	// for (final String id : ids) {
	// st.set(id, type);
	// final Set<String> indexees = this.indexeefinder.visit(n.f3, id);
	//
	// if (indexees.size() > 0) {
	// indexees.addAll(this.indexeefinder.visit(n.f5, id));
	// src.append("for (int ___" + id + " = 0; ");
	// final List<String> array = new ArrayList<String>(indexees);
	// src.append("___" + id + " < ___" + array.get(0) + ".length");
	// for (int i = 1; i < array.size(); i++)
	// src.append(" && " + id + " < " + array.get(i) + ".length");
	// src.append("; ___" + id + "++)\n");
	// }
	// }
	// }
	//
	// src.append("if (" + n.f3.accept(this, st) + ")\n");
	//
	// src.append(n.f5.accept(this, st));
	//
	// return src.toString();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public String visit(final WhileStatement n, final SymbolTable argu) {
	// throw new RuntimeException("unimplemented");
	// }
}