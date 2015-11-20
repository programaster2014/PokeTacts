package com.programasterapps.poketacts;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Roberto Brunner on 6/21/2015.
 */
public class results_fragment extends Fragment {

    static final String TAG = MainContactsList.class.getSimpleName();

    static final String ID              = "id";
    static final String NAME 			= "name";
    static final String HOME_NUMBER 	= "home_number";
    static final String WORK_NUMBER 	= "work_number";
	static final String MOBILE_NUMBER 	= "mobile_number";
	static final String HEIGHT_FT		= "height_ft";
    static final String HEIGHT_IN       = "height_in";
	static final String WEIGHT 			= "weight";
	static final String ADDRESS 		= "address";
	static final String DESCRIPTION 	= "description";
	static final String TYPE_1 			= "type1";
	static final String TYPE_2 			= "type2";
    static final String PHOTO           = "contact_photo";
    static final String NUMBER_ARRAY    = "number_array";
    static final String NUMBER_TYPES    = "number_types";

    static final String SAVE_CONTACT    = "savedContact";

    private static final int MOBILE     = 0;
    private static final int HOME       = 1;
    private static final int WORK       = 2;

    private GradientDrawable type_shape;

    public static results_fragment newInstance(my_contact contact)
    {
        results_fragment my_results = new results_fragment();


        my_results.setArguments(setArgs(contact));

        return my_results;
    }

    public static Bundle setArgs(my_contact contact)
    {
        Bundle args = new Bundle();
        //Put relevant information for the details fragment in the bundle
        //for display
        args.putString(ID, contact.getId());
        args.putString(NAME, contact.getName());
        args.putString(HOME_NUMBER, contact.getHome());
        args.putString(WORK_NUMBER, contact.getWork());
        args.putString(MOBILE_NUMBER, contact.getMobile());
        args.putString(ADDRESS, contact.getAddress());
        args.putString(DESCRIPTION, contact.getDescription());

        args.putLong(PHOTO, contact.getContact_photo());

        args.putInt(HEIGHT_FT, contact.getHeight_ft());
        args.putInt(HEIGHT_IN, contact.getHeight_in());
        args.putInt(WEIGHT, contact.getWeight());
        args.putInt(TYPE_1, contact.getType_1());
        args.putInt(TYPE_2, contact.getType_2());

        args.putStringArrayList(NUMBER_ARRAY, contact.getPhone_numbers());
        args.putIntegerArrayList(NUMBER_TYPES, contact.getPhone_types());

        return args;
    }

	//Get functions for the bundle arguments
    public String getID() {return getArguments().getString(ID);}
    public String getName() {return getArguments().getString(NAME);}
    public String getHomeNumber(){return getArguments().getString(HOME_NUMBER);}
    public String getWorkNumber(){return getArguments().getString(WORK_NUMBER);}
    public String getMobileNumber(){return getArguments().getString(MOBILE_NUMBER);}
    public String getAddress(){return getArguments().getString(ADDRESS);}
    public String getDescription(){return getArguments().getString(DESCRIPTION);}
    public int getHeightFt() {return getArguments().getInt(HEIGHT_FT);}
    public int getHeightIn() {return getArguments().getInt(HEIGHT_IN);}
    public int getWeight()   {return getArguments().getInt(WEIGHT);}
    public int getType1()    {return getArguments().getInt(TYPE_1);}
    public int getType2()    {return getArguments().getInt(TYPE_2);}
    public ArrayList<String> getPhones() {return getArguments().getStringArrayList(NUMBER_ARRAY);}
    public ArrayList<Integer> getTypes() {return getArguments().getIntegerArrayList(NUMBER_TYPES);}

    public Bitmap getPhoto()
    {
        Bitmap myPhoto = null;
        long data_id = getArguments().getLong(PHOTO);

        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                getActivity().getContentResolver(),
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, data_id)
        );

        if(input != null)
        {
            myPhoto = BitmapFactory.decodeStream(input);
            try{
                input.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return myPhoto;
        }
        return null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null)
        {
            my_contact saved = (my_contact)savedInstanceState.getParcelable(SAVE_CONTACT);
            Bundle my_args = setArgs(saved);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.results_fragment_layout, container, false);

        String aquired_string;
        LinearLayout aquired_layout;
        Bitmap contact_pic;

		//Find all needed views
        TextView name = (TextView) view.findViewById(R.id.name);
        ImageView contact_image = (ImageView) view.findViewById(R.id.results_photo);

		//Using bundle arguments, get all needed information and set the appropriate
		//Textview text
        if(name != null) {name.setText(getName());}

        set_textview_int(view, R.id.type_1, getType1(), "Type 1", true, -1);
        set_textview_int(view, R.id.type_2, getType2(), "Type 2", true, -1);
        set_textview_int(view, R.id.weight, getWeight(), "Weight", false, 0);
        set_textview_int(view, R.id.height, getHeightFt(), "Height", false, 1);
        set_textview_int(view, R.id.height, getHeightIn(), "Height", false, 2);

        set_textview_text(view, R.id.description_layout, R.id.description, getDescription(), "Description");

        create_number_display(view, getPhones(), getTypes());
        set_textview_text(view, R.id.contact_address_layout, R.id.contact_address, getAddress(), "Address");

        if(contact_image != null)
        {
            contact_pic = getPhoto();
            if(contact_pic != null){contact_image.setImageBitmap(contact_pic);}
        }

        return view;
    }

    private void create_number_display(View view, ArrayList<String> phones, ArrayList<Integer> types) {
        boolean home_added = false;
        boolean work_added = false;
        boolean mobile_added = false;

        LinearLayout home_layout   = (LinearLayout)view.findViewById(R.id.home_number_layout);
        LinearLayout mobile_layout = (LinearLayout)view.findViewById(R.id.mobile_number_layout);
        LinearLayout work_layout   = (LinearLayout)view.findViewById(R.id.work_number_layout);

        if(phones.size() > 0 && types.size() > 0)
        {
            for(int counter = 0; counter < phones.size(); counter++)
            {
                String theNumber = phones.get(counter);

                RelativeLayout.LayoutParams number_layout_params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp_to_px(50));
                RelativeLayout.LayoutParams number_textview_params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                RelativeLayout.LayoutParams imageButtonCallParams = new RelativeLayout.LayoutParams(
                        dp_to_px(45), ViewGroup.LayoutParams.MATCH_PARENT);
                RelativeLayout.LayoutParams imageButtonTextParams = new RelativeLayout.LayoutParams(
                        dp_to_px(45), ViewGroup.LayoutParams.MATCH_PARENT);

                RelativeLayout number_layout = new RelativeLayout(getActivity());

                TextView number_textview = new TextView(getActivity());
                ImageButton callButton = new ImageButton(getActivity());
                ImageButton textButton = new ImageButton(getActivity());

                //Set layout params
                textButton.setId(counter + 111);
                imageButtonTextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                callButton.setId(counter + 211);
                imageButtonCallParams.addRule(RelativeLayout.LEFT_OF, textButton.getId());

                number_textview_params.addRule(RelativeLayout.LEFT_OF, callButton.getId());

                number_layout.setLayoutParams(number_layout_params);
                number_textview.setLayoutParams(number_textview_params);
                callButton.setLayoutParams(imageButtonCallParams);
                textButton.setLayoutParams(imageButtonTextParams);

                number_layout.setPadding(dp_to_px(10), 0, dp_to_px(10), 0);

                textButton.setImageResource(R.drawable.icon_text_message);
                textButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                textButton.setTag(phones.get(counter));

                callButton.setImageResource(R.drawable.icon_phone_black);
                callButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                callButton.setTag(phones.get(counter));

                number_textview.setText(phones.get(counter));
                number_textview.setTextSize(18);
                number_textview.setGravity(Gravity.CENTER_VERTICAL);

                number_layout.addView(textButton);
                number_layout.addView(callButton);
                number_layout.addView(number_textview);

                textButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        text(v.getTag().toString());
                    }
                });

                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        call(v.getTag().toString());
                    }
                });

                switch (types.get(counter))
                {
                    case HOME:
                        home_layout.addView(number_layout);
                        home_added = true;
                        break;
                    case MOBILE:
                        mobile_layout.addView(number_layout);
                        mobile_added = true;
                        break;
                    case WORK:
                        work_layout.addView(number_layout);
                        work_added = true;
                        break;
                    default:
                }
            }
        }

        if(!home_added)
        {
            TextView myText = (TextView)view.findViewById(R.id.homeNumberText);
            myText.setVisibility(View.GONE);
            home_layout.setVisibility(View.GONE);
        }
        if(!work_added)
        {
            TextView myText = (TextView)view.findViewById(R.id.workNumberText);
            myText.setVisibility(View.GONE);
            work_layout.setVisibility(View.GONE);
        }
        if(!mobile_added)
        {
            TextView myText = (TextView)view.findViewById(R.id.mobileNumberText);
            myText.setVisibility(View.GONE);
            mobile_layout.setVisibility(View.GONE);
        }


    }

    public int dp_to_px(int sizeInDp)
    {
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (sizeInDp*scale + 0.5f);

        return dpAsPixels;
    }


    public void set_textview_text(View myView, int layout_id, int textView_id, String data_string,
                                  String TextView_Error_Message)
    {
        if(data_string != null)
        {
            if(!data_string.isEmpty())
            {
                TextView updated_view = (TextView) myView.findViewById(textView_id);

                if(updated_view != null)
                {
                    updated_view.setText(data_string);
                }
            }
            else
            {
                LinearLayout aquired_layout = (LinearLayout) myView.findViewById(layout_id);
                if (aquired_layout != null) {
                    aquired_layout.setVisibility(View.GONE);
                }
            }
        }
    }


    public void set_textview_int(View myView, int textView_id, int data_int,
                                  String TextView_Error_Message, boolean type_view, int textview_select)
    {
        TextView updated_view = (TextView) myView.findViewById(textView_id);
        if(data_int != 0)
        {
            if(updated_view != null)
            {
                if(type_view)
                {
                    //updated_view.setBackgroundColor(Color.parseColor(CONSTANT.type_color[data_int]));
                    type_shape = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[] {Color.parseColor(CONSTANT.type_color[data_int]),
                                       Color.parseColor(CONSTANT.type_end_color[data_int])}
                    );

                    type_shape.setGradientRadius(90);
                    type_shape.setCornerRadius(20);

                    if(data_int != 0)
                    {
                        type_shape.setStroke(2, Color.parseColor("#555555"));
                    }
                    else
                    {
                        type_shape.setStroke(2, Color.parseColor("#FFFFFF"));
                    }

                    updated_view.setBackground(type_shape);
                    updated_view.setText(CONSTANT.type_text[data_int]);
                }
                else {
                    switch (textview_select)
                    {
                        case 0:
                            updated_view.setText("Weight:  " + Integer.toString(data_int) + " lbs");
                            break;
                        case 1:
                            updated_view.setText("Height:  " + Integer.toString(data_int) + " ft. ");
                            break;
                        case 2:
                            updated_view.append(" " + Integer.toString(data_int) + " in.");
                            break;
                    }
                }
            }
        }

        if(!type_view)
        {
            if (updated_view == null || (data_int == 0 && textview_select != 2)) {
                updated_view.setVisibility(View.GONE);
            }
        }
    }



    public void call(String number)
    {
        Intent call_intent = new Intent(Intent.ACTION_CALL);
        call_intent.setData(Uri.parse("tel:"+number.replace("[^0-9]", "")));
        startActivity(call_intent);
    }

    public void text(String number)
    {
        Intent text_intent = new Intent(Intent.ACTION_VIEW);
        text_intent.setData(Uri.parse("sms:" + number.replace("[^0-9]", "")));
        startActivity(text_intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        my_contact savedContact = new my_contact();
            savedContact.setId(getID());
            savedContact.setName(getName());
            savedContact.setHeight_ft(getHeightFt());
            savedContact.setHeight_in(getHeightIn());
            savedContact.setWeight(getWeight());
            savedContact.setType_1(getType1());
            savedContact.setType_2(getType2());
            savedContact.setMobile(getMobileNumber());
            savedContact.setHome(getHomeNumber());
            savedContact.setWork(getWorkNumber());
            savedContact.setDescription(getDescription());
            savedContact.setAddress(getAddress());
            savedContact.setContact_photo(getArguments().getLong(PHOTO));
        outState.putParcelable(SAVE_CONTACT, savedContact);
        super.onSaveInstanceState(outState);
    }
}
