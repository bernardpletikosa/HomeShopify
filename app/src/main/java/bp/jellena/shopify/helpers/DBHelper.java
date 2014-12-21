package bp.jellena.shopify.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DBHelper {

    private static final String TAG = DBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "Shopify.db";
    private static final String PACKAGE_NAME = "bp.jellena.shopify";

    public static File getCurrentDBFile() {
        String currentDBPath = "/data/" + PACKAGE_NAME + "/databases/" + DATABASE_NAME;

        return new File(Environment.getDataDirectory(), currentDBPath);
    }

    public static File getBackupDBFile() {
        String backupDBPath = "Shopify/backups/database";

        if (!new File(Environment.getExternalStorageDirectory(), backupDBPath).exists()) {
            Log.w(TAG, "Directory structure doesn't exist.");
            boolean mkdirs = new File(Environment.getExternalStorageDirectory(), backupDBPath).mkdirs();
            Log.w(TAG, "Directory structure " + (mkdirs ? "created successfully." : "creation problem!"));
        }

        backupDBPath = backupDBPath + "/" + DATABASE_NAME;

        return new File(Environment.getExternalStorageDirectory(), backupDBPath);
    }

    public static boolean transferDBData(FileChannel source, FileChannel destination) {
        try {
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            Log.e(TAG, "IOException while transferring DB data.");
            e.printStackTrace();
            return false;
        }

        try {
            source.close();
            destination.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException while closing File channels.");
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
