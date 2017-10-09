package com.twitter.client.adapters;

import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.twitter.client.R;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.transformations.CircularTransformation;
import com.twitter.client.utils.PatternEditableBuilder;

import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;


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
        void onTweetItemClickListener(View view, Tweet selectedTweet, int position);
        void onReplyClickListener(View view, Tweet selectedTweet, int position);
        void onRetweetClickListener(View view, Tweet selectedTweet, int position);
        void onFavoriteClickListener(View view, Tweet selectedTweet, int position);
        void onAvatarImageClickListener(View view, Tweet selectedTweet, int position);
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

    public Tweet getItemAt(int position) {
        return tweetList.get(position);
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

    /**
     * Inserts a new tweet at the top of the list
     * @param tweet
     */
    public void addNewItem(Tweet tweet) {
        tweetList.add(0, tweet);
        this.notifyItemInserted(0);
    }

    /**
     * Updates a tweet
     * @param tweet
     */
    public void updateData(int position, Tweet tweet) {
        tweetList.set(position, tweet);
        this.notifyItemChanged(position);
    }

    /**
     * Replaces adapter items with new data
     * @param newItems
     */
    public void replaceItems(List<Tweet> newItems) {
        tweetList.clear();
        tweetList.addAll(newItems);
        this.notifyItemRangeChanged(0, newItems.size());
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
                    viewHolder = new TweetViewHolder(textOnlyView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        switch (getItemViewType(position)) {
            case IMAGE_VIEW_TYPE:
                ImageTweetViewHolder imageTweetViewHolder = (ImageTweetViewHolder) holder;
                setupTweetViewHolder(imageTweetViewHolder, position);

                // setup additional fields
                Glide.with(holder.itemView.getContext())
                        .load(Uri.parse(tweetList.get(position).getMediaList().get(0).getMediaUrl()))
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageTweetViewHolder.mainImageView);
                break;

            case TEXT_ONLY_VIEW_TYPE:
            default:
                TweetViewHolder textOnlyViewHolder = (TweetViewHolder) holder;
                setupTweetViewHolder(textOnlyViewHolder, position);
                break;
        }
    }

    public class ImageTweetViewHolder extends TweetViewHolder implements View.OnClickListener {
        @BindView(R.id.main_image) ImageView mainImageView;

        public ImageTweetViewHolder(View rootView) {
            super(rootView);

            // Bind views using ButterKnife
            ButterKnife.bind(this, rootView);
            rootView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onTweetItemClickListener(view, tweetList.get(getAdapterPosition()), getAdapterPosition());
        }
    }

    private void setupTweetViewHolder(TweetViewHolder holder, int position) {
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
        holder.retweetCountText.setText(String.valueOf(tweet.getRetweetCount()));
        holder.favoriteCountText.setText(String.valueOf(tweet.getFavoriteCount()));

        bindRetweetButtonRes(holder, tweet);
        bindFavoriteButtonRes(holder, tweet);
        setupClickableSpanForHashTagsnUserName(holder.titleText, position);
    }

    private void bindRetweetButtonRes(TweetViewHolder holder, Tweet tweet) {
        if (tweet.isRetweeted()) {
            holder.retweetTweetButton.setImageResource(R.drawable.ic_re_tweet_enabled);
        } else {
            holder.retweetTweetButton.setImageResource(R.drawable.ic_re_tweet);
        }
    }

    private void bindFavoriteButtonRes(TweetViewHolder holder, Tweet tweet) {
        if (tweet.isFavorited()) {
            holder.favTweetButton.setImageResource(R.drawable.ic_fav_tweet_enabled);
        } else {
            holder.favTweetButton.setImageResource(R.drawable.ic_fav_tweet);
        }
    }

    private void setupClickableSpanForHashTagsnUserName(final TextView titleText, final int position) {
        // Style clickable spans based on pattern
        new PatternEditableBuilder().
                addPattern(Pattern.compile("\\@(\\w+)"), ContextCompat.getColor(titleText.getContext(), R.color.colorAccent),
                        new PatternEditableBuilder.SpannableClickedListener() {
                            @Override
                            public void onSpanClicked(String text) {
                                itemClickListener.onAvatarImageClickListener(titleText, tweetList.get(position), position);
                            }
                        })
        .addPattern(Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(titleText.getContext(), R.color.colorAccent),
                new PatternEditableBuilder.SpannableClickedListener() {
                    @Override
                    public void onSpanClicked(String text) {
                        itemClickListener.onAvatarImageClickListener(titleText, tweetList.get(position), position);
                    }
                        }).into(titleText);

    }

    public class TweetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.title_text) TextView titleText;
        @BindView(R.id.handle_text) TextView handleText;
        @BindView(R.id.timestamp_text) TextView timeStampText;
        @BindView(R.id.screen_name_text) TextView screenNameText;
        @BindView(R.id.re_tweet_count) TextView retweetCountText;
        @BindView(R.id.fav_tweet_count) TextView favoriteCountText;
        @BindView(R.id.avatar_image) ImageView avatarImageView;
        @BindView(R.id.fav_tweet_button) ImageButton favTweetButton;
        @BindView(R.id.reply_tweet_button) ImageButton replyTweetButton;
        @BindView(R.id.re_tweet_button) ImageButton retweetTweetButton;

        public TweetViewHolder(View rootView) {
            super(rootView);

            // Bind views using ButterKnife
            ButterKnife.bind(this, rootView);
            rootView.setOnClickListener(this);

            avatarImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onAvatarImageClickListener(v, tweetList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            setupActionButtonListeners();

            // Can also make links clickable by setting setMovementMethod(LinkMovementMethod.getInstance())
            // In that case need to remove autoLink = "web" from xml
            // Refer https://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onTweetItemClickListener(view, tweetList.get(getAdapterPosition()), getAdapterPosition());
        }

        private void setupActionButtonListeners() {
            // reply
            replyTweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onReplyClickListener(v, tweetList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            // retweet
            retweetTweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onRetweetClickListener(v, tweetList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            // fav
            favTweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onFavoriteClickListener(v, tweetList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }
}
