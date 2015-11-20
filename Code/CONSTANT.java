package com.programasterapps.poketacts;

import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by Rob on 7/19/2015.
 */
public final class CONSTANT {
	//Having a private constructor makes sure that a new object is not created
	private CONSTANT(){}
	
	/************************* LOG TAG CONSTANTS *************************/
	public static final String TAG 				= MainContactsList.class.getSimpleName();
	
	/************************* KEY-VALUE STRING CONSTANTS *************************/
	public static final String DB_CREATED          = "DB_CREATED";

    //Intent Keys
    public static final String PICKED_CONTACT_ID = "cid";
    public static final String PICKED_CONTACT_NAME = "cName";
    public static final String PICKED_CONTACT_HEIGHT_FT = "chfeet";
    public static final String PICKED_CONTACT_HEIGHT_IN = "chin";
    public static final String PICKED_CONTACT_WEIGHT = "cweight";
    public static final String PICKED_CONTACT_TYPE1 = "ct1";
    public static final String PICKED_CONTACT_TYPE2 = "ct2";
    public static final String PICKED_CONTACT_MOBILE = "cmobile";
    public static final String PICKED_CONTACT_HOME = "chome";
    public static final String PICKED_CONTACT_WORK = "cwork";
    public static final String PICKED_CONTACT_DESCRIPTION = "cdescription";
    public static final String PICKED_CONTACT_PHOTO = "cphoto";
    public static final String PICKED_CONTACT_ADDRESS = "cAddress";

    public static final String PICKED_CONTACT_PARCEL = "contact_parcel";

	
	/************************* CONTENT URI CONSTANTS *************************/
	public static final Uri CONTACTS_URI 		= ContactsContract.Contacts.CONTENT_URI;
    public static final Uri DATA_CONTACTS_URI 	= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	
	
	/************************* POKEMON DESCRIPTION DEFAULTS *************************/
	public static final String[] DEFAULT_DESCRIPTION_TEXT=
	{
		"Using the tentacles sprouting from its sides, it absorbs the life-force from its surroundings for nourishment. It traps unwary " +
			"travelers who are often never seen again.",
		"It puffs its cheeks to ward off predators. When attacked, it shoots poisonous quills from its back.",
		"It never strays far from home. In a group, they group together to keep warm on cold nights and have strong ties to its brothers and sisters.",
		"An underwater dweller; it is most commonly seen nipping at the toes of swimmers. Non-aggressive in nature, it packs a powerful electric " +
            "shock.",
		"Rarely seen in the wild, it uses its psychic powers to avoid potential dangers. It is said to have the power to travel through different dimension.",
		"A trickster in nature; it has the interesting ability to rapidly grow other people's hair. It uses its own to trap its opponents and attack."
	};

	/************************* POKEMON TYPES CONSTANTS *************************/
    public static final int NONE     = 0;
    public static final int NORMAL   = 1;
    public static final int FIRE     = 2;
    public static final int WATER    = 3;
    public static final int ELECTRIC = 4;
    public static final int GRASS    = 5;
    public static final int ICE      = 6;
    public static final int FIGHTING = 7;
    public static final int POISON   = 8;
    public static final int GROUND   = 9;
    public static final int FLYING   = 10;
    public static final int PSYCHIC  = 11;
    public static final int BUG      = 12;
    public static final int ROCK     = 13;
    public static final int GHOST    = 14;
    public static final int DRAGON   = 15;
    public static final int DARK     = 16;
    public static final int STEEL    = 17;
    public static final int FAIRY    = 18;

    public static final int NONE_TYPE_RAND_CHANCE = 40;

    public static final String[] type_text = {
            "",
            "NORMAL",
            "FIRE",
            "WATER",
            "ELECTRIC",
            "GRASS",
            "ICE",
            "FIGHTING",
            "POISON",
            "GROUND",
            "FLYING",
            "PSYCHIC",
            "BUG",
            "ROCK",
            "GHOST",
            "DRAGON",
            "DARK",
            "STEEL",
            "FAIRY"
    };

    public static final String[] type_color = {
            "#FFFFFF",
            "#AAA87F",
            "#F67D4E",
            "#6595EA",
            "#FFCA08",
            "#87C45D",
            "#96D6D6",
            "#C13128",
            "#89488C",
            "#F5DC77",
            "#A990D4",
            "#E75F87",
            "#ACBB2A",
            "#BDAE2D",
            "#5B5265",
            "#753FDF",
            "#625044",
            "#C4C7D6",
            "#F2D0F3"
    };

    public static final String[] type_end_color = {
            "#FFFFFF",
            "#C6C4A9",
            "#F9AA8B",
            "#A5C2F3",
            "#FFD747",
            "#A6D487",
            "#C4E8E8",
            "#DC5F56",
            "#AF6BB3",
            "#F9EBB3",
            "#D1C4E8",
            "#EF95B0",
            "#C7D548",
            "#D8CC5A",
            "#7A6E87",
            "#9A73E7",
            "#846C5C",
            "#F3F4F7",
            "#FAEFFB"
    };

    /************************* ON ACTIVITY FOR RESULTS CONSTANTS *************************/
    static final int SELECT_PICTURE = 0;
    static final int LOAD_EDIT_CONTACT = 1;

    static final String IMAGE_URI = "image_uri";

    static final String MY_ACCOUNT_NAME = "PokeTacts";
    static final String MY_ACCOUNT_TYPE = "PokeTacts_Type";

    static final String EDIT_CONTACT_ID = "my_edit_id";
    static final String EDIT_CONTACT_BOOL = "editing_contact";
}
