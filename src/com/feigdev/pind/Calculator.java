package com.feigdev.pind;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Calculator {
	private static final String TAG = "Calculator";
	
	public static Bitmap generateBmp(String picture) throws OutOfMemoryError{
		Bitmap bitmap = null;
		URL url;
		if (picture.equals("") || picture.equals("null") || picture.equals(null)){
			return null;
		}

		try {
			url = new URL(picture);
			URLConnection conn = url.openConnection();
			InputStream instream = conn.getInputStream();
		
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 1;
			conn.connect();
			bitmap = BitmapFactory.decodeStream(instream, null, options);
		}catch (OutOfMemoryError e) {
		    System.gc();

		    try {
		    	url = new URL(picture);
				URLConnection conn = url.openConnection();
				InputStream instream = conn.getInputStream();
			
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 1;
				conn.connect();
				bitmap = BitmapFactory.decodeStream(instream, null, options);
		    } catch (Exception e3) {
				if (Constants.DEBUG){
					Log.w(TAG, "getPic blew up =(");
					e3.printStackTrace();
				}
			}
		}catch (Exception e) {
			if (Constants.DEBUG){
				Log.w(TAG, "getPic blew up =(");
				e.printStackTrace();
			}
		} finally {
			System.gc();
		}
		
		
		return bitmap;
	}
	
	public static Bitmap generateLocalBmp(String picture) {
		if (picture.equals("") || picture.equals("null") || picture.equals(null)){
			return null;
		}

		try {
			Bitmap bitmap = null;
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 1;
			bitmap = BitmapFactory.decodeFile(picture, options);
			return bitmap;
		}catch (OutOfMemoryError e) {
		    System.gc();
		    try {
				Bitmap bitmap = null;
		    	BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 1;
				bitmap = BitmapFactory.decodeFile(picture, options);
				return bitmap;
			} catch (Exception e3) {
				if (Constants.DEBUG){
					Log.w(TAG, "getPic blew up =(");
					e3.printStackTrace();
				}
			}
		}catch (Exception e) {
			if (Constants.DEBUG){
				Log.w(TAG, "getPic blew up =(");
				e.printStackTrace();
			}
		} finally {
			System.gc();
		}
		return null;
	}
	
	public static Bitmap getBmpFromBytes(byte [] photoBin){
		return BitmapFactory.decodeByteArray(photoBin, 0, photoBin.length);
	}
	
	public static  byte[] getPhotoBytes(Bitmap photoBin_s, Context context){
		if (photoBin_s == null){
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				Bitmap photoBin = defaultPic(context);
				photoBin.compress(Bitmap.CompressFormat.PNG, 80, baos); //bm is the bitmap object   
				return baos.toByteArray();
			} catch (Exception e){
				if (Constants.DEBUG){
					Log.w(TAG, "photoBin_s == null");
					e.printStackTrace();
				}
				
			} finally {
				System.gc();
			}
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			photoBin_s.compress(Bitmap.CompressFormat.PNG, 80, baos); //bm is the bitmap object   
			return baos.toByteArray();
		} catch (Exception ex){
			if (Constants.DEBUG){
				Log.w(TAG, "getSPhotoBytes failed, trying default");
				ex.printStackTrace();
			}
			try {
				System.gc();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				Bitmap photoBin = defaultPic(context);
				photoBin.compress(Bitmap.CompressFormat.PNG, 80, baos); //bm is the bitmap object   
				return baos.toByteArray();
			} catch (Exception e){
				if (Constants.DEBUG){
					Log.w(TAG, "getSPhotoBytes failed");
					e.printStackTrace();
				}
				
			} finally {
				System.gc();
			}
		}
		return null;
	}
	
	public static Bitmap defaultPic(Context context){
		try {
			Bitmap bmp;
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 2;
			Resources res;
			res = context.getResources();
			bmp = BitmapFactory.decodeResource(res, R.drawable.pind_icon);
			if (bmp == null){
				return null;
			}
			int width = bmp.getWidth();
            int height = bmp.getHeight();
            int newWidth = 60;
            int newHeight = 60;
	            
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
	             
            bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
            return bmp;
		} catch (Exception ex){
			if (Constants.DEBUG){
				Log.w(TAG, "defaultPic blew up  =(");
				ex.printStackTrace();
			}
			return null;
		} finally {
			System.gc();
		}
	}

	 public static Bitmap resizeImage(Bitmap bitmap, int boundBoxInDp)
	    {
	    	try {
		        // Get the ImageView and its bitmap
		        if (null == bitmap){
		    		return null;
		        }
		        // Get current dimensions
		        int width = bitmap.getWidth();
		        int height = bitmap.getHeight();
		        
		        
		        
		        // Determine how much to scale: the dimension requiring less scaling is
		        // closer to the its side. This way the image always stays inside your
		        // bounding box AND either x/y axis touches it.
		        float xScale = ((float) boundBoxInDp) / width;
		        
		        float newHeight = height * xScale;
		        
		        float yScale = ((float) newHeight) / height;
		//        float scale = (xScale <= yScale) ? xScale : yScale;
		
		        // Create a matrix for the scaling and add the scaling data
		        Matrix matrix = new Matrix();
		        matrix.postScale(xScale, yScale);
		
		        // Create a new bitmap and convert it to a format understood by the ImageView
		        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		        if (null == scaledBitmap){
		        	return null;
		        }
		        width = scaledBitmap.getWidth();
		        height = scaledBitmap.getHeight();
		
		        // Now change ImageView's dimensions to match the scaled image
		        return scaledBitmap;
	    	} catch (OutOfMemoryError e){
	    		System.gc();
    			// Get the ImageView and its bitmap
		        if (null == bitmap){
		    		return null;
		        }
		        // Get current dimensions
		        int width = bitmap.getWidth();
		        int height = bitmap.getHeight();
		        
		        
		        
		        // Determine how much to scale: the dimension requiring less scaling is
		        // closer to the its side. This way the image always stays inside your
		        // bounding box AND either x/y axis touches it.
		        float xScale = ((float) boundBoxInDp) / width;
		        
		        float newHeight = height * xScale;
		        
		        float yScale = ((float) newHeight) / height;
		//        float scale = (xScale <= yScale) ? xScale : yScale;
		
		        // Create a matrix for the scaling and add the scaling data
		        Matrix matrix = new Matrix();
		        matrix.postScale(xScale, yScale);
		
		        // Create a new bitmap and convert it to a format understood by the ImageView
		        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		        if (null == scaledBitmap){
		        	return null;
		        }
		        width = scaledBitmap.getWidth();
		        height = scaledBitmap.getHeight();
		
		        // Now change ImageView's dimensions to match the scaled image
		        return scaledBitmap;
	    	} finally {
	    		System.gc();
	    	}
	    }
	    
	 public static Bitmap resizeImageFromFile(String file, int boundBoxInDp) {
		 return resizeImage(generateLocalBmp(file),boundBoxInDp);
	 }
	 
	 	public static Point getSizeOfImageFile(String file){
	 		Bitmap bitmap = null;
	 		try {
	 		bitmap = generateLocalBmp(file);
	 		Point p = new Point(bitmap.getWidth(),bitmap.getHeight());
	    	return p;
	 		} finally {
	 			System.gc();
	 		}
	 		
	 	}
	 
	    public static void scaleImage(ImageView view, int boundBoxInDp)
	    {
	    	if (null == view){
	    		return;
	    	}
	    	try {
		        // Get the ImageView and its bitmap
		        Drawable drawing = view.getDrawable();
		        Bitmap scaledBitmap = resizeImage(((BitmapDrawable)drawing).getBitmap(),boundBoxInDp);
		        
		        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
		        
		        // Apply the scaled bitmap
		        view.setImageDrawable(result);
		
		        // Now change ImageView's dimensions to match the scaled image
		        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
		        params.width = scaledBitmap.getWidth();
		        params.height = scaledBitmap.getHeight();
		        view.setLayoutParams(params);
	    	} catch (OutOfMemoryError e){
	    		return;
	    	} finally {
	    		System.gc();
	    	}
	    }

		public static Bitmap generateBmpPrev(String picture) {
			Bitmap bitmap = null;
			URL url;
			if (picture.equals("") || picture.equals("null") || picture.equals(null)){
				return null;
			}

			try {
				url = new URL(picture);
				URLConnection conn = url.openConnection();
				InputStream instream = conn.getInputStream();
			
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 2;
				conn.connect();
				bitmap = BitmapFactory.decodeStream(instream, null, options);
			}catch (OutOfMemoryError e) {
			    System.gc();

			    try {
			    	url = new URL(picture);
					URLConnection conn = url.openConnection();
					InputStream instream = conn.getInputStream();
				
					BitmapFactory.Options options=new BitmapFactory.Options();
					options.inSampleSize = 2;
					conn.connect();
					bitmap = BitmapFactory.decodeStream(instream, null, options);
			    } catch (OutOfMemoryError e2) {
			      e2.printStackTrace();
			    } catch (Exception e3) {
					if (Constants.DEBUG){
						Log.w(TAG, "getPic blew up =(");
						e3.printStackTrace();
					}
				}
			}catch (Exception e) {
				if (Constants.DEBUG){
					Log.w(TAG, "getPic blew up =(");
					e.printStackTrace();
				}
			} finally {
				System.gc();
			}
			
			
			return bitmap;
		}
		
		public static Bitmap generateLocalBmpPrev(String picture) {
//			if (picture.equals("") || picture.equals("null") || picture.equals(null)){
//				return null;
//			}
			try {
				InputStream is = PindActivity.PACKAGE_RESOLVER.openInputStream(Uri.parse(picture));
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 4;
				Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
				is.close();
				return bitmap;
			}catch (OutOfMemoryError e) {
			    System.gc();
			    try {
			    	InputStream is = PindActivity.PACKAGE_RESOLVER.openInputStream(Uri.parse(picture));
					BitmapFactory.Options options=new BitmapFactory.Options();
					options.inSampleSize = 4;
					Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
					return bitmap;
			    } catch (OutOfMemoryError e2) {
			      e2.printStackTrace();
			    } catch (Exception e3) {
					if (Constants.DEBUG){
						Log.w(TAG, "getPic blew up =(");
						e3.printStackTrace();
					}
				}
			}catch (Exception e) {
				if (Constants.DEBUG){
					Log.w(TAG, "getPic blew up =(");
					e.printStackTrace();
				}
			} finally {
				System.gc();
			}
			return null;
			
		}
		
		public static byte[] getLocalJpgBytes(String img){
			Bitmap bmp = generateLocalBmp(img);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bmp.compress(CompressFormat.JPEG, 75, bos);
			return bos.toByteArray();
		}
		
		public static byte[] getJpgBytes(Bitmap bmp){
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bmp.compress(CompressFormat.JPEG, 75, bos);
			return bos.toByteArray();
		}
		
		public static String getFileFromUrl(String url){
			url = url.split("\\?")[0];
			int slashIndex = url.lastIndexOf('/');
			return getDefaultFilePath() + url.substring(slashIndex + 1);
		}
		
		public static String getDefaultFilePath(){
			String storageState = Environment.getExternalStorageState();
	        if(storageState.equals(Environment.MEDIA_MOUNTED)) {
	        	// http://stackoverflow.com/a/5054673/974800
	            return Environment.getExternalStorageDirectory().getName() 
	            		+ File.separatorChar + "Android/data/" 
	            		+ PindActivity.PACKAGE_NAME + "/"; 
	        }
	        else {
	        	return null;
	        }
		}
		
		public static byte[] readFile (String file) throws IOException {
			Uri path = Uri.parse(file);
			if ("content".equals(path.getScheme())){
				return readStream(path);
			}
	        return readFile(new File(path.getPath()));
	    }

		public static byte[] readStream (Uri path) throws IOException {
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        	InputStream inputStream = PindActivity.PACKAGE_RESOLVER.openInputStream(path);
        	// this is storage overwritten on each iteration with bytes
        	int bufferSize = 1024;
        	byte[] buffer = new byte[bufferSize];

        	// we need to know how may bytes were read to write them to the byteBuffer
        	int len = 0;
        	while ((len = inputStream.read(buffer)) != -1) {
        		byteBuffer.write(buffer, 0, len);
        	}

        	// and then we can return your byte array.
        	return byteBuffer.toByteArray();
        }
		
	    public static byte[] readFile (File file) throws IOException {
	        // Open file
	        RandomAccessFile f = new RandomAccessFile(file, "r");

	        try {
	            // Get and check length
	            long longlength = f.length();
	            int length = (int) longlength;
	            if (length != longlength) throw new IOException("File size >= 2 GB");

	            // Read file and return data
	            byte[] data = new byte[length];
	            f.readFully(data);
	            return data;
	        }
	        finally {
	            f.close();
	        }
	    }
}
