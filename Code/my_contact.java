package com.programasterapps.poketacts;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Roberto Brunner on 6/26/2015.
 */
public class my_contact implements Parcelable{

    private String id;
    private String name;

    //phone number variables
    private String mobile;
    private String work;
    private String home;

    private ArrayList<String> phone_numbers;
    private ArrayList<Integer>phone_types;

    private String address;
    private String description;

    private int type_1;
    private int type_2;
    private int height_ft;
    private int height_in;
    private int weight;

    private long contact_photo;

    public my_contact() {
        this.id = "";
        this.name = "";
        this.height_ft = 0;
        this.height_in = 0;
        this.weight = 0;
        this.mobile = "";
        this.work = "";
        this.home = "";
        this.address = "";
        this.description = "";
        this.type_1 = 0;
        this.type_2 = 0;
        this.contact_photo = 0;
        this.phone_numbers = new ArrayList<String>();
        this.phone_types = new ArrayList<Integer>();
    }

    private my_contact(Parcel in)
    {
        this.id = in.readString();
        this.name = in.readString();
        this.height_ft = in.readInt();
        this.height_in = in.readInt();
        this.weight = in.readInt();
        this.mobile = in.readString();
        this.work = in.readString();
        this.home = in.readString();
        this.address = in.readString();
        this.description = in.readString();
        this.type_1 = in.readInt();
        this.type_2 = in.readInt();
        this.contact_photo = in.readLong();
        this.phone_numbers =  convertStringArray_to_Arraylist(in.createStringArray());
        this.phone_types = convertIntArray_to_ArrayList(in.createIntArray());
    }

    public void clear_contact()
    {
        this.id = "";
        this.name = "";
        this.height_ft = 0;
        this.height_in = 0;
        this.weight = 0;
        this.mobile = "";
        this.work = "";
        this.home = "";
        this.address = "";
        this.description = "";
        this.type_1 = 0;
        this.type_2 = 0;
        this.contact_photo = 0;
        this.phone_numbers.clear();
        this.phone_types.clear();
    }

    public int getHeight_ft() {return height_ft;}
    public void setHeight_ft(int height_ft) {this.height_ft = height_ft;}
    public int getHeight_in() {return height_in;}
    public void setHeight_in(int height_in) {this.height_in = height_in;}
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public int getWeight() {return weight;}
    public void setWeight(int weight) {this.weight = weight;}
    public String getMobile() {return mobile;}
    public void setMobile(String mobile) {this.mobile = mobile;}
    public String getWork() {return work;}
    public void setWork(String work) {this.work = work;}
    public String getHome() {return home;}
    public void setHome(String home) {this.home = home;}
    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public int getType_1() {return type_1;}
    public void setType_1(int type_1) {this.type_1 = type_1;}
    public int getType_2() {return type_2;}
    public void setType_2(int type_2) {this.type_2 = type_2;}
    public long getContact_photo() {return contact_photo;}
    public void setContact_photo(long contact_photo) {this.contact_photo = contact_photo;}

    public ArrayList<String> getPhone_numbers() {
        return phone_numbers;
    }

    public void setPhone_numbers(ArrayList<String> phone_numbers) {
        this.phone_numbers = phone_numbers;
    }

    public ArrayList<Integer> getPhone_types() {
        return phone_types;
    }

    public void setPhone_types(ArrayList<Integer> phone_types) {
        this.phone_types = phone_types;
    }

    public void addNumber(String number, int type)
    {
        phone_numbers.add(number);
        phone_types.add(type);
    }




    /*********************************************PARCEL FUNCTIONS******************************/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.height_ft);
        dest.writeInt(this.height_in);
        dest.writeInt(this.weight);
        dest.writeString(this.mobile);
        dest.writeString(this.work);
        dest.writeString(this.home);
        dest.writeString(this.address);
        dest.writeString(this.description);
        dest.writeInt(this.type_1);
        dest.writeInt(this.type_2);
        dest.writeLong(this.contact_photo);
        dest.writeStringArray(this.phone_numbers.toArray(new String[phone_numbers.size()]));
        dest.writeIntArray(convertIntegerArray_to_intArray(this.phone_types));
    }

    public static int[] convertIntegerArray_to_intArray(ArrayList<Integer> myints)
    {
        int[] ret = new int[myints.size()];

        Iterator<Integer> integerIterator = myints.iterator();
        for(int counter = 0; counter < ret.length; counter++)
        {
            ret[counter] = integerIterator.next().intValue();
        }
        return ret;
    }

    public static ArrayList<Integer> convertIntArray_to_ArrayList(int[] myints)
    {
        ArrayList<Integer> ret = new ArrayList<Integer>();

        for(int counter = 0; counter < myints.length; counter++)
        {
            ret.add(myints[counter]);
        }

        return ret;
    }

    public static ArrayList<String> convertStringArray_to_Arraylist(String[] mystrings)
    {
        ArrayList<String> ret = new ArrayList<String>();

        for(int counter = 0; counter < mystrings.length; counter++)
        {
            ret.add(mystrings[counter]);
        }

        return ret;
    }

    public static final Parcelable.Creator<my_contact> CREATOR =
            new Parcelable.Creator<my_contact>(){
                public my_contact createFromParcel(Parcel in)
                {
                    return new my_contact(in);
                }

                public my_contact[] newArray(int size)
                {
                    return new my_contact[size];
                }
            };
}
