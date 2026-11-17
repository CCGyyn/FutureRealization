package com.ccg.futurerealization.backup;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Local backup attachment plus a mail draft. The mail app owns the actual send operation. */
public final class EmailBackup {
    private EmailBackup() { }

    public static Prepared prepare(Context context) throws IOException {
        byte[] bytes = LedgerBackupStore.exportLedger(context);
        LedgerBackup backup = LedgerBackup.decode(bytes);
        File directory = new File(context.getCacheDir(), "backup-email");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("无法创建备份附件目录");
        // Retain recent attachments so returning to a mail draft does not immediately break them.
        long cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
        File[] oldFiles = directory.listFiles();
        if (oldFiles != null) for (File old : oldFiles) {
            if (old.isFile() && old.getName().startsWith("FutureRealization-backup-") && old.lastModified() < cutoff) old.delete();
        }
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new java.util.Date());
        File file = File.createTempFile("FutureRealization-backup-" + stamp + "-", ".json", directory);
        try (FileOutputStream output = new FileOutputStream(file)) { output.write(bytes); output.flush(); }
        catch (IOException error) { file.delete(); throw error; }
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".backupfiles", file);
        return new Prepared(uri, file.getName(), backup);
    }

    /** Discover email apps via mailto, then require support for an attachment compose intent. */
    public static List<Target> targets(Context context) {
        PackageManager manager = context.getPackageManager();
        List<Target> result = new ArrayList<>();
        Set<String> packages = new HashSet<>();
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        for (ResolveInfo info : manager.queryIntentActivities(email, PackageManager.MATCH_DEFAULT_ONLY)) {
            String name = info.activityInfo.packageName;
            if (!packages.add(name)) continue;
            Intent send = new Intent(Intent.ACTION_SEND).setType("application/json").setPackage(name);
            if (manager.resolveActivity(send, PackageManager.MATCH_DEFAULT_ONLY) == null) continue;
            CharSequence label = info.activityInfo.applicationInfo.loadLabel(manager);
            result.add(new Target(name, label == null ? name : label.toString()));
        }
        result.sort((a, b) -> a.label.compareToIgnoreCase(b.label));
        return result;
    }

    public static Intent compose(Context context, Prepared attachment, String packageName) {
        int goals = attachment.backup.data.goals == null ? 0 : attachment.backup.data.goals.size();
        Intent intent = new Intent(Intent.ACTION_SEND).setType("application/json").setPackage(packageName);
        intent.putExtra(Intent.EXTRA_SUBJECT, "未来想做 · 数据备份 " + attachment.backup.exportedAt);
        intent.putExtra(Intent.EXTRA_TEXT, "备份时间：" + attachment.backup.exportedAt + "\n账单：" + attachment.backup.data.accounts.size() +
                " 笔\n分类：" + attachment.backup.data.categories.size() + " 个\n目标：" + goals +
                " 个（包含完成状态）\n\n附件为完整 JSON 备份。换机后，可在本应用“备份 → 从文件恢复到新手机”中选择附件恢复。\n\n请在邮件应用中填写收件人并确认发送。");
        intent.putExtra(Intent.EXTRA_STREAM, attachment.uri);
        intent.setClipData(ClipData.newUri(context.getContentResolver(), attachment.filename, attachment.uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    public static final class Target {
        public final String packageName, label;
        Target(String packageName, String label) { this.packageName = packageName; this.label = label; }
    }
    public static final class Prepared {
        public final Uri uri;
        public final String filename;
        public final LedgerBackup backup;
        Prepared(Uri uri, String filename, LedgerBackup backup) { this.uri = uri; this.filename = filename; this.backup = backup; }
    }
}
