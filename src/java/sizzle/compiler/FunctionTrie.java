package sizzle.compiler;

import java.util.Arrays;
import java.util.HashMap;

import sizzle.types.SizzleFunction;
import sizzle.types.SizzleType;
import sizzle.types.SizzleVarargs;

public class FunctionTrie {
	@SuppressWarnings("rawtypes")
	private final HashMap trie;

	@SuppressWarnings("rawtypes")
	public FunctionTrie() {
		this.trie = new HashMap();
	}

	private SizzleFunction getFunction(final Object[] ids) {
		if (this.trie.containsKey(ids[0])) {
			if (ids[0].equals("")) {
				return (SizzleFunction) this.trie.get("");
			} else {
				final SizzleFunction function = ((FunctionTrie) this.trie.get(ids[0])).getFunction(Arrays.copyOfRange(ids, 1, ids.length));

				if (function != null)
					return function;
			}
		} else {
			for (final Object o : this.trie.keySet())
				if (o instanceof SizzleVarargs && ((SizzleVarargs) o).assigns((SizzleType) ids[0])) {
					return ((FunctionTrie) this.trie.get(o)).getFunction();
				} else if (o instanceof SizzleType && ((SizzleType) o).assigns((SizzleType) ids[0])) {
					final SizzleFunction function = ((FunctionTrie) this.trie.get(o)).getFunction(Arrays.copyOfRange(ids, 1, ids.length));

					if (function != null)
						return function;
				}
		}

		return null;
	}

	private SizzleFunction getFunction() {
		return (SizzleFunction) this.trie.get("");
	}

	public SizzleFunction getFunction(final String name, final SizzleType[] formalParameters) {
		final Object[] ids = new Object[formalParameters.length + 2];

		ids[0] = name;

		for (int i = 0; i < formalParameters.length; i++)
			ids[i + 1] = formalParameters[i];

		ids[ids.length - 1] = "";

		final SizzleFunction function = this.getFunction(ids);

		if (function == null)
			throw new TypeException("no such function " + name + "(" + Arrays.toString(formalParameters) + ")");

		return function;
	}

	public boolean hasFunction(final String name, final SizzleType[] formalParameters) {
		try {
			this.getFunction(name, formalParameters);

			return true;
		} catch (final TypeException e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private void addFunction(final Object[] ids, final SizzleFunction sizzleFunction) {
		if (this.trie.containsKey(ids[0])) {
			if (ids[0].equals("")) {
				throw new RuntimeException("function already defined");
			} else {
				((FunctionTrie) this.trie.get(ids[0])).addFunction(Arrays.copyOfRange(ids, 1, ids.length), sizzleFunction);
			}
		} else {
			if (ids[0].equals("")) {
				this.trie.put("", sizzleFunction);
			} else {
				final FunctionTrie functionTrie = new FunctionTrie();
				functionTrie.addFunction(Arrays.copyOfRange(ids, 1, ids.length), sizzleFunction);
				this.trie.put(ids[0], functionTrie);
			}
		}
	}

	public void addFunction(final String name, final SizzleFunction sizzleFunction) {
		final SizzleType[] formalParameters = sizzleFunction.getFormalParameters();

		final Object[] ids = new Object[formalParameters.length + 2];

		ids[0] = name;

		for (int i = 0; i < formalParameters.length; i++)
			ids[i + 1] = formalParameters[i];

		ids[ids.length - 1] = "";

		this.addFunction(ids, sizzleFunction);
	}
}
