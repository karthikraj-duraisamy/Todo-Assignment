package net.karthikraj.todobestpractices.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;

import net.karthikraj.todobestpractices.R;
import net.karthikraj.todobestpractices.data.api.ApiCallMaker;
import net.karthikraj.todobestpractices.data.db.TodoTable;
import net.karthikraj.todobestpractices.ui.NewTodoActivity;
import net.karthikraj.todobestpractices.ui.events.TodoPullCompleteEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by root on 10/11/16.
 */

public class PendingFragment extends Fragment {


    private static final String WHERE_ID_QUERY = "id = ";
    private static final String SUCCESS = "Success";

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeMenuRefresh)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.mainLayout)
    LinearLayout thisView;
    @BindString(R.string.marked_as_done)
    String markedAsDoneMessage;
    @BindString(R.string.undo)
    String undoLabel;
    @BindString(R.string.restored)
    String restoredMessage;


    private Long itemId;
    private PendingTodoAdapter pendingTodoAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<TodoTable> todoItemsList;
    private List<Integer> selectedItems;

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
        pendingTodoAdapter.notifyDataSetChanged();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout rv = (LinearLayout) inflater.inflate(
                R.layout.fragment_pending_tasks, container, false);
        ButterKnife.bind(this, rv);
        todoItemsList = new ArrayList<TodoTable>();
        selectedItems = new ArrayList<Integer>();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        setHasOptionsMenu(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        pendingTodoAdapter = new PendingTodoAdapter();
        mRecyclerView.setAdapter(pendingTodoAdapter);

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

        if (todoTableList.isEmpty()) {
            swipeContainer.setRefreshing(true);
            ApiCallMaker.makeTodoPullCall(getActivity());
        } else {
            for (TodoTable todoTable : todoTableList) {
                if (todoTable.state == 0) {
                    todoItemsList.add(todoTable);
                }
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (selectedItems.size() > 0) {
            inflater.inflate(R.menu.menu_delete_items, menu);
        } else {
            inflater.inflate(R.menu.menu_todo_pending, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_todo:
                launchNewTaskIntent();
                break;
            case R.id.action_delete_items:
                removeItemFromList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchNewTaskIntent() {
        Intent intent = new Intent(getActivity(), NewTodoActivity.class);
        startActivity(intent);
    }

    private void removeItemFromList() {
        int position;
        for (int i = 0; i < selectedItems.size(); i++) {
            position = selectedItems.get(i);
            TodoTable todoTable = new Select().from(TodoTable.class).where(WHERE_ID_QUERY + todoItemsList.get(position).getId()).executeSingle();
            if (todoTable != null) {
                todoTable.delete();
                todoTable.save();
            }
        }
        selectedItems.clear();
        getActivity().invalidateOptionsMenu();
        mRecyclerView.setAdapter(pendingTodoAdapter);
        EventBus.getDefault().post(new TodoPullCompleteEvent(SUCCESS));
    }


    public class PendingTodoAdapter extends RecyclerView.Adapter<PendingTodoAdapter.ViewHolder> {

        View mainView;


        // Create new views (invoked by the layout manager)
        @Override
        public PendingTodoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
            // create a new view
            mainView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_todo, parent, false);
            PendingTodoAdapter.ViewHolder vh = new PendingTodoAdapter.ViewHolder(mainView);
            ButterKnife.bind(vh, mainView);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final PendingTodoAdapter.ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            TodoTable todoTableTemp = todoItemsList.get(position);
            holder.bindItem(todoTableTemp, position);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!holder.selected) {
                        selectedItems.add(holder.getAdapterPosition());
                        holder.itemView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        holder.selected = true;
                        if (selectedItems.size() == 1)
                            getActivity().invalidateOptionsMenu();

                    } else {
                        for (int i = 0; i < selectedItems.size(); i++) {
                            if (selectedItems.get(i) == holder.getAdapterPosition()) {
                                selectedItems.remove(selectedItems.get(i));
                            }
                            holder.selected = false;
                        }
                        holder.itemView.setBackgroundColor(getResources().getColor(R.color.colorIcons));
                        if (selectedItems.size() <= 0)
                            getActivity().invalidateOptionsMenu();
                    }

                    return false;
                }
            });


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
            private TodoTable todoTable;
            private boolean selected;

            public ViewHolder(View v) {
                super(v);
            }

            public void bindItem(TodoTable todo, int position) {
                todoTable = todo;
                tvTodoDescrtion.setText(todoTable.name);
                selected = false;
                if (position == todoItemsList.size() - 1)
                    seperatorView.setVisibility(View.GONE);
                else
                    seperatorView.setVisibility(View.VISIBLE);

                mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        //holder.getAdapterPosition()
                        if (b) {
                            TodoTable todoTable = new Select()
                                    .from(TodoTable.class)
                                    .where(WHERE_ID_QUERY + todoItemsList.get(getAdapterPosition()).getId())
                                    .executeSingle();
                            todoTable.state = 1;
                            todoTable.save();
                            Snackbar.make(thisView, markedAsDoneMessage,
                                    Snackbar.LENGTH_LONG)
                                    .setActionTextColor(getResources().getColor(R.color.colorAccent))
                                    .setAction(undoLabel, new UndoBarListener())
                                    .show();
                            itemId = todoItemsList.get(getAdapterPosition()).getId();
                            EventBus.getDefault().post(new TodoPullCompleteEvent(SUCCESS));
                            compoundButton.setChecked(false);
                        }
                    }
                });
            }
        }
    }

    class UndoBarListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            TodoTable todoTable = new Select()
                    .from(TodoTable.class)
                    .where(WHERE_ID_QUERY + itemId)
                    .executeSingle();
            todoTable.state = 0;
            todoTable.save();
            Snackbar.make(thisView, restoredMessage,
                    Snackbar.LENGTH_SHORT)
                    .show();
            EventBus.getDefault().post(new TodoPullCompleteEvent(SUCCESS));
        }
    }


}
