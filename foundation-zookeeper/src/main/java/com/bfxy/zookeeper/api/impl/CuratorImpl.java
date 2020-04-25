package com.bfxy.zookeeper.api.impl;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.bfxy.zookeeper.api.ChangedEvent;
import com.bfxy.zookeeper.api.NodeListener;
import com.bfxy.zookeeper.api.ZookeeperClient;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.ThreadUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@ConfigurationProperties(prefix="zookeeper")
public class CuratorImpl implements ZookeeperClient, InitializingBean {

	
	private String address;
	
	private int sessionTimeout;
	
	private int connectionTimeout;
	
	private CuratorFramework client;
	
	private final ExecutorService EVENT_THREAD_POOL = Executors.newFixedThreadPool(1, ThreadUtils.newThreadFactory("T-pathChildrenCache"));
	
	private final ExecutorService DIRECT_EXECUTOR = MoreExecutors.newDirectExecutorService();
	
	//私有化的构造方法
	private CuratorImpl() {}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		creator();
	}

	private void creator() {
		client = CuratorFrameworkFactory.builder()
				.connectString(address)
				.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
				.connectionTimeoutMs(connectionTimeout)
				.sessionTimeoutMs(sessionTimeout)
				.build();
		//启动服务
		client.start();
		
	}

	@Override
	public CuratorFramework getClient() {
		if(client == null) {
			creator();
		} 
		return client;
	}

	@Override
	public void addPersistentNode(String path, String data) throws Exception {
		try {
			client.create()
			.creatingParentsIfNeeded()
			.withMode(CreateMode.PERSISTENT)
			.forPath(path , data.getBytes(Charset.defaultCharset()));			
		} catch (KeeperException.NodeExistsException e) {
			log.warn("Node already exists: {}" , path);
		} catch (Exception e) {
			throw new Exception("addPersistentNode error", e);
		}
	}

	@Override
	public String addEphemeralNode(String path, String data) throws Exception {
		return client.create()
				.withMode(CreateMode.EPHEMERAL)
				.forPath(path, data.getBytes(Charset.defaultCharset()));
	}

	@Override
	public Stat setData(String path, String data) throws Exception {
		return client.setData().forPath(path, data.getBytes(Charset.defaultCharset()));
	}

	@Override
	public String getData(String path) throws Exception {
		return new String(client.getData().forPath(path), Charset.defaultCharset());
	}

	@Override
	public void deletePath(String path) throws Exception {
		client.delete().forPath(path);
	}

	@Override
	public boolean isConnected() {
		return client.getZookeeperClient().isConnected();
	}

	@Override
	public Stat stat(String path) throws Exception {
		return client.checkExists().forPath(path);
	}

	@Override
	public void listener4ChildrenPath(final String parent, final NodeListener listener) throws Exception {
		PathChildrenCache cache = new PathChildrenCache(this.client, parent, true, false, EVENT_THREAD_POOL);
		cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
		log.info("add listener parent path start, path : {} ", parent);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework curator, PathChildrenCacheEvent event) throws Exception {
				ChildData cd = event.getData();
				if(cd == null) return;
				switch(event.getType()) {
					case CHILD_ADDED:
						 listener.nodeChanged(CuratorImpl.this, 
								 new ChangedEvent(cd.getPath(), new String(cd.getData()), ChangedEvent.Type.CHILD_ADDED));
						 break;
					case CHILD_UPDATED:
						 listener.nodeChanged(CuratorImpl.this, 
								 new ChangedEvent(cd.getPath(), new String(cd.getData()), ChangedEvent.Type.CHILD_UPDATED));
						 break;
					case CHILD_REMOVED:
						 listener.nodeChanged(CuratorImpl.this, 
								 new ChangedEvent(cd.getPath(), new String(cd.getData()), ChangedEvent.Type.CHILD_REMOVED));						
						 break;
				}
			}
		}, DIRECT_EXECUTOR);
	}

	@Override
	public void close() {
		if(null != client) {
			try {
				this.client.close();
			} catch (Exception e) {
				e.printStackTrace();
				log.error("zookeeper client is closed error: {}" , e);
			}
		}
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

}
