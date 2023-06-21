package shared.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import shared.*;
import shared.quiz.Quiz;
import shared.utility.GsonConverters.ZonedDateTimeConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;

public class GsonUtility {
    public static final Type ZONED_DATE_TIME_TYPE = new TypeToken<ZonedDateTime>(){}.getType();

    private static final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

    static RuntimeTypeAdapterFactory<LVKategorieElement> elementadapter = RuntimeTypeAdapterFactory.of(LVKategorieElement.class)
            .registerSubtype(LVKategorieElement.class)
            .registerSubtype(KategorieDatei.class)
            .registerSubtype(Quiz.class)
            .registerSubtype(Todo.class)
            .registerSubtype(Reminder.class)
            .registerSubtype(Lernkarte.class)
            .registerSubtype(Bewertung.class);
    static RuntimeTypeAdapterFactory<User> useradapter = RuntimeTypeAdapterFactory.of(User.class)
            .registerSubtype(User.class)
            .registerSubtype(Lehrende.class)
            .registerSubtype(Student.class);


    static final GsonBuilder preparedBuilder = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapterFactory(elementadapter)
            .registerTypeAdapterFactory(useradapter)
            .registerTypeAdapter(ZONED_DATE_TIME_TYPE, new ZonedDateTimeConverter());


    public static void registerType(RuntimeTypeAdapterFactory<?> adapter) {
        gsonBuilder.registerTypeAdapterFactory(adapter);
    }

    public static Gson getGson() {
        //return gsonBuilder.create();
        return preparedBuilder.create();
    }
}
