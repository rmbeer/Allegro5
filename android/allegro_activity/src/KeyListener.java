package org.liballeg.android;

import android.media.AudioManager;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

class KeyListener implements View.OnKeyListener
{
   private Context context;
   private boolean captureVolume = false;
   private AllegroActivity activity;

   native void nativeOnKeyDown(int key);
   native void nativeOnKeyUp(int key);
   native void nativeOnKeyChar(int key, int unichar);
   native void nativeOnJoystickButton(int index, int button, boolean down);

   KeyListener(Context context, AllegroActivity activity)
   {
      this.context = context;
      this.activity = activity;
   }

   void setCaptureVolumeKeys(boolean onoff)
   {
      captureVolume = onoff;
   }

   public boolean onKeyboardKey(View v, int keyCode, KeyEvent event)
   {
      int unichar;

      /* We want media player controls to keep working in background apps */
      if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
         return false;
      }

      if (event.getAction() == KeyEvent.ACTION_DOWN) {
         if (!captureVolume) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
               volumeChange(1);
               return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
               volumeChange(-1);
               return true;
            }
         }
         if (Key.alKey(keyCode) == Key.ALLEGRO_KEY_BACKSPACE) {
            unichar = '\b';
         }
         else if (Key.alKey(keyCode) == Key.ALLEGRO_KEY_ENTER) {
            unichar = '\r';
         }
         else {
            unichar = event.getUnicodeChar();
         }

         if (event.getRepeatCount() == 0) {
            nativeOnKeyDown(Key.alKey(keyCode));
            nativeOnKeyChar(Key.alKey(keyCode), unichar);
         }
         else {
            nativeOnKeyChar(Key.alKey(keyCode), unichar);
         }
         return true;
      }
      else if (event.getAction() == KeyEvent.ACTION_UP) {
         nativeOnKeyUp(Key.alKey(keyCode));
         return true;
      }

      return false;
   }

   private int getCode(int keyCode, KeyEvent event, int index1) {
      int code = -1;
      if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
         code = 0;
      }
      else if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
         code = 1;
      }
      else if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
         code = 2;
      }
      else if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
         code = 3;
      }
      else if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
         code = 4;
      }
      else if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
         code = 5;
      }
      else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
         if (event.getAction() == KeyEvent.ACTION_DOWN)
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_L_BUTTON, true);
         else
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_L_BUTTON, false);
         return -2;
      }
      else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
         if (event.getAction() == KeyEvent.ACTION_DOWN)
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_R_BUTTON, true);
         else
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_R_BUTTON, false);
         return -2;
      }
      else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
         if (event.getAction() == KeyEvent.ACTION_DOWN)
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_U_BUTTON, true);
         else
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_U_BUTTON, false);
         return -2;
      }
      else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
         if (event.getAction() == KeyEvent.ACTION_DOWN)
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_D_BUTTON, true);
         else
            nativeOnJoystickButton(index1, AllegroActivity.DPAD_D_BUTTON, false);
         return -2;
      }
      return code;
   }

   public boolean onKey(View v, int keyCode, KeyEvent event) {
      int id = event.getDeviceId();
      int index = activity.indexOfJoystick(id);
      int code = -1;
      if (index >= 0) {
         code = getCode(keyCode, event, index+1);
      }
      if (code == -1) {
         return onKeyboardKey(v, keyCode, event);
      }
      else if (code == -2) {
         return true;
      }
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
         if (event.getRepeatCount() == 0) {
            nativeOnJoystickButton(index, code, true);
         }
      }
      else {
         nativeOnJoystickButton(index, code, false);
      }
      return true;
   }

   private void volumeChange(int inc)
   {
      AudioManager mAudioManager =
         (AudioManager)this.context.getApplicationContext()
         .getSystemService(Context.AUDIO_SERVICE);

      int curr = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      int vol = curr + inc;

      if (0 <= vol && vol <= max) {
         // Set a new volume level manually with the FLAG_SHOW_UI flag.
         mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol,
            AudioManager.FLAG_SHOW_UI);
      }
   }
}

/* vim: set sts=3 sw=3 et: */
