package com.programasterapps.poketacts;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;

/**
 * Created by Rob on 7/27/2015.
 */
public class PokeImageCursorAdapter extends CursorAdapter {

    MySQLiteHelper my_database;
    Cursor myData;

    GradientDrawable pressed_background;
    contact_list_fragment calling_fragment;

    Context myContext;
    MainContactsList myActivity;

    boolean long_click;

    public PokeImageCursorAdapter(Context context, Cursor c, boolean autoRequery, contact_list_fragment my_fragment,
                                  Activity theActivity) {
        super(context, c, autoRequery);
        myData = c;
        calling_fragment = my_fragment;
        long_click = false;
        myContext = context;
        myActivity = (MainContactsList)theActivity;
        init_background();

    }

    private void init_background() {
        pressed_background = new GradientDrawable();

        pressed_background = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.parseColor("#FFFFFF"),
                        Color.parseColor("#EC2727")}
        );
        pressed_background.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        pressed_background.setGradientRadius(500);
    }

    public PokeImageCursorAdapter(Context context, Cursor c, int flags, contact_list_fragment my_fragment) {
        super(context, c, flags);
        myData = c;
        calling_fragment = my_fragment;
        init_background();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.gridlayout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        LinearLayout grid_layout = (LinearLayout)view.findViewById(R.id.grid_contact_cell);
        ImageView contact_image = (ImageView)view.findViewById(R.id.contact_image);
        TextView contact_name = (TextView)view.findViewById(R.id.contact_name);

        if(myData != null)
        {
            String contact_id = myData.getString(myData.getColumnIndex(ContactsContract.Contacts._ID));
            String cName = myData.getString(myData.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            Bitmap contact = getPhoto(context, Long.parseLong(contact_id));

            if(contact != null)
            {
                contact_image.setImageBitmap(contact);
            }
            else
            {
                contact_image.setImageResource(R.drawable.nophoto);
            }

            grid_layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    int action = event.getActionMasked();
                    switch (action)
                    {
                        case MotionEvent.ACTION_DOWN:
                            v.setBackground(pressed_background);
                            v.setMinimumWidth(0);
                            break;
                        case MotionEvent.ACTION_UP:
                            v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            if(v.getMinimumWidth() != 1)
                            {
                                calling_fragment.on_grid_item_pressed(v);
                                v.setMinimumWidth(0);
                            }
							break;
                        case MotionEvent.ACTION_CANCEL:
                            v.setBackgroundColor(Color.parseColor("#FFFFFF"));
							break;
                    }
                    long_click = false;
                    return false;
                }
            });
			
			grid_layout.setOnLongClickListener(new View.OnLongClickListener(){
			
				@Override
				public boolean onLongClick(View v){
                    v.setMinimumWidth(1);
                    myActivity.show_edit_delete_dialog(v);
					return true;
				}
			});

			grid_layout.setLongClickable(true);
            contact_name.setText(cName);
            view.setTag(contact_id);
            grid_layout.setBackground(new ColorDrawable(Color.parseColor("#FFFFFF")));
        }
    }

    public Bitmap getPhoto(Context context, Long data_id)
    {
        Bitmap myPhoto = null;

        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getContentResolver(),
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, data_id)
        );

        if(input != null)
        {
            myPhoto = BitmapFactory.decodeStream(input);
            return myPhoto;
        }
        return null;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        myData = newCursor;
        return super.swapCursor(newCursor);
    }
}
