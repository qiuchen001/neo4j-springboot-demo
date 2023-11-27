package org.example.config;

import org.example.utils.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowFlakeHolder {

    private static SnowFlake snowFlake;

    @Autowired
    public SnowFlakeHolder(SnowFlake snowFlake) {
        SnowFlakeHolder.snowFlake = snowFlake;
    }

    public static SnowFlake getSnowFlake() {
        return snowFlake;
    }
}