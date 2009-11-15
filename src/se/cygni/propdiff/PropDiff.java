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
	public static void main(String[] names) throws IOException {
	
		if (names.length == 0) {
			System.out.println("Input: filenames of .properties files. Output: csv file that marks missing entries (view in OpenOffice/Excel).");
			System.exit(1);
		} 
	
		SortedSet<String> filenames = new TreeSet<String>(Arrays.asList(names));
		
		Map<String, Properties> map = readPropertiesFiles(filenames);
		Map<String, Set<String>> propertyExistIn = mapPropertiesToFilenames(map);
		removePropertiesExistingInAllFiles(propertyExistIn, filenames);
		printOut(propertyExistIn, filenames);
	}

	private static void printOut(Map<String, Set<String>> propertyExistIn, Set<String> filenames) {
		System.out.print("Property");
		for (String filename : filenames) {
			System.out.print(",");
			System.out.print(filename);
		}
		System.out.println();
		for (Entry<String, Set<String>> entry: propertyExistIn.entrySet()) {
			String property = entry.getKey();
			System.out.print(property);
			for (String filename : filenames) {
				System.out.print(",");
				if (entry.getValue().contains(filename)) {
					System.out.print("X");
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
