package my_app.example.my_adventures;

import android.content.Context;
import android.content.ContextWrapper;

public class Files {

    public static boolean doesExist(Context context, String folder, String filename) {
        ContextWrapper cw = new ContextWrapper(context);
        java.io.File directory = cw.getDir(folder, Context.MODE_PRIVATE);
        java.io.File file = new java.io.File(directory, filename);
        return file.exists();
    }

    public static boolean removeFile(String folder, String filename, Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        java.io.File directory = cw.getDir(folder, Context.MODE_PRIVATE);
        java.io.File file = new java.io.File(directory, filename);
        return file.delete();
    }

    public static String getPath(Context context, String folder, String filename){
        ContextWrapper cw = new ContextWrapper(context);
        java.io.File directory = cw.getDir(folder, Context.MODE_PRIVATE);
        java.io.File file = new java.io.File(directory, filename);
        return file.getPath();
    }

}
