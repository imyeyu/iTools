package net.imyeyu.itools.config;

public class ConfigT<T> {
	
	private String key;
	
	public ConfigT(String key) {
		this.key = key;
	}
	
	public String get() {
		return key;
	}
}
