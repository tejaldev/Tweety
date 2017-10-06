package com.twitter.client.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.twitter.client.R;
import com.twitter.client.TweetApplication;
import com.twitter.client.adapters.TweetStatusActionHelper;
import com.twitter.client.storage.TweetDatabaseHelper;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.transformations.CircularTransformation;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class TweetDetailActivity extends AppCompatActivity implements TweetStatusActionHelper.OnStatusUpdatedListener {
    public static String TAG = TweetDetailActivity.class.getSimpleName();
    public static final String ARG_SELECTED_TWEET = "SELECTED_TWEET";
    public static final String ARG_SELECTED_TWEET_POS = "SELECTED_TWEET_POS";
    public static final String ARG_IS_TWEET_MODIFIED = "IS_MODIFIED";
    public static final String ARG_IS_TWEET_REPLIED = "IS_REPLIED";
    public static final String ARG_REPLIED_TWEET_INFO = "REPLIED_TWEET_INFO";

    private static int CHAR_MAX_LIMIT = 140;

    private int position;
    private int charCounter = 0;

    private boolean isReplied;
    private boolean isTweetModified;
    private String composeHintText;

    private Tweet selectedTweet;
    private Tweet repliedTweet;

    private TextView titleText;
    private TextView handleText;
    private EditText composeText;
    private TextView screenNameText;
    private TextView charCounterText;
    private ImageView mainImageView;
    private ImageView avatarImageView;
    private Button saveTweetReplyButton;
    private ImageButton favTweetButton;
    private ImageButton replyTweetButton;
    private ImageButton retweetTweetButton;
    private TweetStatusActionHelper statusActionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.detail_toolbar_title);
        toolbar.setNavigationIcon(R.drawable.ic_back_nav);
        setSupportActionBar(toolbar);
        statusActionHelper = new TweetStatusActionHelper(this);

        // get selectedTweet
        Intent intent = getIntent();
        selectedTweet = Parcels.unwrap(intent.getParcelableExtra(ARG_SELECTED_TWEET));
        position = intent.getIntExtra(ARG_SELECTED_TWEET_POS, -1);
        composeHintText = getString(R.string.detail_compose_hint_text, selectedTweet.getUser().getScreenName());

        titleText = (TextView) findViewById(R.id.title_text);
        handleText = (TextView) findViewById(R.id.handle_text);
        screenNameText = (TextView) findViewById(R.id.screen_name_text);
        mainImageView = (ImageView) findViewById(R.id.main_image);
        avatarImageView = (ImageView) findViewById(R.id.avatar_image);
        favTweetButton = (ImageButton) findViewById(R.id.fav_tweet_button);
        replyTweetButton = (ImageButton) findViewById(R.id.reply_tweet_button);
        retweetTweetButton = (ImageButton) findViewById(R.id.re_tweet_button);
        charCounterText = (TextView) findViewById(R.id.char_limit_counter_text);
        composeText = (EditText) findViewById(R.id.compose_edit_text);
        saveTweetReplyButton = (Button) findViewById(R.id.save_tweet_reply);

        setupComposeTextEventListeners();
        setupSaveTweetButtonListener();
        setupActionButtonListeners();
        bindTweetDetailsToView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the toolbar NavigationIcon as up/home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onFinish();
        super.onBackPressed();
    }

    private void onFinish() {
        // send result back to calling activity
        Intent data = new Intent();
        data.putExtra(ARG_SELECTED_TWEET, Parcels.wrap(selectedTweet));
        data.putExtra(ARG_SELECTED_TWEET_POS, position);
        data.putExtra(ARG_IS_TWEET_REPLIED, isReplied);
        data.putExtra(ARG_IS_TWEET_MODIFIED, isTweetModified);
        if (isReplied) {
            data.putExtra(ARG_REPLIED_TWEET_INFO, Parcels.wrap(repliedTweet));
        }
        setResult(RESULT_OK, data);
    }

    private void bindTweetDetailsToView() {
        titleText.setText(selectedTweet.getText());
        handleText.setText(selectedTweet.getUser().getUserHandle());
        screenNameText.setText(selectedTweet.getUser().getScreenName());
        composeText.setHint(composeHintText);

        Glide.with(this)
                .load(Uri.parse(selectedTweet.getUser().getProfileImageUrl()))
                .bitmapTransform(new CircularTransformation(this))
                .placeholder(R.drawable.placeholder_image)
                .into(avatarImageView);

        if (selectedTweet != null && selectedTweet.getMediaList() != null && !selectedTweet.getMediaList().isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(selectedTweet.getMediaList().get(0).getMediaUrl()))
                    .placeholder(R.drawable.placeholder_image)
                    .into(mainImageView);
        }

        // bind image
        toggleRetweetButton();
        toggleFavtweetButton();
    }

    private void toggleRetweetButton() {
        if (selectedTweet.isRetweeted()) {
            retweetTweetButton.setImageResource(R.drawable.ic_re_tweet_enabled);
        } else {
            retweetTweetButton.setImageResource(R.drawable.ic_re_tweet);
        }
    }

    private void toggleFavtweetButton() {
        if (selectedTweet.isFavorited()) {
            favTweetButton.setImageResource(R.drawable.ic_fav_tweet_enabled);
        } else {
            favTweetButton.setImageResource(R.drawable.ic_fav_tweet);
        }
    }

    private void setupActionButtonListeners() {
        // reply
        replyTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeText.callOnClick();
            }
        });

        // retweet
        retweetTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusActionHelper.handleRetweetStatusAction(selectedTweet, position);
                //handleRetweetButtonClick();
            }
        });

        // fav
        favTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusActionHelper.handleFavoritedStatusAction(selectedTweet, position);
                //handleFavtweetButtonClick();
            }
        });
    }

    private void setupComposeTextEventListeners() {
        composeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    charCounter = 0;
                } else {
                    charCounter = CHAR_MAX_LIMIT - s.length();
                }
                charCounterText.setText(String.valueOf(charCounter));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        composeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeText.getText().toString().isEmpty()) {
                    composeText.setText(selectedTweet.getUser().getUserHandle());
                }
                composeText.setCursorVisible(true);
            }
        });
    }

    private void setupSaveTweetButtonListener() {
        saveTweetReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!composeText.getText().toString().isEmpty()) {
                    handleReplyTweetStatusAction(composeText.getText().toString());
                    return;
                }
                Toast.makeText(TweetDetailActivity.this, "Nothing to post.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetComposeText() {
        composeText.getText().clear();
    }

    @Override
    public void onRetweetActionSuccess(Tweet tweet, boolean isUndoAction, int adapterPosition) {
        Tweet tweetToUpdate;
        if (!isUndoAction) {
            //
            tweetToUpdate = selectedTweet;
            tweetToUpdate.setRetweeted(tweet.isRetweeted());
            tweetToUpdate.setRetweetCount(tweet.getRetweetCount());

        } else {
            // refresh tweet info in adapter
            int count = tweet.getRetweetCount();
            tweet.setRetweetCount(--count);
            tweet.setRetweeted(false);
            tweetToUpdate = tweet;
        }
        isTweetModified = true;
        selectedTweet = tweetToUpdate;
        toggleRetweetButton();
        Toast.makeText(this, "Retweet status updated.", Toast.LENGTH_SHORT).show();

        TweetDatabaseHelper.getInstance().saveTweetToDB(tweetToUpdate, new Transaction.Success() {
            @Override
            public void onSuccess(@NonNull Transaction transaction) {
                Log.d(TAG, "Tweets saved to database.");
            }
        }, new Transaction.Error() {
            @Override
            public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
            }
        });
    }

    @Override
    public void onRetweetActionFailure(String error, boolean isUndoAction) {
        Log.e(TAG, "Failed to update retweet status. Error: " + error);
    }

    @Override
    public void onFavoritedActionSuccess(Tweet tweet, boolean isUndoAction, int adapterPosition) {
        isTweetModified = true;
        selectedTweet = tweet;
        toggleFavtweetButton();
        Toast.makeText(this, "Favorite status updated.", Toast.LENGTH_SHORT).show();

        TweetDatabaseHelper.getInstance().saveTweetToDB(tweet, new Transaction.Success() {
            @Override
            public void onSuccess(@NonNull Transaction transaction) {
                Log.d(TAG, "Tweets saved to database.");
            }
        }, new Transaction.Error() {
            @Override
            public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
            }
        });
    }

    @Override
    public void onFavoritedActionFailure(String error, boolean isUndoAction) {
        Log.e(TAG, "Failed to update favorite status. Error: " + error);
    }

    public void handleReplyTweetStatusAction(final String status) {
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                RequestParams params = new RequestParams();
                params.put("status", status);
                params.put("in_reply_to_status_id", String.valueOf(selectedTweet.getTweetId()));
                TweetApplication.getTweetRestClient().postReplyToTweet(params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, "ReplyTweet:: Reply posted successfully.");
                        Toast.makeText(TweetDetailActivity.this, "Reply posted successfully.", Toast.LENGTH_SHORT).show();

                        isReplied = true;
                        repliedTweet = new Tweet(response);
                        resetComposeText();

                        TweetDatabaseHelper.getInstance().saveTweetToDB(repliedTweet, new Transaction.Success() {
                            @Override
                            public void onSuccess(@NonNull Transaction transaction) {
                                Log.d(TAG, "Tweets saved to database.");
                            }
                        }, new Transaction.Error() {
                            @Override
                            public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                                Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e(TAG, "ReplyTweet:: Failed to post tweet reply. Error: " + responseString, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, "ReplyTweet:: Failed to post tweet reply. Error: " + errorResponse, throwable);
                    }
                });
            }
        };
        new Thread(runnableCode).run();
    }
}
