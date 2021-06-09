package ru.todolist.stores;

import ru.todolist.model.Category;
import ru.todolist.model.Task;
import ru.todolist.model.User;

import java.util.Collection;

public interface Store {

    Collection<Task> getAllTasksByUser(User user);

    Collection<Task> getNewTasksByUser(User user);

    Task getTaskById(int id);

    int addTask(Task task);

    void updateTaskStatus(int id);

    void deleteTask(int id);

    int addUser(User user);

    User getUserById(int id);

    User getUserByEmail(String email);

    Category getCatById(int id);

    Collection<Category> getAllCategories();
}
