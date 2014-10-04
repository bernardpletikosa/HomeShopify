package bp.jellena.shopify.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import bp.jellena.shopify.R;
import bp.jellena.shopify.helpers.DBHelper;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by bp 02/10/14.
 */
public class FragmentSettings extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.frag_sett_export_db)
    public void exportDB() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Data backup").setIcon(R.drawable.ic_launcher).setCancelable(false);
        alertDialog.setMessage("You will replace previously backup data. Do you want to continue?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    FileChannel source = new FileInputStream(DBHelper.getCurrentDBFile()).getChannel();
                    FileChannel destination = new FileOutputStream(DBHelper.getBackupDBFile()).getChannel();
                    if (DBHelper.transferDBData(source, destination))
                        Toast.makeText(getActivity(), "Data backup successful.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Data backup failed... Please try again.", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e(DBHelper.class.getSimpleName(), "IOException while exporting DB data.");
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @OnClick(R.id.frag_sett_import_db)
    public void importDatabase() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Data backup").setIcon(R.drawable.ic_launcher).setCancelable(false);
        alertDialog.setMessage("You will overwrite all your current data. Do you want to continue?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    FileChannel source = new FileInputStream(DBHelper.getBackupDBFile()).getChannel();
                    FileChannel destination = new FileOutputStream(DBHelper.getCurrentDBFile()).getChannel();
                    if (DBHelper.transferDBData(source, destination))
                        Toast.makeText(getActivity(), "Data import successful.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Data import failed... Please try again.", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e(DBHelper.class.getSimpleName(), "IOException while importing DB data.");
                    e.printStackTrace();
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
