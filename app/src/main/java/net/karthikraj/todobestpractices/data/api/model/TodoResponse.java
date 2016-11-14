package net.karthikraj.todobestpractices.data.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karthik on 10/11/16.
 */

public class TodoResponse {

    private List<Todo> data = new ArrayList<Todo>();

    /**
     *
     * @return data
     * The data
     */
    public List<Todo> getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(List<Todo> data) {
        this.data = data;
    }
}
