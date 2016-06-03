package space.works.crosswordsolver;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appbrain.AppBrain;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class CrosswordConceptualSolverActivity extends Activity {
    Boolean upgraded = Boolean.FALSE;
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
            new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // handle error here
                System.out.println("FAILURE");
                // upgraded = Boolean.TRUE;
                allstuff();
            } else {
                // does the user have the premium upgrade?
                upgraded = inventory.hasPurchase("pro_upgrade");
                System.out.println("SUCCESS");
                allstuff();
            }
        }
    };
    Boolean iable = Boolean.FALSE;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                return;
            } else if (purchase.getSku().equals("pro_upgrade")) {
                allstuff();
            }
        }
    };
    IabHelper mHelper;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static InputStream getInputStreamFromUrl(String url) {
        InputStream content = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            content = response.getEntity().getContent();
        } catch (Exception e) {
            Log.e("[GET REQUEST]", "Network exception");
        }
        return content;
    }

    public void launchMain() {
        finish();
        startActivity(new Intent(CrosswordConceptualSolverActivity.this,
                CrosswordConceptualSolverActivity.class));
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String base64EncodedPublicKey;
        base64EncodedPublicKey = getString(R.string.base64);
        System.out.println(base64EncodedPublicKey);
        mHelper = new IabHelper(CrosswordConceptualSolverActivity.this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    iable = Boolean.FALSE;
                    System.out.println("SETUP X");
                } else {
                    // if successful
                    System.out.println("SETUP O");
                    iable = Boolean.TRUE;
                    if (iable) {
                        mHelper.queryInventoryAsync(mGotInventoryListener);
                    }
                }
            }
        });
        allstuff();
    }

    private void allstuff() {
        if (upgraded) {
            setContentView(R.layout.pro);
        } else {
            setContentView(R.layout.main);
        }
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            ActionBar actionBar = getActionBar();
            actionBar.hide();
        }

        //prevent network error on GB and ICS
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (!upgraded) {
            AppBrain.init(this);
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        final TextView info1 = (TextView) findViewById(R.id.info1);
        info1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(CrosswordConceptualSolverActivity.this)
                        .setTitle("How to enter clues")
                        .setMessage(
                                "- You may enter the clue as it appears in the " +
                                "puzzle.\n\n- For clues which include blanks, e.g. " +
                                "Helen of ____, an underscore _ can be used to represent" +
                                " the blanks"
                        )
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("AlertDialog", "Negative");
                            }
                        })
                        .show();
            }
        });

        final TextView ex1 = (TextView) findViewById(R.id.Ex1);
        ex1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText infoa = (EditText) findViewById(R.id.find);
                infoa.setText("");
            }
        });

        final TextView ex2 = (TextView) findViewById(R.id.Ex2);
        ex2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText infob = (EditText) findViewById(R.id.patt);
                infob.setText("");
            }
        });

        if (!upgraded) {
            final TextView pro = (TextView) findViewById(R.id.pro);
            pro.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (iable) {
                        mHelper.launchPurchaseFlow(CrosswordConceptualSolverActivity.this,
                                "pro_upgrade", 123, mPurchaseFinishedListener
                        );
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Sorry, your device does not support in-app purchases",
                                Toast.LENGTH_SHORT
                        );
                        toast.show();
                    }
                }
            });

            // more apps button
            final TextView reset = (TextView) findViewById(R.id.reset);
            reset.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AppBrain.getAds().showInterstitial(CrosswordConceptualSolverActivity.this);
                }
            });
        }

        final TextView info2 = (TextView) findViewById(R.id.info2);
        info2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(CrosswordConceptualSolverActivity.this)
                        .setTitle("How to enter patterns")
                        .setMessage("-If none of the characters are known," +
                                "you may enter a number representing " +
                                "the length of the word \n\n- Alternatively you may use " +
                                "question marks or fullstops (? or .) " +
                                "to represent each letter. E.g. 5, ?????, ..... are " +
                                "equivalent.\n\n-If you are certain of some letters, " +
                                "you enter them in the correct order as CAPITAL letters" +
                                ", e.g. ??R?E??? or 2R1E3. Note that these letters may" +
                                "ignored if better answers exist.")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("AlertDialog", "Negative");
                            }
                        })
                        .show();
            }
        });


        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isNetworkAvailable(getBaseContext())) {
                    Toast.makeText(getApplicationContext(), "No network connectivity!",
                            Toast.LENGTH_LONG).show();
                } else {
                    final String android_id = Settings.Secure.getString(getBaseContext()
                            .getContentResolver(), Settings.Secure.ANDROID_ID);
                    final EditText find = (EditText) findViewById(R.id.find);

                    String foo = find.getText().toString();

                    final EditText pattern = (EditText) findViewById(R.id.patt);
                    pattern.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    String bar = pattern.getText().toString();

                    //hide softkeyboard after submit
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE
                    );
                    imm.hideSoftInputFromWindow(pattern.getWindowToken(), 0);

                    if (pattern.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Pattern is mandatory!!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        new PostPost().execute(String.valueOf(foo), String.valueOf(bar),
                                String.valueOf(android_id));
                    }
                }
            }
        });

    }

    class PostPost extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog = new ProgressDialog(CrosswordConceptualSolverActivity.this);
        void show() {
            dialog.setMessage(
                    "Loading... This may take a few seconds if your connection is slow."
            );
            dialog.show();
        }

        void hide() {
            dialog.dismiss();
        }

        protected void onPreExecute() {
            show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            hide();
        }

        protected String doInBackground(String... params) {
            String url = getString(R.string.api);
            String uid = getString(R.string.uid);
            List<NameValuePair> paramas = new LinkedList<NameValuePair>();
            paramas.add(new BasicNameValuePair("text", String.valueOf(params[0])));
            paramas.add(new BasicNameValuePair("pattern", String.valueOf(params[1])));
            if (upgraded) {
                paramas.add(new BasicNameValuePair("uid", uid));
            } else {
                paramas.add(new BasicNameValuePair("uid", String.valueOf(params[2])));
            }
            String paramString = URLEncodedUtils.format(paramas, "utf-8");
            url += paramString;

            final InputStream test = getInputStreamFromUrl(url);
            runOnUiThread(new Runnable() {
                public void run() {
                    String total = null;
                    String results = "";
                    try {
                        total = IOUtils.toString(test);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final TextView mTextView = (TextView) findViewById(R.id.textView1);
                    mTextView.setShadowLayer(1, 0, 0, Color.BLACK);
                    mTextView.setText(total);

                    if (mTextView.getText().toString().length() > 0) {
                        Toast.makeText(getApplicationContext(), "Possible Solutions Found",
                                Toast.LENGTH_SHORT).show();
                        publishProgress(100);
                        //     return total;
                    } else {
                        Toast.makeText(getApplicationContext(), "No results",
                                Toast.LENGTH_SHORT).show();
                        publishProgress(100);
                        //return "potato";
                    }
                }
            });
            return "done";
        }
    }
}