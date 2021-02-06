package net.imyeyu.itools.config;

/**
 * <br>配置转换，针对储存数据和表现不一的配置
 * <br>比如储存为 23:00:00，表现为 3 个输入框，那么绑定就需要做转换
 * <p>第一个泛型为配置值的类型，第二个泛型为绑定组件的数据类型
 * 
 * @author 夜雨
 *
 */
public interface ConfigConverter<T, K> {

	/**
	 * 当获取配置并即将设置到组件时调用
	 * 
	 * @param data 配置值
	 * @return
	 */
	public T beforeSet(K data);
	
	/**
	 * 当组件更新值并即将设置到缓存配置时调用
	 * 
	 * @param data  组件更新值
	 * @param cache 缓存的原配置值，这可能会被其他组件更新
	 * @return
	 */
	public K beforeUpdate(T data, K cache);
}
