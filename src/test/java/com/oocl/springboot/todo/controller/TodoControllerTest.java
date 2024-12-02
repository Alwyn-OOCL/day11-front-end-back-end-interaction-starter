package com.oocl.springboot.todo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.oocl.springboot.todo.exception.TodoNotFoundException;
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

    @Test
    void should_return_paged_companies_when_get_by_page_params() throws Exception {
        // Given
        var pageIndex = 2;
        var pageSize = 2;
        final var the3thEmployeeCompanyInPage2 = todoRepository.findById(todo3.getId());

        // When
        // Then
        client.perform(MockMvcRequestBuilders.get(String.format("/todo?pageIndex=%s&pageSize=%s", pageIndex, pageSize)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(the3thEmployeeCompanyInPage2.get().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].text").value(the3thEmployeeCompanyInPage2.get().getText()));
    }

    @Test
    void should_return_created_todo() throws Exception {
        //Given
        var givenText = "New text";
        var givenDone = false;
        String givenTodo = String.format(
                "{\"text\": \"%s\", \"done\": %b}",
                givenText,
                givenDone
        );

        // When
        // Then
        client.perform(
                        MockMvcRequestBuilders.post("/todo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(givenTodo))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(givenText));
    }

    @Test
    void should_return_updated_todo_when_update_with_id_and_data() throws Exception {
        // Given
        var idToUpdate = todo3.getId();
        var textToUpdate = "New Text";
        String requestBody = String.format("{\"text\": \"%s\" }", textToUpdate);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.put("/todo/" + idToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(idToUpdate))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(textToUpdate));
    }

    @Test
    void should_return_no_content_when_delete() throws Exception {
        // Given
        var toDeleteTodoId = todo3.getId();

        // When
        final var result =
                client.perform(MockMvcRequestBuilders.delete("/todo/" + toDeleteTodoId)).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(todoRepository.findAll()).hasSize(2);
    }

    @Test
    void should_return_todo_when_get_by_id() throws Exception {
        // Given

        final var todoGiven = todoRepository.findAll().get(0);

        // When
        // Then
        client.perform(MockMvcRequestBuilders.get("/todo/" + todoGiven.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(todoGiven.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(todoGiven.getText()));
    }

    @Test
    void should_print_message_when_find_by_id_not_found() {
        // Given
        var notFoundId = 999;

        // When


        // Then
        assertThrows(Exception.class, () -> {
            client.perform(MockMvcRequestBuilders.get("/todo/" + notFoundId)).andReturn();
        });
    }
}
