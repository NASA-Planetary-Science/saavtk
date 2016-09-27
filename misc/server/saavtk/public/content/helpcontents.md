---
title: User Manual
---

User Manual
===========

The Small Body Mapping Tool is divided into 2 panels, the rendering
panel on the right for displaying small bodies and their associated
data in 3D and a control panel on the left consisting of several tabs
controlling how the data is displayed in the rendering panel. In
addition there is a status bar on the bottom and a menu bar on top.

When the Small Body Mapping Tool is started, initially, a low resolution
model of the Eros asteroid is shown. The small body shown can be changed
in the View menu. There other numerous other small bodies besides Eros
available. Most of them have been downloaded from the PDS. The number
of and contents of the tabs in the control panel are different for each
shape model. Each of these tabs will be described below.

## The Rendering Panel

The rendering panel shows a 3 dimensional view of the small bodies and
their associated data. Using the mouse and keyboard one can easily
navigate through the data in the rendering panel. The following mouse
and key bindings work with the rendering panel:

-   Left Mouse Button: rotate camera
-   Middle Mouse Button: pan camera
-   Right Mouse Button: zoom camera
-   Mousewheel: zoom camera
-   Shift + Left Mouse Button: pan camera
-   Ctrl + Left Mouse Button: spin camera
-   Keypress 3: toggle stereo mode
-   Keypress f: fly to point most recently clicked
-   Keypress c: set center of rotation of camera to be point cursor is
    hovering over
-   Keypress r: reset camera
-   Keypress s: modify objects in scene to be shown as surfaces
-   Keypress w: modify objects in scene to be shown as wireframe
-   Keypress x (lowercase): reorient camera to point in positive x
    direction
-   Keypress X (uppercase): reorient camera to point in negative x
    direction
-   Keypress y (lowercase): reorient camera to point in positive y
    direction
-   Keypress Y (uppercase): reorient camera to point in negative y
    direction
-   Keypress z (lowercase): reorient camera to point in positive z
    direction
-   Keypress Z (uppercase): reorient camera to point in negative z
    direction
-   Keypress n: spin camera so that positive z points up

In addition, whenever one moves the mouse pointer over a point of the
small body, the latitude, longitude, radius, and distance is shown on
the right part of the status bar. Longitude is defined east longitude
and varies from 0 to 360 degrees. Radius is the distance from the center
of the body to the point and distance is the distance from the camera to
the center of the body.

In addition if one clicks with the left mouse button on certain object,
more information about that object is shown on the left side of the
status bar. If one clicks with the right button on certain objects, a
context menu is displayed with various options depending on the object.

## Control Panel

### Leftmost tab with same name as shape model (e.g. Eros, Itokawa)

Each small body has a tab with the same name as the body for controlling
general options related to that body, such as resolution, how the
asteroid is colored, whether to show a coordinate grid, or what shading
to use (flat or smooth). Some of these options may be different for
different small bodies. Note that the coordinate grid lines are
separated by 10 degrees in latitude or longitude.

### Imaging tab (e.g. MSI for Eros, AMICA for Itokawa, etc.)

The imaging tab (MSI, AMICA, etc.)  provides options for searching and
displaying images acquired by cameras of several of the
asteroids. Only several shape models currently have this tab. Usually,
one of two databases of images can be searched, which can be specified
in the "Source" dropdown menu:

1.  The original database of images submitted to PDS which use the
    SPICE kernel files for pointing information.  These images may be
    slightly misaligned with the asteroid due to imprecision in the
    SPICE kernels from which the pointing information was derived.
2.  Robert Gaskell's list which he used to create the shape models. This
    list is a subset of the first and are much better registered with
    the asteroid.

To do a search, choose the desired options and click the Search button.
A list of the matching images will be returned below. When a search is
performed, the outline of the footprint of the first several matching
images is shown in the renderer in red. This allows one to quickly see
what part of the asteroid is within the image frustum without
downloading the entire image. One can see additional footprints by
clicking on the next and previous buttons below the list. The number of
outlines shown at one time can also be controlled.

The two buttons, Select Region and Clear Region, can be used to restrict
the search to a specific region on the asteroid. If you click on the
Select Region a new mode is entered which allows you to draw a circular
region on the asteroid but does not allow you to navigate around the
asteroid. Click the Select Region button again to leave this mode and
return to the default navigation mode. Click the Clear Region button to
remove the region. If there is no region drawn, then the search includes
the entire asteroid.

You might notice when you do searches that often images are returned
that are outside of the circle drawn. This is due to the fact that as
long as a part of the image is within a small distance of the circle,
it will be returned in the search. This results from the approximations
used in the search algorithm.

To map the image directly onto the asteroid, right-click either on the
outline in the renderer or on an item in the returned list in the
control panel. A popup menu will appear and clicking on the "Show Image"
option will map the image onto the asteroid. Once an image is shown,
additional menu items become active such as showing a properties window
and generation of backplanes.

The properties window that appears shows a 2D view of the image, various
properties about the image, as well as a slider to modify the contrast.

The backplane generation option generates an image volume where each
plane in the volume contains information about each pixel in the MSI
Image.

This is the list, in order of the backplanes generated. All values are
in float (4 bytes)

1.  image pixel value
2.  x value of point in body centered coordinates (kilometers)
3.  y value of point in body centered coordinates (kilometers)
4.  z value of point in body centered coordinates (kilometers)
5.  latitude (geocentric) (degrees)
6.  longitude (degrees)
7.  distance from center of asteroid (kilometers)
8.  solar incidence angle (degrees)
9.  emission angle (degrees)
10. phase angle (degrees)
11. horizontal pixel scale in kilometers per pixel
12. vertical pixel scale in kilometers per pixel
13. Slope in degrees
14. Elevation in meters
15. Gravitational acceleration in meters per second squared
16. Gravitational potential in joules per kilogram

### NIS tab (Eros only)

The NIS tab provides an interface for searching, displaying, and
plotting spectra acquired by the NEAR Near-Infrared Spectrograph (NIS)
which measured several hundred thousand spectra of the asteroid between
January and May 2000. Each spectrum consists of 64 separate channels
between 812 nm and 2708 nm. The NIS tab is very similar to the MSI tab
and much of the information described there applies here as well.

### Lidar data tab (NLR for Eros, LIDAR for Itokawa)

The NLR tab for Eros and the LIDAR tab for Itokawa provides an
interface showing lidar data. Currently there are 2 subtabs within the
lidar tab, the Browse tab and the Search tab.

The Browse tab allows you to browse and show data from individual days.
To show data from a specific data, select a day and click on the Show
button beneath the list. The text of the button will then change to
Remove and if you click it again the data will be removed from the
renderer. Click the Remove All Lidar Data to remove all the data being
displayed. The Save button can be used to save the raw data file to
disk. Since the data may not be well registered with the asteroid a
slider is provided for changing the radial offset of the data. In
addition another slider for controlling how much data of a given day is
shown. There are 2 knobs on the slider which control the start and end
data points shown.

The Search tab can be used search for lidar data by specifying
specific start and stop dates. In addition, a search region to
restrict the search to can be specified as well exactly like the
imaging tab. A radial offset slider is provided as in the Browse tab.


## Menu Options

This section explains the available menu options in the tool.

### File Menu

##### Export to Image

Saves the current renderer to an image file. You can save it using
common image formats such as PNG, JPEG, and TIFF formats.

##### Export Six Views along Axes to Images

Automatically positions the renderer's camera to point in +x, -x, +y,
-y, +z, and -z directions and saves an image to file of each of the 6
views. When the file dialog prompts you for the name of the output
file, it is only necessary to enter the initial part of the filename
without the extensions. For example, if you enter the name "image",
then the following 6 files will be generated (if PNG format is
selected): image-x.png, image+x.png, image-z.png, image-y.png,
image+z.png, image+y.png.

##### Export Shape Model

Exports the currently viewed shape model to a file. The format of the
exported file is in the same format which Robert Gaskell distributes
his shape models (vertex-facet version). See
e.g. [here](http://sbn.psi.edu/pds/resource/erosshape.html).

##### Camera

Opens dialog which allows you to change some preperties of the
camera. Currently changing only vertical field of view and the
distance of the camera from the origin is supported.

##### Preferences (On Macs, this in the Small Body Mapping Tool menu)

Opens a dialog that allows you to change various preferences.

### View Menu

The View menu contains all built-in shape models that the tool
supports. When you click on a shape model from the view menu, that
shape model will be displayed in the renderer and the tabs in the
control panel on the left will change depending on the shape
model. Note that renderers for different shape models are completely
independent. The shape model you were viewing before changing to a
different shape model will still be maintained in memory and if you
return to it later, it will be in exactly the same state you left it.

### Help Menu

##### Help Contents

Opens a web browser showing this page.

##### About Small Body Mapping Tool (On Macs, this in the Small Body Mapping Tool menu)

Opens a dialog showing the version of the tool and copyright information.
