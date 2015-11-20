package com.programasterapps.poketacts;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;


public class MainContactsList extends ActionBarActivity {

    private static final int REFRESH = 1;

    /************************Database Variables************************/
	public SQLiteDatabase additional_contacts;
    public MySQLiteHelper additional_info;

    public DialogFragment edit_delete_dialog;
    public contact_list_fragment my_contact_list;
	
    public ActionBar myBar;

    private String editID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		//Get the action bar and change its background color to red
        myBar = getSupportActionBar();
        myBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EC2727")));

        setContentView(R.layout.activity_main_contacts_list);

        boolean database_created = getPreferences(Context.MODE_PRIVATE).getBoolean(CONSTANT.DB_CREATED, false);
        if(!database_created)
        {
            additional_info = new MySQLiteHelper(this.getApplicationContext());
            additional_contacts = additional_info.getWritableDatabase();

            SharedPreferences.Editor pEdit = getPreferences(Context.MODE_PRIVATE).edit();
                pEdit.putBoolean(CONSTANT.DB_CREATED, true);
            pEdit.commit();
        }

        my_contact_list = (contact_list_fragment)getSupportFragmentManager().findFragmentById(R.id.contacts_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_contacts_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //no inspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_addContact)
        {
            Intent add_contact = new Intent(this, addeditcontact.class);
                add_contact.putExtra(CONSTANT.EDIT_CONTACT_BOOL, false);
            startActivity(add_contact);
        }

        return super.onOptionsItemSelected(item);
    }

    public void show_edit_delete_dialog(View view)
    {
        editID = view.getTag().toString();
        edit_delete_dialog = new longClickDialog();
        edit_delete_dialog.show(this.getFragmentManager(), "image");
    }

    public void edit_clicked_contact(View view) {
        edit_delete_dialog.dismiss();
        Intent add_contact = new Intent(this, addeditcontact.class);
            add_contact.putExtra(CONSTANT.EDIT_CONTACT_BOOL, true);
            add_contact.putExtra(CONSTANT.EDIT_CONTACT_ID, editID);
        startActivityForResult(add_contact, REFRESH);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REFRESH)
        {
            if(resultCode == RESULT_OK)
            {
                my_contact_list.reloadResult();
            }
        }
    }

    public void setContact(my_contact contact)
    {

    }
}
