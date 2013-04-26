Sizzle [![Build Status](https://travis-ci.org/anthonyu/Sizzle.png)](https://travis-ci.org/anthonyu/Sizzle)
======

What is Sizzle?
---------------

Sizzle is an open source implementation of the Sawzall programming language designed for
interoperation with the Hadoop MapReduce and DFS stack.  It is implemented in pure Java, is easily
extensible, and the programs produced by it will run anywhere that has a recent Hadoop installed,
even if Sizzle is not also installed.

Why Sizzle?
-----------

Up until a few days ago, there was no publicly available implementation of Sawzall.

About six months ago, I asked some of the authors of _Interpreting the Data: Parallel Analysis with
Sawzall_ [[http://code.google.com/p/szl/wiki/Interpreting_the_Data]] for more specific details about
how Sawzall worked than was explained in that high-level document.  Mr. Pike explained that he
intended to open the source to Sawzall; however, when I didn't hear from him for several months I
started my own implementation.

What is the of status of Sizzle?
--------------------------------

The Sizzle release v0.0 available at [[https://github.com/anthonyu/Sizzle]] has:

* 100% compatibility with the syntax described in the Sawzall paper
* Pretty much all the useful Sawzall intrinsic functions described by [[http://szl.googlecode.com/svn/doc/sawzall-intrinsics.html]]. Currently missing are:
    * the protobuf, resourcestats and additionalinput functions, because I haven't yet personally found a need for them,
    * the convert function, because explicit and implicit casting works just as well 
    * the sortx, new and regex functions, because I didn't have time to finish them
* All of the aggregators discussed in [[http://code.google.com/p/szl/wiki/Sawzall_Table_Types]] with the exception of:
    * the sample aggregators, as they require an initial statistics generation pass over the data that Sizzle doesn't yet support.
    * the set and recordio aggregators, as I have no idea what they are supposed to do yet
* A complete runtime, allowing you to run Sawzall program on any recent Hadoop cluster

How is Sizzle better than szl?
------------------------------

If you are looking to run Sawzall programs on a single machine, then it's won't be: szl is currently
more complete and better tested.  However, it does not come with a MapReduce system and does not
interoperate with Hadoop, so you won't be easily running szl on more than one machine at a time for now.

For those who use Hadoop on the other hand, Sizzle is the only game in town because it makes it
possible to run non-trivial Sawzall progams on large computing clusters today, without needing to have
access to the MapReduce clusters down at the Googleplex.

The Sizzle compiler and runtime was designed from the start to interoperate with Hadoop, and does so
seamlessly.

In the long term, even after szl is integrated with Hadoop, Sizzle will still be a better choice for
most as it is more easily extended, and since it is native Java, more easily modified by its user base
of Java developers.

How do I compile Sizzle?
------------------------

Run ant in the top level directory.

E.g:

`bash$ ant`

How do I compile a Sawzall program with Sizzle?
-----------------------------------------------

It's as simple as running:

java -jar ***location of the sizzle compiler jar*** -h ***location of hadoop distribution***
-i ***a file containing Sawzall source code***

E.g.:

`bash$ java -jar /path/to/sizzle/dist/sizzle-compiler.jar  -h /path/to/hadoop-0.21.0 -i Simple.szl`

This compilation step will output a jar file, in this case named 'Simple.jar', which contains everything
necessary to run your Sawzall program on your local machine or a multi-node Hadoop cluster.

See also: [[Compiling Sizzle Programs]]

How do I run a Sawzall program?
-------------------------------

It's as simple as running:

hadoop jar ***output of the Sizzle compiler*** ***main class*** ***input file*** ***output file***

E.g., to continue the previous example:

`bash$ hadoop jar Simple.jar sizzle.Simple input output`

Which will run the program *Simple* on the file *input* and place its results in file *output*.

See also: [[Running Sizzle Programs]]

How do I extend Sizzle with new intrinsic functions?
----------------------------------------------------

It's as simple as writing a `public static` Java method and decorating it with the
`sizzle.functions.FunctionSpec` annotation.  For example, the following code implements and exports a
function named 'getenv' that takes a single 'string' argument and returns a 'string.' 

<code>
    @FunctionSpec(name = "getenv", returnType = "string", formalParameters = { "string" })
    public static String getenv(String variable) {
        return System.getenv(variable);
    }
</code>

Specify the jar containing that function's enclosing whenever you compile a Sizzle program, and it will
be made available to your Sawzall code.

See also: [[Extending Sizzle]]

How do I extend Sizzle with new aggregators?
----------------------------------------------------

It's as simple as writing a class that extends `sizzle.aggregators.Aggregator` and decorating it
with the `sizzle.aggregators.AggregatorSpec` annotation.

For example, the following code implements and exports an aggregator named 'log' that 
logs any data emitted to it via Log4J:

<code>
    import sizzle.aggregators.Aggregator;

    import org.apache.log4j.Logger;

    @AggregatorSpec(name = "log")
    public class LogAggregator extends Aggregator {
        private static Logger logger = Logger.getLogger(LogAggregator.class);

        @Override
        public void aggregate(final String data, final String metadata) throws IOException {
            logger.info(data);
        }
    }
</code>

Specify the jar containing that function's enclosing whenever you compile a Sizzle program, and it will
be made available to your Sawzall code.

See also: [[Extending Sizzle]]

How do I contribute to Sizzle development?
------------------------------------------

You name it. Sizzle is in need of your bug reports, test cases, documentation, examples and the
implementation of any missing features. Stake your claim by filing an issue in github, then send me a
pull request when you are ready.

Your contributions will be greatly appreciated!

License
-------

Copyright 2013 Anthony D. Urso (anthonyu)

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
