package com.cm.beer.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.R;
import android.app.Notification;
import android.app.Service;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

//Adapted Reflect Class:
public class Reflect {
	private static String TAG = Reflect.class.getName();

	private static Method Parameters_getSupportedPictureSizes;
	private static Method Parameters_getSupportedPreviewSizes;
	private static Field google_adview;

	private static Method Service_startForeground;
	private static final Class[] mStartForegroundSignature = new Class[] {
			int.class, Notification.class };

	private static Method Service_stopForeground;
	private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };

	private static Method Parameters_getSupportedFlashModes;
	private static final Class[] mGetSupportedFlashModesSignature = new Class[] {};
	private static Method Parameters_setFlashMode;
	private static final Class[] mSetFlashModeSignature = new Class[] { String.class };
	private static Method Parameters_getFlashMode;
	private static final Class[] mGetFlashModeSignature = new Class[] {};

	private static Method Parameters_getSupportedSceneModes;
	private static final Class[] mGetSupportedSceneModesSignature = new Class[] {};
	private static Method Parameters_setSceneMode;
	private static final Class[] mSetSceneModeSignature = new Class[] { String.class };
	private static Method Parameters_getSceneMode;
	private static final Class[] mGetSceneModeSignature = new Class[] {};

	private static Method Parameters_getSupportedAntibanding;
	private static final Class[] mGetSupportedAntibandingSignature = new Class[] {};
	private static Method Parameters_setAntibanding;
	private static final Class[] mSetAntibandingSignature = new Class[] { String.class };
	private static Method Parameters_getAntibanding;
	private static final Class[] mGetAntibandingSignature = new Class[] {};

	private static Method Parameters_getSupportedColorEffects;
	private static final Class[] mGetSupportedColorEffectsSignature = new Class[] {};
	private static Method Parameters_setColorEffect;
	private static final Class[] mSetColorEffectSignature = new Class[] { String.class };
	private static Method Parameters_getColorEffect;
	private static final Class[] mGetColorEffectSignature = new Class[] {};

	private static Method Parameters_getSupportedFocusModes;
	private static final Class[] mGetSupportedFocusModesSignature = new Class[] {};
	private static Method Parameters_setFocusMode;
	private static final Class[] mSetFocusModeSignature = new Class[] { String.class };
	private static Method Parameters_getFocusMode;
	private static final Class[] mGetFocusModeSignature = new Class[] {};

	private static Method Parameters_getSupportedWhiteBalance;
	private static final Class[] mGetSupportedWhiteBalanceSignature = new Class[] {};
	private static Method Parameters_setWhiteBalance;
	private static final Class[] mSetWhiteBalanceSignature = new Class[] { String.class };
	private static Method Parameters_getWhiteBalance;
	private static final Class[] mGetWhiteBalanceSignature = new Class[] {};

	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		/**************************************************************/
		try {
			Parameters_getSupportedPictureSizes = Camera.Parameters.class
					.getMethod("getSupportedPictureSizes", new Class[] {});
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedPictureSizes() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/
		try {
			Parameters_getSupportedPreviewSizes = Camera.Parameters.class
					.getMethod("getSupportedPreviewSizes", new Class[] {});
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedPreviewSizes() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/
		try {
			google_adview = R.id.class.getField("google_adview");
			/* success, this is a newer device */
			Log.i(TAG, "Field google_adview() is available!");
		} catch (NoSuchFieldException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/
		try {
			Service_startForeground = Service.class.getMethod(
					"startForeground", mStartForegroundSignature);
			Log.i(TAG, "Method startForeground() is available!");
			Service_stopForeground = Service.class.getMethod("stopForeground",
					mStopForegroundSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method stopForeground() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/
		try {
			Parameters_getSupportedFlashModes = Camera.Parameters.class
					.getMethod("getSupportedFlashModes",
							mGetSupportedFlashModesSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedFlashModes() is available!");
			Parameters_setFlashMode = Camera.Parameters.class.getMethod(
					"setFlashMode", mSetFlashModeSignature);
			Log.i(TAG, "Method setFlashMode() is available!");
			Parameters_getFlashMode = Camera.Parameters.class.getMethod(
					"getFlashMode", mGetFlashModeSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getFlashMode() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/
		try {
			Parameters_getSupportedSceneModes = Camera.Parameters.class
					.getMethod("getSupportedSceneModes",
							mGetSupportedSceneModesSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedSceneModes() is available!");
			Parameters_setSceneMode = Camera.Parameters.class.getMethod(
					"setSceneMode", mSetSceneModeSignature);
			Log.i(TAG, "Method setSceneMode() is available!");
			Parameters_getSceneMode = Camera.Parameters.class.getMethod(
					"getSceneMode", mGetSceneModeSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getSceneMode() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/

		try {
			Parameters_getSupportedAntibanding = Camera.Parameters.class
					.getMethod("getSupportedAntibanding",
							mGetSupportedAntibandingSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedAntibanding() is available!");
			Parameters_setAntibanding = Camera.Parameters.class.getMethod(
					"setAntibanding", mSetAntibandingSignature);
			Log.i(TAG, "Method setAntibanding() is available!");
			Parameters_getAntibanding = Camera.Parameters.class.getMethod(
					"getAntibanding", mGetAntibandingSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getAntibanding() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/

		try {
			Parameters_getSupportedColorEffects = Camera.Parameters.class
					.getMethod("getSupportedColorEffects",
							mGetSupportedColorEffectsSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedColorEffects() is available!");
			Parameters_setColorEffect = Camera.Parameters.class.getMethod(
					"setColorEffect", mSetColorEffectSignature);
			Log.i(TAG, "Method setColorEffect() is available!");
			Parameters_getColorEffect = Camera.Parameters.class.getMethod(
					"getColorEffect", mGetColorEffectSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getColorEffect() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/

		try {
			Parameters_getSupportedFocusModes = Camera.Parameters.class
					.getMethod("getSupportedFocusModes",
							mGetSupportedFocusModesSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedFocusModes() is available!");
			Parameters_setFocusMode = Camera.Parameters.class.getMethod(
					"setFocusMode", mSetFocusModeSignature);
			Log.i(TAG, "Method setFocusMode() is available!");
			Parameters_getFocusMode = Camera.Parameters.class.getMethod(
					"getFocusMode", mGetFocusModeSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getFocusMode() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/
		try {
			Parameters_getSupportedWhiteBalance = Camera.Parameters.class
					.getMethod("getSupportedWhiteBalance",
							mGetSupportedWhiteBalanceSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getSupportedWhiteBalance() is available!");
			Parameters_setWhiteBalance = Camera.Parameters.class.getMethod(
					"setWhiteBalance", mSetWhiteBalanceSignature);
			Log.i(TAG, "Method setWhiteBalance() is available!");
			Parameters_getWhiteBalance = Camera.Parameters.class.getMethod(
					"getWhiteBalance", mGetWhiteBalanceSignature);
			/* success, this is a newer device */
			Log.i(TAG, "Method getWhiteBalance() is available!");
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			Log.w(TAG, nsme);
		}
		/**************************************************************/

	}

	/**
	 * 
	 * @param p
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Size> getSupportedPictureSizes(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedPictureSizes != null) {
				Log.i(TAG, "Method getSupportedPictureSizes() is available!");
				return (List<Size>) Parameters_getSupportedPictureSizes
						.invoke(p);
			} else {
				Log.i(TAG,
						"Method getSupportedPictureSizes() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/******************************************************************************/

	@SuppressWarnings("unchecked")
	public static List<Size> getSupportedPreviewSizes(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedPreviewSizes != null) {
				Log.i(TAG, "Method getSupportedPreviewSizes() is available!");
				return (List<Size>) Parameters_getSupportedPreviewSizes
						.invoke(p);
			} else {
				Log.i(TAG,
						"Method getSupportedPreviewSizes() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/******************************************************************************/

	/**
	 * 
	 * @return
	 */
	public static int getGoogleAdview() {
		try {
			if (google_adview != null) {
				Log.i(TAG, "google_adview found!");
				return google_adview.getInt(R.id.class);
			} else {
				Log.i(TAG, "google_adview NOT found!");
				return 0;
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return 0;
		}

	}

	/******************************************************************************/

	/**
	 * Make this service run in the foreground, supplying the ongoing
	 * notification to be shown to the user while in this state.
	 * 
	 * @param id
	 * @param notification
	 * @param service
	 *            the service to the invoked
	 */
	public static final boolean service_startForeground(int id,
			Notification notification, Service service) {
		try {
			if (Service_startForeground != null) {
				Log.i(TAG, "Method service_startForeground() is available!");
				Object[] _startForegroundArgs = new Object[2];
				_startForegroundArgs[0] = Integer.valueOf(id);
				_startForegroundArgs[1] = notification;
				Service_startForeground.invoke(service, _startForegroundArgs);
				Log.i(TAG, Service_startForeground.getName() + " invoked!");
				return true;
			} else {
				Log
						.i(TAG,
								"Method service_startForeground() is NOT available!");
				return false;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return false;
		}
	}

	/******************************************************************************/

	/**
	 * Remove this service from foreground state, allowing it to be killed if
	 * more memory is needed.
	 * 
	 * @param removeNotification
	 * @param service
	 *            the service to the invoked
	 */
	public static final boolean service_stopForeground(
			boolean removeNotification, Service service) {
		try {
			if (Service_stopForeground != null) {
				Log.i(TAG, "Method service_stopForeground() is available!");
				Object[] _stopForegroundArgs = new Object[1];
				_stopForegroundArgs[0] = Boolean.valueOf(removeNotification);
				Service_stopForeground.invoke(service, _stopForegroundArgs);
				Log.i(TAG, Service_stopForeground.getName() + " invoked!");
				return true;
			} else {
				Log.i(TAG, "Method service_stopForeground() is NOT available!");
				return false;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return false;
		}
	}

	/******************************************************************************/

	/**
	 * Gets the supported flash modes.
	 * 
	 * @param p
	 * @return a list of supported flash modes. null if flash mode setting is
	 *         not supported.
	 */
	public static List<String> getSupportedFlashModes(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedFlashModes != null) {
				Log.i(TAG, "Method getSupportedFlashModes() is available!");
				return (List<String>) Parameters_getSupportedFlashModes
						.invoke(p);
			} else {
				Log.i(TAG, "Method getSupportedFlashModes() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/**
	 * Sets the flash mode.
	 * 
	 * @param p
	 * @param value
	 * @return success or failure
	 */
	public static final boolean setFlashMode(Camera.Parameters p, String value) {
		try {
			if (Parameters_setFlashMode != null) {
				Log.i(TAG, "Method setFlashMode() is available!");
				Object[] _args = new Object[1];
				_args[0] = value;
				Parameters_setFlashMode.invoke(p, _args);
				Log.i(TAG, "Flash Mode set to " + value);
				return true;
			} else {
				Log.i(TAG, "Method setFlashMode() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
			return false;
		}
		return false;
	}

	/**
	 * Gets the current flash mode
	 * 
	 * @param p
	 * @return
	 */
	public static final String getFlashMode(Camera.Parameters p) {
		String value = null;
		try {
			if (Parameters_getFlashMode != null) {
				Log.i(TAG, "Method getFlashMode() is available!");
				value = (String) Parameters_getFlashMode.invoke(p);
			} else {
				Log.i(TAG, "Method getFlashMode() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
		}
		return value;
	}

	/******************************************************************************/

	/**
	 * Gets the supported scene modes.
	 * 
	 * @param p
	 * @return a list of supported scene modes. null if scene mode setting is
	 *         not supported.
	 */
	public static List<String> getSupportedSceneModes(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedSceneModes != null) {
				Log.i(TAG, "Method getSupportedSceneModes() is available!");
				return (List<String>) Parameters_getSupportedSceneModes
						.invoke(p);
			} else {
				Log.i(TAG, "Method getSupportedSceneModes() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/**
	 * Sets the scene mode. Changing scene mode may override other parameters
	 * (such as flash mode, focus mode, white balance). For example, suppose
	 * originally flash mode is on and supported flash modes are on/off. In
	 * night scene mode, both flash mode and supported flash mode may be changed
	 * to off. After setting scene mode, applications should call getParameters
	 * to know if some parameters are changed.
	 * 
	 * @param p
	 * @param value
	 * @return success or failure
	 */
	public static final boolean setSceneMode(Camera.Parameters p, String value) {
		try {
			if (Parameters_setSceneMode != null) {
				Log.i(TAG, "Method setSceneMode() is available!");
				Object[] _args = new Object[1];
				_args[0] = value;
				Parameters_setSceneMode.invoke(p, _args);
				Log.i(TAG, "Scene Mode set to " + value);
				return true;
			} else {
				Log.i(TAG, "Method setSceneMode() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
			return false;
		}
		return false;
	}

	/**
	 * Gets the current scene mode
	 * 
	 * @param p
	 * @return
	 */
	public static final String getSceneMode(Camera.Parameters p) {
		String value = null;
		try {
			if (Parameters_getSceneMode != null) {
				Log.i(TAG, "Method getSceneMode() is available!");
				value = (String) Parameters_getSceneMode.invoke(p);
			} else {
				Log.i(TAG, "Method getSceneMode() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
		}
		return value;
	}

	/******************************************************************************/

	/**
	 * Gets the supported antibanding values.
	 * 
	 * @param p
	 * @return a list of supported antibanding values. null if antibanding
	 *         setting is not supported.
	 */
	public static List<String> getSupportedAntibanding(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedAntibanding != null) {
				Log.i(TAG, "Method getSupportedAntibanding() is available!");
				return (List<String>) Parameters_getSupportedAntibanding
						.invoke(p);
			} else {
				Log
						.i(TAG,
								"Method getSupportedAntibanding() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/**
	 * Sets antibanding
	 * 
	 * @param p
	 * @param value
	 * @return
	 */
	public static final boolean setAntibanding(Camera.Parameters p, String value) {
		try {
			if (Parameters_setAntibanding != null) {
				Log.i(TAG, "Method setAntibanding() is available!");
				Object[] _args = new Object[1];
				_args[0] = value;
				Parameters_setAntibanding.invoke(p, _args);
				Log.i(TAG, "Antibanding set to " + value);
				return true;
			} else {
				Log.i(TAG, "Method setAntibanding() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
			return false;
		}
		return false;
	}

	/**
	 * Gets the current antibanding
	 * 
	 * @param p
	 * @return
	 */
	public static final String getAntibanding(Camera.Parameters p) {
		String value = null;
		try {
			if (Parameters_getAntibanding != null) {
				Log.i(TAG, "Method getAntibanding() is available!");
				value = (String) Parameters_getAntibanding.invoke(p);
			} else {
				Log.i(TAG, "Method getAntibanding() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
		}
		return value;
	}

	/******************************************************************************/

	/**
	 * Gets the supported color effects.
	 * 
	 * @param p
	 * @return a list of supported color effects. null if color effect setting
	 *         is not supported.
	 */
	public static List<String> getSupportedColorEffects(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedColorEffects != null) {
				Log.i(TAG, "Method getSupportedColorEffects() is available!");
				return (List<String>) Parameters_getSupportedColorEffects
						.invoke(p);
			} else {
				Log.i(TAG,
						"Method getSupportedColorEffects() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/**
	 * Sets color effect
	 * 
	 * @param p
	 * @param value
	 * @return
	 */
	public static final boolean setColorEffect(Camera.Parameters p, String value) {
		try {
			if (Parameters_setColorEffect != null) {
				Log.i(TAG, "Method setColorEffect() is available!");
				Object[] _args = new Object[1];
				_args[0] = value;
				Parameters_setColorEffect.invoke(p, _args);
				Log.i(TAG, "Color Effect set to " + value);
				return true;
			} else {
				Log.i(TAG, "Method setColorEffect() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
			return false;
		}
		return false;
	}

	/**
	 * Gets the current color effect
	 * 
	 * @param p
	 * @return
	 */
	public static final String getColorEffect(Camera.Parameters p) {
		String value = null;
		try {
			if (Parameters_getColorEffect != null) {
				Log.i(TAG, "Method getColorEffect() is available!");
				value = (String) Parameters_getColorEffect.invoke(p);
			} else {
				Log.i(TAG, "Method getColorEffect() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
		}
		return value;
	}

	/******************************************************************************/

	/**
	 * Gets the supported focus modes.
	 * 
	 * @param p
	 * @return a list of supported focus modes. This method will always return a
	 *         list with at least one element.
	 */
	public static List<String> getSupportedFocusModes(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedFocusModes != null) {
				Log.i(TAG, "Method getSupportedFocusModes() is available!");
				return (List<String>) Parameters_getSupportedFocusModes
						.invoke(p);
			} else {
				Log.i(TAG, "Method getSupportedFocusModes() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/**
	 * Set focus mode
	 * 
	 * @param p
	 * @param value
	 * @return
	 */
	public static final boolean setFocusMode(Camera.Parameters p, String value) {
		try {
			if (Parameters_setFocusMode != null) {
				Log.i(TAG, "Method setFocusMode() is available!");
				Object[] _args = new Object[1];
				_args[0] = value;
				Parameters_setFocusMode.invoke(p, _args);
				Log.i(TAG, "Focus Mode set to " + value);
				return true;
			} else {
				Log.i(TAG, "Method setFocusMode() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
			return false;
		}
		return false;
	}

	/**
	 * Gets the current focus mode
	 * 
	 * @param p
	 * @return
	 */
	public static final String getFocusMode(Camera.Parameters p) {
		String value = null;
		try {
			if (Parameters_getFocusMode != null) {
				Log.i(TAG, "Method getFocusMode() is available!");
				value = (String) Parameters_getFocusMode.invoke(p);
			} else {
				Log.i(TAG, "Method getFocusMode() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
		}
		return value;
	}

	/******************************************************************************/

	/**
	 * Gets the supported white balance.
	 * 
	 * @param p
	 * @return a list of supported white balance. null if white balance setting
	 *         is not supported.
	 */
	public static List<String> getSupportedWhiteBalance(Camera.Parameters p) {
		try {
			if (Parameters_getSupportedWhiteBalance != null) {
				Log.i(TAG, "Method getSupportedWhiteBalance() is available!");
				return (List<String>) Parameters_getSupportedWhiteBalance
						.invoke(p);
			} else {
				Log.i(TAG,
						"Method getSupportedWhiteBalance() is NOT available!");
				return null;
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.w(TAG, ie);
			return null;
		}
	}

	/**
	 * Sets white balance
	 * 
	 * @param p
	 * @param value
	 * @return
	 */
	public static final boolean setWhiteBalance(Camera.Parameters p,
			String value) {
		try {
			if (Parameters_setWhiteBalance != null) {
				Log.i(TAG, "Method setWhiteBalance() is available!");
				Object[] _args = new Object[1];
				_args[0] = value;
				Parameters_setWhiteBalance.invoke(p, _args);
				Log.i(TAG, "White Balance set to " + value);
				return true;
			} else {
				Log.i(TAG, "Method setWhiteBalance() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
			return false;
		}
		return false;
	}

	/**
	 * Gets the current white balance
	 * 
	 * @param p
	 * @return
	 */
	public static final String getWhiteBalance(Camera.Parameters p) {
		String value = null;
		try {
			if (Parameters_getWhiteBalance != null) {
				Log.i(TAG, "Method getWhiteBalance() is available!");
				value = (String) Parameters_getWhiteBalance.invoke(p);
			} else {
				Log.i(TAG, "Method getWhiteBalance() is NOT available!");
			}
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Log.w(TAG, ite);
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (Throwable ie) {
			Log.w(TAG, ie);
		}
		return value;
	}
	/******************************************************************************/

}