package com.nachumToDoApp.vr2.Mission;

public class ToDoModel {

    //the id of the task
    private int id;
    //the status of the task 1 for complete and 0 for not complete
    private int status;
    //the content of the task
    private String task;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
