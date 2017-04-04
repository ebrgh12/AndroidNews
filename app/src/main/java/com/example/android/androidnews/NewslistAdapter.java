package com.example.android.androidnews;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
public class NewslistAdapter extends ArrayAdapter<News> {
    List<News> newsList = new ArrayList<>();
        public NewslistAdapter(Activity context, List<News> newses) {
         super(context, 0, newses);
        newsList = newses;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_view, parent, false);
        }
        // Find the newslist at the given position in the list of news
        News currentNews = getItem(position);
        // Find the TitleView
        TextView titleView = (TextView) listItemView.findViewById(R.id.TitleView);
        // Display the title
        titleView.setText(currentNews.getmTitle());
        // Find the DateView
        TextView dateView = (TextView) listItemView.findViewById(R.id.DateView);
        // Display the date
       dateView.setText(currentNews.getmDate());
        // Find the AuthorView
        TextView authorView = (TextView) listItemView.findViewById(R.id.AuthorView);
        authorView.setText(currentNews.getmAuthor());
        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }
    @Override
    public int getCount() {
        return newsList.size();
    }
}

