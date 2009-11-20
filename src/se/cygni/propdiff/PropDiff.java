package se.cygni.propdiff;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class PropDiff {
	private static final String FIELD_SEPARATOR = "\t";

	public static void main(String[] names) throws IOException {
		boolean showValues = false;
		if (names.length > 0 && names[0].equals("-v")) {
			showValues = true;
			names = shiftLeft(names);
		}
	
		if (names.length == 0) {
			System.out.println("Input: filenames of .properties files. \nOutput: csv file (on stdout) that marks missing entries (view in OpenOffice/Excel). \nYou can prepend the filenames with '-v' to get the values in the output.");
			System.exit(1);
		} 
	
		SortedSet<String> filenames = new TreeSet<String>(Arrays.asList(names));
		
		Map<String, Properties> map = readPropertiesFiles(filenames);
		Map<String, Set<String>> propertyExistIn = mapPropertiesToFilenames(map);
		removePropertiesExistingInAllFiles(propertyExistIn, filenames);
		printOut(propertyExistIn, filenames, showValues ? map : null);
	}

	private static String[] shiftLeft(String[] names) {
		String[] unshifted = names;
		names = new String[unshifted.length - 1];
		for (int i = 1; i < unshifted.length; ++i) {
			names[i-1] = unshifted[i];
		}
		return names;
	}

	private static void printOut(Map<String, Set<String>> propertyExistIn, Set<String> filenames, Map<String, Properties> propertiesMap) {
		System.out.print("Property");
		for (String filename : filenames) {
			System.out.print(FIELD_SEPARATOR);
			System.out.print(filename);
		}
		System.out.println();
		for (Entry<String, Set<String>> entry: propertyExistIn.entrySet()) {
			String property = entry.getKey();
			System.out.print(property);
			for (String filename : filenames) {
				System.out.print(FIELD_SEPARATOR);
				if (entry.getValue().contains(filename)) {
					if (propertiesMap != null) {
						System.out.print(propertiesMap.get(filename).getProperty(entry.getKey()));
					} else {
						System.out.print("X");
					}
				} else {
					System.out.print("");
				}
			}
			System.out.println();
		}
	}

	private static void removePropertiesExistingInAllFiles(Map<String, Set<String>> propertyExistIn, Set<String> filenames) {
		int count = filenames.size();
		Set<String> removeThese = new HashSet<String>();
		for (Entry<String, Set<String>> entry: propertyExistIn.entrySet()) {
			if (entry.getValue().size() == count) {
				removeThese.add(entry.getKey());
			}
		}
		for (String key : removeThese) {
			propertyExistIn.remove(key);
		}
	}

	private static Map<String, Set<String>> mapPropertiesToFilenames(Map<String, Properties> map) {
		SortedMap<String, Set<String>> propertyExistIn = new TreeMap<String, Set<String>>();
		for (Map.Entry<String, Properties> entry : map.entrySet()) {
			for (Enumeration<?> en = entry.getValue().propertyNames(); en.hasMoreElements(); ) {
				String property = (String) en.nextElement();
				Set<String> propSet = propertyExistIn.get(property);
				if (propSet == null) {
					propSet = new HashSet<String>();
					propertyExistIn.put(property, propSet);
				}
				propSet.add(entry.getKey());
			}
		}
		return propertyExistIn;
	}

	private static Map<String, Properties> readPropertiesFiles(Set<String> filenames) 
			throws FileNotFoundException, IOException {
		Map<String, Properties> map = new HashMap<String, Properties>();
		for (String filename : filenames) {
			Properties properties = new Properties();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(filename);
				properties.load(fis);
				map.put(filename, properties);
			} finally {
				if (fis != null) fis.close();
			}
		}
		return map;
	}
}
