package net.karthikraj.todobestpractices.data.api;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.util.SQLiteUtils;

import net.karthikraj.todobestpractices.data.api.model.Todo;
import net.karthikraj.todobestpractices.data.api.model.TodoResponse;
import net.karthikraj.todobestpractices.data.db.TodoTable;
import net.karthikraj.todobestpractices.ui.events.TodoPullCompleteEvent;

import org.greenrobot.eventbus.EventBus;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by root on 10/11/16.
 */

public class ApiCallMaker {

    private static final String DELETE_QUERY_STARTER = "DELETE FROM ";
    private static final String TAG = ApiCallMaker.class.getSimpleName();
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";
    public static void makeTodoPullCall(Context mContext) {
        fetchTodoFromCloud(mContext);
    }


    private static Retrofit getRetrofit() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …
        // add logging as last interceptor
        httpClient.addInterceptor(logging);
        return new Retrofit.Builder()
                .baseUrl(TodoAPIService.ENDPOINT)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static void fetchTodoFromCloud(final Context mContext) {
        Retrofit retrofit = getRetrofit();
        TodoAPIService service = retrofit.create(TodoAPIService.class);
        Call<TodoResponse> todoResponseCall = service.getTodoItems();
        todoResponseCall.enqueue(new Callback<TodoResponse>() {
            @Override
            public void onResponse(Call<TodoResponse> call, Response<TodoResponse> response) {
                if (response.raw().code() == 200) {
                    SQLiteUtils.execSql(DELETE_QUERY_STARTER + TodoTable.class.getSimpleName());
                    TodoResponse data = response.body();
                    ActiveAndroid.beginTransaction();
                    try {
                        for (Todo todoTemp : data.getData()) {
                            TodoTable todoTable = new TodoTable();
                            todoTable.name = todoTemp.getName();
                            todoTable.state = todoTemp.getState();
                            todoTable.save();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                } else {
                    Log.d(TAG, response.message() + "Code :" + response.code());
                }
                EventBus.getDefault().post(new TodoPullCompleteEvent(SUCCESS));
            }

            @Override
            public void onFailure(Call<TodoResponse> call, Throwable t) {
                Log.e(TAG, "Failed", t);
                Toast.makeText(mContext, "Server Error + " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new TodoPullCompleteEvent(ERROR));
            }
        });
    }


}
