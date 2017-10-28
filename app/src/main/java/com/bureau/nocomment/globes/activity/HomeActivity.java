package com.bureau.nocomment.globes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
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
import com.bureau.nocomment.globes.application.Globes;
import com.bureau.nocomment.globes.common.ForegroundDispatcher;
import com.bureau.nocomment.globes.common.Locale;
import com.bureau.nocomment.globes.fragment.ArchitectsFragment;
import com.bureau.nocomment.globes.fragment.BaseFragment;
import com.bureau.nocomment.globes.fragment.MapFragment;
import com.bureau.nocomment.globes.fragment.RoutesFragment;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements ArchitectsFragment.ProjectSelectedObserver, RoutesFragment.ProjectSelectedObserver {

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

        mViewPager.setAdapter(getPagerAdapter());
        mViewPager.setOffscreenPageLimit(getPagerAdapter().getCount());

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
            ModelRepository.getInstance().getItemLibrary().localeDidChange();
            this.recreate();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        getPagerAdapter().updateCacheWithFragment(fragment);
    }

    @Override
    public void onProjectSelected(Project p) {
        mViewPager.setCurrentItem(getPagerAdapter().getMapIndex(), true);
        getPagerAdapter().getMap().focusOnProject(p);
    }

    private HomePagerAdapter getPagerAdapter() {
        if (mPagerAdapter != null) {
            return mPagerAdapter;
        }
        mPagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        return mPagerAdapter;
    }

    // HomePageAdapter

    private static class HomePagerAdapter extends FragmentStatePagerAdapter {

        private interface HomeFragmentFactory {
            BaseFragment make();
            String getTitle();
            Class getFragmentClass();
        }

        private enum HomeFragmentsEnum {
            MAP(new HomeFragmentFactory() {
                @Override
                public BaseFragment make() {
                    return new MapFragment();
                }

                @Override
                public String getTitle() {
                    return Globes.getAppContext().getResources().getString(R.string.tab_map);
                }

                @Override
                public Class getFragmentClass() {
                    return MapFragment.class;
                }
            }),
            ARCHITECTS(new HomeFragmentFactory() {
                @Override
                public  BaseFragment make() {
                    return new ArchitectsFragment();
                }

                @Override
                public String getTitle() {
                    return Globes.getAppContext().getResources().getString(R.string.tab_architects);
                }

                @Override
                public Class getFragmentClass() {
                    return ArchitectsFragment.class;
                }
            }),
            ROUTES(new HomeFragmentFactory() {
                @Override
                public  BaseFragment make() {
                    return new RoutesFragment();
                }

                @Override
                public String getTitle() {
                    return Globes.getAppContext().getResources().getString(R.string.tab_routes);
                }

                @Override
                public Class getFragmentClass() {
                    return RoutesFragment.class;
                }
            });

            public HomeFragmentFactory homeFragmentFactory;

            HomeFragmentsEnum(HomeFragmentFactory homeFragmentFactory) {
                this.homeFragmentFactory = homeFragmentFactory;
            }

            public static BaseFragment makeHomeFragmentInstance(int enumIndex) {
                return HomeFragmentsEnum.values()[enumIndex].homeFragmentFactory.make();
            }

            public static String getFragmentTitle(int enumIndex) {
                return HomeFragmentsEnum.values()[enumIndex].homeFragmentFactory.getTitle();
            }

            public static Class getFragmentClass(int enumIndex) {
                return HomeFragmentsEnum.values()[enumIndex].homeFragmentFactory.getFragmentClass();
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
                BaseFragment newFragment = HomeFragmentsEnum.makeHomeFragmentInstance(position);
                putIntoCache(position, newFragment);
                return newFragment;
            }
        }

        public void updateCacheWithFragment(Fragment fragment) {
            for(int pos = 0; pos < getCount(); ++pos) {
                Class clazz = HomeFragmentsEnum.getFragmentClass(pos);
                if (fragment.getClass() == clazz) {
                    putIntoCache(pos, (BaseFragment) fragment);
                    break;
                }
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

        int getRoutesIndex() {
            return HomeFragmentsEnum.ROUTES.ordinal();
        }

        RoutesFragment getRoutes() {
            return getExistingFragmentAt(getRoutesIndex());
        }

        private <T> T getExistingFragmentAt(int index) {
            if (index >= homeFragments.size()) {
                return null;
            }
            return (T) homeFragments.get(index).get();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return HomeFragmentsEnum.getFragmentTitle(position);
        }

        private void putIntoCache(int position, BaseFragment newFragment) {
            homeFragments.set(position, new WeakReference<>(newFragment));
        }

        private boolean fragmentExists(int position) {
            return homeFragments.get(position).get() != null;
        }
    }
}
