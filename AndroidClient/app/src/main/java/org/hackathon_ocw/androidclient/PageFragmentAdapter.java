package org.hackathon_ocw.androidclient;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * Created by dianyang on 2016/3/12.
 */
public class PageFragmentAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "简介", "笔记"};
    private Context context;
    public TabComment tabComment;

    public PageFragmentAdapter(FragmentManager fm, Context context){
        super(fm);
        this.context = context;
    }

    public TabComment getTabComment(){
        return tabComment;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                TabDescription tabDescription = new TabDescription();
                return tabDescription;
            case 1:
                tabComment = new TabComment();
                return tabComment;
            default:
                return new TabDescription();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
