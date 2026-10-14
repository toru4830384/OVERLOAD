package overload_api.controller;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import overload_api.exception.GlobalExceptionHandler;
import overload_api.service.UserService;
/** API境界でプロフィールの保存条件を検証する。 */
class ReviewProfileValidationTest {
    @Test
    void missingAndNullBodyWeightNeverReachService() throws Exception {
        var service = mock(UserService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (String field : new String[] {"", "\"bodyWeightKg\":null,", "\"bodyWeightKg\":1000,", "\"bodyWeightKg\":70.001,"}) {
            mvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                    .content("{" + field + "\"age\":30,\"name\":\"Test\",\"gender\":\"male\"}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
    @Test
    void nameBoundaryIsValidatedBeforeService() throws Exception {
        var service = mock(UserService.class);
        when(service.update(anyLong(), anyString(), anyString(), anyInt(), any())).thenReturn(new overload_api.model.User());
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (String field : new String[] {"", "\"name\":null,", "\"name\":\"" + "a".repeat(51) + "\","}) {
            mvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                    .content("{" + field + "\"age\":30,\"bodyWeightKg\":70,\"gender\":\"male\"}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
        for (String name : new String[] {"", "a".repeat(50)}) {
            mvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"" + name + "\",\"age\":30,\"bodyWeightKg\":70,\"gender\":\"male\"}"))
                    .andExpect(status().isOk());
        }
    }

}
