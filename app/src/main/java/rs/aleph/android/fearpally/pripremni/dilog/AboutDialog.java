package rs.aleph.android.fearpally.pripremni.dilog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import rs.aleph.android.fearpally.R;

/**
 * Created by milossimic on 11/19/16.
 */

public class AboutDialog extends AlertDialog.Builder{

    public AboutDialog(Context context) {
        super(context);

        setTitle(R.string.dialog_about_title);
        setMessage("Milos Simic");
        setCancelable(false);

        setPositiveButton(R.string.dialog_about_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        setNegativeButton(R.string.dialog_about_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }


    public AlertDialog prepareDialog(){
        AlertDialog dialog = create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

}