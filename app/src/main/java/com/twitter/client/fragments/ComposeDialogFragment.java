package com.twitter.client.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.twitter.client.network.response.models.User;
import com.twitter.client.transformations.CircularTransformation;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * To compose new tweet
 *
 * @author tejalpar
 */
public class ComposeDialogFragment extends DialogFragment {
    private static String TAG = ComposeDialogFragment.class.getSimpleName();
    private static int CHAR_MAX_LIMIT = 140;
    public static final String ARG_IS_IMPLICIT = "IMPLICIT_INVOCATION";

    private int charCounter = 0;
    private TextView handleText;
    private EditText composeText;
    private TextView screenNameText;
    private TextView charCounterText;
    private ImageView avatarImageView;

    private static TweetPostCompletionListener postCompletionListener;

    public interface TweetPostCompletionListener {
        void onPostCompleted(Tweet newTweet);
        void onPostFailure(Throwable throwable);
    }

    public ComposeDialogFragment() {}

    public static ComposeDialogFragment newInstance(String dialogTitle, TweetPostCompletionListener listener, Intent implicitIntent) {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", dialogTitle);

        if (implicitIntent != null) {
            args.putBoolean(ARG_IS_IMPLICIT, true);
            args.putString(Intent.EXTRA_SUBJECT, implicitIntent.getStringExtra(Intent.EXTRA_SUBJECT));
            args.putString(Intent.EXTRA_TEXT, implicitIntent.getStringExtra(Intent.EXTRA_TEXT));
            args.putParcelable(Intent.EXTRA_STREAM, implicitIntent.getParcelableExtra(Intent.EXTRA_STREAM));
        } else {
            args.putBoolean(ARG_IS_IMPLICIT, false);
        }
        frag.setArguments(args);
        postCompletionListener = listener;
        return frag;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.compose_dialog_frag_layout, container);

        handleText = (TextView) view.findViewById(R.id.handle_text);
        screenNameText = (TextView) view.findViewById(R.id.screen_name_text);
        charCounterText = (TextView) view.findViewById(R.id.char_limit_counter_text);
        composeText = (EditText) view.findViewById(R.id.compose_edit_text);
        avatarImageView = (ImageView) view.findViewById(R.id.avatar_image);

        final ImageButton closeDialogButton = (ImageButton) view.findViewById(R.id.close_button);
        final Button saveTweetButton = (Button) view.findViewById(R.id.save_tweet);

        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });

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

        final Context context = view.getContext();
        saveTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComposedTweet(context, composeText.getText().toString());
            }
        });

        populateUI(view.getContext());
        return view;
    }

    public void postComposedTweet(final Context context, String status) {
        RequestParams params = new RequestParams();
        params.put("status", status);
        TweetApplication.getTweetRestClient().postNewTweet(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "postComposedTweet:: Tweet posted successfully.");
                Toast.makeText(context, "Tweet posted successfully.", Toast.LENGTH_LONG).show();
                postCompletionListener.onPostCompleted(getTweetFromResponse(response));
                dismissDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "postComposedTweet:: Failed to post tweet: " + responseString, throwable);
                Toast.makeText(context, "Failed to post tweet.", Toast.LENGTH_LONG).show();
                postCompletionListener.onPostFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "postComposedTweet:: Failed to post tweet: " + errorResponse, throwable);
                Toast.makeText(context, "Failed to post tweet.", Toast.LENGTH_LONG).show();
                postCompletionListener.onPostFailure(throwable);
            }
        });
    }

    private void dismissDialog() {
        dismiss();
    }

    private User getUserDetails() {
        return TweetApplication.getLoggedInUser();
    }

    private void populateUI(Context context) {
        User loggedInUser = getUserDetails();

        if (loggedInUser != null) {
            handleText.setText(loggedInUser.getUserHandle());
            screenNameText.setText(loggedInUser.getScreenName());

            Glide.with(context)
                    .load(Uri.parse(loggedInUser.getProfileImageUrl()))
                    .bitmapTransform(new CircularTransformation(context))
                    .placeholder(R.drawable.placeholder_image)
                    .into(avatarImageView);
        }

        Bundle bundle = getArguments();
        if (bundle.getBoolean(ARG_IS_IMPLICIT)) {
            // then pre-fill the text and title of the web page when composing a tweet
            String titleOfPage = bundle.getString(Intent.EXTRA_SUBJECT);
            String urlOfPage = bundle.getString(Intent.EXTRA_TEXT);
            composeText.setText(titleOfPage + " " + urlOfPage);
        }
    }

    private Tweet getTweetFromResponse(JSONObject response) {
        return Tweet.parseTweetFromJson(response);
    }
}
