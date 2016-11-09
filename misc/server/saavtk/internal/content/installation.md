---
title: Small Body Mapping Tool
---

## Installation Instructions

### Prerequisites

The Small Body Mapping Tool is supported on the following platforms:

-   Windows 7 or higher (64-bit)
-   Linux, recent distributions (64-bit)
-   Mac OS X version 10.5 or higher (64-bit Intel-based Macs only)

In addition, the 64-bit version of Java 6 or 7 must be installed on
your system for Windows or Linux platforms (unless running the Windows
version of the tool below which includes Java). For Mac platforms,
Java 6 must be installed--Java 7 is not supported at this time.

For Windows and Linux platforms, Java can be downloaded from
[http://www.java.com](http://www.java.com), if not already installed
on your system. Make sure to download the 64-bit version of Java, not
the 32-bit version.

On Macs, Java 6 comes either preinstalled (Leopard or Snow Leopard),
or is available as an add-on directly from Apple (Lion or later). The
version of Java available at
[http://www.java.com](http://www.java.com) for Macs, which is Java 7,
will not work with the SBMT. However, even if you've already installed
Java 7, it is not necessary to uninstall it, and the SBMT should be
able to find Java 6 if it is present on your system.

### Download the tool

The Small Body Mapping Tool is distributed as a self-contained zip
file. Download the version appropriate for your platform and follow
the steps below. For the benefit of Windows users, we provide 2
versions of the tool, one with Java and one without. If you download
the version with Java included, then you do not need to install Java
separately on your system.

   -  Mac: [sbmt-2014.04.09-macosx-x64.zip](releases/sbmt-2014.04.09-macosx-x64.zip)
   -  Linux: [sbmt-2014.04.09-linux-x64.zip](releases/sbmt-2014.04.09-linux-x64.zip)
   -  Windows: [sbmt-2014.04.09-windows-x64.zip](releases/sbmt-2014.04.09-windows-x64.zip) (does not include Java)
   -  Windows: [sbmt-2014.04.09-windows-x64-with-java.zip](releases/sbmt-2014.04.09-windows-x64-with-java.zip) (includes Java)

For those who have previously used the tool, please note that we no
longer support launching it with Java Web Start. The only way to launch
the tool now is by downloading one of the above files.

### Steps to launch the tool:

1. Make sure Java is installed on your system (unless using the Windows version above which includes Java).
2. Download the appropriate file above for your platform.
3. Unzip the file to any folder
4. Navigate to the 'sbmt' folder
5. (This step may be omitted if located on the APL campus.)
   You will need to create a password.txt file that contains your
   username and password so that the tool can connect to the APL
   server. There are 2 ways to do this:
       * Open the 'password.txt' file located in the 'sbmt' folder in a text
         editor and enter your username on the first line and password on the
         second line.
       * Alternatively, create a password.txt file that contains your
         username on the first line and password on the second line
         and place this file in the folder '\$HOME/.neartool'. '\$HOME' is
         the path to your home folder and '.neartool' is the folder the
         SBMT uses internally to store preferences and cached
         data. For example, if your home folder is located at
         '/Users/foo', then you would create the file at
         '/Users/foo/.neartool/password.txt'. Note that you would
         normally need to create the file using a terminal since
         hidden files are normally not visible with Finder. If the
         folder '.neartool' does not yet exist, create it. Placing the
         password.txt file in this location has the advantage of not
         requiring you to edit the password.txt file each time you
         download a new version of the tool.

    The tool first looks in the '.neartool' folder for the password.txt file
    and, if not present, then looks in the 'sbmt' folder.
6. On Mac and Linux platforms, run the 'runsbmt' shell script. On Windows, run the 'runsbmt.exe' program.


## Mac Application Bundle (Mac Only)

For Mac users, a Mac Application Bundle is available which provides a
convenient way to always run the latest version of the
tool. Internally, it consists of a script which downloads the latest
version of the tool (if necessary), unzips it and runs it. This is a
newer version of the launcher (version 2.0) that replaces the previous
version that was available on the older website (the previous version
made use of Java Web Start which we no longer use).

To run the tool using this method, download the file
[sbmt-launcher-2.0.zip](sbmt-launcher-2.0.zip) and unzip it to any
folder. From within Finder, you can then double-click on the SBMT
Launcher application to launch the tool. In addition, you can drag the
application to the Dock and launch it from there. Note that each time
you run the launcher, a new instance of the tool will start.

If outside the APL campus, you will need to create password.txt file
so the tool can connect to the SBMT server. See step 5 above (second
alternate way only) for instructions how to this, as those
instructions apply here as well.

For those interested, the launcher downloads the latest version of the
SBMT to the folder '$HOME/.neartool/app'.

## Previous Releases

Previous releases of the tool can be found [here](releases). Previous
releases are not supported and are provided only as a fallback in
case of a problem with the most recent version.
