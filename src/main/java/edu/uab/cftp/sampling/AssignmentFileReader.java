package edu.uab.cftp.sampling;

import static com.google.common.collect.Maps.newHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class AssignmentFileReader<K, V> {

	private static final String COMMENTINDICATOR = "#";

	private final String fileName;
	private final String delimiter;

	private Map<K, V> keyValuesMap;

	private final Class<K> k;
	private final Class<V> v;

	protected AssignmentFileReader(String fileName, String delimiter,
			Class<K> k, Class<V> v) {
		this.fileName = fileName;
		this.delimiter = delimiter;
		this.k = k;
		this.v = v;
	}

	public Map<K, V> getWeightMap() {
		keyValuesMap = newHashMap();
		readWeightsFromFile();
		return keyValuesMap;
	}

	private void readWeightsFromFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(COMMENTINDICATOR)) {
					continue;
				}
				String[] split = line.split(delimiter);
				try {
					addToMap(split[0].trim(), split[1].trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addToMap(String key, String value) throws Exception {
		keyValuesMap.put(convertToClass(k, key), convertToClass(v, value));
	}

	private <T> T convertToClass(Class<T> t, String toConvert)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return t.getConstructor(String.class).newInstance(toConvert);
	}
}
