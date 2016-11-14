package net.karthikraj.todobestpractices.data.api;

import net.karthikraj.todobestpractices.data.api.model.TodoResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by karthik on 10/11/16.
 */

public interface TodoAPIService {
    String ENDPOINT = "https://dl.dropboxusercontent.com/";

    @GET("u/6890301/tasks.json")
    Call<TodoResponse> getTodoItems();
}
