package sizzle.compiler;

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
import sizzle.parser.syntaxtree.NodeChoice;
import sizzle.parser.syntaxtree.NodeOptional;
import sizzle.parser.syntaxtree.NodeSequence;
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
import sizzle.parser.visitor.GJDepthFirst;

public class IndexeeFindingVisitor extends GJDepthFirst<Set<String>, String> {
	private final NameFindingVisitor namefinder;

	public IndexeeFindingVisitor(final NameFindingVisitor namefinder) {
		this.namefinder = namefinder;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Assignment n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final AssignmentList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final AssignmentRest n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final AssignmentStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Atom n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Block n, final String argu) {
		final Set<String> set = new HashSet<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				set.addAll(((Statement) node).accept(this, argu));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final CallExpression n, final String argu) {
		if (n.f2.present()) {
			return ((ExpressionList) n.f2.node).accept(this, argu);
		} else {
			return new HashSet<String>();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ComparisonExpression n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		if (n.f1.present())
			indexees.addAll(((PlusExpression) ((NodeSequence) n.f1.node).nodes.get(1)).accept(this, argu));

		indexees.addAll(n.f0.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final EmitStatement n, final String argu) {
		return n.f4.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Expression n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((NodeSequence) node).nodes.get(1).accept(this, argu));

		indexees.addAll(n.f0.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ExpressionList n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((ExpressionRest) node).accept(this, argu));

		indexees.addAll(n.f0.accept(this, argu));

		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ExpressionRest n, final String argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final FloatingPointLiteral n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ForStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Identifier n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IdentifierList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IdentifierRest n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IfStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Index n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IndexExpression n, final String argu) {
		final HashSet<String> set = new HashSet<String>();

		if (n.f1.present())
			for (final String s : this.namefinder.visit((Expression) ((NodeSequence) n.f1.node).nodes.get(1)))
				if (s.equals(argu))
					set.add(n.f0.f0.tokenImage);

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Initializer n, final String argu) {
		switch (n.f0.which) {
		case 0:
			return ((Expression) n.f0.choice).accept(this, argu);
		case 1: // node sequence
			final NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.f0.choice).nodes.get(1);
			if (nodeOptional.present()) {
				final NodeChoice nodeChoice = (NodeChoice) nodeOptional.node;
				switch (nodeChoice.which) {
				case 1: // expression list
					return ((ExpressionList) nodeChoice.choice).accept(this, argu);
				default:
					throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
				}
			} else {
				return new HashSet<String>();
			}
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final IntegerLiteral n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Mapping n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final MappingList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final MappingRest n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final MemberExpression n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final ParentheticalExpression n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final PlusExpression n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((TimesExpression) ((NodeSequence) node).nodes.get(1)).accept(this, argu));

		indexees.addAll(n.f0.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Program n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		for (final Node node : n.f1.nodes)
			indexees.addAll(((Statement) node).accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Proto n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final SliceExpression n, final String argu) {
		final String id = n.f0.f0.tokenImage;

		final HashSet<String> set = new HashSet<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes) {
				for (final String s : this.namefinder.visit((Expression) ((NodeSequence) node).nodes.get(1)))
					if (s.equals(argu))
						set.add(id);
				for (final String s : this.namefinder.visit((Expression) ((NodeSequence) node).nodes.get(3)))
					if (s.equals(argu))
						set.add(id);
			}

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Start n, final String argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Statement n, final String argu) {
		switch (n.f0.which) {
		case 0: // table declaration
			return ((Block) n.f0.choice).accept(this, argu);
		case 1: // table declaration
			return ((TableDeclaration) n.f0.choice).accept(this, argu);
		case 2: // variable declaration
			return ((VariableDeclaration) n.f0.choice).accept(this, argu);
		case 3: // variable declaration
			return ((VariableDeclaration) n.f0.choice).accept(this, argu);
		case 5: // emit statement
			return ((EmitStatement) n.f0.choice).accept(this, argu);
		case 9: // when statement
			return ((WhenStatement) n.f0.choice).accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final StringLiteral n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TableDeclaration n, final String argu) {
		return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Term n, final String argu) {
		switch (n.f0.which) {
		case 0: // index expression
			return ((CallExpression) n.f0.choice).accept(this, argu);
		case 2: // slice expression
			return ((SliceExpression) n.f0.choice).accept(this, argu);
		case 3: // index expression
			return ((IndexExpression) n.f0.choice).accept(this, argu);
		case 5: // atom
			return new HashSet<String>();
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TimesExpression n, final String argu) {
		final Set<String> indexees = new HashSet<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				indexees.addAll(((UnaryExpression) node).accept(this, argu));

		indexees.addAll(n.f0.accept(this, argu));

		return indexees;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final Typle n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TypleList n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final TypleRest n, final String argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final UnaryExpression n, final String argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final VariableDeclaration n, final String argu) {
		if (n.f4.present())
			return ((Initializer) ((NodeSequence) n.f4.node).nodes.get(1)).accept(this, argu);
		else
			return new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final WhenStatement n, final String argu) {
		final Set<String> set = new HashSet<String>();

		// the variable declarations in the node list n.f2 cannot be indexed by
		// anything
		set.addAll(n.f3.accept(this, argu));
		set.addAll(n.f5.accept(this, argu));

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> visit(final WhileStatement n, final String argu) {
		throw new RuntimeException("unimplemented");
	}
}
