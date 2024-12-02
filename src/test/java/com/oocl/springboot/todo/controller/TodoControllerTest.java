package com.oocl.springboot.todo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.oocl.springboot.todo.model.Todo;
import com.oocl.springboot.todo.repository.TodoRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    private MockMvc client;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private JacksonTester<List<Todo>> todoListJacksonTester;

    private Todo todo1;
    private Todo todo2;
    private Todo todo3;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
        todoRepository.flush();

        todo1 = new Todo(null, "text1", false);
        todo2 = new Todo(null, "text2", false);
        todo3 = new Todo(null, "text3", false);

        todoRepository.saveAll(List.of(todo1,todo2,todo3));
    }

    @Test
    void should_return_all_todos() throws Exception {
        // Given
        final List<Todo> givenTodos = todoRepository.findAll();

        // When
        final MvcResult result = client.perform(MockMvcRequestBuilders.get("/todo")).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        final List<Todo> fetchedTodos = todoListJacksonTester.parseObject(result.getResponse().getContentAsString());
        assertThat(fetchedTodos).hasSameSizeAs(givenTodos);
        assertThat(fetchedTodos)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(givenTodos);
    }

}
