package edu.jhuapl.saavtk.util.file;

import com.google.common.base.Preconditions;

class FileReader
{
	protected static final IndexableTuple EMPTY_INDEXABLE = new IndexableTuple() {

		@Override
		public int getNumberCells()
		{
			return 0;
		}

		@Override
		public String getName(@SuppressWarnings("unused") int cellIndex)
		{
			return null;
		}

		@Override
		public String getUnits(@SuppressWarnings("unused") int cellIndex)
		{
			return null;
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public Tuple get(@SuppressWarnings("unused") int index)
		{
			return new Tuple() {

				@Override
				public int size()
				{
					return 0;
				}

				@Override
				public double get(@SuppressWarnings("unused") int cellIndex)
				{
					throw new UnsupportedOperationException();
				}

			};
		}

	};

	protected static int checkColumnNumbers(Iterable<Integer> columnNumbers)
	{
		Preconditions.checkNotNull(columnNumbers);

		int numberColumns = 0;
		for (Integer column : columnNumbers)
		{
			Preconditions.checkArgument(column >= 0);
			++numberColumns;
		}
		return numberColumns;
	}

	protected FileReader()
	{
		throw new AssertionError();
	}
}
