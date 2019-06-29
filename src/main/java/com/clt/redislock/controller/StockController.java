package com.clt.redislock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ Author   ：clt.
 * @ Date     ：Created in 14:16 2019/6/29
 */
@RestController
public class StockController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/")
    public String buy(){
        /**
         *对不同线程生成不同锁值
         */
        String lockvalue = UUID.randomUUID().toString();
        /**
         * 检测库存是否加锁，如果没锁则进行加锁并设置锁的超时时间
         */
        boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("lock",lockvalue,2 ,TimeUnit.SECONDS);
        if (!flag){
            return "error";
        }
        try {
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if (stock > 0){
                /**
                 * 模拟线程出现异常
                 */
                if ((System.currentTimeMillis()&3) == 0){
                    int a = 1/0;
                }
                stock--;
                stringRedisTemplate.opsForValue().decrement("stock");
                System.out.println("购买成功，商品库存还剩"+stock);
            } else {
                System.out.println("购买失败，库存不足");
                return "error";
            }
        }finally {
            /**
             * 判断线程锁是否是自己的持有锁，如果是则进行锁释放
             */
            if (lockvalue.equals(stringRedisTemplate.opsForValue().get("lock"))){
                stringRedisTemplate.delete("lock");
            }
        }
        return "success";
    }
}
