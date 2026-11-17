package com.ccg.futurerealization.test;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** Separate test-APK process: reads a draft attachment, has no send operation or mail transport. */
public class FakeEmailActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        TextView status = new TextView(this); status.setPadding(32, 80, 32, 32); status.setTextSize(18); setContentView(status);
        try {
            Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            byte[] bytes;
            try (InputStream input = getContentResolver().openInputStream(uri); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                if (input == null) throw new IllegalArgumentException("Missing attachment");
                byte[] buffer = new byte[8192]; int read;
                while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
                bytes = output.toByteArray();
            }
            JSONObject data = new JSONObject(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("data");
            boolean readOnly = checkUriPermission(uri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == PackageManager.PERMISSION_DENIED;
            status.setText("测试邮件草稿（不会发送）\n附件读取成功\n账单数：" + data.getJSONArray("accounts").length() +
                    "\n目标数：" + data.getJSONArray("goals").length() + "\n记账时间：" + data.getJSONArray("accounts").getJSONObject(0).getString("time") +
                    "\n仅只读授权：" + readOnly);
        } catch (Exception error) { status.setText("附件读取失败：" + error.getClass().getSimpleName()); }
    }
}
