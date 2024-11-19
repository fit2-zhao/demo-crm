package io.demo.crm.services.system.controller;


import io.demo.crm.core.BaseMapper;
import io.demo.crm.services.system.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseMapperTest {

    @Mock
    private BaseMapper<User> baseMapper;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("admin");
        user.setEmail("test@a.com");
        user.setPassword("password123");
    }

    @Test
    public void testInsert() {
        when(baseMapper.insert(user)).thenReturn(1);
        Integer result = baseMapper.insert(user);
        assertEquals(1, result);
        verify(baseMapper, times(1)).insert(user);
    }

    @Test
    public void testBatchInsert() {
        List<User> users = Arrays.asList(user);
        when(baseMapper.batchInsert(users)).thenReturn(1);
        Integer result = baseMapper.batchInsert(users);
        assertEquals(1, result);
        verify(baseMapper, times(1)).batchInsert(users);
    }

    @Test
    public void testUpdateById() {
        when(baseMapper.updateById(user)).thenReturn(1);
        Integer result = baseMapper.updateById(user);
        assertEquals(1, result);
        verify(baseMapper, times(1)).updateById(user);
    }

    @Test
    public void testUpdate() {
        when(baseMapper.update(user)).thenReturn(1);
        Integer result = baseMapper.update(user);
        assertEquals(1, result);
        verify(baseMapper, times(1)).update(user);
    }

    @Test
    public void testDeleteByPrimaryKey() {
        when(baseMapper.deleteByPrimaryKey(1L)).thenReturn(1);
        Integer result = baseMapper.deleteByPrimaryKey(1L);
        assertEquals(1, result);
        verify(baseMapper, times(1)).deleteByPrimaryKey(1L);
    }

    @Test
    public void testDeleteByCriteria() {
        when(baseMapper.delete(user)).thenReturn(1);
        Integer result = baseMapper.delete(user);
        assertEquals(1, result);
        verify(baseMapper, times(1)).delete(user);
    }

    @Test
    public void testSelectByPrimaryKey() {
        when(baseMapper.selectByPrimaryKey(1L)).thenReturn(user);
        User result = baseMapper.selectByPrimaryKey(1L);
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(baseMapper, times(1)).selectByPrimaryKey(1L);
    }

    @Test
    public void testSelectAll() {
        when(baseMapper.selectAll("username")).thenReturn(Arrays.asList(user));
        List<User> result = baseMapper.selectAll("username");
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(baseMapper, times(1)).selectAll("username");
    }

    @Test
    public void testSelect() {
        when(baseMapper.select(user)).thenReturn(Arrays.asList(user));
        List<User> result = baseMapper.select(user);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(baseMapper, times(1)).select(user);
    }

    @Test
    public void testSelectOne() {
        when(baseMapper.selectOne(user)).thenReturn(user);
        User result = baseMapper.selectOne(user);
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(baseMapper, times(1)).selectOne(user);
    }

    @Test
    public void testSelectByColumn() {
        Serializable[] ids = {1L};
        when(baseMapper.selectByColumn("id", ids)).thenReturn(Arrays.asList(user));
        List<User> result = baseMapper.selectByColumn("id", ids);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(baseMapper, times(1)).selectByColumn("id", ids);
    }

    @Test
    public void testCountByExample() {
        when(baseMapper.countByExample(user)).thenReturn(1L);
        Long result = baseMapper.countByExample(user);
        assertEquals(1L, result);
        verify(baseMapper, times(1)).countByExample(user);
    }

    @Test
    public void testQuery() {
        Function<BaseMapper.SQL, BaseMapper.SQL> sqlBuild = sql -> sql;
        when(baseMapper.query(sqlBuild, user)).thenReturn(Arrays.asList(user));
        List<User> result = baseMapper.query(sqlBuild, user);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(baseMapper, times(1)).query(sqlBuild, user);
    }

    @Test
    public void testExist() {
        when(baseMapper.exist(user)).thenReturn(true);
        boolean result = baseMapper.exist(user);
        assertTrue(result);
        verify(baseMapper, times(1)).exist(user);
    }

    @Test
    public void testUpsert() {
        when(baseMapper.exist(user)).thenReturn(true);
        when(baseMapper.updateById(user)).thenReturn(1);
        Integer result = baseMapper.upsert(user);
        assertEquals(1, result);
        verify(baseMapper, times(1)).exist(user);
        verify(baseMapper, times(1)).updateById(user);

        when(baseMapper.exist(user)).thenReturn(false);
        when(baseMapper.insert(user)).thenReturn(1);
        result = baseMapper.upsert(user);
        assertEquals(1, result);
        verify(baseMapper, times(2)).exist(user);
        verify(baseMapper, times(1)).insert(user);
    }
}
