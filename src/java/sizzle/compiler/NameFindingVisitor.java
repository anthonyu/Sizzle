package sizzle.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import sizzle.parser.syntaxtree.ForStatement;
import sizzle.parser.syntaxtree.Function;
import sizzle.parser.syntaxtree.FunctionType;
import sizzle.parser.syntaxtree.Identifier;
import sizzle.parser.syntaxtree.IdentifierList;
import sizzle.parser.syntaxtree.IfStatement;
import sizzle.parser.syntaxtree.Index;
import sizzle.parser.syntaxtree.IntegerLiteral;
import sizzle.parser.syntaxtree.MapType;
import sizzle.parser.syntaxtree.Node;
import sizzle.parser.syntaxtree.NodeSequence;
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
import sizzle.parser.visitor.GJNoArguDepthFirst;

public class NameFindingVisitor extends GJNoArguDepthFirst<Set<String>> {

	@Override
	public Set<String> visit(final Start n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Program n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Declaration n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final TypeDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final StaticVarDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final VarDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Type n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Component n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ArrayType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final TupleType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SimpleTupleType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SimpleMemberList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SimpleMember n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoTupleType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoMemberList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoMember n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ProtoFieldDecl n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final MapType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final OutputType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ExprList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final FunctionType n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Statement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Assignment n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Block n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final BreakStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ContinueStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final DoStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final EmitStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ExprStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ForStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final IfStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ResultStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final ReturnStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SwitchStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final WhenStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final IdentifierList n) {
		final HashSet<String> set = new HashSet<String>();

		set.add(n.f0.f0.tokenImage);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(((NodeSequence) node).elementAt(1).accept(this));

		return set;
	}

	@Override
	public Set<String> visit(final WhileStatement n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Expression n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Conjunction n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Comparison n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final SimpleExpr n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Term n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Factor n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Selector n) {
		return new HashSet<String>(Arrays.asList(n.f1.f0.tokenImage));
	}

	@Override
	public Set<String> visit(final Index n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Call n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final RegexpList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Regexp n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Operand n) {
		switch (n.f0.which) {
		case 0: // identifier
			return n.f0.accept(this);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	@Override
	public Set<String> visit(final Composite n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final PairList n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Pair n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Function n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final StatementExpr n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final Identifier n) {
		return new HashSet<String>(Arrays.asList(n.f0.tokenImage));
	}

	@Override
	public Set<String> visit(final IntegerLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final FingerprintLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final FloatingPointLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final CharLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final StringLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final BytesLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public Set<String> visit(final TimeLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Assignment n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final AssignmentList n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final AssignmentRest n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final AssignmentStatement n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Atom n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Block n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final CallExpression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final ComparisonExpression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final EmitStatement n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Expression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final ExpressionList n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final ExpressionRest n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final FloatingPointLiteral n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final ForStatement n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Identifier n) {
	// return new HashSet<String>(Arrays.asList(n.f0.tokenImage));
	// }
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final IndexExpression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Initializer n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final IntegerLiteral n) {
	// return new HashSet<String>();
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Pair n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final PairList n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final MemberExpression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final ParentheticalExpression n) {
	// return n.f1.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final PlusExpression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Program n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final ProtoStatement n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final SliceExpression n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Start n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Statement n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final StringLiteral n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Term n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final TimesExpression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final Typle n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final TypleList n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final TypleRest n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final UnaryExpression n) {
	// return n.f0.accept(this);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final VariableDeclaration n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final WhenStatement n) {
	// throw new RuntimeException("unimplemented");
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public Set<String> visit(final WhileStatement n) {
	// throw new RuntimeException("unimplemented");
	// }
}
