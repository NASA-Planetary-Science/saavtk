package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.collect.ImmutableList;

/**
 * Front-end to the standard Java {@link Paths} facility that properly
 * interprets input strings that mix separators from different operating systems
 * (OS), i.e., / and \ in the same inputs. In addition to methods with the same
 * signatures as methods in {@link Paths}, some additional similar methods are
 * provided that accept Iterable<String>, and that return Strings rather than
 * paths.
 * <p>
 * It is worth stating explicitly that no path may safely use a separator from
 * another OS as a normal character, e.g., while Unix supports naming a file
 * "bad\idea.txt", this facility would interpret the \ as a separator. This is
 * not a significant limitation for code that is intended to run on multiple OS.
 * <p>
 * Note that this would need to be updated to support an OS that uses some other
 * separator.
 * 
 * @author James Peachey
 *
 */
public class SafeURLPaths
{
	private static final SafeURLPaths INSTANCE = new SafeURLPaths(File.separator);

	public static SafeURLPaths instance()
	{
		return INSTANCE;
	}

	private final String pathSegmentDelimiter;

	private SafeURLPaths(String pathSegmentDelimiter)
	{
		this.pathSegmentDelimiter = pathSegmentDelimiter;
	}

	/**
	 * Uses {@link Paths} to convert a path string, or a sequence of strings that
	 * when joined form a path string, to a {@link Path} object containing a
	 * lexically valid path on the current OS. The input strings may include and
	 * freely mix separators valid for any supported OS. All such separators are
	 * converted to the correct separator for the current OS, as specified by
	 * File.separator. Multiple consecutive separators are replaced with a single
	 * separator. The Path object is checked to ensure it is normalized prior to
	 * return, and if not an IllegalArgumentException is thrown.
	 * <p>
	 * For example C:\Users\my/data//file.txt would be converted to
	 * C:\Users\my\data\file.txt on Windows and C:/Users/my/data/file.txt on Unix or
	 * MacOS.
	 * 
	 * @param first the first string in the sequence being combined into the path
	 * @param more any remaining strings in the sequence being combined into the
	 *            path
	 * @return the {@link Path} object
	 * @throws IllegalArgumentException if the converted path is not "normalized",
	 *             i.e., if any directory element in the converted path is "." or
	 *             "..".
	 */
	public Path get(String first, String... more)
	{
		String string = getString(first, more);
		if (string.contains("spud"))
		{
			System.err.println("first is " + first + " get string is " + string);
		}
		return Paths.get(string);
	}

	/**
	 * Uses {@link Paths} to convert an {@link Iterable} sequence of strings that
	 * when joined form a path string, to a {@link Path} object containing a
	 * lexically valid path on the current OS. The input strings may include and
	 * freely mix separators valid for any supported OS. All such separators are
	 * converted to the correct separator for the current OS, as specified by
	 * File.separator. Multiple consecutive separators are replaced with a single
	 * separator. The Path object is checked to ensure it is normalized prior to
	 * return, and if not an IllegalArgumentException is thrown.
	 * <p>
	 * For example C:\Users\my/data//file.txt would be converted to
	 * C:\Users\my\data\file.txt on Windows and C:/Users/my/data/file.txt on Unix or
	 * MacOS.
	 * 
	 * @param sequence the {@link Iterable} sequence of strings being combined into
	 *            the path
	 * @return the {@link Path} object
	 * @throws IllegalArgumentException if the converted path is not "normalized",
	 *             i.e., if any directory element in the converted path is "." or
	 *             "..".
	 */
	public Path get(Iterable<String> sequence)
	{
		String string = getString(sequence);
		if (string.contains("spud"))
		{
			System.err.println("first is " + sequence.iterator().next() + " get string is " + string);
		}
		return Paths.get(string);
	}

	/**
	 * Uses {@link Paths} to convert a path string, or a sequence of strings that
	 * when joined form a path string, to a string containing a lexically valid path
	 * on the current OS. The input strings may include and freely mix separators
	 * valid for any supported OS. All such separators are converted to the correct
	 * separator for the current OS, as specified by File.separator. Multiple
	 * consecutive separators are replaced with a single separator. The converted
	 * string is checked to ensure it is normalized prior to return, and if not an
	 * IllegalArgumentException is thrown.
	 * <p>
	 * For example C:\Users\my/data//file.txt would be converted to
	 * C:\Users\my\data\file.txt on Windows and C:/Users/my/data/file.txt on Unix or
	 * MacOS.
	 * 
	 * @param first the first string in the sequence being combined into the path
	 * @param more any remaining strings in the sequence being combined into the
	 *            path
	 * @return the path string
	 * @throws IllegalArgumentException if the converted path is not "normalized",
	 *             i.e., if any directory element in the converted path is "." or
	 *             "..".
	 */
	public String getString(String first, String... more)
	{
		return joinSegments(getSegments(first, more));
	}

	/**
	 * Uses {@link Paths} to convert an {@link Iterable} sequence of strings that
	 * when joined form a path string, to a string containing a lexically valid path
	 * on the current OS. The input strings may include and freely mix separators
	 * valid for any supported OS. All such separators are converted to the correct
	 * separator for the current OS, as specified by File.separator. Multiple
	 * consecutive separators are replaced with a single separator. The converted
	 * string is checked to ensure it is normalized prior to return, and if not an
	 * IllegalArgumentException is thrown.
	 * <p>
	 * For example C:\Users\my/data//file.txt would be converted to
	 * C:\Users\my\data\file.txt on Windows and C:/Users/my/data/file.txt on Unix or
	 * MacOS.
	 * 
	 * @param sequence the {@link Iterable} sequence of strings being combined into
	 *            the path
	 * @return the path string
	 * @throws IllegalArgumentException if the converted path is not "normalized",
	 *             i.e., if any directory element in the converted path is "." or
	 *             "..".
	 */
	public String getString(Iterable<String> sequence)
	{
		return getString(String.join("/", sequence));
	}

	private String[] getSegments(String first, String... more)
	{
		// Join all the arguments with slashes between, then split them on any kind of path delimiter.
		String combined = more.length == 0 ? first : String.join("/", first, String.join("/", more));
		String[] segments = combined.split("[/\\\\]+", -1);

		// Check if these segments begin with a non-file URL protocol.  
		boolean nonLocalUrl = segments[0].matches("\\w+:") && !segments[0].equalsIgnoreCase("file:");

		if (nonLocalUrl)
		{
			for (String segment : segments)
			{
				if (segment.equals(".") || segment.equals(".."))
				{
					throw new IllegalArgumentException("A non-local URL may not contain any redirections (./ or ../)");
				}
			}
		}
		return segments;
	}

	private String joinSegments(String[] segments)
	{
		StringBuilder builder = new StringBuilder(segments[0]);

		boolean url;
		if (segments[0].equalsIgnoreCase("file:"))
		{
			url = true;
			if (segments.length > 1 && (segments[1].equals(".") || segments[1].equals("..")))
			{
				builder.append("/");
				// This appears to be a relative path reference within a file url. Attempt to do
				// what is probably intended in this case.
				try
				{
					segments[1] = new File(".").getCanonicalPath().replaceAll("[/\\\\]", "/") + "/" + segments[1];
				}
				catch (@SuppressWarnings("unused") IOException e)
				{
					// Ignore this exception -- probably it won't ever happen. If it does,
					// another exception will be thrown as soon as anyone tries to access the file.
					// That exception will still be clear, as segments[1] will indicate a relative path.
				}
			}
			else
			{
				builder.append("//");
			}
		}
		else if (segments[0].matches("^\\w\\w+:$")) // This is kinda hinky -- drive letters on Windows are 1 character. Assume all protocols have > 1 character.
		{
			url = true;
			builder.append("/");
		}
		else
		{
			url = false;
		}

		String delimiter = url ? "/" : pathSegmentDelimiter;
		for (int index = 1; index < segments.length; ++index)
		{
			builder.append(delimiter);
			builder.append(segments[index]);
		}
		return builder.toString();
	}

	// TEST CODE.
	public static void main(String[] args)
	{
		// Test Linux/Mac:
		test("/");

		System.out.println();

		// Test Windows:
		test("\\");
	}

	// TEST CODE.
	private static void test(String separator)
	{
		System.out.println("Running test with separator \"" + separator + "\"");

		SafeURLPaths safePaths = new SafeURLPaths(separator);

		String[] testPaths = { "C:\\Users\\\\user/data\\/file.txt", "/\\home/user/\\\\/", "file.txt",
				"/file", "relativePath/", "/", "bin/foo/", "",
				"file:/C:/spud/junk.html", "https://sbmt.jhuapl.edu", "file:", "ftp:", "http:///", "file://///",
				"file:/../../../../../../Downloads/SHAPE0.obj", "file:///Downloads/../Downloads/SHAPE0.obj"
		};

		System.out.println("Test0");
		for (String path : testPaths)
		{
			System.out.println("Paths.getString(\"" + path + "\") = \"" + safePaths.getString(path) + "\"");
		}

		System.out.println("\nTest1");
		ImmutableList<String> testList = ImmutableList.of("path", "to/", "\\\\my", "files/file.txt");
		System.out.println("Paths.getString(" + testList + ") = \"" + safePaths.getString(testList) + "\"");
	}
}
