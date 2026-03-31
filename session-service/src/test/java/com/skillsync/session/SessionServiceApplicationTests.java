package com.skillsync.session;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.skillsync.session.client.MentorClient;

@SpringBootTest
class SessionServiceApplicationTests {

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private MentorClient mentorClient;

    @Test
    void contextLoads() {
    }

}
