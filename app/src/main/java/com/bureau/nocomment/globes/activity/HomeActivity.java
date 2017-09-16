package com.bureau.nocomment.globes.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.fragment.ArchitectsFragment;
import com.bureau.nocomment.globes.fragment.BaseFragment;
import com.bureau.nocomment.globes.fragment.MapFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ViewPager mViewPager;
    TabLayout mTabs;
    HomePagerAdapter mPagerAdapter;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabs = (TabLayout) findViewById(R.id.tabs);
        mPagerAdapter = new HomePagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());

        mTabs.setupWithViewPager(mViewPager);

        // mViewPager.setCurrentItem(0, false);

        int elevation = getResources().getDimensionPixelSize(R.dimen.tabbar_elevation);
        ViewCompat.setElevation(mTabs, elevation);
        // apply elevation to homeToolbar too, otherwise the tabbar hides it
        ViewCompat.setElevation(mToolbar, elevation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.switch_language) {
            View switchLanguageIconView = mToolbar.findViewById(R.id.switch_language);
            PopupMenu menu = new PopupMenu(this, switchLanguageIconView);
            menu.inflate(R.menu.menu_language);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // TODO: change locale
                    return false;
                }
            });
            MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) menu.getMenu(), switchLanguageIconView);
            menuHelper.setForceShowIcon(true);
            menuHelper.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private static class HomePagerAdapter extends FragmentStatePagerAdapter {

        private interface HomeFragmentFactory {
            BaseFragment make();
        }

        private enum HomeFragmentsEnum {
            MAP(new HomeFragmentFactory() {
                @Override
                public BaseFragment make() {
                    return new MapFragment();
                }
            }),
            ARCHITECTS(new HomeFragmentFactory() {
                @Override
                public  BaseFragment make() {
                    return new ArchitectsFragment();
                }
            });

            public HomeFragmentFactory homeFragmentFactory;

            HomeFragmentsEnum(HomeFragmentFactory homeFragmentFactory) {
                this.homeFragmentFactory = homeFragmentFactory;
            }

            public static BaseFragment homeFragmentInstance(int enumIndex) {
                return HomeFragmentsEnum.values()[enumIndex].homeFragmentFactory.make();
            }
        }

        List<WeakReference<BaseFragment>> homeFragments;

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
            homeFragments = new ArrayList<>(getCount());
            for (int i = 0; i < getCount(); i++) {
                homeFragments.add(new WeakReference<BaseFragment>(null));
            }
        }

        @Override
        public int getCount() {
            return HomeFragmentsEnum.values().length;
        }

        @Override
        public BaseFragment getItem(int position) {
            if (fragmentExists(position)) {
                return homeFragments.get(position).get();
            } else {
                BaseFragment newFragment = HomeFragmentsEnum.homeFragmentInstance(position);
                putIntoCache(position, newFragment);
                return newFragment;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getItem(position).getTabName();
        }

        private void putIntoCache(int position, BaseFragment newFragment) {
            homeFragments.set(position, new WeakReference<>(newFragment));
        }

        private boolean fragmentExists(int position) {
            return homeFragments.get(position).get() != null;
        }
    }
}
