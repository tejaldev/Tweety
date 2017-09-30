package com.twitter.client.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.twitter.client.R;
import com.twitter.client.network.response.models.Tweet;
import com.twitter.client.transformations.CircularTransformation;

import java.util.List;


/**
 * Tweet recycler view adapter
 *
 * @author tejalpar
 */
public class TweetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TEXT_ONLY_VIEW_TYPE = 0; // articles that contains only text
    private static final int IMAGE_VIEW_TYPE = 1; // articles that contains thumbnail images

    private List<Tweet> tweetList;
    private TweetItemClickListener itemClickListener;

    public interface TweetItemClickListener {
        void onTweetItemClickListener(View view, Tweet selectedArticle);
    }

    public TweetAdapter(List<Tweet> tweetList, TweetItemClickListener itemClickListener) {
        this.tweetList = tweetList;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (tweetList.get(position).hasImage()) {
            return IMAGE_VIEW_TYPE;
        }
        return TEXT_ONLY_VIEW_TYPE;
    }

    /**
     * Inserts new tweets at the top of the list
     * @param newItems
     */
    public void setNewData(List<Tweet> newItems) {
        tweetList.addAll(0, newItems);
        this.notifyItemRangeChanged(0, newItems.size());
    }

    /**
     * Appends next page data at the bottom of list
     * @param moreItemList
     */
    public void setMoreData(List<Tweet> moreItemList) {
        tweetList.addAll(moreItemList);
        this.notifyItemInserted(moreItemList.size());
    }

    public void addNewItem(Tweet tweet) {
        tweetList.add(0, tweet);
        this.notifyItemInserted(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case IMAGE_VIEW_TYPE:
                View popularView = inflater.inflate(R.layout.tweet_image_row_layout, parent, false);

                //create view holder from above view
                viewHolder = new ImageTweetViewHolder(popularView);
                break;

            case TEXT_ONLY_VIEW_TYPE:
                default:
                    View textOnlyView = inflater.inflate(R.layout.tweet_text_row_layout, parent, false);

                    //create view holder from above view
                    viewHolder = new TextOnlyViewHolder(textOnlyView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        switch (getItemViewType(position)) {
            case IMAGE_VIEW_TYPE:
                ImageTweetViewHolder imageTweetViewHolder = (ImageTweetViewHolder) holder;
                setupImageTweetViewHolder(imageTweetViewHolder, position);
                break;

            case TEXT_ONLY_VIEW_TYPE:
            default:
                TextOnlyViewHolder textOnlyViewHolder = (TextOnlyViewHolder) holder;
                setupTextTweetViewHolder(textOnlyViewHolder, position);
                break;
        }
    }

    public class ImageTweetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleText;
        public TextView handleText;
        public TextView timeStampText;
        public TextView screenNameText;
        public ImageView mainImageView;
        public ImageView avatarImageView;

        public ImageTweetViewHolder(View rootView) {
            super(rootView);

            rootView.setOnClickListener(this);
            titleText = (TextView) rootView.findViewById(R.id.title_text);
            handleText = (TextView) rootView.findViewById(R.id.handle_text);
            timeStampText = (TextView) rootView.findViewById(R.id.timestamp_text);
            screenNameText = (TextView) rootView.findViewById(R.id.screen_name_text);
            mainImageView = (ImageView) rootView.findViewById(R.id.main_image);
            avatarImageView = (ImageView) rootView.findViewById(R.id.avatar_image);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onTweetItemClickListener(view, tweetList.get(getAdapterPosition()));
        }
    }

    public class TextOnlyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleText;
        public TextView handleText;
        public TextView timeStampText;
        public TextView screenNameText;
        public ImageView avatarImageView;

        public TextOnlyViewHolder(View rootView) {
            super(rootView);

            rootView.setOnClickListener(this);
            titleText = (TextView) rootView.findViewById(R.id.title_text);
            handleText = (TextView) rootView.findViewById(R.id.handle_text);
            timeStampText = (TextView) rootView.findViewById(R.id.timestamp_text);
            screenNameText = (TextView) rootView.findViewById(R.id.screen_name_text);
            avatarImageView = (ImageView) rootView.findViewById(R.id.avatar_image);
            // Can also make links clickable by setting setMovementMethod(LinkMovementMethod.getInstance())
            // In that case need to remove autoLink = "web" from xml
            // Refer https://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onTweetItemClickListener(view, tweetList.get(getAdapterPosition()));
        }
    }

    private void setupImageTweetViewHolder(ImageTweetViewHolder holder, int position) {
        Tweet tweet = tweetList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(Uri.parse(tweet.getEntities().getMedia().get(0).getMediaUrl()))
                .placeholder(R.drawable.placeholder_image)
                .into(holder.mainImageView);

        Glide.with(holder.itemView.getContext())
                .load(Uri.parse(tweet.getUser().getProfileImageUrl()))
                .bitmapTransform(new CircularTransformation(holder.itemView.getContext()))
                .placeholder(R.drawable.placeholder_image)
                .into(holder.avatarImageView);

        holder.titleText.setText(tweet.getText());
        holder.handleText.setText(tweet.getUser().getUserHandle());
        holder.timeStampText.setText(tweet.getRelativeTimeStamp());
        holder.screenNameText.setText(tweet.getUser().getScreenName());
    }

    private void setupTextTweetViewHolder(TextOnlyViewHolder holder, int position) {
        Tweet tweet = tweetList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(Uri.parse(tweet.getUser().getProfileImageUrl()))
                .bitmapTransform(new CircularTransformation(holder.itemView.getContext()))
                .placeholder(R.drawable.placeholder_image)
                .into(holder.avatarImageView);

        holder.titleText.setText(tweet.getText());
        holder.handleText.setText(tweet.getUser().getUserHandle());
        holder.timeStampText.setText(tweet.getRelativeTimeStamp());
        holder.screenNameText.setText(tweet.getUser().getScreenName());
    }
}
