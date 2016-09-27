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
in the View menu. Three other numerous other small bodies besides Eros
available. Most of them have been downloaded from the PDS. In addition
it is possible for the user to import a custom shape model. The number
of and contents of the tabs in the control panel are different for each
asteroid. Each of these tabs will be described below.

The Rendering Panel
-------------------

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

Control Panel
-------------

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
asteroids. Usually, one of two databases of images can be
searched, which can be specified in the "Source" dropdown menu:

1.  The original database of images submitted to PDS which use the
    SPICE kernel files for pointing information.  These images may be
    slightly misaligned with the asteroid due to imprecision in the
    SPICE kernels from which the pointing information was derived.
2.  Bob Gaskell's list which he used to create the shape models. This
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

After selecting the desired options and clicking Search, a list of
tracks will be displayed. Right-clicking on a track (either in the list
or directly on the track in the 3D view) will display a popup with
various options. The following is an explanation of these options:

-   Track Color - Change color of track
-   Save Track - Save selected track to a text file. The text file contains
    these columns in order:
      1. Time of lidar point in UTC
      2. X lidar point in kilometers in body frame coordinates
      3. Y lidar point in kilometers in body frame coordinates
      4. Z lidar point in kilometers in body frame coordinates
      5. X spacecraft position in kilometers in body frame coordinates
      6. Y spacecraft position in kilometers in body frame coordinates
      7. Z spacecraft position in kilometers in body frame coordinates
      8. Lidar range in kilometers (distance in kilometers between
         lidar point and spacecraft position)
-   Save All Visible Tracks - Save all visible tracks (i.e. excluding
    tracks which are hidden using options below). Output files have
    same format as the Save Track option.
-   Hide Track - Hide track
-   Hide Other Tracks - Hide all other tracks
-   Plot Track... - Create 6 plots showing elevation, potential, and
    acceleration vs. distance and time. The algorithm of Werner and
    Scheeres is used for the computation as explained at the bottom of
    [this](references.html) web page. The data in each of these plots
    can be saved to a text file using the "Export Data..." option in the
    File menu of each plot window.


### Lineament tab (Eros only)

The Lineament tab shows lineaments structures drawn by Debra Buczkowski
several years ago using a different tool. Since the lineaments are not
perfectly registered with the asteroid, a slider is provided for
slightly changing the radial offset of the lineaments.

### Structures tab (Available for all bodies)

Besides searching and visualizing mission data, the tool allows you to
draw different types of structures directly on the asteroid. Currently 5
types of structures are supported: paths, polygons, circles, ellipses,
and points, and each of these are controlled within a separate tab
within the Structures tab. Each of the 5 tabs (paths, polygons, circles,
ellipses, and points) works completely independent of each other. You
can only be drawing one type of structure at a time.

#### Drawing Circles, Ellipses, or Points

To draw a circle, ellipse, or point click on the "Edit" button. That
will activate a special editing mode where the asteroid will be frozen
and navigation is disabled. For circles, click on any 3 points on the
perimeter of the circle and when the 3rd point is clicked, a circle
will appear whose perimeter passes through these 3 points. For
ellipses, click 3 points on the asteroid in the following manner: The
first 2 points should lie on the endpoints of the major axis of the
desired ellipse. The third point should lie on one of the endpoints of
the minor axis of the desired ellipse. After clicking the third point,
an ellipse is drawn that passes through the points. For points, click
anywhere on the asteroid and the point will appear centered where you
clicked. Once a circle, ellipse, or point has been created, you can
drag it around with the left mouse button. To change the size of a
circle or point, drag it while holding the Control or Shift
button. For ellipses there are 3 ways to change its shape: To change
the size of the ellipse while keeping the ratio of the semi-minor axis
to semi-major axis constant, drag it while holding the Control or
Shift button, like for circles or points. To change the flattening of
the ellipse, i.e. the ratio of the semi-minor axis to semi-major axis,
drag it while holding the "z" or forward slash ("/") keys down. The
flattening cannot be greater than 1.0 or less than 0.001. Finally to
change the orientation of the ellipse, drag it while holding the "x"
or period (".") keys down. When you're finished editing, press the
Edit button again and the 3D view will return to the standard
rotate/pan/zoom mode.

#### Drawing Paths

Drawing paths works a little differently than circles, ellipses, or
points. Unlike circles, ellipses or points, you can only be editing a
single path at a time. To create a new path press New and to edit an
existing path, press Edit. You can add new vertices to paths and drag
around existing vertices.

#### Drawing Polygons

Drawing polygons is very similar to drawing paths and works by placing
and dragging control points. The surface area and perimeter length are
shown in the table on the left for each polygon. Both convex and concave
polygons are supported. However, polygons with crossovers are not drawn
correctly and should be avoided (though the tool will not prevent it).
Out of all the shapes, polygons are the most expensive to render. Also,
as more control points are added, the longer it takes to render.

#### Editing Structures

You can also right-click a structure within the 3D view and a popup will
show allowing you to delete it or change its color. For paths, the popup
includes the option to edit that path as well (For circles, ellipses,
and points simply press the Edit button in the control panel).

#### Saving and Loading Structures

You can save/load all the structures to/from disk using the Save and
Load buttons. Note that the file format for paths is different than the
format for circles, ellipses, or points.

The file format for paths is XML. The XML format consists of a series of
path elements, one element per path. Each element contains the following
attributes:

-   id - a unique integer identifying the path.
-   color - the color the path is to drawn in (specified as
    red,green,blue).
-   length - the total length of the path. This equals the sum of all
    the path's segments.
-   name - a user specified name given to the path.
-   vertices - the list of vertices making up the path. Each vertex is
    specified as a 3D point in spherical coordinates, i.e. latitude in
    degrees, longitude in degrees and the radius. There are thus 3N
    value in this attribute where N is the number of vertices and N-1 is
    the number of path segments. Thus if the path consists of 2
    segments, 3 vertices will be listed for a total of 9 numbers.

The file format for circles, ellipses, and points is a tab separated
table with the following columns:

1.  id (integer)
2.  name (string)
3.  x-coordinate (km)
4.  y-coordinate (km)
5.  z-coordinate (km)
6.  latitude (degrees)
7.  longitude (degrees)
8.  distance from center of body (km)
9.  slope (degrees)
10. elevation (meters)
11. gravitational acceleration (meters per second squared)
12. gravitational potential (joules per kilogram)
13. size (km). For circles and points this is twice the radius. For
    ellipses this is twice the semi-major axis.
14. flattening (ratio of semi-minor axis to semi-major axis)
15. angle (degrees)
16. color (red,green,blue)
17. ellipse angle relative to gravity vector (only saved for ellipses,
    not circles or points)

Note that when loading circles, ellipses, or points files, only columns
1 through 5 and 15 through 16 are actually read (for ellipses, columns
13 and 14 are read as well). All other columns are ignored. In addition
note that when saving circles or ellipses, the slope and elevation are
averaged over the rim of the circle or ellipse, whereas the acceleration
and potential are computed at the center of the circle or ellipse. For
points, the center of the point is used for all 4 values. Finally note
that if using a shape model for which slope, elevation, acceleration,
and potential are not specified such as the higher resolution shape
models for Eros or Itokawa, then 'NA' is printed for those values.

For ellipses, the final column in the file is the angle between the
semimajor axis of the ellipse and the gravity acceleration vector. This
angle is only computed if such gravity information is available for the
shape model. If no gravity information is available, then 'NA' is
printed in that column. The angle is compute using the following steps:

1.  First the plate closest to the center of the ellipse is rotated such
    that its normal vector points in the direction of positive z-axis
    and the plane of the plate is aligned with the xy-plane.
2.  Then the gravity vector is rotated by the same rotation that the
    normal vector was rotated in Step 1.
3.  Then the rotated gravity vector is projected into the xy-plane (the
    plane of the plate).
4.  There are 2 vectors that point in the direction of the semimajor
    axis of the ellipse, each in opposite directions. The angle required
    to rotate the projected gravity vector (counterclockwise as viewed
    from an observer looking down in the negative z direction onto the
    xy-plane) into each of these 2 vectors is computed.
5.  The smaller of these 2 angles is what is saved to the file. It is
    between 0 and 180 degrees.

#### Saving Profile of a Path

For paths that consists of exactly 2 control points, you can right-click
on it and select the "Save profile" option. A profile is a file which
contains elevation data vs distance along the path. The file is
formatted as a CSV file with the first column being distance along the
path and the second elevation. Here is how distance and elevation are
computed:\
 How distance is computed: Note that a path consists of a set of
vertices connecting the 2 control points specified by the user. The
control points are points How elevation is compute: If the current shape
model contains elevation data for each plate (which you would normally
use to color the shape model in the leftmost tab), then that is used. If
no elevation is available, then the user is prompted asking if the tool
should evaluate elevation as the distance between a point on the path
and the center of the asteroid. While not as accurate as true elevation
data, this may be acceptable for bodies that have a circular shape.

### Mapmaker tab (Eros only)

The Mapmaker tab provides an interface for running Bob Gaskell's
Mapmaker tool. You need to draw a region on the asteroid, choose a name
and output folder, and click "Run Mapmaker". If you close the tool and
then restart it, you can load in the cube file with the "Load Cube File"
button without having to rerun the mapmaker program.

The first time you run the mapmaker tool it will download and unzip all
the mapmaker files. This can take a while since it's about 700MB of
data. It won't happen again unless changes are made to those files on
the server. A progress indicator is provided to show what is happening.

When the mapmaker program completes, a new window will appear showing a
3D maplet as well as a graph for plotting profiles. You can draw and
edit profiles with the New Profile and Edit Profile buttons. It works
similar to, though not exactly like, the Paths drawing tool in the
Structures tab: When you press New Profile, a new Edit mode is entered
in which standard navigation is disabled so that when you click on the
maplet the endpoints of the profile are drawn. The profile is
immediately plotted in the graph as soon as you click the second
endpoint. The start of the profile is indicated with a green dot and the
end with a red dot. Default colors are chosen for the lines but you can
change them by right-clicking on a profile (provided you are not in
"Edit" mode). You can also color the maplet with any of the 3 channels
produced by the mapmaker program (height relative to gravity, height
relative to plane, and slope). Note that what's plotted is height
relative to gravity. You can also load and save profiles.

Note that you can the pan the profiles graph while holding the Control
button (Alt on the Mac) and the mousewheel allows you to zoom in and
out.

In addition an outline of the maplet is shown on the asteroid in
dark green. It disappears when you close the maplet window.

Menu Options
------------

This section explains the available menu options in the tool.

### File Menu

##### Export to Image...

Use this option to save the current rendering view to an image
file. You can save it using common image formats such as PNG, JPEG,
and TIFF formats.

##### Export Six Views along Axes to Images...

##### Export Shape Model...

##### Camera...

### View Menu

The View menu is divided into 3 section. The top section contains all
built-in shape models that the tool supports. When you click on a
shape model from the view menu, that shape model will be displayed in
the renderer and the tabs in the control panel on the left will change
depending on the shape model. Note that the shape model you were
viewing before changing to a different shape model will still be
maintained in memory and if you return to it later, it will be in
exactly the same state you left it.

The second section consists of a single item, "Import Shape
Models...".  This option is used to import custom shape models into
the tool. Two types of shape models can be imported: Shape models from
files in several supported formats as well as ellipsoids whose size
you can specify. Upon clicking on the "Import Shape Model..." option,
a dialog will appear allowing you to enter information about the
shape model to import.

The third section contains a list of the shape models you have
imported. New items will be added to the list when you import a new
shape model.

### Help Menu

##### Help Contents
Opens a web browser showing this page.

##### Recent Changes
Opens a web browser showing recent changes to the SBMT.
