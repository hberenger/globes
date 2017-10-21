package com.bureau.nocomment.globes.activity;

import android.content.Intent;
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
import com.bureau.nocomment.globes.common.ForegroundDispatcher;
import com.bureau.nocomment.globes.common.Locale;
import com.bureau.nocomment.globes.fragment.ArchitectsFragment;
import com.bureau.nocomment.globes.fragment.BaseFragment;
import com.bureau.nocomment.globes.fragment.MapFragment;
import com.bureau.nocomment.globes.fragment.TabFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ViewPager mViewPager;
    TabLayout mTabs;
    HomePagerAdapter mPagerAdapter;
    Toolbar mToolbar;
    ForegroundDispatcher mForegroundDispatcher;

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

        mForegroundDispatcher = new ForegroundDispatcher(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mForegroundDispatcher.start(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mForegroundDispatcher.stop(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(mForegroundDispatcher.isNfcIntent(intent)) {
            Intent detailActivityIntent = new Intent(this, DetailActivity.class);
            detailActivityIntent.setAction(intent.getAction());
            detailActivityIntent.putExtras(intent);
            startActivity(detailActivityIntent);
        }
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
            presentLanguageMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    // Private

    private void presentLanguageMenu() {
        View switchLanguageIconView = mToolbar.findViewById(R.id.switch_language);
        PopupMenu menu = new PopupMenu(this, switchLanguageIconView);
        menu.inflate(R.menu.menu_language);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == Locale.FRENCH.getMenuId()) {
                    changeLocale(Locale.FRENCH);
                } else if (item.getItemId() == Locale.ENGLISH.getMenuId()) {
                    changeLocale(Locale.ENGLISH);
                }
                return false;
            }
        });
        MenuItem selectedMenuItem = menu.getMenu().findItem(Locale.getCurrent(this).getMenuId());
        selectedMenuItem.setChecked(true);
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) menu.getMenu(), switchLanguageIconView);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    private void changeLocale(Locale locale) {
        if (!locale.setAsCurrent(this)) {
            this.recreate();
        }
    }

    // HomePageAdapter

    private static class HomePagerAdapter extends FragmentStatePagerAdapter {

        private interface HomeFragmentFactory {
            TabFragment make();
        }

        private enum HomeFragmentsEnum {
            MAP(new HomeFragmentFactory() {
                @Override
                public TabFragment make() {
                    return new MapFragment();
                }
            }),
            ARCHITECTS(new HomeFragmentFactory() {
                @Override
                public  TabFragment make() {
                    return new ArchitectsFragment();
                }
            });

            public HomeFragmentFactory homeFragmentFactory;

            HomeFragmentsEnum(HomeFragmentFactory homeFragmentFactory) {
                this.homeFragmentFactory = homeFragmentFactory;
            }

            public static TabFragment homeFragmentInstance(int enumIndex) {
                return HomeFragmentsEnum.values()[enumIndex].homeFragmentFactory.make();
            }
        }

        List<WeakReference<TabFragment>> homeFragments;

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
            homeFragments = new ArrayList<>(getCount());
            for (int i = 0; i < getCount(); i++) {
                homeFragments.add(new WeakReference<TabFragment>(null));
            }
        }

        @Override
        public int getCount() {
            return HomeFragmentsEnum.values().length;
        }

        @Override
        public TabFragment getItem(int position) {
            if (fragmentExists(position)) {
                return homeFragments.get(position).get();
            } else {
                TabFragment newFragment = HomeFragmentsEnum.homeFragmentInstance(position);
                putIntoCache(position, newFragment);
                return newFragment;
            }
        }

        int getMapIndex() {
            return HomeFragmentsEnum.MAP.ordinal();
        }

        MapFragment getMap() {
            return getExistingFragmentAt(getMapIndex());
        }

        int getArchitectsIndex() {
            return HomeFragmentsEnum.ARCHITECTS.ordinal();
        }

        ArchitectsFragment getArchitects() {
            return getExistingFragmentAt(getArchitectsIndex());
        }

        private <T> T getExistingFragmentAt(int index) {
            if (index >= homeFragments.size()) {
                return null;
            }
            return (T) homeFragments.get(index).get();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getItem(position).getTabName();
        }

        private void putIntoCache(int position, TabFragment newFragment) {
            homeFragments.set(position, new WeakReference<>(newFragment));
        }

        private boolean fragmentExists(int position) {
            return homeFragments.get(position).get() != null;
        }
    }
}
