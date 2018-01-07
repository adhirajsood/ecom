package com.heady.headyecom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.heady.headyecom.models.Categories;
import com.heady.headyecom.models.Category;
import com.heady.headyecom.models.Product;
import com.heady.headyecom.models.Product_;
import com.heady.headyecom.models.Ranking;
import com.heady.headyecom.models.Variant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by adhirajsood on 07/01/18.
 */

public class DBHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "headyEcom";

    private static final String TABLE_CATEGORY = "Category";
    public static final String KEY_CAT_ID = "CatID";
    public static final String KEY_HAS_CHILD = "hasChild";
    public static final String KEY_CAT_NAME = "CatName";

    private static final String TABLE_PRODUCTS = "Products";
    public static final String KEY_PROD_ID = "ProdId";
    public static final String KEY_PROD_NAME = "ProdName";
    public static final String KEY_TAX_NAME = "TaxName";
    public static final String KEY_TAX_VALUE = "TaxValue";
    public static final String KEY_PROD_SHARES = "ShareCount";
    public static final String KEY_PROD_ORDERS = "OrderCount";
    public static final String KEY_PROD_VIEWS = "ViewCount";



    private static final String TABLE_VARIANTS = "Variants";
    public static final String KEY_VAR_ID = "VarID";
    public static final String KEY_VAR_COLOR = "VarColor";
    public static final String KEY_VAR_SIZE = "VarSize";
    public static final String KEY_VAR_PRICE = "VarPrice";

    private static final String TABLE_SUB_CAT = "SubCategories";
    private static final String KEY_PARENT_ID = "ParentId";
    private static final String KEY_CHILD_ID = "ChildId";





    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static DBHandler instance;


    public static synchronized DBHandler getHelper(Context context) {
        if (instance == null)
            instance = new DBHandler(context);

        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + "("
                + KEY_CAT_ID + " INT PRIMARY KEY UNIQUE,"
                + KEY_HAS_CHILD + " BOOLEAN, "
                + KEY_CAT_NAME + " TEXT )";



        String CREATE_TABLE_SUB_CAT = "CREATE TABLE " + TABLE_SUB_CAT + "("
                + KEY_PARENT_ID + " INT,"
                + KEY_CHILD_ID + " INT )";



        String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_PROD_ID + " INT PRIMARY KEY UNIQUE,"
                + KEY_PROD_NAME + " TEXT,"
                + KEY_PROD_ORDERS + " INT,"
                + KEY_PROD_SHARES + " INT,"
                + KEY_PROD_VIEWS + " INT,"
                + KEY_TAX_NAME + " TEXT,"
                + KEY_TAX_VALUE + " INT,"
                + KEY_CAT_ID + " INT, "
                + "FOREIGN KEY("+KEY_CAT_ID+") REFERENCES "+TABLE_CATEGORY+"("+KEY_CAT_ID+"))";

        String CREATE_TABLE_VARIANT = "CREATE TABLE "+ TABLE_VARIANTS + "("
                + KEY_VAR_ID + " INT PRIMARY KEY UNIQUE,"
                + KEY_VAR_SIZE +" INT,"
                + KEY_VAR_COLOR +" TEXT,"
                + KEY_VAR_PRICE +" INT,"
                + KEY_PROD_ID +" INT, "
                + "FOREIGN KEY("+KEY_PROD_ID+") REFERENCES "+TABLE_PRODUCTS+"("+KEY_PROD_ID+"))";


        try {
            System.out.println(CREATE_TABLE_SUB_CAT);
            db.execSQL(CREATE_TABLE_CATEGORY);
            db.execSQL(CREATE_TABLE_PRODUCTS);
            db.execSQL(CREATE_TABLE_VARIANT);
            db.execSQL(CREATE_TABLE_SUB_CAT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Create tables again
        onCreate(db);
    }

    public void addData(Categories categories) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();


            for (Category category:categories.getCategories()){
                if (category.getChildCategories().size()>0){

                    ContentValues values = new ContentValues();
                    values.put(KEY_CAT_ID, category.getId());
                    values.put(KEY_CAT_NAME, category.getName());
                    values.put(KEY_HAS_CHILD,true);

                    // Inserting Row
                    db.insertWithOnConflict(TABLE_CATEGORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    for (Integer integer:category.getChildCategories()) {
                        values = new ContentValues();
                        values.put(KEY_PARENT_ID, category.getId());
                        values.put(KEY_CHILD_ID,integer);
                        db.insertWithOnConflict(TABLE_SUB_CAT, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                    }

                }else {

                    ContentValues values = new ContentValues();
                    values.put(KEY_CAT_ID, category.getId());
                    values.put(KEY_CAT_NAME, category.getName());
                    values.put(KEY_HAS_CHILD,false);

                    // Inserting Row
                    db.insertWithOnConflict(TABLE_CATEGORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    if (category.getProducts().size()>0) {
                        for (Product product:category.getProducts()) {
                            values = new ContentValues();
                            values.put(KEY_PROD_ID, product.getId());
                            values.put(KEY_PROD_NAME, product.getName());
                            values.put(KEY_CAT_ID,category.getId());
                            values.put(KEY_TAX_NAME,product.getTax().getName());
                            values.put(KEY_TAX_VALUE,product.getTax().getValue());
                            db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);


                            if (product.getVariants().size()>0){
                                for (Variant variant:product.getVariants()) {
                                    values = new ContentValues();
                                    values.put(KEY_VAR_ID, variant.getId());
                                    values.put(KEY_VAR_COLOR, variant.getColor());
                                    values.put(KEY_VAR_PRICE, variant.getPrice());
                                    values.put(KEY_VAR_SIZE, variant.getSize());
                                    values.put(KEY_PROD_ID, product.getId());
                                    db.insertWithOnConflict(TABLE_VARIANTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                                }
                            }
                        }
                    }
                }

            }


            if (categories.getRankings()!=null){
                updateRankings(categories);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    public JSONArray getStores() {
        JSONArray storesLoc = new JSONArray();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STORES +" ORDER BY "+KEY_DISTANCE+" ASC";

        System.out.println(selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int i = 0;

        try{

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    JSONObject locationResponse = new JSONObject();
                    try {
                        locationResponse.put(KEY_BRAND_ID, cursor.getString(0));
                        locationResponse.put(KEY_BRAND_NAME, cursor.getString(1));
                        locationResponse.put(KEY_NEIGHBOURHOOD_NAME, cursor.getString(2));
                        locationResponse.put(KEY_LOGO_URL, cursor.getString(3));
                        locationResponse.put(KEY_LATITUDE, cursor.getString(4));
                        locationResponse.put(KEY_LONGITUDE, cursor.getString(5));
                        locationResponse.put(KEY_DISTANCE, cursor.getString(6));

                        storesLoc.put(i, locationResponse);
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            if(!cursor.isClosed()) {
                cursor.close();
            }
        }

        //db.close();
        return storesLoc;
    }*/


    public ArrayList<Category> getCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY +" ORDER BY "+KEY_HAS_CHILD+" DESC";

        System.out.println(selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int i = 0;

        try{

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Category category = new Category();
                        category.setId(cursor.getInt(0));
                        category.setHasChildCount(cursor.getInt(1));
                        category.setName(cursor.getString(2));
                        categories.add(category);
                        i++;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            if(!cursor.isClosed()) {
                cursor.close();
            }
        }

        //db.close();
        return categories;
    }


    public void updateRankings(Categories categories) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "";
        for (Ranking ranking :categories.getRankings()) {
            for (Product_ product:ranking.getProducts()) {
                if (ranking.getRanking().contains("Most Viewed Products")) {
                    updateQuery = "update " + TABLE_PRODUCTS + " set " + KEY_PROD_VIEWS + " = '" + product.getViewCount() + "' where " + KEY_PROD_ID + " = " + product.getId();
                } else if (ranking.getRanking().contains("Most OrdeRed Products")) {
                    updateQuery = "update " + TABLE_PRODUCTS + " set " + KEY_PROD_ORDERS + " = '" + product.getOrderCount() + "' where " + KEY_PROD_ID + " = " + product.getId();

                } else if (ranking.getRanking().contains("Most ShaRed Products")) {
                    updateQuery = "update " + TABLE_PRODUCTS + " set " + KEY_PROD_SHARES + " = '" + product.getShares() + "' where " + KEY_PROD_ID + " = " + product.getId();

                }
            }
            }

        db.execSQL(updateQuery);
    }
}
