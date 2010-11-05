package sizzle.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import sizzle.aggregators.AggregatorSpec;
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
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBool;
import sizzle.types.SizzleFloat;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleInt;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleScalar;
import sizzle.types.SizzleString;
import sizzle.types.SizzleTable;
import sizzle.types.SizzleTuple;
import sizzle.types.SizzleType;

/**
 * Prescan the Sizzle program and check that all variables are consistently
 * typed.
 * 
 * @author anthonyu
 * 
 */
public class TypeCheckingVisitor extends GJDepthFirst<SizzleType, SymbolTable> {
	private final NameFindingVisitor namefinder;

	/**
	 * Construct a TypeCheckingVisitor.
	 * 
	 * @param namefinder
	 *            A {@link NameFindingVisitor} used to find names.
	 */
	public TypeCheckingVisitor(final NameFindingVisitor namefinder) {
		this.namefinder = namefinder;
	}

	public List<SizzleType> check(final ExpressionList e, final SymbolTable argu) {
		final List<SizzleType> types = new ArrayList<SizzleType>();

		types.add(e.f0.accept(this, argu));

		if (e.f1.present())
			for (final Node i : e.f1.nodes)
				types.addAll(this.check((ExpressionRest) i, argu));

		return types;
	}

	public List<SizzleType> check(final ExpressionRest e, final SymbolTable argu) {
		final List<SizzleType> types = new ArrayList<SizzleType>();

		types.add(e.f1.accept(this, argu));

		return types;
	}

	public List<SizzleScalar> check(final TypleList t, final SymbolTable argu) {
		final List<SizzleScalar> types = new ArrayList<SizzleScalar>();

		final SizzleType type = t.f0.accept(this, argu);
		if (type instanceof SizzleScalar)
			types.add((SizzleScalar) type);
		else
			throw new TypeException("unexpected type " + type + " in a typle list");

		if (t.f1.present())
			for (final Node i : t.f1.nodes)
				types.addAll(this.check((TypleRest) i, argu));

		return types;
	}

	public List<SizzleScalar> check(final TypleRest t, final SymbolTable argu) {
		final List<SizzleScalar> types = new ArrayList<SizzleScalar>();

		final SizzleType type = t.f1.accept(this, argu);
		if (type instanceof SizzleScalar)
			types.add((SizzleScalar) type);
		else
			throw new TypeException("unexpected type " + type + " in a typle list");

		return types;
	}

	// FIXME: really support implicit cast on assignment?
	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Assignment n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;
		final SizzleType t = argu.get(id);
		final SizzleType u = n.f2.accept(this, argu);

		if (t.assigns(u))
			return t;

		if (argu.hasCast(u, t))
			return t;

		throw new TypeException("incorrect type " + u + " for assignment to " + id + ':' + t);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final AssignmentList n, final SymbolTable argu) {
		n.f0.accept(this, argu);
		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				((EmitStatement) node).accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final AssignmentRest n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final AssignmentStatement n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Atom n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // identifier
			return ((Identifier) n.f0.choice).accept(this, argu);
		case 1: // string
			return new SizzleString();
		case 2: // integer
			return new SizzleInt();
		case 3: // float
			return new SizzleFloat();
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Block n, final SymbolTable argu) {

		if (n.f1.present()) {
			SymbolTable st;
			try {
				st = argu.cloneNonLocals();
			} catch (final IOException e) {
				throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
			}

			for (final Node node : n.f1.nodes)
				((Statement) node).accept(this, st);
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final CallExpression n, final SymbolTable argu) {
		final String name = n.f0.f0.tokenImage;

		if (n.f2.present())
			return argu.getFunction(name, this.check((ExpressionList) n.f2.node, argu));
		else
			return argu.getFunction(name);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ComparisonExpression n, final SymbolTable argu) {
		final SizzleType lhs = n.f0.accept(this, argu);

		if (n.f1.present()) {
			final SizzleType rhs = ((NodeSequence) n.f1.node).nodes.get(1).accept(this, argu);
			if (!rhs.compares(lhs))
				throw new TypeException("invalid type " + rhs + " for comparison with " + lhs);
			return new SizzleBool();
		}

		return lhs;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final EmitStatement n, final SymbolTable argu) {
		final String id = n.f1.f0.tokenImage;
		final SizzleTable t = (SizzleTable) argu.get(id);

		if (t == null)
			throw new TypeException("emitting to undeclared table " + id);

		if (n.f2.present()) {
			final List<SizzleType> indices = new ArrayList<SizzleType>();
			for (final Node node : n.f2.nodes)
				indices.add(((NodeSequence) node).nodes.get(1).accept(this, argu));

			if (indices.size() != t.countIndices())
				throw new TypeException("incorrect number of indices for " + id);

			for (int i = 0; i < t.countIndices(); i++)
				if (!t.getIndex(i).assigns(indices.get(i)))
					throw new TypeException("incorrect type " + indices.get(i) + " for index " + i);
		} else if (t.countIndices() > 0)
			throw new TypeException("indices missing from emit");

		final List<SizzleType> expressions = this.check(n.f4, argu);
		if (!t.accepts(expressions))
			throw new TypeException("incorrect types " + expressions + " for " + id + ":" + t);

		if (n.f5.present()) {
			if (t.getWeightType() == null)
				throw new TypeException("unexpected weight specified by emit");

			final SizzleType wtype = ((NodeSequence) n.f5.node).nodes.get(1).accept(this, argu);

			if (!t.acceptsWeight(wtype))
				throw new TypeException("incorrect type " + wtype + " for weight of " + id + ":" + t.getWeightType());
		} else if (t.getWeightType() != null)
			throw new TypeException("no weight specified by emit");

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Expression n, final SymbolTable argu) {
		final SizzleType lhs = n.f0.accept(this, argu);

		if (n.f1.present()) {
			for (final Node node : n.f1.nodes) {
				final SizzleType rhs = ((NodeSequence) node).nodes.get(1).accept(this, argu);
				if (!lhs.compares(rhs))
					throw new TypeException("invalid type " + rhs + " for testing agains " + lhs);
			}
			return new SizzleBool();
		} else
			return lhs;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ExpressionList n, final SymbolTable argu) {
		final SizzleType type = n.f0.accept(this, argu);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				if (!type.assigns(((ExpressionRest) node).accept(this, argu)))
					throw new TypeException("invalid expression list");

		return new SizzleArray((SizzleScalar) type);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ExpressionRest n, final SymbolTable argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final FloatingPointLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ForStatement n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		if (n.f2.present())
			((AssignmentList) n.f2.node).accept(this, st);
		if (n.f4.present())
			((ExpressionList) n.f4.node).accept(this, st);
		if (n.f6.present())
			((ExpressionList) n.f6.node).accept(this, st);

		n.f8.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Identifier n, final SymbolTable argu) {
		final SizzleType sizzleType = argu.get(n.f0.tokenImage);

		if (sizzleType == null)
			throw new TypeException(n.f0.tokenImage + " is not defined");

		return sizzleType;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IdentifierList n, final SymbolTable argu) {
		for (final String id : this.namefinder.visit(n))
			if (argu.contains(id))
				throw new TypeException(id + " already declared as " + argu.get(id));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IdentifierRest n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IfStatement n, final SymbolTable argu) {
		n.f2.accept(this, argu);
		n.f4.accept(this, argu);

		if (n.f5.present())
			((Statement) ((NodeSequence) n.f5.node).elementAt(1)).accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Index n, final SymbolTable argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IndexExpression n, final SymbolTable argu) {
		final SizzleType type = n.f0.accept(this, argu);

		if (n.f1.present()) {
			final SizzleType itype = ((NodeSequence) n.f1.node).nodes.get(1).accept(this, argu);
			if (type instanceof SizzleArray) {
				if (!new SizzleInt().assigns(itype))
					throw new TypeException("incorrect type " + itype + " for indexing into array");

				return ((SizzleArray) type).getType();
			} else if (type instanceof SizzleMap) {
				final SizzleScalar ktype = ((SizzleMap) type).getIndexType();

				if (!ktype.assigns(itype))
					throw new TypeException("incorrect type " + itype + " for indexing into " + type);

				return ((SizzleMap) type).getType();
			} else
				throw new TypeException("type " + type + " cannot be indexed into");
		}

		return type;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Initializer n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // expression
			return n.f0.accept(this, argu);
		case 1:
			return ((NodeSequence) n.f0.choice).nodes.get(1).accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}

	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IntegerLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Mapping n, final SymbolTable argu) {
		final SizzleType vtype = n.f0.accept(this, argu);
		if (!vtype.getClass().getSuperclass().equals(SizzleScalar.class))
			throw new TypeException("invalid type " + vtype + " for map value");

		final SizzleType ktype = n.f2.accept(this, argu);
		if (!ktype.getClass().getSuperclass().equals(SizzleScalar.class))
			throw new TypeException("invalid type " + ktype + " for map key");

		return new SizzleMap((SizzleScalar) vtype, (SizzleScalar) ktype);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final MappingList n, final SymbolTable argu) {
		final SizzleMap mtype = (SizzleMap) n.f0.accept(this, argu);

		if (n.f1.present())
			for (final Node node : n.f1.nodes) {
				final SizzleType rtype = ((MappingRest) node).accept(this, argu);
				if (!mtype.assigns(rtype))
					throw new TypeException("invalid type " + rtype + " for entry in " + mtype);
			}

		return mtype;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final MappingRest n, final SymbolTable argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final MemberExpression n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;
		final String member = n.f2.f0.tokenImage;

		final SizzleType stype = argu.get(id);

		if (!(stype instanceof SizzleTuple))
			throw new TypeException(id + " is not a tuple" + stype.getClass().getName());

		final SizzleType mtype = ((SizzleTuple) stype).getMember(member);

		if (mtype == null)
			throw new TypeException("no such member " + member + " in " + id);

		return mtype;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ParentheticalExpression n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final PlusExpression n, final SymbolTable argu) {
		if (n.f1.present()) {
			SizzleType type = n.f0.accept(this, argu);
			for (final Node node : n.f1.nodes) {
				final SizzleType accept = ((NodeSequence) node).nodes.get(1).accept(this, argu);
				type = type.arithmetics(accept);
			}
			return type;
		} else
			return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Program n, final SymbolTable argu) {
		if (n.f0.present())
			for (final Node node : n.f0.nodes)
				((Proto) node).accept(this, argu);

		for (final Node node : n.f1.nodes)
			((Statement) node).accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Proto n, final SymbolTable argu) {
		final String tokenImage = n.f1.f0.tokenImage;

		argu.importProto(tokenImage.substring(1, tokenImage.length() - 1));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SliceExpression n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;
		final SizzleType type = argu.get(id);

		if (n.f1.present())
			for (final Node node : n.f1.nodes) {
				final SizzleType slicetype = ((NodeSequence) node).nodes.get(1).accept(this, argu);
				if (slicetype.getClass() != SizzleInt.class)
					throw new TypeException("invalid type " + slicetype + " for indexing into an array");
				final SizzleType slicetype2 = ((NodeSequence) node).nodes.get(3).accept(this, argu);
				if (slicetype2.getClass() != SizzleInt.class)
					throw new TypeException("invalid type " + slicetype2 + " for indexing into an array");
			}

		return type;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Start n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Statement n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // table declaration
			((Block) n.f0.choice).accept(this, argu);
			break;
		case 1: // table declaration
			((TableDeclaration) n.f0.choice).accept(this, argu);
			break;
		case 2:
		case 3: // variable declaration
			((VariableDeclaration) n.f0.choice).accept(this, argu);
			break;
		case 4: // assignment statement
			((AssignmentStatement) n.f0.choice).accept(this, argu);
			break;
		case 5: // emit statement
			((EmitStatement) n.f0.choice).accept(this, argu);
			break;
		case 6: // emit statement
			((IfStatement) n.f0.choice).accept(this, argu);
			break;
		case 8: // for statement
			((ForStatement) n.f0.choice).accept(this, argu);
			break;
		case 9: // when statement
			((WhenStatement) n.f0.choice).accept(this, argu);
			break;
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StringLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TableDeclaration n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;

		if (argu.contains(id))
			throw new TypeException("variable " + id + " already declared as " + argu.get(id));

		List<SizzleScalar> indexTypes = null;
		if (n.f5.present()) {
			indexTypes = new ArrayList<SizzleScalar>();

			for (final Node node : n.f5.nodes) {
				final SizzleType sizzleType = node.accept(this, argu);

				if (!(sizzleType instanceof SizzleScalar))
					throw new TypeException("incorrect type " + sizzleType + " for index");

				indexTypes.add((SizzleScalar) sizzleType);
			}
		}

		List<SizzleScalar> types;
		List<String> names = null;
		switch (n.f7.which) {
		case 0: // typle
			types = Arrays.asList((SizzleScalar) n.f7.choice.accept(this, argu));
			// names = new
			// ArrayList<String>(this.namefinder.visit((Typle)n.f7.choice));
			break;
		case 1: // identifier
			types = Arrays.asList((SizzleScalar) argu.getType(((Identifier) n.f7.choice).f0.tokenImage));
			break;
		case 2: // typle list
			final TypleList tl = (TypleList) ((NodeSequence) n.f7.choice).nodes.get(1);
			types = this.check(tl, argu);
			names = new ArrayList<String>(this.namefinder.visit(tl.f0));
			if (tl.f1.present())
				for (final Node i : tl.f1.nodes)
					names.addAll(this.namefinder.visit(((TypleRest) i).f1));
			break;
		default:
			throw new RuntimeException("unexpected choice " + n.f7.which + " is " + n.f7.choice.getClass());
		}

		final AggregatorSpec annotation = ((Class<?>) argu.getAggregator(n.f3.f0.tokenImage, types.get(0))).getAnnotation(AggregatorSpec.class);

		SizzleScalar tweight = null;
		if (n.f8.present()) {
			if (annotation.weightType().equals("none"))
				throw new TypeException("unexpected weight for table declaration");

			final SizzleType aweight = argu.getType(annotation.weightType());
			tweight = (SizzleScalar) ((NodeSequence) n.f8.node).nodes.get(1).accept(this, argu);

			if (!aweight.assigns(tweight))
				throw new TypeException("incorrect weight type for table declaration");
		} else if (!annotation.weightType().equals("none"))
			throw new TypeException("missing weight for table declaration");

		if (n.f4.present())
			if (annotation.formalParameters().length == 0)
				throw new TypeException("no arguments for " + n.f3.f0.tokenImage);

		argu.set(id, new SizzleTable(types, names, indexTypes, tweight));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Term n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // call
			return n.f0.choice.accept(this, argu);
		case 1: // member
			return n.f0.choice.accept(this, argu);
		case 2: // slice
			return n.f0.choice.accept(this, argu);
		case 3: // index
			return n.f0.choice.accept(this, argu);
		case 4: // parenthetical
			return ((ParentheticalExpression) n.f0.choice).f1.accept(this, argu);
		case 5: // atom
			return n.f0.choice.accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TimesExpression n, final SymbolTable argu) {
		final SizzleType accepts = n.f0.accept(this, argu);
		if (n.f1.present()) {
			SizzleScalar type;
			if (accepts instanceof SizzleFunction)
				type = (SizzleScalar) ((SizzleFunction) accepts).getType();
			else
				type = (SizzleScalar) accepts;

			for (final Node node : n.f1.nodes) {
				final SizzleType accept = ((NodeSequence) node).nodes.get(1).accept(this, argu);

				type = type.arithmetics(accept);
			}
			return type;
		} else
			return accepts;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Typle n, final SymbolTable argu) {
		return argu.getType(n.f2.f0.tokenImage);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TypleList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TypleRest n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final UnaryExpression n, final SymbolTable argu) {
		final SizzleType type = n.f0.accept(this, argu);

		if (n.f1.present())
			if (!type.getClass().equals(SizzleInt.class))
				throw new TypeException("unary arithmetic on non-integer " + type.toString());

		return type;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final VariableDeclaration n, final SymbolTable argu) {
		for (final String id : this.namefinder.visit(n.f1)) {
			if (argu.contains(id))
				throw new TypeException("variable " + id + " already declared as " + argu.get(id));

			switch (n.f3.which) {
			case 0: // identifier
				final SizzleType ittype = argu.getType(((Identifier) n.f3.choice).f0.tokenImage);

				if (n.f4.present()) {
					final SizzleType ietype = ((Initializer) ((NodeSequence) n.f4.node).nodes.get(1)).accept(this, argu);

					if (!ittype.assigns(ietype) && !argu.hasCast(ittype, ietype))
						throw new TypeException("incorrect type " + ietype + " for assignment to " + id + ':' + ittype);

					argu.set(id, ittype, n.f0.present());
				} else
					argu.set(id, ittype);
				break;
			case 1: // map
				final NodeSequence seq = (NodeSequence) n.f3.choice;
				final SizzleType mvtype = new SizzleMap((SizzleScalar) argu.getType(((Identifier) seq.elementAt(2)).f0.tokenImage),
						(SizzleScalar) argu.getType(((Identifier) seq.elementAt(5)).f0.tokenImage));

				if (n.f4.present()) {
					final SizzleType mitype = ((Initializer) ((NodeSequence) n.f4.node).nodes.get(1)).accept(this, argu);

					if (!mvtype.assigns(mitype))
						throw new TypeException("incorrect type " + mitype + " for assignment to " + id + ':' + mvtype);
				}

				argu.set(id, mvtype);
				break;
			case 2: // array
				final SizzleType avtype = new SizzleArray((SizzleScalar) argu.getType(((Identifier) ((NodeSequence) n.f3.choice).nodes.get(2)).f0.tokenImage));

				if (n.f4.present()) {
					final SizzleType aitype = ((Initializer) ((NodeSequence) n.f4.node).nodes.get(1)).accept(this, argu);

					if (!avtype.assigns(aitype))
						throw new TypeException("incorrect type " + aitype + " for assignment to " + id + ':' + avtype);
				}

				argu.set(id, avtype);
				break;
			default:
				throw new RuntimeException("unexpected choice " + n.f3.which + " is " + n.f3.choice.getClass());
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final WhenStatement n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		for (final Node node : n.f2.nodes) {
			final SizzleType type = st.getType(((Identifier) ((NodeSequence) node).nodes.get(3)).f0.tokenImage);

			final Set<String> ids = this.namefinder.visit((IdentifierList) ((NodeSequence) node).nodes.get(0));

			for (final String id : ids)
				st.set(id, type);
		}

		n.f3.accept(this, st);
		n.f5.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final WhileStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}
}
