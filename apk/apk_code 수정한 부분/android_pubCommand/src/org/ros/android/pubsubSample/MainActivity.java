/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.pubsubSample;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.ros.android.MessageCallable;
import org.ros.android.MessagePub;
import org.ros.android.RosActivity;
import org.ros.android.MessageSub;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;
import java.util.Locale;
import static android.speech.tts.TextToSpeech.ERROR;


/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends RosActivity {

  private MessagePub<std_msgs.String> pubString;
  //private MessagePub<std_msgs.String> cameraDatatString;
  private Button commandButton0;
  private Button commandButton1;
  private Button commandButton2;
  private Context mContext;
  private BroadcastReceiver mReceiver = null;
  private final String BROADCAST_MESSAGE = "org.ros.android.pubsubSample.facetracking";
  private MessagePub<std_msgs.String> pubVoice;
  private final int REQ_CODE_SPEECH_INPUT = 100;
  private ImageButton btnSpeak;
  private TextView txtSpeechInput;

  boolean camerapulishapk = true;

  final String command0Array[] = {  "시작", "움직여","찍어", "스타트" ,"들어" } ;
  final String command1Array[] = { "정지" , "멈춤", "멈춰", "그만", "잠깐", "스탑" } ;
  final String command2Array[] = { "내려", "놓으세요", "놔둬"} ;

  private TextToSpeech tts;              // TTS 변수 선언

  public MainActivity() {
    // The RosActivity constructor configures the notification title and ticker
    // messages.
    super("Pubsub Sample", "Pubsub Sample");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mContext = getApplicationContext();
    commandButton0 = (Button) findViewById(R.id.Button0);
    commandButton1 = (Button) findViewById(R.id.Button1);
    commandButton2 = (Button) findViewById(R.id.Button2);
    btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
    txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);


    commandButton0.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pubString.message.setData("0");
        pubString.publish();
      }
    });

    commandButton1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pubString.message.setData("1");
        pubString.publish();
      }
    });

    commandButton2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pubString.message.setData("2");
        pubString.publish();
      }
    });

    tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if(status != ERROR) {
          // 언어를 선택한다.
          tts.setLanguage(Locale.KOREAN);
        }
      }
    });

    //registerReceiver();
  }


  @Override
  protected void init(NodeMainExecutor nodeMainExecutor) {

    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
    nodeConfiguration.setMasterUri(getMasterUri());

    ////////////////publish string_command //
    pubString = new MessagePub<std_msgs.String>(mContext);
    pubString.setTopicName("command_Key");
    pubString.setMessageType(std_msgs.String._TYPE);
    nodeMainExecutor.execute(pubString,
            nodeConfiguration.setNodeName("android/command_Key"));

    ////////////////publish cameraDatatString //
    /*cameraDatatString = new MessagePub<std_msgs.String>(mContext);
    cameraDatatString.setTopicName("facedetectdataset");
    cameraDatatString.setMessageType(std_msgs.String._TYPE);
    nodeMainExecutor.execute(cameraDatatString,
            nodeConfiguration.setNodeName("android/facedetectdataset"));*/


    ////////////////publish String //
    pubVoice =  new MessagePub<std_msgs.String>(mContext) ;
    pubVoice.setTopicName("~recognizer/output");
    pubVoice.setMessageType(std_msgs.String._TYPE);
    nodeMainExecutor.execute(pubVoice,
            nodeConfiguration.setNodeName("android/publish_voice_string"));

    btnSpeak.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        promptSpeechInput();
      }
    });

  }


  /**
   * Showing google speech input dialog
   * */
  private void promptSpeechInput() {

    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt));

    try {
      startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
    } catch (ActivityNotFoundException a) {
      Toast.makeText(getApplicationContext(),
              getString(R.string.speech_not_supported),
              Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Receiving speech input
   * */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    switch (requestCode) {
      case REQ_CODE_SPEECH_INPUT: {
        if (resultCode == RESULT_OK && null != data) {

          ArrayList<String> result = data
                  .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          String sentance = result.get(0) ;
          String controlDirection = judgeCommand ( sentance );
          if( controlDirection != null ) {
            pubString.message.setData(controlDirection);
            pubString.publish();
            pubVoice.message.setData(sentance);
            pubVoice.publish();

            if (controlDirection.equals("0")) {
              txtSpeechInput.setText("'" + sentance + "' 를 인식했습니다." + "\n " +
                      "따라서 0.시작 실행 합니다. ");
              tts.setPitch(1.0f);
              tts.setSpeechRate(1.0f);
              // editText에 있는 문장을 읽는다.
              tts.speak("네 알겠습니다. 폰을 집겠습니다.",TextToSpeech.QUEUE_FLUSH, null);

            } else if (controlDirection.equals("1")) {
              txtSpeechInput.setText("'" + sentance + "' 를 인식했습니다." + "\n " +
                      "따라서 1.정지 실행 합니다. ");
              tts.speak(" 움직임을 정지 했습니다.  ",TextToSpeech.QUEUE_FLUSH, null);
            } else if (controlDirection.equals("2")) {
              txtSpeechInput.setText("'" + sentance + "' 를 인식했습니다." + "\n " +
                      "따라서 2.폰 내려 놓기를 실행 합니다. ");
              tts.speak("폰을 내려 놓겠습니다. ",TextToSpeech.QUEUE_FLUSH, null);
            } else {
              txtSpeechInput.setText("해당 명령이 존재 하지 않습니다. ");
            }
          }else{
            txtSpeechInput.setText("해당 명령이 존재 하지 않습니다. ");
          }

        }
        break;
      }
      default:
        super.onActivityResult(requestCode, resultCode, data);
        break;
    }
  }

  /*private void registerReceiver(){

    if(mReceiver != null) return;

    final IntentFilter theFilter = new IntentFilter();
    theFilter.addAction(BROADCAST_MESSAGE);

    this.mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id",0);
        int CenterX = intent.getIntExtra("CenterX",0);
        int CenterY = intent.getIntExtra("CenterY",0);
        int Width = intent.getIntExtra("Width",0);
        int Height = intent.getIntExtra("Height",0);
        int EulerY = intent.getIntExtra("EulerY",0);
        int EulerZ = intent.getIntExtra("EulerZ",0);
        int LeftEyeOpen = intent.getIntExtra("LeftEyeOpen",0);
        int RightEyeOpen = intent.getIntExtra("RightEyeOpen",0);
        int Smile = intent.getIntExtra("Smile",0);
        Log.d("test","Width : "+Width);
        Log.d("test","Height :"+Height);
        Log.d("test","EulerY : "+EulerY);
        Log.d("test","EulerZ : "+EulerZ);
        Log.d("test","LeftEyeOpen : "+LeftEyeOpen);
        Log.d("test","RightEyeOpen : "+RightEyeOpen);
        Log.d("test","Smile : "+Smile);
        if(intent.getAction().equals(BROADCAST_MESSAGE)){
          //Toast.makeText(context, "recevied Data : "+receviedData, Toast.LENGTH_SHORT).show();
          cameraDatatString.message.setData(""+id+","+CenterX+","+CenterY+","+Width+","+Height+","+EulerY+","+EulerZ+","
                  +LeftEyeOpen+","+RightEyeOpen+","+Smile);
          cameraDatatString.publish();
        }
      }
    };

    this.registerReceiver(this.mReceiver, theFilter);

  }

  private void unregisterReceiver() {
    if(mReceiver != null){
      this.unregisterReceiver(mReceiver);
      mReceiver = null;
    }

  }*/

  public String judgeCommand( String sentance ){

    String currentControlState = null;
    int i = 0 ;
    for( i = 0 ; i<command0Array.length ; i++ ){
      if( sentance.contains(command0Array[i]) ){
        currentControlState = "0" ;
        break;
      }
    }
    for( i = 0 ; i<command1Array.length ; i++ ){
      if( sentance.contains(command1Array[i]) ){
        currentControlState = "1" ;
        break;
      }
    }
    for( i = 0 ; i<command2Array.length ; i++ ){
      if( sentance.contains(command2Array[i]) ){
        currentControlState = "2" ;
        break;
      }
    }
    return currentControlState ;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //unregisterReceiver();

  }

}