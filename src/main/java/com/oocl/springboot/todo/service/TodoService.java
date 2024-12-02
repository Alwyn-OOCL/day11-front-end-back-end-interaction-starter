package com.oocl.springboot.todo.service;

import com.oocl.springboot.todo.exception.TodoNotFoundException;
import com.oocl.springboot.todo.model.Todo;
import com.oocl.springboot.todo.repository.TodoRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> findAll(){
        return todoRepository.findAll();
    }

    public Page<Todo> findAll(int pageIndex, int pageSize) {
        PageRequest page = PageRequest.of(pageIndex - 1, pageSize);
        Page<Todo> todos = todoRepository.findAll(page);
        return new PageImpl<>(todos.getContent(), page, todos.getTotalElements());
    }

    public Todo findById(Integer id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("No such todo"));
    }

    public Todo create(Todo company) {
        return todoRepository.save(company);
    }

    public Todo update(Integer id, Todo todo) {
        final var todoNeedToUpdate = todoRepository
                .findById(id)
                .orElseThrow();

        var textToUpdate = todo.getText() == null ? todoNeedToUpdate.getText() : todo.getText();
        var doneToUpdate = todo.getDone() == null ? todoNeedToUpdate.getDone() : todo.getDone();

        final var todoToUpdate = new Todo(id,textToUpdate,doneToUpdate);
        return todoRepository.save(todoToUpdate);
    }

    public void delete(Integer id) {
        todoRepository.deleteById(id);
    }
}
