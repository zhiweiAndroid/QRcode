package com.example.shundai.erweimascan;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompressImageUtil {

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
   
    	  /* 质量压缩图片，图片占用内存减小，像素数不变，常用于上传 
             * @param image
    	     * @param size 期望图片的大小，单位为kb 
    	     * @param options 图片压缩的质量，取值1-100，越小表示压缩的越厉害,如输入30，表示压缩70% 
    	     * @return 
    	     */
    public static Bitmap compressImage(Bitmap image, int size, int options, File tempFile) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, size, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > size) {

            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            // 这里压缩options%，把压缩后的数据存放到baos中

            if (options > 0 && options < 100) {
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);

            }
        }
        image.recycle();
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveBitmapbyBitmap(tempFile, bitmap);
        return bitmap;
    }
    //第二：图片按比例大小压缩方法（根据路径获取图片并压缩）：

//public static Bitmap getimage(String srcPath) {
//    BitmapFactory.Options newOpts = new BitmapFactory.Options();
//    //开始读入图片，此时把options.inJustDecodeBounds 设回true了
//    newOpts.inJustDecodeBounds = true;
//    Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空
//
//    newOpts.inJustDecodeBounds = false;
//    int w = newOpts.outWidth;
//    int h = newOpts.outHeight;
//    //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
//    float hh = 800f;//这里设置高度为800f
//    float ww = 480f;//这里设置宽度为480f
//    //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
//    int be = 1;//be=1表示不缩放
//    if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
//        be = (int) (newOpts.outWidth / ww);
//    } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
//        be = (int) (newOpts.outHeight / hh);
//    }
//    if (be <= 0)
//        be = 1;
//    newOpts.inSampleSize = be;//设置缩放比例
//    //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
//    bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
//    return compressImage(bitmap,100,80);//压缩好比例大小后再进行质量压缩
//}

    /**
     * 根据图片的Uri获取图片的绝对路径(已经适配多种API)
     *
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < 11) {
            // SDK < Api11
            return getRealPathFromUri_BelowApi11(context, uri);
        }
        if (sdkVersion < 19) {
            // SDK > 11 && SDK < 19
            return getRealPathFromUri_Api11To18(context, uri);
        }
        // SDK > 19
        return getRealPathFromUri_AboveApi19(context, uri);
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_AboveApi19(Context context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * 适配api11-api18,根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_Api11To18(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        CursorLoader loader = new CursorLoader(context, uri, projection, null,
                null, null);
        Cursor cursor = loader.loadInBackground();

        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_BelowApi11(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }


    public static void saveBitmapbyBitmap(File tempFile, Bitmap comp) {
        if (tempFile == null) {
            return;
        }

        try {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            FileOutputStream out = new FileOutputStream(tempFile);
            comp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
//压缩照相图片

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    
     	  /* 质量压缩图片，图片占用内存减小，像素数不变，常用于上传 
              * @param image
     	     * @param size 期望图片的大小，单位为kb 
     	     * @param options 图片压缩的质量，取值1-100，越小表示压缩的越厉害,如输入30，表示压缩70% 
     	     * @return 
     	     */
    public static Bitmap compressImage(Bitmap image, int size, int options) {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > size) {

            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            // 这里压缩options%，把压缩后的数据存放到baos中

            if (options > 0 && options < 100) {

                try {
                    image.compress(Bitmap.CompressFormat.JPEG, options, baos);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

}
