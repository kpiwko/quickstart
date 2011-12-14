package org.jboss.as.quickstarts.tasks;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Task implements Serializable {
    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String title;

//    private String description;
//
//    private Date due;
//
//    private boolean done;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Date getDue() {
//        return due;
//    }
//
//    public void setDue(Date due) {
//        this.due = due;
//    }
//
//    public boolean isDone() {
//        return done;
//    }
//
//    public void setDone(boolean done) {
//        this.done = done;
//    }
}
