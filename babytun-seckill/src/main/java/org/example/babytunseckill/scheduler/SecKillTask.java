package org.example.babytunseckill.scheduler;

import org.example.babytunseckill.dao.PromotionSecKillDAO;
import org.example.babytunseckill.entity.PromotionSecKill;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;


import javax.annotation.Resource;
import java.util.List;

@Component
public class SecKillTask {
    @Resource
    private PromotionSecKillDAO promotionSecKillDAO;
    //RedisTempldate是Spring封装的Redis操作类，提供了一系列操作redis的模板方法
    @Resource
    private RedisTemplate redisTemplate;
    @Scheduled(cron = "0/5 * * * * ?")
    public void startSecKill(){
        List<PromotionSecKill> list  = promotionSecKillDAO.findUnstartSecKill();
        for(PromotionSecKill ps : list){
            System.out.println(ps.getPsId() + "秒杀活动已启动");
            //删掉以前重复的活动任务缓存
            redisTemplate.delete("seckill:count:" + ps.getPsId());
            //有几个库存商品，则初始化几个list对象
            for(int i = 0 ; i < ps.getPsCount() ; i++){
                redisTemplate.opsForList().rightPush("seckill:count:" + ps.getPsId() , ps.getGoodsId());
            }
            ps.setStatus(1);
            promotionSecKillDAO.update(ps);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void endSecKill(){
        List<PromotionSecKill> psList = promotionSecKillDAO.findExpireSecKill();
        for (PromotionSecKill ps : psList) {
            System.out.println(ps.getPsId() + "秒杀活动已结束");
            ps.setStatus(2);
            promotionSecKillDAO.update(ps);
            redisTemplate.delete("seckill:count:" + ps.getPsId());
        }
    }
}
