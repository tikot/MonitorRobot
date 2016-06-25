package co.rytikov.monitorrobot.accounts;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    private static final String ACCOUNT_TYPE = "monitorrobot.rytikov.co";
    public static final String ACCOUNT_NAME = "monitor.robot.sync";

    private Authenticator mAuthenticator;

    public static Account getSyncAccount() {
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
