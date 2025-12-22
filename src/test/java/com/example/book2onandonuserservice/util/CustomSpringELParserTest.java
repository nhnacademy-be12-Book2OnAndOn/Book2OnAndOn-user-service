package com.example.book2onandonuserservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.book2onandonuserservice.global.util.CustomSpringELParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomSpringELParserTest {

    private final CustomSpringELParser parser = new CustomSpringELParser();

    @Test
    @DisplayName("단일 변수 SpEL 파싱 테스트")
    void testSingleVariableParsing() {
        // given
        String[] paramNames = {"userId"};
        Object[] args = {12345L};
        String spel = "#userId";

        // when
        Object result = parser.getDynamicValue(paramNames, args, spel);

        // then
        assertThat(result).isEqualTo(12345L);
    }

    @Test
    @DisplayName("여러 변수를 포함한 SpEL 파싱 테스트")
    void testMultipleVariableParsing() {
        // given
        String[] paramNames = {"userId", "orderId"};
        Object[] args = {10L, 999L};
        String spel = "T(String).valueOf(#userId) + '-' + T(String).valueOf(#orderId)";

        // when
        Object result = parser.getDynamicValue(paramNames, args, spel);

        // then
        assertThat(result).isEqualTo("10-999");
    }

    @Test
    @DisplayName("객체 값을 전달받아 SpEL로 필드 접근 테스트")
    void testObjectFieldParsing() {
        // given
        class Dummy {
            public String name = "testUser";
        }

        String[] paramNames = {"dummy"};
        Object[] args = {new Dummy()};
        String spel = "#dummy.name";

        // when
        Object result = parser.getDynamicValue(paramNames, args, spel);

        // then
        assertThat(result).isEqualTo("testUser");
    }

    @Test
    @DisplayName("존재하지 않는 변수 사용 시 null 반환 테스트")
    void testInvalidVariable() {
        // given
        String[] paramNames = {"a"};
        Object[] args = {1};
        String spel = "#b"; // 존재하지 않음

        // when
        Object result = parser.getDynamicValue(paramNames, args, spel);

        // then
        assertThat(result).isNull();
    }
}
