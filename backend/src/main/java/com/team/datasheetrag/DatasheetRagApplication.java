package com.team.datasheetrag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 진입점.
 * 인텔리제이 실행 방법: 이 클래스 옆 ▶ 버튼을 누르거나,
 * Run Configuration 에서 Main class: com.team.datasheetrag.DatasheetRagApplication 으로 실행.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class DatasheetRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatasheetRagApplication.class, args);
    }
}
