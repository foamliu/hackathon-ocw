package org.hackathon_ocw.androidclient;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * Created by dianyang on 2016/3/12.
 */
public class PageFragmentAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "简介", "评论", "相关课程" };
    private Context context;

    public PageFragmentAdapter(FragmentManager fm, Context context){
        super(fm);
        this.context = context;
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
                TabComment tabComment = new TabComment();
                return tabComment;
            default:
                return PageFragment.newInstance(position + 1);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
