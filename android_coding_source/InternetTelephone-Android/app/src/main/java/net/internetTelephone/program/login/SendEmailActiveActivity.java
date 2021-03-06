package net.internetTelephone.program.login;

import com.loopj.android.http.RequestParams;

import net.internetTelephone.program.R;
import net.internetTelephone.program.common.Global;
import net.internetTelephone.program.common.base.MyJsonResponse;
import net.internetTelephone.program.common.network.MyAsyncHttpClient;
import net.internetTelephone.program.model.RequestData;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.json.JSONObject;

@EActivity(R.layout.activity_base_send_email)
public class SendEmailActiveActivity extends SendEmailBaseActivity {

    @AfterViews
    protected final void initResetPassword() {
        loginButton.setText("重发激活邮件");
    }

    @Click
    protected final void loginButton() {
        if (!isEnterSuccess()) {
            return;
        }

        String url = Global.HOST_API + "/activate";
        RequestParams params = new RequestParams();
        params.put("email", getEmail());
        params.put("j_captcha", getValify());
        MyAsyncHttpClient.get(this, new RequestData(url, params), new MyJsonResponse(SendEmailActiveActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                showMiddleToast("重置密码邮件已经发送，请尽快去邮箱查收");
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);

                downloadValifyPhoto();
            }
        });
    }
}
