package com.postit.classified.postit.database;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Transaction;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.REPLACE;
import static com.postit.classified.postit.database.AppDatabase.*;

@Database( version = 1, entities = {User.class}, exportSchema = false)
@TypeConverters( Converters.class )
public abstract class AppDatabase extends RoomDatabase
{
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public synchronized static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    private static AppDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder( context.getApplicationContext(), AppDatabase.class, "post_it").build();
    }

    @Entity( tableName = "users" )
    public static class User
    {
        @PrimaryKey
        public int id;

        @ColumnInfo(name = "user_id")
        public int userId;

        @ColumnInfo(name = "first_name")
        public String firstName;

        @ColumnInfo(name = "last_name")
        public String lastName;

        @ColumnInfo(name = "user_name")
        public String userName;

        @ColumnInfo(name = "email")
        public String email;

        @ColumnInfo(name = "gender")
        public String gender;

        @ColumnInfo(name = "phone")
        public String phone;

        @ColumnInfo(name = "country_id")
        public int countryId;

        @ColumnInfo(name = "photo_link")
        public String photoLink;

        @ColumnInfo(name = "user_type")
        public String userType;

        @ColumnInfo(name = "user_state")
        public UserState userState;

        @ColumnInfo(name = "last_login")
        public DateTime lastLogin;

        @ColumnInfo(name = "created_at")
        public DateTime createdAt;

        public User(){}

        public User(Intent intent) {
            userId = intent.getIntExtra( "id", -1 );
            firstName = intent.getStringExtra( "first_name" );
            lastName = intent.getStringExtra( "last_name" );
            userName = intent.getStringExtra( "user_name" );
            email = intent.getStringExtra( "email" );
            gender = intent.getStringExtra( "gender" );
            phone = intent.getStringExtra( "phone" );
            countryId = intent.getIntExtra( "country_id", -1 );
            photoLink = intent.getStringExtra( "photo_link" );
            userType = intent.getStringExtra( "user_type" );
            lastLogin = DateTime.parse( intent.getStringExtra( "last_login" ),
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss" ) );
            createdAt = DateTime.parse( intent.getStringExtra( "created_at" ),
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss" ));
            userState = User.UserState.LOGIN;
        }

        public void toIntent( Intent intent )
        {
            intent.putExtra( "id", userId );
            intent.putExtra( "first_name", firstName );
            intent.putExtra( "last_name", lastName );
            intent.putExtra( "user_name", userName );
            intent.putExtra( "email", email );
            intent.putExtra( "gender", gender );
            intent.putExtra( "phone", phone );
            intent.putExtra( "country_id", countryId );
            intent.putExtra( "photo_link", photoLink);
            intent.putExtra( "user_type", userType );
            intent.putExtra( "last_login", lastLogin );
            intent.putExtra( "created_at", createdAt );
            intent.putExtra( "user_action", "LOGIN" );
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", userId=" + userId +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", userName='" + userName + '\'' +
                    ", email='" + email + '\'' +
                    ", gender='" + gender + '\'' +
                    ", phone='" + phone + '\'' +
                    ", countryId=" + countryId +
                    ", photoLink='" + photoLink + '\'' +
                    ", userType='" + userType + '\'' +
                    ", userState=" + userState +
                    ", lastLogin=" + lastLogin +
                    ", createdAt=" + createdAt +
                    '}';
        }

        public static enum UserState{ LOGIN, LOGOUT }

    }

    @Dao
    public static abstract class UserDao
    {
        @Query("SELECT * FROM users WHERE id = :id" )
        abstract User getUsersById(int id);

        @Query("SELECT * FROM users WHERE user_id = :userId" )
        abstract User getUsersByUserId(int userId);

        @Query("SELECT * FROM users" )
        abstract List< User > getUsers();

        @Query("SELECT * FROM users WHERE user_state LIKE :state" )
        abstract User getUsersByState(User.UserState state);

        @Insert( onConflict = REPLACE )
        abstract void addUser( User user );

        @Update
        abstract void updateUser( User user );

        @Transaction
        public void logout()
        {
            User user = getUsersByState(User.UserState.LOGIN);
            user.userState = User.UserState.LOGOUT;
            updateUser( user );
        }

        @Transaction
        public User getLogin()
        {
            User user = getUsersByState(User.UserState.LOGIN);
            Log.d( "Login_Password", "This is the user " + user );
            return user;
        }

//        @Transaction
//        public void login( Intent intent )
//        {
//            User user = getUsersByUserId(intent.getIntExtra("id", -1));
//            if ( user != null )
//            {
//                user.userState = User.UserState.LOGIN;
//                updateUser( user );
//            }
//            else
//            {
//                user = new User( intent );
//                addUser( user );
//            }
//        }

        @Transaction
        public void login( Intent intent )
        {
            User user = new User( intent );
            User exist = getUsersByUserId(intent.getIntExtra("id", -1));
            if ( exist != null )
                updateUser( user );
            else
                addUser( user );
        }
    }

    public static class Converters
    {
        @TypeConverter
        public DateTime fromDuration( String time )
        {
            return DateTime.parse( time, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss" ) );
        }

        @TypeConverter
        public String toDuration( DateTime time )
        {
            return time.toString( DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss" ) );
        }

        @TypeConverter
        public User.UserState fromLoginState( String state )
        {
            return Enum.valueOf(User.UserState.class, state);
        }

        @TypeConverter
        public String toLoginState( User.UserState state )
        {
            return state.name();
        }
    }
}


