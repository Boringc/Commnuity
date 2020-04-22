package com.boring.community;

import com.boring.community.until.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.sql.SQLOutput;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text = "这里可以赌博,可以嫖娼，可以吸毒，哈哈哈！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以赌⭐博,可以嫖⭐娼，可以吸⭐毒，哈哈哈！ 成人小说";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }


}
