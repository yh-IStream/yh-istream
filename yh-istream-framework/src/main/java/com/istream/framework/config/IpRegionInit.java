package com.istream.framework.config;

import com.istream.framework.util.IpRegionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 预加载 ip2region 数据
 *
 * @author istream
 * @since 2026-10-17
 */
@Slf4j
@Component
public class IpRegionInit implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始预加载 ip2region 数据...");
        long start = System.currentTimeMillis();

        IpRegionUtils.init();

        long cost = System.currentTimeMillis() - start;
        log.info("ip2region 数据预加载完成，耗时: {} ms", cost);
    }
}