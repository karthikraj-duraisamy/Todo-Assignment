package net.karthikraj.todobestpractices.ui.fragments;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;

import net.karthikraj.todobestpractices.R;
import net.karthikraj.todobestpractices.data.api.ApiCallMaker;
import net.karthikraj.todobestpractices.data.db.TodoTable;
import net.karthikraj.todobestpractices.ui.events.TodoPullCompleteEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by root on 10/11/16.
 */

public class DoneFragment extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeMenuRefresh)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.mainLayout)
    LinearLayout thisView;



    private DoneTodoAdapter doneTodoAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<TodoTable> todoItemsList;

    public static List<TodoTable> getToDoList() {
        return new Select()
                .from(TodoTable.class)
                .execute();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTodoPullCompleteEvent(TodoPullCompleteEvent event) {
        if (swipeContainer.isRefreshing())
            swipeContainer.setRefreshing(false);
        prepareTodoValues();
        doneTodoAdapter.notifyDataSetChanged();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout rv = (LinearLayout) inflater.inflate(
                R.layout.fragment_pending_tasks, container, false);
        ButterKnife.bind(this, rv);

        todoItemsList = new ArrayList<TodoTable>();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        setHasOptionsMenu(false);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        doneTodoAdapter = new DoneTodoAdapter();
        mRecyclerView.setAdapter(doneTodoAdapter);

        prepareTodoValues();
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                ApiCallMaker.makeTodoPullCall(getActivity());

            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        return rv;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    private void prepareTodoValues() {

        if (!todoItemsList.isEmpty())
            todoItemsList.clear();
        List<TodoTable> todoTableList = getToDoList();

        if (!todoTableList.isEmpty()) {
            for (TodoTable todoTable : todoTableList) {
                if (todoTable.state == 1) {
                    todoItemsList.add(todoTable);
                }
            }
        }
    }


    public class DoneTodoAdapter extends RecyclerView.Adapter<DoneTodoAdapter.ViewHolder> {

        View mainView;

        // Create new views (invoked by the layout manager)
        @Override
        public DoneTodoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
            // create a new view
            mainView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_todo, parent, false);

            DoneTodoAdapter.ViewHolder vh = new DoneTodoAdapter.ViewHolder(mainView);
            ButterKnife.bind(vh, mainView);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final DoneTodoAdapter.ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.tvTodoDescrtion.setText(todoItemsList.get(position).name);
            if (position == todoItemsList.size() - 1)
                holder.seperatorView.setVisibility(View.GONE);
            else
                holder.seperatorView.setVisibility(View.VISIBLE);

            holder.mCheckBox.setChecked(true);
            holder.mCheckBox.setEnabled(false);
            holder.tvTodoDescrtion.setPaintFlags(holder.tvTodoDescrtion.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return todoItemsList.size();
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            @BindView(R.id.checkbox)
            CheckBox mCheckBox;
            @BindView(R.id.tvTodoDescription)
            TextView tvTodoDescrtion;
            @BindView(R.id.seperator)
            View seperatorView;

            public ViewHolder(View v) {
                super(v);
            }
        }
    }
}
