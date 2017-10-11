package edu.jhuapl.saavtk.config;

public class BodyConfiguration extends ExtensibleTypedLookup implements Configurable
{

    private static final Key<FixedTypedLookup.Builder> BUILDER_KEY = Key.of("BodyConfiguration");

    public static Builder<BodyConfiguration> builder()
    {
    	final FixedTypedLookup.Builder fixedBuilder = FixedTypedLookup.builder(BUILDER_KEY);
    	
    	return new Builder<BodyConfiguration>(fixedBuilder)
    	{
			@Override
			public BodyConfiguration build()
			{
				return new BodyConfiguration(getFixedBuilder());
			}
    	};
    }

    protected BodyConfiguration(FixedTypedLookup.Builder builder)
    {
		super(BUILDER_KEY, builder);
	}

}
