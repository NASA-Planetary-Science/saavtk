package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Preconditions;
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
		return Paths.get(getString(first, more));
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
		return Paths.get(getString(sequence));
	}

	public Path get(String[] sequence)
	{
		return Paths.get(getString(sequence));
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

	public String getString(String[] sequence)
	{
		return getString(String.join("/", sequence));
	}

	public String getUrl(String string)
	{
		if (!hasProtocol(string))
		{
			// Assume string is a file path. Convert it to canonical form and replace backslashes with forward slashes.
			try
			{
				string = new File(string).getCanonicalPath().replace("\\", "/");
			}
			catch (IOException e)
			{
				// Print but otherwise ignore this error. The URL may have an invalid string in it, but this
				// should cause obvious problems when it's accessed.
				e.printStackTrace();
			}

			// Create a file:///absolute/url out of the string with exactly 3 slashes after file: no matter what OS.
			string = "file:///" + string.replaceFirst("^/", "");
		}
		return string;
	}

	/**
	 * Return whether the supplied string looks like it has a URL protocol that
	 * SafeURLPaths could handle. Specifically, this method checks whether the
	 * supplied string starts with 2 or more characters followed by a colon,
	 * optionally followed by one or more path delimiters (any kind) and other text.
	 * 2 characters are required to distinguish a potential protocol from the case
	 * of drive letters on a Windows system.
	 * 
	 * <pre>
	 * This method does not check the rest of the string for valid URL syntax.
	 * 
	 * @param path the path string to check.
	 * @return true if the path starts with what appears to be a URL protocol, false
	 *         otherwise.
	 */
	boolean hasProtocol(String path)
	{
		Preconditions.checkNotNull(path);
		return path.matches("\\w\\w+:$") || path.matches("\\w\\w+:[/\\\\].*");
	}

	boolean hasFileProtocol(String path)
	{
		Preconditions.checkNotNull(path);
		return path.matches("[Ff][Ii][Ll][Ee]:") || path.matches("[Ff][Ii][Ll][Ee]:[/\\\\].*");
	}

	private String[] getSegments(String first, String... more)
	{
		// Join all the arguments with slashes between.
		String combined = more.length == 0 ? first : String.join("/", first, String.join("/", more));

		// Now split the combined string on any kind of path delimiter.
		String[] segments = combined.split("[/\\\\]+", -1);

		// Check if these segments begin with a non-file URL protocol.  
		boolean nonLocalUrl = hasProtocol(segments[0]) && !hasFileProtocol(segments[0]);

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
		boolean isUrl = hasProtocol(segments[0]);
		boolean isFileUrl = hasFileProtocol(segments[0]);

		StringBuilder builder = new StringBuilder(segments[0]);

		if (isFileUrl)
		{
			builder.append("///");
		}
		else if (isUrl)
		{
			builder.append("//");
		}

		// For URLs, start with a blank delimiter because the right number
		// of initial slashes was added above.
		String delimiter = isUrl ? "" : pathSegmentDelimiter;
		for (int index = 1; index < segments.length; ++index)
		{
			builder.append(delimiter);
			builder.append(segments[index]);
			delimiter = isUrl ? "/" : pathSegmentDelimiter;
		}

		String result = builder.toString();
		if (pathSegmentDelimiter.equals("\\") && result.startsWith(pathSegmentDelimiter))
		{
			// Windows hack to prevent leading backslashes before drive letters.
			result = result.replaceFirst("^\\\\([A-Za-z]:)", "$1");
		}
		return result;
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

		String[] testPaths = {
				"\\C:\\Users\\\\user/data\\/file.txt", "/\\home/user/\\\\/", "/C:/", "\\c", "\\c:",
				"file.txt", "/file", "relativePath/", "/", "bin/foo/", "",
				"file:/C:/spud/junk.html", "https://sbmt.jhuapl.edu",
				"file:", "file://///", "ftp:", "http:///", "file:/../../../../../../Downloads/SHAPE0.obj",
				"file:///Downloads/../Downloads/SHAPE0.obj", "local/relative/path", "/absolute/path"
		};

		System.out.println("Test0");
		for (String path : testPaths)
		{
			System.out.println("Paths.getString(\"" + path + "\") = \"" + safePaths.getString(path) + "\"");
		}

		System.out.println("\nTest1");
		ImmutableList<String> testList = ImmutableList.of("path", "to/", "\\\\my", "files/file.txt");
		System.out.println("Paths.getString(" + testList + ") = \"" + safePaths.getString(testList) + "\"");

		System.out.println("\nTest2");
		try
		{
			safePaths.getString("http://sbmt.jhuapl.edu/sbmt/../secret/path");
			System.err.println("FAILED -- no exception thrown for a relative http:// URL");
		}
		catch (@SuppressWarnings("unused") IllegalArgumentException e)
		{
			System.out.println("Passed");
		}

		System.out.println("\nTest3");
		System.out.println("Paths.getUrl(relative/path/) = \"" + safePaths.getUrl("relative/path").toString() + "\"");

		System.out.println("\nTest4");
		System.out.println("Paths.getUrl(/absolute/path/) = \"" + safePaths.getUrl("/absolute/path/").toString() + "\"");

		System.out.println("\nTest5");
		System.out.println("Paths.getUrl(relative/path/./../path/with/redirects) = \"" + safePaths.getUrl("relative/path/./../path/with/redirects").toString() + "\"");

		System.out.println("\nTest6 (on Windows, should be interpreted as file:///C:/absolute/path. Otherwise, treated like a relative path.)");
		System.out.println("Paths.getUrl(C:\\absolute\\path\\) = \"" + safePaths.getUrl("C:\\absolute\\path\\").toString() + "\"");

	}
}
