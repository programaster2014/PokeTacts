package com.programasterapps.poketacts;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by Roberto Brunner on 6/21/2015.
 */
public class contact_list_fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	/*************** CONSTANTS **************/
	
    static final int 	LOAD_ALL_CONTACTS 	= 0;
    static final int 	LOAD_SINGLE_CONTACT = 1;
	static final Uri 	CONTACTS_URI 		= ContactsContract.Contacts.CONTENT_URI;
    static final Uri 	DATA_CONTACTS_URI 	= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    static final String FIRST_ID            = "first_id_in_query";
    static final String ITEM_CLICKED        = "item_clicked";
	static final String CONTACT_ID_KEY		= "contact_id";

    /*************** DB Get Fields **************/
    static final int CONTACT_ID_COL = 0;
    static final int TYPE_1_COL     = 1;
    static final int TYPE_2_COL     = 2;
    static final int DESCRIPTION_COL= 3;
    static final int HEIGHT_FEET_COL= 4;
    static final int HEIGHT_IN_COL  = 5;
    static final int WEIGHT_COL     = 6;

    private static final int MOBILE     = 0;
    private static final int HOME       = 1;
    private static final int WORK       = 2;
	
	/*************** VIEWS **************/
    GridView gv;
    LinearLayout alpha_scroll;

	/*************** GLOBAL VARIABLES **************/
	
	//ARRAYS
	int[] jump_rows;
	
	//BOOLEANS
	boolean isLandscape;
    boolean item_has_been_clicked;
    boolean has_loaded;

    boolean home_logged;
    boolean work_logged;
    boolean mobile_logged;

    //STRINGS
	String first_ID;
	
	//LONGS
    Long clicked_contact_ID;
	
	//CONTACT VARIABLES
	my_contact picked_contact;
    my_contact shown_contact;

    //ANDROID VARIABLES
    PokeImageCursorAdapter gridAdapter;
	Bundle list_query_bundle;
    MySQLiteHelper additionl_info;
    MainContactsList myActivity;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		//Initialize Variables
        list_query_bundle = new Bundle();
        picked_contact = new my_contact();
        jump_rows = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0};

        if(savedInstanceState != null)
        {
            item_has_been_clicked = savedInstanceState.getBoolean(ITEM_CLICKED);
            first_ID = savedInstanceState.getString(FIRST_ID);
            clicked_contact_ID = savedInstanceState.getLong("CLICKED_ID");
        }
        else
        {
            item_has_been_clicked = false;
            first_ID = "";
        }

		//Get screen orientation to know what needs to be loaded or shown. Save as boolean
		//for future reference
		int screen = getActivity().getResources().getConfiguration().orientation;
		isLandscape = (screen == Configuration.ORIENTATION_LANDSCAPE);

		//Check if app has been loaded before. If preferences aren't found, default to false
        has_loaded = getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(CONSTANT.DB_CREATED, false);

		//Get reference to sqlHelper to have access to database
        additionl_info = new MySQLiteHelper(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_list_fragment_layout, container, false);

		//Get all views that are needed
		gv = 		   (GridView)     view.findViewById(R.id.contacts_gridview);
		alpha_scroll = (LinearLayout) view.findViewById(R.id.alpha_scroll);

		//Add adapter to gridview. the pokeimagecursoradapter takes a contact_list_fragment as a 
		//parameter to allow ontouch to fire onitemclicked events
        gridAdapter = new PokeImageCursorAdapter(
                getActivity(),	//Get the current activity
                null,			//pass null cursor to prevent queries on IU thread
                false,				//0 for flags, no idea what the flags are for TBH
                this,			//this contact_list_fragment instance for onTouch events
                getActivity()
        );
        
        if(gv != null)
        {
			//If the device is in landscape mode, the gridview should only have 1 column to make it
			//look like a listview. If in portrait, then the gridview should have 3 columns
            if(isLandscape){gv.setNumColumns(1);}	//in Landscape, 1 column
            else{gv.setNumColumns(3);}				//in portrait, 3 columns

            gv.setChoiceMode(ListView.CHOICE_MODE_SINGLE); //Only one item should be selected at a time
            gv.setAdapter(gridAdapter); //Apply the pokeimagecursoradapter
        }

        
		//The following code creates the alphabetical quickscroll functionality
		//Plan on making this an optional setting
        char letter = 'A'; //start with the letter A (duh)
        for(int count = 0; count < 13; count++)  //Only displaying half of the letters, otherwise the views are too small to click/see
        {
			//New Textview parameters
            TextView alpha_textview = new TextView(this.getActivity());
            alpha_textview.setText(String.valueOf(letter));
            alpha_textview.setTag(count); //set the tag to hold the item count values for jumping
            alpha_textview.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            alpha_textview.setGravity(Gravity.CENTER);
            alpha_textview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
					//On touch, scroll to the position of the first instance of the selected letter
					//Check if I need to multiply by 3 here or divide by 3 on creation. Seems like it cancels
                    gv.smoothScrollToPositionFromTop(jump_rows[Integer.parseInt(v.getTag().toString())] * 3, 0, 0);
                    return true;
                }

            });
			
			//Change alpha_scroll text size based on screen orientation
            if(!isLandscape){alpha_textview.setTextSize(10f);}
            else{alpha_textview.setTextSize(14f);}

			//add the new textview to the scrollbar
            alpha_scroll.addView(alpha_textview);

			//Increment the letter twice
            letter+=2;
        }


		//Begin the load all contacts query in non-UI thread
        getLoaderManager().initLoader(LOAD_ALL_CONTACTS, null, this);

        return view;
    }

    public void run_list_item_query()
    {
        getLoaderManager().restartLoader(LOAD_SINGLE_CONTACT, list_query_bundle, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		//Initialize query params to make it so this function only needs one
		//return statement
        String[] columnsWanted;
		String[] selectArgs;
        String select; 
		String sort_by;
        Uri uri = null;
		
        switch (id){
            case LOAD_ALL_CONTACTS:
				/*******************************************************
					Query Scope: Access all contacts
					Columns returned: 
						1. Contact ID
						2. Contact Display Name
					Where Clause(s): 
						1. Display Name is not null
						2. Contact has phone number is true
					Sorted: yes, alphabetical by name
				*******************************************************/
                uri = CONTACTS_URI;
                select = ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL AND " +
                        ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";
                columnsWanted = new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME};
                selectArgs = null;
				sort_by = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
            case LOAD_SINGLE_CONTACT:
				/*******************************************************
					Query Scope: Access single contact's data
					Columns returned: ALL
					Where Clause(s): 
						1. Contact ID is equal to the click contact id
					Sorted: no
				*******************************************************/
                uri = DATA_CONTACTS_URI;
                select = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                columnsWanted = null;
                selectArgs = new String[]{
                        args.getString(CONTACT_ID_KEY)
                };
                sort_by = null;
                break;
            default:
				uri = CONTACTS_URI;
                columnsWanted = null;
                select = "";
                selectArgs = null;
                sort_by = null;
        };

        return new CursorLoader(
                getActivity(),
                uri,
                columnsWanted,
                select,
                selectArgs,
                sort_by);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id){
            case LOAD_ALL_CONTACTS:
                data.moveToFirst();
                first_ID = data.getString(data.getColumnIndex(ContactsContract.Contacts._ID));

				//Load new all contacts cursor into the listview
                gridAdapter.swapCursor(data);
                createAlphaScroll(data);
				
				//If a list item has not been clicked and the device is in landscape mode,
				//run a query for the first id and populate the results fragment.
				//This makes sure that a contact is always showing and there isn't just a 
				//blank white slate
				if (!item_has_been_clicked)
				{
					list_query_bundle.putString(CONTACT_ID_KEY, first_ID);
                    run_list_item_query();
                }

                if(!has_loaded)
                {
                    randomize_contact_details(data);
                }
                break;
            case LOAD_SINGLE_CONTACT:
				//Begin parsing cursor for details fragment information
                list_item_query_logic(data);
        }
    }

	/*************************************************
	Creating the Alphabetical quickscroll
	
	Takes the cursor and queries it for the contact names.
	
	If there is no name starting with a certain letter, the quickscroll
	scrolls to the location specified by the letter above it (recursive)
	
	*************************************************/
    private void createAlphaScroll(Cursor data) {
        if(data.moveToFirst())
        {
			//Since there are 3 columns, the row is the item number divided by 3 (integer division)
            int counter = 0;
            int row = 0;
            char firstChar = 'A';
            jump_rows[counter] = row;

            firstChar++;

            do {

                String name = data.getString(data.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if(name.charAt(0) > firstChar)
                {
                    switch (name.charAt(0))
                    {
                        case 'C':
                            jump_rows[1] = row/3;
                            break;
                        case 'E':
                            jump_rows[2] = row/3;
                            break;
                        case 'G':
                            jump_rows[3] = row/3;
                            break;
                        case 'I':
                            jump_rows[4] = row/3;
                            break;
                        case 'K':
                            jump_rows[5] = row/3;
                            break;
                        case 'M':
                            jump_rows[6] = row/3;
                            break;
                        case 'O':
                            jump_rows[7] = row/3;
                            break;
                        case 'Q':
                            jump_rows[8] = row/3;
                            break;
                        case 'S':
                            jump_rows[9] = row/3;
                            break;
                        case 'U':
                            jump_rows[10] = row/3;
                            break;
                        case 'W':
                            jump_rows[11] = row/3;
                            break;
                        case 'Y':
                            jump_rows[12] = row/3;
                            break;
                    }

                    firstChar = name.charAt(0);
                }
                row++;
            }while(data.moveToNext());

            for(counter = 1; counter < 13; counter++)
            {
				//Cycle through the jump positions and if the row number is still 0,
				//give it the position value of the letter above it.
                if(jump_rows[counter] == 0)
                {
                    jump_rows[counter] = jump_rows[counter-1];
                }
            }
        }
    }

    private void randomize_contact_details(Cursor data) {

		int t1, t2, ft, in, wt, di;
				
        if(data.moveToFirst())
        {
            do {
                String randomized_ID = data.getString(data.getColumnIndex(ContactsContract.Contacts._ID));

                t1 = randInt(1, 17);
                t2 = randInt(0, 17);
				
				while(t2 == t1)
				{
					t2 = randInt(0, 17);
				}
				
                ft = randInt(3, 7);
                in = randInt(0, 11);
                wt = randInt(90, 400);
				di = randInt(0,5);
				
                additionl_info.insert_contact(randomized_ID, t1, t2, CONSTANT.DEFAULT_DESCRIPTION_TEXT[di], ft, in, wt);

            }while(data.moveToNext());
        }
    }

    private int randInt(int min, int max)
    {
        Random rand = new Random();
        int randomNumber = rand.nextInt((max - min) + 1) + min;
        return randomNumber;
    }

    private void list_item_query_logic(Cursor data) {
		//Check if the cursor returned any data. If not, report an error
        home_logged = false;
        work_logged = false;
        mobile_logged = false;

        if(data.moveToFirst())
        {
            picked_contact.clear_contact();

            //Set the proper members of the contact object
            picked_contact.setId(data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));
            picked_contact.setName(data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));

            String[] selection = null;
            String current_ID = null;
            if(clicked_contact_ID != null)
            {
                selection = new String[]{
                        clicked_contact_ID.toString()
                };
                current_ID = clicked_contact_ID.toString();
            }
            else
            {
                selection = new String[]{
                        first_ID
                };
                current_ID = first_ID;
            }

            Cursor addressCursor = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?",
                    selection,
                    null);

            if(addressCursor != null && addressCursor.moveToFirst())
            {
                picked_contact.setAddress(addressCursor.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)));
                addressCursor.close();
            }


            Cursor poketacts_db_info = additionl_info.runQuery(1, current_ID);

            if(poketacts_db_info != null && poketacts_db_info.moveToFirst())
            {
                picked_contact.setType_1(poketacts_db_info.getInt(TYPE_1_COL));
                picked_contact.setType_2(poketacts_db_info.getInt(TYPE_2_COL));
                picked_contact.setDescription(poketacts_db_info.getString(DESCRIPTION_COL));
                picked_contact.setHeight_ft(poketacts_db_info.getInt(HEIGHT_FEET_COL));
                picked_contact.setHeight_in(poketacts_db_info.getInt(HEIGHT_IN_COL));
                picked_contact.setWeight(poketacts_db_info.getInt(WEIGHT_COL));

                poketacts_db_info.close();
            }

            if(item_has_been_clicked) {picked_contact.setContact_photo(clicked_contact_ID);}
            else {picked_contact.setContact_photo(Long.parseLong(first_ID));}

            //Cycle through the cursor to find all the phone number types returned and place them in the appropriate spot
            do{
                //Get the normalized number in the row
                String the_number = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                if(the_number == null) //If the normalized number is null, get the non-normalized number
                {
                    the_number = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }

                //Check what type of number it is and perform the appropriate logic
                int number_type = data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                switch (number_type){
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        picked_contact.addNumber(the_number, HOME);
                        if(data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) > 0)
                        {
                            picked_contact.setHome(the_number);
                        }
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        picked_contact.addNumber(the_number, WORK);
                        if(data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) > 0)
                        {
                            picked_contact.setWork(the_number);
                        }
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        picked_contact.addNumber(the_number, MOBILE);
                        if(data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) > 0)
                        {
                            picked_contact.setMobile(the_number);
                        }
                        break;
                    default:
                }

            }while(data.moveToNext());

            //Update the details fragment now that all needed data has been collected
            showContactInformation(picked_contact);
		}

		//close the cursor
        data.close();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {gridAdapter.swapCursor(null);}

	
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(clicked_contact_ID != null)
        {
            outState.putLong("CLICKED_ID", clicked_contact_ID);
        }
        else
        {
            outState.putLong("CLICKED_ID", Long.parseLong(first_ID));
        }
        outState.putString(FIRST_ID, first_ID);
        outState.putBoolean(ITEM_CLICKED, item_has_been_clicked);
        super.onSaveInstanceState(outState);
    }


    public void on_grid_item_pressed(View view)
    {
        //On list item clicked, get the value of the textview's tag and store it as
        //a bundle argument to use it for the individual query
        String select = view.getTag().toString();
        if(item_has_been_clicked)
        {
            if(Long.parseLong(select) != clicked_contact_ID)
            {
                list_query_bundle.putString(CONTACT_ID_KEY, select);
                clicked_contact_ID = Long.parseLong(select);

                //If the device is in landscape mode, run the query now and update the
                //details fragment to the right
                run_list_item_query();
            }
        }
        else
        {
            item_has_been_clicked = true;
            list_query_bundle.putString(CONTACT_ID_KEY, select);
            clicked_contact_ID = Long.parseLong(select);

            if(!select.equalsIgnoreCase(first_ID))
            {
                //If the device is in landscape mode, run the query now and update the
                //details fragment to the right
                run_list_item_query();
            }
        }
    }

    public void reloadResult()
    {
        getLoaderManager().restartLoader(LOAD_SINGLE_CONTACT, list_query_bundle, this);
    }
	
	/**************************************LANDSCAPE VIEW FUNCTIONS**************************************/
	private void showContactInformation(my_contact the_contact) {
        shown_contact = the_contact;
		//create a new instance of the results fragment by passing in the contact object with the 
		//needed information
        results_fragment contact_info_fragment = results_fragment.newInstance(the_contact);
		
		//Begin fragment replacement logic
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        ft.replace(R.id.results_summary, contact_info_fragment);
        ft.commit();
    }


}
