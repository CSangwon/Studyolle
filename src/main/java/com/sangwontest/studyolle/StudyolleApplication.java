package com.sangwontest.studyolle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StudyolleApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyolleApplication.class, args);
    }

}
