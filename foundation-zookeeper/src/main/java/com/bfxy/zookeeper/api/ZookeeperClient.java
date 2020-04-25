package com.bfxy.zookeeper.api;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

public interface ZookeeperClient {

	void addPersistentNode(String path, String data) throws Exception;
	
	String addEphemeralNode(String path, String data) throws Exception;
	
	Stat setData(String path, String data) throws Exception;
	
	String getData(String path) throws Exception;
	
	void deletePath(String path) throws Exception;
	
	boolean isConnected();
	
	Stat stat(String path) throws Exception;
	
	void listener4ChildrenPath(final String parent, final NodeListener listener) throws Exception;

	void close();
	
	CuratorFramework getClient();
}
