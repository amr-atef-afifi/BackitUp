package net.backitup.manage_backups;

import java.util.ArrayList;
import java.util.List;

import net.backitup.android.ServerUtilities;
import net.backitup.android.URLFactory;
import net.backitup.database_utilities.Photo;
import net.backitup.ui.ApplicationHttpClient;
import net.backitup.ui.ViewAssistant;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

public class Photos
{

	Context	context;

	public Photos(Context context)
	{
		this.context = context;
	}

	/**
	 * Images new backup, This method is used to backup the current photos to
	 * server with the given backup id
	 * 
	 * @param userID, deviceID, backupID
	 */
	public void createBackup(String userID, String deviceID, String backupID)
	{
		upload_images(userID, deviceID, backupID);
	}

	/**
	 * this method is used to merge the images of the device with the images of
	 * the given backup
	 * 
	 * @param userID
	 * @param deviceID
	 * @param backupID
	 */
	public void mergeBackup(String userID, String deviceID, String backupID)
	{
		merge_images(userID, deviceID, backupID);
	}

	/**
	 * this method is used to merge images in two or more existing backups
	 * 
	 * @param userID
	 * @param deviceID
	 * @param backupIDs
	 */
	public void mergeExistingsBackup(String userID, String deviceID,
			ArrayList<String> backupIDs)
	{
		merge_existing_images(userID, deviceID, backupIDs);
	}

	/**
	 * restore the photos from the server from the backup whose id is given
	 * 
	 * @param backupID
	 */
	public void restoreBackup(String backupID)
	{
		download_images(backupID);
	}

	/**
	 * Delete given images or all images from backup.
	 * 
	 * @param ids
	 * @param userID
	 * @param deviceID
	 * @param backupID
	 * @param all_selected
	 */
	public void deleteFromServer(String ids, String userID, String deviceID,
			String backupID, String all_selected)
	{
		AsyncTask<String, Void, Void> deleteImagesAsyncTask = new AsyncTask<String, Void, Void>()
		{
			@Override
			protected Void doInBackground(String... params)
			{
				String response = "";
				String ids = params[0];
				String userID = params[1];
				String deviceID = params[2];
				String backupID = params[3];
				String all_selected = params[4];
				try
				{
					response = delete_images(ids, userID, deviceID, backupID,
							all_selected);
					Log.v("DELETE_FROM_SERVER", response);
				} catch (Exception e)
				{
					response = "DELETE_FROM_SERVER: Error while deleting images from server";
					Log.e("EXCEPTION", "DELETE_FROM_SERVER: " + e.toString());
				}
				return null;
			}
		};
		deleteImagesAsyncTask.execute(ids, userID, deviceID, backupID,
				all_selected);
	}

	/**
	 * listing images of given backup
	 * 
	 * @param backupID
	 */
	public void listImages(String backupID)
	{
		AsyncTask<String, Void, Void> listImagesAsyncTask = new AsyncTask<String, Void, Void>()
		{
			@Override
			protected Void doInBackground(String... params)
			{
				String backupID = params[0];
				try
				{
					ArrayList<String> imageUrlsArr = new ArrayList<String>();
					ArrayList<String> imageIdsArr = new ArrayList<String>();
					ArrayList<String> imageNamesArr = new ArrayList<String>();
					ArrayList<String> imageMiniThumbsArr = new ArrayList<String>();
					ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
					postParams
							.add(new BasicNameValuePair("backup_id", backupID));
					// send http request with backup id retrieving images
					String response = ApplicationHttpClient.executeHttpPost(
							URLFactory.LIST_IMAGES, postParams);
					// image urls
					JSONObject jsonObject = new JSONObject(response);
					JSONArray jsonArrayImageUrls = jsonObject
							.getJSONArray("image_urls");
					for (int i = 0; i < jsonArrayImageUrls.length(); i++)
					{
						imageUrlsArr.add(jsonArrayImageUrls.getString(i));
					}
					// image ids
					JSONArray jsonArrayImageIds = jsonObject
							.getJSONArray("image_ids");
					for (int i = 0; i < jsonArrayImageIds.length(); i++)
					{
						imageIdsArr.add(jsonArrayImageIds.getString(i));
					}
					// image names
					JSONArray jsonArrayImageNames = jsonObject
							.getJSONArray("image_names");
					for (int i = 0; i < jsonArrayImageNames.length(); i++)
					{
						imageNamesArr.add(jsonArrayImageNames.getString(i));
					}
					// image thumbs
					JSONArray jsonArrayImagethumbs = jsonObject
							.getJSONArray("image_mini_thumbs");
					for (int i = 0; i < jsonArrayImagethumbs.length(); i++)
					{
						imageMiniThumbsArr.add(jsonArrayImagethumbs
								.getString(i));
					}

					for (int j = 0; j < jsonArrayImageIds.length(); j++)
					{
						Log.v("ImageID", "\n" + imageIdsArr.get(j));
						Log.v("ImageURL", "\n" + imageUrlsArr.get(j));
						Log.v("ImageName", "\n" + imageNamesArr.get(j));
						Log.v("ImageThumb", "\n" + imageMiniThumbsArr.get(j));
					}
				} catch (Exception e)
				{
					Log.e("EXCEPTION",
							"LIST_IMAGES: Error while listing images, "
									+ e.toString());
				}
				return null;
			}
		};
		listImagesAsyncTask.execute(backupID);
	}

	private void upload_images(String userID, String deviceID, String backupID)
	{
		AsyncTask<String, Void, Void> uploadImagesAsyncTask = new AsyncTask<String, Void, Void>()
		{
			@Override
			protected Void doInBackground(String... params)
			{
				String userID = params[0];
				String deviceID = params[1];
				String backupID = params[2];
				try
				{
					ArrayList<String[]> files = get_images_list(context);
					Log.v("UPLOAD_IMAGES", "done fetching images with size "
							+ files.size() + "\n");
					ServerUtilities.upload_files(ServerUtilities.IS_IMAGE,
							files, userID, deviceID, backupID);
				} catch (Exception e)
				{
					Log.e("EXCEPTION",
							"UPLOAD_IMAGES: Error while backing up images, "
									+ e.toString());
				}
				return null;
			}
		};
		uploadImagesAsyncTask.execute(userID, deviceID, backupID);
	}

	/**
	 * add the new images to the server
	 * 
	 * @param userID
	 * @param deviceID
	 * @param backupID
	 */

	private void merge_images(String userID, String deviceID, String backupID)
	{
		AsyncTask<String, Void, Void> mergeImagesAsyncTask = new AsyncTask<String, Void, Void>()
		{
			@Override
			protected Void doInBackground(String... params)
			{
				String userID = params[0];
				String deviceID = params[1];
				String backupID = params[2];
				try
				{
					ArrayList<String[]> files = get_images_list(context);
					ServerUtilities.merge_all(URLFactory.MERGE_IMAGES,
							ServerUtilities.IS_IMAGE, userID, deviceID,
							backupID, files);
					Log.v("INFO",
							"Images has been merges with existing backup successfully");
				} catch (Exception e)
				{
					Log.e("EXCEPTION",
							"MERGE_IMAGES: Error while merging images "
									+ e.toString());
				}
				return null;
			}
		};
		mergeImagesAsyncTask.execute(userID, deviceID, backupID);
	}

	/**
	 * merge existing images at different backups together
	 * 
	 * @param userID
	 * @param deviceID
	 * @param backupIDs
	 */
	private void merge_existing_images(String userID, String deviceID,
			ArrayList<String> backupIDs)
	{

	}

	private void download_images(String backupID)
	{
		AsyncTask<String, Void, Void> downloadTask = new AsyncTask<String, Void, Void>()
		{

			@Override
			protected Void doInBackground(String... params)
			{
				String backupID = params[0];
				ServerUtilities.download_all(URLFactory.DOWNLOAD_IMAGES,
						ServerUtilities.IS_IMAGE, backupID, context);
				return null;
			}
		};
		downloadTask.execute(backupID);
	}

	//@formatter:off
	/**
	 * delete photos from a specific backup
	 * in server:
	 * if(all_selected)
	 * 	delete all photos of the backup whose id = backup id
	 * else
	 * 	delete photos whose id is in the ids array 
	 * @param ids
	 *            the server id of that photos in the form 10,11,12,13, ...
	 *            or 11 if a single photo selected
	 * @param backupId
	 *            the backup id is used only if all is selected so you delete
	 *            the photos of that backup regardless of their ids
	 * @param all_selcted
	 *            = 1 if all selected
	 */
	//@formatter:on

	/*
	 * Delete images execution.
	 */
	private String delete_images(String ids, String userID, String deviceID,
			String backupID, String all_selected)
	{
		String response = "";
		ArrayList<NameValuePair> postparams = new ArrayList<NameValuePair>();
		postparams.add(new BasicNameValuePair("image_ids", ids));
		postparams.add(new BasicNameValuePair("user_id", userID));
		postparams.add(new BasicNameValuePair("device_id", deviceID));
		postparams.add(new BasicNameValuePair("backup_id", backupID));
		postparams.add(new BasicNameValuePair("all_selected", all_selected));
		try
		{
			response = ApplicationHttpClient.executeHttpPost(
					URLFactory.DELETE_IMAGES, postparams);
		} catch (Exception e)
		{
			response = "DELETE_IMAGES: Error";
			Log.e("EXCEPTION", "DELETE_IMAGES: " + e.toString());
		}
		return response;
	}

	/*
	 * Return an array list with images thumbnails
	 */
	private ArrayList<String[]> get_images_list(Context context)
	{

		String[] projection = { MediaStore.Images.Media.DATA,
				MediaStore.Images.Media._ID };
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, null);
		ArrayList<String[]> result = new ArrayList<String[]>(cursor.getCount());
		int dataColumn = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		int idColumn = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

		while (cursor.moveToNext())
		{
			String[] pair = new String[2];
			String data = cursor.getString(dataColumn);
			long uri = cursor.getLong(idColumn);
			Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
					context.getContentResolver(), uri,
					MediaStore.Images.Thumbnails.MINI_KIND,
					(BitmapFactory.Options) null);
			pair[0] = data;
			if (bitmap != null)
			{
				pair[1] = ViewAssistant.encodeTobase64(bitmap);
			}
			else
			{
				pair[1] = "";
			}
			result.add(pair);
		}
		cursor.close();
		return result;
	}

	public static List<Photo> getSelfPhotos(Context context)
	{
		return null;
	}
}
