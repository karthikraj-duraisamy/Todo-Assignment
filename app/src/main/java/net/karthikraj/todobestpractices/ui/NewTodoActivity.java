package net.karthikraj.todobestpractices.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.karthikraj.todobestpractices.R;
import net.karthikraj.todobestpractices.data.db.TodoTable;
import net.karthikraj.todobestpractices.ui.events.TodoPullCompleteEvent;
import net.karthikraj.todobestpractices.ui.transitions.FabTransform;
import net.karthikraj.todobestpractices.ui.transitions.MorphTransform;
import net.karthikraj.todobestpractices.ui.widget.BottomSheet;
import net.karthikraj.todobestpractices.ui.widget.ObservableScrollView;
import net.karthikraj.todobestpractices.utils.AnimUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by karthik on 10/11/16.
 */
@TargetApi(21)
public class NewTodoActivity extends AppCompatActivity {
    public static final int RESULT_DRAG_DISMISSED = 3;
    private static final String SUCCESS = "Success";
    @BindView(R.id.bottom_sheet)
    BottomSheet bottomSheet;
    @BindView(R.id.bottom_sheet_content)
    ViewGroup bottomSheetContent;
    @BindView(R.id.title)
    TextView sheetTitle;
    @BindView(R.id.scroll_container)
    ObservableScrollView scrollContainer;
    @BindView(R.id.new_todo_title)
    EditText title;
    @BindView(R.id.new_todo_post)
    Button post;
    @BindDimen(R.dimen.z_app_bar)
    float appBarElevation;

    @Override
    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_todo);
        ButterKnife.bind(this);
        if (!FabTransform.setup(this, bottomSheetContent)) {
            MorphTransform.setup(this, bottomSheetContent,
                    ContextCompat.getColor(this, R.color.colorPrimaryLight), 0);
        }

        bottomSheet.registerCallback(new BottomSheet.Callbacks() {
            @Override
            public void onSheetDismissed() {
                // After a drag dismiss, finish without the shared element return transition as
                // it no longer makes sense.  Let the launching window know it's a drag dismiss so
                // that it can restore any UI used as an entering shared element
                setResult(RESULT_DRAG_DISMISSED);
                finish();
            }
        });

        scrollContainer.setListener(new ObservableScrollView.OnScrollListener() {
            @Override
            public void onScrolled(int scrollY) {
                if (scrollY != 0
                        && sheetTitle.getTranslationZ() != appBarElevation) {
                    sheetTitle.animate()
                            .translationZ(appBarElevation)
                            .setStartDelay(0L)
                            .setDuration(80L)
                            .setInterpolator(AnimUtils.getFastOutSlowInInterpolator
                                    (NewTodoActivity.this))
                            .start();
                } else if (scrollY == 0 && sheetTitle.getTranslationZ() == appBarElevation) {
                    sheetTitle.animate()
                            .translationZ(0f)
                            .setStartDelay(0L)
                            .setDuration(80L)
                            .setInterpolator(AnimUtils.getFastOutSlowInInterpolator
                                    (NewTodoActivity.this))
                            .start();
                }
            }
        });


        overridePendingTransition(R.anim.todo_new_enter_anim, R.anim.todo_new_exit_anim);
        bottomSheetContent.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        bottomSheetContent.getViewTreeObserver().removeOnPreDrawListener(this);
                        bottomSheetContent.setTranslationY(bottomSheetContent.getHeight());
                        bottomSheetContent.animate()
                                .translationY(0f)
                                .setStartDelay(120L)
                                .setDuration(240L)
                                .setInterpolator(AnimUtils.getLinearOutSlowInInterpolator
                                        (NewTodoActivity.this));
                        return false;
                    }
                });
    }

    @Override
    protected void onPause() {
        // customize window animations
        overridePendingTransition(R.anim.todo_new_enter_anim, R.anim.todo_new_exit_anim);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @OnClick(R.id.bottom_sheet)
    protected void dismiss() {
        bottomSheetContent.animate()
                .translationY(bottomSheetContent.getHeight())
                .setDuration(160L)
                .setInterpolator(AnimUtils.getFastOutLinearInInterpolator
                        (NewTodoActivity.this))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                    }
                });
    }

    @OnTextChanged(R.id.new_todo_title)
    protected void titleTextChanged(CharSequence text) {
        setPostButtonState();
    }


    @OnClick(R.id.new_todo_post)
    protected void postNewTodo() {
        TodoTable todoTable = new TodoTable();
        todoTable.name = title.getText().toString();
        todoTable.state = 0;
        todoTable.save();
        finishAfterTransition();

        EventBus.getDefault().post(new TodoPullCompleteEvent(SUCCESS));
    }

    private void setPostButtonState() {
        post.setEnabled(!TextUtils.isEmpty(title.getText())
        );
    }


}
