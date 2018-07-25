package edu.jhuapl.saavtk.metadata.gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.MetadataManagerCollection;
import edu.jhuapl.saavtk.metadata.Serializer;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.metadata.gson.GsonElement.ElementIO;

public class GsonSerializer implements Serializer
{
	private static final Version SERIALIZER_VERSION = Version.of(1, 0);
	private static final ListIO LIST_IO = new ListIO();
	private static final SortedMapIO SORTED_MAP_IO = new SortedMapIO();
	private static final MapIO MAP_IO = new MapIO();
	private static final SetIO SET_IO = new SetIO();
	private static final SortedSetIO SORTED_SET_IO = new SortedSetIO();
	private static final MetadataIO METADATA_IO = new MetadataIO();
	private static final GsonVersionIO VERSION_IO = new GsonVersionIO();
	private static final ElementIO ELEMENT_IO = new ElementIO();
	private static final Gson GSON = configureGson();

	private final MetadataManagerCollection managerCollection;

	public static GsonSerializer of()
	{
		return new GsonSerializer();
	}

	protected GsonSerializer()
	{
		this.managerCollection = MetadataManagerCollection.of();
	}

	@Override
	public Version getVersion()
	{
		return SERIALIZER_VERSION;
	}

	@Override
	public void register(Key<? extends Metadata> key, MetadataManager manager)
	{
		managerCollection.add(key, manager);
	}

	@Override
	public void deregister(Key<? extends Metadata> key)
	{
		managerCollection.remove(key);
	}

	@Override
	public void load(File file) throws IOException
	{
		Preconditions.checkNotNull(file);

		SettableMetadata source = SettableMetadata.of(Version.of(0, 0));
		try (JsonReader reader = GSON.newJsonReader(new FileReader(file)))
		{
			Version fileVersion = null;
			try
			{
				fileVersion = GSON.fromJson(reader, DataTypeInfo.VERSION.getType());
			}
			catch (Exception e)
			{
				throw new IOException("Metadata reader version " + SERIALIZER_VERSION + " cannot read this metadata format", e);
			}
			if (!SERIALIZER_VERSION.equals(fileVersion))
			{
				throw new IOException("Metadata reader version " + SERIALIZER_VERSION + " cannot read metadata format version " + fileVersion);
			}
			Map<String, Metadata> metadataMap = GSON.fromJson(reader, DataTypeInfo.MAP.getType());
			for (Entry<String, Metadata> entry : metadataMap.entrySet())
			{
				source.put(Key.of(entry.getKey()), entry.getValue());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (SwingUtilities.isEventDispatchThread())
		{
			//			retrieveInSwingContext(source);
			retrieveInSingleThreadContext(source);
		}
		else
		{
			retrieveInSingleThreadContext(source);
		}
	}

	@Override
	public void save(File file) throws IOException
	{
		Preconditions.checkNotNull(file);
		try (FileWriter fileWriter = new FileWriter(file))
		{
			try (JsonWriter jsonWriter = GSON.newJsonWriter(fileWriter))
			{
				Map<String, Metadata> metadataMap = new HashMap<>();
				for (Key<? extends Metadata> key : managerCollection.getKeys())
				{
					MetadataManager manager = managerCollection.getManager(key);
					Metadata metadata = manager.store();
					metadataMap.put(key.getId(), metadata);
				}
				GSON.toJson(SERIALIZER_VERSION, DataTypeInfo.VERSION.getType(), jsonWriter);
				jsonWriter.flush();
				fileWriter.write('\n');
				GSON.toJson(metadataMap, DataTypeInfo.MAP.getType(), jsonWriter);
				jsonWriter.flush();
				fileWriter.write('\n');
			}
		}
	}

	private void retrieveInSwingContext(Metadata source)
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			for (Key<? extends Metadata> key : managerCollection.getKeys())
			{
				try
				{
					Metadata element = source.get(key);
					if (element != null)
					{
						SwingUtilities.invokeAndWait(() -> {
							managerCollection.getManager(key).retrieve(element);
						});
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		executor.shutdown();
	}

	private void retrieveInSingleThreadContext(Metadata source)
	{
		try
		{
			for (Key<? extends Metadata> key : managerCollection.getKeys())
			{
				Metadata element = source.get(key);
				if (element != null)
				{
					managerCollection.getManager(key).retrieve(element);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Gson configureGson()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.setPrettyPrinting();
		builder.serializeSpecialFloatingPointValues();
		builder.registerTypeAdapter(DataTypeInfo.SORTED_SET.getType(), SORTED_SET_IO);
		builder.registerTypeAdapter(DataTypeInfo.SET.getType(), SET_IO);
		builder.registerTypeAdapter(DataTypeInfo.LIST.getType(), LIST_IO);
		builder.registerTypeAdapter(DataTypeInfo.SORTED_MAP.getType(), SORTED_MAP_IO);
		builder.registerTypeAdapter(DataTypeInfo.MAP.getType(), MAP_IO);
		builder.registerTypeAdapter(DataTypeInfo.METADATA.getType(), METADATA_IO);
		builder.registerTypeAdapter(DataTypeInfo.VERSION.getType(), VERSION_IO);
		builder.registerTypeAdapter(DataTypeInfo.ELEMENT.getType(), ELEMENT_IO);
		return builder.create();
	}

	private static class TestManager implements MetadataManager
	{
		private final SettableMetadata metadata;

		TestManager(SettableMetadata metadata)
		{
			this.metadata = metadata;
		}

		@Override
		public Metadata store()
		{
			SettableMetadata destination = SettableMetadata.of(metadata.getVersion());
			for (Key<?> key : metadata.getKeys())
			{
				@SuppressWarnings("unchecked")
				Key<Object> newKey = (Key<Object>) key;
				destination.put(newKey, metadata.get(key));
			}

			return destination;
		}

		@Override
		public void retrieve(Metadata source)
		{
			metadata.clear();
			for (Key<?> key : source.getKeys())
			{
				Object value = source.get(key);
				@SuppressWarnings("unchecked")
				Key<Object> newKey = (Key<Object>) key;
				metadata.put(newKey, value);
			}
		}

	}

	public static void main(String[] args) throws IOException
	{
		GsonSerializer serializer = new GsonSerializer();
		final Version testMetadataVersion = Version.of(1, 0);

		final String v3 = "Bennu / V3";
		final Key<SettableMetadata> testV3StateKey = Key.of(v3);
		SettableMetadata v3State = SettableMetadata.of(Version.of(3, 1));

		v3State.put(Key.of("tab"), "1");
		v3State.put(Key.of("facets"), 2000000001L);
		v3State.put(Key.of("showBaseMap"), true);
		v3State.put(Key.of("resolution"), -5.e64);
		v3State.put(Key.of("int"), 20);
		v3State.put(Key.of("long"), (long) 20);
		v3State.put(Key.of("short"), (short) 20);
		v3State.put(Key.of("byte"), (byte) 20);
		v3State.put(Key.of("double"), (double) 20);
		v3State.put(Key.of("float"), (float) 20);
		v3State.put(Key.of("char"), (char) 20);
		v3State.put(Key.of("boolean"), false);
		v3State.put(Key.of("string"), "a string");
		v3State.put(Key.of("stringNull"), null);
		v3State.put(Key.of("longNull"), null);

		List<String> stringList = new ArrayList<>();
		stringList.add("String0");
		stringList.add(null);
		stringList.add("String2");
		Key<List<String>> stringListKey = Key.of("stringList");
		v3State.put(stringListKey, stringList);

		List<Integer> intList = new ArrayList<>();
		intList.add(0);
		intList.add(null);
		intList.add(2);
		Key<List<Integer>> intListKey = Key.of("intList");
		v3State.put(intListKey, intList);

		List<List<String>> listListString = new ArrayList<>();
		listListString.add(null);
		listListString.add(ImmutableList.of("X", "y", "z"));
		listListString.add(stringList);

		final Key<SettableMetadata> testStateKey = Key.of("testState");
		final SettableMetadata state = SettableMetadata.of(testMetadataVersion);
		MetadataManager manager = new TestManager(state);
		MetadataManager manager2 = new TestManager(v3State.copy());

		Key<List<List<String>>> listListStringKey = Key.of("listListString");
		state.put(Key.of("Current View"), v3);
		state.put(Key.of("Tab Number"), new Integer(3));
		state.put(Key.of("Current View2"), v3);
		state.put(listListStringKey, listListString);
		state.put(Key.of("stringSet"), ImmutableSortedSet.of("liver", "spleen", "aardvark"));
		state.put(Key.of("Metadata"), "a string that happens to have the key \"Metadata\"");

		Map<Byte, Short> byteShortMap = new HashMap<>();
		byteShortMap.put((byte) 1, null);
		byteShortMap.put(null, (short) 12);
		byteShortMap.put((byte) 11, (short) 23);
		byteShortMap.put((byte) 10, (short) 17);
		Key<Map<Byte, Short>> byteShortMapKey = Key.of("byteShortMap");
		state.put(byteShortMapKey, byteShortMap);

		SortedMap<Byte, Short> byteSortedMap = new TreeMap<>();
		byteSortedMap.put((byte) 1, null);
		byteSortedMap.put((byte) 11, (short) 23);
		byteSortedMap.put((byte) 10, (short) 17);
		Key<SortedMap<Byte, Short>> byteSortedMapKey = Key.of("byteSortedMap");
		state.put(byteSortedMapKey, byteSortedMap);

		state.put(testV3StateKey, v3State);

		File file = new File("/Users/peachjm1/Downloads/MyState.sbmt");
		serializer.register(testStateKey, manager);
		serializer.register(testV3StateKey, manager2);
		serializer.save(file);
		System.out.println("Original state is: " + state);

		SettableMetadata state2 = SettableMetadata.of(testMetadataVersion);
		SettableMetadata v3State2 = SettableMetadata.of(testMetadataVersion);

		serializer = GsonSerializer.of();
		manager = new TestManager(state2);
		manager2 = new TestManager(v3State2);
		// Register in reverse order -- should not matter.
		serializer.register(testV3StateKey, manager2);
		serializer.register(testStateKey, manager);
		serializer.load(file);

		System.out.println("Reloaded state is: " + state2);
		System.out.println("States were" + (state.equals(state2) ? " " : " not ") + "found equal");

		System.out.println("Reloaded v3State is: " + v3State2);
		System.out.println("States were" + (v3State.equals(v3State2) ? " " : " not ") + "found equal");

		// Test some further unpacking.
		v3State2 = state2.get(testV3StateKey);
		Long longNull = v3State2.get(Key.of("longNull"));
		System.out.println("longNull is " + longNull);

		System.out.println("stringSet is " + state2.get(Key.of("stringSet")));

		// This fails at runtime. If this were a real key templated on Long it would fail at compile time.
		//		Float fVal = v3State2.get(Key.of("long"));

		// This doesn't fail at runtime or compile time but I wish it would:
		//		List<List<Integer>> unpackedListList = state2.get(Key.of("listListString"));

		// But the following does fail to compile, which is probably good enough.
		//		unpackedListList = state2.get(listListStringKey);
		// And this fails at runtime, as it should.
		//		System.out.println(unpackedListList.get(1).get(1) * 7);

		// It would be OK if this were to work but it doesn't. 
		//		float fVal = v3State2.get(Key.of("resolution"));

		// This one is supposed to throw an exception. 
		//		Short floatAsDouble = v3State2.get(Key.of("facets"));
		//		System.out.println("float as double is " + floatAsDouble);
	}

}
