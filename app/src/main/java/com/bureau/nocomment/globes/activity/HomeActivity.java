package com.bureau.nocomment.globes.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bureau.nocomment.globes.R;
import com.bureau.nocomment.globes.application.Globes;
import com.bureau.nocomment.globes.common.ClassicNfcTextRecordParser;
import com.bureau.nocomment.globes.common.ForegroundDispatcher;
import com.bureau.nocomment.globes.common.Locale;
import com.bureau.nocomment.globes.fragment.ArchitectsFragment;
import com.bureau.nocomment.globes.fragment.BaseFragment;
import com.bureau.nocomment.globes.fragment.MapFragment;
import com.bureau.nocomment.globes.fragment.RoutesFragment;
import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.model.Project;
import com.bureau.nocomment.globes.model.Route;
import com.bureau.nocomment.globes.model.Table;
import com.bureau.nocomment.globes.service.KioskService;
import com.bureau.nocomment.globes.service.NotifBarHider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class HomeActivity extends AppCompatActivity implements ArchitectsFragment.ProjectSelectedObserver, RoutesFragment.RouteSelectedObserver {

    public static final String SHUTDOWN_INTENT = "com.nocomment.globes.homeactivity.SHUTDOWN";

    public interface NfcTagMessageParser {
        public int readGlobeIdFromNdefMessage(NdefMessage message);
    }

    ViewPager mViewPager;
    TabLayout mTabs;
    HomePagerAdapter mPagerAdapter;
    Toolbar mToolbar;
    ForegroundDispatcher mForegroundDispatcher;
    private NfcTagMessageParser tagParser;
    private boolean autoplayOnResume;
    NotifBarHider mNotifBarHider;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean receiverRegistered = false;
    private boolean norespawn          = false;

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
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                hideSoftKeyboard();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mTabs.setupWithViewPager(mViewPager);

        // mViewPager.setCurrentItem(0, false);

        int elevation = getResources().getDimensionPixelSize(R.dimen.tabbar_elevation);
        ViewCompat.setElevation(mTabs, elevation);
        // apply elevation to homeToolbar too, otherwise the tabbar hides it
        ViewCompat.setElevation(mToolbar, elevation);

        mForegroundDispatcher = new ForegroundDispatcher(this);
        tagParser = new ClassicNfcTextRecordParser();

        autoplayOnResume = mForegroundDispatcher.isNfcIntent(getIntent());

        mNotifBarHider = new NotifBarHider();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SHUTDOWN_INTENT)) {
                    norespawn = true;
                    HomeActivity.this.finish();
                }
            }
        };
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
    protected void onStop() {
        super.onStop();
        mNotifBarHider.enableStatusBarExpansion(this);
        if (!norespawn) {
            startService(new Intent(this, KioskService.class)); // start KioskService
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        stopService(new Intent(this, KioskService.class)); // start KioskService
        mNotifBarHider.preventStatusBarExpansion(this);

        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(SHUTDOWN_INTENT);
            registerReceiver(mBroadcastReceiver, filter);
            receiverRegistered = true;
            norespawn = false;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(mForegroundDispatcher.isNfcIntent(intent)) {
            loadFromNfcIntent(intent);
        }
    }

    @Override
    public void onBackPressed() {
        // Nothing - we want to disable back button
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog - to disable the long power button press
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
            // Caught
            return true;
        }
        return super.onKeyDown(keyCode, event);
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


    private void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        getPagerAdapter().updateCacheWithFragment(fragment);
        if (autoplayOnResume && fragment.getClass() == MapFragment.class) {
            loadFromNfcIntent(getIntent());
            autoplayOnResume = false;
        }
    }

    @Override
    public void onProjectSelected(Project p) {
        mViewPager.setCurrentItem(getPagerAdapter().getMapIndex(), true);
        getPagerAdapter().getMap().focusOnProject(p);
    }

    @Override
    public void onRouteSelected(Route route) {
        mViewPager.setCurrentItem(getPagerAdapter().getMapIndex(), true);
        getPagerAdapter().getMap().showRoute(route);
    }

    private HomePagerAdapter getPagerAdapter() {
        if (mPagerAdapter != null) {
            return mPagerAdapter;
        }
        mPagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        return mPagerAdapter;
    }

    private void loadFromNfcIntent(Intent intent) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null && rawMessages.length > 0) {
            NdefMessage message = (NdefMessage) rawMessages[0];
            int tableId = tagParser.readGlobeIdFromNdefMessage(message);
            // When a tag is detected by the ForegroundDispatcher, onNewIntent is called between
            // onPause and onResume (which pause and start the player). So we can hot-swap the
            // player source without bothering about threading or the progress refresher
            // Les tables de l'expo commenence à 0
            tableId += 1;
            Table table = ModelRepository.getInstance().getItemLibrary().findTable(tableId);

            getPagerAdapter().getMap().focusAndPlayTable(table);
            return;
        }
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
