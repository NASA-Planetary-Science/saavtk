package edu.jhuapl.saavtk.util;

import java.io.File;
import java.net.URI;
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
public class SafePaths
{
	// Regular expression that will match one or more separators.
	private static final String PATH_SEPARATORS_REGEX = "[/\\\\][/\\\\]*";

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
	public static Path get(String first, String... more)
	{
		Path result = Paths.get(getStringUnchecked(first, more));
		if (result.compareTo(result.normalize()) != 0)
		{
			throw new IllegalArgumentException("Safe path strings may not include any redirections (./ or ../)");
		}
		return result;
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
	public static Path get(Iterable<String> sequence)
	{
		Path result = Paths.get(getStringUnchecked(sequence));
		if (result.compareTo(result.normalize()) != 0)
		{
			throw new IllegalArgumentException("Safe path strings may not include any redirections (./ or ../)");
		}
		return result;
	}

	/**
	 * Uses {@link Paths} to convert the given URI to a {@link Path} object. Prior
	 * to conversion, the URI object is checked to ensure it is normalized, and if
	 * not an IllegalArgumentException is thrown.
	 * 
	 * @param uri the input {@link URI} object
	 * @return the {@link Path} object
	 * @throws IllegalArgumentException if the input URI is not "normalized", i.e.,
	 *             if any URI element is "." or "..".
	 */
	public static Path get(URI uri)
	{
		if (uri.compareTo(uri.normalize()) != 0)
		{
			throw new IllegalArgumentException("Safe path URIs may not include any redirections (./ or ../)");
		}
		return Paths.get(uri);
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
	public static String getString(String first, String... more)
	{
		return get(first, more).toString();
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
	public static String getString(Iterable<String> sequence)
	{
		return get(sequence).toString();
	}

	/**
	 * Concatenate a path string, or a sequence of strings that when joined form a
	 * path string, to a string containing a lexically valid path on the current OS.
	 * The input strings may include and freely mix separators valid for any
	 * supported OS. All such separators are converted to the correct separator for
	 * the current OS, as specified by File.separator. Multiple consecutive
	 * separators are replaced with a single separator. This method does not check
	 * for normalization.
	 * <p>
	 * For example C:\Users\my/data//file.txt would be converted to
	 * C:\Users\my\data\file.txt on Windows and C:/Users/my/data/file.txt on Unix or
	 * MacOS.
	 * 
	 * @param first the first string in the sequence being combined into the path
	 * @param more any remaining strings in the sequence being combined into the
	 *            path
	 * @return the path string
	 */
	public static String getStringUnchecked(String first, String... more)
	{
		String combined = more.length == 0 ? first : String.join("/", first, String.join("/", more));
		// If the separator is backslash, which Java uses for escape sequences, need to
		// escape the escape or replaceAll will get confused.
		String separator = File.separator.equals("\\") ? "\\\\" : File.separator;
		return combined.replaceAll(PATH_SEPARATORS_REGEX, separator);
	}

	/**
	 * Concatenate an {@link Iterable} sequence of strings that when joined form a
	 * path string, to a string containing a lexically valid path on the current OS.
	 * The input strings may include and freely mix separators valid for any
	 * supported OS. All such separators are converted to the correct separator for
	 * the current OS, as specified by File.separator. Multiple consecutive
	 * separators are replaced with a single separator. This method does not check
	 * for normalization.
	 * <p>
	 * For example C:\Users\my/data//file.txt would be converted to
	 * C:\Users\my\data\file.txt on Windows and C:/Users/my/data/file.txt on Unix or
	 * MacOS.
	 * 
	 * @param sequence the {@link Iterable} sequence of strings being combined into
	 *            the path
	 * @return the path string
	 */
	public static String getStringUnchecked(Iterable<String> sequence)
	{
		return getStringUnchecked(String.join("/", sequence));
	}

	// TEST CODE.
	public static void main(String[] args)
	{
		String[] testPaths = { "C:\\Users\\\\user/data\\/file.txt", "/\\home/user/\\\\/", "file.txt", "/file", "relativePath/", "/", "bin/foo/", ""
		};

		System.out.println("Test0");
		for (String path : testPaths)
		{
			System.out.println("Paths.getString(\"" + path + "\") = \"" + SafePaths.getString(path) + "\"");
		}

		System.out.println("\nTest1");
		ImmutableList<String> testList = ImmutableList.of("path", "to/", "\\\\my", "files/file.txt");
		System.out.println("Paths.getString(" + testList + ") = \"" + SafePaths.getString(testList) + "\"");
	}
}
