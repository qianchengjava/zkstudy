package com.bfxy.zookeeper.api;

public class ChangedEvent {

	public static enum Type {
		CHILD_ADDED,
		CHILD_UPDATED,
		CHILD_REMOVED;
	}
	
	private String path;
	private String data;
	private Type type;
	
	public ChangedEvent(String path, String data, Type type) {
		this.path = path;
		this.data = data;
		this.type = type;
	}
	
	public String getPath() {
		return path;
	}
	public String getData() {
		return data;
	}
	public Type getType() {
		return type;
	}
	
	
	
}
