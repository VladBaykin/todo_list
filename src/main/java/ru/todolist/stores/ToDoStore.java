package ru.todolist.stores;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.todolist.model.Category;
import ru.todolist.model.Task;
import ru.todolist.model.User;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class ToDoStore implements Store, AutoCloseable {
    private final static ToDoStore INST = new ToDoStore();
    private final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .configure().build();
    private final SessionFactory sf = new MetadataSources(registry)
            .buildMetadata().buildSessionFactory();

    private ToDoStore() {
    }

    public static ToDoStore instOf() {
        return INST;
    }

    @Override
    public void close() {
        StandardServiceRegistryBuilder.destroy(registry);
    }

    private <T> T tx(final Function<Session, T> command) {
        T rsl;
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            rsl = command.apply(session);
            session.getTransaction().commit();
        }
        return rsl;
    }

    private void consume(final Consumer<Session> command) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            command.accept(session);
            session.getTransaction().commit();
        }
    }

    @Override
    public Collection<Task> getAllTasksByUser(User user) {
        return tx(
                session -> session.createQuery(
                        "select distinct t from Task t left join fetch t.categories " +
                                "where t.owner = :user")
                        .setParameter("user", user)
                        .list()
        );
    }

    public void addCategory(Category category) {
        consume(session -> session.save(category));
    }

    @Override
    public Collection<Task> getNewTasksByUser(User user) {
        return tx(
                session -> session.createQuery(
                        "select distinct t from Task t left join fetch t.categories " +
                                "where t.owner = :user and t.status = false")
                        .setParameter("user", user)
                        .list()
        );
    }

    @Override
    public Task getTaskById(int id) {
        return tx(
                session -> session.get(Task.class, id)
        );
    }

    @Override
    public int addTask(Task task) {
        return tx(
                session -> (Integer) session.save(task)
        );
    }

    @Override
    public void updateTaskStatus(int id) {
        consume(session -> {
            Task existing = session.get(Task.class, id);
            existing.setStatus(true);
            session.update(existing);
        });
    }

    @Override
    public void deleteTask(int id) {
        consume(session -> {
            Task deleting = new Task();
            deleting.setId(id);
            session.delete(deleting);
        });
    }

    @Override
    public int addUser(User user) {
        return tx(session -> (Integer) session.save(user));
    }

    @Override
    public User getUserById(int id) {
        return tx(session -> session.get(User.class, id));
    }

    @Override
    public User getUserByEmail(String email) {
        return tx(
                session -> (User) session.createQuery("from User where email = :email")
                        .setParameter("email", email)
                        .uniqueResult());
    }

    @Override
    public Category getCatById(int id) {
        return tx(
                session -> session.get(Category.class, id)
        );
    }

    @Override
    public Collection<Category> getAllCategories() {
        return tx(
                session -> session.createQuery("from Category ").list()
        );
    }
}
