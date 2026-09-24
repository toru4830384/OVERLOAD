package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.mapper.MuscleMapper;
import overload_api.model.Muscle;

class MuscleServiceTest {

    @Test
    void findAll_部位が存在する場合_一覧を返す() {
        MuscleMapper muscleMapper =
                mock(MuscleMapper.class);

        MuscleService muscleService =
                new MuscleService(muscleMapper);

        Muscle muscle1 = new Muscle();
        muscle1.setId(1L);
        muscle1.setName("胸");

        Muscle muscle2 = new Muscle();
        muscle2.setId(2L);
        muscle2.setName("背中");

        when(muscleMapper.findAll())
                .thenReturn(List.of(muscle1, muscle2));

        List<Muscle> result =
                muscleService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("胸", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals("背中", result.get(1).getName());
    }
    
    @Test
    void findAll_部位が存在しない場合_空のリストを返す() {
        MuscleMapper muscleMapper =
                mock(MuscleMapper.class);

        MuscleService muscleService =
                new MuscleService(muscleMapper);

        when(muscleMapper.findAll())
                .thenReturn(List.of());

        List<Muscle> result =
                muscleService.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}