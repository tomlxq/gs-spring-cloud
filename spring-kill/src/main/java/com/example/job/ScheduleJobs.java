package com.example.job;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleJobs {
    public final static long SECOND = 1 * 1000;
    FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    //固定等待时间 @Scheduled(fixedDelay = 时间间隔 )
    @Scheduled(fixedDelay = SECOND * 2)
    public void fixedDelayJob() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        System.out.println("[FixedDelayJob Execute]" + fdf.format(new Date()));
    }

    //固定间隔时间 @Scheduled(fixedRate = 时间间隔 )
    @Scheduled(fixedRate = SECOND * 4)
    public void fixedRateJob() {
        System.out.println("[FixedRateJob Execute]" + fdf.format(new Date()));
    }


    //Corn表达式 @Scheduled(cron = Corn表达式)
    @Scheduled(cron = "0/4 * * * * ?")
    public void cronJob() {
        System.out.println("[CronJob Execute]" + fdf.format(new Date()));
    }


}
