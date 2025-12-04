package com.example.book2onandonuserservice.restpage;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.book2onandonuserservice.global.dto.RestPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class RestPageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void restPage_constructor_basic() {

        RestPage<String> page = new RestPage<>(
                List.of("A", "B"),
                PageRequest.of(0, 10),
                2
        );

        assertThat(page.getNumber()).isZero();
        assertThat(page.getContent()).containsExactly("A", "B");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }


    @Test
    void json_deserialization_success() throws Exception {

        String json = """
                {
                  "content": ["A", "B"],
                  "number": 0,
                  "size": 10,
                  "totalElements": 2,
                  "pageable": {},
                  "last": false,
                  "totalPages": 1,
                  "sort": {},
                  "first": true,
                  "numberOfElements": 2
                }
                """;

        RestPage<String> page = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructParametricType(RestPage.class, String.class)
        );

        assertThat(page.getContent()).containsExactly("A", "B");
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(10);
        assertThat(page.getNumber()).isZero();
    }
}
