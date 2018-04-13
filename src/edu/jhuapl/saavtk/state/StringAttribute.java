package edu.jhuapl.saavtk.state;

public class StringAttribute extends Scalar<String> implements Attribute
{
	private static final ValueType VALUE_TYPE = () -> {
		return "String";
	};

	public static ValueType getValueType()
	{
		return VALUE_TYPE;
	}

	public StringAttribute(String value)
	{
		super(value);
	}

	@Override
	public String toString()
	{
		return "(" + VALUE_TYPE.getId() + ") " + get();
	}
}
