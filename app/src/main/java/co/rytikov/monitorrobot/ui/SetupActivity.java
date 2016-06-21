package co.rytikov.monitorrobot.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import co.rytikov.monitorrobot.R;

public class SetupActivity extends TheActivity {

    private static final String PREF_NAME = "co.rytikov.monitorrobot";
    private static final String PREF_API_KEY = "UPTIME_API_KEY";

    @BindView(R.id.api_key) TextView apiTextView;
    @BindString(R.string.api_required) String requiredError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }

    @OnClick(R.id.next)
    public void next() {
        CharSequence api = apiTextView.getText();

        if (api.length() == 0) {
            apiTextView.setError(getString(R.string.api_required));
            return;
        }

        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString(PREF_API_KEY, api.toString()).commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
