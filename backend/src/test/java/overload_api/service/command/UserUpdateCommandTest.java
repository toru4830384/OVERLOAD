package overload_api.service.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import overload_api.controller.dto.UserUpdateRequest;
import overload_api.mapper.UserMapper;
import overload_api.service.UserService;

/** APIとService直接呼び出しで、同じプロフィール保存条件を適用する。 */
class UserUpdateCommandTest {
    private UserUpdateRequest input(String name, String gender, Integer age, BigDecimal weight) {
        var input = new UserUpdateRequest(); input.setName(name); input.setGender(gender);
        input.setAge(age); input.setBodyWeightKg(weight); return input;
    }
    @Test
    void invalidProfileIsRejectedByBothApiAndCommandBeforeDatabaseAccess() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var mapper = mock(UserMapper.class); var service = new UserService(mapper);
            for (var input : List.of(input(null, "male", 30, new BigDecimal("70")),
                    input("a".repeat(51), "male", 30, new BigDecimal("70")),
                    input("Test", null, 30, new BigDecimal("70")), input("Test", "none", 30, new BigDecimal("70")),
                    input("Test", "male", null, new BigDecimal("70")), input("Test", "male", -1, new BigDecimal("70")),
                    input("Test", "male", 30, null), input("Test", "male", 30, BigDecimal.ZERO),
                    input("Test", "male", 30, new BigDecimal("1000")),
                    input("Test", "male", 30, new BigDecimal("70.000")),
                    input("Test", "male", 30, new BigDecimal("70.001")))) {
                assertFalse(validator.validate(input).isEmpty());
                assertThrows(IllegalArgumentException.class, () -> service.update(1L,
                        input.getName(), input.getGender(), input.getAge(), input.getBodyWeightKg()));
            }
            verifyNoInteractions(mapper);
        }
    }
    @Test
    void acceptsSameBoundaryValuesAndEmptyNameAsApi() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            for (var input : List.of(input("", "male", 0, new BigDecimal("0.01")),
                    input("a".repeat(50), "female", Integer.MAX_VALUE, new BigDecimal("999.99")))) {
                assertTrue(factory.getValidator().validate(input).isEmpty());
                var command = new UserUpdateCommand(1L, input.getName(), input.getGender(), input.getAge(), input.getBodyWeightKg());
                assertEquals(input.getBodyWeightKg(), command.bodyWeightKg());
                assertEquals(input.getName(), command.name());
            }
        }
    }
}
