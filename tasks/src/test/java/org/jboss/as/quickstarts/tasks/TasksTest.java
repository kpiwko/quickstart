package org.jboss.as.quickstarts.tasks;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Arquillian.class)
public class TasksTest {

    private static long userId;
    private static final String username = "john_doe";
    private static final String[] taskTitles = new String[]{"task1", "task2", "task3"};

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Deployment
    public static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackage(User.class.getPackage())
                .addAsResource("persistence.xml", "META-INF/persistence.xml")
                .addAsManifestResource(new ByteArrayAsset(new byte[0]), "beans.xml");
    }

    @Test
    public void persistenceUnitInjectionTest() {
        Assert.assertNotNull(emf);
    }

    @Test
    public void createUserTest() {
        EntityManager em = emf.createEntityManager();
        User user = new User();
        user.setUsername(username);

        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();

        Assert.assertNotNull(user.getId());
        Assert.assertEquals(user, em.find(User.class, user.getId()));
        userId = user.getId();

        em.close();
    }

    @Test
    public void readUserTest() {
        EntityManager em = emf.createEntityManager();
        User found = em.find(User.class, userId);
        Assert.assertEquals(username, found.getUsername());
        em.close();
    }

    @Test
    public void updateUserTest() {
        String newUsername = "new_username";

        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, userId);
        user.setUsername(newUsername);
        em.getTransaction().begin();
        em.getTransaction().commit();
        User found = em.find(User.class, userId);
        Assert.assertEquals(newUsername, found.getUsername());
        em.close();
    }

    @Test
    public void createAndReadTaskTest() {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, userId);
        Set<Task> tasks = new HashSet<Task>();
        for (String title : taskTitles) {
            Task task = new Task();
            task.setTitle(title);
            tasks.add(task);

            user.getTasks().add(task);
            task.setUser(user);
        }

        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();

        List<Task> resultList = em.createQuery(
                "select t from Task t where t.user = :user", Task.class)
                .setParameter("user", user)
                .getResultList();

        Set<Task> persistedTasks = new HashSet<Task>(resultList);
        Assert.assertEquals(tasks, persistedTasks);
    }

    @Test
    public void updateTaskTest() {
        String newTitle = "new_title";

        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, userId);
        Task task = em.createQuery("select t from Task t where t.user = :user and t.title = :title", Task.class)
                .setParameter("user", user)
                .setParameter("title", taskTitles[0])
                .getResultList()
                .get(0);

        task.setTitle(newTitle);
        em.getTransaction().begin();
        em.getTransaction().commit();

        Task found = em.find(Task.class, task.getId());
        Assert.assertEquals(newTitle, found.getTitle());
        em.close();
    }

    @Test
    public void deleteTaskTest() {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, userId);
        TypedQuery<Task> query =
                em.createQuery("select t from Task t where t.user = :user", Task.class).setParameter("user", user);

        List<Task> before = query.getResultList();
        Task toRemove = before.get(0);

        user.getTasks().remove(toRemove);
        em.getTransaction().begin();
        em.remove(toRemove);
        em.getTransaction().commit();

        List<Task> after = query.getResultList();
        Assert.assertNull(em.find(Task.class, toRemove.getId()));
        Assert.assertEquals(before.size() - 1, after.size());

        em.close();
    }

    @Test
    public void deleteUserTest() {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, userId);

        em.getTransaction().begin();
        em.remove(user);
        em.getTransaction().commit();

        Assert.assertNull(em.find(User.class, user.getId()));

        List<Task> tasks = em.createQuery("select t from Task t", Task.class).getResultList();
        Assert.assertTrue(tasks.isEmpty());

        em.close();
    }

}
