package sizzle.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.scannotation.ClasspathUrlFinder;

import sizzle.parser.ParseException;
import sizzle.parser.SizzleParser;
import sizzle.parser.syntaxtree.Start;

public class SizzleCompiler {
	private static Logger LOG = Logger.getLogger(SizzleCompiler.class);

	private static final List<String> find(final File f) {
		final List<String> l = new ArrayList<String>();

		if (f.isDirectory()) {
			for (final File g : f.listFiles())
				l.addAll(SizzleCompiler.find(g));
		} else {
			l.add(f.toString());
		}

		return l;
	}

	private static final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				SizzleCompiler.delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}

	private static void write(final InputStream in, final OutputStream out) throws IOException {
		final byte[] b = new byte[4096];
		int len;
		while ((len = in.read(b)) > 0)
			out.write(b, 0, len);
	}

	public static void main(final String[] args) throws IOException, ParseException {
		// parse the command line options
		final Options options = new Options();
		options.addOption("h", "hadoop-base", true, "base directory for Hadoop installation");
		options.addOption("l", "libs", true, "extra jars to be compiled into the jar");
		options.addOption("i", "in", true, "file to be compiled");
		options.addOption("o", "out", true, "the name of the resulting jar");
		options.addOption("n", "name", true, "the name of the job");

		CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());

			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("SizzleCompiler", options);

			return;
		}

		// get the base of the hadoop installation for compilation purposes
		String hadoopBase;
		if (cl.hasOption('h')) {
			hadoopBase = cl.getOptionValue('h');
		} else {
			System.err.println("missing required option `hadoop-base'");

			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("SizzleCompiler", options);

			return;
		}

		// find the location of the jar this class is in
		final String path = ClasspathUrlFinder.findClassBase(SizzleCompiler.class).getPath();
		// find the location of the Sizzle distribution
		final String root = new File(path.substring(path.indexOf(':') + 1, path.indexOf('!'))).getParentFile().getParent();

		// get the filename of the sizzle program we will be compiling
		File in;
		if (cl.hasOption('i'))
			in = new File(cl.getOptionValue('i'));
		else {
			System.err.println("missing required option `in'");

			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("SizzleCompiler", options);

			return;
		}
		final String filename = in.getName();

		final String name = filename.substring(0, filename.lastIndexOf('.'));

		// get the filename of the jar we will be writing
		String out;
		if (cl.hasOption('o'))
			out = cl.getOptionValue('o');
		else
			out = filename.substring(0, filename.indexOf('.')) + ".jar";

		// check filename for sanity
		if (!(filename.endsWith(".sizzle") || filename.endsWith(".szl")))
			throw new RuntimeException("unsupported extension for " + in.getAbsolutePath());

		// make the output directory
		final File dir = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		final File dirfile = new File(dir.getPath() + File.separatorChar + "sizzle");
		if (!dirfile.mkdirs())
			throw new IOException("unable to mkdir " + dirfile);

		final List<URL> libs = new ArrayList<URL>();
		if (cl.hasOption('l'))
			for (final String lib : cl.getOptionValues('l'))
				libs.add(new File(lib).toURI().toURL());

		final BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(dirfile.toString() + File.separatorChar + name + ".java"));
		try {
			final TypeCheckingVisitor typeChecker = new TypeCheckingVisitor();

			final URL url = CodeGeneratingVisitor.class.getClassLoader().getResource("SizzleJavaHadoop.stg");
			final BufferedReader s = new BufferedReader(new InputStreamReader(url.openStream()));
			final StringTemplateGroup stg;
			try {
				stg = new StringTemplateGroup(s);
			} finally {
				s.close();
			}

			final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor(name, stg);

			final SymbolTable st = new SymbolTable(libs);

			final BufferedReader r = new BufferedReader(new FileReader(in));
			try {
				new SizzleParser(r);

				final Start start = SizzleParser.Start();

				typeChecker.visit(start, st);

				final String src = codeGenerator.visit(start, st);

				o.write(src.getBytes());
			} finally {
				r.close();
			}
		} finally {
			o.close();
		}

		final String runtime = root + "/dist/sizzle-runtime.jar";
		final StringBuilder classPath = new StringBuilder(runtime);
		for (final File f : new File(hadoopBase).listFiles())
			if (Pattern.compile("hadoop-[a-z]+-\\d+\\.\\d+\\.\\d+\\.jar").matcher(f.getName()).matches())
				classPath.append(":" + f);
		for (final File f : new File(hadoopBase + File.separatorChar + "lib").listFiles())
			if (f.toString().endsWith(".jar"))
				classPath.append(":" + f);

		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		for (final String f1 : SizzleCompiler.find(dir)) {
			SizzleCompiler.LOG.info("compiling " + f1);
			if (f1.toString().endsWith(".java"))
				if (compiler.run(null, null, null, "-cp", classPath.toString(), f1.toString()) != 0)
					throw new RuntimeException("compile failed");
		}

		final JarOutputStream jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(new File(out))));
		try {
			final int sub = dir.toString().length() + 1;
			for (final String f : SizzleCompiler.find(dir)) {
				SizzleCompiler.LOG.info("adding " + f + " to " + out);
				jar.putNextEntry(new ZipEntry(f.substring(sub)));

				final InputStream inx = new BufferedInputStream(new FileInputStream(f));
				try {
					SizzleCompiler.write(inx, jar);
				} finally {
					inx.close();
				}

				jar.closeEntry();
			}

			final List<String> libsJars = new ArrayList<String>();
			libsJars.add(runtime);
			if (cl.hasOption('l'))
				libsJars.addAll(Arrays.asList(cl.getOptionValues('l')));
			for (final String lib : libsJars) {
				final File f = new File(lib);

				SizzleCompiler.LOG.info("adding lib/" + f.getName() + " to " + out);
				jar.putNextEntry(new JarEntry("lib" + File.separatorChar + f.getName()));
				final InputStream inx = new BufferedInputStream(new FileInputStream(f));
				try {
					SizzleCompiler.write(inx, jar);
				} finally {
					inx.close();
				}
			}
		} finally {
			jar.close();
		}

		SizzleCompiler.delete(dir);
	}
}
