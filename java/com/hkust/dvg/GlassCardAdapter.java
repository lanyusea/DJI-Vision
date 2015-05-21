package com.hkust.dvg;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xufang on 14/11/14.
 */
public class GlassCardAdapter extends CardScrollAdapter {

    private Context mContext;
    private List<Card> mCards = new ArrayList<Card>();
    private boolean mShowingFootnotes = true;

    public GlassCardAdapter(Context context) {
        mContext = context;
        createCards();
    }

    @Override
    public int getPosition(Object item) {
        return mCards.indexOf(item);
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Object getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return Card.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position){
        return mCards.get(position).getItemViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mCards.get(position).getView(convertView, parent);
    }

    public void toggleFootnotes() {
        mShowingFootnotes = !mShowingFootnotes;
        mCards.clear();
        createCards();
        notifyDataSetChanged();
    }

    private void createCards() {

        Card card = new Card(mContext);
        card.setText("One");
        if (mShowingFootnotes) {
            card.setFootnote("Wave right");
        }
        mCards.add(card);

        card = new Card(mContext);
        card.setText("Two");
        if (mShowingFootnotes) {
            card.setFootnote("Wave left/right");
        }
        mCards.add(card);

        card = new Card(mContext);
        card.setText("Three");
        if (mShowingFootnotes) {
            card.setFootnote("Wave left");
        }
        mCards.add(card);
    }

    public void addCard(String text)
    {
        Card card = new Card(mContext);
        card.setText(text);
        mCards.add(0,card);
        notifyDataSetChanged();
    }

    public void updateBatteryInfo(String text)
    {
        Card card = new Card(mContext);
        card.setText(text);
        mCards.remove(0);
        mCards.add(0,card);
        notifyDataSetChanged();
    }


}
