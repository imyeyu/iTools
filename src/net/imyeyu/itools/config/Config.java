package net.imyeyu.itools.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import net.imyeyu.itools.EncodeUtils;
import net.imyeyu.itools.ToolsUtils;

/**
 * <p>配置对象，以 HashMap 储存配置，使用 get 数据类型简化 Map 的数据获取方式
 * <p>请确保配置对象数据类型，否则有转化异常直接向 JVM 抛出
 * 
 * @author 夜雨
 * @createdAt 2021-01-25 23:29:30
 *
 */
public class Config {
	
	private Map<String, Object> map;
	// 缓存 Config，执行 bindUpdate() 才会更新到实体的 Config 中
	private Map<String, Object> configCache;
	
	public Config(Map<String, Object> config) {
		this.map = config;
		this.configCache = new HashMap<>();
	}
	
	// 配置绑定相关开始-----------------------------------------------------------------------------
	public void bindTextProperty(Control control, String key, ConfigConverter<String, String> converter) {
		try {
			// 反射组件父类方法
			Method method = ToolsUtils.getDeclaredMethod(control, "textProperty");
			method.setAccessible(true);
			// 获得可观察对象
			StringProperty property = (StringProperty) method.invoke(control);
			String v = getString(key);
			// 设置值
			property.set(converter == null ? v : converter.beforeSet(v));
			// 监听值
			property.addListener((obs, o, text) -> {
				// 配置值和显示值的转换器
				if (converter == null) {
					configCache.put(key, text);	
				} else {
					String c = configCache.get(key) == null ? v : configCache.get(key).toString();
					configCache.put(key, converter.beforeUpdate(text, c));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void bindTextProperty(Control control, String key) {
		bindTextProperty(control, key, null);
	}
	// --
	public void bindSelectedProperty(Control control, String key) {
		try {
			Method method = ToolsUtils.getDeclaredMethod(control, "selectedProperty");
			method.setAccessible(true);
			BooleanProperty property = (BooleanProperty) method.invoke(control);
			property.set(is(key));
			property.addListener((obs, o, v) -> configCache.put(key, v));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// --
	@SuppressWarnings("unchecked")
	public <T, K>void bindValueProperty(Control control, ConfigT<T> key, ConfigConverter<T, K> converter) {
		try {
			String stringKey = key.get();
			Method method = ToolsUtils.getDeclaredMethod(control, "valueProperty");
			method.setAccessible(true);
			ObjectProperty<T> property = (ObjectProperty<T>) method.invoke(control);
			T t = getT(key);
			configCache.put(stringKey, t);
			property.set(converter == null ? t : converter.beforeSet((K) t));
			property.addListener((obs, o, v) -> {
				if (converter == null) {
					configCache.put(stringKey, v);	
				} else {
					configCache.put(stringKey, converter.beforeUpdate(v, (K) configCache.get(stringKey)));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public <T>void bindValueProperty(Control control, ConfigT<T> key) {
		bindValueProperty(control, key, null);
	}
	/**
	 * 更新缓存 Config 到实体 Config
	 * 
	 * @return 返回需要进行更新的 Config 键
	 */
	public List<String> bindUpdate() {
		List<String> keys = new ArrayList<String>();
		for (Map.Entry<String, Object> item : configCache.entrySet()) {
			// 配置在缓存，并且缓存值和实体值不同，表示已修改配置
			if (isUpdateConfig(item.getKey())) {
				keys.add(item.getKey());
			}
			map.put(item.getKey(), item.getValue());
		}
		return keys;
	}
	// 比较储存的配置和缓存配置是否匹配（String）
	public boolean isUpdateConfig(String key) {
		Object cache = configCache.get(key);
		return cache != null && !getString(key).equals(cache.toString());
	}
	// 配置绑定相关结束-----------------------------------------------------------------------------
	
	/**
	 * <br>检测是否存在该配置
	 * <br>这里只能从 map 调用，因为本类 get 方法也需要此方法
	 * 
	 * @param key 配置键
	 * @return boolean true 为存在，反之不存在
	 */
	public boolean has(String key) {
		return map.get(key) != null;
	}
	
	/**
	 * 获取整型数组
	 * 
	 * @param key 配置键
	 * @return int
	 */
	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}
	
	/**
	 * 以指定进制获取整型数据
	 * 
	 * @param key   配置键
	 * @param radix 进制
	 * @return 短整型
	 */
	public int getInt(String key, int radix) {
		return Integer.parseInt(getString(key), radix);
	}
	
	/**
	 * 获取长整型数据
	 * 
	 * @param key 配置键
	 * @return 长整型
	 */
	public long getLong(String key) {
		return Long.parseLong(getString(key));
	}
	
	/**
	 * 不转换直接获取对象
	 * 
	 * @param key 配置键
	 * @return 对象
	 */
	public Object getObject(String key) {
		return get(key);
	}
	
	/**
	 * 获取字符串数据
	 * 
	 * @param key 配置键
	 * @return 字符串
	 */
	public String getString(String key) {
		return get(key).toString();
	}
	
	/**
	 * 获取单精度浮点型数据
	 * 
	 * @param key 配置键
	 * @return 单精度浮点型
	 */
	public float getFloat(String key) {
		return Float.valueOf(getString(key));
	}
	
	/**
	 * 获取双精度浮点数据
	 * 
	 * @param key 配置键
	 * @return 双精度浮点型
	 */
	public double getDouble(String key) {
		return Double.parseDouble(getString(key));
	}
	
	/**
	 * 获取布尔型数据
	 * 
	 * @param key 配置键
	 * @return 布尔型
	 */
	public boolean is(String key) {
		return Boolean.parseBoolean(getString(key));
	}
	public boolean isNot(String key) {
		return !is(key);
	}
	
	/**
	 * 获取以 "," 为分隔符的整型数组
	 * 
	 * @param key 配置键
	 * @return 短整型数组
	 */
	public int[] getInts(String key) {
		String[] strings = getString(key).split(",");
		int[] ints = new int[strings.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = Integer.parseInt(strings[i]);
		}
		return ints;
	}
	
	/**
	 * 获取以指定分隔符的整型数组
	 * 
	 * @param key   配置键
	 * @param split 分隔符
	 * @return 短整型数组
	 */
	public int[] getInts(String key, String split) {
		String[] strings = getString(key).split(split);
		int[] ints = new int[strings.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = Integer.parseInt(strings[i]);
		}
		return ints;
	}
	
	/**
	 * 获取以 "," 为分隔符的字符串数组
	 * 
	 * @param key 配置键
	 * @return 字符串数组
	 */
	public String[] getStrings(String key) {
		return getString(key).split(",");
	}
	
	/**
	 * 获取以指定分隔符的字符串数组
	 * 
	 * @param key   配置键
	 * @param split 分隔符
	 * @return 字符串数组
	 */
	public String[] getStrings(String key, String split) {
		return getString(key).split(split);
	}

	/**
	 * 获取类型 T 的数据对象，通常是数字或字符串
	 * 
	 * @param <T> 指定类型
	 * @param key 配置键
	 * @return 指定类型
	 */
	@SuppressWarnings("unchecked")
	public <T>T getT(ConfigT<T> key) {
		String v = getString(key.get());
		if (EncodeUtils.isNumber(v)) {
			Number n;
			if (v.indexOf(".") != -1) {
				n = Double.parseDouble(v);
			} else {
				n = Integer.parseInt(v);
			}
			return (T) n;
		}
		return (T) get(key.get());
	}
	
	/**
	 * 修改指定配置
	 * 
	 * @param key   配置键
	 * @param value 配置值
	 */
	public void put(String key, Object value) {
		this.map.put(key, value);
	}
	
	/**
	 * 防空指针以及兼容性保留
	 * 
	 * @param key 配置键
	 * @return 对象
	 */
	public Object get(String key) {
		if (has(key)) {
			return map.get(key);
		} else {
			throw new NullPointerException("找不到配置属性: " + key);
		}
	}

	/**
	 * 获取配置文件 HashMap
	 * 
	 * @return 哈希链表
	 */
	public Map<String, Object> getMap() {
		return map;
	}

	/**
	 * 设置配置文件 HashMap
	 * 
	 * @param map 哈希链表
	 */
	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
}