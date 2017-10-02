package com.twitter.client.activities;

import android.content.Intent;
import android.net.Uri;
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
import com.twitter.client.R;
import com.twitter.client.TweetApplication;
import com.twitter.client.network.response.models.Tweet;
import com.twitter.client.transformations.CircularTransformation;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class TweetDetailActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.detail_toolbar_title);
        toolbar.setNavigationIcon(R.drawable.ic_back_nav);
        setSupportActionBar(toolbar);

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

        if (selectedTweet.getEntities() != null && selectedTweet.getEntities().getMedia() != null && !selectedTweet.getEntities().getMedia().isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(selectedTweet.getEntities().getMedia().get(0).getMediaUrl()))
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
                handleRetweetButtonClick();
            }
        });

        // fav
        favTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFavtweetButtonClick();
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

    private void handleRetweetButtonClick() {
        if (selectedTweet.isRetweeted()) {
            // user wants to undo retweet action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    TweetApplication.getTweetRestClient().postUnReTweetStatus(String.valueOf(selectedTweet.getId()), null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "UndoRetweet:: Undo retweet successful.");
                            Toast.makeText(TweetDetailActivity.this, "Undo retweet successful.", Toast.LENGTH_SHORT).show();

                            isTweetModified = true;
                            selectedTweet = Tweet.parseTweetFromJson(response);
                            toggleRetweetButton();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "UndoRetweet:: Failed to undo retweet. Error: " + responseString, throwable);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "UndoRetweet:: Failed to undo retweet. Error: " + errorResponse, throwable);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();

        } else {
            // user wants to perform retweet action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    TweetApplication.getTweetRestClient().postReTweetStatus(String.valueOf(selectedTweet.getId()), null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "Retweet:: Retweet successful.");
                            Toast.makeText(TweetDetailActivity.this, "Retweet successful.", Toast.LENGTH_SHORT).show();

                            isTweetModified = true;
                            selectedTweet = Tweet.parseTweetFromJson(response);
                            toggleRetweetButton();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "Retweet:: Failed to retweet. Error: " + responseString, throwable);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Retweet:: Failed to retweet. Error: " + errorResponse, throwable);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();
        }
    }

    private void handleFavtweetButtonClick() {
        if (selectedTweet.isFavorited()) {
            // user wants to undo favorite action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    RequestParams params = new RequestParams();
                    params.put("id", String.valueOf(selectedTweet.getId()));
                    TweetApplication.getTweetRestClient().postFavoritesDestroyStatus(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "UndoFavorite:: Undo favorite successful.");
                            Toast.makeText(TweetDetailActivity.this, "Undo favorite successful.", Toast.LENGTH_SHORT).show();

                            isTweetModified = true;
                            selectedTweet = Tweet.parseTweetFromJson(response);
                            toggleFavtweetButton();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "UndoFavorite:: Failed to undo retweet. Error: " + responseString, throwable);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "UndoFavorite:: Failed to undo retweet. Error: " + errorResponse, throwable);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();

        } else {
            // user wants to perform favorite action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    RequestParams params = new RequestParams();
                    params.put("id", String.valueOf(selectedTweet.getId()));
                    TweetApplication.getTweetRestClient().postFavoritesCreateStatus(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "Favorite:: Favorite action successful.");
                            Toast.makeText(TweetDetailActivity.this, "Favorite action successful.", Toast.LENGTH_SHORT).show();

                            isTweetModified = true;
                            selectedTweet = Tweet.parseTweetFromJson(response);
                            toggleFavtweetButton();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "Favorite:: Failed to favorite. Error: " + responseString, throwable);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Favorite:: Failed to favorite. Error: " + errorResponse, throwable);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();
        }
    }


    public void handleReplyTweetStatusAction(final String status) {
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                RequestParams params = new RequestParams();
                params.put("status", status);
                params.put("in_reply_to_status_id", String.valueOf(selectedTweet.getId()));
                TweetApplication.getTweetRestClient().postReplyToTweet(params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, "ReplyTweet:: Reply posted successfully.");
                        Toast.makeText(TweetDetailActivity.this, "Reply posted successfully.", Toast.LENGTH_SHORT).show();

                        isReplied = true;
                        repliedTweet = Tweet.parseTweetFromJson(response);
                        resetComposeText();
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
