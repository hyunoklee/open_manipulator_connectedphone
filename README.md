# 실행 명령어     
'''bash
roscore
roslaunch open_manipulator_gazebo open_manipulator_gazebo.launch  
roslaunch open_manipulator_controller open_manipulator_controller.launch use_platform:=false  
roslaunch open_manipulator_connectedphone connectedphone.launch use_platform:=false  
roslaunch open_manipulator_connectedphone key_command.launch  
'''

# 실물이 아닌 가제보 시뮬레이션에서 구동하기 위한  명령어   
'''bash
roscore
roslaunch open_manipulator_gazebo open_manipulator_gazebo.launch  
roslaunch open_manipulator_controller open_manipulator_controller.launch use_platform:=false  
roslaunch open_manipulator_connectedphone connectedphone.launch use_platform:=false  
roslaunch open_manipulator_connectedphone key_command.launch  
'''

# 실행 명령어 설명  
1.roscore   
2.그다음에 2개폰에 APP 실행  
   ->  op가 잡고있는 폰은 PubFaceTracker App 실행 후 홈키로 해당 앱 백그라운드로 돌리고 Face Tracker App 실행  
   ->  조종용 폰은 android_pubCommnadVoice App만 실행.   
3.rqt 를 실행시켜 topic monitor로 아래 토픽들이 생겼는지 확인, 데이타는 실제 버튼 누르고 얼굴 트래킹 시작해야 날라 오므로   
  여기서는 토픽 생겼는지만 확인    
    /command_Key       -> 조정하는 폰에서 커맨드 입력해야 데이타 뜸   
    /facedetectdataset -> op가 잡고있는 폰에서 얼굴인식해야 데이타 뜸   
4.roslaunch open_manipulator_controller open_manipulator_controller.launch   
   -> op에 토크걸고 조정 가능한 서비스 만들어주는 프로그램  
5.roslaunch open_manipulator_connectedphone connectedphone.launch open_rviz:=true  
   -> 실제 Face Tracking 프로그램 과 RVIZ실행 시키는 명령어  
6.roslaunch open_manipulator_connectedphone key_command.launch  
   -> 조종용 폰이 동작을 하지 않을때 명령을 키보드 입력으로 내릴수 있게 하는 프로그램   
7. 모든 프로그램들이 실행되고 OP 가 기본자세를 취하고 있으면 폰을 정해진 위치에 위치시킴  
8. 조종용 폰이나  key_command.launch 터미널을 통해 시작명령어를 내림.  
   그러면 OP가 폰 집어 트래킹 시작   


# 만일 구동을 하지 않는다.   
1. 조종용 폰이나 key_command.launch 창에서 시작 버튼이나 명령을 내려 트랙킹이 되는지 확인함.  
2. 그래도 안된다.  rqt 를 실행시켜 topic monitor로 아래 토픽들이 phone으로 부터 날라오는지 확인   
    /command_Key       -> 조정하는 폰에서 커맨드 입력해야 데이타 뜸   
    /facedetectdataset -> op가 잡고있는 폰에서 얼굴인식해야 데이타 뜸   
   날라오지 않으면 폰에 있는 앱들을 홈키 옆에있는 설정키 눌러 완전 종료 시킨후 다시 실행시킴.   
   이때 주의사항이 op가 잡고 있는 폰은 facetracking App 외에도  PubFaceTracker 앱이 실행 후 홈키를 눌러 백그라운드로 돌고 있어야함.  
3. 그래도 동작을 하지 않는다. op가 정상 동작하는지 확인해야 함.  
   두개 터미널에서 아래 두개 명령어 각각 실행   

   roslaunch open_manipulator_controller open_manipulator_controller.launch  
     -> 이때 다이나믹셀을 찾을 수 없다는 빨간 에러 메세지가 나오면 모터에 전원이 잘 들어오고 있는지 확인하거나 ( 전원 스위치 ON 되어있는지 확인 )  
        그래도 안되면 U2D2 로 들어가는 모든 전원을 뺏다 꽂음 ( USB 케이블, 전원 케이블 )       
   roslaunch open_manipulator_control_gui open_manipulator_control_gui.launch   
     -> GUI 화면에서 Time Start -> Home Pose 눌러서 움직이면 op는 정상임. op 정상임이 확인되면 다시 실행 명령어 try 함.   

