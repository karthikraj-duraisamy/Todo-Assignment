package net.karthikraj.todobestpractices.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.karthikraj.todobestpractices.R;
import net.karthikraj.todobestpractices.data.api.ApiCallMaker;
import net.karthikraj.todobestpractices.ui.fragments.DoneFragment;
import net.karthikraj.todobestpractices.ui.fragments.PendingFragment;
import net.karthikraj.todobestpractices.ui.transitions.FabTransform;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static net.karthikraj.todobestpractices.R.id.fab;

public class TodoActivity extends AppCompatActivity {
    private static final String TAG = TodoActivity.class.getSimpleName();
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";

    @BindView(fab)
    FloatingActionButton addItemFab;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.pending_tab_title)
    String tabTitlePending;
    @BindString(R.string.done_tab_title)
    String tabTitleDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_todo);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new PendingFragment(), tabTitlePending);
        adapter.addFragment(new DoneFragment(), tabTitleDone);
        viewPager.setAdapter(adapter);
    }

    @OnClick(fab)
    protected void fabClick() {
        Intent intent = new Intent(this, NewTodoActivity.class);
        FabTransform.addExtras(intent,
                ContextCompat.getColor(this, R.color.colorAccent), R.drawable.ic_plus_dark);
        startActivity(intent);
    }


    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

}
