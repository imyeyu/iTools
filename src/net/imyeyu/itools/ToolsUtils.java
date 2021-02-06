package net.imyeyu.itools;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.sun.management.OperatingSystemMXBean;

public class ToolsUtils {

	public static String cutString(String data, int length, boolean dot) {
		if (data.length() < length / 2) {
			return data;
		}
		int count = 0;
		StringBuffer sb = new StringBuffer();
		String[] array = data.split("");
		for (int i = 0; i < array.length; i++) {
			count += array[i].getBytes().length > 1 ? 2 : 1;
			sb.append(array[i]);
			if (count >= length) {
				break;
			}
		}
		if (dot) {
			return (sb.toString().length() < data.length()) ? sb.append("...").toString() : data;
		}
		return sb.toString();
	}

	public static Map<Object, Object> randomMap(Map<Object, Object> map, int limit) {
		Map<Object, Object> resultTemporary = new LinkedHashMap<Object, Object>();
		List<Map.Entry<Object, Object>> list = new ArrayList<Map.Entry<Object, Object>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Object, Object>>() {
			public int compare(Entry<Object, Object> lhs, Entry<Object, Object> rhs) {
				int randomOne = (int) (Math.random() * 10);
				int randomTwo = (int) (Math.random() * 10);
				return randomOne - randomTwo;
			}
		});
		for (int i = 0, l = list.size(); i < l; i++) {
			Map.Entry<Object, Object> mapEntry = list.get(i);
			if (resultTemporary.size() < limit) {
				resultTemporary.put(mapEntry.getKey(), mapEntry.getValue());
			}
		}
		return resultTemporary;
	}

	public static Map<String, File> randomFileMap(Map<String, File> map) {
		Map<String, File> resultTemporary = new LinkedHashMap<String, File>();
		List<Map.Entry<String, File>> list = new ArrayList<Map.Entry<String, File>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, File>>() {
			public int compare(Entry<String, File> lhs, Entry<String, File> rhs) {
				int randomOne = (int) (Math.random() * 10);
				int randomTwo = (int) (Math.random() * 10);
				return randomOne - randomTwo;
			}
		});
		for (int i = 0, l = list.size(); i < l; i++) {
			Map.Entry<String, File> mapEntry = list.get(i);
			resultTemporary.put(mapEntry.getKey(), mapEntry.getValue());
		}
		return resultTemporary;
	}

	public static Map<String, Object> sortMapByStringKey(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		class MapKeyComparator implements Comparator<String> {
			public int compare(String str0, String str1) {
				return str0.compareTo(str1);
			}
		}
		Map<String, Object> sortMap = new TreeMap<String, Object>(new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}

	public static Map<Long, String> sortMapByLongKey(Map<Long, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		class MapKeyComparator implements Comparator<Long> {
			public int compare(Long arg0, Long arg1) {
				return arg0.compareTo(arg1);
			}
		}
		Map<Long, String> sortMap = new TreeMap<Long, String>(new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}

	public static Map<String, File> removeFileMapByKey(Map<String, File> map, String key) {
		Iterator<String> iterator = map.keySet().iterator();
		String k;
		while (iterator.hasNext()) {
			k = iterator.next();
			if (key.equals(k)) {
				iterator.remove();
			}
		}
		return map;
	}

	public static int getSystemMemorySize() {
		OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		long size = osmb.getTotalPhysicalMemorySize();
		size = size / 1024 / 1024;
		return (int) size;
	}

	public static boolean findProcess(String processName, String threadName) {
		BufferedReader bufferedReader = null;
		try {
			Process proc = Runtime.getRuntime().exec("tasklist -v -fi " + '"' + "imagename eq " + processName + '"');
			bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.startsWith(processName) && line.indexOf(threadName) != -1) {
					return true;
				}
			}
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void setIntoClipboard(String content) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
	}

	public static String getIntoClipboard() throws Exception {
		return Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
	}

	public static String storageFormat(double byteValue, DecimalFormat format) {
		if (byteValue == -1)
			return "";
		double value = byteValue;
		if (value < 1024)
			return format.format(value) + " B ";
		value = value / 1024;
		if (value < 10240)
			return format.format(value) + " KB";
		value = value / 1024;
		if (value < 10240)
			return format.format(value) + " MB";
		value = value / 1024;
		if (value < 1024)
			return format.format(value) + " GB";
		return format.format(value / 1024) + " TB";
	}

	public static String netSpeedFormat(double byteValue, DecimalFormat format) {
		if (byteValue == -1)
			return "0 B";
		double value = byteValue;
		if (value < 1024)
			return format.format(value) + " B ";
		value = value / 1024;
		if (value < 1124)
			return format.format(value) + " KB";
		return format.format(value / 1024) + " MB";
	}

	public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
		Method method = null;
		for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				method = clazz.getDeclaredMethod(methodName, parameterTypes);
				return method;
			} catch (Exception e) {
				// 向上转型
			}
		}
		return null;
	}

	private static float levenshteinDistance(String source, String target, boolean isIgnore) {
		int[][] d;
		int n = source.length(), m = target.length(), i, j, temp;
		char charS, charT;
		if (n == 0)
			return m;
		if (m == 0)
			return n;
		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}
		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}
		for (i = 1; i <= n; i++) {
			charS = source.charAt(i - 1);
			for (j = 1; j <= m; j++) {
				charT = target.charAt(j - 1);
				if (isIgnore) {
					if (charS == charT || charS == charT + 32 || charS + 32 == charT) {
						temp = 0;
					} else {
						temp = 1;
					}
				} else {
					if (charS == charT) {
						temp = 0;
					} else {
						temp = 1;
					}
				}
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
			}
		}
		return d[n][m];
	}

	private static int min(int one, int two, int three) {
		return (one = one < two ? one : two) < three ? one : three;
	}

	public static float getSimilarityRatio(String source, String target) {
		return getSimilarityRatio(source, target, true);
	}

	public static float getSimilarityRatio(String source, String target, boolean isIgnore) {
		float ret = 0;
		if (Math.max(source.length(), target.length()) == 0) {
			ret = 1;
		} else {
			ret = 1 - (float) levenshteinDistance(source, target, isIgnore)
					/ Math.max(source.length(), target.length());
		}
		return ret;
	}

	public static <T, K> List<T> mapKeys(Map<T, K> map) {
		if ((map != null) && (!map.isEmpty())) {
			List<T> r = new ArrayList<>();
			for (T t : map.keySet()) {
				r.add(t);
			}
			return r;
		}
		return null;
	}

	public static float[][] bezier(float[][] poss, int precision) {
		int dimersion = poss[0].length;
		int number = poss.length;
		if (number < 2 || dimersion < 2) return null;
		float[][] result = new float[precision][dimersion];
		int[] mi = new int[number];
		mi[0] = mi[1] = 1;
		for (int i = 3; i <= number; i++) {
			int[] t = new int[i - 1];
			for (int j = 0; j < t.length; j++) {
				t[j] = mi[j];
			}
			mi[0] = mi[i - 1] = 1;
			for (int j = 0; j < i - 2; j++) {
				mi[j + 1] = t[j] + t[j + 1];
			}
		}
		for (int i = 0; i < precision; i++) {
			float t = (float) i / precision;
			for (int j = 0; j < dimersion; j++) {
				float temp = 0.0f;
				for (int k = 0; k < number; k++) {
					temp += Math.pow(1 - t, number - k - 1) * poss[k][j] * Math.pow(t, k) * mi[k];
				}
				result[i][j] = temp;
			}
		}
		return result;
	}
}