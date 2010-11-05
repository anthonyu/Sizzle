package sizzle.compiler;

import sizzle.parser.syntaxtree.Initializer;
import sizzle.parser.syntaxtree.Mapping;
import sizzle.parser.syntaxtree.MappingList;
import sizzle.parser.syntaxtree.MappingRest;
import sizzle.parser.syntaxtree.Node;
import sizzle.parser.syntaxtree.NodeSequence;
import sizzle.parser.syntaxtree.Program;
import sizzle.parser.syntaxtree.Start;
import sizzle.parser.syntaxtree.Statement;
import sizzle.parser.syntaxtree.VariableDeclaration;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleType;

/**
 * Prescan the Sizzle program and generate initializer code for any static
 * variables.
 * 
 * @author anthonyu
 * 
 */
public class StaticDeclarationCodeGeneratingVisitor extends CodeGeneratingVisitor {
	private final NameFindingVisitor namefinder;

	/**
	 * Construct a StaticDeclarationCodeGeneratingVisitor.
	 * 
	 * @param namefinder
	 *            A {@link NameFindingVisitor} used to find names.
	 */
	public StaticDeclarationCodeGeneratingVisitor(final NameFindingVisitor namefinder) {
		this.namefinder = namefinder;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Mapping n, final SymbolTable argu) {
		return argu.getMapName() + ".put(" + n.f0.accept(this, argu) + ", " + n.f2.accept(this, argu) + ");\n";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final MappingList n, final SymbolTable argu) {
		final StringBuilder src = new StringBuilder();

		src.append(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				src.append(((MappingRest) node).accept(this, argu));

		return src.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final MappingRest n, final SymbolTable argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Start n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Program n, final SymbolTable argu) {
		final StringBuilder statements = new StringBuilder();

		for (final Node i : n.f1.nodes) {
			final String src = ((Statement) i).accept(this, argu);
			if (src != null && !src.equals(""))
				statements.append(src + "\n");
		}

		return statements.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Statement n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 2: // static variable declaration
			return ((VariableDeclaration) n.f0.choice).accept(this, argu);
		default:
			return "";
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VariableDeclaration n, final SymbolTable argu) {
		final StringBuilder src = new StringBuilder();

		for (final String id : this.namefinder.visit(n.f1)) {
			final SizzleType sizzleType = argu.get(id);

			final String javaType = sizzleType.toJavaType();

			if (!n.f0.present())
				return "";

			if (n.f4.present()) {
				argu.setInitializerType(sizzleType);
				argu.setMapName("___" + id);
				final String initializer = ((Initializer) ((NodeSequence) n.f4.node).elementAt(1)).accept(this, argu);
				argu.setMapName(null);
				argu.setInitializerType(null);

				src.append("private static final " + javaType + " ___" + id + " = " + initializer + ";");
				if (sizzleType.getClass().equals(SizzleMap.class)) {
					src.append("\n{\n" + argu.getStaticInitializer() + "\n}\n");
				}
			} else {
				throw new RuntimeException("static variables must be initialized");
			}

		}

		return src.toString();
	}
}
