package edu.jhuapl.saavtk.state;

public class IntegerAttribute extends Scalar<Integer> implements Attribute
{
	private static final ValueType VALUE_TYPE = () -> {
		return "Integer";
	};

	public static ValueType getValueType()
	{
		return VALUE_TYPE;
	}

	public IntegerAttribute(Integer value)
	{
		super(value);
	}

	@Override
	public String toString()
	{
		return "(" + VALUE_TYPE.getId() + ") " + get();
	}
}
