package com.bfxy.zookeeper.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.bfxy.zookeeper.api.impl.CuratorImpl;

@ConditionalOnProperty(prefix = "zookeeper", name = "connectionTimeout", havingValue = "10000", matchIfMissing = true)
@EnableConfigurationProperties(CuratorImpl.class)
public class ZookeeperAutoConfiguration {
	
	
}
