package sizzle.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sizzle.parser.syntaxtree.Assignment;
import sizzle.parser.syntaxtree.AssignmentList;
import sizzle.parser.syntaxtree.AssignmentRest;
import sizzle.parser.syntaxtree.AssignmentStatement;
import sizzle.parser.syntaxtree.Atom;
import sizzle.parser.syntaxtree.Block;
import sizzle.parser.syntaxtree.CallExpression;
import sizzle.parser.syntaxtree.ComparisonExpression;
import sizzle.parser.syntaxtree.EmitStatement;
import sizzle.parser.syntaxtree.Expression;
import sizzle.parser.syntaxtree.ExpressionList;
import sizzle.parser.syntaxtree.ExpressionRest;
import sizzle.parser.syntaxtree.FloatingPointLiteral;
import sizzle.parser.syntaxtree.ForStatement;
import sizzle.parser.syntaxtree.Identifier;
import sizzle.parser.syntaxtree.IdentifierList;
import sizzle.parser.syntaxtree.IdentifierRest;
import sizzle.parser.syntaxtree.IfStatement;
import sizzle.parser.syntaxtree.Index;
import sizzle.parser.syntaxtree.IndexExpression;
import sizzle.parser.syntaxtree.Initializer;
import sizzle.parser.syntaxtree.IntegerLiteral;
import sizzle.parser.syntaxtree.Mapping;
import sizzle.parser.syntaxtree.MappingList;
import sizzle.parser.syntaxtree.MappingRest;
import sizzle.parser.syntaxtree.MemberExpression;
import sizzle.parser.syntaxtree.Node;
import sizzle.parser.syntaxtree.ParentheticalExpression;
import sizzle.parser.syntaxtree.PlusExpression;
import sizzle.parser.syntaxtree.Program;
import sizzle.parser.syntaxtree.Proto;
import sizzle.parser.syntaxtree.SliceExpression;
import sizzle.parser.syntaxtree.Start;
import sizzle.parser.syntaxtree.Statement;
import sizzle.parser.syntaxtree.StringLiteral;
import sizzle.parser.syntaxtree.TableDeclaration;
import sizzle.parser.syntaxtree.Term;
import sizzle.parser.syntaxtree.TimesExpression;
import sizzle.parser.syntaxtree.Typle;
import sizzle.parser.syntaxtree.TypleList;
import sizzle.parser.syntaxtree.TypleRest;
import sizzle.parser.syntaxtree.UnaryExpression;
import sizzle.parser.syntaxtree.VariableDeclaration;
import sizzle.parser.syntaxtree.WhenStatement;
import sizzle.parser.syntaxtree.WhileStatement;
import sizzle.parser.visitor.GJNoArguDepthFirst;

public class NameFindingVisitor extends GJNoArguDepthFirst<Set<String>> {
	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Assignment n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final AssignmentList n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final AssignmentRest n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final AssignmentStatement n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Atom n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Block n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final CallExpression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ComparisonExpression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final EmitStatement n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Expression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ExpressionList n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ExpressionRest n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final FloatingPointLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ForStatement n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Identifier n) {
		return new HashSet<String>(Arrays.asList(n.f0.tokenImage));
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IdentifierList n) {
		final HashSet<String> set = new HashSet<String>();

		set.add(n.f0.f0.tokenImage);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.add(((IdentifierRest) node).f1.f0.tokenImage);

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IdentifierRest n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IfStatement n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Index n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IndexExpression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Initializer n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IntegerLiteral n) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Mapping n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final MappingList n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final MappingRest n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final MemberExpression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ParentheticalExpression n) {
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final PlusExpression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Program n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Proto n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SliceExpression n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Start n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Statement n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final StringLiteral n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TableDeclaration n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Term n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TimesExpression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Typle n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TypleList n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TypleRest n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final UnaryExpression n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final VariableDeclaration n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final WhenStatement n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final WhileStatement n) {
		throw new RuntimeException("unimplemented");
	}
}
