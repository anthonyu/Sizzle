package sizzle.compiler;

import junit.framework.Assert;

import org.junit.Test;

import sizzle.types.SizzleAny;
import sizzle.types.SizzleBool;
import sizzle.types.SizzleFloat;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleInt;
import sizzle.types.SizzleString;
import sizzle.types.SizzleType;
import sizzle.types.SizzleVarargs;

public class TestFunctionTrie {
	@Test
	public void testFunctionTrieSingleParameter() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final SizzleFunction sizzleFunction = new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleString() });

		functionTrie.addFunction("function", sizzleFunction);
		functionTrie.addFunction("function", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleInt() }));

		Assert.assertEquals("did not return correct function", sizzleFunction, functionTrie.getFunction("function", new SizzleType[] { new SizzleString() }));
	}

	@Test
	public void testFunctionTrieMultiParameter() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final SizzleFunction sizzleFunction = new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleInt(), new SizzleFloat(), new SizzleString() });

		functionTrie.addFunction("function", sizzleFunction);
		functionTrie.addFunction("function", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleString(), new SizzleFloat(), new SizzleInt() }));

		Assert.assertEquals("did not return correct function", sizzleFunction,
				functionTrie.getFunction("function", new SizzleType[] { new SizzleInt(), new SizzleFloat(), new SizzleString() }));
	}

	@Test
	public void testFunctionTrieOverloadedArgsShort() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final SizzleFunction sizzleFunction = new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleInt() });

		functionTrie.addFunction("function", sizzleFunction);
		functionTrie.addFunction("function", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleInt(), new SizzleString() }));

		Assert.assertEquals("did not return correct function", sizzleFunction, functionTrie.getFunction("function", new SizzleType[] { new SizzleInt() }));
	}

	@Test
	public void testFunctionTrieOverloadedArgsLong() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final SizzleFunction sizzleFunction = new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleInt(), new SizzleString() });

		functionTrie.addFunction("function", sizzleFunction);
		functionTrie.addFunction("function", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleInt() }));

		Assert.assertEquals("did not return correct function", sizzleFunction,
				functionTrie.getFunction("function", new SizzleType[] { new SizzleInt(), new SizzleString() }));
	}

	@Test
	public void testFunctionTrieGeneric() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final SizzleFunction sizzleFunction = new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleAny() });

		functionTrie.addFunction("function", sizzleFunction);
		functionTrie.addFunction("function", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleInt() }));

		Assert.assertEquals("did not return correct function", sizzleFunction, functionTrie.getFunction("function", new SizzleType[] { new SizzleString() }));
	}

	@Test
	public void testFunctionTrieVarargs() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final SizzleFunction sizzleFunction = new SizzleFunction(new SizzleBool(),
				new SizzleType[] { new SizzleString(), new SizzleVarargs(new SizzleString()) });

		functionTrie.addFunction("function", sizzleFunction);
		functionTrie.addFunction("function", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleString(), new SizzleVarargs(new SizzleInt()) }));

		Assert.assertEquals("did not return correct function", sizzleFunction,
				functionTrie.getFunction("function", new SizzleType[] { new SizzleString(), new SizzleString(), new SizzleString(), new SizzleString() }));
	}
}
