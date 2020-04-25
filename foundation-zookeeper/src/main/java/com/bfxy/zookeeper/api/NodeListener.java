package com.bfxy.zookeeper.api;

public interface NodeListener {

	void nodeChanged(ZookeeperClient client, ChangedEvent event) throws Exception;
}
