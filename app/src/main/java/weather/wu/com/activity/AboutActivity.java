package weather.wu.com.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import weather.wu.com.utils.Utility;
import weather.wu.com.weather.R;

public class AboutActivity extends AppCompatActivity {
   /* @BindView(R.id.toolbar)*/
    Toolbar mToolbar;
   // @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.tv_version)
    TextView mTvVersion;

    @BindView(R.id.bt_code)
    Button mBtCode;
    @BindView(R.id.bt_blog)
    Button mBtBlog;
    @BindView(R.id.bt_pay)
    Button mBtPay;
    @BindView(R.id.bt_share)
    Button mBtShare;
    @BindView(R.id.bt_update)
    Button mBtUpdate;
    @BindView(R.id.bt_bug)
    Button mBtBug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
       // StatusBarUtil.setImmersiveStatusBar(this);
       // StatusBarUtil.setImmersiveStatusBarToolbar(mToolbar,this);
        initView();
    }

    private void initView() {
        mToolbar = (Toolbar)findViewById(R.id.about_toolbar);
        mToolbarLayout= (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        setSupportActionBar(mToolbar);
      ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        mTvVersion.setText(String.format("当前版本: %s (Build %s)", Utility.getVersion(this), Utility.getVersionCode(this)));
        mToolbarLayout.setTitleEnabled(false);

        mToolbarLayout.setTitle(getString(R.string.app_name));
        mToolbar.setTitle(this.getString(R.string.app_name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @OnClick({ R.id.bt_code, R.id.bt_blog, R.id.bt_pay, R.id.bt_share, R.id.bt_bug, R.id.bt_update })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_code:
                goToHtml(getString(R.string.app_html));
                break;
            case R.id.bt_blog:
                goToHtml("http://blog.csdn.net/w77996?viewmode=contents");
                break;
            case R.id.bt_pay:
                Utility.copyToClipboard(getString(R.string.alipay), this);
                Toast.makeText(getApplicationContext(),"支付宝账号已粘贴到剪贴板",Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_txt));
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_app)));
                break;
            case R.id.bt_bug:
                goToHtml(getString(R.string.bugTableUrl));
                break;
            case R.id.bt_update:
              //  CheckVersion.checkVersion(this, true);
                Utility.copyToClipboard("1047239335", this);
                Toast.makeText(getApplicationContext(),"QQ号已粘贴到剪贴板",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void goToHtml(String url) {
       Uri uri = Uri.parse(url);   //指定网址
        /*Intent intent = new Intent(this,WebViewActivity.class);
        intent.putExtra("url", url);*/
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);           //指定Action
        intent.setData(uri);                            //设置Uri*/
        startActivity(intent);        //启动Activity
    }

    public static void launch(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }
}
