package com.twitter.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.codepath.oauth.OAuthLoginActionBarActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.twitter.client.network.TweetClient;
import com.twitter.client.storage.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends OAuthLoginActionBarActivity<TweetClient> {
    private static String TAG = LoginActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}


	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	// OAuth authenticated successfully, launch primary authenticated activity
	// i.e Display application "homepage"
	@Override
	public void onLoginSuccess() {
        fetchLoggedInUserDetails();

		Intent i = new Intent(this, TweetListActivity.class);
		if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
			// this is implicitly invoked intent so pass intent data to TweetListActivity
			i.setAction(Intent.ACTION_SEND);
			i.setType(getIntent().getType());
			i.putExtras(getIntent());
		}
		startActivity(i);
	}

	// OAuth authentication flow failed, handle the error
	// i.e Display an error dialog or toast
	@Override
	public void onLoginFailure(Exception e) {
		Toast.makeText(this, "Login failed." + e.getMessage(), Toast.LENGTH_LONG).show();
		e.printStackTrace();
	}

	// Click handler method for the button used to start OAuth flow
	// Uses the client to initiate OAuth authorization
	// This should be tied to a button used to login
	public void loginToRest(View view) {
		getClient().connect();
	}

    private void fetchLoggedInUserDetails() {
        TweetApplication.getTweetRestClient().getUserDetails(null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "fetchLoggedInUserDetails:: Response received successfully.");
                TweetApplication.setLoggedInUser(User.parseUserInfoFromJson(response));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "fetchLoggedInUserDetails:: Response received successfully.");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "fetchLoggedInUserDetails:: Failed to receive api response: " + responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "fetchLoggedInUserDetails:: Failed to receive api response: " + errorResponse, throwable);
            }
        });
    }
}
