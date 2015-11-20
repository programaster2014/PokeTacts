package com.programasterapps.poketacts;

import android.app.DialogFragment;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;


public class addeditcontact extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /***************ON Saved Instance State************/
    private static final String NUMBER_STRING_ARRAY = "contact_numbers";
    private static final String NUMBER_TYPES_ARRAY 	= "contact_number_types";
    private static final String NUMBER_OF_NUMBERS 	= "number_of_numbers";
    private static final String ADDRESSES_SHOWING 	= "address_bool";
    private static final String EDDITTING_ADDRESS   = "edditing_address";
    private static final String ADDRESSES_LIST 		= "address_edit_list";

    private static final String THE_EDIT_ID         = "the_edit_id";
    private static final Uri 	DATA_CONTACTS_URI 	= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	
	/***************Local Constants************/
	private static final Uri 	CONTACTS_URI 		= ContactsContract.Contacts.CONTENT_URI;

    private static final int STREET     = 0;
    private static final int CITY       = 1;
    private static final int STATE      = 2;
    private static final int ZIP        = 3;

    private static final int MOBILE     = 0;
    private static final int HOME       = 1;
    private static final int WORK       = 2;

    /*************** DB Get Fields **************/
    static final int CONTACT_ID_COL = 0;
    static final int TYPE_1_COL     = 1;
    static final int TYPE_2_COL     = 2;
    static final int DESCRIPTION_COL= 3;
    static final int HEIGHT_FEET_COL= 4;
    static final int HEIGHT_IN_COL  = 5;
    static final int WEIGHT_COL     = 6;

	/***************Layout Views************/
	//Types
    private Spinner type1;
    private Spinner type2;

    /*************Get All Edittexts***************/
    private EditText cName;
    private EditText cHeightFt;
    private EditText cHeightIn;
    private EditText cWeight;
    private EditText cDescription;


    private ImageView contact_imageView;
	
	//Numbers
    private ArrayList<Spinner>  spinners;			//Types
	private ArrayList<EditText> number_editTexts;	//Numbers
    private ArrayList<String>   number_IDs;         //Number IDs
	private ArrayList<EditText> addressEditTexts;	//Address
	
    private ImageView myImage;
    private Bitmap contact_image;

    private MySQLiteHelper additional_info;
    private ActionBar myBar;

    private DialogFragment image_dialog_fragment;
    private DialogFragment extra_field_dialog;

	/***************Normal Globals************/
    private int number_field_counter;
    private int centerizer_id;

    private String theID;
    private String dataID;
    private String rawID;
	
	private boolean Address_Showing;
    private boolean Edditing_Contact;
    private boolean Edditing_Address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addeditcontact);

        centerizer_id = 1000;

        //Get the action bar and change its background color to red
        myBar = getSupportActionBar();
        myBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EC2727")));

        //Item position will work for these. Add 1 to type 1 for correct
        //array access
        type1 	= (Spinner)findViewById(R.id.type_1_spinner);
        type2 	= (Spinner)findViewById(R.id.type_2_spinner);
        myImage = (ImageView)findViewById(R.id.add_edit_image);

        cName     =    (EditText)findViewById(R.id.add_edit_contact_name);
        cHeightFt =    (EditText)findViewById(R.id.add_edit_contact_height_ft);
        cHeightIn =    (EditText)findViewById(R.id.add_edit_contact_height_in);
        cWeight   =    (EditText)findViewById(R.id.add_edit_contact_weight);
        cDescription = (EditText)findViewById(R.id.contact_description);

        contact_imageView = (ImageView)findViewById(R.id.add_edit_image);


        spinners 			= new ArrayList<Spinner>();
        number_editTexts 	= new ArrayList<EditText>();
        number_IDs          = new ArrayList<String>();
        addressEditTexts 	= new ArrayList<EditText>();

        additional_info = new MySQLiteHelper(getApplicationContext());

        create_phone_button_and_label();

        Intent myIntent = getIntent();
        Edditing_Contact = myIntent.getBooleanExtra(CONSTANT.EDIT_CONTACT_BOOL, false);
        if(Edditing_Contact)
        {
            editting_setup(myIntent, savedInstanceState);
        }
        else
        {
            adding_setup(savedInstanceState);
        }
    }

    /**********************************************************************************
     editting_setup

     Description: If the user is editting a contact, use this function for the setup
     Parameters:
        1. Intent myIntent - intent containing the editting contact variable,
                             comes from the parent activity
        2. Bundle savedInstanceState - savedInstanceState, will be null if the
                             activity has only been created once
     **********************************************************************************/
    public void editting_setup(Intent myIntent, Bundle savedInstanceState)
    {
        //Set Editing Booleans to false for starters
        Edditing_Address = false;

        //Get ID and place in bundle for loader manager
        theID = myIntent.getStringExtra(CONSTANT.EDIT_CONTACT_ID);
        Bundle id_bundle = new Bundle();
        id_bundle.putString(THE_EDIT_ID, theID);

        getSupportLoaderManager().restartLoader(CONSTANT.LOAD_EDIT_CONTACT, id_bundle, this);

        setTitle("Edit Contact");

        //search for contact image and set the imageview if one is found
        if(contact_imageView != null)
        {
            contact_image = getPhoto(theID);
            if(contact_image != null)
            {
                contact_imageView.setImageBitmap(contact_image);
            }
            else
            {
                contact_image = BitmapFactory.decodeResource(getResources(), R.drawable.nophoto);
            }
        }

        //if there is a saved instance state, check to see if the user was editting an address
        if(savedInstanceState != null)
        {
            Edditing_Address = savedInstanceState.getBoolean(EDDITTING_ADDRESS);
        }
    }

    /**********************************************************************************
     adding_setup

     Description: If the user is editting a contact, use this function for the setup
     Parameters:
     1. Bundle savedInstanceState - savedInstanceState, will be null if the
     activity has only been created once
     **********************************************************************************/
    public void adding_setup(Bundle savedInstanceState)
    {
        Address_Showing = false;
        setTitle("Add New Contact");
        contact_image = BitmapFactory.decodeResource(getResources(), R.drawable.nophoto);

        /**************************Check SavedInstanceState************************************/
        if(savedInstanceState != null)
        {
            //get all numbers that the user was working on
            ArrayList<String> number_recover = savedInstanceState.getStringArrayList(NUMBER_STRING_ARRAY);

            //check if the address was being editted at the time
            Address_Showing = savedInstanceState.getBoolean(ADDRESSES_SHOWING);

            for(int counter = 0; counter < savedInstanceState.getInt(NUMBER_OF_NUMBERS); counter++)
            {
                add_number_field(true, number_recover.get(counter), 0);
            }

            //if the address was showing, then add all the address field details
            //mainly used for screen orientation changes on recreate
            if(Address_Showing)
            {
                ArrayList<String> my_address_info = savedInstanceState.getStringArrayList(ADDRESSES_LIST);
                add_address_fields(my_address_info, true);
            }
        }
        else
        {
            //if theres no savedstate, then add a blank field
            add_number_field(false, null, -1);
        }
    }

    /**********************************************************************************
     getPhoto

     Description: get a photo using a contact id
     Parameters:
     1. String my_id - the id of the contact needed to get the contact photo
     **********************************************************************************/
    public Bitmap getPhoto(String my_id)
    {
        Bitmap myPhoto = null;
        long data_id = Long.parseLong(my_id);

        //open an input stream to access the image data stream
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                getContentResolver(),
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, data_id)
        );

        //Check to see if a photo is there
        if(input != null)
        {
            myPhoto = BitmapFactory.decodeStream(input);                    //Decode the stream into a bitmap
            try{
                input.close();                                              //Close the input stream
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return myPhoto;
        }
        return null;
    }

    /**********************************************************************************
     create_phone_button_and_label

     Description: programmatically creates and implements the functionality of the phone
                  number area. This includes the add new number button to add a new edit
                  text field and a phone number type spinner
     Parameters:

     **********************************************************************************/
    public void create_phone_button_and_label()
    {
        //Get the parent layout to put the new button area in
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.phone_number_parent_layout);

        //Create all the new views needed
        RelativeLayout new_field_layout = new RelativeLayout(this);
        TextView phone_view             = new TextView(this);       //"Phone", label at beginning of phone area
        Button add_phone_number_button  = new Button(this);         //"ADD NUMBER FIELD" button
        View centerizer                 = new View(this);           //Centerizer between Phone label and add number button

        //Relative Layout Params
        RelativeLayout.LayoutParams parent_params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp_to_px(50));
        new_field_layout.setLayoutParams(parent_params);

        //Centerizer Params
        RelativeLayout.LayoutParams centerizer_params = new RelativeLayout.LayoutParams(0,0);
            centerizer_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centerizer.setLayoutParams(centerizer_params);
        centerizer.setVisibility(View.INVISIBLE);
        centerizer.setId(centerizer_id);

        //Textview params
        RelativeLayout.LayoutParams textview_params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                textview_params.addRule(RelativeLayout.LEFT_OF, centerizer.getId());
                phone_view.setTextSize(20);
                phone_view.setText(R.string.addedit_phone);
        phone_view.setLayoutParams(textview_params);
        phone_view.setGravity(Gravity.CENTER_VERTICAL);

        //Button params
        RelativeLayout.LayoutParams button_params = new RelativeLayout.LayoutParams(dp_to_px(200),
                ViewGroup.LayoutParams.MATCH_PARENT);
                button_params.addRule(RelativeLayout.RIGHT_OF, centerizer.getId());
        add_phone_number_button.setLayoutParams(button_params);
        add_phone_number_button.setText(R.string.addnumberfieldbutton);

        //Create the onclicklistener for the add number button, adds a new field
        add_phone_number_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_number_field(false, null, -1);
            }
        });

        //Add all of the new views to the parent
        new_field_layout.addView(phone_view);                 //add Phone label to beginning of phone number area
        new_field_layout.addView(centerizer);                 //centerizer to go between Phone label and add new number button
        new_field_layout.addView(add_phone_number_button);    //add "add number field" button to right of phone label

        parentLayout.addView(new_field_layout);
    }

    /**********************************************************************************
     add_number_field

     Description: programmatically creates and implements the functionality of the phone
     number area. This includes the add new number button to add a new edit
     text field and a phone number type spinner
     Parameters:
        1. boolean saved_recover: boolean to check if recovering from a savedinstance state
        2. String number: string used to populate the number edittexts
        3. int type: the type (home, mobile, work) of the phone number
     **********************************************************************************/
	public void add_number_field(boolean saved_recover, String number, int type) {

        number_field_counter++;

        //Get the parent layout to put the new button area in
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.phone_number_parent_layout);

        RelativeLayout new_field_layout = new RelativeLayout(this);
        EditText new_number = new EditText(this);
        Spinner new_number_type = new Spinner(this);
        Button delete_number_field = new Button(this);
        View centerizer = new View(this);

        RelativeLayout spinner_button_layout = new RelativeLayout(this);
        View spinner_button_centerizer = new View(this);

        String[] phone_types = getResources().getStringArray(R.array.phone_types);
        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, phone_types);
        new_number_type.setAdapter(spinner_adapter);
        new_number_type.setId(number_field_counter);

        //spinner centerizer details
        RelativeLayout.LayoutParams spinner_centerizer_params = new RelativeLayout.LayoutParams(0,0);
        spinner_centerizer_params.addRule(RelativeLayout.RIGHT_OF, new_number_type.getId());
        spinner_button_centerizer.setLayoutParams(spinner_centerizer_params);
        spinner_button_centerizer.setVisibility(View.INVISIBLE);
        spinner_button_centerizer.setId(number_field_counter + 1000);

        //Delete Number Button
        RelativeLayout.LayoutParams delete_params = new RelativeLayout.LayoutParams(
                dp_to_px(75), ViewGroup.LayoutParams.MATCH_PARENT);
        delete_params.addRule(RelativeLayout.RIGHT_OF, spinner_button_centerizer.getId());

        //Spinner details
        RelativeLayout.LayoutParams spinner_params = new RelativeLayout.LayoutParams(
                dp_to_px(100), ViewGroup.LayoutParams.MATCH_PARENT);
        new_number_type.setLayoutParams(spinner_params);

        //Spinner_button_layout params
        RelativeLayout.LayoutParams spinner_layout_params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        spinner_layout_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        spinner_button_layout.setLayoutParams(spinner_layout_params);
        spinner_button_layout.setId(number_field_counter + 2000);

        //Centerizer details
        RelativeLayout.LayoutParams centerizer_params = new RelativeLayout.LayoutParams(0, 0);
            centerizer_params.addRule(RelativeLayout.LEFT_OF, spinner_button_layout.getId());
        centerizer.setLayoutParams(centerizer_params);
        centerizer.setVisibility(View.INVISIBLE);
        centerizer.setId(number_field_counter + 50);

        //EditText details
        RelativeLayout.LayoutParams edit_text_params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            edit_text_params.addRule(RelativeLayout.LEFT_OF, centerizer.getId());
        edit_text_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        delete_number_field.setText("Delete");
        delete_number_field.setLayoutParams(delete_params);

        new_number.setHint(R.string.addedit_number);
        new_number.setLayoutParams(edit_text_params);
        new_number.setInputType(InputType.TYPE_CLASS_NUMBER);

        spinner_button_layout.addView(new_number_type);
        spinner_button_layout.addView(spinner_button_centerizer);
        spinner_button_layout.addView(delete_number_field);

        //Relative Layout Params
        RelativeLayout.LayoutParams parent_params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp_to_px(50));
        new_field_layout.setLayoutParams(parent_params);

        new_field_layout.addView(new_number);
        new_field_layout.addView(centerizer);
        new_field_layout.addView(spinner_button_layout);

        parentLayout.addView(new_field_layout);

        if(saved_recover)
        {
            new_number.setText(number);           //if recovering from savedinstance, set the number to the edittext
            new_number_type.setSelection(type);   //if recovering from savedinstance, set the number type
        }

        spinners.add(new_number_type);
        number_editTexts.add(new_number);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_addeditcontact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int dp_to_px(int sizeInDp)
    {
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (sizeInDp*scale + 0.5f);

        return dpAsPixels;
    }

    public void add_new_image(View view) {
        image_dialog_fragment = new imageDialog();
        image_dialog_fragment.show(getFragmentManager(), "image");
    }

    public void add_new_field_dialog(View view){
        extra_field_dialog = new newFieldDialog();
        extra_field_dialog.show(getFragmentManager(), "extra_field");
    }

    /**********************************************************************************
     from_images

     Description: get image from saved images using image dialog
     Parameters:
     1. View view: not used in function
     **********************************************************************************/
    public void from_images(View view) {
        Intent image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(image, CONSTANT.SELECT_PICTURE);
        image_dialog_fragment.dismiss();
    }

    /**********************************************************************************
     from_camera

     Description: get image from camera using image dialog
     Parameters:
     1. View view: not used in function
     **********************************************************************************/
	public void from_camera(View view) {
	
	}

    /**********************************************************************************
     onActivityResult

     Description: get image from activity
     Parameters:
     1. SELECT_PICTURE: grabbing picture from image gallery
     **********************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null)
        {
            switch(requestCode)
            {
                case CONSTANT.SELECT_PICTURE:
                    Bundle image_bundle = new Bundle();
                    image_bundle.putString(CONSTANT.IMAGE_URI, data.getData().toString());
                    getSupportLoaderManager().restartLoader(CONSTANT.SELECT_PICTURE, image_bundle, this);
                    break;
                default:
            }
        }
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

        switch (id) {
            case 0:
                uri = Uri.parse(args.getString(CONSTANT.IMAGE_URI));
                select = null;
                columnsWanted = new String[]{ MediaStore.Images.Media.DATA };
                selectArgs = null;
                sort_by = null;
                break;
            case 1:
                uri = DATA_CONTACTS_URI;
                select = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                columnsWanted = null;
                selectArgs = new String[]{
                        args.getString(THE_EDIT_ID)
                };
                sort_by = null;
                break;
            default:
                uri = CONTACTS_URI;
                columnsWanted = null;
                select = "";
                selectArgs = null;
                sort_by = null;
        }

        return new CursorLoader(
                this,
                uri,
                columnsWanted,
                select,
                selectArgs,
                sort_by);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if(data != null)
        {
            switch (id)
            {
                case CONSTANT.SELECT_PICTURE:
                    if(data.moveToFirst())
                    {
                        String uri_img = data.getString(data.getColumnIndex(MediaStore.Images.Media.DATA));
                        Uri image_uri = Uri.parse("file://"+uri_img);
                        contact_image = getPhoto(image_uri);
                        if(contact_image != null)
                        {
                            myImage.setImageBitmap(contact_image);
                        }
                    }
                    break;
                case CONSTANT.LOAD_EDIT_CONTACT:
                    if(data.moveToFirst())
                    {
                        cName.setText(data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));

                        do{
                            String myNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                            if(myNumber == null) //If the normalized number is null, get the non-normalized number
                            {
                                myNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }

                            myNumber = myNumber.replace("+", "");
                            //Check what type of number it is and perform the appropriate logic
                            int number_type = data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                            switch (number_type){
                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                    add_number_field(true, myNumber, HOME);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                    add_number_field(true, myNumber, WORK);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                    add_number_field(true, myNumber, MOBILE);
                                    break;
                                default:
                            }
                            number_IDs.add(data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));


                        }while(data.moveToNext());

                        if(data.moveToFirst())
                        {
                            String[] selection = new String[]{theID};

                            Cursor addressCursor = this.getContentResolver().query(
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?",
                                    selection,
                                    null);

                            if(addressCursor != null && addressCursor.moveToFirst())
                            {
                                String street = addressCursor.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                                String city = addressCursor.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                                String state = addressCursor.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                                String zip = addressCursor.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));

                                ArrayList<String> fill_address = new ArrayList<String>();
                                fill_address.add(street);
                                fill_address.add(city);
                                fill_address.add(state);
                                fill_address.add(zip);
                                add_address_fields(fill_address, true);

                                addressCursor.close();
                                Address_Showing = true;
                                Edditing_Address = true;
                            }
                            else
                            {
                                Address_Showing = false;
                            }
                        }

                        Cursor poketacts_db_info = additional_info.runQuery(1, theID);

                        if(poketacts_db_info != null && poketacts_db_info.moveToFirst())
                        {
                            type1.setSelection(poketacts_db_info.getInt(TYPE_1_COL) - 1);
                            type2.setSelection(poketacts_db_info.getInt(TYPE_2_COL));
                            cDescription.setText(poketacts_db_info.getString(DESCRIPTION_COL));
                            cHeightFt.setText(Integer.toString(poketacts_db_info.getInt(HEIGHT_FEET_COL)));
                            cHeightIn.setText(Integer.toString(poketacts_db_info.getInt(HEIGHT_IN_COL)));
                            cWeight.setText(Integer.toString(poketacts_db_info.getInt(WEIGHT_COL)));

                            poketacts_db_info.close();
                        }
                    }
            }

            data.close();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    public Bitmap getPhoto(Uri imageUri)
    {
        Bitmap myPhoto = null;

        try{
            myPhoto = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            return myPhoto;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }



    public void save_contact(MenuItem item) {
        EditText description = (EditText)findViewById(R.id.contact_description);

        //Strings
        String contact_name = cName.getText().toString();

        if(!contact_name.equals(""))
        {

            int contact_height_ft;
            int contact_height_in;
            int contact_weight;
            String theAddress = "";

            if(cHeightFt.getText().toString().isEmpty()) {contact_height_ft = 4;}
            else{contact_height_ft = Integer.parseInt(cHeightFt.getText().toString());}

            if(cHeightIn.getText().toString().isEmpty()){contact_height_in = 0;}
            else {contact_height_in = Integer.parseInt(cHeightIn.getText().toString());}

            if(cWeight.getText().toString().isEmpty()){contact_weight = 100;}
            else {contact_weight = Integer.parseInt(cWeight.getText().toString());}

            int contactType1 = type1.getSelectedItemPosition() + 1;
            int contactType2 = type2.getSelectedItemPosition();

            String description_text = description.getText().toString();

            //Contact Image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            contact_image.compress(Bitmap.CompressFormat.PNG, 100, stream);

            my_contact theContact = new my_contact();
                theContact.setName(contact_name);
                theContact.setHeight_ft(contact_height_ft);
                theContact.setHeight_in(contact_height_in);
                theContact.setWeight(contact_weight);
                theContact.setType_1(contactType1);
                theContact.setType_2(contactType2);
                theContact.setDescription(description_text);

            if(Edditing_Contact)
            {
                editting_contact_save(contact_name, theContact);
            }
            else
            {
                add_contact_save(contact_name, stream, theContact);
            }


            try
            {
                stream.flush();
                stream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        finish();
    }

    private void add_contact_save(String contact_name, ByteArrayOutputStream stream, my_contact theContact) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder;

        /*************************************SAVE CONTACT*****************************************/
        builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null);
        ops.add(builder.build());

        //Add Contact Name
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact_name);
        ops.add(builder.build());

        //Contact Image
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray());
        ops.add(builder.build());

        //Contact numbers
        for(int counter = 0; counter < number_editTexts.size(); counter++)
        {
            //Add Contact Number
            String contact_1_number = number_editTexts.get(counter).getText().toString();
            if(contact_1_number != null)
            {
                if(!contact_1_number.isEmpty())
                {
                    int phone_type = 0;
                    switch(spinners.get(counter).getSelectedItemPosition())
                    {
                        case 0:
                            phone_type = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                            break;
                        case 1:
                            phone_type = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
                            break;
                        case 2:
                            phone_type = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
                            break;
                    }
                    builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact_1_number)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phone_type);
                    ops.add(builder.build());
                }
            }
        }

        //Contact Address
        if(Address_Showing)
        {
            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, addressEditTexts.get(STREET).getText().toString().trim())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, addressEditTexts.get(CITY).getText().toString().trim())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, addressEditTexts.get(STATE).getText().toString().trim())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, addressEditTexts.get(ZIP).getText().toString().trim());
            ops.add(builder.build());
        }

        try
        {
            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            rawID = results[0].toString().replace("ContentProviderResult(uri=content://com.android.contacts/raw_contacts/", "");
            rawID = rawID.replace(")", "");

            String[] selection = new String[]
                    {
                            ContactsContract.RawContacts._ID,
                            ContactsContract.RawContacts.CONTACT_ID
                    };
            String[] selectArg = new String[]
                    {
                            rawID
                    };
            Cursor after_adding_id_search = getContentResolver().query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    selection,
                    ContactsContract.RawContacts._ID + " = ?",
                    selectArg,
                    null);

            if(after_adding_id_search != null && after_adding_id_search.moveToFirst())
            {
                theID = after_adding_id_search.getString(after_adding_id_search.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
            }

            additional_info.insert_contact(theID, theContact.getType_1(), theContact.getType_2(), theContact.getDescription(),
                    theContact.getHeight_ft(), theContact.getHeight_in(), theContact.getWeight());

            number_editTexts.clear();
        }
        catch (RemoteException e) {Log.e(CONSTANT.TAG, "Remote Exception");e.printStackTrace();}
        catch (OperationApplicationException e) {Log.e(CONSTANT.TAG, "OperationApplicationException Exception");e.printStackTrace();}
    }


    private void editting_contact_save(String contact_name, my_contact theContact)
    {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder;

        String[] selection = new String[]
                {
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID
                };
        String[] selectArg = new String[]
                {
                        theID
                };
        Cursor contact_search = getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                selection,
                ContactsContract.RawContacts.CONTACT_ID + " = ?",
                selectArg,
                null);

        if(contact_search != null && contact_search.moveToFirst())
        {
            rawID = contact_search.getString(contact_search.getColumnIndex(ContactsContract.RawContacts._ID));

            String[] rawSelection = new String[]
                    {
                            ContactsContract.Data._ID,
                            ContactsContract.Data.RAW_CONTACT_ID
                    };
            String[] rawSelectArg = new String[]
                    {
                            rawID
                    };

            Cursor raw_contact_cursor = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    rawSelection,
                    ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                    rawSelectArg,
                    null
            );

            if(raw_contact_cursor != null && raw_contact_cursor.moveToFirst())
            {
                dataID = raw_contact_cursor.getString(raw_contact_cursor.getColumnIndex(ContactsContract.Data._ID));
            }

        }

        /*************************************EDIT CONTACT*****************************************/
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data._ID + " = ?", new String[]{dataID})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact_name);
        ops.add(builder.build());

        //Cycle through numbers and update them
        for(int counter = 0; counter < number_editTexts.size(); counter++)
        {
            int phone_type = 0;
            String number = number_editTexts.get(counter).getText().toString();

            if(number != null)
            {
                if(!number.isEmpty())
                {
                    switch(spinners.get(counter).getSelectedItemPosition())
                    {
                        case 0:
                            phone_type = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                            break;
                        case 1:
                            phone_type = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
                            break;
                        case 2:
                            phone_type = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
                            break;
                    }

                    String where = ContactsContract.CommonDataKinds.Phone._ID + " = ?";
                    String[] params = new String[]
                            {
                                    number_IDs.get(counter)
                            };

                    builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                            .withSelection(where, params)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phone_type);
                    ops.add(builder.build());
                }
            }
        }

        if(Address_Showing)
        {
            String street   = addressEditTexts.get(STREET).getText().toString();
            String city     = addressEditTexts.get(CITY).getText().toString();
            String state    = addressEditTexts.get(STATE).getText().toString();
            String zip      = addressEditTexts.get(ZIP).getText().toString();

            if(Edditing_Address)
            {
                String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ?";
                String[] params = new String[]
                        {
                                rawID,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                        };
                builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(where,  params)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, street)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, city)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, state)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, zip);
                ops.add(builder.build());
            }
        }

        try
        {
            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            String theID = results[0].toString().replace("ContentProviderResult(uri=content://com.android.contacts/raw_contacts/", "");
            theID = theID.replace(")", "");

            //additional_info.insert_contact(theID, contactType1, contactType2, description_text,
            //       contact_height_ft, contact_height_in, contact_weight);

            number_editTexts.clear();
        }
        catch (RemoteException e)
        {
            Log.e(CONSTANT.TAG, "Remote Exception");
            e.printStackTrace();
        }
        catch (OperationApplicationException e)
        {
            Log.e(CONSTANT.TAG, "OperationApplicationException Exception");
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<String> created_numbers = new ArrayList<String>();
        ArrayList<Integer> number_types = new ArrayList<Integer>();
        ArrayList<String> address_fields = new ArrayList<String>();
        if(number_editTexts.size() > 0)
        {
            for(int counter = 0; counter < number_field_counter; counter++)
            {
                created_numbers.add(number_editTexts.get(counter).getText().toString());
                number_types.add(spinners.get(counter).getSelectedItemPosition());
            }

            outState.putInt(NUMBER_OF_NUMBERS, number_field_counter);
            outState.putStringArrayList(NUMBER_STRING_ARRAY, created_numbers);
        }


        outState.putBoolean(ADDRESSES_SHOWING, Address_Showing);
        outState.putBoolean(EDDITTING_ADDRESS, Edditing_Address);
        if(Address_Showing)
        {
            address_fields.add(addressEditTexts.get(STREET).getText().toString());
            address_fields.add(addressEditTexts.get(CITY).getText().toString());
            address_fields.add(addressEditTexts.get(STATE).getText().toString());
            address_fields.add(addressEditTexts.get(ZIP).getText().toString());

            outState.putStringArrayList(ADDRESSES_LIST, address_fields);
        }

        super.onSaveInstanceState(outState);
    }

    public void add_new_field(View view) {
        int view_id = view.getId();

        switch (view_id)
        {
            case R.id.add_address:
                if(!Address_Showing) {add_address_fields(null, false);}
                break;
        }
        extra_field_dialog.dismiss();
    }

    private void add_address_fields(ArrayList<String> edit_text_text, boolean fill) {
        float text_size = 18;
        String street = "Street";
        String state = "State";
        String city = "City";
        String zip = "Zip";

        LinearLayout parent = (LinearLayout)findViewById(R.id.extra_field);

        LinearLayout address_parent_layout = new LinearLayout(this);

        TextView address_label      = new TextView(this);

        LinearLayout streetLayout       = new LinearLayout(this);
        LinearLayout cityLayout         = new LinearLayout(this);
        LinearLayout stateLayout        = new LinearLayout(this);
        LinearLayout zipLayout          = new LinearLayout(this);

        TextView street_textview        = new TextView(this);
        TextView city_textview          = new TextView(this);
        TextView state_textview         = new TextView(this);
        TextView zip_textview           = new TextView(this);

        EditText street_edittext        = new EditText(this);
        EditText city_edittext          = new EditText(this);
        EditText state_edittext         = new EditText(this);
        EditText zip_edittext           = new EditText(this);

        LinearLayout.LayoutParams layoutParams    = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams subLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp_to_px(50));
        LinearLayout.LayoutParams textview_params = new LinearLayout.LayoutParams(dp_to_px(100), dp_to_px(50));
        LinearLayout.LayoutParams editview_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp_to_px(50));

        address_label.setLayoutParams(textview_params);
        address_label.setText("Address");
        address_label.setTextSize(20);
        address_label.setGravity(Gravity.CENTER_VERTICAL);

        //Set Layout Parameters
        streetLayout.setLayoutParams(subLayoutParams);
        cityLayout.setLayoutParams(subLayoutParams);
        stateLayout.setLayoutParams(subLayoutParams);
        zipLayout.setLayoutParams(subLayoutParams);

        street_textview.setLayoutParams(textview_params);
        city_textview.setLayoutParams(textview_params);
        state_textview.setLayoutParams(textview_params);
        zip_textview.setLayoutParams(textview_params);

        street_edittext.setLayoutParams(editview_params);
        city_edittext.setLayoutParams(editview_params);
        state_edittext.setLayoutParams(editview_params);
        zip_edittext.setLayoutParams(editview_params);

        address_parent_layout.setLayoutParams(layoutParams);

        streetLayout.setOrientation(LinearLayout.HORIZONTAL);
        cityLayout.setOrientation(LinearLayout.HORIZONTAL);
        stateLayout.setOrientation(LinearLayout.HORIZONTAL);
        zipLayout.setOrientation(LinearLayout.HORIZONTAL);
        address_parent_layout.setOrientation(LinearLayout.VERTICAL);

        //Set extra parameters
        street_textview.setText(street);
        street_textview.setTextSize(text_size);
        street_edittext.setHint(street);
        street_edittext.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        city_textview.setText(city);
        city_textview.setTextSize(text_size);
        city_edittext.setHint(city);
        city_edittext.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        state_textview.setText(state);
        state_textview.setTextSize(text_size);
        state_edittext.setHint(state);
        state_edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        state_edittext.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        zip_textview.setText(zip);
        zip_textview.setTextSize(text_size);
        zip_edittext.setHint(zip);
        zip_edittext.setInputType(InputType.TYPE_CLASS_NUMBER);

        //Add views
        streetLayout.addView(street_textview);
        streetLayout.addView(street_edittext);

        cityLayout.addView(city_textview);
        cityLayout.addView(city_edittext);

        stateLayout.addView(state_textview);
        stateLayout.addView(state_edittext);

        zipLayout.addView(zip_textview);
        zipLayout.addView(zip_edittext);

        address_parent_layout.addView(address_label);
        address_parent_layout.addView(streetLayout);
        address_parent_layout.addView(cityLayout);
        address_parent_layout.addView(stateLayout);
        address_parent_layout.addView(zipLayout);

        address_parent_layout.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.bottomborder, null));
        address_parent_layout.setPadding(dp_to_px(10), 0, 0, dp_to_px(10));

        parent.addView(address_parent_layout);
        parent.setVisibility(View.VISIBLE);

        if(fill)
        {
            street_edittext.setText(edit_text_text.get(STREET));
            city_edittext.setText(edit_text_text.get(CITY));
            state_edittext.setText(edit_text_text.get(STATE));
            zip_edittext.setText(edit_text_text.get(ZIP));
        }

        addressEditTexts.add(street_edittext);
        addressEditTexts.add(city_edittext);
        addressEditTexts.add(state_edittext);
        addressEditTexts.add(zip_edittext);

        Address_Showing = true;
    }

    public void randomize_contact(MenuItem item)
    {
        cHeightFt.setText(Integer.toString(randInt(3, 7)));
        cHeightIn.setText(Integer.toString(randInt(0, 11)));
        cWeight.setText(Integer.toString(randInt(100, 300)));

        int my_type1 = randInt(CONSTANT.NORMAL, CONSTANT.FAIRY) - 1;
        int my_type2;
        int my_type_chance = randInt(0,100);

        if(my_type_chance > CONSTANT.NONE_TYPE_RAND_CHANCE)
        {
            do
            {
                my_type2 = randInt(CONSTANT.NONE, CONSTANT.FAIRY);
            }while(my_type2 == my_type1);
        }
        else
        {
            my_type2 = CONSTANT.NONE;
        }

        type1.setSelection(my_type1);
        type2.setSelection(my_type2);

        cDescription.setText(CONSTANT.DEFAULT_DESCRIPTION_TEXT[randInt(0,5)]);

    }

    private int randInt(int min, int max)
    {
        Random rand = new Random();
        int randomNumber = rand.nextInt((max - min) + 1) + min;
        return randomNumber;
    }

}
