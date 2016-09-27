% Recent Changes
%
%

# Recent Changes

### April 9, 2014

-   Added shape models of Jupiter, Callisto, Europa, Ganymede, and Io
    along with LORRI images from New Horizons flyby in 2007. These
    shape models are simple ellipsoid models and have been added for
    the benefit of the New Horizons team in preparation of the Pluto
    flyby in 2015.
-   Also added preliminary shape models of Pluto and its 5 moons in
    preparation of the Pluto flyby in 2015.

### March 25, 2014

-   When saving lidar tracks to file (by right-clicking on a track and
    selecting one of the Save Track options), a new column is written
    out listing the range of each lidar point.
-   When running the Mapmaker tool, the output maplet file is now
    written out in FITS format. There is no label file saved out
    anymore but some information about the maplet is saved in the
    header portion of the FITS file. Also when loading in a maplet
    file (by clicking the Load button in the Mapmaker tab), the file
    must now be in FITS format.

### February 14, 2014

-   When saving out polygons to file, area is now written out as
    well. Note though that if the area was not already computed by
    right-clicking on a polygon and clicking "Display Interior" then
    zero is written out. Thus you must for click "Display Interior"
    for a polygon for the correct area to be saved to the file. Also,
    when loading polygons from a file, the area saved in the file is
    displayed in the table (even though "Display Interior" has not been
    clicked.
-   Deimos images are now indexed in the database so that search options
    work now.

### January 30, 2014

-   Fixed performance issue when drawing large polygons which still
    remained in previous release. Drawing polygons should now be as
    fast as drawing paths, even with polygons with hundreds of
    vertices. Displaying the interior of polygons with many vertices
    is still very slow though.
-   Fixed bug in which popup menu would not appear when right-clicking
    on paths.

### January 28, 2014

-   Improved performance somewhat when drawing large polygon
    structures. By default interior of polygon is now not
    displayed. To display the interior, right-click on a polygon and
    select "Display Interior". At this point, the area of the polygon
    will be calculated and shown in the table. As soon as you start
    editing again, the interior of the polygon will no longer be shown
    and will only reappear if you click "Display Interior"
    again. Performance still degrades with large polygons but is not
    as bad as previously. We hope to improve this further in the near
    future.


### December 26, 2013

-   Fixed problem which prevented importing custom plate data for custom
    shape models.
-   Changed how plate data are saved out so that all plate data is
    saved out to a single file including information about plates
    such as as area and coordinates of center of plate.
-   Added plate data (slope, elevation, acceleration, potential) to most
    shape models.
-   Added new Steins, Hartley, Lutetia, Bennu, Dione, Tethys, Mimas and
    Phoebe shape models which were recently delivered to the
    PDS. Dione and Tethys are now no longer restricted to only certain
    accounts.
-   Added support for loading multiple custom lidar tracks in lidar
    search panel at once (when pressing "Load Tracks From Files...").
-   Changed "Source:" to "Pointing:" in image search tabs.
-   In lidar browse panels, show start and stop times of each file.
-   Changed title of main window to display name of shape model currently being viewed.
-   Added option in Help menu for opening web page listing sources of all data.
-   [OSIRIS-REx team only] Added OLA tab to Bennu shape model and
    added some simulated lidar data.


### September 23, 2013

-   Added options in the Preferences panel for changing the look of the
    orientation axes such as the color, size, font size and color,
    thickness, and cone (tip) size and radius.
-   Added option in Camera dialog (available in File menu) for using
    orthographic projection in renderer (previously only perspective
    projection was supported).


### August 30, 2013

-   In the mapmaker view window, you can now right click on a profile
    and an option called "Save Profile" will appear. (Previously there
    were several options but none of them worked). This will invoke the
    gravity program which will compute the acceleration, potential,
    elevation and slope at all points along the profile using the shape
    model at the resolution currently set for, say, the Eros shape
    model. The file will be in CSV format.
    Note that the save profile option is similar to the save profile
    option in the main renderer view (when you right click on a path with
    exactly 2 points) except that the latter does yet not currently use
    the gravity program but instead gets the gravity values from the plate
    data closest to the points.
-   Fixed bug when importing new shape models which required you to
    restart the tool to view the new shape model. This is no longer
    required.
-   Added mapmaker tab back to Bennu shape model (had accidentally
    removed it)
-   Added slope, elevation, acceleration, potential data for Mathilde
    and fixed a problem that occurred when generating backplanes for the
    MSI image currently available.
-   Fixed a bug which prevented importing custom plate data


### July 8, 2013

-   Added Imaging tab to Thomas Deimos shape model with SPICE derived
    Viking and HRSC (framing camera only) images. Unfortunately, the
    pointing is not very accurate so the images do not line up with
    the asteroid. We are working on improving this. Simply press the
    Search button to see a list of images as the search options do not
    work yet.
-   Added generation of backplanes label files for all
    cameras. Previously backplane label files were only being
    generated for MSI and AMICA images.
-   Added slope, elevation, gravitation acceleration and potential
    plate data for Ida and Gaspra.


### July 3, 2013

-   When plotting elevation, acceleration and potential of a lidar track
    (which you can do by right-clicking on a track and selecting "Plot
    Track..." in the menu), the shape model used when computing the
    plots values is at the same resolution as is currently viewed in
    the renderer. Previously, the lowest resolution shape model was
    always used regardless of the current resolution. Because of this
    change, plotting will take much longer when using high resolution
    shape models. Also changed algorithm to use Werner method rather
    than Cheng method since the former is more accurate, though it is
    slower.
-   Fixed problem in MSI backplanes label file where MSI backplanes
    could not be loaded into ISIS.

### June 19, 2013

-   Fixed problem on Mac and Linux platforms where tool would not start
    if placed in folder with a space in its path.
-   When saving out profile of a path (i.e. when right-clicking on a
    path with only 2 control points and clicking "Save Profile..." in
    the popup menu), now cartesian coordinates,
    latitude/longitude/radius, and all plate data (as listed in the
    leftmost tab in the Plate Coloring section) along the profile are
    saved out to separate columns in a CSV file.
-   Changed scale bar in renderer to have black text on white background
    (partially transparent) with text centered justified.
-   When right-clicking on an image and clicking Properties in the popup
    menu, the surface area of the image footprint is shown in the list of
    properties.

### June 6, 2013

-   Added MEX HRSC images (framing camera only) for Phobos using SPICE
    pointing data. In the Imaging tab for Phobos, in the source drop
    down menu, select "PDS derived" and just click search and you'll
    see a list of 1655 images (these images are not indexed in the
    database so the other search options do not work). Unfortunately,
    these images are not registered very well with the asteroid,
    probably due to imprecisions in the SPICE kernels, and do not line
    up well with the surface features. We are working on improving
    this.

### May 30, 2013

-   Added 3 new shape models of Eros, available at
    [http://sbn.psi.edu/pds/resource/nearbrowse.html](http://sbn.psi.edu/pds/resource/nearbrowse.html). They
    are: 1. Peter Thomas' model based on MSI images (6 resolution
    levels), 2. NAV Plate Model, and 3. NLR Shape Data.
-   Added option in preferences for changing background color of renderer view.

### May 8, 2013

-   Reorganized list of shape models in View menu.

### April 30, 2013

-   Added slope, elevation, gravitation acceleration and potential
    plate data for Phoebe and Mimas.
-   Added Robert Gaskell's shape models of Dione, Rhea, Tethys,
    Hyperion, and Tempel 1. These are experimental and only available
    to several accounts.
-   Added Imaging Data for Phoebe and Mimas. These images have not yet
    been indexed in the database, so simply press the search button and
    all the available images will be listed. The images listed are
    those Robert Gaskell used to create the shape model.

### April 17, 2013

-   Phobos images have been indexed in the database so
    search options are now functional.
-   Added slope, elevation, gravitation acceleration and potential
    plate data for Phobos.
-   Added option in File menu for exporting currently shown shape
    model to Wavefront OBJ format.

### March 5, 2013

-   Added new tab to Phobos shape model for mapping Viking and Phobos
    2 images. The various search options are not functional
    yet. Simply click the Search button to show a list of available
    images.
-   Fixed problem where checkbox to show image map would appear for some
    shape models for which there was none.

### Feb 15, 2013

-   Added Robert Gaskell's Lutetia shape model. This shape model,
    however, has not yet been released to the public so most users
    will not be able to access it yet (only a few accounts have access
    to it).

### January 24, 2013

-   There are now options in the main leftmost tab of each view for
    customization of the plate data by the user. To customize plate
    data, click the Customize button. This will allow users to load
    their own plate data for both built-in and imported shape models
    (previously it was only possible to customize plate data for
    imported models. When importing new shape models, you will no
    longer see the options for loading custom plate data since this
    functionality is now available in the main tab of each view.)
    Also made all views support RGB false coloring.
-   Added buttons in Images tab for moving items up and down. Also
    made the Delete button delete all selected items.
-   Added ability to change the name of an image in Images panel and show
    name in image list.
-   Implemented multi-selection for structures. You can now select
    multiple structures in the structures list and apply certain
    operations to all the selected structures (such as Delete or
    change color). To select multiple structures, you need to click on
    the items with the mouse while holding down the Shift or Control
    (Command on Mac) key (with the shift key, a single contiguous
    block of items will be selected and with the Control or Command
    key, multiple contiguous blocks can be selected). Note also that
    multiple structures can be selected in the renderer itself as
    well by holding down the shift or control (command on Mac) key.
-   Added option for hiding subset of structures by right-clicking on
    structures and clicking Hide. Hidden structures are now grayed out.
-   Added option for centering a structure in the window (similar to
    the way images can be centered) by right-clicking on a
    structure and clicking Center in Window. After centering a
    structure the shape model will be oriented such that the top of
    the window points in the direction of the north pole.
-   Added ability to change selection color in preferences.

### October 30, 2012

-   Added support for multi-selection in image lists popup menu. This
    works with the list of images that's returned when doing a search
    (MSI, AMICA, etc) as well as the list of images in the Images
    tab. To select multiple images, you need to click on the items
    with the mouse while holding down the Shift or Control (Command on
    Mac) key (with the shift key, a single contiguous block of items
    will be selected and with the Control or Command key, multiple
    contiguous blocks can be selected).
-   Changed how drawing of ellipses is done so that now it is similar
    to circles in that you click 3 point and an ellipse is drawn to
    fit them. Also fixed earlier problem where interactive changing of
    the flattening did not always follow the current mouse position
    exactly. Also added warning info about new way to create ellipses

### October 9, 2012

-   Fixed problem where NIS custom formulas did not allow dividing by a
    band (e.g. B01/B02 was flagged as invalid)

### September 2012

-   Added the ability to save out the plate data. For the built-in shape
    models, there is a button in the leftmost tab called Save Plate
    Data. For the mapmaker view, the option is in the File menu. This saves out
    the currently shown plate data set to a text file, where each line has the
    value for the corresponding plate.
-   Updated the tool so that now Windows users can run the
    mapmaker program (tested only on Windows 7--may not work on
    previous versions of Windows).
-   Added the ability to save out plate data for the interiors of
    polygons, circles, and ellipses.  If you right-click on one of these
    structures, there is now an option called "Save plate data inside..."
    If you click it, it will save out a text file with the following
    columns:
        1. plate id the integer id of the plate of the shape model.
        2. latitude at center of plate.
        3. longitude at center of plate.
        4. slope at center of plate.
        5. elevation at center of plate.
        6. gravitational acceleration at center of plate.
        7. gravitational potential slope at center of plate.
    Note that if a plate from the shape model is only partially inside the
    polygon, ellipse or circle, then it still gets saved out to the
    file. Thus there may be some plates saved out that are not exactly
    inside the structure.
-   added ability to delete individual structures by pressing delete or
    backspace key.
-   added Hide All and Show All buttons to structure mapping panels to
    temporarily show and hide all structures, without deleting them. Still
    working on supporting hiding/showing of individual structures.
-   when loading structures from file and there are already structures
    drawn, user is now prompted if they want to append to or overwrite
    existing structures. This allows loading more than 1 file at a time
    without needing to combine them into a single file.
-   added ability to change line thickness and color of maplet boundary.
-   In lidar panels (for Eros and Itokawa), added options for saving
    both modified and unmodified tracks.
-   In lidar panels (for Eros and Itokawa), added options for saving
    all visible tracks at once.
-   In the NIS panel for Eros, you can now use RGB color to color NIS footprints, unlike
    previously where only grayscale was supported.
-   In the NIS panel for Eros, you can now save out an individual NIS spectrum to a text file (by right
    clicking on a spectrum in the list or on the footprint in the
    renderer).
-   In the NIS panel for Eros, 3 requested formulas were added to the very end of the list of
    available bands in drop down menus. You should see "B36 - B05",
    "B01 - B05",  and "B52 - B36" at the end of the list. You can now
    select these when coloring NIS spectra.
-   In the NIS panel for Eros, you can now create your own custom formulas. There is a new
    button named "Custom Formulas..." in the NIS panel. Click on it
    and a dialog will open showing your list of formulas. Click 'Add'
    and another dialog will appear allowing you to input your
    formula. You can use the variables B01 through B64 to refer to
    individual bands. Standard math and trigonometric functions should
    work. E.g. (B21-B01)*sin(B33)/sqrt(2.5). When you close the first
    dialog, the custom formulas are added to the dropdown list from
    which to choose from. The list of formulas is saved from session
    to session so you won't lose your work when you close the tool.

### August 2012

-   A new polygons tab has been created for drawing polygon structures.
    This works similar to the Paths shape and works by placing and
    dragging control points. The surface area and perimeter length are
    shown in the table on the left for each polygon. Both convex and
    concave polygons are supported. However, polygons with crossovers
    are not drawn correctly and should be avoided (though the tool will
    not prevent it). Out of all the shapes, polygons are the most
    expensive to render. Also, as more control points are added, the
    longer it takes to render.
-   In the structures tab, a new button is provided on the bottom for
    changing line thickness of structures. This is enabled for all shape
    types with the exception of Points.
-   In the lidar search tab, there is now a button called "Translate
    Tracks...". When pressed, a dialog window will open allowing you to
    explicitly specify the translation (3 cartesian coordinates in
    kilometers). All tracks will then get shifted by this amount.
-   In the lidar search tab, there is now a "Drag Tracks" toggle button.
    When pressed, a new "drag mode" will be activated which disables
    standard navigation around the asteroid and allows you to drag the
    tracks around. As you drag around, the tracks get shifted such that
    the selected point is moved to the location on the asteroid the
    mouse is currently positioned over. If there is no selected point,
    nothing happens. (Note the "selected point" is the point shown in
    blue after clicking and for which some information about it is shown
    in the status bar)
-   In the lidar search tabs, there is a new checkbox called "Show Track
    Error". When pressed, the error of the displaced tracks is shown to
    the right of the checkbox. This error is computed as the mean
    distance between each lidar point and its closest point on the
    asteroid (expressed in km). This error is computed using only the
    visible tracks, not tracks that are currently hidden. This error is
    updated every time you make a change to the tracks, e.g. change
    translation or offset or hide/show a track. There may be a
    performance penalty for calculating the error which is why this
    option is provided to disable it. (Note that if you change the
    resolution level of the asteroid, the error does not get updated.
    You will then need to uncheck and recheck it to show the correct
    error).
-   In the lidar search tabs, there is now an option for loading a track
    you previously saved to disk (using the "Save Track..." option).
    Click this button, choose the track file and the track will load and
    replace any existing tracks in the list. Note that it is currently
    only possible to have a single track loaded from file at a time. You
    cannot load more than one track at a time from files.
-   Fixed a bug which prevented running the tool using Java version 7 on
    some Linux systems.
-   When saving lidar tracks (by right clicking on a track and clicking
    Save track), you can now choose between saving the original track or
    the track with the translation and radial offset applied.
-   When plotting a track (by right clicking on a track and clicking
    Save track), 3 sets of plots are now displayed. Previously only
    potential vs distance and time was shown, but now, in addition,
    elevation and gravitation acceleration vs distance and time are
    shown. Each is show in a separate window. In addition it is possible
    to save out the data of these plots to a text file using the "Export
    Data..." option in the File menu of each plot window.

### July 2012

-   For paths that consists of exactly 2 control points, you can
    right-click on it and select an option for saving a profile. A
    profile is a file which contains elevation data vs distance along
    the path. The file is formatted as a CSV file with the first column
    being distance and the second elevation. If the current shape model
    contains elevation data for each plate (which you would normally use
    to color the shape model in the leftmost tab), then that is used. If
    no elevation is available, then the user is prompted asking if the
    tool should evaluate elevation as the distance between a point on
    the path and the center of the asteroid. While not as accurate as
    true elevation data, this may be acceptable for bodies that have a
    circular shape.
-   Added options when right-clicking on a coordinate grid for changing
    its color and line thickness.
-   In the Camera dialog (available from the File menu) it is now
    possible to set the distance of the virtual camera to the center of
    the asteroid. The direction of the camera is not affected when
    changing this value.
-   In the Preferences dialog it is now possible to set the "Motion
    Factor" of the mouse wheel. This factor controls the speed of
    zooming in and out with the mouse wheel. Larger values result in
    faster speeds and smaller values result in slower speeds. The
    default value is 1. Negative values reverse the direction of the
    zoom (i.e. zoom out becomes zoom in and zoom in becomes zoom out).
-   Help contents now load in a web browser rather than a separate
    window.
-   Added "Recent Changes" option to Help menu which opens this page in
    a web browser.

### June 2012

-   When right-clicking on an image, there is now an option to change
    its opacity
-   It is now possible to delete an individual vertex from a Path
    (previously, it was necessary to delete the entire path and start
    again). To delete a vertex, press the Delete or Backspace key while
    in Edit mode and the vertex that is blue will be deleted. There's
    only one vertex at a time that's blue, all others are red. The blue
    vertex represents the "current" vertex. To change the current
    vertex, simply click on a new vertex while hovering over it (so that
    the cursor changes to a hand shape).
-   Added option to export mapmaker maplet to a plate file.
-   In the File menu, there is now an option to automatically save 6
    images of views along both directions of all 3 axes. This option
    will automatically reorient the view to successively point the
    virtual camera in the positive x, negative x, positive y, negative
    y, positive z, and negative z directions and save an image for each
    view. When prompted to choose a file name in the dialog, the name
    you choose will be modified to indicate each of the six views. For
    example, if you choose the name "image.png", then the following six
    files will get saved out: "image+x.png", "image-x.png",
    "image+y.png", "image-y.png", "image+z.png", and "image-z.png".
-   There is now a new tab called "Images" for each View to the right of
    the Structures tab, for both built-in and custom models, so instead
    of specifying images when you create a new custom shape model (which
    previously was the only to import custom images), you would now do
    it in the Images tab, which shows a list of the images you've added.
    To add a new image, press "New" and a dialog will appear in which
    you can specify the image and projection parameters. To edit an
    existing image, click the "Edit" button. And to remove an image from
    the list press "Delete from List". Adding an image doesn't
    immediately make it visible on the renderer. To view it, right-click
    on the image in the list, and press Map Image. To remove it,
    right-click and press Map Image again (clicking Hide also removes
    it--the difference is that Map does all the computation needed to
    map the image to view unlike toggling the Hide option, so using the
    Hide option is faster). To remove all images from the renderer (not
    from the list), press the "Remove All from View".

    The menu that appears when you right-click is the same as the one
    for the MSI and AMICA images, though some options are not enabled.
    The "Properties" option allows you to view a 2D view of the image as
    well as change the contrast (however, contrast stretching is
    currently only supported for grayscale images).

    In addition to the cylindrical projection, I've also added the
    ability to use a perspective projection, which is the same
    projection used to map the original spacecraft images (i.e. MSI or
    AMICA). To do this you need to provide a sumfile in the format that
    Bob Gaskell uses which contains the position and orientation of the
    spacecraft. You also need to provide the angular field of view of
    the camera in degrees for both dimensions (e.g. for AMICA images
    both field of views would be 5.839083 degrees)

    The "Show Image Map" checkbox which you previously would check to
    show the images is no longer present in the custom shape models. It
    is present, however, in several of the built-in models and is used
    to simply show a single image of the entire asteroid.

    Existing lists of images (from custom imported shape models) should
    show up in the list of the Images tab. If this is not the case,
    please let me know.

    Some current limitations to be aware of which I hope to address:

    -   When doing a perspective projection, the image must be FITS
        format, unlike a cylindrical projection where the image can be
        png, jpg, tiff, etc. (the typical formats), though not FITS.
    -   As mentioned already, the adjusting contrast stretching is only
        available for single channel images, not color images. If the
        image is really grayscale but stored as a color image (i.e. the
        3 channels are the same), you will need to use some image
        processing software to convert it to a true grayscale format. I
        hope to add stretching for color images as well.

-   When rescaling data range of plate data (e.g. Elevation or slope),
    it is now possible to set the min and max values to be less than min
    value of the data or greater than the max value of the data,
    respectively.
-   In the File menu, there is now a new Camera option which opens a
    dialog in which you can specify vertical field of view of the
    virtual camera. The default value is 30 degrees. Note that the field
    of view changes if you right click on an MSI or AMICA image and
    press "Center in Window". When you do that, the field of view
    changes to the field of view of the MSI or AMICA camera.
-   The Statistics label (at the bottom of the leftmost tab for each
    view) is now selectable, so it is now possible to copy and paste the
    values to another program (with Ctrl-C).
-   added options to preferences for setting the pick tolerance
-   There is now and option in the Preferences dialog to switch between
    a "Trackball Interactor Style" and a "Joystick Interactor Style".
    Until now, only the "Trackball" style has been used by the tool. The
    Joystick style provides a different way to navigate around the
    asteroid. One of things you can do with Joystick style is to
    continuously spin the asteroid around, while holding down the left
    mouse button.
-   Added SSI tab for Ida and Gaspra, and MSI tab for Mathilde. Only a
    few images are currently available for these asteroids (Simply click
    Search to show a list of images--all the search options are
    currently ignored). Work on adding the remaining images is currently
    in progress.
-   Some of the AMICA images of Itokawa are not registered well,
    especially the ones acquired on Nov 12 and Nov 19. We have begun
    work on correcting some of these misregistered images and you can
    view the currently available corrected images by selecting
    "Corrected" from the Source dropdown menu at the top of the AMICA
    tab and then pressing Search (all the search options are ignored in
    this case). Work on adding additional corrected images is currently
    in progress.

### April 2012

-   added requested feature to show a scale bar on the view to indicate
    size of asteroid at current zoom
-   added option for exporting shape model to plt format
-   Fixed crash which sometimes occurred when changing orientation axes
    properties in Preferences dialog.
-   Added new lighting options in a new Preferences dialog.

### March 2012

-   Added support for loading in custom plate data when importing a
    custom shape model.
-   Added support for arbitrary positioning of custom images on custom
    non-ellipsoidal shape models. Previously images on custom
    non-ellipsoidal shape models were required to cover the entire
    model. Also removed all restrictions on the values for longitudes.

### February 2012

-   Added many new shape models to tool such as models by Thomas and
    Stooke as well as radar based shape models. All of these were
    obtained from the PDS.

### January 2012

-   Added support for explicitly changing the normal offset (shift) of
    an image or structure away from the shape model with a new dialog.
-   Improved lidar search capability for both Eros and Itokawa. Has more
    features which should hopefully make it easier to find, view, and
    export lidar tracks.
-   Lidar browse functionality now includes options for showing
    spacecraft.
-   Added unfiltered and optimized versions of Itokawa Lidar data for
    both searching and browsing.
-   Added extent of small body in statistics label.
-   Added option when searching for "PDS derived" images to exclude
    Gaskell images.

### October 2011

-   Added Itokawa LIDAR data (new tab).

### September 2011

-   Fixed problem where wrong FOV size was being used for Itokawa images
    causing incorrect shifts in images.
-   When saving ellipse structures, added column for orientation angle
    of ellipse relative to gravity vector (for shape models for which
    such data is available).
-   Improved documentation of structure file formats.
-   Changed cursor to crosshairs when drawing structures.
-   When drawing structures, increased sensitivity when hovering over
    existing structures.
-   When drawing a circle (by clicking 3 points on perimeter),
    interacting with other circles is disabled after the first point has
    been clicked until the completion of the circle.
-   Fixed inability to save structures when no backplane data is
    available.
-   Use superscripts for exponents in Statistics label.
-   Fixed problem where values on color bar would not get updated when
    changing resolution level.
-   Added backplanes for very high resolution Itokawa shape model
    (previously, backplanes were a subsampled version of the high
    resolution model).
-   Added ability to map multiple images to a custom shape model.
-   Added ability to edit and duplicate a custom shape model.

### August 2011

-   Added more Itokawa images to AMICA search panels. Pointing info of
    these images are derived from SPICE kernel files.
-   Added new Hide Image option to image context menu for temporarily
    hiding an image
-   Added ability to create circle simply by clicking on any 3 points on
    its perimeter.
-   Fixed problem with distortions appearing around edges of images
    mapped to custom shape models.
-   Removed "seam" that appeared on images mapped to shape model along
    zero-longitude line.
-   Added new option to set focal point of camera by pressing the c key.
    Focal point is set to point in 3D view the cursor is currently over.

### July 2011

-   Added ability to import custom shape models into tool and map an
    image.
-   Fixed problem where 8-bit Itokawa AMICA images were not being
    displayed correctly.
-   Fixed problem where export to image was not working after switching
    to a different asteroid.
-   Added slope/elevation/gravity data for medium and high resolution
    shape models of Itokawa.
-   Speed improvement in loading circles/ellipses/points on Itokawa.
-   New Delete All button in structures tab for deleting all structures
    at once.
-   Fixed problem with computation of incidence and emission backplanes
    for MSI and AMICA images.
-   Fixed problem where old circle structure format could not be loaded
    into new ellipse tab.
