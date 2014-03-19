//Dylan Milligan 2013

package com.example.tumblrapi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.PhotoPost;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String REQUEST_TOKEN_URL = "https://www.tumblr.com/oauth/request_token";
	private static final String ACCESS_TOKEN_URL = "https://www.tumblr.com/oauth/access_token";
	private static final String AUTH_URL = "https://www.tumblr.com/oauth/authorize";

	// Get these from Tumblr app registration
	private static final String CONSUMER_KEY = "enter your tumblr app key";
	private static final String CONSUMER_SECRET = "enter your tumblr app key";

	// Set the callback url in the manifest. There are a few more things you
	// must include in the manifest, so check it!
	private static final String CALLBACK_URL = "example://ok"; // for this
																// example, it
																// would be
																// "example://ok"
	private static final String FILE_LOCATION = "/DCIM/";

	/*
	 * 
	 * c0tlpoHkfSMXdor6onmVY88J4GfhxvWZR7maZFyTT99X1MB2tISecret Key:
	 * WqM2G3Tk0fe4WVlAet4aqILK36Y6zBkXUkh8UfusuOHh5qEXQO
	 */
	String authUrl;

	CommonsHttpOAuthConsumer consumer;
	CommonsHttpOAuthProvider provider;

	SharedPreferences preferences;

	// Just put a listView in the activity_main
	// private ListView listView;
	private Button buttonTumb;
	protected Context context;
	protected CharSequence token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		preferences = this.getSharedPreferences("MyPreferences",
				Context.MODE_PRIVATE);
		buttonTumb = (Button) findViewById(R.id.buttontumb);
		if (!preferences.getString("accessToken", "").isEmpty()) {
			buttonTumb.setBackgroundResource(R.drawable.tumblr_alt_connected);
			token = preferences.getString("accessToken", "");
		}

		buttonTumb.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				buttonTumb.setBackgroundResource(R.drawable.tumblr_alt);
				SharedPreferences.Editor editor = preferences
						.edit();
				editor.putString("accessToken",
						"");
				editor.commit();
				return false;
			}
		}) ;
		
		buttonTumb.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// To get the oauth token after the user has granted permissions

				if (preferences.getString("accessToken", "").isEmpty()) {
					consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
							CONSUMER_SECRET);

					provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL,
							ACCESS_TOKEN_URL, AUTH_URL);

					SharedPreferences.Editor editor = preferences.edit();
					editor.putBoolean("gettingTokens", true);
					editor.commit();

					Thread thread = new Thread() {
						@Override
						public void run() {
							try {
								authUrl = provider.retrieveRequestToken(
										consumer, CALLBACK_URL);

								SharedPreferences.Editor editor = preferences
										.edit();
								editor.putString("requestToken",
										consumer.getToken());
								editor.putString("requestSecret",
										consumer.getTokenSecret());
								editor.commit();

								startActivity(new Intent(
										"android.intent.action.VIEW", Uri
												.parse(authUrl)));

							} catch (OAuthMessageSignerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OAuthNotAuthorizedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OAuthExpectationFailedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OAuthCommunicationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					thread.start();
				} else {
					if (!preferences.getString("accessToken", "").isEmpty()) {
						token=preferences.getString("accessToken", "");
						Toast.makeText(context, token, Toast.LENGTH_LONG).show();
						System.out.println("token exists"
								+ preferences.getString("accessToken", ""));
					}
				
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		String theToken = preferences.getString("requestToken", "");
		String theSecret = preferences.getString("requestSecret", "");
		boolean needsQuery = preferences.getBoolean("gettingTokens", false);
		if (!(theToken.equals("") || theSecret.equals("")) && needsQuery) {
			Uri uri = this.getIntent().getData();
			String token;
			try {
				token = uri.getQueryParameter(OAuth.OAUTH_TOKEN);
				System.out.println("OATH Token" + token);
				buttonTumb
						.setBackgroundResource(R.drawable.tumblr_alt_connected);

				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("accessToken", token);
				editor.commit();

			} catch (NullPointerException e) {
				Log.e("DrawLog", "Registration not complete");
				return;
			}
			final String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

			// The consumer object was lost because the browser got into
			// foreground, need to instantiate it again with your apps token and
			// secret.
			consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
					CONSUMER_SECRET);

			String rToken = preferences.getString("requestToken", "");
			String rSecret = preferences.getString("requestSecret", "");

			// Set the requestToken and the tokenSecret that you got earlier by
			// calling retrieveRequestToken.
			consumer.setTokenWithSecret(rToken, rSecret);

			// The provider object is lost, too, so instantiate it again.
			provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL,
					ACCESS_TOKEN_URL, AUTH_URL);
			// Now that's really important. Because you don't perform the
			// retrieveRequestToken method at this moment, the OAuth method is
			// not detected automatically (there is no communication with
			// Tumblr). So, the default is 1.0 which is wrong because the
			// initial request was performed with 1.0a.
			provider.setOAuth10a(true); // WHAT??!

			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						provider.retrieveAccessToken(consumer, verifier);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString("myToken", consumer.getToken());
						editor.putString("mySecret", consumer.getTokenSecret());
						editor.putBoolean("gettingTokens", false);
						editor.commit();
						handler.sendEmptyMessage(0);
					} catch (OAuthMessageSignerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthNotAuthorizedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			thread.start();

		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			postPicture("img.png");
		}
	};

	public void postPicture(final String name) {
		final String theToken = preferences.getString("myToken", "");
		final String theSecret = preferences.getString("mySecret", "");
		final JumblrClient client = new JumblrClient(CONSUMER_KEY,
				CONSUMER_SECRET);
		client.setToken(theToken, theSecret);

		// Have the user pick one of their blogs to post to.
//		final String blog = preferences.getString("selectedBlog", "");
//		if (blog.equals("")) {
//			Toast.makeText(
//					this,
//					"Please go to settings (top right corner) and select a blog to publish to.",
//					Toast.LENGTH_LONG).show();
//		} else {
			Thread thread = new Thread() {
				@Override
				public void run() {
					String root_sd = Environment.getExternalStorageDirectory()
							.toString();
					File file = new File(root_sd + "/DCIM/" + name);

					PhotoPost post;
					try {
						post = client.newPost("", PhotoPost.class);
						post.setData(file);
						post.save();

					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NullPointerException e) {
						Log.d("DrawLog", "null pointer wtf");
					}

				}
			};
			thread.start();
			Toast.makeText(
					this,
					"Now posting in the background. It may take a few seconds to appear on your blog.",
					Toast.LENGTH_LONG).show();
//		}
	}

}
