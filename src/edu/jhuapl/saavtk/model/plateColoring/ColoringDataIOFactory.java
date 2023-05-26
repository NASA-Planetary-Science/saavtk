package edu.jhuapl.saavtk.model.plateColoring;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Class that serves as a factory for {@link ColoringDataIO} implementations
 * capable of loading/saving arbitrary combinations of file formats. The design
 * is a hybrid singleton/factory, with a default singleton instance that returns
 * an I/O object capable of loading and saving only VTK format files.
 * <p>
 * This may also be subclassed and other instances created as needed to support
 * new file formats, and/or to handle multiple formats in the same factory.
 * 
 * @author James Peachey
 *
 */
public abstract class ColoringDataIOFactory
{
    public static final String VtkFormat = "VTK";

    private static final ImmutableList<String> DefaultFormats = ImmutableList.of(VtkFormat);

    private static final VtkColoringDataIO DefaultDataIO = new VtkColoringDataIO();

    private static final ColoringDataIOFactory DefaultInstance = new ColoringDataIOFactory() {

        @Override
        public ImmutableList<String> getSupportedFormats()
        {
            return DefaultFormats;
        }

        @Override
        public ColoringDataIO of(String format)
        {
            Preconditions.checkNotNull(format);
            Preconditions.checkArgument(format.equalsIgnoreCase(DefaultFormats.get(0)));

            return DefaultDataIO;
        }

    };

    /**
     * Return a factory that can load/save plate coloring data from/to VTK format
     * files.
     * 
     * @return
     */
    public static ColoringDataIOFactory of()
    {
        return DefaultInstance;
    }

    protected ColoringDataIOFactory()
    {
        super();
    }

    /**
     * Return the file formats this factory supports.
     * 
     * @return the formats
     */
    public abstract ImmutableList<String> getSupportedFormats();

    /**
     * Provide a {@link ColoringDataIO} instance capable of loading/saving data
     * from/to the specified file format type.
     * 
     * @param format the format desired for loading/saving
     * @return the {@link ColoringDataIO} that can load/save the specified format
     * @throws IllegalArgumentException if this factory cannot provide a
     *             {@link ColoringDataIO} that can load/save the specified format.
     */
    public abstract ColoringDataIO of(String format);

}
