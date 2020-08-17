package my_app.example.my_adventures.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import my_app.example.my_adventures.MainActivity;

public class Pager_Adapter extends FragmentPagerAdapter {
    private MainActivity mainActivity;
    private int tabsNumber;
    public Home stories_fragment;
    public static boolean toRefresh = false;

    public Pager_Adapter(@NonNull FragmentManager fm, int behavior, int tabs, MainActivity mainActivity) {
        super(fm, behavior);
        this.mainActivity = mainActivity;
        this.tabsNumber = tabs;
        stories_fragment = new Home("stories", mainActivity);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Home("stories", mainActivity);
            case 1:
                return new Home("stories", true, mainActivity);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabsNumber;
    }
}
