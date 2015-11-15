package bteamdevelopment.qrapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    // Declare Variable
    Button logout, createQR;
    List<ParseObject> ob;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the view
        setContentView(R.layout.activity_main);

        // Retrieve current user from Parse.com
        ParseUser currentUser = ParseUser.getCurrentUser();

        // Convert currentUser into String
        String struser = currentUser.getUsername().toString();

        // Locate TextView in welcome.xml
        TextView txtuser = (TextView) findViewById(R.id.txtuser);

        // Set the currentUser String into TextView
        txtuser.setText("You are logged in as " + struser);

        // Locate Button in welcome.xml
        logout = (Button) findViewById(R.id.logout);
        createQR = (Button) findViewById(R.id.createQR);

        createQR.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, QRActivity.class);
                startActivity(intent);
            }
        });

        // Logout Button Click Listener
        logout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Logout current user
                ParseUser.logOut();
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // On Resume Reload ListView (NOT WORKING)
    @Override
    public void onResume() {
        super.onResume();
        new RemoteDataTask().execute();
    }

    // AsyncTask is designed to be a helper class around Thread and Handler and does not constitute a generic threading framework.
    // AsyncTasks should ideally be used for short operations (a few seconds at the most.)
    // RemoteDataTask AsyncTask
    // Use AsyncTask to get loop through the contactData table on parse.com and create a new contact based on results
    public class RemoteDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            // Create New Array List of Objects from Parse.com
            try {
                // Query contactData Table on Parse.com
                ParseQuery<ParseObject> query = new ParseQuery<>("qrData");
                ob = query.find();
                // For each object in contactData table
                for (ParseObject qrData : ob) {

                    // PULL DATA FROM PARSE.COM
                }
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        // On Post setAdapter and Populate ListView
        @Override
        public void onPostExecute(Void result) {

            // SET ADAPTER AND DISPLAY RESULTS
        }
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                // Set read/write Access
                ParseACL defaultACL = new ParseACL();
                defaultACL.setPublicReadAccess(true);
                defaultACL.setPublicWriteAccess(true);
                ParseACL.setDefaultACL(defaultACL, true);

                // Add Contents to Parse.com Database
                ParseObject qrData = new ParseObject("qrData");

                qrData.put("qrCode", contents);

                qrData.saveInBackground();

                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();

                Intent sendMessage = new Intent(MainActivity.this, MessageActivity.class);
                sendMessage.putExtra("qrCode", contents);
                startActivity(sendMessage);
            }
        }
    }
}
